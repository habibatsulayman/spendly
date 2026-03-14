package com.example.daily_expenses.ui.summary

import androidx.lifecycle.*
import com.example.daily_expenses.data.local.Expense

import com.example.daily_expenses.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.*

data class MonthlySummary(
    val totalAmount: Double,
    val categoryTotals: List<CategorySpend>
)

data class CategorySpend(
    val category: String,
    val amount: Double,
    val percentage: Float
)


class SummaryViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val now = Calendar.getInstance()
    
    val monthlySummary: StateFlow<MonthlySummary> = repository.getExpensesForMonth(
        now.get(Calendar.YEAR),
        now.get(Calendar.MONTH)
    ).map { expenses ->
        val total = expenses.sumOf { it.amount }
        val categoryGroup = expenses.groupBy { it.category }
        val categorySpends = categoryGroup.map { (cat, list) ->
            val amt = list.sumOf { it.amount }
            CategorySpend(
                category = cat,
                amount = amt,
                percentage = if (total > 0) (amt / total).toFloat() else 0f
            )
        }.sortedByDescending { it.amount }
        
        MonthlySummary(total, categorySpends)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlySummary(0.0, emptyList()))
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
