package com.gymflow.feature.trainer.joincode

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val pendingRequests: List<PendingConnectionRequest> = listOf(
        PendingConnectionRequest(
            id = "req-1",
            memberName = "Alex Miller",
            requestedAgo = "15m ago",
            fitnessGoal = "Fat Loss & Conditioning",
            age = 29,
            currentWeightKg = 74.2f
        ),
        PendingConnectionRequest(
            id = "req-2",
            memberName = "Daniel Carter",
            requestedAgo = "2h ago",
            fitnessGoal = "Hypertrophy Starter",
            age = 24,
            currentWeightKg = 68.0f
        )
    ),
    val lastApprovedMemberName: String? = null
)

class TrainerJoinCodeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TrainerJoinCodeUiState())
    val uiState: StateFlow<TrainerJoinCodeUiState> = _uiState.asStateFlow()

    fun generateNewCode() {
        val randomNum = Random.nextInt(10, 99)
        val chars = ('A'..'Z').toList()
        val randomChars = "" + chars.random() + chars.random() + Random.nextInt(10, 99)
        val newCode = "GYM-$randomNum$randomChars"
        _uiState.update {
            it.copy(
                currentCode = newCode,
                expiresHoursRemaining = 24,
                isCopiedNotification = false
            )
        }
    }

    fun onCodeCopied() {
        _uiState.update { it.copy(isCopiedNotification = true) }
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
