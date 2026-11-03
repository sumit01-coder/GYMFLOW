package com.gymflow.feature.member.exercises

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowBorder
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite

object ExerciseVisuals {

    fun getEquipmentIcon(equipment: String): ImageVector {
        return when (equipment.lowercase()) {
            "machine" -> Icons.Default.Tune
            "cable" -> Icons.Default.Build
            "barbell" -> Icons.Default.FitnessCenter
            "dumbbell" -> Icons.Default.FitnessCenter
            "bodyweight" -> Icons.Default.AccessibilityNew
            else -> Icons.Default.Speed
        }
    }

    fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "chest" -> Color(0xFF1E293B)
            "back" -> Color(0xFF0F172A)
            "shoulders" -> Color(0xFF334155)
            "biceps" -> Color(0xFF1E1E24)
            "triceps" -> Color(0xFF27272A)
            "legs" -> Color(0xFF18181B)
            "abs" -> Color(0xFF09090B)
            else -> GymFlowBlack
        }
    }
}

@Composable
fun ExerciseEquipmentBadge(
    equipment: String,
    machineName: String? = null,
    modifier: Modifier = Modifier
) {
    val displayLabel = machineName ?: equipment
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GymFlowTheme.colors.surfaceVariant)
            .border(0.5.dp, GymFlowBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ExerciseVisuals.getEquipmentIcon(equipment),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = GymFlowTheme.colors.textSecondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = displayLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = GymFlowTheme.colors.textSecondary
        )
    }
}

@Composable
fun MuscleCategoryChip(
    category: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GymFlowBlack)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = GymFlowWhite,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ExerciseThumbnail(
    category: String,
    equipment: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(GymFlowTheme.colors.surfaceVariant)
            .border(1.dp, GymFlowBorder, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ExerciseVisuals.getEquipmentIcon(equipment),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = GymFlowTheme.colors.textPrimary
        )
    }
}

