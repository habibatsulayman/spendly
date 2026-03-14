package com.example.daily_expenses.data.local

import androidx.room.*

import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE date >= :startTimestamp AND date <= :endTimestamp ORDER BY date DESC")
    fun getExpensesInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE date >= :startTimestamp AND date <= :endTimestamp")
    fun getTotalSpentInRange(startTimestamp: Long, endTimestamp: Long): Flow<Double>

    @Query("SELECT * FROM expenses WHERE date >= :startOfDay AND date <= :endOfDay ORDER BY date DESC")
    fun getExpensesForDay(startOfDay: Long, endOfDay: Long): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE date >= :startOfDay AND date <= :endOfDay")
    fun getTotalSpentForDay(startOfDay: Long, endOfDay: Long): Flow<Double>

}
