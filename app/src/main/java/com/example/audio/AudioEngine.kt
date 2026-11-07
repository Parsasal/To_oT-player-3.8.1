package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.OpenableColumns
import android.util.Log
import com.example.service.AudioServiceBridge
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class AudioEngine(private val context: Context) {
    private val TAG = "AudioEngine"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mainHandler = Handler(Looper.getMainLooper())

    private val mediaPlayers = ConcurrentHashMap<Long, MediaPlayer>()
    private val equalizers = ConcurrentHashMap<Long, Equalizer>()

    // Master state
    private val _masterVolume = MutableStateFlow(0.9f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    private val _isMasterMuted = MutableStateFlow(false)
    val isMasterMuted: StateFlow<Boolean> = _isMasterMuted.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    // Crossfader state
    private val _crossfadeLeftLaneId = MutableStateFlow<Long?>(null)
    val crossfadeLeftLaneId: StateFlow<Long?> = _crossfadeLeftLaneId.asStateFlow()

    private val _crossfadeRightLaneId = MutableStateFlow<Long?>(null)
    val crossfadeRightLaneId: StateFlow<Long?> = _crossfadeRightLaneId.asStateFlow()

    private val _crossfadePosition = MutableStateFlow(0.5f) // 0.0 = 100% Left, 1.0 = 100% Right
    val crossfadePosition: StateFlow<Float> = _crossfadePosition.asStateFlow()

    private val _crossfadeDurationSec = MutableStateFlow(4)
    val crossfadeDurationSec: StateFlow<Int> = _crossfadeDurationSec.asStateFlow()

    private var crossfadeJob: Job? = null

    // Sleep Timer state
    private val _sleepTimerRemainingSec = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingSec: StateFlow<Int?> = _sleepTimerRemainingSec.asStateFlow()

    private val _sleepTimerInitialSec = MutableStateFlow<Int?>(null)
    val sleepTimerInitialSec: StateFlow<Int?> = _sleepTimerInitialSec.asStateFlow()

    private val _sleepTimerTargetEndTimeMs = MutableStateFlow<Long?>(null)
    val sleepTimerTargetEndTimeMs: StateFlow<Long?> = _sleepTimerTargetEndTimeMs.asStateFlow()

    private var sleepTimerJob: Job? = null
    private var sleepTimerFadeMultiplier = 1.0f

    // Equalizer state: 5 bands (-12dB to +12dB)
    // 0: Bass (60Hz), 1: Low-Mid (250Hz), 2: Mid (1kHz), 3: High-Mid (4kHz), 4: Treble (14kHz)
    private val _eqEnabled = MutableStateFlow(false)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled.asStateFlow()

    private val _eqBands = MutableStateFlow(listOf(0f, 0f, 0f, 0f, 0f))
    val eqBands: StateFlow<List<Float>> = _eqBands.asStateFlow()

    private val _eqPresetName = MutableStateFlow("Flat")
    val eqPresetName: StateFlow<String> = _eqPresetName.asStateFlow()

    // Real-time spectrum visualizer amplitudes (24 bands for rich visual rendering)
    private val _visualizerFrequencies = MutableStateFlow(FloatArray(24) { 0.05f })
    val visualizerFrequencies: StateFlow<FloatArray> = _visualizerFrequencies.asStateFlow()

    // Real-time per-lane frequency levels for individual lane waveform visualization
    private val _laneFrequencies = MutableStateFlow<Map<Long, FloatArray>>(emptyMap())
    val laneFrequencies: StateFlow<Map<Long, FloatArray>> = _laneFrequencies.asStateFlow()

    // Lane States
    private val _laneStates = MutableStateFlow<Map<Long, AudioLaneState>>(emptyMap())
    val laneStates: StateFlow<Map<Long, AudioLaneState>> = _laneStates.asStateFlow()

    // Explicit tracking of lanes that the user has started playing
    private val activePlayingLanes = java.util.concurrent.ConcurrentHashMap.newKeySet<Long>()

    // Persistence listener for exact playback timestamps
    var onLanePositionPersistRequested: ((laneId: Long, positionMs: Long) -> Unit)? = null
    var onLaneLoopPersistRequested: ((laneId: Long, isLoop: Boolean) -> Unit)? = null
    var onLanePlaylistLoopPersistRequested: ((laneId: Long, isLoop: Boolean) -> Unit)? = null
    var onLaneShufflePersistRequested: ((laneId: Long, isShuffle: Boolean) -> Unit)? = null

    // Track debounced timestamps for persistent saving
    private val lastPersistedPositionMap = java.util.concurrent.ConcurrentHashMap<Long, Long>()
    private val lastPersistTimestampMap = java.util.concurrent.ConcurrentHashMap<Long, Long>()

    // Track the last focused active lane for earbud resume
    var lastFocusedLaneId: Long? = null
        private set

    fun setLastFocusedLane(laneId: Long?) {
        lastFocusedLaneId = laneId
    }

    // Ticker job for updating positions and visualizer
    private var tickerJob: Job? = null

    val eqPresets = listOf(
        EqPreset("Flat", listOf(0f, 0f, 0f, 0f, 0f)),
        EqPreset("Vocal Booster", listOf(-2f, 1f, 4f, 3f, 1f)),
        EqPreset("Morning Bright", listOf(1f, 2f, 2f, 5f, 6f)),
        EqPreset("Night Warm", listOf(5f, 3f, 1f, -2f, -4f)),
        EqPreset("Bass Boost", listOf(6f, 4f, 1f, 0f, -1f))
    )

    val routingManager = AudioRoutingManager(context)

    init {
        AudioServiceBridge.registerAudioEngine(this)
        startTicker()
        routingManager.onRoutingChanged = {
            applyAudioRouting()
        }
    }

    fun applyAudioRouting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val dev = routingManager.selectedOutput.value?.audioDeviceInfo
            mediaPlayers.values.forEach { player ->
                try {
                    player.setPreferredDevice(dev)
                } catch (e: Exception) {
                    Log.w(TAG, "setPreferredDevice error: $e")
                }
            }
        }
    }

    fun syncServiceAndWidgets(activeLaneId: Long? = null) {
        try {
            AudioServiceBridge.onPlaybackStateChanged(
                context = context,
                lanes = _laneStates.value.values.toList(),
                activeLaneId = activeLaneId ?: _laneStates.value.values.find { it.isPlaying }?.laneId
            )
        } catch (e: Exception) {
            Log.w(TAG, "syncServiceAndWidgets non-fatal error: $e")
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch(Dispatchers.Default) {
            var phase = 0f
            while (isActive) {
                val anyPlaying = _laneStates.value.values.any { it.isPlaying }
                if (anyPlaying) {
                    try {
                        updatePlaybackPositions()
                        updateVisualizerSpectrum(phase)
                    } catch (e: Exception) {
                        Log.w(TAG, "Ticker loop non-fatal error: $e")
                    }
                    phase += 0.15f
                    delay(33) // ~30 FPS when active
                } else {
                    // When idle, sleep and do not emit continuous state updates
                    delay(500)
                }
            }
        }
    }

    private fun updatePlaybackPositions() {
        val currentStates = _laneStates.value.toMutableMap()
        var updated = false
        val now = System.currentTimeMillis()

        for ((laneId, player) in mediaPlayers) {
            val state = currentStates[laneId] ?: continue
            try {
                if (player.isPlaying) {
                    val pos = try { player.currentPosition.toLong() } catch (e: Exception) { state.currentPositionMs }
                    val dur = try { player.duration.toLong() } catch (e: Exception) { state.durationMs }
                    if (pos != state.currentPositionMs || dur != state.durationMs || !state.isPlaying) {
                        currentStates[laneId] = state.copy(
                            currentPositionMs = pos,
                            durationMs = if (dur > 0) dur else state.durationMs,
                            isPlaying = true
                        )
                        updated = true
                    }

                    // Periodic debounced position persistence every 1000ms
                    val lastPos = lastPersistedPositionMap[laneId] ?: 0L
                    val lastTime = lastPersistTimestampMap[laneId] ?: 0L
                    if (now - lastTime >= 1000L && Math.abs(pos - lastPos) >= 500L) {
                        lastPersistTimestampMap[laneId] = now
                        lastPersistedPositionMap[laneId] = pos
                        onLanePositionPersistRequested?.invoke(laneId, pos)
                    }
                } else if (state.isPlaying) {
                    val isLooping = try { player.isLooping } catch (e: Exception) { false }
                    if (!isLooping) {
                        currentStates[laneId] = state.copy(isPlaying = false)
                        updated = true
                    }
                }
            } catch (e: Exception) {
                // Catch IllegalStateException or other transient player state safely
            }
        }

        if (updated) {
            _laneStates.value = currentStates
        }
    }

    private fun updateVisualizerSpectrum(phase: Float) {
        val states = _laneStates.value.values
        val anyPlaying = states.any { it.isPlaying }

        val bands = FloatArray(24)
        val laneFreqMap = mutableMapOf<Long, FloatArray>()
        val eqGains = _eqBands.value
        val effectiveMasterVol = if (_isMasterMuted.value) 0f else _masterVolume.value * sleepTimerFadeMultiplier

        if (!anyPlaying) {
            // Idle gentle ambient breathing
            for (i in 0 until 24) {
                val ambient = 0.04f + 0.03f * sin(phase * 0.5f + i * 0.4f)
                bands[i] = max(0.02f, ambient)
            }
        } else {
            // Synthesize dynamic spectrum based on active playing lanes and EQ bands
            var totalAudibleVolume = 0f
            states.filter { it.isPlaying }.forEach { lane ->
                val laneVol = calculateAudibleVolume(lane.laneId)
                totalAudibleVolume += laneVol

                // Calculate per-lane 16-band frequency array for real-time waveform inspection
                val laneBands = FloatArray(16)
                val laneOffset = (lane.laneId % 5) * 0.7f
                for (b in 0 until 16) {
                    val laneEqIdx = when {
                        b < 3 -> 0
                        b < 6 -> 1
                        b < 10 -> 2
                        b < 13 -> 3
                        else -> 4
                    }
                    val laneEqBoost = if (_eqEnabled.value) {
                        (eqGains.getOrElse(laneEqIdx) { 0f } + 12f) / 24f
                    } else 0.5f

                    val wave = (sin(phase * 2.5f + b * 0.8f + laneOffset) * 0.35f +
                            cos(phase * 1.8f + b * 1.2f + laneOffset) * 0.35f + 0.5f)
                    laneBands[b] = min(1.0f, max(0.05f, (laneVol * effectiveMasterVol) * wave * (0.6f + 0.8f * laneEqBoost)))
                }
                laneFreqMap[lane.laneId] = laneBands
            }

            val baseEnergy = min(1.0f, totalAudibleVolume * effectiveMasterVol)

            for (i in 0 until 24) {
                // Map band index to 5 EQ sections (Bass, LowMid, Mid, HighMid, Treble)
                val eqIdx = when {
                    i < 4 -> 0 // Bass
                    i < 9 -> 1 // Low-Mid
                    i < 15 -> 2 // Mid
                    i < 20 -> 3 // High-Mid
                    else -> 4 // Treble
                }
                val eqBoost = if (_eqEnabled.value) {
                    (eqGains.getOrElse(eqIdx) { 0f } + 12f) / 24f // Normalized 0..1
                } else 0.5f

                val sinMod1 = sin(phase * 2.2f + i * 0.7f)
                val cosMod2 = cos(phase * 1.5f + i * 1.1f)
                val dynamicFlutter = (sinMod1 * 0.25f + cosMod2 * 0.25f + 0.5f)

                val rawAmp = baseEnergy * (0.3f + 0.7f * dynamicFlutter) * (0.6f + 0.8f * eqBoost)
                bands[i] = min(1.0f, max(0.04f, rawAmp))
            }
        }
        _visualizerFrequencies.value = bands
        _laneFrequencies.value = laneFreqMap
    }

    fun registerLane(
        laneId: Long,
        name: String,
        uriString: String? = null,
        fileName: String? = null,
        volume: Float = 0.85f,
        isMuted: Boolean = false,
        isSoloed: Boolean = false,
        playlist: List<PlaylistItem> = emptyList(),
        currentTrackIndex: Int = 0,
        isLoopEnabled: Boolean = false,
        isPlaylistLoopEnabled: Boolean = false,
        colorHex: String? = null,
        currentPositionMs: Long = 0L
    ) {
        val current = _laneStates.value.toMutableMap()
        val existing = current[laneId]
        current[laneId] = AudioLaneState(
            laneId = laneId,
            name = name,
            uriString = uriString ?: existing?.uriString,
            fileName = fileName ?: existing?.fileName,
            volume = existing?.volume ?: volume,
            isMuted = existing?.isMuted ?: isMuted,
            isSoloed = existing?.isSoloed ?: isSoloed,
            isPlaying = existing?.isPlaying ?: false,
            playlist = if (playlist.isNotEmpty()) playlist else (existing?.playlist ?: emptyList()),
            currentTrackIndex = if (playlist.isNotEmpty()) currentTrackIndex else (existing?.currentTrackIndex ?: 0),
            isLoopEnabled = existing?.isLoopEnabled ?: isLoopEnabled,
            isPlaylistLoopEnabled = existing?.isPlaylistLoopEnabled ?: isPlaylistLoopEnabled,
            isShuffleEnabled = existing?.isShuffleEnabled ?: false,
            colorHex = colorHex ?: existing?.colorHex,
            currentPositionMs = if (currentPositionMs > 0L) currentPositionMs else (existing?.currentPositionMs ?: 0L)
        )
        _laneStates.value = current
        applyAllLaneVolumes()

        // Only load file if player not already prepared for this lane
        if (!mediaPlayers.containsKey(laneId)) {
            val targetUri = uriString ?: existing?.uriString
            val targetPlaylist = if (playlist.isNotEmpty()) playlist else (existing?.playlist ?: emptyList())
            if (targetUri != null) {
                loadFileForLane(laneId, Uri.parse(targetUri), autoStart = false, initialPositionMs = currentPositionMs)
            } else if (targetPlaylist.isNotEmpty()) {
                val validIndex = currentTrackIndex.coerceIn(0, targetPlaylist.size - 1)
                playPlaylistItem(laneId, validIndex, autoStart = false, initialPositionMs = currentPositionMs)
            }
        }
    }

    fun updateLaneColor(laneId: Long, colorHex: String?) {
        val current = _laneStates.value.toMutableMap()
        val state = current[laneId] ?: AudioLaneState(laneId = laneId, name = "Lane $laneId")
        current[laneId] = state.copy(colorHex = colorHex)
        _laneStates.value = current
    }

    fun updateLanePlaylist(laneId: Long, playlist: List<PlaylistItem>) {
        val current = _laneStates.value.toMutableMap()
        val state = current[laneId] ?: return
        val newIndex = if (playlist.isEmpty()) 0 else state.currentTrackIndex.coerceIn(0, playlist.size - 1)
        
        if (playlist.isEmpty()) {
            if (state.playlist.isNotEmpty()) {
                current[laneId] = state.copy(
                    playlist = emptyList(),
                    currentTrackIndex = 0,
                    uriString = null,
                    fileName = null,
                    durationMs = 0L,
                    isPlaying = false,
                    currentPositionMs = 0L
                )
                _laneStates.value = current
                stopLane(laneId)
            }
            return
        }

        val currentUriInList = playlist.any { it.uriString == state.uriString }
        val newTrack = if (currentUriInList) {
            playlist.find { it.uriString == state.uriString }
        } else {
            playlist.getOrNull(newIndex)
        }

        val updatedIndex = if (currentUriInList) {
            playlist.indexOfFirst { it.uriString == state.uriString }.coerceAtLeast(0)
        } else {
            newIndex
        }

        current[laneId] = state.copy(
            playlist = playlist,
            currentTrackIndex = updatedIndex,
            fileName = newTrack?.title ?: state.fileName,
            uriString = newTrack?.uriString ?: state.uriString,
            durationMs = if (newTrack != null && newTrack.durationMs > 0) newTrack.durationMs else state.durationMs
        )
        _laneStates.value = current

        // If no file loaded yet and playlist now has items, load the first item into player (paused)
        if (state.uriString == null && playlist.isNotEmpty()) {
            playPlaylistItem(laneId, 0, autoStart = false)
        } else if (!currentUriInList && newTrack != null) {
            // Active item was removed, load new active item in stopped/paused state unless explicitly played
            playPlaylistItem(laneId, updatedIndex, autoStart = false)
        }
    }

    fun toggleLaneLoop(laneId: Long) {
        val current = _laneStates.value.toMutableMap()
        val state = current[laneId] ?: AudioLaneState(laneId = laneId, name = "Lane $laneId", volume = 0.85f)
        val newLoop = !state.isLoopEnabled
        current[laneId] = state.copy(isLoopEnabled = newLoop)
        _laneStates.value = current

        // Set native MediaPlayer.isLooping for gapless repeat of the exact currently playing track
        val player = mediaPlayers[laneId]
        try {
            player?.isLooping = newLoop
        } catch (e: Exception) {
            Log.w(TAG, "Error updating player.isLooping on toggle: $e")
        }
        onLaneLoopPersistRequested?.invoke(laneId, newLoop)
    }

    fun toggleLanePlaylistLoop(laneId: Long) {
        val current = _laneStates.value.toMutableMap()
        val state = current[laneId] ?: AudioLaneState(laneId = laneId, name = "Lane $laneId", volume = 0.85f)
        val newLoop = !state.isPlaylistLoopEnabled
        current[laneId] = state.copy(isPlaylistLoopEnabled = newLoop)
        _laneStates.value = current
        onLanePlaylistLoopPersistRequested?.invoke(laneId, newLoop)
    }

    fun toggleLaneShuffle(laneId: Long) {
        val current = _laneStates.value.toMutableMap()
        val state = current[laneId] ?: AudioLaneState(laneId = laneId, name = "Lane $laneId", volume = 0.85f)
        val newShuffle = !state.isShuffleEnabled
        current[laneId] = state.copy(isShuffleEnabled = newShuffle)
        _laneStates.value = current
        onLaneShufflePersistRequested?.invoke(laneId, newShuffle)
    }

    fun playNextTrack(laneId: Long, autoStart: Boolean? = null) {
        val state = _laneStates.value[laneId] ?: return
        val list = state.playlist
        if (list.isEmpty()) {
            if (state.isLoopEnabled || state.isPlaylistLoopEnabled) {
                seekLaneTo(laneId, 0L)
                if (autoStart != false) {
                    playLane(laneId)
                }
            }
            return
        }

        val shouldAutoStart = autoStart ?: activePlayingLanes.contains(laneId) ?: state.isPlaying
        val nextIndex = if (state.isShuffleEnabled && list.size > 1) {
            val candidates = list.indices.filter { it != state.currentTrackIndex }
            candidates.random()
        } else {
            val idx = state.currentTrackIndex + 1
            if (idx >= list.size) {
                if (state.isPlaylistLoopEnabled) {
                    // Loop playlist: restart from the first music of this lane
                    0
                } else {
                    activePlayingLanes.remove(laneId)
                    stopLane(laneId)
                    return
                }
            } else idx
        }

        playPlaylistItem(laneId, nextIndex, autoStart = shouldAutoStart)
    }

    fun playPreviousTrack(laneId: Long, autoStart: Boolean? = null) {
        val state = _laneStates.value[laneId] ?: return
        val list = state.playlist
        if (list.isEmpty()) {
            seekLaneTo(laneId, 0L)
            if (autoStart == true) playLane(laneId)
            return
        }

        val shouldAutoStart = autoStart ?: activePlayingLanes.contains(laneId) ?: state.isPlaying
        // If played more than 3 seconds, restart current track
        if (state.currentPositionMs > 3000L) {
            seekLaneTo(laneId, 0L)
            if (shouldAutoStart) playLane(laneId)
            return
        }

        val prevIndex = if (state.currentTrackIndex - 1 < 0) {
            if (state.isPlaylistLoopEnabled && list.isNotEmpty()) {
                list.size - 1
            } else {
                0
            }
        } else {
            state.currentTrackIndex - 1
        }

        playPlaylistItem(laneId, prevIndex, autoStart = shouldAutoStart)
    }

    fun playPlaylistItem(laneId: Long, index: Int, autoStart: Boolean = false, initialPositionMs: Long = 0L) {
        if (autoStart) {
            activePlayingLanes.add(laneId)
        } else {
            activePlayingLanes.remove(laneId)
        }
        val current = _laneStates.value.toMutableMap()
        val state = current[laneId] ?: return
        val list = state.playlist
        if (index !in list.indices) return

        val item = list[index]
        current[laneId] = state.copy(
            currentTrackIndex = index,
            uriString = item.uriString,
            fileName = item.title,
            durationMs = item.durationMs,
            isPlaying = autoStart,
            currentPositionMs = initialPositionMs
        )
        _laneStates.value = current

        loadFileForLane(laneId, Uri.parse(item.uriString), autoStart = autoStart, initialPositionMs = initialPositionMs)
    }

    fun removeLane(laneId: Long) {
        stopLane(laneId)
        val player = mediaPlayers.remove(laneId)
        try {
            player?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing player: $e")
        }

        val eq = equalizers.remove(laneId)
        try {
            eq?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing equalizer: $e")
        }

        val current = _laneStates.value.toMutableMap()
        current.remove(laneId)
        _laneStates.value = current

        if (_crossfadeLeftLaneId.value == laneId) _crossfadeLeftLaneId.value = null
        if (_crossfadeRightLaneId.value == laneId) _crossfadeRightLaneId.value = null
        applyAllLaneVolumes()
    }

    fun updateLaneName(laneId: Long, newName: String) {
        val current = _laneStates.value.toMutableMap()
        current[laneId]?.let {
            current[laneId] = it.copy(name = newName)
            _laneStates.value = current
        }
    }

    fun loadFileForLane(
        laneId: Long,
        uri: Uri,
        autoStart: Boolean = false,
        initialPositionMs: Long = 0L,
        customTitle: String? = null,
        onLoaded: ((name: String, durationMs: Long) -> Unit)? = null
    ) {
        val current = _laneStates.value.toMutableMap()
        val state = current[laneId] ?: AudioLaneState(
            laneId = laneId,
            name = customTitle ?: "Lane $laneId",
            volume = 0.85f
        ).also {
            current[laneId] = it
        }

        current[laneId] = state.copy(isLoading = true, errorMessage = null)
        _laneStates.value = current

        scope.launch(Dispatchers.IO) {
            try {
                // Get filename & duration
                var resolvedName = customTitle
                if (resolvedName.isNullOrBlank()) {
                    if (uri.scheme == "file" || (uri.scheme == null && uri.path != null)) {
                        try {
                            val path = uri.path
                            if (path != null) {
                                val localFile = File(path)
                                if (localFile.exists() && localFile.name.isNotBlank()) {
                                    resolvedName = localFile.name
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
                if (resolvedName.isNullOrBlank()) {
                    try {
                        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex != -1 && cursor.moveToFirst()) {
                                resolvedName = cursor.getString(nameIndex) ?: resolvedName
                            }
                        }
                    } catch (e: Exception) {
                        resolvedName = uri.lastPathSegment ?: "Audio Track"
                    }
                }
                if (resolvedName.isNullOrBlank()) {
                    resolvedName = uri.lastPathSegment ?: "Audio Track"
                }

                // Release previous equalizer and player for this lane
                equalizers.remove(laneId)?.let { oldEq ->
                    try {
                        oldEq.enabled = false
                        oldEq.release()
                    } catch (e: Exception) {
                        Log.w(TAG, "Old equalizer cleanup error: $e")
                    }
                }

                mediaPlayers.remove(laneId)?.let { oldPlayer ->
                    try {
                        if (oldPlayer.isPlaying) oldPlayer.stop()
                        oldPlayer.reset()
                        oldPlayer.release()
                    } catch (e: Exception) {
                        Log.w(TAG, "Old player cleanup error: $e")
                    }
                }

                val newPlayer = MediaPlayer().apply {
                    try {
                        setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to set wake mode on MediaPlayer: $e")
                    }
                    setAudioAttributes(routingManager.buildAudioAttributes())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        routingManager.selectedOutput.value?.audioDeviceInfo?.let { dev ->
                            try {
                                setPreferredDevice(dev)
                            } catch (e: Exception) {
                                Log.w(TAG, "setPreferredDevice on newPlayer error: $e")
                            }
                        }
                    }

                    var preparedSuccessfully = false

                    // Attempt 1: Direct file access if file:// URI or local filesystem path via open FileInputStream descriptor
                    if (uri.scheme == "file" || (uri.scheme == null && uri.path != null)) {
                        try {
                            val path = uri.path
                            if (path != null) {
                                val localFile = File(path)
                                if (localFile.exists()) {
                                    java.io.FileInputStream(localFile).use { fis ->
                                        setDataSource(fis.fd, 0, localFile.length())
                                        prepare()
                                        preparedSuccessfully = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Direct local file FileInputStream setDataSource failed: $e, falling back to absolutePath")
                            try {
                                reset()
                                val path = uri.path
                                if (path != null) {
                                    val localFile = File(path)
                                    if (localFile.exists()) {
                                        setDataSource(localFile.absolutePath)
                                        prepare()
                                        preparedSuccessfully = true
                                    }
                                }
                            } catch (e2: Exception) {
                                reset()
                            }
                        }
                    }

                    // Attempt 2: Standard Android Context + Uri handling (ideal for content://)
                    if (!preparedSuccessfully) {
                        try {
                            setDataSource(context, uri)
                            prepare()
                            preparedSuccessfully = true
                        } catch (e: Exception) {
                            Log.w(TAG, "setDataSource(context, uri) failed: $e, falling back to FileDescriptor")
                            reset()
                        }
                    }

                    // Attempt 3: ContentResolver openFileDescriptor with safe lifetime
                    if (!preparedSuccessfully) {
                        try {
                            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                                if (pfd.statSize > 0) {
                                    setDataSource(pfd.fileDescriptor, 0, pfd.statSize)
                                } else {
                                    setDataSource(pfd.fileDescriptor)
                                }
                                prepare()
                                preparedSuccessfully = true
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "openFileDescriptor fallback failed: $e")
                            reset()
                        }
                    }

                    // Attempt 4: Fallback for raw file path via FileInputStream
                    if (!preparedSuccessfully && uri.path != null) {
                        try {
                            val localFile = File(uri.path!!)
                            if (localFile.exists()) {
                                java.io.FileInputStream(localFile).use { fis ->
                                    setDataSource(fis.fd, 0, localFile.length())
                                    prepare()
                                    preparedSuccessfully = true
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Raw file path fallback failed: $e")
                            reset()
                        }
                    }

                    if (!preparedSuccessfully) {
                        throw IllegalStateException("Failed to initialize media player for uri: $uri")
                    }
                }

                val duration = try {
                    newPlayer.duration.toLong()
                } catch (e: Exception) {
                    0L
                }
                mediaPlayers[laneId] = newPlayer

                // Configure EQ for this player's session safely
                try {
                    val sessionId = newPlayer.audioSessionId
                    if (sessionId > 0) {
                        attachEqualizer(laneId, sessionId)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not attach EQ to session: $e")
                }

                // Apply initial volume
                applyLaneVolume(laneId)

                // Engage native gapless looping for the currently playing track
                val isLoopActive = _laneStates.value[laneId]?.isLoopEnabled == true
                try {
                    newPlayer.isLooping = isLoopActive
                } catch (e: Exception) {
                    Log.w(TAG, "Error setting initial newPlayer.isLooping: $e")
                }

                newPlayer.setOnCompletionListener {
                    mainHandler.post {
                        val st = _laneStates.value[laneId]
                        if (st != null) {
                            if (st.isLoopEnabled) {
                                // Loop specifically repeats the EXACT currently playing music, not the playlist
                                activePlayingLanes.add(laneId)
                                try {
                                    newPlayer.isLooping = true
                                    newPlayer.seekTo(0)
                                    applyPlaybackSpeed(newPlayer, _playbackSpeed.value)
                                    newPlayer.start()
                                    applyLaneVolume(laneId)
                                    val map = _laneStates.value.toMutableMap()
                                    map[laneId]?.let { s ->
                                        map[laneId] = s.copy(isPlaying = true, currentPositionMs = 0L)
                                        _laneStates.value = map
                                    }
                                    syncServiceAndWidgets(laneId)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error restarting single track loop, falling back to reload: $e")
                                    val uriStr = st.uriString ?: st.playlist.getOrNull(st.currentTrackIndex)?.uriString
                                    if (uriStr != null) {
                                        loadFileForLane(laneId, Uri.parse(uriStr), autoStart = true, initialPositionMs = 0L)
                                    }
                                }
                            } else {
                                if (st.playlist.size > 1 && st.currentTrackIndex < st.playlist.size - 1) {
                                    activePlayingLanes.add(laneId)
                                    playNextTrack(laneId, autoStart = true)
                                } else if (st.isPlaylistLoopEnabled && st.playlist.isNotEmpty()) {
                                    // Playlist reached end: restart playing from the first track of this lane
                                    activePlayingLanes.add(laneId)
                                    playPlaylistItem(laneId, 0, autoStart = true)
                                } else {
                                    activePlayingLanes.remove(laneId)
                                    val map = _laneStates.value.toMutableMap()
                                    map[laneId]?.let { s ->
                                        map[laneId] = s.copy(isPlaying = false, currentPositionMs = 0L)
                                        _laneStates.value = map
                                    }
                                    syncServiceAndWidgets(laneId)
                                }
                            }
                        }
                    }
                }

                newPlayer.setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error on lane $laneId: what=$what, extra=$extra")
                    activePlayingLanes.remove(laneId)
                    val map = _laneStates.value.toMutableMap()
                    map[laneId]?.let { st ->
                        map[laneId] = st.copy(
                            isPlaying = false,
                            isLoading = false,
                            errorMessage = "Audio playback error (code $what). Format may be unsupported."
                        )
                        _laneStates.value = map
                    }
                    true
                }

                if (initialPositionMs > 0L) {
                    try {
                        newPlayer.seekTo(initialPositionMs.toInt())
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to apply initial seek to $initialPositionMs: $e")
                    }
                }

                mainHandler.post {
                    val map = _laneStates.value.toMutableMap()
                    val currentLatest = map[laneId] ?: state
                    if (autoStart) {
                        activePlayingLanes.add(laneId)
                    }
                    val shouldStart = autoStart
                    val restoredPos = if (initialPositionMs > 0L) initialPositionMs else 0L
                    val finalTrackTitle = resolvedName ?: "Audio Track"
                    val finalDuration = if (duration > 0) duration else 0L

                    val updatedPlaylist = if (currentLatest.playlist.none { it.uriString == uri.toString() }) {
                        currentLatest.playlist + PlaylistItem(
                            laneId = laneId,
                            uriString = uri.toString(),
                            title = finalTrackTitle,
                            durationMs = finalDuration,
                            sortOrder = currentLatest.playlist.size
                        )
                    } else {
                        currentLatest.playlist
                    }
                    val matchingIdx = updatedPlaylist.indexOfFirst { it.uriString == uri.toString() }
                    val updatedTrackIdx = if (matchingIdx >= 0) matchingIdx else currentLatest.currentTrackIndex

                    map[laneId] = currentLatest.copy(
                        uriString = uri.toString(),
                        fileName = finalTrackTitle,
                        durationMs = finalDuration,
                        playlist = updatedPlaylist,
                        currentPositionMs = restoredPos,
                        currentTrackIndex = updatedTrackIdx,
                        isLoading = false,
                        isPlaying = shouldStart,
                        errorMessage = null
                    )
                    _laneStates.value = map
                    if (shouldStart) {
                        try {
                            applyPlaybackSpeed(newPlayer, _playbackSpeed.value)
                            newPlayer.start()
                            applyLaneVolume(laneId)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error autostarting player: $e")
                        }
                    } else {
                        try {
                            if (newPlayer.isPlaying) {
                                newPlayer.pause()
                            }
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                    onLoaded?.invoke(finalTrackTitle, if (duration > 0) duration else 0L)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load audio for lane $laneId: $e")
                mainHandler.post {
                    val map = _laneStates.value.toMutableMap()
                    map[laneId] = state.copy(
                        isLoading = false,
                        isPlaying = false,
                        errorMessage = "Cannot play file: ${e.localizedMessage ?: "Invalid or unsupported format"}"
                    )
                    _laneStates.value = map
                }
            }
        }
    }

    fun playLane(laneId: Long) {
        lastFocusedLaneId = laneId
        activePlayingLanes.add(laneId)
        val state = _laneStates.value[laneId] ?: return
        val player = mediaPlayers[laneId]
        if (player == null) {
            if (state.playlist.isNotEmpty()) {
                val idx = state.currentTrackIndex.coerceIn(0, state.playlist.size - 1)
                playPlaylistItem(laneId, idx, autoStart = true)
                return
            } else if (state.uriString != null) {
                loadFileForLane(laneId, Uri.parse(state.uriString), autoStart = true)
                return
            }
        }
        try {
            if (player != null) {
                applyPlaybackSpeed(player, _playbackSpeed.value)
                if (!player.isPlaying) {
                    player.start()
                }
                applyLaneVolume(laneId)
                val current = _laneStates.value.toMutableMap()
                current[laneId] = state.copy(isPlaying = true)
                _laneStates.value = current
                syncServiceAndWidgets(laneId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting playback on lane $laneId: $e")
        }
    }

    /**
     * Resumes playback on the specified lane from the exact stored millisecond timestamp
     * with a gentle fade-in ramp (e.g. for Bluetooth earbud touch resume).
     */
    fun fadeResumeLane(laneId: Long, fadeDurationMs: Long = 150L) {
        lastFocusedLaneId = laneId
        activePlayingLanes.add(laneId)
        val state = _laneStates.value[laneId] ?: return
        val player = mediaPlayers[laneId]

        if (player == null) {
            if (state.playlist.isNotEmpty()) {
                val idx = state.currentTrackIndex.coerceIn(0, state.playlist.size - 1)
                playPlaylistItem(laneId, idx, autoStart = true, initialPositionMs = state.currentPositionMs)
                return
            } else if (state.uriString != null) {
                loadFileForLane(laneId, Uri.parse(state.uriString), autoStart = true, initialPositionMs = state.currentPositionMs)
                return
            }
            return
        }

        scope.launch(Dispatchers.Main) {
            try {
                applyPlaybackSpeed(player, _playbackSpeed.value)
                
                // Ensure starting from stored millisecond timestamp
                if (state.currentPositionMs > 0 && Math.abs(player.currentPosition - state.currentPositionMs) > 200) {
                    player.seekTo(state.currentPositionMs.toInt())
                }

                // Start from 0 volume for smooth ramp
                player.setVolume(0f, 0f)
                if (!player.isPlaying) {
                    player.start()
                }

                val current = _laneStates.value.toMutableMap()
                current[laneId] = state.copy(isPlaying = true)
                _laneStates.value = current
                syncServiceAndWidgets(laneId)

                // Smooth fade-in ramp
                val targetVol = calculateAudibleVolume(laneId)
                val steps = 15
                val stepDelay = max(5L, fadeDurationMs / steps)

                for (step in 1..steps) {
                    val factor = step / steps.toFloat()
                    val currentVol = targetVol * factor
                    try {
                        player.setVolume(currentVol, currentVol)
                    } catch (e: Exception) {
                        Log.w(TAG, "Error ramping volume up for lane $laneId: $e")
                    }
                    delay(stepDelay)
                }

                applyLaneVolume(laneId)
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming lane with fade-in: $e")
                applyLaneVolume(laneId)
            }
        }
    }

    /**
     * Resumes playback on the last focused active lane (or all active playing lanes).
     */
    fun resumeLastFocusedLaneOrAll(fadeDurationMs: Long = 150L) {
        val targetId = lastFocusedLaneId
            ?: _laneStates.value.values.find { it.isPlaying }?.laneId
            ?: _laneStates.value.keys.firstOrNull()

        if (targetId != null) {
            fadeResumeLane(targetId, fadeDurationMs)
        } else {
            playAllLanes()
        }
    }

    fun pauseLane(laneId: Long) {
        interruptedLanesOnTransientLoss.remove(laneId)
        activePlayingLanes.remove(laneId)
        val player = mediaPlayers[laneId]
        val state = _laneStates.value[laneId] ?: return
        try {
            val exactPos = if (player != null) player.currentPosition.toLong() else state.currentPositionMs
            if (player != null && player.isPlaying) {
                player.pause()
            }
            val current = _laneStates.value.toMutableMap()
            current[laneId] = state.copy(isPlaying = false, currentPositionMs = exactPos)
            _laneStates.value = current
            onLanePositionPersistRequested?.invoke(laneId, exactPos)
            syncServiceAndWidgets(laneId)
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing lane $laneId: $e")
        }
    }

    /**
     * Smoothly pauses a specific lane with a 100ms micro fade-out and persists exact timestamp.
     */
    fun fadePauseLane(laneId: Long, fadeDurationMs: Long = 100L, onComplete: (() -> Unit)? = null) {
        val state = _laneStates.value[laneId] ?: run {
            onComplete?.invoke()
            return
        }
        val player = mediaPlayers[laneId]
        if (player == null || !state.isPlaying) {
            onComplete?.invoke()
            return
        }

        lastFocusedLaneId = laneId

        scope.launch(Dispatchers.Main) {
            val steps = 10
            val stepDelay = max(5L, fadeDurationMs / steps)
            val baseVol = calculateAudibleVolume(laneId)

            for (step in (steps - 1) downTo 0) {
                val factor = step / steps.toFloat()
                val currentVol = baseVol * factor
                try {
                    player.setVolume(currentVol, currentVol)
                } catch (e: Exception) {
                    Log.w(TAG, "Error fading lane $laneId: $e")
                }
                delay(stepDelay)
            }

            activePlayingLanes.remove(laneId)
            interruptedLanesOnTransientLoss.remove(laneId)
            try {
                val exactPos = player.currentPosition.toLong()
                player.pause()
                val current = _laneStates.value.toMutableMap()
                current[laneId] = state.copy(isPlaying = false, currentPositionMs = exactPos)
                _laneStates.value = current
                onLanePositionPersistRequested?.invoke(laneId, exactPos)
            } catch (e: Exception) {
                Log.e(TAG, "Error finalizing lane pause: $e")
            }

            applyLaneVolume(laneId)
            syncServiceAndWidgets(laneId)
            onComplete?.invoke()
        }
    }

    /**
     * Smoothly pauses all active lanes with a 100ms micro fade-out to prevent sudden loud speaker output
     * when earbuds are removed or disconnected, and immediately persists exact playback timestamps.
     */
    fun fadePauseAll(fadeDurationMs: Long = 100L, onComplete: (() -> Unit)? = null) {
        val playingLaneIds = _laneStates.value.filter { it.value.isPlaying }.keys.toList()
        if (playingLaneIds.isEmpty()) {
            onComplete?.invoke()
            return
        }

        // Keep track of the first playing lane as last focused
        if (lastFocusedLaneId == null || !playingLaneIds.contains(lastFocusedLaneId)) {
            lastFocusedLaneId = playingLaneIds.firstOrNull()
        }

        scope.launch(Dispatchers.Main) {
            val steps = 10
            val stepDelay = max(5L, fadeDurationMs / steps)
            val initialVolumes = mutableMapOf<Long, Float>()
            for (laneId in playingLaneIds) {
                initialVolumes[laneId] = calculateAudibleVolume(laneId)
            }

            for (step in (steps - 1) downTo 0) {
                val factor = step / steps.toFloat()
                for (laneId in playingLaneIds) {
                    val base = initialVolumes[laneId] ?: 0.5f
                    val currentVol = base * factor
                    mediaPlayers[laneId]?.let { player ->
                        try {
                            player.setVolume(currentVol, currentVol)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error fading out player $laneId: $e")
                        }
                    }
                }
                delay(stepDelay)
            }

            // Pause all players, capture exact millisecond positions and persist
            val current = _laneStates.value.toMutableMap()
            for (laneId in playingLaneIds) {
                activePlayingLanes.remove(laneId)
                val player = mediaPlayers[laneId]
                val state = current[laneId]
                if (player != null && state != null) {
                    try {
                        val exactPos = player.currentPosition.toLong()
                        player.pause()
                        current[laneId] = state.copy(isPlaying = false, currentPositionMs = exactPos)
                        onLanePositionPersistRequested?.invoke(laneId, exactPos)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error finalizing pause on lane $laneId: $e")
                    }
                }
            }
            _laneStates.value = current

            // Restore calculated volumes for future playback
            for (laneId in playingLaneIds) {
                applyLaneVolume(laneId)
            }

            syncServiceAndWidgets()
            onComplete?.invoke()
        }
    }

    // Tracks lanes that were actively playing when audio focus was temporarily lost (e.g. incoming/active phone call)
    private val interruptedLanesOnTransientLoss = mutableSetOf<Long>()
    private var isDuckedDueTransientLoss = false

    /**
     * Pauses all playing lanes specifically due to transient audio focus loss (e.g., incoming/active phone call).
     * Saves the interrupted lane IDs and their exact positions so they can be seamlessly resumed once the call ends.
     */
    fun handleTransientFocusLoss(): List<Long> {
        val playingLaneIds = _laneStates.value.filter { it.value.isPlaying }.keys.toList()
        interruptedLanesOnTransientLoss.clear()
        if (playingLaneIds.isEmpty()) return emptyList()

        interruptedLanesOnTransientLoss.addAll(playingLaneIds)
        Log.d(TAG, "handleTransientFocusLoss: Pausing ${playingLaneIds.size} playing lanes for phone call/transient loss: $playingLaneIds")
        fadePauseAll(fadeDurationMs = 80L)
        return playingLaneIds
    }

    /**
     * Resumes playback for the lanes that were interrupted by a phone call or transient audio focus loss.
     * Starts them from their exact persisted millisecond position with a smooth fade-in.
     */
    fun handleFocusRegained(specificLaneIds: List<Long>? = null) {
        val lanesToResume = specificLaneIds?.filter { it > 0 }
            ?: interruptedLanesOnTransientLoss.toList()
        interruptedLanesOnTransientLoss.clear()

        val validLanes = if (lanesToResume.isNotEmpty()) {
            lanesToResume
        } else {
            val candidate = lastFocusedLaneId
                ?: _laneStates.value.keys.firstOrNull()
            if (candidate != null && (_laneStates.value[candidate]?.uriString != null || _laneStates.value[candidate]?.playlist?.isNotEmpty() == true)) {
                listOf(candidate)
            } else {
                emptyList()
            }
        }

        if (validLanes.isEmpty()) {
            Log.d(TAG, "handleFocusRegained: No valid lanes to resume after phone call.")
            return
        }

        Log.d(TAG, "handleFocusRegained: Resuming interrupted lanes after phone call: $validLanes")
        scope.launch(Dispatchers.Main) {
            // Short 200ms buffer to allow Android telecom framework to switch audio routing
            // from call mode (earpiece/SCO) back to media mode (speaker/headphones/Bluetooth A2DP)
            delay(200L)
            for (laneId in validLanes) {
                if (_laneStates.value.containsKey(laneId)) {
                    fadeResumeLane(laneId, fadeDurationMs = 150L)
                }
            }
        }
    }

    fun clearInterruptedFocusLanes() {
        interruptedLanesOnTransientLoss.clear()
    }

    fun duckVolume(duckFactor: Float = 0.2f) {
        isDuckedDueTransientLoss = true
        scope.launch(Dispatchers.Main) {
            for ((laneId, player) in mediaPlayers) {
                if (_laneStates.value[laneId]?.isPlaying == true) {
                    val base = calculateAudibleVolume(laneId)
                    val ducked = base * duckFactor
                    try {
                        player.setVolume(ducked, ducked)
                    } catch (e: Exception) {
                        Log.w(TAG, "Error ducking volume for lane $laneId: $e")
                    }
                }
            }
        }
    }

    fun restoreDuckedVolume() {
        if (!isDuckedDueTransientLoss) return
        isDuckedDueTransientLoss = false
        scope.launch(Dispatchers.Main) {
            for (laneId in _laneStates.value.keys) {
                applyLaneVolume(laneId)
            }
        }
    }

    fun togglePlayPauseLane(laneId: Long) {
        val state = _laneStates.value[laneId] ?: return
        if (state.isPlaying) {
            fadePauseLane(laneId)
        } else {
            fadeResumeLane(laneId)
        }
    }

    fun stopLane(laneId: Long) {
        interruptedLanesOnTransientLoss.remove(laneId)
        activePlayingLanes.remove(laneId)
        val player = mediaPlayers[laneId]
        val state = _laneStates.value[laneId] ?: return
        try {
            if (player != null && player.isPlaying) {
                player.pause()
            }
            player?.seekTo(0)
            val current = _laneStates.value.toMutableMap()
            current[laneId] = state.copy(isPlaying = false, currentPositionMs = 0L)
            _laneStates.value = current
            syncServiceAndWidgets(laneId)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping lane $laneId: $e")
        }
    }

    fun seekLaneTo(laneId: Long, positionMs: Long) {
        val player = mediaPlayers[laneId] ?: return
        val state = _laneStates.value[laneId] ?: return
        try {
            val clampedPos = max(0L, min(positionMs, state.durationMs))
            player.seekTo(clampedPos.toInt())
            val current = _laneStates.value.toMutableMap()
            current[laneId] = state.copy(currentPositionMs = clampedPos)
            _laneStates.value = current
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking lane $laneId: $e")
        }
    }

    fun skipLane(laneId: Long, deltaMs: Long) {
        val state = _laneStates.value[laneId] ?: return
        val newPos = state.currentPositionMs + deltaMs
        seekLaneTo(laneId, newPos)
    }

    fun playAllLanes() {
        _laneStates.value.keys.forEach { playLane(it) }
        syncServiceAndWidgets()
    }

    fun pauseAllLanes() {
        _laneStates.value.keys.forEach { pauseLane(it) }
        syncServiceAndWidgets()
    }

    fun stopAllLanes() {
        interruptedLanesOnTransientLoss.clear()
        _laneStates.value.keys.forEach { stopLane(it) }
        syncServiceAndWidgets()
    }

    fun setLaneVolume(laneId: Long, volume: Float) {
        val clamped = max(0f, min(1f, volume))
        val current = _laneStates.value.toMutableMap()
        current[laneId]?.let {
            current[laneId] = it.copy(volume = clamped)
            _laneStates.value = current
            applyLaneVolume(laneId)
        }
    }

    fun toggleLaneMute(laneId: Long) {
        val current = _laneStates.value.toMutableMap()
        current[laneId]?.let {
            val newMute = !it.isMuted
            current[laneId] = it.copy(isMuted = newMute)
            _laneStates.value = current
            applyAllLaneVolumes()
        }
    }

    fun toggleLaneSolo(laneId: Long) {
        val current = _laneStates.value.toMutableMap()
        current[laneId]?.let {
            val newSolo = !it.isSoloed
            current[laneId] = it.copy(isSoloed = newSolo)
            _laneStates.value = current
            applyAllLaneVolumes()
        }
    }

    fun clearAllSolos() {
        val current = _laneStates.value.toMutableMap()
        current.keys.forEach { id ->
            current[id]?.let { current[id] = it.copy(isSoloed = false) }
        }
        _laneStates.value = current
        applyAllLaneVolumes()
    }

    fun clearAllMutes() {
        val current = _laneStates.value.toMutableMap()
        current.keys.forEach { id ->
            current[id]?.let { current[id] = it.copy(isMuted = false) }
        }
        _laneStates.value = current
        applyAllLaneVolumes()
    }

    fun setMasterVolume(vol: Float) {
        _masterVolume.value = max(0f, min(1f, vol))
        applyAllLaneVolumes()
    }

    fun toggleMasterMute() {
        _isMasterMuted.value = !_isMasterMuted.value
        applyAllLaneVolumes()
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        for ((laneId, player) in mediaPlayers) {
            if (activePlayingLanes.contains(laneId) || player.isPlaying) {
                applyPlaybackSpeed(player, speed)
            }
        }
    }

    private fun applyPlaybackSpeed(player: MediaPlayer, speed: Float) {
        try {
            val params = player.playbackParams
            params.speed = speed
            params.pitch = 1.0f
            player.playbackParams = params
        } catch (e: Exception) {
            Log.w(TAG, "Error setting playback params: $e")
        }
    }

    private fun calculateAudibleVolume(laneId: Long): Float {
        val states = _laneStates.value
        val laneState = states[laneId] ?: return 0f

        if (_isMasterMuted.value || laneState.isMuted) return 0f

        val anySolo = states.values.any { it.isSoloed }
        if (anySolo && !laneState.isSoloed) return 0f

        // Crossfade multiplier
        var crossfadeFactor = 1.0f
        val leftId = _crossfadeLeftLaneId.value
        val rightId = _crossfadeRightLaneId.value
        val cfPos = _crossfadePosition.value // 0.0 = 100% Left, 1.0 = 100% Right

        if (leftId != null && rightId != null && leftId != rightId) {
            if (laneId == leftId) {
                crossfadeFactor = 1.0f - cfPos
            } else if (laneId == rightId) {
                crossfadeFactor = cfPos
            }
        }

        val baseVol = laneState.volume * _masterVolume.value * sleepTimerFadeMultiplier * crossfadeFactor
        return max(0f, min(1f, baseVol))
    }

    private fun applyLaneVolume(laneId: Long) {
        val player = mediaPlayers[laneId] ?: return
        val vol = calculateAudibleVolume(laneId)
        try {
            player.setVolume(vol, vol)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume for lane $laneId: $e")
        }
    }

    fun applyAllLaneVolumes() {
        _laneStates.value.keys.forEach { applyLaneVolume(it) }
    }

    // Crossfader methods
    fun setCrossfadeLeftLane(laneId: Long?) {
        _crossfadeLeftLaneId.value = laneId
        applyAllLaneVolumes()
    }

    fun setCrossfadeRightLane(laneId: Long?) {
        _crossfadeRightLaneId.value = laneId
        applyAllLaneVolumes()
    }

    fun setCrossfadePosition(position: Float) {
        _crossfadePosition.value = max(0f, min(1f, position))
        applyAllLaneVolumes()
    }

    fun setCrossfadeDurationSec(sec: Int) {
        _crossfadeDurationSec.value = max(2, min(8, sec))
    }

    fun triggerAutoCrossfade(targetRight: Boolean) {
        crossfadeJob?.cancel()
        val targetPos = if (targetRight) 1.0f else 0.0f
        val startPos = _crossfadePosition.value
        val durationMs = _crossfadeDurationSec.value * 1000L
        val steps = 40
        val stepDelay = durationMs / steps

        crossfadeJob = scope.launch {
            for (step in 1..steps) {
                val progress = step / steps.toFloat()
                val current = startPos + (targetPos - startPos) * progress
                setCrossfadePosition(current)
                delay(stepDelay)
            }
            setCrossfadePosition(targetPos)
        }
    }

    // Sleep Timer methods
    fun startSleepTimer(minutes: Int) {
        startSleepTimerSeconds(minutes * 60)
    }

    fun startSleepTimerSeconds(totalSec: Int) {
        cancelSleepTimer()
        if (totalSec <= 0) return

        val targetEndTime = System.currentTimeMillis() + totalSec * 1000L
        _sleepTimerTargetEndTimeMs.value = targetEndTime
        _sleepTimerInitialSec.value = totalSec
        _sleepTimerRemainingSec.value = totalSec
        sleepTimerFadeMultiplier = 1.0f

        sleepTimerJob = scope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val remainingMs = targetEndTime - now

                if (remainingMs <= 0L) {
                    _sleepTimerRemainingSec.value = 0
                    // Final fade to 0 before stopping playback
                    sleepTimerFadeMultiplier = 0.0f
                    applyAllLaneVolumes()
                    pauseAllLanes()
                    sleepTimerFadeMultiplier = 1.0f
                    applyAllLaneVolumes()
                    _sleepTimerRemainingSec.value = null
                    _sleepTimerInitialSec.value = null
                    _sleepTimerTargetEndTimeMs.value = null
                    break
                }

                val remainingSec = (remainingMs / 1000L).coerceAtLeast(0L).toInt()
                _sleepTimerRemainingSec.value = remainingSec

                // Smooth fade out during last 30 seconds based on remaining milliseconds
                val fadeWindowMs = (30 * 1000L).coerceAtMost(totalSec * 1000L)
                if (remainingMs <= fadeWindowMs && fadeWindowMs > 0L) {
                    sleepTimerFadeMultiplier = (remainingMs.toFloat() / fadeWindowMs.toFloat()).coerceIn(0f, 1f)
                    applyAllLaneVolumes()
                } else {
                    if (sleepTimerFadeMultiplier != 1.0f) {
                        sleepTimerFadeMultiplier = 1.0f
                        applyAllLaneVolumes()
                    }
                }

                delay(250L) // Frequent tick (250ms) for rock-solid precision against wall-clock time
            }
        }
    }

    fun addSleepTimerMinutes(minutes: Int) {
        val currentTarget = _sleepTimerTargetEndTimeMs.value
        val addSec = minutes * 60
        if (currentTarget != null && currentTarget > System.currentTimeMillis()) {
            val remainingSec = ((currentTarget - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L).toInt() + addSec
            startSleepTimerSeconds(remainingSec)
        } else {
            startSleepTimer(minutes)
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemainingSec.value = null
        _sleepTimerInitialSec.value = null
        _sleepTimerTargetEndTimeMs.value = null
        sleepTimerFadeMultiplier = 1.0f
        applyAllLaneVolumes()
    }

    // Equalizer methods
    private fun attachEqualizer(laneId: Long, audioSessionId: Int) {
        try {
            val eq = Equalizer(0, audioSessionId)
            eq.enabled = _eqEnabled.value
            equalizers[laneId] = eq
            applyEqToInstance(eq)
        } catch (e: Exception) {
            Log.w(TAG, "Equalizer hardware session not available on this device: $e")
        }
    }

    private fun applyEqToInstance(eq: Equalizer) {
        try {
            eq.enabled = _eqEnabled.value
            if (!eq.enabled) return
            val minLevel = eq.bandLevelRange[0] // e.g. -1500 (-15dB)
            val maxLevel = eq.bandLevelRange[1] // e.g. +1500 (+15dB)
            val numBands = eq.numberOfBands.toInt()
            val gains = _eqBands.value

            for (i in 0 until numBands) {
                val gainDb = gains.getOrElse(i) { 0f }
                val millibels = (gainDb * 100).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt())
                eq.setBandLevel(i.toShort(), millibels.toShort())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying EQ to instance: $e")
        }
    }

    fun setEqEnabled(enabled: Boolean) {
        _eqEnabled.value = enabled
        equalizers.values.forEach { eq ->
            try {
                eq.enabled = enabled
                if (enabled) applyEqToInstance(eq)
            } catch (e: Exception) {
                Log.w(TAG, "Error toggling EQ enabled: $e")
            }
        }
    }

    fun setEqBands(bands: List<Float>, presetName: String = "Custom") {
        _eqBands.value = bands
        _eqPresetName.value = presetName
        equalizers.values.forEach { eq ->
            applyEqToInstance(eq)
        }
    }

    fun applyEqPreset(preset: EqPreset) {
        setEqBands(preset.bands, preset.name)
    }

    fun release() {
        AudioServiceBridge.unregisterAudioEngine(this)
        tickerJob?.cancel()
        crossfadeJob?.cancel()
        sleepTimerJob?.cancel()
        mediaPlayers.values.forEach {
            try {
                it.stop()
                it.release()
            } catch (e: Exception) {}
        }
        mediaPlayers.clear()
        equalizers.values.forEach {
            try {
                it.release()
            } catch (e: Exception) {}
        }
        equalizers.clear()
        routingManager.release()
    }
}
