package com.perftop.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "history",
    indices = [Index("hardwareId")]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hardwareId: Int,
    val visitedAt: Long = System.currentTimeMillis()
)
