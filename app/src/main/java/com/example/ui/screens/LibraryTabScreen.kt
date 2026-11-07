package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlin.math.absoluteValue
import com.example.audio.AudioLaneState
import com.example.data.model.DeviceAudioFile
import com.example.ui.components.AudioSortMode
import com.example.ui.theme.LocalAppThemePalette
import java.util.Locale

@Composable
fun LibraryTabScreen(
    deviceAudioFiles: List<DeviceAudioFile>,
    isScanning: Boolean,
    onRescan: () -> Unit,
    lanes: List<AudioLaneState>,
    onAddFilesToLane: (Long, List<DeviceAudioFile>) -> Unit,
    onImportFiles: (List<Uri>) -> Unit = {},
    onDeleteTrack: (Long) -> Unit = {},
    onPlayPreview: (Uri) -> Unit,
    onStopPreview: () -> Unit = {},
    previewPlayingUri: Uri?,
    mainSelectedLaneId: Long? = null,
    isPersianRtl: Boolean = false,
    isAiSearching: Boolean = false,
    aiSearchMatchedIds: Set<Long>? = null,
    aiSearchActiveQuery: String? = null,
    aiSearchImageUri: Uri? = null,
    onSearchWithAi: (String) -> Unit = {},
    onSearchWithImage: (Uri) -> Unit = {},
    onClearAiSearch: () -> Unit = {},
    aiMusicSearchService: com.example.ai.AiMusicSearchService? = null,
    modifier: Modifier = Modifier
) {
    val themePalette = LocalAppThemePalette.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    val requiredAudioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, requiredAudioPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            onRescan()
        }
    }

    // Auto-check and scan on first appearance
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, requiredAudioPermission) == PackageManager.PERMISSION_GRANTED
        hasAudioPermission = granted
        if (granted) {
            onRescan()
        } else {
            // Auto prompt for permission to list all audio files right away
            permissionLauncher.launch(requiredAudioPermission)
        }
    }

    val view = LocalView.current
    var isSearchFocused by remember { mutableStateOf(false) }
    var searchBoxBoundsInScreen by remember { mutableStateOf<Rect?>(null) }

    fun forceHideKeyboard() {
        keyboardController?.hide()
        val imm = view.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
        focusManager.clearFocus(force = true)
    }

    val dismissKeyboard: () -> Unit = {
        forceHideKeyboard()
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            forceHideKeyboard()
        }
    }

    val searchFocusRequester = remember { FocusRequester() }

    val nestedScrollConnection = remember {
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

    // Ensure keyboard is closed when library tab opens
    LaunchedEffect(Unit) {
        dismissKeyboard()
    }

    // Dismiss keyboard whenever scrolling the track list
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            dismissKeyboard()
        }
    }

    // Automatically stop preview playback whenever leaving or navigating away from the Library screen
    DisposableEffect(Unit) {
        onDispose {
            onStopPreview()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(AudioSortMode.NEWEST) }
    var showSortDropdown by remember { mutableStateOf(false) }
    val selectedFiles = remember { mutableStateListOf<DeviceAudioFile>() }

    // Target Session: Prioritize the main screen selected lane so it is placed FIRST in the horizontal list
    val defaultTargetId = mainSelectedLaneId ?: lanes.firstOrNull()?.laneId ?: 1L
    var selectedTargetLaneId by remember(lanes, mainSelectedLaneId) {
        mutableStateOf(if (lanes.any { it.laneId == mainSelectedLaneId }) mainSelectedLaneId!! else (lanes.firstOrNull()?.laneId ?: 1L))
    }

    val prioritizedLanes = remember(lanes, mainSelectedLaneId) {
        val target = lanes.find { it.laneId == mainSelectedLaneId } ?: lanes.firstOrNull()
        val rest = lanes.filter { it.laneId != (target?.laneId ?: -1L) }
        if (target != null) listOf(target) + rest else lanes
    }

    val effectiveTargetLane = lanes.find { it.laneId == selectedTargetLaneId } ?: lanes.firstOrNull()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onImportFiles(uris)
            val converted = uris.map { uri ->
                DeviceAudioFile(
                    id = uri.hashCode().toLong(),
                    uri = uri,
                    title = uri.lastPathSegment ?: "Audio Track",
                    artist = "External File",
                    album = "Imported",
                    durationMs = 0L,
                    sizeBytes = 0L,
                    dateAddedSec = System.currentTimeMillis() / 1000,
                    dateModifiedSec = System.currentTimeMillis() / 1000,
                    path = uri.path,
                    relativePath = "Audio",
                    displayName = uri.lastPathSegment ?: "Audio Track",
                    isFromDownloadFolder = false
                )
            }
            onAddFilesToLane(selectedTargetLaneId, converted)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onSearchWithImage(uri)
        }
    }

    val filteredFiles = remember(deviceAudioFiles, searchQuery, aiSearchMatchedIds, sortMode) {
        var list = when {
            aiSearchMatchedIds != null -> {
                val matched = deviceAudioFiles.filter { it.id in aiSearchMatchedIds }
                if (searchQuery.isNotBlank()) {
                    val q = searchQuery.trim().lowercase(Locale.getDefault())
                    matched.filter {
                        it.title.lowercase(Locale.getDefault()).contains(q) ||
                        (it.artist?.lowercase(Locale.getDefault())?.contains(q) == true) ||
                        it.displayName.lowercase(Locale.getDefault()).contains(q)
                    }
                } else {
                    matched
                }
            }
            searchQuery.isNotBlank() -> {
                val expandedTerms = aiMusicSearchService?.expandQueryTerms(searchQuery)
                    ?: listOf(searchQuery.trim().lowercase(Locale.getDefault()))
                val rawTrimmed = searchQuery.trim().lowercase(Locale.getDefault())

                deviceAudioFiles.filter { track ->
                    val titleLower = track.title.lowercase(Locale.getDefault())
                    val artistLower = (track.artist ?: "").lowercase(Locale.getDefault())
                    val displayLower = track.displayName.lowercase(Locale.getDefault())

                    // Exact query match or any expanded bilingual/mood term match
                    titleLower.contains(rawTrimmed) ||
                    artistLower.contains(rawTrimmed) ||
                    displayLower.contains(rawTrimmed) ||
                    expandedTerms.any { term ->
                        term.length >= 2 && (titleLower.contains(term) || artistLower.contains(term) || displayLower.contains(term))
                    }
                }
            }
            else -> deviceAudioFiles
        }

        when (sortMode) {
            AudioSortMode.NEWEST -> list.sortedByDescending { if (it.dateModifiedSec > 0) it.dateModifiedSec else it.dateAddedSec }
            AudioSortMode.TITLE -> list.sortedBy { it.title.lowercase(Locale.getDefault()) }
            AudioSortMode.ARTIST -> list.sortedBy { (it.artist ?: "").lowercase(Locale.getDefault()) }
            AudioSortMode.DURATION -> list.sortedByDescending { it.durationMs }
            AudioSortMode.SIZE -> list.sortedByDescending { it.sizeBytes }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val downChange = event.changes.firstOrNull { it.changedToDown() }
                            if (downChange != null) {
                                val touchPos = downChange.position
                                val rect = searchBoxBoundsInScreen
                                val inSearch = rect != null && rect.contains(touchPos)
                                if (!inSearch) {
                                    dismissKeyboard()
                                }
                            }
                        }
                    }
                }
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
                        text = if (isPersianRtl) "کتابخانه صوتی دستگاه" else "Audio Library",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = themePalette.textPrimary,
                            fontSize = 22.sp
                        )
                    )
                    Text(
                        text = if (isPersianRtl) "${filteredFiles.size} فایل صوتی پیدا شد" else "${filteredFiles.size} audio files found",
                        style = MaterialTheme.typography.bodySmall.copy(color = themePalette.textMuted)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRescan,
                        modifier = Modifier.testTag("rescan_library_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                color = themePalette.primary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Rescan",
                                tint = themePalette.primary
                            )
                        }
                    }

                    Button(
                        onClick = { filePickerLauncher.launch(arrayOf("audio/*")) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themePalette.primary,
                            contentColor = if (themePalette.isDarkMode) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPersianRtl) "فایل جدید" else "Open File",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Search Field with AI Intelligence (Bilingual, Mood, and Image Search)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (aiSearchMatchedIds != null && it.isBlank()) {
                        onClearAiSearch()
                    }
                },
                placeholder = {
                    Text(
                        text = if (isPersianRtl) "جستجوی هوشمند (فارسی، انگلیسی، حس‌و‌حال، خواننده)..." else "Smart search (EN, FA, mood, artist, track...)",
                        color = themePalette.textMuted,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Search",
                        tint = themePalette.primary,
                        modifier = Modifier.size(19.dp)
                    )
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        if (searchQuery.isNotEmpty() || aiSearchMatchedIds != null || aiSearchImageUri != null) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                    onClearAiSearch()
                                    dismissKeyboard()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = themePalette.textSecondary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        // AI Image search button (pick photo/screenshot of playlist/tracklist)
                        IconButton(
                            onClick = {
                                dismissKeyboard()
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("ai_image_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = if (isPersianRtl) "جستجوی هوشمند با عکس یا لیست آهنگ‌ها" else "Search tracks from image",
                                tint = if (aiSearchImageUri != null) themePalette.primary else themePalette.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // 1-tap deep AI search trigger if query is not empty
                        if (searchQuery.isNotBlank() && !isAiSearching) {
                            IconButton(
                                onClick = {
                                    dismissKeyboard()
                                    onSearchWithAi(searchQuery)
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("ai_deep_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = if (isPersianRtl) "جستجوی هوش مصنوعی" else "AI Search",
                                    tint = themePalette.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        // Manual keyboard hide button
                        IconButton(
                            onClick = { forceHideKeyboard() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("library_hide_keyboard_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardHide,
                                contentDescription = if (isPersianRtl) "بستن کیبورد" else "Hide Keyboard",
                                tint = themePalette.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        forceHideKeyboard()
                        if (searchQuery.isNotBlank()) {
                            onSearchWithAi(searchQuery)
                        }
                    },
                    onDone = { forceHideKeyboard() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .focusRequester(searchFocusRequester)
                    .onFocusChanged {
                        isSearchFocused = it.isFocused
                    }
                    .onGloballyPositioned { coordinates ->
                        searchBoxBoundsInScreen = coordinates.boundsInParent()
                    }
                    .testTag("library_search_field"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themePalette.primary,
                    unfocusedBorderColor = themePalette.cardBorder,
                    focusedContainerColor = themePalette.cardBackground,
                    unfocusedContainerColor = themePalette.cardBackground,
                    focusedTextColor = themePalette.textPrimary,
                    unfocusedTextColor = themePalette.textPrimary
                ),
                singleLine = true
            )

            // Non-intrusive status row if Image search is processing or active
            if (isAiSearching || aiSearchImageUri != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themePalette.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, themePalette.primary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("ai_search_status_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAiSearching) {
                                CircularProgressIndicator(
                                    color = themePalette.primary,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = themePalette.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isAiSearching && aiSearchImageUri != null ->
                                        if (isPersianRtl) "در حال اسکن تصویر و جستجوی هوشمند موزیک‌ها در گوشی..." else "Analyzing image & matching songs on phone..."
                                    isAiSearching ->
                                        if (isPersianRtl) "در حال جستجوی هوش مصنوعی..." else "Searching with AI..."
                                    aiSearchImageUri != null ->
                                        if (isPersianRtl) "موزیک‌های پیدا شده از روی تصویر (${filteredFiles.size} آهنگ)" else "Found from image (${filteredFiles.size} songs)"
                                    else ->
                                        if (isPersianRtl) "نتایج هوش مصنوعی (${filteredFiles.size} آهنگ)" else "AI results (${filteredFiles.size} tracks)"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = themePalette.textPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onClearAiSearch() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = themePalette.textSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Target Session Selector Row (Lanes side by side, Main lane first)
            if (prioritizedLanes.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = themePalette.cardBackground,
                    border = BorderStroke(1.dp, themePalette.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isPersianRtl) "افزودن فایل‌ها به لاین مقصد:" else "Target Audio Lane:",
                                style = MaterialTheme.typography.labelSmall.copy(color = themePalette.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            )
                            effectiveTargetLane?.let {
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.labelSmall.copy(color = themePalette.primary, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            prioritizedLanes.forEachIndexed { index, lane ->
                                val isSelected = lane.laneId == selectedTargetLaneId
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) themePalette.primary.copy(alpha = 0.22f) else themePalette.colorSurfaceSecondary.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, if (isSelected) themePalette.primary else themePalette.cardBorder),
                                    modifier = Modifier
                                        .clickable {
                                            dismissKeyboard()
                                            selectedTargetLaneId = lane.laneId
                                        }
                                        .testTag("select_library_target_lane_${lane.laneId}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (index == 0 && lane.laneId == (mainSelectedLaneId ?: prioritizedLanes.firstOrNull()?.laneId)) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = themePalette.primary,
                                                modifier = Modifier.padding(end = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (isPersianRtl) "اصلی" else "MAIN",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (themePalette.isDarkMode) Color.Black else Color.White,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 8.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Audiotrack,
                                            contentDescription = null,
                                            tint = if (isSelected) themePalette.primary else themePalette.textMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = lane.name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) themePalette.textPrimary else themePalette.textSecondary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Batch selection control & SORT Dropdown Menu bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isPersianRtl) "${filteredFiles.size} فایل" else "${filteredFiles.size} tracks",
                        style = MaterialTheme.typography.labelSmall.copy(color = themePalette.textSecondary, fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Select All / Deselect All
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = themePalette.cardBackground,
                        border = BorderStroke(0.8.dp, themePalette.cardBorder),
                        modifier = Modifier
                            .clickable {
                                dismissKeyboard()
                                if (selectedFiles.size == filteredFiles.size && filteredFiles.isNotEmpty()) {
                                    selectedFiles.clear()
                                } else {
                                    selectedFiles.clear()
                                    selectedFiles.addAll(filteredFiles)
                                }
                            }
                            .testTag("library_toggle_select_all")
                    ) {
                        Text(
                            text = if (selectedFiles.size == filteredFiles.size && filteredFiles.isNotEmpty()) {
                                if (isPersianRtl) "لغو انتخاب" else "Deselect All"
                            } else {
                                if (isPersianRtl) "انتخاب همه" else "Select All"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = themePalette.primary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // SORT Dropdown Menu
                Box {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = themePalette.cardBackground,
                        border = BorderStroke(0.8.dp, themePalette.cardBorder),
                        modifier = Modifier
                            .clickable {
                                dismissKeyboard()
                                showSortDropdown = true
                            }
                            .testTag("library_sort_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = themePalette.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = (if (isPersianRtl) "مرتب‌سازی: " else "Sort: ") + when (sortMode) {
                                    AudioSortMode.NEWEST -> if (isPersianRtl) "جدیدترین" else "Newest"
                                    AudioSortMode.TITLE -> if (isPersianRtl) "نام A-Z" else "Title A-Z"
                                    AudioSortMode.ARTIST -> if (isPersianRtl) "خواننده" else "Artist"
                                    AudioSortMode.DURATION -> if (isPersianRtl) "مدت زمان" else "Duration"
                                    AudioSortMode.SIZE -> if (isPersianRtl) "حجم" else "Size"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = themePalette.textPrimary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSortDropdown,
                        onDismissRequest = { showSortDropdown = false },
                        modifier = Modifier.background(themePalette.cardBackground)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isPersianRtl) "🆕 جدیدترین فایل‌ها (تاریخ)" else "🆕 Newest First",
                                    color = if (sortMode == AudioSortMode.NEWEST) themePalette.primary else themePalette.textPrimary,
                                    fontWeight = if (sortMode == AudioSortMode.NEWEST) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                dismissKeyboard()
                                sortMode = AudioSortMode.NEWEST
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isPersianRtl) "🔤 نام آهنگ (الفبا A-Z)" else "🔤 Title (A to Z)",
                                    color = if (sortMode == AudioSortMode.TITLE) themePalette.primary else themePalette.textPrimary,
                                    fontWeight = if (sortMode == AudioSortMode.TITLE) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                dismissKeyboard()
                                sortMode = AudioSortMode.TITLE
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isPersianRtl) "🎤 نام خواننده / هنرمند" else "🎤 Artist Name",
                                    color = if (sortMode == AudioSortMode.ARTIST) themePalette.primary else themePalette.textPrimary,
                                    fontWeight = if (sortMode == AudioSortMode.ARTIST) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                dismissKeyboard()
                                sortMode = AudioSortMode.ARTIST
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isPersianRtl) "⏱️ طولانی‌ترین به کوتاه‌ترین" else "⏱️ Duration (Longest first)",
                                    color = if (sortMode == AudioSortMode.DURATION) themePalette.primary else themePalette.textPrimary,
                                    fontWeight = if (sortMode == AudioSortMode.DURATION) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                dismissKeyboard()
                                sortMode = AudioSortMode.DURATION
                                showSortDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isPersianRtl) "💾 بیشترین حجم فایل" else "💾 File Size (Largest first)",
                                    color = if (sortMode == AudioSortMode.SIZE) themePalette.primary else themePalette.textPrimary,
                                    fontWeight = if (sortMode == AudioSortMode.SIZE) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                dismissKeyboard()
                                sortMode = AudioSortMode.SIZE
                                showSortDropdown = false
                            }
                        )
                    }
                }
            }

            // List of Files
            if (filteredFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (!hasAudioPermission) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = themePalette.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isPersianRtl) "دسترسی به فایل‌های صوتی دستگاه لازم است" else "Audio permission is required to list tracks",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = themePalette.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isPersianRtl) "برای نمایش خودکار تمام موزیک‌های گوشی، دسترسی را تأیید کنید" else "Grant access to discover and play your device music",
                                style = MaterialTheme.typography.bodySmall.copy(color = themePalette.textMuted)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { permissionLauncher.launch(requiredAudioPermission) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themePalette.primary,
                                    contentColor = if (themePalette.isDarkMode) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isPersianRtl) "اعطای دسترسی و اسکن" else "Grant Permission & Scan",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = themePalette.textMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isPersianRtl) "فایل صوتی یافت نشد" else "No audio files found",
                                style = MaterialTheme.typography.bodyMedium.copy(color = themePalette.textMuted)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .nestedScroll(nestedScrollConnection),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = if (selectedFiles.isNotEmpty()) 80.dp else 16.dp)
                ) {
                    items(filteredFiles, key = { it.id }) { file ->
                        val isPreviewing = previewPlayingUri == file.uri
                        val isSelected = selectedFiles.any { it.id == file.id }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) themePalette.primary.copy(alpha = 0.15f) else if (isPreviewing) themePalette.primary.copy(alpha = 0.08f) else themePalette.cardBackground,
                            border = BorderStroke(1.dp, if (isSelected) themePalette.primary else if (isPreviewing) themePalette.primary.copy(alpha = 0.6f) else themePalette.cardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    dismissKeyboard()
                                    if (isSelected) {
                                        selectedFiles.removeAll { it.id == file.id }
                                    } else {
                                        selectedFiles.add(file)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Multi-select Checkbox
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) themePalette.primary else themePalette.textMuted,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                // Play / Preview Button
                                Surface(
                                    shape = CircleShape,
                                    color = if (isPreviewing) themePalette.primary else themePalette.cardBorder,
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clickable {
                                            dismissKeyboard()
                                            onPlayPreview(file.uri)
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isPreviewing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Preview",
                                            tint = if (isPreviewing) (if (themePalette.isDarkMode) Color.Black else Color.White) else themePalette.textPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) themePalette.primary else themePalette.textPrimary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${file.artist ?: (if (isPersianRtl) "نامشخص" else "Unknown")} • ${formatTimeMs(file.durationMs)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = themePalette.textMuted,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // 1-Tap Add to selected session
                                IconButton(
                                    onClick = {
                                        dismissKeyboard()
                                        onAddFilesToLane(selectedTargetLaneId, listOf(file))
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = themePalette.secondary.copy(alpha = 0.18f),
                                        border = BorderStroke(0.6.dp, themePalette.secondary)
                                    ) {
                                        Box(modifier = Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add to session",
                                                tint = themePalette.secondary,
                                                modifier = Modifier.size(18.dp)
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

        // Floating Action Button for Batch Adding Selected Tracks
        AnimatedVisibility(
            visible = selectedFiles.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = {
                    dismissKeyboard()
                    effectiveTargetLane?.let { lane ->
                        onAddFilesToLane(lane.laneId, selectedFiles.toList())
                        selectedFiles.clear()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = themePalette.primary,
                    contentColor = if (themePalette.isDarkMode) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("batch_add_library_files_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPersianRtl)
                            "افزودن ${selectedFiles.size} موزیک به (${effectiveTargetLane?.name ?: "لاین"})"
                        else
                            "Add ${selectedFiles.size} tracks to ${effectiveTargetLane?.name ?: "Lane"}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}

private fun formatTimeMs(ms: Long): String {
    if (ms <= 0) return "--:--"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

