package com.gymflow.feature.trainer.members

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymflow.feature.trainer.components.TrainerBadge
import com.gymflow.feature.trainer.components.TrainerBottomNavBar
import com.gymflow.feature.trainer.components.TrainerBottomNavTab
import com.gymflow.feature.trainer.components.TrainerButton
import com.gymflow.feature.trainer.components.TrainerButtonStyle
import com.gymflow.feature.trainer.components.TrainerCard
import com.gymflow.feature.trainer.components.TrainerTextField
import com.gymflow.feature.trainer.components.TrainerTopBar
import com.gymflow.feature.trainer.theme.TrainerDarkBg
import com.gymflow.feature.trainer.theme.TrainerDarkBorder
import com.gymflow.feature.trainer.theme.TrainerDarkCard
import com.gymflow.feature.trainer.theme.TrainerDarkTextPrimary
import com.gymflow.feature.trainer.theme.TrainerDarkTextSecondary
import com.gymflow.feature.trainer.theme.TrainerDarkTextTertiary
import com.gymflow.feature.trainer.theme.TrainerTheme

@Composable
fun MemberManagementScreen(
    onBackClick: () -> Unit,
    onMemberClick: (String) -> Unit,
    onViewProgressClick: (String) -> Unit,
    onCreatePlanForMember: (String) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MemberManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TrainerTheme {
        Scaffold(
            topBar = {
                TrainerTopBar(
                    title = "Member Management",
                    subtitle = "${uiState.allMembers.size} Total Athletes",
                    onBackClick = onBackClick
                )
            },
            bottomBar = {
                TrainerBottomNavBar(
                    selectedTab = TrainerBottomNavTab.MEMBERS,
                    onTabSelected = { tab ->
                        when (tab) {
                            TrainerBottomNavTab.DASHBOARD -> onNavigateToDashboard()
                            TrainerBottomNavTab.MEMBERS -> {}
                            TrainerBottomNavTab.PLANS -> onNavigateToPlans()
                            TrainerBottomNavTab.ANALYTICS -> onNavigateToAnalytics()
                            TrainerBottomNavTab.SETTINGS -> onNavigateToSettings()
                        }
                    }
                )
            },
            containerColor = TrainerDarkBg
        ) { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                TrainerTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = "Search members, programs, goals...",
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = TrainerDarkTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else null
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Filter Tabs: [All], [Active], [Pending]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MemberFilterTab.entries.forEach { tab ->
                        val isSelected = uiState.selectedFilter == tab
                        val tabLabel = when (tab) {
                            MemberFilterTab.ALL -> "All (${uiState.allMembers.size})"
                            MemberFilterTab.ACTIVE -> "Active (${uiState.allMembers.count { it.status == "Active" }})"
                            MemberFilterTab.PENDING -> "Pending (${uiState.allMembers.count { it.status == "Pending" }})"
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(if (isSelected) Color.White else TrainerDarkCard)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.White else TrainerDarkBorder,
                                    RoundedCornerShape(9999.dp)
                                )
                                .clickable { viewModel.onFilterSelected(tab) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else TrainerDarkTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Members List
                if (uiState.filteredMembers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No matching members found",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TrainerDarkTextSecondary
                            )
                            Text(
                                text = "Try modifying your filter or search query",
                                fontSize = 12.sp,
                                color = TrainerDarkTextTertiary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.filteredMembers, key = { it.id }) { member ->
                            TrainerCard(
                                backgroundColor = TrainerDarkCard,
                                borderColor = TrainerDarkBorder,
                                contentPadding = 16.dp,
                                onClick = { onMemberClick(member.id) }
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Row 1: Avatar, Name, Status Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF27272A)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = member.name.take(2).uppercase(),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TrainerDarkTextPrimary
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = member.name,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TrainerDarkTextPrimary
                                                )
                                                Text(
                                                    text = member.goal,
                                                    fontSize = 12.sp,
                                                    color = TrainerDarkTextSecondary
                                                )
                                            }
                                        }

                                        TrainerBadge(
                                            text = member.status,
                                            status = member.status
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Row 2: Program and Meta Stats
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF141416))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "CURRENT PROGRAM",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TrainerDarkTextTertiary
                                            )
                                            Text(
                                                text = member.currentProgram,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF4ADE80)
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "COMPLETED",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TrainerDarkTextTertiary
                                            )
                                            Text(
                                                text = "${member.workoutsCompleted} sessions (${member.consistencyRate}%)",
                                                fontSize = 13.sp,
                                                color = TrainerDarkTextSecondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Row 3: Action Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TrainerButton(
                                            text = "Progress",
                                            onClick = { onViewProgressClick(member.id) },
                                            style = TrainerButtonStyle.SECONDARY,
                                            height = 38.dp,
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        )

                                        TrainerButton(
                                            text = "Assign Plan",
                                            onClick = { onCreatePlanForMember(member.id) },
                                            style = TrainerButtonStyle.OUTLINED,
                                            height = 38.dp,
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.FitnessCenter,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}
