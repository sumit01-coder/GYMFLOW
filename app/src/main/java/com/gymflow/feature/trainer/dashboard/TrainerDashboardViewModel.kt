package com.gymflow.feature.trainer.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.WorkoutSessionDao
import com.gymflow.core.firebase.FirestoreConnectionManager
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.ConnectedMember
import com.gymflow.domain.model.GymMember
import com.gymflow.domain.model.MembershipDuration
import com.gymflow.domain.model.MembershipOrder
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
import javax.inject.Inject

data class TrainerDashboardKpis(
    val totalMembers: Int = 0,
    val activeMembers: Int = 0,
    val completedToday: Int = 0,
    val pendingReviews: Int = 0
)

data class TrainerDashboardUiState(
    val coachName: String = "Coach",
    val greeting: String = "Good morning, Coach!",
    val gymName: String = "GymFlow Fitness Center",
    val kpis: TrainerDashboardKpis = TrainerDashboardKpis(),
    val recentMembers: List<ConnectedMember> = emptyList(),
    val pendingRenewalOrders: List<MembershipOrder> = emptyList(),
    val invitationCode: String = "",
    val unreadNotificationsCount: Int = 0
)

@HiltViewModel
class TrainerDashboardViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userDao: UserDao,
    private val gymMemberRepository: GymMemberRepository,
    private val workoutSessionDao: WorkoutSessionDao,
    private val firestoreConnectionManager: FirestoreConnectionManager,
    private val firestoreSyncManager: com.gymflow.core.firebase.FirestoreSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainerDashboardUiState())
    val uiState: StateFlow<TrainerDashboardUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(gymName = tokenManager.getGymName()) }
        observeCoachData()
        observeRealMembers()
        syncConnectedMembersFromCloud()
        fetchPendingRenewalOrders()
    }

    fun fetchPendingRenewalOrders() {
        viewModelScope.launch {
            val trainerId = tokenManager.getUserIdSync() ?: return@launch
            try {
                val orders = firestoreSyncManager.pullPendingOrdersForTrainer(trainerId)
                _uiState.update { it.copy(pendingRenewalOrders = orders) }
            } catch (_: Exception) {}
        }
    }

    fun approveOrder(order: MembershipOrder) {
        viewModelScope.launch {
            val trainerId = tokenManager.getUserIdSync() ?: order.trainerId
            firestoreSyncManager.approveMembershipOrder(
                orderId = order.orderId,
                trainerId = trainerId,
                memberId = order.memberId,
                durationMonths = order.planDurationMonths,
                feeAmount = order.amountInr
            )
            // Refresh orders and members
            fetchPendingRenewalOrders()
            firestoreSyncManager.pullConnectedMembersForTrainer(trainerId)
        }
    }

    fun rejectOrder(order: MembershipOrder, reason: String = "Payment not verified") {
        viewModelScope.launch {
            val trainerId = tokenManager.getUserIdSync() ?: order.trainerId
            firestoreSyncManager.rejectMembershipOrder(order.orderId, trainerId, reason)
            fetchPendingRenewalOrders()
        }
    }

    fun syncConnectedMembersFromCloud() {
        viewModelScope.launch {
            val trainerId = tokenManager.getUserIdSync() ?: return@launch
            try {
                firestoreSyncManager.pullConnectedMembersForTrainer(trainerId)
                val orders = firestoreSyncManager.pullPendingOrdersForTrainer(trainerId)
                _uiState.update { it.copy(pendingRenewalOrders = orders) }
            } catch (_: Exception) {}
        }
        viewModelScope.launch {
            tokenManager.getUserId().collect { trainerId ->
                if (!trainerId.isNullOrBlank() && trainerId != "default_member") {
                    try {
                        firestoreSyncManager.pullConnectedMembersForTrainer(trainerId)
                        val orders = firestoreSyncManager.pullPendingOrdersForTrainer(trainerId)
                        _uiState.update { it.copy(pendingRenewalOrders = orders) }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun observeCoachData() {
        viewModelScope.launch {
            val cachedName = tokenManager.getName()
            if (!cachedName.isNullOrBlank()) {
                val formattedName = if (cachedName.startsWith("Coach", ignoreCase = true)) cachedName else "Coach $cachedName"
                _uiState.update {
                    it.copy(
                        coachName = formattedName,
                        greeting = "Good morning, $formattedName!"
                    )
                }
            }

            userDao.getCurrentUserFlow().collect { userEntity ->
                if (userEntity != null && userEntity.role == Role.TRAINER && userEntity.name.isNotBlank()) {
                    val formattedName = if (userEntity.name.startsWith("Coach", ignoreCase = true)) userEntity.name else "Coach ${userEntity.name}"
                    _uiState.update {
                        it.copy(
                            coachName = formattedName,
                            greeting = "Good morning, $formattedName!"
                        )
                    }
                }
            }
        }
    }

    private fun observeRealMembers() {
        viewModelScope.launch {
            val startOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            combine(
                gymMemberRepository.getAllMembers(),
                userDao.getConnectedAthletesFlow(),
                workoutSessionDao.getAllCompletedSessionsFlow()
            ) { members, athletes, sessions ->
                val existingIds = members.map { it.id }.toSet()
                // Sync any athlete from UserDao not yet registered in gym_members
                athletes.filter { it.id !in existingIds && (it.connectedTrainerId != null || it.role == Role.MEMBER) }
                    .forEach { athlete ->
                        val newMember = GymMember(
                            id = athlete.id,
                            name = athlete.name.ifBlank { "Athlete" },
                            phoneNumber = "",
                            email = athlete.email,
                            assignedTrainerId = athlete.connectedTrainerId,
                            assignedTrainerName = _uiState.value.coachName,
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
                        gymMemberRepository.addMember(newMember)
                    }

                val athleteAsMembers = athletes
                    .filter { it.id !in existingIds && (it.connectedTrainerId != null || it.role == Role.MEMBER) }
                    .map { athlete ->
                        GymMember(
                            id = athlete.id,
                            name = athlete.name.ifBlank { "Athlete" },
                            phoneNumber = "",
                            email = athlete.email,
                            assignedTrainerId = athlete.connectedTrainerId,
                            assignedTrainerName = _uiState.value.coachName,
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

                val allMembers = members + athleteAsMembers
                val total = allMembers.size
                val active = allMembers.count { !it.isExpired && it.paymentStatus == PaymentStatus.PAID }
                val pending = allMembers.count { it.paymentStatus != PaymentStatus.PAID }
                val expiring = allMembers.count { it.isExpiringSoon }
                val completedToday = sessions.count { it.startTimeEpochMs >= startOfToday }

                val connectedMembers = allMembers.map { member ->
                    ConnectedMember(
                        memberId = member.id,
                        name = member.name,
                        currentWorkout = "${member.duration.displayName} · ${if (member.isExpired) "Expired" else "${member.remainingDays}d left"}",
                        lastActive = if (member.isExpired) "Expired" else "Active",
                        status = if (member.isExpired) "Expired" else if (member.paymentStatus != PaymentStatus.PAID) "Pending" else "Active"
                    )
                }

                _uiState.update {
                    it.copy(
                        kpis = TrainerDashboardKpis(
                            totalMembers = total,
                            activeMembers = active,
                            completedToday = completedToday,
                            pendingReviews = pending
                        ),
                        recentMembers = connectedMembers,
                        unreadNotificationsCount = expiring
                    )
                }
            }.collect {}
        }
    }
}
