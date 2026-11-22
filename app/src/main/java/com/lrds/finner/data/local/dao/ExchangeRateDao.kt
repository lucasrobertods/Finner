package com.lrds.finner.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lrds.finner.data.local.entity.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates WHERE year = :year ORDER BY month ASC")
    fun observeRatesForYear(year: Int): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates WHERE year = :year AND month = :month LIMIT 1")
    fun observeRateForMonth(year: Int, month: Int): Flow<ExchangeRateEntity?>

    @Query("SELECT * FROM exchange_rates WHERE year = :year ORDER BY month ASC")
    suspend fun getRatesForYear(year: Int): List<ExchangeRateEntity>

    @Query("SELECT * FROM exchange_rates WHERE year = :year AND month = :month LIMIT 1")
    suspend fun getRateForMonth(year: Int, month: Int): ExchangeRateEntity?

    @Upsert
    suspend fun upsert(exchangeRate: ExchangeRateEntity)

    @Upsert
    suspend fun upsertAll(rates: List<ExchangeRateEntity>)

    @Query("DELETE FROM exchange_rates WHERE year = :year")
    suspend fun deleteByYear(year: Int)
}
