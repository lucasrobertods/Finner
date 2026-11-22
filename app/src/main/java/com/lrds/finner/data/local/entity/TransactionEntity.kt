package com.lrds.finner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["year", "month"]),
        Index(value = ["category_id"]),
        Index(value = ["user_id"]),
        Index(value = ["sync_status"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "description")
    val description: String,

    /** Stored in minor units (cents/centavos), e.g., €15.90 -> 1590L */
    @ColumnInfo(name = "amount_cents")
    val amountCents: Long,

    @ColumnInfo(name = "currency")
    val currency: CurrencyCode,

    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "year")
    val year: Int = date.year,

    @ColumnInfo(name = "month")
    val month: Int = date.monthValue,

    @ColumnInfo(name = "category_id")
    val categoryId: String?,

    @ColumnInfo(name = "type")
    val type: TransactionType,

    /** Authenticated user ID or device member ID */
    @ColumnInfo(name = "user_id")
    val userId: String,

    /** Human-readable name of the household member who logged this transaction */
    @ColumnInfo(name = "user_display_name")
    val userDisplayName: String = "",

    @ColumnInfo(name = "is_paid")
    val isPaid: Boolean = true,

    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
)
