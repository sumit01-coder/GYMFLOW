package com.gymflow.feature.member.fatloss

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowGreenLight
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite

@Composable
fun InteractiveBodyMap(
    selectedView: BodyView,
    selectedZoneId: String?,
    onViewChange: (BodyView) -> Unit,
    onZoneSelect: (TargetFatLossZone) -> Unit,
    modifier: Modifier = Modifier
) {
    val zones = TargetFatLossCatalog.getZonesByView(selectedView)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GymFlowTheme.colors.surface)
            .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // View Toggle Header (Front / Back)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ANATOMICAL FOCUS MAP",
                    style = GymFlowTheme.typography.Caption.copy(
                        color = GymFlowGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Select Body Target Area",
                    style = GymFlowTheme.typography.Heading2.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Front / Back Pill Selector
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(GymFlowTheme.colors.surfaceVariant)
                    .padding(4.dp)
            ) {
                BodyViewPill(
                    title = "Front View",
                    isSelected = selectedView == BodyView.FRONT,
                    onClick = { onViewChange(BodyView.FRONT) }
                )
                BodyViewPill(
                    title = "Back View",
                    isSelected = selectedView == BodyView.BACK,
                    onClick = { onViewChange(BodyView.BACK) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Silhouette & Zones Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GymFlowTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                            GymFlowTheme.colors.surface
                        )
                    )
                )
                .border(1.dp, GymFlowTheme.colors.border.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                zones.forEach { zone ->
                    val isSelected = zone.id == selectedZoneId
                    TargetZoneCardItem(
                        zone = zone,
                        isSelected = isSelected,
                        onClick = { onZoneSelect(zone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BodyViewPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) GymFlowBlack else Color.Transparent,
        animationSpec = tween(200)
    )
    val textFg by animateColorAsState(
        targetValue = if (isSelected) GymFlowWhite else GymFlowTheme.colors.textSecondary,
        animationSpec = tween(200)
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = GymFlowTheme.typography.Caption.copy(
                color = textFg,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
private fun TargetZoneCardItem(
    zone: TargetFatLossZone,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) GymFlowGreen else GymFlowTheme.colors.border,
        animationSpec = tween(200)
    )
    val cardBg by animateColorAsState(
        targetValue = if (isSelected) GymFlowGreen.copy(alpha = 0.08f) else GymFlowTheme.colors.surface,
        animationSpec = tween(200)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) GymFlowGreen.copy(alpha = 0.2f) else GymFlowTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (isSelected) GymFlowGreen else GymFlowTheme.colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = zone.name,
                    style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = zone.subtitle,
                    style = GymFlowTheme.typography.Caption.copy(color = GymFlowTheme.colors.textSecondary),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Target Deficit Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) GymFlowGreen else GymFlowTheme.colors.surfaceVariant)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "-${zone.recommendedCaloricDeficitKcal} kcal",
                style = GymFlowTheme.typography.Caption.copy(
                    color = if (isSelected) GymFlowWhite else GymFlowTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}
