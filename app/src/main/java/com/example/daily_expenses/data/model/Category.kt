package com.example.daily_expenses.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

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
        "Other"
    )

    fun getIconForCategory(category: String): ImageVector {
        return when (category) {
            "Food", "Food & Dining" -> Icons.Default.Restaurant
            "Transport" -> Icons.Default.DirectionsCar
            "Data", "Bills & Utilities" -> Icons.Default.Wifi
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
            else -> 3
        }
        return palette[idx % palette.size]
    }
}
