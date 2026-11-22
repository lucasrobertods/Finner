package com.lrds.finner.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lrds.finner.data.local.entity.BudgetBucketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetBucketDao {
    @Query("SELECT * FROM budget_buckets WHERE is_deleted = 0 ORDER BY sort_order ASC, bucket_name ASC")
    fun observeActiveBuckets(): Flow<List<BudgetBucketEntity>>

    @Upsert
    suspend fun upsert(bucket: BudgetBucketEntity)

    @Upsert
    suspend fun upsertAll(buckets: List<BudgetBucketEntity>)
}
