package com.gymflow.feature.member.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.security.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TrainerConnectionStatus {
    NOT_CONNECTED,
    PENDING_APPROVAL,
    CONNECTED
}

data class TrainerDetails(
    val id: String,
    val name: String,
    val specialization: String,
    val experienceYears: Int,
    val activeMembersCount: Int,
    val gymName: String,
    val invitationCode: String
)

data class JoinTrainerUiState(
    val codeInput: String = "",
    val connectionStatus: TrainerConnectionStatus = TrainerConnectionStatus.NOT_CONNECTED,
    val activeTrainer: TrainerDetails? = null,
    val previewTrainer: TrainerDetails? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class TrainerConnectionViewModel @Inject constructor(
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinTrainerUiState())
    val uiState: StateFlow<JoinTrainerUiState> = _uiState.asStateFlow()

    init {
        observeConnectionStatus()
        TokenManager.pendingScannedJoinCode?.let { pendingCode ->
            TokenManager.pendingScannedJoinCode = null
            onCodeChanged(pendingCode)
            submitJoinCode()
        }
    }

    private fun currentUserId(): String = tokenManager.getUserIdSync() ?: "default_member"

    private fun observeConnectionStatus() {
        val userId = currentUserId()
        viewModelScope.launch {
            userDao.getUserFlow(userId).collect { user ->
                if (user != null && !user.connectedTrainerId.isNullOrBlank()) {
                    val realTrainerUser = userDao.getUserById(user.connectedTrainerId) ?: userDao.getPrimaryTrainer()
                    val trainerName = tokenManager.getConnectedTrainerName()
                        ?: realTrainerUser?.name?.let { if (it.startsWith("Coach", ignoreCase = true)) it else "Coach $it" }
                        ?: "Coach"
                    val gymName = tokenManager.getConnectedTrainerGym() ?: "GymFlow Fitness Center"
                    val code = tokenManager.getConnectedTrainerCode() ?: tokenManager.getTrainerInviteCode()

                    val trainer = TrainerDetails(
                        id = user.connectedTrainerId,
                        name = trainerName,
                        specialization = "Personal Training & Fitness",
                        experienceYears = 5,
                        activeMembersCount = 1,
                        gymName = gymName,
                        invitationCode = code
                    )
                    _uiState.update {
                        it.copy(
                            connectionStatus = TrainerConnectionStatus.CONNECTED,
                            activeTrainer = trainer
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            connectionStatus = TrainerConnectionStatus.NOT_CONNECTED,
                            activeTrainer = null
                        )
                    }
                }
            }
        }
    }

    fun onCodeChanged(input: String) {
        val formatted = formatInvitationCode(input)
        viewModelScope.launch {
            val preview = if (formatted.startsWith("GYM-") && formatted.length >= 7) {
                val realTrainerUser = userDao.getPrimaryTrainer()
                val trainerName = realTrainerUser?.name?.let { if (it.startsWith("Coach", ignoreCase = true)) it else "Coach $it" }
                    ?: tokenManager.getName()?.let { if (it.startsWith("Coach", ignoreCase = true)) it else "Coach $it" }
                    ?: "Coach"
                TrainerDetails(
                    id = realTrainerUser?.id ?: "trainer-verified",
                    name = trainerName,
                    specialization = "Personal Training & Fitness",
                    experienceYears = 5,
                    activeMembersCount = 1,
                    gymName = "GymFlow Fitness Center",
                    invitationCode = formatted
                )
            } else null

            _uiState.update { current ->
                current.copy(
                    codeInput = formatted,
                    errorMessage = null,
                    previewTrainer = preview
                )
            }
        }
    }

    fun submitJoinCode() {
        val code = formatInvitationCode(_uiState.value.codeInput)
        if (code.length < 5) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid invitation code (e.g. GYM-82K91)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            delay(300) // Snappy feedback

            if (code.contains("GYM")) {
                val realTrainerUser = userDao.getPrimaryTrainer()
                val trainerId = realTrainerUser?.id ?: "trainer-verified"
                val trainerName = realTrainerUser?.name?.let { if (it.startsWith("Coach", ignoreCase = true)) it else "Coach $it" }
                    ?: tokenManager.getName()?.let { if (it.startsWith("Coach", ignoreCase = true)) it else "Coach $it" }
                    ?: "Coach"
                val gymName = "GymFlow Fitness Center"

                val userId = currentUserId()
                userDao.updateTrainerConnection(userId, trainerId, "Connected")
                tokenManager.saveConnectedTrainerInfo(trainerId, trainerName, gymName, code)

                val connected = TrainerDetails(
                    id = trainerId,
                    name = trainerName,
                    specialization = "Personal Training & Fitness",
                    experienceYears = 5,
                    activeMembersCount = 1,
                    gymName = gymName,
                    invitationCode = code
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        connectionStatus = TrainerConnectionStatus.CONNECTED,
                        activeTrainer = connected,
                        successMessage = "Successfully connected to $trainerName!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Invalid code. Check with your trainer for their GYM-XXXXX code."
                    )
                }
            }
        }
    }

    fun disconnectTrainer() {
        viewModelScope.launch {
            val userId = currentUserId()
            userDao.updateTrainerConnection(userId, null, "Not Connected")
            tokenManager.clearConnectedTrainer()
            _uiState.update {
                it.copy(
                    connectionStatus = TrainerConnectionStatus.NOT_CONNECTED,
                    activeTrainer = null,
                    codeInput = "",
                    previewTrainer = null,
                    successMessage = "Disconnected from trainer."
                )
            }
        }
    }

    private fun formatInvitationCode(raw: String): String {
        val extracted = if (raw.contains("code=", ignoreCase = true)) {
            raw.substringAfter("code=", "").substringBefore("&").trim()
        } else if (raw.contains("GYM-", ignoreCase = true)) {
            "GYM-" + raw.substringAfter("GYM-", "").trim()
        } else {
            raw
        }
        val cleaned = extracted.uppercase().replace("[^A-Z0-9-]".toRegex(), "")
        return if (!cleaned.startsWith("GYM-") && cleaned.length >= 3 && !cleaned.contains("-")) {
            "GYM-$cleaned"
        } else {
            cleaned.take(12)
        }
    }
}
