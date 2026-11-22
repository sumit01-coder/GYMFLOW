package com.gymflow.feature.member.trainer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.GymMember
import com.gymflow.domain.model.MembershipDuration
import com.gymflow.domain.model.PaymentMethod
import com.gymflow.domain.model.PaymentStatus
import com.gymflow.domain.repository.GymMemberRepository
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
    private val tokenManager: TokenManager,
    private val gymMemberRepository: GymMemberRepository,
    private val firestoreSyncManager: FirestoreSyncManager
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
                    val trainerId = user.connectedTrainerId
                    var trainerName = tokenManager.getConnectedTrainerName()
                    var gymName = tokenManager.getConnectedTrainerGym() ?: "GymFlow Fitness Center"
                    var code = tokenManager.getConnectedTrainerCode()

                    // Ensure we don't display the member's own name as coach or an invalid code
                    if (trainerName.isNullOrBlank() || trainerName.equals(user.name, ignoreCase = true) || trainerName.equals("Coach ${user.name}", ignoreCase = true) || code.isNullOrBlank()) {
                        val cloudInfo = firestoreSyncManager.getTrainerProfileInfo(trainerId)
                        if (cloudInfo != null) {
                            trainerName = cloudInfo.name
                            gymName = cloudInfo.gymName
                            code = cloudInfo.inviteCode
                            tokenManager.saveConnectedTrainerInfo(trainerId, trainerName, gymName, code)
                        }
                    }

                    val finalTrainerName = if (!trainerName.isNullOrBlank()) {
                        if (trainerName.startsWith("Coach", ignoreCase = true)) trainerName else "Coach $trainerName"
                    } else "Coach"

                    val trainer = TrainerDetails(
                        id = trainerId,
                        name = finalTrainerName,
                        specialization = "Personal Training & Fitness",
                        experienceYears = 5,
                        activeMembersCount = 1,
                        gymName = gymName,
                        invitationCode = code ?: ""
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
            val codeInfo = if (formatted.startsWith("GYM-") && formatted.length >= 7) {
                firestoreSyncManager.lookupTrainerInviteCode(formatted)
            } else null

            val preview = if (codeInfo != null) {
                val tName = if (codeInfo.trainerName.startsWith("Coach", ignoreCase = true)) codeInfo.trainerName else "Coach ${codeInfo.trainerName}"
                TrainerDetails(
                    id = codeInfo.trainerId,
                    name = tName,
                    specialization = "Personal Training & Fitness",
                    experienceYears = 5,
                    activeMembersCount = 1,
                    gymName = codeInfo.gymName,
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
            delay(200)

            val codeInfo = firestoreSyncManager.lookupTrainerInviteCode(code)
            if (codeInfo == null || codeInfo.trainerId.isBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Trainer code '$code' not found on server. Please ask your trainer for their valid code."
                    )
                }
                return@launch
            }

            val trainerId = codeInfo.trainerId
            val trainerName = if (codeInfo.trainerName.startsWith("Coach", ignoreCase = true)) codeInfo.trainerName else "Coach ${codeInfo.trainerName}"
            val gymName = codeInfo.gymName

            val userId = currentUserId()
            userDao.updateTrainerConnection(userId, trainerId, "Connected")
            tokenManager.saveConnectedTrainerInfo(trainerId, trainerName, gymName, code)

            // Sync athlete into GymMemberRepository so the trainer dashboard reflects them immediately
            val currentUser = userDao.getUserById(userId) ?: userDao.getCurrentUser()
            val memberName = currentUser?.name?.takeIf { it.isNotBlank() }
                ?: tokenManager.getName()?.takeIf { it.isNotBlank() }
                ?: "Athlete Member"
            val memberEmail = currentUser?.email?.takeIf { it.isNotBlank() }
                ?: tokenManager.getEmail()
                ?: "athlete@gymflow.local"
            val existing = gymMemberRepository.getMemberById(userId)
            val memberPhone = existing?.phoneNumber?.takeIf { it.isNotBlank() }
                ?: tokenManager.getPhoneNumber()
                ?: ""
            val gymMember = GymMember(
                id = userId,
                name = memberName,
                phoneNumber = memberPhone,
                email = memberEmail,
                assignedTrainerId = trainerId,
                assignedTrainerName = trainerName,
                duration = existing?.duration ?: MembershipDuration.ONE_MONTH,
                startDateEpochMs = existing?.startDateEpochMs ?: System.currentTimeMillis(),
                endDateEpochMs = existing?.endDateEpochMs ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000),
                feeAmount = existing?.feeAmount ?: 1500.0,
                amountPaid = existing?.amountPaid ?: 1500.0,
                paymentStatus = PaymentStatus.PAID,
                paymentMethod = PaymentMethod.UPI,
                notes = "Connected via Trainer Code: $code",
                isActive = true
            )
            if (existing != null) {
                gymMemberRepository.updateMember(gymMember)
            } else {
                gymMemberRepository.addMember(gymMember)
            }

            // Register 2-way cloud connection in Firestore so remote trainer devices receive real-time sync
            launch {
                try {
                    firestoreSyncManager.registerMemberTrainerConnection(
                        code = code,
                        memberId = userId,
                        memberName = memberName,
                        memberEmail = memberEmail,
                        trainerId = trainerId,
                        trainerName = trainerName,
                        memberPhone = memberPhone
                    )
                } catch (_: Exception) {}
            }

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
        }
    }

    fun disconnectTrainer() {
        viewModelScope.launch {
            val userId = currentUserId()
            val oldTrainerId = tokenManager.getConnectedTrainerName() ?: _uiState.value.activeTrainer?.id
            userDao.updateTrainerConnection(userId, null, "Not Connected")
            tokenManager.clearConnectedTrainer()
            val existing = gymMemberRepository.getMemberById(userId)
            if (existing != null) {
                gymMemberRepository.updateMember(
                    existing.copy(
                        assignedTrainerId = null,
                        assignedTrainerName = null,
                        notes = "Disconnected from trainer"
                    )
                )
            }
            if (!oldTrainerId.isNullOrBlank()) {
                launch {
                    try {
                        firestoreSyncManager.removeMemberTrainerConnection(userId, oldTrainerId)
                    } catch (_: Exception) {}
                }
            }
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
