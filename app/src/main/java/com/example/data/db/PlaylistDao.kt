package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM lane_playlist_items WHERE laneId = :laneId ORDER BY sortOrder ASC, id ASC")
    fun getItemsForLane(laneId: Long): Flow<List<PlaylistItemEntity>>

    @Query("SELECT * FROM lane_playlist_items WHERE laneId = :laneId ORDER BY sortOrder ASC, id ASC")
    suspend fun getItemsForLaneSync(laneId: Long): List<PlaylistItemEntity>

    @Query("SELECT * FROM lane_playlist_items ORDER BY sortOrder ASC, id ASC")
    fun getAllPlaylistItems(): Flow<List<PlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PlaylistItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PlaylistItemEntity>): List<Long>

    @Update
    suspend fun updateItem(item: PlaylistItemEntity)

    @Update
    suspend fun updateAll(items: List<PlaylistItemEntity>)

    @Query("DELETE FROM lane_playlist_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM lane_playlist_items WHERE laneId = :laneId")
    suspend fun deleteAllForLane(laneId: Long)
}
