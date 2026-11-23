package com.lrds.finner.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// =============================================================================
// Finner Spacing Scale (4dp Base Grid)
// Aligned with Stitch "Finner 2027" mobile layouts (px-4/px-5/space-y-4).
// =============================================================================

@Immutable
data class FinnerSpacing(
    // Core 4dp-grid spacing scale
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,

    // Semantic layout & chrome spacing tokens
    val screenHorizontal: Dp = 16.dp,
    val topBarHorizontal: Dp = 20.dp,
    val topBarVertical: Dp = 12.dp,
    val sheetHorizontal: Dp = 20.dp,
    val sheetVertical: Dp = 12.dp,
    val sheetBottomPadding: Dp = 16.dp,
    val cardPadding: Dp = 16.dp,
    val cardPaddingCompactHorizontal: Dp = 12.dp,
    val cardPaddingCompactVertical: Dp = 8.dp,
    val sectionGap: Dp = 16.dp,
    val itemGap: Dp = 12.dp,
    val pillHorizontal: Dp = 16.dp,
    val pillVertical: Dp = 8.dp,
    val compactPillHorizontal: Dp = 12.dp,
    val compactPillVertical: Dp = 6.dp,
    val badgeHorizontal: Dp = 8.dp,
    val badgeVertical: Dp = 2.dp,
    val fabGap: Dp = 12.dp,
    val floatingNavBottomMargin: Dp = 12.dp,
    val floatingNavBottomClearance: Dp = 124.dp,
    val detailListBottomClearance: Dp = 36.dp,
)

val DefaultFinnerSpacing = FinnerSpacing()

val LocalFinnerSpacing = staticCompositionLocalOf { DefaultFinnerSpacing }
