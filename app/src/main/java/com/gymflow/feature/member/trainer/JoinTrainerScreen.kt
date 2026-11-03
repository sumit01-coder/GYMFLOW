package com.gymflow.feature.member.trainer

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymflow.core.components.BadgeStatus
import com.gymflow.core.components.GymFlowBadge
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.GymFlowDestructiveButton
import com.gymflow.core.components.GymFlowPrimaryButton
import com.gymflow.core.components.GymFlowTextField
import com.gymflow.core.components.GymFlowTopBar
import com.gymflow.core.theme.MemberBg
import com.gymflow.core.theme.MemberCardBorder
import com.gymflow.core.theme.MemberSurfaceSubtle
import com.gymflow.core.theme.MemberTextPrimary
import com.gymflow.core.theme.MemberTextSecondary
import com.gymflow.core.theme.MemberTextTertiary
import com.gymflow.core.theme.StatusActive
import com.gymflow.core.theme.StatusDestructive

@Composable
fun JoinTrainerScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TrainerConnectionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MemberBg)
    ) {
        GymFlowTopBar(
            title = "Join Your Trainer",
            onBackClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Status Header Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sync Status",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MemberTextSecondary
                )

                val badgeText: String
                val badgeStatus: BadgeStatus
                when (state.connectionStatus) {
                    TrainerConnectionStatus.CONNECTED -> {
                        badgeText = "Connected"
                        badgeStatus = BadgeStatus.ACTIVE
                    }
                    TrainerConnectionStatus.PENDING_APPROVAL -> {
                        badgeText = "Pending Approval"
                        badgeStatus = BadgeStatus.PENDING
                    }
                    TrainerConnectionStatus.NOT_CONNECTED -> {
                        badgeText = "Not Connected"
                        badgeStatus = BadgeStatus.NEUTRAL
                    }
                }
                GymFlowBadge(text = badgeText, status = badgeStatus)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Explanation Card
            GymFlowCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 16.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Enter Invitation Code",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MemberTextPrimary
                        )
                        Text(
                            text = "Get your unique 9-character code from your trainer (e.g. GYM-82K91)",
                            fontSize = 12.sp,
                            color = MemberTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Input Field
                GymFlowTextField(
                    value = state.codeInput,
                    onValueChange = { viewModel.onCodeChanged(it) },
                    placeholder = "e.g. GYM-82K91",
                    isMonospace = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters
                    )
                )

                if (state.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.errorMessage!!,
                        fontSize = 12.sp,
                        color = StatusDestructive
                    )
                }

                if (state.successMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.successMessage!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = StatusActive
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                GymFlowPrimaryButton(
                    text = if (state.isLoading) "Connecting..." else "Join Trainer",
                    onClick = { viewModel.submitJoinCode() },
                    enabled = !state.isLoading && state.codeInput.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trainer Preview or Connected Trainer Card
            val displayedTrainer = state.activeTrainer ?: state.previewTrainer
            if (displayedTrainer != null) {
                Text(
                    text = if (state.connectionStatus == TrainerConnectionStatus.CONNECTED) "Your Assigned Trainer" else "Trainer Preview",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MemberTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                GymFlowCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "MV",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = displayedTrainer.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MemberTextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (state.connectionStatus == TrainerConnectionStatus.CONNECTED) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = StatusActive,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = displayedTrainer.specialization,
                                fontSize = 13.sp,
                                color = MemberTextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MemberTextTertiary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = displayedTrainer.gymName,
                                    fontSize = 12.sp,
                                    color = MemberTextTertiary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrainerStatMini(label = "Experience", value = "${displayedTrainer.experienceYears} Years")
                        TrainerStatMini(label = "Active Members", value = "${displayedTrainer.activeMembersCount} Athletes")
                        TrainerStatMini(label = "Code", value = displayedTrainer.invitationCode)
                    }

                    if (state.connectionStatus == TrainerConnectionStatus.CONNECTED) {
                        Spacer(modifier = Modifier.height(16.dp))
                        GymFlowDestructiveButton(
                            text = "Disconnect Trainer",
                            onClick = { viewModel.disconnectTrainer() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Help link
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = { /* Help action */ }) {
                    Text(
                        text = "Need help finding your code?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MemberTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TrainerStatMini(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MemberTextTertiary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MemberTextPrimary
        )
    }
}
