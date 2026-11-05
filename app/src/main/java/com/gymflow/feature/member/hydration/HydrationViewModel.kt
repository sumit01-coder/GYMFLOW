package com.gymflow.feature.member.hydration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.common.Resource
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.HydrationLog
import com.gymflow.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class HydrationUiState(
    val currentIntakeMl: Int = 0,
    val dailyGoalMl: Int = 3000,
    val todayLogs: List<HydrationLog> = emptyList(),
    val postWorkoutDeficitMl: Int = 0,
    val isCustomDialogOpen: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val progressPercent: Float
        get() = if (dailyGoalMl > 0) (currentIntakeMl.toFloat() / dailyGoalMl.toFloat()).coerceIn(0f, 1f) else 0f
}

@HiltViewModel
class HydrationViewModel @Inject constructor(
    private val weightRepository: WeightRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HydrationUiState())
    val uiState: StateFlow<HydrationUiState> = _uiState.asStateFlow()

    init {
        observeTodayHydration()
    }

    private fun currentUserId(): String = tokenManager.getUserIdSync() ?: "default_member"

    private fun observeTodayHydration() {
        val userId = currentUserId()
        viewModelScope.launch {
            weightRepository.getTodayHydration(userId).collect { totalMl ->
                _uiState.update { current ->
                    current.copy(currentIntakeMl = totalMl)
                }
            }
        }
    }

    fun addWater(amountMl: Int) {
        val userId = currentUserId()
        val goal = _uiState.value.dailyGoalMl
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val newLog = HydrationLog(
                id = UUID.randomUUID().toString(),
                memberId = userId,
                amountMl = amountMl,
                dailyGoalMl = goal,
                recordedAtEpochMs = System.currentTimeMillis()
            )

            weightRepository.logHydration(
                memberId = userId,
                amountMl = amountMl,
                dailyGoalMl = goal
            ).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { current ->
                            val updatedLogs = listOf(newLog) + current.todayLogs
                            val updatedDeficit = (current.postWorkoutDeficitMl - amountMl).coerceAtLeast(0)
                            current.copy(
                                isSaving = false,
                                isCustomDialogOpen = false,
                                todayLogs = updatedLogs,
                                postWorkoutDeficitMl = updatedDeficit
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isSaving = false, errorMessage = resource.message) }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                }
            }
        }
    }

    fun saveIntake(targetTotalMl: Int) {
        val current = _uiState.value.currentIntakeMl
        val clampedTarget = targetTotalMl.coerceAtLeast(0)
        val delta = clampedTarget - current
        if (delta != 0) {
            addWater(delta)
        }
    }

    fun setLiveIntake(intakeMl: Int) {
        _uiState.update { it.copy(currentIntakeMl = intakeMl.coerceAtLeast(0)) }
    }

    fun openCustomDialog() {
        _uiState.update { it.copy(isCustomDialogOpen = true, errorMessage = null) }
    }

    fun closeCustomDialog() {
        _uiState.update { it.copy(isCustomDialogOpen = false, errorMessage = null) }
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
