package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.AmberSolo
import com.example.ui.theme.EmeraldPlay
import com.example.ui.theme.LocalAppThemePalette
import com.example.ui.theme.RoseMute
import com.example.ui.theme.ThemeStyledCard
import java.util.Locale

@Composable
fun PersistentMiniPlayer(
    activeLane: AudioLaneState?,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onOpenPlaylist: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onPlayAll: () -> Unit = {},
    onPauseAll: () -> Unit = {},
    onStopAll: () -> Unit = {},
    currentSpeed: Float = 1.0f,
    isPersianRtl: Boolean = false,
    modifier: Modifier = Modifier
) {
    val themePalette = LocalAppThemePalette.current
    var showMoreMenu by remember { mutableStateOf(false) }

    if (activeLane == null) return

    val progressFraction = if (activeLane.durationMs > 0) {
        (activeLane.currentPositionMs.toFloat() / activeLane.durationMs).coerceIn(0f, 1f)
    } else 0f

    val displayTitle = when {
        !activeLane.fileName.isNullOrBlank() -> activeLane.fileName
        activeLane.playlist.isNotEmpty() -> activeLane.playlist.getOrNull(activeLane.currentTrackIndex)?.title ?: "Playlist Item"
        else -> if (isPersianRtl) "آماده برای پخش" else "Ready to play"
    }

    ThemeStyledCard(
        isPlaying = activeLane.isPlaying,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("persistent_mini_player"),
        contentPadding = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Artwork Thumbnail with Real Album Art & Micro-Motion
                val activeTrackUri = activeLane.uriString
                    ?: activeLane.playlist.getOrNull(activeLane.currentTrackIndex)?.uriString
                SessionArtwork(
                    sessionName = activeLane.name,
                    isPlaying = activeLane.isPlaying,
                    trackUriString = activeTrackUri,
                    modifier = Modifier.size(46.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Titles & Subtitles
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    Text(
                        text = activeLane.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = themePalette.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = displayTitle ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = themePalette.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Transport Actions: Prev, Circular Play/Pause, Next, More
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("mini_player_prev")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = themePalette.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Prominent Theme-Styled Play/Pause Button
                    Surface(
                        shape = themePalette.buttonShape,
                        color = themePalette.primary,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { onPlayPause() }
                            .testTag("mini_player_play_pause")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (activeLane.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = themePalette.buttonTextColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("mini_player_next")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = themePalette.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // More Menu
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("mini_player_more")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = themePalette.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            modifier = Modifier.background(themePalette.cardBackground)
                        ) {
                            // Master Lane Actions: Play All, Pause All, Stop All
                            DropdownMenuItem(
                                text = { Text(if (isPersianRtl) "پخش همه لاین‌ها" else "Play All", color = themePalette.textPrimary) },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EmeraldPlay) },
                                onClick = {
                                    showMoreMenu = false
                                    onPlayAll()
                                },
                                modifier = Modifier.testTag("mini_player_menu_play_all")
                            )
                            DropdownMenuItem(
                                text = { Text(if (isPersianRtl) "توقف موقت همه (Pause)" else "Pause All", color = themePalette.textPrimary) },
                                leadingIcon = { Icon(Icons.Default.Pause, contentDescription = null, tint = AmberSolo) },
                                onClick = {
                                    showMoreMenu = false
                                    onPauseAll()
                                },
                                modifier = Modifier.testTag("mini_player_menu_pause_all")
                            )
                            DropdownMenuItem(
                                text = { Text(if (isPersianRtl) "توقف کامل همه (Stop)" else "Stop All", color = themePalette.textPrimary) },
                                leadingIcon = { Icon(Icons.Default.Stop, contentDescription = null, tint = RoseMute) },
                                onClick = {
                                    showMoreMenu = false
                                    onStopAll()
                                },
                                modifier = Modifier.testTag("mini_player_menu_stop_all")
                            )

                            HorizontalDivider(
                                color = themePalette.cardBorder.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            DropdownMenuItem(
                                text = { Text(if (isPersianRtl) "لیست پخش نشست" else "Open Session Queue", color = themePalette.textPrimary) },
                                leadingIcon = { Icon(Icons.Default.QueueMusic, contentDescription = null, tint = themePalette.primary) },
                                onClick = {
                                    showMoreMenu = false
                                    onOpenPlaylist()
                                }
                            )

                            HorizontalDivider(
                                color = themePalette.cardBorder.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            val speeds = listOf(0.8f, 1.0f, 1.25f, 1.5f, 2.0f)
                            speeds.forEach { speed ->
                                DropdownMenuItem(
                                    text = { Text("${if (isPersianRtl) "سرعت" else "Speed"} ${speed}x ${if (currentSpeed == speed) "✓" else ""}", color = if (currentSpeed == speed) themePalette.primary else themePalette.textPrimary) },
                                    leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, tint = themePalette.secondary) },
                                    onClick = {
                                        showMoreMenu = false
                                        onSpeedChange(speed)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Thin Seek Bar + Timestamps Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMiniPlayerTime(activeLane.currentPositionMs),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = themePalette.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(end = 6.dp)
                )

                // Interactive Thin Seek Slider
                Slider(
                    value = progressFraction,
                    onValueChange = { ratio ->
                        if (activeLane.durationMs > 0) {
                            onSeek((ratio * activeLane.durationMs).toLong())
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .testTag("mini_player_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = themePalette.primary,
                        activeTrackColor = themePalette.primary,
                        inactiveTrackColor = themePalette.cardBorder
                    )
                )

                Text(
                    text = formatMiniPlayerTime(activeLane.durationMs),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = themePalette.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

private fun formatMiniPlayerTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, remainingMinutesMini(minutes), seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

private fun remainingMinutesMini(minutes: Long): Long = minutes % 60
