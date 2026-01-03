package com.example.mazika.model

import androidx.room.Entity

@Entity(
    primaryKeys = ["parentPlaylistId", "childPlaylistId"]
)
data class PlaylistPlaylist(
    val parentPlaylistId: Int,
    val childPlaylistId: Int
) {
}