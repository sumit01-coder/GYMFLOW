package com.gymflow.feature.trainer.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.UserDao
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.Role
import com.gymflow.domain.repository.GymMemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainerSettingsUiState(
    val coachName: String = "Coach",
    val title: String = "Strength & Conditioning Specialist",
    val email: String = "",
    val experienceYears: Int = 0,
    val specialization: String = "General Strength & Conditioning",
    val gymAffiliation: String = "GymFlow Fitness Center",
    val isGymNameDialogOpen: Boolean = false,
    val trainerUpiId: String = "",
    val trainerUpiName: String = "",
    val isUpiDialogOpen: Boolean = false,
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
    private val gymMemberRepository: GymMemberRepository,
    private val firestoreSyncManager: FirestoreSyncManager,
    private val planDao: PlanDao,
    private val appUpdateManager: com.gymflow.core.updater.AppUpdateManager
) : ViewModel() {

    fun checkForUpdates() {
        appUpdateManager.checkForUpdate(isManualCheck = true)
    }

    private val _uiState = MutableStateFlow(TrainerSettingsUiState())
    val uiState: StateFlow<TrainerSettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                gymAffiliation = tokenManager.getGymName(),
                trainerUpiId = tokenManager.getTrainerUpiId() ?: "",
                trainerUpiName = tokenManager.getTrainerUpiName() ?: tokenManager.getName() ?: ""
            )
        }
        observeCoachInfo()
        syncConnectedMembersFromCloud()
        observeStudioMembersAndPlans()
    }

    fun openGymNameDialog() {
        _uiState.update { it.copy(isGymNameDialogOpen = true) }
    }

    fun dismissGymNameDialog() {
        _uiState.update { it.copy(isGymNameDialogOpen = false) }
    }

    fun updateGymName(newGymName: String) {
        val cleanName = newGymName.trim().ifBlank { "GymFlow Fitness Center" }
        tokenManager.saveGymName(cleanName)
        _uiState.update { it.copy(gymAffiliation = cleanName, isGymNameDialogOpen = false) }
        viewModelScope.launch {
            val trainerId = tokenManager.getUserIdSync() ?: ""
            firestoreSyncManager.updateTrainerGymName(trainerId, cleanName)
        }
    }

    fun openUpiDialog() {
        _uiState.update { it.copy(isUpiDialogOpen = true) }
    }

    fun dismissUpiDialog() {
        _uiState.update { it.copy(isUpiDialogOpen = false) }
    }

    fun updateTrainerUpi(upiId: String, upiName: String) {
        val cleanUpi = upiId.trim()
        val cleanName = upiName.trim().ifBlank { _uiState.value.gymAffiliation }
        tokenManager.saveTrainerUpiId(cleanUpi)
        tokenManager.saveTrainerUpiName(cleanName)
        _uiState.update {
            it.copy(
                trainerUpiId = cleanUpi,
                trainerUpiName = cleanName,
                isUpiDialogOpen = false
            )
        }
        viewModelScope.launch {
            val trainerId = tokenManager.getUserIdSync() ?: ""
            firestoreSyncManager.updateTrainerUpi(trainerId, cleanUpi, cleanName)
        }
    }

    private fun syncConnectedMembersFromCloud() {
        viewModelScope.launch {
            val trainerId = tokenManager.getUserIdSync() ?: return@launch
            try {
                firestoreSyncManager.pullConnectedMembersForTrainer(trainerId)
            } catch (_: Exception) {}
        }
    }

    private fun observeStudioMembersAndPlans() {
        viewModelScope.launch {
            val trainerId = tokenManager.getUserIdSync() ?: ""
            combine(
                gymMemberRepository.getAllMembers(),
                userDao.getConnectedAthletesFlow(),
                planDao.getPlansForTrainer(trainerId)
            ) { members, athletes, plans ->
                val allMemberIds = (members.map { it.id } + athletes.map { it.id }).toSet()
                val total = allMemberIds.size
                val activePlans = plans.count { it.isActive }
                _uiState.update {
                    it.copy(
                        totalMembers = total,
                        activePlansCount = activePlans
                    )
                }
            }.collect {}
        }
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
