package com.lrds.finner.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.lrds.finner.ui.theme.FinnerTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateDialog(
    monthLabel: String,
    selectedYear: Int = 2026,
    currentRate: Double,
    onDismiss: () -> Unit,
    onConfirmRate: (Double) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rateInput by remember(currentRate) {
        mutableStateOf(String.format(Locale.US, "%.2f", currentRate))
    }
    var rateError by remember { mutableStateOf<String?>(null) }

    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerLow,
        shape = shapes.bottomSheet,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.sheetHorizontal, vertical = spacing.sheetVertical),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Text(
                text = "Euro Exchange Rate — $monthLabel $selectedYear",
                style = typography.titleLarge,
                color = colors.onSurface,
            )
            Text(
                text = "Set the manual Euro exchange rate (1 € in R$) to convert Brazil commitments for this month.",
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            OutlinedTextField(
                value = rateInput,
                onValueChange = {
                    rateInput = it
                    rateError = null
                },
                label = { Text("1 EUR (€) = BRL (R$) *") },
                isError = rateError != null,
                supportingText = rateError?.let { err -> { Text(err) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val parsed = rateInput.trim().replace(",", ".").toDoubleOrNull()
                        if (parsed == null || parsed <= 0.0) {
                            rateError = "Please enter a valid exchange rate greater than zero"
                        } else {
                            onConfirmRate(parsed)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save")
                }
            }

            Spacer(modifier = Modifier.height(spacing.sheetBottomPadding))
        }
    }
}
