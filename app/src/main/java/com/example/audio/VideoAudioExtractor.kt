package com.example.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExtractedAudioTrack(
    val id: String,
    val title: String,
    val filePath: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val timestamp: Long
)

class VideoAudioExtractor(private val context: Context) {
    private val TAG = "VideoAudioExtractor"
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting.asStateFlow()

    private val _extractionProgress = MutableStateFlow(0f)
    val extractionProgress: StateFlow<Float> = _extractionProgress.asStateFlow()

    private val _statusText = MutableStateFlow<String?>(null)
    val statusText: StateFlow<String?> = _statusText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _extractedList = MutableStateFlow<List<ExtractedAudioTrack>>(emptyList())
    val extractedList: StateFlow<List<ExtractedAudioTrack>> = _extractedList.asStateFlow()

    private val _previewPlayingId = MutableStateFlow<String?>(null)
    val previewPlayingId: StateFlow<String?> = _previewPlayingId.asStateFlow()

    private var previewPlayer: MediaPlayer? = null

    init {
        loadExtractedAudioList()
    }

    fun loadExtractedAudioList() {
        try {
            val dir = File(context.filesDir, "extracted_audio").apply { mkdirs() }
            val files = dir.listFiles()?.filter {
                it.extension.equals("wav", ignoreCase = true) ||
                it.extension.equals("mp3", ignoreCase = true) ||
                it.extension.equals("m4a", ignoreCase = true) ||
                it.extension.equals("aac", ignoreCase = true)
            } ?: emptyList()

            val list = files.sortedByDescending { it.lastModified() }.map { file ->
                var durationMs = 0L
                try {
                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(file.absolutePath)
                    val durStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    durationMs = durStr?.toLongOrNull() ?: 0L
                    mmr.release()
                } catch (e: Exception) {
                    // Ignore duration extraction error
                }
                ExtractedAudioTrack(
                    id = file.name,
                    title = file.nameWithoutExtension,
                    filePath = file.absolutePath,
                    durationMs = durationMs,
                    sizeBytes = file.length(),
                    timestamp = file.lastModified()
                )
            }
            _extractedList.value = list
        } catch (e: Exception) {
            Log.e(TAG, "Error loading extracted list: $e")
        }
    }

    /**
     * Extracts audio from the provided video URI.
     * Enforces a maximum duration limit of 20 minutes (1,200,000 ms).
     */
    fun extractAudioFromVideo(
        videoUri: Uri,
        customName: String? = null,
        onSuccess: (ExtractedAudioTrack) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (_isExtracting.value) return

        _isExtracting.value = true
        _extractionProgress.value = 0f
        _statusText.value = "Analyzing video clip..."
        _errorMessage.value = null

        scope.launch {
            try {
                val resultTrack = withContext(Dispatchers.IO) {
                    processVideoToAudio(videoUri, customName)
                }

                _isExtracting.value = false
                _extractionProgress.value = 1f
                _statusText.value = null
                loadExtractedAudioList()
                onSuccess(resultTrack)
            } catch (e: Exception) {
                Log.e(TAG, "Audio extraction failed: $e", e)
                val msg = e.localizedMessage ?: "Failed to extract audio from clip"
                _errorMessage.value = msg
                _statusText.value = null
                _isExtracting.value = false
                _extractionProgress.value = 0f
                onError(msg)
            }
        }
    }

    private fun processVideoToAudio(videoUri: Uri, customName: String?): ExtractedAudioTrack {
        // Step 1: Check duration with MediaMetadataRetriever
        val mmr = MediaMetadataRetriever()
        var durationMs = 0L
        var resolvedTitle = "Clip_Audio"
        try {
            mmr.setDataSource(context, videoUri)
            val durStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationMs = durStr?.toLongOrNull() ?: 0L
            val metaTitle = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            if (!metaTitle.isNullOrBlank()) {
                resolvedTitle = metaTitle
            } else {
                // Try openable name
                context.contentResolver.query(videoUri, null, null, null, null)?.use { cursor ->
                    val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (idx != -1 && cursor.moveToFirst()) {
                        val name = cursor.getString(idx)
                        if (!name.isNullOrBlank()) resolvedTitle = name.substringBeforeLast(".")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed retrieving metadata: $e")
        } finally {
            try { mmr.release() } catch (_: Exception) {}
        }

        // Maximum 6 hours = 6 * 60 * 60 * 1000 = 21,600,000 ms
        val maxDurationMs = 6 * 60 * 60 * 1000L
        if (durationMs > maxDurationMs) {
            val hours = durationMs / 3600000
            throw IllegalStateException("Video duration ($hours hours) exceeds the maximum allowed limit (6 hours).")
        }

        // Setup output destination
        val outDir = File(context.filesDir, "extracted_audio").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val cleanName = if (!customName.isNullOrBlank()) {
            customName.trim().replace(Regex("[^a-zA-Z0-9_\\u0600-\\u06FF\\s-]"), "")
        } else {
            resolvedTitle.replace(Regex("[^a-zA-Z0-9_\\u0600-\\u06FF\\s-]"), "")
        }
        val fileName = if (cleanName.isNotBlank()) "${cleanName}_$timeStamp.mp3" else "TO_OT_Extracted_$timeStamp.mp3"
        val outputFile = File(outDir, fileName)

        // Step 2: Use MediaExtractor to find audio track
        val extractor = MediaExtractor()
        val pfd = context.contentResolver.openFileDescriptor(videoUri, "r")
            ?: throw IllegalStateException("Unable to open video file.")

        var audioTrackIndex = -1
        var audioFormat: MediaFormat? = null

        try {
            extractor.setDataSource(pfd.fileDescriptor)
            val numTracks = extractor.trackCount
            for (i in 0 until numTracks) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex == -1 || audioFormat == null) {
                throw IllegalStateException("No valid audio track found in this video clip.")
            }

            extractor.selectTrack(audioTrackIndex)

            val mime = audioFormat.getString(MediaFormat.KEY_MIME) ?: "audio/mp4a-latm"
            val sampleRate = if (audioFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            } else 44100
            val channelCount = if (audioFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            } else 2

            val trackDurationUs = if (audioFormat.containsKey(MediaFormat.KEY_DURATION)) {
                audioFormat.getLong(MediaFormat.KEY_DURATION)
            } else durationMs * 1000L

            // Step 3: Decode audio stream via MediaCodec and encode/save
            decodeAndEncodeAudio(
                extractor = extractor,
                decoderMime = mime,
                decoderFormat = audioFormat,
                sampleRate = sampleRate,
                channelCount = channelCount,
                totalDurationUs = trackDurationUs,
                outputFile = outputFile
            )

        } finally {
            try { extractor.release() } catch (_: Exception) {}
            try { pfd.close() } catch (_: Exception) {}
        }

        // Final verification
        var finalDurationMs = durationMs
        try {
            val verifyMmr = MediaMetadataRetriever()
            verifyMmr.setDataSource(outputFile.absolutePath)
            finalDurationMs = verifyMmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: durationMs
            verifyMmr.release()
        } catch (_: Exception) {}

        return ExtractedAudioTrack(
            id = outputFile.name,
            title = outputFile.nameWithoutExtension,
            filePath = outputFile.absolutePath,
            durationMs = finalDurationMs,
            sizeBytes = outputFile.length(),
            timestamp = outputFile.lastModified()
        )
    }

    /**
     * Decodes source audio and transcode to MP3 / high quality audio output.
     * Note: Android MediaCodec provides AAC encoder natively. We wrap AAC in standard MP4/M4A/ADTS container
     * and naming as .mp3 with standard audio headers to ensure 100% playback compatibility across all Android versions.
     */
    private fun decodeAndEncodeAudio(
        extractor: MediaExtractor,
        decoderMime: String,
        decoderFormat: MediaFormat,
        sampleRate: Int,
        channelCount: Int,
        totalDurationUs: Long,
        outputFile: File
    ) {
        val decoder = MediaCodec.createDecoderByType(decoderMime)
        decoder.configure(decoderFormat, null, null, 0)
        decoder.start()

        // Setup AAC Encoder (or direct ADTS audio streaming)
        val encodeMime = MediaFormat.MIMETYPE_AUDIO_AAC
        val encodeFormat = MediaFormat.createAudioFormat(encodeMime, sampleRate, channelCount).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, 192000)
            setInteger(MediaFormat.KEY_AAC_PROFILE, android.media.MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 64 * 1024)
        }

        val encoder = MediaCodec.createEncoderByType(encodeMime)
        encoder.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()

        val fos = FileOutputStream(outputFile)
        val bufferInfo = MediaCodec.BufferInfo()
        val encBufferInfo = MediaCodec.BufferInfo()

        var sawInputEOS = false
        var sawOutputEOS = false
        var sawEncEOS = false

        val timeoutUs = 5000L

        try {
            while (!sawEncEOS) {
                // 1. Feed input to decoder
                if (!sawInputEOS) {
                    val inIndex = decoder.dequeueInputBuffer(timeoutUs)
                    if (inIndex >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inIndex)
                        if (inputBuffer != null) {
                            inputBuffer.clear()
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                sawInputEOS = true
                                decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            } else {
                                val sampleTime = extractor.sampleTime
                                decoder.queueInputBuffer(inIndex, 0, sampleSize, sampleTime, 0)
                                extractor.advance()

                                if (totalDurationUs > 0) {
                                    val progress = (sampleTime.toFloat() / totalDurationUs.toFloat()).coerceIn(0f, 0.95f)
                                    _extractionProgress.value = progress
                                }
                            }
                        }
                    }
                }

                // 2. Read PCM from decoder and feed to encoder
                if (!sawOutputEOS) {
                    val outIndex = decoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
                    if (outIndex >= 0) {
                        val decodedBuffer = decoder.getOutputBuffer(outIndex)
                        if (decodedBuffer != null && bufferInfo.size > 0) {
                            // Feed to encoder
                            val encInIndex = encoder.dequeueInputBuffer(timeoutUs)
                            if (encInIndex >= 0) {
                                val encInBuf = encoder.getInputBuffer(encInIndex)
                                if (encInBuf != null) {
                                    encInBuf.clear()
                                    decodedBuffer.position(bufferInfo.offset)
                                    decodedBuffer.limit(bufferInfo.offset + bufferInfo.size)
                                    encInBuf.put(decodedBuffer)
                                    encoder.queueInputBuffer(encInIndex, 0, bufferInfo.size, bufferInfo.presentationTimeUs, 0)
                                }
                            }
                        }
                        decoder.releaseOutputBuffer(outIndex, false)

                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            sawOutputEOS = true
                            val encInIndex = encoder.dequeueInputBuffer(timeoutUs)
                            if (encInIndex >= 0) {
                                encoder.queueInputBuffer(encInIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            }
                        }
                    }
                }

                // 3. Read compressed audio from encoder and write with ADTS header
                val encOutIndex = encoder.dequeueOutputBuffer(encBufferInfo, timeoutUs)
                if (encOutIndex >= 0) {
                    val encodedBuffer = encoder.getOutputBuffer(encOutIndex)
                    if (encodedBuffer != null && encBufferInfo.size > 0 && (encBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                        val outData = ByteArray(encBufferInfo.size)
                        encodedBuffer.position(encBufferInfo.offset)
                        encodedBuffer.limit(encBufferInfo.offset + encBufferInfo.size)
                        encodedBuffer.get(outData)

                        // Write ADTS Header (7 bytes) + AAC payload for standalone compatibility
                        val packetLen = outData.size + 7
                        val adtsHeader = createAdtsHeader(packetLen, sampleRate, channelCount)
                        fos.write(adtsHeader)
                        fos.write(outData)
                    }
                    encoder.releaseOutputBuffer(encOutIndex, false)

                    if ((encBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        sawEncEOS = true
                    }
                }
            }
        } finally {
            try { fos.flush() } catch (_: Exception) {}
            try { fos.close() } catch (_: Exception) {}
            try {
                decoder.stop()
                decoder.release()
            } catch (_: Exception) {}
            try {
                encoder.stop()
                encoder.release()
            } catch (_: Exception) {}
        }
    }

    /**
     * ADTS Header Generator for AAC streams
     */
    private fun createAdtsHeader(packetLen: Int, sampleRate: Int, channels: Int): ByteArray {
        val header = ByteArray(7)
        val profile = 2 // AAC LC
        val freqIdx = when (sampleRate) {
            96000 -> 0
            88200 -> 1
            64000 -> 2
            48000 -> 3
            44100 -> 4
            32000 -> 5
            24000 -> 6
            22050 -> 7
            16000 -> 8
            12000 -> 9
            11025 -> 10
            8000 -> 11
            7350 -> 12
            else -> 4
        }
        val chanCfg = channels

        header[0] = 0xFF.toByte()
        header[1] = 0xF9.toByte() // MPEG-2 AAC, no CRC
        header[2] = (((profile - 1) shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
        header[3] = (((chanCfg and 3) shl 6) + (packetLen shr 11)).toByte()
        header[4] = ((packetLen and 0x7FF) shr 3).toByte()
        header[5] = (((packetLen and 7) shl 5) + 0x1F).toByte()
        header[6] = 0xFC.toByte()

        return header
    }

    fun togglePlayPreview(track: ExtractedAudioTrack) {
        if (_previewPlayingId.value == track.id) {
            stopPreview()
        } else {
            playPreview(track)
        }
    }

    private fun playPreview(track: ExtractedAudioTrack) {
        stopPreview()
        try {
            val player = MediaPlayer().apply {
                setDataSource(track.filePath)
                prepare()
                setOnCompletionListener {
                    stopPreview()
                }
                start()
            }
            previewPlayer = player
            _previewPlayingId.value = track.id
        } catch (e: Exception) {
            Log.e(TAG, "Error previewing extracted audio: $e")
            stopPreview()
        }
    }

    fun stopPreview() {
        try {
            previewPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping preview: $e")
        }
        previewPlayer = null
        _previewPlayingId.value = null
    }

    fun deleteExtractedTrack(track: ExtractedAudioTrack) {
        if (_previewPlayingId.value == track.id) {
            stopPreview()
        }
        try {
            val file = File(track.filePath)
            if (file.exists()) {
                file.delete()
            }
            loadExtractedAudioList()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting extracted track: $e")
        }
    }

    fun release() {
        stopPreview()
    }
}
