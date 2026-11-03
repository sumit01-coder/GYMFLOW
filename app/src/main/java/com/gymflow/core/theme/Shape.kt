package com.gymflow.core.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object GymFlowShapesTokens {
    val CardMain = RoundedCornerShape(16.dp)
    val CardSmall = RoundedCornerShape(12.dp)
    val CardLarge = RoundedCornerShape(20.dp)
    val ButtonPill = RoundedCornerShape(9999.dp)
    val InputField = RoundedCornerShape(12.dp)
    val BadgePill = RoundedCornerShape(9999.dp)
    val Circle = CircleShape
}

val GymFlowShapes = Shapes(
    small = GymFlowShapesTokens.CardSmall,
    medium = GymFlowShapesTokens.CardMain,
    large = GymFlowShapesTokens.CardLarge,
    extraLarge = RoundedCornerShape(24.dp)
)
