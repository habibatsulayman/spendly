package com.example.daily_expenses.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults

import com.example.daily_expenses.data.local.Expense
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import java.text.SimpleDateFormat

import java.util.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val expenses by viewModel.allExpenses.collectAsState()
    val totalToday by viewModel.totalSpentToday.collectAsState()
    val editingExpense by viewModel.editingExpense.collectAsState()

    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food & Dining") }
    var customCategoryText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }

    // Update fields when editingExpense changes
    LaunchedEffect(editingExpense) {
        editingExpense?.let {
            amountText = it.amount.toString()
            if (com.example.daily_expenses.data.model.CategoryHelper.predefinedCategories.contains(it.category)) {
                selectedCategory = it.category
                customCategoryText = ""
            } else {
                selectedCategory = "Other"
                customCategoryText = it.category
            }
            descriptionText = it.description
        } ?: run {
            amountText = ""
            selectedCategory = "Food & Dining"
            customCategoryText = ""
            descriptionText = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Simple Top Bar instead of nested Scaffold
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Text(
                "Spendly",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        DailySummaryHeader(totalToday ?: 0.0)

        // Logging Section
        LogExpenseSection(
            amount = amountText,
            onAmountChange = { amountText = it },
            selectedCategory = selectedCategory,
            onCategoryChange = { selectedCategory = it },
            customCategory = customCategoryText,
            onCustomCategoryChange = { customCategoryText = it },
            description = descriptionText,
            onDescriptionChange = { descriptionText = it },
            isEditing = editingExpense != null,
            onCancelEdit = { viewModel.setEditingExpense(null) },
            onLogClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (amt > 0) {
                    val categoryToLog = if (selectedCategory == "Other" && customCategoryText.isNotBlank()) {
                        customCategoryText
                    } else {
                        selectedCategory
                    }
                    
                    if (editingExpense != null) {
                        viewModel.updateExpense(editingExpense!!.copy(
                            amount = amt,
                            category = categoryToLog,
                            description = descriptionText
                        ))
                    } else {
                        viewModel.addExpense(amt, categoryToLog, descriptionText)
                    }
                    
                    amountText = ""
                    selectedCategory = "Food & Dining"
                    customCategoryText = ""
                    descriptionText = ""
                }
            }
        )

        // Activity List
        Text(
            text = "Today's Activity",
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(expenses) { expense ->
                ExpenseItem(
                    expense = expense,
                    onEditClick = { viewModel.setEditingExpense(expense) },
                    onDeleteClick = { viewModel.deleteExpense(expense) }
                )
            }
        }
    }
}


@Composable
fun DailySummaryHeader(total: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Spending Today", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
            Text(
                text = "₦${String.format("%,.2f", total)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White // User requested White
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogExpenseSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    customCategory: String,
    onCustomCategoryChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    isEditing: Boolean,
    onCancelEdit: () -> Unit,
    onLogClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isEditing) "EDIT EXPENSE" else "QUICK ENTRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (isEditing) {
                    TextButton(onClick = onCancelEdit) {
                        Text("Cancel", color = Color.Black, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("₦") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )

            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Row
            Text("Select Category", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.example.daily_expenses.data.model.CategoryHelper.predefinedCategories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategoryChange(category) },
                        label = { Text(category) },
                        leadingIcon = {
                            Icon(
                                com.example.daily_expenses.data.model.CategoryHelper.getIconForCategory(category), 
                                contentDescription = null, 
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            if (selectedCategory == "Other") {
                OutlinedTextField(
                    value = customCategory,
                    onValueChange = onCustomCategoryChange,
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )

                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )

            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onLogClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (isEditing) "Save Changes" else "Log Expense")
            }
        }
    }
}

@Composable
fun ExpenseItem(
    expense: Expense,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = com.example.daily_expenses.data.model.CategoryHelper.getIconForCategory(expense.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.category,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
                val time = SimpleDateFormat("h:mm a · MMM d", Locale.getDefault()).format(Date(expense.date))
                Text(time, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                if (expense.description.isNotBlank()) {
                    Text(
                        expense.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        maxLines = 1
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-₦${String.format("%,.0f", expense.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
