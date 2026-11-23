package com.lrds.finner.ui.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.lrds.finner.core.currency.CurrencyConverter
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.GoalEntity
import com.lrds.finner.data.local.entity.GoalPriority
import com.lrds.finner.ui.dashboard.components.bucketColorFor
import com.lrds.finner.ui.theme.FinnerAlpha
import com.lrds.finner.ui.theme.FinnerTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GoalDialog(
    availableBuckets: List<String>,
    existingGoal: GoalEntity? = null,
    onDismiss: () -> Unit,
    onAddBucket: (String) -> Unit,
    onSaveGoal: (
        title: String,
        targetAmountCents: Long,
        currentSavedCents: Long,
        currency: CurrencyCode,
        priority: GoalPriority,
        bucketName: String?,
        notes: String,
    ) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = existingGoal != null

    var title by remember(existingGoal) { mutableStateOf(existingGoal?.title ?: "") }
    var targetText by remember(existingGoal) {
        mutableStateOf(
            if (existingGoal != null && existingGoal.targetAmountCents > 0L) {
                String.format(Locale.US, "%.2f", existingGoal.targetAmountCents / 100.0)
            } else {
                ""
            },
        )
    }
    var currentSavedText by remember(existingGoal) {
        mutableStateOf(
            if (existingGoal != null && existingGoal.currentSavedCents > 0L) {
                String.format(Locale.US, "%.2f", existingGoal.currentSavedCents / 100.0)
            } else {
                ""
            },
        )
    }
    var selectedCurrency by remember(existingGoal) {
        mutableStateOf(existingGoal?.currency ?: CurrencyCode.EUR)
    }
    var selectedPriority by remember(existingGoal) {
        mutableStateOf(existingGoal?.priority ?: GoalPriority.SHORT_TERM)
    }
    var selectedBucket by remember(existingGoal) {
        mutableStateOf<String?>(existingGoal?.bucketName)
    }
    var notes by remember(existingGoal) { mutableStateOf(existingGoal?.notes ?: "") }

    var showNewBucketField by remember { mutableStateOf(false) }
    var newBucketText by remember { mutableStateOf("") }
    var newBucketError by remember { mutableStateOf<String?>(null) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var targetError by remember { mutableStateOf<String?>(null) }

    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.sheetHorizontal, vertical = spacing.sheetVertical),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Text(
                text = if (isEditing) "Edit Goal / Target" else "New Goal / Target",
                style = typography.titleLarge,
                color = colors.onSurface,
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = { Text("Title (e.g., Emergency Fund, Trip) *") },
                isError = titleError != null,
                supportingText = titleError?.let { err -> { Text(err) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                OutlinedTextField(
                    value = targetText,
                    onValueChange = {
                        targetText = it
                        targetError = null
                    },
                    label = { Text("Target (${selectedCurrency.symbol}) *") },
                    isError = targetError != null,
                    supportingText = targetError?.let { err -> { Text(err) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = currentSavedText,
                    onValueChange = { currentSavedText = it },
                    label = { Text("Initial / Extra Saved (${selectedCurrency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                CurrencyCode.entries.forEach { cur ->
                    FilterChip(
                        selected = selectedCurrency == cur,
                        onClick = { selectedCurrency = cur },
                        label = { Text("${cur.code} ${cur.symbol}") },
                    )
                }
            }

            Text(
                text = "Link to an Envelope / Bucket (calculates progress and completion date)",
                style = typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.compactPillVertical),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                FilterChip(
                    selected = selectedBucket == null,
                    onClick = { selectedBucket = null },
                    label = { Text("No Bucket") },
                )

                val displayedBuckets = remember(availableBuckets, selectedBucket) {
                    buildList {
                        addAll(availableBuckets)
                        val cur = selectedBucket
                        if (!cur.isNullOrBlank() && none { it.equals(cur, ignoreCase = true) }) {
                            add(cur)
                        }
                    }
                }
                displayedBuckets.forEach { bucket ->
                    val bColor = bucketColorFor(bucket)
                    val isSelected = selectedBucket != null && selectedBucket.equals(bucket, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedBucket = bucket },
                        label = {
                            Text(
                                text = bucket,
                                style = if (isSelected) typography.labelLargeBold else typography.labelLargeMedium,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = bColor.copy(alpha = FinnerAlpha.selectedContainer),
                            selectedLabelColor = bColor,
                            labelColor = bColor,
                        ),
                        border = BorderStroke(
                            width = if (isSelected) sizes.borderSelected else sizes.borderThin,
                            color = if (isSelected) bColor else bColor.copy(alpha = FinnerAlpha.unselectedBorder),
                        ),
                    )
                }

                FilterChip(
                    selected = showNewBucketField,
                    onClick = { showNewBucketField = !showNewBucketField },
                    label = { Text("+ New Bucket") },
                )
            }

            if (showNewBucketField) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    OutlinedTextField(
                        value = newBucketText,
                        onValueChange = {
                            newBucketText = it
                            newBucketError = null
                        },
                        label = { Text("New bucket name *") },
                        isError = newBucketError != null,
                        supportingText = newBucketError?.let { err -> { Text(err) } },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = {
                            val cleanBucket = newBucketText.trim()
                            if (cleanBucket.isEmpty()) {
                                newBucketError = "Please enter the bucket name"
                            } else {
                                onAddBucket(cleanBucket)
                                selectedBucket = cleanBucket
                                newBucketText = ""
                                newBucketError = null
                                showNewBucketField = false
                            }
                        },
                        modifier = Modifier.padding(top = spacing.sm),
                    ) {
                        Text("Create")
                    }
                }
            }

            Text(
                text = "Priority / Timeframe",
                style = typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.compactPillVertical),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                GoalPriority.entries.forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { selectedPriority = priority },
                        label = { Text(priority.label) },
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                singleLine = true,
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
                        val cleanTitle = title.trim()
                        val targetCents = CurrencyConverter.parseToCents(targetText)
                        titleError = if (cleanTitle.isEmpty()) "Please enter the goal title" else null
                        targetError = if (targetCents == null || targetCents <= 0L) "Please enter a target greater than zero" else null

                        if (titleError == null && targetError == null && targetCents != null) {
                            val savedCents = CurrencyConverter.parseToCents(currentSavedText) ?: 0L
                            onSaveGoal(
                                cleanTitle,
                                targetCents,
                                savedCents.coerceAtLeast(0L),
                                selectedCurrency,
                                selectedPriority,
                                selectedBucket,
                                notes.trim(),
                            )
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
