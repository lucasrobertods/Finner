package com.lrds.finner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lrds.finner.R

// =============================================================================
// Finner Typography (Revolut-Inspired Geometric Sans + Tabular Numerals)
// - Plus Jakarta Sans variable font across display, headers, body, and metadata
//   for crisp fintech legibility on OLED dark & light surfaces.
// - Tabular lining figures ("tnum, lnum") ensure EUR/BRL amounts align cleanly.
// =============================================================================

internal const val TABULAR_NUMBERS = "tnum, lnum"

@OptIn(ExperimentalTextApi::class)
val FinnerBodyFontFamily = FontFamily(
    Font(
        resId = R.font.plus_jakarta_sans_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        resId = R.font.plus_jakarta_sans_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        resId = R.font.plus_jakarta_sans_variable,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        resId = R.font.plus_jakarta_sans_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
    Font(
        resId = R.font.plus_jakarta_sans_variable,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800)),
    ),
)

val FinnerDisplayFontFamily: FontFamily = FinnerBodyFontFamily

@Immutable
data class FinnerTypography(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleMediumBold: TextStyle,
    val titleSmall: TextStyle,
    val titleSmallBold: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodyMediumSemiBold: TextStyle,
    val bodyMediumBold: TextStyle,
    val bodySmall: TextStyle,
    val bodySmallSemiBold: TextStyle,
    val labelLarge: TextStyle,
    val labelLargeMedium: TextStyle,
    val labelLargeBold: TextStyle,
    val labelMedium: TextStyle,
    val labelMediumMedium: TextStyle,
    val labelMediumBold: TextStyle,
    val labelSmall: TextStyle,
    val labelSmallMedium: TextStyle,
    val labelSmallBold: TextStyle,
) {
    fun toMaterialTypography(): Typography = Typography(
        displayLarge = displayLarge,
        displayMedium = displayMedium,
        displaySmall = displaySmall,
        headlineLarge = headlineLarge,
        headlineMedium = headlineMedium,
        headlineSmall = headlineSmall,
        titleLarge = titleLarge,
        titleMedium = titleMedium,
        titleSmall = titleSmall,
        bodyLarge = bodyLarge,
        bodyMedium = bodyMedium,
        bodySmall = bodySmall,
        labelLarge = labelLarge,
        labelMedium = labelMedium,
        labelSmall = labelSmall,
    )
}

/**
 * Dedicated tabular-numeral styles for financial amounts and stat callouts.
 * Every style guarantees `fontFeatureSettings = "tnum, lnum"`.
 */
@Immutable
data class FinnerNumericStyles(
    val heroAmount: TextStyle,
    val sectionAmount: TextStyle,
    val cardAmount: TextStyle,
    val rowAmount: TextStyle,
    val rowAmountBold: TextStyle,
    val metaAmount: TextStyle,
)

private val BaseTitleMedium = TextStyle(
    fontFamily = FinnerBodyFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = (-0.1).sp,
    fontFeatureSettings = TABULAR_NUMBERS,
)

private val BaseTitleSmall = TextStyle(
    fontFamily = FinnerBodyFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = TABULAR_NUMBERS,
)

private val BaseBodyMedium = TextStyle(
    fontFamily = FinnerBodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = TABULAR_NUMBERS,
)

private val BaseBodySmall = TextStyle(
    fontFamily = FinnerBodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 17.sp,
    letterSpacing = 0.1.sp,
    fontFeatureSettings = TABULAR_NUMBERS,
)

private val BaseLabelLarge = TextStyle(
    fontFamily = FinnerBodyFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.1.sp,
    fontFeatureSettings = TABULAR_NUMBERS,
)

private val BaseLabelMedium = TextStyle(
    fontFamily = FinnerBodyFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.5.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.35.sp,
    fontFeatureSettings = TABULAR_NUMBERS,
)

private val BaseLabelSmall = TextStyle(
    fontFamily = FinnerBodyFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 10.5.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.7.sp,
    fontFeatureSettings = TABULAR_NUMBERS,
)

val DefaultFinnerTypography = FinnerTypography(
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
    titleMedium = BaseTitleMedium,
    titleMediumBold = BaseTitleMedium.copy(fontWeight = FontWeight.Bold),
    titleSmall = BaseTitleSmall,
    titleSmallBold = BaseTitleSmall.copy(fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    bodyMedium = BaseBodyMedium,
    bodyMediumSemiBold = BaseBodyMedium.copy(fontWeight = FontWeight.SemiBold),
    bodyMediumBold = BaseBodyMedium.copy(fontWeight = FontWeight.Bold),
    bodySmall = BaseBodySmall,
    bodySmallSemiBold = BaseBodySmall.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = BaseLabelLarge,
    labelLargeMedium = BaseLabelLarge.copy(fontWeight = FontWeight.Medium),
    labelLargeBold = BaseLabelLarge.copy(fontWeight = FontWeight.Bold),
    labelMedium = BaseLabelMedium,
    labelMediumMedium = BaseLabelMedium.copy(fontWeight = FontWeight.Medium),
    labelMediumBold = BaseLabelMedium.copy(fontWeight = FontWeight.Bold),
    labelSmall = BaseLabelSmall,
    labelSmallMedium = BaseLabelSmall.copy(fontWeight = FontWeight.Medium),
    labelSmallBold = BaseLabelSmall.copy(fontWeight = FontWeight.Bold),
)

val DefaultFinnerNumericStyles = FinnerNumericStyles(
    heroAmount = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1.1).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    sectionAmount = TextStyle(
        fontFamily = FinnerDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    cardAmount = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.25).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    rowAmount = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    rowAmountBold = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
    metaAmount = TextStyle(
        fontFamily = FinnerBodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.2.sp,
        fontFeatureSettings = TABULAR_NUMBERS,
    ),
)

val LocalFinnerTypography = staticCompositionLocalOf { DefaultFinnerTypography }
val LocalFinnerNumericStyles = staticCompositionLocalOf { DefaultFinnerNumericStyles }
