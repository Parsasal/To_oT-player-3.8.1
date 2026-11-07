package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Universal Theme-Styled Card Container for TO_OT Player
 * Implements 11 Distinct Visual Paradigms
 */
@Composable
fun ThemeStyledCard(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isSelected: Boolean = false,
    shape: Shape? = null,
    border: BorderStroke? = null,
    backgroundBrush: Brush? = null,
    contentPadding: Dp = 14.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalAppThemePalette.current
    val effectiveShape = shape ?: palette.cardShape

    // Theme-specific background brush
    val effectiveBgBrush = backgroundBrush ?: when (palette.style) {
        AppThemeStyle.COSMIC_AURORA -> {
            if (isPlaying) {
                Brush.linearGradient(listOf(Color(0xFF1E2355).copy(alpha = 0.85f), Color(0xFF13173B).copy(alpha = 0.90f)))
            } else {
                Brush.linearGradient(listOf(Color(0xFF131738).copy(alpha = 0.70f), Color(0xFF0C0F26).copy(alpha = 0.80f)))
            }
        }
        AppThemeStyle.EARTHY_BIOPHILIC -> {
            if (isPlaying) {
                Brush.verticalGradient(listOf(Color(0xFF455541), Color(0xFF334030)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFF384635), Color(0xFF2C382A)))
            }
        }
        AppThemeStyle.MINIMALIST_MONOCHROME -> Brush.linearGradient(listOf(Color.Black, Color.Black))
        AppThemeStyle.RETRO_ANALOG -> {
            if (isPlaying) {
                Brush.verticalGradient(listOf(Color(0xFF422C1C), Color(0xFF2C1E13)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFF342316), Color(0xFF24180E)))
            }
        }
        AppThemeStyle.RETRO_TERMINAL -> {
            if (isPlaying) {
                Brush.verticalGradient(listOf(Color(0xFF07200D), Color(0xFF031006)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFF041007), Color(0xFF010603)))
            }
        }
        AppThemeStyle.MODERN_BRUTALISM -> Brush.linearGradient(listOf(Color(0xFFFFE500), Color(0xFFFFE500)))
        AppThemeStyle.CLAYMORPHIC_PASTEL -> {
            if (isPlaying) {
                Brush.verticalGradient(listOf(Color(0xFFE5DDF5), Color(0xFFD3C8E7)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFDCD4EC), Color(0xFFCEC2E0)))
            }
        }
        AppThemeStyle.HOLOGRAPHIC_JELLY -> {
            if (isPlaying) {
                Brush.linearGradient(listOf(Color(0x6500F5D4), Color(0x45E056FD), Color(0x551E143E)))
            } else {
                Brush.linearGradient(listOf(Color(0x451A1238), Color(0x35281B4E), Color(0x45140E2D)))
            }
        }
        AppThemeStyle.NEUMORPHIC_GLASS -> {
            if (isPlaying) {
                Brush.verticalGradient(listOf(Color(0xFFEBF2FA), Color(0xFFDDE6F0)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFE2EAF2), Color(0xFFD5DFEC)))
            }
        }
        AppThemeStyle.CYBERPUNK_NEON -> {
            if (isPlaying) {
                Brush.linearGradient(listOf(Color(0xFF0F172E).copy(alpha = 0.95f), Color(0xFF060B16).copy(alpha = 0.98f)))
            } else {
                Brush.linearGradient(listOf(Color(0xFF080D1A).copy(alpha = 0.85f), Color(0xFF03050C).copy(alpha = 0.90f)))
            }
        }
        AppThemeStyle.DARK_LUXURY -> {
            if (isPlaying) {
                Brush.verticalGradient(listOf(Color(0xFF1E1D18), Color(0xFF131316)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFF16161A), Color(0xFF101013)))
            }
        }
        AppThemeStyle.CRIMSON_CARBON -> {
            if (isPlaying) {
                Brush.linearGradient(listOf(Color(0xFF280C12).copy(alpha = 0.95f), Color(0xFF14070A).copy(alpha = 0.98f)))
            } else {
                Brush.linearGradient(listOf(Color(0xFF16161D).copy(alpha = 0.88f), Color(0xFF0C0C10).copy(alpha = 0.92f)))
            }
        }
        AppThemeStyle.SCARLET_STUDIO -> {
            if (isPlaying) {
                Brush.verticalGradient(listOf(Color(0xFFCE1840), Color(0xFFB51134)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFBE163C), Color(0xFFA81033)))
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "theme_card_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val effectiveBorder = border ?: when {
        isPlaying -> {
            when (palette.style) {
                AppThemeStyle.MODERN_BRUTALISM -> BorderStroke(2.5.dp, Color.Black)
                AppThemeStyle.MINIMALIST_MONOCHROME -> BorderStroke(1.5.dp, Color.White)
                AppThemeStyle.RETRO_TERMINAL -> BorderStroke(1.5.dp, Color(0xFF33FF66).copy(alpha = pulseAlpha))
                AppThemeStyle.CYBERPUNK_NEON -> BorderStroke(1.8.dp, Brush.linearGradient(listOf(Color(0xFF00F0FF), Color(0xFFE056FD).copy(alpha = pulseAlpha))))
                AppThemeStyle.CRIMSON_CARBON -> BorderStroke(1.6.dp, Brush.linearGradient(listOf(Color(0xFFFF1E40), Color.White.copy(alpha = pulseAlpha), Color(0xFFFF1E40))))
                AppThemeStyle.SCARLET_STUDIO -> BorderStroke(1.4.dp, Brush.linearGradient(listOf(Color(0xFF1A0206), Color(0xFF4A0512))))
                AppThemeStyle.COSMIC_AURORA -> BorderStroke(1.6.dp, Brush.linearGradient(listOf(Color(0xFF00F5D4), Color(0xFF818CF8).copy(alpha = pulseAlpha))))
                AppThemeStyle.DARK_LUXURY -> BorderStroke(1.4.dp, Brush.linearGradient(listOf(Color(0xFFF3E5AB), Color(0xFFD4AF37).copy(alpha = pulseAlpha))))
                AppThemeStyle.RETRO_ANALOG -> BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFFD4AF37), Color(0xFF8A653E))))
                AppThemeStyle.HOLOGRAPHIC_JELLY -> BorderStroke(1.6.dp, Brush.linearGradient(listOf(Color(0xFF00F5D4), Color(0xFFE056FD).copy(alpha = pulseAlpha), Color(0xFF38BDF8))))
                AppThemeStyle.CLAYMORPHIC_PASTEL -> BorderStroke(1.4.dp, Color(0xFFD4AF37).copy(alpha = pulseAlpha))
                AppThemeStyle.NEUMORPHIC_GLASS -> BorderStroke(1.2.dp, Color(0xFF06B6D4).copy(alpha = pulseAlpha))
                AppThemeStyle.EARTHY_BIOPHILIC -> BorderStroke(1.4.dp, Brush.linearGradient(listOf(Color(0xFFD97757), Color(0xFF7E9F59).copy(alpha = pulseAlpha))))
            }
        }
        isSelected -> BorderStroke(
            palette.borderStrokeWidth * 1.3f,
            Brush.linearGradient(listOf(palette.secondary, palette.primary))
        )
        palette.style == AppThemeStyle.MODERN_BRUTALISM -> BorderStroke(2.5.dp, Color.Black)
        palette.style == AppThemeStyle.MINIMALIST_MONOCHROME -> BorderStroke(1.2.dp, Color.White)
        palette.style == AppThemeStyle.RETRO_TERMINAL -> BorderStroke(1.dp, Color(0xFF33FF66).copy(alpha = 0.7f))
        palette.style == AppThemeStyle.DARK_LUXURY -> BorderStroke(0.9.dp, Color(0xFFD4AF37).copy(alpha = 0.40f))
        palette.style == AppThemeStyle.CRIMSON_CARBON -> BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFFFF1E40).copy(alpha = 0.6f), Color.White.copy(alpha = 0.4f))))
        palette.style == AppThemeStyle.SCARLET_STUDIO -> BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFF6E071D), Color(0xFF520515))))
        palette.style == AppThemeStyle.COSMIC_AURORA -> BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFF818CF8).copy(alpha = 0.4f), Color(0xFF00F5D4).copy(alpha = 0.4f))))
        palette.style == AppThemeStyle.CYBERPUNK_NEON -> BorderStroke(1.4.dp, Color(0xFF00F0FF).copy(alpha = 0.5f))
        palette.style == AppThemeStyle.HOLOGRAPHIC_JELLY -> BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFF00F5D4).copy(alpha = 0.45f), Color(0xFFE056FD).copy(alpha = 0.35f))))
        else -> BorderStroke(palette.borderStrokeWidth, palette.cardBorderBrush)
    }

    Box(
        modifier = modifier
            .then(
                if (palette.hasHardOffsetShadow) {
                    Modifier.drawBehind {
                        // Neo-Brutalism Hard Shadow (offset 5dp, 5dp)
                        drawRoundRect(
                            color = Color.Black,
                            topLeft = Offset(5.dp.toPx(), 5.dp.toPx()),
                            size = size,
                            cornerRadius = CornerRadius(0f, 0f)
                        )
                    }
                } else if (palette.cardElevation > 0.dp || isPlaying) {
                    Modifier.shadow(
                        elevation = if (isPlaying) (palette.cardElevation + 4.dp).coerceAtLeast(6.dp) else palette.cardElevation,
                        shape = effectiveShape,
                        spotColor = if (isPlaying) palette.primary else palette.glowAccent,
                        ambientColor = palette.glowAccent.copy(alpha = 0.2f)
                    )
                } else Modifier
            )
            .clip(effectiveShape)
            .border(effectiveBorder, effectiveShape)
            .background(effectiveBgBrush)
            .drawBehind {
                val w = size.width
                val h = size.height

                // 1. Earthy & Biophilic Botanical Leaf Sprig Watermarks
                if (palette.style == AppThemeStyle.EARTHY_BIOPHILIC) {
                    val leafColor = Color(0xFF8FA96D).copy(alpha = 0.18f)
                    val stemStartX = w - 18.dp.toPx()
                    val stemStartY = h * 0.10f
                    val stemEndX = w - 46.dp.toPx()
                    val stemEndY = h * 0.90f
                    drawLine(
                        color = leafColor,
                        start = Offset(stemStartX, stemStartY),
                        end = Offset(stemEndX, stemEndY),
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    for (i in 1..4) {
                        val frac = i * 0.2f
                        val lx = stemStartX + (stemEndX - stemStartX) * frac
                        val ly = stemStartY + (stemEndY - stemStartY) * frac
                        val leafOffset = if (i % 2 == 0) 14.dp.toPx() else -14.dp.toPx()
                        drawLine(
                            color = leafColor,
                            start = Offset(lx, ly),
                            end = Offset(lx + leafOffset, ly - 6.dp.toPx()),
                            strokeWidth = 1.3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawCircle(
                            color = leafColor,
                            radius = 3.8.dp.toPx(),
                            center = Offset(lx + leafOffset, ly - 6.dp.toPx())
                        )
                    }
                }

                // 2. Cyberpunk HUD Corner Brackets & Circuit Traces
                if (palette.hasCyberHud) {
                    val hudColor = Color(0xFF00F0FF).copy(alpha = if (isPlaying) 0.85f else 0.45f)
                    val bracketLen = 14.dp.toPx()
                    // Top Right Bracket
                    drawLine(hudColor, Offset(w - bracketLen - 4.dp.toPx(), 4.dp.toPx()), Offset(w - 4.dp.toPx(), 4.dp.toPx()), 2.dp.toPx())
                    drawLine(hudColor, Offset(w - 4.dp.toPx(), 4.dp.toPx()), Offset(w - 4.dp.toPx(), 4.dp.toPx() + bracketLen), 2.dp.toPx())
                    // Bottom Left Bracket
                    drawLine(hudColor, Offset(4.dp.toPx(), h - bracketLen - 4.dp.toPx()), Offset(4.dp.toPx(), h - 4.dp.toPx()), 2.dp.toPx())
                    drawLine(hudColor, Offset(4.dp.toPx(), h - 4.dp.toPx()), Offset(4.dp.toPx() + bracketLen, h - 4.dp.toPx()), 2.dp.toPx())
                }

                // 3. Retro CRT Scanline Overlay
                if (palette.hasScanlineOverlay) {
                    val lineSpacing = 3.5.dp.toPx()
                    var y = 0f
                    while (y < h) {
                        drawLine(
                            color = Color(0xFF33FF66).copy(alpha = 0.06f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += lineSpacing
                    }
                }

                // 4. Holographic Specular Top Glass Reflection Highlight
                if (palette.hasIridescentRim) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF00F5D4).copy(alpha = 0.40f),
                                Color.White.copy(alpha = 0.65f),
                                Color(0xFFE056FD).copy(alpha = 0.40f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(16.dp.toPx(), 1.5.dp.toPx()),
                        end = Offset(w - 16.dp.toPx(), 1.5.dp.toPx()),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 5. Metallic Specular Sheen Gradient Highlight (Dark Luxury & Vintage Hi-Fi)
                if (palette.hasMetallicSheen) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFFD4AF37).copy(alpha = 0.30f),
                                Color(0xFFFFF6D6).copy(alpha = 0.60f),
                                Color(0xFFD4AF37).copy(alpha = 0.30f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(20.dp.toPx(), 1.5.dp.toPx()),
                        end = Offset(w - 20.dp.toPx(), 1.5.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // 6. Vintage Wood Texture Accents (Retro Analog)
                if (palette.hasWoodTexture) {
                    // Subtle brass corner screws
                    val screwColor = Color(0xFFD4AF37).copy(alpha = 0.65f)
                    val r = 2.5.dp.toPx()
                    drawCircle(screwColor, r, Offset(10.dp.toPx(), 10.dp.toPx()))
                    drawCircle(screwColor, r, Offset(w - 10.dp.toPx(), 10.dp.toPx()))
                    drawCircle(screwColor, r, Offset(10.dp.toPx(), h - 10.dp.toPx()))
                    drawCircle(screwColor, r, Offset(w - 10.dp.toPx(), h - 10.dp.toPx()))
                }

                // 7. Cosmic Aurora & Waves Subtle Luminous Wave Accent
                if (palette.style == AppThemeStyle.COSMIC_AURORA) {
                    val auroraCyan = Color(0xFF00F5D4).copy(alpha = if (isPlaying) 0.28f else 0.14f)
                    val auroraPurple = Color(0xFFA78BFA).copy(alpha = if (isPlaying) 0.24f else 0.12f)
                    val wavePath = Path().apply {
                        moveTo(0f, h * 0.85f)
                        cubicTo(
                            w * 0.28f, h * 0.72f,
                            w * 0.68f, h * 0.96f,
                            w, h * 0.82f
                        )
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(
                        path = wavePath,
                        brush = Brush.horizontalGradient(
                            listOf(auroraCyan, auroraPurple, Color.Transparent)
                        )
                    )
                }
            }
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(contentPadding),
        content = content
    )
}

/**
 * Universal Theme-Styled Action Button for TO_OT Player
 */
@Composable
fun ThemeStyledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    brush: Brush? = null,
    border: BorderStroke? = null,
    contentPadding: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalAppThemePalette.current
    val effectiveShape = shape ?: palette.buttonShape
    val effectiveBrush = brush ?: palette.buttonBrush
    val effectiveBorder = border ?: palette.buttonBorder

    Box(
        modifier = modifier
            .then(
                if (palette.hasHardOffsetShadow) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = Color.Black,
                            topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                            size = size,
                            cornerRadius = CornerRadius(0f, 0f)
                        )
                    }
                } else if (palette.cardElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = 4.dp,
                        shape = effectiveShape,
                        spotColor = palette.glowAccent
                    )
                } else Modifier
            )
            .clip(effectiveShape)
            .then(
                if (effectiveBorder != null) Modifier.border(effectiveBorder, effectiveShape) else Modifier
            )
            .background(if (enabled) effectiveBrush else Brush.linearGradient(listOf(palette.colorSurfaceSecondary, palette.colorSurfaceSecondary)))
            .clickable(enabled = enabled) { onClick() }
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Theme-Styled Dynamic Status Badge
 */
@Composable
fun ThemeStyledBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isActive: Boolean = true
) {
    val palette = LocalAppThemePalette.current

    val badgeBg = when (palette.style) {
        AppThemeStyle.MODERN_BRUTALISM -> Color.Black
        AppThemeStyle.MINIMALIST_MONOCHROME -> Color.Black
        AppThemeStyle.RETRO_TERMINAL -> Color(0xFF33FF66).copy(alpha = 0.20f)
        AppThemeStyle.CYBERPUNK_NEON -> Color(0xFF00F0FF).copy(alpha = 0.20f)
        AppThemeStyle.COSMIC_AURORA -> Color(0xFF00F5D4).copy(alpha = 0.25f)
        AppThemeStyle.EARTHY_BIOPHILIC -> Color(0xFFC86D51).copy(alpha = 0.25f)
        AppThemeStyle.DARK_LUXURY -> palette.primary.copy(alpha = 0.22f)
        AppThemeStyle.CRIMSON_CARBON -> Color(0xFFFF1E40).copy(alpha = 0.22f)
        AppThemeStyle.SCARLET_STUDIO -> Color(0xFF1A0206).copy(alpha = 0.25f)
        AppThemeStyle.RETRO_ANALOG -> Color(0xFFD4AF37).copy(alpha = 0.25f)
        AppThemeStyle.CLAYMORPHIC_PASTEL -> Color(0xFFD4AF37).copy(alpha = 0.25f)
        AppThemeStyle.NEUMORPHIC_GLASS -> Color(0xFF06B6D4).copy(alpha = 0.20f)
        AppThemeStyle.HOLOGRAPHIC_JELLY -> Color(0xFF00F5D4).copy(alpha = 0.25f)
    }

    val badgeTextColor = when (palette.style) {
        AppThemeStyle.MODERN_BRUTALISM -> Color(0xFFFFE500)
        AppThemeStyle.MINIMALIST_MONOCHROME -> Color.White
        AppThemeStyle.RETRO_TERMINAL -> Color(0xFF33FF66)
        AppThemeStyle.CYBERPUNK_NEON -> Color(0xFF00F0FF)
        AppThemeStyle.COSMIC_AURORA -> Color(0xFF00F5D4)
        AppThemeStyle.EARTHY_BIOPHILIC -> Color(0xFFE89274)
        AppThemeStyle.DARK_LUXURY -> palette.primary
        AppThemeStyle.CRIMSON_CARBON -> Color(0xFFFF3355)
        AppThemeStyle.SCARLET_STUDIO -> Color(0xFFFFFFFF)
        AppThemeStyle.RETRO_ANALOG -> Color(0xFFF3E5AB)
        AppThemeStyle.CLAYMORPHIC_PASTEL -> Color(0xFF9D84B7)
        AppThemeStyle.NEUMORPHIC_GLASS -> Color(0xFF06B6D4)
        AppThemeStyle.HOLOGRAPHIC_JELLY -> Color(0xFF00F5D4)
    }

    val badgeBorder = when (palette.style) {
        AppThemeStyle.MODERN_BRUTALISM -> BorderStroke(1.5.dp, Color.Black)
        AppThemeStyle.MINIMALIST_MONOCHROME -> BorderStroke(1.dp, Color.White)
        AppThemeStyle.RETRO_TERMINAL -> BorderStroke(1.dp, Color(0xFF33FF66))
        AppThemeStyle.CYBERPUNK_NEON -> BorderStroke(1.dp, Color(0xFF00F0FF))
        AppThemeStyle.COSMIC_AURORA -> BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.8f))
        AppThemeStyle.EARTHY_BIOPHILIC -> BorderStroke(0.8.dp, Color(0xFFC86D51).copy(alpha = 0.8f))
        AppThemeStyle.DARK_LUXURY -> BorderStroke(0.8.dp, palette.primary.copy(alpha = 0.8f))
        AppThemeStyle.CRIMSON_CARBON -> BorderStroke(0.9.dp, Color(0xFFFF1E40).copy(alpha = 0.8f))
        AppThemeStyle.SCARLET_STUDIO -> BorderStroke(0.9.dp, Color(0xFF1A0206))
        AppThemeStyle.RETRO_ANALOG -> BorderStroke(0.8.dp, Color(0xFFD4AF37).copy(alpha = 0.8f))
        AppThemeStyle.CLAYMORPHIC_PASTEL -> BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.7f))
        AppThemeStyle.NEUMORPHIC_GLASS -> BorderStroke(0.8.dp, Color(0xFF06B6D4).copy(alpha = 0.6f))
        AppThemeStyle.HOLOGRAPHIC_JELLY -> BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.8f))
    }

    val badgeShape = if (palette.style == AppThemeStyle.MODERN_BRUTALISM) RoundedCornerShape(0.dp) else RoundedCornerShape(50)

    Surface(
        shape = badgeShape,
        color = badgeBg,
        border = badgeBorder,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeTextColor,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = if (palette.hasTerminalAscii) "[*] $text" else text,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = badgeTextColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = if (palette.hasTerminalAscii) FontFamily.Monospace else FontFamily.Default,
                    fontSize = 9.5.sp,
                    letterSpacing = 0.4.sp
                )
            )
        }
    }
}
