package com.gymflow.feature.member.hydration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.domain.model.HydrationLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class HydrationUiState(
    val currentIntakeMl: Int = 0,
    val dailyGoalMl: Int = 3000,
    val todayLogs: List<HydrationLog> = emptyList(),
    val postWorkoutDeficitMl: Int = 0,
    val isCustomDialogOpen: Boolean = false
) {
    val progressPercent: Float
        get() = if (dailyGoalMl > 0) (currentIntakeMl.toFloat() / dailyGoalMl.toFloat()).coerceIn(0f, 1f) else 0f
}

class HydrationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HydrationUiState())
    val uiState: StateFlow<HydrationUiState> = _uiState.asStateFlow()

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val newLog = HydrationLog(
                id = UUID.randomUUID().toString(),
                memberId = "current_user",
                amountMl = amountMl,
                dailyGoalMl = _uiState.value.dailyGoalMl,
                recordedAtEpochMs = System.currentTimeMillis()
            )

            _uiState.update { current ->
                val updatedTotal = current.currentIntakeMl + amountMl
                val updatedLogs = listOf(newLog) + current.todayLogs
                val updatedDeficit = (current.postWorkoutDeficitMl - amountMl).coerceAtLeast(0)
                current.copy(
                    currentIntakeMl = updatedTotal,
                    todayLogs = updatedLogs,
                    postWorkoutDeficitMl = updatedDeficit,
                    isCustomDialogOpen = false
                )
            }
        }
    }

    fun openCustomDialog() {
        _uiState.update { it.copy(isCustomDialogOpen = true) }
    }

    fun closeCustomDialog() {
        _uiState.update { it.copy(isCustomDialogOpen = false) }
    }

    fun updateGoal(newGoalMl: Int) {
        _uiState.update { it.copy(dailyGoalMl = newGoalMl.coerceAtLeast(500)) }
    }

    fun resetDaily() {
        _uiState.update {
            it.copy(
                currentIntakeMl = 0,
                todayLogs = emptyList()
            )
        }
    }
}
