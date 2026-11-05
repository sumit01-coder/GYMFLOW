package com.gymflow.feature.member.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.database.WeightDao
import com.gymflow.core.security.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ActivityWorkoutItem(
    val id: String,
    val title: String,
    val details: String,
    val timeFormatted: String,
    val caloriesFormatted: String,
    val setsCount: Int,
    val totalVolumeKg: Float,
    val durationMinutes: Long
)

data class MemberActivityUiState(
    val isLoading: Boolean = false,
    val workoutHistory: List<ActivityWorkoutItem> = emptyList(),
    val sessionsThisWeekCount: Int = 0,
    val todayHydrationMl: Int = 0,
    val hydrationGoalMl: Int = 2500,
    val currentStreakDays: Int = 0,
    val milestonesCount: Int = 0
) {
    val hydrationProgressPercent: Int
        get() = if (hydrationGoalMl > 0) {
            ((todayHydrationMl.toFloat() / hydrationGoalMl.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else 0

    val hydrationSubtitle: String
        get() = "${NumberFormat.getNumberInstance(Locale.US).format(todayHydrationMl)} / ${NumberFormat.getNumberInstance(Locale.US).format(hydrationGoalMl)} ml logged today ($hydrationProgressPercent%)"

    val achievementsSubtitle: String
        get() {
            val streakText = if (currentStreakDays == 1) "1-Day Streak" else "$currentStreakDays-Day Streak"
            val milestoneText = if (milestonesCount == 1) "1 Milestone" else "$milestonesCount Milestones"
            return "$streakText · $milestoneText"
        }

    val sessionsThisWeekLabel: String
        get() = if (sessionsThisWeekCount == 1) "1 session this week" else "$sessionsThisWeekCount sessions this week"
}

@HiltViewModel
class MemberActivityViewModel @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao,
    private val weightDao: WeightDao,
    private val tokenManager: TokenManager,
    private val firestoreSyncManager: com.gymflow.core.firebase.FirestoreSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemberActivityUiState(isLoading = true))
    val uiState: StateFlow<MemberActivityUiState> = _uiState.asStateFlow()

    init {
        val memberId = tokenManager.getUserIdSync() ?: "default_member"
        viewModelScope.launch {
            try {
                firestoreSyncManager.pullAllUserDataFromFirestore(memberId)
            } catch (_: Exception) {}
        }
        observeActivityData()
    }

    private fun observeActivityData() {
        val memberId = tokenManager.getUserIdSync() ?: "default_member"

        val now = LocalDate.now()
        val startOfDayMs = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val startOfWeekMs = now.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            combine(
                workoutSessionDao.getAllSessionsWithSets(memberId),
                weightDao.getTodayHydrationTotal(memberId, startOfDayMs)
            ) { sessionsWithSets, todayHydrationNullable ->
                val completedSessions = sessionsWithSets
                    .filter { it.session.isCompleted }
                    .sortedByDescending { it.session.startTimeEpochMs }

                val numberFormat = NumberFormat.getNumberInstance(Locale.US)

                val historyItems = completedSessions.map { sessionWithSets ->
                    val s = sessionWithSets.session
                    val setsList = sessionWithSets.setLogs

                    val actualVolume = if (s.totalVolumeKg > 0f) {
                        s.totalVolumeKg
                    } else {
                        var vol = 0.0
                        for (setLog in setsList) {
                            vol += (setLog.actualWeightKg * setLog.actualReps).toDouble()
                        }
                        vol.toFloat()
                    }

                    val durationMins = if (s.durationSeconds > 0) {
                        maxOf(1, s.durationSeconds / 60)
                    } else {
                        45L
                    }

                    val calories = if (s.caloriesBurned > 0) {
                        s.caloriesBurned
                    } else {
                        (durationMins * 7).toInt() // realistic average burn
                    }

                    val setsCount = if (s.setsCompleted > 0) s.setsCompleted else maxOf(1, setsList.size)

                    val timeStr = formatSessionTime(s.startTimeEpochMs)
                    val volumeStr = numberFormat.format(actualVolume.toInt())
                    val detailsText = "$setsCount sets · $volumeStr kg volume · $durationMins min"

                    ActivityWorkoutItem(
                        id = s.id,
                        title = s.workoutTitle.ifBlank { "Workout Session" },
                        details = detailsText,
                        timeFormatted = timeStr,
                        caloriesFormatted = "$calories kcal",
                        setsCount = setsCount,
                        totalVolumeKg = actualVolume,
                        durationMinutes = durationMins
                    )
                }

                val thisWeekCount = completedSessions.count { it.session.startTimeEpochMs >= startOfWeekMs }

                // Calculate real streak from completed session dates
                val sessionDates = completedSessions.map {
                    Instant.ofEpochMilli(it.session.startTimeEpochMs).atZone(ZoneId.systemDefault()).toLocalDate()
                }.distinct().sortedDescending()

                var streak = 0
                var checkDate = now
                if (!sessionDates.contains(checkDate) && sessionDates.contains(now.minusDays(1))) {
                    checkDate = now.minusDays(1)
                }
                while (sessionDates.contains(checkDate)) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                }

                // Compute real milestones unlocked
                var milestones = 0
                if (completedSessions.isNotEmpty()) milestones++ // First Step
                if (completedSessions.size >= 10) milestones++ // Iron Habit
                var totalVolume = 0.0
                for (cs in completedSessions) {
                    totalVolume += cs.session.totalVolumeKg.toDouble()
                }
                if (totalVolume >= 100000.0) milestones++ // Century Club
                var totalSets = 0
                for (cs in completedSessions) {
                    totalSets += cs.setLogs.size
                }
                if (totalSets >= 50) milestones++ // Heavy Lifter
                if ((todayHydrationNullable ?: 0) >= 2500) milestones++ // Hydration milestone

                MemberActivityUiState(
                    isLoading = false,
                    workoutHistory = historyItems,
                    sessionsThisWeekCount = thisWeekCount,
                    todayHydrationMl = todayHydrationNullable ?: 0,
                    hydrationGoalMl = 2500,
                    currentStreakDays = streak,
                    milestonesCount = milestones
                )
            }.collect { newState ->
                _uiState.update { newState }
            }
        }
    }

    private fun formatSessionTime(epochMs: Long): String {
        val sessionDate = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeString = timeFormat.format(Date(epochMs))

        return when (sessionDate) {
            today -> "Today, $timeString"
            today.minusDays(1) -> "Yesterday, $timeString"
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                dateFormat.format(Date(epochMs))
            }
        }
    }
}
