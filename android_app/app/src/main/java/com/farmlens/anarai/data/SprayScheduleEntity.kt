package com.farmlens.anarai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spray_schedule")
data class SprayScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val chemicalName: String,
    val isCompleted: Boolean = false,
    val notes: String = ""
)
