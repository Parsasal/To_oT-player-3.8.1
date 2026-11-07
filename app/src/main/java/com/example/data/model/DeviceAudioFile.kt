package com.example.data.model

import android.net.Uri
import java.util.Locale

data class DeviceAudioFile(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val sizeBytes: Long,
    val dateAddedSec: Long,
    val dateModifiedSec: Long,
    val path: String?,
    val relativePath: String?,
    val displayName: String,
    val isFromDownloadFolder: Boolean
) {
    val formattedDuration: String
        get() {
            if (durationMs <= 0) return "--:--"
            val totalSec = durationMs / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            return String.format(Locale.getDefault(), "%02d:%02d", min, sec)
        }

    val formattedSize: String
        get() {
            if (sizeBytes <= 0) return ""
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1.0) {
                String.format(Locale.getDefault(), "%.1f MB", mb)
            } else {
                val kb = sizeBytes / 1024.0
                String.format(Locale.getDefault(), "%.0f KB", kb)
            }
        }

    val formattedRelativeDate: String
        get() {
            val fileTimeSec = if (dateModifiedSec > 0) dateModifiedSec else dateAddedSec
            if (fileTimeSec <= 0) return ""
            val nowSec = System.currentTimeMillis() / 1000
            val diffSec = (nowSec - fileTimeSec).coerceAtLeast(0)
            val diffMin = diffSec / 60
            val diffHours = diffMin / 60
            val diffDays = diffHours / 24

            return when {
                diffMin < 1 -> "Just now"
                diffMin < 60 -> "$diffMin min ago"
                diffHours < 24 -> "$diffHours hr ago"
                diffDays < 7 -> "$diffDays d ago"
                diffDays < 30 -> "${diffDays / 7} w ago"
                else -> "${diffDays / 30} mo ago"
            }
        }
}
