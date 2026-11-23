package com.lrds.finner.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
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
)

private val LightColorScheme = lightColorScheme(
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
    inversePrimary = PaperInversePrimaryDark,
    scrim = PaperScrimLight,
)

@Composable
fun FinnerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = if (darkTheme) DarkFinnerExtendedColors else LightFinnerExtendedColors

    CompositionLocalProvider(LocalFinnerExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = FinnerShapes,
            content = content
        )
    }
}

object FinnerTheme {
    val extendedColors: FinnerExtendedColors
        @Composable get() = LocalFinnerExtendedColors.current
}
