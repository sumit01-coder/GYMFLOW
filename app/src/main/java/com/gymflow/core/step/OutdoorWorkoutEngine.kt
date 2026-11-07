package com.gymflow.core.step

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.gymflow.core.database.OutdoorGpsPointEntity
import com.gymflow.core.database.OutdoorWorkoutDao
import com.gymflow.core.database.OutdoorWorkoutEntity
import com.gymflow.core.database.UserDao
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.security.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class OutdoorTrackingUiState(
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val workoutId: String = "",
    val workoutType: String = "RUN", // "RUN", "WALK", "HIKE"
    val durationSeconds: Long = 0L,
    val distanceMeters: Float = 0f,
    val currentPaceSecondsPerKm: Float = 0f,
    val avgPaceSecondsPerKm: Float = 0f,
    val currentSpeedMps: Float = 0f,
    val stepsCount: Int = 0,
    val cadenceSpm: Int = 0,
    val caloriesBurned: Int = 0,
    val gpsPoints: List<Pair<Double, Double>> = emptyList(),
    val gpsSignalStatus: String = "SEARCHING" // "SEARCHING", "GOOD", "STRONG", "LOST"
) {
    val formattedDuration: String
        get() {
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return "%02d:%02d".format(mins, secs)
        }

    val formattedDistanceKm: String
        get() = "%.2f".format(distanceMeters / 1000f)

    val formattedPace: String
        get() {
            if (currentPaceSecondsPerKm <= 0 || currentPaceSecondsPerKm > 1800f) return "--:--"
            val mins = (currentPaceSecondsPerKm / 60).toInt()
            val secs = (currentPaceSecondsPerKm % 60).toInt()
            return "%d:%02d".format(mins, secs)
        }

    val formattedAvgPace: String
        get() {
            if (avgPaceSecondsPerKm <= 0 || avgPaceSecondsPerKm > 1800f) return "--:--"
            val mins = (avgPaceSecondsPerKm / 60).toInt()
            val secs = (avgPaceSecondsPerKm % 60).toInt()
            return "%d:%02d".format(mins, secs)
        }
}

@Singleton
class OutdoorWorkoutEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val outdoorWorkoutDao: OutdoorWorkoutDao,
    private val userDao: UserDao,
    private val tokenManager: TokenManager,
    private val firestoreSyncManager: FirestoreSyncManager,
    private val stepSensorManager: StepSensorManager
) {
    private val tag = "OutdoorWorkoutEngine"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _state = MutableStateFlow(OutdoorTrackingUiState())
    val state: StateFlow<OutdoorTrackingUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var lastLocation: Location? = null
    private var rawGpsPoints = mutableListOf<OutdoorGpsPointEntity>()
    private var startStepCount = 0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                handleNewLocation(location)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startWorkout(workoutType: String = "RUN") {
        if (_state.value.isTracking) return

        val workoutId = UUID.randomUUID().toString()
        val memberId = tokenManager.getUserIdSync() ?: "default_member"
        rawGpsPoints.clear()
        lastLocation = null
        startStepCount = stepSensorManager.liveSteps.value

        _state.value = OutdoorTrackingUiState(
            isTracking = true,
            isPaused = false,
            workoutId = workoutId,
            workoutType = workoutType,
            durationSeconds = 0L,
            distanceMeters = 0f,
            currentPaceSecondsPerKm = 0f,
            avgPaceSecondsPerKm = 0f,
            currentSpeedMps = 0f,
            stepsCount = 0,
            cadenceSpm = 0,
            caloriesBurned = 0,
            gpsPoints = emptyList(),
            gpsSignalStatus = "SEARCHING"
        )

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(2000L)
            .setMinUpdateDistanceMeters(4f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            Log.i(tag, "Started GPS location updates for workout $workoutId")
        } catch (e: SecurityException) {
            Log.e(tag, "Location permission missing", e)
        }

        startTimer()
        stepSensorManager.startTracking()
    }

    fun pauseWorkout() {
        if (!_state.value.isTracking || _state.value.isPaused) return
        _state.update { it.copy(isPaused = true) }
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    fun resumeWorkout() {
        if (!_state.value.isTracking || !_state.value.isPaused) return
        _state.update { it.copy(isPaused = false) }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(2000L)
            .setMinUpdateDistanceMeters(4f)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (_: Exception) {}
    }

    fun finishWorkout(onFinished: ((OutdoorWorkoutEntity) -> Unit)? = null) {
        if (!_state.value.isTracking) return

        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}

        timerJob?.cancel()
        val current = _state.value
        val memberId = tokenManager.getUserIdSync() ?: "default_member"
        val now = System.currentTimeMillis()
        val startTime = now - (current.durationSeconds * 1000L)

        val polyline = PolylineEncoder.encode(current.gpsPoints)

        val entity = OutdoorWorkoutEntity(
            id = current.workoutId,
            memberId = memberId,
            workoutType = current.workoutType,
            startTimeEpochMs = startTime,
            endTimeEpochMs = now,
            durationSeconds = current.durationSeconds,
            totalSteps = current.stepsCount,
            totalDistanceMeters = current.distanceMeters,
            avgPaceSecondsPerKm = current.avgPaceSecondsPerKm,
            maxSpeedMps = current.currentSpeedMps,
            caloriesBurned = current.caloriesBurned,
            polylineEncoded = polyline,
            isCompleted = true,
            isSynced = false
        )

        scope.launch {
            try {
                outdoorWorkoutDao.upsertOutdoorWorkout(entity)
                outdoorWorkoutDao.insertGpsPoints(rawGpsPoints)
            } catch (e: Exception) {
                Log.w(tag, "Failed to persist outdoor workout locally", e)
            }
        }

        _state.value = OutdoorTrackingUiState()
        onFinished?.invoke(entity)
    }

    private fun handleNewLocation(location: Location) {
        if (_state.value.isPaused || !_state.value.isTracking) return
        if (location.accuracy > 25f) {
            _state.update { it.copy(gpsSignalStatus = "WEAK") }
            return
        }

        val quality = if (location.accuracy <= 10f) "STRONG" else "GOOD"
        val prevLoc = lastLocation
        var addedDistance = 0f

        if (prevLoc != null) {
            val dist = prevLoc.distanceTo(location)
            // Filter noise: must move at least 2.5 meters
            if (dist >= 2.5f) {
                addedDistance = dist
            }
        }
        lastLocation = location

        val currentSteps = (stepSensorManager.liveSteps.value - startStepCount).coerceAtLeast(0)
        val newDistance = _state.value.distanceMeters + addedDistance

        val speed = if (location.hasSpeed() && location.speed > 0f) location.speed else {
            if (addedDistance > 0f) (addedDistance / 3f) else 0f
        }

        val paceSecondsPerKm = if (speed > 0.5f) (1000f / speed) else 0f
        val distanceKm = newDistance / 1000f
        val durationMins = _state.value.durationSeconds / 60f
        val avgPace = if (distanceKm > 0.05f && durationMins > 0f) {
            (_state.value.durationSeconds.toFloat() / distanceKm)
        } else paceSecondsPerKm

        // MET-based calorie formula: Running = 8.5 METs, Walking = 3.8 METs
        val met = if (_state.value.workoutType == "RUN") 8.5f else 3.8f
        val calories = (met * 70f * (durationMins / 60f)).toInt()

        val cadence = if (durationMins > 0.5f) {
            (currentSteps / durationMins).toInt().coerceIn(0, 240)
        } else 0

        val newPoint = Pair(location.latitude, location.longitude)
        val updatedGpsPoints = _state.value.gpsPoints + newPoint

        rawGpsPoints.add(
            OutdoorGpsPointEntity(
                workoutId = _state.value.workoutId,
                timestampEpochMs = System.currentTimeMillis(),
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                accuracy = location.accuracy,
                speed = speed,
                cumulativeSteps = currentSteps
            )
        )

        _state.update {
            it.copy(
                distanceMeters = newDistance,
                currentSpeedMps = speed,
                currentPaceSecondsPerKm = paceSecondsPerKm,
                avgPaceSecondsPerKm = avgPace,
                stepsCount = currentSteps,
                cadenceSpm = cadence,
                caloriesBurned = calories,
                gpsPoints = updatedGpsPoints,
                gpsSignalStatus = quality
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                if (!_state.value.isPaused && _state.value.isTracking) {
                    _state.update { it.copy(durationSeconds = it.durationSeconds + 1) }
                }
            }
        }
    }
}

