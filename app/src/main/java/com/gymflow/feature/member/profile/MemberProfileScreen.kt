package com.gymflow.feature.member.profile

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gymflow.core.components.GymFlowBadge
import com.gymflow.core.components.GymFlowBottomBar
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowButtonStyle
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.GymFlowTextField
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite

@Composable
fun MemberProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToMeasurements: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToJoinTrainer: () -> Unit = {},
    onNavigateToEmailVerification: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isGoalDialogOpen by remember { mutableStateOf(false) }
    var isNotificationDialogOpen by remember { mutableStateOf(false) }
    var isAgeDialogOpen by remember { mutableStateOf(false) }
    var isHeightDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GymFlowTheme.colors.background,
        bottomBar = {
            GymFlowBottomBar(
                currentRoute = "member/profile",
                onNavigate = { route ->
                    when (route) {
                        "member/home" -> onNavigateToHome()
                        "member/schedule" -> onNavigateToSchedule()
                        "member/progress" -> onNavigateToProgress()
                        "member/activity" -> onNavigateToActivity()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Profile",
                    style = GymFlowTheme.typography.Heading1.copy(fontSize = 26.sp),
                    color = GymFlowTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Avatar & Real Name
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(GymFlowBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.initials,
                    style = GymFlowTheme.typography.Heading1.copy(
                        color = GymFlowWhite,
                        fontSize = 28.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.user.name.ifBlank { "GymFlow Member" },
                    style = GymFlowTheme.typography.Heading2.copy(fontWeight = FontWeight.Bold),
                    color = GymFlowTheme.colors.textPrimary
                )
                if (uiState.user.isEmailVerified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Email",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (uiState.user.email.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = uiState.user.email,
                    style = GymFlowTheme.typography.BodyRegular,
                    color = GymFlowTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                GymFlowBadge(
                    text = "MEMBER · ${uiState.joinedDateFormatted.uppercase()}",
                    backgroundColor = GymFlowTheme.colors.surfaceVariant,
                    textColor = GymFlowTheme.colors.textSecondary
                )
                if (uiState.user.isEmailVerified) {
                    Spacer(modifier = Modifier.width(8.dp))
                    GymFlowBadge(
                        text = "VERIFIED",
                        backgroundColor = Color(0xFFECFDF5),
                        textColor = Color(0xFF059669)
                    )
                }
            }

            if (!uiState.user.isEmailVerified) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFFBEB))
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
                        .clickable { onNavigateToEmailVerification() }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailUnread,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Verify Email Address",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Tap to verify ${uiState.user.email.ifBlank { "your email" }}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Personal Info Summary (3 cols: Age, Height, Goal)
            GymFlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isAgeDialogOpen = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "AGE",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (uiState.profile.age > 0) "${uiState.profile.age}" else "--",
                            style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = GymFlowTheme.colors.textPrimary
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isHeightDialogOpen = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "HEIGHT",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (uiState.profile.heightCm > 0f) "${uiState.profile.heightCm.toInt()} cm" else "--",
                            style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = GymFlowTheme.colors.textPrimary
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isGoalDialogOpen = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "GOAL",
                            style = GymFlowTheme.typography.Caption,
                            color = GymFlowTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.profile.fitnessGoal.ifBlank { "Set Goal" },
                            style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = GymFlowTheme.colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "PERSONAL FITNESS",
                    style = GymFlowTheme.typography.Caption.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = GymFlowTheme.colors.textSecondary
                )
            }

            // 1. Body Measurements Row
            ProfileNavigationCard(
                title = "Body Measurements",
                subtitle = "Track chest, waist, hips, arms & legs",
                icon = Icons.Default.Straighten,
                onClick = onNavigateToMeasurements
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Fitness Goals Row
            ProfileNavigationCard(
                title = "Fitness Goals",
                subtitle = "Active: ${uiState.profile.fitnessGoal}",
                icon = Icons.Default.FitnessCenter,
                onClick = { isGoalDialogOpen = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Connected Trainer Row
            ProfileNavigationCard(
                title = "Trainer Connection",
                subtitle = if (!uiState.profile.connectedTrainerId.isNullOrBlank()) "Connected with Coach Marcus" else "Link with your Trainer via Invite Code",
                icon = Icons.Default.Person,
                onClick = onNavigateToJoinTrainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Notifications & Reminders Row
            ProfileNavigationCard(
                title = "Notifications & Reminders",
                subtitle = "Workout reminders & weigh-in alerts",
                icon = Icons.Default.Notifications,
                onClick = { isNotificationDialogOpen = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Settings & Privacy Row
            ProfileNavigationCard(
                title = "Settings & Privacy",
                subtitle = "Theme, data sync, account security",
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings
            )

            Spacer(modifier = Modifier.height(24.dp))

            GymFlowButton(
                text = "Log Out",
                onClick = { viewModel.showLogoutDialog() },
                style = GymFlowButtonStyle.DESTRUCTIVE,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog 1: Fitness Goals Selection Dialog
    if (isGoalDialogOpen) {
        val goals = listOf(
            "Build Muscle" to "Focus on hypertrophy, progressive volume & strength gains",
            "Lose Fat / Cut" to "High metabolic rate, calorie deficit & conditioning",
            "Strength & Power" to "Heavy compound lifts & maximal 1RM development",
            "Maintain & Tone" to "Balanced physical wellness & functional fitness",
            "Endurance & Stamina" to "Cardiovascular health, aerobic capacity & recovery"
        )

        var selectedGoal by remember { mutableStateOf(uiState.profile.fitnessGoal) }

        AlertDialog(
            onDismissRequest = { isGoalDialogOpen = false },
            title = {
                Text(
                    text = "Select Fitness Goal",
                    style = GymFlowTheme.typography.Heading2.copy(fontSize = 20.sp),
                    color = GymFlowTheme.colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Choose your primary training objective. Your schedules and analytics will adjust accordingly.",
                        style = GymFlowTheme.typography.BodyRegular,
                        color = GymFlowTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    goals.forEach { (goalName, goalDesc) ->
                        val isSelected = selectedGoal.equals(goalName, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) GymFlowTheme.colors.surfaceVariant else GymFlowTheme.colors.surface
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) GymFlowTheme.colors.textPrimary else GymFlowTheme.colors.border,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedGoal = goalName }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = goalName,
                                        style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GymFlowTheme.colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = goalDesc,
                                        style = GymFlowTheme.typography.Caption,
                                        color = GymFlowTheme.colors.textSecondary
                                    )
                                }

                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(GymFlowTheme.colors.textPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = GymFlowTheme.colors.surface,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateGoal(selectedGoal)
                        isGoalDialogOpen = false
                    }
                ) {
                    Text(
                        text = "Save Goal",
                        color = GymFlowTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isGoalDialogOpen = false }) {
                    Text(text = "Cancel", color = GymFlowTheme.colors.textSecondary)
                }
            },
            containerColor = GymFlowTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog 1b: Edit Age Dialog
    if (isAgeDialogOpen) {
        var ageInput by remember { mutableStateOf(if (uiState.profile.age > 0) "${uiState.profile.age}" else "") }

        AlertDialog(
            onDismissRequest = { isAgeDialogOpen = false },
            title = {
                Text(
                    text = "Edit Age",
                    style = GymFlowTheme.typography.Heading2.copy(fontSize = 20.sp),
                    color = GymFlowTheme.colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enter your current age to optimize workout intensity and metabolic calculations.",
                        style = GymFlowTheme.typography.BodyRegular,
                        color = GymFlowTheme.colors.textSecondary
                    )
                    GymFlowTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it.filter { char -> char.isDigit() }.take(3) },
                        label = "Age (Years)",
                        placeholder = "e.g. 25",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = ageInput.toIntOrNull() ?: 0
                        viewModel.updateAge(parsed)
                        isAgeDialogOpen = false
                    }
                ) {
                    Text(
                        text = "Save Age",
                        color = GymFlowTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isAgeDialogOpen = false }) {
                    Text(text = "Cancel", color = GymFlowTheme.colors.textSecondary)
                }
            },
            containerColor = GymFlowTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog 1c: Edit Height Dialog
    if (isHeightDialogOpen) {
        var heightInput by remember {
            mutableStateOf(if (uiState.profile.heightCm > 0f) "${uiState.profile.heightCm.toInt()}" else "")
        }

        AlertDialog(
            onDismissRequest = { isHeightDialogOpen = false },
            title = {
                Text(
                    text = "Edit Height",
                    style = GymFlowTheme.typography.Heading2.copy(fontSize = 20.sp),
                    color = GymFlowTheme.colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enter your height in centimeters (cm) for accurate BMI, posture and volume metrics.",
                        style = GymFlowTheme.typography.BodyRegular,
                        color = GymFlowTheme.colors.textSecondary
                    )
                    GymFlowTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it.filter { char -> char.isDigit() || char == '.' }.take(5) },
                        label = "Height (cm)",
                        placeholder = "e.g. 175",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = heightInput.toFloatOrNull() ?: 0f
                        viewModel.updateHeight(parsed)
                        isHeightDialogOpen = false
                    }
                ) {
                    Text(
                        text = "Save Height",
                        color = GymFlowTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isHeightDialogOpen = false }) {
                    Text(text = "Cancel", color = GymFlowTheme.colors.textSecondary)
                }
            },
            containerColor = GymFlowTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog 2: Notifications & Reminders Dialog
    if (isNotificationDialogOpen) {
        AlertDialog(
            onDismissRequest = { isNotificationDialogOpen = false },
            title = {
                Text(
                    text = "Notification Settings",
                    style = GymFlowTheme.typography.Heading2.copy(fontSize = 20.sp),
                    color = GymFlowTheme.colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Push Notifications",
                                style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = GymFlowTheme.colors.textPrimary
                            )
                            Text(
                                text = "General workout and progress updates",
                                style = GymFlowTheme.typography.Caption,
                                color = GymFlowTheme.colors.textSecondary
                            )
                        }
                        Switch(
                            checked = uiState.pushNotificationsEnabled,
                            onCheckedChange = { viewModel.togglePushNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GymFlowTheme.colors.surface,
                                checkedTrackColor = GymFlowTheme.colors.textPrimary
                            )
                        )
                    }

                    HorizontalDivider(color = GymFlowTheme.colors.border, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Workout Reminders",
                                style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = GymFlowTheme.colors.textPrimary
                            )
                            Text(
                                text = "Morning reminder of today's assigned session",
                                style = GymFlowTheme.typography.Caption,
                                color = GymFlowTheme.colors.textSecondary
                            )
                        }
                        Switch(
                            checked = uiState.workoutRemindersEnabled,
                            onCheckedChange = { viewModel.toggleWorkoutReminders(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GymFlowTheme.colors.surface,
                                checkedTrackColor = GymFlowTheme.colors.textPrimary
                            )
                        )
                    }

                    HorizontalDivider(color = GymFlowTheme.colors.border, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Weigh-in Check-in",
                                style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = GymFlowTheme.colors.textPrimary
                            )
                            Text(
                                text = "Prompt to log pre & post workout weight",
                                style = GymFlowTheme.typography.Caption,
                                color = GymFlowTheme.colors.textSecondary
                            )
                        }
                        Switch(
                            checked = uiState.weightRemindersEnabled,
                            onCheckedChange = { viewModel.toggleWeightReminders(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GymFlowTheme.colors.surface,
                                checkedTrackColor = GymFlowTheme.colors.textPrimary
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isNotificationDialogOpen = false }) {
                    Text(
                        text = "Done",
                        color = GymFlowTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = GymFlowTheme.colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialog 3: Logout Dialog
    if (uiState.isLogoutDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogoutDialog() },
            title = {
                Text(text = "Log Out", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "Are you sure you want to log out of GYMFLOW?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmLogout(onLogout)
                    }
                ) {
                    Text(text = "Log Out", color = GymFlowTheme.colors.accentDestructive, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLogoutDialog() }) {
                    Text(text = "Cancel", color = GymFlowTheme.colors.textSecondary)
                }
            },
            containerColor = GymFlowTheme.colors.surface
        )
    }
}

@Composable
private fun ProfileNavigationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GymFlowCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GymFlowTheme.colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = GymFlowTheme.colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = GymFlowTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = subtitle,
                        style = GymFlowTheme.typography.Caption,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = GymFlowTheme.colors.textTertiary
            )
        }
    }
}
