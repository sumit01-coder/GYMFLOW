package com.gymflow.feature.member.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.MemberProfile
import com.gymflow.domain.model.Role
import com.gymflow.domain.model.User
import com.gymflow.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ProfileUiState(
    val user: User = User(
        id = "default_member",
        name = "",
        email = "",
        role = Role.MEMBER
    ),
    val profile: MemberProfile = MemberProfile(
        userId = "default_member",
        heightCm = 178f,
        currentWeightKg = 67.8f,
        fitnessGoal = "Build Muscle",
        age = 28,
        connectedTrainerId = "trainer_1",
        syncStatus = "Connected"
    ),
    val initials: String = "GF",
    val joinedDateFormatted: String = "Joined Recently",
    val pushNotificationsEnabled: Boolean = true,
    val workoutRemindersEnabled: Boolean = true,
    val weightRemindersEnabled: Boolean = true,
    val selectedTheme: String = "Light",
    val isLogoutDialogOpen: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        run {
            val name = tokenManager.getName() ?: ""
            val email = tokenManager.getEmail() ?: ""
            val id = tokenManager.getUserIdSync() ?: "default_member"
            val inits = if (name.isNotBlank()) extractInitials(name) else "GF"
            ProfileUiState(
                user = User(
                    id = id,
                    name = name,
                    email = email,
                    role = Role.MEMBER
                ),
                initials = inits
            )
        }
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeUserData()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            // First check TokenManager for instant identity
            val cachedName = tokenManager.getName()
            val cachedEmail = tokenManager.getEmail()
            val cachedUserId = tokenManager.getUserIdSync()

            if (!cachedName.isNullOrBlank()) {
                val initials = extractInitials(cachedName)
                _uiState.update { current ->
                    current.copy(
                        user = current.user.copy(
                            id = cachedUserId ?: current.user.id,
                            name = cachedName,
                            email = cachedEmail ?: current.user.email
                        ),
                        initials = initials
                    )
                }
            }

            // Observe reactive Room changes
            userDao.getCurrentUserFlow().collect { userEntity ->
                if (userEntity != null) {
                    val initials = extractInitials(userEntity.name)
                    val joinedDate = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                        .format(Date(userEntity.lastUpdated))
                    
                    _uiState.update { current ->
                        current.copy(
                            user = userEntity.toDomain(),
                            profile = userEntity.toMemberProfile(),
                            initials = initials,
                            joinedDateFormatted = "Joined $joinedDate"
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun extractInitials(name: String): String {
            val parts = name.trim().split(" ").filter { it.isNotBlank() }
            return when {
                parts.isEmpty() -> "GF"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
            }
        }
    }

    fun updateGoal(newGoal: String) {
        val userId = _uiState.value.user.id
        _uiState.update { current ->
            current.copy(profile = current.profile.copy(fitnessGoal = newGoal))
        }
        viewModelScope.launch {
            userDao.updateMemberProfile(
                id = userId,
                heightCm = _uiState.value.profile.heightCm,
                weightKg = _uiState.value.profile.currentWeightKg,
                goal = newGoal
            )
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
        viewModelScope.launch {
            authRepository.logout().collect {
                onLoggedOut()
            }
        }
    }
}
