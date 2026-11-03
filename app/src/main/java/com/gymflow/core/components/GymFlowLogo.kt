package com.gymflow.core.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite

enum class GymFlowLogoSize(val iconSize: Dp, val fontSize: Int, val spacing: Dp) {
    SMALL(32.dp, 16, 8.dp),
    MEDIUM(48.dp, 22, 10.dp),
    LARGE(72.dp, 30, 14.dp),
    HERO(96.dp, 36, 18.dp)
}

enum class GymFlowLogoVariant {
    ICON_ONLY,
    HORIZONTAL,
    VERTICAL
}

@Composable
fun GymFlowLogo(
    modifier: Modifier = Modifier,
    size: GymFlowLogoSize = GymFlowLogoSize.MEDIUM,
    variant: GymFlowLogoVariant = GymFlowLogoVariant.HORIZONTAL,
    isDark: Boolean = GymFlowTheme.colors.isDark,
    showTagline: Boolean = false,
    animated: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val pulse by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        rememberInfiniteTransition(label = "static").animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "static"
        )
    }

    val primaryColor = if (isDark) GymFlowWhite else GymFlowBlack
    val accentColor = Color(0xFF10B981) // Athletic Emerald
    val plateColor = if (isDark) Color(0xFF71717A) else Color(0xFF9CA3AF)

    when (variant) {
        GymFlowLogoVariant.ICON_ONLY -> {
            GymFlowLogoEmblem(
                size = size.iconSize * pulse,
                primaryColor = primaryColor,
                accentColor = accentColor,
                plateColor = plateColor,
                modifier = modifier
            )
        }
        GymFlowLogoVariant.HORIZONTAL -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                GymFlowLogoEmblem(
                    size = size.iconSize * pulse,
                    primaryColor = primaryColor,
                    accentColor = accentColor,
                    plateColor = plateColor
                )
                Spacer(modifier = Modifier.width(size.spacing))
                Column {
                    Text(
                        text = "GYMFLOW",
                        fontSize = size.fontSize.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = primaryColor
                    )
                    if (showTagline) {
                        Text(
                            text = "TRAIN · TRACK · IMPROVE",
                            fontSize = (size.fontSize * 0.38f).sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp,
                            color = plateColor
                        )
                    }
                }
            }
        }
        GymFlowLogoVariant.VERTICAL -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GymFlowLogoEmblem(
                    size = size.iconSize * pulse,
                    primaryColor = primaryColor,
                    accentColor = accentColor,
                    plateColor = plateColor
                )
                Spacer(modifier = Modifier.height(size.spacing))
                Text(
                    text = "GYMFLOW",
                    fontSize = size.fontSize.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.5.sp,
                    color = primaryColor
                )
                if (showTagline) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TRAIN · TRACK · IMPROVE",
                        fontSize = (size.fontSize * 0.38f).sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        color = plateColor
                    )
                }
            }
        }
    }
}

@Composable
fun GymFlowLogoEmblem(
    size: Dp,
    primaryColor: Color,
    accentColor: Color,
    plateColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (primaryColor == GymFlowWhite) Color(0xFF18181B) else Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.72f)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Center Barbell Bar
            val barHeight = h * 0.08f
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.22f, (h - barHeight) / 2f),
                size = Size(w * 0.56f, barHeight),
                cornerRadius = CornerRadius(barHeight / 2f, barHeight / 2f)
            )

            // 2. Left Outer Plate
            val plateWidth = w * 0.09f
            val outerPlateHeight = h * 0.52f
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.16f, (h - outerPlateHeight) / 2f),
                size = Size(plateWidth, outerPlateHeight),
                cornerRadius = CornerRadius(plateWidth / 3f, plateWidth / 3f)
            )

            // 3. Left Inner Plate
            val innerPlateHeight = h * 0.38f
            drawRoundRect(
                color = plateColor,
                topLeft = Offset(w * 0.28f, (h - innerPlateHeight) / 2f),
                size = Size(plateWidth * 0.75f, innerPlateHeight),
                cornerRadius = CornerRadius(plateWidth / 4f, plateWidth / 4f)
            )

            // 4. Right Inner Plate
            drawRoundRect(
                color = plateColor,
                topLeft = Offset(w * 0.65f, (h - innerPlateHeight) / 2f),
                size = Size(plateWidth * 0.75f, innerPlateHeight),
                cornerRadius = CornerRadius(plateWidth / 4f, plateWidth / 4f)
            )

            // 5. Right Outer Plate
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.75f, (h - outerPlateHeight) / 2f),
                size = Size(plateWidth, outerPlateHeight),
                cornerRadius = CornerRadius(plateWidth / 3f, plateWidth / 3f)
            )

            // 6. Kinetic 'G' Flow Arc (Top Arc)
            val strokeW = w * 0.085f
            val topArcPath = Path().apply {
                moveTo(w * 0.72f, h * 0.26f)
                cubicTo(
                    w * 0.58f, h * 0.12f,
                    w * 0.32f, h * 0.14f,
                    w * 0.22f, h * 0.32f
                )
            }
            drawPath(
                path = topArcPath,
                color = Color.White,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // 7. Kinetic Emerald Flow Arc (Bottom Arc)
            val bottomArcPath = Path().apply {
                moveTo(w * 0.28f, h * 0.74f)
                cubicTo(
                    w * 0.42f, h * 0.88f,
                    w * 0.68f, h * 0.86f,
                    w * 0.78f, h * 0.68f
                )
            }
            drawPath(
                path = bottomArcPath,
                color = accentColor,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )

            // 8. Kinetic Central Flow Spark
            drawCircle(
                color = accentColor,
                radius = w * 0.055f,
                center = Offset(w / 2f, h / 2f)
            )
        }
    }
}
