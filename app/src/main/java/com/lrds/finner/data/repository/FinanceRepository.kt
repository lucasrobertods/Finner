package com.lrds.finner.data.repository

import com.lrds.finner.data.local.entity.BudgetBucketEntity
import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.ExchangeRateEntity
import com.lrds.finner.data.local.entity.GoalEntity
import com.lrds.finner.data.local.entity.InstallmentContractEntity
import com.lrds.finner.data.local.entity.TransactionEntity
import com.lrds.finner.data.remote.CloudSyncState
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    val cloudSyncState: CloudSyncState

    fun startRealtimeSync(householdId: String)
    fun observeCategories(): Flow<List<CategoryEntity>>
    fun observeCategoriesForMonth(year: Int, month: Int): Flow<List<CategoryEntity>>
    fun observeTransactionsForMonth(year: Int, month: Int): Flow<List<TransactionEntity>>
    fun observeTransactionsForYear(year: Int): Flow<List<TransactionEntity>>
    fun observeInstallmentContracts(): Flow<List<InstallmentContractEntity>>
    fun observeExchangeRateForMonth(year: Int, month: Int): Flow<ExchangeRateEntity?>
    fun observeExchangeRatesForYear(year: Int): Flow<List<ExchangeRateEntity>>
    fun observeBudgetBuckets(): Flow<List<BudgetBucketEntity>>
    fun observeGoals(): Flow<List<GoalEntity>>

    suspend fun saveTransaction(
        transaction: TransactionEntity,
        applyToSubsequentMonths: Boolean = false,
        originalTransactionForSeriesMatch: TransactionEntity? = null
    )
    suspend fun deleteTransaction(id: String, applyToSubsequentMonths: Boolean = false)
    suspend fun saveInstallmentContract(contract: InstallmentContractEntity)
    suspend fun deleteInstallmentContract(id: String)
    suspend fun updateExchangeRate(year: Int, month: Int, eurToBrlRate: Double, userId: String)
    suspend fun saveCategory(category: CategoryEntity, applyToSubsequentMonths: Boolean = false)
    suspend fun deleteCategory(id: String, applyToSubsequentMonths: Boolean = false)
    suspend fun saveBudgetBucket(bucketName: String)
    suspend fun saveGoal(goal: GoalEntity)
    suspend fun deleteGoal(id: String)
    suspend fun initializeYearFromPreviousDecember(sourceYear: Int, targetYear: Int)
    suspend fun deleteYear(year: Int)
}
