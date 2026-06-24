package com.perftop.android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Benchmark(
    val source: String,
    val metric: String,
    val score: Double,
    val unit: String
)

@Serializable
data class BenchmarkSource(
    val name: String,
    val weight: Float = 1.0f
)

enum class BenchmarkMetric(val displayName: String) {
    SINGLE_CORE("单核"),
    MULTI_CORE("多核"),
    GPU("GPU"),
    GAMING("游戏"),
    AI("AI"),
    EFFICIENCY("能效"),
    MEMORY("内存")
}
