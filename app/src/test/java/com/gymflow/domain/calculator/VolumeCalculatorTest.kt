package com.gymflow.domain.calculator

import com.gymflow.domain.model.ExerciseSet
import com.gymflow.domain.model.SetLog
import org.junit.Assert.assertEquals
import org.junit.Test

class VolumeCalculatorTest {

    @Test
    fun testSetVolume() {
        val volume = VolumeCalculator.calculateSetVolume(weightKg = 80f, reps = 10)
        assertEquals(800f, volume, 0.01f)
    }

    @Test
    fun testTotalVolumeFromLogs() {
        val logs = listOf(
            SetLog(
                id = "1",
                sessionId = "s1",
                exerciseId = "e1",
                exerciseName = "Bench Press",
                setNumber = 1,
                targetReps = 10,
                targetWeightKg = 60f,
                actualReps = 10,
                actualWeightKg = 60f,
                completedAtEpochMs = 1000L
            ),
            SetLog(
                id = "2",
                sessionId = "s1",
                exerciseId = "e1",
                exerciseName = "Bench Press",
                setNumber = 2,
                targetReps = 10,
                targetWeightKg = 60f,
                actualReps = 8,
                actualWeightKg = 60f,
                completedAtEpochMs = 2000L
            ),
            SetLog(
                id = "3",
                sessionId = "s1",
                exerciseId = "e2",
                exerciseName = "Incline Dumbbell Press",
                setNumber = 1,
                targetReps = 12,
                targetWeightKg = 24f,
                actualReps = 12,
                actualWeightKg = 24f,
                completedAtEpochMs = 3000L
            )
        )

        // 60*10 = 600, 60*8 = 480, 24*12 = 288 -> total = 1368
        val total = VolumeCalculator.calculateTotalVolumeFromLogs(logs)
        assertEquals(1368f, total, 0.01f)

        val grouped = VolumeCalculator.groupVolumeByExercise(logs)
        assertEquals(1080f, grouped["Bench Press"] ?: 0f, 0.01f)
        assertEquals(288f, grouped["Incline Dumbbell Press"] ?: 0f, 0.01f)
    }

    @Test
    fun testExerciseVolumeOnlyCompletedSets() {
        val sets = listOf(
            ExerciseSet(setNumber = 1, targetReps = 10, targetWeightKg = 50f, actualReps = 10, actualWeightKg = 50f, isCompleted = true),
            ExerciseSet(setNumber = 2, targetReps = 10, targetWeightKg = 50f, isCompleted = false)
        )
        val vol = VolumeCalculator.calculateExerciseVolume(sets)
        assertEquals(500f, vol, 0.01f)
    }

    @Test
    fun testVolumeDeltaPercent() {
        val delta = VolumeCalculator.calculateVolumeDeltaPercent(currentVolume = 1100f, previousVolume = 1000f)
        assertEquals(10f, delta, 0.01f)
    }
}
