package com.perftop.android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Specs(
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
