package com.perftop.android.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HardwareDto(
    val id: Int,
    val name: String,
    val brand: String,
    val category: String,
    val architecture: String,
    val launchDate: String? = null,
    val specs: SpecsDto? = null,
    val overallScore: Double,
    val benchmarks: List<BenchmarkDto> = emptyList(),
    val priceInfo: PriceInfoDto? = null,
    val imageUrl: String? = null
)

@Serializable
data class SpecsDto(
    val cores: Int? = null,
    val threads: Int? = null,
    val baseClockGHz: Double? = null,
    val boostClockGHz: Double? = null,
    val tdpWatts: Int? = null,
    val lithography: String? = null,
    val vramGB: Int? = null,
    val memoryType: String? = null,
    val bandwidthGBs: Double? = null,
    val cache: String? = null
)

@Serializable
data class BenchmarkDto(
    val source: String,
    val metric: String,
    val score: Double,
    val unit: String
)

@Serializable
data class PriceInfoDto(
    val currency: String,
    val amount: Double,
    val source: String,
    val updated: String? = null
)

@Serializable
data class HardwareListResponse(
    val data: List<HardwareDto>,
    val page: Int,
    val perPage: Int,
    val total: Int
)

@Serializable
data class HardwareDetailResponse(
    val data: HardwareDto
)

@Serializable
data class CompareResponse(
    val data: List<HardwareDto>,
    val comparison: Map<String, Map<String, Any>>
)

@Serializable
data class FilterOptionsResponse(
    val brands: List<String>,
    val architectures: List<String>,
    val coreRanges: List<String>,
    val frequencyRanges: List<String>,
    val years: List<Int>
)
