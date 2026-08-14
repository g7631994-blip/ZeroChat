package com.zeroclone.app.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZeroChatDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4D6BFE),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFF0F0F0F),
    onBackground = Color(0xFFE3E3E3),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE3E3E3),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFE3E3E3)
)

@Composable
fun ZeroChatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ZeroChatDarkColorScheme,
        content = content
    )
}
