package com.gymflow.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Native athletic modern grotesk font family
val GymFlowFontFamily = FontFamily.Default

// Extended Custom GymFlow Text Styles
object GymFlowTypographyTokens {
    val DisplayHero = TextStyle(
        fontFamily = GymFlowFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    )

    val Heading1 = TextStyle(
        fontFamily = GymFlowFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.2).sp
    )

    val Heading2 = TextStyle(
        fontFamily = GymFlowFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.1).sp
    )

    val Subtitle = TextStyle(
        fontFamily = GymFlowFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )

    val BodyRegular = TextStyle(
        fontFamily = GymFlowFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val BodyMedium = TextStyle(
        fontFamily = GymFlowFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )

    val Caption = TextStyle(
        fontFamily = GymFlowFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.2.sp
    )

    val BodySmall = Caption

    val Timer = TextStyle(
        fontFamily = GymFlowFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        lineHeight = 54.sp,
        letterSpacing = (-1.0).sp
    )

    val TimerSmall = TextStyle(
        fontFamily = GymFlowFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    )

    val Button = TextStyle(
        fontFamily = GymFlowFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
}

// Material 3 Typography Mapping
val GymFlowTypography = Typography(
    displayLarge = GymFlowTypographyTokens.DisplayHero,
    headlineLarge = GymFlowTypographyTokens.Heading1,
    headlineMedium = GymFlowTypographyTokens.Heading2,
    titleLarge = GymFlowTypographyTokens.Heading2,
    titleMedium = GymFlowTypographyTokens.Subtitle,
    bodyLarge = GymFlowTypographyTokens.BodyMedium,
    bodyMedium = GymFlowTypographyTokens.BodyRegular,
    bodySmall = GymFlowTypographyTokens.Caption,
    labelLarge = GymFlowTypographyTokens.Button,
    labelMedium = GymFlowTypographyTokens.Caption,
    labelSmall = GymFlowTypographyTokens.Caption.copy(fontSize = 11.sp)
)
