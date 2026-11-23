package com.lrds.finner.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

// =============================================================================
// Finner Color Tokens: Revolut-Inspired OLED Dark & Crisp Neutral Light
// Aligned with Stitch "Finner 2027" (Hero First, January Details, Goals,
// History, Profile, Quick Add Modal).
//
// Raw hex primitives are private to `ui/theme` — feature screens must consume
// semantic tokens exclusively via `FinnerTheme.colors` or `MaterialTheme.colorScheme`.
// =============================================================================

// --- Core Stitch Dark Primitives (OLED Near-Black + Elevated Charcoal Slate) ---
private val StitchMintDark = Color(0xFF00D09C)              // Stitch accentMint
private val StitchOnMintDark = Color(0xFF00291E)
private val StitchMintContainerDark = Color(0xFF0D2E25)
private val StitchOnMintContainerDark = Color(0xFF6FF7CD)

private val StitchSlateSecondaryDark = Color(0xFFB4BAC6)
private val StitchOnSlateSecondaryDark = Color(0xFF1A1D24)
private val StitchSlateSecondaryContainerDark = Color(0xFF1F2430)
private val StitchOnSlateSecondaryContainerDark = Color(0xFFE6EAF2)

private val StitchAmberTertiaryDark = Color(0xFFF59E0B)     // Brazil / warm accent
private val StitchOnAmberTertiaryDark = Color(0xFF311B00)
private val StitchAmberTertiaryContainerDark = Color(0xFF2B2212)
private val StitchOnAmberTertiaryContainerDark = Color(0xFFFDE68A)

private val StitchCoralErrorDark = Color(0xFFF43F5E)        // Negative / deficit / alert coral
private val StitchOnCoralErrorDark = Color(0xFFFFFFFF)
private val StitchCoralErrorContainerDark = Color(0xFF2D141B)
private val StitchOnCoralErrorContainerDark = Color(0xFFFDA4AF)

private val ObsidianBackgroundDark = Color(0xFF090A0D)              // Stitch appDark
private val ObsidianOnBackgroundDark = Color(0xFFF9FAFB)
private val ObsidianSurfaceDark = Color(0xFF090A0D)
private val ObsidianOnSurfaceDark = Color(0xFFF9FAFB)
private val ObsidianSurfaceVariantDark = Color(0xFF1C1F26)
private val ObsidianOnSurfaceVariantDark = Color(0xFF8E929B)

private val ObsidianSurfaceDimDark = Color(0xFF06070A)
private val ObsidianSurfaceBrightDark = Color(0xFF262A34)
private val ObsidianSurfaceContainerLowestDark = Color(0xFF050608)
private val ObsidianSurfaceContainerLowDark = Color(0xFF111318)     // Modal sheets & recessed panels
private val ObsidianSurfaceContainerDark = Color(0xFF16181D)        // Stitch cardDark
private val ObsidianSurfaceContainerHighDark = Color(0xFF1C1F26)    // Nested rows / subtle elevated pills
private val ObsidianSurfaceContainerHighestDark = Color(0xFF22252D) // Progress tracks / elevated pills

private val ObsidianOutlineDark = Color(0xFF3A3F4D)
private val ObsidianOutlineVariantDark = Color(0xFF232834)          // Stitch cardBorder hairline
private val ObsidianInverseSurfaceDark = Color(0xFFFFFFFF)          // Crisp white active pill in dark mode
private val ObsidianInverseOnSurfaceDark = Color(0xFF090A0D)
private val ObsidianInversePrimaryDark = Color(0xFF008F6B)
private val ObsidianScrimDark = Color(0xFF000000)

// --- Crisp Light Primitives (Harmonized Neutral + Mint/Coral) ---
private val StitchMintLight = Color(0xFF008F6B)
private val StitchOnMintLight = Color(0xFFFFFFFF)
private val StitchMintContainerLight = Color(0xFFD3F6EA)
private val StitchOnMintContainerLight = Color(0xFF002B1F)

private val StitchSlateSecondaryLight = Color(0xFF475060)
private val StitchOnSlateSecondaryLight = Color(0xFFFFFFFF)
private val StitchSlateSecondaryContainerLight = Color(0xFFE6EAF0)
private val StitchOnSlateSecondaryContainerLight = Color(0xFF141820)

private val StitchAmberTertiaryLight = Color(0xFFB45309)
private val StitchOnAmberTertiaryLight = Color(0xFFFFFFFF)
private val StitchAmberTertiaryContainerLight = Color(0xFFFEF3C7)
private val StitchOnAmberTertiaryContainerLight = Color(0xFF451A03)

private val StitchCoralErrorLight = Color(0xFFE11D48)
private val StitchOnCoralErrorLight = Color(0xFFFFFFFF)
private val StitchCoralErrorContainerLight = Color(0xFFFFE4E6)
private val StitchOnCoralErrorContainerLight = Color(0xFF4C0519)

private val PaperBackgroundLight = Color(0xFFF4F6F9)
private val PaperOnBackgroundLight = Color(0xFF0B0D12)
private val PaperSurfaceLight = Color(0xFFF4F6F9)
private val PaperOnSurfaceLight = Color(0xFF0B0D12)
private val PaperSurfaceVariantLight = Color(0xFFE8ECF2)
private val PaperOnSurfaceVariantLight = Color(0xFF5D6472)

private val PaperSurfaceDimLight = Color(0xFFE2E6EE)
private val PaperSurfaceBrightLight = Color(0xFFFFFFFF)
private val PaperSurfaceContainerLowestLight = Color(0xFFFFFFFF)
private val PaperSurfaceContainerLowLight = Color(0xFFF9FAFC)
private val PaperSurfaceContainerLight = Color(0xFFFFFFFF)
private val PaperSurfaceContainerHighLight = Color(0xFFEDF0F5)
private val PaperSurfaceContainerHighestLight = Color(0xFFE3E7EF)

private val PaperOutlineLight = Color(0xFF9AA1B0)
private val PaperOutlineVariantLight = Color(0xFFE2E6EE)
private val PaperInverseSurfaceLight = Color(0xFF0B0D12)
private val PaperInverseOnSurfaceLight = Color(0xFFFFFFFF)
private val PaperInversePrimaryLight = Color(0xFF00D09C)
private val PaperScrimLight = Color(0xFF000000)

// --- Domain Accent & Chrome Primitives ---
private val LightInflowContainer = Color(0xFFD6F5EA)
private val LightBrazilAccent = Color(0xFFD97706)
private val LightHeroGradientStart = Color(0xFFDBE6F8)
private val LightNavDockBackground = Color(0xF5FFFFFF)

private val DarkOnBrazilAccent = Color(0xFF2A1800)
private val DarkWarning = Color(0xFFF5A524)
private val DarkOnWarning = Color(0xFF2B1B00)
private val DarkWarningContainer = Color(0xFF332610)
private val DarkHeroGradientStart = Color(0xFF1B2B4B) // Revolut ambient slate-blue aura
private val DarkNavDockBackground = Color(0xEB14171F)

// --- Budget Envelope Bucket Primitives (Light & Dark Contrast-Tuned) ---
private val BucketBlueDark = Color(0xFF3B82F6)    // Revo Blue (Bills)
private val BucketPurpleDark = Color(0xFFA855F7)  // Revo Purple (Joint)
private val BucketOrangeDark = Color(0xFFF97316)  // Revo Orange (Trip)
private val BucketCyanDark = Color(0xFF06B6D4)
private val BucketPinkDark = Color(0xFFEC4899)
private val BucketIndigoDark = Color(0xFF6366F1)
private val BucketTealDark = Color(0xFF14B8A6)
private val BucketLimeDark = Color(0xFF84CC16)
private val BucketFallbackDark = Color(0xFF64748B)

private val BucketBlueLight = Color(0xFF2563EB)
private val BucketPurpleLight = Color(0xFF9333EA)
private val BucketOrangeLight = Color(0xFFEA580C)
private val BucketCyanLight = Color(0xFF0891B2)
private val BucketPinkLight = Color(0xFFDB2777)
private val BucketIndigoLight = Color(0xFF4F46E5)
private val BucketTealLight = Color(0xFF0D9488)
private val BucketLimeLight = Color(0xFF65A30D)
private val BucketFallbackLight = Color(0xFF475569)

/**
 * Semantic opacity tokens for tinted icon containers, chip states, subtle dividers,
 * and ambient gradients so alpha values remain consistent across themes.
 */
object FinnerAlpha {
    const val tintedContainer: Float = 0.16f
    const val envelopeBadgeContainer: Float = 0.18f
    const val selectedContainer: Float = 0.22f
    const val unselectedBorder: Float = 0.50f
    const val previewContainer: Float = 0.55f
    const val dividerSubtle: Float = 0.60f
    const val selectedBorder: Float = 0.70f
    const val auraTop: Float = 0.45f
    const val auraMid: Float = 0.12f
}

/**
 * Theme-aware semantic colors for budget envelopes (buckets).
 * Resolves default envelopes deterministically and hashes custom bucket names
 * into a curated high-contrast palette.
 */
@Immutable
data class FinnerBucketColors(
    val bills: Color,
    val reserve: Color,
    val joint: Color,
    val personal: Color,
    val trip: Color,
    val fallback: Color,
    val customPalette: List<Color>,
) {
    fun forName(bucketName: String?): Color {
        if (bucketName.isNullOrBlank()) return fallback
        return when (val normalized = bucketName.trim().lowercase()) {
            "bills" -> bills
            "reserve" -> reserve
            "joint" -> joint
            "personal" -> personal
            "trip" -> trip
            else -> {
                val hash = abs(normalized.hashCode())
                customPalette[hash % customPalette.size]
            }
        }
    }
}

val LightFinnerBucketColors = FinnerBucketColors(
    bills = BucketBlueLight,
    reserve = StitchMintLight,
    joint = BucketPurpleLight,
    personal = LightBrazilAccent,
    trip = BucketOrangeLight,
    fallback = BucketFallbackLight,
    customPalette = listOf(
        BucketOrangeLight,
        BucketCyanLight,
        BucketPinkLight,
        BucketIndigoLight,
        BucketTealLight,
        BucketLimeLight,
    ),
)

val DarkFinnerBucketColors = FinnerBucketColors(
    bills = BucketBlueDark,
    reserve = StitchMintDark,
    joint = BucketPurpleDark,
    personal = StitchAmberTertiaryDark,
    trip = BucketOrangeDark,
    fallback = BucketFallbackDark,
    customPalette = listOf(
        BucketOrangeDark,
        BucketCyanDark,
        BucketPinkDark,
        BucketIndigoDark,
        BucketTealDark,
        BucketLimeDark,
    ),
)

/**
 * Single semantic color palette for Finner covering Material 3 surface/accent roles,
 * domain-specific financial status roles, chrome tokens, and budget bucket colors.
 */
@Immutable
data class FinnerColors(
    val isDark: Boolean,

    // Brand & Accent Roles
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,

    // Surface & Neutral Hierarchy
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceDim: Color,
    val surfaceBright: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val outline: Color,
    val outlineVariant: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val inversePrimary: Color,
    val scrim: Color,

    // Financial Domain Status Roles
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

    // Chrome & Ambient Surfaces
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val cardBorder: Color,
    val navDockBackground: Color,

    // Budget Envelope Tokens
    val buckets: FinnerBucketColors,
) {
    fun toMaterialColorScheme(): ColorScheme = if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceDim = surfaceDim,
            surfaceBright = surfaceBright,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            outline = outline,
            outlineVariant = outlineVariant,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            scrim = scrim,
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceDim = surfaceDim,
            surfaceBright = surfaceBright,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            outline = outline,
            outlineVariant = outlineVariant,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            scrim = scrim,
        )
    }

    fun mergeDynamicScheme(scheme: ColorScheme): FinnerColors = copy(
        primary = scheme.primary,
        onPrimary = scheme.onPrimary,
        primaryContainer = scheme.primaryContainer,
        onPrimaryContainer = scheme.onPrimaryContainer,
        secondary = scheme.secondary,
        onSecondary = scheme.onSecondary,
        secondaryContainer = scheme.secondaryContainer,
        onSecondaryContainer = scheme.onSecondaryContainer,
        tertiary = scheme.tertiary,
        onTertiary = scheme.onTertiary,
        tertiaryContainer = scheme.tertiaryContainer,
        onTertiaryContainer = scheme.onTertiaryContainer,
        error = scheme.error,
        onError = scheme.onError,
        errorContainer = scheme.errorContainer,
        onErrorContainer = scheme.onErrorContainer,
        background = scheme.background,
        onBackground = scheme.onBackground,
        surface = scheme.surface,
        onSurface = scheme.onSurface,
        surfaceVariant = scheme.surfaceVariant,
        onSurfaceVariant = scheme.onSurfaceVariant,
        surfaceDim = scheme.surfaceDim,
        surfaceBright = scheme.surfaceBright,
        surfaceContainerLowest = scheme.surfaceContainerLowest,
        surfaceContainerLow = scheme.surfaceContainerLow,
        surfaceContainer = scheme.surfaceContainer,
        surfaceContainerHigh = scheme.surfaceContainerHigh,
        surfaceContainerHighest = scheme.surfaceContainerHighest,
        outline = scheme.outline,
        outlineVariant = scheme.outlineVariant,
        inverseSurface = scheme.inverseSurface,
        inverseOnSurface = scheme.inverseOnSurface,
        inversePrimary = scheme.inversePrimary,
        scrim = scheme.scrim,
    )
}

val LightFinnerColors = FinnerColors(
    isDark = false,
    primary = StitchMintLight,
    onPrimary = StitchOnMintLight,
    primaryContainer = StitchMintContainerLight,
    onPrimaryContainer = StitchOnMintContainerLight,
    secondary = StitchSlateSecondaryLight,
    onSecondary = StitchOnSlateSecondaryLight,
    secondaryContainer = StitchSlateSecondaryContainerLight,
    onSecondaryContainer = StitchOnSlateSecondaryContainerLight,
    tertiary = StitchAmberTertiaryLight,
    onTertiary = StitchOnAmberTertiaryLight,
    tertiaryContainer = StitchAmberTertiaryContainerLight,
    onTertiaryContainer = StitchOnAmberTertiaryContainerLight,
    error = StitchCoralErrorLight,
    onError = StitchOnCoralErrorLight,
    errorContainer = StitchCoralErrorContainerLight,
    onErrorContainer = StitchOnCoralErrorContainerLight,
    background = PaperBackgroundLight,
    onBackground = PaperOnBackgroundLight,
    surface = PaperSurfaceLight,
    onSurface = PaperOnSurfaceLight,
    surfaceVariant = PaperSurfaceVariantLight,
    onSurfaceVariant = PaperOnSurfaceVariantLight,
    surfaceDim = PaperSurfaceDimLight,
    surfaceBright = PaperSurfaceBrightLight,
    surfaceContainerLowest = PaperSurfaceContainerLowestLight,
    surfaceContainerLow = PaperSurfaceContainerLowLight,
    surfaceContainer = PaperSurfaceContainerLight,
    surfaceContainerHigh = PaperSurfaceContainerHighLight,
    surfaceContainerHighest = PaperSurfaceContainerHighestLight,
    outline = PaperOutlineLight,
    outlineVariant = PaperOutlineVariantLight,
    inverseSurface = PaperInverseSurfaceLight,
    inverseOnSurface = PaperInverseOnSurfaceLight,
    inversePrimary = PaperInversePrimaryLight,
    scrim = PaperScrimLight,
    inflow = StitchMintLight,
    onInflow = StitchOnMintLight,
    inflowContainer = LightInflowContainer,
    outflow = StitchCoralErrorLight,
    onOutflow = StitchOnCoralErrorLight,
    outflowContainer = StitchCoralErrorContainerLight,
    brazilAccent = LightBrazilAccent,
    onBrazilAccent = StitchOnAmberTertiaryLight,
    brazilContainer = StitchAmberTertiaryContainerLight,
    warning = StitchAmberTertiaryLight,
    onWarning = StitchOnAmberTertiaryLight,
    warningContainer = StitchAmberTertiaryContainerLight,
    heroGradientStart = LightHeroGradientStart,
    heroGradientEnd = PaperBackgroundLight,
    cardBorder = PaperOutlineVariantLight,
    navDockBackground = LightNavDockBackground,
    buckets = LightFinnerBucketColors,
)

val DarkFinnerColors = FinnerColors(
    isDark = true,
    primary = StitchMintDark,
    onPrimary = StitchOnMintDark,
    primaryContainer = StitchMintContainerDark,
    onPrimaryContainer = StitchOnMintContainerDark,
    secondary = StitchSlateSecondaryDark,
    onSecondary = StitchOnSlateSecondaryDark,
    secondaryContainer = StitchSlateSecondaryContainerDark,
    onSecondaryContainer = StitchOnSlateSecondaryContainerDark,
    tertiary = StitchAmberTertiaryDark,
    onTertiary = StitchOnAmberTertiaryDark,
    tertiaryContainer = StitchAmberTertiaryContainerDark,
    onTertiaryContainer = StitchOnAmberTertiaryContainerDark,
    error = StitchCoralErrorDark,
    onError = StitchOnCoralErrorDark,
    errorContainer = StitchCoralErrorContainerDark,
    onErrorContainer = StitchOnCoralErrorContainerDark,
    background = ObsidianBackgroundDark,
    onBackground = ObsidianOnBackgroundDark,
    surface = ObsidianSurfaceDark,
    onSurface = ObsidianOnSurfaceDark,
    surfaceVariant = ObsidianSurfaceVariantDark,
    onSurfaceVariant = ObsidianOnSurfaceVariantDark,
    surfaceDim = ObsidianSurfaceDimDark,
    surfaceBright = ObsidianSurfaceBrightDark,
    surfaceContainerLowest = ObsidianSurfaceContainerLowestDark,
    surfaceContainerLow = ObsidianSurfaceContainerLowDark,
    surfaceContainer = ObsidianSurfaceContainerDark,
    surfaceContainerHigh = ObsidianSurfaceContainerHighDark,
    surfaceContainerHighest = ObsidianSurfaceContainerHighestDark,
    outline = ObsidianOutlineDark,
    outlineVariant = ObsidianOutlineVariantDark,
    inverseSurface = ObsidianInverseSurfaceDark,
    inverseOnSurface = ObsidianInverseOnSurfaceDark,
    inversePrimary = ObsidianInversePrimaryDark,
    scrim = ObsidianScrimDark,
    inflow = StitchMintDark,
    onInflow = StitchOnMintDark,
    inflowContainer = StitchMintContainerDark,
    outflow = StitchCoralErrorDark,
    onOutflow = StitchOnCoralErrorDark,
    outflowContainer = StitchCoralErrorContainerDark,
    brazilAccent = StitchAmberTertiaryDark,
    onBrazilAccent = DarkOnBrazilAccent,
    brazilContainer = StitchAmberTertiaryContainerDark,
    warning = DarkWarning,
    onWarning = DarkOnWarning,
    warningContainer = DarkWarningContainer,
    heroGradientStart = DarkHeroGradientStart,
    heroGradientEnd = ObsidianBackgroundDark,
    cardBorder = ObsidianOutlineVariantDark,
    navDockBackground = DarkNavDockBackground,
    buckets = DarkFinnerBucketColors,
)

val LocalFinnerColors = staticCompositionLocalOf { DarkFinnerColors }
