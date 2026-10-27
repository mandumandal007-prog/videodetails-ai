package com.videodetails.ai.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4D6BFE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1E7FF),
    onPrimaryContainer = Color(0xFF101B4D),
    secondary = Color(0xFF6A5AE0),
    onSecondary = Color.White,
    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF0D0D0D),
    surface = Color.White,
    onSurface = Color(0xFF0D0D0D),
    surfaceVariant = Color(0xFFEDEFF7),
    onSurfaceVariant = Color(0xFF4A4F63),
    outline = Color(0xFFD5D8E5),
    error = Color(0xFFD93025),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9AA9FF),
    onPrimary = Color(0xFF0B1B5E),
    primaryContainer = Color(0xFF23306E),
    onPrimaryContainer = Color(0xFFE1E7FF),
    secondary = Color(0xFFB9B0FF),
    onSecondary = Color(0xFF1B1440),
    background = Color(0xFF0E1016),
    onBackground = Color(0xFFF2F3F7),
    surface = Color(0xFF161A23),
    onSurface = Color(0xFFF2F3F7),
    surfaceVariant = Color(0xFF232838),
    onSurfaceVariant = Color(0xFFC3C7D6),
    outline = Color(0xFF39405A),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF3B0905)
)

@Composable
fun AppTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content
    )
}
