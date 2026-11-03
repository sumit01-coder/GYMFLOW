package com.gymflow.feature.member.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StopwatchLap(
    val lapNumber: Int,
    val splitMillis: Long,
    val totalMillis: Long,
    val isBest: Boolean = false,
    val isSlowest: Boolean = false
) {
    val formattedSplit: String
        get() = formatStopwatchTime(splitMillis)

    val formattedTotal: String
        get() = formatStopwatchTime(totalMillis)
}

data class StopwatchUiState(
    val elapsedMillis: Long = 0L,
    val isRunning: Boolean = false,
    val laps: List<StopwatchLap> = emptyList(),
    val targetPresetSeconds: Int? = null,
    val isCountdown: Boolean = false
) {
    val formattedTime: String
        get() = formatStopwatchTime(elapsedMillis)

    val minutesPart: String
        get() {
            val totalSeconds = elapsedMillis / 1000
            val minutes = totalSeconds / 60
            return "%02d".format(minutes)
        }

    val secondsPart: String
        get() {
            val totalSeconds = elapsedMillis / 1000
            val seconds = totalSeconds % 60
            return "%02d".format(seconds)
        }

    val hundredthsPart: String
        get() {
            val hundredths = (elapsedMillis % 1000) / 10
            return "%02d".format(hundredths)
        }
}

fun formatStopwatchTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hundredths = (millis % 1000) / 10
    return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
}

@HiltViewModel
class StopwatchViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StopwatchUiState())
    val uiState: StateFlow<StopwatchUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var startTimeMillis: Long = 0L
    private var accumulatedMillis: Long = 0L
    private var lastLapTotalMillis: Long = 0L

    fun toggleStartPause() {
        if (_uiState.value.isRunning) {
            pause()
        } else {
            start()
        }
    }

    fun start() {
        if (_uiState.value.isRunning) return
        startTimeMillis = System.currentTimeMillis() - accumulatedMillis

        _uiState.update { it.copy(isRunning = true) }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                accumulatedMillis = System.currentTimeMillis() - startTimeMillis
                _uiState.update { it.copy(elapsedMillis = accumulatedMillis) }
                delay(20) // ~50fps smooth precision display
            }
        }
    }

    fun pause() {
        timerJob?.cancel()
        accumulatedMillis = System.currentTimeMillis() - startTimeMillis
        _uiState.update {
            it.copy(
                isRunning = false,
                elapsedMillis = accumulatedMillis
            )
        }
    }

    fun reset() {
        timerJob?.cancel()
        startTimeMillis = 0L
        accumulatedMillis = 0L
        lastLapTotalMillis = 0L
        _uiState.update {
            it.copy(
                elapsedMillis = 0L,
                isRunning = false,
                laps = emptyList(),
                targetPresetSeconds = null
            )
        }
    }

    fun recordLap() {
        val currentTotal = accumulatedMillis
        if (currentTotal == 0L) return

        val split = currentTotal - lastLapTotalMillis
        lastLapTotalMillis = currentTotal

        val currentLaps = _uiState.value.laps
        val newLapNumber = currentLaps.size + 1

        val newLap = StopwatchLap(
            lapNumber = newLapNumber,
            splitMillis = split,
            totalMillis = currentTotal
        )

        val updatedRaw = listOf(newLap) + currentLaps

        // Compute best and slowest
        val minSplit = updatedRaw.minOfOrNull { it.splitMillis }
        val maxSplit = updatedRaw.maxOfOrNull { it.splitMillis }
        val showHighlights = updatedRaw.size >= 2

        val decoratedLaps = updatedRaw.map { lap ->
            lap.copy(
                isBest = showHighlights && lap.splitMillis == minSplit,
                isSlowest = showHighlights && lap.splitMillis == maxSplit
            )
        }

        _uiState.update { it.copy(laps = decoratedLaps) }
    }

    fun setQuickPreset(seconds: Int) {
        reset()
        _uiState.update { it.copy(targetPresetSeconds = seconds) }
    }
}

