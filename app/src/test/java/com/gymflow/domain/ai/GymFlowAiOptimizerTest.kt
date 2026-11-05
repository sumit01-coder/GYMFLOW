package com.gymflow.domain.ai

import com.gymflow.domain.model.WorkoutExercise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GymFlowAiOptimizerTest {

    @Test
    fun `test strength goal heuristics`() {
        val suggestions = GymFlowAiOptimizer.optimize(
            age = 25,
            heightCm = 180f,
            weightKg = 85f,
            goal = "Strength",
            experienceLevel = "Intermediate"
        )

        val volumeSuggestion = suggestions.firstOrNull { it.tag == "VOLUME" }
        assertNotNull(volumeSuggestion)
        assertEquals(5, volumeSuggestion?.recommendedSets)
        assertEquals(5, volumeSuggestion?.recommendedReps)
        assertEquals(120, volumeSuggestion?.targetRestSeconds)
        assertTrue(volumeSuggestion!!.explanation.contains("4-5 sets"))
    }

    @Test
    fun `test hypertrophy goal heuristics`() {
        val suggestions = GymFlowAiOptimizer.optimize(
            age = 28,
            heightCm = 175f,
            weightKg = 75f,
            goal = "Hypertrophy",
            experienceLevel = "Intermediate"
        )

        val volumeSuggestion = suggestions.firstOrNull { it.tag == "VOLUME" }
        assertNotNull(volumeSuggestion)
        assertEquals(4, volumeSuggestion?.recommendedSets)
        assertEquals(10, volumeSuggestion?.recommendedReps)
        assertEquals(60, volumeSuggestion?.targetRestSeconds)
        assertTrue(volumeSuggestion!!.explanation.contains("8-12 reps"))
    }

    @Test
    fun `test fat loss goal heuristics`() {
        val suggestions = GymFlowAiOptimizer.optimize(
            age = 30,
            heightCm = 170f,
            weightKg = 80f,
            goal = "Fat Loss",
            experienceLevel = "Intermediate"
        )

        val metabolicSuggestion = suggestions.firstOrNull { it.tag == "METABOLIC" }
        assertNotNull(metabolicSuggestion)
        assertEquals(3, metabolicSuggestion?.recommendedSets)
        assertEquals(15, metabolicSuggestion?.recommendedReps)
        assertEquals(45, metabolicSuggestion?.targetRestSeconds)
        assertTrue(metabolicSuggestion!!.explanation.contains("12-15 reps"))
    }

    @Test
    fun `test age 40 plus joint-friendly adaptations with barbell bench press`() {
        val chestExercises = listOf(
            WorkoutExercise(
                exerciseId = "ex_bench",
                exerciseName = "Flat Barbell Bench Press",
                category = "Chest",
                targetSets = 4,
                targetReps = 8,
                targetWeightKg = 80f,
                restDurationSeconds = 90
            )
        )

        val suggestions = GymFlowAiOptimizer.optimize(
            age = 45,
            heightCm = 178f,
            weightKg = 82f,
            goal = "Hypertrophy",
            experienceLevel = "Advanced",
            exercises = chestExercises
        )

        val jointSuggestion = suggestions.firstOrNull { it.tag == "JOINT_HEALTH" }
        assertNotNull(jointSuggestion)
        assertTrue(jointSuggestion!!.title.contains("Joint Protection"))
        assertNotNull(jointSuggestion.recommendedAlternative)
        assertTrue(jointSuggestion.recommendedAlternative!!.contains("Dumbbell"))
    }

    @Test
    fun `test age 40 plus joint-friendly adaptations with barbell squat`() {
        val legExercises = listOf(
            WorkoutExercise(
                exerciseId = "ex_squat",
                exerciseName = "Barbell Back Squat",
                category = "Legs",
                targetSets = 4,
                targetReps = 8,
                targetWeightKg = 100f,
                restDurationSeconds = 90
            )
        )

        val suggestions = GymFlowAiOptimizer.optimize(
            age = 42,
            heightCm = 180f,
            weightKg = 85f,
            goal = "Strength",
            experienceLevel = "Intermediate",
            exercises = legExercises
        )

        val jointSuggestion = suggestions.firstOrNull { it.tag == "JOINT_HEALTH" }
        assertNotNull(jointSuggestion)
        assertTrue(jointSuggestion!!.title.contains("Spinal Decompression"))
        assertTrue(jointSuggestion.recommendedAlternative!!.contains("Bulgarian Split Squat"))
    }

    @Test
    fun `test beginner experience level adaptation`() {
        val suggestions = GymFlowAiOptimizer.optimize(
            age = 22,
            heightCm = 170f,
            weightKg = 65f,
            goal = "General Fitness",
            experienceLevel = "Beginner"
        )

        val techniqueSuggestion = suggestions.firstOrNull { it.tag == "TECHNIQUE" }
        assertNotNull(techniqueSuggestion)
        assertTrue(techniqueSuggestion!!.title.contains("Beginner"))
        assertEquals(75, techniqueSuggestion.targetRestSeconds)
    }
}

