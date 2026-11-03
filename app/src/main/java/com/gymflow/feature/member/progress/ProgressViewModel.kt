package com.gymflow.feature.member.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val totalWorkouts: Int = 18,
    val totalSets: Int = 342,
    val totalReps: Int = 4126,
    val caloriesBurned: Int = 5480,
    val totalVolumeKg: Float = 48250f,
    val weightProgressPoints: List<ProgressChartPoint> = default30DayPoints,
    val minY: Float = 66.5f,
    val maxY: Float = 69.5f,
    val personalRecords: List<PersonalRecordItem> = defaultPersonalRecords
) {
    companion object {
        val default7DayPoints = listOf(
            ProgressChartPoint("Mon", 68.1f, "Oct 27"),
            ProgressChartPoint("Tue", 68.0f, "Oct 28"),
            ProgressChartPoint("Wed", 67.9f, "Oct 29"),
            ProgressChartPoint("Thu", 67.8f, "Oct 30"),
            ProgressChartPoint("Fri", 67.7f, "Oct 31"),
            ProgressChartPoint("Sat", 67.6f, "Nov 01"),
            ProgressChartPoint("Sun", 67.5f, "Nov 02")
        )

        val default30DayPoints = listOf(
            ProgressChartPoint("Apr 1", 69.2f, "Apr 01"),
            ProgressChartPoint("Apr 8", 68.7f, "Apr 08"),
            ProgressChartPoint("Apr 15", 68.3f, "Apr 15"),
            ProgressChartPoint("Apr 22", 67.9f, "Apr 22"),
            ProgressChartPoint("Apr 29", 67.5f, "Apr 29")
        )

        val default3MonthPoints = listOf(
            ProgressChartPoint("Aug", 70.4f, "August"),
            ProgressChartPoint("Sep", 69.1f, "September"),
            ProgressChartPoint("Oct", 67.5f, "October")
        )

        val defaultPersonalRecords = listOf(
            PersonalRecordItem("Barbell Bench Press", 92.5f, "80 kg × 5 reps", "Oct 24"),
            PersonalRecordItem("Barbell Back Squat", 125.0f, "110 kg × 4 reps", "Oct 19"),
            PersonalRecordItem("Deadlift", 150.0f, "135 kg × 3 reps", "Oct 12"),
            PersonalRecordItem("Overhead Press", 62.5f, "55 kg × 4 reps", "Oct 05")
        )
    }
}

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val weightRepository: WeightRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        observeWeightData()
    }

    private fun observeWeightData() {
        val userId = tokenManager.getUserIdSync() ?: "default_member"
        viewModelScope.launch {
            weightRepository.getWeightHistory(userId).collect { logs ->
                if (logs.isNotEmpty()) {
                    val points = logs.take(7).reversed().map { log ->
                        val date = Date(log.recordedAtEpochMs)
                        val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
                        val dateLabel = SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
                        ProgressChartPoint(dayLabel, log.postWeightKg, dateLabel)
                    }
                    val min = (points.minOf { it.weightKg } - 1.0f).coerceAtLeast(0f)
                    val max = points.maxOf { it.weightKg } + 1.0f

                    _uiState.update { current ->
                        current.copy(
                            weightProgressPoints = points,
                            minY = Math.round(min * 10f) / 10f,
                            maxY = Math.round(max * 10f) / 10f
                        )
                    }
                }
            }
        }
    }

    fun selectTimeRange(tab: TimeRangeTab) {
        _uiState.update { current ->
            when (tab) {
                TimeRangeTab.SEVEN_DAYS -> current.copy(
                    selectedTab = tab,
                    totalWorkouts = 5,
                    totalSets = 95,
                    totalReps = 1140,
                    caloriesBurned = 1520,
                    totalVolumeKg = 13450f,
                    weightProgressPoints = if (current.weightProgressPoints.isNotEmpty()) current.weightProgressPoints else ProgressUiState.default7DayPoints,
                    minY = 67.0f,
                    maxY = 68.5f
                )
                TimeRangeTab.THIRTY_DAYS -> current.copy(
                    selectedTab = tab,
                    totalWorkouts = 18,
                    totalSets = 342,
                    totalReps = 4126,
                    caloriesBurned = 5480,
                    totalVolumeKg = 48250f,
                    weightProgressPoints = if (current.weightProgressPoints.isNotEmpty()) current.weightProgressPoints else ProgressUiState.default30DayPoints,
                    minY = 66.5f,
                    maxY = 69.5f
                )
                TimeRangeTab.THREE_MONTHS -> current.copy(
                    selectedTab = tab,
                    totalWorkouts = 52,
                    totalSets = 980,
                    totalReps = 11800,
                    caloriesBurned = 16200,
                    totalVolumeKg = 142000f,
                    weightProgressPoints = ProgressUiState.default3MonthPoints,
                    minY = 66.0f,
                    maxY = 71.0f
                )
                TimeRangeTab.SIX_MONTHS -> current.copy(
                    selectedTab = tab,
                    totalWorkouts = 104,
                    totalSets = 1960,
                    totalReps = 23600,
                    caloriesBurned = 32400,
                    totalVolumeKg = 284000f,
                    weightProgressPoints = listOf(
                        ProgressChartPoint("May", 72.0f, "May"),
                        ProgressChartPoint("Jul", 70.8f, "July"),
                        ProgressChartPoint("Sep", 69.1f, "September"),
                        ProgressChartPoint("Nov", 67.5f, "November")
                    ),
                    minY = 66.0f,
                    maxY = 73.0f
                )
                TimeRangeTab.ONE_YEAR -> current.copy(
                    selectedTab = tab,
                    totalWorkouts = 208,
                    totalSets = 3920,
                    totalReps = 47200,
                    caloriesBurned = 64800,
                    totalVolumeKg = 568000f,
                    weightProgressPoints = listOf(
                        ProgressChartPoint("Nov '24", 74.0f, "Nov 2024"),
                        ProgressChartPoint("Feb '25", 72.2f, "Feb 2025"),
                        ProgressChartPoint("May '25", 70.5f, "May 2025"),
                        ProgressChartPoint("Aug '25", 68.8f, "Aug 2025"),
                        ProgressChartPoint("Nov '25", 67.5f, "Nov 2025")
                    ),
                    minY = 66.0f,
                    maxY = 75.0f
                )
            }
        }
    }
}
