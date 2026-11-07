package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeStyle
import com.example.ui.theme.LocalAppThemePalette
import com.example.util.AlbumArtHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class SessionCategoryType(val defaultLabel: String, val badgeLabel: String) {
    PODCAST("Podcast & Voice", "PODCAST"),
    WORKOUT("Workout", "WORKOUT\nMODE"),
    AUDIOBOOK("Audiobooks", "AUDIO\nBOOK"),
    NIGHT_CHILL("Night Chill", "NIGHT\nCHILL"),
    FOCUS("Focus & Study", "FOCUS"),
    CUSTOM("Session", "TRACKS");

    companion object {
        fun fromName(name: String): SessionCategoryType {
            val lower = name.lowercase()
            return when {
                lower.contains("podcast") || lower.contains("voice") || lower.contains("پادکست") -> PODCAST
                lower.contains("workout") || lower.contains("sport") || lower.contains("gym") || lower.contains("ورزش") -> WORKOUT
                lower.contains("book") || lower.contains("story") || lower.contains("کتاب") -> AUDIOBOOK
                lower.contains("night") || lower.contains("chill") || lower.contains("sleep") || lower.contains("شب") || lower.contains("آرامش") -> NIGHT_CHILL
                lower.contains("focus") || lower.contains("study") || lower.contains("تمرکز") -> FOCUS
                else -> CUSTOM
            }
        }
    }
}

/**
 * Universal Theme-Adaptive Session Artwork
 * Implements 11 Distinct Visual Paradigms
 */
@Composable
fun SessionArtwork(
    sessionName: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier.size(76.dp),
    trackUriString: String? = null,
    cornerRadius: Dp = 14.dp,
    categoryType: SessionCategoryType = SessionCategoryType.fromName(sessionName)
) {
    val context = LocalContext.current
    val themePalette = LocalAppThemePalette.current

    var albumArtBitmap by remember(trackUriString) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(trackUriString) {
        if (!trackUriString.isNullOrBlank()) {
            val bytes = AlbumArtHelper.getAlbumArtBytes(context, trackUriString)
            if (bytes != null && bytes.isNotEmpty()) {
                withContext(Dispatchers.Default) {
                    try {
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        albumArtBitmap = bmp
                    } catch (e: Exception) {
                        albumArtBitmap = null
                    }
                }
            } else {
                albumArtBitmap = null
            }
        } else {
            albumArtBitmap = null
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "artwork_motion")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "art_breathing_scale"
    )
    val glowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "art_glow_phase"
    )

    val shape = if (themePalette.style == AppThemeStyle.MODERN_BRUTALISM) RoundedCornerShape(0.dp) else RoundedCornerShape(cornerRadius)

    // Artwork Background styling per theme
    val artBgBrush = when (themePalette.style) {
        AppThemeStyle.MINIMALIST_MONOCHROME -> Brush.linearGradient(listOf(Color.Black, Color.Black))
        AppThemeStyle.MODERN_BRUTALISM -> Brush.linearGradient(listOf(Color(0xFFFFE500), Color(0xFFFFE500)))
        AppThemeStyle.RETRO_TERMINAL -> Brush.verticalGradient(listOf(Color(0xFF071C0C), Color(0xFF030D06)))
        AppThemeStyle.EARTHY_BIOPHILIC -> Brush.verticalGradient(listOf(Color(0xFF51624D), Color(0xFF3F4E3C)))
        AppThemeStyle.CLAYMORPHIC_PASTEL -> Brush.verticalGradient(listOf(Color(0xFFDCD3ED), Color(0xFFCAC0DC)))
        AppThemeStyle.RETRO_ANALOG -> Brush.verticalGradient(listOf(Color(0xFF3A2617), Color(0xFF22160C)))
        AppThemeStyle.DARK_LUXURY -> Brush.verticalGradient(listOf(Color(0xFF1E1E24), Color(0xFF121215)))
        AppThemeStyle.CRIMSON_CARBON -> Brush.linearGradient(listOf(Color(0xFF220A10), Color(0xFF100508)))
        AppThemeStyle.SCARLET_STUDIO -> Brush.linearGradient(listOf(Color(0xFF8B0E27), Color(0xFF5E0719)))
        AppThemeStyle.COSMIC_AURORA -> Brush.linearGradient(listOf(Color(0xFF15193B), Color(0xFF0C0E24)))
        AppThemeStyle.CYBERPUNK_NEON -> Brush.linearGradient(listOf(Color(0xFF0D152B), Color(0xFF050914)))
        AppThemeStyle.HOLOGRAPHIC_JELLY -> Brush.linearGradient(listOf(Color(0xFF26194A), Color(0xFF140D2A)))
        AppThemeStyle.NEUMORPHIC_GLASS -> Brush.verticalGradient(listOf(Color(0xFFE2EAF2), Color(0xFFD6E0EC)))
    }

    val artBorder = when (themePalette.style) {
        AppThemeStyle.MODERN_BRUTALISM -> BorderStroke(2.dp, Color.Black)
        AppThemeStyle.MINIMALIST_MONOCHROME -> BorderStroke(1.2.dp, Color.White)
        AppThemeStyle.RETRO_TERMINAL -> BorderStroke(1.2.dp, Color(0xFF33FF66))
        AppThemeStyle.CYBERPUNK_NEON -> BorderStroke(1.2.dp, Color(0xFF00F0FF))
        AppThemeStyle.DARK_LUXURY -> BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0xFFF3E5AB), Color(0xFFD4AF37), Color(0xFF8A6520))))
        AppThemeStyle.CRIMSON_CARBON -> BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFFFF1E40), Color.White, Color(0xFFFF1E40))))
        AppThemeStyle.SCARLET_STUDIO -> BorderStroke(1.2.dp, Color(0xFF1A0206))
        AppThemeStyle.COSMIC_AURORA -> BorderStroke(1.2.dp, Color(0xFF00F5D4).copy(alpha = 0.6f))
        AppThemeStyle.HOLOGRAPHIC_JELLY -> BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFF00F5D4), Color(0xFFE056FD))))
        AppThemeStyle.CLAYMORPHIC_PASTEL -> BorderStroke(1.2.dp, Color(0xFFD4AF37).copy(alpha = 0.6f))
        AppThemeStyle.RETRO_ANALOG -> BorderStroke(1.2.dp, Color(0xFF8A653E))
        AppThemeStyle.EARTHY_BIOPHILIC -> BorderStroke(1.dp, Color(0xFF7E9F59).copy(alpha = 0.5f))
        AppThemeStyle.NEUMORPHIC_GLASS -> BorderStroke(1.dp, Color.White)
    }

    val iconColor = when (themePalette.style) {
        AppThemeStyle.MODERN_BRUTALISM -> Color.Black
        AppThemeStyle.MINIMALIST_MONOCHROME -> Color.White
        AppThemeStyle.RETRO_TERMINAL -> Color(0xFF33FF66)
        AppThemeStyle.CYBERPUNK_NEON -> Color(0xFF00F0FF)
        AppThemeStyle.DARK_LUXURY -> Color(0xFFD4AF37)
        AppThemeStyle.CRIMSON_CARBON -> Color(0xFFFF1E40)
        AppThemeStyle.SCARLET_STUDIO -> Color(0xFFFFFFFF)
        AppThemeStyle.RETRO_ANALOG -> Color(0xFFE8C87A)
        AppThemeStyle.CLAYMORPHIC_PASTEL -> Color(0xFFD4AF37)
        AppThemeStyle.EARTHY_BIOPHILIC -> Color(0xFFE89274)
        AppThemeStyle.HOLOGRAPHIC_JELLY -> Color(0xFF00F5D4)
        AppThemeStyle.COSMIC_AURORA -> Color(0xFF00F5D4)
        AppThemeStyle.NEUMORPHIC_GLASS -> Color(0xFF06B6D4)
    }

    val categoryIcon: ImageVector = when (categoryType) {
        SessionCategoryType.PODCAST -> Icons.Default.Mic
        SessionCategoryType.WORKOUT -> Icons.Default.FitnessCenter
        SessionCategoryType.AUDIOBOOK -> Icons.Default.AutoStories
        SessionCategoryType.NIGHT_CHILL -> Icons.Default.NightsStay
        SessionCategoryType.FOCUS -> Icons.Default.SelfImprovement
        SessionCategoryType.CUSTOM -> Icons.Default.Audiotrack
    }

    Box(
        modifier = modifier
            .clip(shape)
            .border(artBorder, shape)
            .background(artBgBrush)
            .graphicsLayer {
                if (isPlaying) {
                    scaleX = breathingScale
                    scaleY = breathingScale
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (albumArtBitmap != null) {
            Image(
                bitmap = albumArtBitmap!!.asImageBitmap(),
                contentDescription = sessionName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Stylized Icon Artwork
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Background artistic accents per style
                if (themePalette.hasIridescentRim) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color(0xFFE056FD).copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                    }
                }

                Icon(
                    imageVector = categoryIcon,
                    contentDescription = sessionName,
                    tint = iconColor,
                    modifier = Modifier.size(34.dp)
                )

                // Bottom badge overlay for category
                if (!themePalette.hasTerminalAscii && themePalette.style != AppThemeStyle.MINIMALIST_MONOCHROME) {
                    Text(
                        text = categoryType.badgeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = iconColor.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.sp,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}
