package com.lrds.finner.domain.model

import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.InstallmentMonthProjection
import com.lrds.finner.data.local.entity.TransactionEntity
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs

data class MonthlyDashboardData(
    val year: Int,
    val month: Int,
    val eurToBrlRate: Double,
    val hasManualExchangeRate: Boolean = false,
    val lastUpdatedDate: LocalDate,
    val plannedIncomeEurCents: Long = 0L,
    val plannedOutflowsEurCents: Long = 0L,
    /** Planned Spare Cash in EUR: Planned Inflows - Planned Outflows (feeds Planned "Personal" bucket) */
    val plannedSpareCashEurCents: Long = 0L,
    val plannedSpareCashBrlCents: Long = 0L,
    val totalIncomeEurCents: Long,
    val totalLocalExpenseEurCents: Long,
    val totalBrazilExpenseBrlCents: Long,
    val totalBrazilExpenseEurCents: Long,
    val totalOutflowsEurCents: Long,
    /** Actual Spare Cash in EUR: Actual Inflows - Actual Outflows (feeds Actual "Personal" bucket) */
    val faltaSobraEurCents: Long,
    val faltaSobraBrlCents: Long,
    val activeInstallments: List<ActiveInstallmentItem>,
    val categoryReconciliations: List<CategoryReconciliationRow>,
    val envelopeSummaries: List<EnvelopeSummary>,
    val transactions: List<TransactionEntity>
)

data class ActiveInstallmentItem(
    val projection: InstallmentMonthProjection,
    val amountOriginalCents: Long,
    val currency: CurrencyCode,
    val amountEurCents: Long,
    val categoryName: String,
    val isPaid: Boolean = false
)

/**
 * Spreadsheet reconciliation row: PREVISTO | REALIZADO | DISPONÍVEL (PREVISTO - REALIZADO)
 */
data class CategoryReconciliationRow(
    val category: CategoryEntity,
    val previstoCents: Long,
    val realizadoCents: Long,
    val disponivelCents: Long,
    val previstoEurCents: Long,
    val realizadoEurCents: Long,
    val transactions: List<TransactionEntity>,
    val installments: List<ActiveInstallmentItem>
)

data class EnvelopeSummary(
    val bucketName: String,
    /** Proportion (%) of the planned bucket amount against the planned income for the month */
    val spentOfIncomePercentage: Double,
    val plannedEurCents: Long,
    val actualEurCents: Long,
    val totalIncomeEurCents: Long
) {
    val formattedPercentage: String
        get() {
            val roundedInt = kotlin.math.round(spentOfIncomePercentage).toInt()
            return if (abs(spentOfIncomePercentage - roundedInt) < 0.05) {
                "$roundedInt%"
            } else {
                String.format(Locale.forLanguageTag("pt-BR"), "%.1f%%", spentOfIncomePercentage)
            }
        }

    val referenceBudgetEurCents: Long
        get() = plannedEurCents

    /**
     * Progress bar fraction based on Realizado (actualEurCents) against Previsto (plannedEurCents)
     * for the bucket.
     */
    val usageFraction: Float
        get() = if (plannedEurCents > 0L) {
            (actualEurCents.toFloat() / plannedEurCents.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}
