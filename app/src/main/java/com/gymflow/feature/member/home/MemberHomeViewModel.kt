package com.gymflow.feature.member.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.domain.model.ExerciseSet
import com.gymflow.domain.model.WorkoutDay
import com.gymflow.domain.model.WorkoutExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberHomeUiState(
    val memberName: String = "Alex",
    val greeting: String = "Good morning, Alex!",
    val motivationalSubtext: String = "Ready to achieve your goals?",
    val currentWeightKg: String = "67.8",
    val caloriesToday: String = "320",
    val exercisesProgress: String = "4/5",
    val workoutDurationMinutes: String = "48",
    val todayWorkoutTitle: String = "Chest Day",
    val todayExercises: List<WorkoutExercise> = emptyList(),
    val hasUnreadNotifications: Boolean = true,
    val isLoading: Boolean = false
)

@HiltViewModel
class MemberHomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MemberHomeUiState())
    val uiState: StateFlow<MemberHomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        val sampleExercises = listOf(
            WorkoutExercise(
                exerciseId = "ex_chest_press",
                exerciseName = "Chest Press",
                category = "Chest",
                targetSets = 3,
                targetReps = 12,
                targetWeightKg = 20f,
                restDurationSeconds = 45,
                sets = listOf(
                    ExerciseSet(1, 12, 20f, actualReps = 12, actualWeightKg = 20f, isCompleted = true),
                    ExerciseSet(2, 12, 20f, actualReps = 12, actualWeightKg = 20f, isCompleted = true),
                    ExerciseSet(3, 12, 20f, actualReps = null, actualWeightKg = null, isCompleted = false)
                )
            ),
            WorkoutExercise(
                exerciseId = "ex_cable_fly",
                exerciseName = "Cable Fly",
                category = "Chest",
                targetSets = 3,
                targetReps = 12,
                targetWeightKg = 10f,
                restDurationSeconds = 45,
                sets = listOf(
                    ExerciseSet(1, 12, 10f, isCompleted = false),
                    ExerciseSet(2, 12, 10f, isCompleted = false),
                    ExerciseSet(3, 12, 10f, isCompleted = false)
                )
            )
        )

        _uiState.update {
            it.copy(
                todayExercises = sampleExercises
            )
        }
    }
}
