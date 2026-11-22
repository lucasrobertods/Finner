package com.lrds.finner.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lrds.finner.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE is_deleted = 0 ORDER BY year ASC, month ASC, COALESCE(envelope_name, '') COLLATE NOCASE ASC, name COLLATE NOCASE ASC")
    fun observeActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE is_deleted = 0 AND year = :year AND month = :month ORDER BY COALESCE(envelope_name, '') COLLATE NOCASE ASC, name COLLATE NOCASE ASC")
    fun observeCategoriesForMonth(year: Int, month: Int): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE is_deleted = 0")
    suspend fun getAllActiveCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE is_deleted = 0 AND year = :year")
    suspend fun getActiveCategoriesForYear(year: Int): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE is_deleted = 0 AND year = :year AND month >= :startMonth")
    suspend fun getActiveCategoriesFromMonth(year: Int, startMonth: Int): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE is_deleted = 0 AND year = :year AND month = :month")
    suspend fun getActiveCategoriesForMonth(year: Int, month: Int): List<CategoryEntity>

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Query("UPDATE categories SET is_deleted = 1, sync_status = 'PENDING_DELETE', updated_at = :timestamp WHERE id = :id")
    suspend fun softDeleteById(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE categories SET is_deleted = 1, sync_status = 'PENDING_DELETE', updated_at = :timestamp WHERE year = :year AND is_deleted = 0")
    suspend fun softDeleteByYear(year: Int, timestamp: Long = System.currentTimeMillis())
}
