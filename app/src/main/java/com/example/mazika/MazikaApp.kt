package com.example.mazika

import android.app.Application
import androidx.room.Room
import com.example.mazika.repository.PlayBackRepository
import com.example.mazika.repository.PlaylistRepository
import com.example.mazika.repository.SongRepository

class MazikaApp: Application() {
    override fun onCreate() {
        super.onCreate()
        PlayBackRepository.init(this)
        SongRepository.init(this)

        val db = Room.databaseBuilder(this, MyDatabase::class.java,
            "mazika.db")
            .fallbackToDestructiveMigration(false).build()

        PlaylistRepository.init(db.playlistDao,db.playlistSongDao,db.playlistPlaylistDao)
    }
}