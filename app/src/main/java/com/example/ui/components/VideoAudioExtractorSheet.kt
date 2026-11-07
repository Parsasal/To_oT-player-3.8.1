package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.core.content.FileProvider
import com.example.audio.AudioLaneState
import com.example.ui.theme.LocalAppThemePalette
import com.example.ui.theme.TootThemePalette
import com.example.util.AudioEffectPreset
import com.example.util.AudioEffectsSettings
import com.example.util.AudioExtractionMode
import com.example.util.LocalActiveLanguage
import com.example.util.LocalAppStrings
import com.example.util.VideoAudioExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoAudioExtractorSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    lanes: List<AudioLaneState>,
    selectedLaneId: Long?,
    onPlayExtractedFile: (targetLaneId: Long, uri: Uri, title: String) -> Unit,
    onAddExtractedToLane: (targetLaneId: Long, uri: Uri, title: String) -> Unit,
    onSwitchToPlayer: () -> Unit = {}
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val currentLang = LocalActiveLanguage.current
    val isPersianRtl = currentLang.isRtl
    val themePalette = LocalAppThemePalette.current
    val coroutineScope = rememberCoroutineScope()

    // Video Selection State
    var selectedVideoInfo by remember { mutableStateOf<VideoAudioExtractor.VideoInfo?>(null) }
    var isInspectingVideo by remember { mutableStateOf(false) }

    // Extraction & Output State
    var outputFileName by remember { mutableStateOf("") }
    var extractionMode by remember { mutableStateOf(AudioExtractionMode.FULL) }
    var trimStartSec by remember { mutableFloatStateOf(0f) }
    var trimEndSec by remember { mutableFloatStateOf(60f) }
    var splitPointSec by remember { mutableFloatStateOf(30f) }
    var fadeInEnabled by remember { mutableStateOf(false) }
    var fadeOutEnabled by remember { mutableStateOf(false) }
    var selectedVolumeGain by remember { mutableFloatStateOf(1.0f) }
    var selectedPreset by remember { mutableStateOf(AudioEffectPreset.NORMAL) }

    var selectedTargetLaneId by rememberSaveable {
        mutableLongStateOf(selectedLaneId ?: lanes.firstOrNull()?.laneId ?: -1L)
    }

    LaunchedEffect(isOpen) {
        if (isOpen) {
            val validInitial = when {
                selectedLaneId != null && lanes.any { it.laneId == selectedLaneId } -> selectedLaneId
                lanes.isNotEmpty() -> lanes.first().laneId
                else -> -1L
            }
            if (selectedTargetLaneId == 0L || (selectedTargetLaneId > 0L && lanes.none { it.laneId == selectedTargetLaneId })) {
                selectedTargetLaneId = validInitial
            }
        }
    }

    var isExtracting by remember { mutableStateOf(false) }
    var extractionProgress by remember { mutableFloatStateOf(0f) }
    var extractedResultFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastAddedTrackNotice by remember { mutableStateOf<String?>(null) }

    // Unified Audio Preview Player State (ONE Player for everything)
    var isPreviewPlaying by remember { mutableStateOf(false) }
    var previewCurrentSec by remember { mutableFloatStateOf(0f) }
    var previewBoundStartSec by remember { mutableFloatStateOf(0f) }
    var previewBoundEndSec by remember { mutableFloatStateOf(0f) }
    var isLoopPreview by remember { mutableStateOf(false) }
    var isUserDraggingSlider by remember { mutableStateOf(false) }
    var isSeekingPreview by remember { mutableStateOf(false) }
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val videoDurSec = ((selectedVideoInfo?.durationMs ?: 0L) / 1000f).coerceAtLeast(1f)

    // Player Helpers
    val stopPreviewAudio: () -> Unit = {
        try {
            previewPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
            }
        } catch (_: Exception) {}
        isPreviewPlaying = false
        isSeekingPreview = false
    }

    val playPreviewRange: (startSec: Float, endSec: Float, loop: Boolean) -> Unit = { start, end, loop ->
        val uri = selectedVideoInfo?.uri
        if (uri != null) {
            val safeStart = start.coerceIn(0f, videoDurSec)
            val safeEnd = end.coerceIn(safeStart + 0.1f, videoDurSec)
            previewBoundStartSec = safeStart
            previewBoundEndSec = safeEnd
            previewCurrentSec = safeStart
            isLoopPreview = loop

            try {
                var player = previewPlayer
                if (player == null) {
                    player = MediaPlayer().apply {
                        setDataSource(context, uri)
                        prepare()
                    }
                    previewPlayer = player
                }
                player.seekTo((safeStart * 1000).toInt())
                player.start()
                isPreviewPlaying = true
            } catch (e: Exception) {
                Log.e("VideoAudioExtractor", "Error playing preview audio: $e", e)
                stopPreviewAudio()
            }
        }
    }

    val seekPreviewTo: (Float) -> Unit = { targetSec ->
        val uri = selectedVideoInfo?.uri
        if (uri != null) {
            val safeSec = targetSec.coerceIn(0f, videoDurSec)
            previewCurrentSec = safeSec

            try {
                var player = previewPlayer
                if (player == null) {
                    player = MediaPlayer().apply {
                        setDataSource(context, uri)
                        prepare()
                    }
                    previewPlayer = player
                }
                isSeekingPreview = true
                player.seekTo((safeSec * 1000).toInt())
                isSeekingPreview = false
                if (isPreviewPlaying && !player.isPlaying) {
                    player.start()
                }
            } catch (e: Exception) {
                Log.e("VideoAudioExtractor", "Error seeking preview: $e")
                isSeekingPreview = false
            }
        }
    }

    val skipPreviewBy: (Float) -> Unit = { deltaSec ->
        val newSec = (previewCurrentSec + deltaSec).coerceIn(0f, videoDurSec)
        seekPreviewTo(newSec)
    }

    // Toggle master play/pause
    val togglePlayPause: () -> Unit = {
        if (isPreviewPlaying) {
            stopPreviewAudio()
        } else {
            val start = if (previewCurrentSec in trimStartSec..(trimEndSec - 0.2f)) previewCurrentSec else trimStartSec
            playPreviewRange(start, trimEndSec, isLoopPreview)
        }
    }

    val playFromTrimStart: () -> Unit = {
        stopPreviewAudio()
        previewCurrentSec = trimStartSec
        playPreviewRange(trimStartSec, trimEndSec, isLoopPreview)
    }

    val resetToFullSelection: () -> Unit = {
        stopPreviewAudio()
        trimStartSec = 0f
        trimEndSec = videoDurSec
        previewCurrentSec = 0f
    }

    // Track active playback position and enforce range boundaries
    LaunchedEffect(isPreviewPlaying, previewBoundStartSec, previewBoundEndSec, isLoopPreview, isUserDraggingSlider) {
        if (isPreviewPlaying) {
            while (isActive && isPreviewPlaying) {
                val player = previewPlayer
                if (player != null && isPreviewPlaying) {
                    try {
                        val currentMs = player.currentPosition
                        val currentSec = currentMs / 1000f

                        if (!isUserDraggingSlider && !isSeekingPreview) {
                            previewCurrentSec = currentSec
                        }

                        // Check if reached end boundary
                        if (previewBoundEndSec > 0f && currentSec >= previewBoundEndSec - 0.08f) {
                            if (isLoopPreview) {
                                isSeekingPreview = true
                                player.seekTo((previewBoundStartSec * 1000).toInt())
                                previewCurrentSec = previewBoundStartSec
                                isSeekingPreview = false
                            } else {
                                stopPreviewAudio()
                                previewCurrentSec = previewBoundStartSec
                            }
                        } else if (!player.isPlaying && !isSeekingPreview) {
                            stopPreviewAudio()
                        }
                    } catch (_: Exception) {}
                }
                delay(80)
            }
        }
    }

    // Cleanup on exit
    DisposableEffect(Unit) {
        onDispose {
            stopPreviewAudio()
            try {
                previewPlayer?.stop()
                previewPlayer?.release()
            } catch (_: Exception) {}
            previewPlayer = null
        }
    }

    fun handleVideoPicked(uri: Uri) {
        stopPreviewAudio()
        selectedVideoInfo = null
        errorMessage = null
        extractedResultFiles = emptyList()
        isInspectingVideo = true

        coroutineScope.launch {
            try {
                val info = withContext(Dispatchers.IO) {
                    VideoAudioExtractor.inspectVideo(context, uri)
                }
                selectedVideoInfo = info
                outputFileName = info.fileName.ifBlank { "extracted_audio" }
                val durSec = (info.durationMs / 1000f).coerceAtLeast(1f)
                trimStartSec = 0f
                trimEndSec = durSec
                splitPointSec = (durSec / 2f).coerceAtLeast(1f)
                previewBoundStartSec = 0f
                previewBoundEndSec = durSec
                previewCurrentSec = 0f
            } catch (e: Throwable) {
                errorMessage = if (isPersianRtl) "خطا در بارگذاری اطلاعات ویدیو: ${e.message}" else "Error loading video info: ${e.message}"
            } finally {
                isInspectingVideo = false
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) handleVideoPicked(uri)
    }

    val fallbackPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) handleVideoPicked(uri)
    }

    fun launchVideoChooser() {
        stopPreviewAudio()
        try {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
            )
        } catch (_: Exception) {
            fallbackPickerLauncher.launch("video/*")
        }
    }

    fun formatSec(seconds: Float): String {
        val totalSec = seconds.toInt().coerceAtLeast(0)
        val hours = totalSec / 3600
        val min = (totalSec % 3600) / 60
        val sec = totalSec % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, min, sec)
        } else {
            String.format(Locale.US, "%02d:%02d", min, sec)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            stopPreviewAudio()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = themePalette.colorBackground,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF6366F1).copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.videoAudioExtractor,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = themePalette.textPrimary,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = if (isPersianRtl) "تبدیل، برش و استخراج صوت با یک پلیر هوشمند و دقیق" else "Convert, trim & extract audio with unified smart player",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = themePalette.textSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = {
                        stopPreviewAudio()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("close_audio_extractor_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = themePalette.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 1: Video File Selection (Or Video Card)
            val video = selectedVideoInfo
            if (video == null) {
                // Empty State / Picker Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = themePalette.cardBackground,
                    border = BorderStroke(1.2.dp, themePalette.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isInspectingVideo) { launchVideoChooser() }
                        .testTag("select_video_button")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isInspectingVideo) {
                            CircularProgressIndicator(
                                color = themePalette.primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isPersianRtl) "در حال پردازش و استخراج اطلاعات ویدیو..." else "Inspecting video format & audio streams...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = themePalette.textSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = themePalette.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = themePalette.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isPersianRtl) "انتخاب ویدیو از دستگاه" else "Select Video File",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = themePalette.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isPersianRtl) "پشتیبانی از انواع ویدیو: MP4, MKV, WebM, MOV, AVI" else "Supported: MP4, MKV, WebM, MOV, AVI",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = themePalette.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { launchVideoChooser() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themePalette.primary,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPersianRtl) "انتخاب فایل ویدیو" else "Browse Video",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // Sleek Selected Video Info Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = themePalette.cardBackground,
                    border = BorderStroke(1.dp, themePalette.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF6366F1).copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = Color(0xFF6366F1),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = video.fileName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = themePalette.textPrimary
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${video.formattedDuration} • ${video.fileName.substringAfterLast('.', "Video").uppercase()}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = themePalette.textSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { launchVideoChooser() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themePalette.cardBorder.copy(alpha = 0.8f),
                                contentColor = themePalette.textPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (isPersianRtl) "تغییر ویدیو" else "Change",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val isFullSelection = trimStartSec <= 0.2f && trimEndSec >= (videoDurSec - 0.2f)

                // =========================================================================
                // UNIFIED AUDIO AUDITION & DUAL-HANDLE RANGE TRIMMER
                // =========================================================================
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = themePalette.cardBackground,
                    border = BorderStroke(
                        1.5.dp,
                        if (isPreviewPlaying) themePalette.primary else themePalette.cardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audio_preview_trimmer_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title row & playback status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isPreviewPlaying) themePalette.primary.copy(alpha = 0.2f) else themePalette.cardBorder,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isPreviewPlaying) Icons.Default.GraphicEq else Icons.Default.Headphones,
                                            contentDescription = null,
                                            tint = if (isPreviewPlaying) themePalette.primary else themePalette.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isPersianRtl) "پلیر و انتخابگر بازه صوتی" else "Audio Player & Range Trimmer",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = themePalette.textPrimary,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = when {
                                            isPreviewPlaying && !isFullSelection ->
                                                if (isPersianRtl) "در حال پخش بازه انتخابی (${formatSec(trimStartSec)} تا ${formatSec(trimEndSec)})" else "Playing Selection (${formatSec(trimStartSec)} - ${formatSec(trimEndSec)})"
                                            isPreviewPlaying ->
                                                if (isPersianRtl) "در حال پخش پیش‌شنیدن..." else "Playing preview..."
                                            isFullSelection ->
                                                if (isPersianRtl) "کل فایل صوتی انتخاب شده است (${formatSec(videoDurSec)})" else "Full audio track selected (${formatSec(videoDurSec)})"
                                            else ->
                                                if (isPersianRtl) "بازه مشخص شده: ${formatSec(trimStartSec)} تا ${formatSec(trimEndSec)}" else "Range: ${formatSec(trimStartSec)} to ${formatSec(trimEndSec)}"
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isPreviewPlaying) themePalette.primary else themePalette.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            // Time Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPreviewPlaying) themePalette.primary.copy(alpha = 0.15f) else themePalette.cardBorder.copy(alpha = 0.6f),
                                border = BorderStroke(0.8.dp, if (isPreviewPlaying) themePalette.primary else Color.Transparent)
                            ) {
                                Text(
                                    text = "${formatSec(previewCurrentSec)} / ${formatSec(videoDurSec)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isPreviewPlaying) themePalette.primary else themePalette.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive Range Cards (Start Time - Selection Duration - End Time)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Start Time Card (Left Handle)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = themePalette.colorBackground,
                                border = BorderStroke(1.dp, if (!isFullSelection) themePalette.primary.copy(alpha = 0.6f) else themePalette.cardBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isPersianRtl) "شروع (دستگیره ۱)" else "Start (Handle 1)",
                                        fontSize = 10.sp,
                                        color = themePalette.textSecondary
                                    )
                                    Text(
                                        text = formatSec(trimStartSec),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isFullSelection) themePalette.primary else themePalette.textPrimary
                                    )
                                }
                            }

                            // Middle: Duration & Quick Reset
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (!isFullSelection) themePalette.primary.copy(alpha = 0.15f) else themePalette.cardBorder.copy(alpha = 0.5f),
                                border = BorderStroke(0.8.dp, if (!isFullSelection) themePalette.primary else Color.Transparent),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isPersianRtl) "طول برش: ${formatSec(trimEndSec - trimStartSec)}" else "Length: ${formatSec(trimEndSec - trimStartSec)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isFullSelection) themePalette.primary else themePalette.textPrimary
                                    )
                                    if (!isFullSelection) {
                                        Text(
                                            text = if (isPersianRtl) "انتخاب کل صوت" else "Select Full",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = themePalette.primary,
                                            modifier = Modifier
                                                .clickable { resetToFullSelection() }
                                                .padding(top = 2.dp)
                                        )
                                    } else {
                                        Text(
                                            text = if (isPersianRtl) "کل فایل" else "Full Track",
                                            fontSize = 9.5.sp,
                                            color = themePalette.textSecondary,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }

                            // End Time Card (Right Handle)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = themePalette.colorBackground,
                                border = BorderStroke(1.dp, if (!isFullSelection) themePalette.primary.copy(alpha = 0.6f) else themePalette.cardBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isPersianRtl) "پایان (دستگیره ۲)" else "End (Handle 2)",
                                        fontSize = 10.sp,
                                        color = themePalette.textSecondary
                                    )
                                    Text(
                                        text = formatSec(trimEndSec),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isFullSelection) themePalette.primary else themePalette.textPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dual-Handle Range Slider (Dragging from start and end)
                        RangeSlider(
                            value = trimStartSec..trimEndSec,
                            onValueChange = { range ->
                                isUserDraggingSlider = true
                                val start = range.start.coerceIn(0f, videoDurSec)
                                val end = range.endInclusive.coerceIn(start + 0.5f, videoDurSec)
                                trimStartSec = start
                                trimEndSec = end
                            },
                            onValueChangeFinished = {
                                isUserDraggingSlider = false
                                if (previewCurrentSec < trimStartSec || previewCurrentSec > trimEndSec) {
                                    seekPreviewTo(trimStartSec)
                                }
                            },
                            valueRange = 0f..videoDurSec,
                            colors = SliderDefaults.colors(
                                thumbColor = themePalette.primary,
                                activeTrackColor = themePalette.primary,
                                inactiveTrackColor = themePalette.cardBorder.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trim_dual_handle_slider")
                        )

                        // Visual Helper & Playhead tracker
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isPersianRtl) "« کشیدن ابتدا" else "◀ Drag Start",
                                fontSize = 10.sp,
                                color = themePalette.textSecondary
                            )
                            Text(
                                text = if (isPersianRtl) "با کشیدن دو دستگیره بازه را انتخاب کنید" else "Drag handles to select cut range",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = themePalette.textSecondary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = if (isPersianRtl) "کشیدن انتها »" else "Drag End ▶",
                                fontSize = 10.sp,
                                color = themePalette.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Unified Audition Control Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Replay from trim start
                            FilledTonalIconButton(
                                onClick = playFromTrimStart,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = themePalette.cardBorder.copy(alpha = 0.7f),
                                    contentColor = themePalette.textPrimary
                                ),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastRewind,
                                    contentDescription = "Rewind to Start",
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Skip -5s
                            FilledTonalIconButton(
                                onClick = { skipPreviewBy(-5f) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = themePalette.cardBorder.copy(alpha = 0.7f),
                                    contentColor = themePalette.textPrimary
                                ),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Text("-5s", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Primary Play / Pause Button (Plays selected range)
                            Button(
                                onClick = togglePlayPause,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPreviewPlaying) themePalette.secondary else themePalette.primary,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .size(54.dp)
                                    .testTag("preview_play_pause_btn")
                            ) {
                                Icon(
                                    imageVector = if (isPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPreviewPlaying) "Pause" else "Play",
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Skip +5s
                            FilledTonalIconButton(
                                onClick = { skipPreviewBy(5f) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = themePalette.cardBorder.copy(alpha = 0.7f),
                                    contentColor = themePalette.textPrimary
                                ),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Text("+5s", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Loop Toggle
                            IconButton(
                                onClick = { isLoopPreview = !isLoopPreview },
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = "Loop Selection",
                                    tint = if (isLoopPreview) themePalette.primary else themePalette.textSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Compact Fade In & Out Options (Clean & Minimalist)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(themePalette.colorBackground.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = fadeInEnabled,
                                    onCheckedChange = { fadeInEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = themePalette.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPersianRtl) "محو شروع (Fade In)" else "Fade In",
                                    fontSize = 11.sp,
                                    color = themePalette.textPrimary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = fadeOutEnabled,
                                    onCheckedChange = { fadeOutEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = themePalette.primary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPersianRtl) "محو پایان (Fade Out)" else "Fade Out",
                                    fontSize = 11.sp,
                                    color = themePalette.textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step 3: Output Settings & Enhancement
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = themePalette.cardBackground,
                border = BorderStroke(1.dp, themePalette.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isPersianRtl) "تنظیمات فایل خروجی" else "Output & Sound Settings",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = themePalette.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filename
                    OutlinedTextField(
                        value = outputFileName,
                        onValueChange = { outputFileName = it },
                        label = { Text(if (isPersianRtl) "نام فایل صوتی" else "Audio File Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("output_mp3_name_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themePalette.primary,
                            unfocusedBorderColor = themePalette.cardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sound Enhancement Preset Chips
                    Text(
                        text = if (isPersianRtl) "تقویت و تنظیم صدا:" else "Sound Profile:",
                        fontSize = 11.5.sp,
                        color = themePalette.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            AudioEffectPreset.NORMAL to (if (isPersianRtl) "معمولی" else "Normal"),
                            AudioEffectPreset.BASS_BOOST to (if (isPersianRtl) "بیس قوی" else "Bass Boost"),
                            AudioEffectPreset.VOCAL_CLARITY to (if (isPersianRtl) "وضوح صدا" else "Vocal"),
                            AudioEffectPreset.CONCERT_HALL to (if (isPersianRtl) "کنسرت" else "Concert")
                        ).forEach { (preset, label) ->
                            FilterChip(
                                selected = selectedPreset == preset,
                                onClick = { selectedPreset = preset },
                                label = { Text(label, fontSize = 10.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = themePalette.primary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Volume Boost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPersianRtl) "تقویت بلندی صدا (Volume Boost):" else "Volume Boost:",
                            fontSize = 11.5.sp,
                            color = themePalette.textSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                1.0f to "100%",
                                1.35f to "+35%",
                                1.8f to "+80%"
                            ).forEach { (gain, label) ->
                                FilterChip(
                                    selected = selectedVolumeGain == gain,
                                    onClick = { selectedVolumeGain = gain },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = themePalette.secondary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    TargetLaneSelectionSection(
                        lanes = lanes,
                        selectedTargetLaneId = selectedTargetLaneId,
                        onSelectTargetLane = { selectedTargetLaneId = it },
                        isPersianRtl = isPersianRtl,
                        themePalette = themePalette
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Extraction Progress
            if (isExtracting) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themePalette.cardBackground,
                    border = BorderStroke(1.dp, themePalette.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isPersianRtl) "در حال پردازش و استخراج صوت..." else "Extracting high-quality audio...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themePalette.primary
                            )
                            Text(
                                text = "${(extractionProgress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themePalette.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { extractionProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = themePalette.primary,
                            trackColor = themePalette.cardBorder
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Error Message
            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFEF4444),
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Main Extract Button
            if (extractedResultFiles.isEmpty()) {
                Button(
                    onClick = {
                        stopPreviewAudio()
                        isExtracting = true
                        extractionProgress = 0f
                        errorMessage = null

                        val isFull = trimStartSec <= 0.2f && trimEndSec >= (videoDurSec - 0.2f)
                        val activeMode = if (isFull) AudioExtractionMode.FULL else AudioExtractionMode.TRIM

                        val finalSettings = AudioEffectsSettings(
                            preset = selectedPreset,
                            mode = activeMode,
                            startMs = (trimStartSec * 1000).toLong(),
                            endMs = (trimEndSec * 1000).toLong(),
                            splitMs = (splitPointSec * 1000).toLong(),
                            volumeGain = selectedVolumeGain,
                            fadeInSeconds = if (fadeInEnabled) 2.0f else 0f,
                            fadeOutSeconds = if (fadeOutEnabled) 2.0f else 0f
                        )

                        coroutineScope.launch {
                            val result = VideoAudioExtractor.extractAudioWithEffects(
                                context = context,
                                videoUri = video.uri,
                                outputBaseName = outputFileName.ifBlank { video.fileName },
                                settings = finalSettings,
                                onProgress = { p -> extractionProgress = p }
                            )
                            isExtracting = false
                            result.onSuccess { files ->
                                extractedResultFiles = files
                                val primaryFile = files.firstOrNull()
                                if (primaryFile != null) {
                                    val safeUri = try {
                                        FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            primaryFile
                                        )
                                    } catch (_: Exception) {
                                        Uri.fromFile(primaryFile)
                                    }
                                    val trackTitle = primaryFile.nameWithoutExtension
                                    onAddExtractedToLane(selectedTargetLaneId, safeUri, trackTitle)
                                    val laneName = when {
                                        selectedTargetLaneId == -1L -> if (isPersianRtl) "لاین جدید" else "New Lane"
                                        else -> lanes.find { it.laneId == selectedTargetLaneId }?.name ?: (if (isPersianRtl) "لاین صوتی" else "Audio Lane")
                                    }
                                    lastAddedTrackNotice = laneName
                                    Toast.makeText(
                                        context,
                                        if (isPersianRtl) "فایل با موفقیت استخراج و در «$laneName» قرار گرفت" else "Extracted and placed into $laneName",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }.onFailure { err ->
                                errorMessage = if (isPersianRtl) "خطا در استخراج فایل صوتی: ${err.message}" else "Extraction error: ${err.message}"
                            }
                        }
                    },
                    enabled = !isExtracting && video.isDurationValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_extract_mp3_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themePalette.primary,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    val isFull = trimStartSec <= 0.2f && trimEndSec >= (videoDurSec - 0.2f)
                    Text(
                        text = when {
                            isExtracting -> strings.extractingAudio
                            isFull -> if (isPersianRtl) "استخراج کل فایل صوتی (${formatSec(videoDurSec)})" else "Extract Full Audio (${formatSec(videoDurSec)})"
                            else -> if (isPersianRtl) "استخراج بازه انتخابی (${formatSec(trimStartSec)} تا ${formatSec(trimEndSec)})" else "Extract Selection (${formatSec(trimStartSec)} - ${formatSec(trimEndSec)})"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }

            // Success Results Cards
            if (extractedResultFiles.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    border = BorderStroke(1.2.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            val targetLaneName = when {
                                selectedTargetLaneId == -1L -> if (isPersianRtl) "لاین جدید" else "New Lane"
                                else -> lanes.find { it.laneId == selectedTargetLaneId }?.name ?: (if (isPersianRtl) "لاین انتخابی" else "Selected Lane")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPersianRtl) "استخراج انجام شد و در «$targetLaneName» قرار گرفت!" else "Extracted and placed into $targetLaneName!",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = if (isPersianRtl) "صوت بلافاصله در لاین آماده پخش است" else "Audio is ready in the lane",
                                    style = MaterialTheme.typography.bodySmall.copy(color = themePalette.textSecondary, fontSize = 11.sp)
                                )
                            }
                            TextButton(
                                onClick = onSwitchToPlayer,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isPersianRtl) "مشاهده پلیر" else "View Player",
                                    color = themePalette.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Target Lane Assignment Section
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = themePalette.cardBackground.copy(alpha = 0.75f),
                            border = BorderStroke(1.dp, themePalette.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TargetLaneSelectionSection(
                                lanes = lanes,
                                selectedTargetLaneId = selectedTargetLaneId,
                                onSelectTargetLane = { selectedTargetLaneId = it },
                                isPersianRtl = isPersianRtl,
                                themePalette = themePalette,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        extractedResultFiles.forEachIndexed { index, file ->
                            val fileUri = try {
                                FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                            } catch (_: Exception) {
                                Uri.fromFile(file)
                            }
                            val trackTitle = file.nameWithoutExtension

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = themePalette.cardBackground,
                                border = BorderStroke(0.8.dp, themePalette.cardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "${index + 1}. ${file.name}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = themePalette.textPrimary
                                        )
                                    )
                                    Text(
                                        text = "${if (isPersianRtl) "حجم: " else "Size: "}${String.format(Locale.US, "%.2f", file.length() / (1024f * 1024f))} MB",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = themePalette.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val targetLaneDisplayName = when {
                                        selectedTargetLaneId == -1L -> if (isPersianRtl) "لاین جدید" else "New Lane"
                                        else -> lanes.find { it.laneId == selectedTargetLaneId }?.name ?: (if (isPersianRtl) "لاین صوتی" else "Audio Lane")
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Play directly in Toot Player
                                        Button(
                                            onClick = {
                                                onPlayExtractedFile(selectedTargetLaneId, fileUri, trackTitle)
                                                onDismiss()
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF10B981),
                                                contentColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isPersianRtl) "پخش در «$targetLaneDisplayName»" else "Play in $targetLaneDisplayName",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Add to lane
                                        Button(
                                            onClick = {
                                                val targetId = selectedTargetLaneId
                                                onAddExtractedToLane(targetId, fileUri, trackTitle)
                                                val msg = if (targetId == -1L) {
                                                    if (isPersianRtl) "لاین صوتی جدید با موفقیت ایجاد و فایل افزوده شد" else "New audio lane created and audio added"
                                                } else {
                                                    if (isPersianRtl) "فایل با موفقیت به «$targetLaneDisplayName» افزوده شد" else "Added to $targetLaneDisplayName"
                                                }
                                                lastAddedTrackNotice = targetLaneDisplayName
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = themePalette.primary,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isPersianRtl) "افزودن به «$targetLaneDisplayName»" else "Add to $targetLaneDisplayName",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Share
                                        IconButton(
                                            onClick = {
                                                try {
                                                    val shareUri = try {
                                                        FileProvider.getUriForFile(
                                                            context,
                                                            "${context.packageName}.fileprovider",
                                                            file
                                                        )
                                                    } catch (_: Exception) {
                                                        fileUri
                                                    }
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "audio/*"
                                                        putExtra(Intent.EXTRA_STREAM, shareUri)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(
                                                        Intent.createChooser(
                                                            shareIntent,
                                                            if (isPersianRtl) "اشتراک فایل صوتی" else "Share audio file"
                                                        )
                                                    )
                                                } catch (e: Exception) {
                                                    Toast.makeText(
                                                        context,
                                                        if (isPersianRtl) "خطا در اشتراک‌گذاری: ${e.message}" else "Error sharing: ${e.message}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Share", tint = themePalette.textSecondary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        if (lastAddedTrackNotice != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = themePalette.primary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, themePalette.primary.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
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
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = themePalette.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isPersianRtl) "به «$lastAddedTrackNotice» اضافه شد" else "Added to $lastAddedTrackNotice",
                                            color = themePalette.textPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    TextButton(
                                        onClick = { onSwitchToPlayer() }
                                    ) {
                                        Text(
                                            text = if (isPersianRtl) "انتقال به پلیر" else "Go to Player",
                                            color = themePalette.primary,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onSwitchToPlayer()
                                },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, themePalette.primary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = themePalette.primary
                                )
                            ) {
                                Icon(Icons.Default.QueueMusic, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPersianRtl) "مشاهده در پلیر صوتی" else "View in Audio Player",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = {
                                    selectedVideoInfo = null
                                    extractedResultFiles = emptyList()
                                    errorMessage = null
                                    launchVideoChooser()
                                },
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = themePalette.textSecondary
                                )
                            ) {
                                Text(
                                    text = if (isPersianRtl) "ویدیوی دیگر" else "Another video",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        } // Close else branch of selectedVideoInfo

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun TargetLaneSelectionSection(
    lanes: List<AudioLaneState>,
    selectedTargetLaneId: Long,
    onSelectTargetLane: (Long) -> Unit,
    isPersianRtl: Boolean,
    themePalette: TootThemePalette,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = themePalette.primary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = if (isPersianRtl) "لاین صوتی هدف برای اختصاص فایل:" else "Target Audio Lane Assignment:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = themePalette.textPrimary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            // Option 1: New Lane (+ لاین جدید)
            FilterChip(
                selected = selectedTargetLaneId == -1L,
                onClick = { onSelectTargetLane(-1L) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                },
                label = {
                    Text(
                        text = if (isPersianRtl) "+ لاین جدید" else "+ New Lane",
                        fontSize = 11.sp,
                        fontWeight = if (selectedTargetLaneId == -1L) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = themePalette.primary,
                    selectedLabelColor = Color.Black,
                    selectedLeadingIconColor = Color.Black,
                    containerColor = themePalette.cardBackground.copy(alpha = 0.5f),
                    labelColor = themePalette.textPrimary
                )
            )

            // Option 2: Existing lanes
            lanes.forEach { lane ->
                FilterChip(
                    selected = selectedTargetLaneId == lane.laneId,
                    onClick = { onSelectTargetLane(lane.laneId) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                    },
                    label = {
                        Text(
                            text = lane.name,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTargetLaneId == lane.laneId) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = themePalette.primary,
                        selectedLabelColor = Color.Black,
                        selectedLeadingIconColor = Color.Black,
                        containerColor = themePalette.cardBackground.copy(alpha = 0.5f),
                        labelColor = themePalette.textPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        val currentTargetName = when {
            selectedTargetLaneId == -1L -> if (isPersianRtl) "یک لاین جدید و مستقل" else "a new independent lane"
            else -> lanes.find { it.laneId == selectedTargetLaneId }?.name?.let { "«$it»" } ?: (if (isPersianRtl) "لاین انتخابی" else "selected lane")
        }
        Text(
            text = if (selectedTargetLaneId == -1L) {
                if (isPersianRtl) "✔ صوت در لاین جدید مستقل قرار خواهد گرفت." else "✔ Audio will be assigned to a new independent lane."
            } else {
                if (isPersianRtl) "✔ صوت مستقیماً به $currentTargetName اختصاص می‌یابد." else "✔ Audio will be assigned directly to $currentTargetName."
            },
            fontSize = 10.5.sp,
            color = themePalette.primary.copy(alpha = 0.9f)
        )
    }
}
