package com.gymflow.feature.member.exercises

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExploreExercisesTest {

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
    fun `stretching catalog contains 17 comprehensive routines`() {
        val stretches = StretchingCatalog.stretches
        assertEquals(17, stretches.size)

        for (stretch in stretches) {
            assertTrue(stretch.name.isNotBlank())
            assertTrue(stretch.instructions.isNotEmpty())
            assertTrue(stretch.breathingCue.isNotBlank())
            assertTrue(stretch.coachTip.isNotBlank())
            assertTrue(stretch.recommendedDurationSeconds > 0)
        }
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
        val savedStateHandle = SavedStateHandle(mapOf("category" to "CHEST"))
        val viewModel = ExploreExercisesViewModel(savedStateHandle)

        val initialState = viewModel.uiState.value
        assertEquals(MuscleCategory.CHEST, initialState.selectedCategory)
        assertTrue(initialState.exercises.isNotEmpty())
        assertTrue(initialState.exercises.all { it.category == "Chest" })

        // Switch to Stretching
        viewModel.onSelectCategory(MuscleCategory.STRETCHING)
        val stretchState = viewModel.uiState.value
        assertEquals(MuscleCategory.STRETCHING, stretchState.selectedCategory)
        assertEquals(17, stretchState.stretches.size)

        // Search query
        viewModel.onSearchQueryChange("doorway")
        val searchedState = viewModel.uiState.value
        assertEquals(1, searchedState.stretches.size)
        assertEquals("Doorway Pectoral Stretch", searchedState.stretches[0].name)
    }
}

