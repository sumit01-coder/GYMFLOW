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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    val preWeightKg: Float = 0f,
    val postWeightKg: Float = 0f,
    val deltaAnalysis: WeightDifferenceCalculator.WeightDeltaAnalysis =
        WeightDifferenceCalculator.analyze(0f, 0f),
    val trendPoints: List<WeightTrendPoint> = emptyList(),
    val minY: Float = 0f,
    val maxY: Float = 100f,
    val recentLogs: List<WeightLog> = emptyList(),
    val isLogDialogOpen: Boolean = false
)

class WeightViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    fun onFilterSelected(filter: WeightTimeFilter) {
        _uiState.update { current ->
            current.copy(selectedFilter = filter)
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
                memberId = "current_user",
                preWeightKg = preKg,
                postWeightKg = postKg,
                deltaKg = analysis.deltaKg,
                recordedAtEpochMs = System.currentTimeMillis(),
                notes = notes
            )

            _uiState.update { current ->
                val updatedLogs = listOf(newLog) + current.recentLogs
                val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(Date())
                val dateLabel = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date())
                val updatedPoints = current.trendPoints + WeightTrendPoint(dayLabel, postKg, dateLabel)
                val min = (updatedPoints.minOf { it.weightKg } - 0.5f).coerceAtLeast(0f)
                val max = updatedPoints.maxOf { it.weightKg } + 0.5f

                current.copy(
                    preWeightKg = preKg,
                    postWeightKg = postKg,
                    deltaAnalysis = analysis,
                    recentLogs = updatedLogs,
                    trendPoints = updatedPoints,
                    minY = Math.round(min * 10f) / 10f,
                    maxY = Math.round(max * 10f) / 10f,
                    isLogDialogOpen = false
                )
            }
        }
    }
}
