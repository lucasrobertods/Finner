package com.lrds.finner.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.lrds.finner.domain.model.CategoryReconciliationRow
import com.lrds.finner.domain.model.EnvelopeSummary
import com.lrds.finner.domain.model.MonthlyDashboardData
import com.lrds.finner.ui.dashboard.components.ExchangeRateChip
import com.lrds.finner.ui.dashboard.components.FinnerDestination
import com.lrds.finner.ui.dashboard.components.FinnerFloatingScaffold
import com.lrds.finner.ui.dashboard.components.FinnerTopBar
import com.lrds.finner.ui.dashboard.components.MonthPillTabs
import com.lrds.finner.ui.dashboard.components.bucketColorFor
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
                subtitle = "${uiState.session.householdName}\nEUR / BRL"
            )
        },
        selectedDestination = destination,
        onDestinationSelected = { target ->
            destination = target
            when (target) {
                FinnerDestination.MESES -> viewModel.selectSection(AppSection.MONTHLY)
                FinnerDestination.OBJETIVOS -> viewModel.selectSection(AppSection.GOALS)
                else -> Unit
            }
        },
        fabLabel = when (destination) {
            FinnerDestination.MESES -> "Add ($activeMonthLabel)"
            FinnerDestination.OBJETIVOS -> "New Goal"
            else -> null
        },
        onFabClick = {
            if (destination == FinnerDestination.MESES) {
                preselectedCategoryForModal = null
                editingTransaction = null
                editingInstallmentContract = null
                showTransactionModal = true
            } else {
                editingGoal = null
                showGoalDialog = true
            }
        }
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
                    val pageMonthLabel = MONTH_SHORT_LABELS[pageMonthZeroBased]
                    val pageMonthFullName = MONTH_FULL_NAMES[pageMonthZeroBased]
                    if (monthData != null) {
                        MonthlyStatementPage(
                            monthLabel = pageMonthLabel,
                            monthFullName = pageMonthFullName,
                            data = monthData,
                            onEditExchangeRate = { showExchangeRateDialog = true },
                            onAddBucket = { showBucketDialog = true },
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
                            onQuickAddForCategory = { category ->
                                preselectedCategoryForModal = category
                                editingTransaction = null
                                editingInstallmentContract = null
                                showTransactionModal = true
                            },
                            onEditTransaction = { tx ->
                                preselectedCategoryForModal = null
                                editingTransaction = tx
                                editingInstallmentContract = null
                                showTransactionModal = true
                            },
                            onEditInstallment = { item ->
                                preselectedCategoryForModal = null
                                editingTransaction = null
                                editingInstallmentContract = item.projection.contract
                                showTransactionModal = true
                            },
                            onDeleteCategory = { category ->
                                pendingDelete = PendingDeleteAction(
                                    title = "Delete Category",
                                    message = "Do you want to delete the category \"${category.name}\" only in $pageMonthFullName ${uiState.selectedYear}, or from all months and years?\nAll entries in this category will also be deleted.",
                                    confirmLabel = "Only $pageMonthFullName",
                                    confirmAllLabel = "All months & years",
                                    onConfirm = { viewModel.deleteCategory(category.id, deleteFromAllMonthsAndYears = false) },
                                    onConfirmAll = { viewModel.deleteCategory(category.id, deleteFromAllMonthsAndYears = true) }
                                )
                            },
                            onDeleteTransaction = { tx ->
                                val txMonthName = MONTH_FULL_NAMES[(tx.month - 1).coerceIn(0, 11)]
                                pendingDelete = PendingDeleteAction(
                                    title = "Delete Entry",
                                    message = "Do you want to delete the entry \"${tx.description}\" (${CurrencyConverter.formatCents(tx.amountCents, CurrencyCode.EUR)}) only in $txMonthName ${tx.year}, or from all months and years?",
                                    confirmLabel = "Only $txMonthName",
                                    confirmAllLabel = "All months & years",
                                    onConfirm = { viewModel.deleteTransaction(tx.id, deleteFromAllMonthsAndYears = false) },
                                    onConfirmAll = { viewModel.deleteTransaction(tx.id, deleteFromAllMonthsAndYears = true) }
                                )
                            },
                            onDeleteInstallment = { item ->
                                pendingDelete = PendingDeleteAction(
                                    title = "Delete Installment Contract",
                                    message = "Are you sure you want to delete the installment contract \"${item.projection.contract.name}\"?",
                                    onConfirm = { viewModel.deleteInstallmentContract(item.projection.contract.id) }
                                )
                            },
                            onToggleInstallmentPaid = { item ->
                                viewModel.toggleInstallmentPaid(item)
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading data for $pageMonthFullName...")
                        }
                    }
                }
            }

            FinnerDestination.OBJETIVOS -> GoalsSectionPage(
                goals = uiState.goals,
                selectedYear = uiState.selectedYear,
                selectedMonth = activeMonthNumber,
                monthlyDataByMonth = uiState.monthlyDataByMonth,
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
                    showTransactionModal = true
                },
                onDeleteTransaction = { tx ->
                    val txMonthName = MONTH_FULL_NAMES[(tx.month - 1).coerceIn(0, 11)]
                    pendingDelete = PendingDeleteAction(
                        title = "Delete Entry",
                        message = "Do you want to delete the entry \"${tx.description}\" (${CurrencyConverter.formatCents(tx.amountCents, CurrencyCode.EUR)}) only in $txMonthName ${tx.year}, or from all months and years?",
                        confirmLabel = "Only $txMonthName",
                        confirmAllLabel = "All months & years",
                        onConfirm = { viewModel.deleteTransaction(tx.id, deleteFromAllMonthsAndYears = false) },
                        onConfirmAll = { viewModel.deleteTransaction(tx.id, deleteFromAllMonthsAndYears = true) }
                    )
                }
            )

            FinnerDestination.PERFIL -> ProfileSectionPage(
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

@Composable
private fun MonthlyStatementPage(
    monthLabel: String,
    monthFullName: String,
    data: MonthlyDashboardData,
    onEditExchangeRate: () -> Unit,
    onAddBucket: () -> Unit,
    onAddCategory: (TransactionType) -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    onQuickAddForCategory: (CategoryEntity) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onEditInstallment: (ActiveInstallmentItem) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onDeleteInstallment: (ActiveInstallmentItem) -> Unit,
    onToggleInstallmentPaid: (ActiveInstallmentItem) -> Unit
) {
    val isCurrentMonth = remember(data.year, data.month) {
        val now = LocalDate.now()
        data.year == now.year && data.month == now.monthValue
    }
    val uncategorizedTransactions = remember(data.transactions, data.categoryReconciliations) {
        val matchedIds = data.categoryReconciliations
            .flatMap { it.transactions }
            .map { it.id }
            .toSet()
        data.transactions.filter { it.id !in matchedIds }
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

    val incomePlannedEur = remember(incomeCategories, data.eurToBrlRate) {
        incomeCategories.sumOf {
            CurrencyConverter.toEurCents(it.previstoCents, it.category.currency, data.eurToBrlRate)
        }
    }
    val incomeActualEur = remember(incomeCategories) {
        incomeCategories.sumOf { it.realizadoEurCents }
    }

    val localPlannedEur = remember(localExpenseCategories, data.eurToBrlRate) {
        localExpenseCategories.sumOf {
            CurrencyConverter.toEurCents(it.previstoCents, it.category.currency, data.eurToBrlRate)
        }
    }
    val localActualEur = remember(localExpenseCategories) {
        localExpenseCategories.sumOf { it.realizadoEurCents }
    }

    val brazilPlannedBrl = remember(brazilCategories) {
        brazilCategories.sumOf { it.previstoCents }
    }
    val brazilPlannedEur = remember(brazilCategories) {
        brazilCategories.sumOf { it.previstoEurCents }
    }
    val brazilActualEur = remember(brazilCategories) {
        brazilCategories.sumOf { it.realizadoEurCents }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 168.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Monthly Header Card (Full month name title, e.g. "November")
        item {
            MonthlyHeaderCard(
                monthFullName = monthFullName,
                data = data,
                onEditExchangeRate = onEditExchangeRate
            )
        }

        // 2. Envelope Allocation / Buckets
        item {
            EnvelopeBucketsCard(
                envelopes = data.envelopeSummaries,
                onAddBucket = onAddBucket
            )
        }

        // 3. Brazil & Active Installments Module (1/3, etc.)
        item {
            InstallmentsAndBrazilCard(
                monthLabel = monthLabel,
                installments = data.activeInstallments,
                isCurrentMonth = isCurrentMonth,
                onToggleInstallmentPaid = onToggleInstallmentPaid,
                onEditInstallment = onEditInstallment,
                onDeleteInstallment = onDeleteInstallment
            )
        }

        // 4. Budget Reconciliation Sections separated by: Inflows (€), Outflows (€), Brazil (R$)
        item {
            Text(
                text = "Categories (PLANNED vs ACTUAL)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Section 4A: Cash Inflows
        item {
            CategorySectionHeader(
                title = "Inflows (€)",
                totalsSummary = "Planned: ${CurrencyConverter.formatCents(incomePlannedEur, CurrencyCode.EUR)}\nActual: ${CurrencyConverter.formatCents(incomeActualEur, CurrencyCode.EUR)}",
                accentColor = Color(0xFF4CAF50),
                onAddClick = { onAddCategory(TransactionType.INCOME) }
            )
        }
        if (incomeCategories.isEmpty()) {
            item {
                EmptyCategorySectionCard(
                    message = "No Inflow categories registered in $monthFullName."
                )
            }
        } else {
            items(
                items = incomeCategories,
                key = { it.category.id }
            ) { row ->
                CategoryReconciliationCard(
                    row = row,
                    isCurrentMonth = isCurrentMonth,
                    onQuickAddEntry = { onQuickAddForCategory(row.category) },
                    onEditCategory = { onEditCategory(row.category) },
                    onDeleteCategory = { onDeleteCategory(row.category) },
                    onToggleInstallmentPaid = onToggleInstallmentPaid,
                    onEditInstallment = onEditInstallment,
                    onDeleteInstallment = onDeleteInstallment,
                    onEditTransaction = onEditTransaction,
                    onDeleteTransaction = onDeleteTransaction
                )
            }
        }

        // Section 4B: Cash Outflows (Local €)
        item {
            CategorySectionHeader(
                title = "Outflows (€)",
                totalsSummary = "Planned: ${CurrencyConverter.formatCents(localPlannedEur, CurrencyCode.EUR)}\nActual: ${CurrencyConverter.formatCents(localActualEur, CurrencyCode.EUR)}",
                accentColor = MaterialTheme.colorScheme.primary,
                onAddClick = { onAddCategory(TransactionType.EXPENSE_LOCAL) }
            )
        }
        if (localExpenseCategories.isEmpty()) {
            item {
                EmptyCategorySectionCard(
                    message = "No Outflow (€) categories registered in $monthFullName."
                )
            }
        } else {
            items(
                items = localExpenseCategories,
                key = { it.category.id }
            ) { row ->
                CategoryReconciliationCard(
                    row = row,
                    isCurrentMonth = isCurrentMonth,
                    onQuickAddEntry = { onQuickAddForCategory(row.category) },
                    onEditCategory = { onEditCategory(row.category) },
                    onDeleteCategory = { onDeleteCategory(row.category) },
                    onToggleInstallmentPaid = onToggleInstallmentPaid,
                    onEditInstallment = onEditInstallment,
                    onDeleteInstallment = onDeleteInstallment,
                    onEditTransaction = onEditTransaction,
                    onDeleteTransaction = onDeleteTransaction
                )
            }
        }

        // Section 4C: Brazil Commitments (R$)
        item {
            CategorySectionHeader(
                title = "Brazil (R$)",
                totalsSummary = "Planned: ${CurrencyConverter.formatCents(brazilPlannedBrl, CurrencyCode.BRL)} (≈ ${CurrencyConverter.formatCents(brazilPlannedEur, CurrencyCode.EUR)})\nActual: ${CurrencyConverter.formatCents(brazilActualEur, CurrencyCode.EUR)}",
                accentColor = Color(0xFFFF7043),
                onAddClick = { onAddCategory(TransactionType.EXPENSE_BRAZIL) }
            )
        }
        if (brazilCategories.isEmpty()) {
            item {
                EmptyCategorySectionCard(
                    message = "No Brazil (R$) categories registered in $monthFullName."
                )
            }
        } else {
            items(
                items = brazilCategories,
                key = { it.category.id }
            ) { row ->
                CategoryReconciliationCard(
                    row = row,
                    isCurrentMonth = isCurrentMonth,
                    onQuickAddEntry = { onQuickAddForCategory(row.category) },
                    onEditCategory = { onEditCategory(row.category) },
                    onDeleteCategory = { onDeleteCategory(row.category) },
                    onToggleInstallmentPaid = onToggleInstallmentPaid,
                    onEditInstallment = onEditInstallment,
                    onDeleteInstallment = onDeleteInstallment,
                    onEditTransaction = onEditTransaction,
                    onDeleteTransaction = onDeleteTransaction
                )
            }
        }

        // 5. Uncategorized Transactions (if user logs a transaction before creating categories)
        if (uncategorizedTransactions.isNotEmpty()) {
            item {
                UncategorizedTransactionsCard(
                    transactions = uncategorizedTransactions,
                    onEditTransaction = onEditTransaction,
                    onDeleteTransaction = onDeleteTransaction
                )
            }
        }
    }
}

@Composable
private fun CategorySectionHeader(
    title: String,
    totalsSummary: String,
    accentColor: Color,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = totalsSummary,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(
            onClick = onAddClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "+ Add",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun EmptyCategorySectionCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun MonthlyHeaderCard(
    monthFullName: String,
    data: MonthlyDashboardData,
    onEditExchangeRate: () -> Unit
) {
    val plannedPositive = data.plannedSpareCashEurCents >= 0L
    val plannedColor = if (plannedPositive) Color(0xFF4CAF50) else Color(0xFFEF5350)

    val actualPositive = data.faltaSobraEurCents >= 0L
    val actualColor = if (actualPositive) Color(0xFF4CAF50) else Color(0xFFEF5350)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = monthFullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (data.hasManualExchangeRate) {
                            "Last updated: ${data.lastUpdatedDate}"
                        } else {
                            "Monthly financial summary"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                ExchangeRateChip(
                    rate = data.eurToBrlRate,
                    onClick = onEditExchangeRate
                )
            }

            HorizontalDivider()

            SpareCashRowSurface(
                title = "PLANNED SPARE CASH",
                amountEurCents = data.plannedSpareCashEurCents,
                accentColor = plannedColor
            )

            SpareCashRowSurface(
                title = "ACTUAL SPARE CASH",
                amountEurCents = data.faltaSobraEurCents,
                accentColor = actualColor
            )
        }
    }
}

@Composable
private fun SpareCashRowSurface(
    title: String,
    amountEurCents: Long,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.14f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            Text(
                text = CurrencyConverter.formatCents(
                    amountCents = amountEurCents,
                    currency = CurrencyCode.EUR,
                    includeSign = true
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun EnvelopeBucketsCard(
    envelopes: List<EnvelopeSummary>,
    onAddBucket: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = "Budget Envelopes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Actual vs Planned (Total per bucket)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = onAddBucket,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+ New Bucket",
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            envelopes.forEach { env ->
                val bucketColor = bucketColorFor(env.bucketName)
                val isOverPlanned = env.actualEurCents > env.plannedEurCents && env.plannedEurCents > 0L
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(bucketColor)
                            )
                            Text(
                                text = "${env.bucketName} (${env.formattedPercentage})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = bucketColor
                            )
                        }
                        Text(
                            text = "${CurrencyConverter.formatCents(env.actualEurCents, CurrencyCode.EUR)} / ${CurrencyConverter.formatCents(env.plannedEurCents, CurrencyCode.EUR)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    LinearProgressIndicator(
                        progress = { env.usageFraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isOverPlanned) {
                            MaterialTheme.colorScheme.error
                        } else {
                            bucketColor
                        },
                        trackColor = bucketColor.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InstallmentsAndBrazilCard(
    monthLabel: String,
    installments: List<ActiveInstallmentItem>,
    isCurrentMonth: Boolean,
    onToggleInstallmentPaid: (ActiveInstallmentItem) -> Unit,
    onEditInstallment: (ActiveInstallmentItem) -> Unit,
    onDeleteInstallment: (ActiveInstallmentItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    text = "Brazil Commitments & Installments ($monthLabel)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Active installments mapped automatically for $monthLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (installments.isEmpty()) {
                Text(
                    text = "No active installment contracts in $monthLabel.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                installments.forEach { item ->
                    val proj = item.projection
                    val authorLabel = proj.contract.userDisplayName.ifBlank { "Shared" }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isCurrentMonth) {
                                        Checkbox(
                                            checked = item.isPaid,
                                            onCheckedChange = { onToggleInstallmentPaid(item) },
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Installment ${proj.badgeText}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = proj.contract.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (item.isPaid) {
                                                "${item.categoryName}\n$authorLabel - Paid"
                                            } else {
                                                "${item.categoryName}\n$authorLabel - Unpaid"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (item.isPaid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyConverter.formatCents(item.amountEurCents, CurrencyCode.EUR),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (item.currency == CurrencyCode.BRL) {
                                            Text(
                                                text = CurrencyConverter.formatCents(item.amountOriginalCents, CurrencyCode.BRL),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onEditInstallment(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit contract",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteInstallment(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Remove contract",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            LinearProgressIndicator(
                                progress = { proj.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryReconciliationCard(
    row: CategoryReconciliationRow,
    isCurrentMonth: Boolean,
    onQuickAddEntry: () -> Unit,
    onEditCategory: () -> Unit,
    onDeleteCategory: () -> Unit,
    onToggleInstallmentPaid: (ActiveInstallmentItem) -> Unit,
    onEditInstallment: (ActiveInstallmentItem) -> Unit,
    onDeleteInstallment: (ActiveInstallmentItem) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val isBrazil = row.category.type == TransactionType.EXPENSE_BRAZIL
    val plannedCurrency = if (isBrazil) CurrencyCode.BRL else CurrencyCode.EUR
    val actualAndAvailableCurrency = CurrencyCode.EUR
    val isIncome = row.category.type == TransactionType.INCOME
    val disponivelPositive = if (isIncome) row.realizadoCents >= row.previstoCents else row.disponivelCents >= 0L
    val bucketName = row.category.envelopeName
    val bucketColor = bucketColorFor(bucketName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = row.category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!isIncome && !bucketName.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = bucketColor.copy(alpha = 0.16f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(bucketColor)
                                    )
                                    Text(
                                        text = bucketName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = bucketColor
                                    )
                                }
                            }
                        }
                        Text(
                            text = row.category.type.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onQuickAddEntry,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "New entry in this category",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onEditCategory,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit category",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteCategory,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete category",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            // Three spreadsheet columns: PLANNED | ACTUAL | AVAILABLE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    val pctLabel = row.category.plannedPercentage?.takeIf { it > 0.0 }?.let { pct ->
                        val roundedInt = kotlin.math.round(pct).toInt()
                        if (kotlin.math.abs(pct - roundedInt) < 0.05) " ($roundedInt%)" else String.format(" (%.1f%%)", pct)
                    } ?: ""
                    Text(
                        text = "PLANNED$pctLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyConverter.formatCents(row.previstoCents, plannedCurrency),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column {
                    Text(
                        text = "ACTUAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyConverter.formatCents(row.realizadoCents, actualAndAvailableCurrency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "AVAILABLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyConverter.formatCents(row.disponivelCents, actualAndAvailableCurrency),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (disponivelPositive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider()
                    if (row.transactions.isEmpty() && row.installments.isEmpty()) {
                        Text(
                            text = "No entries recorded in this category this month.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        row.transactions.forEach { tx ->
                            val authorLabel = tx.userDisplayName.ifBlank { "Shared" }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditTransaction(tx) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tx.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${tx.date}\n$authorLabel",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = CurrencyConverter.formatCents(tx.amountCents, CurrencyCode.EUR),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { onEditTransaction(tx) },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit entry",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteTransaction(tx) },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete entry",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        row.installments.forEach { inst ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditInstallment(inst) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isCurrentMonth) {
                                        Checkbox(
                                            checked = inst.isPaid,
                                            onCheckedChange = { onToggleInstallmentPaid(inst) },
                                            modifier = Modifier.size(36.dp)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${inst.projection.contract.name} (${inst.projection.badgeText})",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = if (inst.isPaid) "Paid" else "Unpaid",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (inst.isPaid) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = CurrencyConverter.formatCents(inst.amountEurCents, CurrencyCode.EUR),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { onEditInstallment(inst) },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit installment contract",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteInstallment(inst) },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete installment contract",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UncategorizedTransactionsCard(
    transactions: List<TransactionEntity>,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Uncategorized Entries",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            transactions.forEach { tx ->
                val authorLabel = tx.userDisplayName.ifBlank { "Shared" }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tx.description,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${tx.type.label}\n${tx.date} - $authorLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = CurrencyConverter.formatCents(tx.amountCents, tx.currency),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { onEditTransaction(tx) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDeleteTransaction(tx) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsSectionPage(
    goals: List<GoalEntity>,
    selectedYear: Int,
    selectedMonth: Int,
    monthlyDataByMonth: Map<Int, MonthlyDashboardData>,
    onIncrementGoal: (GoalEntity) -> Unit,
    onEditGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit
) {
    val activeMonthData = monthlyDataByMonth[selectedMonth]
    val activeRate = activeMonthData?.eurToBrlRate ?: CurrencyConverter.DEFAULT_EUR_TO_BRL_RATE

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 168.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "GOALS & Wishlist (Short, Medium, and Long Term)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (goals.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "No goals registered yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap the 'New Goal' button below to add your financial targets and link them to a bucket to estimate when you will reach each goal.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                val bucketName = goal.bucketName
                val hasBucket = !bucketName.isNullOrBlank()
                val bucketColor = if (hasBucket) bucketColorFor(bucketName) else MaterialTheme.colorScheme.primary

                // Compute total actual accumulated in associated bucket across the year (months 1..12)
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

                // Compute monthly contribution pace from the associated bucket (Actual or Planned in active month, or average of planned months)
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
                    remainingCents <= 0L -> "Goal reached! 🎉"
                    hasBucket && monthlyPaceInGoalCurrency > 0L -> {
                        val monthsNeeded = ceil(remainingCents.toDouble() / monthlyPaceInGoalCurrency.toDouble()).toLong().coerceAtLeast(1L)
                        val targetYearMonth = YearMonth.of(selectedYear, selectedMonth).plusMonths(monthsNeeded)
                        val targetMonthName = MONTH_FULL_NAMES[(targetYearMonth.monthValue - 1).coerceIn(0, 11)]
                        "Estimated completion: $targetMonthName ${targetYearMonth.year}\n(~$monthsNeeded ${if (monthsNeeded == 1L) "month" else "months"} at ${CurrencyConverter.formatCents(monthlyPaceInGoalCurrency, goal.currency)}/mo via $bucketName)"
                    }
                    hasBucket -> "Set a planned amount or log contributions in bucket '$bucketName' to estimate when you will reach this goal."
                    else -> null
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = bucketColor
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = goal.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (hasBucket) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = bucketColor.copy(alpha = 0.16f)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(bucketColor)
                                                    )
                                                    Text(
                                                        text = bucketName!!,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = bucketColor
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${goal.priority.label}${goal.targetDate?.let { "\nTarget date: $it" } ?: ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(
                                    onClick = { onIncrementGoal(goal) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("+ 100 ${goal.currency.symbol}")
                                }
                                IconButton(
                                    onClick = { onEditGoal(goal) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit goal",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteGoal(goal) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete goal",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (goal.notes.isNotBlank()) {
                            Text(
                                text = goal.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Saved: ${CurrencyConverter.formatCents(effectiveSavedCents, goal.currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = bucketColor
                            )
                            Text(
                                text = "Target: ${CurrencyConverter.formatCents(goal.targetAmountCents, goal.currency)} (${(effectiveProgress * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        LinearProgressIndicator(
                            progress = { effectiveProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = bucketColor,
                            trackColor = bucketColor.copy(alpha = 0.2f)
                        )

                        if (forecastText != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = bucketColor.copy(alpha = 0.12f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = forecastText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = bucketColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySectionPage(
    transactions: List<TransactionEntity>,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 168.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "No entries recorded in this year yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${tx.type.label}\n${tx.date} - ${tx.userDisplayName.ifBlank { "Shared" }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = CurrencyConverter.formatCents(tx.amountCents, tx.currency),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { onEditTransaction(tx) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { onDeleteTransaction(tx) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionPage(
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 168.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = userDisplayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userEmail ?: "No Firebase account connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Budget: $householdName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onManageAccount
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isCloudConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCloudConnected) "Cloud synchronization" else "Offline mode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Workspace: $householdId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Manage",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            text = "View entries from",
            style = MaterialTheme.typography.labelLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedUserFilter == null,
                onClick = { onUserFilterChange(null) },
                label = { Text("All") }
            )
            FilterChip(
                selected = selectedUserFilter == currentUserId,
                onClick = { onUserFilterChange(currentUserId) },
                label = { Text(userDisplayName) }
            )
        }

        OutlinedButton(onClick = onManageAccount, modifier = Modifier.fillMaxWidth()) {
            Text("Account, login & workspace")
        }
    }
}
