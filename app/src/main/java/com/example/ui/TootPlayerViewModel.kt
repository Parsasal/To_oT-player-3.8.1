package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.audio.AudioLaneState
import com.example.audio.EqPreset
import com.example.audio.MasterRecorder
import com.example.audio.RecordedTrack
import com.example.audio.VideoAudioExtractor
import com.example.audio.ExtractedAudioTrack
import com.example.service.AudioServiceBridge
import com.example.data.db.AppDatabase
import com.example.data.db.AppSettingsEntity
import com.example.data.db.LaneEntity
import com.example.data.model.DeviceAudioFile
import com.example.data.repository.AudioRepository
import com.example.data.repository.DeviceAudioScanner
import android.media.MediaPlayer
import com.example.data.db.LibraryTrackEntity
import com.example.util.AppLanguage
import com.example.util.SampleAudioGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

typealias AudioMixerViewModel = TootPlayerViewModel

enum class PlaylistSortType {
    NAME_ASC,
    NAME_DESC,
    DURATION_DESC,
    DURATION_ASC,
    DATE_ADDED
}

data class TootPlayerUiState(
    val lanes: List<AudioLaneState> = emptyList(),
    val selectedLaneId: Long? = null,
    val masterVolume: Float = 0.9f,
    val isMasterMuted: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val activeLanguage: AppLanguage = AppLanguage.EN,
    val isPersianRtl: Boolean = false,
    val themeStyle: com.example.ui.theme.AppThemeStyle = com.example.ui.theme.AppThemeStyle.EARTHY_BIOPHILIC,
    val isDarkMode: Boolean = true,
    val isThemeSelectorOpen: Boolean = false,
    val isLanguageSelectorOpen: Boolean = false,
    val isEqualizerOpen: Boolean = false,
    val isCrossfaderOpen: Boolean = false,
    val isSleepTimerOpen: Boolean = false,
    val isRecorderOpen: Boolean = false,
    val isAboutDialogOpen: Boolean = false,
    val isPrivacyPolicyOpen: Boolean = false,
    val laneToRename: AudioLaneState? = null,
    val laneToColorPick: AudioLaneState? = null,
    val isHomescreenWidgetDialogOpen: Boolean = false,
    val playlistSheetLaneId: Long? = null,
    val isDeviceAudioExplorerOpen: Boolean = false,
    val deviceAudioTargetLaneId: Long? = null,
    val deviceAudioFiles: List<DeviceAudioFile> = emptyList(),
    val isScanningDeviceAudio: Boolean = false,
    val previewPlayingUri: Uri? = null,
    val isAiSearching: Boolean = false,
    val aiSearchMatchedIds: Set<Long>? = null,
    val aiSearchActiveQuery: String? = null,
    val aiSearchImageUri: Uri? = null,
    val isOnboardingTourOpen: Boolean = false,
    val isHelpModalOpen: Boolean = false,
    val isVideoAudioExtractorOpen: Boolean = false,
    val autoResumeAfterCall: Boolean = true,
    val activeAiProvider: String = "gemini",
    val customGeminiApiKey: String = "",
    val customOpenAiApiKey: String = "",
    val customDeepSeekApiKey: String = "",
    val customGroqApiKey: String = "",
    val hasValidAiApiKey: Boolean = false,
    val hasValidGeminiApiKey: Boolean = false
)

class TootPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = AudioRepository(database)
    val audioEngine = AudioEngine(application)
    val masterRecorder by lazy { MasterRecorder(application) }
    val videoAudioExtractor by lazy { VideoAudioExtractor(application) }
    val deviceAudioScanner by lazy { DeviceAudioScanner(application) }
    val aiMusicSearchService by lazy { com.example.ai.AiMusicSearchService(application) }

    private val prefs = application.getSharedPreferences("to_ot_player_prefs", android.content.Context.MODE_PRIVATE)
    private val initialThemeName = run {
        val stored = prefs.getString("to_ot_active_theme", null)
        val isMigrated = prefs.getBoolean("to_ot_theme_earthy_biophilic_v343", false)
        if (!isMigrated || stored == null || stored == "RETRO_ANALOG") {
            prefs.edit()
                .putString("to_ot_active_theme", com.example.ui.theme.AppThemeStyle.EARTHY_BIOPHILIC.name)
                .putBoolean("to_ot_theme_earthy_biophilic_v343", true)
                .apply()
            com.example.ui.theme.AppThemeStyle.EARTHY_BIOPHILIC.name
        } else {
            stored
        }
    }
    private val initialDarkMode = prefs.getBoolean("to_ot_is_dark_mode", true)
    private val isLangEnMigrated = prefs.getBoolean("to_ot_lang_en_default_v341", false)
    private val initialLangCode = if (!isLangEnMigrated) {
        prefs.edit().putBoolean("to_ot_lang_en_default_v341", true).putString("to_ot_active_lang", "en").apply()
        "en"
    } else {
        prefs.getString("to_ot_active_lang", "en")
    }
    private val isTourCompleted = prefs.getBoolean("to_ot_tour_completed", false)
    private val initialAutoResumeAfterCall = prefs.getBoolean("to_ot_auto_resume_after_call", true)

    private val _autoResumeAfterCall = MutableStateFlow(initialAutoResumeAfterCall)
    val autoResumeAfterCall: StateFlow<Boolean> = _autoResumeAfterCall.asStateFlow()

    private var previewPlayer: MediaPlayer? = null

    private val _isOnboardingTourOpen = MutableStateFlow(!isTourCompleted)
    val isOnboardingTourOpen: StateFlow<Boolean> = _isOnboardingTourOpen.asStateFlow()

    private val _selectedLaneId = MutableStateFlow<Long?>(null)
    val selectedLaneId: StateFlow<Long?> = _selectedLaneId.asStateFlow()

    private val _activeLanguage = MutableStateFlow(AppLanguage.fromCode(initialLangCode))
    val activeLanguage: StateFlow<AppLanguage> = _activeLanguage.asStateFlow()

    private val _isPersianRtl = MutableStateFlow(AppLanguage.fromCode(initialLangCode).isRtl)
    val isPersianRtl: StateFlow<Boolean> = _isPersianRtl.asStateFlow()

    private val _themeStyle = MutableStateFlow(com.example.ui.theme.AppThemeStyle.fromIdOrDefault(initialThemeName))
    val themeStyle: StateFlow<com.example.ui.theme.AppThemeStyle> = _themeStyle.asStateFlow()

    private val _isDarkMode = MutableStateFlow(initialDarkMode)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isThemeSelectorOpen = MutableStateFlow(false)
    val isThemeSelectorOpen: StateFlow<Boolean> = _isThemeSelectorOpen.asStateFlow()

    private val _isLanguageSelectorOpen = MutableStateFlow(false)
    val isLanguageSelectorOpen: StateFlow<Boolean> = _isLanguageSelectorOpen.asStateFlow()

    private val _isEqualizerOpen = MutableStateFlow(false)
    val isEqualizerOpen: StateFlow<Boolean> = _isEqualizerOpen.asStateFlow()

    private val _isCrossfaderOpen = MutableStateFlow(false)
    val isCrossfaderOpen: StateFlow<Boolean> = _isCrossfaderOpen.asStateFlow()

    private val _isSleepTimerOpen = MutableStateFlow(false)
    val isSleepTimerOpen: StateFlow<Boolean> = _isSleepTimerOpen.asStateFlow()

    private val _isRecorderOpen = MutableStateFlow(false)
    val isRecorderOpen: StateFlow<Boolean> = _isRecorderOpen.asStateFlow()

    private val _isAboutDialogOpen = MutableStateFlow(false)
    val isAboutDialogOpen: StateFlow<Boolean> = _isAboutDialogOpen.asStateFlow()

    private val _isPrivacyPolicyOpen = MutableStateFlow(false)
    val isPrivacyPolicyOpen: StateFlow<Boolean> = _isPrivacyPolicyOpen.asStateFlow()

    private val _isHelpModalOpen = MutableStateFlow(false)
    val isHelpModalOpen: StateFlow<Boolean> = _isHelpModalOpen.asStateFlow()

    private val _isAudioRoutingSheetOpen = MutableStateFlow(false)
    val isAudioRoutingSheetOpen: StateFlow<Boolean> = _isAudioRoutingSheetOpen.asStateFlow()

    val availableAudioOutputs: StateFlow<List<com.example.audio.AudioOutputDevice>> = audioEngine.routingManager.availableOutputs
    val selectedAudioOutput: StateFlow<com.example.audio.AudioOutputDevice?> = audioEngine.routingManager.selectedOutput
    val isPhoneAudioScoMode: StateFlow<Boolean> = audioEngine.routingManager.isPhoneAudioScoMode

    private val _isVideoAudioExtractorOpen = MutableStateFlow(false)
    val isVideoAudioExtractorOpen: StateFlow<Boolean> = _isVideoAudioExtractorOpen.asStateFlow()

    private val _laneToRename = MutableStateFlow<AudioLaneState?>(null)
    val laneToRename: StateFlow<AudioLaneState?> = _laneToRename.asStateFlow()

    private val _laneToColorPick = MutableStateFlow<AudioLaneState?>(null)
    val laneToColorPick: StateFlow<AudioLaneState?> = _laneToColorPick.asStateFlow()

    private val _isHomescreenWidgetDialogOpen = MutableStateFlow(false)
    val isHomescreenWidgetDialogOpen: StateFlow<Boolean> = _isHomescreenWidgetDialogOpen.asStateFlow()

    private val _playlistSheetLaneId = MutableStateFlow<Long?>(null)
    val playlistSheetLaneId: StateFlow<Long?> = _playlistSheetLaneId.asStateFlow()

    private val _isDeviceAudioExplorerOpen = MutableStateFlow(false)
    val isDeviceAudioExplorerOpen: StateFlow<Boolean> = _isDeviceAudioExplorerOpen.asStateFlow()

    private val _deviceAudioTargetLaneId = MutableStateFlow<Long?>(null)
    val deviceAudioTargetLaneId: StateFlow<Long?> = _deviceAudioTargetLaneId.asStateFlow()

    private val _deviceAudioFiles = MutableStateFlow<List<DeviceAudioFile>>(emptyList())
    val deviceAudioFiles: StateFlow<List<DeviceAudioFile>> = _deviceAudioFiles.asStateFlow()

    private val _isScanningDeviceAudio = MutableStateFlow(false)
    val isScanningDeviceAudio: StateFlow<Boolean> = _isScanningDeviceAudio.asStateFlow()

    private val _previewPlayingUri = MutableStateFlow<Uri?>(null)
    val previewPlayingUri: StateFlow<Uri?> = _previewPlayingUri.asStateFlow()

    private val _isAiSearching = MutableStateFlow(false)
    val isAiSearching: StateFlow<Boolean> = _isAiSearching.asStateFlow()

    private val _aiSearchMatchedIds = MutableStateFlow<Set<Long>?>(null)
    val aiSearchMatchedIds: StateFlow<Set<Long>?> = _aiSearchMatchedIds.asStateFlow()

    private val _aiSearchActiveQuery = MutableStateFlow<String?>(null)
    val aiSearchActiveQuery: StateFlow<String?> = _aiSearchActiveQuery.asStateFlow()

    private val _aiSearchImageUri = MutableStateFlow<Uri?>(null)
    val aiSearchImageUri: StateFlow<Uri?> = _aiSearchImageUri.asStateFlow()

    private val _activeAiProvider = MutableStateFlow(aiMusicSearchService.getActiveProvider().id)
    val activeAiProvider: StateFlow<String> = _activeAiProvider.asStateFlow()

    private val _customGeminiApiKey = MutableStateFlow(aiMusicSearchService.getApiKeyForProvider(com.example.ai.AiProvider.GEMINI) ?: "")
    val customGeminiApiKey: StateFlow<String> = _customGeminiApiKey.asStateFlow()

    private val _customOpenAiApiKey = MutableStateFlow(aiMusicSearchService.getApiKeyForProvider(com.example.ai.AiProvider.OPENAI) ?: "")
    val customOpenAiApiKey: StateFlow<String> = _customOpenAiApiKey.asStateFlow()

    private val _customDeepSeekApiKey = MutableStateFlow(aiMusicSearchService.getApiKeyForProvider(com.example.ai.AiProvider.DEEPSEEK) ?: "")
    val customDeepSeekApiKey: StateFlow<String> = _customDeepSeekApiKey.asStateFlow()

    private val _customGroqApiKey = MutableStateFlow(aiMusicSearchService.getApiKeyForProvider(com.example.ai.AiProvider.GROQ) ?: "")
    val customGroqApiKey: StateFlow<String> = _customGroqApiKey.asStateFlow()

    private var initializedFromDb = false

    private data class MasterAudioState(
        val masterVolume: Float,
        val isMasterMuted: Boolean,
        val playbackSpeed: Float
    )

    private val masterAudioFlow = combine(
        audioEngine.masterVolume,
        audioEngine.isMasterMuted,
        audioEngine.playbackSpeed
    ) { vol, muted, speed ->
        MasterAudioState(vol, muted, speed)
    }

    private data class DeviceExplorerFlags(
        val isExplorerOpen: Boolean,
        val targetLaneId: Long?,
        val files: List<DeviceAudioFile>,
        val isScanning: Boolean,
        val previewUri: Uri?,
        val isAiSearching: Boolean,
        val aiMatchedIds: Set<Long>?,
        val aiActiveQuery: String?,
        val aiImageUri: Uri?
    )

    private val deviceExplorerFlow = combine(
        combine(_isDeviceAudioExplorerOpen, _deviceAudioTargetLaneId, _deviceAudioFiles, _isScanningDeviceAudio, _previewPlayingUri) { open, target, files, scanning, preview ->
            Tuple5(open, target, files, scanning, preview)
        },
        combine(_isAiSearching, _aiSearchMatchedIds, _aiSearchActiveQuery, _aiSearchImageUri) { isAi, matched, q, img ->
            Tuple4(isAi, matched, q, img)
        }
    ) { (open, target, files, scanning, preview), (isAi, matched, q, img) ->
        DeviceExplorerFlags(
            isExplorerOpen = open,
            targetLaneId = target,
            files = files,
            isScanning = scanning,
            previewUri = preview,
            isAiSearching = isAi,
            aiMatchedIds = matched,
            aiActiveQuery = q,
            aiImageUri = img
        )
    }

    private data class DialogFlags(
        val isSleepTimerOpen: Boolean,
        val isRecorderOpen: Boolean,
        val isAboutDialogOpen: Boolean,
        val isPrivacyPolicyOpen: Boolean,
        val laneToRename: AudioLaneState?,
        val laneToColorPick: AudioLaneState?,
        val isHomescreenWidgetDialogOpen: Boolean,
        val playlistSheetLaneId: Long?,
        val isThemeSelectorOpen: Boolean,
        val isLanguageSelectorOpen: Boolean,
        val isOnboardingTourOpen: Boolean,
        val isHelpModalOpen: Boolean,
        val isVideoAudioExtractorOpen: Boolean
    )

    private val dialogFlagsFlow = combine(
        combine(_isSleepTimerOpen, _isRecorderOpen, _isAboutDialogOpen, _isPrivacyPolicyOpen, _isOnboardingTourOpen) { sleep, rec, about, privacy, tour ->
            Tuple5(sleep, rec, about, privacy, tour)
        },
        combine(_laneToRename, _laneToColorPick, _isHomescreenWidgetDialogOpen) { r, c, w ->
            Tuple3(r, c, w)
        },
        combine(_playlistSheetLaneId, _isThemeSelectorOpen, _isLanguageSelectorOpen, _isHelpModalOpen, _isVideoAudioExtractorOpen) { p, t, l, h, v -> Tuple5(p, t, l, h, v) }
    ) { (sleep, rec, about, privacy, tour), (rename, colorPick, widgetDialog), tuple5 ->
        DialogFlags(
            isSleepTimerOpen = sleep,
            isRecorderOpen = rec,
            isAboutDialogOpen = about,
            isPrivacyPolicyOpen = privacy,
            laneToRename = rename,
            laneToColorPick = colorPick,
            isHomescreenWidgetDialogOpen = widgetDialog,
            playlistSheetLaneId = tuple5.first,
            isThemeSelectorOpen = tuple5.second,
            isLanguageSelectorOpen = tuple5.third,
            isOnboardingTourOpen = tour,
            isHelpModalOpen = tuple5.fourth,
            isVideoAudioExtractorOpen = tuple5.fifth
        )
    }

    private data class Tuple3<A, B, C>(val first: A, val second: B, val third: C)
    private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
    private data class Tuple5<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
    private data class Tuple6<A, B, C, D, E, F>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E, val sixth: F)

    private data class AppUiDialogState(
        val activeLanguage: AppLanguage,
        val isPersianRtl: Boolean,
        val themeStyle: com.example.ui.theme.AppThemeStyle,
        val isDarkMode: Boolean,
        val selectedLaneId: Long?,
        val isEqualizerOpen: Boolean,
        val isCrossfaderOpen: Boolean,
        val dialogFlags: DialogFlags,
        val explorerFlags: DeviceExplorerFlags,
        val autoResumeAfterCall: Boolean,
        val activeAiProvider: String,
        val customGeminiApiKey: String,
        val customOpenAiApiKey: String,
        val customDeepSeekApiKey: String,
        val customGroqApiKey: String,
        val hasValidAiApiKey: Boolean
    )

    private data class AppearanceState(
        val activeLanguage: AppLanguage,
        val isPersianRtl: Boolean,
        val themeStyle: com.example.ui.theme.AppThemeStyle,
        val isDarkMode: Boolean,
        val selectedLaneId: Long?,
        val autoResumeAfterCall: Boolean,
        val activeAiProvider: String,
        val customGeminiApiKey: String,
        val customOpenAiApiKey: String,
        val customDeepSeekApiKey: String,
        val customGroqApiKey: String,
        val hasValidAiApiKey: Boolean
    )

    private val aiKeysFlow = combine(
        _activeAiProvider,
        _customGeminiApiKey,
        _customOpenAiApiKey,
        _customDeepSeekApiKey,
        _customGroqApiKey
    ) { prov, gem, oai, dsk, grq ->
        val providerObj = com.example.ai.AiProvider.fromId(prov)
        val hasValid = when (providerObj) {
            com.example.ai.AiProvider.GEMINI -> gem.isNotBlank() || aiMusicSearchService.hasValidApiKey(com.example.ai.AiProvider.GEMINI)
            com.example.ai.AiProvider.OPENAI -> oai.isNotBlank()
            com.example.ai.AiProvider.DEEPSEEK -> dsk.isNotBlank()
            com.example.ai.AiProvider.GROQ -> grq.isNotBlank()
        }
        Tuple6(prov, gem, oai, dsk, grq, hasValid)
    }

    private val appearanceFlow = combine(
        _activeLanguage,
        _isPersianRtl,
        _themeStyle,
        _isDarkMode,
        combine(_selectedLaneId, _autoResumeAfterCall, aiKeysFlow) { selId, autoResume, aiKeys ->
            Triple(selId, autoResume, aiKeys)
        }
    ) { lang, isRtl, theme, isDark, (selId, autoResume, aiKeys) ->
        AppearanceState(
            activeLanguage = lang,
            isPersianRtl = isRtl,
            themeStyle = theme,
            isDarkMode = isDark,
            selectedLaneId = selId,
            autoResumeAfterCall = autoResume,
            activeAiProvider = aiKeys.first,
            customGeminiApiKey = aiKeys.second,
            customOpenAiApiKey = aiKeys.third,
            customDeepSeekApiKey = aiKeys.fourth,
            customGroqApiKey = aiKeys.fifth,
            hasValidAiApiKey = aiKeys.sixth
        )
    }

    private val appUiDialogFlow = combine(
        appearanceFlow,
        _isEqualizerOpen,
        _isCrossfaderOpen,
        dialogFlagsFlow,
        deviceExplorerFlow
    ) { app, eq, cross, d, e ->
        AppUiDialogState(
            activeLanguage = app.activeLanguage,
            isPersianRtl = app.isPersianRtl,
            themeStyle = app.themeStyle,
            isDarkMode = app.isDarkMode,
            selectedLaneId = app.selectedLaneId,
            isEqualizerOpen = eq,
            isCrossfaderOpen = cross,
            dialogFlags = d,
            explorerFlags = e,
            autoResumeAfterCall = app.autoResumeAfterCall,
            activeAiProvider = app.activeAiProvider,
            customGeminiApiKey = app.customGeminiApiKey,
            customOpenAiApiKey = app.customOpenAiApiKey,
            customDeepSeekApiKey = app.customDeepSeekApiKey,
            customGroqApiKey = app.customGroqApiKey,
            hasValidAiApiKey = app.hasValidAiApiKey
        )
    }

    val uiState: StateFlow<TootPlayerUiState> = combine(
        repository.allLanes,
        audioEngine.laneStates,
        masterAudioFlow,
        appUiDialogFlow
    ) { dbLanes, engineStates, masterState, dialogState ->
        val mappedLanes = dbLanes.map { dbLane ->
            val engineState = engineStates[dbLane.id]
            val effectiveColor = engineState?.colorHex ?: dbLane.colorHex
            if (engineState != null) {
                engineState.copy(
                    name = dbLane.name,
                    colorHex = effectiveColor
                )
            } else {
                AudioLaneState(
                    laneId = dbLane.id,
                    name = dbLane.name,
                    uriString = dbLane.uriString,
                    fileName = dbLane.fileName,
                    durationMs = dbLane.durationMs,
                    volume = dbLane.volume,
                    isMuted = dbLane.isMuted,
                    isSoloed = dbLane.isSoloed,
                    isLoopEnabled = dbLane.isLoopEnabled,
                    isPlaylistLoopEnabled = dbLane.isPlaylistLoopEnabled,
                    isShuffleEnabled = dbLane.isShuffleEnabled,
                    colorHex = effectiveColor,
                    currentPositionMs = dbLane.playbackPositionMs
                )
            }
        }

        val effectiveSelectedId = when {
            dialogState.selectedLaneId != null && mappedLanes.any { it.laneId == dialogState.selectedLaneId } -> dialogState.selectedLaneId
            mappedLanes.isNotEmpty() -> mappedLanes.first().laneId
            else -> null
        }

        val dFlags = dialogState.dialogFlags
        val eFlags = dialogState.explorerFlags

        TootPlayerUiState(
            lanes = mappedLanes,
            selectedLaneId = effectiveSelectedId,
            masterVolume = masterState.masterVolume,
            isMasterMuted = masterState.isMasterMuted,
            playbackSpeed = masterState.playbackSpeed,
            activeLanguage = dialogState.activeLanguage,
            isPersianRtl = dialogState.isPersianRtl,
            themeStyle = dialogState.themeStyle,
            isDarkMode = dialogState.isDarkMode,
            isThemeSelectorOpen = dFlags.isThemeSelectorOpen,
            isLanguageSelectorOpen = dFlags.isLanguageSelectorOpen,
            isEqualizerOpen = dialogState.isEqualizerOpen,
            isCrossfaderOpen = dialogState.isCrossfaderOpen,
            isSleepTimerOpen = dFlags.isSleepTimerOpen,
            isRecorderOpen = dFlags.isRecorderOpen,
            isAboutDialogOpen = dFlags.isAboutDialogOpen,
            isPrivacyPolicyOpen = dFlags.isPrivacyPolicyOpen,
            laneToRename = dFlags.laneToRename,
            laneToColorPick = dFlags.laneToColorPick,
            isHomescreenWidgetDialogOpen = dFlags.isHomescreenWidgetDialogOpen,
            playlistSheetLaneId = dFlags.playlistSheetLaneId,
            isDeviceAudioExplorerOpen = eFlags.isExplorerOpen,
            deviceAudioTargetLaneId = eFlags.targetLaneId ?: effectiveSelectedId,
            deviceAudioFiles = eFlags.files,
            isScanningDeviceAudio = eFlags.isScanning,
            previewPlayingUri = eFlags.previewUri,
            isAiSearching = eFlags.isAiSearching,
            aiSearchMatchedIds = eFlags.aiMatchedIds,
            aiSearchActiveQuery = eFlags.aiActiveQuery,
            aiSearchImageUri = eFlags.aiImageUri,
            isOnboardingTourOpen = dFlags.isOnboardingTourOpen,
            isHelpModalOpen = dFlags.isHelpModalOpen,
            isVideoAudioExtractorOpen = dFlags.isVideoAudioExtractorOpen,
            autoResumeAfterCall = dialogState.autoResumeAfterCall,
            activeAiProvider = dialogState.activeAiProvider,
            customGeminiApiKey = dialogState.customGeminiApiKey,
            customOpenAiApiKey = dialogState.customOpenAiApiKey,
            customDeepSeekApiKey = dialogState.customDeepSeekApiKey,
            customGroqApiKey = dialogState.customGroqApiKey,
            hasValidAiApiKey = dialogState.hasValidAiApiKey,
            hasValidGeminiApiKey = dialogState.hasValidAiApiKey
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TootPlayerUiState()
    )

    init {
        // Initialize bridge with user preference
        AudioServiceBridge.isAutoResumeAfterCallEnabled = initialAutoResumeAfterCall

        // Enforce that all playback is strictly stopped on launch
        audioEngine.stopAllLanes()

        // Hook automatic timestamp persistence for in-ear detection, noisy auto-pause, and manual pauses
        audioEngine.onLanePositionPersistRequested = { laneId, positionMs ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    repository.updatePlaybackPosition(laneId, positionMs)
                } catch (e: Exception) {
                    Log.w("TootPlayerViewModel", "Failed to persist lane position: $e")
                }
            }
        }

        // Hook loop toggle persistence
        audioEngine.onLaneLoopPersistRequested = { laneId, isLoop ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    repository.updateLaneLoop(laneId, isLoop)
                } catch (e: Exception) {
                    Log.w("TootPlayerViewModel", "Failed to persist lane loop: $e")
                }
            }
        }

        // Hook playlist loop toggle persistence
        audioEngine.onLanePlaylistLoopPersistRequested = { laneId, isLoop ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    repository.updateLanePlaylistLoop(laneId, isLoop)
                } catch (e: Exception) {
                    Log.w("TootPlayerViewModel", "Failed to persist lane playlist loop: $e")
                }
            }
        }

        // Hook shuffle toggle persistence
        audioEngine.onLaneShufflePersistRequested = { laneId, isShuffle ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    repository.updateLaneShuffle(laneId, isShuffle)
                } catch (e: Exception) {
                    Log.w("TootPlayerViewModel", "Failed to persist lane shuffle: $e")
                }
            }
        }

        // Initialize default lanes and settings on IO
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.initializeDefaultsIfNeeded()
            } catch (e: Exception) {
                Log.e("TootPlayerVM", "Error initializing default lanes: $e")
            }
        }

        // Auto scan files immediately on IO in background
        viewModelScope.launch(Dispatchers.IO) {
            try {
                scanDeviceAudioFiles()
            } catch (e: Exception) {
                Log.e("TootPlayerVM", "Error launching initial scan: $e")
            }
        }

        // Collect settings
        viewModelScope.launch {
            try {
                repository.settings.collect { settings ->
                    if (settings != null && !initializedFromDb) {
                        audioEngine.setMasterVolume(settings.masterVolume)
                        if (settings.isMasterMuted) audioEngine.toggleMasterMute()
                        audioEngine.setPlaybackSpeed(settings.playbackSpeed)
                        audioEngine.setEqEnabled(settings.eqEnabled)
                        audioEngine.setEqBands(
                            listOf(settings.eqBand0, settings.eqBand1, settings.eqBand2, settings.eqBand3, settings.eqBand4),
                            settings.eqPreset
                        )
                        val lang = if (!isLangEnMigrated) {
                            viewModelScope.launch {
                                repository.saveSettings(settings.copy(languageCode = "en", isPersianRtl = false))
                            }
                            AppLanguage.EN
                        } else {
                            AppLanguage.fromCode(settings.languageCode.ifBlank { prefs.getString("to_ot_active_lang", "en") ?: "en" })
                        }
                        _activeLanguage.value = lang
                        _isPersianRtl.value = lang.isRtl
                        _selectedLaneId.value = settings.selectedLaneId
                        _isDarkMode.value = settings.isDarkMode
                        val currentSettingsTheme = settings.themeStyle
                        val isThemeV343Migrated = prefs.getBoolean("to_ot_theme_earthy_biophilic_v343_db", false)
                        val themeToSet = if (!isThemeV343Migrated || currentSettingsTheme == "RETRO_ANALOG" || currentSettingsTheme.isBlank()) {
                            prefs.edit().putBoolean("to_ot_theme_earthy_biophilic_v343_db", true).apply()
                            viewModelScope.launch {
                                repository.saveSettings(settings.copy(themeStyle = com.example.ui.theme.AppThemeStyle.EARTHY_BIOPHILIC.name))
                            }
                            com.example.ui.theme.AppThemeStyle.EARTHY_BIOPHILIC
                        } else {
                            com.example.ui.theme.AppThemeStyle.fromIdOrDefault(currentSettingsTheme)
                        }
                        _themeStyle.value = themeToSet
                        prefs.edit()
                            .putString("to_ot_active_lang", lang.code)
                            .putString("to_ot_active_theme", themeToSet.name)
                            .putBoolean("to_ot_is_dark_mode", settings.isDarkMode)
                            .apply()
                    }
                }
            } catch (e: Exception) {
                Log.e("TootPlayerVM", "Error collecting settings: $e")
            }
        }

        viewModelScope.launch {
            repository.allLanes.collect { dbLanes ->
                if (!initializedFromDb && dbLanes.isNotEmpty()) {
                    initializedFromDb = true
                    dbLanes.forEach { lane ->
                        val playlistEntities = repository.getPlaylistForLaneSync(lane.id)
                        val playlistItems = playlistEntities.map { p ->
                            com.example.audio.PlaylistItem(
                                id = p.id,
                                laneId = p.laneId,
                                uriString = p.uriString,
                                title = p.title,
                                durationMs = p.durationMs,
                                sortOrder = p.sortOrder
                            )
                        }
                        audioEngine.registerLane(
                            laneId = lane.id,
                            name = lane.name,
                            uriString = lane.uriString,
                            fileName = lane.fileName,
                            volume = lane.volume,
                            isMuted = lane.isMuted,
                            isSoloed = lane.isSoloed,
                            playlist = playlistItems,
                            isLoopEnabled = lane.isLoopEnabled,
                            isPlaylistLoopEnabled = lane.isPlaylistLoopEnabled,
                            colorHex = lane.colorHex,
                            currentPositionMs = lane.playbackPositionMs
                        )
                    }
                    if (_selectedLaneId.value == null && dbLanes.isNotEmpty()) {
                        _selectedLaneId.value = dbLanes.first().id
                    }
                } else if (initializedFromDb && dbLanes.isNotEmpty()) {
                    // Safe synchronization: if any new lane is in DB but not yet in AudioEngine, register it; if any removed from DB, remove it
                    val existingEngineKeys = audioEngine.laneStates.value.keys.toSet()
                    val currentDbIds = dbLanes.map { it.id }.toSet()
                    for (engineKey in existingEngineKeys) {
                        if (engineKey !in currentDbIds) {
                            audioEngine.removeLane(engineKey)
                        }
                    }
                    if (_selectedLaneId.value !in currentDbIds) {
                        _selectedLaneId.value = dbLanes.firstOrNull()?.id
                    }
                    for (lane in dbLanes) {
                        if (lane.id !in existingEngineKeys) {
                            val playlistEntities = repository.getPlaylistForLaneSync(lane.id)
                            val playlistItems = playlistEntities.map { p ->
                                com.example.audio.PlaylistItem(
                                    id = p.id,
                                    laneId = p.laneId,
                                    uriString = p.uriString,
                                    title = p.title,
                                    durationMs = p.durationMs,
                                    sortOrder = p.sortOrder
                                )
                            }
                            audioEngine.registerLane(
                                laneId = lane.id,
                                name = lane.name,
                                uriString = lane.uriString,
                                fileName = lane.fileName,
                                volume = lane.volume,
                                isMuted = lane.isMuted,
                                isSoloed = lane.isSoloed,
                                playlist = playlistItems,
                                isLoopEnabled = lane.isLoopEnabled,
                                isPlaylistLoopEnabled = lane.isPlaylistLoopEnabled,
                                colorHex = lane.colorHex,
                                currentPositionMs = lane.playbackPositionMs
                            )
                        }
                    }
                }
            }
        }

        // Keep Library tracks synced with database
        viewModelScope.launch {
            repository.allLibraryTracks.collect { libTracks ->
                if (libTracks.isNotEmpty()) {
                    val currentList = _deviceAudioFiles.value.toMutableList()
                    var changed = false
                    for (track in libTracks) {
                        if (currentList.none { it.uri.toString() == track.uriString || it.displayName == track.title }) {
                            currentList.add(
                                DeviceAudioFile(
                                    id = track.id,
                                    uri = Uri.parse(track.uriString),
                                    title = track.title,
                                    artist = track.artist,
                                    album = track.album ?: "Library",
                                    durationMs = track.durationMs,
                                    sizeBytes = track.sizeBytes,
                                    dateAddedSec = track.dateAddedSec,
                                    dateModifiedSec = track.dateAddedSec,
                                    path = null,
                                    relativePath = "library/",
                                    displayName = track.title,
                                    isFromDownloadFolder = false
                                )
                            )
                            changed = true
                        }
                    }
                    if (changed) {
                        _deviceAudioFiles.value = currentList.sortedByDescending { it.dateAddedSec }
                    }
                }
            }
        }

        // Keep lane playlists synced with database updates
        viewModelScope.launch {
            repository.allPlaylistItems.collect { allItems ->
                if (initializedFromDb) {
                    val grouped = allItems.groupBy { it.laneId }
                    val allLaneIds = (audioEngine.laneStates.value.keys + grouped.keys + uiState.value.lanes.map { it.laneId }).distinct()
                    allLaneIds.forEach { laneId ->
                        val items = (grouped[laneId] ?: emptyList()).map { p ->
                            com.example.audio.PlaylistItem(
                                id = p.id,
                                laneId = p.laneId,
                                uriString = p.uriString,
                                title = p.title,
                                durationMs = p.durationMs,
                                sortOrder = p.sortOrder
                            )
                        }
                        val currentPlaylist = audioEngine.laneStates.value[laneId]?.playlist ?: emptyList()
                        if (items != currentPlaylist && (items.isNotEmpty() || currentPlaylist.isNotEmpty())) {
                            audioEngine.updateLanePlaylist(laneId, items)
                        }
                    }
                }
            }
        }
    }

    fun selectLane(laneId: Long) {
        _selectedLaneId.value = laneId
        com.example.service.AudioServiceBridge.activeSessionLaneId = laneId
        audioEngine.syncServiceAndWidgets(laneId)
        saveSettingsToDb()
    }

    fun setLanguage(lang: AppLanguage) {
        _activeLanguage.value = lang
        _isPersianRtl.value = lang.isRtl
        prefs.edit().putString("to_ot_active_lang", lang.code).apply()
        saveSettingsToDb()
    }

    fun setLanguageSelectorOpen(open: Boolean) {
        _isLanguageSelectorOpen.value = open
    }

    fun togglePersianRtl() {
        val nextLang = if (_activeLanguage.value == AppLanguage.FA) AppLanguage.EN else AppLanguage.FA
        setLanguage(nextLang)
    }

    fun toggleDarkMode() {
        val newDark = !_isDarkMode.value
        _isDarkMode.value = newDark
        prefs.edit().putBoolean("to_ot_is_dark_mode", newDark).apply()
        saveSettingsToDb()
    }

    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
        prefs.edit().putBoolean("to_ot_is_dark_mode", isDark).apply()
        saveSettingsToDb()
    }

    fun setThemeStyle(style: com.example.ui.theme.AppThemeStyle) {
        _themeStyle.value = style
        prefs.edit().putString("to_ot_active_theme", style.name).apply()
        saveSettingsToDb()
    }

    fun setThemeSelectorOpen(open: Boolean) { _isThemeSelectorOpen.value = open }
    fun setEqualizerOpen(open: Boolean) { _isEqualizerOpen.value = open }
    fun setCrossfaderOpen(open: Boolean) { _isCrossfaderOpen.value = open }
    fun setSleepTimerOpen(open: Boolean) { _isSleepTimerOpen.value = open }
    fun setRecorderOpen(open: Boolean) { _isRecorderOpen.value = open }
    fun setAboutDialogOpen(open: Boolean) { _isAboutDialogOpen.value = open }
    fun setPrivacyPolicyOpen(open: Boolean) { _isPrivacyPolicyOpen.value = open }
    fun setHelpModalOpen(open: Boolean) { _isHelpModalOpen.value = open }
    fun setVideoAudioExtractorOpen(open: Boolean) { _isVideoAudioExtractorOpen.value = open }
    fun setAudioRoutingSheetOpen(open: Boolean) {
        _isAudioRoutingSheetOpen.value = open
        if (open) {
            audioEngine.routingManager.refreshDevices()
        }
    }

    fun selectAudioOutput(device: com.example.audio.AudioOutputDevice?) {
        audioEngine.routingManager.selectOutput(device)
    }

    fun setPhoneAudioScoMode(enabled: Boolean) {
        audioEngine.routingManager.setPhoneAudioScoMode(enabled)
    }

    fun refreshAudioOutputs() {
        audioEngine.routingManager.refreshDevices()
    }

    fun setAutoResumeAfterCall(enabled: Boolean) {
        _autoResumeAfterCall.value = enabled
        prefs.edit().putBoolean("to_ot_auto_resume_after_call", enabled).apply()
        AudioServiceBridge.isAutoResumeAfterCallEnabled = enabled
    }

    fun toggleAutoResumeAfterCall() {
        setAutoResumeAfterCall(!_autoResumeAfterCall.value)
    }

    fun playDirectExtractedAudio(targetLaneId: Long? = null, uri: Uri, title: String) {
        viewModelScope.launch {
            val resolvedLaneId = resolveOrCreateTargetLane(targetLaneId, title)
            _selectedLaneId.value = resolvedLaneId
            loadAudioForLane(resolvedLaneId, uri, autoStart = true, customTitle = title)
        }
    }

    fun addExtractedTrackToLane(targetLaneId: Long? = null, uri: Uri, title: String) {
        viewModelScope.launch {
            val resolvedLaneId = resolveOrCreateTargetLane(targetLaneId, title)
            _selectedLaneId.value = resolvedLaneId
            loadAudioForLane(resolvedLaneId, uri, autoStart = false, customTitle = title)
        }
    }

    private suspend fun resolveOrCreateTargetLane(targetLaneId: Long?, title: String): Long {
        if (targetLaneId != null && targetLaneId > 0L) {
            val existsInUi = uiState.value.lanes.any { it.laneId == targetLaneId }
            val existsInEngine = audioEngine.laneStates.value.containsKey(targetLaneId)
            val existsInDb = repository.getLaneById(targetLaneId) != null
            if (existsInUi || existsInEngine || existsInDb) {
                return targetLaneId
            }
        }
        if (targetLaneId != null && targetLaneId == -1L) {
            // User explicitly requested creating a brand new audio lane
            val currentLanes = uiState.value.lanes
            val preAdditionLoops = currentLanes.associate { it.laneId to it.isLoopEnabled }
            val totalLanes = try { repository.getAllLanesSync().size } catch (_: Exception) { currentLanes.size }
            val nextNumber = totalLanes + 1
            val newName = title.ifBlank { "Lane $nextNumber" }
            val newId = repository.addLaneAtTop(newName)
            audioEngine.registerLane(laneId = newId, name = newName, volume = 0.85f, isMuted = false, isSoloed = false, isLoopEnabled = false, isPlaylistLoopEnabled = false)
            logAndVerifyLoopConfigurationsOnLaneAddition(newId, newName, preAdditionLoops)
            return newId
        }
        val existingId = _selectedLaneId.value ?: uiState.value.lanes.firstOrNull()?.laneId
        if (existingId != null && existingId > 0L) {
            val existsInUi = uiState.value.lanes.any { it.laneId == existingId }
            val existsInEngine = audioEngine.laneStates.value.containsKey(existingId)
            val existsInDb = repository.getLaneById(existingId) != null
            if (existsInUi || existsInEngine || existsInDb) {
                return existingId
            }
        }
        val currentLanes = uiState.value.lanes
        val preAdditionLoops = currentLanes.associate { it.laneId to it.isLoopEnabled }
        val totalLanes = try { repository.getAllLanesSync().size } catch (_: Exception) { currentLanes.size }
        val nextNumber = totalLanes + 1
        val newName = title.ifBlank { "Lane $nextNumber" }
        val newId = repository.addLaneAtTop(newName)
        audioEngine.registerLane(laneId = newId, name = newName, volume = 0.85f, isMuted = false, isSoloed = false, isLoopEnabled = false, isPlaylistLoopEnabled = false)
        logAndVerifyLoopConfigurationsOnLaneAddition(newId, newName, preAdditionLoops)
        return newId
    }
    fun setOnboardingTourOpen(open: Boolean) {
        _isOnboardingTourOpen.value = open
        if (!open) {
            prefs.edit().putBoolean("to_ot_tour_completed", true).apply()
        }
    }
    fun replayTour() {
        _isOnboardingTourOpen.value = true
    }
    fun setLaneToRename(lane: AudioLaneState?) { _laneToRename.value = lane }
    fun setLaneToColorPick(lane: AudioLaneState?) { _laneToColorPick.value = lane }
    fun setHomescreenWidgetDialogOpen(open: Boolean) { _isHomescreenWidgetDialogOpen.value = open }
    fun setPlaylistSheetLaneId(laneId: Long?) { _playlistSheetLaneId.value = laneId }

    fun updateLaneColor(laneId: Long, colorHex: String?) {
        audioEngine.updateLaneColor(laneId, colorHex)
        _laneToColorPick.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateLaneColor(laneId, colorHex)
            } catch (e: Exception) {
                Log.w("TootPlayerViewModel", "Failed to persist lane color: $e")
            }
        }
    }

    fun addTracksToLane(laneId: Long, tracks: List<LibraryTrackEntity>) {
        if (tracks.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getPlaylistForLaneSync(laneId)
            val startIndex = existing.size
            val playlistItems = tracks.mapIndexed { index, t ->
                com.example.data.db.PlaylistItemEntity(
                    laneId = laneId,
                    uriString = t.uriString,
                    title = t.title,
                    durationMs = t.durationMs,
                    sortOrder = startIndex + index
                )
            }
            repository.addPlaylistItems(playlistItems)
            val updated = repository.getPlaylistForLaneSync(laneId)
            audioEngine.updateLanePlaylist(laneId, updated.map {
                com.example.audio.PlaylistItem(
                    id = it.id,
                    laneId = it.laneId,
                    uriString = it.uriString,
                    title = it.title,
                    durationMs = it.durationMs,
                    sortOrder = it.sortOrder
                )
            })
        }
    }

    fun createLaneWithTracks(laneName: String, tracks: List<LibraryTrackEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = repository.addLaneAtTop(laneName)
            audioEngine.registerLane(
                laneId = newId,
                name = laneName,
                volume = 0.85f,
                isMuted = false,
                isSoloed = false,
                isLoopEnabled = false
            )
            _selectedLaneId.value = newId
            if (tracks.isNotEmpty()) {
                val playlistItems = tracks.mapIndexed { index, t ->
                    com.example.data.db.PlaylistItemEntity(
                        laneId = newId,
                        uriString = t.uriString,
                        title = t.title,
                        durationMs = t.durationMs,
                        sortOrder = index
                    )
                }
                repository.addPlaylistItems(playlistItems)
                audioEngine.updateLanePlaylist(newId, playlistItems.map {
                    com.example.audio.PlaylistItem(
                        id = it.id,
                        laneId = it.laneId,
                        uriString = it.uriString,
                        title = it.title,
                        durationMs = it.durationMs,
                        sortOrder = it.sortOrder
                    )
                })
            }
        }
    }

    fun playTracksInLane(laneName: String, tracks: List<LibraryTrackEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (tracks.isEmpty()) return@launch
            val all = repository.getAllLanesSync()
            val existingLane = all.find { it.name.trim().equals(laneName.trim(), ignoreCase = true) }
            val laneId = existingLane?.id ?: repository.addLaneAtTop(laneName)
            _selectedLaneId.value = laneId

            repository.clearPlaylistForLane(laneId)
            val playlistItems = tracks.mapIndexed { index, t ->
                com.example.data.db.PlaylistItemEntity(
                    laneId = laneId,
                    uriString = t.uriString,
                    title = t.title,
                    durationMs = t.durationMs,
                    sortOrder = index
                )
            }
            repository.addPlaylistItems(playlistItems)
            val savedPlaylist = repository.getPlaylistForLaneSync(laneId)
            val engineItems = savedPlaylist.map {
                com.example.audio.PlaylistItem(
                    id = it.id,
                    laneId = it.laneId,
                    uriString = it.uriString,
                    title = it.title,
                    durationMs = it.durationMs,
                    sortOrder = it.sortOrder
                )
            }
            audioEngine.updateLanePlaylist(laneId, engineItems)
            if (engineItems.isNotEmpty()) {
                audioEngine.playPlaylistItem(laneId, 0, autoStart = true)
            }
        }
    }

    // Device Audio Explorer controls
    fun openDeviceAudioExplorer(targetLaneId: Long? = null) {
        val target = targetLaneId ?: _selectedLaneId.value ?: uiState.value.lanes.firstOrNull()?.laneId
        _deviceAudioTargetLaneId.value = target
        _isDeviceAudioExplorerOpen.value = true
        scanDeviceAudioFiles()
    }

    fun setDeviceAudioExplorerOpen(open: Boolean) {
        _isDeviceAudioExplorerOpen.value = open
        if (!open) {
            stopPreviewAudio()
        }
    }

    fun setDeviceAudioTargetLaneId(laneId: Long) {
        _deviceAudioTargetLaneId.value = laneId
    }

    fun scanDeviceAudioFiles() {
        viewModelScope.launch {
            _isScanningDeviceAudio.value = true
            try {
                val files = deviceAudioScanner.scanAudioFiles()
                _deviceAudioFiles.value = files
            } catch (e: Exception) {
                Log.e("TootPlayerVM", "Error scanning device audio: $e")
            } finally {
                _isScanningDeviceAudio.value = false
            }
        }
    }

    fun playPreviewAudio(uri: Uri) {
        if (_previewPlayingUri.value == uri) {
            stopPreviewAudio()
            return
        }
        stopPreviewAudio()
        try {
            previewPlayer = MediaPlayer().apply {
                setDataSource(getApplication(), uri)
                setOnPreparedListener { mp ->
                    mp.start()
                    _previewPlayingUri.value = uri
                }
                setOnCompletionListener {
                    _previewPlayingUri.value = null
                }
                setOnErrorListener { _, _, _ ->
                    _previewPlayingUri.value = null
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("TootPlayerVM", "Error playing preview audio: $e")
            _previewPlayingUri.value = null
        }
    }

    fun stopPreviewAudio() {
        try {
            previewPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (e: Exception) {
            Log.w("TootPlayerVM", "Error stopping preview player: $e")
        } finally {
            previewPlayer = null
            _previewPlayingUri.value = null
        }
    }

    fun searchWithAi(query: String, allTracks: List<DeviceAudioFile> = emptyList()) {
        val tracksToSearch = if (allTracks.isNotEmpty()) allTracks else _deviceAudioFiles.value
        if (query.isBlank()) {
            clearAiSearch()
            return
        }
        viewModelScope.launch {
            _isAiSearching.value = true
            _aiSearchActiveQuery.value = query
            _aiSearchImageUri.value = null
            try {
                val result = aiMusicSearchService.searchWithAi(query, tracksToSearch)
                val ids = result.getOrNull()
                _aiSearchMatchedIds.value = ids?.toSet()
            } catch (e: Exception) {
                Log.e("TootPlayerVM", "Error in searchWithAi: $e")
                val fallback = aiMusicSearchService.matchOffline(query, tracksToSearch)
                _aiSearchMatchedIds.value = fallback.toSet()
            } finally {
                _isAiSearching.value = false
            }
        }
    }

    fun searchWithAiImage(imageUri: Uri, allTracks: List<DeviceAudioFile> = emptyList()) {
        val tracksToSearch = if (allTracks.isNotEmpty()) allTracks else _deviceAudioFiles.value
        viewModelScope.launch {
            _isAiSearching.value = true
            _aiSearchImageUri.value = imageUri
            _aiSearchActiveQuery.value = null
            try {
                val result = aiMusicSearchService.searchWithImage(imageUri, tracksToSearch)
                if (result.isSuccess) {
                    val ids = result.getOrNull()
                    _aiSearchMatchedIds.value = ids?.toSet() ?: emptySet()
                    if (ids.isNullOrEmpty()) {
                        withContext(Dispatchers.Main) {
                            android.widget.Toast.makeText(
                                getApplication(),
                                if (_isPersianRtl.value) "هیچ آهنگی از این تصویر در حافظه دستگاه یافت نشد." else "No songs from this image were found on the device.",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "خطا در پردازش تصویر"
                    _aiSearchMatchedIds.value = emptySet()
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(getApplication(), errorMsg, android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("TootPlayerVM", "Error in searchWithAiImage: $e")
                _aiSearchMatchedIds.value = emptySet()
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        getApplication(),
                        e.localizedMessage ?: "Error in AI Image Search",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } finally {
                _isAiSearching.value = false
            }
        }
    }

    fun clearAiSearch() {
        _isAiSearching.value = false
        _aiSearchMatchedIds.value = null
        _aiSearchActiveQuery.value = null
        _aiSearchImageUri.value = null
    }

    fun selectAiProvider(providerId: String) {
        val provider = com.example.ai.AiProvider.fromId(providerId)
        aiMusicSearchService.setActiveProvider(provider)
        _activeAiProvider.value = provider.id
    }

    fun saveAiProviderApiKey(providerId: String, key: String) {
        val provider = com.example.ai.AiProvider.fromId(providerId)
        val trimmed = key.trim()
        aiMusicSearchService.saveApiKeyForProvider(provider, if (trimmed.isEmpty()) null else trimmed)
        when (provider) {
            com.example.ai.AiProvider.GEMINI -> _customGeminiApiKey.value = trimmed
            com.example.ai.AiProvider.OPENAI -> _customOpenAiApiKey.value = trimmed
            com.example.ai.AiProvider.DEEPSEEK -> _customDeepSeekApiKey.value = trimmed
            com.example.ai.AiProvider.GROQ -> _customGroqApiKey.value = trimmed
        }
    }

    fun clearAiProviderApiKey(providerId: String) {
        val provider = com.example.ai.AiProvider.fromId(providerId)
        aiMusicSearchService.saveApiKeyForProvider(provider, null)
        when (provider) {
            com.example.ai.AiProvider.GEMINI -> _customGeminiApiKey.value = ""
            com.example.ai.AiProvider.OPENAI -> _customOpenAiApiKey.value = ""
            com.example.ai.AiProvider.DEEPSEEK -> _customDeepSeekApiKey.value = ""
            com.example.ai.AiProvider.GROQ -> _customGroqApiKey.value = ""
        }
    }

    fun saveCustomGeminiApiKey(key: String) {
        saveAiProviderApiKey(com.example.ai.AiProvider.GEMINI.id, key)
    }

    fun clearCustomGeminiApiKey() {
        clearAiProviderApiKey(com.example.ai.AiProvider.GEMINI.id)
    }

    fun addDeviceAudioFilesToLane(laneId: Long, files: List<DeviceAudioFile>) {
        if (files.isEmpty()) return
        stopPreviewAudio()
        viewModelScope.launch(Dispatchers.IO) {
            val currentLane = uiState.value.lanes.find { it.laneId == laneId }
            val existingCount = currentLane?.playlist?.size ?: 0
            val maxAllowed = 100
            val remainingCapacity = (maxAllowed - existingCount).coerceAtLeast(0)
            val filesToAdd = files.take(remainingCapacity)
            if (filesToAdd.isEmpty()) return@launch

            val newEntities = filesToAdd.mapIndexed { index, file ->
                try {
                    val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    getApplication<Application>().contentResolver.takePersistableUriPermission(file.uri, flags)
                } catch (e: Exception) {
                    // Ignore non-persistable uri errors
                }
                val titleWithArtist = if (!file.artist.isNullOrBlank() && file.artist != "<unknown>") {
                    "${file.artist} - ${file.title}"
                } else {
                    file.title
                }
                com.example.data.db.PlaylistItemEntity(
                    laneId = laneId,
                    uriString = file.uri.toString(),
                    title = titleWithArtist,
                    durationMs = file.durationMs,
                    sortOrder = existingCount + index
                )
            }

            try {
                repository.addPlaylistItems(newEntities)
            } catch (e: Exception) {
                Log.e("TootPlayerVM", "Error adding device audio playlist items: $e")
            }

            // If the lane didn't have any audio file loaded, auto-load the first track
            if (currentLane?.fileName == null && files.isNotEmpty()) {
                val first = files.first()
                loadAudioForLane(laneId, first.uri)
            }
        }
    }

    // Playlist Controls for Lanes
    fun addAudioFilesToLanePlaylist(laneId: Long, uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val currentLane = uiState.value.lanes.find { it.laneId == laneId }
            val existingCount = currentLane?.playlist?.size ?: 0
            val maxAllowed = 100
            val remainingCapacity = (maxAllowed - existingCount).coerceAtLeast(0)
            val filesToAdd = uris.take(remainingCapacity)
            if (filesToAdd.isEmpty()) return@launch

            val newEntities = mutableListOf<com.example.data.db.PlaylistItemEntity>()

            filesToAdd.forEachIndexed { index, uri ->
                try {
                    val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    getApplication<Application>().contentResolver.takePersistableUriPermission(uri, flags)
                } catch (e: Exception) {
                    // Ignore non-persistable provider errors
                }

                var resolvedTitle = "Track ${existingCount + index + 1}"
                try {
                    getApplication<Application>().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1 && cursor.moveToFirst()) {
                            resolvedTitle = cursor.getString(nameIndex) ?: resolvedTitle
                        }
                    }
                } catch (e: Exception) {
                    resolvedTitle = uri.lastPathSegment ?: resolvedTitle
                }

                newEntities.add(
                    com.example.data.db.PlaylistItemEntity(
                        laneId = laneId,
                        uriString = uri.toString(),
                        title = resolvedTitle,
                        durationMs = 0L,
                        sortOrder = existingCount + index
                    )
                )
            }

            try {
                repository.addPlaylistItems(newEntities)
            } catch (e: Exception) {
                Log.e("TootPlayerVM", "Error saving playlist items: $e")
            }

            // If the lane didn't have any audio file loaded, auto-load the first added track
            if (currentLane?.fileName == null && filesToAdd.isNotEmpty()) {
                val firstUri = filesToAdd.first()
                loadAudioForLane(laneId, firstUri)
            }
        }
    }

    fun playLanePlaylistItem(laneId: Long, index: Int, autoStart: Boolean = true) {
        _selectedLaneId.value = laneId
        audioEngine.playPlaylistItem(laneId, index, autoStart = autoStart)
        
        val currentLane = uiState.value.lanes.find { it.laneId == laneId } ?: audioEngine.laneStates.value[laneId]
        val item = currentLane?.playlist?.getOrNull(index)
        if (item != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val dbLane = repository.getLaneById(laneId)
                    val loopState = currentLane?.isLoopEnabled ?: dbLane?.isLoopEnabled ?: false
                    val playlistLoopState = currentLane?.isPlaylistLoopEnabled ?: dbLane?.isPlaylistLoopEnabled ?: false
                    val shuffleState = currentLane?.isShuffleEnabled ?: dbLane?.isShuffleEnabled ?: false
                    val sortOrder = dbLane?.sortOrder ?: 0
                    repository.updateLane(
                        LaneEntity(
                            id = laneId,
                            name = currentLane?.name ?: dbLane?.name ?: "Lane $laneId",
                            uriString = item.uriString,
                            fileName = item.title,
                            durationMs = item.durationMs,
                            volume = currentLane?.volume ?: dbLane?.volume ?: 0.85f,
                            isMuted = currentLane?.isMuted ?: dbLane?.isMuted ?: false,
                            isSoloed = currentLane?.isSoloed ?: dbLane?.isSoloed ?: false,
                            playbackPositionMs = 0L,
                            sortOrder = sortOrder,
                            isLoopEnabled = loopState,
                            isPlaylistLoopEnabled = playlistLoopState,
                            isShuffleEnabled = shuffleState
                        )
                    )
                } catch (e: Exception) {
                    Log.e("TootPlayerVM", "Error updating lane with active track: $e")
                }
            }
        }
    }

    fun selectLanePlaylistItem(laneId: Long, index: Int) {
        val lane = uiState.value.lanes.find { it.laneId == laneId }
        // If the lane is already playing, switch and continue playing; otherwise just select and load without playing
        val shouldAutoStart = lane?.isPlaying == true
        playLanePlaylistItem(laneId, index, autoStart = shouldAutoStart)
    }

    fun playNextTrack(laneId: Long, autoStart: Boolean? = null) {
        val isCurrentlyPlaying = uiState.value.lanes.find { it.laneId == laneId }?.isPlaying == true
        audioEngine.playNextTrack(laneId, autoStart = autoStart ?: isCurrentlyPlaying)
    }

    fun playPreviousTrack(laneId: Long, autoStart: Boolean? = null) {
        val isCurrentlyPlaying = uiState.value.lanes.find { it.laneId == laneId }?.isPlaying == true
        audioEngine.playPreviousTrack(laneId, autoStart = autoStart ?: isCurrentlyPlaying)
    }

    fun toggleLaneLoop(laneId: Long) {
        audioEngine.toggleLaneLoop(laneId)
        val newLoop = audioEngine.laneStates.value[laneId]?.isLoopEnabled ?: false
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateLaneLoop(laneId, newLoop)
            } catch (e: Exception) {
                Log.w("TootPlayerViewModel", "Failed to persist lane loop in DB: $e")
            }
        }
    }

    fun toggleLanePlaylistLoop(laneId: Long) {
        audioEngine.toggleLanePlaylistLoop(laneId)
        val newLoop = audioEngine.laneStates.value[laneId]?.isPlaylistLoopEnabled ?: false
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateLanePlaylistLoop(laneId, newLoop)
            } catch (e: Exception) {
                Log.w("TootPlayerViewModel", "Failed to persist lane playlist loop in DB: $e")
            }
        }
    }

    fun toggleLaneShuffle(laneId: Long) {
        audioEngine.toggleLaneShuffle(laneId)
        val newShuffle = audioEngine.laneStates.value[laneId]?.isShuffleEnabled ?: false
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateLaneShuffle(laneId, newShuffle)
            } catch (e: Exception) {
                Log.w("TootPlayerViewModel", "Failed to persist lane shuffle in DB: $e")
            }
        }
    }

    fun removePlaylistItem(itemId: Long) {
        viewModelScope.launch {
            repository.deletePlaylistItem(itemId)
        }
    }

    fun clearLanePlaylist(laneId: Long) {
        viewModelScope.launch {
            repository.clearPlaylistForLane(laneId)
        }
    }

    fun sortLanePlaylist(laneId: Long, sortType: PlaylistSortType) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentEntities = repository.getPlaylistForLaneSync(laneId)
            if (currentEntities.isEmpty()) return@launch
            val sorted = when (sortType) {
                PlaylistSortType.NAME_ASC -> currentEntities.sortedBy { it.title.lowercase() }
                PlaylistSortType.NAME_DESC -> currentEntities.sortedByDescending { it.title.lowercase() }
                PlaylistSortType.DURATION_DESC -> currentEntities.sortedByDescending { it.durationMs }
                PlaylistSortType.DURATION_ASC -> currentEntities.sortedBy { it.durationMs }
                PlaylistSortType.DATE_ADDED -> currentEntities.sortedBy { it.id }
            }
            val updated = sorted.mapIndexed { index, entity ->
                entity.copy(sortOrder = index)
            }
            repository.reorderPlaylistItems(updated)

            val currentLane = uiState.value.lanes.find { it.laneId == laneId }
            if (currentLane != null) {
                val updatedPlaylists = updated.map {
                    com.example.audio.PlaylistItem(
                        id = it.id,
                        laneId = it.laneId,
                        uriString = it.uriString,
                        title = it.title,
                        durationMs = it.durationMs,
                        sortOrder = it.sortOrder
                    )
                }
                audioEngine.updateLanePlaylist(laneId, updatedPlaylists)
            }
        }
    }

    private fun logAndVerifyLoopConfigurationsOnLaneAddition(
        newLaneId: Long,
        newLaneName: String,
        preAdditionLoops: Map<Long, Boolean>
    ) {
        val currentEngineStates = audioEngine.laneStates.value
        val postAdditionLoops = currentEngineStates.mapValues { it.value.isLoopEnabled }
        val discrepancies = preAdditionLoops.filter { (id, wasLoop) ->
            val nowLoop = postAdditionLoops[id] ?: false
            nowLoop != wasLoop
        }

        if (discrepancies.isEmpty()) {
            Log.i(
                "AudioMixerViewModel",
                "[DIAGNOSTIC] New lane added successfully (id=$newLaneId, name=\"$newLaneName\"). Verified all ${preAdditionLoops.size} existing lane loop configurations remain 100% unchanged: $preAdditionLoops. Current engine states: $postAdditionLoops"
            )
        } else {
            Log.e(
                "AudioMixerViewModel",
                "[DIAGNOSTIC] CRITICAL: Loop configuration mismatch detected after adding lane $newLaneId ($newLaneName)! Discrepancies: $discrepancies. Previous: $preAdditionLoops, Current: $postAdditionLoops"
            )
        }
    }

    fun addNewLane(customName: String? = null) {
        viewModelScope.launch {
            val currentLanes = uiState.value.lanes
            val preAdditionLoops = currentLanes.associate { it.laneId to it.isLoopEnabled }
            val nextNumber = currentLanes.size + 1
            val fallbackName = if (_isPersianRtl.value) "لاین $nextNumber" else "Lane $nextNumber"
            val resolvedName = customName?.trim()?.ifEmpty { null } ?: fallbackName
            val newId = repository.addLaneAtTop(resolvedName)
            audioEngine.registerLane(
                laneId = newId,
                name = resolvedName,
                volume = 0.85f,
                isMuted = false,
                isSoloed = false,
                isLoopEnabled = false,
                isPlaylistLoopEnabled = false
            )
            _selectedLaneId.value = newId
            logAndVerifyLoopConfigurationsOnLaneAddition(newId, resolvedName, preAdditionLoops)
        }
    }

    fun renameLane(laneId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val current = uiState.value.lanes.find { it.laneId == laneId } ?: return@launch
            val dbLane = repository.getLaneById(laneId)
            val entity = LaneEntity(
                id = current.laneId,
                name = trimmed,
                uriString = current.uriString,
                fileName = current.fileName,
                durationMs = current.durationMs,
                volume = current.volume,
                isMuted = current.isMuted,
                isSoloed = current.isSoloed,
                playbackPositionMs = current.currentPositionMs,
                sortOrder = dbLane?.sortOrder ?: 0,
                isLoopEnabled = current.isLoopEnabled,
                isPlaylistLoopEnabled = current.isPlaylistLoopEnabled,
                isShuffleEnabled = current.isShuffleEnabled
            )
            repository.updateLane(entity)
            audioEngine.updateLaneName(laneId, trimmed)
            _laneToRename.value = null
        }
    }

    fun deleteLane(laneId: Long) {
        viewModelScope.launch {
            audioEngine.removeLane(laneId)
            repository.deleteLane(laneId)
            if (_selectedLaneId.value == laneId) {
                val remaining = uiState.value.lanes.filter { it.laneId != laneId }
                _selectedLaneId.value = remaining.firstOrNull()?.laneId
            }
        }
    }

    fun moveLaneUp(laneId: Long) {
        val currentLanes = uiState.value.lanes
        val currentIndex = currentLanes.indexOfFirst { it.laneId == laneId }
        if (currentIndex > 0) {
            val ids = currentLanes.map { it.laneId }.toMutableList()
            val item = ids.removeAt(currentIndex)
            ids.add(currentIndex - 1, item)
            viewModelScope.launch(Dispatchers.IO) {
                repository.reorderLanes(ids)
                audioEngine.applyAllLaneVolumes()
            }
        }
    }

    fun moveLaneDown(laneId: Long) {
        val currentLanes = uiState.value.lanes
        val currentIndex = currentLanes.indexOfFirst { it.laneId == laneId }
        if (currentIndex != -1 && currentIndex < currentLanes.size - 1) {
            val ids = currentLanes.map { it.laneId }.toMutableList()
            val item = ids.removeAt(currentIndex)
            ids.add(currentIndex + 1, item)
            viewModelScope.launch(Dispatchers.IO) {
                repository.reorderLanes(ids)
                audioEngine.applyAllLaneVolumes()
            }
        }
    }

    fun reorderLanes(fromIndex: Int, toIndex: Int) {
        val currentLanes = uiState.value.lanes
        if (fromIndex in currentLanes.indices && toIndex in currentLanes.indices && fromIndex != toIndex) {
            val ids = currentLanes.map { it.laneId }.toMutableList()
            val item = ids.removeAt(fromIndex)
            ids.add(toIndex, item)
            viewModelScope.launch(Dispatchers.IO) {
                repository.reorderLanes(ids)
                audioEngine.applyAllLaneVolumes()
            }
        }
    }

    fun persistCurrentLanesOrder() {
        val ids = uiState.value.lanes.map { it.laneId }
        if (ids.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.reorderLanes(ids)
                audioEngine.applyAllLaneVolumes()
            }
        }
    }

    fun loadAudioForLane(laneId: Long, uri: Uri, autoStart: Boolean = false, customTitle: String? = null) {
        try {
            if (uri.scheme == "content") {
                val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                getApplication<Application>().contentResolver.takePersistableUriPermission(uri, flags)
            }
        } catch (e: Exception) {
            // Some content providers do not support persistable URI grants
        }

        val immediateTitle = customTitle?.ifBlank { null } ?: when {
            uri.scheme == "file" && uri.path != null -> File(uri.path!!).nameWithoutExtension
            else -> uri.lastPathSegment ?: "Audio Track"
        }

        // 1. Immediately persist to Room DB so the UI updates without waiting for decoder/MediaPlayer
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val current = uiState.value.lanes.find { it.laneId == laneId } ?: audioEngine.laneStates.value[laneId]
                val dbLane = repository.getLaneById(laneId)
                val currentName = dbLane?.name?.takeIf { it.isNotBlank() } ?: current?.name?.takeIf { it.isNotBlank() } ?: "Lane $laneId"
                val currentVol = dbLane?.volume ?: current?.volume ?: 0.85f
                val currentMute = dbLane?.isMuted ?: current?.isMuted ?: false
                val currentSolo = dbLane?.isSoloed ?: current?.isSoloed ?: false
                val sortOrder = dbLane?.sortOrder ?: 0

                repository.updateLane(
                    LaneEntity(
                        id = laneId,
                        name = currentName,
                        uriString = uri.toString(),
                        fileName = immediateTitle,
                        durationMs = dbLane?.durationMs ?: 0L,
                        volume = currentVol,
                        isMuted = currentMute,
                        isSoloed = currentSolo,
                        playbackPositionMs = 0L,
                        sortOrder = sortOrder,
                        isLoopEnabled = current?.isLoopEnabled ?: false,
                        isPlaylistLoopEnabled = current?.isPlaylistLoopEnabled ?: false,
                        isShuffleEnabled = current?.isShuffleEnabled ?: false
                    )
                )

                repository.addLibraryTrack(
                    com.example.data.db.LibraryTrackEntity(
                        uriString = uri.toString(),
                        title = immediateTitle,
                        durationMs = dbLane?.durationMs ?: 0L,
                        sourceTag = "Imported"
                    )
                )

                val existingPlaylist = repository.getPlaylistForLaneSync(laneId)
                if (existingPlaylist.none { it.uriString == uri.toString() }) {
                    repository.addPlaylistItems(
                        listOf(
                            com.example.data.db.PlaylistItemEntity(
                                laneId = laneId,
                                uriString = uri.toString(),
                                title = immediateTitle,
                                durationMs = dbLane?.durationMs ?: 0L,
                                sortOrder = existingPlaylist.size
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("TootPlayerVM", "Error in immediate lane persistence: $e")
            }
        }

        // 2. Load into AudioEngine
        audioEngine.loadFileForLane(laneId, uri, autoStart = autoStart, customTitle = immediateTitle) { resolvedName, durationMs ->
            viewModelScope.launch(Dispatchers.IO) {
                val current = uiState.value.lanes.find { it.laneId == laneId } ?: audioEngine.laneStates.value[laneId]
                val dbLane = repository.getLaneById(laneId)
                val fallbackName = current?.name?.takeIf { it.isNotBlank() } ?: "Lane $laneId"
                val currentName = dbLane?.name?.takeIf { it.isNotBlank() } ?: fallbackName
                val currentVol = dbLane?.volume ?: current?.volume ?: 0.85f
                val currentMute = dbLane?.isMuted ?: current?.isMuted ?: false
                val currentSolo = dbLane?.isSoloed ?: current?.isSoloed ?: false
                val sortOrder = dbLane?.sortOrder ?: 0

                try {
                    repository.updateLane(
                        LaneEntity(
                            id = laneId,
                            name = currentName,
                            uriString = uri.toString(),
                            fileName = resolvedName,
                            durationMs = durationMs,
                            volume = currentVol,
                            isMuted = currentMute,
                            isSoloed = currentSolo,
                            playbackPositionMs = 0L,
                            sortOrder = sortOrder,
                            isLoopEnabled = current?.isLoopEnabled ?: false,
                            isPlaylistLoopEnabled = current?.isPlaylistLoopEnabled ?: false,
                            isShuffleEnabled = current?.isShuffleEnabled ?: false
                        )
                    )

                    repository.addLibraryTrack(
                        com.example.data.db.LibraryTrackEntity(
                            uriString = uri.toString(),
                            title = resolvedName,
                            durationMs = durationMs,
                            sourceTag = "Imported"
                        )
                    )

                    val existingPlaylist = repository.getPlaylistForLaneSync(laneId)
                    val alreadyInPlaylist = existingPlaylist.any { it.uriString == uri.toString() }
                    if (!alreadyInPlaylist) {
                        val newItem = com.example.data.db.PlaylistItemEntity(
                            laneId = laneId,
                            uriString = uri.toString(),
                            title = resolvedName,
                            durationMs = durationMs,
                            sortOrder = existingPlaylist.size
                        )
                        repository.addPlaylistItems(listOf(newItem))
                    }
                } catch (e: Exception) {
                    Log.e("TootPlayerVM", "Error updating lane with selected file: $e")
                }
            }
        }
    }

    fun togglePlayPauseLane(laneId: Long) {
        _selectedLaneId.value = laneId
        audioEngine.togglePlayPauseLane(laneId)
    }

    fun stopLane(laneId: Long) {
        audioEngine.stopLane(laneId)
    }

    fun seekLane(laneId: Long, positionMs: Long) {
        audioEngine.seekLaneTo(laneId, positionMs)
    }

    fun setLaneVolume(laneId: Long, volume: Float) {
        audioEngine.setLaneVolume(laneId, volume)
        saveLaneStateToDb(laneId)
    }

    fun toggleLaneMute(laneId: Long) {
        audioEngine.toggleLaneMute(laneId)
        saveLaneStateToDb(laneId)
    }

    fun toggleLaneSolo(laneId: Long) {
        audioEngine.toggleLaneSolo(laneId)
        saveLaneStateToDb(laneId)
    }

    fun clearAllSolos() {
        audioEngine.clearAllSolos()
        viewModelScope.launch {
            uiState.value.lanes.forEach { lane ->
                if (lane.isSoloed) {
                    saveLaneStateToDb(lane.laneId)
                }
            }
        }
    }

    fun clearAllMutes() {
        audioEngine.clearAllMutes()
        viewModelScope.launch {
            uiState.value.lanes.forEach { lane ->
                if (lane.isMuted) {
                    saveLaneStateToDb(lane.laneId)
                }
            }
        }
    }

    // Master Controls
    fun toggleMasterPlayPause() {
        val currentId = _selectedLaneId.value
        val selectedLane = uiState.value.lanes.find { it.laneId == currentId }
            ?: uiState.value.lanes.firstOrNull { it.fileName != null || it.playlist.isNotEmpty() }

        if (selectedLane != null) {
            audioEngine.togglePlayPauseLane(selectedLane.laneId)
        }
    }

    fun stopMaster() {
        val currentId = _selectedLaneId.value ?: return
        audioEngine.stopLane(currentId)
    }

    fun seekMaster(positionMs: Long) {
        val currentId = _selectedLaneId.value ?: return
        audioEngine.seekLaneTo(currentId, positionMs)
    }

    fun skipMaster(deltaMs: Long) {
        val currentId = _selectedLaneId.value ?: return
        audioEngine.skipLane(currentId, deltaMs)
    }

    fun playAll() { audioEngine.playAllLanes() }
    fun pauseAll() { audioEngine.pauseAllLanes() }
    fun stopAll() { audioEngine.stopAllLanes() }

    fun setMasterVolume(vol: Float) {
        audioEngine.setMasterVolume(vol)
        saveSettingsToDb()
    }

    fun toggleMasterMute() {
        audioEngine.toggleMasterMute()
        saveSettingsToDb()
    }

    fun setPlaybackSpeed(speed: Float) {
        audioEngine.setPlaybackSpeed(speed)
        saveSettingsToDb()
    }

    // Equalizer
    fun toggleEqualizer(enabled: Boolean) {
        audioEngine.setEqEnabled(enabled)
        saveSettingsToDb()
    }

    fun setEqBand(index: Int, gainDb: Float) {
        val currentBands = audioEngine.eqBands.value.toMutableList()
        if (index in currentBands.indices) {
            currentBands[index] = gainDb
            audioEngine.setEqBands(currentBands, "Custom")
            saveSettingsToDb()
        }
    }

    fun applyEqPreset(preset: EqPreset) {
        audioEngine.applyEqPreset(preset)
        saveSettingsToDb()
    }

    // Crossfader
    fun setCrossfadeLeft(laneId: Long?) { audioEngine.setCrossfadeLeftLane(laneId) }
    fun setCrossfadeRight(laneId: Long?) { audioEngine.setCrossfadeRightLane(laneId) }
    fun setCrossfadePosition(pos: Float) { audioEngine.setCrossfadePosition(pos) }
    fun setCrossfadeDurationSec(sec: Int) { audioEngine.setCrossfadeDurationSec(sec) }
    fun triggerAutoCrossfade(targetRight: Boolean) { audioEngine.triggerAutoCrossfade(targetRight) }

    // Sleep Timer
    fun startSleepTimer(minutes: Int) { audioEngine.startSleepTimer(minutes) }
    fun startSleepTimerSeconds(seconds: Int) { audioEngine.startSleepTimerSeconds(seconds) }
    fun addSleepTimerMinutes(minutes: Int) { audioEngine.addSleepTimerMinutes(minutes) }
    fun cancelSleepTimer() { audioEngine.cancelSleepTimer() }

    fun addRecordingToLane(laneId: Long, track: RecordedTrack) {
        val fileUri = Uri.fromFile(java.io.File(track.filePath))
        viewModelScope.launch(Dispatchers.IO) {
            val currentLane = uiState.value.lanes.find { it.laneId == laneId }
            val existingCount = currentLane?.playlist?.size ?: 0
            val newItem = com.example.data.db.PlaylistItemEntity(
                laneId = laneId,
                uriString = fileUri.toString(),
                title = track.title,
                durationMs = track.durationMs,
                sortOrder = existingCount
            )
            repository.addPlaylistItems(listOf(newItem))
            if (currentLane?.fileName == null) {
                loadAudioForLane(laneId, fileUri)
            }
        }
    }

    fun importFilesToLibrary(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val newTracks = mutableListOf<com.example.data.db.LibraryTrackEntity>()
            for (uri in uris) {
                try {
                    val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    getApplication<Application>().contentResolver.takePersistableUriPermission(uri, flags)
                } catch (e: Exception) {}

                var resolvedTitle = "Imported Audio"
                var sizeBytes = 0L
                try {
                    getApplication<Application>().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                        if (nameIndex != -1 && cursor.moveToFirst()) {
                            resolvedTitle = cursor.getString(nameIndex) ?: resolvedTitle
                        }
                        if (sizeIndex != -1 && cursor.moveToFirst()) {
                            sizeBytes = cursor.getLong(sizeIndex)
                        }
                    }
                } catch (e: Exception) {
                    resolvedTitle = uri.lastPathSegment ?: resolvedTitle
                }

                newTracks.add(
                    com.example.data.db.LibraryTrackEntity(
                        uriString = uri.toString(),
                        title = resolvedTitle,
                        artist = "User Import",
                        album = "My Library",
                        durationMs = 0L,
                        sizeBytes = sizeBytes,
                        sourceTag = "Imported"
                    )
                )
            }
            repository.addLibraryTracks(newTracks)
            scanDeviceAudioFiles()
        }
    }

    fun deleteLibraryTrack(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLibraryTrack(id)
            val updated = _deviceAudioFiles.value.filter { it.id != id }
            _deviceAudioFiles.value = updated
        }
    }

    private fun saveLaneStateToDb(laneId: Long) {
        viewModelScope.launch {
            val state = audioEngine.laneStates.value[laneId] ?: return@launch
            val dbLane = repository.getLaneById(laneId)
            val entity = LaneEntity(
                id = state.laneId,
                name = state.name,
                uriString = state.uriString,
                fileName = state.fileName,
                durationMs = state.durationMs,
                volume = state.volume,
                isMuted = state.isMuted,
                isSoloed = state.isSoloed,
                playbackPositionMs = state.currentPositionMs,
                sortOrder = dbLane?.sortOrder ?: 0,
                isLoopEnabled = state.isLoopEnabled,
                isPlaylistLoopEnabled = state.isPlaylistLoopEnabled,
                isShuffleEnabled = state.isShuffleEnabled
            )
            repository.updateLane(entity)
        }
    }

    private fun saveSettingsToDb() {
        viewModelScope.launch {
            val bands = audioEngine.eqBands.value
            val settings = AppSettingsEntity(
                masterVolume = audioEngine.masterVolume.value,
                isMasterMuted = audioEngine.isMasterMuted.value,
                playbackSpeed = audioEngine.playbackSpeed.value,
                selectedLaneId = _selectedLaneId.value ?: 1L,
                eqEnabled = audioEngine.eqEnabled.value,
                eqPreset = audioEngine.eqPresetName.value,
                eqBand0 = bands.getOrElse(0) { 0f },
                eqBand1 = bands.getOrElse(1) { 0f },
                eqBand2 = bands.getOrElse(2) { 0f },
                eqBand3 = bands.getOrElse(3) { 0f },
                eqBand4 = bands.getOrElse(4) { 0f },
                isPersianRtl = _activeLanguage.value.isRtl,
                languageCode = _activeLanguage.value.code,
                themeStyle = _themeStyle.value.name,
                isDarkMode = _isDarkMode.value
            )
            repository.saveSettings(settings)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPreviewAudio()
        masterRecorder.release()
        videoAudioExtractor.release()
        audioEngine.release()
    }
}
