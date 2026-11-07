package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.audio.PlaylistItem
import java.io.File

object PlaylistShareHelper {

    /**
     * Shares all tracks of a given playlist.
     * Uses ACTION_SEND for single track or ACTION_SEND_MULTIPLE for multiple tracks.
     */
    fun shareAllPlaylistTracks(
        context: Context,
        playlistName: String,
        tracks: List<PlaylistItem>,
        isPersianRtl: Boolean = false
    ) {
        if (tracks.isEmpty()) {
            val emptyMsg = if (isPersianRtl) "این لیست پخش فاقد قطعه صوتی برای اشتراک‌گذاری است"
            else "This playlist has no audio tracks to share"
            Toast.makeText(context, emptyMsg, Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uris = ArrayList<Uri>()
            for (track in tracks) {
                val uriString = track.uriString
                val uri = if (uriString.startsWith("content://")) {
                    Uri.parse(uriString)
                } else if (uriString.startsWith("file://")) {
                    val path = Uri.parse(uriString).path
                    if (path != null) {
                        val file = File(path)
                        if (file.exists()) {
                            try {
                                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            } catch (_: Exception) {
                                Uri.parse(uriString)
                            }
                        } else Uri.parse(uriString)
                    } else Uri.parse(uriString)
                } else {
                    val file = File(uriString)
                    if (file.exists()) {
                        try {
                            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        } catch (_: Exception) {
                            Uri.parse(uriString)
                        }
                    } else {
                        Uri.parse(uriString)
                    }
                }
                uris.add(uri)
            }

            if (uris.isEmpty()) {
                val emptyMsg = if (isPersianRtl) "هیچ قطعه صوتی برای اشتراک پیدا نشد"
                else "No shareable audio tracks found"
                Toast.makeText(context, emptyMsg, Toast.LENGTH_SHORT).show()
                return
            }

            val shareIntent = if (uris.size == 1) {
                Intent(Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, uris[0])
                    putExtra(Intent.EXTRA_SUBJECT, playlistName)
                    putExtra(
                        Intent.EXTRA_TEXT,
                        if (isPersianRtl) "اشتراک قطعه «${tracks.first().title}» از لیست پخش $playlistName"
                        else "Audio track '${tracks.first().title}' from playlist $playlistName"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "audio/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    putExtra(Intent.EXTRA_SUBJECT, playlistName)
                    putExtra(
                        Intent.EXTRA_TEXT,
                        if (isPersianRtl) "اشتراک ${uris.size} قطعه صوتی از لیست پخش $playlistName"
                        else "Sharing ${uris.size} audio tracks from playlist $playlistName"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

            val chooserTitle = if (isPersianRtl) "اشتراک تمام تراک‌های $playlistName"
            else "Share all tracks from $playlistName"

            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            val errMsg = if (isPersianRtl) "خطا در اشتراک‌گذاری: ${e.localizedMessage}"
            else "Error sharing tracks: ${e.localizedMessage}"
            Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
        }
    }
}
