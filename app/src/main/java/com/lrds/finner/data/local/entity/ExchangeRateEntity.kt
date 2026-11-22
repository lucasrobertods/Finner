package com.lrds.finner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.time.LocalDate

@Entity(
    tableName = "exchange_rates",
    primaryKeys = ["year", "month"]
)
data class ExchangeRateEntity(
    @ColumnInfo(name = "year")
    val year: Int,

    @ColumnInfo(name = "month")
    val month: Int,

    /** 1 EUR = eurToBrlRate BRL (e.g., 6.44 in Jan, 5.75 in Aug, 5.50 in Nov) */
    @ColumnInfo(name = "eur_to_brl_rate")
    val eurToBrlRate: Double,

    /** Tracks "última atualização" in the monthly spreadsheet header */
    @ColumnInfo(name = "last_updated_date")
    val lastUpdatedDate: LocalDate = LocalDate.now(),

    @ColumnInfo(name = "updated_by_user_id")
    val updatedByUserId: String = "lucas",

    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
) {
    val documentId: String
        get() = "%04d_%02d".format(year, month)
}
