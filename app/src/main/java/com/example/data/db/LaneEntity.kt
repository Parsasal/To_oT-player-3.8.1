package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_lanes")
data class LaneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val uriString: String? = null,
    val fileName: String? = null,
    val durationMs: Long = 0L,
    val volume: Float = 0.85f,
    val isMuted: Boolean = false,
    val isSoloed: Boolean = false,
    val playbackPositionMs: Long = 0L,
    val sortOrder: Int = 0,
    val isLoopEnabled: Boolean = false,
    val isPlaylistLoopEnabled: Boolean = false,
    val isShuffleEnabled: Boolean = false,
    val colorHex: String? = null
)
