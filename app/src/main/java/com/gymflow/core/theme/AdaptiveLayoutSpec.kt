package com.gymflow.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen width categories according to standard Android adaptive breakpoints:
 * - COMPACT: < 600dp (most standard mobile phones in portrait)
 * - MEDIUM: 600dp - 839dp (foldables unfolded, large portrait tablets, mini tablets)
 * - EXPANDED: >= 840dp (tablets in landscape, desktop-like displays)
 */
enum class WindowWidthSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

/**
 * Sub-categorization for fine-grained phone layout tuning:
 * - SMALL_PHONE: < 360dp width (e.g. Galaxy Z Flip cover screen, small older phones)
 * - NORMAL_PHONE: 360dp - 411dp width (standard modern smartphones)
 * - LARGE_PHONE: 412dp - 599dp width (larger Pro/Plus/Ultra smartphones)
 * - FOLDABLE_OR_TABLET: >= 600dp width
 */
enum class DevicePosture {
    SMALL_PHONE,
    NORMAL_PHONE,
    LARGE_PHONE,
    FOLDABLE_OR_TABLET
}

/**
 * Holds adaptive dimensions and typography scaling calculated dynamically
 * based on the device's actual screen configuration.
 */
data class AdaptiveLayoutSpec(
    val screenWidthDp: Dp,
    val screenHeightDp: Dp,
    val widthClass: WindowWidthSizeClass,
    val posture: DevicePosture,
    val isLandscape: Boolean,
    
    // Spacings & Margins
    val horizontalScreenPadding: Dp,
    val verticalScreenPadding: Dp,
    val gridSpacing: Dp,
    val cardContentPadding: Dp,
    
    // Metrics & Grid columns
    val statCardGridColumns: Int,
    val maxContentWidth: Dp,
    
    // Header & Hero scale factors
    val heroHeadingSize: TextUnit,
    val subHeadingSize: TextUnit,
    val bodyScale: Float,
    
    // Steps & Cardio card dimensions
    val stepsCardSparklineHeight: Dp,
    val stepsTilePadding: Dp
)

val LocalAdaptiveSpec = staticCompositionLocalOf {
    AdaptiveLayoutSpec(
        screenWidthDp = 390.dp,
        screenHeightDp = 844.dp,
        widthClass = WindowWidthSizeClass.COMPACT,
        posture = DevicePosture.NORMAL_PHONE,
        isLandscape = false,
        horizontalScreenPadding = 20.dp,
        verticalScreenPadding = 12.dp,
        gridSpacing = 12.dp,
        cardContentPadding = 16.dp,
        statCardGridColumns = 2,
        maxContentWidth = Dp.Unspecified,
        heroHeadingSize = 34.sp,
        subHeadingSize = 20.sp,
        bodyScale = 1.0f,
        stepsCardSparklineHeight = 38.dp,
        stepsTilePadding = 8.dp
    )
}

@Composable
fun rememberAdaptiveLayoutSpec(): AdaptiveLayoutSpec {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    val widthClass = when {
        configuration.screenWidthDp < 600 -> WindowWidthSizeClass.COMPACT
        configuration.screenWidthDp < 840 -> WindowWidthSizeClass.MEDIUM
        else -> WindowWidthSizeClass.EXPANDED
    }

    val posture = when {
        configuration.screenWidthDp < 360 -> DevicePosture.SMALL_PHONE
        configuration.screenWidthDp < 412 -> DevicePosture.NORMAL_PHONE
        configuration.screenWidthDp < 600 -> DevicePosture.LARGE_PHONE
        else -> DevicePosture.FOLDABLE_OR_TABLET
    }

    val horizontalPadding = when (posture) {
        DevicePosture.SMALL_PHONE -> 12.dp
        DevicePosture.NORMAL_PHONE -> 18.dp
        DevicePosture.LARGE_PHONE -> 22.dp
        DevicePosture.FOLDABLE_OR_TABLET -> 32.dp
    }

    val gridSpacing = when (posture) {
        DevicePosture.SMALL_PHONE -> 8.dp
        DevicePosture.NORMAL_PHONE -> 12.dp
        DevicePosture.LARGE_PHONE -> 14.dp
        DevicePosture.FOLDABLE_OR_TABLET -> 16.dp
    }

    val cardPadding = when (posture) {
        DevicePosture.SMALL_PHONE -> 12.dp
        DevicePosture.NORMAL_PHONE -> 16.dp
        DevicePosture.LARGE_PHONE -> 18.dp
        DevicePosture.FOLDABLE_OR_TABLET -> 20.dp
    }

    val columns = when (widthClass) {
        WindowWidthSizeClass.COMPACT -> 2
        WindowWidthSizeClass.MEDIUM -> 3
        WindowWidthSizeClass.EXPANDED -> 4
    }

    val heroSize = when (posture) {
        DevicePosture.SMALL_PHONE -> 28.sp
        DevicePosture.NORMAL_PHONE -> 32.sp
        DevicePosture.LARGE_PHONE -> 36.sp
        DevicePosture.FOLDABLE_OR_TABLET -> 42.sp
    }

    val subHeading = when (posture) {
        DevicePosture.SMALL_PHONE -> 17.sp
        DevicePosture.NORMAL_PHONE -> 19.sp
        DevicePosture.LARGE_PHONE -> 21.sp
        DevicePosture.FOLDABLE_OR_TABLET -> 24.sp
    }

    val maxContent = if (widthClass != WindowWidthSizeClass.COMPACT) 840.dp else Dp.Unspecified

    return AdaptiveLayoutSpec(
        screenWidthDp = screenWidth,
        screenHeightDp = screenHeight,
        widthClass = widthClass,
        posture = posture,
        isLandscape = isLandscape,
        horizontalScreenPadding = horizontalPadding,
        verticalScreenPadding = if (posture == DevicePosture.SMALL_PHONE) 8.dp else 12.dp,
        gridSpacing = gridSpacing,
        cardContentPadding = cardPadding,
        statCardGridColumns = columns,
        maxContentWidth = maxContent,
        heroHeadingSize = heroSize,
        subHeadingSize = subHeading,
        bodyScale = if (posture == DevicePosture.SMALL_PHONE) 0.92f else 1.0f,
        stepsCardSparklineHeight = if (posture == DevicePosture.SMALL_PHONE) 30.dp else 38.dp,
        stepsTilePadding = if (posture == DevicePosture.SMALL_PHONE) 6.dp else 8.dp
    )
}

