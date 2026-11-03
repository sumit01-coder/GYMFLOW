package com.gymflow.feature.trainer.members

import androidx.lifecycle.ViewModel
import com.gymflow.domain.model.ConnectedMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class MemberFilterTab {
    ALL,
    ACTIVE,
    PENDING
}

data class MemberItemData(
    val id: String,
    val name: String,
    val goal: String,
    val currentProgram: String,
    val lastActive: String,
    val status: String, // Active, Pending
    val workoutsCompleted: Int,
    val consistencyRate: Int
)

data class MemberManagementUiState(
    val searchQuery: String = "",
    val selectedFilter: MemberFilterTab = MemberFilterTab.ALL,
    val allMembers: List<MemberItemData> = listOf(
        MemberItemData(
            id = "sarah-miller-101",
            name = "Sarah Miller",
            goal = "Build Muscle / Hypertrophy",
            currentProgram = "Chest + Triceps",
            lastActive = "12m ago",
            status = "Active",
            workoutsCompleted = 14,
            consistencyRate = 92
        ),
        MemberItemData(
            id = "mike-ross-102",
            name = "Mike Ross",
            goal = "Strength Conditioning",
            currentProgram = "Leg Day",
            lastActive = "45m ago",
            status = "Pending",
            workoutsCompleted = 8,
            consistencyRate = 75
        ),
        MemberItemData(
            id = "emma-davis-103",
            name = "Emma Davis",
            goal = "Endurance & Tone",
            currentProgram = "Back + Biceps",
            lastActive = "2h ago",
            status = "Active",
            workoutsCompleted = 19,
            consistencyRate = 96
        ),
        MemberItemData(
            id = "james-wilson-104",
            name = "James Wilson",
            goal = "Powerlifting Peak",
            currentProgram = "Upper Body Power",
            lastActive = "Yesterday",
            status = "Active",
            workoutsCompleted = 11,
            consistencyRate = 88
        ),
        MemberItemData(
            id = "olivia-brown-105",
            name = "Olivia Brown",
            goal = "Mobility & Core",
            currentProgram = "Core & HIIT",
            lastActive = "3d ago",
            status = "Active",
            workoutsCompleted = 7,
            consistencyRate = 70
        ),
        MemberItemData(
            id = "daniel-carter-106",
            name = "Daniel Carter",
            goal = "Hypertrophy Starter",
            currentProgram = "Hypertrophy Starter",
            lastActive = "Pending Approval",
            status = "Pending",
            workoutsCompleted = 0,
            consistencyRate = 0
        )
    )
) {
    val filteredMembers: List<MemberItemData>
        get() {
            return allMembers.filter { member ->
                val matchesQuery = searchQuery.isBlank() ||
                        member.name.contains(searchQuery, ignoreCase = true) ||
                        member.currentProgram.contains(searchQuery, ignoreCase = true) ||
                        member.goal.contains(searchQuery, ignoreCase = true)

                val matchesTab = when (selectedFilter) {
                    MemberFilterTab.ALL -> true
                    MemberFilterTab.ACTIVE -> member.status.equals("Active", ignoreCase = true)
                    MemberFilterTab.PENDING -> member.status.equals("Pending", ignoreCase = true)
                }

                matchesQuery && matchesTab
            }
        }
}

class MemberManagementViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MemberManagementUiState())
    val uiState: StateFlow<MemberManagementUiState> = _uiState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFilterSelected(tab: MemberFilterTab) {
        _uiState.update { it.copy(selectedFilter = tab) }
    }
}
