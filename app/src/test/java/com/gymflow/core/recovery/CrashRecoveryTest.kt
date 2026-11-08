package com.gymflow.core.recovery

import com.gymflow.core.updater.VersionComparator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashRecoveryTest {

    @Test
    fun testEmergencyUpdateVersionComparison() {
        val current = "1.0.36"

        // Newer releases must trigger update available
        assertTrue(VersionComparator.isNewerVersion("v1.0.37", current))
        assertTrue(VersionComparator.isNewerVersion("1.0.38", current))
        assertTrue(VersionComparator.isNewerVersion("v1.1.0", current))
        assertTrue(VersionComparator.isNewerVersion("v2.0.0", current))

        // Same or older releases must NOT trigger update
        assertFalse(VersionComparator.isNewerVersion("v1.0.36", current))
        assertFalse(VersionComparator.isNewerVersion("1.0.36", current))
        assertFalse(VersionComparator.isNewerVersion("v1.0.35", current))
        assertFalse(VersionComparator.isNewerVersion("v1.0.0", current))
    }

    @Test
    fun testEmergencyUpdateInfoDataClass() {
        val info = EmergencyUpdateInfo(
            versionName = "v1.0.37",
            releaseNotes = "Fixed startup database migration crash",
            downloadUrl = "https://github.com/sumit01-coder/GYMFLOW/releases/download/v1.0.37/app-release.apk",
            apkSizeMb = 34.5f,
            isNewer = true
        )

        assertNotNull(info)
        assertEquals("v1.0.37", info.versionName)
        assertTrue(info.isNewer)
        assertTrue(info.downloadUrl.endsWith(".apk"))
        assertEquals(34.5f, info.apkSizeMb, 0.01f)
    }

    @Test
    fun testVersionParsingEdgeCases() {
        // Test prefixes and tags
        assertTrue(VersionComparator.isNewerVersion("v1.0.37-release", "1.0.36"))
        assertTrue(VersionComparator.isNewerVersion("V1.0.37+build10", "1.0.36"))
        assertFalse(VersionComparator.isNewerVersion("1.0.36", "1.0.36"))
    }
}

