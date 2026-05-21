package com.dormiq.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DormiqLightColorScheme = lightColorScheme(
    primary = DormiqBlue,
    onPrimary = DormiqSurface,
    primaryContainer = DormiqBlueGlow,
    onPrimaryContainer = DormiqBlue,
    secondary = DormiqTeal,
    onSecondary = DormiqSurface,
    secondaryContainer = DormiqTealGlow,
    onSecondaryContainer = DormiqTeal,
    tertiary = DormiqPurple,
    onTertiary = DormiqSurface,
    background = DormiqNavy,
    onBackground = DormiqText,
    surface = DormiqSurface,
    onSurface = DormiqText,
    surfaceVariant = DormiqCard,
    onSurfaceVariant = DormiqTextSecondary,
    error = DormiqRed,
    onError = DormiqSurface,
    outline = DormiqBorder,
    outlineVariant = DormiqDivider,
)

@Composable
fun DormiqTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DormiqLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DormiqTypography,
        content = content
    )
}
