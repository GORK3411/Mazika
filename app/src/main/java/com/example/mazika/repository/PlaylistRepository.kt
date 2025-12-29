package com.example.mazika.repository

import com.example.mazika.dao.PlaylistDao
import com.example.mazika.dao.PlaylistSongDao
import com.example.mazika.model.Playlist
import com.example.mazika.model.PlaylistSong
import com.example.mazika.model.Song

object PlaylistRepository {
    lateinit var playlistDao: PlaylistDao
    lateinit var playlistSongDao: PlaylistSongDao
    fun init(playlistDao: PlaylistDao , playlistSongDao: PlaylistSongDao) {
        this.playlistSongDao = playlistSongDao
        this.playlistDao = playlistDao
    }
    fun getPlaylists() = playlistDao.getAll()
    suspend fun addPlaylist(playlist: Playlist) = playlistDao.insert(playlist)

    suspend fun getSongsForPlaylist(playlistId: Int) : List<Song>
    {
        val songIds = playlistSongDao.getSongIdsForPlaylist(playlistId)
        return SongRepository.getSongsByIds(songIds)
    }

    suspend fun addSongsToPlaylist(playlistId:Int,songIds : List<Long>)
    {
        for (songId in songIds)
        {
            playlistSongDao.addSongToPlaylist(PlaylistSong(playlistId,songId))
        }
    }
}