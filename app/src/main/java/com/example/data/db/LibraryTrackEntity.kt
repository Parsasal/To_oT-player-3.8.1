package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "library_tracks")
data class LibraryTrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uriString: String,
    val title: String,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val dateAddedSec: Long = System.currentTimeMillis() / 1000,
    val isDemo: Boolean = false,
    val sourceTag: String = "Imported" // "Sample", "Recording", "Device", "Imported"
)
