package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.SessionArtwork
import com.example.ui.theme.LocalAppThemePalette
import com.example.util.LocalAppStrings
import com.example.util.PlaylistShareHelper
import com.example.util.getLocalizedSessionName
import java.util.Locale

@Composable
fun PlaylistsTabScreen(
    lanes: List<AudioLaneState>,
    onOpenSessionPlaylist: (Long) -> Unit,
    onPlayTrackInSession: (Long, Int) -> Unit,
    isPersianRtl: Boolean = false,
    modifier: Modifier = Modifier
) {
    val themePalette = LocalAppThemePalette.current
    val strings = LocalAppStrings.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (isPersianRtl) "لیست‌های پخش نشست‌ها" else "Session Playlists",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = themePalette.textPrimary,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = if (isPersianRtl) "مدیریت ترک‌ها و قطعات هر نشست" else "Manage queues and tracks per session",
                    style = MaterialTheme.typography.bodySmall.copy(color = themePalette.textMuted)
                )
            }
        }

        if (lanes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPersianRtl) "هیچ نشستی ساخته نشده است" else "No audio sessions created",
                    style = MaterialTheme.typography.bodyMedium.copy(color = themePalette.textMuted)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                items(lanes, key = { it.laneId }) { lane ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = themePalette.cardBackground,
                        border = BorderStroke(1.dp, if (lane.isPlaying) themePalette.primary.copy(alpha = 0.6f) else themePalette.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(11.dp)
                        ) {
                            // Session Header
                            val activeTrackUri = lane.uriString
                                ?: lane.playlist.getOrNull(lane.currentTrackIndex)?.uriString
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SessionArtwork(
                                    sessionName = lane.name,
                                    isPlaying = lane.isPlaying,
                                    trackUriString = activeTrackUri,
                                    modifier = Modifier.size(42.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    val localizedName = getLocalizedSessionName(lane.name, isPersianRtl)
                                    Text(
                                        text = localizedName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = themePalette.textPrimary,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Text(
                                        text = "${lane.playlist.size} ${if (isPersianRtl) "قطعه در صف" else "tracks in queue"}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = themePalette.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (lane.playlist.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = themePalette.secondary.copy(alpha = 0.18f),
                                            border = BorderStroke(0.8.dp, themePalette.secondary.copy(alpha = 0.5f)),
                                            modifier = Modifier
                                                .clickable {
                                                    PlaylistShareHelper.shareAllPlaylistTracks(
                                                        context = context,
                                                        playlistName = getLocalizedSessionName(lane.name, isPersianRtl),
                                                        tracks = lane.playlist,
                                                        isPersianRtl = isPersianRtl
                                                    )
                                                }
                                                .testTag("share_playlist_${lane.laneId}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = strings.shareAllTracks,
                                                    tint = themePalette.secondary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = if (isPersianRtl) "اشتراک" else "Share",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = themePalette.secondary
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { onOpenSessionPlaylist(lane.laneId) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themePalette.primary.copy(alpha = 0.18f),
                                            contentColor = themePalette.primary
                                        ),
                                        border = BorderStroke(0.8.dp, themePalette.primary.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.QueueMusic, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isPersianRtl) "مدیریت" else "Manage",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }
                            }

                            // Preview list items (max 4 visible height, scrollable if > 4, nothing if empty)
                            if (lane.playlist.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                val trackScrollState = rememberScrollState()
                                val showScrollbar = lane.playlist.size > 4
                                val visibleCount = minOf(lane.playlist.size, 4)
                                val containerHeight = (visibleCount * 36 + (visibleCount - 1).coerceAtLeast(0) * 4).dp

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(containerHeight),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .then(if (showScrollbar) Modifier.verticalScroll(trackScrollState) else Modifier),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        lane.playlist.forEachIndexed { idx, item ->
                                            val isCurrent = lane.currentTrackIndex == idx
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isCurrent) themePalette.primary.copy(alpha = 0.12f) else themePalette.cardBorder.copy(alpha = 0.3f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(36.dp)
                                                    .clickable { onPlayTrackInSession(lane.laneId, idx) }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (isCurrent && lane.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                        contentDescription = null,
                                                        tint = if (isCurrent) themePalette.primary else themePalette.textMuted,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = item.title,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = if (isCurrent) themePalette.primary else themePalette.textSecondary,
                                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                            fontSize = 12.sp
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (showScrollbar) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        // Scrollbar track & thumb
                                        BoxWithConstraints(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(themePalette.cardBorder.copy(alpha = 0.4f))
                                        ) {
                                            val totalH = maxHeight
                                            val maxScroll = trackScrollState.maxValue.toFloat().coerceAtLeast(1f)
                                            val scrollFraction = (trackScrollState.value.toFloat() / maxScroll).coerceIn(0f, 1f)
                                            val thumbHeight = (totalH * (4f / lane.playlist.size.toFloat())).coerceIn(24.dp, totalH * 0.75f)
                                            val maxOffset = totalH - thumbHeight
                                            val currentOffset = maxOffset * scrollFraction

                                            Box(
                                                modifier = Modifier
                                                    .offset(y = currentOffset)
                                                    .width(4.dp)
                                                    .height(thumbHeight)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(themePalette.primary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
