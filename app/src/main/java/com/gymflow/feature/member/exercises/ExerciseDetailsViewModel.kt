package com.gymflow.feature.member.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExerciseTab {
    INSTRUCTIONS,
    MUSCLE_INFO
}

data class ExerciseDetailsUiState(
    val exerciseId: String = "ex_chest_press",
    val exerciseName: String = "Chest Press",
    val metadataSubhead: String = "3 sets · 12 reps · 20 kg",
    val videoTitle: String = "Barbell Bench Press Technique",
    val selectedTab: ExerciseTab = ExerciseTab.INSTRUCTIONS,
    val instructions: List<String> = emptyList(),
    val primaryMuscle: String = "",
    val secondaryMuscles: List<String> = emptyList(),
    val equipmentRequired: String = "",
    val coachTip: String = ""
)

@HiltViewModel
class ExerciseDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val exerciseId: String = savedStateHandle.get<String>("exerciseId") ?: "ex_chest_press"

    private val _uiState = MutableStateFlow(buildInitialUiState(exerciseId))
    val uiState: StateFlow<ExerciseDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            exerciseRepository.getExerciseById(exerciseId).collect { ex ->
                if (ex != null) {
                    _uiState.update { current ->
                        current.copy(
                            exerciseId = ex.id,
                            exerciseName = ex.name,
                            metadataSubhead = "${ex.defaultSets} sets · ${ex.defaultReps} reps · ${ex.defaultWeightKg} kg",
                            videoTitle = "${ex.name} Technique & Form",
                            instructions = if (ex.instructions.isNotEmpty()) ex.instructions else current.instructions,
                            primaryMuscle = "${ex.category} (${ex.muscleGroup})",
                            equipmentRequired = ex.machineName ?: ex.equipment,
                            coachTip = ex.coachTip.ifBlank { current.coachTip }
                        )
                    }
                }
            }
        }
    }

    private fun buildInitialUiState(id: String): ExerciseDetailsUiState {
        val details = ExerciseCatalog.getExercise(id)
        return ExerciseDetailsUiState(
            exerciseId = details.exerciseId,
            exerciseName = details.exerciseName,
            metadataSubhead = details.metadataSubhead,
            videoTitle = details.videoTitle,
            selectedTab = ExerciseTab.INSTRUCTIONS,
            instructions = details.instructions,
            primaryMuscle = details.primaryMuscle,
            secondaryMuscles = details.secondaryMuscles,
            equipmentRequired = details.equipmentRequired,
            coachTip = details.coachTip
        )
    }

    fun onSelectTab(tab: ExerciseTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
