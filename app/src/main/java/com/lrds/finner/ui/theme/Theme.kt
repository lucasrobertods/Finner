package com.lrds.finner.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

@Composable
fun FinnerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    spacing: FinnerSpacing = DefaultFinnerSpacing,
    sizes: FinnerSizes = DefaultFinnerSizes,
    radius: FinnerRadius = DefaultFinnerRadius,
    elevation: FinnerElevation = DefaultFinnerElevation,
    typography: FinnerTypography = DefaultFinnerTypography,
    numericStyles: FinnerNumericStyles = DefaultFinnerNumericStyles,
    content: @Composable () -> Unit,
) {
    val baseColors = if (darkTheme) DarkFinnerColors else LightFinnerColors
    val resolvedColors = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        val dynamicScheme = if (darkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
        baseColors.mergeDynamicScheme(dynamicScheme)
    } else {
        baseColors
    }
    val shapes = if (radius == DefaultFinnerRadius) {
        DefaultFinnerShapes
    } else {
        FinnerShapeTokens.fromRadius(radius)
    }

    CompositionLocalProvider(
        LocalFinnerColors provides resolvedColors,
        LocalFinnerSpacing provides spacing,
        LocalFinnerSizes provides sizes,
        LocalFinnerRadius provides radius,
        LocalFinnerShapes provides shapes,
        LocalFinnerElevation provides elevation,
        LocalFinnerTypography provides typography,
        LocalFinnerNumericStyles provides numericStyles,
    ) {
        MaterialTheme(
            colorScheme = resolvedColors.toMaterialColorScheme(),
            typography = typography.toMaterialTypography(),
            shapes = shapes.toMaterialShapes(),
            content = content,
        )
    }
}

/**
 * Unified access point for all Finner Design System tokens.
 *
 * Usage:
 * - `FinnerTheme.colors` (semantic surface, accent, status, chrome & bucket tokens)
 * - `FinnerTheme.spacing` (4dp-grid spacing scale & layout spacing tokens)
 * - `FinnerTheme.sizes` (icon sizes, button heights, touch targets, pill dimensions, borders)
 * - `FinnerTheme.radius` (raw corner radius tokens)
 * - `FinnerTheme.shapes` (pre-built `RoundedCornerShape` tokens derived from `radius`)
 * - `FinnerTheme.elevation` (surface & floating dock elevation tokens)
 * - `FinnerTheme.typography` (Plus Jakarta Sans text hierarchy with tabular figures)
 * - `FinnerTheme.numericStyles` (financial amount & callout styles with `tnum, lnum`)
 */
object FinnerTheme {
    val colors: FinnerColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFinnerColors.current

    val spacing: FinnerSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalFinnerSpacing.current

    val sizes: FinnerSizes
        @Composable
        @ReadOnlyComposable
        get() = LocalFinnerSizes.current

    val radius: FinnerRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalFinnerRadius.current

    val shapes: FinnerShapeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalFinnerShapes.current

    val elevation: FinnerElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalFinnerElevation.current

    val typography: FinnerTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalFinnerTypography.current

    val numericStyles: FinnerNumericStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalFinnerNumericStyles.current
}
