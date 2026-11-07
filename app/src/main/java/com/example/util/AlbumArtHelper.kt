package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AlbumArtHelper {
    private const val TAG = "AlbumArtHelper"

    // In-memory cache for fast, stutter-free UI rendering (20MB)
    private val memoryCache: LruCache<String, ByteArray> = object : LruCache<String, ByteArray>(20 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ByteArray): Int {
            return value.size
        }
    }

    private val EMPTY_BYTES = ByteArray(0)

    fun getAlbumArtBytesSync(context: Context, uriString: String?): ByteArray? {
        if (uriString.isNullOrBlank()) return null

        // Check memory cache first (including negative cache hits)
        memoryCache.get(uriString)?.let {
            return if (it.isEmpty()) null else it
        }

        try {
            val uri = Uri.parse(uriString)
            val retriever = MediaMetadataRetriever()
            try {
                if (uriString.startsWith("content://") || uriString.startsWith("android.resource://")) {
                    retriever.setDataSource(context, uri)
                } else if (uriString.startsWith("file://")) {
                    retriever.setDataSource(uri.path)
                } else {
                    retriever.setDataSource(uriString)
                }
                val pic = retriever.embeddedPicture
                if (pic != null && pic.isNotEmpty()) {
                    memoryCache.put(uriString, pic)
                    return pic
                }
            } catch (e: Exception) {
                Log.d(TAG, "MediaMetadataRetriever could not extract embedded art for $uriString: ${e.message}")
            } finally {
                try {
                    retriever.release()
                } catch (ignored: Exception) {}
            }

            // Fallback for MediaStore Content Uris: try opening album art stream
            if (uriString.startsWith("content://media/")) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bytes = stream.readBytes()
                        if (bytes.isNotEmpty()) {
                            memoryCache.put(uriString, bytes)
                            return bytes
                        }
                    }
                } catch (ignored: Exception) {}
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed loading album art: $e")
        }

        // Cache negative result so we don't query MediaMetadataRetriever repeatedly
        memoryCache.put(uriString, EMPTY_BYTES)
        return null
    }

    suspend fun getAlbumArtBytes(context: Context, uriString: String?): ByteArray? = withContext(Dispatchers.IO) {
        getAlbumArtBytesSync(context, uriString)
    }

    fun getAlbumArtBitmapSync(context: Context, uriString: String?, maxDimension: Int = 512): Bitmap? {
        val bytes = getAlbumArtBytesSync(context, uriString) ?: return null
        return try {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight
            var inSampleSize = 1
            if (origHeight > maxDimension || origWidth > maxDimension) {
                val halfHeight = origHeight / 2
                val halfWidth = origWidth / 2
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
        } catch (t: Throwable) {
            Log.w(TAG, "Error decoding album art bitmap: $t")
            null
        }
    }

    suspend fun getAlbumArtBitmap(context: Context, uriString: String?, maxDimension: Int = 512): Bitmap? = withContext(Dispatchers.IO) {
        getAlbumArtBitmapSync(context, uriString, maxDimension)
    }
}
