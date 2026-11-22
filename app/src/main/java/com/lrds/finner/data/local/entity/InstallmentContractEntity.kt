package com.lrds.finner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "installment_contracts",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["start_year", "start_month"]),
        Index(value = ["category_id"]),
        Index(value = ["sync_status"])
    ]
)
data class InstallmentContractEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "description")
    val description: String,

    /** Amount per single monthly installment in minor units (cents/centavos) */
    @ColumnInfo(name = "monthly_amount_cents")
    val monthlyAmountCents: Long,

    @ColumnInfo(name = "currency")
    val currency: CurrencyCode = CurrencyCode.BRL,

    /** Anchor year when tracking in this contract starts (e.g. 2026) */
    @ColumnInfo(name = "start_year")
    val startYear: Int,

    /** Anchor month (1 = JAN .. 12 = DEZ) */
    @ColumnInfo(name = "start_month")
    val startMonth: Int,

    /**
     * Installment number at (startYear, startMonth).
     * - Default is 1 for new purchases (e.g. "1/3" in Jan, "2/3" in Feb, "3/3" in Mar, stops in Apr).
     * - Also supports ongoing commitments carried into the year (e.g. 34 of 48).
     */
    @ColumnInfo(name = "initial_installment_number")
    val initialInstallmentNumber: Int = 1,

    /** Total number of installments for the contract (e.g., 3, 12, 48, 360) */
    @ColumnInfo(name = "total_installments")
    val totalInstallments: Int,

    @ColumnInfo(name = "day_of_month")
    val dayOfMonth: Int = 10,

    @ColumnInfo(name = "category_id")
    val categoryId: String?,

    @ColumnInfo(name = "type")
    val type: TransactionType = TransactionType.EXPENSE_BRAZIL,

    @ColumnInfo(name = "user_id")
    val userId: String = "shared",

    @ColumnInfo(name = "user_display_name")
    val userDisplayName: String = "",

    /** Comma-separated "YYYY-MM" keys for months in which this installment has been marked as paid */
    @ColumnInfo(name = "paid_months")
    val paidMonths: String = "",

    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMillis: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.PENDING_UPLOAD
) {
    /** Alias matching the Phase 2 `name` property specification */
    val name: String
        get() = description

    /**
     * Dynamically calculates the current installment number (1..totalInstallments) for a target (year, month),
     * or returns null if the contract has not started yet or has already finished.
     */
    fun currentInstallmentForMonth(targetYear: Int, targetMonth: Int): Int? {
        if (isDeleted || totalInstallments <= 0 || initialInstallmentNumber <= 0) return null
        val startMonthAbsolute = startYear * 12 + (startMonth - 1)
        val targetMonthAbsolute = targetYear * 12 + (targetMonth - 1)
        val monthsElapsed = targetMonthAbsolute - startMonthAbsolute
        if (monthsElapsed < 0) return null

        val currentInstallment = initialInstallmentNumber + monthsElapsed
        return if (currentInstallment in 1..totalInstallments) currentInstallment else null
    }

    /**
     * Projects this installment contract onto a target (year, month).
     * Example: 3 installments starting in Jan 2026:
     * - Jan 2026 -> "1/3"
     * - Feb 2026 -> "2/3"
     * - Mar 2026 -> "3/3"
     * - Apr 2026 -> null (stops in April)
     */
    fun projectForMonth(targetYear: Int, targetMonth: Int): InstallmentMonthProjection? {
        val currentInstallment = currentInstallmentForMonth(targetYear, targetMonth) ?: return null
        val remainingInstallments = totalInstallments - currentInstallment
        return InstallmentMonthProjection(
            contract = this,
            targetYear = targetYear,
            targetMonth = targetMonth,
            currentInstallment = currentInstallment,
            totalInstallments = totalInstallments,
            remainingInstallmentsAfterThis = remainingInstallments,
            badgeText = "$currentInstallment/$totalInstallments",
            progressFraction = (currentInstallment.toFloat() / totalInstallments.toFloat()).coerceIn(0f, 1f)
        )
    }

    fun isPaidForMonth(targetYear: Int, targetMonth: Int): Boolean {
        if (paidMonths.isBlank()) return false
        val key = monthKey(targetYear, targetMonth)
        return paidMonths.split(',').any { it.trim() == key }
    }

    fun withPaidForMonth(targetYear: Int, targetMonth: Int, paid: Boolean): InstallmentContractEntity {
        val key = monthKey(targetYear, targetMonth)
        val current = paidMonths
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toMutableSet()
        if (paid) current.add(key) else current.remove(key)
        return copy(paidMonths = current.sorted().joinToString(","))
    }

    companion object {
        fun monthKey(year: Int, month: Int): String =
            String.format(java.util.Locale.US, "%04d-%02d", year, month)
    }
}

data class InstallmentMonthProjection(
    val contract: InstallmentContractEntity,
    val targetYear: Int,
    val targetMonth: Int,
    val currentInstallment: Int,
    val totalInstallments: Int,
    val remainingInstallmentsAfterThis: Int,
    val badgeText: String,
    val progressFraction: Float
)
