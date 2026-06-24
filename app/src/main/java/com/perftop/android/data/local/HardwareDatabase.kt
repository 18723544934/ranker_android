package com.perftop.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.perftop.android.data.local.dao.BenchmarkDao
import com.perftop.android.data.local.dao.FavoriteDao
import com.perftop.android.data.local.dao.HardwareDao
import com.perftop.android.data.local.dao.HistoryDao
import com.perftop.android.data.local.entity.BenchmarkEntity
import com.perftop.android.data.local.entity.FavoriteEntity
import com.perftop.android.data.local.entity.HardwareEntity
import com.perftop.android.data.local.entity.HistoryEntity

@Database(
    entities = [
        HardwareEntity::class,
        BenchmarkEntity::class,
        FavoriteEntity::class,
        HistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HardwareDatabase : RoomDatabase() {
    abstract fun hardwareDao(): HardwareDao
    abstract fun benchmarkDao(): BenchmarkDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
}
