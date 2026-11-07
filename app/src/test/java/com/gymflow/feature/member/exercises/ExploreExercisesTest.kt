package com.gymflow.feature.member.exercises

import androidx.lifecycle.SavedStateHandle
import com.gymflow.domain.model.Exercise
import com.gymflow.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExploreExercisesTest {

    private class FakeExerciseRepository(
        initialExercises: List<Exercise> = emptyList()
    ) : ExerciseRepository {
        private val list = initialExercises.toMutableList()
        private val flow = MutableStateFlow<List<Exercise>>(list)

        override fun getExercises(): Flow<List<Exercise>> = flow
        override fun getExercisesByCategory(category: String): Flow<List<Exercise>> =
            flow.map { l -> if (category.equals("All", true)) l else l.filter { it.category.equals(category, true) } }
        override fun getExercisesByCategoryAndEquipment(category: String, equipment: String): Flow<List<Exercise>> =
            flow.map { l -> l.filter { (category.equals("All", true) || it.category.equals(category, true)) && (equipment.equals("All", true) || it.equipment.equals(equipment, true)) } }
        override fun searchExercises(query: String): Flow<List<Exercise>> =
            flow.map { l -> l.filter { it.name.contains(query, true) || it.category.contains(query, true) } }
        override fun getExerciseById(id: String): Flow<Exercise?> =
            flow.map { l -> l.find { it.id == id } }
        override suspend fun seedDefaultCatalogIfEmpty() {}
        override suspend fun syncExercisesWithFirestore() {}
        override suspend fun saveCustomExercise(exercise: Exercise) {
            list.add(exercise)
            flow.value = list.toList()
        }
    }

    @Test
    fun `exercise catalog contains exercises across all muscle groups`() {
        val all = ExerciseCatalog.getAllExercises()
        assertTrue("Catalog should contain comprehensive exercises", all.size >= 14)

        val categories = all.map { it.category }.toSet()
        assertTrue("Contains Chest", categories.contains("Chest"))
        assertTrue("Contains Back", categories.contains("Back"))
        assertTrue("Contains Biceps", categories.contains("Biceps"))
        assertTrue("Contains Triceps", categories.contains("Triceps"))
        assertTrue("Contains Shoulders", categories.contains("Shoulders"))
        assertTrue("Contains Legs", categories.contains("Legs"))
        assertTrue("Contains Abs", categories.contains("Abs"))
    }

    @Test
    fun `category filtering returns only exercises for selected muscle`() {
        val chestExercises = ExerciseCatalog.getExercisesByCategory("Chest")
        assertTrue(chestExercises.isNotEmpty())
        assertTrue(chestExercises.all { it.category == "Chest" })

        val backExercises = ExerciseCatalog.getExercisesByCategory("Back")
        assertTrue(backExercises.isNotEmpty())
        assertTrue(backExercises.all { it.category == "Back" })
    }

    @Test
    fun `search filter works with keyword and category`() {
        val searchCurl = ExerciseCatalog.searchExercises("curl", "ALL")
        assertTrue(searchCurl.isNotEmpty())
        assertTrue(searchCurl.all { it.exerciseName.contains("curl", ignoreCase = true) })

        val chestOnlyPress = ExerciseCatalog.searchExercises("press", "Chest")
        assertTrue(chestOnlyPress.isNotEmpty())
        assertTrue(chestOnlyPress.all { it.category == "Chest" && it.exerciseName.contains("press", ignoreCase = true) })
    }

    @Test
    fun `stretching catalog contains 73 comprehensive routines with real gifs`() {
        val stretches = StretchingCatalog.stretches
        assertEquals(73, stretches.size)

        var openSourceGifsCount = 0
        for (stretch in stretches) {
            assertTrue(stretch.name.isNotBlank())
            assertTrue(stretch.instructions.isNotEmpty())
            assertTrue(stretch.breathingCue.isNotBlank())
            assertTrue(stretch.coachTip.isNotBlank())
            assertTrue(stretch.recommendedDurationSeconds > 0)
            if (!stretch.gifUrl.isNullOrBlank()) {
                assertTrue("GIF URL should be a valid web link", stretch.gifUrl.startsWith("http"))
                if (stretch.gifUrl.contains("jsdelivr.net")) {
                    openSourceGifsCount++
                }
            }
        }
        assertTrue("Should contain open-source GIFs from jsDelivr CDN", openSourceGifsCount >= 50)
    }

    @Test
    fun `exercise details resolves matching stretches`() {
        val chestStretches = StretchingCatalog.getStretchesForExercise("Chest Press")
        assertTrue("Chest Press should resolve stretches", chestStretches.isNotEmpty())
        val hasWarmup = chestStretches.any { it.timing == StretchTiming.PRE_WORKOUT_DYNAMIC }
        val hasCooldown = chestStretches.any { it.timing == StretchTiming.POST_WORKOUT_STATIC }
        assertTrue("Should contain warmup stretch", hasWarmup)
        assertTrue("Should contain cooldown stretch", hasCooldown)
    }

    @Test
    fun `explore exercises viewmodel filters categories and queries correctly`() {
        val sampleExercises = listOf(
            Exercise(
                id = "ex_chest_press",
                name = "Barbell Bench Press",
                category = "Chest",
                muscleGroup = "Pectorals",
                equipment = "Barbell"
            ),
            Exercise(
                id = "ex_lat_pulldown",
                name = "Lat Pulldown",
                category = "Back",
                muscleGroup = "Lats",
                equipment = "Cable"
            )
        )
        val fakeRepo = FakeExerciseRepository(sampleExercises)
        val savedStateHandle = SavedStateHandle(mapOf("category" to "CHEST"))
        val viewModel = ExploreExercisesViewModel(
            exerciseRepository = fakeRepo,
            context = null,
            savedStateHandle = savedStateHandle
        )

        // Wait a tick for coroutine collection
        Thread.sleep(150)

        val initialState = viewModel.uiState.value
        assertEquals(MuscleCategory.CHEST, initialState.selectedCategory)
        assertTrue(initialState.exercises.isNotEmpty())
        assertTrue(initialState.exercises.all { it.category == "Chest" })

        // Switch to Stretching
        viewModel.onSelectCategory(MuscleCategory.STRETCHING)
        val stretchState = viewModel.uiState.value
        assertEquals(MuscleCategory.STRETCHING, stretchState.selectedCategory)
        assertEquals(73, stretchState.stretches.size)

        // Search query
        viewModel.onSearchQueryChange("doorway")
        val searchedState = viewModel.uiState.value
        assertEquals(1, searchedState.stretches.size)
        assertEquals("Doorway Pectoral Stretch", searchedState.stretches[0].name)
    }
}
