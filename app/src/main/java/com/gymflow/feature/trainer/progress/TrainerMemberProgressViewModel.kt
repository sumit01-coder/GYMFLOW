package com.gymflow.feature.trainer.progress

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ProgressTimeframe {
    D7,
    D30,
    M3,
    M6,
    Y1
}

data class WeightDataPoint(
    val dateLabel: String,
    val weightKg: Float
)

data class WorkoutSessionLog(
    val sessionDate: String,
    val workoutName: String,
    val totalVolumeKg: Float,
    val durationMinutes: Int,
    val coachApprovalStatus: String // Approved, Under Review
)

data class MemberProgressUiState(
    val memberId: String = "sarah-miller-101",
    val memberName: String = "Sarah Miller",
    val selectedTimeframe: ProgressTimeframe = ProgressTimeframe.D30,
    val totalWorkouts: Int = 12,
    val totalSets: Int = 248,
    val totalCalories: Int = 3850,
    val bestBenchPressKg: Float = 30.0f,
    val startingWeightKg: Float = 64.5f,
    val currentWeightKg: Float = 62.0f,
    val weightTrendPoints: List<WeightDataPoint> = listOf(
        WeightDataPoint("Oct 10", 64.2f),
        WeightDataPoint("Oct 17", 63.6f),
        WeightDataPoint("Oct 24", 62.9f),
        WeightDataPoint("Oct 31", 62.3f),
        WeightDataPoint("Nov 03", 62.0f)
    ),
    val recentSessions: List<WorkoutSessionLog> = listOf(
        WorkoutSessionLog("Nov 2, 2026", "Chest + Triceps", 3420f, 48, "Approved by Coach"),
        WorkoutSessionLog("Oct 31, 2026", "Back + Biceps", 4180f, 52, "Approved by Coach"),
        WorkoutSessionLog("Oct 29, 2026", "Leg Day Hypertrophy", 5600f, 58, "Approved by Coach")
    ),
    val coachFeedbackInput: String = "",
    val feedbackSubmittedToast: Boolean = false
)

class TrainerMemberProgressViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MemberProgressUiState())
    val uiState: StateFlow<MemberProgressUiState> = _uiState.asStateFlow()

    fun selectTimeframe(timeframe: ProgressTimeframe) {
        _uiState.update { it.copy(selectedTimeframe = timeframe) }
    }

    fun onCoachFeedbackInputChanged(text: String) {
        _uiState.update { it.copy(coachFeedbackInput = text, feedbackSubmittedToast = false) }
    }

    fun submitFeedback() {
        if (_uiState.value.coachFeedbackInput.isBlank()) return
        _uiState.update {
            it.copy(
                coachFeedbackInput = "",
                feedbackSubmittedToast = true
            )
        }
    }
}
