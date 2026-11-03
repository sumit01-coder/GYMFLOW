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
    }

    private fun currentUserId(): String = tokenManager.getUserIdSync() ?: "default_member"

    private fun observeConnectionStatus() {
        val userId = currentUserId()
        viewModelScope.launch {
            userDao.getUserFlow(userId).collect { user ->
                if (user != null && !user.connectedTrainerId.isNullOrBlank()) {
                    val trainer = TrainerDetails(
                        id = user.connectedTrainerId,
                        name = "Coach Marcus",
                        specialization = "Strength & Conditioning",
                        experienceYears = 5,
                        activeMembersCount = 12,
                        gymName = "Iron Works Performance",
                        invitationCode = "GYM-82K91"
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
        _uiState.update { current ->
            current.copy(
                codeInput = formatted,
                errorMessage = null,
                previewTrainer = if (formatted.startsWith("GYM-") && formatted.length >= 7) {
                    TrainerDetails(
                        id = "trainer-new",
                        name = "Coach Marcus",
                        specialization = "Strength & Conditioning",
                        experienceYears = 5,
                        activeMembersCount = 12,
                        gymName = "Iron Works Performance",
                        invitationCode = formatted
                    )
                } else null
            )
        }
    }

    fun submitJoinCode() {
        val code = _uiState.value.codeInput.trim().uppercase()
        if (code.length < 5) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid invitation code (e.g. GYM-82K91)") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            delay(500) // Fast snappy feedback

            if (code.contains("GYM")) {
                val userId = currentUserId()
                userDao.updateTrainerConnection(userId, "trainer-verified", "Connected")

                val connected = TrainerDetails(
                    id = "trainer-verified",
                    name = "Coach Marcus",
                    specialization = "Strength & Conditioning",
                    experienceYears = 5,
                    activeMembersCount = 12,
                    gymName = "Iron Works Performance",
                    invitationCode = code
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        connectionStatus = TrainerConnectionStatus.CONNECTED,
                        activeTrainer = connected,
                        successMessage = "Successfully connected to your Coach!"
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
        val cleaned = raw.uppercase().replace("[^A-Z0-9-]".toRegex(), "")
        return if (!cleaned.startsWith("GYM-") && cleaned.length >= 3 && !cleaned.contains("-")) {
            "GYM-$cleaned"
        } else {
            cleaned.take(12)
        }
    }
}
