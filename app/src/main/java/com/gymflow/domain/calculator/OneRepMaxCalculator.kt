package com.gymflow.domain.calculator

import kotlin.math.roundToInt

/**
 * Pure domain calculator for estimated One Rep Max (1RM) and intensity percentages.
 *
 * Primary Formula: Epley formula
 * 1RM = weight * (1.0 + reps / 30.0)
 * Validated standard for reps <= 12.
 */
object OneRepMaxCalculator {

    const val MAX_EPLEY_REPS = 12

    data class RepEstimate(
        val reps: Int,
        val percentageOf1RM: Float,
        val estimatedWeightKg: Float
    )

    /**
     * Calculates the estimated One Rep Max (1RM) using Epley's formula.
     * If reps == 1, returns the exact weight.
     * For reps > 12, warns or applies standard scaling while capping recommendation reliability.
     */
    fun calculate1RM(weightKg: Float, reps: Int): Float {
        if (weightKg <= 0f || reps <= 0) return 0f
        if (reps == 1) return weightKg

        // Epley formula: weight * (1.0 + reps / 30.0)
        val validReps = reps.coerceAtMost(MAX_EPLEY_REPS)
        val oneRepMax = weightKg * (1.0f + (validReps / 30.0f))
        return Math.round(oneRepMax * 10f) / 10f
    }

    /**
     * Calculates working weight at a specific intensity percentage of 1RM.
     */
    fun calculateWeightAtIntensity(oneRepMaxKg: Float, percentage: Float): Float {
        if (oneRepMaxKg <= 0f || percentage <= 0f) return 0f
        val calculated = oneRepMaxKg * (percentage / 100.0f)
        return Math.round(calculated * 10f) / 10f
    }

    /**
     * Provides a standard percentage table for rep ranges based on 1RM.
     * (e.g. 1 rep = 100%, 3 reps = 93%, 5 reps = 87%, 8 reps = 80%, 10 reps = 75%, 12 reps = 70%)
     */
    fun getRepEstimates(oneRepMaxKg: Float): List<RepEstimate> {
        val repToPercentageMap = listOf(
            1 to 100f,
            2 to 95f,
            3 to 93f,
            4 to 90f,
            5 to 87f,
            6 to 85f,
            8 to 80f,
            10 to 75f,
            12 to 70f
        )
        return repToPercentageMap.map { (reps, pct) ->
            RepEstimate(
                reps = reps,
                percentageOf1RM = pct,
                estimatedWeightKg = calculateWeightAtIntensity(oneRepMaxKg, pct)
            )
        }
    }

    /**
     * Inverts the formula to estimate the maximum reps achievable at a given target weight.
     */
    fun estimateRepsAtWeight(oneRepMaxKg: Float, targetWeightKg: Float): Int {
        if (oneRepMaxKg <= 0f || targetWeightKg <= 0f) return 0
        if (targetWeightKg >= oneRepMaxKg) return 1
        // targetWeight = 1RM / (1 + reps/30) => reps = 30 * ((1RM / targetWeight) - 1)
        val estimatedReps = 30.0f * ((oneRepMaxKg / targetWeightKg) - 1.0f)
        return estimatedReps.roundToInt().coerceIn(1, MAX_EPLEY_REPS)
    }
}
