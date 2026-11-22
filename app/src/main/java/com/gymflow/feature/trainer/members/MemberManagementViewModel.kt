package com.gymflow.feature.trainer.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.firebase.FirestoreSyncManager
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.GymMember
import com.gymflow.domain.model.MembershipDuration
import com.gymflow.domain.model.PaymentMethod
import com.gymflow.domain.model.PaymentStatus
import com.gymflow.domain.model.Role
import com.gymflow.domain.repository.GymMemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

enum class MemberFilterTab {
    ALL,
    EXPIRING_SOON,
    EXPIRED,
    PENDING_PAYMENT
}

data class MemberManagementUiState(
    val searchQuery: String = "",
    val selectedFilter: MemberFilterTab = MemberFilterTab.ALL,
    val members: List<GymMember> = emptyList(),
    val isAddMemberDialogOpen: Boolean = false,
    val selectedMemberForRenewal: GymMember? = null,
    val selectedMemberForWhatsApp: GymMember? = null,
    val isRenewDialogOpen: Boolean = false,
    val isWhatsAppPreviewOpen: Boolean = false,
    val customWhatsAppMessage: String = ""
) {
    val filteredMembers: List<GymMember>
        get() {
            return members.filter { member ->
                val matchesQuery = searchQuery.isBlank() ||
                        member.name.contains(searchQuery, ignoreCase = true) ||
                        member.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                        member.email.contains(searchQuery, ignoreCase = true) ||
                        (member.assignedTrainerName?.contains(searchQuery, ignoreCase = true) == true)

                val matchesTab = when (selectedFilter) {
                    MemberFilterTab.ALL -> true
                    MemberFilterTab.EXPIRING_SOON -> member.isExpiringSoon
                    MemberFilterTab.EXPIRED -> member.isExpired
                    MemberFilterTab.PENDING_PAYMENT -> member.paymentStatus != PaymentStatus.PAID
                }

                matchesQuery && matchesTab
            }
        }

    val totalMembersCount: Int get() = members.size
    val expiringCount: Int get() = members.count { it.isExpiringSoon }
    val expiredCount: Int get() = members.count { it.isExpired }
    val pendingPaymentCount: Int get() = members.count { it.paymentStatus != PaymentStatus.PAID }
}

@HiltViewModel
class MemberManagementViewModel @Inject constructor(
    private val gymMemberRepository: GymMemberRepository,
    private val userDao: UserDao,
    private val firestoreSyncManager: FirestoreSyncManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemberManagementUiState())
    val uiState: StateFlow<MemberManagementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            gymMemberRepository.seedInitialMembersIfEmpty()
            val trainerId = tokenManager.getUserIdSync()
            if (!trainerId.isNullOrBlank()) {
                try {
                    firestoreSyncManager.pullConnectedMembersForTrainer(trainerId)
                } catch (_: Exception) {}
            }
            combine(
                gymMemberRepository.getAllMembers(),
                userDao.getConnectedAthletesFlow()
            ) { members, athletes ->
                val memberIds = members.map { it.id }.toSet()
                val athleteAsMembers = athletes
                    .filter { it.id !in memberIds && (it.connectedTrainerId != null || it.role == Role.MEMBER) }
                    .map { athlete ->
                        GymMember(
                            id = athlete.id,
                            name = athlete.name.ifBlank { "Athlete" },
                            phoneNumber = "",
                            email = athlete.email,
                            assignedTrainerId = athlete.connectedTrainerId,
                            assignedTrainerName = "Coach",
                            duration = MembershipDuration.ONE_MONTH,
                            startDateEpochMs = athlete.createdAtEpochMs,
                            endDateEpochMs = athlete.createdAtEpochMs + (30L * 24 * 60 * 60 * 1000),
                            feeAmount = 1500.0,
                            amountPaid = 1500.0,
                            paymentStatus = PaymentStatus.PAID,
                            paymentMethod = PaymentMethod.UPI,
                            notes = "Connected athlete",
                            isActive = true
                        )
                    }
                members + athleteAsMembers
            }.collect { list ->
                _uiState.update { it.copy(members = list) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFilterSelected(tab: MemberFilterTab) {
        _uiState.update { it.copy(selectedFilter = tab) }
    }

    fun openAddMemberDialog() {
        _uiState.update { it.copy(isAddMemberDialogOpen = true) }
    }

    fun closeAddMemberDialog() {
        _uiState.update { it.copy(isAddMemberDialogOpen = false) }
    }

    fun openRenewDialog(member: GymMember) {
        _uiState.update {
            it.copy(
                selectedMemberForRenewal = member,
                isRenewDialogOpen = true
            )
        }
    }

    fun closeRenewDialog() {
        _uiState.update {
            it.copy(
                selectedMemberForRenewal = null,
                isRenewDialogOpen = false
            )
        }
    }

    fun openWhatsAppPreview(member: GymMember) {
        _uiState.update {
            it.copy(
                selectedMemberForWhatsApp = member,
                isWhatsAppPreviewOpen = true
            )
        }
    }

    fun closeWhatsAppPreview() {
        _uiState.update {
            it.copy(
                selectedMemberForWhatsApp = null,
                isWhatsAppPreviewOpen = false
            )
        }
    }

    fun addNewMember(
        name: String,
        phone: String,
        email: String,
        trainerName: String?,
        duration: MembershipDuration,
        feeAmount: Double,
        amountPaid: Double,
        paymentMethod: PaymentMethod,
        notes: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.MONTH, duration.months)
            }
            val endDate = cal.timeInMillis

            val paymentStatus = when {
                amountPaid >= feeAmount -> PaymentStatus.PAID
                amountPaid > 0 -> PaymentStatus.PARTIALLY_PAID
                else -> PaymentStatus.PENDING
            }

            val newMember = GymMember(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                phoneNumber = phone.trim(),
                email = email.trim(),
                assignedTrainerName = trainerName?.trim()?.ifBlank { "Unassigned" },
                duration = duration,
                startDateEpochMs = now,
                endDateEpochMs = endDate,
                feeAmount = feeAmount,
                amountPaid = amountPaid,
                paymentStatus = paymentStatus,
                paymentMethod = paymentMethod,
                notes = notes.trim()
            )

            gymMemberRepository.addMember(newMember)
            closeAddMemberDialog()
        }
    }

    fun renewMember(
        memberId: String,
        additionalDuration: MembershipDuration,
        feeAmount: Double,
        paymentMethod: PaymentMethod
    ) {
        viewModelScope.launch {
            gymMemberRepository.renewMembership(
                memberId = memberId,
                additionalMonths = additionalDuration.months,
                fee = feeAmount,
                paymentMethod = paymentMethod.name
            )
            closeRenewDialog()
        }
    }

    fun markReminderSent(memberId: String) {
        viewModelScope.launch {
            gymMemberRepository.markRenewalReminderSent(memberId)
        }
    }
}
