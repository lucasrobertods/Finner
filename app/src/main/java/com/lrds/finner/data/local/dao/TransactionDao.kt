package com.lrds.finner.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lrds.finner.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT * FROM transactions
        WHERE year = :year AND month = :month AND is_deleted = 0
        ORDER BY date DESC, updated_at DESC
        """
    )
    fun observeTransactionsForMonth(year: Int, month: Int): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE year = :year AND is_deleted = 0
        ORDER BY month ASC, date DESC
        """
    )
    fun observeTransactionsForYear(year: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE is_deleted = 0")
    suspend fun getAllActiveTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE year = :year AND is_deleted = 0")
    suspend fun getActiveTransactionsForYear(year: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE year = :year AND month = :month AND is_deleted = 0")
    suspend fun getActiveTransactionsForMonth(year: Int, month: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE year = :year AND month >= :startMonth AND is_deleted = 0")
    suspend fun getActiveTransactionsFromMonth(year: Int, startMonth: Int): List<TransactionEntity>

    @Upsert
    suspend fun upsert(transaction: TransactionEntity)

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Query(
        """
        UPDATE transactions
        SET is_deleted = 1, sync_status = 'PENDING_DELETE', updated_at = :timestamp
        WHERE id = :id
        """
    )
    suspend fun softDeleteById(id: String, timestamp: Long = System.currentTimeMillis())

    @Query(
        """
        UPDATE transactions
        SET is_deleted = 1, sync_status = 'PENDING_DELETE', updated_at = :timestamp
        WHERE year = :year AND is_deleted = 0
        """
    )
    suspend fun softDeleteByYear(year: Int, timestamp: Long = System.currentTimeMillis())
}
