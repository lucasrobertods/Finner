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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.EnvelopeType
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.ui.dashboard.components.bucketColorFor
import com.lrds.finner.ui.theme.FinnerAlpha
import com.lrds.finner.ui.theme.FinnerTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryDialog(
    monthLabel: String,
    selectedMonth: Int,
    selectedYear: Int,
    availableBuckets: List<String>,
    plannedIncomeEurCents: Long = 0L,
    eurToBrlRate: Double = CurrencyConverter.DEFAULT_EUR_TO_BRL_RATE,
    existingCategory: CategoryEntity? = null,
    initialType: TransactionType = TransactionType.EXPENSE_LOCAL,
    onDismiss: () -> Unit,
    onAddBucket: (String) -> Unit,
    onSaveCategory: (
        name: String,
        type: TransactionType,
        envelopeName: String?,
        plannedAmountCents: Long,
        plannedPercentage: Double?,
        currency: CurrencyCode,
        applyToSubsequentMonths: Boolean,
    ) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = existingCategory != null
    val fixedType = existingCategory?.type ?: initialType

    var name by remember(existingCategory) {
        mutableStateOf(existingCategory?.name ?: "")
    }
    var selectedBucket by remember(existingCategory, availableBuckets) {
        mutableStateOf(
            existingCategory?.envelopeName
                ?: availableBuckets.firstOrNull()
                ?: EnvelopeType.BILLS.displayName,
        )
    }
    val selectedCurrency = if (fixedType == TransactionType.EXPENSE_BRAZIL) {
        CurrencyCode.BRL
    } else {
        CurrencyCode.EUR
    }
    var usePercentageMode by remember(existingCategory, fixedType) {
        mutableStateOf(fixedType != TransactionType.INCOME && existingCategory?.plannedPercentage != null)
    }
    var plannedPercentageText by remember(existingCategory) {
        mutableStateOf(
            existingCategory?.plannedPercentage?.let { pct ->
                val roundedInt = kotlin.math.round(pct).toInt()
                if (kotlin.math.abs(pct - roundedInt) < 0.001) {
                    roundedInt.toString()
                } else {
                    String.format(Locale.US, "%.2f", pct)
                }
            } ?: "",
        )
    }
    var plannedAmountText by remember(existingCategory) {
        mutableStateOf(
            if (existingCategory != null && existingCategory.plannedAmountCents > 0L) {
                String.format(Locale.US, "%.2f", existingCategory.plannedAmountCents / 100.0)
            } else {
                ""
            },
        )
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var bucketError by remember { mutableStateOf<String?>(null) }
    var percentageError by remember { mutableStateOf<String?>(null) }

    var showNewBucketField by remember { mutableStateOf(false) }
    var newBucketText by remember { mutableStateOf("") }
    var newBucketError by remember { mutableStateOf<String?>(null) }
    var showSubsequentMonthsPrompt by remember { mutableStateOf(false) }

    val parsedPercentage: Double? = remember(plannedPercentageText) {
        plannedPercentageText.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 }
    }

    val plannedInflowsInSelectedCurrencyCents: Long = remember(plannedIncomeEurCents, selectedCurrency, eurToBrlRate) {
        when (selectedCurrency) {
            CurrencyCode.EUR -> plannedIncomeEurCents
            CurrencyCode.BRL -> CurrencyConverter.eurCentsToBrlCents(plannedIncomeEurCents, eurToBrlRate)
        }
    }

    val calculatedPercentageAmountCents: Long = remember(parsedPercentage, plannedInflowsInSelectedCurrencyCents) {
        val pct = parsedPercentage ?: 0.0
        kotlin.math.round(plannedInflowsInSelectedCurrencyCents.toDouble() * (pct / 100.0)).toLong()
    }

    fun validateFields(): Boolean {
        val cleanName = name.trim()
        nameError = if (cleanName.isEmpty()) "Please enter the category name" else null
        bucketError = if (fixedType != TransactionType.INCOME && selectedBucket.trim().isEmpty()) {
            "Please select a required bucket"
        } else {
            null
        }
        percentageError = if (fixedType != TransactionType.INCOME && usePercentageMode && parsedPercentage == null) {
            "Please enter a valid percentage greater than 0"
        } else {
            null
        }
        return nameError == null && bucketError == null && percentageError == null
    }

    fun commitSave(applyToSubsequentMonths: Boolean) {
        if (!validateFields()) return
        val cleanName = name.trim()
        val isPct = fixedType != TransactionType.INCOME && usePercentageMode
        val plannedCents = if (isPct) {
            calculatedPercentageAmountCents
        } else {
            CurrencyConverter.parseToCents(plannedAmountText) ?: 0L
        }
        val pctForSave = if (isPct) parsedPercentage else null
        val envelopeForSave = if (fixedType == TransactionType.INCOME) null else selectedBucket.trim()
        onSaveCategory(
            cleanName,
            fixedType,
            envelopeForSave,
            plannedCents.coerceAtLeast(0L),
            pctForSave,
            selectedCurrency,
            applyToSubsequentMonths,
        )
        onDismiss()
    }

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
                text = if (isEditing) {
                    "Edit Category (${fixedType.label}) — $monthLabel $selectedYear"
                } else {
                    "New Category (${fixedType.label}) — $monthLabel $selectedYear"
                },
                style = typography.titleLarge,
                color = colors.onSurface,
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError != null) nameError = null
                },
                label = { Text("Category Name *") },
                isError = nameError != null,
                supportingText = nameError?.let { err -> { Text(err) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Inflows do not have buckets associated
            if (fixedType != TransactionType.INCOME) {
                Text(
                    text = "Envelope / Bucket *",
                    style = typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.compactPillVertical),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    val displayedBuckets = remember(availableBuckets, selectedBucket) {
                        buildList {
                            addAll(availableBuckets)
                            if (selectedBucket.isNotBlank() && none { it.equals(selectedBucket, ignoreCase = true) }) {
                                add(selectedBucket)
                            }
                        }
                    }
                    displayedBuckets.forEach { bucket ->
                        val bColor = bucketColorFor(bucket)
                        val isSelected = selectedBucket.equals(bucket, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedBucket = bucket
                                bucketError = null
                            },
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

                if (bucketError != null) {
                    Text(
                        text = bucketError!!,
                        style = typography.bodySmall,
                        color = colors.error,
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
            }

            if (fixedType != TransactionType.INCOME) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surfaceContainerHighest,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = spacing.cardPaddingCompactHorizontal,
                            vertical = spacing.md,
                        ),
                        verticalArrangement = Arrangement.spacedBy(spacing.xxs),
                    ) {
                        Text(
                            text = "Planned Inflows ($monthLabel): ${CurrencyConverter.formatCents(plannedIncomeEurCents, CurrencyCode.EUR)}",
                            style = typography.bodyMediumSemiBold,
                            color = colors.inflow,
                        )
                        if (selectedCurrency == CurrencyCode.BRL) {
                            Text(
                                text = "In BRL: ${CurrencyConverter.formatCents(plannedInflowsInSelectedCurrencyCents, CurrencyCode.BRL)}",
                                style = FinnerTheme.numericStyles.metaAmount,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }

                Text(
                    text = "Planned Value Mode",
                    style = typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    FilterChip(
                        selected = !usePercentageMode,
                        onClick = {
                            usePercentageMode = false
                            percentageError = null
                        },
                        label = { Text("Fixed Value") },
                    )
                    FilterChip(
                        selected = usePercentageMode,
                        onClick = {
                            usePercentageMode = true
                            percentageError = null
                        },
                        label = { Text("% of Planned Inflows") },
                    )
                }
            }

            if (fixedType != TransactionType.INCOME && usePercentageMode) {
                OutlinedTextField(
                    value = plannedPercentageText,
                    onValueChange = {
                        plannedPercentageText = it
                        if (percentageError != null) percentageError = null
                    },
                    label = { Text("Percentage of Inflows (%) *") },
                    isError = percentageError != null,
                    supportingText = percentageError?.let { err -> { Text(err) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                OutlinedTextField(
                    value = plannedAmountText,
                    onValueChange = { plannedAmountText = it },
                    label = { Text("Planned Amount (${selectedCurrency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (fixedType != TransactionType.INCOME && usePercentageMode) {
                val pctDisplay = parsedPercentage?.let { pct ->
                    val roundedInt = kotlin.math.round(pct).toInt()
                    if (kotlin.math.abs(pct - roundedInt) < 0.001) "$roundedInt%" else String.format(Locale.US, "%.1f%%", pct)
                } ?: "0%"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.primaryContainer.copy(alpha = FinnerAlpha.previewContainer),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = spacing.cardPaddingCompactHorizontal,
                            vertical = spacing.md,
                        ),
                        verticalArrangement = Arrangement.spacedBy(spacing.xxs),
                    ) {
                        Text(
                            text = "Amount to be added ($pctDisplay of Inflows): ${CurrencyConverter.formatCents(calculatedPercentageAmountCents, selectedCurrency)}",
                            style = typography.bodyMediumBold,
                            color = colors.onPrimaryContainer,
                        )
                        Text(
                            text = "Automatically updates whenever Planned Inflows change.",
                            style = typography.labelSmall,
                            color = colors.onPrimaryContainer,
                        )
                    }
                }
            }

            if (showSubsequentMonthsPrompt) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.secondaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(spacing.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        Text(
                            text = if (isEditing) {
                                "Do you want to apply this change to subsequent months ($monthLabel to DEC) or only in $monthLabel?"
                            } else {
                                "Do you want to create this category in subsequent months ($monthLabel to DEC) or only in $monthLabel?"
                            },
                            style = typography.bodyMediumSemiBold,
                            color = colors.onSecondaryContainer,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            OutlinedButton(
                                onClick = { commitSave(applyToSubsequentMonths = false) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Only $monthLabel")
                            }
                            Button(
                                onClick = { commitSave(applyToSubsequentMonths = true) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("$monthLabel and following")
                            }
                        }
                    }
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
                            if (validateFields()) {
                                if (!isEditing) {
                                    // Every new category created is automatically replicated to all subsequent months
                                    commitSave(applyToSubsequentMonths = true)
                                } else if (selectedMonth < 12) {
                                    showSubsequentMonthsPrompt = true
                                } else {
                                    commitSave(applyToSubsequentMonths = false)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Save")
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.sheetBottomPadding))
        }
    }
}
