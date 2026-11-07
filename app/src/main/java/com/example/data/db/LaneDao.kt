package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LaneDao {
    @Query("SELECT * FROM audio_lanes ORDER BY sortOrder ASC, id ASC")
    fun getAllLanes(): Flow<List<LaneEntity>>

    @Query("SELECT * FROM audio_lanes ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllLanesSync(): List<LaneEntity>

    @Query("SELECT * FROM audio_lanes WHERE id = :id LIMIT 1")
    suspend fun getLaneById(id: Long): LaneEntity?

    @Query("SELECT COUNT(*) FROM audio_lanes")
    suspend fun getLaneCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLane(lane: LaneEntity): Long

    @androidx.room.Transaction
    suspend fun insertLaneAtTop(name: String): Long {
        val existing = getAllLanesSync()
        existing.forEachIndexed { index, lane ->
            updateSortOrder(lane.id, index + 1)
        }
        return insertLane(LaneEntity(name = name, sortOrder = 0))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lanes: List<LaneEntity>)

    @Update
    suspend fun updateLane(lane: LaneEntity)

    @Query("UPDATE audio_lanes SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: Long, order: Int)

    @androidx.room.Transaction
    suspend fun reorderLanes(laneIdsInOrder: List<Long>) {
        laneIdsInOrder.forEachIndexed { index, id ->
            updateSortOrder(id, index)
        }
    }

    @Query("UPDATE audio_lanes SET playbackPositionMs = :positionMs WHERE id = :id")
    suspend fun updatePlaybackPosition(id: Long, positionMs: Long)

    @Query("UPDATE audio_lanes SET isLoopEnabled = :isLoop WHERE id = :id")
    suspend fun updateLoop(id: Long, isLoop: Boolean)

    @Query("UPDATE audio_lanes SET isPlaylistLoopEnabled = :isLoop WHERE id = :id")
    suspend fun updatePlaylistLoop(id: Long, isLoop: Boolean)

    @Query("UPDATE audio_lanes SET isShuffleEnabled = :isShuffle WHERE id = :id")
    suspend fun updateShuffle(id: Long, isShuffle: Boolean)

    @Query("UPDATE audio_lanes SET colorHex = :colorHex WHERE id = :id")
    suspend fun updateColor(id: Long, colorHex: String?)

    @Query("DELETE FROM audio_lanes WHERE id = :id")
    suspend fun deleteLaneById(id: Long)

    @Query("DELETE FROM audio_lanes")
    suspend fun deleteAllLanes()
}
