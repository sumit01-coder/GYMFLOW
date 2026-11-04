package com.gymflow.feature.trainer.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainerSettingsUiState(
    val coachName: String = "Coach",
    val title: String = "Strength & Conditioning Specialist",
    val email: String = "",
    val experienceYears: Int = 0,
    val specialization: String = "General Strength & Conditioning",
    val gymAffiliation: String = "",
    val totalMembers: Int = 0,
    val activePlansCount: Int = 0,
    val workoutCompletionNotifications: Boolean = true,
    val joinRequestNotifications: Boolean = true,
    val soundAndHaptics: Boolean = true,
    val defaultRestTimerSeconds: Int = 45,
    val unitSystemKg: Boolean = true,
    val showLogoutConfirmation: Boolean = false
)

@HiltViewModel
class TrainerSettingsViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userDao: UserDao,
    private val appUpdateManager: com.gymflow.core.updater.AppUpdateManager
) : ViewModel() {

    fun checkForUpdates() {
        appUpdateManager.checkForUpdate(isManualCheck = true)
    }

    private val _uiState = MutableStateFlow(TrainerSettingsUiState())
    val uiState: StateFlow<TrainerSettingsUiState> = _uiState.asStateFlow()

    init {
        observeCoachInfo()
    }

    private fun observeCoachInfo() {
        viewModelScope.launch {
            val cachedName = tokenManager.getName()
            val cachedEmail = tokenManager.getEmail()
            if (!cachedName.isNullOrBlank()) {
                val formattedName = if (cachedName.startsWith("Coach", ignoreCase = true)) cachedName else "Coach $cachedName"
                _uiState.update {
                    it.copy(
                        coachName = formattedName,
                        email = cachedEmail ?: it.email
                    )
                }
            }

            userDao.getCurrentUserFlow().collect { userEntity ->
                if (userEntity != null && userEntity.role == Role.TRAINER) {
                    val formattedName = if (userEntity.name.startsWith("Coach", ignoreCase = true)) userEntity.name else "Coach ${userEntity.name}"
                    _uiState.update {
                        it.copy(
                            coachName = formattedName,
                            email = userEntity.email
                        )
                    }
                }
            }
        }
    }

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
