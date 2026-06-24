package com.perftop.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.perftop.android.data.local.entity.BenchmarkEntity

@Dao
interface BenchmarkDao {
    @Query("SELECT * FROM benchmarks WHERE hardwareId = :hardwareId")
    suspend fun getBenchmarksByHardwareId(hardwareId: Int): List<BenchmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBenchmark(benchmark: BenchmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBenchmarks(benchmarks: List<BenchmarkEntity>)

    @Query("DELETE FROM benchmarks WHERE hardwareId = :hardwareId")
    suspend fun deleteBenchmarksByHardwareId(hardwareId: Int)

    @Query("DELETE FROM benchmarks")
    suspend fun clearAll()

    @Query("SELECT * FROM benchmarks WHERE hardwareId = :hardwareId AND source = :source")
    suspend fun getBenchmarkBySource(hardwareId: Int, source: String): BenchmarkEntity?
}
