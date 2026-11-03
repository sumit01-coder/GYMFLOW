package com.gymflow.core.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymflow.R
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite

enum class GymFlowLogoSize(
    val iconSize: Dp,
    val logoWidth: Dp,
    val fontSize: Int,
    val spacing: Dp
) {
    SMALL(36.dp, 130.dp, 16, 6.dp),
    MEDIUM(54.dp, 180.dp, 20, 8.dp),
    LARGE(80.dp, 240.dp, 26, 12.dp),
    HERO(110.dp, 290.dp, 32, 16.dp)
}

enum class GymFlowLogoVariant {
    ICON_ONLY,
    HORIZONTAL,
    VERTICAL
}

/**
 * Official GYMFLOW Brand Logo Component
 * Renders the iconic 3D infinity dumbbell emblem and bold typography
 * matching the official brand identity.
 */
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
    val scale by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
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

    when (variant) {
        GymFlowLogoVariant.ICON_ONLY -> {
            Image(
                painter = painterResource(id = R.drawable.gymflow_emblem_transparent),
                contentDescription = "GYMFLOW Emblem",
                contentScale = ContentScale.Fit,
                modifier = modifier
                    .size(size.iconSize)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }

        GymFlowLogoVariant.HORIZONTAL -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gymflow_emblem_transparent),
                    contentDescription = "GYMFLOW Emblem",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(size.iconSize)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
                Spacer(modifier = Modifier.width(size.spacing))
                Column {
                    Text(
                        text = "GYMFLOW",
                        fontSize = size.fontSize.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = if (isDark) GymFlowWhite else GymFlowBlack
                    )
                    if (showTagline) {
                        Text(
                            text = "TRAIN · TRACK · IMPROVE",
                            fontSize = (size.fontSize * 0.42).sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp,
                            color = if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)
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
                Image(
                    painter = painterResource(id = R.drawable.gymflow_logo_transparent),
                    contentDescription = "GYMFLOW Brand Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(size.logoWidth)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
                if (showTagline) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "TRAIN · TRACK · IMPROVE",
                        fontSize = (size.fontSize * 0.38).sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = if (isDark) Color(0xFFA1A1AA) else Color(0xFF71717A)
                    )
                }
            }
        }
    }
}
