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
// Finner Typography (Revolut-Inspired Geometric Sans + Tabular Numerals):
// - Plus Jakarta Sans variable font across display, headers, body, and metadata
//   for crisp fintech legibility on OLED dark & light surfaces.
// - Tabular lining figures ("tnum, lnum") ensure EUR/BRL amounts align cleanly.
// =============================================================================

private const val TABULAR_NUMBERS = "tnum, lnum"

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
    Font(
        resId = R.font.plus_jakarta_sans_variable,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    ),
)

val FinnerDisplayFontFamily: FontFamily = FinnerBodyFontFamily

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 42.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1.1).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    displayMedium = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.8).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    displaySmall = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),

    headlineLarge = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.45).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    headlineMedium = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    headlineSmall = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.2).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),

    titleLarge = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.25).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    titleMedium = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.1).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    titleSmall = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),

    bodyLarge = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    bodyMedium = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    bodySmall = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),

    labelLarge = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    labelMedium = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.5.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.35.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    labelSmall = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.7.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
)

/** Dedicated tabular-numeral styles for financial amounts and stat callouts. */
object FinnerNumericStyles {
    val heroAmount = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1.1).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    )
    val cardAmount = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.25).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    )
    val rowAmount = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    )
}
