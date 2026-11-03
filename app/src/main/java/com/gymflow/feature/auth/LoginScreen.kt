package com.gymflow.feature.auth

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowButtonStyle
import com.gymflow.core.components.GymFlowTextField
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.domain.model.Role

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: (Role) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showJoinCodeDialog by remember { mutableStateOf(false) }

    if (showJoinCodeDialog) {
        AlertDialog(
            onDismissRequest = { showJoinCodeDialog = false },
            containerColor = GymFlowTheme.colors.surface,
            title = {
                Text(
                    text = "Login with Join Code",
                    style = GymFlowTheme.typography.Heading2,
                    color = GymFlowTheme.colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your 8-character invitation code provided by your trainer.",
                        style = GymFlowTheme.typography.BodyRegular,
                        color = GymFlowTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GymFlowTextField(
                        value = uiState.joinCode,
                        onValueChange = viewModel::onJoinCodeChange,
                        placeholder = "e.g. GYM-82K91",
                        leadingIcon = Icons.Default.Key
                    )
                }
            },
            confirmButton = {
                GymFlowButton(
                    text = "Verify & Join",
                    onClick = {
                        showJoinCodeDialog = false
                        viewModel.loginWithJoinCode(onLoginSuccess)
                    },
                    fullWidth = false
                )
            },
            dismissButton = {
                TextButton(onClick = { showJoinCodeDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = GymFlowTheme.colors.textSecondary
                    )
                }
            }
        )
    }

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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (onNavigateBack != null) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GymFlowTheme.colors.textPrimary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Centered Dumbbell Logo
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(GymFlowBlack, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "GymFlow",
                        modifier = Modifier.size(22.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Welcome Headings
            Text(
                text = "Welcome Back",
                style = GymFlowTheme.typography.Heading1.copy(fontSize = 28.sp),
                color = GymFlowTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Login to continue your journey",
                style = GymFlowTheme.typography.Subtitle,
                color = GymFlowTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Form Fields
            GymFlowTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email or Phone",
                placeholder = "you@example.com",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            GymFlowTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Password",
                placeholder = "••••••••",
                isPassword = true,
                leadingIcon = Icons.Default.Lock,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                errorMessage = uiState.error
            )

            // Forgot Password Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    style = GymFlowTheme.typography.Caption.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = GymFlowTheme.colors.textPrimary,
                    modifier = Modifier.clickable(onClick = onNavigateToForgotPassword)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Login Action
            GymFlowButton(
                text = "Login",
                onClick = { viewModel.login(onLoginSuccess) },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Divider: — or —
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = GymFlowTheme.colors.border
                )
                Text(
                    text = "  or  ",
                    style = GymFlowTheme.typography.Caption,
                    color = GymFlowTheme.colors.textTertiary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = GymFlowTheme.colors.border
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Outlined Join Code Action
            GymFlowButton(
                text = "Login with Join Code",
                onClick = { showJoinCodeDialog = true },
                style = GymFlowButtonStyle.OUTLINED,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = GymFlowTheme.colors.textPrimary
                    )
                }
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Create Account Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = GymFlowTheme.typography.BodyRegular,
                    color = GymFlowTheme.colors.textSecondary
                )
                Text(
                    text = "Create Account",
                    style = GymFlowTheme.typography.BodyRegular.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GymFlowTheme.colors.textPrimary,
                    modifier = Modifier.clickable(onClick = onNavigateToRegister)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
