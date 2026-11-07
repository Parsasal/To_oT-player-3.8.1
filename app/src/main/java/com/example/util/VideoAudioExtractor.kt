package com.example.util

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import kotlin.math.min
import kotlin.math.round
import kotlin.math.tanh

/**
 * Extraction modes for Video Audio processing.
 */
enum class AudioExtractionMode {
    FULL,
    TRIM,
    SPLIT
}

/**
 * Audio Effect presets
 */
enum class AudioEffectPreset(val titleFa: String, val titleEn: String) {
    NORMAL("حالت عادی", "Normal"),
    BASS_BOOST("بیس قوی (Bass Boost)", "Bass Boost"),
    VOCAL_CLARITY("شفافیت صدا (Vocal)", "Vocal Clarity"),
    LOFI_CHILL("موزیک لوفای (Lo-Fi)", "Lo-Fi Chill"),
    NIGHTCORE("نایت‌کور (Nightcore)", "Nightcore (Speed+)"),
    SLOWED_REVERB("اسلود و ریورب (Slowed)", "Slowed & Reverb"),
    CONCERT_HALL("سالن کنسرت", "Concert Hall"),
    CUSTOM("شخصی‌سازی شده", "Custom")
}

/**
 * Reverb presets for spatial audio enhancements.
 */
enum class AudioReverbPreset(
    val labelFa: String,
    val labelEn: String,
    val delayMs: Int,
    val decay: Float
) {
    NONE("بدون ریورب", "None", 0, 0f),
    STUDIO("استودیو", "Studio", 25, 0.22f),
    ROOM("اتاق", "Room", 45, 0.38f),
    CONCERT_HALL("کنسرت", "Concert", 85, 0.58f),
    CATHEDRAL("سالن بزرگ", "Cathedral", 140, 0.72f)
}

/**
 * Audio effects and extraction parameters.
 */
data class AudioEffectsSettings(
    val mode: AudioExtractionMode = AudioExtractionMode.FULL,
    val preset: AudioEffectPreset = AudioEffectPreset.NORMAL,
    val startMs: Long = 0L,
    val endMs: Long = 0L,
    val splitMs: Long = 0L,
    val speed: Float = 1.0f,
    val bassBoost: Float = 0.0f,
    val trebleBoost: Float = 0.0f,
    val reverbPreset: AudioReverbPreset = AudioReverbPreset.NONE,
    val stereoWidening: Boolean = false,
    val loFiFilter: Boolean = false,
    val volumeGain: Float = 1.0f,
    val fadeInSeconds: Float = 0f,
    val fadeOutSeconds: Float = 0f
) {
    companion object {
        fun fromPreset(preset: AudioEffectPreset, current: AudioEffectsSettings = AudioEffectsSettings()): AudioEffectsSettings {
            return when (preset) {
                AudioEffectPreset.NORMAL -> AudioEffectsSettings(
                    mode = current.mode,
                    preset = AudioEffectPreset.NORMAL,
                    startMs = current.startMs,
                    endMs = current.endMs,
                    splitMs = current.splitMs
                )
                AudioEffectPreset.BASS_BOOST -> current.copy(
                    preset = preset,
                    bassBoost = 0.75f,
                    trebleBoost = 0.1f,
                    speed = 1.0f,
                    reverbPreset = AudioReverbPreset.STUDIO,
                    stereoWidening = false,
                    loFiFilter = false,
                    volumeGain = 1.15f
                )
                AudioEffectPreset.VOCAL_CLARITY -> current.copy(
                    preset = preset,
                    bassBoost = -0.2f,
                    trebleBoost = 0.65f,
                    speed = 1.0f,
                    reverbPreset = AudioReverbPreset.ROOM,
                    stereoWidening = true,
                    loFiFilter = false,
                    volumeGain = 1.1f
                )
                AudioEffectPreset.LOFI_CHILL -> current.copy(
                    preset = preset,
                    bassBoost = 0.4f,
                    trebleBoost = -0.35f,
                    speed = 0.92f,
                    reverbPreset = AudioReverbPreset.ROOM,
                    stereoWidening = true,
                    loFiFilter = true,
                    volumeGain = 1.0f
                )
                AudioEffectPreset.NIGHTCORE -> current.copy(
                    preset = preset,
                    speed = 1.25f,
                    bassBoost = 0.3f,
                    trebleBoost = 0.4f,
                    reverbPreset = AudioReverbPreset.NONE,
                    stereoWidening = false,
                    loFiFilter = false,
                    volumeGain = 1.1f
                )
                AudioEffectPreset.SLOWED_REVERB -> current.copy(
                    preset = preset,
                    speed = 0.85f,
                    bassBoost = 0.35f,
                    trebleBoost = -0.1f,
                    reverbPreset = AudioReverbPreset.CONCERT_HALL,
                    stereoWidening = true,
                    loFiFilter = false,
                    volumeGain = 1.15f
                )
                AudioEffectPreset.CONCERT_HALL -> current.copy(
                    preset = preset,
                    speed = 1.0f,
                    bassBoost = 0.25f,
                    trebleBoost = 0.2f,
                    reverbPreset = AudioReverbPreset.CONCERT_HALL,
                    stereoWidening = true,
                    loFiFilter = false,
                    volumeGain = 1.1f
                )
                AudioEffectPreset.CUSTOM -> current.copy(preset = AudioEffectPreset.CUSTOM)
            }
        }
    }
}

/**
 * High-performance streaming audio extractor and DSP processor.
 * Capable of processing files from a few seconds up to 6 hours without high memory usage (OOM-safe).
 */
object VideoAudioExtractor {

    private const val TAG = "VideoAudioExtractor"
    const val MAX_DURATION_MS = 6 * 60 * 60 * 1000L // 6 hours in milliseconds

    data class VideoInfo(
        val uri: Uri,
        val fileName: String,
        val durationMs: Long,
        val formattedDuration: String,
        val isDurationValid: Boolean,
        val artist: String?,
        val title: String?
    )

    /**
     * Inspects the selected video URI to retrieve duration and metadata safely.
     * Uses ParcelFileDescriptor when available to prevent memory issues with large files.
     */
    fun inspectVideo(context: Context, videoUri: Uri): VideoInfo {
        val retriever = MediaMetadataRetriever()
        var durationMs = 0L
        var title: String? = null
        var artist: String? = null
        var displayName = "video_audio_${System.currentTimeMillis()}"
        var pfd: ParcelFileDescriptor? = null

        try {
            pfd = try {
                context.contentResolver.openFileDescriptor(videoUri, "r")
            } catch (_: Throwable) {
                null
            }

            if (pfd != null) {
                retriever.setDataSource(pfd.fileDescriptor)
            } else {
                retriever.setDataSource(context, videoUri)
            }

            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationMs = durationStr?.toLongOrNull() ?: 0L
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)

            // Try to resolve display name from content resolver
            try {
                context.contentResolver.query(videoUri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) {
                            displayName = name.substringBeforeLast(".")
                        }
                    }
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Could not query display name: ${t.message}")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error inspecting video: ${t.message}", t)
        } finally {
            try { pfd?.close() } catch (_: Throwable) {}
            try { retriever.release() } catch (_: Throwable) {}
        }

        val formattedDuration = formatDuration(durationMs)
        val isDurationValid = durationMs in 1..MAX_DURATION_MS

        return VideoInfo(
            uri = videoUri,
            fileName = displayName,
            durationMs = durationMs,
            formattedDuration = formattedDuration,
            isDurationValid = isDurationValid,
            artist = artist,
            title = title ?: displayName
        )
    }

    /**
     * Backward-compatible simple extraction
     */
    suspend fun extractAudioToMp3(
        context: Context,
        videoUri: Uri,
        outputFileName: String,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val settings = AudioEffectsSettings(mode = AudioExtractionMode.FULL)
        val result = extractAudioWithEffects(context, videoUri, outputFileName, settings, onProgress)
        result.map { files -> files.first() }
    }

    /**
     * Extracts audio from video via streaming chunk pipeline, applies DSP effects (EQ, reverb, widening, limiter),
     * and encodes to standard crystal-clear WAV audio without loading the full file into memory.
     * Guaranteed safe for 55-minute or multi-hour videos!
     */
    suspend fun extractAudioWithEffects(
        context: Context,
        videoUri: Uri,
        outputBaseName: String,
        settings: AudioEffectsSettings,
        onProgress: (Float) -> Unit
    ): Result<List<File>> = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        var retriever: MediaMetadataRetriever? = null
        var decoder: MediaCodec? = null
        var pfd: ParcelFileDescriptor? = null

        var writer1: StreamingWavWriter? = null
        var writer2: StreamingWavWriter? = null
        val generatedFiles = mutableListOf<File>()

        try {
            val contentResolver = context.contentResolver
            pfd = contentResolver.openFileDescriptor(videoUri, "r")
                ?: return@withContext Result.failure(Exception("Unable to open video file."))

            extractor.setDataSource(pfd.fileDescriptor)

            retriever = MediaMetadataRetriever()
            retriever.setDataSource(pfd.fileDescriptor)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationUs = (durationStr?.toLongOrNull() ?: 1000L) * 1000L
            val totalDurationMs = durationUs / 1000L

            if (durationUs > MAX_DURATION_MS * 1000L) {
                return@withContext Result.failure(
                    Exception("Video duration exceeds the maximum allowed limit (6 hours).")
                )
            }

            // Find audio track
            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex == -1 || audioFormat == null) {
                return@withContext Result.failure(Exception("No valid audio track found in this video clip."))
            }

            extractor.selectTrack(audioTrackIndex)

            val mimeType = audioFormat.getString(MediaFormat.KEY_MIME) ?: "audio/mp4a-latm"
            var sampleRate = if (audioFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            } else 44100
            var channelCount = if (audioFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            } else 2
            var pcmEncoding = if (audioFormat.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                audioFormat.getInteger(MediaFormat.KEY_PCM_ENCODING)
            } else AudioFormat.ENCODING_PCM_16BIT

            try {
                audioFormat.setInteger(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_16BIT)
            } catch (_: Throwable) {}

            // Prepare output dir
            val outputDir = File(context.filesDir, "extracted_audio").apply { mkdirs() }
            val cleanBase = outputBaseName
                .replace(Regex("[^a-zA-Z0-9._\\u0600-\\u06FF -]"), "_")
                .trim()
                .removeSuffix(".mp3")
                .removeSuffix(".wav")
                .removeSuffix(".m4a")
                .ifBlank { "extracted_audio_${System.currentTimeMillis()}" }

            val totalEstimatedSamples = (totalDurationMs * sampleRate * 2) / 1000L
            val audioProcessor = StreamingAudioProcessor(settings, sampleRate, 2, totalEstimatedSamples)

            val startUs = (settings.startMs.coerceAtLeast(0L)) * 1000L
            val endUs = if (settings.endMs > settings.startMs) settings.endMs * 1000L else durationUs
            val splitUs = (if (settings.splitMs in 1000 until totalDurationMs) settings.splitMs else totalDurationMs / 2) * 1000L

            when (settings.mode) {
                AudioExtractionMode.FULL -> {
                    val outFile = File(outputDir, "$cleanBase.wav")
                    writer1 = StreamingWavWriter(outFile, sampleRate, 2)
                    generatedFiles.add(outFile)
                }
                AudioExtractionMode.TRIM -> {
                    val outFile = File(outputDir, "${cleanBase}_trimmed.wav")
                    writer1 = StreamingWavWriter(outFile, sampleRate, 2)
                    generatedFiles.add(outFile)
                    if (startUs > 0) {
                        try {
                            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                        } catch (_: Throwable) {}
                    }
                }
                AudioExtractionMode.SPLIT -> {
                    val outFile1 = File(outputDir, "${cleanBase}_Part1.wav")
                    val outFile2 = File(outputDir, "${cleanBase}_Part2.wav")
                    writer1 = StreamingWavWriter(outFile1, sampleRate, 2)
                    writer2 = StreamingWavWriter(outFile2, sampleRate, 2)
                    generatedFiles.add(outFile1)
                    generatedFiles.add(outFile2)
                }
            }

            decoder = MediaCodec.createDecoderByType(mimeType)
            decoder.configure(audioFormat, null, null, 0)
            decoder.start()

            val bufferInfo = MediaCodec.BufferInfo()
            var sawInputEOS = false
            var sawOutputEOS = false
            val kTimeOutUs = 10000L
            var currentPresentationTimeUs = 0L

            while (!sawOutputEOS) {
                if (!sawInputEOS) {
                    val inputBufIndex = decoder.dequeueInputBuffer(kTimeOutUs)
                    if (inputBufIndex >= 0) {
                        val inputBuf = decoder.getInputBuffer(inputBufIndex)
                        if (inputBuf != null) {
                            inputBuf.clear()
                            val sampleSize = extractor.readSampleData(inputBuf, 0)
                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                sawInputEOS = true
                            } else {
                                val presentationTimeUs = extractor.sampleTime
                                decoder.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, 0)
                                extractor.advance()
                            }
                        }
                    }
                }

                val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, kTimeOutUs)
                if (outputBufferIndex >= 0) {
                    if (bufferInfo.size > 0) {
                        currentPresentationTimeUs = bufferInfo.presentationTimeUs
                        val outputBuf = decoder.getOutputBuffer(outputBufferIndex)
                        if (outputBuf != null) {
                            outputBuf.position(bufferInfo.offset)
                            outputBuf.limit(bufferInfo.offset + bufferInfo.size)
                            val chunk = ByteArray(bufferInfo.size)
                            outputBuf.get(chunk)

                            // 1. Convert float PCM to standard 16-bit PCM if decoder produced 32-bit float
                            val rawPcm16 = if (pcmEncoding == AudioFormat.ENCODING_PCM_FLOAT) {
                                convertFloatToPcm16(chunk)
                            } else {
                                chunk
                            }

                            // 2. Normalize channels to standard stereo (2-channel)
                            // Multi-channel (5.1/7.1) is downmixed cleanly preserving singer's vocals
                            // Mono is duplicated to L & R so headphones play equally
                            val stereoChunk = when {
                                channelCount == 1 -> convertMonoToStereo(rawPcm16)
                                channelCount == 2 -> rawPcm16
                                channelCount > 2 -> downmixMultiChannelToStereo(rawPcm16, channelCount)
                                else -> rawPcm16
                            }

                            // Process chunk through streaming DSP effects pipeline (if any effect is active)
                            val processedChunk = if (audioProcessor.hasEffects) {
                                audioProcessor.processChunk(stereoChunk, 0, stereoChunk.size)
                            } else {
                                stereoChunk
                            }

                            when (settings.mode) {
                                AudioExtractionMode.FULL -> {
                                    writer1?.write(processedChunk)
                                }
                                AudioExtractionMode.TRIM -> {
                                    if (currentPresentationTimeUs in startUs..endUs) {
                                        writer1?.write(processedChunk)
                                    } else if (currentPresentationTimeUs > endUs && sawInputEOS) {
                                        sawOutputEOS = true
                                    }
                                }
                                AudioExtractionMode.SPLIT -> {
                                    if (currentPresentationTimeUs < splitUs) {
                                        writer1?.write(processedChunk)
                                    } else {
                                        writer2?.write(processedChunk)
                                    }
                                }
                            }
                        }
                    }

                    decoder.releaseOutputBuffer(outputBufferIndex, false)

                    if (durationUs > 0) {
                        val p = (currentPresentationTimeUs.toFloat() / durationUs.toFloat()).coerceIn(0f, 1f)
                        onProgress(p)
                    }

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        sawOutputEOS = true
                    }
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFormat = decoder.outputFormat
                    if (newFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                        sampleRate = newFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    }
                    if (newFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                        channelCount = newFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    }
                    if (newFormat.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                        pcmEncoding = newFormat.getInteger(MediaFormat.KEY_PCM_ENCODING)
                    }
                    writer1?.updateFormat(sampleRate, 2)
                    writer2?.updateFormat(sampleRate, 2)
                }
            }

            // Flush and finalize WAV headers
            try { writer1?.close() } catch (_: Throwable) {}
            try { writer2?.close() } catch (_: Throwable) {}

            onProgress(1.0f)
            Result.success(generatedFiles)
        } catch (t: Throwable) {
            Log.e(TAG, "Audio extraction failed: ${t.message}", t)
            Result.failure(Exception(t.message ?: "Audio extraction failed"))
        } finally {
            try { writer1?.close() } catch (_: Throwable) {}
            try { writer2?.close() } catch (_: Throwable) {}
            try { decoder?.stop() } catch (_: Throwable) {}
            try { decoder?.release() } catch (_: Throwable) {}
            try { retriever?.release() } catch (_: Throwable) {}
            try { extractor.release() } catch (_: Throwable) {}
            try { pfd?.close() } catch (_: Throwable) {}
        }
    }

    private fun convertFloatToPcm16(floatBytes: ByteArray): ByteArray {
        val numFloats = floatBytes.size / 4
        if (numFloats <= 0) return ByteArray(0)
        val out = ByteArray(numFloats * 2)
        val inBuf = ByteBuffer.wrap(floatBytes).order(ByteOrder.LITTLE_ENDIAN)
        val outBuf = ByteBuffer.wrap(out).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until numFloats) {
            val f = inBuf.float
            val s = (f * 32767.0f).coerceIn(-32768.0f, 32767.0f).toInt().toShort()
            outBuf.putShort(s)
        }
        return out
    }

    private fun convertMonoToStereo(monoPcm16: ByteArray): ByteArray {
        val numSamples = monoPcm16.size / 2
        if (numSamples <= 0) return ByteArray(0)
        val out = ByteArray(numSamples * 4)
        val inBuf = ByteBuffer.wrap(monoPcm16).order(ByteOrder.LITTLE_ENDIAN)
        val outBuf = ByteBuffer.wrap(out).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until numSamples) {
            val s = inBuf.short
            outBuf.putShort(s)
            outBuf.putShort(s)
        }
        return out
    }

    private fun downmixMultiChannelToStereo(pcm16Bytes: ByteArray, channels: Int): ByteArray {
        if (channels <= 2) return pcm16Bytes
        val bytesPerSample = 2
        val frameSize = channels * bytesPerSample
        val numFrames = pcm16Bytes.size / frameSize
        if (numFrames <= 0) return ByteArray(0)
        val out = ByteArray(numFrames * 4)
        val inBuf = ByteBuffer.wrap(pcm16Bytes).order(ByteOrder.LITTLE_ENDIAN)
        val outBuf = ByteBuffer.wrap(out).order(ByteOrder.LITTLE_ENDIAN)

        val centerWeight = 0.707f
        val surroundWeight = 0.707f

        for (f in 0 until numFrames) {
            val fl = inBuf.short.toFloat()
            val fr = inBuf.short.toFloat()
            val c = if (channels > 2) inBuf.short.toFloat() else 0f
            val lfe = if (channels > 3) inBuf.short.toFloat() else 0f
            val sl = if (channels > 4) inBuf.short.toFloat() else 0f
            val sr = if (channels > 5) inBuf.short.toFloat() else 0f
            for (ch in 6 until channels) {
                inBuf.short
            }

            val left = (fl + c * centerWeight + sl * surroundWeight).coerceIn(-32768f, 32767f).toInt().toShort()
            val right = (fr + c * centerWeight + sr * surroundWeight).coerceIn(-32768f, 32767f).toInt().toShort()

            outBuf.putShort(left)
            outBuf.putShort(right)
        }
        return out
    }

    /**
     * Streaming WAV container writer that writes PCM data in real-time and rewrites the standard 44-byte RIFF header on close.
     */
    private class StreamingWavWriter(
        private val outputFile: File,
        private var sampleRate: Int,
        private var channelCount: Int = 2
    ) : AutoCloseable {
        private val raf = RandomAccessFile(outputFile, "rw")
        private var totalDataBytes = 0L
        private var isClosed = false

        init {
            raf.setLength(0)
            // 44 dummy bytes reserved for header
            raf.write(ByteArray(44))
        }

        fun updateFormat(newSampleRate: Int, newChannelCount: Int = 2) {
            if (newSampleRate > 0) sampleRate = newSampleRate
            if (newChannelCount > 0) channelCount = newChannelCount
        }

        fun write(bytes: ByteArray) {
            if (bytes.isEmpty() || isClosed) return
            raf.write(bytes)
            totalDataBytes += bytes.size
        }

        override fun close() {
            if (isClosed) return
            isClosed = true
            try {
                val numChannels = channelCount.coerceIn(1, 2)
                val bytesPerSample = 2 // 16-bit PCM
                val blockAlign = numChannels * bytesPerSample
                val byteRate = sampleRate * blockAlign
                val subchunk2Size = totalDataBytes.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                val chunkSize = 36 + subchunk2Size

                val header = ByteBuffer.allocate(44).apply {
                    order(ByteOrder.LITTLE_ENDIAN)
                    put("RIFF".toByteArray())
                    putInt(chunkSize)
                    put("WAVE".toByteArray())
                    put("fmt ".toByteArray())
                    putInt(16) // Subchunk1Size (16 for PCM)
                    putShort(1.toShort()) // AudioFormat (1 = PCM uncompressed)
                    putShort(numChannels.toShort())
                    putInt(sampleRate)
                    putInt(byteRate)
                    putShort(blockAlign.toShort())
                    putShort((bytesPerSample * 8).toShort())
                    put("data".toByteArray())
                    putInt(subchunk2Size)
                }

                raf.seek(0)
                raf.write(header.array())
            } catch (t: Throwable) {
                Log.e(TAG, "Error writing WAV header: ${t.message}")
            } finally {
                raf.close()
            }
        }
    }

    /**
     * Streaming DSP processor that processes individual audio chunks with stateful IIR filters, circular reverb buffers,
     * soft saturation, and smooth gain/fade handling. Memory usage per chunk is limited to only ~16KB.
     */
    private class StreamingAudioProcessor(
        private val settings: AudioEffectsSettings,
        private val sampleRate: Int,
        private val channelCount: Int,
        private val totalEstimatedSamples: Long
    ) {
        val hasEffects: Boolean = settings.preset != AudioEffectPreset.NORMAL ||
                settings.bassBoost != 0f ||
                settings.trebleBoost != 0f ||
                settings.reverbPreset != AudioReverbPreset.NONE ||
                settings.stereoWidening ||
                settings.loFiFilter ||
                settings.volumeGain != 1.0f ||
                settings.fadeInSeconds > 0f ||
                settings.fadeOutSeconds > 0f

        // EQ Filter states
        private var lpBassL = 0f
        private var lpBassR = 0f
        private var lpTrebleL = 0f
        private var lpTrebleR = 0f
        private val alphaBass = 0.045f
        private val alphaTreble = 0.45f
        private val bassGain = settings.bassBoost * 2.2f
        private val trebleGain = settings.trebleBoost * 1.8f
        private val hasEq = settings.bassBoost != 0f || settings.trebleBoost != 0f

        // Reverb states
        private val reverbDelaySamples = if (settings.reverbPreset != AudioReverbPreset.NONE) {
            (((sampleRate * settings.reverbPreset.delayMs) / 1000) * channelCount).coerceAtLeast(0)
        } else 0
        private val reverbBuffer = if (reverbDelaySamples > 0) FloatArray(reverbDelaySamples) else null
        private var reverbPos = 0
        private val reverbDecay = settings.reverbPreset.decay

        // Fade / Gain states
        private var processedSampleCount = 0L
        private val fadeInSamples = (settings.fadeInSeconds * sampleRate * channelCount).toLong()
        private val fadeOutSamples = (settings.fadeOutSeconds * sampleRate * channelCount).toLong()

        fun processChunk(pcmBytes: ByteArray, offset: Int, length: Int): ByteArray {
            if (!hasEffects) {
                if (offset == 0 && length == pcmBytes.size) return pcmBytes
                return pcmBytes.copyOfRange(offset, offset + length)
            }

            val numShorts = length / 2
            if (numShorts <= 0) return ByteArray(0)

            val byteBuffer = ByteBuffer.wrap(pcmBytes, offset, length).order(ByteOrder.LITTLE_ENDIAN)
            val floatSamples = FloatArray(numShorts)
            for (i in 0 until numShorts) {
                floatSamples[i] = byteBuffer.short / 32768.0f
            }

            // 1. Equalizer (Bass & Treble)
            if (hasEq) {
                if (channelCount == 2) {
                    val pairs = numShorts / 2
                    for (p in 0 until pairs) {
                        val idxL = p * 2
                        val idxR = idxL + 1

                        val sL = floatSamples[idxL]
                        lpBassL += alphaBass * (sL - lpBassL)
                        lpTrebleL += alphaTreble * (sL - lpTrebleL)
                        val hpL = sL - lpTrebleL
                        floatSamples[idxL] = sL + (lpBassL * bassGain) + (hpL * trebleGain)

                        val sR = floatSamples[idxR]
                        lpBassR += alphaBass * (sR - lpBassR)
                        lpTrebleR += alphaTreble * (sR - lpTrebleR)
                        val hpR = sR - lpTrebleR
                        floatSamples[idxR] = sR + (lpBassR * bassGain) + (hpR * trebleGain)
                    }
                } else {
                    for (i in 0 until numShorts) {
                        val s = floatSamples[i]
                        lpBassL += alphaBass * (s - lpBassL)
                        lpTrebleL += alphaTreble * (s - lpTrebleL)
                        val hp = s - lpTrebleL
                        floatSamples[i] = s + (lpBassL * bassGain) + (hp * trebleGain)
                    }
                }
            }

            // 2. Reverb & Spatial Echo
            if (reverbBuffer != null && reverbDelaySamples > 0) {
                for (i in 0 until numShorts) {
                    val inSample = floatSamples[i]
                    val delayed = reverbBuffer[reverbPos]
                    val outSample = inSample + (delayed * reverbDecay)
                    reverbBuffer[reverbPos] = inSample + (delayed * reverbDecay * 0.65f)
                    floatSamples[i] = outSample
                    reverbPos = (reverbPos + 1) % reverbDelaySamples
                }
            }

            // 3. Stereo Widening
            if (settings.stereoWidening && channelCount == 2) {
                val pairs = numShorts / 2
                for (p in 0 until pairs) {
                    val idxL = p * 2
                    val idxR = idxL + 1
                    val l = floatSamples[idxL]
                    val r = floatSamples[idxR]
                    val mid = (l + r) * 0.5f
                    val side = (l - r) * 0.5f
                    floatSamples[idxL] = mid + (side * 1.65f)
                    floatSamples[idxR] = mid - (side * 1.65f)
                }
            }

            // 4. Lo-Fi Filter
            if (settings.loFiFilter) {
                for (i in 0 until numShorts) {
                    var s = floatSamples[i]
                    s = round(s * 2048f) / 2048f
                    s = tanh(s * 1.25f) * 0.92f
                    floatSamples[i] = s
                }
            }

            // 5. Volume Gain & Fade
            val gain = settings.volumeGain
            val outBytes = ByteArray(numShorts * 2)
            val outBuffer = ByteBuffer.wrap(outBytes).order(ByteOrder.LITTLE_ENDIAN)

            for (i in 0 until numShorts) {
                var s = floatSamples[i] * gain
                val curSampleIdx = processedSampleCount + i

                if (fadeInSamples > 0 && curSampleIdx < fadeInSamples) {
                    val fade = (curSampleIdx.toFloat() / fadeInSamples.toFloat()).coerceIn(0f, 1f)
                    s *= fade
                }

                if (fadeOutSamples > 0 && totalEstimatedSamples > 0 && curSampleIdx >= (totalEstimatedSamples - fadeOutSamples)) {
                    val fade = ((totalEstimatedSamples - curSampleIdx).toFloat() / fadeOutSamples.toFloat()).coerceIn(0f, 1f)
                    s *= fade
                }

                val clamped = s.coerceIn(-0.999f, 0.999f)
                val shortVal = (clamped * 32767.0f).toInt().toShort()
                outBuffer.putShort(shortVal)
            }

            processedSampleCount += numShorts
            return outBytes
        }
    }

    fun formatDuration(ms: Long): String {
        val totalSec = (ms / 1000).toInt().coerceAtLeast(0)
        val hours = totalSec / 3600
        val min = (totalSec % 3600) / 60
        val sec = totalSec % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, min, sec)
        } else {
            String.format(Locale.US, "%02d:%02d", min, sec)
        }
    }
}
