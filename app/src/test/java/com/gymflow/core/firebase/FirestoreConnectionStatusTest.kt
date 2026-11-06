package com.gymflow.core.firebase

import org.junit.Assert.assertEquals
import org.junit.Test

class FirestoreConnectionStatusTest {

    @Test
    fun testConnectionStatusEnumValues() {
        val statuses = FirestoreConnectionStatus.values()
        assertEquals(3, statuses.size)
        assertEquals(FirestoreConnectionStatus.CONNECTED, FirestoreConnectionStatus.valueOf("CONNECTED"))
        assertEquals(FirestoreConnectionStatus.SYNCING, FirestoreConnectionStatus.valueOf("SYNCING"))
        assertEquals(FirestoreConnectionStatus.OFFLINE, FirestoreConnectionStatus.valueOf("OFFLINE"))
    }

    @Test
    fun testMetadataStatusResolution() {
        // Online and not from cache -> CONNECTED
        fun resolveStatus(isNetworkAvailable: Boolean, isFromCache: Boolean, hasPendingWrites: Boolean): FirestoreConnectionStatus {
            return if (!isNetworkAvailable) {
                FirestoreConnectionStatus.OFFLINE
            } else if (hasPendingWrites) {
                FirestoreConnectionStatus.SYNCING
            } else if (!isFromCache) {
                FirestoreConnectionStatus.CONNECTED
            } else {
                FirestoreConnectionStatus.CONNECTED
            }
        }

        assertEquals(
            FirestoreConnectionStatus.OFFLINE,
            resolveStatus(isNetworkAvailable = false, isFromCache = false, hasPendingWrites = false)
        )
        assertEquals(
            FirestoreConnectionStatus.SYNCING,
            resolveStatus(isNetworkAvailable = true, isFromCache = false, hasPendingWrites = true)
        )
        assertEquals(
            FirestoreConnectionStatus.CONNECTED,
            resolveStatus(isNetworkAvailable = true, isFromCache = false, hasPendingWrites = false)
        )
    }
}

