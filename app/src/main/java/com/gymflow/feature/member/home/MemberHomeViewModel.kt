package com.gymflow.feature.member.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.security.TokenManager
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
    val memberName: String = "Member",
    val greeting: String = "Good morning!",
    val motivationalSubtext: String = "Ready to achieve your goals?",
    val currentWeightKg: String = "67.8",
    val caloriesToday: String = "320",
    val exercisesProgress: String = "4/5",
    val workoutDurationMinutes: String = "48",
    val todayWorkoutTitle: String = "Chest Day",
    val todayExercises: List<WorkoutExercise> = emptyList(),
    val hasUnreadNotifications: Boolean = true,
    val isLoading: Boolean = false,
    val userInitials: String = "GF"
)

@HiltViewModel
class MemberHomeViewModel @Inject constructor(
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemberHomeUiState())
    val uiState: StateFlow<MemberHomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observeCurrentUser()
    }

    private fun extractInitials(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "GF"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            // Check TokenManager cache first
            val cachedName = tokenManager.getName()
            if (!cachedName.isNullOrBlank()) {
                val firstName = cachedName.trim().split(" ").firstOrNull() ?: cachedName
                val initials = extractInitials(cachedName)
                _uiState.update {
                    it.copy(
                        memberName = firstName,
                        greeting = "Good morning, $firstName!",
                        userInitials = initials
                    )
                }
            }

            // Observe dynamic database updates
            userDao.getCurrentUserFlow().collect { userEntity ->
                if (userEntity != null && userEntity.name.isNotBlank()) {
                    val firstName = userEntity.name.trim().split(" ").firstOrNull() ?: userEntity.name
                    val initials = extractInitials(userEntity.name)
                    _uiState.update {
                        it.copy(
                            memberName = firstName,
                            greeting = "Good morning, $firstName!",
                            userInitials = initials,
                            currentWeightKg = String.format("%.1f", userEntity.currentWeightKg)
                        )
                    }
                }
            }
        }
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
                    ExerciseSet(1, 12, 10f, actualReps = 12, actualWeightKg = 10f, isCompleted = true),
                    ExerciseSet(2, 12, 10f, actualReps = 12, actualWeightKg = 10f, isCompleted = true),
                    ExerciseSet(3, 12, 10f, actualReps = null, actualWeightKg = null, isCompleted = false)
                )
            )
        )

        _uiState.update {
            it.copy(
                todayWorkoutTitle = "Chest Day",
                todayExercises = sampleExercises,
                isLoading = false
            )
        }
    }
}
