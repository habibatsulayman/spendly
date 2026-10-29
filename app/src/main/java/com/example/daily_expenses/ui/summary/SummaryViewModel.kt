package com.example.daily_expenses.ui.summary

import androidx.lifecycle.*
import com.example.daily_expenses.data.local.Expense
import com.example.daily_expenses.data.model.MonthlyAggregate
import com.example.daily_expenses.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

data class MonthlySummary(
    val totalAmount: Double,
    val transactionCount: Int,
    val categoryTotals: List<CategorySpend>
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

    fun selectMonth(year: Int, month: Int) {
        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        _selectedMonth.value = AvailableMonth(
            label = monthLabelFormat.format(cal.time),
            year = year,
            month = month
        )
    }

    // ── Summary for selected month (Daily expenses + sorted Needs & Wants) ──
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val monthlySummary: StateFlow<MonthlySummary> = _selectedMonth
        .flatMapLatest { selected ->
            repository.getExpensesForMonth(selected.year, selected.month)
                .map { expenses -> buildSummary(expenses) }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MonthlySummary(0.0, 0, emptyList())
        )

    internal fun isActualSpend(expense: Expense): Boolean {
        if (expense.bucket == "Income") return false
        return expense.bucket == "Expense" || expense.isSorted
    }

    internal fun mapToStandardCategory(expense: Expense): String {
        val text = "${expense.category} ${expense.description}".lowercase()
        return when {
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

    internal fun buildSummary(expenses: List<Expense>): MonthlySummary {
        val nonIncomeExpenses = expenses.filter { it.bucket != "Income" }
        // Include daily expenses + sorted Wants & Needs items; if nothing is sorted yet, fall back to all items in the month so the chart and total reflect what was entered
        val countedExpenses = nonIncomeExpenses.filter { isActualSpend(it) }.ifEmpty { nonIncomeExpenses }
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

        // Consolidate beyond top 3 into "Other" if there are more than 4 categories so the 4-color donut stays clean
        val finalCategories = if (grouped.size <= 4) {
            grouped
        } else {
            val top3 = grouped.take(3)
            val restAmount = grouped.drop(3).sumOf { it.amount }
            top3 + CategorySpend(
                category = "Other",
                amount = restAmount,
                percentage = if (total > 0) (restAmount / total).toFloat() else 0f
            )
        }

        return MonthlySummary(
            totalAmount = total,
            transactionCount = countedExpenses.size,
            categoryTotals = finalCategories
        )
    }

    // ── Past months aggregates ──────────────────────────────────────────────
    val pastMonths: StateFlow<List<MonthlyAggregate>> = combine(
        repository.getAllExpensesFlow(),
        _selectedMonth
    ) { allExpenses, selected ->
        allExpenses
            .groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
            }
            .mapNotNull { (key, list) ->
                val parts = key.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                if (year == selected.year && month == selected.month) return@mapNotNull null

                val nonIncome = list.filter { it.bucket != "Income" }
                val counted = nonIncome.filter { isActualSpend(it) }.ifEmpty { nonIncome }
                val sum = counted.sumOf { it.amount }
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
