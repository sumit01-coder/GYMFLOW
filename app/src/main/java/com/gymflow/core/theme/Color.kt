package com.gymflow.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core Brand Palette
val GymFlowBlack = Color(0xFF111111)
val GymFlowPureBlack = Color(0xFF000000)
val GymFlowWhite = Color(0xFFFFFFFF)
val GymFlowLightGray = Color(0xFFF5F5F5)
val GymFlowSurfaceSubtle = Color(0xFFF1F3F5)
val GymFlowSurfaceCard = Color(0xFFF8F9FA)
val GymFlowBorder = Color(0xFFE5E7EB)

// Secondary & Accent Grays
val GymFlowGray50 = Color(0xFFF9FAFB)
val GymFlowGray100 = Color(0xFFF3F4F6)
val GymFlowGray200 = Color(0xFFE5E7EB)
val GymFlowGray300 = Color(0xFFD1D5DB)
val GymFlowGray400 = Color(0xFF9CA3AF)
val GymFlowGray500 = Color(0xFF6B7280)
val GymFlowGray600 = Color(0xFF4B5563)
val GymFlowGray700 = Color(0xFF374151)
val GymFlowGray800 = Color(0xFF1F2937)
val GymFlowGray900 = Color(0xFF111827)

// Status & Semantic Accents
val GymFlowGreen = Color(0xFF16A36A) // Active / Completed
val GymFlowGreenLight = Color(0xFF22C55E)
val GymFlowGreenSubtle = Color(0x1F22C55E)
val GymFlowAmber = Color(0xFFE9A23B) // Pending / Warning
val GymFlowAmberSubtle = Color(0x1FF59E0B)
val GymFlowRed = Color(0xFFEF4444) // Error / Destructive
val GymFlowRedSubtle = Color(0x1FEF4444)

// Member Light Aliases
val MemberBg = GymFlowWhite
val MemberSurface = GymFlowWhite
val MemberSurfaceCard = GymFlowSurfaceCard
val MemberSurfaceSubtle = GymFlowSurfaceSubtle
val MemberBorder = GymFlowBorder
val MemberCardBorder = GymFlowBorder
val MemberTextPrimary = GymFlowBlack
val MemberTextSecondary = GymFlowGray500
val MemberTextTertiary = GymFlowGray400
val MemberBtnPrimaryBg = GymFlowBlack
val MemberBtnPrimaryFg = GymFlowWhite

val StatusActive = GymFlowGreenLight
val StatusActiveLight = GymFlowGreenSubtle
val StatusPending = GymFlowAmber
val StatusPendingLight = GymFlowAmberSubtle
val StatusDestructive = GymFlowRed
val StatusDestructiveLight = GymFlowRedSubtle

// Trainer Dark Mode Surfaces
val TrainerDarkBg = Color(0xFF09090B)
val TrainerDarkSurface = Color(0xFF141416)
val TrainerDarkCard = Color(0xFF1E1E22)
val TrainerDarkBorder = Color(0xFF27272A)
val TrainerDarkTextPrimary = Color(0xFFFFFFFF)
val TrainerDarkTextSecondary = Color(0xFFA1A1AA)

// OLED Rest Mode
val OledRestBg = Color(0xFF000000)
val OledRestSurface = Color(0xFF121212)
val OledRestCard = Color(0xFF18181B)
val OledRestBorder = Color(0xFF27272A)
val OledRestTextPrimary = Color(0xFFFFFFFF)
val OledRestTextMuted = Color(0xFF71717A)

@Immutable
data class GymFlowColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val primaryButtonBg: Color,
    val primaryButtonFg: Color,
    val secondaryButtonBg: Color,
    val secondaryButtonFg: Color,
    val accentActive: Color,
    val accentPending: Color,
    val accentDestructive: Color,
    val isDark: Boolean
)

val MemberLightColors = GymFlowColors(
    background = GymFlowWhite,
    surface = GymFlowWhite,
    surfaceVariant = GymFlowSurfaceCard,
    border = GymFlowBorder,
    textPrimary = GymFlowBlack,
    textSecondary = GymFlowGray500,
    textTertiary = GymFlowGray400,
    primaryButtonBg = GymFlowBlack,
    primaryButtonFg = GymFlowWhite,
    secondaryButtonBg = GymFlowGray100,
    secondaryButtonFg = GymFlowGray900,
    accentActive = GymFlowGreen,
    accentPending = GymFlowAmber,
    accentDestructive = GymFlowRed,
    isDark = false
)

val TrainerDarkColors = GymFlowColors(
    background = TrainerDarkBg,
    surface = TrainerDarkSurface,
    surfaceVariant = TrainerDarkCard,
    border = TrainerDarkBorder,
    textPrimary = TrainerDarkTextPrimary,
    textSecondary = TrainerDarkTextSecondary,
    textTertiary = GymFlowGray500,
    primaryButtonBg = GymFlowWhite,
    primaryButtonFg = GymFlowBlack,
    secondaryButtonBg = TrainerDarkCard,
    secondaryButtonFg = GymFlowWhite,
    accentActive = GymFlowGreenLight,
    accentPending = GymFlowAmber,
    accentDestructive = GymFlowRed,
    isDark = true
)

val OledRestColors = GymFlowColors(
    background = OledRestBg,
    surface = OledRestSurface,
    surfaceVariant = OledRestCard,
    border = OledRestBorder,
    textPrimary = OledRestTextPrimary,
    textSecondary = OledRestTextMuted,
    textTertiary = GymFlowGray600,
    primaryButtonBg = GymFlowWhite,
    primaryButtonFg = GymFlowPureBlack,
    secondaryButtonBg = OledRestCard,
    secondaryButtonFg = GymFlowWhite,
    accentActive = GymFlowGreenLight,
    accentPending = GymFlowAmber,
    accentDestructive = GymFlowRed,
    isDark = true
)

val LocalGymFlowColors = staticCompositionLocalOf { MemberLightColors }
