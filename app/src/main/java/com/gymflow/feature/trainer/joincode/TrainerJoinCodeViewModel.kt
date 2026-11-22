package com.gymflow.feature.trainer.joincode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.repository.GymMemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class PendingConnectionRequest(
    val id: String,
    val memberName: String,
    val requestedAgo: String,
    val fitnessGoal: String,
    val age: Int,
    val currentWeightKg: Float
)

data class TrainerJoinCodeUiState(
    val currentCode: String = "GYM-82K91",
    val expiresHoursRemaining: Int = 24,
    val isCopiedNotification: Boolean = false,
    val pendingRequests: List<PendingConnectionRequest> = emptyList(),
    val lastApprovedMemberName: String? = null
)

@HiltViewModel
class TrainerJoinCodeViewModel(
    private val firestoreSyncManager: FirestoreSyncManager?,
    private val tokenManager: TokenManager?,
    private val userDao: UserDao?,
    private val gymMemberRepository: GymMemberRepository?,
    @Suppress("UNUSED_PARAMETER") private val isHilt: Boolean
) : ViewModel() {

    @Inject
    constructor(
        firestoreSyncManager: FirestoreSyncManager,
        tokenManager: TokenManager,
        userDao: UserDao,
        gymMemberRepository: GymMemberRepository
    ) : this(
        firestoreSyncManager,
        tokenManager,
        userDao,
        gymMemberRepository,
        true
    )

    constructor() : this(null, null, null, null, false)

    private val _uiState = MutableStateFlow(TrainerJoinCodeUiState())
    val uiState: StateFlow<TrainerJoinCodeUiState> = _uiState.asStateFlow()

    init {
        val savedCode = tokenManager?.getTrainerInviteCode() ?: _uiState.value.currentCode
        _uiState.update { it.copy(currentCode = savedCode) }
        publishCodeToCloud(savedCode)
        syncConnectedMembersFromCloud()
        observeConnectedAthletes()
    }

    private fun syncConnectedMembersFromCloud() {
        val fsm = firestoreSyncManager ?: return
        val trainerId = tokenManager?.getUserIdSync() ?: return
        viewModelScope.launch {
            try {
                fsm.pullConnectedMembersForTrainer(trainerId)
            } catch (_: Exception) {}
        }
    }

    private fun observeConnectedAthletes() {
        val dao = userDao ?: return
        viewModelScope.launch {
            dao.getConnectedAthletesFlow().collect { athletes ->
                val requests = athletes.map { athlete ->
                    PendingConnectionRequest(
                        id = athlete.id,
                        memberName = athlete.name.ifBlank { "Athlete" },
                        requestedAgo = "Just now",
                        fitnessGoal = athlete.fitnessGoal.ifBlank { "Strength & Conditioning" },
                        age = athlete.age.takeIf { it > 0 } ?: 24,
                        currentWeightKg = athlete.currentWeightKg.takeIf { it > 0f } ?: 72f
                    )
                }
                _uiState.update { it.copy(pendingRequests = requests) }
            }
        }
    }

    private fun publishCodeToCloud(code: String) {
        val fsm = firestoreSyncManager ?: return
        viewModelScope.launch {
            val trainerId = tokenManager?.getUserIdSync() ?: userDao?.getPrimaryTrainer()?.id ?: "trainer-verified"
            val trainerName = tokenManager?.getName() ?: userDao?.getPrimaryTrainer()?.name ?: "Coach"
            fsm.publishTrainerInviteCode(
                code = code,
                trainerId = trainerId,
                trainerName = trainerName,
                gymName = "GymFlow Fitness Center"
            )
        }
    }

    fun generateNewCode() {
        val randomNum = Random.nextInt(10, 99)
        val chars = ('A'..'Z').toList()
        val randomChars = "" + chars.random() + chars.random() + Random.nextInt(10, 99)
        val newCode = "GYM-$randomNum$randomChars"
        tokenManager?.saveTrainerInviteCode(newCode)
        _uiState.update {
            it.copy(
                currentCode = newCode,
                expiresHoursRemaining = 24,
                isCopiedNotification = false
            )
        }
        publishCodeToCloud(newCode)
    }

    fun onCodeCopied() {
        _uiState.update { it.copy(isCopiedNotification = true) }
    }

    fun setPendingRequests(requests: List<PendingConnectionRequest>) {
        _uiState.update { it.copy(pendingRequests = requests) }
    }

    fun approveRequest(requestId: String) {
        val approvedReq = _uiState.value.pendingRequests.firstOrNull { it.id == requestId }
        _uiState.update { state ->
            state.copy(
                pendingRequests = state.pendingRequests.filterNot { it.id == requestId },
                lastApprovedMemberName = approvedReq?.memberName
            )
        }
    }

    fun rejectRequest(requestId: String) {
        _uiState.update { state ->
            state.copy(
                pendingRequests = state.pendingRequests.filterNot { it.id == requestId }
            )
        }
    }
}
