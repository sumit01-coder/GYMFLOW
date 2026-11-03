package com.gymflow.feature.trainer.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.core.database.UserDao
import com.gymflow.core.security.TokenManager
import com.gymflow.domain.model.ConnectedMember
import com.gymflow.domain.model.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainerDashboardKpis(
    val totalMembers: Int = 32,
    val activeMembers: Int = 24,
    val completedToday: Int = 18,
    val pendingReviews: Int = 6
)

data class TrainerDashboardUiState(
    val coachName: String = "Coach Marcus",
    val greeting: String = "Good morning, Coach!",
    val kpis: TrainerDashboardKpis = TrainerDashboardKpis(),
    val recentMembers: List<ConnectedMember> = listOf(
        ConnectedMember(
            memberId = "sarah-miller-101",
            name = "Sarah Miller",
            currentWorkout = "Chest + Triceps · Today",
            lastActive = "12m ago",
            status = "Active"
        ),
        ConnectedMember(
            memberId = "mike-ross-102",
            name = "Mike Ross",
            currentWorkout = "Leg Day · Today",
            lastActive = "45m ago",
            status = "Pending"
        ),
        ConnectedMember(
            memberId = "emma-davis-103",
            name = "Emma Davis",
            currentWorkout = "Back + Biceps · Today",
            lastActive = "2h ago",
            status = "Active"
        )
    ),
    val invitationCode: String = "GYM-82K91",
    val unreadNotificationsCount: Int = 3
)

@HiltViewModel
class TrainerDashboardViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainerDashboardUiState())
    val uiState: StateFlow<TrainerDashboardUiState> = _uiState.asStateFlow()

    init {
        observeCoachData()
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
}
