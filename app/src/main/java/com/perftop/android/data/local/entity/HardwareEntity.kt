package com.perftop.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.perftop.android.domain.model.Category

@Entity(tableName = "hardwares")
data class HardwareEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val brand: String,
    val category: String,
    val architecture: String,
    val launchDate: String?,
    val specsJson: String?,
    val overallScore: Double,
    val priceInfoJson: String?,
    val imageUrl: String?,
    val isFavorite: Boolean = false
)
