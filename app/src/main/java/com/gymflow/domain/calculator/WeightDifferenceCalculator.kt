package com.gymflow.domain.calculator

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Pure domain calculator for immediate post-workout weight variation,
 * interpreting acute sweat/fluid loss and calculating rehydration requirements.
 */
object WeightDifferenceCalculator {

    enum class FluidLossSeverity {
        NEUTRAL_OR_GAIN,
        MINIMAL,      // < 1% of body weight
        MODERATE,     // 1% to 2% of body weight
        SIGNIFICANT   // > 2% of body weight (impaired performance/recovery)
    }

    data class WeightDeltaAnalysis(
        val preWeightKg: Float,
        val postWeightKg: Float,
        val deltaKg: Float,
        val percentChange: Float,
        val fluidLossMl: Int,
        val recommendedFluidIntakeMl: Int,
        val severity: FluidLossSeverity,
        val summaryNote: String,
        val advice: String
    )

    /**
     * Standard sports science replenishment multiplier:
     * ~1.25L to 1.5L of fluid per 1.0kg of acute weight loss.
     */
    private const val FLUID_REPLENISHMENT_FACTOR = 1.25f

    /**
     * Calculates the delta between post-workout and pre-workout weight:
     * Delta = postWeightKg - preWeightKg
     */
    fun calculateDelta(preWeightKg: Float, postWeightKg: Float): Float {
        val delta = postWeightKg - preWeightKg
        return Math.round(delta * 100f) / 100f
    }

    /**
     * Performs a comprehensive acute weight & fluid loss analysis.
     */
    fun analyze(preWeightKg: Float, postWeightKg: Float): WeightDeltaAnalysis {
        val delta = calculateDelta(preWeightKg, postWeightKg)
        val percent = if (preWeightKg > 0f) (delta / preWeightKg) * 100f else 0f
        val absLossKg = if (delta < 0) abs(delta) else 0f
        val fluidLossMl = (absLossKg * 1000f).roundToInt()
        val recommendedIntakeMl = (absLossKg * FLUID_REPLENISHMENT_FACTOR * 1000f).roundToInt()

        val severity: FluidLossSeverity
        val summaryNote: String
        val advice: String

        when {
            delta >= 0f -> {
                severity = FluidLossSeverity.NEUTRAL_OR_GAIN
                summaryNote = "Weight maintained or increased during workout."
                advice = "Great hydration maintenance. Continue regular daily water intake."
            }
            abs(percent) < 1.0f -> {
                severity = FluidLossSeverity.MINIMAL
                summaryNote = "This is an immediate weight change, mostly reflecting fluid loss and other short-term fluctuations."
                advice = "Drink approximately ${recommendedIntakeMl} ml over the next 1-2 hours to replenish sweat loss."
            }
            abs(percent) in 1.0f..2.0f -> {
                severity = FluidLossSeverity.MODERATE
                summaryNote = "Moderate acute fluid loss detected (-${abs(delta)} kg)."
                advice = "Rehydrate with ${recommendedIntakeMl} ml of water. Consider taking electrolytes to restore sodium balance."
            }
            else -> {
                severity = FluidLossSeverity.SIGNIFICANT
                summaryNote = "Significant dehydration loss (>2% body weight)."
                advice = "Urgent rehydration needed. Drink ${recommendedIntakeMl} ml with electrolyte replacement solutions over the next few hours."
            }
        }

        return WeightDeltaAnalysis(
            preWeightKg = preWeightKg,
            postWeightKg = postWeightKg,
            deltaKg = delta,
            percentChange = Math.round(percent * 100f) / 100f,
            fluidLossMl = fluidLossMl,
            recommendedFluidIntakeMl = recommendedIntakeMl,
            severity = severity,
            summaryNote = summaryNote,
            advice = advice
        )
    }
}
