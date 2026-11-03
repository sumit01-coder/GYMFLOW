package com.gymflow.feature.auth

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.R
import com.gymflow.core.components.GymFlowButton
import com.gymflow.core.components.GymFlowTextField
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.domain.model.Role

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (Role) -> Unit,
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GymFlowTheme.colors.textPrimary
                    )
                }

                // Official Brand Emblem
                Image(
                    painter = painterResource(id = R.drawable.gymflow_emblem_transparent),
                    contentDescription = "GYMFLOW",
                    modifier = Modifier.size(46.dp)
                )

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Create Account",
                style = GymFlowTheme.typography.Heading1.copy(fontSize = 28.sp),
                color = GymFlowTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Start your fitness journey today",
                style = GymFlowTheme.typography.Subtitle,
                color = GymFlowTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Role Selector Segmented Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "I am joining as a",
                    style = GymFlowTheme.typography.Caption,
                    color = GymFlowTheme.colors.textSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = uiState.role == Role.MEMBER,
                        onClick = { viewModel.onRoleChange(Role.MEMBER) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = GymFlowTheme.colors.primaryButtonBg,
                            activeContentColor = GymFlowTheme.colors.primaryButtonFg,
                            inactiveContainerColor = GymFlowTheme.colors.surfaceVariant,
                            inactiveContentColor = GymFlowTheme.colors.textSecondary
                        )
                    ) {
                        Text(text = "Member", fontWeight = FontWeight.SemiBold)
                    }

                    SegmentedButton(
                        selected = uiState.role == Role.TRAINER,
                        onClick = { viewModel.onRoleChange(Role.TRAINER) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = GymFlowTheme.colors.primaryButtonBg,
                            activeContentColor = GymFlowTheme.colors.primaryButtonFg,
                            inactiveContainerColor = GymFlowTheme.colors.surfaceVariant,
                            inactiveContentColor = GymFlowTheme.colors.textSecondary
                        )
                    ) {
                        Text(text = "Trainer", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            GymFlowTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = "Full Name",
                placeholder = "Alex Johnson",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            GymFlowTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email Address",
                placeholder = "alex@example.com",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            GymFlowTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Password",
                placeholder = "At least 6 characters",
                isPassword = true,
                leadingIcon = Icons.Default.Lock,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                errorMessage = uiState.error
            )

            Spacer(modifier = Modifier.height(28.dp))

            GymFlowButton(
                text = "Create Account",
                onClick = { viewModel.register(onRegisterSuccess) },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = GymFlowTheme.typography.BodyRegular,
                    color = GymFlowTheme.colors.textSecondary
                )
                Text(
                    text = "Log In",
                    style = GymFlowTheme.typography.BodyRegular.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GymFlowTheme.colors.textPrimary,
                    modifier = Modifier.clickable(onClick = onNavigateToLogin)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
