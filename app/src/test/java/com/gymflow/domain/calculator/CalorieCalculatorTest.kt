package com.gymflow.domain.calculator

import org.junit.Assert.*
import org.junit.Test

class CalorieCalculatorTest {

    @Test
    fun netKcal_knownValue() =
        assertEquals(280f, CalorieCalculator.netKcal(5f, 70f, 3600f), 0.5f)

    @Test
    fun netKcal_metBelowOne_isZero() =
        assertEquals(0f, CalorieCalculator.netKcal(0.9f, 70f, 3600f), 0f)

    @Test
    fun netKcal_negativeDuration_isZero() =
        assertEquals(0f, CalorieCalculator.netKcal(5f, 70f, -10f), 0f)

    @Test
    fun bodyWeight_prefersLogged() =
        assertEquals(82f, CalorieCalculator.resolveBodyWeightKg(82f, 170, Gender.MALE), 0f)

    @Test
    fun bodyWeight_fromHeight_bmi22() =
        assertEquals(60.6f, CalorieCalculator.resolveBodyWeightKg(null, 166, Gender.MALE), 0.1f)

    @Test
    fun bodyWeight_defaults_male() =
        assertEquals(70f, CalorieCalculator.resolveBodyWeightKg(null, null, Gender.MALE), 0f)

    @Test
    fun bodyWeight_defaults_female() =
        assertEquals(60f, CalorieCalculator.resolveBodyWeightKg(null, null, Gender.FEMALE), 0f)

    @Test
    fun effort_usesPercent1Rm_vigorous() {
        val s = SetInput("Squat", 5, 100f)
        assertEquals(Effort.VIGOROUS, CalorieCalculator.effortFor(s, 120f)) // 83%
    }

    @Test
    fun effort_usesPercent1Rm_light() {
        val s = SetInput("Squat", 5, 100f)
        assertEquals(Effort.LIGHT, CalorieCalculator.effortFor(s, 220f)) // 45%
    }

    @Test
    fun effort_unknown_isModerate() =
        assertEquals(Effort.MODERATE, CalorieCalculator.effortFor(SetInput("X", 10, 20f), null))

    @Test
    fun heavySingle_notClassifiedLight() {
        val s = SetInput("Deadlift", 1, 140f)
        assertEquals(Effort.VIGOROUS, CalorieCalculator.effortFor(s, 150f)) // 93%
    }

    @Test
    fun session_pauseIsCapped() {
        // 3 sets of 10 curls → estimated ~240 s active; session 3 hours (paused)
        val sets = List(3) { SetInput("Curl", 10, 5f) }
        val r = CalorieCalculator.estimateSession(sets, 70f, 3 * 3600L, emptyMap())
        assertTrue("Expected < 60 kcal, got ${r.totalKcal}", r.totalKcal < 60)
    }

    @Test
    fun session_allocationSumsToTotal() {
        val sets = listOf(
            SetInput("A", 10, 20f),
            SetInput("B", 5, 60f),
            SetInput("B", 5, 60f)
        )
        val r = CalorieCalculator.estimateSession(sets, 70f, 1800L, emptyMap())
        assertEquals(r.totalKcal.toFloat(), r.perExercise.values.sum().toFloat(), 2f)
    }

    @Test
    fun session_emptyList_isSafe() {
        assertEquals(0, CalorieCalculator.estimateSession(emptyList(), 70f, 600L, emptyMap()).totalKcal)
    }

    @Test
    fun session_zeroBodyWeight_isSafe() {
        val sets = listOf(SetInput("Bench", 10, 60f))
        assertEquals(0, CalorieCalculator.estimateSession(sets, 0f, 600L, emptyMap()).totalKcal)
    }

    @Test
    fun session_measuredDurationMethod() {
        val sets = listOf(SetInput("Bench", 10, 60f))
        val r = CalorieCalculator.estimateSession(sets, 70f, 1800L, emptyMap())
        assertEquals(SessionKcal.Method.MEASURED_DURATION, r.method)
    }

    @Test
    fun session_nullDurationMethod() {
        val sets = listOf(SetInput("Bench", 10, 60f))
        val r = CalorieCalculator.estimateSession(sets, 70f, null, emptyMap())
        assertEquals(SessionKcal.Method.ESTIMATED_DURATION, r.method)
    }

    @Test
    fun estimate1Rm_singleRep_returnsWeight() =
        assertEquals(100f, CalorieCalculator.estimate1Rm(100f, 1)!!, 0f)

    @Test
    fun estimate1Rm_outOfRange_returnsNull() =
        assertNull(CalorieCalculator.estimate1Rm(100f, 15))

    @Test
    fun userProfile_exactScenario_chestPressAndBicepsCurl() {
        // 1. User profile parameters from screenshot: Height 166 cm, Male
        val bodyWeightKg = CalorieCalculator.resolveBodyWeightKg(null, 166, Gender.MALE)
        assertEquals(60.6f, bodyWeightKg, 0.1f)

        // 2. PR history from screenshot: Chest Press 28 kg, Biceps Curl 6 kg
        val prHistory = mapOf(
            "Chest Press" to 28f,
            "Barbell Biceps Curl With Arm Blaster" to 6f
        )

        // 3. User logged workout:
        // Chest Press: 20 kg x 12 reps -> 20 / 28 = 71.4% (MODERATE: MET 5.0)
        // Biceps Curl: 5 kg x 10 reps -> 5 / 6 = 83.3% (VIGOROUS: MET 6.0)
        val sets = listOf(
            SetInput("Chest Press", 12, 20f),
            SetInput("Chest Press", 12, 20f),
            SetInput("Chest Press", 12, 20f),
            SetInput("Barbell Biceps Curl With Arm Blaster", 10, 5f),
            SetInput("Barbell Biceps Curl With Arm Blaster", 10, 5f),
            SetInput("Barbell Biceps Curl With Arm Blaster", 10, 5f)
        )

        // 4. Test with 45 minutes measured session (2700s)
        val result45Min = CalorieCalculator.estimateSession(
            sets = sets,
            bodyWeightKg = bodyWeightKg,
            sessionSeconds = 45 * 60L,
            best1RmByExercise = prHistory
        )

        println("=== TEST 1: User Profile 45-min Session Simulation ===")
        println("User Body Weight (BMI-22): $bodyWeightKg kg")
        println("Method: ${result45Min.method}")
        println("Total Net Calories: ${result45Min.totalKcal} kcal")
        result45Min.perExercise.forEach { (exercise, kcal) ->
            println(" -> $exercise: $kcal kcal")
        }

        // Note: 6 sets estimated active time is ~501s (~8.3 min). The 2.5x guard rail caps the 45-min
        // session at 1,252s (~20.8 min) to prevent idle pause inflation, yielding ~94 net kcal.
        assertTrue("45 min workout capped by guard rail should burn between 70 and 200 net kcal", result45Min.totalKcal in 70..200)
        assertEquals(SessionKcal.Method.MEASURED_DURATION, result45Min.method)

        // 5. Test with estimated duration (no timer, pure set-effort estimate)
        val resultEstimated = CalorieCalculator.estimateSession(
            sets = sets,
            bodyWeightKg = bodyWeightKg,
            sessionSeconds = null,
            best1RmByExercise = prHistory
        )

        println("\n=== TEST 2: Estimated Duration (No Timer) ===")
        println("Method: ${resultEstimated.method}")
        println("Total Net Calories: ${resultEstimated.totalKcal} kcal")
        resultEstimated.perExercise.forEach { (exercise, kcal) ->
            println(" -> $exercise: $kcal kcal")
        }

        assertTrue("Estimated duration should produce realistic calories", resultEstimated.totalKcal > 0)
        assertEquals(SessionKcal.Method.ESTIMATED_DURATION, resultEstimated.method)
    }
}
