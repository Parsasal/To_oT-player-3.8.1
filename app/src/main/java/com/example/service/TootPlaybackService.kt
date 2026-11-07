package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import com.example.MainActivity
import com.example.R
import com.example.util.AlbumArtHelper

class TootPlaybackService : Service() {

    companion object {
        private const val TAG = "TootPlaybackService"
        const val CHANNEL_ID = "toot_playback_channel_v1"
        const val NOTIFICATION_ID = 9001

        const val ACTION_PLAY_PAUSE = "com.example.service.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.service.ACTION_NEXT"
        const val ACTION_PREV = "com.example.service.ACTION_PREV"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_UPDATE_STATE = "com.example.service.ACTION_UPDATE_STATE"

        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_TRACK_TITLE = "extra_track_title"
        const val EXTRA_SESSION_NAME = "extra_session_name"
        const val EXTRA_TRACK_URI = "extra_track_uri"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_POSITION = "extra_position"
        const val EXTRA_LANE_ID = "extra_lane_id"
    }

    private var mediaSession: MediaSessionCompat? = null
    private var isPlaying = false
    private var trackTitle = "No Track Playing"
    private var sessionName = "TO_OT Player"
    private var trackUriString: String? = null
    private var durationMs: Long = 0L
    private var positionMs: Long = 0L
    private var targetLaneId: Long = -1L
    private var isForegroundActive = false

    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var audioDeviceCallback: AudioDeviceCallback? = null
    private var isNoisyReceiverRegistered = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private var pausedByTransientFocusLoss = false
    private var isDuckedByTransientFocusLoss = false
    private val interruptedLaneIds = mutableListOf<Long>()

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        Log.d(TAG, "onAudioFocusChange: focusChange=$focusChange, isPlaying=$isPlaying, pausedByTransient=$pausedByTransientFocusLoss")
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d(TAG, "AUDIOFOCUS_LOSS: Permanent focus loss")
                pausedByTransientFocusLoss = false
                isDuckedByTransientFocusLoss = false
                interruptedLaneIds.clear()
                AudioServiceBridge.clearInterruptedFocusLanes()
                if (isPlaying) {
                    AudioServiceBridge.triggerAudioBecomingNoisy()
                }
                abandonAudioFocus()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT: Phone call or transient focus loss detected")
                if (isPlaying) {
                    pausedByTransientFocusLoss = true
                    val interrupted = AudioServiceBridge.onTransientFocusLoss()
                    interruptedLaneIds.clear()
                    interruptedLaneIds.addAll(interrupted)
                    // We deliberately do not abandon audio focus here.
                    // Keeping audioFocusRequest active ensures Android OS delivers AUDIOFOCUS_GAIN
                    // when the call finishes so music can seamlessly resume.
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
                if (isPlaying) {
                    isDuckedByTransientFocusLoss = true
                    AudioServiceBridge.onDuckVolume(0.2f)
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d(TAG, "AUDIOFOCUS_GAIN: Focus regained! pausedByTransient=$pausedByTransientFocusLoss, ducked=$isDuckedByTransientFocusLoss")
                if (pausedByTransientFocusLoss) {
                    pausedByTransientFocusLoss = false
                    val lanesToResume = interruptedLaneIds.toList()
                    interruptedLaneIds.clear()
                    AudioServiceBridge.onFocusRegained(if (lanesToResume.isNotEmpty()) lanesToResume else null)
                } else if (isDuckedByTransientFocusLoss) {
                    isDuckedByTransientFocusLoss = false
                    AudioServiceBridge.onRestoreDuckedVolume()
                }
            }
        }
    }

    /**
     * BroadcastReceiver for detecting earbud removal or Bluetooth disconnection.
     * Invokes micro fade-out and persists exact playback position.
     */
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                Log.d(TAG, "ACTION_AUDIO_BECOMING_NOISY received! Earbuds removed or disconnected.")
                AudioServiceBridge.triggerAudioBecomingNoisy()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            createNotificationChannel()
            setupMediaSession()
            setupAudioRoutingDetection()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: $e")
        }
    }

    private fun setupAudioRoutingDetection() {
        // Register BroadcastReceiver for ACTION_AUDIO_BECOMING_NOISY
        try {
            if (!isNoisyReceiverRegistered) {
                val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(becomingNoisyReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    registerReceiver(becomingNoisyReceiver, filter)
                }
                isNoisyReceiverRegistered = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error registering becomingNoisyReceiver: $e")
        }

        // Register AudioDeviceCallback for proactive earbud removal detection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                audioDeviceCallback = object : AudioDeviceCallback() {
                    override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                        super.onAudioDevicesAdded(addedDevices)
                        Log.d(TAG, "Audio device connected (${addedDevices?.size} devices). Re-evaluating active audio routing.")
                        try {
                            AudioServiceBridge.getAudioEngine()?.applyAudioRouting()
                        } catch (e: Exception) {
                            Log.w(TAG, "Error applying audio routing on device added: $e")
                        }
                    }

                    override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                        super.onAudioDevicesRemoved(removedDevices)
                        if (removedDevices == null) return
                        val isHeadsetRemoved = removedDevices.any { device ->
                            when (device.type) {
                                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                                AudioDeviceInfo.TYPE_WIRED_HEADSET,
                                AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                                AudioDeviceInfo.TYPE_HEARING_AID,
                                AudioDeviceInfo.TYPE_USB_HEADSET -> true
                                else -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        device.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                                                device.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                                                device.type == AudioDeviceInfo.TYPE_BLE_BROADCAST
                                    } else false
                                }
                            }
                        }
                        if (isHeadsetRemoved && isPlaying) {
                            Log.d(TAG, "Audio device disconnected (Bluetooth / Headset). Triggering smooth fade-out auto-pause.")
                            AudioServiceBridge.triggerAudioBecomingNoisy()
                        }
                    }
                }
                audioManager?.registerAudioDeviceCallback(audioDeviceCallback, mainHandler)
            } catch (e: Exception) {
                Log.w(TAG, "Error registering AudioDeviceCallback: $e")
            }
        }
    }

    private fun safelyStartForeground(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            isForegroundActive = true
        } catch (e: Exception) {
            Log.w(TAG, "Could not start foreground: $e")
            try {
                // Fallback startForeground without explicit type
                startForeground(NOTIFICATION_ID, notification)
                isForegroundActive = true
            } catch (inner: Exception) {
                Log.e(TAG, "Fatal fallback startForeground failed: $inner")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.playback_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = getString(R.string.playback_channel_desc)
                    setShowBadge(false)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification channel: $e")
            }
        }
    }

    private fun setupMediaSession() {
        try {
            val mediaButtonReceiverComponentName = ComponentName(applicationContext, MediaButtonReceiver::class.java)
            val mediaButtonPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                0,
                Intent(Intent.ACTION_MEDIA_BUTTON).setComponent(mediaButtonReceiverComponentName),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )

            mediaSession = MediaSessionCompat(this, "TootMediaSession", mediaButtonReceiverComponentName, mediaButtonPendingIntent).apply {
                setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                )
                setMediaButtonReceiver(mediaButtonPendingIntent)
                setCallback(object : MediaSessionCompat.Callback() {
                    override fun onPlay() {
                        pausedByTransientFocusLoss = false
                        interruptedLaneIds.clear()
                        AudioServiceBridge.triggerResumeWithFadeIn(if (targetLaneId > 0) targetLaneId else null)
                    }

                    override fun onPause() {
                        pausedByTransientFocusLoss = false
                        interruptedLaneIds.clear()
                        AudioServiceBridge.clearInterruptedFocusLanes()
                        AudioServiceBridge.triggerPauseWithFadeOut(if (targetLaneId > 0) targetLaneId else null)
                        abandonAudioFocus()
                    }

                    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                        val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mediaButtonEvent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            mediaButtonEvent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT) as? KeyEvent
                        }
                        if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                            when (keyEvent.keyCode) {
                                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                    pausedByTransientFocusLoss = false
                                    interruptedLaneIds.clear()
                                    AudioServiceBridge.triggerResumeWithFadeIn(if (targetLaneId > 0) targetLaneId else null)
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                    pausedByTransientFocusLoss = false
                                    interruptedLaneIds.clear()
                                    AudioServiceBridge.clearInterruptedFocusLanes()
                                    AudioServiceBridge.triggerPauseWithFadeOut(if (targetLaneId > 0) targetLaneId else null)
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                                KeyEvent.KEYCODE_HEADSETHOOK -> {
                                    AudioServiceBridge.triggerPlayPauseWithFade(if (targetLaneId > 0) targetLaneId else null)
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_NEXT,
                                KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> {
                                    AudioServiceBridge.triggerNext(if (targetLaneId > 0) targetLaneId else null)
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                                KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD -> {
                                    AudioServiceBridge.triggerPrevious(if (targetLaneId > 0) targetLaneId else null)
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                                    AudioServiceBridge.triggerFastForward(if (targetLaneId > 0) targetLaneId else null)
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_REWIND -> {
                                    AudioServiceBridge.triggerRewind(if (targetLaneId > 0) targetLaneId else null)
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_STOP -> {
                                    AudioServiceBridge.triggerStop()
                                    stopForegroundService()
                                    return true
                                }
                            }
                        }
                        return super.onMediaButtonEvent(mediaButtonEvent)
                    }

                    override fun onSkipToNext() {
                        AudioServiceBridge.triggerNext(if (targetLaneId > 0) targetLaneId else null)
                    }

                    override fun onSkipToPrevious() {
                        AudioServiceBridge.triggerPrevious(if (targetLaneId > 0) targetLaneId else null)
                    }

                    override fun onSeekTo(pos: Long) {
                        positionMs = pos
                        AudioServiceBridge.triggerSeekTo(pos, if (targetLaneId > 0) targetLaneId else null)
                        updateMediaSessionState()
                    }

                    override fun onFastForward() {
                        AudioServiceBridge.triggerFastForward(if (targetLaneId > 0) targetLaneId else null)
                    }

                    override fun onRewind() {
                        AudioServiceBridge.triggerRewind(if (targetLaneId > 0) targetLaneId else null)
                    }

                    override fun onStop() {
                        AudioServiceBridge.triggerStop()
                        stopForegroundService()
                    }
                })
                isActive = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up media session: $e")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_STICKY

        try {
            // Handle hardware media buttons and lockscreen key events
            MediaButtonReceiver.handleIntent(mediaSession, intent)

            when (intent.action) {
                ACTION_PLAY_PAUSE -> {
                    if (isPlaying) {
                        pausedByTransientFocusLoss = false
                        interruptedLaneIds.clear()
                        AudioServiceBridge.clearInterruptedFocusLanes()
                    }
                    AudioServiceBridge.triggerPlayPause(if (targetLaneId > 0) targetLaneId else null)
                }
                ACTION_NEXT -> {
                    AudioServiceBridge.triggerNext(if (targetLaneId > 0) targetLaneId else null)
                }
                ACTION_PREV -> {
                    AudioServiceBridge.triggerPrevious(if (targetLaneId > 0) targetLaneId else null)
                }
                ACTION_STOP -> {
                    pausedByTransientFocusLoss = false
                    interruptedLaneIds.clear()
                    AudioServiceBridge.clearInterruptedFocusLanes()
                    AudioServiceBridge.triggerStop()
                    stopForegroundService()
                    return START_NOT_STICKY
                }
                ACTION_UPDATE_STATE -> {
                    val wasPlaying = isPlaying
                    isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)
                    trackTitle = intent.getStringExtra(EXTRA_TRACK_TITLE) ?: trackTitle
                    sessionName = intent.getStringExtra(EXTRA_SESSION_NAME) ?: sessionName
                    trackUriString = intent.getStringExtra(EXTRA_TRACK_URI)
                    durationMs = intent.getLongExtra(EXTRA_DURATION, 0L)
                    positionMs = intent.getLongExtra(EXTRA_POSITION, 0L)
                    targetLaneId = intent.getLongExtra(EXTRA_LANE_ID, -1L)

                    if (!wasPlaying && isPlaying) {
                        pausedByTransientFocusLoss = false
                        interruptedLaneIds.clear()
                    }

                    updateMediaSessionState()
                    updateNotification()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing onStartCommand: $e")
        }

        return START_STICKY
    }

    private fun updateMediaSessionState() {
        try {
            val state = if (isPlaying) {
                PlaybackStateCompat.STATE_PLAYING
            } else {
                PlaybackStateCompat.STATE_PAUSED
            }

            val playbackSpeed = if (isPlaying) 1.0f else 0.0f
            val playbackState = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_STOP or
                            PlaybackStateCompat.ACTION_SEEK_TO or
                            PlaybackStateCompat.ACTION_FAST_FORWARD or
                            PlaybackStateCompat.ACTION_REWIND
                )
                .setState(
                    state,
                    if (positionMs > 0) positionMs else PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    playbackSpeed,
                    SystemClock.elapsedRealtime()
                )
                .build()

            mediaSession?.setPlaybackState(playbackState)

            // Update lockscreen metadata with real album art or fallback
            val albumArtBitmap = trackUriString?.let { AlbumArtHelper.getAlbumArtBitmapSync(this, it) }
            val metadataBuilder = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, trackTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, sessionName)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, sessionName)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, trackTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, sessionName)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, if (durationMs > 0) durationMs else -1L)

            if (albumArtBitmap != null) {
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArtBitmap)
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, albumArtBitmap)
                metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, albumArtBitmap)
            }

            mediaSession?.setMetadata(metadataBuilder.build())
        } catch (e: Exception) {
            Log.w(TAG, "Error updating MediaSession state: $e")
        }
    }

    private fun updateNotification() {
        try {
            val notification = buildNotification()
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            if (isPlaying) {
                pausedByTransientFocusLoss = false
                isDuckedByTransientFocusLoss = false
                requestAudioFocus()
                safelyStartForeground(notification)
                manager?.notify(NOTIFICATION_ID, notification)
            } else {
                // If paused due to transient focus loss (phone call), KEEP audio focus registered!
                // This allows the system to deliver AUDIOFOCUS_GAIN once the call concludes.
                if (!pausedByTransientFocusLoss) {
                    abandonAudioFocus()
                }
                if (isForegroundActive) {
                    try {
                        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_DETACH)
                        isForegroundActive = false
                    } catch (e: Exception) {
                        Log.w(TAG, "stopForeground error: $e")
                    }
                }
                manager?.notify(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification: $e")
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // Tap notification to open MainActivity
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            pendingIntentFlags
        )

        // Actions with MediaButtonReceiver for universal lockscreen compatibility
        val prevPendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            this,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )

        val playPausePendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            this,
            PlaybackStateCompat.ACTION_PLAY_PAUSE
        )

        val nextPendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            this,
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )

        val stopPendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            this,
            PlaybackStateCompat.ACTION_STOP
        )

        val playPauseActionIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
        val playPauseActionTitle = if (isPlaying) getString(R.string.action_pause) else getString(R.string.action_play)

        val albumArtBitmap = trackUriString?.let { AlbumArtHelper.getAlbumArtBitmapSync(this, it) }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(trackTitle)
            .setContentText("TO_OT Session • $sessionName")
            .setSubText(sessionName)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .addAction(R.drawable.ic_skip_previous, getString(R.string.action_previous), prevPendingIntent)
            .addAction(playPauseActionIcon, playPauseActionTitle, playPausePendingIntent)
            .addAction(R.drawable.ic_skip_next, getString(R.string.action_next), nextPendingIntent)
            .addAction(R.drawable.ic_stop, getString(R.string.action_stop), stopPendingIntent)

        if (albumArtBitmap != null) {
            builder.setLargeIcon(albumArtBitmap)
        }

        val session = mediaSession
        if (session != null) {
            builder.setStyle(
                MediaStyle()
                    .setMediaSession(session.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(stopPendingIntent)
            )
        }

        return builder.build()
    }

    private fun requestAudioFocus() {
        try {
            val isVoiceComm = audioManager?.mode == AudioManager.MODE_IN_COMMUNICATION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (audioFocusRequest == null) {
                    val playbackAttributes = if (isVoiceComm) {
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    } else {
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    }

                    audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(playbackAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(audioFocusChangeListener, mainHandler)
                        .build()
                }
                audioFocusRequest?.let { audioManager?.requestAudioFocus(it) }
            } else {
                @Suppress("DEPRECATION")
                val streamType = if (isVoiceComm) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC
                @Suppress("DEPRECATION")
                audioManager?.requestAudioFocus(
                    audioFocusChangeListener,
                    streamType,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "requestAudioFocus error: $e")
        }
    }

    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(audioFocusChangeListener)
            }
        } catch (e: Exception) {
            Log.w(TAG, "abandonAudioFocus error: $e")
        }
    }

    private fun stopForegroundService() {
        pausedByTransientFocusLoss = false
        interruptedLaneIds.clear()
        abandonAudioFocus()
        try {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            isForegroundActive = false
        } catch (e: Exception) {
            Log.w(TAG, "stopForeground error: $e")
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(NOTIFICATION_ID)
        stopSelf()
    }

    override fun onDestroy() {
        pausedByTransientFocusLoss = false
        interruptedLaneIds.clear()
        abandonAudioFocus()
        if (isNoisyReceiverRegistered) {
            try {
                unregisterReceiver(becomingNoisyReceiver)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering becomingNoisyReceiver: $e")
            }
            isNoisyReceiverRegistered = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioDeviceCallback != null) {
            try {
                audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering audioDeviceCallback: $e")
            }
            audioDeviceCallback = null
        }
        try {
            mediaSession?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing mediaSession: $e")
        }
        mediaSession = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
