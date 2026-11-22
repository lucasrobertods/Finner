package com.lrds.finner

import com.lrds.finner.core.currency.CurrencyConverter
import com.lrds.finner.data.local.entity.BudgetBucketEntity
import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.ExchangeRateEntity
import com.lrds.finner.data.local.entity.InstallmentContractEntity
import com.lrds.finner.data.local.entity.TransactionEntity
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.domain.usecase.ObserveMonthlyDashboardUseCase
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FinanceDomainTest {

    @Test
    fun `installment contract starting in Jan with 3 installments maps 1_3 Jan, 2_3 Feb, 3_3 Mar, null Apr`() {
        val contract = InstallmentContractEntity(
            id = "test_3m",
            description = "Compra Parcelada 3x",
            monthlyAmountCents = 300_00L,
            currency = CurrencyCode.BRL,
            startYear = 2026,
            startMonth = 1,
            initialInstallmentNumber = 1,
            totalInstallments = 3,
            categoryId = "cat_br",
            userId = "lucas"
        )

        val jan = contract.projectForMonth(2026, 1)
        assertNotNull(jan)
        assertEquals("1/3", jan?.badgeText)
        assertEquals(2, jan?.remainingInstallmentsAfterThis)

        val feb = contract.projectForMonth(2026, 2)
        assertNotNull(feb)
        assertEquals("2/3", feb?.badgeText)
        assertEquals(1, feb?.remainingInstallmentsAfterThis)

        val mar = contract.projectForMonth(2026, 3)
        assertNotNull(mar)
        assertEquals("3/3", mar?.badgeText)
        assertEquals(0, mar?.remainingInstallmentsAfterThis)

        // April must show nothing
        val apr = contract.projectForMonth(2026, 4)
        assertNull(apr)
    }

    @Test
    fun `ongoing Brazil loan at 34_48 in Jan 2026 projects accurately until 48_48`() {
        val carLoan = InstallmentContractEntity(
            id = "car_loan",
            description = "Car Loan",
            monthlyAmountCents = 1450_00L,
            currency = CurrencyCode.BRL,
            startYear = 2026,
            startMonth = 1,
            initialInstallmentNumber = 34,
            totalInstallments = 48,
            categoryId = "cat_br",
            userId = "joint"
        )

        assertEquals("34/48", carLoan.projectForMonth(2026, 1)?.badgeText)
        assertEquals("35/48", carLoan.projectForMonth(2026, 2)?.badgeText)
        assertEquals("45/48", carLoan.projectForMonth(2026, 12)?.badgeText)
        assertEquals("48/48", carLoan.projectForMonth(2027, 3)?.badgeText)
        assertNull(carLoan.projectForMonth(2027, 4))
    }

    @Test
    fun `unpaid installment is excluded from actuals until marked paid for that month`() {
        val categories = listOf(
            CategoryEntity(
                id = "cat_br",
                seriesId = "series_br",
                year = 2026,
                month = 11,
                name = "Apartamento",
                type = TransactionType.EXPENSE_BRAZIL,
                envelopeName = "Bills",
                plannedAmountCents = 2200_00L,
                currency = CurrencyCode.BRL
            )
        )
        val contract = InstallmentContractEntity(
            id = "inst_apt",
            description = "Apartamento",
            monthlyAmountCents = 400_00L,
            currency = CurrencyCode.EUR,
            startYear = 2026,
            startMonth = 11,
            initialInstallmentNumber = 53,
            totalInstallments = 360,
            categoryId = "cat_br",
            type = TransactionType.EXPENSE_BRAZIL,
            userId = "lucas"
        )
        val rateNov = ExchangeRateEntity(year = 2026, month = 11, eurToBrlRate = 5.50)

        val unpaidSummary = ObserveMonthlyDashboardUseCase.buildMonthlyDashboardData(
            year = 2026,
            month = 11,
            selectedUserFilter = null,
            categories = categories,
            allTransactions = emptyList(),
            contracts = listOf(contract),
            exchangeRate = rateNov,
            buckets = emptyList()
        )
        assertEquals(0L, unpaidSummary.totalBrazilExpenseEurCents)
        assertEquals(0L, unpaidSummary.categoryReconciliations.first().realizadoEurCents)
        assertEquals(false, unpaidSummary.activeInstallments.first().isPaid)

        val paidContract = contract.withPaidForMonth(2026, 11, true)
        val paidSummary = ObserveMonthlyDashboardUseCase.buildMonthlyDashboardData(
            year = 2026,
            month = 11,
            selectedUserFilter = null,
            categories = categories,
            allTransactions = emptyList(),
            contracts = listOf(paidContract),
            exchangeRate = rateNov,
            buckets = emptyList()
        )
        assertEquals(400_00L, paidSummary.totalBrazilExpenseEurCents)
        assertEquals(400_00L, paidSummary.categoryReconciliations.first().realizadoEurCents)
        assertEquals(true, paidSummary.activeInstallments.first().isPaid)
        assertEquals(false, paidContract.isPaidForMonth(2026, 12))
    }

    @Test
    fun `monthly dashboard calculation computes EUR base Falta Sobra and bucket percentage of total income`() {
        val categories = listOf(
            CategoryEntity(
                id = "cat_sal",
                seriesId = "series_sal",
                year = 2026,
                month = 1,
                name = "Salário",
                type = TransactionType.INCOME,
                envelopeName = null,
                plannedAmountCents = 5000_00L,
                currency = CurrencyCode.EUR
            ),
            CategoryEntity(
                id = "cat_leisure",
                seriesId = "series_leisure",
                year = 2026,
                month = 1,
                name = "Zelda Game",
                type = TransactionType.EXPENSE_LOCAL,
                envelopeName = "Personal",
                plannedAmountCents = 70_00L,
                currency = CurrencyCode.EUR
            ),
            CategoryEntity(
                id = "cat_rent",
                seriesId = "series_rent",
                year = 2026,
                month = 1,
                name = "Rent",
                type = TransactionType.EXPENSE_LOCAL,
                envelopeName = "Bills",
                plannedAmountCents = 1500_00L,
                currency = CurrencyCode.EUR
            ),
            CategoryEntity(
                id = "cat_groceries",
                seriesId = "series_groceries",
                year = 2026,
                month = 1,
                name = "Alimentos",
                type = TransactionType.EXPENSE_LOCAL,
                envelopeName = "Bills",
                plannedAmountCents = 400_00L,
                currency = CurrencyCode.EUR
            ),
            CategoryEntity(
                id = "cat_br",
                seriesId = "series_br",
                year = 2026,
                month = 1,
                name = "Brasil",
                type = TransactionType.EXPENSE_BRAZIL,
                envelopeName = "Bills",
                plannedAmountCents = 1288_00L, // R$ 1.288,00
                currency = CurrencyCode.BRL
            )
        )

        val transactions = listOf(
            TransactionEntity(
                description = "Salário Lucas",
                amountCents = 5000_00L,
                currency = CurrencyCode.EUR,
                date = LocalDate.of(2026, 1, 1),
                categoryId = "cat_sal",
                type = TransactionType.INCOME,
                userId = "lucas"
            ),
            TransactionEntity(
                description = "Aluguel",
                amountCents = 1350_00L,
                currency = CurrencyCode.EUR,
                date = LocalDate.of(2026, 1, 3),
                categoryId = "cat_rent",
                type = TransactionType.EXPENSE_LOCAL,
                userId = "joint"
            )
        )

        val contracts = listOf(
            InstallmentContractEntity(
                description = "Parcela Brasil",
                monthlyAmountCents = 644_00L, // R$ 644,00 -> € 100,00 at 6.44 rate
                currency = CurrencyCode.BRL,
                startYear = 2026,
                startMonth = 1,
                initialInstallmentNumber = 1,
                totalInstallments = 3,
                categoryId = "cat_br",
                type = TransactionType.EXPENSE_BRAZIL,
                userId = "lucas",
                paidMonths = "2026-01"
            )
        )

        val rateJan = ExchangeRateEntity(
            year = 2026,
            month = 1,
            eurToBrlRate = 6.44
        )

        val summary = ObserveMonthlyDashboardUseCase.buildMonthlyDashboardData(
            year = 2026,
            month = 1,
            selectedUserFilter = null,
            categories = categories,
            allTransactions = transactions,
            contracts = contracts,
            exchangeRate = rateJan,
            buckets = listOf(BudgetBucketEntity(bucketName = "Investimentos"))
        )

        assertEquals(5000_00L, summary.totalIncomeEurCents)
        assertEquals(1350_00L, summary.totalLocalExpenseEurCents)
        assertEquals(644_00L, summary.totalBrazilExpenseBrlCents)
        assertEquals(100_00L, summary.totalBrazilExpenseEurCents)
        assertEquals(1450_00L, summary.totalOutflowsEurCents)
        // Sobra = 5000 - (1350 + 100) = 3550 EUR
        assertEquals(3550_00L, summary.faltaSobraEurCents)

        // Verify categories are ordered by bucket ("Bills" before "Personal"), then by name ("Alimentos" before "Rent")
        val localExpenseNames = summary.categoryReconciliations
            .filter { it.category.type == TransactionType.EXPENSE_LOCAL }
            .map { "${it.category.envelopeName}:${it.category.name}" }
        assertEquals(
            listOf("Bills:Alimentos", "Bills:Rent", "Personal:Zelda Game"),
            localExpenseNames
        )

        val rentRow = summary.categoryReconciliations.first { it.category.id == "cat_rent" }
        assertEquals(1500_00L, rentRow.previstoCents)
        assertEquals(1350_00L, rentRow.realizadoCents)
        assertEquals(150_00L, rentRow.disponivelCents) // DISPONÍVEL = 1500 - 1350 = 150

        // Brazil category: Planned in BRL (R$ 1.288,00), Actual in EUR (€ 100,00),
        // Available in EUR = (R$ 1.288,00 / 6.44 = € 200,00) - € 100,00 = € 100,00
        val brazilRow = summary.categoryReconciliations.first { it.category.id == "cat_br" }
        assertEquals(1288_00L, brazilRow.previstoCents)
        assertEquals(200_00L, brazilRow.previstoEurCents)
        assertEquals(100_00L, brazilRow.realizadoCents)
        assertEquals(100_00L, brazilRow.disponivelCents)

        // Bills bucket: actual spent = 1350 + 100 = 1450 EUR
        // Bills planned: 1500 (Rent) + 400 (Alimentos) + 200 (R$ 1288 / 6.44) = 2100 EUR (42% of 5000 EUR planned income)
        val billsEnvelope = summary.envelopeSummaries.first { it.bucketName == "Bills" }
        assertEquals(1450_00L, billsEnvelope.actualEurCents)
        assertEquals(2100_00L, billsEnvelope.plannedEurCents)
        assertEquals(2100_00L, billsEnvelope.referenceBudgetEurCents)
        assertEquals(1450f / 2100f, billsEnvelope.usageFraction, 0.001f)
        assertEquals("42%", billsEnvelope.formattedPercentage)

        // Personal bucket:
        // Planned Spare Cash = Planned Inflows (5000) - Planned Outflows (1500 + 400 + 200 + 70 = 2170) = 2830 EUR (56,6% of 5000 EUR)
        // Actual Spare Cash = Actual Inflows (5000) - Actual Outflows (1450) = 3550 EUR
        assertEquals(5000_00L, summary.plannedIncomeEurCents)
        assertEquals(2170_00L, summary.plannedOutflowsEurCents)
        assertEquals(2830_00L, summary.plannedSpareCashEurCents)
        val personalEnvelope = summary.envelopeSummaries.first { it.bucketName == "Personal" }
        assertEquals(2830_00L, personalEnvelope.plannedEurCents)
        assertEquals(3550_00L, personalEnvelope.actualEurCents)
        assertEquals("56,6%", personalEnvelope.formattedPercentage)

        // Custom bucket "Investimentos" must also appear in envelopeSummaries
        val customEnvelope = summary.envelopeSummaries.firstOrNull { it.bucketName == "Investimentos" }
        assertNotNull(customEnvelope)
        assertEquals("0%", customEnvelope?.formattedPercentage)

        assertEquals("€ 100,00", CurrencyConverter.formatCents(100_00L, CurrencyCode.EUR))
    }

    @Test
    fun `percentage-based category dynamically recalculates planned amount when planned inflows change`() {
        val rate = ExchangeRateEntity(year = 2026, month = 1, eurToBrlRate = 5.50)
        val initialCategories = listOf(
            CategoryEntity(
                id = "cat_sal",
                year = 2026,
                month = 1,
                name = "Salary",
                type = TransactionType.INCOME,
                envelopeName = null,
                plannedAmountCents = 4000_00L,
                currency = CurrencyCode.EUR
            ),
            CategoryEntity(
                id = "cat_savings",
                year = 2026,
                month = 1,
                name = "Emergency Fund",
                type = TransactionType.EXPENSE_LOCAL,
                envelopeName = "Reserve",
                plannedAmountCents = 0L,
                plannedPercentage = 20.0,
                currency = CurrencyCode.EUR
            )
        )

        val initialSummary = ObserveMonthlyDashboardUseCase.buildMonthlyDashboardData(
            year = 2026,
            month = 1,
            selectedUserFilter = null,
            categories = initialCategories,
            allTransactions = emptyList(),
            contracts = emptyList(),
            exchangeRate = rate,
            buckets = emptyList()
        )

        val initialRow = initialSummary.categoryReconciliations.first { it.category.id == "cat_savings" }
        // 20% of € 4,000.00 = € 800.00
        assertEquals(800_00L, initialRow.previstoCents)
        assertEquals(800_00L, initialRow.previstoEurCents)

        // Now planned inflows increase to € 6,000.00 -> 20% automatically becomes € 1,200.00
        val updatedCategories = initialCategories.map {
            if (it.id == "cat_sal") it.copy(plannedAmountCents = 6000_00L) else it
        }
        val updatedSummary = ObserveMonthlyDashboardUseCase.buildMonthlyDashboardData(
            year = 2026,
            month = 1,
            selectedUserFilter = null,
            categories = updatedCategories,
            allTransactions = emptyList(),
            contracts = emptyList(),
            exchangeRate = rate,
            buckets = emptyList()
        )
        val updatedRow = updatedSummary.categoryReconciliations.first { it.category.id == "cat_savings" }
        assertEquals(1200_00L, updatedRow.previstoCents)
        assertEquals(1200_00L, updatedRow.previstoEurCents)
    }
}
