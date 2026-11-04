package com.gymflow.feature.trainer.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymflow.feature.trainer.components.TrainerBottomNavBar
import com.gymflow.feature.trainer.components.TrainerBottomNavTab
import com.gymflow.feature.trainer.components.TrainerButton
import com.gymflow.feature.trainer.components.TrainerButtonStyle
import com.gymflow.feature.trainer.components.TrainerCard
import com.gymflow.feature.trainer.components.TrainerTopBar
import com.gymflow.feature.trainer.theme.TrainerDarkBg
import com.gymflow.feature.trainer.theme.TrainerDarkBorder
import com.gymflow.feature.trainer.theme.TrainerDarkCard
import com.gymflow.feature.trainer.theme.TrainerDarkTextPrimary
import com.gymflow.feature.trainer.theme.TrainerDarkTextSecondary
import com.gymflow.feature.trainer.theme.TrainerDarkTextTertiary
import com.gymflow.feature.trainer.theme.TrainerTheme

@Composable
fun TrainerSettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToJoinCode: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToPlans: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrainerSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TrainerTheme {
        Scaffold(
            topBar = {
                TrainerTopBar(
                    title = "Trainer Settings",
                    subtitle = "Studio Profile & Preferences",
                    onBackClick = onBackClick
                )
            },
            bottomBar = {
                TrainerBottomNavBar(
                    selectedTab = TrainerBottomNavTab.SETTINGS,
                    onTabSelected = { tab ->
                        when (tab) {
                            TrainerBottomNavTab.DASHBOARD -> onNavigateToDashboard()
                            TrainerBottomNavTab.MEMBERS -> onNavigateToMembers()
                            TrainerBottomNavTab.PLANS -> onNavigateToPlans()
                            TrainerBottomNavTab.ANALYTICS -> onNavigateToAnalytics()
                            TrainerBottomNavTab.SETTINGS -> {}
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Coach Profile Card
                TrainerCard(contentPadding = 18.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF27272A))
                                .border(1.dp, Color(0xFF4ADE80), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TrainerDarkTextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = uiState.coachName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrainerDarkTextPrimary
                            )
                            Text(
                                text = uiState.title,
                                fontSize = 12.sp,
                                color = Color(0xFF4ADE80),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = uiState.email,
                                fontSize = 12.sp,
                                color = TrainerDarkTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Credentials & Specializations
                Text(
                    text = "Professional Credentials",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrainerDarkTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                TrainerCard(contentPadding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Coaching Experience", fontSize = 13.sp, color = TrainerDarkTextSecondary)
                            Text("${uiState.experienceYears}+ Years Active", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TrainerDarkTextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Specializations", fontSize = 13.sp, color = TrainerDarkTextSecondary)
                            Text(uiState.specialization, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TrainerDarkTextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gym Affiliation", fontSize = 13.sp, color = TrainerDarkTextSecondary)
                            Text(uiState.gymAffiliation, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TrainerDarkTextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Studio Management Shortcuts
                Text(
                    text = "Studio Management",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrainerDarkTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrainerCard(
                        contentPadding = 14.dp,
                        onClick = onNavigateToMembers
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.People, contentDescription = null, tint = TrainerDarkTextPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Connected Members", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TrainerDarkTextPrimary)
                                    Text("${uiState.totalMembers} active athletes enrolled", fontSize = 11.sp, color = TrainerDarkTextSecondary)
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TrainerDarkTextTertiary, modifier = Modifier.size(16.dp))
                        }
                    }

                    TrainerCard(
                        contentPadding = 14.dp,
                        onClick = onNavigateToJoinCode
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Trainer Invitation Code", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TrainerDarkTextPrimary)
                                    Text("Manage connection code & approval queue", fontSize = 11.sp, color = TrainerDarkTextSecondary)
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TrainerDarkTextTertiary, modifier = Modifier.size(16.dp))
                        }
                    }

                    TrainerCard(
                        contentPadding = 14.dp,
                        onClick = onNavigateToAnalytics
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Analytics, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Studio Analytics & PDF Reports", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TrainerDarkTextPrimary)
                                    Text("78% completion rate, volume analysis", fontSize = 11.sp, color = TrainerDarkTextSecondary)
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TrainerDarkTextTertiary, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Notifications & Studio Preferences
                Text(
                    text = "Studio Preferences & Alerts",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrainerDarkTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                TrainerCard(contentPadding = 14.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Workout Completion Alerts", fontSize = 14.sp, color = TrainerDarkTextPrimary)
                                Text("Instant push notification when an athlete completes sets", fontSize = 11.sp, color = TrainerDarkTextSecondary)
                            }
                            Switch(
                                checked = uiState.workoutCompletionNotifications,
                                onCheckedChange = { viewModel.toggleWorkoutCompletionNotifications(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4ADE80),
                                    uncheckedThumbColor = TrainerDarkTextSecondary,
                                    uncheckedTrackColor = Color(0xFF27272A)
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("New Join Code Requests", fontSize = 14.sp, color = TrainerDarkTextPrimary)
                                Text("Notify when athletes enter your studio invitation code", fontSize = 11.sp, color = TrainerDarkTextSecondary)
                            }
                            Switch(
                                checked = uiState.joinRequestNotifications,
                                onCheckedChange = { viewModel.toggleJoinRequestNotifications(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4ADE80),
                                    uncheckedThumbColor = TrainerDarkTextSecondary,
                                    uncheckedTrackColor = Color(0xFF27272A)
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sound & Haptics", fontSize = 14.sp, color = TrainerDarkTextPrimary)
                                Text("Feedback on plan approval & reviews", fontSize = 11.sp, color = TrainerDarkTextSecondary)
                            }
                            Switch(
                                checked = uiState.soundAndHaptics,
                                onCheckedChange = { viewModel.toggleSoundAndHaptics(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4ADE80),
                                    uncheckedThumbColor = TrainerDarkTextSecondary,
                                    uncheckedTrackColor = Color(0xFF27272A)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Update Card
                TrainerCard(
                    contentPadding = 14.dp,
                    onClick = { viewModel.checkForUpdates() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Check for Updates",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TrainerDarkTextPrimary
                                )
                                Text(
                                    text = "Version ${com.gymflow.BuildConfig.VERSION_NAME} (Build ${com.gymflow.BuildConfig.VERSION_CODE})",
                                    fontSize = 11.sp,
                                    color = TrainerDarkTextSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = TrainerDarkTextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Log Out Action Button
                TrainerButton(
                    text = "Log Out of Trainer Studio",
                    onClick = { viewModel.showLogoutDialog() },
                    style = TrainerButtonStyle.DESTRUCTIVE,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Developed by Sumit Patel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrainerDarkTextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "GYMFLOW Trainer Studio Pro v1.0.0 (OLED Studio Build)",
                    fontSize = 11.sp,
                    color = TrainerDarkTextTertiary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Logout Confirmation Dialog
        if (uiState.showLogoutConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissLogoutDialog() },
                title = {
                    Text(
                        text = "Log Out from Trainer Studio?",
                        color = TrainerDarkTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "You will need to sign in with your verified trainer credentials to manage athlete routines and reviews.",
                        color = TrainerDarkTextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.dismissLogoutDialog()
                            onLogout()
                        }
                    ) {
                        Text("Log Out", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissLogoutDialog() }) {
                        Text("Cancel", color = TrainerDarkTextSecondary)
                    }
                },
                containerColor = TrainerDarkCard,
                textContentColor = TrainerDarkTextSecondary
            )
        }
    }
}
