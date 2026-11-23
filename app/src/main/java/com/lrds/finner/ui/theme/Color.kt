package com.lrds.finner.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =============================================================================
// Finner Visual Identity: Revolut-Inspired OLED Dark & Crisp Neutral Light
// Extracted from Stitch "Dual-Currency Budget App Redesign" (Hero First +
// January Details + Goals + History + Profile + Quick Add Modal).
// =============================================================================

// --- Core Stitch Dark Palette (OLED Near-Black + Elevated Charcoal Slate) ---
val StitchMintDark = Color(0xFF00D09C)              // Primary positive / active mint accent
val StitchOnMintDark = Color(0xFF00291E)
val StitchMintContainerDark = Color(0xFF0D2E25)
val StitchOnMintContainerDark = Color(0xFF6FF7CD)

val StitchSlateSecondaryDark = Color(0xFFB4BAC6)
val StitchOnSlateSecondaryDark = Color(0xFF1A1D24)
val StitchSlateSecondaryContainerDark = Color(0xFF1F2430)
val StitchOnSlateSecondaryContainerDark = Color(0xFFE6EAF2)

val StitchAmberTertiaryDark = Color(0xFFF59E0B)     // Brazil / warm accent
val StitchOnAmberTertiaryDark = Color(0xFF311B00)
val StitchAmberTertiaryContainerDark = Color(0xFF2B2212)
val StitchOnAmberTertiaryContainerDark = Color(0xFFFDE68A)

val StitchCoralErrorDark = Color(0xFFF43F5E)        // Negative / deficit / alert coral
val StitchOnCoralErrorDark = Color(0xFFFFFFFF)
val StitchCoralErrorContainerDark = Color(0xFF2D141B)
val StitchOnCoralErrorContainerDark = Color(0xFFFDA4AF)

// OLED Tonal Surface Hierarchy
val ObsidianBackgroundDark = Color(0xFF090A0D)              // Stitch appDark / brand.bg
val ObsidianOnBackgroundDark = Color(0xFFF9FAFB)            // Crisp near-white
val ObsidianSurfaceDark = Color(0xFF090A0D)
val ObsidianOnSurfaceDark = Color(0xFFF9FAFB)
val ObsidianSurfaceVariantDark = Color(0xFF1C1F26)          // Stitch cardSubtle
val ObsidianOnSurfaceVariantDark = Color(0xFF8E929B)        // Stitch brand.slate

val ObsidianSurfaceDimDark = Color(0xFF06070A)
val ObsidianSurfaceBrightDark = Color(0xFF262A34)
val ObsidianSurfaceContainerLowestDark = Color(0xFF050608)
val ObsidianSurfaceContainerLowDark = Color(0xFF111318)     // Modal sheets & recessed panels
val ObsidianSurfaceContainerDark = Color(0xFF16181D)        // Stitch cardDark / brand.card
val ObsidianSurfaceContainerHighDark = Color(0xFF1C1F26)    // Nested rows / subtle elevated pills
val ObsidianSurfaceContainerHighestDark = Color(0xFF22252D) // Stitch darkPill

val ObsidianOutlineDark = Color(0xFF3A3F4D)
val ObsidianOutlineVariantDark = Color(0xFF232834)          // Stitch cardBorder hairline
val ObsidianInverseSurfaceDark = Color(0xFFFFFFFF)          // Crisp white active pill in dark mode
val ObsidianInverseOnSurfaceDark = Color(0xFF090A0D)
val ObsidianInversePrimaryDark = Color(0xFF008F6B)
val ObsidianScrimDark = Color(0xFF000000)

// --- Crisp Light Palette (Harmonized Neutral + Mint/Coral) ---
val StitchMintLight = Color(0xFF008F6B)
val StitchOnMintLight = Color(0xFFFFFFFF)
val StitchMintContainerLight = Color(0xFFD3F6EA)
val StitchOnMintContainerLight = Color(0xFF002B1F)

val StitchSlateSecondaryLight = Color(0xFF475060)
val StitchOnSlateSecondaryLight = Color(0xFFFFFFFF)
val StitchSlateSecondaryContainerLight = Color(0xFFE6EAF0)
val StitchOnSlateSecondaryContainerLight = Color(0xFF141820)

val StitchAmberTertiaryLight = Color(0xFFB45309)
val StitchOnAmberTertiaryLight = Color(0xFFFFFFFF)
val StitchAmberTertiaryContainerLight = Color(0xFFFEF3C7)
val StitchOnAmberTertiaryContainerLight = Color(0xFF451A03)

val StitchCoralErrorLight = Color(0xFFE11D48)
val StitchOnCoralErrorLight = Color(0xFFFFFFFF)
val StitchCoralErrorContainerLight = Color(0xFFFFE4E6)
val StitchOnCoralErrorContainerLight = Color(0xFF4C0519)

val PaperBackgroundLight = Color(0xFFF4F6F9)
val PaperOnBackgroundLight = Color(0xFF0B0D12)
val PaperSurfaceLight = Color(0xFFF4F6F9)
val PaperOnSurfaceLight = Color(0xFF0B0D12)
val PaperSurfaceVariantLight = Color(0xFFE8ECF2)
val PaperOnSurfaceVariantLight = Color(0xFF5D6472)

val PaperSurfaceDimLight = Color(0xFFE2E6EE)
val PaperSurfaceBrightLight = Color(0xFFFFFFFF)
val PaperSurfaceContainerLowestLight = Color(0xFFFFFFFF)
val PaperSurfaceContainerLowLight = Color(0xFFF9FAFC)
val PaperSurfaceContainerLight = Color(0xFFFFFFFF)
val PaperSurfaceContainerHighLight = Color(0xFFEDF0F5)
val PaperSurfaceContainerHighestLight = Color(0xFFE3E7EF)

val PaperOutlineLight = Color(0xFF9AA1B0)
val PaperOutlineVariantLight = Color(0xFFE2E6EE)
val PaperInverseSurfaceLight = Color(0xFF0B0D12)
val PaperInverseOnSurfaceLight = Color(0xFFFFFFFF)
val PaperInversePrimaryDark = Color(0xFF00D09C)
val PaperScrimLight = Color(0xFF000000)

// --- Extended Domain Tokens (Finance Semantics beyond Material 3 roles) ---
@Immutable
data class FinnerExtendedColors(
    val inflow: Color,
    val onInflow: Color,
    val inflowContainer: Color,
    val outflow: Color,
    val onOutflow: Color,
    val outflowContainer: Color,
    val brazilAccent: Color,
    val onBrazilAccent: Color,
    val brazilContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val cardBorder: Color,
    val navDockBackground: Color,
)

val LightFinnerExtendedColors = FinnerExtendedColors(
    inflow = Color(0xFF008F6B),
    onInflow = Color(0xFFFFFFFF),
    inflowContainer = Color(0xFFD6F5EA),
    outflow = Color(0xFFE11D48),
    onOutflow = Color(0xFFFFFFFF),
    outflowContainer = Color(0xFFFFE4E6),
    brazilAccent = Color(0xFFD97706),
    onBrazilAccent = Color(0xFFFFFFFF),
    brazilContainer = Color(0xFFFEF3C7),
    warning = Color(0xFFB45309),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFFEF3C7),
    heroGradientStart = Color(0xFFDBE6F8),
    heroGradientEnd = Color(0xFFF4F6F9),
    cardBorder = Color(0xFFE2E6EE),
    navDockBackground = Color(0xF5FFFFFF),
)

val DarkFinnerExtendedColors = FinnerExtendedColors(
    inflow = Color(0xFF00D09C),
    onInflow = Color(0xFF00291E),
    inflowContainer = Color(0xFF0D2E25),
    outflow = Color(0xFFF43F5E),
    onOutflow = Color(0xFFFFFFFF),
    outflowContainer = Color(0xFF2D141B),
    brazilAccent = Color(0xFFF59E0B),
    onBrazilAccent = Color(0xFF2A1800),
    brazilContainer = Color(0xFF2B2212),
    warning = Color(0xFFF5A524),
    onWarning = Color(0xFF2B1B00),
    warningContainer = Color(0xFF332610),
    heroGradientStart = Color(0xFF1B2B4B), // Revolut ambient slate-blue aura
    heroGradientEnd = Color(0xFF090A0D),
    cardBorder = Color(0xFF232834),
    navDockBackground = Color(0xEB14171F),
)

val LocalFinnerExtendedColors = staticCompositionLocalOf { DarkFinnerExtendedColors }
