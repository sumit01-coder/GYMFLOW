package com.gymflow.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeightDifferenceCalculatorTest {

    @Test
    fun testWeightDeltaCalculation() {
        val delta = WeightDifferenceCalculator.calculateDelta(
            preWeightKg = 67.80f,
            postWeightKg = 67.50f
        )
        assertEquals(-0.30f, delta, 0.001f)
    }

    @Test
    fun testFluidLossAnalysisModerateSweat() {
        val analysis = WeightDifferenceCalculator.analyze(
            preWeightKg = 67.80f,
            postWeightKg = 67.50f
        )
        assertEquals(-0.30f, analysis.deltaKg, 0.001f)
        assertEquals(300, analysis.fluidLossMl)
        // 300ml * 1.25 = 375ml recommended intake
        assertEquals(375, analysis.recommendedFluidIntakeMl)
        assertEquals(WeightDifferenceCalculator.FluidLossSeverity.MINIMAL, analysis.severity)
        assertTrue(analysis.summaryNote.contains("immediate weight change"))
    }

    @Test
    fun testSignificantDehydrationOverTwoPercent() {
        // 70kg down to 68kg (-2.0kg = -2.85%)
        val analysis = WeightDifferenceCalculator.analyze(
            preWeightKg = 70.0f,
            postWeightKg = 68.0f
        )
        assertEquals(-2.0f, analysis.deltaKg, 0.001f)
        assertEquals(WeightDifferenceCalculator.FluidLossSeverity.SIGNIFICANT, analysis.severity)
        assertEquals(2000, analysis.fluidLossMl)
        assertEquals(2500, analysis.recommendedFluidIntakeMl)
    }

    @Test
    fun testNeutralOrWeightGain() {
        val analysis = WeightDifferenceCalculator.analyze(
            preWeightKg = 67.5f,
            postWeightKg = 67.8f
        )
        assertEquals(0.30f, analysis.deltaKg, 0.001f)
        assertEquals(WeightDifferenceCalculator.FluidLossSeverity.NEUTRAL_OR_GAIN, analysis.severity)
        assertEquals(0, analysis.fluidLossMl)
        assertEquals(0, analysis.recommendedFluidIntakeMl)
    }
}
