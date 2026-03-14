package com.example.daily_expenses

import android.app.Application
import com.example.daily_expenses.data.local.SpendlyDatabase
import com.example.daily_expenses.data.repository.ExpenseRepository

class SpendlyApp : Application() {
    val database by lazy { SpendlyDatabase.getDatabase(this) }
    val repository by lazy { ExpenseRepository(database.expenseDao()) }
}
