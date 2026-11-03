package com.gymflow.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme

@Composable
fun GymFlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    suffixText: String? = null,
    isMonospace: Boolean = false,
    isPassword: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    errorMessage: String? = null,
    isError: Boolean = errorMessage != null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = GymFlowTheme.typography.Caption,
                color = GymFlowTheme.colors.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = enabled,
            singleLine = singleLine,
            shape = GymFlowShapesTokens.InputField,
            textStyle = GymFlowTheme.typography.BodyMedium.copy(
                color = GymFlowTheme.colors.textPrimary,
                fontFamily = if (isMonospace) androidx.compose.ui.text.font.FontFamily.Monospace else androidx.compose.ui.text.font.FontFamily.Default
            ),
            placeholder = if (placeholder != null) {
                {
                    Text(
                        text = placeholder,
                        style = GymFlowTheme.typography.BodyRegular,
                        color = GymFlowTheme.colors.textTertiary
                    )
                }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = GymFlowTheme.colors.textSecondary
                    )
                }
            } else null,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            modifier = Modifier.size(20.dp),
                            tint = GymFlowTheme.colors.textSecondary
                        )
                    }
                }
            } else if (suffixText != null) {
                {
                    Text(
                        text = suffixText,
                        style = GymFlowTheme.typography.BodySmall,
                        color = GymFlowTheme.colors.textSecondary
                    )
                }
            } else trailingIcon,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GymFlowTheme.colors.textPrimary,
                unfocusedBorderColor = GymFlowTheme.colors.border,
                focusedContainerColor = GymFlowTheme.colors.surface,
                unfocusedContainerColor = GymFlowTheme.colors.surface,
                disabledContainerColor = GymFlowTheme.colors.surfaceVariant,
                errorBorderColor = GymFlowTheme.colors.accentDestructive,
                cursorColor = GymFlowTheme.colors.textPrimary
            )
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = GymFlowTheme.typography.Caption,
                color = GymFlowTheme.colors.accentDestructive,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
