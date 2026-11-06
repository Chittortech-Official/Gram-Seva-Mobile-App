package com.chittortech.gramseva.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GramPrimary,
    onPrimary = TextLight,
    primaryContainer = GramPrimaryLight,
    onPrimaryContainer = GramPrimaryDark,
    secondary = GramGold,
    onSecondary = TextLight,
    secondaryContainer = GramGoldLight,
    onSecondaryContainer = GramNavy,
    background = GramBackground,
    onBackground = TextPrimary,
    surface = GramCardWhite,
    onSurface = TextPrimary,
    surfaceVariant = GramSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GramBorder
)

@Composable
fun GramSevaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = GramCardWhite.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

