package com.perftop.android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PriceInfo(
    val currency: String,
    val amount: Double,
    val source: String,
    val updated: String? = null
)
