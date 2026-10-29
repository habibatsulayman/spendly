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
        get() = totalAllocated > 0.0 || income > 0.0

    fun withLoggedIncome(loggedIncome: Double): MonthlyBudgetPlan {
        return if (loggedIncome > 0.0) {
            MonthlyBudgetPlan(
                income = loggedIncome,
                needsBudget = loggedIncome * (needsRatio / 100.0),
                investmentBudget = loggedIncome * (investmentRatio / 100.0),
                wantsBudget = loggedIncome * (wantsRatio / 100.0),
                needsRatio = needsRatio,
                investmentRatio = investmentRatio,
                wantsRatio = wantsRatio,
                investmentSorted = investmentSorted
            )
        } else {
            this
        }
    }
}

fun greetingForHour(hourOfDay: Int, userName: String = "Habeebah"): String {
    return when (hourOfDay) {
        in 5..11 -> "Good morning, $userName"
        in 12..16 -> "Good afternoon, $userName"
        in 17..20 -> "Good evening, $userName"
        else -> "Good night, $userName"
    }
}

data class SelectedDay(
    val year: Int,
    val month: Int, // 0..11
    val dayOfMonth: Int
) {
    fun toCalendar(): Calendar = Calendar.getInstance().apply {
        set(year, month, dayOfMonth, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val isToday: Boolean
        get() {
            val now = Calendar.getInstance()
            return now.get(Calendar.YEAR) == year &&
                now.get(Calendar.MONTH) == month &&
                now.get(Calendar.DAY_OF_MONTH) == dayOfMonth
        }

    val isYesterday: Boolean
        get() {
            val yest = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            return yest.get(Calendar.YEAR) == year &&
                yest.get(Calendar.MONTH) == month &&
                yest.get(Calendar.DAY_OF_MONTH) == dayOfMonth
        }

    val topBarLabel: String
        get() = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(toCalendar().time)

    val sectionLabel: String
        get() = when {
            isToday -> "Today"
            isYesterday -> "Yesterday"
            else -> SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(toCalendar().time)
        }

    val cardHeaderLabel: String
        get() = when {
            isToday -> "Spent today"
            isYesterday -> "Spent yesterday"
            else -> "Spent on ${SimpleDateFormat("d MMM", Locale.getDefault()).format(toCalendar().time)}"
        }

    fun previousDay(): SelectedDay {
        val cal = toCalendar().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return SelectedDay(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun nextDay(): SelectedDay {
        val cal = toCalendar().apply { add(Calendar.DAY_OF_YEAR, 1) }
        return SelectedDay(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    companion object {
        fun today(): SelectedDay {
            val now = Calendar.getInstance()
            return SelectedDay(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
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

data class IslamicQuote(
    val text: String,
    val reference: String
)

object IslamicQuotesProvider {
    val quotes = listOf(
        IslamicQuote(
            "“And whoever puts their trust in Allah, then He will suffice him.”",
            "Qur'an 65:3"
        ),
        IslamicQuote(
            "“If you are grateful, I will surely increase you [in favor].”",
            "Qur'an 14:7"
        ),
        IslamicQuote(
            "“Charity does not decrease wealth.”",
            "Prophet Muhammad ﷺ · Sahih Muslim"
        ),
        IslamicQuote(
            "“Verily, with hardship comes ease.”",
            "Qur'an 94:6"
        ),
        IslamicQuote(
            "“Richness is not having many possessions, but richness is the contentment of the soul.”",
            "Prophet Muhammad ﷺ · Sahih al-Bukhari"
        ),
        IslamicQuote(
            "“And there is no creature on earth but that upon Allah is its provision.”",
            "Qur'an 11:6"
        ),
        IslamicQuote(
            "“Allah does not burden a soul beyond that it can bear.”",
            "Qur'an 2:286"
        ),
        IslamicQuote(
            "“Unquestionably, by the remembrance of Allah hearts are assured.”",
            "Qur'an 13:28"
        ),
        IslamicQuote(
            "“And spend in the way of Allah and do not throw yourselves into destruction, and do good; indeed, Allah loves the doers of good.”",
            "Qur'an 2:195"
        ),
        IslamicQuote(
            "“And those who, when they spend, are neither extravagant nor niggardly, but hold a medium way between those.”",
            "Qur'an 25:67"
        ),
        IslamicQuote(
            "“Indeed, my Lord expands provision for whom He wills and restricts it.”",
            "Qur'an 34:39"
        ),
        IslamicQuote(
            "“Whoever is pleased that his provision be expanded and his life span extended, let him keep ties of kinship.”",
            "Prophet Muhammad ﷺ · Sahih al-Bukhari"
        )
    )

    private const val THREE_HOURS_MS = 3 * 60 * 60 * 1000L

    fun getQuoteForTime(timeMillis: Long, manualOffset: Int = 0): IslamicQuote {
        val slot = ((timeMillis / THREE_HOURS_MS).toInt() + manualOffset).coerceAtLeast(0)
        return quotes[slot % quotes.size]
    }
}

class HomeViewModel(
    private val repository: ExpenseRepository,
    private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("spendly_prefs", Context.MODE_PRIVATE)

    // ─── Selected Day for Home Screen (defaults to Today) ───────────────────
    private val _selectedDay = MutableStateFlow(SelectedDay.today())
    val selectedDay: StateFlow<SelectedDay> = _selectedDay

    fun selectDay(day: SelectedDay) {
        _selectedDay.value = day
    }

    fun selectPreviousDay() {
        _selectedDay.value = _selectedDay.value.previousDay()
    }

    fun selectNextDay() {
        if (!_selectedDay.value.isToday) {
            _selectedDay.value = _selectedDay.value.nextDay()
        }
    }

    fun resetToTodayIfNewDay() {
        val currentToday = SelectedDay.today()
        if (_lastKnownToday != currentToday) {
            _lastKnownToday = currentToday
            _selectedDay.value = currentToday
            _selectedMonth.value = MonthYear(currentToday.year, currentToday.month)
            refreshBudgetPlan()
        }
    }

    private var _lastKnownToday = SelectedDay.today()

    // ─── Expenses for Selected Day (daily expenses + Wants/Needs sorted that day) ──
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val todayExpenses: StateFlow<List<Expense>> = _selectedDay
        .flatMapLatest { day ->
            repository.getExpensesForDate(day.year, day.month, day.dayOfMonth)
                .map { list -> list.filter { it.bucket == "Expense" || (it.isSorted && it.bucket != "Income") } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpentToday: StateFlow<Double> = todayExpenses
        .map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // ─── Daily budget (defaults to 5,000) ───────────────────────────────────
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

    // ─── Monthly Items & Incomes for Selected Month ─────────────────────────
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedMonthItems: StateFlow<List<Expense>> = _selectedMonth
        .flatMapLatest { m ->
            repository.getExpensesForMonth(m.year, m.month)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeItems: StateFlow<List<Expense>> = selectedMonthItems
        .map { list -> list.filter { it.bucket == "Income" }.sortedByDescending { it.date } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalLoggedIncome: StateFlow<Double> = incomeItems
        .map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

    // ─── Monthly 50:30:20 Budget Plan for Selected Month ────────────────────
    private val _storedBudgetPlan = MutableStateFlow(loadBudgetPlanFor(_selectedMonth.value))

    // Combine stored plan with dynamically logged income entries so adding an income stream
    // automatically updates total income and recalculates Needs / Investment / Wants budgets!
    val budgetPlan: StateFlow<MonthlyBudgetPlan> = combine(
        _storedBudgetPlan,
        totalLoggedIncome
    ) { stored, loggedIncome ->
        stored.withLoggedIncome(loggedIncome)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _storedBudgetPlan.value)

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
        _storedBudgetPlan.value = loadBudgetPlanFor(_selectedMonth.value)
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
        val currentSorted = _storedBudgetPlan.value.investmentSorted
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
        _storedBudgetPlan.value = MonthlyBudgetPlan(
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
        val next = !_storedBudgetPlan.value.investmentSorted
        prefs.edit().putBoolean("${p}invest_sorted", next).apply()
        _storedBudgetPlan.value = _storedBudgetPlan.value.copy(investmentSorted = next)
    }

    fun getPreviousMonthPlan(): MonthlyBudgetPlan {
        return loadBudgetPlanFor(_selectedMonth.value.previous())
    }

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
    fun addDailyExpense(
        amount: Double,
        category: String,
        description: String,
        timestamp: Long? = null
    ) {
        viewModelScope.launch {
            val dateToUse = timestamp ?: run {
                val day = _selectedDay.value
                if (day.isToday) System.currentTimeMillis() else day.toCalendar().timeInMillis
            }
            repository.addExpense(
                Expense(
                    amount = amount,
                    category = category,
                    description = description,
                    date = dateToUse,
                    bucket = "Expense",
                    isSorted = true
                )
            )
        }
    }

    fun addIncome(
        amount: Double,
        source: String,
        note: String,
        timestamp: Long? = null
    ) {
        viewModelScope.launch {
            val m = _selectedMonth.value
            val nowCal = Calendar.getInstance()
            val dateToUse = timestamp ?: if (nowCal.get(Calendar.YEAR) == m.year && nowCal.get(Calendar.MONTH) == m.month) {
                System.currentTimeMillis()
            } else {
                Calendar.getInstance().apply { set(m.year, m.month, 1, 12, 0, 0) }.timeInMillis
            }
            repository.addExpense(
                Expense(
                    amount = amount,
                    category = source,      // e.g. "Salary", "Gift / Dash", "Side Gig", "Other"
                    description = note,
                    date = dateToUse,
                    bucket = "Income",
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
