package com.gymflow.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class GymFlowThemeMode {
    MEMBER_LIGHT,
    TRAINER_DARK,
    OLED_REST
}

private val LightColorScheme = lightColorScheme(
    primary = GymFlowBlack,
    onPrimary = GymFlowWhite,
    primaryContainer = GymFlowLightGray,
    onPrimaryContainer = GymFlowBlack,
    secondary = GymFlowGray700,
    onSecondary = GymFlowWhite,
    background = GymFlowWhite,
    onBackground = GymFlowBlack,
    surface = GymFlowWhite,
    onSurface = GymFlowBlack,
    surfaceVariant = GymFlowSurfaceCard,
    onSurfaceVariant = GymFlowGray600,
    outline = GymFlowBorder,
    error = GymFlowRed,
    onError = GymFlowWhite
)

private val TrainerDarkColorScheme = darkColorScheme(
    primary = GymFlowWhite,
    onPrimary = GymFlowBlack,
    primaryContainer = TrainerDarkCard,
    onPrimaryContainer = GymFlowWhite,
    secondary = GymFlowGray400,
    onSecondary = GymFlowBlack,
    background = TrainerDarkBg,
    onBackground = GymFlowWhite,
    surface = TrainerDarkSurface,
    onSurface = GymFlowWhite,
    surfaceVariant = TrainerDarkCard,
    onSurfaceVariant = TrainerDarkTextSecondary,
    outline = TrainerDarkBorder,
    error = GymFlowRed,
    onError = GymFlowWhite
)

private val OledRestColorScheme = darkColorScheme(
    primary = GymFlowWhite,
    onPrimary = GymFlowPureBlack,
    primaryContainer = OledRestCard,
    onPrimaryContainer = GymFlowWhite,
    secondary = GymFlowGray400,
    onSecondary = GymFlowPureBlack,
    background = OledRestBg,
    onBackground = GymFlowWhite,
    surface = OledRestSurface,
    onSurface = GymFlowWhite,
    surfaceVariant = OledRestCard,
    onSurfaceVariant = OledRestTextMuted,
    outline = OledRestBorder,
    error = GymFlowRed,
    onError = GymFlowWhite
)

@Composable
fun GymFlowTheme(
    mode: GymFlowThemeMode = GymFlowThemeMode.MEMBER_LIGHT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val (colorScheme, gymFlowColors) = when (mode) {
        GymFlowThemeMode.MEMBER_LIGHT -> LightColorScheme to MemberLightColors
        GymFlowThemeMode.TRAINER_DARK -> TrainerDarkColorScheme to TrainerDarkColors
        GymFlowThemeMode.OLED_REST -> OledRestColorScheme to OledRestColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = gymFlowColors.background.toArgb()
                window.navigationBarColor = gymFlowColors.background.toArgb()
                val isAppearanceLight = !gymFlowColors.isDark
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = isAppearanceLight
                insetsController.isAppearanceLightNavigationBars = isAppearanceLight
            }
        }
    }

    val adaptiveSpec = rememberAdaptiveLayoutSpec()

    CompositionLocalProvider(
        LocalGymFlowColors provides gymFlowColors,
        LocalAdaptiveSpec provides adaptiveSpec
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = GymFlowTypography,
            shapes = GymFlowShapes,
            content = content
        )
    }
}

object GymFlowTheme {
    val colors: GymFlowColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGymFlowColors.current

    val adaptive: AdaptiveLayoutSpec
        @Composable
        @ReadOnlyComposable
        get() = LocalAdaptiveSpec.current

    val typography = GymFlowTypographyTokens
    val shapes = GymFlowShapesTokens
}
