package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MovieRedPrimary,
    onPrimary = Color.White,
    primaryContainer = MovieRedDark,
    onPrimaryContainer = Color.White,
    secondary = MovieGold,
    onSecondary = Color.Black,
    secondaryContainer = MovieSurfaceVariant,
    onSecondaryContainer = MovieGoldLight,
    tertiary = MovieBlueVip,
    onTertiary = Color.White,
    background = MovieDarkBg,
    onBackground = MovieTextPrimary,
    surface = MovieSurface,
    onSurface = MovieTextPrimary,
    surfaceVariant = MovieSurfaceVariant,
    onSurfaceVariant = MovieTextSecondary,
    outline = MovieBorder,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Movie SW defaults to dark cinematic theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
