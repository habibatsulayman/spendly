package com.example.daily_expenses.ui.home

import android.content.Context
import androidx.lifecycle.*
import com.example.daily_expenses.data.local.Expense
import com.example.daily_expenses.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class MonthlyBudgetPlan(
    val income: Double = 0.0,
    val needsBudget: Double = 0.0,
    val wantsBudget: Double = 0.0,
    val investmentBudget: Double = 0.0,
    val needsRatio: Int = 50,
    val investmentRatio: Int = 30,
    val wantsRatio: Int = 20,
    val investmentSorted: Boolean = false
) {
    val totalAllocated: Double
        get() = needsBudget + wantsBudget + investmentBudget

    val hasBudget: Boolean
        get() = totalAllocated > 0.0
}

data class MonthYear(
    val year: Int,
    val month: Int // 0..11
) {
    val label: String
        get() {
            val cal = Calendar.getInstance().apply { set(year, month, 1) }
            return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
        }

    val shortMonthName: String
        get() {
            val cal = Calendar.getInstance().apply { set(year, month, 1) }
            return SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
        }

    fun previous(): MonthYear {
        val cal = Calendar.getInstance().apply {
            set(year, month, 1)
            add(Calendar.MONTH, -1)
        }
        return MonthYear(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }

    fun next(): MonthYear {
        val cal = Calendar.getInstance().apply {
            set(year, month, 1)
            add(Calendar.MONTH, 1)
        }
        return MonthYear(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
    }
}

class HomeViewModel(
    private val repository: ExpenseRepository,
    private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("spendly_prefs", Context.MODE_PRIVATE)

    // ─── Today's expenses (daily expenses + Wants/Needs sorted today) ───────
    val todayExpenses: StateFlow<List<Expense>> = repository.getExpensesForToday()
        .map { list -> list.filter { it.bucket == "Expense" || it.isSorted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpentToday: StateFlow<Double> = todayExpenses
        .map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ─── Daily budget (defaults to 5,000 like screenshot) ───────────────────
    private val _dailyBudget = MutableStateFlow(prefs.getFloat("daily_budget", 5000f).toDouble())
    val dailyBudget: StateFlow<Double> = _dailyBudget

    fun setDailyBudget(amount: Double) {
        _dailyBudget.value = amount
        prefs.edit().putFloat("daily_budget", amount.toFloat()).apply()
    }

    // ─── Selected Month for Wants & Needs / Insights ────────────────────────
    private val now = Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(
        MonthYear(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
    )
    val selectedMonth: StateFlow<MonthYear> = _selectedMonth

    fun selectPreviousMonth() {
        _selectedMonth.value = _selectedMonth.value.previous()
        refreshBudgetPlan()
    }

    fun selectNextMonth() {
        _selectedMonth.value = _selectedMonth.value.next()
        refreshBudgetPlan()
    }

    // ─── Monthly 50:30:20 Budget Plan for Selected Month ────────────────────
    private val _budgetPlan = MutableStateFlow(loadBudgetPlanFor(_selectedMonth.value))
    val budgetPlan: StateFlow<MonthlyBudgetPlan> = _budgetPlan

    private fun keyPrefix(m: MonthYear) = "budget_${m.year}_${m.month}_"

    fun loadBudgetPlanFor(m: MonthYear): MonthlyBudgetPlan {
        val p = keyPrefix(m)
        val income = prefs.getFloat("${p}income", 0f).toDouble()
        val defaultN = prefs.getInt("default_ratio_needs", 50)
        val defaultI = prefs.getInt("default_ratio_invest", 30)
        val defaultW = prefs.getInt("default_ratio_wants", 20)
        val needsRatio = prefs.getInt("${p}ratio_needs", defaultN)
        val investRatio = prefs.getInt("${p}ratio_invest", defaultI)
        val wantsRatio = prefs.getInt("${p}ratio_wants", defaultW)

        var needs = prefs.getFloat("${p}needs", 0f).toDouble()
        var wants = prefs.getFloat("${p}wants", 0f).toDouble()
        var invest = prefs.getFloat("${p}invest", 0f).toDouble()

        // If a budget was saved before the 50:30:20 (Needs:Investment:Wants) ratio update,
        // automatically align the amounts with the active ratios when income is present.
        if (income > 0 && !prefs.contains("${p}ratio_invest")) {
            needs = income * (needsRatio / 100.0)
            invest = income * (investRatio / 100.0)
            wants = income * (wantsRatio / 100.0)
            prefs.edit()
                .putFloat("${p}needs", needs.toFloat())
                .putFloat("${p}invest", invest.toFloat())
                .putFloat("${p}wants", wants.toFloat())
                .putInt("${p}ratio_needs", needsRatio)
                .putInt("${p}ratio_invest", investRatio)
                .putInt("${p}ratio_wants", wantsRatio)
                .apply()
        }

        val investSorted = prefs.getBoolean("${p}invest_sorted", false)
        return MonthlyBudgetPlan(
            income = income,
            needsBudget = needs,
            wantsBudget = wants,
            investmentBudget = invest,
            needsRatio = needsRatio,
            investmentRatio = investRatio,
            wantsRatio = wantsRatio,
            investmentSorted = investSorted
        )
    }

    private fun refreshBudgetPlan() {
        _budgetPlan.value = loadBudgetPlanFor(_selectedMonth.value)
    }

    fun saveBudgetPlanForSelectedMonth(
        income: Double,
        needs: Double,
        wants: Double,
        investment: Double,
        needsRatio: Int = 50,
        investmentRatio: Int = 30,
        wantsRatio: Int = 20
    ) {
        val m = _selectedMonth.value
        val p = keyPrefix(m)
        val currentSorted = _budgetPlan.value.investmentSorted
        prefs.edit()
            .putFloat("${p}income", income.toFloat())
            .putFloat("${p}needs", needs.toFloat())
            .putFloat("${p}wants", wants.toFloat())
            .putFloat("${p}invest", investment.toFloat())
            .putInt("${p}ratio_needs", needsRatio)
            .putInt("${p}ratio_invest", investmentRatio)
            .putInt("${p}ratio_wants", wantsRatio)
            .putInt("default_ratio_needs", needsRatio)
            .putInt("default_ratio_invest", investmentRatio)
            .putInt("default_ratio_wants", wantsRatio)
            .apply()
        _budgetPlan.value = MonthlyBudgetPlan(
            income = income,
            needsBudget = needs,
            wantsBudget = wants,
            investmentBudget = investment,
            needsRatio = needsRatio,
            investmentRatio = investmentRatio,
            wantsRatio = wantsRatio,
            investmentSorted = currentSorted
        )
    }

    fun toggleInvestmentSorted() {
        val m = _selectedMonth.value
        val p = keyPrefix(m)
        val next = !_budgetPlan.value.investmentSorted
        prefs.edit().putBoolean("${p}invest_sorted", next).apply()
        _budgetPlan.value = _budgetPlan.value.copy(investmentSorted = next)
    }

    fun getPreviousMonthPlan(): MonthlyBudgetPlan {
        return loadBudgetPlanFor(_selectedMonth.value.previous())
    }

    // ─── Wants & Needs items for Selected Month ─────────────────────────────
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedMonthItems: StateFlow<List<Expense>> = _selectedMonth
        .flatMapLatest { m ->
            repository.getExpensesForMonth(m.year, m.month)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val needsItems: StateFlow<List<Expense>> = selectedMonthItems
        .map { list -> list.filter { it.bucket == "Needs" }.sortedBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wantsItems: StateFlow<List<Expense>> = selectedMonthItems
        .map { list -> list.filter { it.bucket == "Wants" }.sortedBy { it.id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily expenses ("Expense") mapped to Needs (Food, Transport, Data) or Wants (Other)
    val dailyNeedsSpent: StateFlow<Double> = selectedMonthItems
        .map { list ->
            list.filter {
                it.bucket == "Expense" &&
                    (it.category == "Food" || it.category == "Transport" || it.category == "Data")
            }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dailyWantsSpent: StateFlow<Double> = selectedMonthItems
        .map { list ->
            list.filter {
                it.bucket == "Expense" &&
                    it.category != "Food" && it.category != "Transport" && it.category != "Data"
            }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val previousMonthUnsortedItems: StateFlow<List<Expense>> = _selectedMonth
        .flatMapLatest { m ->
            val prev = m.previous()
            repository.getExpensesForMonth(prev.year, prev.month)
                .map { list -> list.filter { (it.bucket == "Needs" || it.bucket == "Wants") && !it.isSorted } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun carryOverUnsortedFromPreviousMonth() {
        viewModelScope.launch {
            val unsorted = previousMonthUnsortedItems.value
            val targetMonth = _selectedMonth.value
            val cal = Calendar.getInstance().apply {
                set(targetMonth.year, targetMonth.month, 1, 12, 0, 0)
            }
            unsorted.forEach { item ->
                repository.addExpense(
                    item.copy(
                        id = 0,
                        date = cal.timeInMillis,
                        isSorted = false
                    )
                )
            }
        }
    }

    // ─── Edit state ─────────────────────────────────────────────────────────
    private val _editingExpense = MutableStateFlow<Expense?>(null)
    val editingExpense: StateFlow<Expense?> = _editingExpense

    fun setEditingExpense(expense: Expense?) { _editingExpense.value = expense }

    // ─── CRUD ────────────────────────────────────────────────────────────────
    fun addDailyExpense(amount: Double, category: String, description: String) {
        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    amount = amount,
                    category = category,
                    description = description,
                    bucket = "Expense",
                    isSorted = true
                )
            )
        }
    }

    fun addPlannedItem(title: String, note: String, amount: Double, bucket: String) {
        viewModelScope.launch {
            val m = _selectedMonth.value
            val nowCal = Calendar.getInstance()
            val timestamp = if (nowCal.get(Calendar.YEAR) == m.year && nowCal.get(Calendar.MONTH) == m.month) {
                System.currentTimeMillis()
            } else {
                Calendar.getInstance().apply { set(m.year, m.month, 1, 12, 0, 0) }.timeInMillis
            }
            repository.addExpense(
                Expense(
                    amount = amount,
                    category = title,       // Item title stored in category
                    description = note,     // Optional subtitle stored in description
                    date = timestamp,
                    bucket = bucket,        // "Needs" or "Wants"
                    isSorted = false
                )
            )
        }
    }

    fun toggleExpenseSorted(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense.copy(isSorted = !expense.isSorted))
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense)
            _editingExpense.value = null
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }
}

class HomeViewModelFactory(
    private val repository: ExpenseRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
