package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.data.model.DeviceAudioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DeviceAudioScanner(private val context: Context) {

    companion object {
        private const val TAG = "DeviceAudioScanner"
    }

    suspend fun scanAudioFiles(maxResults: Int = 1000): List<DeviceAudioFile> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<DeviceAudioFile>()
        try {
            val contentUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val projection = mutableListOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                projection.add(MediaStore.Audio.Media.RELATIVE_PATH)
            }

            // We order primarily by DATE_ADDED DESC (so most recently downloaded/added appear first)
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            context.contentResolver.query(
                contentUri,
                projection.toTypedArray(),
                "${MediaStore.Audio.Media.SIZE} > 0",
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val dateModCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                val nameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                } else -1

                while (cursor.moveToNext() && audioList.size < maxResults) {
                    try {
                        val id = cursor.getLong(idCol)
                        val title = if (titleCol != -1) cursor.getString(titleCol) ?: "Unknown Track" else "Unknown Track"
                        val artist = if (artistCol != -1) cursor.getString(artistCol) else null
                        val album = if (albumCol != -1) cursor.getString(albumCol) else null
                        val duration = if (durationCol != -1) cursor.getLong(durationCol) else 0L
                        val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                        val dateAdded = if (dateAddedCol != -1) cursor.getLong(dateAddedCol) else 0L
                        val dateMod = if (dateModCol != -1) cursor.getLong(dateModCol) else 0L
                        val displayName = if (nameCol != -1) cursor.getString(nameCol) ?: title else title
                        val dataPath = if (dataCol != -1) cursor.getString(dataCol) else null
                        val relPath = if (relPathCol != -1) cursor.getString(relPathCol) else null

                        val itemUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                        val isDownload = (relPath?.contains("Download", ignoreCase = true) == true) ||
                                (dataPath?.contains("/Download", ignoreCase = true) == true) ||
                                (dataPath?.contains("/Telegram", ignoreCase = true) == true) ||
                                (dataPath?.contains("/WhatsApp", ignoreCase = true) == true) ||
                                (displayName.contains("download", ignoreCase = true))

                        // Filter out empty or unreadable audio if size is 0
                        if (size > 0 || duration > 0) {
                            audioList.add(
                                DeviceAudioFile(
                                    id = id,
                                    uri = itemUri,
                                    title = if (title.isBlank()) displayName else title,
                                    artist = if (artist == "<unknown>" || artist.isNullOrBlank()) null else artist,
                                    album = if (album == "<unknown>" || album.isNullOrBlank()) null else album,
                                    durationMs = duration,
                                    sizeBytes = size,
                                    dateAddedSec = dateAdded,
                                    dateModifiedSec = dateMod,
                                    path = dataPath,
                                    relativePath = relPath,
                                    displayName = displayName,
                                    isFromDownloadFolder = isDownload
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing single audio row: $e")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore audio: $e")
        }

        // Also check app internal recordings folder if any
        try {
            val recDir = File(context.filesDir, "recordings")
            if (recDir.exists() && recDir.isDirectory) {
                val files = recDir.listFiles() ?: emptyArray()
                for (f in files) {
                    if (f.isFile && (f.name.endsWith(".mp4") || f.name.endsWith(".m4a") || f.name.endsWith(".wav") || f.name.endsWith(".mp3") || f.name.endsWith(".aac"))) {
                        val fileUri = Uri.fromFile(f)
                        val alreadyExists = audioList.any { it.displayName == f.name }
                        if (!alreadyExists) {
                            audioList.add(
                                DeviceAudioFile(
                                    id = f.hashCode().toLong(),
                                    uri = fileUri,
                                    title = f.nameWithoutExtension,
                                    artist = "TO_OT Recorder",
                                    album = "Recordings",
                                    durationMs = 0L,
                                    sizeBytes = f.length(),
                                    dateAddedSec = f.lastModified() / 1000,
                                    dateModifiedSec = f.lastModified() / 1000,
                                    path = f.absolutePath,
                                    relativePath = "recordings/",
                                    displayName = f.name,
                                    isFromDownloadFolder = false
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking internal recordings: $e")
        }

        // Also check extracted audio clips folder
        try {
            val extDir = File(context.filesDir, "extracted_audio")
            if (extDir.exists() && extDir.isDirectory) {
                val files = extDir.listFiles() ?: emptyArray()
                for (f in files) {
                    if (f.isFile && (f.name.endsWith(".wav") || f.name.endsWith(".m4a") || f.name.endsWith(".mp3") || f.name.endsWith(".aac"))) {
                        val fileUri = Uri.fromFile(f)
                        val alreadyExists = audioList.any { it.displayName == f.name }
                        if (!alreadyExists) {
                            audioList.add(
                                DeviceAudioFile(
                                    id = f.hashCode().toLong(),
                                    uri = fileUri,
                                    title = f.nameWithoutExtension,
                                    artist = "Extracted Video Audio",
                                    album = "Extracted Clips",
                                    durationMs = 0L,
                                    sizeBytes = f.length(),
                                    dateAddedSec = f.lastModified() / 1000,
                                    dateModifiedSec = f.lastModified() / 1000,
                                    path = f.absolutePath,
                                    relativePath = "extracted_audio/",
                                    displayName = f.name,
                                    isFromDownloadFolder = false
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking extracted audio: $e")
        }

        // Also check built-in and cached sample tracks
        try {
            val sampleFiles = com.example.util.SampleAudioGenerator.ensureDefaultSampleTracks(context)
            for (f in sampleFiles) {
                if (f.exists() && f.isFile) {
                    val fileUri = Uri.fromFile(f)
                    val alreadyExists = audioList.any { it.displayName == f.name || it.path == f.absolutePath }
                    if (!alreadyExists) {
                        val titleFormatted = when (f.nameWithoutExtension) {
                            "toot_chill_vibe" -> "Chill Vibe & Melodic Chords"
                            "deep_synth_wave" -> "Deep Synth Wave Pulse"
                            "lofi_study_groove" -> "Lo-Fi Study Piano Melody"
                            "sunset_acoustic_harmony" -> "Sunset Acoustic Harmony"
                            "ambient_soundscape_tones" -> "Ambient Meditation Tones"
                            else -> f.nameWithoutExtension.replace("_", " ").replaceFirstChar { it.uppercase() }
                        }
                        val artistFormatted = when (f.nameWithoutExtension) {
                            "toot_chill_vibe" -> "TO_OT Ambient Studio"
                            "deep_synth_wave" -> "Night Synth Collective"
                            "lofi_study_groove" -> "Acoustic Cafe"
                            "sunset_acoustic_harmony" -> "Echoing Strings"
                            "ambient_soundscape_tones" -> "Harmonic Resonance"
                            else -> "TO_OT Master Tracks"
                        }
                        audioList.add(
                            DeviceAudioFile(
                                id = f.hashCode().toLong(),
                                uri = fileUri,
                                title = titleFormatted,
                                artist = artistFormatted,
                                album = "Built-in Library",
                                durationMs = 14000L,
                                sizeBytes = f.length(),
                                dateAddedSec = f.lastModified() / 1000,
                                dateModifiedSec = f.lastModified() / 1000,
                                path = f.absolutePath,
                                relativePath = "samples/",
                                displayName = f.name,
                                isFromDownloadFolder = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking sample tracks: $e")
        }

        // Sort: newest first
        audioList.sortedByDescending { if (it.dateModifiedSec > 0) it.dateModifiedSec else it.dateAddedSec }
    }
}
