package com.gymflow.domain.calculator

/**
 * Session-level energy expenditure calculator using net-MET model.
 *
 * Net formula: (MET − 1) × bodyWeightKg × durationHours
 *
 * Intensity is resolved from %1RM (from actual PR history), then RPE, then a moderate default.
 * Session duration uses real endTime−startTime clamped to [0.5×, 2.5×] estimated active time
 * to prevent inflated numbers from paused/abandoned sessions.
 *
 * The old volume term (sets × reps × weight × 0.085) is intentionally removed — it
 * double-counted work already captured by MET and was empirically ~15–20× too high.
 */

enum class Gender { MALE, FEMALE, UNSPECIFIED }
enum class Effort { LIGHT, MODERATE, VIGOROUS }

data class SetInput(
    val exerciseName: String,
    val reps: Int,
    val weightKg: Float,
    val rpe: Float? = null          // optional, 1..10
)

data class SessionKcal(
    val totalKcal: Int,
    val perExercise: Map<String, Int>,
    val method: Method
) {
    enum class Method { MEASURED_DURATION, ESTIMATED_DURATION }
}

object CalorieCalculator {

    // Compendium of Physical Activities — resistance training gross METs (rest included)
    private const val MET_LIGHT    = 3.5f
    private const val MET_MODERATE = 5.0f
    private const val MET_VIGOROUS = 6.0f

    private const val SEC_PER_REP      = 3.5f
    private const val DEFAULT_REST_SEC = 45f
    private const val MAX_OVERRUN      = 2.5f   // cap: ignore pauses longer than 2.5× active estimate
    private const val MIN_RATIO        = 0.5f   // floor: data glitch protection

    fun metFor(effort: Effort): Float = when (effort) {
        Effort.LIGHT    -> MET_LIGHT
        Effort.MODERATE -> MET_MODERATE
        Effort.VIGOROUS -> MET_VIGOROUS
    }

    /**
     * Net kcal (energy above resting).
     * Formula: (MET − 1) × bodyWeightKg × durationHours
     */
    fun netKcal(met: Float, bodyWeightKg: Float, durationSeconds: Float): Float {
        val hours = durationSeconds.coerceAtLeast(0f) / 3600f
        return (met - 1f).coerceAtLeast(0f) * bodyWeightKg * hours
    }

    /**
     * Epley 1RM estimate. Only trust low-rep sets (1–10 reps).
     * Returns null if inputs are out of valid range.
     */
    fun estimate1Rm(weightKg: Float, reps: Int): Float? =
        if (weightKg <= 0f || reps !in 1..10) null
        else if (reps == 1) weightKg
        else weightKg * (1f + reps / 30f)

    /**
     * Resolve effort from %1RM → RPE → moderate default.
     * @param set       The set being evaluated.
     * @param best1RmKg The user's all-time best 1RM for this exercise from PR history.
     */
    fun effortFor(set: SetInput, best1RmKg: Float?): Effort {
        // 1) %1RM from the user's own PR history — most accurate signal
        if (best1RmKg != null && best1RmKg > 0f) {
            val pct = set.weightKg / best1RmKg
            return when {
                pct >= 0.80f -> Effort.VIGOROUS
                pct >= 0.60f -> Effort.MODERATE
                else         -> Effort.LIGHT
            }
        }
        // 2) RPE if the user logged it
        set.rpe?.let {
            return when {
                it >= 8f -> Effort.VIGOROUS
                it >= 6f -> Effort.MODERATE
                else     -> Effort.LIGHT
            }
        }
        // 3) Unknown — conservative default, don't pretend to know
        return Effort.MODERATE
    }

    /**
     * Body-weight resolution priority:
     *   1. Logged weight from WeightLog / profile
     *   2. BMI-22 from height (anthropometric standard)
     *   3. Gender-based population default
     */
    fun resolveBodyWeightKg(loggedKg: Float?, heightCm: Int?, gender: Gender): Float =
        loggedKg?.takeIf { it in 30f..250f }
            ?: heightCm?.takeIf { it in 120..230 }
                ?.let { 22f * (it / 100f) * (it / 100f) }
                ?.coerceIn(40f, 130f)
            ?: if (gender == Gender.FEMALE) 60f else 70f

    /**
     * Session-level kcal calculation.
     *
     * Allocates total session time proportionally across sets by their estimated
     * effort-time, then applies each set's own MET (resolved from %1RM or RPE).
     *
     * @param sets               All sets logged in this session.
     * @param bodyWeightKg       User's body weight (use resolveBodyWeightKg).
     * @param sessionSeconds     Real duration from endTime−startTime. Null if not yet completed.
     * @param best1RmByExercise  Map of exerciseName → best 1RM (compute once per Flow emission).
     */
    fun estimateSession(
        sets: List<SetInput>,
        bodyWeightKg: Float,
        sessionSeconds: Long?,
        best1RmByExercise: Map<String, Float>
    ): SessionKcal {
        if (sets.isEmpty() || bodyWeightKg <= 0f) {
            return SessionKcal(0, emptyMap(), SessionKcal.Method.ESTIMATED_DURATION)
        }

        // Per-set estimated effort-time (also used as allocation weight)
        val setSeconds = sets.map { it.reps * SEC_PER_REP + DEFAULT_REST_SEC }
        val estTotal   = setSeconds.sum()

        val measured = sessionSeconds?.takeIf { it > 0 }?.toFloat()
        val totalSeconds = measured
            ?.coerceIn(estTotal * MIN_RATIO, estTotal * MAX_OVERRUN)
            ?: estTotal

        // Allocate time proportionally; apply per-set MET; accumulate per exercise
        val perExercise = mutableMapOf<String, Float>()
        sets.forEachIndexed { i, set ->
            val share  = totalSeconds * (setSeconds[i] / estTotal)
            val effort = effortFor(set, best1RmByExercise[set.exerciseName])
            val kcal   = netKcal(metFor(effort), bodyWeightKg, share)
            perExercise.merge(set.exerciseName, kcal, Float::plus)
        }

        val total = perExercise.values.sum().toInt().coerceAtLeast(1)
        return SessionKcal(
            totalKcal   = total,
            perExercise = perExercise.mapValues { it.value.toInt().coerceAtLeast(0) },
            method      = if (measured != null) SessionKcal.Method.MEASURED_DURATION
                          else SessionKcal.Method.ESTIMATED_DURATION
        )
    }

    // ── Legacy compatibility helpers (used by WorkoutEngine for session-end save) ──────────

    /**
     * Legacy category-based MET resolver kept for WorkoutEngine at session end.
     * New code should prefer [estimateSession].
     */
    fun resolveMet(category: String, intensity: ExerciseIntensity = ExerciseIntensity.MODERATE): Float {
        val n = category.trim().lowercase()
        return when {
            n.contains("hiit") || n.contains("interval")                        -> 8.5f
            n.contains("cardio") || n.contains("run") || n.contains("row") ||
            n.contains("cycle") || n.contains("treadmill")                      -> 7.0f
            n.contains("stretch") || n.contains("mobility") || n.contains("yoga") -> 2.5f
            else -> when (intensity) {
                ExerciseIntensity.LIGHT    -> MET_LIGHT
                ExerciseIntensity.MODERATE -> MET_MODERATE
                ExerciseIntensity.VIGOROUS -> MET_VIGOROUS
            }
        }
    }

    enum class ExerciseIntensity { LIGHT, MODERATE, VIGOROUS }

    enum class WorkoutCategory(val defaultMet: Float) {
        RESISTANCE_LIGHT(3.5f),
        RESISTANCE_MODERATE(5.0f),
        RESISTANCE_VIGOROUS(6.0f),
        CARDIO(7.0f),
        HIIT(8.5f),
        CALISTHENICS(4.0f),
        STRETCHING_MOBILITY(2.5f),
        GENERAL(5.0f)
    }

    fun calculate(met: Float, weightKg: Float, durationMinutes: Float): Float {
        if (weightKg <= 0f || durationMinutes <= 0f || met <= 0f) return 0f
        return (met * 3.5f * weightKg / 200.0f) * durationMinutes
    }

    fun calculateForCategory(
        category: String,
        weightKg: Float,
        durationMinutes: Float,
        intensity: ExerciseIntensity = ExerciseIntensity.MODERATE
    ): Float {
        val met = resolveMet(category, intensity)
        return calculate(met = met, weightKg = weightKg, durationMinutes = durationMinutes)
    }

    fun calculateForSession(
        weightKg: Float,
        durationSeconds: Long,
        category: WorkoutCategory = WorkoutCategory.RESISTANCE_MODERATE
    ): Int {
        if (weightKg <= 0f || durationSeconds <= 0L) return 0
        val durationMinutes = durationSeconds / 60.0f
        return Math.round(calculate(met = category.defaultMet, weightKg = weightKg, durationMinutes = durationMinutes))
    }
}
