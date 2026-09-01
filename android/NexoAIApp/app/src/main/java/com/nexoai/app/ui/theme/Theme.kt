package com.nexoai.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NexoAIDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D9FF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004E5C),
    onPrimaryContainer = Color(0xFF00F7FF),
    
    secondary = Color(0xFF00D9FF),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF004E5C),
    onSecondaryContainer = Color(0xFF00F7FF),
    
    tertiary = Color(0xFF6A4C8A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF8F6CB8),
    onTertiaryContainer = Color(0xFF3D2653),
    
    error = Color(0xFFFF6B6B),
    onError = Color.White,
    errorContainer = Color(0xFFFF1744),
    onErrorContainer = Color(0xFFFFEBEE),
    
    background = Color(0xFF0F1419),
    onBackground = Color(0xFFE6EBF5),
    
    surface = Color(0xFF1A1F26),
    onSurface = Color(0xFFE6EBF5),
    
    surfaceVariant = Color(0xFF2A3038),
    onSurfaceVariant = Color(0xFFB0B9C1)
)

@Composable
fun NexoAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NexoAIDarkColorScheme,
        typography = NexoAITypography,
        content = content
    )
}
