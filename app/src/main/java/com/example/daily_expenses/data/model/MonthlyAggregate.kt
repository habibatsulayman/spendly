package com.example.daily_expenses.data.model

data class MonthlyAggregate(
    val month: String, // e.g., "October 2023"
    val monthInt: Int,   // 0-11
    val year: Int,
    val totalAmount: Double
)
