package com.gymflow.feature.member.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveWorkoutUiState(
    val workoutTag: String = "Chest Day",
    val exerciseName: String = "Bench Press",
    val currentSet: Int = 2,
    val totalSets: Int = 3,
    val targetReps: Int = 12,
    val targetWeightKg: Float = 20f,
    val actualReps: Int = 12,
    val actualWeightKg: Float = 20f,
    val elapsedSeconds: Long = 155, // 02:35
    val isPaused: Boolean = false,
    val restDurationSeconds: Int = 45
) {
    val progressPercentage: Float
        get() = (currentSet.toFloat() / totalSets.toFloat()).coerceIn(0f, 1f)

    val formattedDuration: String
        get() {
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: String = savedStateHandle.get<String>("exerciseId") ?: "ex_chest_press"

    private val _uiState = MutableStateFlow(buildInitialUiState(exerciseId))
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private fun buildInitialUiState(id: String): ActiveWorkoutUiState {
        val details = com.gymflow.feature.member.exercises.ExerciseCatalog.getExercise(id)
        return ActiveWorkoutUiState(
            workoutTag = details.primaryMuscle.split(" ").firstOrNull()?.replace("(", "") ?: "Workout",
            exerciseName = details.exerciseName,
            currentSet = 1,
            totalSets = 3,
            targetReps = 12,
            targetWeightKg = 20f,
            actualReps = 12,
            actualWeightKg = 20f,
            elapsedSeconds = 0,
            isPaused = false,
            restDurationSeconds = 45
        )
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (!_uiState.value.isPaused) {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
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

    fun completeSet(onNavigateToRest: (nextSetText: String, durationSeconds: Int) -> Unit) {
        val currentState = _uiState.value
        val nextSetNumber = currentState.currentSet + 1

        val nextSetText = if (nextSetNumber <= currentState.totalSets) {
            "Set $nextSetNumber of ${currentState.totalSets} — ${currentState.exerciseName} (${currentState.targetReps} reps · ${currentState.targetWeightKg.toInt()} kg)"
        } else {
            "Workout Complete — ${currentState.workoutTag}"
        }

        if (nextSetNumber <= currentState.totalSets) {
            _uiState.update { it.copy(currentSet = nextSetNumber) }
        }

        onNavigateToRest(nextSetText, currentState.restDurationSeconds)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
