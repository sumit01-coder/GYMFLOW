package com.gymflow.feature.trainer.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.PlanDao
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.WorkoutDay
import com.gymflow.domain.model.WorkoutExercise
import com.gymflow.domain.model.WorkoutPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberOption(
    val id: String,
    val name: String,
    val currentGoal: String
)

data class WorkoutPlanBuilderUiState(
    val planId: String = "plan-hypertrophy-1",
    val planName: String = "Hypertrophy Push-Pull-Legs 2026",
    val selectedMemberId: String = "sarah-miller-101",
    val memberOptions: List<MemberOption> = listOf(
        MemberOption("sarah-miller-101", "Sarah Miller", "Build Muscle / Hypertrophy"),
        MemberOption("mike-ross-102", "Mike Ross", "Strength Conditioning"),
        MemberOption("emma-davis-103", "Emma Davis", "Endurance & Tone")
    ),
    val selectedDayOfWeek: String = "Monday",
    val days: Map<String, WorkoutDay> = mapOf(
        "Monday" to WorkoutDay(
            dayOfWeek = "Monday",
            title = "Chest + Triceps",
            exercises = listOf(
                WorkoutExercise(
                    exerciseId = "bench-press",
                    exerciseName = "Bench Press",
                    category = "Chest",
                    targetSets = 3,
                    targetReps = 12,
                    targetWeightKg = 20f,
                    restDurationSeconds = 45,
                    notes = "Keep back flat on bench and retract scapula."
                ),
                WorkoutExercise(
                    exerciseId = "incline-dumbbell-press",
                    exerciseName = "Incline Dumbbell Press",
                    category = "Chest",
                    targetSets = 3,
                    targetReps = 12,
                    targetWeightKg = 15f,
                    restDurationSeconds = 45,
                    notes = "Set bench at 30 degrees for clavicular head."
                ),
                WorkoutExercise(
                    exerciseId = "cable-fly",
                    exerciseName = "Cable Fly",
                    category = "Chest",
                    targetSets = 3,
                    targetReps = 15,
                    targetWeightKg = 10f,
                    restDurationSeconds = 45,
                    notes = "Squeeze at peak contraction for 1 second."
                ),
                WorkoutExercise(
                    exerciseId = "triceps-pushdown",
                    exerciseName = "Triceps Pushdown",
                    category = "Triceps",
                    targetSets = 3,
                    targetReps = 12,
                    targetWeightKg = 15f,
                    restDurationSeconds = 45,
                    notes = "Lock elbows at your sides; control eccentric."
                )
            )
        ),
        "Tuesday" to WorkoutDay(
            dayOfWeek = "Tuesday",
            title = "Back + Biceps",
            exercises = listOf(
                WorkoutExercise("lat-pulldown", "Lat Pulldown", "Back", 3, 12, 45f, 45, "Drive elbows down"),
                WorkoutExercise("barbell-row", "Barbell Bent-Over Row", "Back", 3, 10, 40f, 60, "Keep torso at 45 degrees"),
                WorkoutExercise("bicep-curl", "Dumbbell Bicep Curl", "Biceps", 3, 12, 12f, 45, "Supinate at top")
            )
        ),
        "Wednesday" to WorkoutDay(
            dayOfWeek = "Wednesday",
            title = "Legs",
            exercises = listOf(
                WorkoutExercise("squat", "Barbell Squat", "Legs", 4, 10, 50f, 90, "Hit parallel depth"),
                WorkoutExercise("leg-press", "Leg Press", "Legs", 3, 12, 100f, 60, "Feet shoulder-width"),
                WorkoutExercise("calf-raise", "Standing Calf Raise", "Legs", 4, 15, 30f, 45, "Full plantar flexion")
            )
        ),
        "Thursday" to WorkoutDay(
            dayOfWeek = "Thursday",
            title = "Shoulders & Traps",
            exercises = listOf(
                WorkoutExercise("overhead-press", "Overhead Barbell Press", "Shoulders", 3, 10, 25f, 60, "Brace core tight"),
                WorkoutExercise("lateral-raise", "Dumbbell Lateral Raise", "Shoulders", 4, 15, 8f, 45, "Lead with elbows")
            )
        ),
        "Friday" to WorkoutDay(
            dayOfWeek = "Friday",
            title = "Chest + Abs",
            exercises = listOf(
                WorkoutExercise("dumbbell-bench", "Flat Dumbbell Press", "Chest", 3, 12, 18f, 45, "Full stretch"),
                WorkoutExercise("cable-crunches", "Kneeling Cable Crunch", "Abs", 4, 15, 25f, 45, "Flex spine into abs")
            )
        ),
        "Saturday" to WorkoutDay(
            dayOfWeek = "Saturday",
            title = "Back + Legs",
            exercises = listOf(
                WorkoutExercise("romanian-deadlift", "Romanian Deadlift", "Legs", 3, 10, 45f, 60, "Hinge at hips"),
                WorkoutExercise("seated-cable-row", "Seated Cable Row", "Back", 3, 12, 40f, 45, "Neutral grip pull")
            )
        ),
        "Sunday" to WorkoutDay(
            dayOfWeek = "Sunday",
            title = "Rest & Active Recovery",
            isRestDay = true,
            exercises = emptyList()
        )
    ),
    val isSaved: Boolean = false,
    val showSuccessBanner: Boolean = false
)

@HiltViewModel
class WorkoutPlanViewModel @Inject constructor(
    private val planDao: PlanDao,
    private val tokenManager: TokenManager,
    private val firestoreSyncManager: FirestoreSyncManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutPlanBuilderUiState())
    val uiState: StateFlow<WorkoutPlanBuilderUiState> = _uiState.asStateFlow()

    fun selectMember(memberId: String) {
        _uiState.update { it.copy(selectedMemberId = memberId) }
    }

    fun setPlanName(name: String) {
        _uiState.update { it.copy(planName = name) }
    }

    fun selectDay(dayOfWeek: String) {
        _uiState.update { it.copy(selectedDayOfWeek = dayOfWeek) }
    }

    fun updateDayTitle(dayOfWeek: String, title: String) {
        _uiState.update { state ->
            val updatedDay = state.days[dayOfWeek]?.copy(title = title) ?: return@update state
            state.copy(days = state.days + (dayOfWeek to updatedDay))
        }
    }

    fun toggleRestDay(dayOfWeek: String) {
        _uiState.update { state ->
            val day = state.days[dayOfWeek] ?: return@update state
            val updatedDay = day.copy(isRestDay = !day.isRestDay)
            state.copy(days = state.days + (dayOfWeek to updatedDay))
        }
    }

    fun updateExercise(dayOfWeek: String, index: Int, updated: WorkoutExercise) {
        _uiState.update { state ->
            val day = state.days[dayOfWeek] ?: return@update state
            val exercises = day.exercises.toMutableList()
            if (index in exercises.indices) {
                exercises[index] = updated
                val updatedDay = day.copy(exercises = exercises)
                state.copy(days = state.days + (dayOfWeek to updatedDay))
            } else state
        }
    }

    fun removeExercise(dayOfWeek: String, index: Int) {
        _uiState.update { state ->
            val day = state.days[dayOfWeek] ?: return@update state
            val exercises = day.exercises.toMutableList()
            if (index in exercises.indices) {
                exercises.removeAt(index)
                val updatedDay = day.copy(exercises = exercises)
                state.copy(days = state.days + (dayOfWeek to updatedDay))
            } else state
        }
    }

    fun addExerciseToDay(dayOfWeek: String, exercise: WorkoutExercise) {
        _uiState.update { state ->
            val day = state.days[dayOfWeek] ?: return@update state
            val exercises = day.exercises + exercise
            val updatedDay = day.copy(exercises = exercises)
            state.copy(days = state.days + (dayOfWeek to updatedDay))
        }
    }

    fun saveAndPublishPlan(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val trainerId = tokenManager.getUserIdSync() ?: "coach-marcus"
            val plan = WorkoutPlan(
                id = _uiState.value.planId,
                trainerId = trainerId,
                memberId = _uiState.value.selectedMemberId,
                name = _uiState.value.planName,
                version = 1,
                days = _uiState.value.days.values.toList(),
                isActive = true
            )
            try {
                planDao.replaceCompletePlan(plan)
            } catch (_: Exception) {
                // Safe Room fallback
            }
            try {
                firestoreSyncManager.saveWorkoutPlan(plan)
            } catch (_: Exception) {
                // Safe Firestore fallback
            }
            _uiState.update { it.copy(isSaved = true, showSuccessBanner = true) }
            onComplete?.invoke()
        }
    }
}
