package com.perftop.android.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.perftop.android.data.local.entity.FavoriteEntity

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): PagingSource<Int, FavoriteEntity>

    @Query("SELECT * FROM favorites WHERE hardwareId = :hardwareId")
    suspend fun getFavoriteByHardwareId(hardwareId: Int): FavoriteEntity?

    @Query("SELECT * FROM favorites WHERE groupName = :groupName ORDER BY createdAt DESC")
    fun getFavoritesByGroup(groupName: String): PagingSource<Int, FavoriteEntity>

    @Query("SELECT DISTINCT groupName FROM favorites")
    suspend fun getAllGroups(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE hardwareId = :hardwareId")
    suspend fun deleteFavoriteByHardwareId(hardwareId: Int)

    @Query("DELETE FROM favorites WHERE groupName = :groupName")
    suspend fun deleteFavoritesByGroup(groupName: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()
}
