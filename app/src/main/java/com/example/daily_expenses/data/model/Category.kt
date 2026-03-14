package com.example.daily_expenses.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryHelper {
    val predefinedCategories = listOf(
        "Food & Dining",
        "Transport",
        "Shopping",
        "Bills & Utilities",
        "Entertainment",
        "Health",
        "Other"
    )

    fun getIconForCategory(category: String): ImageVector {
        return when (category) {
            "Food & Dining" -> Icons.Default.Restaurant
            "Transport" -> Icons.Default.DirectionsCar
            "Shopping" -> Icons.Default.ShoppingBag
            "Bills & Utilities" -> Icons.Default.Receipt
            "Entertainment" -> Icons.Default.Movie
            "Health" -> Icons.Default.MedicalServices
            else -> Icons.Default.MoreHoriz
        }
    }
}
