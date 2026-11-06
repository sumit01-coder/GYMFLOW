package com.gymflow.feature.member.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.SetLogEntity
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.database.WorkoutSessionWithSets
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.calculator.OneRepMaxCalculator
import com.gymflow.domain.model.WeightLog
import com.gymflow.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class TimeRangeTab(val label: String) {
    SEVEN_DAYS("7D"),
    THIRTY_DAYS("30D"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    ONE_YEAR("1Y")
}

data class ProgressStatItem(
    val title: String,
    val value: String,
    val subtitle: String,
    val changePercent: String? = null
)

data class ProgressChartPoint(
    val label: String,
    val weightKg: Float,
    val fullDate: String
)

data class PersonalRecordItem(
    val exerciseName: String,
    val oneRepMaxKg: Float,
    val bestSet: String,
    val achievedDate: String
)

data class ProgressUiState(
    val selectedTab: TimeRangeTab = TimeRangeTab.THIRTY_DAYS,
    val totalWorkouts: Int = 0,
    val totalSets: Int = 0,
    val totalReps: Int = 0,
    val caloriesBurned: Int = 0,
    val totalVolumeKg: Float = 0f,
    val weightProgressPoints: List<ProgressChartPoint> = emptyList(),
    val minY: Float = 0f,
    val maxY: Float = 100f,
    val personalRecords: List<PersonalRecordItem> = emptyList(),
    // ── user profile stats ──
    val userAge: Int = 0,
    val userHeightCm: Int = 0,
    val userGoal: String = "",
    val userGender: String = ""
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao,
    private val weightRepository: WeightRepository,
    private val tokenManager: TokenManager,
    private val userDao: UserDao
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(TimeRangeTab.THIRTY_DAYS)
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        observeProgressData()
    }

    private fun observeProgressData() {
        val userId = tokenManager.getUserIdSync().takeIf { !it.isNullOrBlank() } ?: "default_member"

        viewModelScope.launch {
            combine(
                _selectedTab,
                workoutSessionDao.getAllSessionsWithSets(userId),
                weightRepository.getWeightHistory(userId),
                userDao.getUserFlow(userId)
            ) { tab, sessionsWithSets, weightLogs, userEntity ->
                buildUiState(tab, sessionsWithSets, weightLogs,
                    userAge = userEntity?.age ?: 0,
                    userHeightCm = userEntity?.heightCm?.toInt() ?: 0,
                    userGoal = userEntity?.fitnessGoal ?: "",
                    userGender = tokenManager.getUserGenderSync()
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectTimeRange(tab: TimeRangeTab) {
        _selectedTab.value = tab
    }

    private fun buildUiState(
        tab: TimeRangeTab,
        sessionsWithSets: List<WorkoutSessionWithSets>,
        weightLogs: List<WeightLog>,
        userAge: Int = 0,
        userHeightCm: Int = 0,
        userGoal: String = "",
        userGender: String = ""
    ): ProgressUiState {
        // Filter completed sessions
        val completedSessions = sessionsWithSets.filter { it.session.isCompleted || it.session.endTimeEpochMs != null }

        val now = System.currentTimeMillis()
        val cutoffMs = when (tab) {
            TimeRangeTab.SEVEN_DAYS -> now - 7L * 24 * 60 * 60 * 1000
            TimeRangeTab.THIRTY_DAYS -> now - 30L * 24 * 60 * 60 * 1000
            TimeRangeTab.THREE_MONTHS -> now - 90L * 24 * 60 * 60 * 1000
            TimeRangeTab.SIX_MONTHS -> now - 180L * 24 * 60 * 60 * 1000
            TimeRangeTab.ONE_YEAR -> now - 365L * 24 * 60 * 60 * 1000
        }

        val inRangeSessions = completedSessions.filter { it.session.startTimeEpochMs >= cutoffMs }
        val inRangeSetLogs = inRangeSessions.flatMap { it.setLogs }

        val totalWorkouts = inRangeSessions.size
        val totalSets = if (inRangeSetLogs.isNotEmpty()) inRangeSetLogs.size else inRangeSessions.sumOf { it.session.setsCompleted }
        val totalReps = inRangeSetLogs.sumOf { it.actualReps }
        val computedVolume = inRangeSetLogs.sumOf { (it.actualWeightKg * it.actualReps).toDouble() }.toFloat()
        val sessionVolume = inRangeSessions.sumOf { it.session.totalVolumeKg.toDouble() }.toFloat()
        val totalVolumeKg = if (computedVolume > 0f) computedVolume else sessionVolume
        val caloriesBurned = inRangeSessions.sumOf { it.session.caloriesBurned }

        // All-time Personal Records dynamically calculated using OneRepMaxCalculator
        val allSetLogs = completedSessions.flatMap { it.setLogs }
        val personalRecords = calculatePersonalRecords(allSetLogs)

        // Weight progression points for selected time range
        val filteredWeightLogs = weightLogs.filter { it.recordedAtEpochMs >= cutoffMs }
        val (chartPoints, minY, maxY) = if (filteredWeightLogs.isNotEmpty()) {
            val sortedLogs = filteredWeightLogs.sortedBy { it.recordedAtEpochMs }
            val points = sortedLogs.map { log ->
                val weight = log.postWeightKg ?: log.preWeightKg
                val date = Date(log.recordedAtEpochMs)
                val dayLabel = SimpleDateFormat("MMM d", Locale.US).format(date)
                val fullDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date)
                ProgressChartPoint(dayLabel, weight, fullDate)
            }
            val min = (points.minOf { it.weightKg } - 1.0f).coerceAtLeast(0f)
            val max = points.maxOf { it.weightKg } + 1.0f
            Triple(points, Math.round(min * 10f) / 10f, Math.round(max * 10f) / 10f)
        } else {
            Triple(emptyList(), 0f, 100f)
        }

        return ProgressUiState(
            selectedTab = tab,
            totalWorkouts = totalWorkouts,
            totalSets = totalSets,
            totalReps = totalReps,
            caloriesBurned = caloriesBurned,
            totalVolumeKg = totalVolumeKg,
            weightProgressPoints = chartPoints,
            minY = minY,
            maxY = maxY,
            personalRecords = personalRecords,
            userAge = userAge,
            userHeightCm = userHeightCm,
            userGoal = userGoal,
            userGender = userGender
        )
    }

    private fun calculatePersonalRecords(setLogs: List<SetLogEntity>): List<PersonalRecordItem> {
        return setLogs
            .filter { it.actualWeightKg > 0f && it.actualReps > 0 && it.exerciseName.isNotBlank() }
            .groupBy { it.exerciseName.trim() }
            .mapNotNull { (exerciseName, logs) ->
                val bestLog = logs.maxByOrNull { log ->
                    OneRepMaxCalculator.calculate1RM(log.actualWeightKg, log.actualReps)
                } ?: return@mapNotNull null

                val calculated1RM = OneRepMaxCalculator.calculate1RM(bestLog.actualWeightKg, bestLog.actualReps)
                val weightDisplay = if (bestLog.actualWeightKg % 1f == 0f) {
                    "${bestLog.actualWeightKg.toInt()} kg"
                } else {
                    "${bestLog.actualWeightKg} kg"
                }
                val bestSetStr = "$weightDisplay × ${bestLog.actualReps} reps"
                val dateStr = if (bestLog.completedAtEpochMs > 0) {
                    SimpleDateFormat("MMM dd", Locale.US).format(Date(bestLog.completedAtEpochMs))
                } else {
                    "Logged"
                }

                PersonalRecordItem(
                    exerciseName = exerciseName,
                    oneRepMaxKg = calculated1RM,
                    bestSet = bestSetStr,
                    achievedDate = dateStr
                )
            }
            .sortedByDescending { it.oneRepMaxKg }
    }
}
