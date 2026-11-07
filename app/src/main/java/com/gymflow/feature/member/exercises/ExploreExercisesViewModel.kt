package com.gymflow.feature.member.exercises

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.domain.model.Exercise
import com.gymflow.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
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
    val exercises: List<Exercise> = emptyList(),
    val stretches: List<StretchingExercise> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class ExploreExercisesViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    @ApplicationContext private val context: Context? = null,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialCategoryParam = savedStateHandle.get<String>("category") ?: "ALL"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private var allExercisesCache: List<Exercise> = emptyList()

    private val _uiState = MutableStateFlow(
        ExploreExercisesUiState(
            selectedCategory = parseCategory(initialCategoryParam),
            isLoading = true
        )
    )
    val uiState: StateFlow<ExploreExercisesUiState> = _uiState.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Fast path: load asset catalog synchronously on IO so UI has all 1,323+ exercises immediately
            val assetExercises = loadCatalogFromAssets()
            if (assetExercises.isNotEmpty()) {
                allExercisesCache = assetExercises
                filterData()
            }

            // 2. Ensure Room SQLite is seeded with complete catalog
            try {
                exerciseRepository.seedDefaultCatalogIfEmpty()
            } catch (_: Exception) {}

            // 3. Observe Room SQLite flow to get verified local exercises & cloud sync updates
            exerciseRepository.getExercises().collect { dbExercises ->
                if (dbExercises.isNotEmpty()) {
                    allExercisesCache = dbExercises
                    filterData()
                }
            }
        }
    }

    private fun loadCatalogFromAssets(): List<Exercise> {
        val ctx = context ?: return emptyList()
        return try {
            ctx.assets.open("exercises_catalog.json").use { stream ->
                val text = stream.bufferedReader().use { it.readText() }
                json.decodeFromString<List<Exercise>>(text)
            }
        } catch (_: Exception) {
            emptyList()
        }
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
                    totalCount = filtered.size,
                    isLoading = false
                )
            }
        } else {
            val filteredByCategory = when (category) {
                MuscleCategory.ALL -> allExercisesCache
                MuscleCategory.CHEST -> allExercisesCache.filter { it.category.equals("Chest", ignoreCase = true) }
                MuscleCategory.BACK -> allExercisesCache.filter { it.category.equals("Back", ignoreCase = true) }
                MuscleCategory.BICEPS -> allExercisesCache.filter { it.category.equals("Biceps", ignoreCase = true) }
                MuscleCategory.TRICEPS -> allExercisesCache.filter { it.category.equals("Triceps", ignoreCase = true) }
                MuscleCategory.SHOULDERS -> allExercisesCache.filter { it.category.equals("Shoulders", ignoreCase = true) }
                MuscleCategory.LEGS -> allExercisesCache.filter { it.category.equals("Legs", ignoreCase = true) }
                MuscleCategory.ABS -> allExercisesCache.filter {
                    it.category.equals("Abs", ignoreCase = true) || it.category.contains("Core", ignoreCase = true)
                }
                MuscleCategory.STRETCHING -> emptyList()
            }

            val finalExercises = if (query.isBlank()) {
                filteredByCategory
            } else {
                filteredByCategory.filter {
                    it.name.lowercase().contains(query) ||
                            it.muscleGroup.lowercase().contains(query) ||
                            it.equipment.lowercase().contains(query) ||
                            it.category.lowercase().contains(query)
                }
            }

            // In ALL category with query, also check matching stretches
            val matchingStretches = if (category == MuscleCategory.ALL && query.isNotBlank()) {
                StretchingCatalog.stretches.filter {
                    it.name.lowercase().contains(query) || it.targetMuscle.lowercase().contains(query)
                }
            } else emptyList()

            _uiState.update {
                it.copy(
                    exercises = finalExercises,
                    stretches = matchingStretches,
                    totalCount = finalExercises.size + matchingStretches.size,
                    isLoading = false
                )
            }
        }
    }
}
