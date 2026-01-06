package com.example.mazika.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mazika.model.Playlist
import com.example.mazika.model.PlaylistSummary
import com.example.mazika.repository.PlaylistRepository
import kotlinx.coroutines.launch

class PlaylistViewModel (): ViewModel(){
    private val playlistRepository = PlaylistRepository

    val playlistSummaries = playlistRepository.getPlaylistSummaries().asLiveData()

    val playlists = playlistRepository.getPlaylists().asLiveData()

    fun addPlaylist(name: String) = viewModelScope.launch {
        playlistRepository.addPlaylist(Playlist(name = name))
    }

    fun renamePlaylist(playlistId: Int, newName: String) = viewModelScope.launch {
        playlistRepository.renamePlaylist(playlistId, newName)
    }

    fun deletePlaylists(list: List<Int>) {
        viewModelScope.launch { playlistRepository.deletePlaylists(list) }
    }

    suspend fun addChildrenToPlaylist(parentId: Int, childIds: List<Int>) {
        playlistRepository.addChildToPlaylist(parentId, childIds)
    }

    suspend fun getPlaylistsWithSongCount(playlistIds: List<Int>) : List<PlaylistSummary>
    {
        return playlistRepository.getPlaylistsWithSongCount(playlistIds)
    }
}