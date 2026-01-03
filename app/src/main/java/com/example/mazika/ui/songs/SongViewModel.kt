package com.example.mazika.ui.songs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mazika.model.Song
import com.example.mazika.repository.PlayBackRepository
import com.example.mazika.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongViewModel : ViewModel() {
    private val playbackRepository = PlayBackRepository
    private val songRepository = SongRepository

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    fun fetchSongs()
    {
        viewModelScope.launch(Dispatchers.IO) {
            val songs = songRepository.loadSongs()
            _songs.postValue(songs)
        }
    }

    //Currently playing song
    val currentSong = playbackRepository.currentSong.asLiveData()
    val isPlaying = playbackRepository.isPlaying.asLiveData()

    //To track the song's currentPosition while playing
    val position = playbackRepository.position.asLiveData()
    val duration = playbackRepository.duration.asLiveData()

    // ▶ Playback commands
    fun playSongs(songIds: List<Long>) {
        playbackRepository.play(songIds)
    }

    fun togglePlayback() {
        playbackRepository.toggle()
    }

    fun next()
    {
        playbackRepository.next()
    }
    fun previous()
    {
        playbackRepository.previous()
    }
    fun seekTo(positionMs: Int)
    {
        playbackRepository.seekTo(positionMs.toLong())
    }
}
