package com.gymflow.feature.auth

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowButtonStyle
import com.gymflow.core.components.GymFlowCard
import com.gymflow.core.components.GymFlowPrimaryButton
import com.gymflow.core.components.GymFlowSecondaryButton
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.MemberCardBorder
import com.gymflow.core.theme.MemberSurfaceSubtle
import com.gymflow.core.theme.MemberTextPrimary
import com.gymflow.core.theme.MemberTextSecondary
import com.gymflow.core.theme.MemberTextTertiary
import com.gymflow.core.theme.StatusActive
import com.gymflow.core.theme.StatusDestructive
import com.gymflow.domain.model.Role

@Composable
fun EmailVerificationScreen(
    viewModel: AuthViewModel,
    onVerificationSuccess: (Role) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GymFlowTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GymFlowTheme.colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Email Verification Badge Illustration
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(GymFlowTheme.colors.surface)
                        .border(1.5.dp, GymFlowTheme.colors.border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(GymFlowBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title and Subtitle
            Text(
                text = "Verify Your Email",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = GymFlowTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We have sent an authentication verification link to:",
                fontSize = 14.sp,
                color = MemberTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Registered Email Card
            GymFlowCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 14.dp,
                backgroundColor = MemberSurfaceSubtle
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MemberTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.email.ifBlank { "athlete@gymflow.com" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = MemberTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Instructions Box
            GymFlowCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 16.dp
            ) {
                Text(
                    text = "Quick Verification Steps",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MemberTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                VerificationStepRow(
                    stepNumber = "1",
                    instruction = "Open your email inbox and search for 'GYMFLOW' or 'Firebase'."
                )
                Spacer(modifier = Modifier.height(10.dp))
                VerificationStepRow(
                    stepNumber = "2",
                    instruction = "Click the secure verification link inside the confirmation message."
                )
                Spacer(modifier = Modifier.height(10.dp))
                VerificationStepRow(
                    stepNumber = "3",
                    instruction = "Return right here and tap 'I've Verified My Email' to unlock your dashboard."
                )
            }

            // Status message / Feedback alert
            if (uiState.verificationMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFECFDF5))
                        .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusActive,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.verificationMessage!!,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }

            if (uiState.verificationError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = StatusDestructive,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.verificationError!!,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Check Verification Button
            GymFlowPrimaryButton(
                text = if (uiState.isCheckingVerification) "Checking Verification..." else "I've Verified My Email",
                onClick = { viewModel.checkEmailVerification(onVerificationSuccess) },
                enabled = !uiState.isCheckingVerification,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Resend Verification Link Button
            val cooldown = uiState.resendCooldownSeconds
            val resendText = if (cooldown > 0) "Resend Email in ${cooldown}s" else "Resend Verification Email"
            GymFlowSecondaryButton(
                text = resendText,
                onClick = { viewModel.resendVerificationEmail() },
                enabled = cooldown == 0 && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Skip for Now link
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { viewModel.skipVerificationForNow(onVerificationSuccess) }
                ) {
                    Text(
                        text = "Skip for now (verify later in Profile)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MemberTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun VerificationStepRow(
    stepNumber: String,
    instruction: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MemberSurfaceSubtle)
                .border(0.5.dp, MemberCardBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MemberTextPrimary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = instruction,
            fontSize = 13.sp,
            color = MemberTextSecondary,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
