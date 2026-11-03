package com.gymflow.feature.member.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.domain.model.MemberProfile
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User = User(
        id = "alex-1",
        name = "Alex Johnson",
        email = "alex.johnson@example.com",
        role = Role.MEMBER
    ),
    val profile: MemberProfile = MemberProfile(
        userId = "alex-1",
        heightCm = 178f,
        currentWeightKg = 67.8f,
        fitnessGoal = "Build Muscle",
        age = 28,
        connectedTrainerId = "trainer-1",
        syncStatus = "Connected"
    ),
    val joinedDateFormatted: String = "Joined 12 Apr 2025",
    val pushNotificationsEnabled: Boolean = true,
    val workoutRemindersEnabled: Boolean = true,
    val weightRemindersEnabled: Boolean = true,
    val selectedTheme: String = "Light",
    val isLogoutDialogOpen: Boolean = false
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateGoal(newGoal: String) {
        _uiState.update { current ->
            current.copy(profile = current.profile.copy(fitnessGoal = newGoal))
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        _uiState.update { it.copy(pushNotificationsEnabled = enabled) }
    }

    fun toggleWorkoutReminders(enabled: Boolean) {
        _uiState.update { it.copy(workoutRemindersEnabled = enabled) }
    }

    fun toggleWeightReminders(enabled: Boolean) {
        _uiState.update { it.copy(weightRemindersEnabled = enabled) }
    }

    fun setTheme(theme: String) {
        _uiState.update { it.copy(selectedTheme = theme) }
    }

    fun showLogoutDialog() {
        _uiState.update { it.copy(isLogoutDialogOpen = true) }
    }

    fun dismissLogoutDialog() {
        _uiState.update { it.copy(isLogoutDialogOpen = false) }
    }

    fun confirmLogout(onLoggedOut: () -> Unit) {
        _uiState.update { it.copy(isLogoutDialogOpen = false) }
        onLoggedOut()
    }
}
