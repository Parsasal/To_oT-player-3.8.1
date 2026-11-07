package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioLaneState
import com.example.audio.PlaylistItem
import com.example.ui.PlaylistSortType
import com.example.ui.theme.LocalAppThemePalette
import com.example.ui.theme.RoseMute
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PlaylistShareHelper
import com.example.util.getLocalizedSessionName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSheet(
    lane: AudioLaneState,
    isPersianRtl: Boolean,
    onDismiss: () -> Unit,
    onAddFiles: (List<Uri>) -> Unit,
    onSelectTrack: (Int) -> Unit,
    onPlayTrack: (Int) -> Unit = {},
    onRemoveTrack: (Long) -> Unit,
    onClearAll: () -> Unit,
    onToggleLoop: () -> Unit,
    onTogglePlaylistLoop: () -> Unit = {},
    onToggleShuffle: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSortPlaylist: (PlaylistSortType) -> Unit = {},
    onOpenDeviceAudioExplorer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val themePalette = LocalAppThemePalette.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSortMenu by remember { mutableStateOf(false) }
    var currentSortType by remember { mutableStateOf<PlaylistSortType?>(null) }

    BackHandler {
        onDismiss()
    }

    // Multiple file picker
    val multiFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onAddFiles(uris)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = themePalette.bgStart,
        tonalElevation = 16.dp,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                color = themePalette.glassBorder,
                shape = RoundedCornerShape(2.dp)
            ) {
                Spacer(modifier = Modifier.size(width = 36.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .testTag("playlist_sheet_${lane.laneId}")
        ) {
            // Header: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = themePalette.primary.copy(alpha = 0.18f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = themePalette.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    val localizedSessionName = getLocalizedSessionName(lane.name, isPersianRtl)
                    Column {
                        Text(
                            text = if (isPersianRtl) "لیست پخش چندتایی ($localizedSessionName)" else "Playlist Queue: $localizedSessionName",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (isPersianRtl)
                                "${lane.playlist.size} موزیک اضافه شده"
                            else
                                "${lane.playlist.size} audio tracks loaded",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_playlist_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Direct Device Audio Explorer Button
            if (onOpenDeviceAudioExplorer != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = themePalette.secondary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, themePalette.secondary.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable {
                            onDismiss()
                            onOpenDeviceAudioExplorer()
                        }
                        .testTag("open_device_audio_from_playlist")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = themePalette.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPersianRtl) "جستجو در فایل‌های صوتی و دانلودهای گوشی" else "Browse Phone Audio & Recent Downloads",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }

            // Action Buttons: Share All + Clear All
            if (lane.playlist.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Share All Tracks Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = themePalette.secondary.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, themePalette.secondary.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .clickable {
                                PlaylistShareHelper.shareAllPlaylistTracks(
                                    context = context,
                                    playlistName = getLocalizedSessionName(lane.name, isPersianRtl),
                                    tracks = lane.playlist,
                                    isPersianRtl = isPersianRtl
                                )
                            }
                            .testTag("share_all_playlist_tracks_button")
                    ) {
                        Box(
                            modifier = Modifier.padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = if (isPersianRtl) "اشتراک تمام قطعات" else "Share All Tracks",
                                tint = themePalette.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Clear All Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = themePalette.glassSurface,
                        border = BorderStroke(1.dp, themePalette.glassBorder),
                        modifier = Modifier
                            .clickable { onClearAll() }
                            .testTag("clear_playlist_button")
                    ) {
                        Box(
                            modifier = Modifier.padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear All",
                                tint = RoseMute,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Quick Playback Bar if playing or tracks available
            if (lane.playlist.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, themePalette.glassBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (lane.isPlaying) Icons.Default.Equalizer else Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = if (lane.isPlaying) themePalette.primary else themePalette.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = lane.fileName ?: (lane.playlist.getOrNull(lane.currentTrackIndex)?.title ?: "Ready"),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (lane.durationMs > 0) formatTimeMs(lane.durationMs) else "${lane.currentTrackIndex + 1}/${lane.playlist.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onPrev,
                                modifier = Modifier.size(32.dp).testTag("playlist_prev_track")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Previous",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = if (lane.isPlaying) themePalette.primary else themePalette.secondary,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { onPlayPause() }
                                    .testTag("playlist_play_pause_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (lane.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onNext,
                                modifier = Modifier.size(32.dp).testTag("playlist_next_track")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Next",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Playlist Items List
            if (lane.playlist.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themePalette.glassSurfaceElevated,
                    border = BorderStroke(1.dp, themePalette.glassBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isPersianRtl)
                                "هیچ فایلی برای این ردیف انتخاب نشده است"
                            else
                                "No tracks added to this lane yet",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPersianRtl)
                                "می‌توانید تا ۱۰۰ موزیک برای موقعیت‌هایی مانند شب، ورزش یا مطالعه انتخاب کنید."
                            else
                                "Select up to 100 audio files for night, workout, or study profiles.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 11.5.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                multiFilePickerLauncher.launch(
                                    arrayOf("audio/*", "application/ogg", "video/ogg", "*/*")
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themePalette.primary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPersianRtl) "انتخاب گروهی موزیک‌ها" else "Pick Audio Files",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isPersianRtl) "ترک‌های صوتی (${lane.playlist.size})" else "Tracks Queue (${lane.playlist.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Sorting Button & Dropdown Menu
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = themePalette.glassSurface,
                            border = BorderStroke(1.dp, themePalette.glassBorder),
                            modifier = Modifier
                                .clickable { showSortMenu = true }
                                .testTag("sort_playlist_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort Tracks",
                                    tint = themePalette.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = when (currentSortType) {
                                        PlaylistSortType.NAME_ASC -> if (isPersianRtl) "نام (الف-ی)" else "Name (A-Z)"
                                        PlaylistSortType.NAME_DESC -> if (isPersianRtl) "نام (ی-الف)" else "Name (Z-A)"
                                        PlaylistSortType.DURATION_DESC -> if (isPersianRtl) "طولانی‌ترین" else "Longest"
                                        PlaylistSortType.DURATION_ASC -> if (isPersianRtl) "کوتاه‌ترین" else "Shortest"
                                        PlaylistSortType.DATE_ADDED -> if (isPersianRtl) "تاریخ افزودن" else "Date Added"
                                        null -> if (isPersianRtl) "مرتب‌سازی" else "Sort"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = themePalette.textPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(themePalette.cardBackground)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Title,
                                            contentDescription = null,
                                            tint = themePalette.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            if (isPersianRtl) "نام (الف تا ی)" else "Name (A to Z)",
                                            color = themePalette.textPrimary,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    currentSortType = PlaylistSortType.NAME_ASC
                                    showSortMenu = false
                                    onSortPlaylist(PlaylistSortType.NAME_ASC)
                                },
                                modifier = Modifier.testTag("sort_by_name_asc")
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Title,
                                            contentDescription = null,
                                            tint = themePalette.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            if (isPersianRtl) "نام (ی تا الف)" else "Name (Z to A)",
                                            color = themePalette.textPrimary,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    currentSortType = PlaylistSortType.NAME_DESC
                                    showSortMenu = false
                                    onSortPlaylist(PlaylistSortType.NAME_DESC)
                                },
                                modifier = Modifier.testTag("sort_by_name_desc")
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = themePalette.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            if (isPersianRtl) "مدت زمان (طولانی به کوتاه)" else "Duration (Longest first)",
                                            color = themePalette.textPrimary,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    currentSortType = PlaylistSortType.DURATION_DESC
                                    showSortMenu = false
                                    onSortPlaylist(PlaylistSortType.DURATION_DESC)
                                },
                                modifier = Modifier.testTag("sort_by_duration_desc")
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = themePalette.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            if (isPersianRtl) "مدت زمان (کوتاه به طولانی)" else "Duration (Shortest first)",
                                            color = themePalette.textPrimary,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    currentSortType = PlaylistSortType.DURATION_ASC
                                    showSortMenu = false
                                    onSortPlaylist(PlaylistSortType.DURATION_ASC)
                                },
                                modifier = Modifier.testTag("sort_by_duration_asc")
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = themePalette.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            if (isPersianRtl) "تاریخ افزودن به صف" else "Date Added / Original",
                                            color = themePalette.textPrimary,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    currentSortType = PlaylistSortType.DATE_ADDED
                                    showSortMenu = false
                                    onSortPlaylist(PlaylistSortType.DATE_ADDED)
                                },
                                modifier = Modifier.testTag("sort_by_date_added")
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    itemsIndexed(lane.playlist, key = { _, item -> item.id }) { index, item ->
                        val isCurrentTrack = index == lane.currentTrackIndex && lane.fileName == item.title

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCurrentTrack) {
                                themePalette.primary.copy(alpha = 0.16f)
                            } else themePalette.glassSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isCurrentTrack) {
                                    themePalette.primary.copy(alpha = 0.7f)
                                } else themePalette.glassBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { onPlayTrack(index) }
                                .testTag("playlist_item_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Play / Track number indicator button
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isCurrentTrack) {
                                            themePalette.primary
                                        } else Color.White.copy(alpha = 0.08f),
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable { onPlayTrack(index) }
                                            .testTag("play_item_${item.id}")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (isCurrentTrack && lane.isPlaying) {
                                                Icon(
                                                    imageVector = Icons.Default.Equalizer,
                                                    contentDescription = "Playing",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play Track",
                                                    tint = if (isCurrentTrack) Color.Black else TextSecondary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (isCurrentTrack) {
                                                    themePalette.primary
                                                } else TextPrimary,
                                                fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isCurrentTrack) {
                                                if (lane.isPlaying) (if (isPersianRtl) "در حال پخش" else "Playing") else (if (isPersianRtl) "آماده برای پخش" else "Ready")
                                            } else {
                                                if (isPersianRtl) "موزیک شماره ${index + 1}" else "Track #${index + 1}"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isCurrentTrack) {
                                                    themePalette.primary
                                                } else TextMuted,
                                                fontSize = 9.5.sp
                                            )
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isCurrentTrack) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = themePalette.primary.copy(alpha = 0.22f),
                                            border = BorderStroke(0.5.dp, themePalette.primary.copy(alpha = 0.8f)),
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            Text(
                                                text = if (lane.isPlaying) {
                                                    if (isPersianRtl) "در حال پخش" else "PLAYING"
                                                } else {
                                                    if (isPersianRtl) "منتخب" else "SELECTED"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = themePalette.primary,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // Delete from playlist button
                                    IconButton(
                                        onClick = { onRemoveTrack(item.id) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("remove_item_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = if (isPersianRtl) "حذف از لیست" else "Remove from Playlist",
                                            tint = RoseMute,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Done / Close Sheet Button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("done_playlist_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                ),
                border = BorderStroke(1.dp, themePalette.glassBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isPersianRtl) "تأیید و بستن لیست" else "Done / Close Playlist",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
