package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.ui.theme.TootThemePalette

/**
 * SystemStatusBarBoundary
 *
 * Visually and clearly delineates the Android OS status bar area (clock, battery, Wi-Fi, notifications)
 * from the TO_OT Player application interface.
 *
 * Features:
 * 1. Safe status bar height calculation with fallback to ensure the separator line is always positioned
 *    precisely at the boundary between the phone's indicators and the application canvas.
 * 2. Translucent contrast scrim behind the phone's clock, battery, and signal indicators so they remain
 *    100% legible across all 11 themes (both dark and light modes).
 * 3. High-visibility, full-width edge-to-edge separator line that maintains solid clarity on both left
 *    and right edges directly underneath the clock and battery indicators.
 * 4. Refined ambient downward glow for sophisticated Material 3 visual harmony without degrading aesthetics.
 */
@Composable
fun SystemStatusBarBoundary(
    themePalette: TootThemePalette,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val statusBarInsets = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    // Safe minimum height so line is always positioned directly beneath status bar indicators
    val effectiveStatusBarHeight = if (statusBarInsets >= 24.dp) statusBarInsets else 28.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(10f)
            .testTag("system_status_bar_boundary")
    ) {
        // Subtle contrast backing behind phone's status indicators (clock, battery, Wi-Fi)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(effectiveStatusBarHeight)
                .background(
                    if (isDarkMode) Color.Black.copy(alpha = 0.32f)
                    else Color.White.copy(alpha = 0.45f)
                )
        )

        // Clear, high-contrast, edge-to-edge separator line
        // Spans full width with solid opacity so the boundary directly under the clock (left)
        // and battery (right) is distinct and sharp in every theme
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(
                    if (isDarkMode) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                themePalette.primary.copy(alpha = 0.85f),
                                themePalette.secondary.copy(alpha = 0.95f),
                                themePalette.primary.copy(alpha = 0.85f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                themePalette.primary.copy(alpha = 0.75f),
                                themePalette.secondary.copy(alpha = 0.85f),
                                themePalette.primary.copy(alpha = 0.75f)
                            )
                        )
                    }
                )
        )

        // Soft ambient downward glow for seamless visual harmony
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            themePalette.primary.copy(alpha = if (isDarkMode) 0.20f else 0.12f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

