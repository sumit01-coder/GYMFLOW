package com.gymflow.feature.member.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.gymflow.core.database.UserDao
import com.gymflow.core.firebase.FirestoreSyncManager
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
import kotlinx.coroutines.tasks.await
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
        heightCm = 0f,
        currentWeightKg = 0f,
        fitnessGoal = "General Fitness",
        age = 0,
        connectedTrainerId = null,
        syncStatus = "Not Connected"
    ),
    val initials: String = "GF",
    val joinedDateFormatted: String = "Joined Recently",
    val pushNotificationsEnabled: Boolean = true,
    val workoutRemindersEnabled: Boolean = true,
    val reminderHour: Int = 6,
    val reminderMinute: Int = 30,
    val formattedReminderTime: String = "06:30 AM",
    val isTimePickerOpen: Boolean = false,
    val weightRemindersEnabled: Boolean = true,
    val selectedTheme: String = "Light",
    val isLogoutDialogOpen: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userDao: UserDao,
    private val tokenManager: TokenManager,
    private val firebaseAuth: FirebaseAuth,
    private val firestoreSyncManager: FirestoreSyncManager,
    private val appUpdateManager: com.gymflow.core.updater.AppUpdateManager,
    private val workoutReminderManager: com.gymflow.core.notification.WorkoutReminderManager
) : ViewModel() {

    fun checkForUpdates() {
        appUpdateManager.checkForUpdate(isManualCheck = true)
    }

    private val _uiState = MutableStateFlow(
        run {
            val name = tokenManager.getName() ?: ""
            val email = tokenManager.getEmail() ?: ""
            val id = tokenManager.getUserIdSync() ?: "default_member"
            val inits = if (name.isNotBlank()) extractInitials(name) else "GF"
            val isVerified = tokenManager.isEmailVerifiedSync() || (firebaseAuth.currentUser?.isEmailVerified == true)
            ProfileUiState(
                user = User(
                    id = id,
                    name = name,
                    email = email,
                    role = Role.MEMBER,
                    isEmailVerified = isVerified
                ),
                initials = inits
            )
        }
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeUserData()
        refreshVerificationStatus()
        initializeReminderState()
    }

    private fun initializeReminderState() {
        val isEnabled = workoutReminderManager.isReminderEnabled()
        val (hour, minute) = workoutReminderManager.getReminderTime()
        val formatted = workoutReminderManager.formatTime(hour, minute)
        _uiState.update { current ->
            current.copy(
                workoutRemindersEnabled = isEnabled,
                reminderHour = hour,
                reminderMinute = minute,
                formattedReminderTime = formatted
            )
        }
    }

    fun refreshVerificationStatus() {
        viewModelScope.launch {
            try {
                val fbUser = firebaseAuth.currentUser
                if (fbUser != null) {
                    fbUser.reload().await()
                    val verified = fbUser.isEmailVerified
                    if (verified) {
                        val userId = tokenManager.getUserIdSync() ?: fbUser.uid
                        userDao.updateEmailVerified(userId, true)
                        tokenManager.setEmailVerified(true)
                        val updatedUser = userDao.getUserById(userId)
                        if (updatedUser != null) {
                            try { firestoreSyncManager.saveUserProfile(updatedUser) } catch (_: Exception) {}
                        }
                        _uiState.update { current ->
                            current.copy(
                                user = current.user.copy(isEmailVerified = true)
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
        }
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
                    if (userEntity.syncStatus == "Connected" && userEntity.connectedTrainerId == null) {
                        userDao.updateTrainerConnection(userEntity.id, null, "Not Connected")
                    }

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
            val updated = userDao.getUserById(userId)
            if (updated != null) {
                try { firestoreSyncManager.saveUserProfile(updated) } catch (_: Exception) {}
            }
        }
    }

    fun updateAge(newAge: Int) {
        val userId = _uiState.value.user.id
        val clampedAge = newAge.coerceIn(10, 120)
        _uiState.update { current ->
            current.copy(profile = current.profile.copy(age = clampedAge))
        }
        viewModelScope.launch {
            userDao.updateFullMemberProfile(
                id = userId,
                heightCm = _uiState.value.profile.heightCm,
                weightKg = _uiState.value.profile.currentWeightKg,
                goal = _uiState.value.profile.fitnessGoal,
                age = clampedAge
            )
            val updated = userDao.getUserById(userId)
            if (updated != null) {
                try { firestoreSyncManager.saveUserProfile(updated) } catch (_: Exception) {}
            }
        }
    }

    fun updateHeight(newHeightCm: Float) {
        val userId = _uiState.value.user.id
        val clampedHeight = newHeightCm.coerceIn(50f, 260f)
        _uiState.update { current ->
            current.copy(profile = current.profile.copy(heightCm = clampedHeight))
        }
        viewModelScope.launch {
            userDao.updateFullMemberProfile(
                id = userId,
                heightCm = clampedHeight,
                weightKg = _uiState.value.profile.currentWeightKg,
                goal = _uiState.value.profile.fitnessGoal,
                age = _uiState.value.profile.age
            )
            val updated = userDao.getUserById(userId)
            if (updated != null) {
                try { firestoreSyncManager.saveUserProfile(updated) } catch (_: Exception) {}
            }
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        _uiState.update { it.copy(pushNotificationsEnabled = enabled) }
    }

    fun toggleWorkoutReminders(enabled: Boolean) {
        val currentHour = _uiState.value.reminderHour
        val currentMin = _uiState.value.reminderMinute
        if (enabled) {
            workoutReminderManager.scheduleReminder(currentHour, currentMin)
        } else {
            workoutReminderManager.cancelReminder()
        }
        _uiState.update { it.copy(workoutRemindersEnabled = enabled) }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        workoutReminderManager.scheduleReminder(hour, minute)
        val formatted = workoutReminderManager.formatTime(hour, minute)
        _uiState.update {
            it.copy(
                workoutRemindersEnabled = true,
                reminderHour = hour,
                reminderMinute = minute,
                formattedReminderTime = formatted,
                isTimePickerOpen = false
            )
        }
    }

    fun setTimePickerOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isTimePickerOpen = isOpen) }
    }

    fun sendTestReminder() {
        workoutReminderManager.showWorkoutNotification(
            title = "Time to Train! 🏋️ (Test)",
            content = "Your scheduled workout is ready. Tap to start!"
        )
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
