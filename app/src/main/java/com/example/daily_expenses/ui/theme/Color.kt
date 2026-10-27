package com.example.daily_expenses.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ─── Shared Forest & Sage Brand Accents ────────────────────────────────────
val ForestDark           = Color(0xFF1A2B21)
val ForestDeep           = Color(0xFF0F1512)
val ForestCard           = Color(0xFF17201B)
val ForestBorder         = Color(0xFF26322B)
val ForestTrack          = Color(0xFF2F4538)
val SageGreen            = Color(0xFF9CC5A1)
val SageMid              = Color(0xFF6E9C79)
val SageDark             = Color(0xFF4A7358)
val SageDeep             = Color(0xFF2F4D3C)

// ─── Light Beige Palette (with Forest & Sage primary colors) ───────────────
val LightBackground      = Color(0xFFF4EFE8)
val LightSurface         = Color(0xFFFBF8F3)
val LightText            = Color(0xFF1A2B21) // Deep forest text
val LightMuted           = Color(0xFF5C6B61) // Muted forest-sage grey
val LightBorder          = Color(0xFFE4DCCF)
val LightAccent          = ForestDark        // Primary forest green (#1A2B21)
val LightOnAccent        = Color(0xFFFBF8F3)

val LightBadgeUpBg       = Color(0xFFF6E1DC)
val LightBadgeUpText     = Color(0xFF9B2F1A)
val LightBadgeDownBg     = Color(0xFFE0EBDD)
val LightBadgeDownText   = Color(0xFF2A5F39)

// ─── Dark Forest & Sage Palette ────────────────────────────────────────────
val DarkBackground       = Color(0xFF0F1512)
val DarkSurface          = Color(0xFF17201B)
val DarkText             = Color(0xFFE9EFE8)
val DarkMuted            = Color(0xFF8FA596)
val DarkBorder           = Color(0xFF26322B)
val DarkAccent           = SageGreen         // Primary sage green (#9CC5A1)
val DarkOnAccent         = Color(0xFF0F1512)

val DarkBadgeUpBg        = Color(0xFF3A2220)
val DarkBadgeUpText      = Color(0xFFF0A090)
val DarkBadgeDownBg      = Color(0xFF1B3324)
val DarkBadgeDownText    = Color(0xFF8CC79B)

// ─── Icon Tokens ───────────────────────────────────────────────────────────
val IconBackground       = Color(0xFF1A2B21)
val IconTrack            = Color(0xFF2F4538)
val IconRing             = Color(0xFF9CC5A1)
val IconTick             = Color(0xFFE9EFE8)

// ─── Category Palettes (Forest & Sage in both Light and Dark modes) ────────
val CategoryColorsLight = listOf(
    Color(0xFF1A2B21), // Forest
    Color(0xFF4A7358), // Deep Sage
    Color(0xFF6E9C79), // Mid Sage
    Color(0xFF9CC5A1)  // Light Sage
)

val CategoryColorsDark = listOf(
    Color(0xFF9CC5A1), // Light Sage
    Color(0xFF6E9C79), // Mid Sage
    Color(0xFF4A7358), // Deep Sage
    Color(0xFF2F4D3C)  // Dark Forest-Sage
)

// ─── Material 3 Color Schemes ──────────────────────────────────────────────
val SpendlyLight = lightColorScheme(
    background           = LightBackground,
    surface              = LightSurface,
    surfaceVariant       = Color(0xFFE9E2D8),
    onBackground         = LightText,
    onSurface            = LightText,
    onSurfaceVariant     = LightMuted,
    outline              = LightBorder,
    primary              = ForestDark,
    onPrimary            = LightSurface,
    primaryContainer     = SageGreen,
    onPrimaryContainer   = ForestDark,
    secondary            = SageDark,
    onSecondary          = LightSurface,
    tertiary             = SageGreen,
    onTertiary           = ForestDark,
    error                = LightBadgeUpText,
    onError              = LightOnAccent,
    errorContainer       = LightBadgeUpBg,
    onErrorContainer     = LightBadgeUpText
)

val SpendlyDark = darkColorScheme(
    background           = DarkBackground,
    surface              = DarkSurface,
    surfaceVariant       = Color(0xFF223029),
    onBackground         = DarkText,
    onSurface            = DarkText,
    onSurfaceVariant     = DarkMuted,
    outline              = DarkBorder,
    primary              = SageGreen,
    onPrimary            = DarkOnAccent,
    primaryContainer     = ForestTrack,
    onPrimaryContainer   = DarkText,
    secondary            = SageMid,
    onSecondary          = DarkOnAccent,
    tertiary             = SageDark,
    onTertiary           = DarkText,
    error                = DarkBadgeUpText,
    onError              = DarkOnAccent,
    errorContainer       = DarkBadgeUpBg,
    onErrorContainer     = DarkBadgeUpText
)
