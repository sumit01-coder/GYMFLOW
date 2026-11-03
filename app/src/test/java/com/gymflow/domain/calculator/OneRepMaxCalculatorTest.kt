package com.gymflow.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OneRepMaxCalculatorTest {

    @Test
    fun testOneRepMaxSingleRep() {
        // Exactly 1 rep -> 1RM should equal the weight lifted
        val oneRepMax = OneRepMaxCalculator.calculate1RM(weightKg = 100f, reps = 1)
        assertEquals(100f, oneRepMax, 0.01f)
    }

    @Test
    fun testEpleyFormulaFiveReps() {
        // 100 kg for 5 reps: 100 * (1 + 5/30) = 100 * (1.166667) = 116.7 kg
        val oneRepMax = OneRepMaxCalculator.calculate1RM(weightKg = 100f, reps = 5)
        assertEquals(116.7f, oneRepMax, 0.1f)
    }

    @Test
    fun testWeightAtIntensity() {
        // 80% of 100kg = 80kg
        val weight = OneRepMaxCalculator.calculateWeightAtIntensity(oneRepMaxKg = 100f, percentage = 80f)
        assertEquals(80f, weight, 0.01f)
    }

    @Test
    fun testEstimateRepsAtWeight() {
        // At 100kg with 1RM of 100kg -> 1 rep
        val repsAtMax = OneRepMaxCalculator.estimateRepsAtWeight(oneRepMaxKg = 100f, targetWeightKg = 100f)
        assertEquals(1, repsAtMax)

        // At 85kg with 1RM of 100kg -> 100/85 = 1.17647 => reps = 30 * 0.17647 = 5.29 => 5 reps
        val repsAt85 = OneRepMaxCalculator.estimateRepsAtWeight(oneRepMaxKg = 100f, targetWeightKg = 85f)
        assertEquals(5, repsAt85)
    }

    @Test
    fun testRepEstimatesTable() {
        val estimates = OneRepMaxCalculator.getRepEstimates(oneRepMaxKg = 100f)
        assertEquals(9, estimates.size)
        assertEquals(100f, estimates.first().estimatedWeightKg, 0.1f)
        assertEquals(70f, estimates.last().estimatedWeightKg, 0.1f)
    }
}
