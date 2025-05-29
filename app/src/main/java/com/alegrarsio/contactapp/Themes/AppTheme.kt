package com.alegrarsio.contactapp.Themes

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

enum class AppTheme {
    DEFAULT, ORANGE, TEAL
}

val DefaultLightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    primaryContainer = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    onPrimaryContainer = Color.Black,
)

val DefaultDarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    primaryContainer = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC5),
    onPrimaryContainer = Color.White
)

val OrangeLightColorScheme = lightColorScheme(
    primary = Color(0xFFFF9800),
    primaryContainer = Color(0xFFFFB74D),
    secondary = Color(0xFFFFC107),
    onPrimaryContainer = Color.Black,
    // Define other colors for the orange theme
    background = Color(0xFFFFF3E0), // Light orange background
    surface = Color(0xFFFFE0B2),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

val TealLightColorScheme = lightColorScheme(
    primary = Color(0xFF009688), // Teal 500
    primaryContainer = Color(0xFF80CBC4), // Teal 200
    secondary = Color(0xFF00796B), // Teal 700
    onPrimaryContainer = Color.Black,
    background = Color(0xFFE0F2F1),
    surface = Color(0xFFB2DFDB),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)