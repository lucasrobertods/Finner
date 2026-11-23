package com.lrds.finner.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lrds.finner.core.model.HouseholdAuthManager
import com.lrds.finner.core.model.HouseholdSession
import com.lrds.finner.data.local.entity.BudgetBucketEntity
import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.EnvelopeType
import com.lrds.finner.data.local.entity.GoalEntity
import com.lrds.finner.data.local.entity.GoalPriority
import com.lrds.finner.data.local.entity.InstallmentContractEntity
import com.lrds.finner.data.local.entity.TransactionEntity
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.data.remote.CloudSyncState
import com.lrds.finner.data.repository.FinanceRepository
import com.lrds.finner.domain.model.ActiveInstallmentItem
import com.lrds.finner.domain.model.MonthlyDashboardData
import com.lrds.finner.domain.usecase.ObserveMonthlyDashboardUseCase
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val selectedYear: Int = LocalDate.now().year,
    val availableYears: List<Int> = listOf(LocalDate.now().year),
    val availableMonths: List<Int> = (LocalDate.now().monthValue..12).toList(),
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedUserFilter: String? = null,
    val session: HouseholdSession = HouseholdSession(
        userId = "local",
        userDisplayName = "Member 1",
        householdId = HouseholdAuthManager.DEFAULT_HOUSEHOLD_ID,
        householdName = HouseholdAuthManager.DEFAULT_HOUSEHOLD_NAME
    ),
    val authFeedbackMessage: String? = null,
    val cloudSyncState: CloudSyncState = CloudSyncState.OFFLINE_LOCAL_ONLY,
    val monthlyDataByMonth: Map<Int, MonthlyDashboardData> = emptyMap(),
    val categories: List<CategoryEntity> = emptyList(),
    val bucketNames: List<String> = EnvelopeType.DEFAULT_BUCKET_NAMES,
    val goals: List<GoalEntity> = emptyList()
)

private data class FilterState(
    val year: Int,
    val availableYears: List<Int>,
    val month: Int,
    val userFilter: String?
)

private data class DomainBundle(
    val monthsMap: Map<Int, MonthlyDashboardData>,
    val categories: List<CategoryEntity>,
    val buckets: List<BudgetBucketEntity>,
    val goals: List<GoalEntity>
)

@OptIn(ExperimentalCoroutinesApi::class)
class MonthlyDashboardViewModel(
    private val repository: FinanceRepository,
    private val observeMonthlyDashboardUseCase: ObserveMonthlyDashboardUseCase,
    private val authManager: HouseholdAuthManager
) : ViewModel() {

    private val availableYears = MutableStateFlow(authManager.loadAvailableYears())
    private val selectedYear = MutableStateFlow(LocalDate.now().year)
    private val selectedMonth = MutableStateFlow(LocalDate.now().monthValue)
    private val selectedUserFilter = MutableStateFlow<String?>(null)
    private val authFeedbackMessage = MutableStateFlow<String?>(null)

    init {
        if (selectedYear.value !in availableYears.value) {
            val updated = (availableYears.value + selectedYear.value).distinct().sorted()
            availableYears.value = updated
            authManager.saveAvailableYears(updated)
        }
        viewModelScope.launch {
            authManager.session.collect { session ->
                repository.startRealtimeSync(session.householdId)
            }
        }
    }

    private val filterStateFlow = combine(
        selectedYear,
        availableYears,
        selectedMonth,
        selectedUserFilter
    ) { year, years, month, userFilter ->
        FilterState(year, years, month, userFilter)
    }

    fun observeMonth(year: Int, month: Int): Flow<MonthlyDashboardData> =
        selectedUserFilter.flatMapLatest { userFilter ->
            observeMonthlyDashboardUseCase(year, month, userFilter)
        }

    private val allMonthsFlow = combine(selectedYear, selectedUserFilter) { year, userFilter ->
        year to userFilter
    }.flatMapLatest { (year, userFilter) ->
        val startMonth = authManager.getStartMonthForYear(year)
        val monthFlows = (startMonth..12).map { month ->
            observeMonthlyDashboardUseCase(year, month, userFilter)
        }
        combine(monthFlows) { array ->
            array.associateBy { it.month }
        }
    }

    private val domainDataFlow = combine(
        allMonthsFlow,
        repository.observeCategories(),
        repository.observeBudgetBuckets(),
        repository.observeGoals()
    ) { monthsMap, categories, buckets, goals ->
        DomainBundle(monthsMap, categories, buckets, goals)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        filterStateFlow,
        authManager.session,
        authFeedbackMessage,
        domainDataFlow
    ) { filter, session, feedback, bundle ->
        val mergedBucketNames = buildList {
            addAll(EnvelopeType.DEFAULT_BUCKET_NAMES)
            bundle.buckets.forEach { b ->
                if (b.bucketName.isNotBlank() && none { it.equals(b.bucketName, ignoreCase = true) }) {
                    add(b.bucketName)
                }
            }
            bundle.categories.forEach { cat ->
                val env = cat.envelopeName?.trim()
                if (cat.type != TransactionType.INCOME && !env.isNullOrEmpty() &&
                    none { it.equals(env, ignoreCase = true) }
                ) {
                    add(env)
                }
            }
            bundle.goals.forEach { goal ->
                val env = goal.bucketName?.trim()
                if (!env.isNullOrEmpty() && none { it.equals(env, ignoreCase = true) }) {
                    add(env)
                }
            }
        }
        val startMonth = authManager.getStartMonthForYear(filter.year)
        val availableMonths = (startMonth..12).toList()
        val safeSelectedMonth = if (filter.month in availableMonths) filter.month else startMonth

        DashboardUiState(
            selectedYear = filter.year,
            availableYears = filter.availableYears,
            availableMonths = availableMonths,
            selectedMonth = safeSelectedMonth,
            selectedUserFilter = filter.userFilter,
            session = session,
            authFeedbackMessage = feedback,
            cloudSyncState = repository.cloudSyncState,
            monthlyDataByMonth = bundle.monthsMap,
            categories = bundle.categories,
            bucketNames = mergedBucketNames,
            goals = bundle.goals
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(
            availableYears = availableYears.value,
            availableMonths = (authManager.getStartMonthForYear(selectedYear.value)..12).toList(),
            selectedMonth = selectedMonth.value.coerceAtLeast(authManager.getStartMonthForYear(selectedYear.value)),
            session = authManager.session.value
        )
    )

    fun selectYear(year: Int) {
        val startMonth = authManager.getStartMonthForYear(year)
        selectedYear.value = year
        if (selectedMonth.value < startMonth) {
            selectedMonth.value = startMonth
        }
    }

    fun addNextYear() {
        val sourceYear = availableYears.value.maxOrNull() ?: selectedYear.value
        val nextYear = sourceYear + 1
        val updated = (availableYears.value + nextYear).distinct().sorted()
        availableYears.value = updated
        authManager.saveAvailableYears(updated)
        viewModelScope.launch {
            repository.initializeYearFromPreviousDecember(
                sourceYear = sourceYear,
                targetYear = nextYear
            )
            selectedYear.value = nextYear
            selectedMonth.value = 1
        }
    }

    fun deleteYear(year: Int) {
        viewModelScope.launch {
            repository.deleteYear(year)
            val currentCalendarYear = LocalDate.now().year
            val remaining = availableYears.value.filter { it != year }.sorted()
            val updated = if (remaining.isEmpty()) listOf(currentCalendarYear) else remaining
            availableYears.value = updated
            authManager.saveAvailableYears(updated)

            val nextSelectedYear = if (selectedYear.value == year || selectedYear.value !in updated) {
                updated.last()
            } else {
                selectedYear.value
            }
            val startMonth = authManager.getStartMonthForYear(nextSelectedYear)
            selectedYear.value = nextSelectedYear
            if (selectedMonth.value < startMonth) {
                selectedMonth.value = startMonth
            }
        }
    }

    fun selectMonth(month: Int) {
        val startMonth = authManager.getStartMonthForYear(selectedYear.value)
        if (month in startMonth..12) {
            selectedMonth.value = month
        }
    }

    fun selectUserFilter(userId: String?) {
        selectedUserFilter.value = userId
    }

    fun updateProfileAndWorkspace(
        displayName: String,
        householdId: String,
        householdName: String
    ) {
        authManager.updateProfileAndWorkspace(displayName, householdId, householdName)
        authFeedbackMessage.value = null
    }

    fun signInWithEmail(
        email: String,
        password: String,
        displayName: String,
        householdId: String
    ) {
        viewModelScope.launch {
            val result = authManager.signInOrRegisterWithEmail(
                email = email,
                password = password,
                displayName = displayName,
                householdId = householdId
            )
            authFeedbackMessage.value = result.fold(
                onSuccess = { "Signed in as ${it.userEmail}" },
                onFailure = { it.message ?: "Email authentication failed" }
            )
        }
    }

    fun signInWithGoogle(
        activityContext: Context,
        webClientId: String?,
        displayName: String,
        householdId: String
    ) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogle(
                activityContext = activityContext,
                customWebClientId = webClientId,
                displayName = displayName,
                householdId = householdId
            )
            authFeedbackMessage.value = result.fold(
                onSuccess = { "Signed in with Google (${it.userEmail ?: it.userDisplayName})" },
                onFailure = { it.message ?: "Google Sign-In failed" }
            )
        }
    }

    fun signOut() {
        authManager.signOut()
        authFeedbackMessage.value = "Firebase session signed out."
    }

    fun getConfiguredWebClientId(): String? = authManager.getConfiguredWebClientId()

    fun clearAuthFeedback() {
        authFeedbackMessage.value = null
    }

    fun updateExchangeRate(month: Int, newRate: Double, year: Int = selectedYear.value) {
        if (newRate <= 0.0) return
        viewModelScope.launch {
            repository.updateExchangeRate(
                year = year,
                month = month,
                eurToBrlRate = newRate,
                userId = authManager.session.value.userId
            )
        }
    }

    fun addSingleTransaction(
        existingTransaction: TransactionEntity? = null,
        description: String,
        amountCents: Long,
        currency: CurrencyCode,
        month: Int,
        categoryId: String?,
        type: TransactionType,
        userId: String,
        userDisplayName: String,
        applyToSubsequentMonths: Boolean = false,
        year: Int = selectedYear.value
    ) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val safeDay = (existingTransaction?.date?.dayOfMonth ?: today.dayOfMonth).coerceAtMost(28)
            val date = LocalDate.of(year, month, safeDay)
            val entity = if (existingTransaction != null) {
                existingTransaction.copy(
                    description = description,
                    amountCents = amountCents,
                    currency = currency,
                    date = date,
                    year = year,
                    month = month,
                    categoryId = categoryId,
                    type = type,
                    userId = userId,
                    userDisplayName = userDisplayName
                )
            } else {
                TransactionEntity(
                    description = description,
                    amountCents = amountCents,
                    currency = currency,
                    date = date,
                    categoryId = categoryId,
                    type = type,
                    userId = userId,
                    userDisplayName = userDisplayName
                )
            }
            repository.saveTransaction(
                transaction = entity,
                applyToSubsequentMonths = applyToSubsequentMonths,
                originalTransactionForSeriesMatch = existingTransaction
            )
        }
    }

    fun addInstallmentContract(
        description: String,
        monthlyAmountCents: Long,
        currency: CurrencyCode,
        startMonth: Int,
        initialInstallmentNumber: Int,
        totalInstallments: Int,
        categoryId: String?,
        type: TransactionType,
        userId: String,
        userDisplayName: String,
        existingContract: InstallmentContractEntity? = null,
        startYear: Int = selectedYear.value
    ) {
        viewModelScope.launch {
            val coercedInitial = initialInstallmentNumber.coerceAtLeast(1)
            val coercedTotal = totalInstallments.coerceAtLeast(coercedInitial)
            val entity = if (existingContract != null) {
                existingContract.copy(
                    description = description,
                    monthlyAmountCents = monthlyAmountCents,
                    currency = currency,
                    initialInstallmentNumber = coercedInitial,
                    totalInstallments = coercedTotal,
                    categoryId = categoryId,
                    type = type,
                    userId = userId,
                    userDisplayName = userDisplayName,
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
            } else {
                InstallmentContractEntity(
                    description = description,
                    monthlyAmountCents = monthlyAmountCents,
                    currency = currency,
                    startYear = startYear,
                    startMonth = startMonth,
                    initialInstallmentNumber = coercedInitial,
                    totalInstallments = coercedTotal,
                    categoryId = categoryId,
                    type = type,
                    userId = userId,
                    userDisplayName = userDisplayName
                )
            }
            repository.saveInstallmentContract(entity)
        }
    }

    fun deleteTransaction(id: String, deleteFromAllMonthsAndYears: Boolean = false) {
        viewModelScope.launch {
            repository.deleteTransaction(id, applyToSubsequentMonths = deleteFromAllMonthsAndYears)
        }
    }

    fun deleteInstallmentContract(id: String) {
        viewModelScope.launch {
            repository.deleteInstallmentContract(id)
        }
    }

    fun toggleInstallmentPaid(
        item: ActiveInstallmentItem,
        today: LocalDate = LocalDate.now()
    ) {
        val targetYear = item.projection.targetYear
        val targetMonth = item.projection.targetMonth
        if (targetYear != today.year || targetMonth != today.monthValue) return
        viewModelScope.launch {
            val updated = item.projection.contract.withPaidForMonth(
                targetYear = targetYear,
                targetMonth = targetMonth,
                paid = !item.isPaid
            )
            repository.saveInstallmentContract(updated)
        }
    }

    fun addSavingsToGoal(goal: GoalEntity, incrementCents: Long) {
        viewModelScope.launch {
            val updated = (goal.currentSavedCents + incrementCents).coerceAtLeast(0L)
            repository.saveGoal(goal.copy(currentSavedCents = updated))
        }
    }

    fun saveCategory(
        existingCategory: CategoryEntity?,
        month: Int,
        name: String,
        type: TransactionType,
        envelopeName: String?,
        plannedAmountCents: Long,
        plannedPercentage: Double? = null,
        currency: CurrencyCode,
        applyToSubsequentMonths: Boolean,
        year: Int = selectedYear.value
    ) {
        viewModelScope.launch {
            val resolvedEnvelope = if (type == TransactionType.INCOME) null else (envelopeName ?: "Bills")
            val normalizedPercentage = if (type == TransactionType.INCOME) null else plannedPercentage
            if (!resolvedEnvelope.isNullOrBlank()) {
                repository.saveBudgetBucket(resolvedEnvelope)
            }
            val entity = if (existingCategory != null) {
                existingCategory.copy(
                    year = year,
                    month = month,
                    name = name,
                    type = type,
                    envelopeName = resolvedEnvelope,
                    plannedAmountCents = plannedAmountCents,
                    plannedPercentage = normalizedPercentage,
                    currency = currency
                )
            } else {
                val newId = UUID.randomUUID().toString()
                CategoryEntity(
                    id = newId,
                    seriesId = newId,
                    year = year,
                    month = month,
                    name = name,
                    type = type,
                    envelopeName = resolvedEnvelope,
                    plannedAmountCents = plannedAmountCents,
                    plannedPercentage = normalizedPercentage,
                    currency = currency
                )
            }
            repository.saveCategory(entity, applyToSubsequentMonths = applyToSubsequentMonths)
        }
    }

    fun addBudgetBucket(bucketName: String) {
        viewModelScope.launch {
            repository.saveBudgetBucket(bucketName)
        }
    }

    fun deleteCategory(id: String, deleteFromAllMonthsAndYears: Boolean = false) {
        viewModelScope.launch {
            repository.deleteCategory(id, applyToSubsequentMonths = deleteFromAllMonthsAndYears)
        }
    }

    fun saveGoal(
        existingGoal: GoalEntity? = null,
        title: String,
        targetAmountCents: Long,
        currentSavedCents: Long,
        currency: CurrencyCode,
        priority: GoalPriority,
        bucketName: String?,
        notes: String
    ) {
        viewModelScope.launch {
            val cleanBucket = bucketName?.trim()?.takeIf { it.isNotEmpty() }
            if (cleanBucket != null) {
                repository.saveBudgetBucket(cleanBucket)
            }
            val entity = if (existingGoal != null) {
                existingGoal.copy(
                    title = title,
                    targetAmountCents = targetAmountCents,
                    currentSavedCents = currentSavedCents,
                    currency = currency,
                    priority = priority,
                    bucketName = cleanBucket,
                    notes = notes
                )
            } else {
                GoalEntity(
                    title = title,
                    targetAmountCents = targetAmountCents,
                    currentSavedCents = currentSavedCents,
                    currency = currency,
                    priority = priority,
                    bucketName = cleanBucket,
                    notes = notes
                )
            }
            repository.saveGoal(entity)
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    companion object {
        fun provideFactory(
            repository: FinanceRepository,
            observeMonthlyDashboardUseCase: ObserveMonthlyDashboardUseCase,
            authManager: HouseholdAuthManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MonthlyDashboardViewModel(
                    repository = repository,
                    observeMonthlyDashboardUseCase = observeMonthlyDashboardUseCase,
                    authManager = authManager
                ) as T
            }
        }
    }
}
