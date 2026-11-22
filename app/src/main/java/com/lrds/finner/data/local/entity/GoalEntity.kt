package com.lrds.finner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

/**
 * Represents items in the "OBJETIVOS" tab (Emergency Fund, Retirement, Italy trip, Robot vacuum, etc.).
 */
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "target_amount_cents")
    val targetAmountCents: Long,

    @ColumnInfo(name = "current_saved_cents")
    val currentSavedCents: Long = 0L,

    @ColumnInfo(name = "currency")
    val currency: CurrencyCode = CurrencyCode.EUR,

    @ColumnInfo(name = "target_date")
    val targetDate: LocalDate? = null,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "priority")
    val priority: GoalPriority = GoalPriority.SHORT_TERM,

    @ColumnInfo(name = "bucket_name")
    val bucketName: String? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
) {
    val progressFraction: Float
        get() = if (targetAmountCents <= 0L) 0f else (currentSavedCents.toFloat() / targetAmountCents.toFloat()).coerceIn(0f, 1f)
}
