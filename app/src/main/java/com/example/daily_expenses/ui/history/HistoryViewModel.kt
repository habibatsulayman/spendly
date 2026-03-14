package com.example.daily_expenses.ui.history

import androidx.lifecycle.*
import com.example.daily_expenses.data.model.MonthlyAggregate
import com.example.daily_expenses.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    val monthlyAggregates: StateFlow<List<MonthlyAggregate>> = repository.getAllExpensesFlow()
        .map { expenses ->
            expenses.groupBy { 
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.date
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
            }.map { (key, list) ->
                val parts = key.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                
                val cal = Calendar.getInstance()
                cal.set(year, month, 1)
                
                MonthlyAggregate(
                    month = monthFormat.format(cal.time),
                    monthInt = month,
                    year = year,
                    totalAmount = list.sumOf { it.amount }
                )
            }.sortedWith(compareByDescending<MonthlyAggregate> { it.year }.thenByDescending { it.monthInt })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class HistoryViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
