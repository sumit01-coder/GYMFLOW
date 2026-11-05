package com.gymflow.feature.member.profile

import android.app.TimePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.GymFlowDestructiveButton
import com.gymflow.core.components.GymFlowSecondaryButton
import com.gymflow.core.components.GymFlowTopBar
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowWhite
import com.gymflow.core.theme.MemberBg
import com.gymflow.core.theme.MemberBtnPrimaryBg
import com.gymflow.core.theme.MemberCardBorder
import com.gymflow.core.theme.MemberSurfaceSubtle
import com.gymflow.core.theme.MemberTextPrimary
import com.gymflow.core.theme.MemberTextSecondary
import com.gymflow.core.theme.MemberTextTertiary
import com.gymflow.core.theme.StatusActive

@Composable
fun MemberSettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateTrainerSync: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val timePickerDialog = remember(context, state.reminderHour, state.reminderMinute) {
        TimePickerDialog(
            context,
            { _, selectedHour: Int, selectedMinute: Int ->
                viewModel.updateReminderTime(selectedHour, selectedMinute)
            },
            state.reminderHour,
            state.reminderMinute,
            false
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MemberBg)
    ) {
        GymFlowTopBar(
            title = "Settings",
            onBackClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Trainer Sync Section
            Text(
                text = "Trainer Connection",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MemberTextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            GymFlowCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 14.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateTrainerSync),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MemberSurfaceSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = MemberTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        val isConnected = state.profile.connectedTrainerId != null
                        Column {
                            Text(
                                text = "Trainer Studio Sync",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MemberTextPrimary
                            )
                            Text(
                                text = if (isConnected) "Connected with Trainer" else "Not Connected (Tap to link)",
                                fontSize = 12.sp,
                                color = if (isConnected) StatusActive else MemberTextTertiary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MemberTextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notifications Section
            Text(
                text = "Notifications & Reminders",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MemberTextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            GymFlowCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 8.dp
            ) {
                SettingSwitchRow(
                    title = "Push Notifications",
                    subtitle = "Trainer notes and plan updates",
                    checked = state.pushNotificationsEnabled,
                    onCheckedChange = { viewModel.togglePushNotifications(it) }
                )
                HorizontalDivider(color = MemberCardBorder, thickness = 0.5.dp)
                SettingSwitchRow(
                    title = "Workout Reminders",
                    subtitle = "Daily schedule alerts",
                    checked = state.workoutRemindersEnabled,
                    onCheckedChange = { viewModel.toggleWorkoutReminders(it) }
                )
                if (state.workoutRemindersEnabled) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MemberSurfaceSubtle)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = MemberTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Daily Reminder Time",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MemberTextPrimary
                                    )
                                    Text(
                                        text = "Exact alarm trigger time",
                                        fontSize = 11.sp,
                                        color = MemberTextTertiary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GymFlowBlack)
                                    .clickable { timePickerDialog.show() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = state.formattedReminderTime,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymFlowWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { viewModel.sendTestReminder() },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MemberTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Send Test Reminder",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MemberTextSecondary
                                )
                            }
                        }
                    }
                }
                HorizontalDivider(color = MemberCardBorder, thickness = 0.5.dp)
                SettingSwitchRow(
                    title = "Weight Log Prompts",
                    subtitle = "Post-workout fluid deficit reminder",
                    checked = state.weightRemindersEnabled,
                    onCheckedChange = { viewModel.toggleWeightReminders(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Appearance & App
            Text(
                text = "Application",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MemberTextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            GymFlowCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 8.dp
            ) {
                SettingClickableRow(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    value = "Monochrome Light"
                )
                HorizontalDivider(color = MemberCardBorder, thickness = 0.5.dp)
                SettingClickableRow(
                    icon = Icons.Default.Lock,
                    title = "Privacy & Data",
                    value = "Encrypted"
                )
                HorizontalDivider(color = MemberCardBorder, thickness = 0.5.dp)
                SettingClickableRow(
                    icon = Icons.Default.Code,
                    title = "Developer",
                    value = "Sumit Patel"
                )
                HorizontalDivider(color = MemberCardBorder, thickness = 0.5.dp)
                SettingClickableRow(
                    icon = Icons.Default.SystemUpdate,
                    title = "Check for Updates",
                    value = "v${com.gymflow.BuildConfig.VERSION_NAME}",
                    onClick = { viewModel.checkForUpdates() }
                )
                HorizontalDivider(color = MemberCardBorder, thickness = 0.5.dp)
                SettingClickableRow(
                    icon = Icons.Default.Info,
                    title = "GYMFLOW Version",
                    value = "${com.gymflow.BuildConfig.VERSION_NAME} (Build ${com.gymflow.BuildConfig.VERSION_CODE})"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            GymFlowDestructiveButton(
                text = "Log Out",
                onClick = { viewModel.showLogoutDialog() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Developed by Sumit Patel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MemberTextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "GYMFLOW High-Performance Athletic Suite",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MemberTextTertiary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (state.isLogoutDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogoutDialog() },
            title = {
                Text(
                    text = "Log Out",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MemberTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of GYMFLOW?",
                    fontSize = 13.sp,
                    color = MemberTextSecondary
                )
            },
            confirmButton = {
                GymFlowDestructiveButton(
                    text = "Log Out",
                    onClick = { viewModel.confirmLogout(onLoggedOut) }
                )
            },
            dismissButton = {
                GymFlowSecondaryButton(
                    text = "Cancel",
                    onClick = { viewModel.dismissLogoutDialog() }
                )
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MemberTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MemberTextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MemberBtnPrimaryBg,
                uncheckedThumbColor = Color(0xFF9CA3AF),
                uncheckedTrackColor = Color(0xFFE5E7EB)
            )
        )
    }
}

@Composable
private fun SettingClickableRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MemberSurfaceSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MemberTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MemberTextPrimary
            )
        }

        Text(
            text = value,
            fontSize = 13.sp,
            color = MemberTextSecondary
        )
    }
}
