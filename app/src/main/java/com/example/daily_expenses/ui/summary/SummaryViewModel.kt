package com.example.daily_expenses.ui.summary

import androidx.lifecycle.*
import com.example.daily_expenses.data.local.Expense
import com.example.daily_expenses.data.model.CategoryHelper
import com.example.daily_expenses.data.model.MonthlyAggregate
import com.example.daily_expenses.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

data class MonthlySummary(
    val totalAmount: Double,
    val transactionCount: Int,
    val categoryTotals: List<CategorySpend>,
    val transactions: List<Expense> = emptyList()
)

data class CategorySpend(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class AvailableMonth(
    val label: String,
    val year: Int,
    val month: Int
)

class SummaryViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val monthLabelFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private val now = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(
        AvailableMonth(
            label = monthLabelFormat.format(now.time),
            year  = now.get(Calendar.YEAR),
            month = now.get(Calendar.MONTH)
        )
    )
    val selectedMonth: StateFlow<AvailableMonth> = _selectedMonth

    private val _investmentSortedAmount = MutableStateFlow(0.0)
    private val _investmentSortedAt = MutableStateFlow(0L)
    private val _pastMonthsRevision = MutableStateFlow(0L)
    private var pastMonthInvestmentProvider: ((year: Int, month: Int, loggedIncome: Double) -> Double)? = null

    fun setPastMonthInvestmentProvider(
        revision: Long = 0L,
        provider: (year: Int, month: Int, loggedIncome: Double) -> Double
    ) {
        pastMonthInvestmentProvider = provider
        _pastMonthsRevision.value = revision
    }

    fun selectMonth(year: Int, month: Int) {
        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        _selectedMonth.value = AvailableMonth(
            label = monthLabelFormat.format(cal.time),
            year = year,
            month = month
        )
    }

    fun setInvestmentSortedAmount(amount: Double, sortedAt: Long = 0L) {
        _investmentSortedAmount.value = amount
        _investmentSortedAt.value = sortedAt
    }

    // ── Summary for selected month (Daily expenses + sorted Needs, Wants & Investment) ──
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val monthlySummary: StateFlow<MonthlySummary> = combine(
        _selectedMonth.flatMapLatest { selected ->
            repository.getExpensesForMonth(selected.year, selected.month)
        },
        _investmentSortedAmount,
        _investmentSortedAt
    ) { expenses, investAmount, investSortedAt ->
        buildSummary(expenses, investAmount, investSortedAt)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlySummary(0.0, 0, emptyList(), emptyList())
    )

    internal fun isActualSpend(expense: Expense): Boolean {
        if (expense.bucket == "Income") return false
        return expense.bucket == "Expense" || expense.isSorted
    }

    internal fun mapToStandardCategory(expense: Expense): String {
        val text = "${expense.category} ${expense.description}".lowercase()
        return when {
            expense.bucket == "Investment" || expense.category.equals("Investment", ignoreCase = true) -> "Investment"
            CategoryHelper.isSadaqahExpense(expense) -> "Sadaqah"
            expense.category.equals("Food", ignoreCase = true) ||
                text.contains("food") || text.contains("lunch") || text.contains("dinner") ||
                text.contains("breakfast") || text.contains("grocer") || text.contains("market") ||
                text.contains("meal") || text.contains("snack") || text.contains("restaurant") -> "Food"
            expense.category.equals("Transport", ignoreCase = true) ||
                text.contains("transport") || text.contains("bus") || text.contains("cab") ||
                text.contains("uber") || text.contains("bolt") || text.contains("fuel") ||
                text.contains("fare") || text.contains("ride") || text.contains("taxi") -> "Transport"
            expense.category.equals("Data", ignoreCase = true) ||
                text.contains("data") || text.contains("airtime") || text.contains("wifi") ||
                text.contains("internet") || text.contains("mtn") || text.contains("airtel") ||
                text.contains("glo") || text.contains("phone") -> "Data"
            expense.category.equals("Other", ignoreCase = true) -> "Other"
            else -> expense.category.ifBlank { "Other" }
        }
    }

    internal fun buildSummary(
        expenses: List<Expense>,
        investmentSortedAmount: Double = 0.0,
        investmentSortedAt: Long = 0L
    ): MonthlySummary {
        val nonIncomeExpenses = expenses.filter { it.bucket != "Income" }
        val actualSpent = nonIncomeExpenses.filter { isActualSpend(it) }
        val baseCounted = if (actualSpent.isEmpty() && investmentSortedAmount <= 0.0) {
            nonIncomeExpenses
        } else {
            actualSpent
        }

        val countedExpenses = if (investmentSortedAmount > 0.0) {
            val investExpense = Expense(
                id = -1L,
                amount = investmentSortedAmount,
                category = "Investment",
                description = "Set aside when pay lands",
                date = investmentSortedAt,
                bucket = "Investment",
                isSorted = true
            )
            if (investmentSortedAt > 0L) {
                (listOf(investExpense) + baseCounted).sortedByDescending { it.date }
            } else {
                listOf(investExpense) + baseCounted.sortedByDescending { it.date }
            }
        } else {
            baseCounted.sortedByDescending { it.date }
        }

        val total = countedExpenses.sumOf { it.amount }

        val grouped = countedExpenses.groupBy { mapToStandardCategory(it) }
            .map { (cat, list) ->
                val amt = list.sumOf { it.amount }
                CategorySpend(
                    category = cat,
                    amount = amt,
                    percentage = if (total > 0) (amt / total).toFloat() else 0f
                )
            }
            .sortedByDescending { it.amount }

        val finalCategories = if (grouped.size <= 5) {
            grouped
        } else {
            val top4 = grouped.take(4)
            val restAmount = grouped.drop(4).sumOf { it.amount }
            top4 + CategorySpend(
                category = "Other",
                amount = restAmount,
                percentage = if (total > 0) (restAmount / total).toFloat() else 0f
            )
        }

        return MonthlySummary(
            totalAmount = total,
            transactionCount = countedExpenses.size,
            categoryTotals = finalCategories,
            transactions = countedExpenses
        )
    }

    // ── Past months aggregates ──────────────────────────────────────────────
    internal fun computePastMonths(
        allExpenses: List<Expense>,
        selected: AvailableMonth,
        investmentProvider: ((year: Int, month: Int, loggedIncome: Double) -> Double)? = pastMonthInvestmentProvider
    ): List<MonthlyAggregate> {
        return allExpenses
            .groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
            }
            .mapNotNull { (key, list) ->
                val parts = key.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                if (year == selected.year && month == selected.month) return@mapNotNull null

                val loggedIncome = list.filter { it.bucket == "Income" }.sumOf { it.amount }
                val investSorted = investmentProvider?.invoke(year, month, loggedIncome) ?: 0.0

                val nonIncome = list.filter { it.bucket != "Income" }
                val actualSpent = nonIncome.filter { isActualSpend(it) }
                val counted = if (actualSpent.isEmpty() && investSorted <= 0.0) {
                    nonIncome
                } else {
                    actualSpent
                }
                val sum = counted.sumOf { it.amount } + investSorted
                if (sum <= 0.0) return@mapNotNull null

                val cal = Calendar.getInstance().apply { set(year, month, 1) }
                MonthlyAggregate(
                    month = monthLabelFormat.format(cal.time),
                    monthInt = month,
                    year = year,
                    totalAmount = sum
                )
            }
            .sortedWith(compareByDescending<MonthlyAggregate> { it.year }.thenByDescending { it.monthInt })
    }

    val pastMonths: StateFlow<List<MonthlyAggregate>> = combine(
        repository.getAllExpensesFlow(),
        _selectedMonth,
        _pastMonthsRevision
    ) { allExpenses, selected, _ ->
        computePastMonths(allExpenses, selected)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class SummaryViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SummaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
