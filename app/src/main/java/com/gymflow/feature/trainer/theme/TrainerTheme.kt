package com.gymflow.feature.trainer.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.gymflow.core.theme.GymFlowShapes
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTypography
import com.gymflow.core.theme.GymFlowTypographyTokens

// Trainer Studio OLED / Dark Theme Tokens
val TrainerDarkBg = Color(0xFF09090B)
val TrainerDarkSurface = Color(0xFF141416)
val TrainerDarkCard = Color(0xFF1E1E22)
val TrainerDarkBorder = Color(0xFF27272A)
val TrainerDarkTextPrimary = Color(0xFFFFFFFF)
val TrainerDarkTextSecondary = Color(0xFFA1A1AA)
val TrainerDarkTextTertiary = Color(0xFF71717A)

// Buttons
val TrainerBtnPrimaryBg = Color(0xFFFFFFFF)
val TrainerBtnPrimaryFg = Color(0xFF000000)
val TrainerBtnCardBg = Color(0xFF27272A)
val TrainerBtnCardFg = Color(0xFFFFFFFF)

// Badges & Status
val TrainerBadgeActiveFg = Color(0xFF4ADE80)
val TrainerBadgeActiveBg = Color(0x264ADE80) // 15% opacity
val TrainerBadgePendingFg = Color(0xFFFBBF24)
val TrainerBadgePendingBg = Color(0x26FBBF24) // 15% opacity
val TrainerBadgeDestructiveFg = Color(0xFFEF4444)
val TrainerBadgeDestructiveBg = Color(0x26EF4444) // 15% opacity

data class TrainerColors(
    val background: Color = TrainerDarkBg,
    val surface: Color = TrainerDarkSurface,
    val cardSurface: Color = TrainerDarkCard,
    val border: Color = TrainerDarkBorder,
    val textPrimary: Color = TrainerDarkTextPrimary,
    val textSecondary: Color = TrainerDarkTextSecondary,
    val textTertiary: Color = TrainerDarkTextTertiary,
    val buttonPrimaryBg: Color = TrainerBtnPrimaryBg,
    val buttonPrimaryFg: Color = TrainerBtnPrimaryFg,
    val buttonCardBg: Color = TrainerBtnCardBg,
    val buttonCardFg: Color = TrainerBtnCardFg,
    val badgeActiveFg: Color = TrainerBadgeActiveFg,
    val badgeActiveBg: Color = TrainerBadgeActiveBg,
    val badgePendingFg: Color = TrainerBadgePendingFg,
    val badgePendingBg: Color = TrainerBadgePendingBg,
    val badgeDestructiveFg: Color = TrainerBadgeDestructiveFg,
    val badgeDestructiveBg: Color = TrainerBadgeDestructiveBg
)

val LocalTrainerColors = staticCompositionLocalOf { TrainerColors() }

private val TrainerDarkColorScheme = darkColorScheme(
    primary = TrainerBtnPrimaryBg,
    onPrimary = TrainerBtnPrimaryFg,
    primaryContainer = TrainerDarkCard,
    onPrimaryContainer = TrainerDarkTextPrimary,
    secondary = TrainerDarkTextSecondary,
    onSecondary = TrainerDarkTextPrimary,
    background = TrainerDarkBg,
    onBackground = TrainerDarkTextPrimary,
    surface = TrainerDarkSurface,
    onSurface = TrainerDarkTextPrimary,
    surfaceVariant = TrainerDarkCard,
    onSurfaceVariant = TrainerDarkTextSecondary,
    outline = TrainerDarkBorder,
    error = TrainerBadgeDestructiveFg,
    onError = Color.White
)

@Composable
fun TrainerTheme(
    colors: TrainerColors = TrainerColors(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colors.background.toArgb()
                window.navigationBarColor = colors.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(
        LocalTrainerColors provides colors
    ) {
        MaterialTheme(
            colorScheme = TrainerDarkColorScheme,
            typography = GymFlowTypography,
            shapes = GymFlowShapes,
            content = content
        )
    }
}

object TrainerThemeTokens {
    val colors: TrainerColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTrainerColors.current

    val typography = GymFlowTypographyTokens
    val shapes = GymFlowShapesTokens
}
