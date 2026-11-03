package com.gymflow.core.service

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import com.gymflow.domain.calculator.CalorieCalculator
import com.gymflow.domain.calculator.VolumeCalculator
import com.gymflow.domain.model.ExerciseSet
import com.gymflow.domain.model.SetLog
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.domain.model.WorkoutSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

enum class WorkoutEngineState {
    NOT_STARTED,
    RUNNING,
    PAUSED,
    RESTING,
    COMPLETED
}

data class WorkoutStateSnapshot(
    val engineState: WorkoutEngineState = WorkoutEngineState.NOT_STARTED,
    val session: WorkoutSession? = null,
    val exercises: List<WorkoutExercise> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val currentSetIndex: Int = 0,
    val completedLogs: List<SetLog> = emptyList(),
    val elapsedSeconds: Long = 0L,
    val restRemainingSeconds: Int = 0,
    val restTotalSeconds: Int = 0,
    val activeExercise: WorkoutExercise? = null,
    val activeSet: ExerciseSet? = null,
    val totalSetsCount: Int = 0,
    val completedSetsCount: Int = 0,
    val totalVolumeKg: Float = 0f,
    val estimatedCalories: Int = 0
)

@Serializable
private data class PersistedWorkoutState(
    val session: WorkoutSession,
    val exercises: List<WorkoutExercise>,
    val currentExerciseIndex: Int,
    val currentSetIndex: Int,
    val completedLogs: List<SetLog>,
    val engineState: String,
    val startWallTimeEpochMs: Long,
    val accumulatedDurationSeconds: Long,
    val lastActiveTimestampMs: Long,
    val restEndTimeMs: Long = 0L,
    val restTotalSeconds: Int = 0,
    val userWeightKg: Float = 67.8f
)

/**
 * State machine and coordinator for active workouts in GYMFLOW.
 *
 * Tracks live session state, sets, auto-rest countdowns, volume & calorie
 * calculations, and state persistence to recover gracefully from app restarts.
 */
class WorkoutEngine private constructor() {

    companion object {
        private const val PREFS_NAME = "gymflow_workout_engine_prefs"
        private const val KEY_PERSISTED_STATE = "key_persisted_state"

        val instance: WorkoutEngine by lazy { WorkoutEngine() }
    }

    private val engineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: Job? = null
    private var restTickerJob: Job? = null

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private var appContext: Context? = null
    private var sharedPrefs: SharedPreferences? = null

    private val _snapshot = MutableStateFlow(WorkoutStateSnapshot())
    val snapshot: StateFlow<WorkoutStateSnapshot> = _snapshot.asStateFlow()

    // Event emitted when rest timer reaches 0
    private val _restFinishedEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val restFinishedEvent: SharedFlow<Unit> = _restFinishedEvent.asSharedFlow()

    private var sessionStartTimeMs: Long = 0L
    private var accumulatedElapsedSeconds: Long = 0L
    private var lastRunResumeRealtime: Long = 0L
    private var restTargetTimestampRealtime: Long = 0L
    private var userWeightKg: Float = 67.8f

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            sharedPrefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            restoreSavedSessionIfAvailable()
        }
    }

    /**
     * Starts a new workout session with given exercises.
     */
    fun startWorkout(
        session: WorkoutSession,
        exercises: List<WorkoutExercise>,
        userWeight: Float = 67.8f
    ) {
        userWeightKg = userWeight
        sessionStartTimeMs = System.currentTimeMillis()
        accumulatedElapsedSeconds = 0L
        lastRunResumeRealtime = SystemClock.elapsedRealtime()

        val totalSets = exercises.sumOf { it.sets.size.coerceAtLeast(it.targetSets) }
        val initialExercise = exercises.firstOrNull()
        val initialSet = initialExercise?.sets?.firstOrNull()

        _snapshot.value = WorkoutStateSnapshot(
            engineState = WorkoutEngineState.RUNNING,
            session = session.copy(startTimeEpochMs = sessionStartTimeMs),
            exercises = exercises,
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            completedLogs = emptyList(),
            elapsedSeconds = 0L,
            restRemainingSeconds = 0,
            restTotalSeconds = 0,
            activeExercise = initialExercise,
            activeSet = initialSet,
            totalSetsCount = totalSets,
            completedSetsCount = 0,
            totalVolumeKg = 0f,
            estimatedCalories = 0
        )

        startTimerTicker()
        persistState()
    }

    /**
     * Pauses the active workout timer.
     */
    fun pauseWorkout() {
        val current = _snapshot.value
        if (current.engineState != WorkoutEngineState.RUNNING && current.engineState != WorkoutEngineState.RESTING) return

        if (current.engineState == WorkoutEngineState.RUNNING) {
            accumulatedElapsedSeconds += ((SystemClock.elapsedRealtime() - lastRunResumeRealtime) / 1000L).coerceAtLeast(0L)
        }
        stopTimerTicker()
        stopRestTicker()

        _snapshot.value = current.copy(
            engineState = WorkoutEngineState.PAUSED,
            elapsedSeconds = accumulatedElapsedSeconds
        )
        persistState()
    }

    /**
     * Resumes the active workout from pause.
     */
    fun resumeWorkout() {
        val current = _snapshot.value
        if (current.engineState != WorkoutEngineState.PAUSED) return

        lastRunResumeRealtime = SystemClock.elapsedRealtime()
        _snapshot.value = current.copy(engineState = WorkoutEngineState.RUNNING)
        startTimerTicker()
        persistState()
    }

    /**
     * Completes current set, logs metrics, and triggers automatic rest countdown.
     */
    fun completeSet(
        actualReps: Int,
        actualWeightKg: Float,
        customRestSeconds: Int? = null
    ) {
        val current = _snapshot.value
        val exercise = current.activeExercise ?: return
        val currentSetIndex = current.currentSetIndex

        val completedSetLog = SetLog(
            id = UUID.randomUUID().toString(),
            sessionId = current.session?.id ?: UUID.randomUUID().toString(),
            exerciseId = exercise.exerciseId,
            exerciseName = exercise.exerciseName,
            setNumber = currentSetIndex + 1,
            targetReps = current.activeSet?.targetReps ?: exercise.targetReps,
            targetWeightKg = current.activeSet?.targetWeightKg ?: exercise.targetWeightKg,
            actualReps = actualReps,
            actualWeightKg = actualWeightKg,
            completedAtEpochMs = System.currentTimeMillis()
        )

        val updatedLogs = current.completedLogs + completedSetLog
        val updatedVolume = VolumeCalculator.calculateTotalVolumeFromLogs(updatedLogs)
        val currentElapsed = computeCurrentElapsedSeconds()
        val calories = CalorieCalculator.calculateForSession(
            weightKg = userWeightKg,
            durationSeconds = currentElapsed,
            category = CalorieCalculator.WorkoutCategory.RESISTANCE_MODERATE
        )

        // Advance to next set or next exercise
        val setsInExercise = exercise.sets.ifEmpty {
            List(exercise.targetSets) { index ->
                ExerciseSet(
                    setNumber = index + 1,
                    targetReps = exercise.targetReps,
                    targetWeightKg = exercise.targetWeightKg
                )
            }
        }

        var nextExerciseIndex = current.currentExerciseIndex
        var nextSetIndex = currentSetIndex + 1
        var workoutFinished = false

        if (nextSetIndex >= setsInExercise.size) {
            // Exercise completed, advance exercise
            nextExerciseIndex++
            nextSetIndex = 0
            if (nextExerciseIndex >= current.exercises.size) {
                workoutFinished = true
            }
        }

        val nextExercise = current.exercises.getOrNull(nextExerciseIndex)
        val nextSet = nextExercise?.sets?.getOrNull(nextSetIndex) ?: nextExercise?.let {
            ExerciseSet(
                setNumber = nextSetIndex + 1,
                targetReps = it.targetReps,
                targetWeightKg = it.targetWeightKg
            )
        }

        if (workoutFinished) {
            finishWorkout()
            return
        }

        val restDuration = customRestSeconds ?: exercise.restDurationSeconds.coerceAtLeast(30)

        _snapshot.value = current.copy(
            completedLogs = updatedLogs,
            currentExerciseIndex = nextExerciseIndex,
            currentSetIndex = nextSetIndex,
            activeExercise = nextExercise,
            activeSet = nextSet,
            completedSetsCount = updatedLogs.size,
            totalVolumeKg = updatedVolume,
            estimatedCalories = calories
        )

        // Trigger automatic rest countdown
        startRest(restDuration)
    }

    /**
     * Starts rest countdown timer.
     */
    fun startRest(durationSeconds: Int) {
        val current = _snapshot.value
        if (current.engineState == WorkoutEngineState.COMPLETED) return

        stopRestTicker()
        val safeDuration = durationSeconds.coerceAtLeast(5)
        restTargetTimestampRealtime = SystemClock.elapsedRealtime() + (safeDuration * 1000L)

        _snapshot.value = current.copy(
            engineState = WorkoutEngineState.RESTING,
            restRemainingSeconds = safeDuration,
            restTotalSeconds = safeDuration
        )
        persistState()

        restTickerJob = engineScope.launch {
            while (isActive) {
                val remainingMs = restTargetTimestampRealtime - SystemClock.elapsedRealtime()
                val remainingSec = (remainingMs / 1000L).coerceAtLeast(0L).toInt()

                if (remainingSec <= 0) {
                    _snapshot.value = _snapshot.value.copy(
                        engineState = WorkoutEngineState.RUNNING,
                        restRemainingSeconds = 0
                    )
                    _restFinishedEvent.emit(Unit)
                    persistState()
                    break
                } else {
                    _snapshot.value = _snapshot.value.copy(restRemainingSeconds = remainingSec)
                }
                delay(500)
            }
        }
    }

    /**
     * Skips rest countdown and returns directly to active RUNNING state.
     */
    fun skipRest() {
        stopRestTicker()
        val current = _snapshot.value
        _snapshot.value = current.copy(
            engineState = WorkoutEngineState.RUNNING,
            restRemainingSeconds = 0
        )
        persistState()
    }

    /**
     * Adds extra rest seconds to active countdown.
     */
    fun addRestSeconds(seconds: Int = 15) {
        if (_snapshot.value.engineState != WorkoutEngineState.RESTING) return
        restTargetTimestampRealtime += (seconds * 1000L)
        val remaining = ((restTargetTimestampRealtime - SystemClock.elapsedRealtime()) / 1000L).toInt()
        val newTotal = _snapshot.value.restTotalSeconds + seconds
        _snapshot.value = _snapshot.value.copy(
            restRemainingSeconds = remaining.coerceAtLeast(0),
            restTotalSeconds = newTotal
        )
        persistState()
    }

    /**
     * Finalizes and completes the workout session.
     */
    fun finishWorkout(): WorkoutSession? {
        val current = _snapshot.value
        val session = current.session ?: return null

        stopTimerTicker()
        stopRestTicker()

        val finalDuration = computeCurrentElapsedSeconds()
        val finalVolume = VolumeCalculator.calculateTotalVolumeFromLogs(current.completedLogs)
        val finalCalories = CalorieCalculator.calculateForSession(
            weightKg = userWeightKg,
            durationSeconds = finalDuration,
            category = CalorieCalculator.WorkoutCategory.RESISTANCE_MODERATE
        )

        val completedSession = session.copy(
            endTimeEpochMs = System.currentTimeMillis(),
            durationSeconds = finalDuration,
            totalVolumeKg = finalVolume,
            caloriesBurned = finalCalories,
            isCompleted = true,
            setsCompleted = current.completedLogs.size,
            totalSets = current.totalSetsCount
        )

        _snapshot.value = current.copy(
            engineState = WorkoutEngineState.COMPLETED,
            session = completedSession,
            elapsedSeconds = finalDuration,
            restRemainingSeconds = 0,
            totalVolumeKg = finalVolume,
            estimatedCalories = finalCalories
        )

        clearSavedSession()
        return completedSession
    }

    /**
     * Cancels active workout session and cleans up state.
     */
    fun cancelWorkout() {
        stopTimerTicker()
        stopRestTicker()
        clearSavedSession()
        _snapshot.value = WorkoutStateSnapshot()
    }

    private fun startTimerTicker() {
        stopTimerTicker()
        tickerJob = engineScope.launch {
            while (isActive) {
                delay(1000)
                val elapsed = computeCurrentElapsedSeconds()
                val calories = CalorieCalculator.calculateForSession(
                    weightKg = userWeightKg,
                    durationSeconds = elapsed,
                    category = CalorieCalculator.WorkoutCategory.RESISTANCE_MODERATE
                )
                _snapshot.value = _snapshot.value.copy(
                    elapsedSeconds = elapsed,
                    estimatedCalories = calories
                )
            }
        }
    }

    private fun stopTimerTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun stopRestTicker() {
        restTickerJob?.cancel()
        restTickerJob = null
    }

    private fun computeCurrentElapsedSeconds(): Long {
        val current = _snapshot.value
        return if (current.engineState == WorkoutEngineState.RUNNING || current.engineState == WorkoutEngineState.RESTING) {
            val delta = (SystemClock.elapsedRealtime() - lastRunResumeRealtime) / 1000L
            accumulatedElapsedSeconds + delta.coerceAtLeast(0L)
        } else {
            accumulatedElapsedSeconds
        }
    }

    private fun persistState() {
        val current = _snapshot.value
        val session = current.session ?: return
        if (current.engineState == WorkoutEngineState.NOT_STARTED || current.engineState == WorkoutEngineState.COMPLETED) {
            clearSavedSession()
            return
        }

        try {
            val persisted = PersistedWorkoutState(
                session = session,
                exercises = current.exercises,
                currentExerciseIndex = current.currentExerciseIndex,
                currentSetIndex = current.currentSetIndex,
                completedLogs = current.completedLogs,
                engineState = current.engineState.name,
                startWallTimeEpochMs = sessionStartTimeMs,
                accumulatedDurationSeconds = computeCurrentElapsedSeconds(),
                lastActiveTimestampMs = System.currentTimeMillis(),
                restEndTimeMs = if (current.engineState == WorkoutEngineState.RESTING) System.currentTimeMillis() + (current.restRemainingSeconds * 1000L) else 0L,
                restTotalSeconds = current.restTotalSeconds,
                userWeightKg = userWeightKg
            )
            sharedPrefs?.edit()?.putString(KEY_PERSISTED_STATE, json.encodeToString(persisted))?.apply()
        } catch (e: Exception) {
            // Ignore serialization exceptions in background
        }
    }

    private fun restoreSavedSessionIfAvailable() {
        val rawJson = sharedPrefs?.getString(KEY_PERSISTED_STATE, null) ?: return
        try {
            val persisted = json.decodeFromString<PersistedWorkoutState>(rawJson)
            userWeightKg = persisted.userWeightKg
            sessionStartTimeMs = persisted.startWallTimeEpochMs

            val timeSinceLastMs = (System.currentTimeMillis() - persisted.lastActiveTimestampMs).coerceAtLeast(0L)
            accumulatedElapsedSeconds = persisted.accumulatedDurationSeconds

            val restoredState = when (persisted.engineState) {
                WorkoutEngineState.PAUSED.name -> WorkoutEngineState.PAUSED
                WorkoutEngineState.RESTING.name -> {
                    val remainingRestMs = persisted.restEndTimeMs - System.currentTimeMillis()
                    if (remainingRestMs > 0) WorkoutEngineState.RESTING else WorkoutEngineState.RUNNING
                }
                else -> WorkoutEngineState.RUNNING
            }

            lastRunResumeRealtime = SystemClock.elapsedRealtime()

            val activeExercise = persisted.exercises.getOrNull(persisted.currentExerciseIndex)
            val activeSet = activeExercise?.sets?.getOrNull(persisted.currentSetIndex)
            val totalSets = persisted.exercises.sumOf { it.sets.size.coerceAtLeast(it.targetSets) }
            val volume = VolumeCalculator.calculateTotalVolumeFromLogs(persisted.completedLogs)
            val calories = CalorieCalculator.calculateForSession(userWeightKg, accumulatedElapsedSeconds)

            _snapshot.value = WorkoutStateSnapshot(
                engineState = restoredState,
                session = persisted.session,
                exercises = persisted.exercises,
                currentExerciseIndex = persisted.currentExerciseIndex,
                currentSetIndex = persisted.currentSetIndex,
                completedLogs = persisted.completedLogs,
                elapsedSeconds = accumulatedElapsedSeconds,
                restRemainingSeconds = if (restoredState == WorkoutEngineState.RESTING) ((persisted.restEndTimeMs - System.currentTimeMillis()) / 1000L).toInt() else 0,
                restTotalSeconds = persisted.restTotalSeconds,
                activeExercise = activeExercise,
                activeSet = activeSet,
                totalSetsCount = totalSets,
                completedSetsCount = persisted.completedLogs.size,
                totalVolumeKg = volume,
                estimatedCalories = calories
            )

            if (restoredState == WorkoutEngineState.RUNNING) {
                startTimerTicker()
            } else if (restoredState == WorkoutEngineState.RESTING) {
                val remainingSec = ((persisted.restEndTimeMs - System.currentTimeMillis()) / 1000L).toInt()
                startRest(remainingSec)
            }
        } catch (e: Exception) {
            clearSavedSession()
        }
    }

    private fun clearSavedSession() {
        sharedPrefs?.edit()?.remove(KEY_PERSISTED_STATE)?.apply()
    }
}
