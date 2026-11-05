package com.gymflow.feature.member.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StopwatchMode {
    STOPWATCH,
    COUNTDOWN
}

sealed interface StopwatchEvent {
    data object CountdownFinished : StopwatchEvent
}

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
    val mode: StopwatchMode = StopwatchMode.STOPWATCH,

    // Forward Stopwatch State
    val elapsedMillis: Long = 0L,
    val isRunning: Boolean = false,
    val laps: List<StopwatchLap> = emptyList(),
    val targetPresetSeconds: Int? = null,

    // Reverse Countdown State
    val countdownTotalMillis: Long = 60_000L,
    val countdownRemainingMillis: Long = 60_000L,
    val isCountdownRunning: Boolean = false,
    val isCountdownFinished: Boolean = false,
    val selectedCountdownPresetSeconds: Int? = 60,
    val customMinutes: Int = 1,
    val customSeconds: Int = 0
) {
    // Forward Stopwatch formatters
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

    // Reverse Countdown formatters
    val countdownProgress: Float
        get() = if (countdownTotalMillis > 0L) {
            (countdownRemainingMillis.toFloat() / countdownTotalMillis.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val countdownMinutesPart: String
        get() {
            val totalSeconds = countdownRemainingMillis / 1000
            val minutes = totalSeconds / 60
            return "%02d".format(minutes)
        }

    val countdownSecondsPart: String
        get() {
            val totalSeconds = countdownRemainingMillis / 1000
            val seconds = totalSeconds % 60
            return "%02d".format(seconds)
        }

    val countdownHundredthsPart: String
        get() {
            val hundredths = (countdownRemainingMillis % 1000) / 10
            return "%02d".format(hundredths)
        }

    val formattedCountdownTime: String
        get() = formatStopwatchTime(countdownRemainingMillis)
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

    private val _events = MutableSharedFlow<StopwatchEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<StopwatchEvent> = _events.asSharedFlow()

    // Forward stopwatch references
    private var stopwatchJob: Job? = null
    private var startTimeMillis: Long = 0L
    private var accumulatedMillis: Long = 0L
    private var lastLapTotalMillis: Long = 0L

    // Reverse countdown references
    private var countdownJob: Job? = null
    private var countdownTargetEndMillis: Long = 0L

    fun setMode(mode: StopwatchMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    // --- FORWARD STOPWATCH ACTIONS ---
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

        stopwatchJob?.cancel()
        stopwatchJob = viewModelScope.launch {
            while (isActive) {
                accumulatedMillis = System.currentTimeMillis() - startTimeMillis
                _uiState.update { it.copy(elapsedMillis = accumulatedMillis) }
                delay(20) // ~50fps smooth precision display
            }
        }
    }

    fun pause() {
        stopwatchJob?.cancel()
        accumulatedMillis = System.currentTimeMillis() - startTimeMillis
        _uiState.update {
            it.copy(
                isRunning = false,
                elapsedMillis = accumulatedMillis
            )
        }
    }

    fun reset() {
        stopwatchJob?.cancel()
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

    // --- REVERSE COUNTDOWN TIMER ACTIONS ---
    fun selectCountdownPreset(seconds: Int) {
        countdownJob?.cancel()
        val totalMs = seconds * 1000L
        _uiState.update {
            it.copy(
                countdownTotalMillis = totalMs,
                countdownRemainingMillis = totalMs,
                isCountdownRunning = false,
                isCountdownFinished = false,
                selectedCountdownPresetSeconds = seconds
            )
        }
    }

    fun setCustomCountdown(minutes: Int, seconds: Int) {
        countdownJob?.cancel()
        val totalSeconds = (minutes * 60 + seconds).coerceAtLeast(1)
        val totalMs = totalSeconds * 1000L
        _uiState.update {
            it.copy(
                countdownTotalMillis = totalMs,
                countdownRemainingMillis = totalMs,
                isCountdownRunning = false,
                isCountdownFinished = false,
                selectedCountdownPresetSeconds = null,
                customMinutes = minutes,
                customSeconds = seconds
            )
        }
    }

    fun startCountdown() {
        val currentRemaining = _uiState.value.countdownRemainingMillis
        if (_uiState.value.isCountdownRunning) return

        val durationToRun = if (currentRemaining <= 0L) {
            _uiState.value.countdownTotalMillis
        } else {
            currentRemaining
        }

        countdownTargetEndMillis = System.currentTimeMillis() + durationToRun

        _uiState.update {
            it.copy(
                countdownRemainingMillis = durationToRun,
                isCountdownRunning = true,
                isCountdownFinished = false
            )
        }

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val remaining = (countdownTargetEndMillis - System.currentTimeMillis()).coerceAtLeast(0L)
                _uiState.update { it.copy(countdownRemainingMillis = remaining) }

                if (remaining <= 0L) {
                    _uiState.update {
                        it.copy(
                            isCountdownRunning = false,
                            isCountdownFinished = true
                        )
                    }
                    _events.tryEmit(StopwatchEvent.CountdownFinished)
                    break
                }
                delay(20) // ~50fps smooth precision display
            }
        }
    }

    fun pauseCountdown() {
        countdownJob?.cancel()
        val remaining = (countdownTargetEndMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        _uiState.update {
            it.copy(
                isCountdownRunning = false,
                countdownRemainingMillis = remaining
            )
        }
    }

    fun resetCountdown() {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(
                countdownRemainingMillis = it.countdownTotalMillis,
                isCountdownRunning = false,
                isCountdownFinished = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopwatchJob?.cancel()
        countdownJob?.cancel()
    }
}
