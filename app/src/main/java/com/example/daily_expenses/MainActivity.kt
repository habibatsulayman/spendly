package com.example.daily_expenses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.daily_expenses.ui.home.AddExpenseSheet
import com.example.daily_expenses.ui.home.HomeScreen
import com.example.daily_expenses.ui.home.HomeViewModel
import com.example.daily_expenses.ui.home.HomeViewModelFactory
import com.example.daily_expenses.ui.home.IncomeScreen
import com.example.daily_expenses.ui.home.WantsAndNeedsScreen
import com.example.daily_expenses.ui.summary.SummaryScreen
import com.example.daily_expenses.ui.summary.SummaryViewModel
import com.example.daily_expenses.ui.summary.SummaryViewModelFactory
import com.example.daily_expenses.ui.theme.SpendlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as SpendlyApp).repository
        setContent {
            SpendlyTheme {
                SpendlyApp(repository)
            }
        }
    }
}

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun SpendlyApp(repository: com.example.daily_expenses.data.repository.ExpenseRepository) {
    val navController = rememberNavController()
    val navItems = listOf(
        NavItem("home", "Home", Icons.Outlined.Home),
        NavItem("budget_income", "Budget & Income", Icons.Outlined.AccountBalanceWallet),
        NavItem("wants_needs", "Wants & Needs", Icons.Outlined.Checklist),
        NavItem("insights", "Insights", Icons.Outlined.PieChart),
    )

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(repository, LocalContext.current)
    )
    val summaryViewModel: SummaryViewModel = viewModel(
        factory = SummaryViewModelFactory(repository)
    )

    val editingExpense by homeViewModel.editingExpense.collectAsState()
    val selectedDay by homeViewModel.selectedDay.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var addSheetInitialBucket by remember { mutableStateOf("Expense") }

    LaunchedEffect(editingExpense) {
        if (editingExpense != null) {
            addSheetInitialBucket = editingExpense!!.bucket
            showAddSheet = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    navItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onAddClick = {
                        homeViewModel.setEditingExpense(null)
                        addSheetInitialBucket = "Expense"
                        showAddSheet = true
                    },
                    onAddIncomeClick = {
                        homeViewModel.setEditingExpense(null)
                        addSheetInitialBucket = "Income"
                        showAddSheet = true
                    }
                )
            }
            composable("budget_income") {
                IncomeScreen(
                    viewModel = homeViewModel,
                    onAddIncome = {
                        homeViewModel.setEditingExpense(null)
                        addSheetInitialBucket = "Income"
                        showAddSheet = true
                    }
                )
            }
            composable("wants_needs") {
                WantsAndNeedsScreen(
                    viewModel = homeViewModel,
                    onAddNeed = {
                        homeViewModel.setEditingExpense(null)
                        addSheetInitialBucket = "Needs"
                        showAddSheet = true
                    },
                    onAddWant = {
                        homeViewModel.setEditingExpense(null)
                        addSheetInitialBucket = "Wants"
                        showAddSheet = true
                    },
                    onOpenBudgetAndIncome = {
                        navController.navigate("budget_income") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable("insights") {
                SummaryScreen(
                    summaryViewModel = summaryViewModel,
                    homeViewModel = homeViewModel
                )
            }
        }
    }

    if (showAddSheet) {
        AddExpenseSheet(
            editingExpense = editingExpense,
            initialBucket = addSheetInitialBucket,
            initialDay = selectedDay,
            onDismiss = {
                showAddSheet = false
                homeViewModel.setEditingExpense(null)
            },
            onDelete = { expenseToDelete ->
                homeViewModel.deleteExpense(expenseToDelete)
            },
            onSave = { amount, category, description, bucket, dateMillis ->
                val editing = editingExpense
                if (editing != null) {
                    homeViewModel.updateExpense(
                        editing.copy(
                            amount = amount,
                            category = category,
                            description = description,
                            bucket = bucket,
                            date = dateMillis
                        )
                    )
                } else {
                    when (bucket) {
                        "Needs", "Wants" -> {
                            homeViewModel.addPlannedItem(
                                title = category,
                                note = description,
                                amount = amount,
                                bucket = bucket
                            )
                        }
                        "Income" -> {
                            homeViewModel.addIncome(
                                amount = amount,
                                source = category,
                                note = description,
                                timestamp = dateMillis
                            )
                        }
                        else -> {
                            homeViewModel.addDailyExpense(
                                amount = amount,
                                category = category,
                                description = description,
                                timestamp = dateMillis
                            )
                        }
                    }
                }
                showAddSheet = false
            }
        )
    }
}
