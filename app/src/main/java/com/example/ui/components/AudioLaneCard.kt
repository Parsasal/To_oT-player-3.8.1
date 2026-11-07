package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioLaneState
import com.example.ui.theme.AmberSolo
import com.example.ui.theme.LocalAppThemePalette
import com.example.ui.theme.RoseMute
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun AudioLaneCard(
    lane: AudioLaneState,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit,
    onRenameClick: () -> Unit,
    onColorPickClick: () -> Unit = {},
    onDeleteClick: () -> Unit,
    onFileSelected: (Uri) -> Unit,
    onOpenPlaylist: () -> Unit = {},
    onOpenDeviceAudioExplorer: () -> Unit = {},
    onNextTrack: () -> Unit = {},
    onPrevTrack: () -> Unit = {},
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    isPersianRtl: Boolean = false,
    anySoloActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val themePalette = LocalAppThemePalette.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onFileSelected(uri)
        }
    }

    val isMutedBySolo = anySoloActive && !lane.isSoloed

    val laneAccentColor = remember(lane.colorHex) {
        lane.colorHex?.let {
            try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { null }
        }
    }

    val activeBorder = when {
        laneAccentColor != null && (lane.isPlaying || isSelected) -> BorderStroke(1.6.dp, laneAccentColor)
        laneAccentColor != null -> BorderStroke(1.2.dp, laneAccentColor.copy(alpha = 0.65f))
        lane.isPlaying -> BorderStroke(1.2.dp, themePalette.primary)
        isSelected -> BorderStroke(1.2.dp, themePalette.secondary)
        lane.isSoloed -> BorderStroke(1.2.dp, AmberSolo.copy(alpha = 0.8f))
        else -> BorderStroke(1.dp, themePalette.cardBorder)
    }

    val cardBg = when {
        lane.isPlaying -> themePalette.activeGlow.copy(alpha = 0.35f)
        isSelected -> themePalette.primary.copy(alpha = 0.18f)
        else -> themePalette.cardBackground
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("lane_card_${lane.laneId}"),
        border = activeBorder,
        backgroundColor = cardBg,
        contentPadding = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Reorder handles, Lane Title, Playing/Solo badges & Action controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Reorder Controls (Move Up / Move Down)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 3.dp)
                    ) {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = canMoveUp,
                            modifier = Modifier
                                .size(20.dp)
                                .testTag("move_up_lane_${lane.laneId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = if (isPersianRtl) "انتقال به خط قبل" else "Move Up",
                                tint = if (canMoveUp) themePalette.primary else TextMuted.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        IconButton(
                            onClick = onMoveDown,
                            enabled = canMoveDown,
                            modifier = Modifier
                                .size(20.dp)
                                .testTag("move_down_lane_${lane.laneId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isPersianRtl) "انتقال به خط بعد" else "Move Down",
                                tint = if (canMoveDown) themePalette.primary else TextMuted.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (lane.isSoloed) AmberSolo.copy(alpha = 0.2f) else if (lane.isPlaying) themePalette.primary.copy(alpha = 0.25f) else themePalette.secondary.copy(alpha = 0.15f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (lane.isSoloed) Icons.Default.Headphones else if (lane.isPlaying) Icons.Default.GraphicEq else Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = if (lane.isPlaying) themePalette.primary else themePalette.secondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = lane.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (lane.isPlaying) themePalette.primary else TextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (lane.isPlaying) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = themePalette.primary.copy(alpha = 0.2f),
                                    border = BorderStroke(0.5.dp, themePalette.primary.copy(alpha = 0.8f))
                                ) {
                                    Text(
                                        text = if (isPersianRtl) "در حال پخش" else "PLAYING",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = themePalette.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onRenameClick,
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("rename_lane_${lane.laneId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = if (isPersianRtl) "ویرایش نام" else "Rename Lane",
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(3.dp))

                            // Lane Color Accent Dot/Button
                            Surface(
                                shape = CircleShape,
                                color = laneAccentColor ?: Color.Transparent,
                                border = BorderStroke(1.2.dp, laneAccentColor ?: themePalette.glassBorder),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onColorPickClick() }
                                    .testTag("color_pick_lane_${lane.laneId}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (laneAccentColor == null) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = if (isPersianRtl) "رنگ اختصاصی لاین" else "Lane Color Accent",
                                            tint = themePalette.secondary,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        val displayTrackName = lane.fileName
                            ?: lane.playlist.getOrNull(lane.currentTrackIndex)?.title
                            ?: lane.playlist.firstOrNull()?.title

                        if (displayTrackName != null) {
                            Text(
                                text = displayTrackName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (lane.isPlaying) Color.White else TextSecondary,
                                    fontSize = 10.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Quick buttons: Playlist queue + Mute + Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Playlist Manager button
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (lane.playlist.isNotEmpty()) themePalette.primary.copy(alpha = 0.22f) else themePalette.glassSurfaceElevated,
                        border = BorderStroke(0.8.dp, if (lane.playlist.isNotEmpty()) themePalette.primary else themePalette.glassBorder),
                        modifier = Modifier
                            .clickable { onOpenPlaylist() }
                            .testTag("open_playlist_lane_${lane.laneId}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = if (isPersianRtl) "لیست پخش" else "Playlist",
                                tint = themePalette.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (lane.playlist.isNotEmpty()) "${lane.playlist.size}" else (if (isPersianRtl) "+ لیست" else "+ List"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = themePalette.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Mute Button
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (lane.isMuted) RoseMute else themePalette.glassSurface,
                        border = BorderStroke(0.5.dp, if (lane.isMuted) RoseMute else themePalette.glassBorder),
                        modifier = Modifier
                            .clickable { onToggleMute() }
                            .testTag("mute_lane_${lane.laneId}")
                    ) {
                        Text(
                            text = if (isPersianRtl) "M" else "MUTE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (lane.isMuted) Color.White else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Delete Button
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("delete_lane_${lane.laneId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = if (isPersianRtl) "حذف لاین" else "Delete Lane",
                            tint = TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // File & Playlist selection prompt if no audio loaded
            if (lane.fileName == null && lane.playlist.isEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Open Device Audio Explorer button (Highlighted & Prominent)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = themePalette.secondary.copy(alpha = 0.2f),
                        border = BorderStroke(0.8.dp, themePalette.secondary.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDeviceAudioExplorer() }
                            .testTag("pick_device_audio_${lane.laneId}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = themePalette.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPersianRtl) "فایل‌های صوتی و دانلودهای گوشی" else "Browse Phone Audio & Downloads",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Open Playlist Sheet button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = themePalette.primary.copy(alpha = 0.18f),
                            border = BorderStroke(0.8.dp, themePalette.primary.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenPlaylist() }
                                .testTag("pick_playlist_${lane.laneId}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = themePalette.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPersianRtl) "لیست پخش" else "Playlist",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = themePalette.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Pick Single File button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.25f),
                            border = BorderStroke(0.5.dp, themePalette.glassBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    filePickerLauncher.launch(
                                        arrayOf("audio/*", "application/ogg", "video/ogg")
                                    )
                                }
                                .testTag("pick_file_${lane.laneId}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPersianRtl) "انتخاب فایل" else "Pick File",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Error display if any
            if (lane.errorMessage != null) {
                lane.errorMessage?.let { error ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = RoseMute,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall.copy(color = RoseMute, fontSize = 10.5.sp)
                        )
                    }
                }
            }

            if (lane.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = themePalette.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPersianRtl) "در حال بارگذاری..." else "Loading...",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }

            // Sleek, Minimal Playback & Seek & Volume Scrollbars
            if (lane.fileName != null || lane.playlist.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))

                // Single Unified Control & Seek Progress Scroll Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Play / Pause Button
                    Surface(
                        shape = CircleShape,
                        color = if (lane.isPlaying) themePalette.primary else themePalette.secondary,
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onPlayPause() }
                            .testTag("play_pause_lane_${lane.laneId}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (lane.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (lane.isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (lane.playlist.isNotEmpty()) {
                        IconButton(
                            onClick = onPrevTrack,
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("prev_track_${lane.laneId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Track",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onNextTrack,
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("next_track_${lane.laneId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Track",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Seek Progress Scroll (Clean & Minimal)
                    Slider(
                        value = if (lane.durationMs > 0) {
                            (lane.currentPositionMs.toFloat() / lane.durationMs).coerceIn(0f, 1f)
                        } else 0f,
                        onValueChange = { ratio ->
                            if (lane.durationMs > 0) {
                                onSeek((ratio * lane.durationMs).toLong())
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .padding(horizontal = 4.dp)
                            .testTag("seek_lane_${lane.laneId}"),
                        colors = SliderDefaults.colors(
                            thumbColor = if (lane.isPlaying) themePalette.primary else themePalette.secondary,
                            activeTrackColor = if (lane.isPlaying) themePalette.primary else themePalette.secondary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                        )
                    )

                    // Volume Scroll (Clean & Minimal)
                    Icon(
                        imageVector = if (lane.isMuted || lane.volume == 0f) Icons.Default.VolumeMute else Icons.Default.VolumeDown,
                        contentDescription = null,
                        tint = if (lane.isMuted) RoseMute else TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )

                    val volumeFraction = lane.volume.coerceIn(0f, 1f)
                    val faintVolumeColor = themePalette.secondary.copy(alpha = 0.22f)
                    val boldVolumeColor = themePalette.secondary.copy(alpha = 1.0f)
                    val dynamicActiveColor = androidx.compose.ui.graphics.lerp(faintVolumeColor, boldVolumeColor, volumeFraction)

                    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                    Slider(
                        value = lane.volume,
                        onValueChange = onVolumeChange,
                        modifier = Modifier
                            .width(80.dp)
                            .height(28.dp)
                            .padding(horizontal = 2.dp)
                            .testTag("vol_lane_${lane.laneId}"),
                        thumb = {
                            Surface(
                                shape = CircleShape,
                                color = dynamicActiveColor,
                                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.95f)),
                                shadowElevation = 2.dp,
                                modifier = Modifier.size(18.dp)
                            ) {}
                        },
                        track = { sliderState ->
                            SliderDefaults.Track(
                                sliderState = sliderState,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = dynamicActiveColor,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                                ),
                                thumbTrackGapSize = 0.dp,
                                drawStopIndicator = null
                            )
                        }
                    )
                }
            }
        }
    }
}

fun formatTimeMs(timeMs: Long): String {
    val totalSec = timeMs / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.getDefault(), "%02d:%02d", min, sec)
}
