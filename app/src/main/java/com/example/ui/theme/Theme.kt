package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = Color(0xFF0F1113),
    secondary = ElectricCyan,
    onSecondary = Color(0xFF0F1113),
    tertiary = ActiveBlue,
    background = ObsidianBackground,
    surface = SlateCard,
    onBackground = WhitePure,
    onSurface = WhitePure,
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFE1E2E4),
    error = BossRed,
    onError = Color(0xFF0F1113)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
