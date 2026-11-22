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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lrds.finner.core.currency.CurrencyConverter
import com.lrds.finner.core.model.HouseholdSession
import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.InstallmentContractEntity
import com.lrds.finner.data.local.entity.TransactionEntity
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.ui.dashboard.components.bucketColorFor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickTransactionModal(
    monthLabel: String,
    selectedMonth: Int,
    selectedYear: Int = 2026,
    session: HouseholdSession,
    categories: List<CategoryEntity>,
    preselectedCategory: CategoryEntity? = null,
    existingTransaction: TransactionEntity? = null,
    existingInstallmentContract: InstallmentContractEntity? = null,
    onDismiss: () -> Unit,
    onRequestCreateCategory: (TransactionType) -> Unit,
    onSaveSingleTransaction: (
        description: String,
        amountCents: Long,
        currency: CurrencyCode,
        categoryId: String?,
        type: TransactionType,
        userId: String,
        userDisplayName: String,
        applyToSubsequentMonths: Boolean
    ) -> Unit,
    onSaveInstallmentContract: (
        description: String,
        monthlyAmountCents: Long,
        currency: CurrencyCode,
        initialInstallment: Int,
        totalInstallments: Int,
        categoryId: String?,
        type: TransactionType,
        userId: String,
        userDisplayName: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditingTransaction = existingTransaction != null
    val isEditingInstallment = existingInstallmentContract != null
    val isEditing = isEditingTransaction || isEditingInstallment

    var isInstallment by remember(existingTransaction, existingInstallmentContract) {
        mutableStateOf(isEditingInstallment)
    }
    var selectedType by remember(existingTransaction, existingInstallmentContract, preselectedCategory) {
        mutableStateOf(
            existingTransaction?.type
                ?: existingInstallmentContract?.type
                ?: preselectedCategory?.type
                ?: TransactionType.EXPENSE_LOCAL
        )
    }
    val selectedCurrency = CurrencyCode.EUR
    var authorDisplayName by remember(existingTransaction, existingInstallmentContract, session.userDisplayName) {
        mutableStateOf(
            existingTransaction?.userDisplayName?.ifBlank { session.userDisplayName }
                ?: existingInstallmentContract?.userDisplayName?.ifBlank { session.userDisplayName }
                ?: session.userDisplayName
        )
    }
    var description by remember(existingTransaction, existingInstallmentContract, preselectedCategory) {
        mutableStateOf(
            existingTransaction?.description
                ?: existingInstallmentContract?.name
                ?: preselectedCategory?.name
                ?: ""
        )
    }
    var amountText by remember(existingTransaction, existingInstallmentContract) {
        val initialCents = existingTransaction?.amountCents
            ?: existingInstallmentContract?.monthlyAmountCents
        mutableStateOf(
            if (initialCents != null && initialCents > 0L) {
                String.format(Locale.US, "%.2f", initialCents / 100.0)
            } else {
                ""
            }
        )
    }
    var initialInstallmentText by remember(existingInstallmentContract) {
        mutableStateOf((existingInstallmentContract?.initialInstallmentNumber ?: 1).toString())
    }
    var totalInstallmentsText by remember(existingInstallmentContract) {
        mutableStateOf((existingInstallmentContract?.totalInstallments ?: 3).toString())
    }

    val matchingCategories = categories
        .filter { it.year == selectedYear && it.month == selectedMonth && it.type == selectedType }
        .sortedWith(
            compareBy<CategoryEntity, String>(String.CASE_INSENSITIVE_ORDER) { it.envelopeName ?: "" }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        )

    // Unselected by default unless editing or opened from a category item's '+' button
    var selectedCategoryId by remember(
        existingTransaction,
        existingInstallmentContract,
        preselectedCategory,
        selectedYear,
        selectedMonth,
        selectedType
    ) {
        val editingCatId = existingTransaction?.categoryId ?: existingInstallmentContract?.categoryId
        val editingType = existingTransaction?.type ?: existingInstallmentContract?.type
        mutableStateOf(
            when {
                editingType == selectedType && editingCatId != null -> {
                    if (matchingCategories.any { it.id == editingCatId }) {
                        editingCatId
                    } else {
                        val referencedCat = categories.firstOrNull { it.id == editingCatId }
                        matchingCategories.firstOrNull {
                            referencedCat != null &&
                                (it.seriesId == referencedCat.seriesId ||
                                    it.name.equals(referencedCat.name, ignoreCase = true))
                        }?.id ?: editingCatId
                    }
                }
                preselectedCategory != null && preselectedCategory.type == selectedType -> preselectedCategory.id
                else -> null
            }
        )
    }

    var descriptionError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var installmentError by remember { mutableStateOf<String?>(null) }
    var showSubsequentMonthsPrompt by remember { mutableStateOf(false) }

    fun validateForm(): Boolean {
        val cleanDesc = description.trim()
        descriptionError = if (cleanDesc.isEmpty()) "Please enter a description" else null

        val cents = CurrencyConverter.parseToCents(amountText)
        amountError = if (cents == null || cents <= 0L) "Please enter a valid amount greater than zero" else null

        categoryError = if (selectedCategoryId == null) "Please select a required category" else null

        if (isInstallment) {
            val initNum = initialInstallmentText.toIntOrNull()
            val totalNum = totalInstallmentsText.toIntOrNull()
            installmentError = if (initNum == null || totalNum == null || initNum < 1 || totalNum < initNum) {
                "Please enter valid installments (e.g., 1 of 3)"
            } else {
                null
            }
        } else {
            installmentError = null
        }

        return descriptionError == null &&
            amountError == null &&
            categoryError == null &&
            installmentError == null
    }

    fun commitSave(applyToSubsequentMonths: Boolean) {
        if (!validateForm()) return
        val cents = CurrencyConverter.parseToCents(amountText) ?: return
        val cleanDesc = description.trim()
        val cleanMemberName = authorDisplayName.trim().ifEmpty { session.userDisplayName }

        if (isInstallment && (!isEditing || isEditingInstallment)) {
            val initNum = initialInstallmentText.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val totalNum = totalInstallmentsText.toIntOrNull()?.coerceAtLeast(initNum) ?: 3
            onSaveInstallmentContract(
                cleanDesc,
                cents,
                selectedCurrency,
                initNum,
                totalNum,
                selectedCategoryId,
                selectedType,
                session.userId,
                cleanMemberName
            )
        } else {
            onSaveSingleTransaction(
                cleanDesc,
                cents,
                selectedCurrency,
                selectedCategoryId,
                selectedType,
                existingTransaction?.userId ?: session.userId,
                cleanMemberName,
                applyToSubsequentMonths
            )
        }
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isEditing) {
                    "Edit Entry — $monthLabel $selectedYear"
                } else {
                    "New Entry — $monthLabel $selectedYear"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Entry Mode: Single Transaction vs Multi-Month Installment Contract (only when creating)
            if (!isEditing) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !isInstallment,
                        onClick = {
                            isInstallment = false
                            showSubsequentMonthsPrompt = false
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Single")
                    }
                    SegmentedButton(
                        selected = isInstallment,
                        onClick = {
                            isInstallment = true
                            showSubsequentMonthsPrompt = false
                            if (selectedType == TransactionType.INCOME) {
                                selectedType = TransactionType.EXPENSE_BRAZIL
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Installment (1/N)")
                    }
                }
            }

            // Transaction Type: INCOME, EXPENSE_LOCAL, EXPENSE_BRAZIL
            val availableTypes = if (isInstallment) {
                listOf(TransactionType.EXPENSE_BRAZIL, TransactionType.EXPENSE_LOCAL)
            } else {
                TransactionType.entries
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                availableTypes.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = type
                            showSubsequentMonthsPrompt = false
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = availableTypes.size
                        )
                    ) {
                        Text(type.label)
                    }
                }
            }

            // Description + Amount (EUR €)
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    if (descriptionError != null) descriptionError = null
                },
                label = {
                    Text(
                        if (isInstallment) "Contract / Installment Description *"
                        else "Description (e.g., Groceries, Salary) *"
                    )
                },
                isError = descriptionError != null,
                supportingText = descriptionError?.let { err -> { Text(err) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    if (amountError != null) amountError = null
                },
                label = {
                    Text(
                        if (isInstallment) "Installment Amount (€) *"
                        else "Amount (€) *"
                    )
                },
                isError = amountError != null,
                supportingText = amountError?.let { err -> { Text(err) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (isInstallment && (!isEditing || isEditingInstallment)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = initialInstallmentText,
                        onValueChange = {
                            initialInstallmentText = it.filter(Char::isDigit)
                            installmentError = null
                        },
                        label = {
                            Text(
                                if (isEditingInstallment) "Initial Installment *"
                                else "Installment in $monthLabel *"
                            )
                        },
                        isError = installmentError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalInstallmentsText,
                        onValueChange = {
                            totalInstallmentsText = it.filter(Char::isDigit)
                            installmentError = null
                        },
                        label = { Text("Total Installments *") },
                        isError = installmentError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (installmentError != null) {
                    Text(
                        text = installmentError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Dynamic Household Member attribution
            OutlinedTextField(
                value = authorDisplayName,
                onValueChange = { authorDisplayName = it },
                label = { Text("Responsible / Family Member") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Category selector with bucket colors
            Text(
                text = if (selectedType == TransactionType.INCOME) "Category *" else "Category & Envelope *",
                style = MaterialTheme.typography.labelLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                matchingCategories.forEach { category ->
                    val isSelected = selectedCategoryId == category.id
                    val hasBucket = category.type != TransactionType.INCOME && !category.envelopeName.isNullOrBlank()
                    val chipColor = if (hasBucket) bucketColorFor(category.envelopeName) else MaterialTheme.colorScheme.primary
                    val chipLabel = if (!hasBucket) {
                        category.name
                    } else {
                        "${category.name} (${category.envelopeName})"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategoryId = category.id
                            categoryError = null
                        },
                        label = {
                            Text(
                                text = chipLabel,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = if (hasBucket) {
                            FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor.copy(alpha = 0.22f),
                                selectedLabelColor = chipColor,
                                labelColor = chipColor
                            )
                        } else {
                            FilterChipDefaults.filterChipColors()
                        },
                        border = if (hasBucket) {
                            BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) chipColor else chipColor.copy(alpha = 0.5f)
                            )
                        } else {
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected
                            )
                        }
                    )
                }
                FilterChip(
                    selected = false,
                    onClick = { onRequestCreateCategory(selectedType) },
                    label = { Text("+ New Category") }
                )
            }
            if (categoryError != null) {
                Text(
                    text = categoryError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (showSubsequentMonthsPrompt) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isEditing) {
                                "Do you want to apply this change to subsequent months ($monthLabel to DEC) or only in $monthLabel?"
                            } else {
                                "Do you want to add this inflow to subsequent months ($monthLabel to DEC) or only in $monthLabel? (Each month remains independent for later edits)"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { commitSave(applyToSubsequentMonths = false) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Only $monthLabel")
                            }
                            Button(
                                onClick = { commitSave(applyToSubsequentMonths = true) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("$monthLabel and following")
                            }
                        }
                    }
                }
            } else {
                Button(
                    onClick = {
                        if (validateForm()) {
                            val shouldAskSubsequent = !isInstallment &&
                                selectedMonth < 12 &&
                                (isEditing || selectedType == TransactionType.INCOME)
                            if (shouldAskSubsequent) {
                                showSubsequentMonthsPrompt = true
                            } else {
                                commitSave(applyToSubsequentMonths = false)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            isEditing -> "Save Changes"
                            isInstallment -> "Save Installment Contract ($selectedMonth/$selectedYear)"
                            else -> "Save Entry"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
