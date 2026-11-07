package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.AppSettingsEntity
import com.example.data.db.LaneEntity
import kotlinx.coroutines.flow.Flow

class AudioRepository(private val database: AppDatabase) {
    private val laneDao = database.laneDao()
    private val settingsDao = database.settingsDao()
    private val playlistDao = database.playlistDao()
    private val libraryDao = database.libraryTrackDao()

    val allLanes: Flow<List<LaneEntity>> = laneDao.getAllLanes()
    val settings: Flow<AppSettingsEntity?> = settingsDao.getSettings()
    val allPlaylistItems: Flow<List<com.example.data.db.PlaylistItemEntity>> = playlistDao.getAllPlaylistItems()
    val allLibraryTracks: Flow<List<com.example.data.db.LibraryTrackEntity>> = libraryDao.getAllTracks()

    suspend fun initializeDefaultsIfNeeded() {
        val count = laneDao.getLaneCount()
        if (count == 0) {
            val defaultLanes = listOf(
                LaneEntity(name = "Remix", sortOrder = 0, isLoopEnabled = false, colorHex = "#EF4444"),
                LaneEntity(name = "Podcast & Voice", sortOrder = 1, isLoopEnabled = false, colorHex = "#3B82F6"),
                LaneEntity(name = "Workout", sortOrder = 2, isLoopEnabled = false, colorHex = "#10B981"),
                LaneEntity(name = "Night Chill", sortOrder = 3, isLoopEnabled = false, colorHex = "#8B5CF6")
            )
            laneDao.insertAll(defaultLanes)
            settingsDao.saveSettings(AppSettingsEntity(themeStyle = "EARTHY_BIOPHILIC"))
        } else {
            val existingLanes = laneDao.getAllLanesSync()
            // Clean up any AI lanes or Audiobooks lanes as requested by user
            for (lane in existingLanes) {
                val tName = lane.name.trim()
                if (tName.equals("خط ai", ignoreCase = true) ||
                    tName.equals("خط AI", ignoreCase = true) ||
                    tName.equals("AI Lane", ignoreCase = true)) {
                    laneDao.deleteLaneById(lane.id)
                } else if (tName.equals("Audiobooks", ignoreCase = true) ||
                    tName.equals("Audiobook", ignoreCase = true) ||
                    tName.equals("Audio books", ignoreCase = true) ||
                    tName.equals("کتاب صوتی", ignoreCase = true)) {
                    laneDao.deleteLaneById(lane.id)
                } else if (tName == "ریمیکس") {
                    laneDao.updateLane(lane.copy(name = "Remix"))
                }
            }
            // Ensure "Remix" lane is present
            val remainingLanes = laneDao.getAllLanesSync()
            val hasRemix = remainingLanes.any {
                it.name.trim().equals("Remix", ignoreCase = true) || it.name.trim().equals("ریمیکس", ignoreCase = true)
            }
            if (!hasRemix) {
                laneDao.insertLaneAtTop("Remix")
            }
            // Assign default vivid studio colors to uncolored lanes
            for (lane in laneDao.getAllLanesSync()) {
                if (lane.colorHex.isNullOrBlank()) {
                    val assignedColor = when {
                        lane.name.contains("Remix", ignoreCase = true) || lane.name.contains("ریمیکس") -> "#EF4444"
                        lane.name.contains("Podcast", ignoreCase = true) || lane.name.contains("Voice", ignoreCase = true) || lane.name.contains("پادکست") -> "#3B82F6"
                        lane.name.contains("Workout", ignoreCase = true) || lane.name.contains("ورزش") -> "#10B981"
                        lane.name.contains("Night", ignoreCase = true) || lane.name.contains("Chill", ignoreCase = true) || lane.name.contains("شب") -> "#8B5CF6"
                        else -> when (lane.sortOrder % 6) {
                            0 -> "#EF4444"
                            1 -> "#3B82F6"
                            2 -> "#10B981"
                            3 -> "#8B5CF6"
                            4 -> "#F59E0B"
                            else -> "#06B6D4"
                        }
                    }
                    laneDao.updateColor(lane.id, assignedColor)
                }
            }
            // Migrate default theme to Earthy & Biophilic
            val currentSettings = settingsDao.getSettingsSync()
            if (currentSettings != null && (currentSettings.themeStyle == "RETRO_ANALOG" || currentSettings.themeStyle.isBlank())) {
                settingsDao.saveSettings(currentSettings.copy(themeStyle = "EARTHY_BIOPHILIC"))
            }
        }
    }

    suspend fun addLibraryTrack(track: com.example.data.db.LibraryTrackEntity): Long {
        val existing = libraryDao.findByUri(track.uriString)
        return if (existing == null) {
            libraryDao.insertTrack(track)
        } else {
            existing.id
        }
    }

    suspend fun addLibraryTracks(tracks: List<com.example.data.db.LibraryTrackEntity>): List<Long> {
        return libraryDao.insertAll(tracks)
    }

    suspend fun deleteLibraryTrack(id: Long) {
        libraryDao.deleteById(id)
    }

    suspend fun getAllLibraryTracksSync(): List<com.example.data.db.LibraryTrackEntity> {
        return libraryDao.getAllTracksSync()
    }

    suspend fun addLane(name: String, sortOrder: Int): Long {
        return laneDao.insertLane(LaneEntity(name = name, sortOrder = sortOrder))
    }

    suspend fun addLaneAtTop(name: String): Long {
        return laneDao.insertLaneAtTop(name)
    }

    suspend fun getAllLanesSync(): List<LaneEntity> {
        return laneDao.getAllLanesSync()
    }

    suspend fun getLaneById(id: Long): LaneEntity? {
        return laneDao.getLaneById(id)
    }

    suspend fun updateLane(lane: LaneEntity) {
        laneDao.updateLane(lane)
    }

    suspend fun updatePlaybackPosition(id: Long, positionMs: Long) {
        laneDao.updatePlaybackPosition(id, positionMs)
    }

    suspend fun updateLaneLoop(id: Long, isLoop: Boolean) {
        laneDao.updateLoop(id, isLoop)
    }

    suspend fun updateLanePlaylistLoop(id: Long, isLoop: Boolean) {
        laneDao.updatePlaylistLoop(id, isLoop)
    }

    suspend fun updateLaneShuffle(id: Long, isShuffle: Boolean) {
        laneDao.updateShuffle(id, isShuffle)
    }

    suspend fun updateLaneColor(id: Long, colorHex: String?) {
        laneDao.updateColor(id, colorHex)
    }

    suspend fun deleteLane(id: Long) {
        playlistDao.deleteAllForLane(id)
        laneDao.deleteLaneById(id)
    }

    suspend fun saveSettings(settings: AppSettingsEntity) {
        settingsDao.saveSettings(settings)
    }

    fun getPlaylistForLane(laneId: Long): Flow<List<com.example.data.db.PlaylistItemEntity>> {
        return playlistDao.getItemsForLane(laneId)
    }

    suspend fun getPlaylistForLaneSync(laneId: Long): List<com.example.data.db.PlaylistItemEntity> {
        return playlistDao.getItemsForLaneSync(laneId)
    }

    suspend fun addPlaylistItem(laneId: Long, uriString: String, title: String, durationMs: Long, sortOrder: Int): Long {
        return playlistDao.insertItem(
            com.example.data.db.PlaylistItemEntity(
                laneId = laneId,
                uriString = uriString,
                title = title,
                durationMs = durationMs,
                sortOrder = sortOrder
            )
        )
    }

    suspend fun addPlaylistItems(items: List<com.example.data.db.PlaylistItemEntity>): List<Long> {
        return playlistDao.insertAll(items)
    }

    suspend fun deletePlaylistItem(id: Long) {
        playlistDao.deleteItemById(id)
    }

    suspend fun clearPlaylistForLane(laneId: Long) {
        playlistDao.deleteAllForLane(laneId)
    }

    suspend fun reorderPlaylistItems(items: List<com.example.data.db.PlaylistItemEntity>) {
        playlistDao.updateAll(items)
    }

    suspend fun reorderLanes(laneIdsInOrder: List<Long>) {
        laneDao.reorderLanes(laneIdsInOrder)
    }
}
