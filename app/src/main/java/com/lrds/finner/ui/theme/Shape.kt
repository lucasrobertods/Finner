package com.lrds.finner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// =============================================================================
// Finner Shape Language:
// Varied radii by semantic role — crisp utility chips and badges contrasted
// against generous hero containers and soft modal bottom sheets.
// =============================================================================

val FinnerShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),  // Micro badges, status tags, progress indicators
    small = RoundedCornerShape(10.dp),      // Filter chips, text fields, nested entry rows
    medium = RoundedCornerShape(16.dp),     // Standard category & reconciliation cards
    large = RoundedCornerShape(24.dp),      // Hero summary containers, dialogs
    extraLarge = RoundedCornerShape(28.dp), // Modal bottom sheets, floating navigation surfaces
)

object FinnerCustomShapes {
    val bottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val pill = RoundedCornerShape(percent = 50)
    val ledgerRow = RoundedCornerShape(10.dp)
    val heroLedgerCard = RoundedCornerShape(24.dp)
}
