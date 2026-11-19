package com.gymflow.feature.trainer.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.ConnectedMember
import com.gymflow.domain.model.PaymentStatus
import com.gymflow.domain.model.Role
import com.gymflow.domain.repository.GymMemberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val kpis: TrainerDashboardKpis = TrainerDashboardKpis(),
    val recentMembers: List<ConnectedMember> = emptyList(),
    val invitationCode: String = "",
    val unreadNotificationsCount: Int = 0
)

@HiltViewModel
class TrainerDashboardViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userDao: UserDao,
    private val gymMemberRepository: GymMemberRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainerDashboardUiState())
    val uiState: StateFlow<TrainerDashboardUiState> = _uiState.asStateFlow()

    init {
        observeCoachData()
        observeRealMembers()
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
            gymMemberRepository.getAllMembers().collect { members ->
                val total = members.size
                val active = members.count { !it.isExpired && it.paymentStatus == PaymentStatus.PAID }
                val pending = members.count { it.paymentStatus != PaymentStatus.PAID }
                val expiring = members.count { it.isExpiringSoon }

                val connectedMembers = members.map { member ->
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
                            completedToday = 0,
                            pendingReviews = pending
                        ),
                        recentMembers = connectedMembers,
                        unreadNotificationsCount = expiring
                    )
                }
            }
        }
    }
}
