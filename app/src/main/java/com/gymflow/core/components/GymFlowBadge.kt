package com.gymflow.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.theme.GymFlowAmber
import com.gymflow.core.theme.GymFlowAmberSubtle
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowGreenSubtle
import com.gymflow.core.theme.GymFlowRed
import com.gymflow.core.theme.GymFlowRedSubtle
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme

enum class BadgeStatus {
    ACTIVE,
    PENDING,
    NEUTRAL,
    DESTRUCTIVE
}

@Composable
fun GymFlowBadge(
    text: String,
    modifier: Modifier = Modifier,
    status: BadgeStatus = BadgeStatus.NEUTRAL,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    borderColor: Color? = null
) {
    val (defaultBg, defaultFg) = when (status) {
        BadgeStatus.ACTIVE -> GymFlowGreenSubtle to GymFlowGreen
        BadgeStatus.PENDING -> GymFlowAmberSubtle to GymFlowAmber
        BadgeStatus.DESTRUCTIVE -> GymFlowRedSubtle to GymFlowRed
        BadgeStatus.NEUTRAL -> GymFlowTheme.colors.surfaceVariant to GymFlowTheme.colors.textSecondary
    }

    val finalBg = backgroundColor ?: defaultBg
    val finalFg = textColor ?: defaultFg
    val shape = GymFlowShapesTokens.BadgePill

    val borderModifier = if (borderColor != null) {
        Modifier.border(1.dp, borderColor, shape)
    } else Modifier

    Box(
        modifier = modifier
            .then(borderModifier)
            .background(finalBg, shape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = GymFlowTheme.typography.Caption.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = finalFg
        )
    }
}

@Composable
fun ActiveWorkoutBadge(modifier: Modifier = Modifier) {
    GymFlowBadge(
        text = "WORKOUT ACTIVE",
        modifier = modifier,
        status = BadgeStatus.ACTIVE
    )
}
