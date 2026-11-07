package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun TootPlayerTheme(
    themeStyle: AppThemeStyle = AppThemeStyle.EARTHY_BIOPHILIC,
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val palette = createTootPalette(themeStyle, isDarkMode = isDarkMode)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // When in dark mode, system status bar icons (clock, battery) must be light (white)
                // When in light mode, system status bar icons must be dark
                insetsController.isAppearanceLightStatusBars = !isDarkMode
            }
        }
    }

    val colorScheme = if (isDarkMode) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = Color.Black,
            primaryContainer = palette.primary.copy(alpha = 0.25f),
            onPrimaryContainer = Color.White,
            secondary = palette.secondary,
            onSecondary = Color.White,
            secondaryContainer = palette.secondary.copy(alpha = 0.25f),
            onSecondaryContainer = Color.White,
            tertiary = AmberSolo,
            onTertiary = Color.Black,
            background = palette.bgStart,
            onBackground = palette.textPrimary,
            surface = palette.bgCenter,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.bgEnd,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.glassBorder,
            error = RoseMute,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = Color.White,
            primaryContainer = palette.primary.copy(alpha = 0.15f),
            onPrimaryContainer = palette.primary,
            secondary = palette.secondary,
            onSecondary = Color.White,
            secondaryContainer = palette.secondary.copy(alpha = 0.15f),
            onSecondaryContainer = palette.secondary,
            tertiary = Color(0xFFD97706),
            onTertiary = Color.White,
            background = palette.bgStart,
            onBackground = palette.textPrimary,
            surface = palette.bgCenter,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.bgEnd,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.glassBorder,
            error = RoseMute,
            onError = Color.White
        )
    }

    CompositionLocalProvider(LocalAppThemePalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
