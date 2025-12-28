package com.example.mazika

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mazika.dao.PlaylistDao
import com.example.mazika.dao.PlaylistSongDao
import com.example.mazika.model.Playlist
import com.example.mazika.model.PlaylistSong

@Database(
    entities = [Playlist::class, PlaylistSong::class],
    version = 2
)
abstract class MyDatabase :RoomDatabase() {
    abstract val playlistDao : PlaylistDao
    abstract val playlistSongDao : PlaylistSongDao
}