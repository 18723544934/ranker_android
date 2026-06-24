package com.perftop.android.data.repository

import com.perftop.android.data.local.dao.BenchmarkDao
import com.perftop.android.data.local.dao.HardwareDao
import com.perftop.android.data.local.entity.BenchmarkEntity
import com.perftop.android.data.local.entity.HardwareEntity
import com.perftop.android.data.remote.api.HardwareListResponse
import com.perftop.android.data.remote.api.PerfTopApi
import com.perftop.android.domain.model.Category
import com.perftop.android.domain.model.Hardware
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareRepository @Inject constructor(
    private val hardwareDao: HardwareDao,
    private val benchmarkDao: BenchmarkDao,
    private val api: PerfTopApi
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getHardwaresByCategory(category: Category) = hardwareDao.getHardwaresByCategory(category.name)
        .map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }

    fun searchHardwares(query: String) = hardwareDao.searchHardwares(query)
        .map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }

    fun getFavoriteHardwares() = hardwareDao.getFavoriteHardwares()
        .map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }

    suspend fun getHardwareById(id: Int): Hardware? {
        val hardwareEntity = hardwareDao.getHardwareById(id) ?: return null
        val benchmarks = benchmarkDao.getBenchmarksByHardwareId(id)
        return hardwareEntity.toDomainModel(benchmarks.map { it.toDomainModel() })
    }

    suspend fun refreshHardwares(category: Category) {
        try {
            val response = api.getHardwares(category.name)
            val hardwareEntities = response.data.map { dto ->
                HardwareEntity(
                    id = dto.id,
                    name = dto.name,
                    brand = dto.brand,
                    category = dto.category,
                    architecture = dto.architecture,
                    launchDate = dto.launchDate,
                    specsJson = dto.specs?.let { json.encodeToString(it) },
                    overallScore = dto.overallScore,
                    priceInfoJson = dto.priceInfo?.let { json.encodeToString(it) },
                    imageUrl = dto.imageUrl,
                    isFavorite = false
                )
            }

            hardwareDao.insertHardwares(hardwareEntities)

            // Insert benchmarks
            val benchmarkEntities = response.data.flatMap { hardwareDto ->
                hardwareDto.benchmarks.map { benchmarkDto ->
                    BenchmarkEntity(
                        hardwareId = hardwareDto.id,
                        source = benchmarkDto.source,
                        metric = benchmarkDto.metric,
                        score = benchmarkDto.score,
                        unit = benchmarkDto.unit
                    )
                }
            }
            benchmarkDao.insertBenchmarks(benchmarkEntities)
        } catch (e: Exception) {
            // Handle network error, fallback to local data
            e.printStackTrace()
        }
    }

    suspend fun updateFavorite(hardwareId: Int, isFavorite: Boolean) {
        val hardware = hardwareDao.getHardwareById(hardwareId) ?: return
        hardwareDao.updateHardware(hardware.copy(isFavorite = isFavorite))
    }

    suspend fun getBrandsByCategory(category: Category) =
        hardwareDao.getBrandsByCategory(category.name)

    suspend fun getArchitecturesByCategory(category: Category) =
        hardwareDao.getArchitecturesByCategory(category.name)

    suspend fun clearAll() {
        hardwareDao.clearAll()
        benchmarkDao.clearAll()
    }

    suspend fun getHardwareCount() = hardwareDao.getCount()

    private fun HardwareEntity.toDomainModel(
        benchmarks: List<com.perftop.android.domain.model.Benchmark> = emptyList()
    ): Hardware {
        val specs = specsJson?.let { json.decodeFromString(it) }
        val priceInfo = priceInfoJson?.let { json.decodeFromString(it) }
        return Hardware(
            id = id,
            name = name,
            brand = brand,
            category = Category.fromString(category) ?: Category.PC_CPU,
            architecture = architecture,
            launchDate = launchDate,
            specs = specs,
            overallScore = overallScore,
            benchmarks = benchmarks,
            priceInfo = priceInfo,
            imageUrl = imageUrl,
            isFavorite = isFavorite
        )
    }

    private fun BenchmarkEntity.toDomainModel() = com.perftop.android.domain.model.Benchmark(
        source = source,
        metric = metric,
        score = score,
        unit = unit
    )
}
