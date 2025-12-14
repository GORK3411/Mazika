package com.example.mazika.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mazika.model.Playlist
import com.example.mazika.repository.PlaylistRepository
import kotlinx.coroutines.launch

class PlaylistViewModel (private val playlistRepository: PlaylistRepository): ViewModel(){
    val playlist = playlistRepository.getPlaylists()
    fun addPlaylist(name:String) = viewModelScope.launch {
        playlistRepository.addPlaylist(Playlist(name = name))
    }
}