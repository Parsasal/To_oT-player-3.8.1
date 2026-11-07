package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioLaneState
import com.example.ui.theme.LocalAppThemePalette
import com.example.ui.theme.RoseMute
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThemeStyledBadge

@Composable
fun MasterControlPanel(
    selectedLane: AudioLaneState?,
    masterVolume: Float,
    isMasterMuted: Boolean,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipBackward: () -> Unit,
    onSkipForward: () -> Unit,
    onMasterVolumeChange: (Float) -> Unit,
    onToggleMasterMute: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onPlayAll: () -> Unit,
    onPauseAll: () -> Unit,
    isPersianRtl: Boolean = false,
    modifier: Modifier = Modifier
) {
    val themePalette = LocalAppThemePalette.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = themePalette.radiusPanel, topEnd = themePalette.radiusPanel))
            .background(
                Brush.verticalGradient(
                    listOf(
                        themePalette.bgCenter.copy(alpha = 0.98f),
                        themePalette.bgStart.copy(alpha = 0.99f)
                    )
                )
            ),
        color = Color.Transparent,
        border = BorderStroke(themePalette.borderStrokeWidth, themePalette.cardBorderBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Selected Lane Info Row & Play All / Pause All
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = themePalette.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedLane != null) "${selectedLane.name} • ${selectedLane.fileName ?: (if (isPersianRtl) "آماده" else "Ready")}" else (if (isPersianRtl) "مستر" else "Master"),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Play All / Pause All Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = themePalette.controlShape,
                        color = themePalette.primary.copy(alpha = 0.2f),
                        border = BorderStroke(0.6.dp, themePalette.primary.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .clickable { onPlayAll() }
                            .testTag("play_all_button")
                    ) {
                        Text(
                            text = if (isPersianRtl) "پخش همه" else "ALL PLAY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = themePalette.primary,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        shape = themePalette.controlShape,
                        color = themePalette.glassSurfaceElevated,
                        border = BorderStroke(0.5.dp, themePalette.glassBorder),
                        modifier = Modifier
                            .clickable { onPauseAll() }
                            .testTag("pause_all_button")
                    ) {
                        Text(
                            text = if (isPersianRtl) "مکث همه" else "ALL PAUSE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Master Seek & Volume in a compact scroll layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seek Bar Slider
                Slider(
                    value = if (selectedLane != null && selectedLane.durationMs > 0) {
                        (selectedLane.currentPositionMs.toFloat() / selectedLane.durationMs).coerceIn(0f, 1f)
                    } else 0f,
                    onValueChange = { ratio ->
                        if (selectedLane != null && selectedLane.durationMs > 0) {
                            onSeek((ratio * selectedLane.durationMs).toLong())
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                        .padding(horizontal = 4.dp)
                        .testTag("master_seek_bar"),
                    colors = SliderDefaults.colors(
                        thumbColor = themePalette.primary,
                        activeTrackColor = themePalette.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                    )
                )

                // Master Volume
                IconButton(
                    onClick = onToggleMasterMute,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("master_mute_button")
                ) {
                    Icon(
                        imageVector = if (isMasterMuted) Icons.Default.VolumeMute else Icons.Default.VolumeDown,
                        contentDescription = if (isPersianRtl) "بی‌صدا کردن کلی" else "Master Mute",
                        tint = if (isMasterMuted) RoseMute else TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Slider(
                    value = masterVolume,
                    onValueChange = onMasterVolumeChange,
                    modifier = Modifier
                        .width(90.dp)
                        .height(26.dp)
                        .padding(horizontal = 2.dp)
                        .testTag("master_volume_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = themePalette.secondary,
                        activeTrackColor = themePalette.secondary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Primary Transport & Speed controls in one compact row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Transport buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSkipBackward,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("skip_backward_10s")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = if (isPersianRtl) "۱۰ ثانیه عقب" else "Skip -10s",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        shape = themePalette.buttonShape,
                        color = if (selectedLane?.isPlaying == true) themePalette.primary else themePalette.secondary,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onPlayPause() }
                            .testTag("master_play_pause_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (selectedLane?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Master Play/Pause",
                                tint = themePalette.buttonTextColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onStop,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("master_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = if (isPersianRtl) "توقف کامل" else "Master Stop",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onSkipForward,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("skip_forward_10s")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = if (isPersianRtl) "۱۰ ثانیه جلو" else "Skip +10s",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Speed Presets
                val speedPresets = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    speedPresets.forEach { speed ->
                        val isSelected = playbackSpeed == speed
                        Surface(
                            shape = themePalette.controlShape,
                            color = if (isSelected) themePalette.primary else themePalette.glassSurfaceElevated,
                            border = BorderStroke(0.5.dp, if (isSelected) themePalette.primary else themePalette.glassBorder),
                            modifier = Modifier
                                .clickable { onSpeedChange(speed) }
                                .testTag("speed_${speed}x")
                        ) {
                            Text(
                                text = "${speed}x",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) themePalette.buttonTextColor else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
