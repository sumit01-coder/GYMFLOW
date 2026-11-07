package com.gymflow.feature.member.exercises

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MuscleCategory(val displayName: String) {
    ALL("All"),
    CHEST("Chest"),
    BACK("Back"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    SHOULDERS("Shoulders"),
    LEGS("Legs"),
    ABS("Abs & Core"),
    STRETCHING("Stretching")
}

data class ExploreExercisesUiState(
    val searchQuery: String = "",
    val selectedCategory: MuscleCategory = MuscleCategory.ALL,
    val exercises: List<ExerciseCatalog.ExerciseDetailData> = emptyList(),
    val stretches: List<StretchingExercise> = emptyList(),
    val totalCount: Int = 0
)

@HiltViewModel
class ExploreExercisesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialCategoryParam = savedStateHandle.get<String>("category") ?: "ALL"

    private val _uiState = MutableStateFlow(
        ExploreExercisesUiState(
            selectedCategory = parseCategory(initialCategoryParam)
        )
    )
    val uiState: StateFlow<ExploreExercisesUiState> = _uiState.asStateFlow()

    init {
        filterData()
    }

    private fun parseCategory(name: String): MuscleCategory {
        return MuscleCategory.values().find {
            it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true)
        } ?: MuscleCategory.ALL
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterData()
    }

    fun onSelectCategory(category: MuscleCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        filterData()
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        filterData()
    }

    private fun filterData() {
        val query = _uiState.value.searchQuery.trim().lowercase()
        val category = _uiState.value.selectedCategory

        if (category == MuscleCategory.STRETCHING) {
            val allStretches = StretchingCatalog.stretches
            val filtered = if (query.isBlank()) {
                allStretches
            } else {
                allStretches.filter {
                    it.name.lowercase().contains(query) ||
                            it.targetMuscle.lowercase().contains(query) ||
                            it.muscleGroup.lowercase().contains(query) ||
                            it.timing.label.lowercase().contains(query)
                }
            }
            _uiState.update {
                it.copy(
                    exercises = emptyList(),
                    stretches = filtered,
                    totalCount = filtered.size
                )
            }
        } else {
            val categoryFilter = if (category == MuscleCategory.ALL) "ALL" else category.displayName
            val filteredExercises = ExerciseCatalog.searchExercises(query, categoryFilter)

            // If query also matches stretches and in ALL category, find relevant stretches
            val matchingStretches = if (category == MuscleCategory.ALL && query.isNotBlank()) {
                StretchingCatalog.stretches.filter {
                    it.name.lowercase().contains(query) || it.targetMuscle.lowercase().contains(query)
                }
            } else emptyList()

            _uiState.update {
                it.copy(
                    exercises = filteredExercises,
                    stretches = matchingStretches,
                    totalCount = filteredExercises.size + matchingStretches.size
                )
            }
        }
    }
}

