package com.example.mazika.model

import androidx.room.Entity


@Entity(
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSong(
    val playlistId: Int,
    val songId: Long
)
