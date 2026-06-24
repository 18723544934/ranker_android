package com.perftop.android.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Favorite(
    val id: Long = 0,
    val hardwareId: Int,
    val groupName: String = "默认",
    val createdAt: Long = System.currentTimeMillis()
)
