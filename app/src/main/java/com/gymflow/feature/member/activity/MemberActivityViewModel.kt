package com.gymflow.feature.member.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.calculator.CalorieCalculator
import com.gymflow.domain.calculator.Gender
import com.gymflow.domain.calculator.SetInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
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

enum class HistoryFilter(val label: String) {
    ALL_TIME("All Time"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    CUSTOM("Custom")
}

data class DateRange(val startMs: Long, val endMs: Long)

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
    val milestonesCount: Int = 0,
    val selectedFilter: HistoryFilter = HistoryFilter.ALL_TIME,
    val customRange: DateRange? = null
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
    private val firestoreSyncManager: com.gymflow.core.firebase.FirestoreSyncManager,
    private val userDao: UserDao
) : ViewModel() {

    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL_TIME)
    private val _customRange = MutableStateFlow<DateRange?>(null)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()
    val customRange: StateFlow<DateRange?> = _customRange.asStateFlow()

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

    fun setHistoryFilter(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    fun setCustomRange(startMs: Long, endMs: Long) {
        _customRange.value = DateRange(startMs, endMs)
        _historyFilter.value = HistoryFilter.CUSTOM
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            // 1. Delete from local Room SQLite database
            workoutSessionDao.deleteSetLogsForSession(sessionId)
            workoutSessionDao.deleteSession(sessionId)

            // 2. Delete from Cloud Firestore so it never gets pulled back on page switch
            try {
                firestoreSyncManager.deleteWorkoutSession(sessionId)
            } catch (_: Exception) {}
        }
    }

    private fun observeActivityData() {
        val memberId = tokenManager.getUserIdSync() ?: "default_member"

        val now = LocalDate.now()
        val startOfDayMs = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val startOfWeekMs = now.minusDays(6).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        viewModelScope.launch {
            combine(
                workoutSessionDao.getAllSessionsWithSets(memberId),
                weightDao.getTodayHydrationTotal(memberId, startOfDayMs),
                userDao.getUserFlow(memberId),
                _historyFilter,
                _customRange
            ) { sessionsWithSets, todayHydrationNullable, userEntity, filter, customRange ->
                val allCompleted = sessionsWithSets
                    .filter { it.session.isCompleted }
                    .sortedByDescending { it.session.startTimeEpochMs }

                // Compute best1RM map ONCE per emission (not inside per-session loop)
                val best1RmMap = mutableMapOf<String, Float>()
                for (sw in allCompleted) {
                    for (log in sw.setLogs) {
                        if (log.actualWeightKg > 0f && log.actualReps > 0) {
                            val est = CalorieCalculator.estimate1Rm(log.actualWeightKg, log.actualReps)
                            if (est != null) {
                                val current = best1RmMap[log.exerciseName] ?: 0f
                                if (est > current) best1RmMap[log.exerciseName] = est
                            }
                        }
                    }
                }

                // Resolve body weight once per emission
                val gender = if (tokenManager.getUserGenderSync().equals("Female", ignoreCase = true)) {
                    Gender.FEMALE
                } else {
                    Gender.MALE
                }
                val bodyWeightKg = CalorieCalculator.resolveBodyWeightKg(
                    loggedKg = userEntity?.currentWeightKg?.takeIf { it > 0f },
                    heightCm = userEntity?.heightCm?.toInt()?.takeIf { it > 0 } ?: 166,
                    gender = gender
                )

                // Determine filter time window
                val nowMs = System.currentTimeMillis()
                val (filterStart, filterEnd) = when (filter) {
                    HistoryFilter.TODAY -> Pair(startOfDayMs, nowMs)
                    HistoryFilter.THIS_WEEK -> Pair(startOfWeekMs, nowMs)
                    HistoryFilter.THIS_MONTH -> {
                        val startOfMonth = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        Pair(startOfMonth, nowMs)
                    }
                    HistoryFilter.CUSTOM -> {
                        if (customRange != null) Pair(customRange.startMs, customRange.endMs)
                        else Pair(0L, nowMs)
                    }
                    HistoryFilter.ALL_TIME -> Pair(0L, nowMs)
                }

                val filteredSessions = allCompleted.filter {
                    it.session.startTimeEpochMs in filterStart..filterEnd
                }

                val numberFormat = NumberFormat.getNumberInstance(Locale.US)

                val historyItems = filteredSessions.map { sessionWithSets ->
                    val s = sessionWithSets.session
                    val setsList = sessionWithSets.setLogs

                    val actualVolume = if (s.totalVolumeKg > 0f) {
                        s.totalVolumeKg
                    } else {
                        setsList.sumOf { (it.actualWeightKg * it.actualReps).toDouble() }.toFloat()
                    }

                    val durationMins = if (s.durationSeconds > 0) {
                        maxOf(1, s.durationSeconds / 60)
                    } else {
                        45L
                    }

                    // Real kcal — use saved value if already computed, else calculate with engine
                    val calories = if (s.caloriesBurned > 0) {
                        s.caloriesBurned
                    } else {
                        val sessionSecs = s.endTimeEpochMs?.let { (it - s.startTimeEpochMs) / 1000 }
                        val inputs = setsList.map { log ->
                            SetInput(log.exerciseName, log.actualReps, log.actualWeightKg)
                        }
                        CalorieCalculator.estimateSession(
                            sets = inputs,
                            bodyWeightKg = bodyWeightKg,
                            sessionSeconds = sessionSecs,
                            best1RmByExercise = best1RmMap
                        ).totalKcal
                    }

                    val isEstimated = s.caloriesBurned <= 0
                    val setsCount = if (s.setsCompleted > 0) s.setsCompleted else maxOf(1, setsList.size)
                    val timeStr = formatSessionTime(s.startTimeEpochMs)
                    val volumeStr = numberFormat.format(actualVolume.toInt())
                    val detailsText = "$setsCount sets · $volumeStr kg volume · $durationMins min"
                    val caloriesStr = if (isEstimated) "≈ $calories kcal" else "$calories kcal"

                    ActivityWorkoutItem(
                        id = s.id,
                        title = s.workoutTitle.ifBlank { "Workout Session" },
                        details = detailsText,
                        timeFormatted = timeStr,
                        caloriesFormatted = caloriesStr,
                        setsCount = setsCount,
                        totalVolumeKg = actualVolume,
                        durationMinutes = durationMins
                    )
                }

                val thisWeekCount = allCompleted.count { it.session.startTimeEpochMs >= startOfWeekMs }

                // Calculate real streak from completed session dates
                val sessionDates = allCompleted.map {
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
                if (allCompleted.isNotEmpty()) milestones++ // First Step
                if (allCompleted.size >= 10) milestones++ // Iron Habit
                val totalVolume = allCompleted.sumOf { it.session.totalVolumeKg.toDouble() }
                if (totalVolume >= 100000.0) milestones++ // Century Club
                val totalSets = allCompleted.sumOf { it.setLogs.size }
                if (totalSets >= 50) milestones++ // Heavy Lifter
                if ((todayHydrationNullable ?: 0) >= 2500) milestones++ // Hydration milestone

                MemberActivityUiState(
                    isLoading = false,
                    workoutHistory = historyItems,
                    sessionsThisWeekCount = thisWeekCount,
                    todayHydrationMl = todayHydrationNullable ?: 0,
                    hydrationGoalMl = 2500,
                    currentStreakDays = streak,
                    milestonesCount = milestones,
                    selectedFilter = filter,
                    customRange = customRange
                )
            }.flowOn(Dispatchers.Default)
            .collect { newState ->
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
