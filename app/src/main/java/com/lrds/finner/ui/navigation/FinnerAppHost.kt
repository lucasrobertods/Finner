package com.lrds.finner.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.lrds.finner.core.currency.CurrencyConverter
import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.GoalEntity
import com.lrds.finner.data.local.entity.InstallmentContractEntity
import com.lrds.finner.data.local.entity.TransactionEntity
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.data.remote.CloudSyncState
import com.lrds.finner.ui.dashboard.MONTH_FULL_NAMES
import com.lrds.finner.ui.dashboard.MONTH_SHORT_LABELS
import com.lrds.finner.ui.dashboard.MonthDetailsScreen
import com.lrds.finner.ui.dashboard.MonthlyDashboardScreen
import com.lrds.finner.ui.dashboard.MonthlyDashboardViewModel
import com.lrds.finner.ui.dashboard.components.ExchangeRateChip
import com.lrds.finner.ui.dashboard.components.FinnerFloatingScaffold
import com.lrds.finner.ui.dashboard.components.FinnerTopBar
import com.lrds.finner.ui.goals.GoalsScreen
import com.lrds.finner.ui.history.HistoryScreen
import com.lrds.finner.ui.profile.ProfileScreen
import com.lrds.finner.ui.transaction.BucketDialog
import com.lrds.finner.ui.transaction.CategoryDialog
import com.lrds.finner.ui.transaction.ConfirmDeleteBottomSheet
import com.lrds.finner.ui.transaction.ExchangeRateDialog
import com.lrds.finner.ui.transaction.GoalDialog
import com.lrds.finner.ui.transaction.HouseholdSyncDialog
import com.lrds.finner.ui.transaction.QuickTransactionModal

private data class PendingDeleteAction(
    val title: String,
    val message: String,
    val confirmLabel: String = "Delete",
    val confirmAllLabel: String? = null,
    val onConfirm: () -> Unit,
    val onConfirmAll: (() -> Unit)? = null
)

@Composable
fun FinnerAppHost(
    viewModel: MonthlyDashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val navigationState = rememberNavigationState(
        startRoute = FinnerRoute.Dashboard,
        topLevelRoutes = TOP_LEVEL_ROUTES
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

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
    var modalTargetYear by remember { mutableIntStateOf(uiState.selectedYear) }
    var modalTargetMonth by remember { mutableIntStateOf(uiState.selectedMonth) }

    val currentRoute = navigationState.currentRoute
    val isTopLevel = currentRoute.isTopLevelRoute()
    val isCloudConnected = uiState.cloudSyncState == CloudSyncState.CLOUD_CONNECTED
    val activeMonthData = uiState.monthlyDataByMonth[uiState.selectedMonth]

    val openTransactionModal: (Int, Int, CategoryEntity?, TransactionEntity?, InstallmentContractEntity?) -> Unit =
        { year, month, preselectedCat, tx, contract ->
            modalTargetYear = year
            modalTargetMonth = month
            preselectedCategoryForModal = preselectedCat
            editingTransaction = tx
            editingInstallmentContract = contract
            showTransactionModal = true
        }

    val openCategoryModal: (Int, Int, CategoryEntity?, TransactionType) -> Unit =
        { year, month, existing, type ->
            modalTargetYear = year
            modalTargetMonth = month
            editingCategory = existing
            initialCategoryType = type
            showCategoryDialog = true
        }

    val openExchangeRateModal: (Int, Int) -> Unit = { year, month ->
        modalTargetYear = year
        modalTargetMonth = month
        showExchangeRateDialog = true
    }

    val requestDeleteCategory: (CategoryEntity, Int, Int) -> Unit = { category, year, month ->
        val monthFullName = MONTH_FULL_NAMES[(month - 1).coerceIn(0, 11)]
        pendingDelete = PendingDeleteAction(
            title = "Delete Category",
            message = "Do you want to delete the category \"${category.name}\" only in $monthFullName $year, or from all months and years?\nAll entries in this category will also be deleted.",
            confirmLabel = "Only $monthFullName",
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

    val requestDeleteInstallment: (String, String) -> Unit = { contractId, contractName ->
        pendingDelete = PendingDeleteAction(
            title = "Delete Installment Contract",
            message = "Are you sure you want to delete the installment contract \"$contractName\"?",
            onConfirm = { viewModel.deleteInstallmentContract(contractId) }
        )
    }

    @Composable
    fun SharedTopBar(showExchangeRate: Boolean) {
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
            trailingContent = if (showExchangeRate && activeMonthData != null) {
                {
                    ExchangeRateChip(
                        rate = activeMonthData.eurToBrlRate,
                        onClick = { openExchangeRateModal(uiState.selectedYear, uiState.selectedMonth) }
                    )
                }
            } else {
                null
            }
        )
    }

    val entryProvider = entryProvider<NavKey> {
        entry<FinnerRoute.Dashboard>(
            metadata = FinnerTransitions.tabMetadata(tabIndex = 0)
        ) {
            val lifecycleOwner = LocalLifecycleOwner.current
            MonthlyDashboardScreen(
                selectedYear = uiState.selectedYear,
                availableMonths = uiState.availableMonths,
                selectedMonth = uiState.selectedMonth,
                monthlyDataByMonth = uiState.monthlyDataByMonth,
                onSelectMonth = viewModel::selectMonth,
                onAddTransaction = {
                    openTransactionModal(uiState.selectedYear, uiState.selectedMonth, null, null, null)
                },
                onEditExchangeRate = {
                    openExchangeRateModal(uiState.selectedYear, uiState.selectedMonth)
                },
                onAddBucket = { showBucketDialog = true },
                onOpenDetails = { year, month, filter ->
                    if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        navigator.navigate(
                            FinnerRoute.MonthDetails(
                                year = year,
                                month = month,
                                initialType = filter.initialType,
                                initialBucket = filter.initialBucket
                            )
                        )
                    }
                },
                onToggleInstallmentPaid = viewModel::toggleInstallmentPaid,
                onEditInstallment = { item ->
                    openTransactionModal(
                        item.projection.targetYear,
                        item.projection.targetMonth,
                        null,
                        null,
                        item.projection.contract
                    )
                },
                topBar = { SharedTopBar(showExchangeRate = true) },
                reselectEvents = navigator.reselectEvents
            )
        }

        entry<FinnerRoute.MonthDetails>(
            metadata = FinnerTransitions.detailMetadata()
        ) { key ->
            val monthFullName = MONTH_FULL_NAMES[(key.month - 1).coerceIn(0, 11)]
            val monthData by remember(key.year, key.month) {
                viewModel.observeMonth(key.year, key.month)
            }.collectAsStateWithLifecycle(
                initialValue = if (key.year == uiState.selectedYear) {
                    uiState.monthlyDataByMonth[key.month]
                } else {
                    null
                }
            )

            val resolvedData = monthData
            if (resolvedData != null) {
                MonthDetailsScreen(
                    monthFullName = monthFullName,
                    data = resolvedData,
                    availableBuckets = uiState.bucketNames,
                    initialFilter = key.toFilter(),
                    onBack = dropUnlessResumed { navigator.goBack() },
                    onEditExchangeRate = { openExchangeRateModal(key.year, key.month) },
                    onAddTransaction = { openTransactionModal(key.year, key.month, null, null, null) },
                    onAddCategory = { type -> openCategoryModal(key.year, key.month, null, type) },
                    onEditCategory = { category ->
                        openCategoryModal(key.year, key.month, category, category.type)
                    },
                    onDeleteCategory = { category ->
                        requestDeleteCategory(category, key.year, key.month)
                    },
                    onQuickAddForCategory = { category ->
                        openTransactionModal(key.year, key.month, category, null, null)
                    },
                    onEditTransaction = { tx ->
                        openTransactionModal(tx.year, tx.month, null, tx, null)
                    },
                    onDeleteTransaction = requestDeleteTransaction,
                    onEditInstallment = { item ->
                        openTransactionModal(key.year, key.month, null, null, item.projection.contract)
                    },
                    onDeleteInstallment = { item ->
                        requestDeleteInstallment(item.projection.contract.id, item.projection.contract.name)
                    },
                    onToggleInstallmentPaid = viewModel::toggleInstallmentPaid
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading $monthFullName ${key.year}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        entry<FinnerRoute.Goals>(
            metadata = FinnerTransitions.tabMetadata(tabIndex = 1)
        ) {
            GoalsScreen(
                goals = uiState.goals,
                selectedYear = uiState.selectedYear,
                selectedMonth = uiState.selectedMonth,
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
                },
                topBar = { SharedTopBar(showExchangeRate = false) },
                reselectEvents = navigator.reselectEvents
            )
        }

        entry<FinnerRoute.History>(
            metadata = FinnerTransitions.tabMetadata(tabIndex = 2)
        ) {
            val sortedTransactions = remember(uiState.monthlyDataByMonth) {
                uiState.monthlyDataByMonth.values
                    .flatMap { it.transactions }
                    .sortedByDescending { it.date }
            }
            HistoryScreen(
                transactions = sortedTransactions,
                onEditTransaction = { tx ->
                    openTransactionModal(tx.year, tx.month, null, tx, null)
                },
                onDeleteTransaction = requestDeleteTransaction,
                topBar = { SharedTopBar(showExchangeRate = false) },
                reselectEvents = navigator.reselectEvents
            )
        }

        entry<FinnerRoute.Profile>(
            metadata = FinnerTransitions.tabMetadata(tabIndex = 3)
        ) {
            ProfileScreen(
                selectedYear = uiState.selectedYear,
                userDisplayName = uiState.session.userDisplayName,
                userEmail = uiState.session.userEmail,
                householdName = uiState.session.householdName,
                householdId = uiState.session.householdId,
                isCloudConnected = isCloudConnected,
                selectedUserFilter = uiState.selectedUserFilter,
                currentUserId = uiState.session.userId,
                onUserFilterChange = viewModel::selectUserFilter,
                onManageAccount = { showHouseholdSyncDialog = true },
                topBar = { SharedTopBar(showExchangeRate = false) },
                reselectEvents = navigator.reselectEvents
            )
        }
    }

    FinnerFloatingScaffold(
        modifier = modifier,
        selectedTopLevelRoute = navigationState.topLevelRoute,
        onDestinationSelected = { target -> navigator.navigate(target) },
        showBottomBar = isTopLevel,
        fabLabel = null,
        onFabClick = {}
    ) {
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
            transitionSpec = FinnerTransitions.forwardTransitionSpec,
            popTransitionSpec = FinnerTransitions.popTransitionSpec,
            predictivePopTransitionSpec = FinnerTransitions.predictivePopTransitionSpec,
            modifier = Modifier.fillMaxSize()
        )
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

    if (showExchangeRateDialog) {
        val rateMonthData = uiState.monthlyDataByMonth[modalTargetMonth]
        val rateMonthFullName = MONTH_FULL_NAMES[(modalTargetMonth - 1).coerceIn(0, 11)]
        ExchangeRateDialog(
            monthLabel = rateMonthFullName,
            selectedYear = modalTargetYear,
            currentRate = rateMonthData?.eurToBrlRate ?: CurrencyConverter.DEFAULT_EUR_TO_BRL_RATE,
            onDismiss = { showExchangeRateDialog = false },
            onConfirmRate = { newRate ->
                viewModel.updateExchangeRate(
                    month = modalTargetMonth,
                    newRate = newRate,
                    year = modalTargetYear
                )
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
        val catMonthLabel = MONTH_SHORT_LABELS[(modalTargetMonth - 1).coerceIn(0, 11)]
        val catMonthData = uiState.monthlyDataByMonth[modalTargetMonth]
        CategoryDialog(
            monthLabel = catMonthLabel,
            selectedMonth = modalTargetMonth,
            selectedYear = modalTargetYear,
            availableBuckets = uiState.bucketNames,
            plannedIncomeEurCents = catMonthData?.plannedIncomeEurCents ?: 0L,
            eurToBrlRate = catMonthData?.eurToBrlRate ?: CurrencyConverter.DEFAULT_EUR_TO_BRL_RATE,
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
                    month = modalTargetMonth,
                    name = name,
                    type = type,
                    envelopeName = envName,
                    plannedAmountCents = plannedCents,
                    plannedPercentage = plannedPct,
                    currency = currency,
                    applyToSubsequentMonths = applyToSubsequent,
                    year = modalTargetYear
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
        val txMonthNumber = editingTransaction?.month ?: modalTargetMonth
        val txYear = editingTransaction?.year ?: modalTargetYear
        val txMonthLabel = MONTH_SHORT_LABELS[(txMonthNumber - 1).coerceIn(0, 11)]
        QuickTransactionModal(
            monthLabel = txMonthLabel,
            selectedMonth = txMonthNumber,
            selectedYear = txYear,
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
                openCategoryModal(txYear, txMonthNumber, null, type)
            },
            onSaveSingleTransaction = { desc, cents, currency, catId, type, userId, userDisplayName, applyToSubsequent ->
                viewModel.addSingleTransaction(
                    existingTransaction = editingTransaction,
                    description = desc,
                    amountCents = cents,
                    currency = currency,
                    month = txMonthNumber,
                    categoryId = catId,
                    type = type,
                    userId = userId,
                    userDisplayName = userDisplayName,
                    applyToSubsequentMonths = applyToSubsequent,
                    year = txYear
                )
            },
            onSaveInstallmentContract = { desc, cents, currency, initInst, totalInst, catId, type, userId, userDisplayName ->
                viewModel.addInstallmentContract(
                    description = desc,
                    monthlyAmountCents = cents,
                    currency = currency,
                    startMonth = txMonthNumber,
                    initialInstallmentNumber = initInst,
                    totalInstallments = totalInst,
                    categoryId = catId,
                    type = type,
                    userId = userId,
                    userDisplayName = userDisplayName,
                    existingContract = editingInstallmentContract,
                    startYear = txYear
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
