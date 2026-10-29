package com.example.daily_expenses

import com.example.daily_expenses.data.local.Expense
import com.example.daily_expenses.data.local.ExpenseDao
import com.example.daily_expenses.data.model.CategoryHelper
import com.example.daily_expenses.data.repository.ExpenseRepository
import com.example.daily_expenses.ui.home.IslamicQuotesProvider
import com.example.daily_expenses.ui.home.MonthYear
import com.example.daily_expenses.ui.home.MonthlyBudgetPlan
import com.example.daily_expenses.ui.home.SelectedDay
import com.example.daily_expenses.ui.home.greetingForHour
import com.example.daily_expenses.ui.summary.SummaryViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

/**
 * In-memory fake implementation of [ExpenseDao] for fast, deterministic unit tests.
 */
class FakeExpenseDao : ExpenseDao {
    private val itemsFlow = MutableStateFlow<List<Expense>>(emptyList())
    private var nextId = 1L

    override fun getAllExpenses(): Flow<List<Expense>> =
        itemsFlow.map { list -> list.sortedByDescending { it.date } }

    override fun getExpensesInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<Expense>> =
        itemsFlow.map { list ->
            list.filter { it.date in startTimestamp..endTimestamp }
                .sortedByDescending { it.date }
        }

    override suspend fun insertExpense(expense: Expense) {
        val current = itemsFlow.value.toMutableList()
        if (expense.id == 0L) {
            current.add(expense.copy(id = nextId++))
        } else {
            val idx = current.indexOfFirst { it.id == expense.id }
            if (idx >= 0) {
                current[idx] = expense
            } else {
                current.add(expense)
            }
        }
        itemsFlow.value = current
    }

    override suspend fun deleteExpense(expense: Expense) {
        itemsFlow.value = itemsFlow.value.filterNot { it.id == expense.id }
    }

    override fun getTotalSpentInRange(startTimestamp: Long, endTimestamp: Long): Flow<Double> =
        getExpensesInRange(startTimestamp, endTimestamp).map { list -> list.sumOf { it.amount } }

    override fun getExpensesForDay(startOfDay: Long, endOfDay: Long): Flow<List<Expense>> =
        itemsFlow.map { list ->
            list.filter { it.date in startOfDay..endOfDay }
                .sortedByDescending { it.date }
        }

    override fun getTotalSpentForDay(startOfDay: Long, endOfDay: Long): Flow<Double> =
        getExpensesForDay(startOfDay, endOfDay).map { list -> list.sumOf { it.amount } }
}

class SpendlyFeaturesTest {

    private lateinit var fakeDao: FakeExpenseDao
    private lateinit var repository: ExpenseRepository

    @Before
    fun setUp() {
        fakeDao = FakeExpenseDao()
        repository = ExpenseRepository(fakeDao)
    }

    // ─── 1. Time-of-Day Welcome Greeting Tests ──────────────────────────────

    @Test
    fun `greetingForHour returns appropriate time-of-day greeting for Habeebah`() {
        // Morning: 5 AM to 11 AM
        assertEquals("Good morning, Habeebah", greetingForHour(5))
        assertEquals("Good morning, Habeebah", greetingForHour(9))
        assertEquals("Good morning, Habeebah", greetingForHour(11))

        // Afternoon: 12 PM to 4 PM (16)
        assertEquals("Good afternoon, Habeebah", greetingForHour(12))
        assertEquals("Good afternoon, Habeebah", greetingForHour(15))
        assertEquals("Good afternoon, Habeebah", greetingForHour(16))

        // Evening: 5 PM (17) to 8 PM (20)
        assertEquals("Good evening, Habeebah", greetingForHour(17))
        assertEquals("Good evening, Habeebah", greetingForHour(19))
        assertEquals("Good evening, Habeebah", greetingForHour(20))

        // Night: 9 PM (21) to 4 AM
        assertEquals("Good night, Habeebah", greetingForHour(21))
        assertEquals("Good night, Habeebah", greetingForHour(23))
        assertEquals("Good night, Habeebah", greetingForHour(0))
        assertEquals("Good night, Habeebah", greetingForHour(4))
    }

    // ─── 2. Rotating Islamic Soul-Lifting Quotes Tests ──────────────────────

    @Test
    fun `IslamicQuotesProvider contains valid quotes and rotates every 3 hours in a loop`() {
        val quotes = IslamicQuotesProvider.quotes
        assertTrue("Expected at least 10 quotes", quotes.size >= 10)
        quotes.forEach { quote ->
            assertTrue("Quote text should not be blank", quote.text.isNotBlank())
            assertTrue("Quote reference should not be blank", quote.reference.isNotBlank())
        }

        val threeHoursMs = 3 * 60 * 60 * 1000L
        val baseTime = 1_730_000_000_000L - (1_730_000_000_000L % threeHoursMs) // aligned to start of 3h slot

        // Within the same 3-hour window, the quote remains the same
        val quoteAtStart = IslamicQuotesProvider.getQuoteForTime(baseTime)
        val quoteAfterOneHour = IslamicQuotesProvider.getQuoteForTime(baseTime + 60 * 60 * 1000L)
        val quoteAfterTwoHours = IslamicQuotesProvider.getQuoteForTime(baseTime + 2 * 60 * 60 * 1000L)
        assertEquals(quoteAtStart, quoteAfterOneHour)
        assertEquals(quoteAtStart, quoteAfterTwoHours)

        // After 3 hours, the quote advances to the next one
        val quoteAfterThreeHours = IslamicQuotesProvider.getQuoteForTime(baseTime + threeHoursMs)
        assertNotEquals(quoteAtStart, quoteAfterThreeHours)

        // Manual tap offset also advances to the next quote immediately
        val quoteWithManualTap = IslamicQuotesProvider.getQuoteForTime(baseTime, manualOffset = 1)
        assertEquals(quoteAfterThreeHours, quoteWithManualTap)

        // After cycling through all quotes.size * 3 hours, it loops back to the first quote
        val quoteAfterFullLoop = IslamicQuotesProvider.getQuoteForTime(baseTime + quotes.size * threeHoursMs)
        assertEquals(quoteAtStart, quoteAfterFullLoop)
    }

    // ─── 3. Deleting a Want, Need, Daily Expense, or Income ─────────────────

    @Test
    fun `deleting a Want or Need removes it from monthly items`() = runBlocking {
        val oct2026 = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 15, 12, 0, 0)
        }.timeInMillis

        repository.addExpense(
            Expense(
                amount = 45000.0,
                category = "Rent",
                description = "Monthly rent",
                date = oct2026,
                bucket = "Needs",
                isSorted = false
            )
        )
        repository.addExpense(
            Expense(
                amount = 15000.0,
                category = "Sneakers",
                description = "Running shoes",
                date = oct2026,
                bucket = "Wants",
                isSorted = false
            )
        )

        val initialItems = repository.getExpensesForMonth(2026, Calendar.OCTOBER).first()
        assertEquals(2, initialItems.size)

        val wantItem = initialItems.first { it.bucket == "Wants" }
        repository.deleteExpense(wantItem)

        val afterDeletingWant = repository.getExpensesForMonth(2026, Calendar.OCTOBER).first()
        assertEquals(1, afterDeletingWant.size)
        assertEquals("Rent", afterDeletingWant.first().category)

        val needItem = afterDeletingWant.first { it.bucket == "Needs" }
        repository.deleteExpense(needItem)

        val afterDeletingNeed = repository.getExpensesForMonth(2026, Calendar.OCTOBER).first()
        assertTrue(afterDeletingNeed.isEmpty())
    }

    // ─── 4. Multiple Income Streams & 50:30:20 / Custom Ratio Calculation ───

    @Test
    fun `multiple income entries sum up and recalculate 50-30-20 and custom ratios accurately`() = runBlocking {
        assertEquals(
            listOf("Salary", "Gift / Dash", "Side Gig", "Other"),
            CategoryHelper.incomeSources
        )

        val oct2026 = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 10, 10, 0, 0)
        }.timeInMillis

        // Log Salary (120,000), Gift / Dash (10,000), and Side Gig (20,000) -> Total 150,000
        repository.addExpense(
            Expense(amount = 120_000.0, category = "Salary", description = "Main job", date = oct2026, bucket = "Income")
        )
        repository.addExpense(
            Expense(amount = 10_000.0, category = "Gift / Dash", description = "From sister", date = oct2026 + 1000, bucket = "Income")
        )
        repository.addExpense(
            Expense(amount = 20_000.0, category = "Side Gig", description = "Design project", date = oct2026 + 2000, bucket = "Income")
        )

        val monthItems = repository.getExpensesForMonth(2026, Calendar.OCTOBER).first()
        val incomeItems = monthItems.filter { it.bucket == "Income" }
        val totalLoggedIncome = incomeItems.sumOf { it.amount }

        assertEquals(3, incomeItems.size)
        assertEquals(150_000.0, totalLoggedIncome, 0.001)

        // Default 50 / 30 / 20 (Needs 50%, Investment 30%, Wants 20%)
        val basePlan = MonthlyBudgetPlan(needsRatio = 50, investmentRatio = 30, wantsRatio = 20)
        val updatedPlan = basePlan.withLoggedIncome(totalLoggedIncome)

        assertEquals(150_000.0, updatedPlan.income, 0.001)
        assertEquals(75_000.0, updatedPlan.needsBudget, 0.001)      // 50% of 150,000
        assertEquals(45_000.0, updatedPlan.investmentBudget, 0.001) // 30% of 150,000
        assertEquals(30_000.0, updatedPlan.wantsBudget, 0.001)      // 20% of 150,000

        // Deleting one income entry (Side Gig 20,000) reduces total income to 130,000
        // where 30% Investment = 39,000
        val sideGig = incomeItems.first { it.category == "Side Gig" }
        repository.deleteExpense(sideGig)

        val remainingIncome = repository.getExpensesForMonth(2026, Calendar.OCTOBER).first()
            .filter { it.bucket == "Income" }
            .sumOf { it.amount }
        assertEquals(130_000.0, remainingIncome, 0.001)

        val planAt130k = basePlan.withLoggedIncome(remainingIncome)
        assertEquals(65_000.0, planAt130k.needsBudget, 0.001)       // 50% of 130,000
        assertEquals(39_000.0, planAt130k.investmentBudget, 0.001)  // 30% of 130,000
        assertEquals(26_000.0, planAt130k.wantsBudget, 0.001)       // 20% of 130,000

        // Custom ratio test: 60% Needs, 25% Investment, 15% Wants on 100,000
        val customPlan = MonthlyBudgetPlan(needsRatio = 60, investmentRatio = 25, wantsRatio = 15)
            .withLoggedIncome(100_000.0)
        assertEquals(60_000.0, customPlan.needsBudget, 0.001)
        assertEquals(25_000.0, customPlan.investmentBudget, 0.001)
        assertEquals(15_000.0, customPlan.wantsBudget, 0.001)
    }

    // ─── 5. Past-Day Date Selection & Forgotten Daily Expense Logging ───────

    @Test
    fun `SelectedDay navigates across days and logs forgotten expenses to the selected past date`() = runBlocking {
        val oct25 = SelectedDay(2026, Calendar.OCTOBER, 25)
        val oct24 = oct25.previousDay()
        val oct26 = oct25.nextDay()

        assertEquals(SelectedDay(2026, Calendar.OCTOBER, 24), oct24)
        assertEquals(SelectedDay(2026, Calendar.OCTOBER, 26), oct26)
        assertFalse(oct24.isToday)

        // Log a forgotten expense on Oct 24 and another on Oct 25
        repository.addExpense(
            Expense(
                amount = 2500.0,
                category = "Food",
                description = "Forgotten lunch on Oct 24",
                date = oct24.toCalendar().timeInMillis,
                bucket = "Expense",
                isSorted = true
            )
        )
        repository.addExpense(
            Expense(
                amount = 1200.0,
                category = "Transport",
                description = "Bus fare on Oct 25",
                date = oct25.toCalendar().timeInMillis,
                bucket = "Expense",
                isSorted = true
            )
        )

        val oct24Expenses = repository.getExpensesForDate(2026, Calendar.OCTOBER, 24).first()
        assertEquals(1, oct24Expenses.size)
        assertEquals("Forgotten lunch on Oct 24", oct24Expenses.first().description)
        assertEquals(2500.0, oct24Expenses.first().amount, 0.001)

        val oct25Expenses = repository.getExpensesForDate(2026, Calendar.OCTOBER, 25).first()
        assertEquals(1, oct25Expenses.size)
        assertEquals("Bus fare on Oct 25", oct25Expenses.first().description)
        assertEquals(1200.0, oct25Expenses.first().amount, 0.001)
    }

    @Test
    fun `MonthYear navigates across year boundaries properly`() {
        val jan2026 = MonthYear(2026, Calendar.JANUARY)
        val dec2025 = jan2026.previous()
        assertEquals(2025, dec2025.year)
        assertEquals(Calendar.DECEMBER, dec2025.month)

        val backToJan2026 = dec2025.next()
        assertEquals(jan2026, backToJan2026)
    }

    // ─── 6. Summary / Insights Excludes Income Entries ──────────────────────

    @Test
    fun `SummaryViewModel excludes Income entries from total spent and category breakdown`() {
        val summaryViewModel = SummaryViewModel(repository)
        val oct2026 = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 18, 14, 0, 0)
        }.timeInMillis

        val entries = listOf(
            Expense(id = 1, amount = 120_000.0, category = "Salary", description = "October salary", date = oct2026, bucket = "Income", isSorted = true),
            Expense(id = 2, amount = 15_000.0, category = "Gift / Dash", description = "Cash gift", date = oct2026, bucket = "Income", isSorted = true),
            Expense(id = 3, amount = 3_500.0, category = "Food", description = "Jollof rice", date = oct2026, bucket = "Expense", isSorted = true),
            Expense(id = 4, amount = 1_500.0, category = "Transport", description = "Bolt ride", date = oct2026, bucket = "Expense", isSorted = true),
            Expense(id = 5, amount = 5_000.0, category = "Data", description = "MTN bundle", date = oct2026, bucket = "Needs", isSorted = true)
        )

        val summary = summaryViewModel.buildSummary(entries)

        // Only Food (3,500) + Transport (1,500) + sorted Data Need (5,000) = 10,000 should be counted
        assertEquals(10_000.0, summary.totalAmount, 0.001)
        assertEquals(3, summary.transactionCount)
        assertFalse(summary.categoryTotals.any { it.category == "Salary" || it.category == "Gift / Dash" })
    }
}
