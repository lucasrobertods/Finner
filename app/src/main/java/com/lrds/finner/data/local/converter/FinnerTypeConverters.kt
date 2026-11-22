package com.lrds.finner.data.local.converter

import androidx.room.TypeConverter
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.EnvelopeType
import com.lrds.finner.data.local.entity.GoalPriority
import com.lrds.finner.data.local.entity.SyncStatus
import com.lrds.finner.data.local.entity.TransactionType
import java.time.LocalDate

class FinnerTypeConverters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromCurrencyCode(currency: CurrencyCode): String = currency.name

    @TypeConverter
    fun toCurrencyCode(value: String): CurrencyCode = CurrencyCode.valueOf(value)

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromEnvelopeType(envelope: EnvelopeType): String = envelope.name

    @TypeConverter
    fun toEnvelopeType(value: String): EnvelopeType = EnvelopeType.valueOf(value)

    @TypeConverter
    fun fromGoalPriority(priority: GoalPriority): String = priority.name

    @TypeConverter
    fun toGoalPriority(value: String): GoalPriority = GoalPriority.valueOf(value)

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}
