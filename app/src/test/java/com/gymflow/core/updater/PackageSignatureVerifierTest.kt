package com.gymflow.core.updater

import android.content.ContextWrapper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PackageSignatureVerifierTest {

    @Test
    fun testNonExistentFileReturnsIncompatible() {
        val dummyFile = File("non_existent_test_apk.apk")
        val dummyContext = object : ContextWrapper(null) {}

        val result = PackageSignatureVerifier.verifyApkCompatibility(
            context = dummyContext,
            targetApkFile = dummyFile
        )
        assertFalse("Non-existent APK should be marked incompatible", result.isCompatible)
        assertTrue("Error message should mention missing file", result.errorMessage?.contains("does not exist") == true)
    }
}

