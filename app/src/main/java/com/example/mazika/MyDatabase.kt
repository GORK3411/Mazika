package com.example.mazika

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mazika.dao.PlaylistDao
import com.example.mazika.model.Playlist

@Database(
    entities = [Playlist::class],
    version = 1
)
abstract class MyDatabase :RoomDatabase() {
    abstract val playlistDao : PlaylistDao
}