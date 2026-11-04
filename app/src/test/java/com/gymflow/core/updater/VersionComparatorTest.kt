package com.gymflow.core.updater

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {

    @Test
    fun `isNewerVersion returns true when remote major is higher`() {
        assertTrue(VersionComparator.isNewerVersion("v2.0.0", "1.0.0"))
        assertTrue(VersionComparator.isNewerVersion("2.0", "1.9.9"))
    }

    @Test
    fun `isNewerVersion returns true when remote minor is higher`() {
        assertTrue(VersionComparator.isNewerVersion("v1.1.0", "1.0.0"))
        assertTrue(VersionComparator.isNewerVersion("1.10.0", "1.9.0"))
    }

    @Test
    fun `isNewerVersion returns true when remote patch is higher`() {
        assertTrue(VersionComparator.isNewerVersion("v1.0.1", "1.0.0"))
        assertTrue(VersionComparator.isNewerVersion("1.0.5", "1.0.4"))
    }

    @Test
    fun `isNewerVersion returns false when versions are identical or remote is older`() {
        assertFalse(VersionComparator.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(VersionComparator.isNewerVersion("v1.0.0", "1.0.0"))
        assertFalse(VersionComparator.isNewerVersion("v0.9.9", "1.0.0"))
        assertFalse(VersionComparator.isNewerVersion("1.0.0", "1.0.1"))
    }

    @Test
    fun `isNewerVersion handles pre-release suffixes correctly`() {
        assertTrue(VersionComparator.isNewerVersion("v1.1.0-beta.1", "1.0.0"))
        assertFalse(VersionComparator.isNewerVersion("v1.0.0-rc1", "1.0.0"))
    }
}
