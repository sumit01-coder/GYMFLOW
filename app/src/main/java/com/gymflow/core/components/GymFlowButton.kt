package com.gymflow.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite

enum class GymFlowButtonStyle {
    PRIMARY, // Solid black pill
    OUTLINED, // Clean 1dp border pill
    SECONDARY, // Subtle gray background pill
    DESTRUCTIVE, // Red background pill
    TEXT // Text only
}

object GymFlowButtonDefaults {
    val Height = 52.dp
    val PillShape = GymFlowShapesTokens.ButtonPill
}

@Composable
fun GymFlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: GymFlowButtonStyle = GymFlowButtonStyle.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    height: Dp = 52.dp,
    fullWidth: Boolean = true
) {
    val buttonModifier = if (fullWidth) {
        modifier.fillMaxWidth().height(height)
    } else {
        modifier.height(height)
    }

    when (style) {
        GymFlowButtonStyle.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = GymFlowShapesTokens.ButtonPill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GymFlowTheme.colors.primaryButtonBg,
                    contentColor = GymFlowTheme.colors.primaryButtonFg,
                    disabledContainerColor = GymFlowTheme.colors.border,
                    disabledContentColor = GymFlowTheme.colors.textTertiary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                ButtonContent(
                    text = text,
                    isLoading = isLoading,
                    contentColor = GymFlowTheme.colors.primaryButtonFg,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            }
        }
        GymFlowButtonStyle.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = GymFlowShapesTokens.ButtonPill,
                border = BorderStroke(1.dp, GymFlowTheme.colors.border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = GymFlowTheme.colors.textPrimary,
                    disabledContentColor = GymFlowTheme.colors.textTertiary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                ButtonContent(
                    text = text,
                    isLoading = isLoading,
                    contentColor = GymFlowTheme.colors.textPrimary,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            }
        }
        GymFlowButtonStyle.SECONDARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = GymFlowShapesTokens.ButtonPill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GymFlowTheme.colors.secondaryButtonBg,
                    contentColor = GymFlowTheme.colors.secondaryButtonFg,
                    disabledContainerColor = GymFlowTheme.colors.border,
                    disabledContentColor = GymFlowTheme.colors.textTertiary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                ButtonContent(
                    text = text,
                    isLoading = isLoading,
                    contentColor = GymFlowTheme.colors.secondaryButtonFg,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            }
        }
        GymFlowButtonStyle.DESTRUCTIVE -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = GymFlowShapesTokens.ButtonPill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GymFlowTheme.colors.accentDestructive,
                    contentColor = GymFlowWhite
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                ButtonContent(
                    text = text,
                    isLoading = isLoading,
                    contentColor = GymFlowWhite,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            }
        }
        GymFlowButtonStyle.TEXT -> {
            TextButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = GymFlowShapesTokens.ButtonPill,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = GymFlowTheme.colors.textPrimary
                )
            ) {
                ButtonContent(
                    text = text,
                    isLoading = isLoading,
                    contentColor = GymFlowTheme.colors.textPrimary,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            }
        }
    }
}

@Composable
fun GymFlowPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) = GymFlowButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    style = GymFlowButtonStyle.PRIMARY,
    enabled = enabled,
    isLoading = isLoading,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon
)

@Composable
fun GymFlowSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) = GymFlowButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    style = GymFlowButtonStyle.SECONDARY,
    enabled = enabled,
    isLoading = isLoading,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon
)

@Composable
fun GymFlowOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) = GymFlowButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    style = GymFlowButtonStyle.OUTLINED,
    enabled = enabled,
    isLoading = isLoading,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon
)

@Composable
fun GymFlowDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) = GymFlowButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    style = GymFlowButtonStyle.DESTRUCTIVE,
    enabled = enabled,
    isLoading = isLoading,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon
)

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    contentColor: Color,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = contentColor,
            strokeWidth = 2.dp
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = GymFlowTheme.typography.Button,
                color = contentColor
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}
