package com.gymflow.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme

@Composable
fun MetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    unit: String? = null,
    iconTint: Color = GymFlowTheme.colors.textPrimary,
    iconBackground: Color = GymFlowTheme.colors.surfaceVariant,
    onClick: (() -> Unit)? = null
) {
    val adaptive = GymFlowTheme.adaptive
    val valueFontSize = when (adaptive.posture) {
        com.gymflow.core.theme.DevicePosture.SMALL_PHONE -> 19.sp
        com.gymflow.core.theme.DevicePosture.NORMAL_PHONE -> 22.sp
        com.gymflow.core.theme.DevicePosture.LARGE_PHONE -> 24.sp
        com.gymflow.core.theme.DevicePosture.FOLDABLE_OR_TABLET -> 26.sp
    }
    val labelFontSize = when (adaptive.posture) {
        com.gymflow.core.theme.DevicePosture.SMALL_PHONE -> 10.sp
        else -> 12.sp
    }
    val iconSize = when (adaptive.posture) {
        com.gymflow.core.theme.DevicePosture.SMALL_PHONE -> 30.dp
        else -> 36.dp
    }
    val internalPadding = when (adaptive.posture) {
        com.gymflow.core.theme.DevicePosture.SMALL_PHONE -> 10.dp
        else -> 14.dp
    }

    GymFlowCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = internalPadding,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .background(iconBackground, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size((iconSize.value * 0.5f).dp),
                        tint = iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (adaptive.posture == com.gymflow.core.theme.DevicePosture.SMALL_PHONE) 8.dp else 12.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = GymFlowTheme.typography.Heading2.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = valueFontSize
                    ),
                    color = GymFlowTheme.colors.textPrimary
                )
                if (unit != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = GymFlowTheme.typography.Caption.copy(
                            fontSize = if (adaptive.posture == com.gymflow.core.theme.DevicePosture.SMALL_PHONE) 10.sp else 12.sp
                        ),
                        color = GymFlowTheme.colors.textSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = GymFlowTheme.typography.Caption.copy(
                    fontSize = labelFontSize
                ),
                color = GymFlowTheme.colors.textSecondary,
                maxLines = 1
            )
        }
    }
}
