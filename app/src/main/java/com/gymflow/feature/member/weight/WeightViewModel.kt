package com.gymflow.feature.member.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.domain.calculator.WeightDifferenceCalculator
import com.gymflow.domain.model.WeightLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class WeightTimeFilter(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

data class WeightTrendPoint(
    val dayLabel: String,
    val weightKg: Float,
    val dateString: String
)

data class WeightUiState(
    val selectedFilter: WeightTimeFilter = WeightTimeFilter.DAILY,
    val preWeightKg: Float = 67.80f,
    val postWeightKg: Float = 67.50f,
    val deltaAnalysis: WeightDifferenceCalculator.WeightDeltaAnalysis =
        WeightDifferenceCalculator.analyze(67.80f, 67.50f),
    val trendPoints: List<WeightTrendPoint> = defaultTrendPoints,
    val minY: Float = 67.0f,
    val maxY: Float = 68.5f,
    val recentLogs: List<WeightLog> = defaultRecentLogs,
    val isLogDialogOpen: Boolean = false
) {
    companion object {
        val defaultTrendPoints = listOf(
            WeightTrendPoint("Mon", 68.30f, "Oct 27"),
            WeightTrendPoint("Tue", 68.10f, "Oct 28"),
            WeightTrendPoint("Wed", 68.00f, "Oct 29"),
            WeightTrendPoint("Thu", 67.90f, "Oct 30"),
            WeightTrendPoint("Fri", 67.80f, "Oct 31"),
            WeightTrendPoint("Sat", 67.65f, "Nov 01"),
            WeightTrendPoint("Sun", 67.50f, "Nov 02")
        )

        val defaultRecentLogs = listOf(
            WeightLog(
                id = "log-1",
                memberId = "alex-1",
                preWeightKg = 67.80f,
                postWeightKg = 67.50f,
                deltaKg = -0.30f,
                recordedAtEpochMs = System.currentTimeMillis() - 86400000L,
                notes = "Post Chest & Triceps session"
            ),
            WeightLog(
                id = "log-2",
                memberId = "alex-1",
                preWeightKg = 68.10f,
                postWeightKg = 67.70f,
                deltaKg = -0.40f,
                recordedAtEpochMs = System.currentTimeMillis() - (86400000L * 3),
                notes = "Heavy leg day sweat session"
            ),
            WeightLog(
                id = "log-3",
                memberId = "alex-1",
                preWeightKg = 68.30f,
                postWeightKg = 68.00f,
                deltaKg = -0.30f,
                recordedAtEpochMs = System.currentTimeMillis() - (86400000L * 6),
                notes = "Morning fasted workout"
            )
        )
    }
}

class WeightViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    fun onFilterSelected(filter: WeightTimeFilter) {
        _uiState.update { current ->
            val points = when (filter) {
                WeightTimeFilter.DAILY -> WeightUiState.defaultTrendPoints
                WeightTimeFilter.WEEKLY -> listOf(
                    WeightTrendPoint("W1", 68.60f, "Oct 06 - 12"),
                    WeightTrendPoint("W2", 68.30f, "Oct 13 - 19"),
                    WeightTrendPoint("W3", 67.95f, "Oct 20 - 26"),
                    WeightTrendPoint("W4", 67.50f, "Oct 27 - Nov 02")
                )
                WeightTimeFilter.MONTHLY -> listOf(
                    WeightTrendPoint("Aug", 69.40f, "August"),
                    WeightTrendPoint("Sep", 68.70f, "September"),
                    WeightTrendPoint("Oct", 67.80f, "October"),
                    WeightTrendPoint("Nov", 67.50f, "November")
                )
            }
            val min = (points.minOf { it.weightKg } - 0.5f)
            val max = (points.maxOf { it.weightKg } + 0.5f)
            current.copy(
                selectedFilter = filter,
                trendPoints = points,
                minY = Math.round(min * 10f) / 10f,
                maxY = Math.round(max * 10f) / 10f
            )
        }
    }

    fun openLogDialog() {
        _uiState.update { it.copy(isLogDialogOpen = true) }
    }

    fun closeLogDialog() {
        _uiState.update { it.copy(isLogDialogOpen = false) }
    }

    fun recordWeight(preKg: Float, postKg: Float, notes: String = "") {
        viewModelScope.launch {
            val analysis = WeightDifferenceCalculator.analyze(preKg, postKg)
            val newLog = WeightLog(
                id = UUID.randomUUID().toString(),
                memberId = "alex-1",
                preWeightKg = preKg,
                postWeightKg = postKg,
                deltaKg = analysis.deltaKg,
                recordedAtEpochMs = System.currentTimeMillis(),
                notes = notes
            )

            _uiState.update { current ->
                val updatedLogs = listOf(newLog) + current.recentLogs
                val updatedPoints = current.trendPoints.toMutableList()
                if (updatedPoints.isNotEmpty()) {
                    val last = updatedPoints.last()
                    updatedPoints[updatedPoints.lastIndex] = last.copy(weightKg = postKg)
                }

                current.copy(
                    preWeightKg = preKg,
                    postWeightKg = postKg,
                    deltaAnalysis = analysis,
                    recentLogs = updatedLogs,
                    trendPoints = updatedPoints,
                    isLogDialogOpen = false
                )
            }
        }
    }
}
