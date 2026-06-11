package com.farmlens.anarai.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // e.g., "Fertilizer", "Pesticide", "Labor"
    val amount: Double,
    val date: Long,
    val notes: String
)
