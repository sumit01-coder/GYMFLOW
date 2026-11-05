package com.gymflow.feature.member

import com.gymflow.core.database.WeightLogEntity
import com.gymflow.domain.calculator.WeightDifferenceCalculator
import com.gymflow.domain.model.WeightLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BiPhasicWeightAndHydrationTest {

    @Test
    fun testBiPhasicPreWorkoutInitialLogging() {
        // Phase 1: Member logs pre-workout weight at 07:30 AM
        val memberId = "member_101"
        val logId = UUID.randomUUID().toString()
        val preTimeEpoch = 1700004600000L // 07:30 AM

        val preWorkoutLog = WeightLog(
            id = logId,
            memberId = memberId,
            preWeightKg = 74.20f,
            postWeightKg = 0f,
            deltaKg = 0f,
            recordedAtEpochMs = preTimeEpoch,
            notes = "Fasted morning session",
            postRecordedAtEpochMs = null
        )

        // Verify pre-workout initial state
        assertEquals(74.20f, preWorkoutLog.preWeightKg, 0.001f)
        assertEquals(0f, preWorkoutLog.postWeightKg, 0.001f)
        assertEquals(0f, preWorkoutLog.deltaKg, 0.001f)
        assertNull(preWorkoutLog.postRecordedAtEpochMs)

        // Verify Entity mapping
        val entity = WeightLogEntity.fromDomain(preWorkoutLog, isSynced = false)
        assertEquals(74.20f, entity.preWeightKg, 0.001f)
        assertEquals(0f, entity.postWeightKg, 0.001f)
        assertNull(entity.postRecordedAtEpochMs)

        val reconstructed = entity.toDomain()
        assertEquals(preWorkoutLog, reconstructed)
    }

    @Test
    fun testBiPhasicPostWorkoutUpdateAndSweatLossDelta() {
        // Phase 2: Member completes session and logs post-workout weight at 08:50 AM
        val memberId = "member_101"
        val logId = UUID.randomUUID().toString()
        val preTimeEpoch = 1700004600000L // 07:30 AM
        val postTimeEpoch = 1700009400000L // 08:50 AM

        val preWorkoutWeight = 74.20f
        val postWorkoutWeight = 73.55f
        val delta = postWorkoutWeight - preWorkoutWeight // -0.65 kg

        val completedSessionLog = WeightLog(
            id = logId,
            memberId = memberId,
            preWeightKg = preWorkoutWeight,
            postWeightKg = postWorkoutWeight,
            deltaKg = delta,
            recordedAtEpochMs = preTimeEpoch,
            notes = "Intense HIIT + sauna",
            postRecordedAtEpochMs = postTimeEpoch
        )

        // Verify exact timestamps and weights
        assertEquals(74.20f, completedSessionLog.preWeightKg, 0.001f)
        assertEquals(73.55f, completedSessionLog.postWeightKg, 0.001f)
        assertEquals(-0.65f, completedSessionLog.deltaKg, 0.001f)
        assertNotNull(completedSessionLog.postRecordedAtEpochMs)
        assertEquals(postTimeEpoch, completedSessionLog.postRecordedAtEpochMs)

        // Sweat loss analysis
        val analysis = WeightDifferenceCalculator.analyze(
            preWeightKg = completedSessionLog.preWeightKg,
            postWeightKg = completedSessionLog.postWeightKg
        )
        assertEquals(-0.65f, analysis.deltaKg, 0.001f)
        assertEquals(650, analysis.fluidLossMl)
        // 650 ml * 1.25 replenishment multiplier = 812.5 ml -> 813 ml
        assertEquals(813, analysis.recommendedFluidIntakeMl)
        assertEquals(WeightDifferenceCalculator.FluidLossSeverity.MINIMAL, analysis.severity)
    }

    @Test
    fun testHydrationDragIncrementalMath() {
        // Test 50 ml incremental stepping logic used by the gesture drag slider
        val startIntakeMl = 1200
        val stepSizeMl = 50

        // Dragging upward by 3 increments: +150 ml
        val stepsUp = 3
        val updatedLevelUp = startIntakeMl + (stepsUp * stepSizeMl)
        assertEquals(1350, updatedLevelUp)

        // Delta to save
        val deltaUp = updatedLevelUp - startIntakeMl
        assertEquals(150, deltaUp)

        // Dragging downward by 2 increments: -100 ml
        val stepsDown = 2
        val updatedLevelDown = startIntakeMl - (stepsDown * stepSizeMl)
        assertEquals(1100, updatedLevelDown)

        val deltaDown = updatedLevelDown - startIntakeMl
        assertEquals(-100, deltaDown)
    }
}
