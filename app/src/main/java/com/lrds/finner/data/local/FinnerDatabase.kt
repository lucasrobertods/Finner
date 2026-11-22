package com.lrds.finner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lrds.finner.data.local.converter.FinnerTypeConverters
import com.lrds.finner.data.local.dao.BudgetBucketDao
import com.lrds.finner.data.local.dao.CategoryDao
import com.lrds.finner.data.local.dao.ExchangeRateDao
import com.lrds.finner.data.local.dao.GoalDao
import com.lrds.finner.data.local.dao.InstallmentContractDao
import com.lrds.finner.data.local.dao.TransactionDao
import com.lrds.finner.data.local.entity.BudgetBucketEntity
import com.lrds.finner.data.local.entity.CategoryEntity
import com.lrds.finner.data.local.entity.ExchangeRateEntity
import com.lrds.finner.data.local.entity.GoalEntity
import com.lrds.finner.data.local.entity.InstallmentContractEntity
import com.lrds.finner.data.local.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        InstallmentContractEntity::class,
        ExchangeRateEntity::class,
        BudgetBucketEntity::class,
        GoalEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(FinnerTypeConverters::class)
abstract class FinnerDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun installmentContractDao(): InstallmentContractDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun budgetBucketDao(): BudgetBucketDao
    abstract fun goalDao(): GoalDao

    companion object {
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE installment_contracts ADD COLUMN paid_months TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile
        private var INSTANCE: FinnerDatabase? = null

        fun getInstance(context: Context): FinnerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FinnerDatabase::class.java,
                    "finner_2026.db"
                )
                    .addMigrations(MIGRATION_6_7)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
