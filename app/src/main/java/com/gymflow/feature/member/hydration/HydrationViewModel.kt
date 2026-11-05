package com.gymflow.feature.member.hydration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.common.Resource
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.HydrationLog
import com.gymflow.domain.repository.WeightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val tokenManager: TokenManager,
    private val firestoreSyncManager: FirestoreSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HydrationUiState())
    val uiState: StateFlow<HydrationUiState> = _uiState.asStateFlow()

    init {
        val userId = currentUserId()
        viewModelScope.launch {
            try {
                firestoreSyncManager.pullAllUserDataFromFirestore(userId)
            } catch (_: Exception) {}
        }
        observeTodayHydration()
    }

    private fun currentUserId(): String = tokenManager.getUserIdSync() ?: "default_member"

    private fun observeTodayHydration() {
        val userId = currentUserId()
        viewModelScope.launch {
            combine(
                weightRepository.getTodayHydration(userId),
                weightRepository.getTodayHydrationLogs(userId)
            ) { totalMl, logs ->
                _uiState.update { current ->
                    current.copy(
                        currentIntakeMl = totalMl,
                        todayLogs = logs
                    )
                }
            }.collect {}
        }
    }

    fun addWater(amountMl: Int) {
        val userId = currentUserId()
        val goal = _uiState.value.dailyGoalMl
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            weightRepository.logHydration(
                memberId = userId,
                amountMl = amountMl,
                dailyGoalMl = goal
            ).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { current ->
                            val updatedDeficit = (current.postWorkoutDeficitMl - amountMl).coerceAtLeast(0)
                            val updatedLogs = (listOf(resource.data) + current.todayLogs).distinctBy { it.id }
                            val updatedTotal = (current.currentIntakeMl + amountMl).coerceAtLeast(0)
                            current.copy(
                                isSaving = false,
                                isCustomDialogOpen = false,
                                currentIntakeMl = updatedTotal,
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

    fun deleteLog(logId: String) {
        val targetLog = _uiState.value.todayLogs.find { it.id == logId }
        val amount = targetLog?.amountMl ?: 0
        // Optimistic UI update so the log vanishes instantly and intake is reduced
        _uiState.update { current ->
            val updatedLogs = current.todayLogs.filter { it.id != logId }
            val updatedTotal = (current.currentIntakeMl - amount).coerceAtLeast(0)
            val updatedDeficit = (current.postWorkoutDeficitMl + amount).coerceAtLeast(0)
            current.copy(
                todayLogs = updatedLogs,
                currentIntakeMl = updatedTotal,
                postWorkoutDeficitMl = updatedDeficit
            )
        }
        viewModelScope.launch {
            weightRepository.deleteHydrationLog(logId).collect {}
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
        val userId = currentUserId()
        _uiState.update {
            it.copy(
                currentIntakeMl = 0,
                todayLogs = emptyList()
            )
        }
        viewModelScope.launch {
            weightRepository.clearTodayHydration(userId).collect {}
        }
    }
}
