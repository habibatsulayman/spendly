package com.example.daily_expenses.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SpendlyPurpleLight,
    secondary = SpendlyPurple,
    tertiary = SpendlyPurpleDark,
    background = SpendlyOnSurface,
    surface = Color(0xFF1C1B1F),
    onPrimary = SpendlyWhite,
    onSecondary = SpendlyWhite,
    onTertiary = SpendlyWhite,
    onBackground = SpendlyWhite,
    onSurface = SpendlyWhite
)

private val LightColorScheme = lightColorScheme(
    primary = SpendlyPurple,
    secondary = SpendlyPurpleLight,
    tertiary = SpendlyPurpleDark,
    background = SpendlyBackground,
    surface = SpendlySurface,
    onPrimary = SpendlyWhite,
    onSecondary = SpendlyWhite,
    onTertiary = SpendlyWhite,
    onBackground = SpendlyOnBackground,
    onSurface = SpendlyOnSurface
)


@Composable
fun SpendlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for consistent branding
    content: @Composable () -> Unit
) {

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}