package com.gymflow.feature.member

import com.gymflow.core.database.SetLogEntity
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.database.WorkoutSessionEntity
import com.gymflow.core.database.WorkoutSessionWithSets
import com.gymflow.domain.calculator.OneRepMaxCalculator
import com.gymflow.feature.member.home.getWallClockGreeting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class RealDataAndProgressTest {

    @Test
    fun testWallClockGreeting() {
        val greeting = getWallClockGreeting("Member")
        val hour = LocalTime.now().hour
        when (hour) {
            in 5..11 -> assertEquals("Good morning, Member!", greeting)
            in 12..16 -> assertEquals("Good afternoon, Member!", greeting)
            in 17..21 -> assertEquals("Good evening, Member!", greeting)
            else -> assertEquals("Late session, Member!", greeting)
        }
    }

    @Test
    fun testOneRepMaxCalculator_prComputation() {
        // Bench press 80kg x 5 reps
        val bench1RM = OneRepMaxCalculator.calculate1RM(80f, 5)
        assertEquals(93.3f, bench1RM, 0.1f)

        // Squat 110kg x 8 reps
        val squat1RM = OneRepMaxCalculator.calculate1RM(110f, 8)
        assertEquals(139.3f, squat1RM, 0.1f)

        // Single rep should return exact weight
        val singleRep = OneRepMaxCalculator.calculate1RM(150f, 1)
        assertEquals(150f, singleRep, 0.01f)

        // Invalid weight / reps should return 0
        assertEquals(0f, OneRepMaxCalculator.calculate1RM(0f, 10), 0.01f)
        assertEquals(0f, OneRepMaxCalculator.calculate1RM(100f, 0), 0.01f)
    }

    @Test
    fun testRealVolumeAndStatsCalculation() {
        val set1 = SetLogEntity(
            id = "s1", sessionId = "sess1", exerciseId = "e1", exerciseName = "Squat",
            setNumber = 1, targetReps = 10, targetWeightKg = 100f, actualReps = 10, actualWeightKg = 100f,
            completedAtEpochMs = 1000L
        )
        val set2 = SetLogEntity(
            id = "s2", sessionId = "sess1", exerciseId = "e1", exerciseName = "Squat",
            setNumber = 2, targetReps = 8, targetWeightKg = 110f, actualReps = 8, actualWeightKg = 110f,
            completedAtEpochMs = 2000L
        )
        val set3 = SetLogEntity(
            id = "s3", sessionId = "sess1", exerciseId = "e2", exerciseName = "Bench Press",
            setNumber = 1, targetReps = 5, targetWeightKg = 80f, actualReps = 5, actualWeightKg = 80f,
            completedAtEpochMs = 3000L
        )

        val sets = listOf(set1, set2, set3)
        val totalReps = sets.sumOf { it.actualReps }
        val totalVolume = sets.sumOf { (it.actualWeightKg * it.actualReps).toDouble() }.toFloat()

        assertEquals(23, totalReps)
        // 100*10 + 110*8 + 80*5 = 1000 + 880 + 400 = 2280
        assertEquals(2280f, totalVolume, 0.01f)

        // Check PR calculation
        val prs = sets.groupBy { it.exerciseName }
            .mapValues { (_, exerciseSets) ->
                exerciseSets.maxOf { OneRepMaxCalculator.calculate1RM(it.actualWeightKg, it.actualReps) }
            }

        assertEquals(2, prs.size)
        assertTrue(prs.containsKey("Squat"))
        assertTrue(prs.containsKey("Bench Press"))
        assertEquals(OneRepMaxCalculator.calculate1RM(110f, 8), prs["Squat"]!!, 0.01f)
        assertEquals(OneRepMaxCalculator.calculate1RM(80f, 5), prs["Bench Press"]!!, 0.01f)
    }

    @Test
    fun testStreakCalculationAlgorithm() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val twoDaysAgo = today.minusDays(2)

        val dates = sortedSetOf(twoDaysAgo, yesterday, today)

        var currentStreak = 0
        var checkDate: LocalDate? = when {
            dates.contains(today) -> today
            dates.contains(yesterday) -> yesterday
            else -> null
        }
        while (checkDate != null && dates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        assertEquals(3, currentStreak)

        // Longest streak
        var longestStreak = 0
        var runningStreak = 0
        var prevDate: LocalDate? = null
        for (date in dates) {
            if (prevDate != null && date == prevDate.plusDays(1)) {
                runningStreak++
            } else {
                runningStreak = 1
            }
            if (runningStreak > longestStreak) {
                longestStreak = runningStreak
            }
            prevDate = date
        }

        assertEquals(3, longestStreak)
    }
}
