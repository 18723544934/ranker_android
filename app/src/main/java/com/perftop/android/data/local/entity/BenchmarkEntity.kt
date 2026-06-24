package com.perftop.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "benchmarks",
    foreignKeys = [
        ForeignKey(
            entity = HardwareEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardwareId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("hardwareId")]
)
data class BenchmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val hardwareId: Int,
    val source: String,
    val metric: String,
    val score: Double,
    val unit: String
)
