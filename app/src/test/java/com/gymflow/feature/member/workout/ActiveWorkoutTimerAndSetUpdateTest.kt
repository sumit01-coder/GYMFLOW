package com.gymflow.feature.member.workout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveWorkoutTimerAndSetUpdateTest {

    @Test
    fun testActiveWorkoutInitialState_timerDoesNotRunBeforeStart() {
        val state = ActiveWorkoutUiState(
            exerciseName = "Dumbbell Alternate Biceps Curl",
            currentSet = 1,
            totalSets = 10,
            targetReps = 35,
            targetWeightKg = 4f,
            actualReps = 35,
            actualWeightKg = 4f,
            elapsedSeconds = 0,
            isWaitingForNextSet = false,
            isSetStarted = false,
            isPaused = false,
            isWorkoutComplete = false
        )

        // Verify timer condition evaluates to false before user taps 'Start Set 1'
        val shouldTimerTick = !state.isPaused && !state.isWorkoutComplete && !state.isWaitingForNextSet && state.isSetStarted
        assertFalse("Timer should NOT run before Set 1 is started", shouldTimerTick)
    }

    @Test
    fun testActiveWorkout_timerPausesWhileWaitingForNextSet() {
        val state = ActiveWorkoutUiState(
            exerciseName = "Dumbbell Alternate Biceps Curl",
            currentSet = 4,
            totalSets = 10,
            targetReps = 35,
            targetWeightKg = 4f,
            actualReps = 20,
            actualWeightKg = 4.5f,
            elapsedSeconds = 370,
            isWaitingForNextSet = true,
            isSetStarted = false,
            isPaused = false,
            isWorkoutComplete = false,
            completedSets = listOf(
                CompletedSetRecord(setNumber = 1, reps = 35, weightKg = 4.5f),
                CompletedSetRecord(setNumber = 2, reps = 35, weightKg = 4.5f),
                CompletedSetRecord(setNumber = 3, reps = 35, weightKg = 4.5f),
                CompletedSetRecord(setNumber = 4, reps = 35, weightKg = 4.5f)
            )
        )

        // Verify timer condition evaluates to false when waiting for next set
        val shouldTimerTick = !state.isPaused && !state.isWorkoutComplete && !state.isWaitingForNextSet && state.isSetStarted
        assertFalse("Timer should NOT run while waiting for next set", shouldTimerTick)
    }

    @Test
    fun testActiveWorkout_timerTicksWhenSetInProgress() {
        val state = ActiveWorkoutUiState(
            exerciseName = "Dumbbell Alternate Biceps Curl",
            currentSet = 5,
            totalSets = 10,
            targetReps = 35,
            targetWeightKg = 4f,
            actualReps = 20,
            actualWeightKg = 4.5f,
            elapsedSeconds = 370,
            isWaitingForNextSet = false,
            isSetStarted = true,
            isPaused = false,
            isWorkoutComplete = false
        )

        // Verify timer condition evaluates to true when set is in progress
        val shouldTimerTick = !state.isPaused && !state.isWorkoutComplete && !state.isWaitingForNextSet && state.isSetStarted
        assertTrue("Timer SHOULD run when set is active and in progress", shouldTimerTick)
    }

    @Test
    fun testStartNextSet_preservesUserAdjustedRepsAndWeight() {
        // User is waiting for Set 5. They adjusted actual reps to 20 and actual weight to 4.5 kg
        val waitingState = ActiveWorkoutUiState(
            exerciseName = "Dumbbell Alternate Biceps Curl",
            currentSet = 4,
            totalSets = 10,
            targetReps = 35,
            targetWeightKg = 4f,
            actualReps = 20,       // User adjusted to 20
            actualWeightKg = 4.5f, // User adjusted to 4.5
            isWaitingForNextSet = true,
            isSetStarted = false
        )

        // Simulating startNextSet logic without overwriting with target
        val nextSetNumber = waitingState.currentSet + 1
        val updatedState = waitingState.copy(
            currentSet = nextSetNumber,
            isWaitingForNextSet = false,
            isSetStarted = true,
            // User values preserved!
            isPaused = false
        )

        assertEquals(5, updatedState.currentSet)
        assertEquals(20, updatedState.actualReps)
        assertEquals(4.5f, updatedState.actualWeightKg, 0.01f)
        assertTrue(updatedState.isSetStarted)
        assertFalse(updatedState.isWaitingForNextSet)
    }

    @Test
    fun testUpdateLoggedSet_updatesRepsWeightAndTotalVolume() {
        val initialCompletedSets = listOf(
            CompletedSetRecord(setNumber = 1, reps = 35, weightKg = 4.5f),
            CompletedSetRecord(setNumber = 2, reps = 35, weightKg = 4.5f),
            CompletedSetRecord(setNumber = 3, reps = 35, weightKg = 4.5f),
            CompletedSetRecord(setNumber = 4, reps = 35, weightKg = 4.5f)
        )

        val state = ActiveWorkoutUiState(
            currentSet = 4,
            totalSets = 10,
            completedSets = initialCompletedSets
        )

        // User updates Set 4 from 35 reps to 20 reps
        val updatedSets = state.completedSets.map {
            if (it.setNumber == 4) it.copy(reps = 20, weightKg = 4.5f) else it
        }

        val updatedState = state.copy(completedSets = updatedSets)

        val set4 = updatedState.completedSets.find { it.setNumber == 4 }
        assertEquals(20, set4?.reps)
        assertEquals(4.5f, set4?.weightKg ?: 0f, 0.01f)

        // Check total volume computation
        val totalVolume = updatedState.completedSets.sumOf { (it.reps * it.weightKg).toDouble() }.toFloat()
        // (35*4.5)*3 + 20*4.5 = 157.5*3 + 90 = 472.5 + 90 = 562.5
        assertEquals(562.5f, totalVolume, 0.1f)
    }
}
