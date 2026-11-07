package com.example.audio

data class PlaylistItem(
    val id: Long = 0,
    val laneId: Long,
    val uriString: String,
    val title: String,
    val durationMs: Long = 0L,
    val sortOrder: Int = 0
)

data class AudioLaneState(
    val laneId: Long,
    val name: String,
    val uriString: String? = null,
    val fileName: String? = null,
    val durationMs: Long = 0L,
    val currentPositionMs: Long = 0L,
    val volume: Float = 0.85f,
    val isMuted: Boolean = false,
    val isSoloed: Boolean = false,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val playlist: List<PlaylistItem> = emptyList(),
    val currentTrackIndex: Int = 0,
    val isLoopEnabled: Boolean = false,
    val isPlaylistLoopEnabled: Boolean = false,
    val isShuffleEnabled: Boolean = false,
    val colorHex: String? = null
)

data class EqBandConfig(
    val name: String,
    val frequencyLabel: String,
    val gainDb: Float // -12f to +12f
)

data class EqPreset(
    val name: String,
    val bands: List<Float> // 5 band gains in dB: Bass, Low-Mid, Mid, High-Mid, Treble
)

data class RecordedTrack(
    val id: String,
    val title: String,
    val filePath: String,
    val durationMs: Long,
    val timestamp: Long
)
