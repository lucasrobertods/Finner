package com.lrds.finner.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =============================================================================
// Finner Visual Identity: "Private Banking Editorial Ledger"
// Warm alabaster paper (Light) & obsidian slate (Dark) with restrained Deep Pine
// primary accent and warm Terracotta secondary highlight for Brazil commitments.
// =============================================================================

// --- Light Palette: Warm Alabaster Paper ---
val PinePrimaryLight = Color(0xFF1B5E4B)          // Restrained deep forest green — primary actions only
val OnPinePrimaryLight = Color(0xFFFFFFFF)
val PinePrimaryContainerLight = Color(0xFFD2ECE2) // Soft sage-tint container
val OnPinePrimaryContainerLight = Color(0xFF06291F)

val OliveSecondaryLight = Color(0xFF4A5D54)       // Quiet structural supporting tone
val OnOliveSecondaryLight = Color(0xFFFFFFFF)
val OliveSecondaryContainerLight = Color(0xFFDCE6E0)
val OnOliveSecondaryContainerLight = Color(0xFF15241D)

val TerracottaTertiaryLight = Color(0xFF9C522B)   // Warm ochre-terracotta for Brazil / highlights
val OnTerracottaTertiaryLight = Color(0xFFFFFFFF)
val TerracottaTertiaryContainerLight = Color(0xFFF6DFD2)
val OnTerracottaTertiaryContainerLight = Color(0xFF391807)

val CrimsonErrorLight = Color(0xFFB3322B)
val OnCrimsonErrorLight = Color(0xFFFFFFFF)
val CrimsonErrorContainerLight = Color(0xFFF9DEDC)
val OnCrimsonErrorContainerLight = Color(0xFF410E0B)

// Warm Paper Tonal Surface Hierarchy (distinct luminance steps from Lowest -> Highest)
val PaperBackgroundLight = Color(0xFFF7F5F0)            // Warm alabaster canvas
val PaperOnBackgroundLight = Color(0xFF171A18)          // Deep warm charcoal ink
val PaperSurfaceLight = Color(0xFFF7F5F0)
val PaperOnSurfaceLight = Color(0xFF171A18)
val PaperSurfaceVariantLight = Color(0xFFE7E2D8)
val PaperOnSurfaceVariantLight = Color(0xFF535A55)      // Muted stone metadata ink

val PaperSurfaceDimLight = Color(0xFFDED9CE)
val PaperSurfaceBrightLight = Color(0xFFFCFBF8)
val PaperSurfaceContainerLowestLight = Color(0xFFFFFFFF) // Crisp pure-paper hero / card lift
val PaperSurfaceContainerLowLight = Color(0xFFF2EFE9)    // Subtle secondary panel
val PaperSurfaceContainerLight = Color(0xFFECE8DF)       // Default card / group surface
val PaperSurfaceContainerHighLight = Color(0xFFE4DFD4)   // Nested entry row / inset surface
val PaperSurfaceContainerHighestLight = Color(0xFFDBD5C8)// Active chip / dense bar surface

val PaperOutlineLight = Color(0xFF838983)
val PaperOutlineVariantLight = Color(0xFFD5CFC2)         // Subtle editorial hairlines
val PaperInverseSurfaceLight = Color(0xFF2B302D)
val PaperInverseOnSurfaceLight = Color(0xFFF1EFEA)
val PaperInversePrimaryLight = Color(0xFF7AD6B5)
val PaperScrimLight = Color(0xFF000000)

// --- Dark Palette: Obsidian Ledger ---
val PinePrimaryDark = Color(0xFF6FD4B0)           // Luminous spruce-mint — crisp & restrained
val OnPinePrimaryDark = Color(0xFF003829)
val PinePrimaryContainerDark = Color(0xFF0F4A3A)
val OnPinePrimaryContainerDark = Color(0xFFC2F2DF)

val OliveSecondaryDark = Color(0xFFB2CCC0)
val OnOliveSecondaryDark = Color(0xFF1D332A)
val OliveSecondaryContainerDark = Color(0xFF334A40)
val OnOliveSecondaryContainerDark = Color(0xFFCFE6DA)

val TerracottaTertiaryDark = Color(0xFFF0A37A)    // Warm brushed terracotta-amber
val OnTerracottaTertiaryDark = Color(0xFF4D210B)
val TerracottaTertiaryContainerDark = Color(0xFF6B3419)
val OnTerracottaTertiaryContainerDark = Color(0xFFFFDCCB)

val CrimsonErrorDark = Color(0xFFF2B8B5)
val OnCrimsonErrorDark = Color(0xFF601410)
val CrimsonErrorContainerDark = Color(0xFF8C1D18)
val OnCrimsonErrorContainerDark = Color(0xFFF9DEDC)

// Obsidian Tonal Surface Hierarchy (stepped luminance so raised layers read clearly)
val ObsidianBackgroundDark = Color(0xFF111413)
val ObsidianOnBackgroundDark = Color(0xFFEAE7E0)
val ObsidianSurfaceDark = Color(0xFF111413)
val ObsidianOnSurfaceDark = Color(0xFFEAE7E0)
val ObsidianSurfaceVariantDark = Color(0xFF282E2B)
val ObsidianOnSurfaceVariantDark = Color(0xFFA0A8A2)

val ObsidianSurfaceDimDark = Color(0xFF0C0E0D)
val ObsidianSurfaceBrightDark = Color(0xFF323936)
val ObsidianSurfaceContainerLowestDark = Color(0xFF0A0C0B)
val ObsidianSurfaceContainerLowDark = Color(0xFF161A18)
val ObsidianSurfaceContainerDark = Color(0xFF1C211F)
val ObsidianSurfaceContainerHighDark = Color(0xFF242A27)
val ObsidianSurfaceContainerHighestDark = Color(0xFF2E3532)

val ObsidianOutlineDark = Color(0xFF6E7671)
val ObsidianOutlineVariantDark = Color(0xFF353D39)
val ObsidianInverseSurfaceDark = Color(0xFFEAE7E0)
val ObsidianInverseOnSurfaceDark = Color(0xFF1A1E1C)
val ObsidianInversePrimaryDark = Color(0xFF1B5E4B)
val ObsidianScrimDark = Color(0xFF000000)

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
)

val LightFinnerExtendedColors = FinnerExtendedColors(
    inflow = Color(0xFF1E6E46),
    onInflow = Color(0xFFFFFFFF),
    inflowContainer = Color(0xFFD6EFE1),
    outflow = Color(0xFF2D4356),
    onOutflow = Color(0xFFFFFFFF),
    outflowContainer = Color(0xFFDCE5EC),
    brazilAccent = Color(0xFF9C522B),
    onBrazilAccent = Color(0xFFFFFFFF),
    brazilContainer = Color(0xFFF6DFD2),
    warning = Color(0xFF9A6700),
    onWarning = Color(0xFFFFFFFF),
    warningContainer = Color(0xFFF9EBC8),
    heroGradientStart = Color(0xFF183D32),
    heroGradientEnd = Color(0xFF245244),
)

val DarkFinnerExtendedColors = FinnerExtendedColors(
    inflow = Color(0xFF6DD69B),
    onInflow = Color(0xFF05381E),
    inflowContainer = Color(0xFF144A2D),
    outflow = Color(0xFF9CBCD6),
    onOutflow = Color(0xFF10283B),
    outflowContainer = Color(0xFF22394D),
    brazilAccent = Color(0xFFF0A37A),
    onBrazilAccent = Color(0xFF4D210B),
    brazilContainer = Color(0xFF633118),
    warning = Color(0xFFE7BA5C),
    onWarning = Color(0xFF3E2900),
    warningContainer = Color(0xFF593D04),
    heroGradientStart = Color(0xFF152922),
    heroGradientEnd = Color(0xFF1D3A30),
)

val LocalFinnerExtendedColors = staticCompositionLocalOf { LightFinnerExtendedColors }
