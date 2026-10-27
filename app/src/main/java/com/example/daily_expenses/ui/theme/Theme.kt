package com.example.daily_expenses.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class SpendlyExtendedColors(
    val badgeUpBg: Color,
    val badgeUpText: Color,
    val badgeDownBg: Color,
    val badgeDownText: Color,
    val categories: List<Color>,
    val isDark: Boolean
)

val LocalSpendlyColors = staticCompositionLocalOf {
    SpendlyExtendedColors(
        badgeUpBg = LightBadgeUpBg,
        badgeUpText = LightBadgeUpText,
        badgeDownBg = LightBadgeDownBg,
        badgeDownText = LightBadgeDownText,
        categories = CategoryColorsLight,
        isDark = false
    )
}

object SpendlyThemeExtras {
    val colors: SpendlyExtendedColors
        @Composable
        get() = LocalSpendlyColors.current
}

@Composable
fun SpendlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SpendlyDark else SpendlyLight
    val extendedColors = if (darkTheme) {
        SpendlyExtendedColors(
            badgeUpBg = DarkBadgeUpBg,
            badgeUpText = DarkBadgeUpText,
            badgeDownBg = DarkBadgeDownBg,
            badgeDownText = DarkBadgeDownText,
            categories = CategoryColorsDark,
            isDark = true
        )
    } else {
        SpendlyExtendedColors(
            badgeUpBg = LightBadgeUpBg,
            badgeUpText = LightBadgeUpText,
            badgeDownBg = LightBadgeDownBg,
            badgeDownText = LightBadgeDownText,
            categories = CategoryColorsLight,
            isDark = false
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalSpendlyColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
