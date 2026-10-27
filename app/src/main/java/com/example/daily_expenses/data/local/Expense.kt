package com.example.daily_expenses.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String,
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val bucket: String = "Expense",   // "Expense" (daily spend), "Needs", "Wants", "Investment"
    val isSorted: Boolean = false     // For Needs / Wants / Investment checklist items
)
