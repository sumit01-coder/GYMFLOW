package com.gymflow.feature.member.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.SetLogEntity
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.database.WorkoutSessionEntity
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.feature.member.exercises.ExerciseCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class CompletedSetRecord(
    val setNumber: Int,
    val reps: Int,
    val weightKg: Float,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class ActiveWorkoutUiState(
    val sessionId: String = UUID.randomUUID().toString(),
    val workoutTag: String = "Workout",
    val exerciseId: String = "ex_chest_press",
    val exerciseName: String = "Bench Press",
    val currentSet: Int = 1,
    val totalSets: Int = 3,
    val targetReps: Int = 12,
    val targetWeightKg: Float = 20f,
    val actualReps: Int = 12,
    val actualWeightKg: Float = 20f,
    val elapsedSeconds: Long = 0,
    val isPaused: Boolean = false,
    val restDurationSeconds: Int = 45,
    val completedSets: List<CompletedSetRecord> = emptyList(),
    val isWaitingForNextSet: Boolean = false,
    val isCurrentExerciseCompleted: Boolean = false,
    val isWorkoutComplete: Boolean = false,

    // Sequential routine progression
    val currentExerciseIndex: Int = 0,
    val totalExercisesCount: Int = 1,
    val completedExerciseIds: Set<String> = emptySet(),
    val hasNextExercise: Boolean = false,
    val nextExerciseTitle: String? = null,
    val routineExercises: List<WorkoutExercise> = emptyList()
) {
    val progressPercentage: Float
        get() = (completedSets.size.toFloat() / totalSets.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    val formattedDuration: String
        get() {
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val planDao: PlanDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val tokenManager: TokenManager,
    private val firestoreSyncManager: FirestoreSyncManager
) : ViewModel() {

    private val initialExerciseId: String = savedStateHandle.get<String>("exerciseId") ?: "ex_chest_press"

    private val _uiState = MutableStateFlow(buildInitialUiState(initialExerciseId))
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var isRoutineLoaded = false

    init {
        restoreOrCreateSession()
    }

    private fun restoreOrCreateSession() {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            val startOfDayMs = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val activeSession = workoutSessionDao.getActiveSessionToday(userId, startOfDayMs)

            if (activeSession != null) {
                // Resume existing active session with preserved duration and completed sets
                val pastSetLogs = workoutSessionDao.getSetLogsSync(activeSession.id)
                val completedIdsToday = workoutSessionDao.getCompletedExerciseIdsTodaySync(startOfDayMs).toSet()

                val exerciseLogs = pastSetLogs.filter { it.exerciseId == initialExerciseId }
                val completedRecords = exerciseLogs.map { log ->
                    CompletedSetRecord(
                        setNumber = log.setNumber,
                        reps = log.actualReps,
                        weightKg = log.actualWeightKg
                    )
                }

                val targetSets = _uiState.value.totalSets
                val isExerciseCompleted = completedRecords.size >= targetSets && targetSets > 0
                val nextSetNumber = if (isExerciseCompleted) targetSets else (completedRecords.size + 1)

                _uiState.update { current ->
                    current.copy(
                        sessionId = activeSession.id,
                        elapsedSeconds = activeSession.durationSeconds,
                        isPaused = false,
                        completedSets = completedRecords,
                        currentSet = nextSetNumber,
                        isCurrentExerciseCompleted = isExerciseCompleted,
                        completedExerciseIds = completedIdsToday
                    )
                }
            } else {
                ensureSessionRecordCreated()
            }

            startTimer()
            loadRoutineForToday()
        }
    }

    private fun ensureSessionRecordCreated() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val existing = workoutSessionDao.getSessionById(currentState.sessionId)
            if (existing == null) {
                val userId = tokenManager.getUserIdSync() ?: "default_member"
                val now = System.currentTimeMillis()
                workoutSessionDao.upsertSession(
                    WorkoutSessionEntity(
                        id = currentState.sessionId,
                        memberId = userId,
                        workoutTitle = currentState.workoutTag.ifBlank { "Workout Session" },
                        startTimeEpochMs = now,
                        endTimeEpochMs = null,
                        durationSeconds = 0,
                        totalVolumeKg = 0f,
                        caloriesBurned = 0,
                        isCompleted = false,
                        setsCompleted = 0,
                        totalSets = currentState.totalSets,
                        isSynced = false
                    )
                )
            }
        }
    }

    private fun buildInitialUiState(id: String): ActiveWorkoutUiState {
        val details = ExerciseCatalog.getExercise(id)
        val initialExercise = WorkoutExercise(
            exerciseId = details.exerciseId,
            exerciseName = details.exerciseName,
            category = details.primaryMuscle.split(" ").firstOrNull()?.replace("(", "") ?: "Workout",
            targetSets = 3,
            targetReps = 12,
            targetWeightKg = 20f,
            restDurationSeconds = 45
        )

        return ActiveWorkoutUiState(
            sessionId = UUID.randomUUID().toString(),
            workoutTag = initialExercise.category,
            exerciseId = initialExercise.exerciseId,
            exerciseName = initialExercise.exerciseName,
            currentSet = 1,
            totalSets = initialExercise.targetSets,
            targetReps = initialExercise.targetReps,
            targetWeightKg = initialExercise.targetWeightKg,
            actualReps = initialExercise.targetReps,
            actualWeightKg = initialExercise.targetWeightKg,
            elapsedSeconds = 0,
            isPaused = false,
            restDurationSeconds = initialExercise.restDurationSeconds,
            completedSets = emptyList(),
            isWaitingForNextSet = false,
            isCurrentExerciseCompleted = false,
            isWorkoutComplete = false,
            currentExerciseIndex = 0,
            totalExercisesCount = 1,
            completedExerciseIds = emptySet(),
            hasNextExercise = false,
            nextExerciseTitle = null,
            routineExercises = listOf(initialExercise)
        )
    }

    private fun loadRoutineForToday() {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            planDao.getActivePlanWithDays(userId).collect { planWithDays ->
                if (!isRoutineLoaded && planWithDays != null) {
                    val domainPlan = planWithDays.toDomain()
                    val today = LocalDate.now()
                    val currentDayFull = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)
                    val currentDayShort = today.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)

                    val todayDay = domainPlan.days.find { day ->
                        day.dayOfWeek.startsWith(currentDayShort, ignoreCase = true) ||
                                day.dayOfWeek.equals(currentDayFull, ignoreCase = true)
                    }

                    if (todayDay != null && todayDay.exercises.isNotEmpty()) {
                        isRoutineLoaded = true
                        val exercises = todayDay.exercises
                        val foundIndex = exercises.indexOfFirst { it.exerciseId == initialExerciseId }
                        val targetIndex = if (foundIndex >= 0) foundIndex else 0
                        loadExerciseAt(targetIndex, exercises)
                    }
                }
            }
        }
    }

    private fun loadExerciseAt(index: Int, exercises: List<WorkoutExercise>) {
        if (index !in exercises.indices) return
        val currentEx = exercises[index]
        val hasNext = index < exercises.lastIndex
        val nextTitle = if (hasNext) exercises[index + 1].exerciseName else null

        viewModelScope.launch {
            val pastLogsForThis = workoutSessionDao.getSetLogsSync(_uiState.value.sessionId)
                .filter { it.exerciseId == currentEx.exerciseId }
            val completedRecords = pastLogsForThis.map {
                CompletedSetRecord(
                    setNumber = it.setNumber,
                    reps = it.actualReps,
                    weightKg = it.actualWeightKg
                )
            }
            val targetSets = currentEx.targetSets.coerceAtLeast(1)
            val isExerciseDone = completedRecords.size >= targetSets
            val setNumber = if (isExerciseDone) targetSets else (completedRecords.size + 1)

            _uiState.update { current ->
                current.copy(
                    workoutTag = currentEx.category.ifBlank { "Workout" },
                    exerciseId = currentEx.exerciseId,
                    exerciseName = currentEx.exerciseName,
                    currentSet = setNumber,
                    totalSets = targetSets,
                    targetReps = currentEx.targetReps.coerceAtLeast(1),
                    targetWeightKg = currentEx.targetWeightKg,
                    actualReps = currentEx.targetReps.coerceAtLeast(1),
                    actualWeightKg = currentEx.targetWeightKg,
                    restDurationSeconds = currentEx.restDurationSeconds.coerceAtLeast(30),
                    completedSets = completedRecords,
                    isWaitingForNextSet = false,
                    isCurrentExerciseCompleted = isExerciseDone,
                    isWorkoutComplete = false,
                    currentExerciseIndex = index,
                    totalExercisesCount = exercises.size,
                    hasNextExercise = hasNext,
                    nextExerciseTitle = nextTitle,
                    routineExercises = exercises
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (!_uiState.value.isPaused && !_uiState.value.isWorkoutComplete) {
                    val updatedSeconds = _uiState.value.elapsedSeconds + 1
                    _uiState.update { it.copy(elapsedSeconds = updatedSeconds) }
                    // Keep duration synced periodically so unexpected exit preserves time
                    if (updatedSeconds % 3L == 0L) {
                        val session = workoutSessionDao.getSessionById(_uiState.value.sessionId)
                        if (session != null && !session.isCompleted) {
                            workoutSessionDao.upsertSession(session.copy(durationSeconds = updatedSeconds))
                        }
                    }
                }
            }
        }
    }

    fun togglePause() {
        val willPause = !_uiState.value.isPaused
        _uiState.update { it.copy(isPaused = willPause) }
        viewModelScope.launch {
            val currentState = _uiState.value
            val existing = workoutSessionDao.getSessionById(currentState.sessionId)
            if (existing != null && !existing.isCompleted) {
                workoutSessionDao.upsertSession(
                    existing.copy(durationSeconds = currentState.elapsedSeconds)
                )
            }
        }
    }

    fun incrementReps() {
        _uiState.update { it.copy(actualReps = it.actualReps + 1) }
    }

    fun decrementReps() {
        _uiState.update { it.copy(actualReps = (it.actualReps - 1).coerceAtLeast(1)) }
    }

    fun incrementWeight() {
        _uiState.update { it.copy(actualWeightKg = it.actualWeightKg + 2.5f) }
    }

    fun decrementWeight() {
        _uiState.update { it.copy(actualWeightKg = (it.actualWeightKg - 2.5f).coerceAtLeast(0f)) }
    }

    /**
     * Completes current set and records its actual reps and weight in Room DB.
     * When all sets of current exercise are completed, marks exercise as completed
     * and prompts user to advance to the next exercise.
     */
    fun completeSet(onNavigateToRest: (nextSetText: String, durationSeconds: Int) -> Unit) {
        val currentState = _uiState.value
        if (currentState.isWaitingForNextSet || currentState.isCurrentExerciseCompleted || currentState.isWorkoutComplete) return

        val completedSet = CompletedSetRecord(
            setNumber = currentState.currentSet,
            reps = currentState.actualReps,
            weightKg = currentState.actualWeightKg
        )
        val updatedCompleted = currentState.completedSets + completedSet

        // Persist set log to Room database immediately so completed tick marks update live
        viewModelScope.launch {
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            val existing = workoutSessionDao.getSessionById(currentState.sessionId)
            if (existing == null) {
                val now = System.currentTimeMillis() - (currentState.elapsedSeconds * 1000L)
                workoutSessionDao.upsertSession(
                    WorkoutSessionEntity(
                        id = currentState.sessionId,
                        memberId = userId,
                        workoutTitle = currentState.workoutTag.ifBlank { "Workout Session" },
                        startTimeEpochMs = now,
                        endTimeEpochMs = null,
                        durationSeconds = currentState.elapsedSeconds,
                        totalVolumeKg = 0f,
                        caloriesBurned = 0,
                        isCompleted = false,
                        setsCompleted = 1,
                        totalSets = currentState.totalSets,
                        isSynced = false
                    )
                )
            } else {
                workoutSessionDao.upsertSession(
                    existing.copy(
                        setsCompleted = existing.setsCompleted + 1,
                        durationSeconds = currentState.elapsedSeconds
                    )
                )
            }

            workoutSessionDao.upsertSetLog(
                SetLogEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = currentState.sessionId,
                    exerciseId = currentState.exerciseId,
                    exerciseName = currentState.exerciseName,
                    setNumber = currentState.currentSet,
                    targetReps = currentState.targetReps,
                    targetWeightKg = currentState.targetWeightKg,
                    actualReps = currentState.actualReps,
                    actualWeightKg = currentState.actualWeightKg,
                    completedAtEpochMs = System.currentTimeMillis(),
                    isSynced = false
                )
            )
        }

        val nextSetNumber = currentState.currentSet + 1
        val hasMoreSets = nextSetNumber <= currentState.totalSets

        if (hasMoreSets) {
            val nextSetText = "Set $nextSetNumber of ${currentState.totalSets} — ${currentState.exerciseName} (${currentState.targetReps} reps · ${currentState.targetWeightKg.toInt()} kg)"
            _uiState.update {
                it.copy(
                    completedSets = updatedCompleted,
                    isWaitingForNextSet = true,
                    isCurrentExerciseCompleted = false
                )
            }
            onNavigateToRest(nextSetText, currentState.restDurationSeconds)
        } else {
            // All sets for this exercise are completed!
            val updatedCompletedExerciseIds = currentState.completedExerciseIds + currentState.exerciseId
            val hasNextEx = currentState.hasNextExercise

            val nextSetText = if (hasNextEx) {
                "Exercise Completed! Next: ${currentState.nextExerciseTitle}"
            } else {
                "All Exercises Complete — ${currentState.workoutTag}"
            }

            _uiState.update {
                it.copy(
                    completedSets = updatedCompleted,
                    isWaitingForNextSet = false,
                    isCurrentExerciseCompleted = true,
                    completedExerciseIds = updatedCompletedExerciseIds,
                    isWorkoutComplete = !hasNextEx
                )
            }

            onNavigateToRest(nextSetText, currentState.restDurationSeconds)
        }
    }

    /**
     * Advances to the next set within the current exercise.
     */
    fun startNextSet() {
        val currentState = _uiState.value
        if (!currentState.isWaitingForNextSet) return

        val nextSetNumber = currentState.currentSet + 1
        if (nextSetNumber <= currentState.totalSets) {
            _uiState.update {
                it.copy(
                    currentSet = nextSetNumber,
                    isWaitingForNextSet = false,
                    actualReps = it.targetReps,
                    actualWeightKg = it.targetWeightKg,
                    isPaused = false
                )
            }
        }
    }

    /**
     * User advances to the next exercise in the routine.
     */
    fun openNextExercise() {
        val currentState = _uiState.value
        if (!currentState.hasNextExercise) return

        val nextIndex = currentState.currentExerciseIndex + 1
        if (nextIndex < currentState.routineExercises.size) {
            loadExerciseAt(nextIndex, currentState.routineExercises)
        }
    }

    /**
     * Finalizes the workout session, saves summary in Room DB, and navigates out.
     */
    fun finishWorkout(onCloseWorkout: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val userId = tokenManager.getUserIdSync() ?: "default_member"
            val now = System.currentTimeMillis()
            val duration = currentState.elapsedSeconds.coerceAtLeast(60L)

            val existing = workoutSessionDao.getSessionById(currentState.sessionId)
            val startTime = existing?.startTimeEpochMs ?: (now - (duration * 1000L))

            val sessionEntity = WorkoutSessionEntity(
                id = currentState.sessionId,
                memberId = userId,
                workoutTitle = currentState.workoutTag.ifBlank { "Workout Session" },
                startTimeEpochMs = startTime,
                endTimeEpochMs = now,
                durationSeconds = duration,
                totalVolumeKg = 0f, // Room DAO recalculates volume from SetLogs
                caloriesBurned = ((duration / 60) * 7).toInt().coerceAtLeast(50),
                isCompleted = true,
                setsCompleted = existing?.setsCompleted ?: currentState.completedSets.size,
                totalSets = currentState.totalSets,
                isSynced = false
            )
            workoutSessionDao.upsertSession(sessionEntity)

            val setLogs = workoutSessionDao.getSetLogsSync(currentState.sessionId)
            try {
                firestoreSyncManager.saveWorkoutSessionWithSets(sessionEntity, setLogs)
                workoutSessionDao.markSessionSynced(sessionEntity.id, true)
                setLogs.forEach { log -> workoutSessionDao.markSetLogSynced(log.id, true) }
            } catch (_: Exception) {}

            onCloseWorkout()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        val currentState = _uiState.value
        if (!currentState.isWorkoutComplete) {
            CoroutineScope(Dispatchers.IO).launch {
                val existing = workoutSessionDao.getSessionById(currentState.sessionId)
                if (existing != null && !existing.isCompleted) {
                    workoutSessionDao.upsertSession(
                        existing.copy(durationSeconds = currentState.elapsedSeconds)
                    )
                }
            }
        }
    }
}
