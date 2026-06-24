package com.perftop.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "favorites",
    indices = [Index("hardwareId")]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: Int,
    val groupName: String = "默认",
    val createdAt: Long = System.currentTimeMillis()
)
