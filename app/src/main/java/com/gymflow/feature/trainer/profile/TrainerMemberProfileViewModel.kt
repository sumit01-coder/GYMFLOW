package com.gymflow.feature.trainer.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RoutineDayStatus(
    val dayOfWeek: String,
    val routineTitle: String,
    val isCompleted: Boolean,
    val isToday: Boolean = false,
    val isRestDay: Boolean = false
)

data class WeightHistoryItem(
    val date: String,
    val weightKg: Float,
    val deltaKg: Float
)

data class CoachNoteItem(
    val id: String,
    val timestamp: String,
    val text: String,
    val author: String = "Coach Marcus"
)

data class MemberProfileDetails(
    val memberId: String = "sarah-miller-101",
    val name: String = "Sarah Miller",
    val email: String = "sarah.m@example.com",
    val status: String = "Active",
    val joinedDate: String = "Joined 12 Apr 2025",
    val goal: String = "Build Muscle / Hypertrophy",
    val currentWeightKg: Float = 62.0f,
    val startingWeightKg: Float = 64.5f,
    val heightCm: Float = 168f,
    val age: Int = 26,
    val attendanceRate: Int = 92,
    val workoutsCompletedThisMonth: Int = 14,
    val routineTitle: String = "Hypertrophy Push-Pull-Legs",
    val weeklyDays: List<RoutineDayStatus> = listOf(
        RoutineDayStatus("Mon", "Chest + Triceps", isCompleted = true),
        RoutineDayStatus("Tue", "Back + Biceps", isCompleted = true),
        RoutineDayStatus("Wed", "Legs", isCompleted = true, isToday = true),
        RoutineDayStatus("Thu", "Shoulders & Traps", isCompleted = false),
        RoutineDayStatus("Fri", "Chest + Abs", isCompleted = false),
        RoutineDayStatus("Sat", "Back + Legs", isCompleted = false),
        RoutineDayStatus("Sun", "Rest & Recovery", isCompleted = false, isRestDay = true)
    ),
    val weightHistory: List<WeightHistoryItem> = listOf(
        WeightHistoryItem("Nov 2", 62.0f, -0.2f),
        WeightHistoryItem("Oct 30", 62.2f, -0.2f),
        WeightHistoryItem("Oct 27", 62.4f, -0.4f),
        WeightHistoryItem("Oct 20", 62.8f, -0.3f),
        WeightHistoryItem("Oct 14", 63.1f, -0.4f)
    ),
    val notesHistory: List<CoachNoteItem> = listOf(
        CoachNoteItem(
            id = "note-1",
            timestamp = "2 hours ago",
            text = "Great form on incline bench press today! Next week let's push for 22.5kg."
        ),
        CoachNoteItem(
            id = "note-2",
            timestamp = "3 days ago",
            text = "Keep focusing on controlled 3-second eccentrics on cable flyes."
        )
    )
)

data class TrainerMemberProfileUiState(
    val profile: MemberProfileDetails = MemberProfileDetails(),
    val feedbackInput: String = "",
    val isNoteSentNoticeVisible: Boolean = false
)

class TrainerMemberProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TrainerMemberProfileUiState())
    val uiState: StateFlow<TrainerMemberProfileUiState> = _uiState.asStateFlow()

    fun onFeedbackInputChanged(text: String) {
        _uiState.update { it.copy(feedbackInput = text, isNoteSentNoticeVisible = false) }
    }

    fun applyPresetNote(preset: String) {
        _uiState.update { it.copy(feedbackInput = preset) }
    }

    fun sendFeedbackNote() {
        val noteText = _uiState.value.feedbackInput.trim()
        if (noteText.isBlank()) return

        val newNote = CoachNoteItem(
            id = "note-${System.currentTimeMillis()}",
            timestamp = "Just now",
            text = noteText
        )

        _uiState.update { current ->
            current.copy(
                feedbackInput = "",
                isNoteSentNoticeVisible = true,
                profile = current.profile.copy(
                    notesHistory = listOf(newNote) + current.profile.notesHistory
                )
            )
        }
    }
}
