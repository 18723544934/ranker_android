package com.perftop.android.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.perftop.android.data.local.entity.HardwareEntity

@Dao
interface HardwareDao {
    @Query("SELECT * FROM hardwares WHERE category = :category ORDER BY overallScore DESC")
    fun getHardwaresByCategory(category: String): PagingSource<Int, HardwareEntity>

    @Query("SELECT * FROM hardwares WHERE id = :id")
    suspend fun getHardwareById(id: Int): HardwareEntity?

    @Query("SELECT * FROM hardwares WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'")
    fun searchHardwares(query: String): PagingSource<Int, HardwareEntity>

    @Query("SELECT * FROM hardwares WHERE category = :category AND brand = :brand ORDER BY overallScore DESC")
    fun getHardwaresByBrand(category: String, brand: String): PagingSource<Int, HardwareEntity>

    @Query("SELECT DISTINCT brand FROM hardwares WHERE category = :category")
    suspend fun getBrandsByCategory(category: String): List<String>

    @Query("SELECT DISTINCT architecture FROM hardwares WHERE category = :category")
    suspend fun getArchitecturesByCategory(category: String): List<String>

    @Query("SELECT * FROM hardwares WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteHardwares(): PagingSource<Int, HardwareEntity>

    @Update
    suspend fun updateHardware(hardware: HardwareEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHardware(hardware: HardwareEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHardwares(hardwares: List<HardwareEntity>)

    @Query("DELETE FROM hardwares")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM hardwares")
    suspend fun getCount(): Int

    @Query("SELECT MAX(overallScore) FROM hardwares WHERE category = :category")
    suspend fun getMaxScore(category: String): Double?

    @Query("SELECT MIN(overallScore) FROM hardwares WHERE category = :category")
    suspend fun getMinScore(category: String): Double?
}
