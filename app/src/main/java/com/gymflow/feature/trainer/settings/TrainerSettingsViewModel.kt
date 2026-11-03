package com.gymflow.feature.trainer.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TrainerSettingsUiState(
    val coachName: String = "Coach Marcus Vance",
    val title: String = "Certified Strength & Conditioning Specialist (CSCS)",
    val email: String = "marcus.vance@gymflow.com",
    val experienceYears: Int = 5,
    val specialization: String = "Hypertrophy & Strength Conditioning",
    val gymAffiliation: String = "MetroFlex Pro Studio",
    val totalMembers: Int = 32,
    val activePlansCount: Int = 18,
    val workoutCompletionNotifications: Boolean = true,
    val joinRequestNotifications: Boolean = true,
    val soundAndHaptics: Boolean = true,
    val defaultRestTimerSeconds: Int = 45,
    val unitSystemKg: Boolean = true,
    val showLogoutConfirmation: Boolean = false
)

class TrainerSettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TrainerSettingsUiState())
    val uiState: StateFlow<TrainerSettingsUiState> = _uiState.asStateFlow()

    fun toggleWorkoutCompletionNotifications(enabled: Boolean) {
        _uiState.update { it.copy(workoutCompletionNotifications = enabled) }
    }

    fun toggleJoinRequestNotifications(enabled: Boolean) {
        _uiState.update { it.copy(joinRequestNotifications = enabled) }
    }

    fun toggleSoundAndHaptics(enabled: Boolean) {
        _uiState.update { it.copy(soundAndHaptics = enabled) }
    }

    fun setDefaultRestTimer(seconds: Int) {
        _uiState.update { it.copy(defaultRestTimerSeconds = seconds) }
    }

    fun toggleUnitSystem() {
        _uiState.update { it.copy(unitSystemKg = !it.unitSystemKg) }
    }

    fun showLogoutDialog() {
        _uiState.update { it.copy(showLogoutConfirmation = true) }
    }

    fun dismissLogoutDialog() {
        _uiState.update { it.copy(showLogoutConfirmation = false) }
    }
}
