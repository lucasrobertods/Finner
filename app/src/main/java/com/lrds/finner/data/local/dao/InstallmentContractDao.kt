package com.lrds.finner.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lrds.finner.data.local.entity.InstallmentContractEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentContractDao {
    @Query("SELECT * FROM installment_contracts WHERE is_deleted = 0 ORDER BY start_year ASC, start_month ASC, description ASC")
    fun observeActiveContracts(): Flow<List<InstallmentContractEntity>>

    @Query("SELECT * FROM installment_contracts WHERE is_deleted = 0")
    suspend fun getAllActiveContracts(): List<InstallmentContractEntity>

    @Upsert
    suspend fun upsert(contract: InstallmentContractEntity)

    @Upsert
    suspend fun upsertAll(contracts: List<InstallmentContractEntity>)

    @Query(
        """
        UPDATE installment_contracts
        SET is_deleted = 1, sync_status = 'PENDING_DELETE', updated_at = :timestamp
        WHERE id = :id
        """
    )
    suspend fun softDeleteById(id: String, timestamp: Long = System.currentTimeMillis())
}
