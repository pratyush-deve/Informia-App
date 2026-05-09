package com.pratyush.infoapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VaultBlue,
    secondary = VaultMint,
    tertiary = VaultCoral,
    background = VaultNight,
    surface = VaultSurface,
    surfaceVariant = VaultSurfaceBright,
    onPrimary = VaultNight,
    onSecondary = VaultNight,
    onTertiary = VaultNight,
    onBackground = VaultText,
    onSurface = VaultText,
    onSurfaceVariant = VaultMuted
)

private val LightColorScheme = lightColorScheme(
    primary = VaultBlue,
    secondary = VaultMint,
    tertiary = VaultCoral,
    background = Color(0xFFF3F6FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6EDF7),
    onPrimary = Color(0xFF04111E),
    onSecondary = Color(0xFF04111E),
    onTertiary = Color(0xFF04111E),
    onBackground = Color(0xFF0F1724),
    onSurface = Color(0xFF0F1724),
    onSurfaceVariant = Color(0xFF4E5A6B)
)

@Composable
fun InfoAppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
