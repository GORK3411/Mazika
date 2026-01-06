package com.example.mazika.ui.songs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mazika.model.Song
import com.example.mazika.repository.PlayBackRepository
import com.example.mazika.repository.PlaylistRepository
import com.example.mazika.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongViewModel : ViewModel() {

    private val playbackRepository = PlayBackRepository
    private val songRepository = SongRepository

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    fun fetchSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            val loaded = songRepository.loadSongs()
            _songs.postValue(loaded)
        }
    }

    // Currently playing song
    val currentSong = playbackRepository.currentSong.asLiveData()
    val isPlaying = playbackRepository.isPlaying.asLiveData()

    // Track position/duration
    val position = playbackRepository.position.asLiveData()
    val duration = playbackRepository.duration.asLiveData()

    fun playSongs(songIds: List<Long>) {
        playbackRepository.play(songIds)
    }


    fun playFromSongId(clickedId: Long) {
        val list = _songs.value ?: return
        val ids = list.map { it.id }
        val startIndex = ids.indexOf(clickedId)

        if (startIndex == -1) {
            playbackRepository.play(listOf(clickedId))
            return
        }

        val reordered = ids.drop(startIndex) + ids.take(startIndex)
        playbackRepository.play(reordered)
    }

    fun togglePlayback() = playbackRepository.toggle()
    fun next() = playbackRepository.next()
    fun previous() = playbackRepository.previous()
    fun seekTo(positionMs: Int) = playbackRepository.seekTo(positionMs.toLong())

    fun addSongsToPlaylist(playlistId: Int, ids: List<Long>) = viewModelScope.launch {
        PlaylistRepository.addSongsToPlaylist(playlistId, ids)
    }
}
