package com.lrds.finner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["year", "month"]),
        Index(value = ["series_id"]),
        Index(value = ["envelope_name"]),
        Index(value = ["type"])
    ]
)
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    /**
     * Links the same logical category across different months of a year so edits can
     * optionally propagate to subsequent months.
     */
    @ColumnInfo(name = "series_id")
    val seriesId: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "year")
    val year: Int = LocalDate.now().year,

    @ColumnInfo(name = "month")
    val month: Int = LocalDate.now().monthValue,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "type")
    val type: TransactionType = TransactionType.EXPENSE_LOCAL,

    /**
     * Bucket/Envelope name (e.g., "Bills", "Reserve", "Joint", "Personal", or a custom user bucket).
     * Must be null for [TransactionType.INCOME] ("Entradas").
     */
    @ColumnInfo(name = "envelope_name")
    val envelopeName: String? = if (type == TransactionType.INCOME) null else EnvelopeType.BILLS.displayName,

    /**
     * Monthly planned budget ("PREVISTO" column in the spreadsheet) in minor units (cents/centavos).
     */
    @ColumnInfo(name = "planned_amount_cents")
    val plannedAmountCents: Long = 0L,

    /**
     * Optional percentage (0..100+) of the month's total planned inflows ("Entradas").
     * When non-null and > 0, the category's planned value dynamically reflects this percentage
     * of the month's planned inflows.
     */
    @ColumnInfo(name = "planned_percentage")
    val plannedPercentage: Double? = null,

    @ColumnInfo(name = "currency")
    val currency: CurrencyCode = CurrencyCode.EUR,

    @ColumnInfo(name = "icon_key")
    val iconKey: String = "category",

    @ColumnInfo(name = "color_hex")
    val colorHex: Long = 0xFF2E7D32,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
)
