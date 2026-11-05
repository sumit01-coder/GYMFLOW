package com.gymflow.feature.member.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.HydrationLogEntity
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.database.WorkoutSessionWithSets
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.Achievement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.IsoFields
import javax.inject.Inject

data class AchievementUiItem(
    val achievement: Achievement,
    val category: String, // "Milestones", "Streaks", "Strength", "Hydration"
    val progressCurrent: Int,
    val progressTarget: Int
) {
    val progressPercent: Float
        get() = (progressCurrent.toFloat() / progressTarget.toFloat()).coerceIn(0f, 1f)
}

data class AchievementsUiState(
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val achievements: List<AchievementUiItem> = emptyList(),
    val selectedAchievement: AchievementUiItem? = null
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao,
    private val weightDao: WeightDao,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        observeAchievementsData()
    }

    private fun observeAchievementsData() {
        val userId = tokenManager.getUserIdSync().takeIf { !it.isNullOrBlank() } ?: "default_member"

        viewModelScope.launch {
            combine(
                workoutSessionDao.getAllSessionsWithSets(userId),
                weightDao.getHydrationLogsForRange(userId, 0L, Long.MAX_VALUE)
            ) { sessions: List<WorkoutSessionWithSets>, hydrationLogs: List<HydrationLogEntity> ->
                buildAchievementsState(sessions, hydrationLogs)
            }.collect { newState ->
                _uiState.update { current ->
                    newState.copy(
                        selectedAchievement = current.selectedAchievement?.let { sel ->
                            newState.achievements.find { it.achievement.id == sel.achievement.id } ?: sel
                        }
                    )
                }
            }
        }
    }

    fun selectAchievement(item: AchievementUiItem?) {
        _uiState.update { it.copy(selectedAchievement = item) }
    }

    private fun buildAchievementsState(
        sessionsWithSets: List<WorkoutSessionWithSets>,
        hydrationLogs: List<HydrationLogEntity>
    ): AchievementsUiState {
        // Filter completed sessions
        val completedSessions = sessionsWithSets
            .filter { it.session.isCompleted || it.session.endTimeEpochMs != null }
            .sortedBy { it.session.startTimeEpochMs }

        val completedCount = completedSessions.size
        val allSetLogs = completedSessions.flatMap { it.setLogs }

        // Real total volume
        val computedVolume = allSetLogs.sumOf { (it.actualWeightKg * it.actualReps).toDouble() }.toFloat()
        val sessionVolume = completedSessions.sumOf { it.session.totalVolumeKg.toDouble() }.toFloat()
        val totalVolumeKg = if (computedVolume > 0f) computedVolume else sessionVolume

        // Real PR existence: At least one set logged with weight > 0 and reps > 0
        val hasPR = allSetLogs.any { it.actualWeightKg > 0f && it.actualReps > 0 }
        val prSet = allSetLogs.filter { it.actualWeightKg > 0f && it.actualReps > 0 }.minByOrNull { it.completedAtEpochMs }

        // Real streak calculation from completed session dates
        val sessionDates = completedSessions.mapNotNull { sessionWithSets ->
            val ts = sessionWithSets.session.startTimeEpochMs
            if (ts > 0) {
                Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDate()
            } else null
        }.toSortedSet()

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        var currentStreak = 0
        var checkDate: LocalDate? = when {
            sessionDates.contains(today) -> today
            sessionDates.contains(yesterday) -> yesterday
            else -> null
        }
        while (checkDate != null && sessionDates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        var longestStreak = 0
        var runningStreak = 0
        var prevDate: LocalDate? = null
        for (date in sessionDates) {
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

        // Weekly Warrior calculation: Completed workouts in any calendar week
        val workoutsByWeek = completedSessions.groupBy {
            val date = Instant.ofEpochMilli(it.session.startTimeEpochMs).atZone(ZoneId.systemDefault()).toLocalDate()
            "${date.year}-${date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}"
        }
        val maxInAnyWeek = workoutsByWeek.values.maxOfOrNull { it.size } ?: 0

        // Hydration streak calculation (3,000 ml water goal)
        val hydrationByDay = hydrationLogs.groupBy {
            Instant.ofEpochMilli(it.recordedAtEpochMs).atZone(ZoneId.systemDefault()).toLocalDate()
        }.mapValues { (_, logs) -> logs.sumOf { it.amountMl } }

        val daysWithGoalMet = hydrationByDay.filter { it.value >= 3000 }.keys.toSortedSet()
        var maxHydrationStreak = 0
        var hStreak = 0
        var hPrev: LocalDate? = null
        for (d in daysWithGoalMet) {
            if (hPrev != null && d == hPrev.plusDays(1)) {
                hStreak++
            } else {
                hStreak = 1
            }
            if (hStreak > maxHydrationStreak) {
                maxHydrationStreak = hStreak
            }
            hPrev = d
        }

        val totalSets = if (allSetLogs.isNotEmpty()) allSetLogs.size else completedSessions.sumOf { it.session.setsCompleted }

        val achievements = listOf(
            // "First Step": Unlocked if completed workouts >= 1
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-1",
                    title = "First Step",
                    description = "Completed your very first workout in GYMFLOW.",
                    iconName = "emoji_events",
                    isUnlocked = completedCount >= 1,
                    unlockedAtEpochMs = if (completedCount >= 1) completedSessions.firstOrNull()?.session?.startTimeEpochMs else null
                ),
                category = "Milestones",
                progressCurrent = if (completedCount >= 1) 1 else 0,
                progressTarget = 1
            ),
            // "Iron Habit": Progress tracks completed workouts / 10
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-2",
                    title = "Iron Habit",
                    description = "Completed 10 workouts with uncompromising discipline.",
                    iconName = "fitness_center",
                    isUnlocked = completedCount >= 10,
                    unlockedAtEpochMs = if (completedCount >= 10) completedSessions.getOrNull(9)?.session?.startTimeEpochMs else null
                ),
                category = "Milestones",
                progressCurrent = completedCount.coerceAtMost(10),
                progressTarget = 10
            ),
            // "PR Crusher": Unlocked if user has set at least 1 personal record
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-3",
                    title = "PR Crusher",
                    description = "Unlocked a new Personal Record on compound lifts.",
                    iconName = "trending_up",
                    isUnlocked = hasPR,
                    unlockedAtEpochMs = if (hasPR) (prSet?.completedAtEpochMs ?: System.currentTimeMillis()) else null
                ),
                category = "Strength",
                progressCurrent = if (hasPR) 1 else 0,
                progressTarget = 1
            ),
            // "Weekly Warrior": Completed 4 workouts in a calendar week
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-4",
                    title = "Weekly Warrior",
                    description = "Completed 4 scheduled workouts in a single calendar week.",
                    iconName = "calendar_month",
                    isUnlocked = maxInAnyWeek >= 4,
                    unlockedAtEpochMs = if (maxInAnyWeek >= 4) System.currentTimeMillis() else null
                ),
                category = "Streaks",
                progressCurrent = maxInAnyWeek.coerceAtMost(4),
                progressTarget = 4
            ),
            // "Hydration Hero": Hit 3,000 ml water goal 7 days in a row
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-5",
                    title = "Hydration Hero",
                    description = "Hit your 3,000 ml water goal 7 days in a row.",
                    iconName = "water_drop",
                    isUnlocked = maxHydrationStreak >= 7,
                    unlockedAtEpochMs = if (maxHydrationStreak >= 7) System.currentTimeMillis() else null
                ),
                category = "Hydration",
                progressCurrent = maxHydrationStreak.coerceAtMost(7),
                progressTarget = 7
            ),
            // "Century Club": Progress tracks total volume / 100,000 kg
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-6",
                    title = "Century Club",
                    description = "Surpass 100,000 kg in cumulative workout volume across all sessions.",
                    iconName = "military_tech",
                    isUnlocked = totalVolumeKg >= 100000f,
                    unlockedAtEpochMs = if (totalVolumeKg >= 100000f) System.currentTimeMillis() else null
                ),
                category = "Milestones",
                progressCurrent = totalVolumeKg.toInt().coerceAtMost(100000),
                progressTarget = 100000
            ),
            // "Heavy Lifter": Log 100 total completed sets
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-7",
                    title = "Heavy Lifter",
                    description = "Log 100 total completed sets in the GYMFLOW engine.",
                    iconName = "scale",
                    isUnlocked = totalSets >= 100,
                    unlockedAtEpochMs = if (totalSets >= 100) System.currentTimeMillis() else null
                ),
                category = "Strength",
                progressCurrent = totalSets.coerceAtMost(100),
                progressTarget = 100
            )
        )

        return AchievementsUiState(
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
            achievements = achievements,
            selectedAchievement = null
        )
    }
}
