package com.lrds.finner.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// =============================================================================
// Finner Component Sizes, Touch Targets, Borders & Elevations
// Aligned with Stitch "Finner 2027" component geometry and Android a11y targets.
// =============================================================================

@Immutable
data class FinnerSizes(
    // Icon scale
    val iconXs: Dp = 14.dp,
    val iconSm: Dp = 16.dp,
    val iconMd: Dp = 18.dp,
    val iconLg: Dp = 20.dp,
    val iconXl: Dp = 22.dp,
    val iconXxl: Dp = 24.dp,

    // Buttons, touch targets & avatars
    val minTouchTarget: Dp = 48.dp,
    val primaryButtonHeight: Dp = 52.dp,
    val quickActionButton: Dp = 52.dp,
    val avatarLg: Dp = 52.dp,
    val iconButtonSm: Dp = 28.dp,
    val iconButtonMd: Dp = 36.dp,
    val iconButtonLg: Dp = 40.dp,
    val iconBadgeSm: Dp = 36.dp,
    val iconBadgeMd: Dp = 40.dp,

    // Pills, dots & progress indicators
    val pillMinHeight: Dp = 38.dp,
    val monthPillMinWidth: Dp = 64.dp,
    val statusDot: Dp = 6.dp,
    val progressBarSm: Dp = 4.dp,
    val progressBarMd: Dp = 6.dp,

    // Chrome & borders
    val floatingNavSlotHeight: Dp = 84.dp,
    val borderThin: Dp = 1.dp,
    val borderSelected: Dp = 1.5.dp,
)

@Immutable
data class FinnerElevation(
    val none: Dp = 0.dp,
    val sm: Dp = 2.dp,
    val md: Dp = 6.dp,
    val lg: Dp = 12.dp,
    val floatingDock: Dp = 16.dp,
)

val DefaultFinnerSizes = FinnerSizes()
val DefaultFinnerElevation = FinnerElevation()

val LocalFinnerSizes = staticCompositionLocalOf { DefaultFinnerSizes }
val LocalFinnerElevation = staticCompositionLocalOf { DefaultFinnerElevation }
