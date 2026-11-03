package com.example.daily_expenses.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.daily_expenses.data.local.Expense

enum class BudgetBucket(
    val label: String,
    val defaultRatio: Float,
    val subtitle: String,
    val icon: ImageVector
) {
    NEEDS("Needs", 0.50f, "50% of income", Icons.Default.Home),
    INVESTMENT("Investment", 0.30f, "30% of income", Icons.Default.Savings),
    WANTS("Wants", 0.20f, "20% of income", Icons.Default.ShoppingBag);

    companion object {
        fun fromString(value: String): BudgetBucket {
            return entries.firstOrNull { it.label.equals(value, ignoreCase = true) } ?: NEEDS
        }
    }
}

object CategoryHelper {
    val predefinedCategories = listOf(
        "Food",
        "Transport",
        "Data",
        "Sadaqah",
        "Other"
    )

    val incomeSources = listOf(
        "Salary",
        "Gift / Dash",
        "Side Gig",
        "Other"
    )

    fun isSadaqahExpense(expense: Expense): Boolean {
        if (expense.category.equals("Sadaqah", ignoreCase = true)) return true
        val text = "${expense.category} ${expense.description}".lowercase()
        return text.contains("sadaqah") ||
            text.contains("sadaqa") ||
            text.contains("masjid") ||
            text.contains("ateeq") ||
            text.contains("mosque") ||
            text.contains("zakat")
    }

    fun isDailyNeedExpense(expense: Expense): Boolean {
        if (expense.bucket != "Expense") return false
        if (isSadaqahExpense(expense)) return true
        return expense.category == "Food" ||
            expense.category == "Transport" ||
            expense.category == "Data" ||
            expense.category == "Sadaqah"
    }

    fun getIconForCategory(category: String): ImageVector {
        return when (category) {
            "Food", "Food & Dining" -> Icons.Default.Restaurant
            "Transport" -> Icons.Default.DirectionsCar
            "Data", "Bills & Utilities" -> Icons.Default.Wifi
            "Sadaqah" -> Icons.Default.VolunteerActivism
            "Shopping" -> Icons.Default.ShoppingBag
            else -> Icons.Default.MoreHoriz
        }
    }

    fun getColorForCategory(category: String, palette: List<Color>): Color {
        if (palette.isEmpty()) return Color.Gray
        val idx = when (category) {
            "Food", "Food & Dining" -> 0
            "Transport" -> 1
            "Data", "Bills & Utilities" -> 2
            "Sadaqah" -> 3
            else -> 3
        }
        return palette[idx % palette.size]
    }
}
