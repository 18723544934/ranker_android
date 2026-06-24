package com.perftop.android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Hardware(
    val id: Int,
    val name: String,
    val brand: String,
    val category: Category,
    val architecture: String,
    val launchDate: String? = null,
    val specs: Specs? = null,
    val overallScore: Double,
    val benchmarks: List<Benchmark> = emptyList(),
    val priceInfo: PriceInfo? = null,
    val imageUrl: String? = null,
    val isFavorite: Boolean = false
) {
    fun getBenchmarkScore(source: String, metric: String): Double? {
        return benchmarks.find { it.source == source && it.metric == metric }?.score
    }

    fun getScoreByMetric(metric: BenchmarkMetric): Double? {
        return benchmarks.find { it.metric == metric.name }?.score
    }

    val performanceRatio: Double
        get() = priceInfo?.let { overallScore / it.amount } ?: 0.0
}
