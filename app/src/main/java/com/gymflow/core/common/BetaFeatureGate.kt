package com.gymflow.core.common

/**
 * Centralized feature gating for experimental and beta modules in GYMFLOW.
 *
 * Controls access to features currently in private testing before public rollout.
 */
object BetaFeatureGate {

    /**
     * Authorized beta tester email permitted to test the AI Food Scanner & Meal Vision feature.
     */
    const val AI_FOOD_SCANNER_BETA_TESTER_EMAIL = "sumitpatel00784@gmail.com"

    /**
     * Evaluates whether the currently authenticated member has access to the AI Food Scanner.
     * Restricts access exclusively to authorized beta tester [AI_FOOD_SCANNER_BETA_TESTER_EMAIL].
     */
    fun isAiFoodScannerEnabled(userEmail: String?): Boolean {
        if (userEmail.isNullOrBlank()) return false
        return userEmail.trim().equals(AI_FOOD_SCANNER_BETA_TESTER_EMAIL, ignoreCase = true)
    }
}
