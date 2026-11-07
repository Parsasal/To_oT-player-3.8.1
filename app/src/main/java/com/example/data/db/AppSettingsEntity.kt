package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val masterVolume: Float = 0.9f,
    val isMasterMuted: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val selectedLaneId: Long = 1L,
    val eqEnabled: Boolean = false,
    val eqPreset: String = "Flat",
    val eqBand0: Float = 0f, // Bass
    val eqBand1: Float = 0f, // Low-Mid
    val eqBand2: Float = 0f, // Mid
    val eqBand3: Float = 0f, // High-Mid
    val eqBand4: Float = 0f, // Treble
    val isPersianRtl: Boolean = false,
    val languageCode: String = "en",
    val themeStyle: String = "EARTHY_BIOPHILIC",
    val isDarkMode: Boolean = true
)
