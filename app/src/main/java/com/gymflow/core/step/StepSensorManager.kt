package com.gymflow.core.step

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.gymflow.core.database.DailyStepsDao
import com.gymflow.core.database.DailyStepsEntity
import com.gymflow.core.database.UserDao
import com.gymflow.core.security.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepSensorManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dailyStepsDao: DailyStepsDao,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : SensorEventListener {

    private val tag = "StepSensorManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("gymflow_steps_prefs", Context.MODE_PRIVATE)

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val _liveSteps = MutableStateFlow(0)
    val liveSteps: StateFlow<Int> = _liveSteps.asStateFlow()

    private val _isSensorAvailable = MutableStateFlow(stepCounterSensor != null || stepDetectorSensor != null)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private var registered = false

    fun startTracking() {
        if (registered) return
        if (stepCounterSensor != null) {
            sensorManager?.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            registered = true
            Log.i(tag, "Registered hardware TYPE_STEP_COUNTER sensor")
        } else if (stepDetectorSensor != null) {
            sensorManager?.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI)
            registered = true
            Log.i(tag, "Registered hardware TYPE_STEP_DETECTOR sensor")
        } else {
            Log.w(tag, "No hardware step sensor available on this device")
        }
    }

    fun stopTracking() {
        if (!registered) return
        sensorManager?.unregisterListener(this)
        registered = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalStepsSinceBoot = event.values[0].toInt()
                handleCumulativeStepCount(totalStepsSinceBoot)
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    handleStepDetected()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getTodayDateString(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun handleCumulativeStepCount(hardwareTotal: Int) {
        val today = getTodayDateString()
        val memberId = tokenManager.getUserIdSync() ?: "default_member"

        val lastRecordedDate = prefs.getString("last_step_date", "")
        var midnightBaseline = prefs.getInt("midnight_baseline_$today", -1)

        if (lastRecordedDate != today || midnightBaseline == -1) {
            // First reading of the day or midnight rollover
            midnightBaseline = hardwareTotal
            prefs.edit()
                .putString("last_step_date", today)
                .putInt("midnight_baseline_$today", midnightBaseline)
                .apply()
        }

        // Handle reboot case where hardware counter resets to a value smaller than baseline
        if (hardwareTotal < midnightBaseline) {
            midnightBaseline = 0
            prefs.edit().putInt("midnight_baseline_$today", 0).apply()
        }

        val todaySteps = (hardwareTotal - midnightBaseline).coerceAtLeast(0)
        _liveSteps.value = todaySteps

        scope.launch {
            persistDailySteps(memberId, today, todaySteps, hardwareTotal)
        }
    }

    private fun handleStepDetected() {
        val today = getTodayDateString()
        val memberId = tokenManager.getUserIdSync() ?: "default_member"

        _liveSteps.value += 1
        val currentSteps = _liveSteps.value

        scope.launch {
            persistDailySteps(memberId, today, currentSteps, currentSteps)
        }
    }

    private suspend fun persistDailySteps(memberId: String, date: String, steps: Int, rawSensor: Int) {
        val existing = dailyStepsDao.getDailyStepsSync(memberId, date)
        val user = userDao.getUserById(memberId) ?: userDao.getCurrentUser()

        val heightCm = if ((user?.heightCm ?: 0f) > 100f) user!!.heightCm else 175f
        val weightKg = if ((user?.currentWeightKg ?: 0f) > 30f) user!!.currentWeightKg else 70f

        // Stride formula: height * 0.414 (in meters)
        val strideMeters = (heightCm * 0.414f) / 100f
        val distanceMeters = steps * strideMeters

        // Approximate calorie burn: ~0.04 kcal per step scaled by body weight
        val calories = (steps * 0.04f * (weightKg / 70f)).toInt()
        val activeMins = (steps / 100) // ~100 steps per active walking minute

        val id = "steps_${memberId}_$date"
        val entity = DailyStepsEntity(
            id = id,
            memberId = memberId,
            date = date,
            stepCount = steps,
            targetGoal = existing?.targetGoal ?: 10000,
            distanceMeters = distanceMeters,
            caloriesKcal = calories,
            activeMinutes = activeMins,
            lastSensorStepCount = rawSensor,
            lastUpdatedEpochMs = System.currentTimeMillis(),
            isSynced = false
        )
        dailyStepsDao.upsertDailySteps(entity)
        try {
            com.gymflow.feature.widget.GymFlowWidgetUpdater.updateStepsWidget(context)
        } catch (_: Exception) {}
    }

    suspend fun getStepsForToday(): DailyStepsEntity? {
        val today = getTodayDateString()
        val memberId = tokenManager.getUserIdSync() ?: "default_member"
        return dailyStepsDao.getDailyStepsSync(memberId, today)
    }
}

