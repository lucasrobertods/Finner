package com.lrds.finner.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.lrds.finner.core.currency.CurrencyConverter
import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.TransactionEntity
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.domain.model.ActiveInstallmentItem
import com.lrds.finner.domain.model.CategoryReconciliationRow
import com.lrds.finner.domain.model.MonthlyDashboardData
import com.lrds.finner.ui.dashboard.components.ExchangeRateChip
import com.lrds.finner.ui.dashboard.components.FinnerSectionCard
import com.lrds.finner.ui.dashboard.components.bucketColorFor
import com.lrds.finner.ui.theme.FinnerAlpha
import com.lrds.finner.ui.theme.FinnerTheme
import java.time.LocalDate

data class MonthDetailsFilter(
    val initialType: TransactionType? = null,
    val initialBucket: String? = null,
)

@Composable
fun MonthDetailsScreen(
    monthFullName: String,
    data: MonthlyDashboardData,
    availableBuckets: List<String>,
    initialFilter: MonthDetailsFilter,
    onBack: () -> Unit,
    onEditExchangeRate: () -> Unit,
    onAddTransaction: () -> Unit,
    onAddCategory: (TransactionType) -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onQuickAddForCategory: (CategoryEntity) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onEditInstallment: (ActiveInstallmentItem) -> Unit,
    onDeleteInstallment: (ActiveInstallmentItem) -> Unit,
    onToggleInstallmentPaid: (ActiveInstallmentItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedType by remember(initialFilter) { mutableStateOf(initialFilter.initialType) }
    var selectedBucket by remember(initialFilter) { mutableStateOf(initialFilter.initialBucket) }

    val isCurrentMonth = remember(data.year, data.month) {
        val now = LocalDate.now()
        data.year == now.year && data.month == now.monthValue
    }

    val incomeCount = remember(data.categoryReconciliations) {
        data.categoryReconciliations.count { it.category.type == TransactionType.INCOME }
    }
    val outflowCount = remember(data.categoryReconciliations) {
        data.categoryReconciliations.count { it.category.type == TransactionType.EXPENSE_LOCAL }
    }
    val brazilCount = remember(data.categoryReconciliations) {
        data.categoryReconciliations.count { it.category.type == TransactionType.EXPENSE_BRAZIL }
    }

    val filteredCategories = remember(data.categoryReconciliations, selectedType, selectedBucket) {
        data.categoryReconciliations.filter { row ->
            val typeMatch = selectedType == null || row.category.type == selectedType
            val bucketMatch = selectedBucket == null ||
                row.category.envelopeName.equals(selectedBucket, ignoreCase = true)
            typeMatch && bucketMatch
        }
    }

    val uncategorizedTransactions = remember(data.transactions, data.categoryReconciliations, selectedType, selectedBucket) {
        if (selectedBucket != null) {
            emptyList()
        } else {
            val matchedIds = data.categoryReconciliations
                .flatMap { it.transactions }
                .map { it.id }
                .toSet()
            data.transactions.filter { tx ->
                tx.id !in matchedIds && (selectedType == null || tx.type == selectedType)
            }
        }
    }

    val summaryPlannedEur = remember(filteredCategories) {
        filteredCategories.sumOf { it.previstoEurCents }
    }
    val summaryActualEur = remember(filteredCategories) {
        filteredCategories.sumOf { it.realizadoEurCents }
    }
    val summaryAvailableEur = summaryPlannedEur - summaryActualEur

    val filterSubtitle = buildString {
        append(
            when (selectedType) {
                null -> "All Categories"
                TransactionType.INCOME -> "Inflows (€)"
                TransactionType.EXPENSE_LOCAL -> "Outflows (€)"
                TransactionType.EXPENSE_BRAZIL -> "Brazil (R$)"
            },
        )
        if (!selectedBucket.isNullOrBlank()) {
            append(" · ")
            append(selectedBucket)
        }
    }

    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        // Sticky-style Revolut Detail Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.topBarVertical),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(
                onClick = onBack,
                shape = shapes.pill,
                color = colors.surfaceContainerHigh,
                border = BorderStroke(sizes.borderThin, colors.cardBorder),
                modifier = Modifier.size(sizes.iconButtonLg),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.onSurface,
                        modifier = Modifier.size(sizes.iconLg),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "$monthFullName ${data.year}",
                    style = typography.titleMediumBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = filterSubtitle.uppercase(),
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Surface(
                onClick = onAddTransaction,
                shape = shapes.pill,
                color = colors.inverseSurface,
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = spacing.pillHorizontal,
                        vertical = spacing.pillVertical,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = colors.inverseOnSurface,
                        modifier = Modifier.size(sizes.iconSm),
                    )
                    Text(
                        text = "Add",
                        style = typography.labelLargeBold,
                        color = colors.inverseOnSurface,
                    )
                }
            }
        }

        // Type Filter Pills (All / Inflows / Outflows / Brazil)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            item {
                DetailFilterPill(
                    label = "All",
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                )
            }
            item {
                DetailFilterPill(
                    label = "Inflows ($incomeCount)",
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { selectedType = TransactionType.INCOME },
                )
            }
            item {
                DetailFilterPill(
                    label = "Outflows ($outflowCount)",
                    selected = selectedType == TransactionType.EXPENSE_LOCAL,
                    onClick = { selectedType = TransactionType.EXPENSE_LOCAL },
                )
            }
            item {
                DetailFilterPill(
                    label = "Brazil ($brazilCount)",
                    selected = selectedType == TransactionType.EXPENSE_BRAZIL,
                    onClick = { selectedType = TransactionType.EXPENSE_BRAZIL },
                )
            }
        }

        // Bucket Filter Pills
        val bucketsToShow = remember(availableBuckets, data.envelopeSummaries) {
            (availableBuckets + data.envelopeSummaries.map { it.bucketName })
                .filter { it.isNotBlank() }
                .distinct()
        }
        if (bucketsToShow.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = spacing.screenHorizontal, vertical = spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                item {
                    BucketFilterPill(
                        label = "All Buckets",
                        dotColor = null,
                        selected = selectedBucket == null,
                        onClick = { selectedBucket = null },
                    )
                }
                items(bucketsToShow) { bucket ->
                    BucketFilterPill(
                        label = bucket,
                        dotColor = bucketColorFor(bucket),
                        selected = selectedBucket.equals(bucket, ignoreCase = true),
                        onClick = {
                            selectedBucket = if (selectedBucket.equals(bucket, ignoreCase = true)) null else bucket
                        },
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                top = spacing.md,
                bottom = spacing.detailListBottomClearance,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.itemGap),
        ) {
            // KPI Summary Strip (PLANNED | ACTUAL | AVAILABLE + Exchange Rate Chip)
            item {
                FinnerSectionCard(contentPadding = PaddingValues(spacing.cardPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        KpiMetricColumn(
                            label = "PLANNED",
                            amount = CurrencyConverter.formatCents(summaryPlannedEur, CurrencyCode.EUR),
                            valueColor = colors.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        KpiMetricColumn(
                            label = "ACTUAL",
                            amount = CurrencyConverter.formatCents(summaryActualEur, CurrencyCode.EUR),
                            valueColor = colors.onSurface,
                            alignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        )
                        val availColor = if (summaryAvailableEur >= 0L) {
                            colors.inflow
                        } else {
                            colors.outflow
                        }
                        KpiMetricColumn(
                            label = "AVAILABLE",
                            amount = CurrencyConverter.formatCents(summaryAvailableEur, CurrencyCode.EUR),
                            valueColor = availColor,
                            alignment = Alignment.End,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.md))
                    HorizontalDivider(color = colors.cardBorder)
                    Spacer(modifier = Modifier.height(spacing.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${filteredCategories.size} ${if (filteredCategories.size == 1) "category" else "categories"}",
                            style = typography.labelMedium,
                            color = colors.onSurfaceVariant,
                        )
                        ExchangeRateChip(
                            rate = data.eurToBrlRate,
                            onClick = onEditExchangeRate,
                        )
                    }
                }
            }

            if (filteredCategories.isEmpty()) {
                item {
                    FinnerSectionCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            Text(
                                text = "No categories match this filter",
                                style = typography.titleSmall,
                                color = colors.onSurface,
                            )
                            Text(
                                text = "Try clearing a filter or create a new category for $monthFullName.",
                                style = typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredCategories,
                    key = { it.category.id },
                ) { row ->
                    DetailCategoryGroupCard(
                        row = row,
                        eurToBrlRate = data.eurToBrlRate,
                        isCurrentMonth = isCurrentMonth,
                        onQuickAdd = { onQuickAddForCategory(row.category) },
                        onEditCategory = { onEditCategory(row.category) },
                        onDeleteCategory = { onDeleteCategory(row.category) },
                        onEditTransaction = onEditTransaction,
                        onDeleteTransaction = onDeleteTransaction,
                        onEditInstallment = onEditInstallment,
                        onDeleteInstallment = onDeleteInstallment,
                        onToggleInstallmentPaid = onToggleInstallmentPaid,
                    )
                }
            }

            // Uncategorized entries (if any)
            if (uncategorizedTransactions.isNotEmpty()) {
                item {
                    FinnerSectionCard {
                        Text(
                            text = "Uncategorized Entries",
                            style = typography.titleSmallBold,
                            color = colors.onSurface,
                        )
                        Spacer(modifier = Modifier.height(spacing.md))
                        uncategorizedTransactions.forEachIndexed { idx, tx ->
                            if (idx > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = spacing.sm),
                                    color = colors.cardBorder,
                                )
                            }
                            DetailTransactionRow(
                                transaction = tx,
                                eurToBrlRate = data.eurToBrlRate,
                                badgeColor = colors.secondary,
                                onEdit = { onEditTransaction(tx) },
                                onDelete = { onDeleteTransaction(tx) },
                            )
                        }
                    }
                }
            }

            // Clean "+ New Category" button at bottom
            item {
                val targetCategoryType = selectedType ?: TransactionType.EXPENSE_LOCAL
                Surface(
                    onClick = { onAddCategory(targetCategoryType) },
                    shape = shapes.actionButton,
                    color = colors.surfaceContainerHigh,
                    border = BorderStroke(sizes.borderThin, colors.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.md),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(sizes.iconMd),
                        )
                        Spacer(modifier = Modifier.width(spacing.compactPillVertical))
                        Text(
                            text = "New ${targetCategoryType.label} Category",
                            style = typography.labelLarge,
                            color = colors.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    Surface(
        onClick = onClick,
        shape = FinnerTheme.shapes.pill,
        color = if (selected) colors.inverseSurface else colors.surfaceContainerHigh,
        border = if (selected) null else BorderStroke(FinnerTheme.sizes.borderThin, colors.cardBorder),
    ) {
        Text(
            text = label,
            style = if (selected) {
                FinnerTheme.typography.labelMediumBold
            } else {
                FinnerTheme.typography.labelMediumMedium
            },
            color = if (selected) colors.inverseOnSurface else colors.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = spacing.pillHorizontal,
                vertical = spacing.pillVertical,
            ),
        )
    }
}

@Composable
private fun BucketFilterPill(
    label: String,
    dotColor: Color?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val activeAccent = dotColor ?: colors.primary

    Surface(
        onClick = onClick,
        shape = FinnerTheme.shapes.pill,
        color = if (selected) {
            activeAccent.copy(alpha = FinnerAlpha.selectedContainer)
        } else {
            colors.surfaceContainer
        },
        border = BorderStroke(
            width = sizes.borderThin,
            color = if (selected) {
                activeAccent.copy(alpha = FinnerAlpha.selectedBorder)
            } else {
                colors.cardBorder
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = spacing.compactPillHorizontal,
                vertical = spacing.compactPillVertical,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.compactPillVertical),
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(sizes.statusDot)
                        .clip(FinnerTheme.shapes.pill)
                        .background(dotColor),
                )
            }
            Text(
                text = label,
                style = if (selected) {
                    FinnerTheme.typography.labelSmallBold
                } else {
                    FinnerTheme.typography.labelSmallMedium
                },
                color = if (selected) colors.onSurface else colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun KpiMetricColumn(
    label: String,
    amount: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment,
        verticalArrangement = Arrangement.spacedBy(FinnerTheme.spacing.xs),
    ) {
        Text(
            text = label,
            style = FinnerTheme.typography.labelSmall,
            color = FinnerTheme.colors.onSurfaceVariant,
        )
        Text(
            text = amount,
            style = FinnerTheme.numericStyles.rowAmountBold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DetailCategoryGroupCard(
    row: CategoryReconciliationRow,
    eurToBrlRate: Double,
    isCurrentMonth: Boolean,
    onQuickAdd: () -> Unit,
    onEditCategory: () -> Unit,
    onDeleteCategory: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onEditInstallment: (ActiveInstallmentItem) -> Unit,
    onDeleteInstallment: (ActiveInstallmentItem) -> Unit,
    onToggleInstallmentPaid: (ActiveInstallmentItem) -> Unit,
) {
    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    var menuExpanded by remember { mutableStateOf(false) }
    val category = row.category
    val isBrazil = category.type == TransactionType.EXPENSE_BRAZIL
    val isIncome = category.type == TransactionType.INCOME
    val displayCurrency = if (isBrazil) CurrencyCode.BRL else CurrencyCode.EUR
    val accentColor = when {
        !category.envelopeName.isNullOrBlank() -> bucketColorFor(category.envelopeName)
        isIncome -> colors.inflow
        isBrazil -> colors.brazilAccent
        else -> colors.primary
    }

    val progressFraction = if (row.previstoCents > 0L) {
        (row.realizadoCents.toFloat() / row.previstoCents.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val remainingColor = when {
        isIncome && row.realizadoCents >= row.previstoCents -> colors.inflow
        !isIncome && row.disponivelCents >= 0L -> colors.inflow
        else -> colors.outflow
    }

    FinnerSectionCard(contentPadding = PaddingValues(spacing.cardPadding)) {
        // Group Header Row (Clean title + bucket badge + '+' + '•••' overflow)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Text(
                        text = category.name,
                        style = typography.titleSmallBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!category.envelopeName.isNullOrBlank()) {
                        Surface(
                            shape = shapes.pill,
                            color = accentColor.copy(alpha = FinnerAlpha.tintedContainer),
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = spacing.badgeHorizontal,
                                    vertical = spacing.badgeVertical,
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(sizes.statusDot)
                                        .clip(shapes.pill)
                                        .background(accentColor),
                                )
                                Text(
                                    text = category.envelopeName,
                                    style = typography.labelSmall,
                                    color = accentColor,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "${CurrencyConverter.formatCents(row.realizadoCents, displayCurrency)} of ${CurrencyConverter.formatCents(row.previstoCents, displayCurrency)} · Left ${CurrencyConverter.formatCents(row.disponivelCents, displayCurrency)}",
                    style = typography.bodySmall,
                    color = remainingColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onQuickAdd,
                    modifier = Modifier.size(sizes.iconButtonMd),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add entry to ${category.name}",
                        tint = colors.primary,
                        modifier = Modifier.size(sizes.iconLg),
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(sizes.iconButtonMd),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Category options",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(sizes.iconLg),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(sizes.iconMd),
                                )
                            },
                            text = { Text("Edit category") },
                            onClick = {
                                menuExpanded = false
                                onEditCategory()
                            },
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = colors.error,
                                    modifier = Modifier.size(sizes.iconMd),
                                )
                            },
                            text = { Text("Delete category", color = colors.error) },
                            onClick = {
                                menuExpanded = false
                                onDeleteCategory()
                            },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.sm))
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(sizes.progressBarSm)
                .clip(shapes.pill),
            color = accentColor,
            trackColor = colors.surfaceContainerHighest,
        )

        val hasItems = row.transactions.isNotEmpty() || row.installments.isNotEmpty()
        if (!hasItems) {
            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                text = "No entries recorded this month. Tap + to add one.",
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        } else {
            Spacer(modifier = Modifier.height(spacing.md))
            HorizontalDivider(color = colors.cardBorder)
            Spacer(modifier = Modifier.height(spacing.compactPillVertical))

            row.transactions.forEachIndexed { index, tx ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = spacing.xs),
                        color = colors.cardBorder.copy(alpha = FinnerAlpha.dividerSubtle),
                    )
                }
                DetailTransactionRow(
                    transaction = tx,
                    eurToBrlRate = eurToBrlRate,
                    badgeColor = accentColor,
                    onEdit = { onEditTransaction(tx) },
                    onDelete = { onDeleteTransaction(tx) },
                )
            }

            row.installments.forEachIndexed { index, inst ->
                if (row.transactions.isNotEmpty() || index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = spacing.xs),
                        color = colors.cardBorder.copy(alpha = FinnerAlpha.dividerSubtle),
                    )
                }
                DetailInstallmentRow(
                    item = inst,
                    isCurrentMonth = isCurrentMonth,
                    onTogglePaid = { onToggleInstallmentPaid(inst) },
                    onEdit = { onEditInstallment(inst) },
                    onDelete = { onDeleteInstallment(inst) },
                )
            }
        }
    }
}

@Composable
private fun DetailTransactionRow(
    transaction: TransactionEntity,
    eurToBrlRate: Double,
    badgeColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    var menuExpanded by remember { mutableStateOf(false) }
    val isIncome = transaction.type == TransactionType.INCOME
    val isBrazil = transaction.type == TransactionType.EXPENSE_BRAZIL
    val icon = when (transaction.type) {
        TransactionType.INCOME -> Icons.Default.ArrowDownward
        TransactionType.EXPENSE_BRAZIL -> Icons.Default.Public
        TransactionType.EXPENSE_LOCAL -> Icons.Default.ArrowUpward
    }
    val amountPrefix = if (isIncome) "+ " else "- "
    val amountColor = if (isIncome) colors.inflow else colors.onSurface
    val brlEquivalentCents = if (isBrazil && transaction.currency == CurrencyCode.EUR) {
        CurrencyConverter.eurCentsToBrlCents(transaction.amountCents, eurToBrlRate)
    } else {
        null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.rowItem)
            .clickable(onClick = onEdit)
            .padding(vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        // Circular Revolut-style icon badge
        Box(
            modifier = Modifier
                .size(sizes.iconBadgeMd)
                .clip(shapes.pill)
                .background(badgeColor.copy(alpha = FinnerAlpha.tintedContainer)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(sizes.iconMd),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Text(
                text = transaction.description,
                style = typography.bodyMediumSemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${transaction.date} · ${transaction.userDisplayName.ifBlank { "Shared" }}",
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Text(
                text = amountPrefix + CurrencyConverter.formatCents(transaction.amountCents, transaction.currency),
                style = FinnerTheme.numericStyles.rowAmountBold,
                color = amountColor,
                maxLines = 1,
            )
            if (brlEquivalentCents != null) {
                Text(
                    text = "≈ ${CurrencyConverter.formatCents(brlEquivalentCents, CurrencyCode.BRL)}",
                    style = FinnerTheme.numericStyles.metaAmount,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(sizes.iconButtonSm),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Entry actions",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(sizes.iconMd),
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(sizes.iconMd),
                        )
                    },
                    text = { Text("Edit entry") },
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier.size(sizes.iconMd),
                        )
                    },
                    text = { Text("Delete entry", color = colors.error) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailInstallmentRow(
    item: ActiveInstallmentItem,
    isCurrentMonth: Boolean,
    onTogglePaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    var menuExpanded by remember { mutableStateOf(false) }
    val installmentBadge = item.projection.badgeText

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.rowItem)
            .clickable(onClick = onEdit)
            .padding(vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        IconButton(
            onClick = onTogglePaid,
            enabled = isCurrentMonth || item.isPaid,
            modifier = Modifier.size(sizes.iconButtonMd),
        ) {
            Icon(
                imageVector = if (item.isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (item.isPaid) "Paid" else "Mark paid",
                tint = if (item.isPaid) colors.inflow else colors.onSurfaceVariant,
                modifier = Modifier.size(sizes.iconXl),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.compactPillVertical),
            ) {
                Text(
                    text = item.projection.contract.name,
                    style = typography.bodyMediumSemiBold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Surface(
                    shape = shapes.pill,
                    color = colors.brazilContainer,
                ) {
                    Text(
                        text = installmentBadge,
                        style = typography.labelSmall,
                        color = colors.brazilAccent,
                        modifier = Modifier.padding(
                            horizontal = spacing.badgeHorizontal,
                            vertical = spacing.badgeVertical,
                        ),
                    )
                }
            }
            Text(
                text = if (item.isPaid) "Paid installment" else "Scheduled installment",
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "- " + CurrencyConverter.formatCents(item.amountOriginalCents, item.currency),
                style = FinnerTheme.numericStyles.rowAmountBold,
                color = colors.onSurface,
            )
            if (item.currency == CurrencyCode.BRL) {
                Text(
                    text = "≈ ${CurrencyConverter.formatCents(item.amountEurCents, CurrencyCode.EUR)}",
                    style = FinnerTheme.numericStyles.metaAmount,
                    color = colors.onSurfaceVariant,
                )
            }
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(sizes.iconButtonSm),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Installment actions",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(sizes.iconMd),
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(sizes.iconMd),
                        )
                    },
                    text = { Text("Edit contract") },
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = colors.error,
                            modifier = Modifier.size(sizes.iconMd),
                        )
                    },
                    text = { Text("Delete contract", color = colors.error) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                )
            }
        }
    }
}
