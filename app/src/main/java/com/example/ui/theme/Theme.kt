package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisBackground,
    primaryContainer = JarvisSurfaceElevated,
    onPrimaryContainer = JarvisCyanBright,
    secondary = JarvisGold,
    onSecondary = JarvisBackground,
    secondaryContainer = JarvisSurface,
    onSecondaryContainer = JarvisGold,
    tertiary = JarvisCyanBright,
    background = JarvisBackground,
    onBackground = JarvisTextPrimary,
    surface = JarvisSurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceElevated,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisSurfaceBorder,
    error = JarvisRed,
    onError = JarvisBackground
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
