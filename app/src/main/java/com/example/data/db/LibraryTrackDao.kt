package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryTrackDao {
    @Query("SELECT * FROM library_tracks ORDER BY id DESC")
    fun getAllTracks(): Flow<List<LibraryTrackEntity>>

    @Query("SELECT * FROM library_tracks ORDER BY id DESC")
    suspend fun getAllTracksSync(): List<LibraryTrackEntity>

    @Query("SELECT * FROM library_tracks WHERE uriString = :uriString LIMIT 1")
    suspend fun findByUri(uriString: String): LibraryTrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: LibraryTrackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<LibraryTrackEntity>): List<Long>

    @Query("DELETE FROM library_tracks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM library_tracks WHERE uriString = :uriString")
    suspend fun deleteByUri(uriString: String)

    @Query("SELECT COUNT(*) FROM library_tracks")
    suspend fun getTrackCount(): Int
}
