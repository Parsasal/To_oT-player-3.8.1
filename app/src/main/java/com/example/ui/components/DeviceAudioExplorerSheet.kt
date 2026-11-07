package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.absoluteValue
import com.example.audio.AudioLaneState
import com.example.data.model.DeviceAudioFile
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBgStart
import com.example.ui.theme.EmeraldPlay
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

enum class AudioFilterCategory {
    ALL,
    DOWNLOADS,
    MUSIC,
    RECORDINGS
}

enum class AudioSortMode {
    NEWEST,
    TITLE,
    ARTIST,
    DURATION,
    SIZE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceAudioExplorerSheet(
    lanes: List<AudioLaneState>,
    selectedTargetLaneId: Long?,
    deviceAudioFiles: List<DeviceAudioFile>,
    isScanning: Boolean,
    previewPlayingUri: Uri?,
    onSelectTargetLane: (Long) -> Unit,
    onScanFiles: () -> Unit,
    onPlayPreview: (Uri) -> Unit,
    onStopPreview: () -> Unit,
    onAddFilesToLane: (Long, List<DeviceAudioFile>) -> Unit,
    onDismiss: () -> Unit,
    isPersianRtl: Boolean,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 3-Tier bulletproof keyboard dismissal (Compose Focus + WindowInsetsController + InputMethodManager)
    val forceHideKeyboard: () -> Unit = remember(view, keyboardController, focusManager) {
        {
            try {
                keyboardController?.hide()
            } catch (_: Exception) {}

            try {
                focusManager.clearFocus(force = true)
            } catch (_: Exception) {}

            try {
                val windowInsetsController = WindowCompat.getInsetsController(
                    (view.context as? android.app.Activity)?.window ?: return@remember,
                    view
                )
                windowInsetsController.hide(WindowInsetsCompat.Type.ime())
            } catch (_: Exception) {}

            try {
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
                view.rootView?.windowToken?.let { rootToken ->
                    imm?.hideSoftInputFromWindow(rootToken, 0)
                }
            } catch (_: Exception) {}
        }
    }

    // Permission checking
    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, requiredPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            onScanFiles()
        }
    }

    // Filters and search state
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AudioFilterCategory.ALL) }
    var sortMode by remember { mutableStateOf(AudioSortMode.NEWEST) }
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    // Multi-selection state
    val selectedFiles = remember { mutableStateListOf<DeviceAudioFile>() }

    // Resolved target lane
    val currentLaneId = selectedTargetLaneId ?: lanes.firstOrNull()?.laneId
    val currentLane = lanes.find { it.laneId == currentLaneId }

    // Filter and sort audio files
    val filteredFiles by remember(deviceAudioFiles, searchQuery, selectedCategory, sortMode) {
        derivedStateOf {
            deviceAudioFiles
                .filter { file ->
                    // Category filter
                    val matchesCategory = when (selectedCategory) {
                        AudioFilterCategory.ALL -> true
                        AudioFilterCategory.DOWNLOADS -> file.isFromDownloadFolder || (file.relativePath?.contains("download", ignoreCase = true) == true)
                        AudioFilterCategory.MUSIC -> file.relativePath?.contains("music", ignoreCase = true) == true || !file.isFromDownloadFolder
                        AudioFilterCategory.RECORDINGS -> file.relativePath?.contains("recording", ignoreCase = true) == true ||
                                file.title.contains("record", ignoreCase = true) ||
                                file.title.contains("voice", ignoreCase = true)
                    }

                    // Query filter
                    val matchesQuery = if (searchQuery.isBlank()) {
                        true
                    } else {
                        val query = searchQuery.trim().lowercase()
                        file.title.lowercase().contains(query) ||
                                (file.artist?.lowercase()?.contains(query) == true) ||
                                (file.album?.lowercase()?.contains(query) == true) ||
                                file.displayName.lowercase().contains(query)
                    }

                    matchesCategory && matchesQuery
                }
                .let { list ->
                    when (sortMode) {
                        AudioSortMode.NEWEST -> list.sortedByDescending { if (it.dateModifiedSec > 0) it.dateModifiedSec else it.dateAddedSec }
                        AudioSortMode.TITLE -> list.sortedBy { it.title.lowercase() }
                        AudioSortMode.ARTIST -> list.sortedBy { it.artist?.lowercase() ?: "" }
                        AudioSortMode.DURATION -> list.sortedByDescending { it.durationMs }
                        AudioSortMode.SIZE -> list.sortedByDescending { it.sizeBytes }
                    }
                }
        }
    }

    val listState = rememberLazyListState()

    // Nested scroll connection: dismisses keyboard immediately on any drag or scroll
    val nestedScrollConnection = remember(forceHideKeyboard) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y.absoluteValue > 0.5f || available.x.absoluteValue > 0.5f) {
                    forceHideKeyboard()
                }
                return Offset.Zero
            }
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (consumed.y.absoluteValue > 0.5f || consumed.x.absoluteValue > 0.5f) {
                    forceHideKeyboard()
                }
                return Offset.Zero
            }
        }
    }

    // Dismiss keyboard instantly as soon as list scroll starts
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            forceHideKeyboard()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            forceHideKeyboard()
            onStopPreview()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = DarkBgStart,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = null,
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .nestedScroll(nestedScrollConnection)
            .testTag("device_audio_explorer_sheet")
    ) {
        val sheetView = LocalView.current
        val sheetFocusManager = LocalFocusManager.current
        val sheetKeyboardController = LocalSoftwareKeyboardController.current

        val dismissFromInsideSheet: () -> Unit = remember(sheetView, sheetKeyboardController, sheetFocusManager) {
            {
                forceHideKeyboard()
                try {
                    sheetKeyboardController?.hide()
                    sheetFocusManager.clearFocus(force = true)
                    val imm = sheetView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(sheetView.windowToken, 0)
                    sheetView.rootView?.windowToken?.let { token ->
                        imm?.hideSoftInputFromWindow(token, 0)
                    }
                } catch (_: Exception) {}
            }
        }

        // Root container with unconsumed pointer interception for instant tap-outside keyboard dismissal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.changes.any { it.changedToDown() }) {
                                dismissFromInsideSheet()
                            }
                        }
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header Bar (Title, Rescan, Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CyanAccent.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (isPersianRtl) "صداها و دانلودهای دستگاه" else "Device Audio & Downloads",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isPersianRtl) {
                                    "${filteredFiles.size} فایل صوتی پیدا شد"
                                } else {
                                    "${filteredFiles.size} audio tracks found"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                forceHideKeyboard()
                                onScanFiles()
                            },
                            enabled = !isScanning,
                            modifier = Modifier.testTag("rescan_audio_button")
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = CyanAccent
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = if (isPersianRtl) "اسکن مجدد" else "Rescan",
                                    tint = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                forceHideKeyboard()
                                onStopPreview()
                                onDismiss()
                            },
                            modifier = Modifier.testTag("close_audio_sheet_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isPersianRtl) "بستن" else "Close",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Session Lane Selector Chips
                if (lanes.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isPersianRtl) "افزودن به لاین فعال:" else "Add to Lane:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            lanes.forEach { lane ->
                                val isSelected = lane.laneId == currentLaneId
                                val laneColor = lane.colorHex?.let {
                                    try {
                                        Color(android.graphics.Color.parseColor(it))
                                    } catch (_: Exception) {
                                        CyanAccent
                                    }
                                } ?: CyanAccent

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) laneColor.copy(alpha = 0.22f) else GlassSurfaceElevated,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) laneColor else GlassBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            forceHideKeyboard()
                                            onSelectTargetLane(lane.laneId)
                                        }
                                        .testTag("target_lane_chip_${lane.laneId}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(laneColor, CircleShape)
                                        )
                                        Text(
                                            text = lane.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) TextPrimary else TextSecondary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (lane.playlist.isNotEmpty()) {
                                            Text(
                                                text = "(${lane.playlist.size})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Search Bar with explicit keyboard dismiss actions
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (isPersianRtl) "جستجوی فایل صوتی، نام خواننده یا پوشه..." else "Search tracks, artists, downloads...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = CyanAccent
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { forceHideKeyboard() },
                                modifier = Modifier.testTag("dismiss_keyboard_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardHide,
                                    contentDescription = if (isPersianRtl) "بستن صفحه کلید" else "Dismiss Keyboard",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { forceHideKeyboard() },
                        onDone = { forceHideKeyboard() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GlassSurfaceElevated,
                        unfocusedContainerColor = GlassSurfaceElevated,
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audio_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Categories & Sort Options Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AudioFilterCategory.values().forEach { category ->
                            val isSelected = selectedCategory == category
                            val label = when (category) {
                                AudioFilterCategory.ALL -> if (isPersianRtl) "همه" else "All"
                                AudioFilterCategory.DOWNLOADS -> if (isPersianRtl) "دانلودها" else "Downloads"
                                AudioFilterCategory.MUSIC -> if (isPersianRtl) "موزیک" else "Music"
                                AudioFilterCategory.RECORDINGS -> if (isPersianRtl) "ضبط‌ها" else "Recordings"
                            }
                            val icon = when (category) {
                                AudioFilterCategory.ALL -> Icons.AutoMirrored.Filled.QueueMusic
                                AudioFilterCategory.DOWNLOADS -> Icons.Default.Download
                                AudioFilterCategory.MUSIC -> Icons.Default.MusicNote
                                AudioFilterCategory.RECORDINGS -> Icons.Default.Mic
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    forceHideKeyboard()
                                    selectedCategory = category
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VioletAccent.copy(alpha = 0.25f),
                                    selectedLabelColor = TextPrimary,
                                    selectedLeadingIconColor = VioletAccent,
                                    containerColor = GlassSurfaceElevated,
                                    labelColor = TextSecondary,
                                    iconColor = TextMuted
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) VioletAccent else GlassBorder
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Sort menu button
                    Box {
                        IconButton(
                            onClick = {
                                forceHideKeyboard()
                                isSortMenuExpanded = true
                            },
                            modifier = Modifier
                                .background(GlassSurfaceElevated, RoundedCornerShape(10.dp))
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort",
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isSortMenuExpanded,
                            onDismissRequest = { isSortMenuExpanded = false },
                            modifier = Modifier.background(DarkBgStart)
                        ) {
                            AudioSortMode.values().forEach { mode ->
                                val modeLabel = when (mode) {
                                    AudioSortMode.NEWEST -> if (isPersianRtl) "جدیدترین" else "Newest"
                                    AudioSortMode.TITLE -> if (isPersianRtl) "نام فایل" else "Title"
                                    AudioSortMode.ARTIST -> if (isPersianRtl) "هنرمند" else "Artist"
                                    AudioSortMode.DURATION -> if (isPersianRtl) "مدت زمان" else "Duration"
                                    AudioSortMode.SIZE -> if (isPersianRtl) "حجم فایل" else "Size"
                                }
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = modeLabel,
                                            color = if (sortMode == mode) CyanAccent else TextPrimary,
                                            fontWeight = if (sortMode == mode) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        sortMode = mode
                                        isSortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Content List or Permission / Empty state
                if (!hasPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isPersianRtl) "نیاز به مجوز دسترسی به فایل‌های صوتی" else "Audio Permission Required",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isPersianRtl) {
                                    "برای نمایش و پخش موزیک‌ها و فایل‌های دانلود شده نیاز به مجوز است."
                                } else {
                                    "Grant permission to explore and load songs and voice notes from your storage."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { permissionLauncher.launch(requiredPermission) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isPersianRtl) "اعطای مجوز" else "Grant Permission",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else if (filteredFiles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isPersianRtl) "هیچ فایل صوتی پیدا نشد" else "No Audio Files Found",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (searchQuery.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isPersianRtl) "برای عبارت «$searchQuery» نتیجه‌ای یافت نشد" else "No match for \"$searchQuery\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { onScanFiles() },
                                border = BorderStroke(1.dp, CyanAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isPersianRtl) "اسکن مجدد حافظه" else "Rescan Storage",
                                    color = CyanAccent
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .nestedScroll(nestedScrollConnection)
                            .testTag("device_audio_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = if (selectedFiles.isNotEmpty()) 80.dp else 16.dp)
                    ) {
                        items(
                            items = filteredFiles,
                            key = { it.id }
                        ) { file ->
                            val isPreviewPlaying = previewPlayingUri == file.uri
                            val isSelected = selectedFiles.any { it.id == file.id }

                            AudioFileListItem(
                                file = file,
                                isPreviewPlaying = isPreviewPlaying,
                                isSelected = isSelected,
                                onToggleSelect = {
                                    forceHideKeyboard()
                                    if (isSelected) {
                                        selectedFiles.removeAll { it.id == file.id }
                                    } else {
                                        selectedFiles.add(file)
                                    }
                                },
                                onPlayPreview = {
                                    forceHideKeyboard()
                                    if (isPreviewPlaying) {
                                        onStopPreview()
                                    } else {
                                        onPlayPreview(file.uri)
                                    }
                                },
                                onQuickAdd = {
                                    forceHideKeyboard()
                                    if (currentLaneId != null) {
                                        onAddFilesToLane(currentLaneId, listOf(file))
                                    }
                                },
                                isPersianRtl = isPersianRtl
                            )
                        }
                    }
                }
            }

            // Floating Bottom Multi-Select Bar
            if (selectedFiles.isNotEmpty()) {
                Surface(
                    color = DarkBgStart.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isPersianRtl) {
                                    "${selectedFiles.size} فایل انتخاب شد"
                                } else {
                                    "${selectedFiles.size} tracks selected"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (selectedFiles.size == filteredFiles.size) {
                                        if (isPersianRtl) "لغو انتخاب همه" else "Deselect All"
                                    } else {
                                        if (isPersianRtl) "انتخاب همه" else "Select All"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanAccent,
                                    modifier = Modifier.clickable {
                                        forceHideKeyboard()
                                        if (selectedFiles.size == filteredFiles.size) {
                                            selectedFiles.clear()
                                        } else {
                                            selectedFiles.clear()
                                            selectedFiles.addAll(filteredFiles)
                                        }
                                    }
                                )
                                Text("•", color = TextMuted, fontSize = 12.sp)
                                Text(
                                    text = if (isPersianRtl) "پاک کردن" else "Clear",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    modifier = Modifier.clickable {
                                        forceHideKeyboard()
                                        selectedFiles.clear()
                                    }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                forceHideKeyboard()
                                if (currentLaneId != null && selectedFiles.isNotEmpty()) {
                                    onAddFilesToLane(currentLaneId, selectedFiles.toList())
                                    selectedFiles.clear()
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAccent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_selected_tracks_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPersianRtl) {
                                    "افزودن به ${currentLane?.name ?: "لاین"}"
                                } else {
                                    "Add to ${currentLane?.name ?: "Lane"}"
                                },
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioFileListItem(
    file: DeviceAudioFile,
    isPreviewPlaying: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onPlayPreview: () -> Unit,
    onQuickAdd: () -> Unit,
    isPersianRtl: Boolean
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) CyanAccent else GlassBorder,
        label = "border_color"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) CyanAccent.copy(alpha = 0.12f) else GlassSurfaceElevated,
        label = "bg_color"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggleSelect() }
            .testTag("audio_file_item_${file.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play / Pause Preview Button
            Surface(
                shape = CircleShape,
                color = if (isPreviewPlaying) EmeraldPlay else GlassSurfaceElevated,
                border = BorderStroke(
                    1.dp,
                    if (isPreviewPlaying) EmeraldPlay else CyanAccent.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable { onPlayPreview() }
                    .testTag("play_preview_button_${file.id}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPreviewPlaying) "Pause" else "Play",
                        tint = if (isPreviewPlaying) Color.Black else CyanAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title, Subtitle, and Metadata
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = file.artist ?: if (isPersianRtl) "نامشخص" else "Unknown",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Text("•", color = TextMuted, fontSize = 10.sp)

                    Text(
                        text = file.formattedDuration,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    if (file.formattedSize.isNotEmpty()) {
                        Text("•", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = file.formattedSize,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    if (file.isFromDownloadFolder) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyanAccent.copy(alpha = 0.15f),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = if (isPersianRtl) "دانلود" else "Download",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons: Quick Add (+) and Multi-Select Checkbox
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onQuickAdd() },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("quick_add_button_${file.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Quick Add",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { onToggleSelect() },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("select_checkbox_${file.id}")
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (isSelected) "Selected" else "Not selected",
                        tint = if (isSelected) CyanAccent else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
