package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioLaneState
import com.example.ui.theme.LocalAppThemePalette
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SpectrumVisualizer(
    frequencies: FloatArray,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    activeLane: AudioLaneState? = null,
    isPersianRtl: Boolean = false,
    onNextTrack: () -> Unit = {},
    onPrevTrack: () -> Unit = {},
    onTogglePlayPause: () -> Unit = {},
    onOpenEqualizer: () -> Unit = {}
) {
    val themePalette = LocalAppThemePalette.current

    // Current track name calculation
    val currentTrackTitle = activeLane?.fileName
        ?: activeLane?.playlist?.getOrNull(activeLane.currentTrackIndex)?.title
        ?: if (activeLane != null && activeLane.playlist.isNotEmpty()) activeLane.playlist.first().title else null

    val totalTracks = activeLane?.playlist?.size ?: 0
    val currentTrackIndex = if (totalTracks > 0) (activeLane?.currentTrackIndex ?: 0) + 1 else 0
    val hasMultipleTracks = totalTracks > 1

    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    // Pulsing alpha for active playing badge
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = themePalette.cardBackground.copy(alpha = 0.95f),
        border = BorderStroke(1.2.dp, if (isPlaying) themePalette.primary.copy(alpha = 0.65f) else themePalette.cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { accumulatedDrag = 0f },
                    onDragEnd = {
                        val dragThreshold = 45f
                        if (accumulatedDrag > dragThreshold) {
                            // Swiped Right
                            if (isPersianRtl) onNextTrack() else onPrevTrack()
                        } else if (accumulatedDrag < -dragThreshold) {
                            // Swiped Left
                            if (isPersianRtl) onPrevTrack() else onNextTrack()
                        }
                        accumulatedDrag = 0f
                    },
                    onDragCancel = { accumulatedDrag = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDrag += dragAmount
                    }
                )
            }
            .testTag("interactive_spectrum_visualizer")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
        ) {
            // Background Canvas: Frequency Spectrum Equalizer Bars
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 6.dp, top = 24.dp)
            ) {
                val width = size.width
                val height = size.height
                val numBars = frequencies.size
                if (numBars == 0) return@Canvas

                val barSpacing = 4.dp.toPx()
                val totalSpacing = barSpacing * (numBars - 1)
                val barWidth = (width - totalSpacing) / numBars

                for (i in 0 until numBars) {
                    val amp = frequencies[i].coerceIn(0.04f, 1.0f)
                    val barHeight = height * amp
                    val x = i * (barWidth + barSpacing)
                    val y = height - barHeight

                    val gradient = Brush.verticalGradient(
                        colors = if (isPlaying) {
                            listOf(
                                themePalette.primary.copy(alpha = 0.9f),
                                themePalette.secondary.copy(alpha = 0.75f),
                                themePalette.primary.copy(alpha = 0.35f)
                            )
                        } else {
                            listOf(
                                themePalette.primary.copy(alpha = 0.25f),
                                themePalette.secondary.copy(alpha = 0.18f),
                                themePalette.cardBackground.copy(alpha = 0.3f)
                            )
                        },
                        startY = y,
                        endY = height
                    )

                    // Draw rounded bar
                    drawRoundRect(
                        brush = gradient,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )

                    // Draw peak cap
                    if (isPlaying && amp > 0.15f) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = (barWidth / 2f).coerceAtMost(2.5.dp.toPx()),
                            center = Offset(x + barWidth / 2f, y)
                        )
                    }
                }

                // Draw Real-Time Smooth Waveform Oscilloscope Curve across the spectrum
                val wavePath = androidx.compose.ui.graphics.Path()
                val midY = height * 0.55f
                val points = 32
                for (p in 0..points) {
                    val progress = p.toFloat() / points
                    val wx = progress * width
                    val freqIdx = (progress * (numBars - 1)).toInt().coerceIn(0, numBars - 1)
                    val freqAmp = frequencies[freqIdx].coerceIn(0.04f, 1.0f)
                    val oscillation = kotlin.math.sin(progress * 14f + (if (isPlaying) pulseAlpha * 6f else 0f))
                    val wy = midY - (oscillation * height * 0.35f * freqAmp)

                    if (p == 0) wavePath.moveTo(wx, wy) else wavePath.lineTo(wx, wy)
                }

                drawPath(
                    path = wavePath,
                    brush = Brush.horizontalGradient(
                        listOf(
                            themePalette.primary,
                            themePalette.secondary,
                            themePalette.primary.copy(alpha = 0.7f)
                        )
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Foreground Glass Gradient Overlay for Crisp Text Legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                themePalette.bgStart.copy(alpha = 0.82f),
                                themePalette.bgCenter.copy(alpha = 0.65f),
                                themePalette.bgStart.copy(alpha = 0.88f)
                            )
                        )
                    )
            )

            // Content Overlay: Track Title, Lane Badge, Swipe Controls & Navigation
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Status badge, Lane name & Playlist Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Live Equalizer / Playing Indicator Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isPlaying) themePalette.primary.copy(alpha = 0.22f) else themePalette.glassSurfaceElevated,
                            border = BorderStroke(0.6.dp, if (isPlaying) themePalette.primary.copy(alpha = pulseAlpha) else themePalette.glassBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.Audiotrack,
                                    contentDescription = null,
                                    tint = if (isPlaying) themePalette.primary else TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isPlaying) {
                                        if (isPersianRtl) "در حال پخش" else "PLAYING"
                                    } else {
                                        if (isPersianRtl) "آماده پخش" else "STANDBY"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isPlaying) themePalette.primary else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp
                                    )
                                )
                            }
                        }

                        // Lane Name Badge
                        if (activeLane != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = themePalette.secondary.copy(alpha = 0.2f),
                                border = BorderStroke(0.6.dp, themePalette.secondary.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = activeLane.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 9.5.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                                )
                            }
                        }
                    }

                    // Playlist Track Index & Swipe Hint
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (hasMultipleTracks) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = themePalette.primary.copy(alpha = 0.18f),
                                border = BorderStroke(0.6.dp, themePalette.primary.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = null,
                                        tint = themePalette.primary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = if (isPersianRtl) "آهنگ $currentTrackIndex از $totalTracks" else "$currentTrackIndex / $totalTracks",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = themePalette.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Middle & Bottom Row: Track Title & Quick Next/Previous Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Previous Track Button (Left)
                    Surface(
                        shape = CircleShape,
                        color = themePalette.primary.copy(alpha = 0.18f),
                        border = BorderStroke(0.8.dp, themePalette.primary.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onPrevTrack() }
                            .testTag("visualizer_prev_track_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPersianRtl) Icons.Default.SkipNext else Icons.Default.SkipPrevious,
                                contentDescription = if (isPersianRtl) "آهنگ بعدی" else "Previous Track",
                                tint = if (currentTrackTitle != null) themePalette.primary else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Center: Track Title & Swipe Instruction
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentTrackTitle ?: if (isPersianRtl) "هیچ آهنگی بارگذاری نشده است" else "No track loaded",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (currentTrackTitle != null) TextPrimary else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Swipe hint text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = themePalette.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isPersianRtl) {
                                    if (hasMultipleTracks) "برای تعویض آهنگ به چپ یا راست بکشید" else "برای کنترل لاین به چپ یا راست بکشید"
                                } else {
                                    if (hasMultipleTracks) "Swipe ◄ ► to change track" else "Swipe ◄ ► for track switch"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary.copy(alpha = 0.85f),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Next Track Button (Right)
                    Surface(
                        shape = CircleShape,
                        color = themePalette.primary.copy(alpha = 0.18f),
                        border = BorderStroke(0.8.dp, themePalette.primary.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onNextTrack() }
                            .testTag("visualizer_next_track_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPersianRtl) Icons.Default.SkipPrevious else Icons.Default.SkipNext,
                                contentDescription = if (isPersianRtl) "آهنگ قبلی" else "Next Track",
                                tint = if (currentTrackTitle != null) themePalette.primary else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
