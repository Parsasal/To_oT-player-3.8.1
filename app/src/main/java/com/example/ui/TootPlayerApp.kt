package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AboutDialog
import com.example.ui.components.AddLaneNameDialog
import com.example.ui.components.AudioOutputRoutingSheet
import com.example.ui.components.CrossfaderSheet
import com.example.ui.components.DeviceAudioExplorerSheet
import com.example.ui.components.EditLaneNameDialog
import com.example.ui.components.EqualizerSheet
import com.example.ui.components.HelpModal
import com.example.ui.components.HomescreenWidgetDialog
import com.example.ui.components.LaneColorPickerDialog
import com.example.ui.components.LanguageSelectorDialog
import com.example.ui.components.LaneDragDropState
import com.example.ui.components.OnboardingTour
import com.example.ui.components.PersistentMiniPlayer
import com.example.ui.components.PersistentSessionCard
import com.example.ui.components.PlaylistSheet
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.RecorderSheet
import com.example.ui.components.SleepTimerSheet
import com.example.ui.components.SystemStatusBarBoundary
import com.example.ui.components.ThemeSelectorSheet
import com.example.ui.components.TootHeaderLogo
import com.example.ui.components.VideoAudioExtractorSheet
import com.example.ui.components.rememberLaneDragDropState
import com.example.ui.screens.LibraryTabScreen
import com.example.ui.screens.PlaylistsTabScreen
import com.example.ui.screens.SettingsTabScreen
import com.example.ui.theme.LocalAppThemePalette
import com.example.ui.theme.TootPlayerTheme
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.LocalActiveLanguage
import com.example.util.LocalAppStrings

enum class MainNavigationTab {
    SESSIONS,
    LIBRARY,
    PLAYLISTS,
    SETTINGS
}

@Composable
fun TootPlayerApp(
    viewModel: TootPlayerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableIntStateOf(0) }
    var isAddLaneDialogOpen by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val mainNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y.absoluteValue > 0.5f || available.x.absoluteValue > 0.5f) {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                }
                return Offset.Zero
            }
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (consumed.y.absoluteValue > 0.5f || consumed.x.absoluteValue > 0.5f) {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                }
                return Offset.Zero
            }
        }
    }

    TootPlayerTheme(themeStyle = uiState.themeStyle, isDarkMode = uiState.isDarkMode) {
        val isAnyPlaying = uiState.lanes.any { it.isPlaying }

        val activeLanguage = uiState.activeLanguage
        val strings = AppStrings.get(activeLanguage)
        val layoutDirection = activeLanguage.layoutDirection

        val eqEnabled by viewModel.audioEngine.eqEnabled.collectAsStateWithLifecycle()
        val isRecording by viewModel.masterRecorder.isRecording.collectAsStateWithLifecycle()

        val crossfadeLeftId by viewModel.audioEngine.crossfadeLeftLaneId.collectAsStateWithLifecycle()
        val crossfadeRightId by viewModel.audioEngine.crossfadeRightLaneId.collectAsStateWithLifecycle()
        val crossfadePos by viewModel.audioEngine.crossfadePosition.collectAsStateWithLifecycle()
        val crossfadeDur by viewModel.audioEngine.crossfadeDurationSec.collectAsStateWithLifecycle()

        val sleepTimerSec by viewModel.audioEngine.sleepTimerRemainingSec.collectAsStateWithLifecycle()

        val isAudioRoutingSheetOpen by viewModel.isAudioRoutingSheetOpen.collectAsStateWithLifecycle()
        val availableAudioOutputs by viewModel.availableAudioOutputs.collectAsStateWithLifecycle()
        val selectedAudioOutput by viewModel.selectedAudioOutput.collectAsStateWithLifecycle()
        val isPhoneAudioScoMode by viewModel.isPhoneAudioScoMode.collectAsStateWithLifecycle()

        val selectedLane = uiState.lanes.find { it.laneId == uiState.selectedLaneId }
        val activePlayingLane = uiState.lanes.find { it.isPlaying && it.laneId == uiState.selectedLaneId }
            ?: uiState.lanes.find { it.isPlaying }
            ?: selectedLane
            ?: uiState.lanes.firstOrNull()

        val themePalette = LocalAppThemePalette.current
        val context = LocalContext.current

        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection,
            LocalAppStrings provides strings,
            LocalActiveLanguage provides activeLanguage
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        // Persistent Mini-Player (Floats directly above navigation bar)
                        activePlayingLane?.let { lane ->
                            PersistentMiniPlayer(
                                activeLane = lane,
                                onPlayPause = { viewModel.togglePlayPauseLane(lane.laneId) },
                                onPrevious = { viewModel.playPreviousTrack(lane.laneId) },
                                onNext = { viewModel.playNextTrack(lane.laneId) },
                                onSeek = { pos -> viewModel.seekLane(lane.laneId, pos) },
                                onOpenPlaylist = { viewModel.setPlaylistSheetLaneId(lane.laneId) },
                                onSpeedChange = { speed -> viewModel.setPlaybackSpeed(speed) },
                                onPlayAll = { viewModel.playAll() },
                                onPauseAll = { viewModel.pauseAll() },
                                onStopAll = { viewModel.stopAll() },
                                currentSpeed = uiState.playbackSpeed,
                                isPersianRtl = uiState.isPersianRtl
                            )
                        }

                        // Bottom Navigation Bar
                        NavigationBar(
                            containerColor = themePalette.bottomNavBackground,
                            tonalElevation = 8.dp,
                            modifier = Modifier.height(60.dp)
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { 
                                    if (currentTab != 0) {
                                        viewModel.stopPreviewAudio()
                                        currentTab = 0 
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Dashboard,
                                        contentDescription = strings.tabSessions,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = strings.tabSessions,
                                        fontSize = 10.sp,
                                        fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (uiState.isDarkMode) Color.Black else Color.White,
                                    selectedTextColor = themePalette.primary,
                                    indicatorColor = themePalette.bottomNavSelectedIndicator,
                                    unselectedIconColor = themePalette.textMuted,
                                    unselectedTextColor = themePalette.textMuted
                                ),
                                modifier = Modifier.testTag("nav_tab_sessions")
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { 
                                    if (currentTab != 1) {
                                        viewModel.stopPreviewAudio()
                                        currentTab = 1 
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = strings.tabLibrary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = strings.tabLibrary,
                                        fontSize = 10.sp,
                                        fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (uiState.isDarkMode) Color.Black else Color.White,
                                    selectedTextColor = themePalette.primary,
                                    indicatorColor = themePalette.bottomNavSelectedIndicator,
                                    unselectedIconColor = themePalette.textMuted,
                                    unselectedTextColor = themePalette.textMuted
                                ),
                                modifier = Modifier.testTag("nav_tab_library")
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { 
                                    if (currentTab != 2) {
                                        viewModel.stopPreviewAudio()
                                        currentTab = 2 
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = strings.tabPlaylists,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = strings.tabPlaylists,
                                        fontSize = 10.sp,
                                        fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (uiState.isDarkMode) Color.Black else Color.White,
                                    selectedTextColor = themePalette.primary,
                                    indicatorColor = themePalette.bottomNavSelectedIndicator,
                                    unselectedIconColor = themePalette.textMuted,
                                    unselectedTextColor = themePalette.textMuted
                                ),
                                modifier = Modifier.testTag("nav_tab_playlists")
                            )

                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { 
                                    if (currentTab != 3) {
                                        viewModel.stopPreviewAudio()
                                        currentTab = 3 
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = strings.tabSettings,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = strings.tabSettings,
                                        fontSize = 10.sp,
                                        fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (uiState.isDarkMode) Color.Black else Color.White,
                                    selectedTextColor = themePalette.primary,
                                    indicatorColor = themePalette.bottomNavSelectedIndicator,
                                    unselectedIconColor = themePalette.textMuted,
                                    unselectedTextColor = themePalette.textMuted
                                ),
                                modifier = Modifier.testTag("nav_tab_settings")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(themePalette.backgroundGradient)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    focusManager.clearFocus(force = true)
                                    keyboardController?.hide()
                                }
                            )
                        }
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // System Status Bar Area Scrim & Dedicated Boundary Line
                        SystemStatusBarBoundary(
                            themePalette = themePalette,
                            isDarkMode = uiState.isDarkMode
                        )

                        // Main Content Tabs Container
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (currentTab) {
                                0 -> {
                                    // SESSIONS TAB (Main Dashboard)
                                    val lanes = uiState.lanes

                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .nestedScroll(mainNestedScrollConnection)
                                            .padding(horizontal = 16.dp),
                                        contentPadding = PaddingValues(top = 10.dp, bottom = 18.dp),
                                        verticalArrangement = Arrangement.spacedBy(11.dp)
                                    ) {
                                        // Top Header Logo & Action Controls
                                        item {
                                            Column(
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                TootHeaderLogo(
                                                    onOpenThemeSelector = { viewModel.setThemeSelectorOpen(true) },
                                                    onOpenHomescreenWidget = null,
                                                    onAddNewSession = { isAddLaneDialogOpen = true },
                                                    onOpenHelpCenter = { viewModel.setHelpModalOpen(true) },
                                                    sleepTimerRemainingSec = sleepTimerSec,
                                                    onOpenSleepTimer = { viewModel.setSleepTimerOpen(true) },
                                                    onOpenAudioRouting = { viewModel.setAudioRoutingSheetOpen(true) },
                                                    isPhoneAudioScoActive = isPhoneAudioScoMode,
                                                    audioOutputType = selectedAudioOutput?.type
                                                )

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Subheading Row
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = strings.myAudioSessions,
                                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = themePalette.textPrimary,
                                                                fontSize = 22.sp,
                                                                letterSpacing = (-0.5).sp
                                                            )
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = "${lanes.size} ${strings.activeSessionsCount}",
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                color = themePalette.textMuted,
                                                                fontSize = 12.sp
                                                            )
                                                        )
                                                    }
                                                }


                                    }
                                }

                                // List of Persistent Session Cards with explicit reordering buttons
                                itemsIndexed(lanes, key = { _, lane -> lane.laneId }) { index, lane ->
                                    PersistentSessionCard(
                                        lane = lane,
                                        isSelected = lane.laneId == uiState.selectedLaneId,
                                        onSelect = { viewModel.selectLane(lane.laneId) },
                                        onResumeOrPlay = {
                                            viewModel.togglePlayPauseLane(lane.laneId)
                                        },
                                        onOpenPlaylist = { viewModel.setPlaylistSheetLaneId(lane.laneId) },
                                        onOpenDeviceAudioExplorer = { viewModel.openDeviceAudioExplorer(lane.laneId) },
                                        onRenameClick = { viewModel.setLaneToRename(lane) },
                                        onColorPickClick = { viewModel.setLaneToColorPick(lane) },
                                        onDeleteClick = { viewModel.deleteLane(lane.laneId) },
                                        onVolumeChange = { vol -> viewModel.setLaneVolume(lane.laneId, vol) },
                                        onToggleMute = { viewModel.toggleLaneMute(lane.laneId) },
                                        onToggleLoop = { viewModel.toggleLaneLoop(lane.laneId) },
                                        onTogglePlaylistLoop = { viewModel.toggleLanePlaylistLoop(lane.laneId) },
                                        onToggleShuffle = { viewModel.toggleLaneShuffle(lane.laneId) },
                                        onFileSelected = { uri -> viewModel.loadAudioForLane(lane.laneId, uri) },
                                        onMoveUp = { viewModel.moveLaneUp(lane.laneId) },
                                        onMoveDown = { viewModel.moveLaneDown(lane.laneId) },
                                        canMoveUp = index > 0,
                                        canMoveDown = index < lanes.size - 1,
                                        isPersianRtl = uiState.isPersianRtl,
                                        modifier = Modifier.animateItem()
                                    )
                                }

                                // Quick "+" Add Lane bottom card
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.Transparent,
                                        border = BorderStroke(1.dp, themePalette.primary.copy(alpha = 0.35f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isAddLaneDialogOpen = true }
                                            .testTag("bottom_add_session_card")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 11.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = strings.addLane,
                                                tint = themePalette.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // LIBRARY TAB
                            LibraryTabScreen(
                                deviceAudioFiles = uiState.deviceAudioFiles,
                                isScanning = uiState.isScanningDeviceAudio,
                                onRescan = { viewModel.scanDeviceAudioFiles() },
                                lanes = uiState.lanes,
                                onAddFilesToLane = { laneId, files -> viewModel.addDeviceAudioFilesToLane(laneId, files) },
                                onImportFiles = { uris -> viewModel.importFilesToLibrary(uris) },
                                onDeleteTrack = { id -> viewModel.deleteLibraryTrack(id) },
                                onPlayPreview = { viewModel.playPreviewAudio(it) },
                                onStopPreview = { viewModel.stopPreviewAudio() },
                                previewPlayingUri = uiState.previewPlayingUri,
                                mainSelectedLaneId = uiState.selectedLaneId,
                                isPersianRtl = uiState.isPersianRtl,
                                isAiSearching = uiState.isAiSearching,
                                aiSearchMatchedIds = uiState.aiSearchMatchedIds,
                                aiSearchActiveQuery = uiState.aiSearchActiveQuery,
                                aiSearchImageUri = uiState.aiSearchImageUri,
                                onSearchWithAi = { q -> viewModel.searchWithAi(q, uiState.deviceAudioFiles) },
                                onSearchWithImage = { uri -> viewModel.searchWithAiImage(uri, uiState.deviceAudioFiles) },
                                onClearAiSearch = { viewModel.clearAiSearch() },
                                aiMusicSearchService = viewModel.aiMusicSearchService
                            )
                        }

                        2 -> {
                            // PLAYLISTS TAB
                            PlaylistsTabScreen(
                                lanes = uiState.lanes,
                                onOpenSessionPlaylist = { viewModel.setPlaylistSheetLaneId(it) },
                                onPlayTrackInSession = { laneId, idx -> viewModel.playLanePlaylistItem(laneId, idx, autoStart = true) },
                                isPersianRtl = uiState.isPersianRtl
                            )
                        }

                        3 -> {
                            // SETTINGS & TOOLS TAB
                            SettingsTabScreen(
                                onOpenEqualizer = { viewModel.setEqualizerOpen(true) },
                                onOpenSleepTimer = { viewModel.setSleepTimerOpen(true) },
                                onOpenRecorder = { viewModel.setRecorderOpen(true) },
                                onOpenAudioExtractor = { viewModel.setVideoAudioExtractorOpen(true) },
                                onOpenAudioRouting = { viewModel.setAudioRoutingSheetOpen(true) },
                                onOpenThemeSelector = { viewModel.setThemeSelectorOpen(true) },
                                onOpenLanguageSelector = { viewModel.setLanguageSelectorOpen(true) },
                                onOpenHomescreenWidgetDialog = { viewModel.setHomescreenWidgetDialogOpen(true) },
                                onOpenAboutDialog = { viewModel.setAboutDialogOpen(true) },
                                onOpenPrivacyPolicy = { viewModel.setPrivacyPolicyOpen(true) },
                                onOpenHelpCenter = { viewModel.setHelpModalOpen(true) },
                                onReplayTutorial = { viewModel.replayTour() },
                                onTogglePersianRtl = { viewModel.togglePersianRtl() },
                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                onPlayAllLanes = { viewModel.playAll() },
                                onPauseAllLanes = { viewModel.pauseAll() },
                                onStopAllLanes = { viewModel.stopAll() },
                                onClearAllMutes = { viewModel.clearAllMutes() },
                                onClearAllSolos = { viewModel.clearAllSolos() },
                                isPersianRtl = uiState.isPersianRtl,
                                isDarkMode = uiState.isDarkMode,
                                currentThemeName = if (uiState.isPersianRtl) uiState.themeStyle.titleFa else uiState.themeStyle.titleEn,
                                eqEnabled = eqEnabled,
                                sleepTimerSec = sleepTimerSec ?: 0,
                                isRecording = isRecording,
                                autoResumeAfterCall = uiState.autoResumeAfterCall,
                                onToggleAutoResumeAfterCall = { viewModel.toggleAutoResumeAfterCall() },
                                activeAiProvider = uiState.activeAiProvider,
                                customGeminiApiKey = uiState.customGeminiApiKey,
                                customOpenAiApiKey = uiState.customOpenAiApiKey,
                                customDeepSeekApiKey = uiState.customDeepSeekApiKey,
                                customGroqApiKey = uiState.customGroqApiKey,
                                hasValidAiApiKey = uiState.hasValidAiApiKey,
                                onSelectAiProvider = { viewModel.selectAiProvider(it) },
                                onSaveAiProviderKey = { prov, key -> viewModel.saveAiProviderApiKey(prov, key) },
                                onClearAiProviderKey = { viewModel.clearAiProviderApiKey(it) },
                                customApiKey = uiState.customGeminiApiKey,
                                hasValidApiKey = uiState.hasValidGeminiApiKey,
                                onSaveCustomApiKey = { viewModel.saveCustomGeminiApiKey(it) },
                                onClearCustomApiKey = { viewModel.clearCustomGeminiApiKey() }
                            )
                        }
                    }
                }
            }
        }
    }

            // Language Selector Modal Dialog
            if (uiState.isLanguageSelectorOpen) {
                LanguageSelectorDialog(
                    currentLanguage = uiState.activeLanguage,
                    onSelectLanguage = { lang ->
                        viewModel.setLanguage(lang)
                    },
                    onDismiss = { viewModel.setLanguageSelectorOpen(false) }
                )
            }

            // Expandable Bottom Sheets & Dialogs
            uiState.playlistSheetLaneId?.let { laneId ->
                val lane = uiState.lanes.find { it.laneId == laneId }
                if (lane != null) {
                    PlaylistSheet(
                        lane = lane,
                        isPersianRtl = uiState.isPersianRtl,
                        onDismiss = { viewModel.setPlaylistSheetLaneId(null) },
                        onAddFiles = { uris -> viewModel.addAudioFilesToLanePlaylist(laneId, uris) },
                        onSelectTrack = { index -> viewModel.selectLanePlaylistItem(laneId, index) },
                        onPlayTrack = { index -> viewModel.playLanePlaylistItem(laneId, index, autoStart = true) },
                        onRemoveTrack = { itemId -> viewModel.removePlaylistItem(itemId) },
                        onClearAll = { viewModel.clearLanePlaylist(laneId) },
                        onToggleLoop = { viewModel.toggleLaneLoop(laneId) },
                        onTogglePlaylistLoop = { viewModel.toggleLanePlaylistLoop(laneId) },
                        onToggleShuffle = { viewModel.toggleLaneShuffle(laneId) },
                        onPlayPause = { viewModel.togglePlayPauseLane(laneId) },
                        onNext = { viewModel.playNextTrack(laneId) },
                        onPrev = { viewModel.playPreviousTrack(laneId) },
                        onSortPlaylist = { sortType -> viewModel.sortLanePlaylist(laneId, sortType) },
                        onOpenDeviceAudioExplorer = { viewModel.openDeviceAudioExplorer(laneId) }
                    )
                }
            }

            if (uiState.isDeviceAudioExplorerOpen) {
                DeviceAudioExplorerSheet(
                    lanes = uiState.lanes,
                    selectedTargetLaneId = uiState.deviceAudioTargetLaneId,
                    deviceAudioFiles = uiState.deviceAudioFiles,
                    isScanning = uiState.isScanningDeviceAudio,
                    previewPlayingUri = uiState.previewPlayingUri,
                    onSelectTargetLane = { viewModel.setDeviceAudioTargetLaneId(it) },
                    onScanFiles = { viewModel.scanDeviceAudioFiles() },
                    onPlayPreview = { viewModel.playPreviewAudio(it) },
                    onStopPreview = { viewModel.stopPreviewAudio() },
                    onAddFilesToLane = { laneId, files -> viewModel.addDeviceAudioFilesToLane(laneId, files) },
                    onDismiss = { viewModel.setDeviceAudioExplorerOpen(false) },
                    isPersianRtl = uiState.isPersianRtl
                )
            }

            if (uiState.isEqualizerOpen) {
                val eqEnabled by viewModel.audioEngine.eqEnabled.collectAsStateWithLifecycle()
                val eqBands by viewModel.audioEngine.eqBands.collectAsStateWithLifecycle()
                val eqPresetName by viewModel.audioEngine.eqPresetName.collectAsStateWithLifecycle()
                val visualizerFrequencies by viewModel.audioEngine.visualizerFrequencies.collectAsStateWithLifecycle()
                val laneFrequencies by viewModel.audioEngine.laneFrequencies.collectAsStateWithLifecycle()

                EqualizerSheet(
                    eqEnabled = eqEnabled,
                    eqBands = eqBands,
                    currentPresetName = eqPresetName,
                    presets = viewModel.audioEngine.eqPresets,
                    masterFrequencies = visualizerFrequencies,
                    laneFrequencies = laneFrequencies,
                    lanes = uiState.lanes,
                    isPlaying = isAnyPlaying,
                    onToggleEnabled = { viewModel.toggleEqualizer(it) },
                    onBandChange = { idx, gain -> viewModel.setEqBand(idx, gain) },
                    onSelectPreset = { viewModel.applyEqPreset(it) },
                    onDismiss = { viewModel.setEqualizerOpen(false) },
                    isPersianRtl = uiState.isPersianRtl
                )
            }

            if (uiState.isCrossfaderOpen) {
                CrossfaderSheet(
                    lanes = uiState.lanes,
                    leftLaneId = crossfadeLeftId,
                    rightLaneId = crossfadeRightId,
                    crossfadePosition = crossfadePos,
                    durationSec = crossfadeDur,
                    onSelectLeftLane = { viewModel.setCrossfadeLeft(it) },
                    onSelectRightLane = { viewModel.setCrossfadeRight(it) },
                    onPositionChange = { viewModel.setCrossfadePosition(it) },
                    onDurationChange = { viewModel.setCrossfadeDurationSec(it) },
                    onAutoCrossfade = { viewModel.triggerAutoCrossfade(it) },
                    onDismiss = { viewModel.setCrossfaderOpen(false) },
                    isPersianRtl = uiState.isPersianRtl
                )
            }

            if (uiState.isSleepTimerOpen) {
                SleepTimerSheet(
                    remainingSeconds = sleepTimerSec,
                    onStartTimer = { viewModel.startSleepTimer(it) },
                    onCancelTimer = { viewModel.cancelSleepTimer() },
                    onDismiss = { viewModel.setSleepTimerOpen(false) },
                    onAddMinutes = { viewModel.addSleepTimerMinutes(it) },
                    isPersianRtl = uiState.isPersianRtl
                )
            }

            if (isAudioRoutingSheetOpen) {
                AudioOutputRoutingSheet(
                    isOpen = true,
                    availableOutputs = availableAudioOutputs,
                    selectedOutput = selectedAudioOutput,
                    isPhoneAudioScoMode = isPhoneAudioScoMode,
                    onSelectOutput = { viewModel.selectAudioOutput(it) },
                    onTogglePhoneAudioSco = { viewModel.setPhoneAudioScoMode(it) },
                    onRefreshOutputs = { viewModel.refreshAudioOutputs() },
                    onDismiss = { viewModel.setAudioRoutingSheetOpen(false) },
                    isPersianRtl = uiState.isPersianRtl
                )
            }

            if (uiState.isRecorderOpen) {
                val isRecording by viewModel.masterRecorder.isRecording.collectAsStateWithLifecycle()
                val isRecorderPaused by viewModel.masterRecorder.isPaused.collectAsStateWithLifecycle()
                val recordingDuration by viewModel.masterRecorder.recordingDurationSec.collectAsStateWithLifecycle()
                val recordingAmplitude by viewModel.masterRecorder.amplitude.collectAsStateWithLifecycle()
                val recordingsList by viewModel.masterRecorder.recordingsList.collectAsStateWithLifecycle()
                val recorderError by viewModel.masterRecorder.errorMessage.collectAsStateWithLifecycle()
                val playingRecordingId by viewModel.masterRecorder.playingTrackId.collectAsStateWithLifecycle()

                RecorderSheet(
                    isRecording = isRecording,
                    isPaused = isRecorderPaused,
                    durationSec = recordingDuration,
                    amplitude = recordingAmplitude,
                    recordings = recordingsList,
                    playingTrackId = playingRecordingId,
                    lanes = uiState.lanes,
                    errorMessage = recorderError,
                    onStartRecording = { viewModel.masterRecorder.startRecording() },
                    onPauseRecording = { viewModel.masterRecorder.pauseRecording() },
                    onResumeRecording = { viewModel.masterRecorder.resumeRecording() },
                    onStopRecording = { viewModel.masterRecorder.stopRecording() },
                    onTogglePlayPreview = { viewModel.masterRecorder.togglePlayPreview(it) },
                    onAddRecordingToLane = { laneId, track -> viewModel.addRecordingToLane(laneId, track) },
                    onDeleteRecording = { viewModel.masterRecorder.deleteRecording(it) },
                    onDismiss = {
                        viewModel.masterRecorder.stopPreviewPlayback()
                        viewModel.setRecorderOpen(false)
                    },
                    isPersianRtl = uiState.isPersianRtl
                )
            }

            if (uiState.isThemeSelectorOpen) {
                ThemeSelectorSheet(
                    currentTheme = uiState.themeStyle,
                    isDarkMode = uiState.isDarkMode,
                    onSelectTheme = { selectedStyle ->
                        viewModel.setThemeStyle(selectedStyle)
                    },
                    onSetDarkMode = { isDark ->
                        viewModel.setDarkMode(isDark)
                    },
                    onDismiss = { viewModel.setThemeSelectorOpen(false) },
                    isPersianRtl = uiState.isPersianRtl
                )
            }

            if (uiState.isVideoAudioExtractorOpen) {
                VideoAudioExtractorSheet(
                    isOpen = uiState.isVideoAudioExtractorOpen,
                    onDismiss = { viewModel.setVideoAudioExtractorOpen(false) },
                    lanes = uiState.lanes,
                    selectedLaneId = uiState.selectedLaneId,
                    onPlayExtractedFile = { targetLaneId, uri, title ->
                        viewModel.playDirectExtractedAudio(targetLaneId, uri, title)
                        currentTab = 0
                    },
                    onAddExtractedToLane = { targetLaneId, uri, title ->
                        viewModel.addExtractedTrackToLane(targetLaneId, uri, title)
                    },
                    onSwitchToPlayer = {
                        currentTab = 0
                        viewModel.setVideoAudioExtractorOpen(false)
                    }
                )
            }

            if (uiState.isAboutDialogOpen) {
                AboutDialog(
                    onDismiss = { viewModel.setAboutDialogOpen(false) },
                    onOpenPrivacyPolicy = { viewModel.setPrivacyPolicyOpen(true) },
                    onOpenHelpCenter = { viewModel.setHelpModalOpen(true) },
                    onReplayTutorial = { viewModel.replayTour() },
                    isPersianRtl = uiState.isPersianRtl
                )
            }

            if (uiState.isPrivacyPolicyOpen) {
                PrivacyPolicyDialog(
                    onDismiss = { viewModel.setPrivacyPolicyOpen(false) },
                    isPersianRtl = uiState.isPersianRtl
                )
            }

            if (uiState.isHelpModalOpen) {
                HelpModal(
                    isOpen = uiState.isHelpModalOpen,
                    onClose = { viewModel.setHelpModalOpen(false) },
                    onStartInteractiveTour = {
                        viewModel.setHelpModalOpen(false)
                        viewModel.replayTour()
                    },
                    isPersianRtl = uiState.isPersianRtl
                )
            }

            uiState.laneToRename?.let { lane ->
                EditLaneNameDialog(
                    lane = lane,
                    onConfirm = { newName -> viewModel.renameLane(lane.laneId, newName) },
                    onDismiss = { viewModel.setLaneToRename(null) },
                    isPersianRtl = uiState.isPersianRtl
                )
            }

            uiState.laneToColorPick?.let { lane ->
                LaneColorPickerDialog(
                    lane = lane,
                    isPersianRtl = uiState.isPersianRtl,
                    onColorSelected = { colorHex ->
                        viewModel.updateLaneColor(lane.laneId, colorHex)
                    },
                    onDismiss = { viewModel.setLaneToColorPick(null) }
                )
            }

            if (uiState.isHomescreenWidgetDialogOpen) {
                HomescreenWidgetDialog(
                    isPersianRtl = uiState.isPersianRtl,
                    onDismiss = { viewModel.setHomescreenWidgetDialogOpen(false) }
                )
            }

            if (isAddLaneDialogOpen) {
                val nextNumber = uiState.lanes.size + 1
                val defaultName = if (uiState.isPersianRtl) "لاین $nextNumber" else "Lane $nextNumber"

                AddLaneNameDialog(
                    defaultName = defaultName,
                    isPersianRtl = uiState.isPersianRtl,
                    onConfirm = { customName ->
                        viewModel.addNewLane(customName)
                        isAddLaneDialogOpen = false
                    },
                    onDismiss = {
                        isAddLaneDialogOpen = false
                    }
                )
            }

            // First-Time User Onboarding Tour Overlay
            OnboardingTour(
                isOpen = uiState.isOnboardingTourOpen,
                onDismiss = { viewModel.setOnboardingTourOpen(false) },
                isPersianRtl = uiState.isPersianRtl
            )
        }
    }
}
