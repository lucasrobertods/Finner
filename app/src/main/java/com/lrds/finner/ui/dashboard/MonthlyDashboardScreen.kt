package com.lrds.finner.ui.dashboard

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.lrds.finner.core.currency.CurrencyConverter
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.domain.model.ActiveInstallmentItem
import com.lrds.finner.domain.model.EnvelopeSummary
import com.lrds.finner.domain.model.MonthlyDashboardData
import com.lrds.finner.ui.dashboard.components.CircularQuickActionButton
import com.lrds.finner.ui.dashboard.components.FinnerSectionCard
import com.lrds.finner.ui.dashboard.components.MonthPillTabs
import com.lrds.finner.ui.dashboard.components.bucketColorFor
import com.lrds.finner.ui.dashboard.components.bucketIconFor
import com.lrds.finner.ui.dashboard.components.finnerTopAura
import com.lrds.finner.ui.navigation.FinnerRoute
import com.lrds.finner.ui.theme.FinnerNumericStyles
import com.lrds.finner.ui.theme.FinnerTheme
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

val MONTH_SHORT_LABELS = listOf(
    "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
    "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
)

val MONTH_FULL_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

@Composable
fun MonthlyDashboardScreen(
    selectedYear: Int,
    availableMonths: List<Int>,
    selectedMonth: Int,
    monthlyDataByMonth: Map<Int, MonthlyDashboardData>,
    onSelectMonth: (Int) -> Unit,
    onAddTransaction: () -> Unit,
    onEditExchangeRate: () -> Unit,
    onAddBucket: () -> Unit,
    onOpenDetails: (year: Int, month: Int, filter: MonthDetailsFilter) -> Unit,
    onToggleInstallmentPaid: (ActiveInstallmentItem) -> Unit,
    onEditInstallment: (ActiveInstallmentItem) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    reselectEvents: Flow<NavKey> = emptyFlow()
) {
    val safeAvailableMonths = remember(availableMonths) {
        availableMonths.ifEmpty { (1..12).toList() }
    }
    val initialPageIndex = remember(safeAvailableMonths, selectedMonth) {
        safeAvailableMonths.indexOf(selectedMonth).coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(
        initialPage = initialPageIndex,
        pageCount = { safeAvailableMonths.size }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedYear, safeAvailableMonths) {
        val targetIndex = safeAvailableMonths.indexOf(selectedMonth).coerceAtLeast(0)
        if (pagerState.currentPage != targetIndex && targetIndex < safeAvailableMonths.size) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage, safeAvailableMonths) {
        val monthNumber = safeAvailableMonths.getOrElse(pagerState.currentPage) { safeAvailableMonths.first() }
        onSelectMonth(monthNumber)
    }

    val activePageIndex = pagerState.currentPage.coerceIn(0, (safeAvailableMonths.size - 1).coerceAtLeast(0))
    val activeMonthNumber = safeAvailableMonths.getOrElse(activePageIndex) { selectedMonth }
    val visibleMonthLabels = remember(safeAvailableMonths) {
        safeAvailableMonths.map { m -> MONTH_SHORT_LABELS[(m - 1).coerceIn(0, 11)] }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .finnerTopAura(enabled = true)
            .testTag("screen_dashboard")
    ) {
        topBar()

        MonthPillTabs(
            months = visibleMonthLabels,
            selectedIndex = activePageIndex,
            onMonthSelected = { index ->
                coroutineScope.launch { pagerState.animateScrollToPage(index) }
            }
        )

        HorizontalPager(
            state = pagerState,
            key = { pageIndex -> safeAvailableMonths.getOrElse(pageIndex) { pageIndex } },
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val pageMonthNumber = safeAvailableMonths.getOrElse(pageIndex) { activeMonthNumber }
            val pageMonthZeroBased = (pageMonthNumber - 1).coerceIn(0, 11)
            val monthData = monthlyDataByMonth[pageMonthNumber]
            val pageMonthFullName = MONTH_FULL_NAMES[pageMonthZeroBased]
            if (monthData != null) {
                MonthlySummaryHomePage(
                    monthFullName = pageMonthFullName,
                    data = monthData,
                    isActivePage = pageIndex == activePageIndex,
                    reselectEvents = reselectEvents,
                    onAddTransaction = onAddTransaction,
                    onEditExchangeRate = onEditExchangeRate,
                    onAddBucket = onAddBucket,
                    onOpenDetails = { filter -> onOpenDetails(monthData.year, monthData.month, filter) },
                    onToggleInstallmentPaid = onToggleInstallmentPaid,
                    onEditInstallment = onEditInstallment
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading $pageMonthFullName...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// =============================================================================================
// MONTHLY SUMMARY HOME (Stitch "Finner 2027 - Hero First (Revolut Style)")
// =============================================================================================

@Composable
private fun MonthlySummaryHomePage(
    monthFullName: String,
    data: MonthlyDashboardData,
    isActivePage: Boolean,
    reselectEvents: Flow<NavKey>,
    onAddTransaction: () -> Unit,
    onEditExchangeRate: () -> Unit,
    onAddBucket: () -> Unit,
    onOpenDetails: (MonthDetailsFilter) -> Unit,
    onToggleInstallmentPaid: (ActiveInstallmentItem) -> Unit,
    onEditInstallment: (ActiveInstallmentItem) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(reselectEvents, isActivePage) {
        if (isActivePage) {
            reselectEvents.collect { route ->
                if (route == FinnerRoute.Dashboard) {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }

    val isCurrentMonth = remember(data.year, data.month) {
        val now = LocalDate.now()
        data.year == now.year && data.month == now.monthValue
    }

    val incomeCategories = remember(data.categoryReconciliations) {
        data.categoryReconciliations.filter { it.category.type == TransactionType.INCOME }
    }
    val localExpenseCategories = remember(data.categoryReconciliations) {
        data.categoryReconciliations.filter { it.category.type == TransactionType.EXPENSE_LOCAL }
    }
    val brazilCategories = remember(data.categoryReconciliations) {
        data.categoryReconciliations.filter { it.category.type == TransactionType.EXPENSE_BRAZIL }
    }

    val incomePlannedEur = remember(incomeCategories) { incomeCategories.sumOf { it.previstoEurCents } }
    val incomeActualEur = remember(incomeCategories) { incomeCategories.sumOf { it.realizadoEurCents } }
    val localPlannedEur = remember(localExpenseCategories) { localExpenseCategories.sumOf { it.previstoEurCents } }
    val localActualEur = remember(localExpenseCategories) { localExpenseCategories.sumOf { it.realizadoEurCents } }
    val brazilPlannedBrl = remember(brazilCategories) { brazilCategories.sumOf { it.previstoCents } }
    val brazilActualBrl = remember(brazilCategories) { brazilCategories.sumOf { it.realizadoCents } }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 124.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Revolut Hero Balance + Circular Quick Actions
        item(key = "hero_balance") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ACTUAL SPARE CASH · ${monthFullName.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = CurrencyConverter.formatCents(data.faltaSobraEurCents, CurrencyCode.EUR),
                    style = FinnerNumericStyles.heroAmount,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Planned Spare Cash Delta Pill
                val plannedPositive = data.plannedSpareCashEurCents >= 0L
                val plannedColor = if (plannedPositive) {
                    FinnerTheme.extendedColors.inflow
                } else {
                    FinnerTheme.extendedColors.outflow
                }
                val plannedContainer = if (plannedPositive) {
                    FinnerTheme.extendedColors.inflowContainer
                } else {
                    FinnerTheme.extendedColors.outflowContainer
                }
                Surface(
                    shape = CircleShape,
                    color = plannedContainer
                ) {
                    Text(
                        text = "Planned ${CurrencyConverter.formatCents(data.plannedSpareCashEurCents, CurrencyCode.EUR)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = plannedColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Revolut Circular Quick-Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularQuickActionButton(
                        icon = Icons.Default.Add,
                        label = "Add",
                        onClick = onAddTransaction,
                        emphasized = true
                    )
                    CircularQuickActionButton(
                        icon = Icons.Default.CurrencyExchange,
                        label = "Rate",
                        onClick = onEditExchangeRate
                    )
                    CircularQuickActionButton(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Bucket",
                        onClick = onAddBucket
                    )
                    CircularQuickActionButton(
                        icon = Icons.Default.FormatListBulleted,
                        label = "Details",
                        onClick = { onOpenDetails(MonthDetailsFilter()) }
                    )
                }
            }
        }

        // 2. Budget Envelopes Card
        item(key = "envelopes_card") {
            RevolutEnvelopeBucketsCard(
                envelopes = data.envelopeSummaries,
                onAddBucket = onAddBucket,
                onBucketClick = { bucketName ->
                    onOpenDetails(MonthDetailsFilter(initialBucket = bucketName))
                }
            )
        }

        // 3. Brazil Commitments & Installments Compact Preview
        item(key = "brazil_commitments_card") {
            RevolutBrazilCommitmentsPreviewCard(
                installments = data.activeInstallments,
                isCurrentMonth = isCurrentMonth,
                onSeeAll = {
                    onOpenDetails(MonthDetailsFilter(initialType = TransactionType.EXPENSE_BRAZIL))
                },
                onTogglePaid = onToggleInstallmentPaid,
                onEditInstallment = onEditInstallment
            )
        }

        // 4. Compact Categories Summary Card
        item(key = "categories_summary_card") {
            FinnerSectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Categories Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Planned vs Actual · Tap to view entries",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                CategorySummaryRow(
                    title = "Inflows (€)",
                    count = incomeCategories.size,
                    actualFormatted = CurrencyConverter.formatCents(incomeActualEur, CurrencyCode.EUR),
                    plannedFormatted = CurrencyConverter.formatCents(incomePlannedEur, CurrencyCode.EUR),
                    icon = Icons.Default.ArrowDownward,
                    accentColor = FinnerTheme.extendedColors.inflow,
                    onClick = { onOpenDetails(MonthDetailsFilter(initialType = TransactionType.INCOME)) }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = FinnerTheme.extendedColors.cardBorder
                )

                CategorySummaryRow(
                    title = "Outflows (€)",
                    count = localExpenseCategories.size,
                    actualFormatted = CurrencyConverter.formatCents(localActualEur, CurrencyCode.EUR),
                    plannedFormatted = CurrencyConverter.formatCents(localPlannedEur, CurrencyCode.EUR),
                    icon = Icons.Default.ArrowUpward,
                    accentColor = FinnerTheme.extendedColors.outflow,
                    onClick = { onOpenDetails(MonthDetailsFilter(initialType = TransactionType.EXPENSE_LOCAL)) }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = FinnerTheme.extendedColors.cardBorder
                )

                CategorySummaryRow(
                    title = "Brazil (R$)",
                    count = brazilCategories.size,
                    actualFormatted = CurrencyConverter.formatCents(brazilActualBrl, CurrencyCode.BRL),
                    plannedFormatted = CurrencyConverter.formatCents(brazilPlannedBrl, CurrencyCode.BRL),
                    icon = Icons.Default.Public,
                    accentColor = FinnerTheme.extendedColors.brazilAccent,
                    onClick = { onOpenDetails(MonthDetailsFilter(initialType = TransactionType.EXPENSE_BRAZIL)) }
                )
            }
        }
    }
}

@Composable
private fun RevolutEnvelopeBucketsCard(
    envelopes: List<EnvelopeSummary>,
    onAddBucket: () -> Unit,
    onBucketClick: (String) -> Unit
) {
    FinnerSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Budget Envelopes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Actual vs Planned · Tap to inspect",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "+ New",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onAddBucket)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (envelopes.isEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No budget envelopes configured yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                envelopes.forEach { envelope ->
                    val badgeColor = bucketColorFor(envelope.bucketName)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onBucketClick(envelope.bucketName) }
                            .padding(vertical = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = bucketIconFor(envelope.bucketName),
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = envelope.bucketName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = envelope.formattedPercentage,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyConverter.formatCents(envelope.actualEurCents, CurrencyCode.EUR),
                                    style = FinnerNumericStyles.rowAmount,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "of ${CurrencyConverter.formatCents(envelope.plannedEurCents, CurrencyCode.EUR)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (envelope.plannedEurCents < 0L) {
                                        FinnerTheme.extendedColors.outflow
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { envelope.usageFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = badgeColor,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RevolutBrazilCommitmentsPreviewCard(
    installments: List<ActiveInstallmentItem>,
    isCurrentMonth: Boolean,
    onSeeAll: () -> Unit,
    onTogglePaid: (ActiveInstallmentItem) -> Unit,
    onEditInstallment: (ActiveInstallmentItem) -> Unit
) {
    FinnerSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Brazil Commitments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (installments.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = FinnerTheme.extendedColors.inflowContainer
                    ) {
                        Text(
                            text = "${installments.size} Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = FinnerTheme.extendedColors.inflow,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (installments.isEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No active installment contracts scheduled for this month.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
            installments.take(3).forEachIndexed { idx, item ->
                if (idx > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = FinnerTheme.extendedColors.cardBorder
                    )
                }
                val badge = item.projection.badgeText
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onEditInstallment(item) }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = { onTogglePaid(item) },
                        enabled = isCurrentMonth || item.isPaid,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (item.isPaid) "Paid" else "Mark paid",
                            tint = if (item.isPaid) {
                                FinnerTheme.extendedColors.inflow
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.projection.contract.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Surface(
                                shape = CircleShape,
                                color = FinnerTheme.extendedColors.brazilContainer
                            ) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FinnerTheme.extendedColors.brazilAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = item.categoryName.ifBlank { "Installment" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyConverter.formatCents(item.amountOriginalCents, item.currency),
                            style = FinnerNumericStyles.rowAmount,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (item.currency == CurrencyCode.BRL) {
                            Text(
                                text = "≈ ${CurrencyConverter.formatCents(item.amountEurCents, CurrencyCode.EUR)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySummaryRow(
    title: String,
    count: Int,
    actualFormatted: String,
    plannedFormatted: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$count ${if (count == 1) "category" else "categories"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = actualFormatted,
                style = FinnerNumericStyles.rowAmount,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "of $plannedFormatted",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open $title",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(name = "Monthly Dashboard Light", showBackground = true)
@Preview(name = "Monthly Dashboard Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MonthlyDashboardScreenPreview() {
    val sampleData = MonthlyDashboardData(
        year = 2026,
        month = 11,
        eurToBrlRate = 5.50,
        lastUpdatedDate = LocalDate.of(2026, 11, 20),
        plannedIncomeEurCents = 5_000_00L,
        plannedOutflowsEurCents = 3_400_00L,
        plannedSpareCashEurCents = 1_600_00L,
        plannedSpareCashBrlCents = 8_800_00L,
        totalIncomeEurCents = 5_000_00L,
        totalLocalExpenseEurCents = 2_100_00L,
        totalBrazilExpenseBrlCents = 1_650_00L,
        totalBrazilExpenseEurCents = 300_00L,
        totalOutflowsEurCents = 2_400_00L,
        faltaSobraEurCents = 2_600_00L,
        faltaSobraBrlCents = 14_300_00L,
        activeInstallments = emptyList(),
        categoryReconciliations = emptyList(),
        envelopeSummaries = listOf(
            EnvelopeSummary(
                bucketName = "Bills",
                spentOfIncomePercentage = 40.0,
                plannedEurCents = 2_000_00L,
                actualEurCents = 1_650_00L,
                totalIncomeEurCents = 5_000_00L
            ),
            EnvelopeSummary(
                bucketName = "Reserve",
                spentOfIncomePercentage = 20.0,
                plannedEurCents = 1_000_00L,
                actualEurCents = 750_00L,
                totalIncomeEurCents = 5_000_00L
            )
        ),
        transactions = emptyList()
    )
    FinnerTheme {
        MonthlyDashboardScreen(
            selectedYear = 2026,
            availableMonths = listOf(10, 11, 12),
            selectedMonth = 11,
            monthlyDataByMonth = mapOf(11 to sampleData),
            onSelectMonth = {},
            onAddTransaction = {},
            onEditExchangeRate = {},
            onAddBucket = {},
            onOpenDetails = { _, _, _ -> },
            onToggleInstallmentPaid = {},
            onEditInstallment = {}
        )
    }
}
