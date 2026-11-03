package com.gymflow.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieCalculatorTest {

    @Test
    fun testMetFormulaCalculation() {
        // MET = 5.0, weight = 70kg, duration = 60 minutes
        // Expected = 5.0 * 3.5 * 70 / 200 * 60 = 367.5 kcal
        val calories = CalorieCalculator.calculate(
            met = 5.0f,
            weightKg = 70.0f,
            durationMinutes = 60.0f
        )
        assertEquals(367.5f, calories, 0.01f)
    }

    @Test
    fun testExerciseCategoryMetLookup() {
        // Resistance light -> 3.5
        assertEquals(3.5f, CalorieCalculator.resolveMet("chest", CalorieCalculator.ExerciseIntensity.LIGHT), 0.01f)
        // Resistance moderate -> 5.0
        assertEquals(5.0f, CalorieCalculator.resolveMet("back", CalorieCalculator.ExerciseIntensity.MODERATE), 0.01f)
        // Resistance vigorous -> 6.0
        assertEquals(6.0f, CalorieCalculator.resolveMet("legs", CalorieCalculator.ExerciseIntensity.VIGOROUS), 0.01f)
        // Cardio -> 7.0
        assertEquals(7.0f, CalorieCalculator.resolveMet("Cardio Treadmill"), 0.01f)
        // HIIT -> 8.5
        assertEquals(8.5f, CalorieCalculator.resolveMet("HIIT Intervals"), 0.01f)
    }

    @Test
    fun testSessionCaloriesCalculation() {
        // 45 minute workout (2700s) at 67.8kg moderate resistance (5.0 MET)
        // 5.0 * 3.5 * 67.8 / 200 * 45 = 266.9 kcal => rounds to 267
        val calories = CalorieCalculator.calculateForSession(
            weightKg = 67.8f,
            durationSeconds = 2700L,
            category = CalorieCalculator.WorkoutCategory.RESISTANCE_MODERATE
        )
        assertEquals(267, calories)
    }

    @Test
    fun testZeroOrNegativeInputs() {
        assertEquals(0f, CalorieCalculator.calculate(5.0f, -70f, 30f), 0.01f)
        assertEquals(0f, CalorieCalculator.calculate(5.0f, 70f, 0f), 0.01f)
        assertEquals(0, CalorieCalculator.calculateForSession(0f, 3600L))
    }
}
