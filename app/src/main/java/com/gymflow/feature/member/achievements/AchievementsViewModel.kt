package com.gymflow.feature.member.achievements

import androidx.lifecycle.ViewModel
import com.gymflow.domain.model.Achievement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
    val currentStreakDays: Int = 5,
    val longestStreakDays: Int = 14,
    val achievements: List<AchievementUiItem> = defaultAchievements,
    val selectedAchievement: AchievementUiItem? = null
) {
    companion object {
        val defaultAchievements = listOf(
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-1",
                    title = "First Step",
                    description = "Completed your very first workout in GYMFLOW.",
                    iconName = "emoji_events",
                    isUnlocked = true,
                    unlockedAtEpochMs = System.currentTimeMillis() - 86400000L * 25
                ),
                category = "Milestones",
                progressCurrent = 1,
                progressTarget = 1
            ),
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-2",
                    title = "Iron Habit",
                    description = "Completed 10 workouts with uncompromising discipline.",
                    iconName = "fitness_center",
                    isUnlocked = true,
                    unlockedAtEpochMs = System.currentTimeMillis() - 86400000L * 6
                ),
                category = "Milestones",
                progressCurrent = 18,
                progressTarget = 10
            ),
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-3",
                    title = "PR Crusher",
                    description = "Unlocked a new Personal Record on compound lifts.",
                    iconName = "trending_up",
                    isUnlocked = true,
                    unlockedAtEpochMs = System.currentTimeMillis() - 86400000L * 3
                ),
                category = "Strength",
                progressCurrent = 4,
                progressTarget = 1
            ),
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-4",
                    title = "Weekly Warrior",
                    description = "Completed all scheduled workouts in a single calendar week.",
                    iconName = "calendar_month",
                    isUnlocked = true,
                    unlockedAtEpochMs = System.currentTimeMillis() - 86400000L * 1
                ),
                category = "Streaks",
                progressCurrent = 4,
                progressTarget = 4
            ),
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-5",
                    title = "Hydration Hero",
                    description = "Hit your 3,000 ml water goal 7 days in a row.",
                    iconName = "water_drop",
                    isUnlocked = false,
                    unlockedAtEpochMs = null
                ),
                category = "Hydration",
                progressCurrent = 5,
                progressTarget = 7
            ),
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-6",
                    title = "Century Club",
                    description = "Log 100 total completed sets in the GYMFLOW engine.",
                    iconName = "military_tech",
                    isUnlocked = true,
                    unlockedAtEpochMs = System.currentTimeMillis() - 86400000L * 8
                ),
                category = "Milestones",
                progressCurrent = 342,
                progressTarget = 100
            ),
            AchievementUiItem(
                achievement = Achievement(
                    id = "ach-7",
                    title = "Heavy Lifter",
                    description = "Surpass 100,000 kg in cumulative workout volume.",
                    iconName = "scale",
                    isUnlocked = false,
                    unlockedAtEpochMs = null
                ),
                category = "Strength",
                progressCurrent = 48250,
                progressTarget = 100000
            )
        )
    }
}

class AchievementsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    fun selectAchievement(item: AchievementUiItem?) {
        _uiState.update { it.copy(selectedAchievement = item) }
    }
}
