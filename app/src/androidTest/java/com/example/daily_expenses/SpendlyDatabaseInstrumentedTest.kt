package com.example.daily_expenses

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.daily_expenses.data.local.Expense
import com.example.daily_expenses.data.local.SpendlyDatabase
import com.example.daily_expenses.data.repository.ExpenseRepository
import com.example.daily_expenses.ui.home.SelectedDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class SpendlyDatabaseInstrumentedTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var repository: ExpenseRepository

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SpendlyDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = ExpenseRepository(database.expenseDao())
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndDeleteWantAndNeedInRoomDatabase() = runBlocking {
        val dateMillis = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 12, 10, 0, 0)
        }.timeInMillis

        repository.addExpense(
            Expense(
                amount = 50_000.0,
                category = "Groceries",
                description = "Monthly foodstuffs",
                date = dateMillis,
                bucket = "Needs",
                isSorted = false
            )
        )
        repository.addExpense(
            Expense(
                amount = 18_000.0,
                category = "Abaya",
                description = "Eid outfit",
                date = dateMillis,
                bucket = "Wants",
                isSorted = false
            )
        )

        val monthItems = repository.getExpensesForMonth(2026, Calendar.OCTOBER).first()
        assertEquals(2, monthItems.size)

        val wantToDelete = monthItems.first { it.bucket == "Wants" }
        repository.deleteExpense(wantToDelete)

        val afterDelete = repository.getExpensesForMonth(2026, Calendar.OCTOBER).first()
        assertEquals(1, afterDelete.size)
        assertEquals("Groceries", afterDelete.first().category)
    }

    @Test
    fun logForgottenExpenseForSpecificPastDay() = runBlocking {
        val pastDay = SelectedDay(2026, Calendar.OCTOBER, 20)
        val otherDay = SelectedDay(2026, Calendar.OCTOBER, 21)

        repository.addExpense(
            Expense(
                amount = 3_200.0,
                category = "Food",
                description = "Forgotten dinner on Oct 20",
                date = pastDay.toCalendar().timeInMillis,
                bucket = "Expense",
                isSorted = true
            )
        )
        repository.addExpense(
            Expense(
                amount = 1_500.0,
                category = "Transport",
                description = "Cab on Oct 21",
                date = otherDay.toCalendar().timeInMillis,
                bucket = "Expense",
                isSorted = true
            )
        )

        val oct20List = repository.getExpensesForDate(2026, Calendar.OCTOBER, 20).first()
        assertEquals(1, oct20List.size)
        assertEquals("Forgotten dinner on Oct 20", oct20List.first().description)

        val oct19List = repository.getExpensesForDate(2026, Calendar.OCTOBER, 19).first()
        assertTrue(oct19List.isEmpty())
    }

    @Test
    fun logMultipleIncomesInRoomDatabase() = runBlocking {
        val octMillis = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 5, 9, 0, 0)
        }.timeInMillis

        repository.addExpense(
            Expense(amount = 120_000.0, category = "Salary", description = "Monthly pay", date = octMillis, bucket = "Income")
        )
        repository.addExpense(
            Expense(amount = 25_000.0, category = "Gift / Dash", description = "Birthday dash", date = octMillis + 5000, bucket = "Income")
        )

        val incomes = repository.getExpensesForMonth(2026, Calendar.OCTOBER).first()
            .filter { it.bucket == "Income" }

        assertEquals(2, incomes.size)
        assertEquals(145_000.0, incomes.sumOf { it.amount }, 0.001)
    }
}
