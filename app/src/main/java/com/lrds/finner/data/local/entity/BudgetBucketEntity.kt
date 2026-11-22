package com.lrds.finner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a budget bucket / envelope (default or user-created) in the workspace.
 */
@Entity(tableName = "budget_buckets")
data class BudgetBucketEntity(
    @PrimaryKey
    @ColumnInfo(name = "bucket_name")
    val bucketName: String,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
)
