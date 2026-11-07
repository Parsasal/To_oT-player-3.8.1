package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.audio.AudioEngine
import com.example.audio.AudioLaneState
import com.example.widget.TootAppWidgetProvider

/**
 * Bridge between UI/AudioEngine, TootPlaybackService, and TootAppWidgetProvider.
 */
object AudioServiceBridge {
    private const val TAG = "AudioServiceBridge"

    var currentAudioEngine: AudioEngine? = null
        private set

    fun getAudioEngine(): AudioEngine? = currentAudioEngine

    var activeSessionLaneId: Long? = null

    fun registerAudioEngine(engine: AudioEngine) {
        currentAudioEngine = engine
    }

    fun unregisterAudioEngine(engine: AudioEngine) {
        if (currentAudioEngine == engine) {
            currentAudioEngine = null
        }
    }

    private var hasStartedService = false

    fun onPlaybackStateChanged(
        context: Context,
        lanes: List<AudioLaneState>,
        activeLaneId: Long?
    ) {
        val anyPlaying = lanes.any { it.isPlaying }
        // CRITICAL: Always prioritize the line that is ACTUALLY PLAYING right now!
        val activeLane = lanes.find { it.isPlaying }
            ?: lanes.find { it.laneId == activeLaneId }
            ?: lanes.find { it.laneId == activeSessionLaneId }
            ?: lanes.firstOrNull()

        val trackTitle = activeLane?.let {
            if (!it.fileName.isNullOrBlank()) it.fileName else it.name
        } ?: "No Track Loaded"

        val trackUri = activeLane?.uriString
            ?: activeLane?.playlist?.getOrNull(activeLane.currentTrackIndex)?.uriString

        val sessionName = activeLane?.name ?: "TO_OT Player"

        val durationMs = activeLane?.durationMs ?: 0L
        val positionMs = activeLane?.currentPositionMs ?: 0L

        // Update home screen widget safely
        try {
            TootAppWidgetProvider.updateAllWidgets(
                context = context,
                isPlaying = anyPlaying,
                trackTitle = trackTitle,
                sessionName = sessionName
            )
        } catch (e: Exception) {
            Log.w(TAG, "Widget update non-fatal error: $e")
        }

        // Notify or start/update Foreground Service safely
        try {
            val intent = Intent(context, TootPlaybackService::class.java).apply {
                action = TootPlaybackService.ACTION_UPDATE_STATE
                putExtra(TootPlaybackService.EXTRA_IS_PLAYING, anyPlaying)
                putExtra(TootPlaybackService.EXTRA_TRACK_TITLE, trackTitle)
                putExtra(TootPlaybackService.EXTRA_SESSION_NAME, sessionName)
                putExtra(TootPlaybackService.EXTRA_TRACK_URI, trackUri)
                putExtra(TootPlaybackService.EXTRA_DURATION, durationMs)
                putExtra(TootPlaybackService.EXTRA_POSITION, positionMs)
                putExtra(TootPlaybackService.EXTRA_LANE_ID, activeLane?.laneId ?: -1L)
            }

            if (anyPlaying) {
                hasStartedService = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        context.startForegroundService(intent)
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not startForegroundService, trying startService: $e")
                        context.startService(intent)
                    }
                } else {
                    context.startService(intent)
                }
            } else if (hasStartedService) {
                try {
                    context.startService(intent)
                } catch (e: Exception) {
                    Log.w(TAG, "startService non-fatal when idle: $e")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to send update to TootPlaybackService: $e")
        }
    }

    /**
     * Resolves the target lane for media buttons / Bluetooth commands.
     * Absolute Priority 1: Any lane that is CURRENTLY PLAYING audio (e.g. Lane 3 or whichever is playing).
     * Priority 2: The last focused/playing lane (for resuming after pause).
     * Priority 3: Active session lane.
     * Priority 4: First available lane.
     */
    fun resolveTargetLaneId(engine: AudioEngine, requestedLaneId: Long?): Long? {
        val states = engine.laneStates.value
        // If an explicit lane is playing, use it
        if (requestedLaneId != null && requestedLaneId > 0 && states[requestedLaneId]?.isPlaying == true) {
            return requestedLaneId
        }
        // Top Priority: Whichever lane is actively playing right now
        val currentlyPlaying = states.values.find { it.isPlaying }
        if (currentlyPlaying != null) {
            return currentlyPlaying.laneId
        }
        // If not playing, use requested lane if valid
        if (requestedLaneId != null && requestedLaneId > 0 && states.containsKey(requestedLaneId)) {
            return requestedLaneId
        }
        // Use last focused / last playing lane (perfect for headset resume)
        if (engine.lastFocusedLaneId != null && states.containsKey(engine.lastFocusedLaneId)) {
            return engine.lastFocusedLaneId
        }
        // Active UI lane
        if (activeSessionLaneId != null && states.containsKey(activeSessionLaneId)) {
            return activeSessionLaneId
        }
        // First lane with loaded track or first lane
        return states.values.find { it.uriString != null || it.playlist.isNotEmpty() }?.laneId
            ?: states.keys.firstOrNull()
    }

    fun triggerPlayPause(laneId: Long? = null) {
        val engine = currentAudioEngine ?: return
        val targetLaneId = resolveTargetLaneId(engine, laneId)
        if (targetLaneId != null) {
            engine.togglePlayPauseLane(targetLaneId)
        } else {
            engine.playAllLanes()
        }
    }

    /**
     * Earbud In-Ear Removal or Disconnection event:
     * Immediately triggers smooth 100ms micro fade-out and persists exact playback timestamps.
     */
    fun triggerAudioBecomingNoisy() {
        val engine = currentAudioEngine ?: return
        engine.fadePauseAll(fadeDurationMs = 100L)
    }

    /**
     * Resumes playback on the targeted or last focused active lane from exact stored millisecond timestamp
     * with a gentle fade-in ramp.
     */
    fun triggerResumeWithFadeIn(laneId: Long? = null) {
        val engine = currentAudioEngine ?: return
        val targetLaneId = resolveTargetLaneId(engine, laneId)
        if (targetLaneId != null) {
            engine.fadeResumeLane(targetLaneId, fadeDurationMs = 150L)
        } else {
            engine.resumeLastFocusedLaneOrAll(fadeDurationMs = 150L)
        }
    }

    /**
     * Pauses playback on the targeted lane with a smooth 100ms micro fade-out.
     */
    fun triggerPauseWithFadeOut(laneId: Long? = null) {
        val engine = currentAudioEngine ?: return
        val targetLaneId = resolveTargetLaneId(engine, laneId)
        if (targetLaneId != null) {
            engine.fadePauseLane(targetLaneId, fadeDurationMs = 100L)
        } else {
            engine.fadePauseAll(fadeDurationMs = 100L)
        }
    }

    /**
     * Single-tap hardware gesture handler (play/pause toggle with gentle fade ramp).
     */
    fun triggerPlayPauseWithFade(laneId: Long? = null) {
        val engine = currentAudioEngine ?: return
        val targetLaneId = resolveTargetLaneId(engine, laneId)
        if (targetLaneId != null) {
            val state = engine.laneStates.value[targetLaneId]
            if (state?.isPlaying == true) {
                engine.fadePauseLane(targetLaneId, fadeDurationMs = 100L)
            } else {
                engine.fadeResumeLane(targetLaneId, fadeDurationMs = 150L)
            }
        } else {
            engine.resumeLastFocusedLaneOrAll(fadeDurationMs = 150L)
        }
    }

    fun triggerNext(laneId: Long? = null) {
        val engine = currentAudioEngine ?: return
        val targetLaneId = resolveTargetLaneId(engine, laneId)
        if (targetLaneId != null) {
            engine.playNextTrack(targetLaneId)
        }
    }

    fun triggerPrevious(laneId: Long? = null) {
        val engine = currentAudioEngine ?: return
        val targetLaneId = resolveTargetLaneId(engine, laneId)
        if (targetLaneId != null) {
            engine.playPreviousTrack(targetLaneId)
        }
    }

    fun triggerFastForward(laneId: Long? = null, deltaMs: Long = 10000L) {
        val engine = currentAudioEngine ?: return
        val targetLaneId = resolveTargetLaneId(engine, laneId)
        if (targetLaneId != null) {
            engine.skipLane(targetLaneId, deltaMs)
        }
    }

    fun triggerRewind(laneId: Long? = null, deltaMs: Long = 10000L) {
        val engine = currentAudioEngine ?: return
        val targetLaneId = resolveTargetLaneId(engine, laneId)
        if (targetLaneId != null) {
            engine.skipLane(targetLaneId, -deltaMs)
        }
    }

    fun triggerSeekTo(positionMs: Long, laneId: Long? = null) {
        val engine = currentAudioEngine ?: return
        val targetLaneId = resolveTargetLaneId(engine, laneId)
        if (targetLaneId != null) {
            engine.seekLaneTo(targetLaneId, positionMs)
        }
    }

    fun triggerStop() {
        val engine = currentAudioEngine ?: return
        engine.stopAllLanes()
    }

    var isAutoResumeAfterCallEnabled: Boolean = true

    fun onTransientFocusLoss(): List<Long> {
        val engine = currentAudioEngine ?: return emptyList()
        return engine.handleTransientFocusLoss()
    }

    fun onFocusRegained(laneIdsToResume: List<Long>? = null) {
        if (!isAutoResumeAfterCallEnabled) {
            Log.d(TAG, "onFocusRegained: auto-resume is disabled by user setting.")
            return
        }
        val engine = currentAudioEngine ?: return
        engine.handleFocusRegained(laneIdsToResume)
    }

    fun clearInterruptedFocusLanes() {
        currentAudioEngine?.clearInterruptedFocusLanes()
    }

    fun onDuckVolume(duckFactor: Float = 0.2f) {
        currentAudioEngine?.duckVolume(duckFactor)
    }

    fun onRestoreDuckedVolume() {
        currentAudioEngine?.restoreDuckedVolume()
    }
}
