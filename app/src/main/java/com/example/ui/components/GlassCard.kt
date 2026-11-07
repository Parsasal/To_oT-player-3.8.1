package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ThemeStyledCard

/**
 * Universal Glass & Material Card container.
 * Delegates to ThemeStyledCard to apply deep UI theme materials,
 * textures, scanlines, hard-shadow offsets, and custom border brushes.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    border: BorderStroke? = null,
    backgroundColor: Color? = null,
    backgroundBrush: Brush? = null,
    contentPadding: Dp = 16.dp,
    isPlaying: Boolean = false,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    ThemeStyledCard(
        modifier = modifier,
        isPlaying = isPlaying,
        isSelected = isSelected,
        shape = shape,
        border = border,
        backgroundBrush = backgroundBrush ?: (if (backgroundColor != null) Brush.linearGradient(listOf(backgroundColor, backgroundColor)) else null),
        contentPadding = contentPadding,
        onClick = onClick,
        content = content
    )
}
