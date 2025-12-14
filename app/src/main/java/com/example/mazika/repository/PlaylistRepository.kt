package com.example.mazika.repository

import com.example.mazika.dao.PlaylistDao
import com.example.mazika.model.Playlist

class PlaylistRepository(private val playlistDao: PlaylistDao) {
    fun getPlaylists() = playlistDao.getAll()
    suspend fun addPlaylist(playlist: Playlist) = playlistDao.insert(playlist)
}