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
}
