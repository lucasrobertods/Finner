package com.lrds.finner.ui.dashboard

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lrds.finner.core.currency.CurrencyConverter
import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.GoalEntity
import com.lrds.finner.data.local.entity.InstallmentContractEntity
import com.lrds.finner.data.local.entity.TransactionEntity
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.data.remote.CloudSyncState
import com.lrds.finner.domain.model.ActiveInstallmentItem
import com.lrds.finner.domain.model.EnvelopeSummary
import com.lrds.finner.domain.model.MonthlyDashboardData
import com.lrds.finner.ui.dashboard.components.CircularQuickActionButton
import com.lrds.finner.ui.dashboard.components.ExchangeRateChip
import com.lrds.finner.ui.dashboard.components.FinnerDestination
import com.lrds.finner.ui.dashboard.components.FinnerFloatingScaffold
import com.lrds.finner.ui.dashboard.components.FinnerSectionCard
import com.lrds.finner.ui.dashboard.components.FinnerTopBar
import com.lrds.finner.ui.dashboard.components.MonthPillTabs
import com.lrds.finner.ui.dashboard.components.bucketColorFor
import com.lrds.finner.ui.dashboard.components.bucketIconFor
import com.lrds.finner.ui.theme.FinnerAlpha
import com.lrds.finner.ui.theme.FinnerTheme
import com.lrds.finner.ui.transaction.BucketDialog
import com.lrds.finner.ui.transaction.CategoryDialog
import com.lrds.finner.ui.transaction.ConfirmDeleteBottomSheet
import com.lrds.finner.ui.transaction.ExchangeRateDialog
import com.lrds.finner.ui.transaction.GoalDialog
import com.lrds.finner.ui.transaction.HouseholdSyncDialog
import com.lrds.finner.ui.transaction.QuickTransactionModal
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.ceil
import kotlinx.coroutines.launch

val MONTH_SHORT_LABELS = listOf(
    "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
    "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
)

val MONTH_FULL_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private data class PendingDeleteAction(
    val title: String,
    val message: String,
    val confirmLabel: String = "Delete",
    val confirmAllLabel: String? = null,
    val onConfirm: () -> Unit,
    val onConfirmAll: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyDashboardScreen(
    viewModel: MonthlyDashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableMonths = remember(uiState.availableMonths) {
        uiState.availableMonths.ifEmpty { (1..12).toList() }
    }
    val initialPageIndex = remember(availableMonths, uiState.selectedMonth) {
        availableMonths.indexOf(uiState.selectedMonth).coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(
        initialPage = initialPageIndex,
        pageCount = { availableMonths.size }
    )
    val coroutineScope = rememberCoroutineScope()

    var destination by rememberSaveable { mutableStateOf(FinnerDestination.MESES) }
    var activeDetailsFilter by remember { mutableStateOf<MonthDetailsFilter?>(null) }

    var showTransactionModal by remember { mutableStateOf(false) }
    var preselectedCategoryForModal by remember { mutableStateOf<CategoryEntity?>(null) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var editingInstallmentContract by remember { mutableStateOf<InstallmentContractEntity?>(null) }
    var showExchangeRateDialog by remember { mutableStateOf(false) }
    var showHouseholdSyncDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var initialCategoryType by remember { mutableStateOf(TransactionType.EXPENSE_LOCAL) }
    var showBucketDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<GoalEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<PendingDeleteAction?>(null) }

    LaunchedEffect(uiState.selectedYear, availableMonths) {
        val targetIndex = availableMonths.indexOf(uiState.selectedMonth).coerceAtLeast(0)
        if (pagerState.currentPage != targetIndex && targetIndex < availableMonths.size) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage, availableMonths) {
        val monthNumber = availableMonths.getOrElse(pagerState.currentPage) { availableMonths.first() }
        viewModel.selectMonth(monthNumber)
    }

    BackHandler(enabled = activeDetailsFilter != null) {
        activeDetailsFilter = null
    }

    val activePageIndex = pagerState.currentPage.coerceIn(0, (availableMonths.size - 1).coerceAtLeast(0))
    val activeMonthNumber = availableMonths.getOrElse(activePageIndex) { uiState.selectedMonth }
    val activeMonthZeroBased = (activeMonthNumber - 1).coerceIn(0, 11)
    val activeMonthLabel = MONTH_SHORT_LABELS[activeMonthZeroBased]
    val activeMonthFullName = MONTH_FULL_NAMES[activeMonthZeroBased]
    val activeMonthData = uiState.monthlyDataByMonth[activeMonthNumber]
    val isCloudConnected = uiState.cloudSyncState == CloudSyncState.CLOUD_CONNECTED
    val visibleMonthLabels = remember(availableMonths) {
        availableMonths.map { m -> MONTH_SHORT_LABELS[(m - 1).coerceIn(0, 11)] }
    }

    val openNewTransaction: (CategoryEntity?) -> Unit = { preselected ->
        preselectedCategoryForModal = preselected
        editingTransaction = null
        editingInstallmentContract = null
        showTransactionModal = true
    }

    val requestDeleteCategory: (CategoryEntity, String) -> Unit = { category, monthName ->
        pendingDelete = PendingDeleteAction(
            title = "Delete Category",
            message = "Do you want to delete the category \"${category.name}\" only in $monthName ${uiState.selectedYear}, or from all months and years?\nAll entries in this category will also be deleted.",
            confirmLabel = "Only $monthName",
            confirmAllLabel = "All months & years",
            onConfirm = { viewModel.deleteCategory(category.id, deleteFromAllMonthsAndYears = false) },
            onConfirmAll = { viewModel.deleteCategory(category.id, deleteFromAllMonthsAndYears = true) }
        )
    }

    val requestDeleteTransaction: (TransactionEntity) -> Unit = { tx ->
        val txMonthName = MONTH_FULL_NAMES[(tx.month - 1).coerceIn(0, 11)]
        pendingDelete = PendingDeleteAction(
            title = "Delete Entry",
            message = "Do you want to delete the entry \"${tx.description}\" (${CurrencyConverter.formatCents(tx.amountCents, tx.currency)}) only in $txMonthName ${tx.year}, or from all months and years?",
            confirmLabel = "Only $txMonthName",
            confirmAllLabel = "All months & years",
            onConfirm = { viewModel.deleteTransaction(tx.id, deleteFromAllMonthsAndYears = false) },
            onConfirmAll = { viewModel.deleteTransaction(tx.id, deleteFromAllMonthsAndYears = true) }
        )
    }

    val requestDeleteInstallment: (ActiveInstallmentItem) -> Unit = { item ->
        pendingDelete = PendingDeleteAction(
            title = "Delete Installment Contract",
            message = "Are you sure you want to delete the installment contract \"${item.projection.contract.name}\"?",
            onConfirm = { viewModel.deleteInstallmentContract(item.projection.contract.id) }
        )
    }

    // Drill-down Month Details & Transactions Screen
    val detailsFilter = activeDetailsFilter
    if (detailsFilter != null && activeMonthData != null) {
        MonthDetailsScreen(
            monthFullName = activeMonthFullName,
            data = activeMonthData,
            availableBuckets = uiState.bucketNames,
            initialFilter = detailsFilter,
            onBack = { activeDetailsFilter = null },
            onEditExchangeRate = { showExchangeRateDialog = true },
            onAddTransaction = { openNewTransaction(null) },
            onAddCategory = { type ->
                editingCategory = null
                initialCategoryType = type
                showCategoryDialog = true
            },
            onEditCategory = { category ->
                editingCategory = category
                initialCategoryType = category.type
                showCategoryDialog = true
            },
            onDeleteCategory = { category -> requestDeleteCategory(category, activeMonthFullName) },
            onQuickAddForCategory = { category -> openNewTransaction(category) },
            onEditTransaction = { tx ->
                preselectedCategoryForModal = null
                editingTransaction = tx
                editingInstallmentContract = null
                showTransactionModal = true
            },
            onDeleteTransaction = requestDeleteTransaction,
            onEditInstallment = { item ->
                preselectedCategoryForModal = null
                editingTransaction = null
                editingInstallmentContract = item.projection.contract
                showTransactionModal = true
            },
            onDeleteInstallment = requestDeleteInstallment,
            onToggleInstallmentPaid = viewModel::toggleInstallmentPaid,
            modifier = modifier
        )
    } else {
        FinnerFloatingScaffold(
            modifier = modifier,
            topBar = {
                FinnerTopBar(
                    selectedYear = uiState.selectedYear,
                    availableYears = uiState.availableYears,
                    onYearSelected = viewModel::selectYear,
                    onAddNextYear = viewModel::addNextYear,
                    onDeleteYear = { yearToDelete ->
                        pendingDelete = PendingDeleteAction(
                            title = "Delete Year $yearToDelete",
                            message = "Are you sure you want to delete all data (categories, entries, and exchange rates) for year $yearToDelete?",
                            onConfirm = { viewModel.deleteYear(yearToDelete) }
                        )
                    },
                    subtitle = "${uiState.session.householdName} · EUR / BRL",
                    trailingContent = if (destination == FinnerDestination.MESES && activeMonthData != null) {
                        {
                            ExchangeRateChip(
                                rate = activeMonthData.eurToBrlRate,
                                onClick = { showExchangeRateDialog = true }
                            )
                        }
                    } else {
                        null
                    }
                )
            },
            selectedDestination = destination,
            onDestinationSelected = { target ->
                activeDetailsFilter = null
                destination = target
                when (target) {
                    FinnerDestination.MESES -> viewModel.selectSection(AppSection.MONTHLY)
                    FinnerDestination.OBJETIVOS -> viewModel.selectSection(AppSection.GOALS)
                    else -> Unit
                }
            },
            fabLabel = null,
            onFabClick = {},
            showTopAura = destination == FinnerDestination.MESES
        ) {
            when (destination) {
                FinnerDestination.MESES -> Column(modifier = Modifier.fillMaxSize()) {
                    MonthPillTabs(
                        months = visibleMonthLabels,
                        selectedIndex = activePageIndex,
                        onMonthSelected = { index ->
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        }
                    )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        val pageMonthNumber = availableMonths.getOrElse(pageIndex) { activeMonthNumber }
                        val pageMonthZeroBased = (pageMonthNumber - 1).coerceIn(0, 11)
                        val monthData = uiState.monthlyDataByMonth[pageMonthNumber]
                        val pageMonthFullName = MONTH_FULL_NAMES[pageMonthZeroBased]
                        if (monthData != null) {
                            MonthlySummaryHomePage(
                                monthFullName = pageMonthFullName,
                                data = monthData,
                                onAddTransaction = { openNewTransaction(null) },
                                onEditExchangeRate = { showExchangeRateDialog = true },
                                onAddBucket = { showBucketDialog = true },
                                onOpenDetails = { filter -> activeDetailsFilter = filter },
                                onToggleInstallmentPaid = viewModel::toggleInstallmentPaid,
                                onEditInstallment = { item ->
                                    preselectedCategoryForModal = null
                                    editingTransaction = null
                                    editingInstallmentContract = item.projection.contract
                                    showTransactionModal = true
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Loading $pageMonthFullName...",
                                    style = FinnerTheme.typography.bodyMedium,
                                    color = FinnerTheme.colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                FinnerDestination.OBJETIVOS -> GoalsSectionPage(
                    goals = uiState.goals,
                    selectedYear = uiState.selectedYear,
                    selectedMonth = activeMonthNumber,
                    monthlyDataByMonth = uiState.monthlyDataByMonth,
                    onAddGoal = {
                        editingGoal = null
                        showGoalDialog = true
                    },
                    onIncrementGoal = { goal -> viewModel.addSavingsToGoal(goal, 100_00L) },
                    onEditGoal = { goal ->
                        editingGoal = goal
                        showGoalDialog = true
                    },
                    onDeleteGoal = { goal ->
                        pendingDelete = PendingDeleteAction(
                            title = "Delete Goal",
                            message = "Are you sure you want to delete the goal \"${goal.title}\"?",
                            onConfirm = { viewModel.deleteGoal(goal.id) }
                        )
                    }
                )

                FinnerDestination.HISTORICO -> HistorySectionPage(
                    transactions = uiState.monthlyDataByMonth.values
                        .flatMap { it.transactions }
                        .sortedByDescending { it.date },
                    onEditTransaction = { tx ->
                        preselectedCategoryForModal = null
                        editingTransaction = tx
                        editingInstallmentContract = null
                        showTransactionModal = true
                    },
                    onDeleteTransaction = requestDeleteTransaction
                )

                FinnerDestination.PERFIL -> ProfileSectionPage(
                    selectedYear = uiState.selectedYear,
                    userDisplayName = uiState.session.userDisplayName,
                    userEmail = uiState.session.userEmail,
                    householdName = uiState.session.householdName,
                    householdId = uiState.session.householdId,
                    isCloudConnected = isCloudConnected,
                    selectedUserFilter = uiState.selectedUserFilter,
                    currentUserId = uiState.session.userId,
                    onUserFilterChange = viewModel::selectUserFilter,
                    onManageAccount = { showHouseholdSyncDialog = true }
                )
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // Modals & Dialogs (100% preserved wiring)
    // -----------------------------------------------------------------------------------------

    if (showHouseholdSyncDialog) {
        HouseholdSyncDialog(
            session = uiState.session,
            configuredWebClientId = viewModel.getConfiguredWebClientId(),
            feedbackMessage = uiState.authFeedbackMessage,
            onDismiss = {
                viewModel.clearAuthFeedback()
                showHouseholdSyncDialog = false
            },
            onSaveProfileAndWorkspace = viewModel::updateProfileAndWorkspace,
            onSignInWithFirebaseEmail = viewModel::signInWithEmail,
            onSignInWithGoogle = viewModel::signInWithGoogle,
            onSignOut = viewModel::signOut
        )
    }

    if (showExchangeRateDialog && activeMonthData != null) {
        ExchangeRateDialog(
            monthLabel = activeMonthFullName,
            selectedYear = uiState.selectedYear,
            currentRate = activeMonthData.eurToBrlRate,
            onDismiss = { showExchangeRateDialog = false },
            onConfirmRate = { newRate ->
                viewModel.updateExchangeRate(activeMonthNumber, newRate)
            }
        )
    }

    if (showBucketDialog) {
        BucketDialog(
            onDismiss = { showBucketDialog = false },
            onSaveBucket = viewModel::addBudgetBucket
        )
    }

    if (showCategoryDialog) {
        CategoryDialog(
            monthLabel = activeMonthLabel,
            selectedMonth = activeMonthNumber,
            selectedYear = uiState.selectedYear,
            availableBuckets = uiState.bucketNames,
            plannedIncomeEurCents = activeMonthData?.plannedIncomeEurCents ?: 0L,
            eurToBrlRate = activeMonthData?.eurToBrlRate ?: CurrencyConverter.DEFAULT_EUR_TO_BRL_RATE,
            existingCategory = editingCategory,
            initialType = initialCategoryType,
            onDismiss = {
                showCategoryDialog = false
                editingCategory = null
            },
            onAddBucket = viewModel::addBudgetBucket,
            onSaveCategory = { name, type, envName, plannedCents, plannedPct, currency, applyToSubsequent ->
                viewModel.saveCategory(
                    existingCategory = editingCategory,
                    month = activeMonthNumber,
                    name = name,
                    type = type,
                    envelopeName = envName,
                    plannedAmountCents = plannedCents,
                    plannedPercentage = plannedPct,
                    currency = currency,
                    applyToSubsequentMonths = applyToSubsequent
                )
            }
        )
    }

    if (showGoalDialog) {
        GoalDialog(
            availableBuckets = uiState.bucketNames,
            existingGoal = editingGoal,
            onDismiss = {
                showGoalDialog = false
                editingGoal = null
            },
            onAddBucket = viewModel::addBudgetBucket,
            onSaveGoal = { title, targetCents, savedCents, currency, priority, bucketName, notes ->
                viewModel.saveGoal(
                    existingGoal = editingGoal,
                    title = title,
                    targetAmountCents = targetCents,
                    currentSavedCents = savedCents,
                    currency = currency,
                    priority = priority,
                    bucketName = bucketName,
                    notes = notes
                )
            }
        )
    }

    if (showTransactionModal) {
        val modalMonthNumber = editingTransaction?.month ?: activeMonthNumber
        val modalMonthLabel = MONTH_SHORT_LABELS[(modalMonthNumber - 1).coerceIn(0, 11)]
        QuickTransactionModal(
            monthLabel = modalMonthLabel,
            selectedMonth = modalMonthNumber,
            selectedYear = uiState.selectedYear,
            session = uiState.session,
            categories = uiState.categories,
            preselectedCategory = preselectedCategoryForModal,
            existingTransaction = editingTransaction,
            existingInstallmentContract = editingInstallmentContract,
            onDismiss = {
                showTransactionModal = false
                preselectedCategoryForModal = null
                editingTransaction = null
                editingInstallmentContract = null
            },
            onRequestCreateCategory = { type ->
                editingCategory = null
                initialCategoryType = type
                showCategoryDialog = true
            },
            onSaveSingleTransaction = { desc, cents, currency, catId, type, userId, userDisplayName, applyToSubsequent ->
                viewModel.addSingleTransaction(
                    existingTransaction = editingTransaction,
                    description = desc,
                    amountCents = cents,
                    currency = currency,
                    month = modalMonthNumber,
                    categoryId = catId,
                    type = type,
                    userId = userId,
                    userDisplayName = userDisplayName,
                    applyToSubsequentMonths = applyToSubsequent
                )
            },
            onSaveInstallmentContract = { desc, cents, currency, initInst, totalInst, catId, type, userId, userDisplayName ->
                viewModel.addInstallmentContract(
                    description = desc,
                    monthlyAmountCents = cents,
                    currency = currency,
                    startMonth = modalMonthNumber,
                    initialInstallmentNumber = initInst,
                    totalInstallments = totalInst,
                    categoryId = catId,
                    type = type,
                    userId = userId,
                    userDisplayName = userDisplayName,
                    existingContract = editingInstallmentContract
                )
            }
        )
    }

    pendingDelete?.let { action ->
        ConfirmDeleteBottomSheet(
            title = action.title,
            message = action.message,
            confirmLabel = action.confirmLabel,
            confirmAllLabel = action.confirmAllLabel,
            onDismiss = { pendingDelete = null },
            onConfirmDelete = action.onConfirm,
            onConfirmDeleteAll = action.onConfirmAll
        )
    }
}

// =============================================================================================
// 1. MONTHLY SUMMARY HOME (Stitch "Finner 2027 - Hero First (Revolut Style)")
// =============================================================================================

@Composable
private fun MonthlySummaryHomePage(
    monthFullName: String,
    data: MonthlyDashboardData,
    onAddTransaction: () -> Unit,
    onEditExchangeRate: () -> Unit,
    onAddBucket: () -> Unit,
    onOpenDetails: (MonthDetailsFilter) -> Unit,
    onToggleInstallmentPaid: (ActiveInstallmentItem) -> Unit,
    onEditInstallment: (ActiveInstallmentItem) -> Unit
) {
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

    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = spacing.sm,
            bottom = spacing.floatingNavBottomClearance,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.sectionGap),
    ) {
        // 1. Revolut Hero Balance + Circular Quick Actions
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.md, bottom = spacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "ACTUAL SPARE CASH · ${monthFullName.uppercase()}",
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(spacing.compactPillVertical))
                Text(
                    text = CurrencyConverter.formatCents(data.faltaSobraEurCents, CurrencyCode.EUR),
                    style = FinnerTheme.numericStyles.heroAmount,
                    color = colors.onSurface,
                )
                Spacer(modifier = Modifier.height(spacing.sm))

                // Planned Spare Cash Delta Pill
                val plannedPositive = data.plannedSpareCashEurCents >= 0L
                val plannedColor = if (plannedPositive) colors.inflow else colors.outflow
                val plannedContainer = if (plannedPositive) colors.inflowContainer else colors.outflowContainer
                Surface(
                    shape = shapes.pill,
                    color = plannedContainer,
                ) {
                    Text(
                        text = "Planned ${CurrencyConverter.formatCents(data.plannedSpareCashEurCents, CurrencyCode.EUR)}",
                        style = typography.labelMedium,
                        color = plannedColor,
                        modifier = Modifier.padding(
                            horizontal = spacing.compactPillHorizontal,
                            vertical = spacing.xs,
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(spacing.xxl))

                // Revolut Circular Quick-Action Row (Zero FAB Overlap!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularQuickActionButton(
                        icon = Icons.Default.Add,
                        label = "Add",
                        onClick = onAddTransaction,
                        emphasized = true,
                    )
                    CircularQuickActionButton(
                        icon = Icons.Default.CurrencyExchange,
                        label = "Rate",
                        onClick = onEditExchangeRate,
                    )
                    CircularQuickActionButton(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Bucket",
                        onClick = onAddBucket,
                    )
                    CircularQuickActionButton(
                        icon = Icons.Default.FormatListBulleted,
                        label = "Details",
                        onClick = { onOpenDetails(MonthDetailsFilter()) },
                    )
                }
            }
        }

        // 2. Budget Envelopes Card (Revolut "Total wealth" style — circular badges + neutral white labels)
        item {
            RevolutEnvelopeBucketsCard(
                envelopes = data.envelopeSummaries,
                onAddBucket = onAddBucket,
                onBucketClick = { bucketName ->
                    onOpenDetails(MonthDetailsFilter(initialBucket = bucketName))
                },
            )
        }

        // 3. Brazil Commitments & Installments Compact Preview
        item {
            RevolutBrazilCommitmentsPreviewCard(
                installments = data.activeInstallments,
                isCurrentMonth = isCurrentMonth,
                onSeeAll = {
                    onOpenDetails(MonthDetailsFilter(initialType = TransactionType.EXPENSE_BRAZIL))
                },
                onTogglePaid = onToggleInstallmentPaid,
                onEditInstallment = onEditInstallment,
            )
        }

        // 4. Compact Categories Summary Card (3 clean drill-down rows instead of 15 expanded cards!)
        item {
            FinnerSectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Categories Summary",
                            style = typography.titleMediumBold,
                            color = colors.onSurface,
                        )
                        Text(
                            text = "Planned vs Actual · Tap to view entries",
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.md))

                CategorySummaryRow(
                    title = "Inflows (€)",
                    count = incomeCategories.size,
                    actualFormatted = CurrencyConverter.formatCents(incomeActualEur, CurrencyCode.EUR),
                    plannedFormatted = CurrencyConverter.formatCents(incomePlannedEur, CurrencyCode.EUR),
                    icon = Icons.Default.ArrowDownward,
                    accentColor = colors.inflow,
                    onClick = { onOpenDetails(MonthDetailsFilter(initialType = TransactionType.INCOME)) },
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = spacing.sm),
                    color = colors.cardBorder,
                )

                CategorySummaryRow(
                    title = "Outflows (€)",
                    count = localExpenseCategories.size,
                    actualFormatted = CurrencyConverter.formatCents(localActualEur, CurrencyCode.EUR),
                    plannedFormatted = CurrencyConverter.formatCents(localPlannedEur, CurrencyCode.EUR),
                    icon = Icons.Default.ArrowUpward,
                    accentColor = colors.outflow,
                    onClick = { onOpenDetails(MonthDetailsFilter(initialType = TransactionType.EXPENSE_LOCAL)) },
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = spacing.sm),
                    color = colors.cardBorder,
                )

                CategorySummaryRow(
                    title = "Brazil (R$)",
                    count = brazilCategories.size,
                    actualFormatted = CurrencyConverter.formatCents(brazilActualBrl, CurrencyCode.BRL),
                    plannedFormatted = CurrencyConverter.formatCents(brazilPlannedBrl, CurrencyCode.BRL),
                    icon = Icons.Default.Public,
                    accentColor = colors.brazilAccent,
                    onClick = { onOpenDetails(MonthDetailsFilter(initialType = TransactionType.EXPENSE_BRAZIL)) },
                )
            }
        }
    }
}

@Composable
private fun RevolutEnvelopeBucketsCard(
    envelopes: List<EnvelopeSummary>,
    onAddBucket: () -> Unit,
    onBucketClick: (String) -> Unit,
) {
    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    FinnerSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Budget Envelopes",
                    style = typography.titleMediumBold,
                    color = colors.onSurface,
                )
                Text(
                    text = "Actual vs Planned · Tap to inspect",
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            Text(
                text = "+ New",
                style = typography.labelLarge,
                color = colors.primary,
                modifier = Modifier
                    .clip(shapes.pill)
                    .clickable(onClick = onAddBucket)
                    .padding(horizontal = spacing.sm, vertical = spacing.xs),
            )
        }

        if (envelopes.isEmpty()) {
            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                text = "No budget envelopes configured yet.",
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        } else {
            Spacer(modifier = Modifier.height(spacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                envelopes.forEach { envelope ->
                    val badgeColor = bucketColorFor(envelope.bucketName)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shapes.rowItem)
                            .clickable { onBucketClick(envelope.bucketName) }
                            .padding(vertical = spacing.xxs),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.md),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(sizes.iconBadgeSm)
                                    .clip(shapes.pill)
                                    .background(badgeColor.copy(alpha = FinnerAlpha.envelopeBadgeContainer)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = bucketIconFor(envelope.bucketName),
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(sizes.iconMd),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.compactPillVertical),
                                ) {
                                    Text(
                                        text = envelope.bucketName,
                                        style = typography.bodyMediumSemiBold,
                                        color = colors.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = envelope.formattedPercentage,
                                        style = typography.labelSmall,
                                        color = colors.onSurfaceVariant,
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyConverter.formatCents(envelope.actualEurCents, CurrencyCode.EUR),
                                    style = FinnerTheme.numericStyles.rowAmountBold,
                                    color = colors.onSurface,
                                )
                                Text(
                                    text = "of ${CurrencyConverter.formatCents(envelope.plannedEurCents, CurrencyCode.EUR)}",
                                    style = FinnerTheme.numericStyles.metaAmount,
                                    color = if (envelope.plannedEurCents < 0L) {
                                        colors.outflow
                                    } else {
                                        colors.onSurfaceVariant
                                    },
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { envelope.usageFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(sizes.progressBarSm)
                                .clip(shapes.pill),
                            color = badgeColor,
                            trackColor = colors.surfaceContainerHighest,
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
    onEditInstallment: (ActiveInstallmentItem) -> Unit,
) {
    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    FinnerSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Text(
                    text = "Brazil Commitments",
                    style = typography.titleMediumBold,
                    color = colors.onSurface,
                )
                if (installments.isNotEmpty()) {
                    Surface(
                        shape = shapes.pill,
                        color = colors.inflowContainer,
                    ) {
                        Text(
                            text = "${installments.size} Active",
                            style = typography.labelSmall,
                            color = colors.inflow,
                            modifier = Modifier.padding(
                                horizontal = spacing.badgeHorizontal,
                                vertical = spacing.xs,
                            ),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .clip(shapes.pill)
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = spacing.xs, vertical = spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "See all",
                    style = typography.labelMedium,
                    color = colors.onSurfaceVariant,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(sizes.iconSm),
                )
            }
        }

        if (installments.isEmpty()) {
            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                text = "No active installment contracts scheduled for this month.",
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        } else {
            Spacer(modifier = Modifier.height(spacing.md))
            installments.take(3).forEachIndexed { idx, item ->
                if (idx > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = spacing.sm),
                        color = colors.cardBorder,
                    )
                }
                val badge = item.projection.badgeText
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shapes.rowItem)
                        .clickable { onEditInstallment(item) }
                        .padding(vertical = spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    IconButton(
                        onClick = { onTogglePaid(item) },
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
                    Column(modifier = Modifier.weight(1f)) {
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
                                    text = badge,
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
                            text = item.categoryName.ifBlank { "Installment" },
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyConverter.formatCents(item.amountOriginalCents, item.currency),
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
    onClick: () -> Unit,
) {
    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.rowItem)
            .clickable(onClick = onClick)
            .padding(vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(sizes.iconBadgeSm)
                .clip(shapes.pill)
                .background(accentColor.copy(alpha = FinnerAlpha.tintedContainer)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(sizes.iconMd),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.bodyMediumSemiBold,
                color = colors.onSurface,
            )
            Text(
                text = "$count ${if (count == 1) "category" else "categories"}",
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = actualFormatted,
                style = FinnerTheme.numericStyles.rowAmountBold,
                color = colors.onSurface,
            )
            Text(
                text = "of $plannedFormatted",
                style = FinnerTheme.numericStyles.metaAmount,
                color = colors.onSurfaceVariant,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open $title",
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(sizes.iconLg),
        )
    }
}

// =============================================================================================
// 2. GOALS SECTION (Stitch "Finner 2027 - Goals")
// =============================================================================================

@Composable
private fun GoalsSectionPage(
    goals: List<GoalEntity>,
    selectedYear: Int,
    selectedMonth: Int,
    monthlyDataByMonth: Map<Int, MonthlyDashboardData>,
    onAddGoal: () -> Unit,
    onIncrementGoal: (GoalEntity) -> Unit,
    onEditGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit
) {
    val activeMonthData = monthlyDataByMonth[selectedMonth]
    val activeRate = activeMonthData?.eurToBrlRate ?: CurrencyConverter.DEFAULT_EUR_TO_BRL_RATE

    val totalTargetEur = remember(goals, activeRate) {
        goals.sumOf { CurrencyConverter.toEurCents(it.targetAmountCents, it.currency, activeRate) }
    }
    val totalSavedEur = remember(goals, activeRate, monthlyDataByMonth) {
        goals.sumOf { goal ->
            val bucketName = goal.bucketName
            val bucketEur = if (!bucketName.isNullOrBlank()) {
                monthlyDataByMonth.values.sumOf { mData ->
                    mData.envelopeSummaries
                        .firstOrNull { it.bucketName.equals(bucketName, ignoreCase = true) }
                        ?.actualEurCents ?: 0L
                }
            } else {
                0L
            }
            CurrencyConverter.toEurCents(goal.currentSavedCents, goal.currency, activeRate) + bucketEur
        }
    }
    val overallProgress = if (totalTargetEur > 0L) {
        (totalSavedEur.toFloat() / totalTargetEur.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = spacing.sm,
            bottom = spacing.floatingNavBottomClearance,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.itemGap),
    ) {
        // Header + "+ New Goal" mint pill
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Financial Goals",
                        style = typography.titleLarge,
                        color = colors.onSurface,
                    )
                    Text(
                        text = "${goals.size} active ${if (goals.size == 1) "target" else "targets"} · ${(overallProgress * 100).toInt()}% completed",
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }

                Surface(
                    onClick = onAddGoal,
                    shape = shapes.pill,
                    color = colors.primary,
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
                            tint = colors.onPrimary,
                            modifier = Modifier.size(sizes.iconSm),
                        )
                        Text(
                            text = "New Goal",
                            style = typography.labelLargeBold,
                            color = colors.onPrimary,
                        )
                    }
                }
            }
        }

        // Total Saved Progress Hero Card
        if (goals.isNotEmpty()) {
            item {
                FinnerSectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "TOTAL SAVED PROGRESS",
                            style = typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                        Surface(
                            shape = shapes.pill,
                            color = colors.inflowContainer,
                        ) {
                            Text(
                                text = "${(overallProgress * 100).toInt()}%",
                                style = typography.labelSmallBold,
                                color = colors.inflow,
                                modifier = Modifier.padding(
                                    horizontal = spacing.badgeHorizontal,
                                    vertical = spacing.xs,
                                ),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(spacing.compactPillVertical))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        Text(
                            text = CurrencyConverter.formatCents(totalSavedEur, CurrencyCode.EUR),
                            style = FinnerTheme.numericStyles.sectionAmount,
                            color = colors.onSurface,
                        )
                        Text(
                            text = "/ ${CurrencyConverter.formatCents(totalTargetEur, CurrencyCode.EUR)}",
                            style = typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = spacing.xxs),
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.md))
                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(sizes.progressBarMd)
                            .clip(shapes.pill),
                        color = colors.primary,
                        trackColor = colors.surfaceContainerHighest,
                    )
                }
            }
        }

        if (goals.isEmpty()) {
            item {
                FinnerSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.compactPillVertical)) {
                        Text(
                            text = "No goals registered yet.",
                            style = typography.titleSmallBold,
                            color = colors.onSurface,
                        )
                        Text(
                            text = "Tap '+ New Goal' above to create financial targets and link them to a bucket for automatic pace forecasts.",
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                var menuExpanded by remember { mutableStateOf(false) }
                val bucketName = goal.bucketName
                val hasBucket = !bucketName.isNullOrBlank()
                val bucketColor = if (hasBucket) bucketColorFor(bucketName) else colors.primary

                val bucketYearlyActualEurCents = if (hasBucket) {
                    monthlyDataByMonth.values.sumOf { mData ->
                        mData.envelopeSummaries
                            .firstOrNull { it.bucketName.equals(bucketName, ignoreCase = true) }
                            ?.actualEurCents ?: 0L
                    }
                } else {
                    0L
                }
                val bucketSavedInGoalCurrency = if (hasBucket) {
                    when (goal.currency) {
                        CurrencyCode.EUR -> bucketYearlyActualEurCents
                        CurrencyCode.BRL -> CurrencyConverter.eurCentsToBrlCents(bucketYearlyActualEurCents, activeRate)
                    }
                } else {
                    0L
                }

                val activeBucketEnvelope = if (hasBucket) {
                    activeMonthData?.envelopeSummaries?.firstOrNull {
                        it.bucketName.equals(bucketName, ignoreCase = true)
                    }
                } else {
                    null
                }
                val monthlyBucketPaceEurCents = if (hasBucket) {
                    val activeMonthPace = when {
                        (activeBucketEnvelope?.actualEurCents ?: 0L) > 0L -> activeBucketEnvelope!!.actualEurCents
                        (activeBucketEnvelope?.plannedEurCents ?: 0L) > 0L -> activeBucketEnvelope!!.plannedEurCents
                        else -> 0L
                    }
                    if (activeMonthPace > 0L) {
                        activeMonthPace
                    } else {
                        val nonZeroMonths = monthlyDataByMonth.values.mapNotNull { mData ->
                            val env = mData.envelopeSummaries.firstOrNull {
                                it.bucketName.equals(bucketName, ignoreCase = true)
                            }
                            val v = if ((env?.actualEurCents ?: 0L) > 0L) env!!.actualEurCents else (env?.plannedEurCents ?: 0L)
                            v.takeIf { it > 0L }
                        }
                        if (nonZeroMonths.isNotEmpty()) nonZeroMonths.average().toLong() else 0L
                    }
                } else {
                    0L
                }
                val monthlyPaceInGoalCurrency = if (hasBucket) {
                    when (goal.currency) {
                        CurrencyCode.EUR -> monthlyBucketPaceEurCents
                        CurrencyCode.BRL -> CurrencyConverter.eurCentsToBrlCents(monthlyBucketPaceEurCents, activeRate)
                    }
                } else {
                    0L
                }

                val effectiveSavedCents = goal.currentSavedCents + bucketSavedInGoalCurrency
                val effectiveProgress = if (goal.targetAmountCents <= 0L) {
                    0f
                } else {
                    (effectiveSavedCents.toFloat() / goal.targetAmountCents.toFloat()).coerceIn(0f, 1f)
                }
                val remainingCents = (goal.targetAmountCents - effectiveSavedCents).coerceAtLeast(0L)

                val forecastText = when {
                    remainingCents <= 0L -> "Goal reached!"
                    hasBucket && monthlyPaceInGoalCurrency > 0L -> {
                        val monthsNeeded = ceil(remainingCents.toDouble() / monthlyPaceInGoalCurrency.toDouble()).toLong().coerceAtLeast(1L)
                        val targetYearMonth = YearMonth.of(selectedYear, selectedMonth).plusMonths(monthsNeeded)
                        val targetMonthName = MONTH_FULL_NAMES[(targetYearMonth.monthValue - 1).coerceIn(0, 11)]
                        "Est. $targetMonthName ${targetYearMonth.year} (~$monthsNeeded mo at ${CurrencyConverter.formatCents(monthlyPaceInGoalCurrency, goal.currency)}/mo)"
                    }
                    hasBucket -> "Log contributions in '$bucketName' to forecast completion."
                    else -> null
                }

                FinnerSectionCard(contentPadding = PaddingValues(spacing.cardPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(sizes.iconBadgeMd)
                                .clip(shapes.pill)
                                .background(bucketColor.copy(alpha = FinnerAlpha.tintedContainer)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (hasBucket) bucketIconFor(bucketName) else Icons.Default.Flag,
                                contentDescription = null,
                                tint = bucketColor,
                                modifier = Modifier.size(sizes.iconMd),
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = goal.title,
                                style = typography.titleSmallBold,
                                color = colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.compactPillVertical),
                            ) {
                                if (hasBucket) {
                                    Text(
                                        text = bucketName!!,
                                        style = typography.labelSmall,
                                        color = bucketColor,
                                    )
                                    Text(
                                        text = "·",
                                        style = typography.labelSmall,
                                        color = colors.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    text = goal.priority.label,
                                    style = typography.labelSmall,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                        }

                        Surface(
                            onClick = { onIncrementGoal(goal) },
                            shape = shapes.pill,
                            color = colors.surfaceContainerHigh,
                            border = BorderStroke(sizes.borderThin, colors.cardBorder),
                        ) {
                            Text(
                                text = "+100 ${goal.currency.symbol}",
                                style = typography.labelSmall,
                                color = colors.primary,
                                modifier = Modifier.padding(
                                    horizontal = spacing.compactPillHorizontal,
                                    vertical = spacing.compactPillVertical,
                                ),
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(sizes.iconButtonSm),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "Goal actions",
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
                                    text = { Text("Edit goal") },
                                    onClick = {
                                        menuExpanded = false
                                        onEditGoal(goal)
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
                                    text = { Text("Delete goal", color = colors.error) },
                                    onClick = {
                                        menuExpanded = false
                                        onDeleteGoal(goal)
                                    },
                                )
                            }
                        }
                    }

                    if (goal.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(spacing.compactPillVertical))
                        Text(
                            text = goal.notes,
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.md))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${CurrencyConverter.formatCents(effectiveSavedCents, goal.currency)} / ${CurrencyConverter.formatCents(goal.targetAmountCents, goal.currency)}",
                            style = FinnerTheme.numericStyles.rowAmount,
                            color = colors.onSurface,
                        )
                        Text(
                            text = "${(effectiveProgress * 100).toInt()}%",
                            style = typography.labelMediumBold,
                            color = bucketColor,
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.compactPillVertical))
                    LinearProgressIndicator(
                        progress = { effectiveProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(sizes.progressBarMd)
                            .clip(shapes.pill),
                        color = bucketColor,
                        trackColor = colors.surfaceContainerHighest,
                    )

                    if (forecastText != null) {
                        Spacer(modifier = Modifier.height(spacing.sm))
                        Text(
                            text = forecastText,
                            style = typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================================
// 3. HISTORY & LOGS (Stitch "Finner 2027 - History")
// =============================================================================================

@Composable
private fun HistorySectionPage(
    transactions: List<TransactionEntity>,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<TransactionType?>(null) }

    val filteredTransactions = remember(transactions, searchQuery, selectedType) {
        val q = searchQuery.trim().lowercase()
        transactions.filter { tx ->
            val typeMatch = selectedType == null || tx.type == selectedType
            val queryMatch = q.isEmpty() ||
                tx.description.lowercase().contains(q) ||
                tx.userDisplayName.lowercase().contains(q)
            typeMatch && queryMatch
        }
    }

    val groupedByDate = remember(filteredTransactions) {
        filteredTransactions.groupBy { it.date }
    }

    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = spacing.sm,
            bottom = spacing.floatingNavBottomClearance,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.itemGap),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                Text(
                    text = "History & Logs",
                    style = typography.titleLarge,
                    color = colors.onSurface,
                )
                Text(
                    text = "${filteredTransactions.size} ${if (filteredTransactions.size == 1) "transaction" else "transactions"} recorded",
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }

        // Revolut Pill Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search description or member...",
                        style = typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                    )
                },
                singleLine = true,
                shape = shapes.pill,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceContainer,
                    unfocusedContainerColor = colors.surfaceContainer,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.cardBorder,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Type Filter Pills
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                item {
                    HistoryFilterPill(
                        label = "All",
                        selected = selectedType == null,
                        onClick = { selectedType = null },
                    )
                }
                item {
                    HistoryFilterPill(
                        label = "Inflows",
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                    )
                }
                item {
                    HistoryFilterPill(
                        label = "Outflows",
                        selected = selectedType == TransactionType.EXPENSE_LOCAL,
                        onClick = { selectedType = TransactionType.EXPENSE_LOCAL },
                    )
                }
                item {
                    HistoryFilterPill(
                        label = "Brazil",
                        selected = selectedType == TransactionType.EXPENSE_BRAZIL,
                        onClick = { selectedType = TransactionType.EXPENSE_BRAZIL },
                    )
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                FinnerSectionCard {
                    Text(
                        text = if (transactions.isEmpty()) {
                            "No entries recorded in this year yet."
                        } else {
                            "No transactions match your search or filter."
                        },
                        style = typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        } else {
            groupedByDate.forEach { (date, dayTransactions) ->
                item(key = "header_$date") {
                    Text(
                        text = date.toString(),
                        style = typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.xs, start = spacing.xs),
                    )
                }
                item(key = "group_$date") {
                    FinnerSectionCard(
                        contentPadding = PaddingValues(
                            horizontal = spacing.cardPaddingCompactHorizontal,
                            vertical = spacing.cardPaddingCompactVertical,
                        ),
                    ) {
                        dayTransactions.forEachIndexed { idx, tx ->
                            if (idx > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = spacing.xs),
                                    color = colors.cardBorder,
                                )
                            }
                            HistoryTransactionRow(
                                transaction = tx,
                                onEdit = { onEditTransaction(tx) },
                                onDelete = { onDeleteTransaction(tx) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterPill(
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
private fun HistoryTransactionRow(
    transaction: TransactionEntity,
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
    val accentColor = when (transaction.type) {
        TransactionType.INCOME -> colors.inflow
        TransactionType.EXPENSE_BRAZIL -> colors.brazilAccent
        TransactionType.EXPENSE_LOCAL -> colors.outflow
    }
    val icon = when (transaction.type) {
        TransactionType.INCOME -> Icons.Default.ArrowDownward
        TransactionType.EXPENSE_BRAZIL -> Icons.Default.Public
        TransactionType.EXPENSE_LOCAL -> Icons.Default.ArrowUpward
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
        Box(
            modifier = Modifier
                .size(sizes.iconBadgeSm)
                .clip(shapes.pill)
                .background(accentColor.copy(alpha = FinnerAlpha.tintedContainer)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(sizes.iconMd),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.description,
                style = typography.bodyMediumSemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${transaction.type.label} · ${transaction.userDisplayName.ifBlank { "Shared" }}",
                style = typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = (if (isIncome) "+ " else "- ") + CurrencyConverter.formatCents(transaction.amountCents, transaction.currency),
            style = FinnerTheme.numericStyles.rowAmountBold,
            color = if (isIncome) colors.inflow else colors.onSurface,
        )

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

// =============================================================================================
// 4. PROFILE & SETTINGS (Stitch "Finner 2027 - Profile & Settings")
// =============================================================================================

@Composable
private fun ProfileSectionPage(
    selectedYear: Int,
    userDisplayName: String,
    userEmail: String?,
    householdName: String,
    householdId: String,
    isCloudConnected: Boolean,
    selectedUserFilter: String?,
    currentUserId: String,
    onUserFilterChange: (String?) -> Unit,
    onManageAccount: () -> Unit
) {
    val initials = remember(userDisplayName) {
        userDisplayName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "F" }
    }

    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = spacing.sm,
            bottom = spacing.floatingNavBottomClearance,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.itemGap),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                Text(
                    text = "Profile & Settings",
                    style = typography.titleLarge,
                    color = colors.onSurface,
                )
                Text(
                    text = "Account & Household Workspace",
                    style = typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }

        // Household Identity Card + 3-column summary strip
        item {
            FinnerSectionCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    Box(
                        modifier = Modifier
                            .size(sizes.avatarLg)
                            .clip(shapes.pill)
                            .background(colors.primary.copy(alpha = FinnerAlpha.selectedContainer)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials,
                            style = typography.titleMediumBold,
                            color = colors.primary,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        Text(
                            text = householdName,
                            style = typography.titleMediumBold,
                            color = colors.onSurface,
                        )
                        Text(
                            text = userEmail ?: userDisplayName,
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Surface(
                        shape = shapes.pill,
                        color = if (isCloudConnected) {
                            colors.inflowContainer
                        } else {
                            colors.surfaceContainerHighest
                        },
                    ) {
                        Text(
                            text = if (isCloudConnected) "Synced" else "Offline",
                            style = typography.labelSmallBold,
                            color = if (isCloudConnected) {
                                colors.inflow
                            } else {
                                colors.onSurfaceVariant
                            },
                            modifier = Modifier.padding(
                                horizontal = spacing.compactPillHorizontal,
                                vertical = spacing.xs,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.lg))
                HorizontalDivider(color = colors.cardBorder)
                Spacer(modifier = Modifier.height(spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ProfileStatItem(label = "CURRENCY", value = "EUR / BRL")
                    ProfileStatItem(label = "MEMBER", value = userDisplayName)
                    ProfileStatItem(label = "CYCLE", value = "Year $selectedYear")
                }
            }
        }

        // Data & Sync Card
        item {
            Text(
                text = "DATA & SYNC",
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = spacing.xs),
            )
        }
        item {
            FinnerSectionCard(onClick = onManageAccount) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    Box(
                        modifier = Modifier
                            .size(sizes.iconBadgeMd)
                            .clip(shapes.pill)
                            .background(colors.primary.copy(alpha = FinnerAlpha.tintedContainer)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isCloudConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(sizes.iconLg),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCloudConnected) "Cloud Synchronization" else "Offline Mode",
                            style = typography.titleSmall,
                            color = colors.onSurface,
                        )
                        Text(
                            text = "Workspace: $householdId",
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = "Manage",
                        style = typography.labelLarge,
                        color = colors.primary,
                    )
                }
            }
        }

        // Entry Visibility Filter Card
        item {
            Text(
                text = "VIEW ENTRIES FROM",
                style = typography.labelSmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = spacing.xs),
            )
        }
        item {
            FinnerSectionCard {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                    HistoryFilterPill(
                        label = "All Members",
                        selected = selectedUserFilter == null,
                        onClick = { onUserFilterChange(null) },
                    )
                    HistoryFilterPill(
                        label = userDisplayName,
                        selected = selectedUserFilter == currentUserId,
                        onClick = { onUserFilterChange(currentUserId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(FinnerTheme.spacing.xxs)) {
        Text(
            text = label,
            style = FinnerTheme.typography.labelSmall,
            color = FinnerTheme.colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style = FinnerTheme.typography.bodySmallSemiBold,
            color = FinnerTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
