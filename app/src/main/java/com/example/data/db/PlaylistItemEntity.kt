package com.example.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lane_playlist_items",
    foreignKeys = [
        ForeignKey(
            entity = LaneEntity::class,
            parentColumns = ["id"],
            childColumns = ["laneId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["laneId"])]
)
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val laneId: Long,
    val uriString: String,
    val title: String,
    val durationMs: Long = 0L,
    val sortOrder: Int = 0
)
