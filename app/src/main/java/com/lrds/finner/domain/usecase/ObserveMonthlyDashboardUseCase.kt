package com.lrds.finner.domain.usecase

import com.lrds.finner.core.currency.CurrencyConverter
import com.lrds.finner.data.local.entity.BudgetBucketEntity
import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.EnvelopeType
import com.lrds.finner.data.local.entity.ExchangeRateEntity
import com.lrds.finner.data.local.entity.InstallmentContractEntity
import com.lrds.finner.data.local.entity.TransactionEntity
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.data.repository.FinanceRepository
import com.lrds.finner.domain.model.ActiveInstallmentItem
import com.lrds.finner.domain.model.CategoryReconciliationRow
import com.lrds.finner.domain.model.EnvelopeSummary
import com.lrds.finner.domain.model.MonthlyDashboardData
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveMonthlyDashboardUseCase(
    private val repository: FinanceRepository
) {
    operator fun invoke(
        year: Int,
        month: Int,
        selectedUserFilter: String? = null
    ): Flow<MonthlyDashboardData> {
        return combine(
            repository.observeCategories(),
            repository.observeTransactionsForMonth(year, month),
            repository.observeInstallmentContracts(),
            repository.observeExchangeRateForMonth(year, month),
            repository.observeBudgetBuckets()
        ) { categories, allTransactions, contracts, exchangeRate, buckets ->
            buildMonthlyDashboardData(
                year = year,
                month = month,
                selectedUserFilter = selectedUserFilter,
                categories = categories,
                allTransactions = allTransactions,
                contracts = contracts,
                exchangeRate = exchangeRate,
                buckets = buckets
            )
        }
    }

    companion object {
        fun buildMonthlyDashboardData(
            year: Int,
            month: Int,
            selectedUserFilter: String?,
            categories: List<CategoryEntity>,
            allTransactions: List<TransactionEntity>,
            contracts: List<InstallmentContractEntity>,
            exchangeRate: ExchangeRateEntity?,
            buckets: List<BudgetBucketEntity>
        ): MonthlyDashboardData {
            val eurToBrlRate = exchangeRate?.eurToBrlRate ?: CurrencyConverter.DEFAULT_EUR_TO_BRL_RATE
            val lastUpdatedDate = exchangeRate?.lastUpdatedDate ?: LocalDate.of(year, month, 1)
            val allCategoryById = categories.associateBy { it.id }

            // Categories independent for this (year, month), ordered by bucket then by name
            val monthCategories = categories
                .filter { it.year == year && it.month == month }
                .sortedWith(
                    compareBy<CategoryEntity> { it.type.ordinal }
                        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.envelopeName ?: "" }
                        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                )

            val filteredTransactions = if (selectedUserFilter.isNullOrBlank()) {
                allTransactions
            } else {
                allTransactions.filter { it.userId.equals(selectedUserFilter, ignoreCase = true) }
            }

            val filteredContracts = if (selectedUserFilter.isNullOrBlank()) {
                contracts
            } else {
                contracts.filter { it.userId.equals(selectedUserFilter, ignoreCase = true) }
            }

            // 1. Project active installments for this (year, month)
            val activeInstallments = filteredContracts.mapNotNull { contract ->
                val projection = contract.projectForMonth(year, month) ?: return@mapNotNull null
                val eurCents = CurrencyConverter.toEurCents(
                    amountCents = contract.monthlyAmountCents,
                    currency = contract.currency,
                    eurToBrlRate = eurToBrlRate
                )
                val catName = contract.categoryId?.let { allCategoryById[it]?.name } ?: "Brazil / Installments"
                ActiveInstallmentItem(
                    projection = projection,
                    amountOriginalCents = contract.monthlyAmountCents,
                    currency = contract.currency,
                    amountEurCents = eurCents,
                    categoryName = catName,
                    isPaid = contract.isPaidForMonth(year, month)
                )
            }

            // 2. Calculate Income ("ENTRADAS") in EUR
            val totalIncomeEurCents = filteredTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { tx ->
                    CurrencyConverter.toEurCents(tx.amountCents, tx.currency, eurToBrlRate)
                }

            // 3. Calculate Local Expenses ("SAÍDAS") in EUR (transactions + any local EUR installments)
            val localTransactionsEurCents = filteredTransactions
                .filter { it.type == TransactionType.EXPENSE_LOCAL }
                .sumOf { tx ->
                    CurrencyConverter.toEurCents(tx.amountCents, tx.currency, eurToBrlRate)
                }
            val localInstallmentsEurCents = activeInstallments
                .filter { it.isPaid && it.projection.contract.type == TransactionType.EXPENSE_LOCAL }
                .sumOf { it.amountEurCents }
            val totalLocalExpenseEurCents = localTransactionsEurCents + localInstallmentsEurCents

            // 4. Calculate Brazil Commitments ("Brasil") in BRL and converted EUR
            val brazilTransactionsBrlCents = filteredTransactions
                .filter { it.type == TransactionType.EXPENSE_BRAZIL }
                .sumOf { tx ->
                    when (tx.currency) {
                        CurrencyCode.BRL -> tx.amountCents
                        CurrencyCode.EUR -> CurrencyConverter.eurCentsToBrlCents(tx.amountCents, eurToBrlRate)
                    }
                }
            val brazilInstallmentsBrlCents = activeInstallments
                .filter { it.isPaid && it.projection.contract.type == TransactionType.EXPENSE_BRAZIL }
                .sumOf { item ->
                    when (item.currency) {
                        CurrencyCode.BRL -> item.amountOriginalCents
                        CurrencyCode.EUR -> CurrencyConverter.eurCentsToBrlCents(item.amountOriginalCents, eurToBrlRate)
                    }
                }
            val totalBrazilExpenseBrlCents = brazilTransactionsBrlCents + brazilInstallmentsBrlCents
            val totalBrazilExpenseEurCents = CurrencyConverter.brlCentsToEurCents(
                brlCents = totalBrazilExpenseBrlCents,
                eurToBrlRate = eurToBrlRate
            )

            // 5. Net Monthly Balance ("FALTA / SOBRA") in EUR and BRL
            val totalOutflowsEurCents = totalLocalExpenseEurCents + totalBrazilExpenseEurCents
            val faltaSobraEurCents = totalIncomeEurCents - totalOutflowsEurCents
            val faltaSobraBrlCents = CurrencyConverter.eurCentsToBrlCents(faltaSobraEurCents, eurToBrlRate)

            // Helper to check if a transaction or contract categoryId belongs to this month's category
            fun matchesMonthCategory(referencedCategoryId: String?, targetCategory: CategoryEntity): Boolean {
                if (referencedCategoryId == null) return false
                if (referencedCategoryId == targetCategory.id) return true
                val referenced = allCategoryById[referencedCategoryId] ?: return false
                return referenced.seriesId == targetCategory.seriesId ||
                    (referenced.name.equals(targetCategory.name, ignoreCase = true) &&
                        referenced.type == targetCategory.type)
            }

            // 6. Calculate Planned Inflows first so percentage-based categories dynamically reflect it
            val rawPlannedIncomeEurCents = monthCategories
                .filter { it.type == TransactionType.INCOME }
                .sumOf { cat ->
                    CurrencyConverter.toEurCents(
                        amountCents = cat.plannedAmountCents,
                        currency = cat.currency,
                        eurToBrlRate = eurToBrlRate
                    )
                }
            val effectivePlannedIncomeForPercentagesEurCents = if (rawPlannedIncomeEurCents > 0L) {
                rawPlannedIncomeEurCents
            } else {
                totalIncomeEurCents
            }

            // 6b. Category Reconciliation: PREVISTO | REALIZADO | DISPONÍVEL = PREVISTO - REALIZADO
            // For Brazil categories (EXPENSE_BRAZIL):
            // - Planned (previstoCents) is in BRL (R$)
            // - Planned in EUR (previstoEurCents) = planned BRL / euro rate
            // - Actual (realizadoCents / realizadoEurCents) is in EUR (€)
            // - Available (disponivelCents) is in EUR (€): (planned / euro rate) - actual
            val categoryReconciliations = monthCategories.map { category ->
                val catTxs = filteredTransactions.filter { matchesMonthCategory(it.categoryId, category) }
                val catInstallments = activeInstallments.filter {
                    matchesMonthCategory(it.projection.contract.categoryId, category)
                }

                if (category.type == TransactionType.EXPENSE_BRAZIL) {
                    val txEurSum = catTxs.sumOf { tx ->
                        CurrencyConverter.toEurCents(tx.amountCents, tx.currency, eurToBrlRate)
                    }
                    val instEurSum = catInstallments
                        .filter { it.isPaid }
                        .sumOf { inst -> inst.amountEurCents }
                    val realizadoEurCents = txEurSum + instEurSum

                    val pct = category.plannedPercentage
                    val previstoBrlCents = if (pct != null && pct > 0.0) {
                        val baseBrl = CurrencyConverter.eurCentsToBrlCents(
                            effectivePlannedIncomeForPercentagesEurCents,
                            eurToBrlRate
                        )
                        kotlin.math.round(baseBrl.toDouble() * (pct / 100.0)).toLong()
                    } else if (category.currency == CurrencyCode.BRL) {
                        category.plannedAmountCents
                    } else {
                        CurrencyConverter.eurCentsToBrlCents(category.plannedAmountCents, eurToBrlRate)
                    }
                    val previstoEurCents = CurrencyConverter.brlCentsToEurCents(
                        brlCents = previstoBrlCents,
                        eurToBrlRate = eurToBrlRate
                    )
                    val disponivelEurCents = previstoEurCents - realizadoEurCents

                    CategoryReconciliationRow(
                        category = category.copy(currency = CurrencyCode.BRL),
                        previstoCents = previstoBrlCents,
                        realizadoCents = realizadoEurCents,
                        disponivelCents = disponivelEurCents,
                        previstoEurCents = previstoEurCents,
                        realizadoEurCents = realizadoEurCents,
                        transactions = catTxs,
                        installments = catInstallments
                    )
                } else {
                    val txSumInCategoryCurrency = catTxs.sumOf { tx ->
                        if (tx.currency == category.currency) {
                            tx.amountCents
                        } else if (category.currency == CurrencyCode.EUR) {
                            CurrencyConverter.toEurCents(tx.amountCents, tx.currency, eurToBrlRate)
                        } else {
                            CurrencyConverter.eurCentsToBrlCents(tx.amountCents, eurToBrlRate)
                        }
                    }
                    val instSumInCategoryCurrency = catInstallments
                        .filter { it.isPaid }
                        .sumOf { inst ->
                        if (inst.currency == category.currency) {
                            inst.amountOriginalCents
                        } else if (category.currency == CurrencyCode.EUR) {
                            inst.amountEurCents
                        } else {
                            CurrencyConverter.eurCentsToBrlCents(inst.amountEurCents, eurToBrlRate)
                        }
                    }

                    val realizadoCents = txSumInCategoryCurrency + instSumInCategoryCurrency
                    val pct = category.plannedPercentage
                    val previstoCents = if (category.type != TransactionType.INCOME && pct != null && pct > 0.0) {
                        val baseInCategoryCurrency = when (category.currency) {
                            CurrencyCode.EUR -> effectivePlannedIncomeForPercentagesEurCents
                            CurrencyCode.BRL -> CurrencyConverter.eurCentsToBrlCents(
                                effectivePlannedIncomeForPercentagesEurCents,
                                eurToBrlRate
                            )
                        }
                        kotlin.math.round(baseInCategoryCurrency.toDouble() * (pct / 100.0)).toLong()
                    } else {
                        category.plannedAmountCents
                    }
                    val disponivelCents = previstoCents - realizadoCents
                    val previstoEurCents = CurrencyConverter.toEurCents(
                        amountCents = previstoCents,
                        currency = category.currency,
                        eurToBrlRate = eurToBrlRate
                    )
                    val realizadoEurCents = CurrencyConverter.toEurCents(
                        amountCents = realizadoCents,
                        currency = category.currency,
                        eurToBrlRate = eurToBrlRate
                    )

                    CategoryReconciliationRow(
                        category = category,
                        previstoCents = previstoCents,
                        realizadoCents = realizadoCents,
                        disponivelCents = disponivelCents,
                        previstoEurCents = previstoEurCents,
                        realizadoEurCents = realizadoEurCents,
                        transactions = catTxs,
                        installments = catInstallments
                    )
                }
            }

            // 6c. Planned Inflows, Planned Outflows & Planned Spare Cash in EUR and BRL
            val plannedIncomeEurCents = categoryReconciliations
                .filter { it.category.type == TransactionType.INCOME }
                .sumOf { it.previstoEurCents }
            val plannedOutflowsEurCents = categoryReconciliations
                .filter { it.category.type != TransactionType.INCOME }
                .sumOf { it.previstoEurCents }
            val plannedSpareCashEurCents = plannedIncomeEurCents - plannedOutflowsEurCents
            val plannedSpareCashBrlCents = CurrencyConverter.eurCentsToBrlCents(
                eurCents = plannedSpareCashEurCents,
                eurToBrlRate = eurToBrlRate
            )

            // 7. Envelope / Bucket Summaries:
            // Combine default buckets + custom buckets + any bucket referenced by expense categories in this month.
            val allBucketNames = buildList {
                addAll(EnvelopeType.DEFAULT_BUCKET_NAMES)
                buckets.forEach { b ->
                    if (b.bucketName.isNotBlank() && none { it.equals(b.bucketName, ignoreCase = true) }) {
                        add(b.bucketName)
                    }
                }
                monthCategories.forEach { cat ->
                    val env = cat.envelopeName?.trim()
                    if (cat.type != TransactionType.INCOME && !env.isNullOrEmpty() &&
                        none { it.equals(env, ignoreCase = true) }
                    ) {
                        add(env)
                    }
                }
            }

            val envelopeSummaries = allBucketNames.map { bucketName ->
                val isPersonalBucket = bucketName.equals(EnvelopeType.PERSONAL.displayName, ignoreCase = true)
                val bucketRows = categoryReconciliations.filter { row ->
                    row.category.type != TransactionType.INCOME &&
                        row.category.envelopeName?.equals(bucketName, ignoreCase = true) == true
                }
                val plannedEurCents = if (isPersonalBucket) {
                    plannedSpareCashEurCents
                } else {
                    bucketRows.sumOf { it.previstoEurCents }
                }
                val actualEurCents = if (isPersonalBucket) {
                    faltaSobraEurCents
                } else {
                    bucketRows.sumOf { it.realizadoEurCents }
                }
                val referencePlannedIncomeCents = if (plannedIncomeEurCents > 0L) {
                    plannedIncomeEurCents
                } else {
                    totalIncomeEurCents
                }
                val spentOfIncomePercentage = if (referencePlannedIncomeCents > 0L) {
                    (plannedEurCents.toDouble() / referencePlannedIncomeCents.toDouble()) * 100.0
                } else {
                    0.0
                }

                EnvelopeSummary(
                    bucketName = bucketName,
                    spentOfIncomePercentage = spentOfIncomePercentage,
                    plannedEurCents = plannedEurCents,
                    actualEurCents = actualEurCents,
                    totalIncomeEurCents = totalIncomeEurCents
                )
            }

            return MonthlyDashboardData(
                year = year,
                month = month,
                eurToBrlRate = eurToBrlRate,
                hasManualExchangeRate = exchangeRate != null,
                lastUpdatedDate = lastUpdatedDate,
                plannedIncomeEurCents = plannedIncomeEurCents,
                plannedOutflowsEurCents = plannedOutflowsEurCents,
                plannedSpareCashEurCents = plannedSpareCashEurCents,
                plannedSpareCashBrlCents = plannedSpareCashBrlCents,
                totalIncomeEurCents = totalIncomeEurCents,
                totalLocalExpenseEurCents = totalLocalExpenseEurCents,
                totalBrazilExpenseBrlCents = totalBrazilExpenseBrlCents,
                totalBrazilExpenseEurCents = totalBrazilExpenseEurCents,
                totalOutflowsEurCents = totalOutflowsEurCents,
                faltaSobraEurCents = faltaSobraEurCents,
                faltaSobraBrlCents = faltaSobraBrlCents,
                activeInstallments = activeInstallments,
                categoryReconciliations = categoryReconciliations,
                envelopeSummaries = envelopeSummaries,
                transactions = filteredTransactions
            )
        }
    }
}
