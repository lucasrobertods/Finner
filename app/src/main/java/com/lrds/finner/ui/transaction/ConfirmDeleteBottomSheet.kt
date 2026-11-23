package com.lrds.finner.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lrds.finner.ui.theme.FinnerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteBottomSheet(
    title: String,
    message: String,
    confirmLabel: String = "Delete",
    confirmAllLabel: String? = null,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
    onConfirmDeleteAll: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                text = title,
                style = typography.titleLarge,
                color = colors.onSurface,
            )
            Text(
                text = message,
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
            )

            if (confirmAllLabel != null && onConfirmDeleteAll != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    OutlinedButton(
                        onClick = {
                            onConfirmDelete()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(confirmLabel)
                    }
                    Button(
                        onClick = {
                            onConfirmDeleteAll()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.error,
                            contentColor = colors.onError,
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(confirmAllLabel)
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel")
                }
            } else {
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
                            onConfirmDelete()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.error,
                            contentColor = colors.onError,
                        ),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(confirmLabel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.sheetBottomPadding))
        }
    }
}
