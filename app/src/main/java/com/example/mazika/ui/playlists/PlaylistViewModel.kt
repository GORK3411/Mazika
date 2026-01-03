package com.example.mazika.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mazika.model.Playlist
import com.example.mazika.repository.PlaylistRepository
import kotlinx.coroutines.launch

class PlaylistViewModel (private val playlistRepository: PlaylistRepository): ViewModel(){

    val playlists = playlistRepository.getPlaylists().asLiveData()
    fun addPlaylist(name:String) = viewModelScope.launch {
        playlistRepository.addPlaylist(Playlist(name = name))
    }
    fun addChildrenToPlaylist(playlistId : Int, selectedIdsInt: List<Int>)
    {
        viewModelScope.launch {
                playlistRepository.addChildToPlaylist(
                    playlistId,
                    selectedIdsInt
                )


        }
    }

    fun deletePlaylists(list: List<Int>)
    {
        viewModelScope.launch {
            playlistRepository.deletePlaylists(list)
        }
    }
}