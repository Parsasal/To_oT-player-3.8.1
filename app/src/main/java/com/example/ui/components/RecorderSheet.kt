package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.audio.AudioLaneState
import com.example.audio.RecordedTrack
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBgCenter
import com.example.ui.theme.EmeraldPlay
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.LocalAppThemePalette
import com.example.ui.theme.RoseMute
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderSheet(
    isRecording: Boolean,
    isPaused: Boolean,
    durationSec: Int,
    amplitude: Float = 0f,
    recordings: List<RecordedTrack>,
    playingTrackId: String? = null,
    lanes: List<AudioLaneState> = emptyList(),
    errorMessage: String?,
    onStartRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onTogglePlayPreview: (RecordedTrack) -> Unit,
    onAddRecordingToLane: (laneId: Long, track: RecordedTrack) -> Unit = { _, _ -> },
    onDeleteRecording: (RecordedTrack) -> Unit,
    onDismiss: () -> Unit,
    isPersianRtl: Boolean = false
) {
    val context = LocalContext.current
    val themePalette = LocalAppThemePalette.current

    // Permission launcher for RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartRecording()
        } else {
            Toast.makeText(
                context,
                if (isPersianRtl) "برای ضبط صدا، مجوز دسترسی به میکروفون لازم است" else "Microphone permission is required to record audio",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = themePalette.bgCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("recorder_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) RoseMute.copy(alpha = 0.2f) else themePalette.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (isRecording) RoseMute else themePalette.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isPersianRtl) "ضبط خروجی مستر و استودیو" else "Master Output & Studio Recorder",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = themePalette.textPrimary
                            )
                        )
                        Text(
                            text = if (isPersianRtl) "ضبط مستقیم سیگنال استریو با نرخ ۴۴.۱ کیلوهرتز" else "Direct stereo mix capture @ 44.1kHz AAC",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = themePalette.textMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Recording Status Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = themePalette.cardBackground,
                border = BorderStroke(
                    1.2.dp,
                    if (isRecording) RoseMute.copy(alpha = pulseAlpha) else themePalette.cardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isRecording) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isPaused) AmberSolo else RoseMute.copy(alpha = pulseAlpha),
                                modifier = Modifier.size(14.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPaused) {
                                    if (isPersianRtl) "ضبط موقتاً متوقف شده" else "RECORDING PAUSED"
                                } else {
                                    if (isPersianRtl) "در حال ضبط خروجی مستر..." else "RECORDING LIVE MASTER MIX..."
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isPaused) AmberSolo else RoseMute,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val min = durationSec / 60
                        val sec = durationSec % 60
                        Text(
                            text = String.format("%02d:%02d", min, sec),
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = themePalette.textPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 42.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Live Dynamic VU / Level Meter
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { if (isPaused) 0f else amplitude.coerceIn(0.05f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (amplitude > 0.85f) RoseMute else themePalette.primary,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("-48 dB", style = MaterialTheme.typography.labelSmall.copy(color = themePalette.textMuted, fontSize = 9.sp))
                                Text("LIVE VU LEVEL", style = MaterialTheme.typography.labelSmall.copy(color = themePalette.textMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                Text("0 dB PEAK", style = MaterialTheme.typography.labelSmall.copy(color = themePalette.textMuted, fontSize = 9.sp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Controls: Pause/Resume + Stop
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isPaused) onResumeRecording() else onPauseRecording()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPaused) EmeraldPlay else AmberSolo
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("recorder_pause_resume")
                            ) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPaused) (if (isPersianRtl) "ادامه ضبط" else "Resume") else (if (isPersianRtl) "مکث ضبط" else "Pause"),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = onStopRecording,
                                colors = ButtonDefaults.buttonColors(containerColor = RoseMute),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("recorder_stop_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPersianRtl) "توقف و ذخیره" else "Stop & Save",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (isPersianRtl) "ضبط فوری و بی‌درنگ صدای خروجی مستر با بالاترین شفافیت و کیفیت استودیویی" else "Record the live combined multi-lane audio stream in high-bitrate AAC format",
                            style = MaterialTheme.typography.bodyMedium.copy(color = themePalette.textSecondary),
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    onStartRecording()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoseMute,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_recording_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPersianRtl) "شروع ضبط مستر (Record Mix)" else "Start Master Recording",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // Error display if any
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RoseMute.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, RoseMute.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = RoseMute,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Saved Recordings List Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isPersianRtl) "میکس‌های ضبط شده (${recordings.size}):" else "Recorded Mixes (${recordings.size}):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = themePalette.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (recordings.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.03f),
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.06f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPersianRtl) "هنوز فایلی ضبط نشده است. دکمه قرمز بالا را لمس کنید." else "No recordings yet. Tap the red button above to record your live mix.",
                            style = MaterialTheme.typography.bodySmall.copy(color = themePalette.textMuted)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(recordings, key = { it.id }) { track ->
                        val isPlayingThis = playingTrackId == track.id
                        var showLaneMenu by remember { mutableStateOf(false) }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isPlayingThis) themePalette.primary.copy(alpha = 0.15f) else themePalette.glassSurfaceElevated,
                            border = BorderStroke(
                                0.8.dp,
                                if (isPlayingThis) themePalette.primary else themePalette.glassBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Play / Stop Preview Button
                                IconButton(
                                    onClick = { onTogglePlayPreview(track) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlayingThis) "Stop" else "Play",
                                        tint = if (isPlayingThis) RoseMute else themePalette.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Track Details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isPlayingThis) themePalette.primary else themePalette.textPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(track.timestamp))
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = themePalette.textMuted,
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                // Actions: Add to Lane, Share, Delete
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Add to Lane Dropdown
                                    Box {
                                        IconButton(
                                            onClick = {
                                                if (lanes.isNotEmpty()) {
                                                    showLaneMenu = true
                                                } else {
                                                    Toast.makeText(context, if (isPersianRtl) "ابتدا یک لاین ایجاد کنید" else "Create a lane first", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.QueueMusic,
                                                contentDescription = "Add to Lane",
                                                tint = themePalette.secondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = showLaneMenu,
                                            onDismissRequest = { showLaneMenu = false }
                                        ) {
                                            Text(
                                                text = if (isPersianRtl) "افزودن به لاین صوتی:" else "Add to Lane:",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = themePalette.textMuted
                                                ),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                            lanes.forEach { lane ->
                                                DropdownMenuItem(
                                                    text = { Text(lane.name, color = themePalette.textPrimary) },
                                                    onClick = {
                                                        showLaneMenu = false
                                                        onAddRecordingToLane(lane.laneId, track)
                                                        Toast.makeText(
                                                            context,
                                                            if (isPersianRtl) "به لاین ${lane.name} افزوده شد" else "Added to ${lane.name}",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Share
                                    IconButton(
                                        onClick = {
                                            try {
                                                val file = File(track.filePath)
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    file
                                                )
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "audio/*"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, "Share Master Recording"))
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Share error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = CyanAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Delete
                                    IconButton(
                                        onClick = { onDeleteRecording(track) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
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

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

val AmberSolo = Color(0xFFF59E0B)
