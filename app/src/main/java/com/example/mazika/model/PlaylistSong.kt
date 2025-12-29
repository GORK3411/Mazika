package com.example.mazika.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    //tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSong(
    val playlistId: Int,
    val songId: Long
)
