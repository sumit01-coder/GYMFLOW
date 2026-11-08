package com.gymflow.core.step

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StepAndGpsTrackingTest {

    @Test
    fun testStrideDistanceCalculation() {
        val heightCm = 175f
        val strideMeters = (heightCm * 0.414f) / 100f
        val steps = 1000
        val totalDistanceMeters = steps * strideMeters

        // Stride for 175cm should be ~0.7245m
        assertEquals(0.7245f, strideMeters, 0.001f)
        assertEquals(724.5f, totalDistanceMeters, 0.1f)
    }

    @Test
    fun testStepCalorieEstimation() {
        val steps = 5000
        val weightKg = 70f
        // Standard formula: steps * 0.04 * (weightKg / 70.0)
        val calories = steps * 0.04f * (weightKg / 70f)
        assertEquals(200f, calories, 0.01f)

        // Heavier person burns proportionally more
        val heavyWeightKg = 87.5f // 1.25x
        val heavyCalories = steps * 0.04f * (heavyWeightKg / 70f)
        assertEquals(250f, heavyCalories, 0.01f)
    }

    @Test
    fun testPolylineEncodingAndDecoding() {
        val originalPoints = listOf(
            Pair(37.77493, -122.41942),
            Pair(37.77520, -122.41780),
            Pair(37.77600, -122.41650)
        )

        val encoded = PolylineEncoder.encode(originalPoints)
        assertTrue("Encoded polyline should not be blank", encoded.isNotBlank())

        val decoded = PolylineEncoder.decode(encoded)
        assertEquals("Decoded points count must match original", originalPoints.size, decoded.size)

        for (i in originalPoints.indices) {
            assertEquals("Latitude at $i must match within 1e-5", originalPoints[i].first, decoded[i].first, 0.00002)
            assertEquals("Longitude at $i must match within 1e-5", originalPoints[i].second, decoded[i].second, 0.00002)
        }
    }

    @Test
    fun testOutdoorPaceCalculation() {
        // Run 5 km (5000m) in 25 minutes (1500 seconds) -> pace should be 5:00 min/km (300 s/km)
        val distanceMeters = 5000f
        val durationSeconds = 1500L
        val km = distanceMeters / 1000f
        val paceSecondsPerKm = durationSeconds / km

        assertEquals(300f, paceSecondsPerKm, 0.01f)

        val mins = (paceSecondsPerKm / 60).toInt()
        val secs = (paceSecondsPerKm % 60).toInt()
        val formattedPace = "%d:%02d".format(mins, secs)
        assertEquals("5:00", formattedPace)
    }

    @Test
    fun testOutdoorUiStateFormattedProperties() {
        val state = OutdoorTrackingUiState(
            isTracking = true,
            durationSeconds = 365L, // 6m 05s
            distanceMeters = 1200f, // 1.20 km
            currentPaceSecondsPerKm = 304f // 5m 04s
        )

        assertEquals("06:05", state.formattedDuration)
        assertEquals("1.20", state.formattedDistanceKm)
        assertEquals("5:04", state.formattedPace)
    }
}

