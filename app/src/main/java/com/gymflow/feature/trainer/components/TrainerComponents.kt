package com.gymflow.feature.trainer.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.feature.trainer.theme.TrainerBadgeActiveBg
import com.gymflow.feature.trainer.theme.TrainerBadgeActiveFg
import com.gymflow.feature.trainer.theme.TrainerBadgePendingBg
import com.gymflow.feature.trainer.theme.TrainerBadgePendingFg
import com.gymflow.feature.trainer.theme.TrainerBtnCardBg
import com.gymflow.feature.trainer.theme.TrainerBtnCardFg
import com.gymflow.feature.trainer.theme.TrainerBtnPrimaryBg
import com.gymflow.feature.trainer.theme.TrainerBtnPrimaryFg
import com.gymflow.feature.trainer.theme.TrainerDarkBg
import com.gymflow.feature.trainer.theme.TrainerDarkBorder
import com.gymflow.feature.trainer.theme.TrainerDarkCard
import com.gymflow.feature.trainer.theme.TrainerDarkSurface
import com.gymflow.feature.trainer.theme.TrainerDarkTextPrimary
import com.gymflow.feature.trainer.theme.TrainerDarkTextSecondary
import com.gymflow.feature.trainer.theme.TrainerDarkTextTertiary
import com.gymflow.feature.trainer.theme.TrainerThemeTokens

enum class TrainerButtonStyle {
    PRIMARY,   // Solid White Pill (Black Text)
    SECONDARY, // Dark Card Button (#27272A)
    OUTLINED,  // Crisp 1dp Border (#27272A)
    DESTRUCTIVE
}

@Composable
fun TrainerTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TrainerDarkBg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TrainerDarkTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TrainerDarkTextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TrainerDarkTextSecondary
                    )
                }
            }

            if (actions != null) {
                actions()
            }
        }
    }
}

@Composable
fun TrainerCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = TrainerDarkCard,
    borderColor: Color = TrainerDarkBorder,
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val clickableModifier = if (onClick != null) {
        modifier.clip(shape).clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = clickableModifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun TrainerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TrainerButtonStyle = TrainerButtonStyle.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    height: Dp = 50.dp,
    fullWidth: Boolean = true
) {
    val buttonModifier = if (fullWidth) {
        modifier.fillMaxWidth().height(height)
    } else {
        modifier.height(height)
    }
    val pillShape = RoundedCornerShape(9999.dp)

    when (style) {
        TrainerButtonStyle.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = pillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrainerBtnPrimaryBg,
                    contentColor = TrainerBtnPrimaryFg,
                    disabledContainerColor = TrainerDarkBorder,
                    disabledContentColor = TrainerDarkTextTertiary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                ButtonInnerContent(
                    text = text,
                    isLoading = isLoading,
                    contentColor = TrainerBtnPrimaryFg,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            }
        }
        TrainerButtonStyle.SECONDARY -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = pillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrainerBtnCardBg,
                    contentColor = TrainerBtnCardFg,
                    disabledContainerColor = TrainerDarkBorder,
                    disabledContentColor = TrainerDarkTextTertiary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                ButtonInnerContent(
                    text = text,
                    isLoading = isLoading,
                    contentColor = TrainerBtnCardFg,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            }
        }
        TrainerButtonStyle.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = pillShape,
                border = BorderStroke(1.dp, TrainerDarkBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = TrainerDarkTextPrimary,
                    disabledContentColor = TrainerDarkTextTertiary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                ButtonInnerContent(
                    text = text,
                    isLoading = isLoading,
                    contentColor = TrainerDarkTextPrimary,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            }
        }
        TrainerButtonStyle.DESTRUCTIVE -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled && !isLoading,
                shape = pillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                ButtonInnerContent(
                    text = text,
                    isLoading = isLoading,
                    contentColor = Color.White,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon
                )
            }
        }
    }
}

@Composable
private fun ButtonInnerContent(
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}

@Composable
fun TrainerBadge(
    text: String,
    modifier: Modifier = Modifier,
    status: String = "Active" // Active, Pending, Inactive
) {
    val (fg, bg) = when (status.lowercase()) {
        "active", "completed" -> TrainerBadgeActiveFg to TrainerBadgeActiveBg
        "pending", "review" -> TrainerBadgePendingFg to TrainerBadgePendingBg
        "danger", "destructive" -> Color(0xFFEF4444) to Color(0x26EF4444)
        else -> TrainerDarkTextSecondary to TrainerDarkBorder
    }

    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(9999.dp))
            .border(1.dp, fg.copy(alpha = 0.3f), RoundedCornerShape(9999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun TrainerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TrainerDarkTextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            shape = RoundedCornerShape(12.dp),
            placeholder = if (placeholder != null) {
                {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        color = TrainerDarkTextTertiary
                    )
                }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = TrainerDarkTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            trailingIcon = trailingIcon,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = TrainerDarkCard,
                unfocusedContainerColor = TrainerDarkCard,
                focusedBorderColor = TrainerDarkTextPrimary,
                unfocusedBorderColor = TrainerDarkBorder,
                focusedTextColor = TrainerDarkTextPrimary,
                unfocusedTextColor = TrainerDarkTextPrimary,
                cursorColor = TrainerDarkTextPrimary,
                errorBorderColor = Color(0xFFEF4444)
            )
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = Color(0xFFEF4444)
            )
        }
    }
}

@Composable
fun TrainerKpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accentColor: Color = TrainerDarkTextPrimary
) {
    TrainerCard(
        modifier = modifier,
        backgroundColor = TrainerDarkCard,
        borderColor = TrainerDarkBorder,
        contentPadding = 14.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TrainerDarkTextSecondary
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TrainerDarkTextTertiary
                )
            }
        }
    }
}

enum class TrainerBottomNavTab {
    DASHBOARD,
    MEMBERS,
    PLANS,
    ANALYTICS,
    SETTINGS
}

@Composable
fun TrainerBottomNavBar(
    selectedTab: TrainerBottomNavTab,
    onTabSelected: (TrainerBottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TrainerDarkBg,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = TrainerDarkBorder
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    Triple(TrainerBottomNavTab.DASHBOARD, "Dashboard", Icons.Default.Grid3x3),
                    Triple(TrainerBottomNavTab.MEMBERS, "Members", Icons.Default.People),
                    Triple(TrainerBottomNavTab.PLANS, "Plans", Icons.Default.FitnessCenter),
                    Triple(TrainerBottomNavTab.ANALYTICS, "Analytics", Icons.Default.Analytics),
                    Triple(TrainerBottomNavTab.SETTINGS, "Settings", Icons.Default.Settings)
                )

                for ((tab, label, icon) in tabs) {
                    val isSelected = selectedTab == tab
                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(bounded = false, radius = 28.dp),
                                onClick = { onTabSelected(tab) }
                            )
                            .padding(vertical = 2.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) TrainerDarkTextPrimary else TrainerDarkTextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected) TrainerDarkTextPrimary else TrainerDarkTextSecondary
                        )
                    }
                }
            }
        }
    }
}
