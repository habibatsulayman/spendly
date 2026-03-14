package com.example.daily_expenses.ui.home

import androidx.lifecycle.*

import com.example.daily_expenses.data.local.Expense

import com.example.daily_expenses.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: ExpenseRepository) : ViewModel() {

    val allExpenses: StateFlow<List<Expense>> = repository.getExpensesForToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSpentToday: StateFlow<Double> = repository.getTotalSpentForToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)


    private val _editingExpense = MutableStateFlow<Expense?>(null)
    val editingExpense: StateFlow<Expense?> = _editingExpense

    fun setEditingExpense(expense: Expense?) {
        _editingExpense.value = expense
    }

    fun addExpense(amount: Double, category: String, description: String) {

        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                category = category,
                description = description
            )
            repository.addExpense(expense)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.addExpense(expense) // Room @Insert(onConflict = OnConflictStrategy.REPLACE) handles update
            _editingExpense.value = null
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }
}

class HomeViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
