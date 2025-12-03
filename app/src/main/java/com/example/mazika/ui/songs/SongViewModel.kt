package com.example.mazika.ui.songs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.asLiveData
import com.example.mazika.model.Song
import com.example.mazika.repository.SongRepository

class SongViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SongRepository


    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    fun fetchSongs()
    {
        val songs = repository.loadSongs(application)
        _songs.postValue(songs)
    }

    //Currently playing song
    val currentSong = repository.currentSong.asLiveData()
    val isPlaying = repository.isPlaying.asLiveData()
}