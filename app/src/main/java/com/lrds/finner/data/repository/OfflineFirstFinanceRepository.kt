package com.lrds.finner.data.repository

import com.lrds.finner.data.local.FinnerDatabase
import com.lrds.finner.data.local.entity.BudgetBucketEntity
import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.ExchangeRateEntity
import com.lrds.finner.data.local.entity.GoalEntity
import com.lrds.finner.data.local.entity.InstallmentContractEntity
import com.lrds.finner.data.local.entity.SyncStatus
import com.lrds.finner.data.local.entity.TransactionEntity
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.data.remote.CloudSyncState
import com.lrds.finner.data.remote.FirestoreSyncDataSource
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class OfflineFirstFinanceRepository(
    private val database: FinnerDatabase,
    private val remoteSync: FirestoreSyncDataSource
) : FinanceRepository {

    override val cloudSyncState: CloudSyncState
        get() = remoteSync.syncState

    override fun startRealtimeSync(householdId: String) {
        remoteSync.attachRealtimeListeners(database, householdId)
    }

    override fun observeCategories(): Flow<List<CategoryEntity>> =
        database.categoryDao().observeActiveCategories()

    override fun observeCategoriesForMonth(year: Int, month: Int): Flow<List<CategoryEntity>> =
        database.categoryDao().observeCategoriesForMonth(year, month)

    override fun observeTransactionsForMonth(year: Int, month: Int): Flow<List<TransactionEntity>> =
        database.transactionDao().observeTransactionsForMonth(year, month)

    override fun observeTransactionsForYear(year: Int): Flow<List<TransactionEntity>> =
        database.transactionDao().observeTransactionsForYear(year)

    override fun observeInstallmentContracts(): Flow<List<InstallmentContractEntity>> =
        database.installmentContractDao().observeActiveContracts()

    override fun observeExchangeRateForMonth(year: Int, month: Int): Flow<ExchangeRateEntity?> =
        database.exchangeRateDao().observeRateForMonth(year, month)

    override fun observeExchangeRatesForYear(year: Int): Flow<List<ExchangeRateEntity>> =
        database.exchangeRateDao().observeRatesForYear(year)

    override fun observeBudgetBuckets(): Flow<List<BudgetBucketEntity>> =
        database.budgetBucketDao().observeActiveBuckets()

    override fun observeGoals(): Flow<List<GoalEntity>> =
        database.goalDao().observeActiveGoals()

    override suspend fun saveTransaction(
        transaction: TransactionEntity,
        applyToSubsequentMonths: Boolean,
        originalTransactionForSeriesMatch: TransactionEntity?
    ) {
        val nowMillis = System.currentTimeMillis()
        val startMonth = transaction.date.monthValue
        val year = transaction.date.year
        val safeDay = transaction.date.dayOfMonth.coerceAtMost(28)

        val currentPending = transaction.copy(
            year = year,
            month = startMonth,
            updatedAtEpochMillis = nowMillis,
            syncStatus = SyncStatus.PENDING_UPLOAD
        )

        val transactionsToSave = mutableListOf(currentPending)

        if (applyToSubsequentMonths) {
            val allCategories = database.categoryDao().getAllActiveCategories()
            val originCategory = transaction.categoryId?.let { catId ->
                allCategories.firstOrNull { it.id == catId }
            }
            val futureYears = allCategories
                .map { it.year }
                .filter { it > year }
                .distinct()
                .sorted()

            val targetYearMonths = buildList {
                for (m in (startMonth + 1)..12) {
                    add(year to m)
                }
                for (y in futureYears) {
                    for (m in 1..12) {
                        add(y to m)
                    }
                }
            }

            val allActiveTransactions = database.transactionDao().getAllActiveTransactions()
            val referenceDescription = originalTransactionForSeriesMatch?.description ?: transaction.description
            val referenceType = originalTransactionForSeriesMatch?.type ?: transaction.type

            for ((targetYear, targetMonth) in targetYearMonths) {
                val targetCategoryId = if (originCategory != null) {
                    allCategories.firstOrNull {
                        it.year == targetYear &&
                            it.month == targetMonth &&
                            (it.seriesId == originCategory.seriesId ||
                                (it.name.equals(originCategory.name, ignoreCase = true) &&
                                    it.type == originCategory.type))
                    }?.id ?: transaction.categoryId
                } else {
                    transaction.categoryId
                }

                val existingInTargetMonth = if (originalTransactionForSeriesMatch != null) {
                    allActiveTransactions.firstOrNull {
                        it.year == targetYear &&
                            it.month == targetMonth &&
                            it.type == referenceType &&
                            it.description.equals(referenceDescription, ignoreCase = true) &&
                            (it.categoryId == targetCategoryId || targetCategoryId == null)
                    }
                } else {
                    null
                }

                if (existingInTargetMonth != null) {
                    transactionsToSave.add(
                        existingInTargetMonth.copy(
                            description = transaction.description,
                            amountCents = transaction.amountCents,
                            currency = transaction.currency,
                            categoryId = targetCategoryId,
                            type = transaction.type,
                            userId = transaction.userId,
                            userDisplayName = transaction.userDisplayName,
                            isPaid = transaction.isPaid,
                            updatedAtEpochMillis = nowMillis,
                            syncStatus = SyncStatus.PENDING_UPLOAD
                        )
                    )
                } else {
                    transactionsToSave.add(
                        TransactionEntity(
                            id = UUID.randomUUID().toString(),
                            description = transaction.description,
                            amountCents = transaction.amountCents,
                            currency = transaction.currency,
                            date = LocalDate.of(targetYear, targetMonth, safeDay),
                            year = targetYear,
                            month = targetMonth,
                            categoryId = targetCategoryId,
                            type = transaction.type,
                            userId = transaction.userId,
                            userDisplayName = transaction.userDisplayName,
                            isPaid = transaction.isPaid,
                            updatedAtEpochMillis = nowMillis,
                            syncStatus = SyncStatus.PENDING_UPLOAD
                        )
                    )
                }
            }
        }

        database.transactionDao().upsertAll(transactionsToSave)
        transactionsToSave.forEach { pending ->
            if (remoteSync.pushTransaction(pending)) {
                database.transactionDao().upsert(pending.copy(syncStatus = SyncStatus.SYNCED))
            }
        }
    }

    override suspend fun deleteTransaction(id: String, applyToSubsequentMonths: Boolean) {
        val nowMillis = System.currentTimeMillis()
        val target = database.transactionDao().getTransactionById(id)
        if (target == null) {
            database.transactionDao().softDeleteById(id, nowMillis)
            return
        }

        if (!applyToSubsequentMonths) {
            val deleted = target.copy(
                isDeleted = true,
                syncStatus = SyncStatus.PENDING_DELETE,
                updatedAtEpochMillis = nowMillis
            )
            database.transactionDao().upsert(deleted)
            remoteSync.pushTransaction(deleted)
            return
        }

        val allCategories = database.categoryDao().getAllCategories()
        val targetCat = target.categoryId?.let { cid -> allCategories.firstOrNull { it.id == cid } }
        val matchingCategoryIds = if (targetCat != null) {
            allCategories
                .filter {
                    it.id == targetCat.id ||
                        it.seriesId == targetCat.seriesId ||
                        (it.name.equals(targetCat.name, ignoreCase = true) && it.type == targetCat.type)
                }
                .map { it.id }
                .toSet()
        } else {
            emptySet()
        }

        val matchingTransactions = database.transactionDao()
            .getAllActiveTransactions()
            .filter { tx ->
                tx.id == target.id ||
                    (tx.type == target.type &&
                        tx.description.equals(target.description, ignoreCase = true) &&
                        (target.categoryId == null || tx.categoryId == target.categoryId || tx.categoryId in matchingCategoryIds))
            }
            .map { tx ->
                tx.copy(
                    isDeleted = true,
                    syncStatus = SyncStatus.PENDING_DELETE,
                    updatedAtEpochMillis = nowMillis
                )
            }

        if (matchingTransactions.isNotEmpty()) {
            database.transactionDao().upsertAll(matchingTransactions)
            matchingTransactions.forEach { tx ->
                remoteSync.pushTransaction(tx)
            }
        } else {
            database.transactionDao().softDeleteById(id, nowMillis)
        }
    }

    override suspend fun saveInstallmentContract(contract: InstallmentContractEntity) {
        val pending = contract.copy(
            updatedAtEpochMillis = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
        database.installmentContractDao().upsert(pending)
        if (remoteSync.pushInstallmentContract(pending)) {
            database.installmentContractDao().upsert(pending.copy(syncStatus = SyncStatus.SYNCED))
        }
    }

    override suspend fun deleteInstallmentContract(id: String) {
        database.installmentContractDao().softDeleteById(id)
    }

    override suspend fun updateExchangeRate(
        year: Int,
        month: Int,
        eurToBrlRate: Double,
        userId: String
    ) {
        val rateEntity = ExchangeRateEntity(
            year = year,
            month = month,
            eurToBrlRate = eurToBrlRate,
            lastUpdatedDate = LocalDate.now(),
            updatedByUserId = userId,
            updatedAtEpochMillis = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
        database.exchangeRateDao().upsert(rateEntity)
        if (remoteSync.pushExchangeRate(rateEntity)) {
            database.exchangeRateDao().upsert(rateEntity.copy(syncStatus = SyncStatus.SYNCED))
        }
    }

    override suspend fun saveCategory(category: CategoryEntity, applyToSubsequentMonths: Boolean) {
        val nowMillis = System.currentTimeMillis()
        val normalizedEnvelope = if (category.type == TransactionType.INCOME) null else category.envelopeName
        val currentMonthEntity = category.copy(
            envelopeName = normalizedEnvelope,
            updatedAtEpochMillis = nowMillis,
            syncStatus = SyncStatus.PENDING_UPLOAD
        )

        val entitiesToSave = mutableListOf(currentMonthEntity)

        if (applyToSubsequentMonths) {
            val allActiveCategories = database.categoryDao().getAllActiveCategories()
            val futureYears = allActiveCategories
                .map { it.year }
                .filter { it > category.year }
                .distinct()
                .sorted()

            val targetYearMonths = buildList {
                for (m in (category.month + 1)..12) {
                    add(category.year to m)
                }
                for (y in futureYears) {
                    for (m in 1..12) {
                        add(y to m)
                    }
                }
            }

            for ((targetYear, targetMonth) in targetYearMonths) {
                val existingInTargetMonth = allActiveCategories.firstOrNull {
                    it.year == targetYear &&
                        it.month == targetMonth &&
                        (it.seriesId == category.seriesId ||
                            (it.name.equals(category.name, ignoreCase = true) && it.type == category.type))
                }
                val targetEntity = if (existingInTargetMonth != null) {
                    existingInTargetMonth.copy(
                        seriesId = category.seriesId,
                        name = category.name,
                        type = category.type,
                        envelopeName = normalizedEnvelope,
                        plannedAmountCents = category.plannedAmountCents,
                        plannedPercentage = category.plannedPercentage,
                        currency = category.currency,
                        updatedAtEpochMillis = nowMillis,
                        syncStatus = SyncStatus.PENDING_UPLOAD
                    )
                } else {
                    CategoryEntity(
                        id = UUID.randomUUID().toString(),
                        seriesId = category.seriesId,
                        year = targetYear,
                        month = targetMonth,
                        name = category.name,
                        type = category.type,
                        envelopeName = normalizedEnvelope,
                        plannedAmountCents = category.plannedAmountCents,
                        plannedPercentage = category.plannedPercentage,
                        currency = category.currency,
                        iconKey = category.iconKey,
                        colorHex = category.colorHex,
                        sortOrder = category.sortOrder,
                        updatedAtEpochMillis = nowMillis,
                        syncStatus = SyncStatus.PENDING_UPLOAD
                    )
                }
                entitiesToSave.add(targetEntity)
            }
        }

        database.categoryDao().upsertAll(entitiesToSave)
        entitiesToSave.forEach { entity ->
            if (remoteSync.pushCategory(entity)) {
                database.categoryDao().upsert(entity.copy(syncStatus = SyncStatus.SYNCED))
            }
        }
    }

    override suspend fun deleteCategory(id: String, applyToSubsequentMonths: Boolean) {
        val nowMillis = System.currentTimeMillis()
        val target = database.categoryDao().getCategoryById(id)
        if (target == null) {
            database.categoryDao().softDeleteById(id, nowMillis)
            return
        }

        val allCategories = database.categoryDao().getAllCategories()
        val seriesCategoryIds = allCategories
            .filter {
                it.id == target.id ||
                    it.seriesId == target.seriesId ||
                    (it.name.equals(target.name, ignoreCase = true) && it.type == target.type)
            }
            .map { it.id }
            .toSet()

        if (!applyToSubsequentMonths) {
            // Delete only this month's category AND all entries belonging to this category in this month
            val deletedCategory = target.copy(
                isDeleted = true,
                syncStatus = SyncStatus.PENDING_DELETE,
                updatedAtEpochMillis = nowMillis
            )
            database.categoryDao().upsert(deletedCategory)
            remoteSync.pushCategory(deletedCategory)

            val monthTransactionsToDelete = database.transactionDao()
                .getActiveTransactionsForMonth(target.year, target.month)
                .filter { tx -> tx.categoryId != null && tx.categoryId in seriesCategoryIds }
                .map { tx ->
                    tx.copy(
                        isDeleted = true,
                        syncStatus = SyncStatus.PENDING_DELETE,
                        updatedAtEpochMillis = nowMillis
                    )
                }

            if (monthTransactionsToDelete.isNotEmpty()) {
                database.transactionDao().upsertAll(monthTransactionsToDelete)
                monthTransactionsToDelete.forEach { tx ->
                    remoteSync.pushTransaction(tx)
                }
            }
            return
        }

        // Delete this category across all months and years AND all entries/contracts belonging to it
        val allMatchingCategories = database.categoryDao()
            .getAllActiveCategories()
            .filter { it.id in seriesCategoryIds }
            .map {
                it.copy(
                    isDeleted = true,
                    syncStatus = SyncStatus.PENDING_DELETE,
                    updatedAtEpochMillis = nowMillis
                )
            }

        if (allMatchingCategories.isNotEmpty()) {
            database.categoryDao().upsertAll(allMatchingCategories)
            allMatchingCategories.forEach { entity ->
                remoteSync.pushCategory(entity)
            }
        } else {
            database.categoryDao().softDeleteById(id, nowMillis)
        }

        val allTransactionsToDelete = database.transactionDao()
            .getAllActiveTransactions()
            .filter { tx -> tx.categoryId != null && tx.categoryId in seriesCategoryIds }
            .map { tx ->
                tx.copy(
                    isDeleted = true,
                    syncStatus = SyncStatus.PENDING_DELETE,
                    updatedAtEpochMillis = nowMillis
                )
            }

        if (allTransactionsToDelete.isNotEmpty()) {
            database.transactionDao().upsertAll(allTransactionsToDelete)
            allTransactionsToDelete.forEach { tx ->
                remoteSync.pushTransaction(tx)
            }
        }

        val allContractsToDelete = database.installmentContractDao()
            .getAllActiveContracts()
            .filter { contract -> contract.categoryId != null && contract.categoryId in seriesCategoryIds }
            .map { contract ->
                contract.copy(
                    isDeleted = true,
                    syncStatus = SyncStatus.PENDING_DELETE,
                    updatedAtEpochMillis = nowMillis
                )
            }

        if (allContractsToDelete.isNotEmpty()) {
            database.installmentContractDao().upsertAll(allContractsToDelete)
            allContractsToDelete.forEach { contract ->
                remoteSync.pushInstallmentContract(contract)
            }
        }
    }

    override suspend fun initializeYearFromPreviousDecember(sourceYear: Int, targetYear: Int) {
        val nowMillis = System.currentTimeMillis()

        // Clear any leftover categories/transactions/rates in targetYear so it cleanly replicates sourceYear December
        database.categoryDao().softDeleteByYear(targetYear, nowMillis)
        database.transactionDao().softDeleteByYear(targetYear, nowMillis)
        database.exchangeRateDao().deleteByYear(targetYear)

        // 1. Copy December categories of sourceYear (or latest available month in sourceYear) into months 1..12 of targetYear
        val sourceYearCategories = database.categoryDao().getActiveCategoriesForYear(sourceYear)
        val referenceCatMonth = if (sourceYearCategories.any { it.month == 12 }) {
            12
        } else {
            sourceYearCategories.maxOfOrNull { it.month } ?: 12
        }
        val decCategories = sourceYearCategories.filter { it.month == referenceCatMonth }
        val decCategoryIdToTargetMonthIds = mutableMapOf<String, MutableMap<Int, String>>()
        val decSeriesIdToTargetMonthIds = mutableMapOf<String, MutableMap<Int, String>>()
        val newCategories = mutableListOf<CategoryEntity>()

        for (decCat in decCategories) {
            val monthMap = mutableMapOf<Int, String>()
            for (month in 1..12) {
                val newCatId = UUID.randomUUID().toString()
                monthMap[month] = newCatId
                newCategories.add(
                    decCat.copy(
                        id = newCatId,
                        seriesId = decCat.seriesId,
                        year = targetYear,
                        month = month,
                        updatedAtEpochMillis = nowMillis,
                        isDeleted = false,
                        syncStatus = SyncStatus.PENDING_UPLOAD
                    )
                )
            }
            decCategoryIdToTargetMonthIds[decCat.id] = monthMap
            decSeriesIdToTargetMonthIds[decCat.seriesId] = monthMap
        }

        if (newCategories.isNotEmpty()) {
            database.categoryDao().upsertAll(newCategories)
            newCategories.forEach { cat ->
                if (remoteSync.pushCategory(cat)) {
                    database.categoryDao().upsert(cat.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }

        // 2. Copy December exchange rate of sourceYear (or latest available month in sourceYear) into months 1..12 of targetYear
        val decRate = database.exchangeRateDao().getRateForMonth(sourceYear, 12)
            ?: database.exchangeRateDao().getRatesForYear(sourceYear).maxByOrNull { it.month }
        if (decRate != null) {
            val newRates = (1..12).map { month ->
                ExchangeRateEntity(
                    year = targetYear,
                    month = month,
                    eurToBrlRate = decRate.eurToBrlRate,
                    lastUpdatedDate = LocalDate.now(),
                    updatedByUserId = decRate.updatedByUserId,
                    updatedAtEpochMillis = nowMillis,
                    syncStatus = SyncStatus.PENDING_UPLOAD
                )
            }
            database.exchangeRateDao().upsertAll(newRates)
            newRates.forEach { rate ->
                if (remoteSync.pushExchangeRate(rate)) {
                    database.exchangeRateDao().upsert(rate.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }

        // 3. Copy December transactions of sourceYear into months 1..12 of targetYear
        val sourceYearTransactions = database.transactionDao().getActiveTransactionsForYear(sourceYear)
        val referenceTxMonth = if (sourceYearTransactions.any { it.month == 12 }) {
            12
        } else {
            sourceYearTransactions.maxOfOrNull { it.month } ?: 12
        }
        val decTransactions = sourceYearTransactions.filter { it.month == referenceTxMonth }
        val sourceCatById = sourceYearCategories.associateBy { it.id }

        if (decTransactions.isNotEmpty()) {
            val newTransactions = mutableListOf<TransactionEntity>()
            for (tx in decTransactions) {
                val safeDay = tx.date.dayOfMonth.coerceAtMost(28)
                val sourceCategory = tx.categoryId?.let { sourceCatById[it] }
                for (month in 1..12) {
                    val mappedCatId = tx.categoryId?.let { origCatId ->
                        decCategoryIdToTargetMonthIds[origCatId]?.get(month)
                            ?: sourceCategory?.let { sc -> decSeriesIdToTargetMonthIds[sc.seriesId]?.get(month) }
                    }
                    newTransactions.add(
                        tx.copy(
                            id = UUID.randomUUID().toString(),
                            date = LocalDate.of(targetYear, month, safeDay),
                            year = targetYear,
                            month = month,
                            categoryId = mappedCatId,
                            updatedAtEpochMillis = nowMillis,
                            isDeleted = false,
                            syncStatus = SyncStatus.PENDING_UPLOAD
                        )
                    )
                }
            }
            database.transactionDao().upsertAll(newTransactions)
            newTransactions.forEach { tx ->
                if (remoteSync.pushTransaction(tx)) {
                    database.transactionDao().upsert(tx.copy(syncStatus = SyncStatus.SYNCED))
                }
            }
        }
    }

    override suspend fun deleteYear(year: Int) {
        val nowMillis = System.currentTimeMillis()
        val yearCategories = database.categoryDao().getActiveCategoriesForYear(year)
        val yearTransactions = database.transactionDao().getActiveTransactionsForYear(year)

        database.categoryDao().softDeleteByYear(year, nowMillis)
        database.transactionDao().softDeleteByYear(year, nowMillis)
        database.exchangeRateDao().deleteByYear(year)

        yearCategories.forEach { cat ->
            remoteSync.pushCategory(
                cat.copy(
                    isDeleted = true,
                    syncStatus = SyncStatus.PENDING_DELETE,
                    updatedAtEpochMillis = nowMillis
                )
            )
        }
        yearTransactions.forEach { tx ->
            remoteSync.pushTransaction(
                tx.copy(
                    isDeleted = true,
                    syncStatus = SyncStatus.PENDING_DELETE,
                    updatedAtEpochMillis = nowMillis
                )
            )
        }
    }

    override suspend fun saveBudgetBucket(bucketName: String) {
        val cleanName = bucketName.trim()
        if (cleanName.isEmpty()) return
        val entity = BudgetBucketEntity(
            bucketName = cleanName,
            updatedAtEpochMillis = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
        database.budgetBucketDao().upsert(entity)
        if (remoteSync.pushBudgetBucket(entity)) {
            database.budgetBucketDao().upsert(entity.copy(syncStatus = SyncStatus.SYNCED))
        }
    }

    override suspend fun saveGoal(goal: GoalEntity) {
        val pending = goal.copy(
            updatedAtEpochMillis = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
        database.goalDao().upsert(pending)
        if (remoteSync.pushGoal(pending)) {
            database.goalDao().upsert(pending.copy(syncStatus = SyncStatus.SYNCED))
        }
    }

    override suspend fun deleteGoal(id: String) {
        database.goalDao().softDeleteById(id)
    }
}
