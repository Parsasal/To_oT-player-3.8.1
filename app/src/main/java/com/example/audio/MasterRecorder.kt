package com.example.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

class MasterRecorder(private val context: Context) {
    private val TAG = "MasterRecorder"
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var recordingJob: Job? = null
    private var previewPlayer: MediaPlayer? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0)
    val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _recordingsList = MutableStateFlow<List<RecordedTrack>>(emptyList())
    val recordingsList: StateFlow<List<RecordedTrack>> = _recordingsList.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _playingTrackId = MutableStateFlow<String?>(null)
    val playingTrackId: StateFlow<String?> = _playingTrackId.asStateFlow()

    init {
        loadExistingRecordings()
    }

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun loadExistingRecordings() {
        try {
            val dir = File(context.filesDir, "recordings")
            if (dir.exists()) {
                val files = dir.listFiles()?.filter { it.extension in listOf("m4a", "aac", "wav", "mp4", "3gp") } ?: emptyList()
                val list = files.sortedByDescending { it.lastModified() }.map { file ->
                    var durationMs = 0L
                    try {
                        val mmr = android.media.MediaMetadataRetriever()
                        mmr.setDataSource(file.absolutePath)
                        val durStr = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                        durationMs = durStr?.toLongOrNull() ?: 0L
                        mmr.release()
                    } catch (e: Exception) {
                        // ignore duration extraction errors
                    }
                    RecordedTrack(
                        id = file.name,
                        title = file.nameWithoutExtension,
                        filePath = file.absolutePath,
                        durationMs = durationMs,
                        timestamp = file.lastModified()
                    )
                }
                _recordingsList.value = list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading recordings: $e")
        }
    }

    fun startRecording(): Boolean {
        if (_isRecording.value) return false
        stopPreviewPlayback()
        _errorMessage.value = null

        if (!hasRecordPermission()) {
            _errorMessage.value = "RECORD_AUDIO permission required"
            return false
        }

        val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(recordingsDir, "TO_OT_Master_$timeStamp.m4a")
        currentOutputFile = file

        val sourcesToTry = listOf(
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            MediaRecorder.AudioSource.CAMCORDER
        )

        var lastException: Exception? = null
        for (source in sourcesToTry) {
            try {
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }

                recorder.apply {
                    setAudioSource(source)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(192000)
                    setAudioSamplingRate(44100)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }

                mediaRecorder = recorder
                _isRecording.value = true
                _isPaused.value = false
                _recordingDurationSec.value = 0
                _amplitude.value = 0f

                startDurationTimer()
                Log.d(TAG, "Recording started successfully with audio source $source")
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Failed starting recorder with source $source: ${e.message}")
                lastException = e
                try {
                    mediaRecorder?.reset()
                    mediaRecorder?.release()
                } catch (_: Exception) {}
                mediaRecorder = null
            }
        }

        // Fallback to 3GP container if MPEG-4 failed
        try {
            val fallbackFile = File(recordingsDir, "TO_OT_Master_$timeStamp.3gp")
            currentOutputFile = fallbackFile
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
                setOutputFile(fallbackFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            _isRecording.value = true
            _isPaused.value = false
            _recordingDurationSec.value = 0
            _amplitude.value = 0f
            startDurationTimer()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "All recording configurations failed: $e")
            _errorMessage.value = "Recording error: ${e.localizedMessage ?: lastException?.localizedMessage ?: "Audio device unavailable"}"
            _isRecording.value = false
            return false
        }
    }

    private fun startDurationTimer() {
        recordingJob?.cancel()
        recordingJob = scope.launch {
            var tickCount = 0
            while (isActive && _isRecording.value) {
                delay(100L)
                tickCount++
                if (tickCount % 10 == 0 && !_isPaused.value) {
                    _recordingDurationSec.value = _recordingDurationSec.value + 1
                }

                // Sample amplitude
                if (!_isPaused.value) {
                    try {
                        val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                        val normalized = (maxAmp / 32767f).coerceIn(0f, 1f)
                        _amplitude.value = normalized
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun pauseRecording() {
        if (!_isRecording.value || _isPaused.value) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                _isPaused.value = true
                _amplitude.value = 0f
            }
        } catch (e: Exception) {
            Log.e(TAG, "Pause recording error: $e")
        }
    }

    fun resumeRecording() {
        if (!_isRecording.value || !_isPaused.value) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                _isPaused.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Resume recording error: $e")
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        recordingJob?.cancel()
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recorder: $e")
        }
        mediaRecorder = null
        _isRecording.value = false
        _isPaused.value = false
        _amplitude.value = 0f

        loadExistingRecordings()
    }

    fun togglePlayPreview(track: RecordedTrack) {
        if (_playingTrackId.value == track.id) {
            stopPreviewPlayback()
        } else {
            playPreview(track)
        }
    }

    private fun playPreview(track: RecordedTrack) {
        stopPreviewPlayback()
        try {
            val player = MediaPlayer().apply {
                setDataSource(track.filePath)
                prepare()
                setOnCompletionListener {
                    stopPreviewPlayback()
                }
                start()
            }
            previewPlayer = player
            _playingTrackId.value = track.id
        } catch (e: Exception) {
            Log.e(TAG, "Error playing recorded preview: $e")
            stopPreviewPlayback()
        }
    }

    fun stopPreviewPlayback() {
        try {
            previewPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing preview player: $e")
        }
        previewPlayer = null
        _playingTrackId.value = null
    }

    fun deleteRecording(track: RecordedTrack) {
        if (_playingTrackId.value == track.id) {
            stopPreviewPlayback()
        }
        try {
            val file = File(track.filePath)
            if (file.exists()) {
                file.delete()
            }
            loadExistingRecordings()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting recording: $e")
        }
    }

    fun release() {
        stopRecording()
        stopPreviewPlayback()
    }
}
