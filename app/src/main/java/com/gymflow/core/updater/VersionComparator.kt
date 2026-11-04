package com.gymflow.core.updater

object VersionComparator {

    /**
     * Compares two semantic version strings (e.g. "v1.1.0" and "1.0.0").
     * Returns true if remoteVersion is strictly greater than currentVersion.
     */
    fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
        val remoteParts = parseVersion(remoteVersion)
        val currentParts = parseVersion(currentVersion)

        val maxLength = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLength) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }

    private fun parseVersion(version: String): List<Int> {
        return version
            .trim()
            .removePrefix("v")
            .removePrefix("V")
            .split('-', '_', '+')[0] // remove prerelease suffix like -beta.1
            .split('.')
            .mapNotNull { it.toIntOrNull() }
    }
}
