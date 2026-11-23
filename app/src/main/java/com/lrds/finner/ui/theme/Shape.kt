package com.lrds.finner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// =============================================================================
// Finner Shape Language (Stitch Revolut-Inspired):
// - Large soft cards (22dp–24dp) paired with crisp circular pills and micro tags
// =============================================================================

val FinnerShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // Micro badges, status tags, progress bars
    small = RoundedCornerShape(12.dp),       // Filter chips, text fields, nested entry rows
    medium = RoundedCornerShape(18.dp),      // Standard cards & modals
    large = RoundedCornerShape(22.dp),       // Primary dashboard section cards
    extraLarge = RoundedCornerShape(28.dp),  // Modal bottom sheets, floating nav dock
)

object FinnerCustomShapes {
    val bottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val pill = RoundedCornerShape(percent = 50)
    val ledgerRow = RoundedCornerShape(14.dp)
    val heroLedgerCard = RoundedCornerShape(24.dp)
}
