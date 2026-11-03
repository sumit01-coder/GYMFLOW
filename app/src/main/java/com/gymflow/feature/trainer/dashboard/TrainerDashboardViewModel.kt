package com.gymflow.feature.trainer.dashboard

import androidx.lifecycle.ViewModel
import com.gymflow.domain.model.ConnectedMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

class TrainerDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TrainerDashboardUiState())
    val uiState: StateFlow<TrainerDashboardUiState> = _uiState.asStateFlow()
}
