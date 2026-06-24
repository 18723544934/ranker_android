package com.perftop.android.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.perftop.android.data.local.entity.HistoryEntity

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 100")
    fun getRecentHistory(): PagingSource<Int, HistoryEntity>

    @Query("SELECT * FROM history WHERE hardwareId = :hardwareId")
    suspend fun getHistoryByHardwareId(hardwareId: Int): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    @Query("UPDATE history SET visitedAt = :visitedAt WHERE hardwareId = :hardwareId")
    suspend fun updateHistoryVisitedAt(hardwareId: Int, visitedAt: Long)

    @Delete
    suspend fun deleteHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE hardwareId = :hardwareId")
    suspend fun deleteHistoryByHardwareId(hardwareId: Int)

    @Query("DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY visitedAt DESC LIMIT 100)")
    suspend fun trimHistory()

    @Query("DELETE FROM history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM history")
    suspend fun getCount(): Int
}
