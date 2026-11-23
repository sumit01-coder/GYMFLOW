package com.gymflow.core.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BetaFeatureGateTest {

    @Test
    fun isAiFoodScannerEnabled_allowsAuthorizedBetaUser() {
        assertTrue(BetaFeatureGate.isAiFoodScannerEnabled("sumitpatel00784@gmail.com"))
        assertTrue(BetaFeatureGate.isAiFoodScannerEnabled("SUMITPATEL00784@GMAIL.COM"))
        assertTrue(BetaFeatureGate.isAiFoodScannerEnabled(" sumitpatel00784@gmail.com "))
    }

    @Test
    fun isAiFoodScannerEnabled_blocksOtherUsersAndNull() {
        assertFalse(BetaFeatureGate.isAiFoodScannerEnabled(null))
        assertFalse(BetaFeatureGate.isAiFoodScannerEnabled(""))
        assertFalse(BetaFeatureGate.isAiFoodScannerEnabled("   "))
        assertFalse(BetaFeatureGate.isAiFoodScannerEnabled("otheruser@gymflow.com"))
        assertFalse(BetaFeatureGate.isAiFoodScannerEnabled("sumit@gmail.com"))
    }
}
