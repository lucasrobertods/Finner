package com.lrds.finner.core.currency

import com.lrds.finner.data.local.entity.CurrencyCode
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

object CurrencyConverter {
    const val DEFAULT_EUR_TO_BRL_RATE: Double = 5.50

    /**
     * Converts BRL centavos to EUR cents using the monthly EUR->BRL exchange rate.
     * Example: R$ 644,00 (64400L centavos) at rate 6.44 -> € 100.00 (10000L cents).
     */
    fun brlCentsToEurCents(brlCents: Long, eurToBrlRate: Double): Long {
        val safeRate = if (eurToBrlRate > 0.0) eurToBrlRate else DEFAULT_EUR_TO_BRL_RATE
        return (brlCents.toDouble() / safeRate).roundToLong()
    }

    /**
     * Converts EUR cents to BRL centavos using the monthly EUR->BRL exchange rate.
     */
    fun eurCentsToBrlCents(eurCents: Long, eurToBrlRate: Double): Long {
        val safeRate = if (eurToBrlRate > 0.0) eurToBrlRate else DEFAULT_EUR_TO_BRL_RATE
        return (eurCents.toDouble() * safeRate).roundToLong()
    }

    /**
     * Converts an amount in the given [currency] into EUR cents (primary base currency).
     */
    fun toEurCents(amountCents: Long, currency: CurrencyCode, eurToBrlRate: Double): Long {
        return when (currency) {
            CurrencyCode.EUR -> amountCents
            CurrencyCode.BRL -> brlCentsToEurCents(amountCents, eurToBrlRate)
        }
    }

    /**
     * Formats minor units (cents/centavos) into a localized string, e.g. "€ 1.250,00" or "R$ 644,00".
     */
    fun formatCents(amountCents: Long, currency: CurrencyCode, includeSign: Boolean = false): String {
        val isNegative = amountCents < 0
        val absValue = kotlin.math.abs(amountCents) / 100.0
        val ptBrFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        val formattedNumber = ptBrFormat.format(absValue)
        val signPrefix = when {
            isNegative -> "- "
            includeSign && amountCents > 0 -> "+ "
            else -> ""
        }
        return "$signPrefix${currency.symbol} $formattedNumber"
    }

    /**
     * Parses a user-entered decimal string (e.g., "125,50" or "125.50") into minor units (cents).
     */
    fun parseToCents(input: String): Long? {
        val normalized = input.trim().replace(",", ".")
        val value = normalized.toDoubleOrNull() ?: return null
        return (value * 100.0).roundToLong()
    }
}
