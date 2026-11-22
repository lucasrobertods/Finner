package com.lrds.finner.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lrds.finner.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE is_deleted = 0 ORDER BY priority ASC, target_date ASC")
    fun observeActiveGoals(): Flow<List<GoalEntity>>

    @Upsert
    suspend fun upsert(goal: GoalEntity)

    @Upsert
    suspend fun upsertAll(goals: List<GoalEntity>)

    @Query("UPDATE goals SET is_deleted = 1, sync_status = 'PENDING_DELETE', updated_at = :timestamp WHERE id = :id")
    suspend fun softDeleteById(id: String, timestamp: Long = System.currentTimeMillis())
}
