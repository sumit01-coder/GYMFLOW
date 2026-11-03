package com.gymflow.feature.member.workout

import androidx.lifecycle.SavedStateHandle
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
import java.net.URLDecoder
import javax.inject.Inject

data class RestTimerUiState(
    val initialSeconds: Int = 45,
    val remainingSeconds: Int = 45,
    val nextSetText: String = "Set 3 of 3 — Bench Press (12 reps · 20 kg)"
) {
    val progress: Float
        get() = if (initialSeconds > 0) remainingSeconds.toFloat() / initialSeconds.toFloat() else 0f

    val formattedRemainingTime: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
}

@HiltViewModel
class RestTimerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rawNextSet: String? = savedStateHandle.get<String>("nextSetText")
    private val decodedNextSet: String = rawNextSet?.let {
        try {
            URLDecoder.decode(it, "UTF-8")
        } catch (e: Exception) {
            it
        }
    } ?: "Set 3 of 3 — Bench Press (12 reps · 20 kg)"

    private val durationSeconds: Int = savedStateHandle?.get<String>("durationSeconds")?.toIntOrNull() ?: 45

    private val _uiState = MutableStateFlow(
        RestTimerUiState(
            initialSeconds = durationSeconds,
            remainingSeconds = durationSeconds,
            nextSetText = decodedNextSet
        )
    )
    val uiState: StateFlow<RestTimerUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        startCountdown()
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive && _uiState.value.remainingSeconds > 0) {
                delay(1000)
                _uiState.update { it.copy(remainingSeconds = (it.remainingSeconds - 1).coerceAtLeast(0)) }
            }
        }
    }

    fun add15Seconds() {
        _uiState.update {
            it.copy(
                remainingSeconds = it.remainingSeconds + 15,
                initialSeconds = maxOf(it.initialSeconds, it.remainingSeconds + 15)
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
