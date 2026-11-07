package com.gymflow.feature.member.exercises

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gymflow.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExerciseTab {
    INSTRUCTIONS,
    MUSCLE_INFO,
    STRETCHES
}

data class ExerciseDetailsUiState(
    val exerciseId: String = "ex_chest_press",
    val exerciseName: String = "Chest Press",
    val metadataSubhead: String = "3 sets · 12 reps · 20 kg",
    val videoTitle: String = "Barbell Bench Press Technique",
    val mediaUrl: String? = null,
    val gifUrl: String? = null,
    val isPlayingGif: Boolean = true,
    val selectedTab: ExerciseTab = ExerciseTab.INSTRUCTIONS,
    val instructions: List<String> = emptyList(),
    val primaryMuscle: String = "",
    val secondaryMuscles: List<String> = emptyList(),
    val equipmentRequired: String = "",
    val coachTip: String = "",
    val relatedStretches: List<StretchingExercise> = emptyList()
)

@HiltViewModel
class ExerciseDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val exerciseId: String = savedStateHandle.get<String>("exerciseId") ?: "ex_chest_press"

    private val _uiState = MutableStateFlow(buildInitialUiState(exerciseId))
    val uiState: StateFlow<ExerciseDetailsUiState> = _uiState.asStateFlow()

    init {
        preloadGif(_uiState.value.gifUrl)
        viewModelScope.launch {
            exerciseRepository.getExerciseById(exerciseId).collect { ex ->
                if (ex != null) {
                    val resolvedGif = ex.gifUrl ?: ExerciseVisuals.getGifUrl(ex.id)
                    preloadGif(resolvedGif)
                    _uiState.update { current ->
                        current.copy(
                            exerciseId = ex.id,
                            exerciseName = ex.name,
                            metadataSubhead = "${ex.defaultSets} sets · ${ex.defaultReps} reps · ${ex.defaultWeightKg} kg",
                            videoTitle = "${ex.name} Technique & Form",
                            mediaUrl = ex.mediaUrl ?: ExerciseVisuals.getImageUrl(ex.id),
                            gifUrl = resolvedGif ?: current.gifUrl,
                            instructions = if (ex.instructions.isNotEmpty()) ex.instructions else current.instructions,
                            primaryMuscle = "${ex.category} (${ex.muscleGroup})",
                            equipmentRequired = ex.machineName ?: ex.equipment,
                            coachTip = ex.coachTip.ifBlank { current.coachTip },
                            relatedStretches = StretchingCatalog.getStretchesForExercise(ex.name + " " + ex.category)
                        )
                    }
                }
            }
        }
    }

    private fun preloadGif(url: String?) {
        if (url.isNullOrBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loader = Coil.imageLoader(context)
                val req = ImageRequest.Builder(context)
                    .data(url)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build()
                loader.enqueue(req)
            } catch (_: Exception) {}
        }
    }

    private fun buildInitialUiState(id: String): ExerciseDetailsUiState {
        val details = ExerciseCatalog.getExercise(id)
        val photoUrl = ExerciseVisuals.getImageUrl(id) ?: ExerciseVisuals.getImageUrl(details.exerciseId)
        val gifUrl = ExerciseVisuals.getGifUrl(id) ?: ExerciseVisuals.getGifUrl(details.exerciseId)
        return ExerciseDetailsUiState(
            exerciseId = id,
            exerciseName = details.exerciseName,
            metadataSubhead = details.metadataSubhead,
            videoTitle = details.videoTitle,
            mediaUrl = photoUrl,
            gifUrl = gifUrl,
            isPlayingGif = false, // Start on static photo so user can tap "VIEW GIF" for instant animated demo
            selectedTab = ExerciseTab.INSTRUCTIONS,
            instructions = details.instructions,
            primaryMuscle = details.primaryMuscle,
            secondaryMuscles = details.secondaryMuscles,
            equipmentRequired = details.equipmentRequired,
            coachTip = details.coachTip,
            relatedStretches = StretchingCatalog.getStretchesForExercise(details.exerciseName + " " + details.primaryMuscle)
        )
    }

    fun onSelectTab(tab: ExerciseTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleMediaMode() {
        _uiState.update { it.copy(isPlayingGif = !it.isPlayingGif) }
    }

    fun setMediaMode(isGif: Boolean) {
        _uiState.update { it.copy(isPlayingGif = isGif) }
    }
}
