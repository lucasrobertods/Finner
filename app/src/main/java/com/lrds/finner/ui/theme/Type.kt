package com.lrds.finner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lrds.finner.R

// =============================================================================
// Finner Typography:
// - Display / Headline: Fraunces (warm editorial serif with optical sizing)
// - UI / Body / Metadata: Plus Jakarta Sans (crisp geometric-humanist sans)
// - Tabular lining figures ("tnum, lnum") applied to numeric-heavy styles so
//   EUR/BRL balances, planned vs. actual columns, and counters align cleanly.
// =============================================================================

private const val TABULAR_NUMBERS = "tnum, lnum"

@OptIn(ExperimentalTextApi::class)
val FinnerDisplayFontFamily = FontFamily(
    Font(
        resId = R.font.fraunces_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(500),
            FontVariation.Setting("opsz", 48f),
            FontVariation.Setting("SOFT", 35f),
        )
    ),
    Font(
        resId = R.font.fraunces_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(600),
            FontVariation.Setting("opsz", 60f),
            FontVariation.Setting("SOFT", 30f),
        )
    ),
    Font(
        resId = R.font.fraunces_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(700),
            FontVariation.Setting("opsz", 72f),
            FontVariation.Setting("SOFT", 25f),
        )
    ),
)

@OptIn(ExperimentalTextApi::class)
val FinnerBodyFontFamily = FontFamily(
    Font(
        resId = R.font.plus_jakarta_sans_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        resId = R.font.plus_jakarta_sans_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        resId = R.font.plus_jakarta_sans_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        resId = R.font.plus_jakarta_sans_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
)

// Keep existing public API name `Typography` intact for seamless compilation
val Typography = Typography(
    // Hero balances & flagship financial totals
    displayLarge = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.75).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    displayMedium = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    displaySmall = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.25).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),

    // Screen headers & major section anchors
    headlineLarge = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.35).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    headlineMedium = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 23.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.2).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    headlineSmall = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.1).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),

    // Card titles, modal titles & category headers
    titleLarge = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.15).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    titleMedium = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    titleSmall = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.05.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),

    // Primary UI copy & transaction descriptions
    bodyLarge = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    bodyMedium = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    bodySmall = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.15.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),

    // Buttons, chips, and uppercase editorial eyebrows (PLANNED / ACTUAL / AVAILABLE)
    labelLarge = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    labelMedium = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.45.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    labelSmall = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.75.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
)

/** Dedicated tabular-numeral styles for financial amounts and stat callouts. */
object FinnerNumericStyles {
    val heroAmount = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.6).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    )
    val cardAmount = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    )
    val rowAmount = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    )
}
