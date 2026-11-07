package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Vintage Hi-Fi Backlit Analog VU Meter with moving needle
 * Matches the warm amber analog aesthetic in the Vintage Analog Hi-Fi theme design
 */
@Composable
fun AnalogVuMeter(
    isPlaying: Boolean,
    modifier: Modifier = Modifier.size(width = 82.dp, height = 52.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vu_needle_anim")
    val needleAngle by infiniteTransition.animateFloat(
        initialValue = if (isPlaying) -35f else -45f,
        targetValue = if (isPlaying) 22f else -45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 650 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vu_needle_angle"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.2.dp, Color(0xFF5A3E22), RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE89A2A), // Amber backlight top
                        Color(0xFFD48218),
                        Color(0xFFB36208)
                    )
                )
            )
            .padding(2.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Meter Scale Arc
            val arcCenter = Offset(w / 2f, h * 1.05f)
            val arcRadius = w * 0.72f

            // Background meter bezel border
            drawArc(
                color = Color(0xFF3B1E05).copy(alpha = 0.5f),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(arcCenter.x - arcRadius, arcCenter.y - arcRadius),
                size = Size(arcRadius * 2f, arcRadius * 2f),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Scale tick marks (-20, -10, -7, -5, -3, 0, +3)
            val ticks = listOf(
                -45f to Color(0xFF2A1504),
                -30f to Color(0xFF2A1504),
                -15f to Color(0xFF2A1504),
                0f to Color(0xFF2A1504),
                15f to Color(0xFFD32F2F), // Red zone
                30f to Color(0xFFD32F2F),
                45f to Color(0xFFD32F2F)
            )

            for ((deg, tickColor) in ticks) {
                val rad = Math.toRadians((deg - 90).toDouble())
                val innerR = arcRadius - 4.dp.toPx()
                val outerR = arcRadius + 2.dp.toPx()
                val x1 = arcCenter.x + (innerR * cos(rad)).toFloat()
                val y1 = arcCenter.y + (innerR * sin(rad)).toFloat()
                val x2 = arcCenter.x + (outerR * cos(rad)).toFloat()
                val y2 = arcCenter.y + (outerR * sin(rad)).toFloat()
                drawLine(
                    color = tickColor,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Moving Black Needle
            val needleRad = Math.toRadians((needleAngle - 90).toDouble())
            val needleLen = arcRadius * 0.95f
            val nx = arcCenter.x + (needleLen * cos(needleRad)).toFloat()
            val ny = arcCenter.y + (needleLen * sin(needleRad)).toFloat()
            drawLine(
                color = Color(0xFF1A0C02),
                start = arcCenter,
                end = Offset(nx, ny),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Bottom Pivot Cap
            drawCircle(
                color = Color(0xFF221104),
                radius = 4.dp.toPx(),
                center = arcCenter
            )
        }

        // dB Label overlay
        Text(
            text = "dB",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF2E1605),
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        )
    }
}

/**
 * Vintage Brass Rotary Knob & Mix Switch Controls
 */
@Composable
fun AnalogBrassControls(
    onMixClick: () -> Unit,
    onLevelClick: () -> Unit,
    onLoopClick: () -> Unit = {},
    isLoopEnabled: Boolean = false,
    onPlaylistLoopClick: () -> Unit = {},
    isPlaylistLoopEnabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TRK LOOP Button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isLoopEnabled) "TRK:ON" else "TRK:LOOP",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isLoopEnabled) Color(0xFFFFF3B0) else Color(0xFFE8C87A),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .border(
                        if (isLoopEnabled) 1.8.dp else 1.dp,
                        if (isLoopEnabled) Color(0xFFFFF6D6) else Color(0xFFF3E5AB),
                        CircleShape
                    )
                    .background(
                        if (isLoopEnabled) {
                            Brush.radialGradient(
                                listOf(Color(0xFFFFF3B0), Color(0xFFE8C87A), Color(0xFFB8860B))
                            )
                        } else {
                            Brush.radialGradient(
                                listOf(Color(0xFFF5E5B8), Color(0xFFD4AF37), Color(0xFF8A6520))
                            )
                        }
                    )
                    .clickable { onLoopClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Loop,
                    contentDescription = "Track Loop",
                    tint = if (isLoopEnabled) Color(0xFF140D04) else Color(0xFF241708),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // PL LOOP Button (Repeat Lane Playlist)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isPlaylistLoopEnabled) "PL:ON" else "PL:LOOP",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isPlaylistLoopEnabled) Color(0xFFFFF3B0) else Color(0xFFE8C87A),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .border(
                        if (isPlaylistLoopEnabled) 1.8.dp else 1.dp,
                        if (isPlaylistLoopEnabled) Color(0xFFFFF6D6) else Color(0xFFF3E5AB),
                        CircleShape
                    )
                    .background(
                        if (isPlaylistLoopEnabled) {
                            Brush.radialGradient(
                                listOf(Color(0xFFFFF3B0), Color(0xFFE8C87A), Color(0xFFB8860B))
                            )
                        } else {
                            Brush.radialGradient(
                                listOf(Color(0xFFF5E5B8), Color(0xFFD4AF37), Color(0xFF8A6520))
                            )
                        }
                    )
                    .clickable { onPlaylistLoopClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Lane Playlist Loop",
                    tint = if (isPlaylistLoopEnabled) Color(0xFF140D04) else Color(0xFF241708),
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // MIX Button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "MIX",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFFE8C87A),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFFF3E5AB), CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFF5E5B8), Color(0xFFD4AF37), Color(0xFF8A6520))
                        )
                    )
                    .clickable { onMixClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = "Mix",
                    tint = Color(0xFF241708),
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // LEVEL Knob
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "LEVEL",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFFE8C87A),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFFF3E5AB), CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFF5E5B8), Color(0xFFD4AF37), Color(0xFF8A6520))
                        )
                    )
                    .clickable { onLevelClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Level",
                    tint = Color(0xFF241708),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
