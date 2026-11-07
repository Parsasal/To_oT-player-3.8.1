package com.example.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Generates lightweight, melodic offline sample audio tracks (.wav format)
 * to ensure the TO_OT Player Library always has playable music even on clean devices.
 */
object SampleAudioGenerator {

    private const val TAG = "SampleAudioGenerator"
    private const val SAMPLE_RATE = 22050 // Fast 22.05 kHz for instant offline synthesis

    data class SampleTrackDef(
        val filename: String,
        val title: String,
        val artist: String,
        val album: String,
        val durationSec: Int,
        val chords: List<DoubleArray>,
        val tempoBpm: Double
    )

    private val DEFAULT_TRACKS = listOf(
        SampleTrackDef(
            filename = "toot_chill_vibe.wav",
            title = "Chill Vibe & Melodic Chords",
            artist = "TO_OT Ambient Studio",
            album = "Chill Horizons",
            durationSec = 4,
            chords = listOf(
                doubleArrayOf(261.63, 329.63, 392.00), // Cmaj
                doubleArrayOf(220.00, 261.63, 329.63), // Am
                doubleArrayOf(174.61, 220.00, 261.63), // Fmaj
                doubleArrayOf(196.00, 246.94, 293.66)  // G7
            ),
            tempoBpm = 80.0
        ),
        SampleTrackDef(
            filename = "deep_synth_wave.wav",
            title = "Deep Synth Wave Pulse",
            artist = "Night Synth Collective",
            album = "Retro Glow",
            durationSec = 4,
            chords = listOf(
                doubleArrayOf(130.81, 196.00, 261.63), // C low
                doubleArrayOf(146.83, 220.00, 293.66), // Dm low
                doubleArrayOf(164.81, 246.94, 329.63), // Em low
                doubleArrayOf(130.81, 196.00, 392.00)  // C octave
            ),
            tempoBpm = 95.0
        ),
        SampleTrackDef(
            filename = "lofi_study_groove.wav",
            title = "Lo-Fi Study Piano Melody",
            artist = "Acoustic Cafe",
            album = "Focus Sessions",
            durationSec = 4,
            chords = listOf(
                doubleArrayOf(349.23, 440.00, 523.25), // Fmaj
                doubleArrayOf(329.63, 392.00, 493.88), // Em
                doubleArrayOf(293.66, 349.23, 440.00), // Dm
                doubleArrayOf(261.63, 329.63, 523.25)  // Cmaj
            ),
            tempoBpm = 75.0
        ),
        SampleTrackDef(
            filename = "sunset_acoustic_harmony.wav",
            title = "Sunset Acoustic Harmony",
            artist = "Echoing Strings",
            album = "Acoustic Lounge",
            durationSec = 4,
            chords = listOf(
                doubleArrayOf(196.00, 293.66, 392.00), // G chord
                doubleArrayOf(220.00, 261.63, 329.63), // Am chord
                doubleArrayOf(174.61, 261.63, 349.23), // F chord
                doubleArrayOf(196.00, 246.94, 392.00)  // G chord
            ),
            tempoBpm = 85.0
        ),
        SampleTrackDef(
            filename = "ambient_soundscape_tones.wav",
            title = "Ambient Meditation Tones",
            artist = "Harmonic Resonance",
            album = "Mindful Waves",
            durationSec = 4,
            chords = listOf(
                doubleArrayOf(220.00, 277.18, 440.00), // A major
                doubleArrayOf(185.00, 220.00, 369.99), // F#m
                doubleArrayOf(146.83, 220.00, 293.66), // D major
                doubleArrayOf(164.81, 246.94, 329.63)  // E major
            ),
            tempoBpm = 60.0
        )
    )

    suspend fun ensureDefaultSampleTracks(context: Context): List<File> = withContext(Dispatchers.IO) {
        val samplesDir = File(context.filesDir, "samples")
        if (!samplesDir.exists()) {
            samplesDir.mkdirs()
        }

        val generatedFiles = mutableListOf<File>()

        for (track in DEFAULT_TRACKS) {
            val file = File(samplesDir, track.filename)
            if (!file.exists() || file.length() < 1000) {
                try {
                    generateWavFile(file, track)
                    Log.d(TAG, "Generated sample track: ${file.absolutePath}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating sample track ${track.filename}: $e")
                }
            }
            if (file.exists()) {
                generatedFiles.add(file)
            }
        }
        generatedFiles
    }

    private fun generateWavFile(targetFile: File, def: SampleTrackDef) {
        val totalSamples = SAMPLE_RATE * def.durationSec
        val numChannels = 2 // Stereo
        val bytesPerSample = 2 // 16-bit
        val blockAlign = numChannels * bytesPerSample
        val byteRate = SAMPLE_RATE * blockAlign
        val dataSize = totalSamples * blockAlign

        FileOutputStream(targetFile).use { fos ->
            // WAV Header
            val header = ByteBuffer.allocate(44).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put("RIFF".toByteArray())
                putInt(36 + dataSize)
                put("WAVE".toByteArray())
                put("fmt ".toByteArray())
                putInt(16) // Subchunk1Size
                putShort(1.toShort()) // AudioFormat (1 = PCM)
                putShort(numChannels.toShort())
                putInt(SAMPLE_RATE)
                putInt(byteRate)
                putShort(blockAlign.toShort())
                putShort((bytesPerSample * 8).toShort()) // BitsPerSample
                put("data".toByteArray())
                putInt(dataSize)
            }
            fos.write(header.array())

            // Synthesize audio buffer fast
            val buffer = ByteBuffer.allocate(8192).apply { order(ByteOrder.LITTLE_ENDIAN) }
            val chordDurationSamples = (SAMPLE_RATE * (60.0 / def.tempoBpm) * 2.0).toInt().coerceAtLeast(SAMPLE_RATE / 2)

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val chordIndex = ((i / chordDurationSamples) % def.chords.size)
                val freqs = def.chords[chordIndex]

                val chordSamplePos = i % chordDurationSamples
                val chordProgress = chordSamplePos.toDouble() / chordDurationSamples
                val envelope = sin(chordProgress * PI).coerceIn(0.0, 1.0) * exp(-0.3 * chordProgress)

                var sampleValue = 0.0
                for (freq in freqs) {
                    val overtone = sin(2.0 * PI * freq * t)
                    sampleValue += overtone * (0.28 / freqs.size)
                }

                sampleValue = (sampleValue * envelope).coerceIn(-0.95, 0.95)
                val shortVal = (sampleValue * 32767.0).toInt().toShort()

                // Left and Right channels
                if (buffer.remaining() < 4) {
                    fos.write(buffer.array(), 0, buffer.position())
                    buffer.clear()
                }
                buffer.putShort(shortVal)
                buffer.putShort(shortVal)
            }

            if (buffer.position() > 0) {
                fos.write(buffer.array(), 0, buffer.position())
            }
        }
    }
}
