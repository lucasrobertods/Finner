package com.lrds.finner.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// =============================================================================
// Finner Corner Radius & Shape Language (Stitch Revolut-Inspired)
// - Large soft cards (22dp) paired with crisp circular pills and 12dp inner rows.
// - All Shapes are derived directly from FinnerRadius for single-source consistency.
// =============================================================================

@Immutable
data class FinnerRadius(
    val xs: Dp = 8.dp,      // Micro badges, status tags, compact indicators
    val sm: Dp = 12.dp,     // Filter chips, text fields, clickable list rows
    val md: Dp = 16.dp,     // Secondary CTA buttons, standard nested cards
    val lg: Dp = 22.dp,     // Primary dashboard section cards (Stitch rounded-[22px])
    val xl: Dp = 28.dp,     // Modal bottom sheets, nav dock active item
    val dock: Dp = 32.dp,   // Floating glass bottom navigation dock
)

@Immutable
data class FinnerShapeTokens(
    val extraSmall: RoundedCornerShape,
    val small: RoundedCornerShape,
    val medium: RoundedCornerShape,
    val large: RoundedCornerShape,
    val extraLarge: RoundedCornerShape,
    val sectionCard: RoundedCornerShape,
    val rowItem: RoundedCornerShape,
    val actionButton: RoundedCornerShape,
    val bottomSheet: RoundedCornerShape,
    val navDock: RoundedCornerShape,
    val navDockItem: RoundedCornerShape,
    val heroLedgerCard: RoundedCornerShape,
    val pill: RoundedCornerShape = CircleShape,
) {
    fun toMaterialShapes(): Shapes = Shapes(
        extraSmall = extraSmall,
        small = small,
        medium = medium,
        large = large,
        extraLarge = extraLarge,
    )

    companion object {
        fun fromRadius(radius: FinnerRadius): FinnerShapeTokens = FinnerShapeTokens(
            extraSmall = RoundedCornerShape(radius.xs),
            small = RoundedCornerShape(radius.sm),
            medium = RoundedCornerShape(radius.md),
            large = RoundedCornerShape(radius.lg),
            extraLarge = RoundedCornerShape(radius.xl),
            sectionCard = RoundedCornerShape(radius.lg),
            rowItem = RoundedCornerShape(radius.sm),
            actionButton = RoundedCornerShape(radius.md),
            bottomSheet = RoundedCornerShape(topStart = radius.xl, topEnd = radius.xl),
            navDock = RoundedCornerShape(radius.dock),
            navDockItem = RoundedCornerShape(radius.xl),
            heroLedgerCard = RoundedCornerShape(radius.lg),
            pill = CircleShape,
        )
    }
}

val DefaultFinnerRadius = FinnerRadius()
val DefaultFinnerShapes = FinnerShapeTokens.fromRadius(DefaultFinnerRadius)

val LocalFinnerRadius = staticCompositionLocalOf { DefaultFinnerRadius }
val LocalFinnerShapes = staticCompositionLocalOf { DefaultFinnerShapes }
