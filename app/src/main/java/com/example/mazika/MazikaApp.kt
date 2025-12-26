package com.example.mazika

import android.app.Application
import com.example.mazika.repository.PlayBackRepository
import com.example.mazika.repository.SongRepository

class MazikaApp: Application() {
    override fun onCreate() {
        super.onCreate()
        PlayBackRepository.init(this)
        SongRepository.init(this)
    }
}