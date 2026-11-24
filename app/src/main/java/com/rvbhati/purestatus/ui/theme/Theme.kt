package com.rvbhati.purestatus.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldGreen,
    onPrimary = OledBackground,
    primaryContainer = OledGlassSurface,
    onPrimaryContainer = EmeraldGreen,
    secondary = NeonCyan,
    onSecondary = OledBackground,
    background = OledBackground,
    onBackground = TextPrimaryDark,
    surface = OledCardSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = OledGlassSurface,
    onSurfaceVariant = TextSecondaryDark,
    outline = OledBorder
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppGreen,
    onPrimary = DayCardSurface,
    primaryContainer = DayGlassSurface,
    onPrimaryContainer = WhatsAppGreen,
    secondary = ElectricBlue,
    onSecondary = DayCardSurface,
    background = DayBackground,
    onBackground = TextPrimaryLight,
    surface = DayCardSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = DayGlassSurface,
    onSurfaceVariant = TextSecondaryLight,
    outline = DayBorder
)

@Composable
fun PureStatusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
