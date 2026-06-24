package com.perftop.android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HistoryEntry(
    val id: Long = 0,
    val hardwareId: Int,
    val visitedAt: Long = System.currentTimeMillis()
)
