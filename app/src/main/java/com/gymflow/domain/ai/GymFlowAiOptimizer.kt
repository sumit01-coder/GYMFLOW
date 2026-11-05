package com.gymflow.domain.ai

import com.gymflow.domain.model.WorkoutExercise
import java.util.Locale

/**
 * Data model for individual AI coaching suggestions generated on-device.
 * Features stable deterministic IDs so dismissals persist across app launches.
 */
data class AiExerciseSuggestion(
    val id: String,
    val title: String,
    val explanation: String,
    val recommendedSets: Int,
    val recommendedReps: Int,
    val targetRestSeconds: Int,
    val recommendedAlternative: String? = null,
    val scienceNote: String? = null,
    val tag: String = "OPTIMIZATION", // "VOLUME", "REST", "JOINT_HEALTH", "METABOLIC", "LOAD", "BIOMETRIC"
    val isApplied: Boolean = false
)

/**
 * Normalized user biometric and anthropometric context for AI exercise recommendations.
 */
data class UserAiProfile(
    val age: Int = 28,
    val gender: String = "Male",
    val heightCm: Float = 175f,
    val weightKg: Float = 75f,
    val chestCm: Float = 0f,
    val waistCm: Float = 0f,
    val armsCm: Float = 0f,
    val thighsCm: Float = 0f,
    val hipsCm: Float = 0f,
    val goal: String = "Hypertrophy",
    val experienceLevel: String = "Intermediate"
) {
    val bmi: Float
        get() = if (heightCm > 0f) {
            val heightM = heightCm / 100f
            weightKg / (heightM * heightM)
        } else 24.5f

    val waistToHipRatio: Float
        get() = if (hipsCm > 0f && waistCm > 0f) waistCm / hipsCm else 0f

    val waistToHeightRatio: Float
        get() = if (heightCm > 0f && waistCm > 0f) waistCm / heightCm else 0f

    val chestToWaistRatio: Float
        get() = if (waistCm > 0f && chestCm > 0f) chestCm / waistCm else 0f

    val thighToWaistRatio: Float
        get() = if (waistCm > 0f && thighsCm > 0f) thighsCm / waistCm else 0f

    val armToChestRatio: Float
        get() = if (chestCm > 0f && armsCm > 0f) armsCm / chestCm else 0f
}

/**
 * On-Device GYMFLOW AI Exercise Optimizer.
 * Executes purely local, science-based biomechanical, anthropometric, and physiological heuristics
 * to optimize training volume, rep ranges, rest intervals, and muscle balance based on:
 * Age, Gender, Height, Weight, Chest, Waist, Arms, Thighs, and Hips.
 */
object GymFlowAiOptimizer {

    /**
     * Main entry point: Generates actionable coaching recommendations based on
     * full biometric measurements, gender, goals, and planned exercises.
     */
    fun optimize(
        age: Int,
        gender: String = "Male",
        heightCm: Float,
        weightKg: Float,
        chestCm: Float = 0f,
        waistCm: Float = 0f,
        armsCm: Float = 0f,
        thighsCm: Float = 0f,
        hipsCm: Float = 0f,
        goal: String,
        experienceLevel: String = "Intermediate",
        exercises: List<WorkoutExercise> = emptyList()
    ): List<AiExerciseSuggestion> {
        val userProfile = UserAiProfile(
            age = if (age > 0) age else 28,
            gender = if (gender.isNotBlank()) gender else "Male",
            heightCm = if (heightCm > 0f) heightCm else 175f,
            weightKg = if (weightKg > 0f) weightKg else 75f,
            chestCm = chestCm,
            waistCm = waistCm,
            armsCm = armsCm,
            thighsCm = thighsCm,
            hipsCm = hipsCm,
            goal = goal.ifBlank { "Hypertrophy" },
            experienceLevel = experienceLevel.ifBlank { "Intermediate" }
        )

        val suggestions = mutableListOf<AiExerciseSuggestion>()

        // 1. Goal-Specific Volume & Rest Heuristics
        suggestions.add(generateGoalSuggestion(userProfile))

        // 2. Biometric & Anthropometric Ratios Analysis (Waist, Hips, Chest, Arms, Thighs)
        val biometricSuggestions = generateBiometricSuggestions(userProfile)
        suggestions.addAll(biometricSuggestions)

        // 3. Gender-Specific Biomechanics & Kinematics
        suggestions.add(generateGenderSuggestion(userProfile))

        // 4. Age 40+ Joint-Friendly Adaptations
        val jointSuggestions = generateJointAdaptations(userProfile, exercises)
        suggestions.addAll(jointSuggestions)

        // 5. Experience Level & Progression Cadence
        suggestions.add(generateExperienceSuggestion(userProfile))

        // 6. Exercise-Specific Micro-Optimizations (if specific routine is provided)
        exercises.take(2).forEach { ex ->
            val specific = generateExerciseSpecificSuggestion(ex, userProfile)
            if (specific != null && suggestions.none { it.id == specific.id }) {
                suggestions.add(specific)
            }
        }

        return suggestions
    }

    /**
     * Generates volume, rep, and rest recommendations tailored to the physiological goal.
     */
    private fun generateGoalSuggestion(profile: UserAiProfile): AiExerciseSuggestion {
        val normalizedGoal = profile.goal.lowercase()

        return when {
            normalizedGoal.contains("strength") || normalizedGoal.contains("power") -> {
                AiExerciseSuggestion(
                    id = "ai_opt_goal_strength",
                    title = "Neuromuscular Strength Protocol",
                    explanation = "Structure work to 4-5 sets of 4-6 reps with heavier loads (>80% 1RM) and 90-120s rest to maximize central motor unit recruitment.",
                    recommendedSets = 5,
                    recommendedReps = 5,
                    targetRestSeconds = 120,
                    scienceNote = "High load training requires 90-120 seconds for near-complete phosphocreatine (ATP-CP) resynthesis between maximal efforts.",
                    tag = "VOLUME"
                )
            }
            normalizedGoal.contains("fat") || normalizedGoal.contains("loss") ||
                    normalizedGoal.contains("tone") || normalizedGoal.contains("cut") ||
                    normalizedGoal.contains("endurance") -> {
                AiExerciseSuggestion(
                    id = "ai_opt_goal_fat_loss",
                    title = "Metabolic Conditioning & EPOC Protocol",
                    explanation = "Structure work to 3-4 sets of 12-15 reps with brisk 45s rest intervals to elevate metabolic burn and post-exercise oxygen consumption.",
                    recommendedSets = 3,
                    recommendedReps = 15,
                    targetRestSeconds = 45,
                    scienceNote = "Shorter rest (45s) maintains elevated lactate accumulation and continuous cardiovascular demand, increasing lipid oxidation during and after training.",
                    tag = "METABOLIC"
                )
            }
            else -> {
                AiExerciseSuggestion(
                    id = "ai_opt_goal_hypertrophy",
                    title = "Hypertrophic Tension & Sarcoplasmic Volume",
                    explanation = "Target 3-4 sets of 8-12 reps with 60s rest to achieve the optimal balance of mechanical tension and intramuscular metabolic stress.",
                    recommendedSets = 4,
                    recommendedReps = 10,
                    targetRestSeconds = 60,
                    scienceNote = "The 8-12 rep continuum maximizes muscle protein synthesis pathways (mTORC1) while maintaining high time under tension.",
                    tag = "VOLUME"
                )
            }
        }
    }

    /**
     * Biometric & Anthropometric Ratios Analysis:
     * Analyzes Waist, Hips, Chest, Arms, and Thigh measurements to guide body composition.
     */
    private fun generateBiometricSuggestions(profile: UserAiProfile): List<AiExerciseSuggestion> {
        val suggestions = mutableListOf<AiExerciseSuggestion>()
        val isFemale = profile.gender.equals("Female", ignoreCase = true)

        // 1. Waist-to-Hip Ratio (WHR)
        val whr = profile.waistToHipRatio
        if (whr > 0f) {
            val highThreshold = if (isFemale) 0.85f else 0.90f
            if (whr >= highThreshold) {
                suggestions.add(
                    AiExerciseSuggestion(
                        id = "ai_opt_biometric_whr_high",
                        title = "Metabolic Core & Visceral Trim (WHR: ${String.format(Locale.US, "%.2f", whr)})",
                        explanation = "Your waist-to-hip ratio indicates central mass distribution. Incorporate compound multi-joint movements with 45s rest to accelerate core trimming and insulin sensitivity.",
                        recommendedSets = 4,
                        recommendedReps = 12,
                        targetRestSeconds = 45,
                        scienceNote = "High metabolic density compound training enhances systemic glucose disposal and accelerates visceral fat reduction.",
                        tag = "BIOMETRIC"
                    )
                )
            } else if (whr in 0.70f..highThreshold) {
                suggestions.add(
                    AiExerciseSuggestion(
                        id = "ai_opt_biometric_whr_athletic",
                        title = "Anabolic Priming (WHR: ${String.format(Locale.US, "%.2f", whr)})",
                        explanation = "Ideal athletic waist-to-hip ratio detected. Your body composition is optimal for pure lean muscle progression with 8-10 heavy reps and 75s rest.",
                        recommendedSets = 4,
                        recommendedReps = 8,
                        targetRestSeconds = 75,
                        scienceNote = "Optimal waist-to-hip metrics correlate with balanced androgen sensitivity and high recovery capacity.",
                        tag = "BIOMETRIC"
                    )
                )
            }
        }

        // 2. Chest-to-Waist Ratio (V-Taper Index)
        val cwr = profile.chestToWaistRatio
        if (cwr > 0f && cwr < 1.25f && !isFemale) {
            suggestions.add(
                AiExerciseSuggestion(
                    id = "ai_opt_biometric_v_taper",
                    title = "V-Taper Upper Torso Specialization",
                    explanation = "Chest-to-waist ratio is ${String.format(Locale.US, "%.2f", cwr)}. Prioritize wide-grip lat pulldowns and lateral deltoid raises (12-14 reps) to expand shoulder width and taper the waist.",
                    recommendedSets = 4,
                    recommendedReps = 12,
                    targetRestSeconds = 60,
                    recommendedAlternative = "Wide-Grip Lat Pulldown & Cable Lateral Raise",
                    scienceNote = "Hypertrophy of the latissimus dorsi and clavicular fibers enhances the biomechanical upper body ratio.",
                    tag = "BIOMETRIC"
                )
            )
        }

        // 3. Lower Body Balance: Thigh-to-Waist
        val twr = profile.thighToWaistRatio
        if (twr > 0f && twr < 0.65f) {
            suggestions.add(
                AiExerciseSuggestion(
                    id = "ai_opt_biometric_thighs",
                    title = "Quad & Hamstring Hypertrophy Drive",
                    explanation = "Thigh measurement suggests lower body hypertrophy focus. Add 1 working set to your primary leg movement with a strict 3-second eccentric tempo.",
                    recommendedSets = 4,
                    recommendedReps = 10,
                    targetRestSeconds = 90,
                    recommendedAlternative = "Hack Squat / Bulgarian Split Squat",
                    scienceNote = "Lengthening muscle under load during controlled eccentrics yields maximum mechanical tension for quadriceps growth.",
                    tag = "BIOMETRIC"
                )
            )
        }

        // 4. Arm Proportion
        val acr = profile.armToChestRatio
        if (acr > 0f && acr < 0.32f) {
            suggestions.add(
                AiExerciseSuggestion(
                    id = "ai_opt_biometric_arms",
                    title = "Bicep & Tricep Hypertrophic Isolation",
                    explanation = "Arm measurement relative to chest indicates room for direct arm isolation. Add 3 sets of 10-12 reps of overhead tricep extensions and incline dumbbell curls.",
                    recommendedSets = 3,
                    recommendedReps = 12,
                    targetRestSeconds = 45,
                    recommendedAlternative = "Overhead Cable Triceps & Incline DB Curls",
                    scienceNote = "The long head of the triceps and biceps require shoulder flexion/extension angles for complete sarcomere recruitment.",
                    tag = "BIOMETRIC"
                )
            )
        }

        return suggestions
    }

    /**
     * Gender-Specific Biomechanics & Hormonal Recovery Heuristics.
     */
    private fun generateGenderSuggestion(profile: UserAiProfile): AiExerciseSuggestion {
        val isFemale = profile.gender.equals("Female", ignoreCase = true)
        return if (isFemale) {
            AiExerciseSuggestion(
                id = "ai_opt_gender_female",
                title = "Posterior Chain & Knee Stability (Female Kinematics)",
                explanation = "Female athletes typically demonstrate higher Type-I muscle endurance. Structure sets to 10-14 reps with 60s rest, prioritizing glute-to-hamstring balance to protect knee joints.",
                recommendedSets = 4,
                recommendedReps = 12,
                targetRestSeconds = 60,
                recommendedAlternative = "Barbell Hip Thrust & Romanian Deadlift",
                scienceNote = "Higher estrogen levels confer enhanced muscle recovery and fatigue resistance during moderate rep ranges.",
                tag = "BIOMETRIC"
            )
        } else {
            AiExerciseSuggestion(
                id = "ai_opt_gender_male",
                title = "Mechanical Tension & Anabolic Load (Male Kinematics)",
                explanation = "Capitalize on high mechanical tension with progressive overload in the 6-10 rep bracket, allowing 75-90s rest between heavy compound sets.",
                recommendedSets = 4,
                recommendedReps = 8,
                targetRestSeconds = 75,
                scienceNote = "Targeting high-threshold motor units under high tension triggers peak hormonal signaling and muscle fiber hypertrophy.",
                tag = "BIOMETRIC"
            )
        }
    }

    /**
     * Age 40+ Joint-Friendly Adaptations:
     * Detects barbell movements and recommends Dumbbell or Cable movements over heavy flat barbell.
     */
    private fun generateJointAdaptations(
        profile: UserAiProfile,
        exercises: List<WorkoutExercise>
    ): List<AiExerciseSuggestion> {
        val jointSuggestions = mutableListOf<AiExerciseSuggestion>()

        if (profile.age >= 40) {
            val hasHeavyBarbellBench = exercises.any {
                it.exerciseName.contains("bench", ignoreCase = true) &&
                        it.exerciseName.contains("barbell", ignoreCase = true)
            }
            val hasHeavySquat = exercises.any {
                it.exerciseName.contains("squat", ignoreCase = true) &&
                        it.exerciseName.contains("barbell", ignoreCase = true)
            }
            val hasOverheadPress = exercises.any {
                (it.exerciseName.contains("overhead", ignoreCase = true) ||
                        it.exerciseName.contains("military", ignoreCase = true)) &&
                        it.exerciseName.contains("barbell", ignoreCase = true)
            }

            if (hasHeavyBarbellBench) {
                jointSuggestions.add(
                    AiExerciseSuggestion(
                        id = "ai_opt_joint_bench",
                        title = "Joint Protection: Converging Chest Movement",
                        explanation = "At age ${profile.age}, substitute Flat Barbell Bench with Incline Dumbbell Press or Cable Chest Fly to allow natural humeral rotation and protect rotator cuffs.",
                        recommendedSets = 4,
                        recommendedReps = 10,
                        targetRestSeconds = 60,
                        recommendedAlternative = "Incline Dumbbell Press / Cable Fly",
                        scienceNote = "Fixed barbells enforce internal shoulder rotation at the bottom of the rep; independent dumbbells or cables preserve synovial joint integrity.",
                        tag = "JOINT_HEALTH"
                    )
                )
            } else if (hasHeavySquat) {
                jointSuggestions.add(
                    AiExerciseSuggestion(
                        id = "ai_opt_joint_squat",
                        title = "Spinal Decompression: Unilateral Squats",
                        explanation = "Substitute heavy Barbell Back Squats with Bulgarian Split Squats or Hack Squats to dramatically reduce lumbar axial compression.",
                        recommendedSets = 3,
                        recommendedReps = 10,
                        targetRestSeconds = 75,
                        recommendedAlternative = "Bulgarian Split Squat / Hack Squat",
                        scienceNote = "Unilateral movements produce equal quad and glute hypertrophy while reducing axial spinal disc pressure by over 60%.",
                        tag = "JOINT_HEALTH"
                    )
                )
            } else if (hasOverheadPress) {
                jointSuggestions.add(
                    AiExerciseSuggestion(
                        id = "ai_opt_joint_overhead",
                        title = "Acromial Relief: Seated DB / Cable Shoulder Press",
                        explanation = "Replace Barbell Overhead Press with Seated Dumbbell Press or Cable Lateral Raises to preserve subacromial clearance.",
                        recommendedSets = 3,
                        recommendedReps = 12,
                        targetRestSeconds = 60,
                        recommendedAlternative = "Seated Dumbbell Shoulder Press",
                        scienceNote = "Neutral or semi-pronated dumbbell grips prevent impingement of the supraspinatus tendon against the acromion.",
                        tag = "JOINT_HEALTH"
                    )
                )
            } else {
                jointSuggestions.add(
                    AiExerciseSuggestion(
                        id = "ai_opt_joint_age_general",
                        title = "Longevity & Joint Biomechanics (Age ${profile.age}+)",
                        explanation = "Prioritize Dumbbell and Cable pathways over fixed straight barbells to respect your natural joint carrying angles and optimize tendon recovery.",
                        recommendedSets = profileGoalSets(profile.goal),
                        recommendedReps = 10,
                        targetRestSeconds = 60,
                        recommendedAlternative = "Dumbbells & Cable Attachments",
                        scienceNote = "Synovial fluid replenishment and tendon collagen synthesis benefit significantly from multi-planar converging trajectories.",
                        tag = "JOINT_HEALTH"
                    )
                )
            }
        }

        return jointSuggestions
    }

    /**
     * Experience Level & Biometric Calibration.
     */
    private fun generateExperienceSuggestion(profile: UserAiProfile): AiExerciseSuggestion {
        val exp = profile.experienceLevel.lowercase()
        return when {
            exp.contains("beginner") || exp.contains("novice") -> {
                AiExerciseSuggestion(
                    id = "ai_opt_exp_beginner",
                    title = "Neuromuscular Calibration (Beginner)",
                    explanation = "Focus on strict 3-second eccentric tempo and deliberate form. Rest an extra 15-30s per set to reset mental focus and motor patterns.",
                    recommendedSets = 3,
                    recommendedReps = 10,
                    targetRestSeconds = 75,
                    scienceNote = "Beginner strength gains are primarily neural (motor unit synchrony) rather than muscular, requiring controlled pacing.",
                    tag = "TECHNIQUE"
                )
            }
            exp.contains("advanced") || exp.contains("elite") -> {
                AiExerciseSuggestion(
                    id = "ai_opt_exp_advanced",
                    title = "Intensity Density & Overload (Advanced)",
                    explanation = "Incorporate undulating load schemes and high density. Limit rest strictly to 60s on hypertrophy sets to induce peak cellular swelling.",
                    recommendedSets = 4,
                    recommendedReps = 8,
                    targetRestSeconds = 60,
                    scienceNote = "Advanced lifters require greater intra-workout training density and varied stimulus to overcome systemic adaptation plateaus.",
                    tag = "LOAD"
                )
            }
            else -> {
                AiExerciseSuggestion(
                    id = "ai_opt_exp_intermediate",
                    title = "Progressive Overload Cadence (Intermediate)",
                    explanation = "Aim for a double-progression model: when you reach the top of the 8-12 rep range on all sets, increase load by 2.5-5kg.",
                    recommendedSets = 4,
                    recommendedReps = 10,
                    targetRestSeconds = 60,
                    scienceNote = "Double progression guarantees systematic mechanical overload while preventing premature technical failure.",
                    tag = "LOAD"
                )
            }
        }
    }

    /**
     * Micro-optimization for an individual workout exercise.
     */
    fun generateExerciseSpecificSuggestion(
        exercise: WorkoutExercise,
        profile: UserAiProfile
    ): AiExerciseSuggestion? {
        val name = exercise.exerciseName.lowercase()

        if (profile.age >= 40 && name.contains("bench") && name.contains("barbell")) {
            return AiExerciseSuggestion(
                id = "ai_opt_sub_bench_${exercise.exerciseId}",
                title = "Substitute ${exercise.exerciseName}",
                explanation = "Switch to Incline Dumbbell Press (30° incline) for safer shoulder kinematics and peak upper-pectoral fiber recruitment.",
                recommendedSets = 4,
                recommendedReps = 10,
                targetRestSeconds = 60,
                recommendedAlternative = "Incline Dumbbell Press",
                scienceNote = "Dumbbells permit converging wrist angles, reducing subacromial friction.",
                tag = "JOINT_HEALTH"
            )
        }

        if (name.contains("deadlift") && (profile.age >= 40 || profile.bmi > 28f)) {
            return AiExerciseSuggestion(
                id = "ai_opt_sub_deadlift_${exercise.exerciseId}",
                title = "Lower Back Optimization: Trap Bar Deadlift",
                explanation = "A hexagonal Trap Bar places the center of gravity inside your base of support, reducing spinal shear stress while lifting heavy.",
                recommendedSets = 4,
                recommendedReps = 6,
                targetRestSeconds = 90,
                recommendedAlternative = "Trap Bar (Hex Bar) Deadlift",
                scienceNote = "Peak lumbar moment arm is reduced by ~22% compared to conventional straight-bar deadlifting.",
                tag = "JOINT_HEALTH"
            )
        }

        return null
    }

    private fun profileGoalSets(goal: String): Int {
        val g = goal.lowercase()
        return when {
            g.contains("strength") -> 5
            g.contains("fat") || g.contains("tone") -> 3
            else -> 4
        }
    }

    fun getRecommendedRepsForGoal(goal: String): Int {
        val g = goal.lowercase()
        return when {
            g.contains("strength") -> 5
            g.contains("fat") || g.contains("tone") -> 14
            else -> 10
        }
    }

    fun getRecommendedRestForGoal(goal: String): Int {
        val g = goal.lowercase()
        return when {
            g.contains("strength") -> 120
            g.contains("fat") || g.contains("tone") -> 45
            else -> 60
        }
    }
}
