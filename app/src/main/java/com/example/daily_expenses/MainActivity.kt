package com.example.daily_expenses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.daily_expenses.ui.home.HomeScreen
import com.example.daily_expenses.ui.home.HomeViewModel
import com.example.daily_expenses.ui.home.HomeViewModelFactory
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

sealed class Screen(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Home : Screen("home", "Home", { Icon(Icons.Default.Home, contentDescription = null) })
    object Summary : Screen("summary", "Summary", { Icon(Icons.Default.PieChart, contentDescription = null) })
}

@Composable
fun SpendlyApp(repository: com.example.daily_expenses.data.repository.ExpenseRepository) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Home, Screen.Summary)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = screen.icon,
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository))
                HomeScreen(viewModel)
            }
            composable(Screen.Summary.route) {
                val viewModel: SummaryViewModel = viewModel(factory = SummaryViewModelFactory(repository))
                SummaryScreen(viewModel)
            }
        }
    }
}