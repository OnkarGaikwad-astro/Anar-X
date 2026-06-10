package com.farmlens.anarai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUri: String,
    val disease: String,
    val severity: Int,
    val confidence: Float,
    val timestamp: Long
)
