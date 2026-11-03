package com.gymflow.feature.member.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class ExerciseTab {
    INSTRUCTIONS,
    MUSCLE_INFO
}

data class ExerciseDetailsUiState(
    val exerciseId: String = "ex_chest_press",
    val exerciseName: String = "Chest Press",
    val metadataSubhead: String = "3 sets · 12 reps · 20 kg",
    val selectedTab: ExerciseTab = ExerciseTab.INSTRUCTIONS,
    val instructions: List<String> = listOf(
        "Keep your back flat on the bench.",
        "Grip the bar slightly wider than shoulder width.",
        "Lower the bar to your chest.",
        "Push back up and repeat."
    ),
    val primaryMuscle: String = "Pectoralis Major (Middle & Lower Chest)",
    val secondaryMuscles: List<String> = listOf("Triceps Brachii", "Anterior Deltoid (Shoulders)"),
    val equipmentRequired: String = "Olympic Barbell & Flat Bench",
    val coachTip: String = "Retract your scapula and keep your feet planted firmly on the floor for maximum pressing stability."
)

@HiltViewModel
class ExerciseDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val exerciseId: String = savedStateHandle.get<String>("exerciseId") ?: "ex_chest_press"

    private val _uiState = MutableStateFlow(
        ExerciseDetailsUiState(exerciseId = exerciseId)
    )
    val uiState: StateFlow<ExerciseDetailsUiState> = _uiState.asStateFlow()

    fun onSelectTab(tab: ExerciseTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
