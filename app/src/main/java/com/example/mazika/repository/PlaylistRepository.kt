package com.example.mazika.repository

import com.example.mazika.dao.PlaylistDao
import com.example.mazika.dao.PlaylistPlaylistDao
import com.example.mazika.dao.PlaylistSongDao
import com.example.mazika.model.Playlist
import com.example.mazika.model.PlaylistPlaylist
import com.example.mazika.model.PlaylistSong
import com.example.mazika.model.Song

object PlaylistRepository {
    lateinit var playlistDao: PlaylistDao
    lateinit var playlistSongDao: PlaylistSongDao
    lateinit var playlistPlaylistDao: PlaylistPlaylistDao
    fun init(playlistDao: PlaylistDao , playlistSongDao: PlaylistSongDao , playlistPlaylistDao: PlaylistPlaylistDao) {
        this.playlistSongDao = playlistSongDao
        this.playlistDao = playlistDao
        this.playlistPlaylistDao = playlistPlaylistDao
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
    suspend fun addChildToPlaylist(parentPlaylistId: Int, childIds: List<Int>) {
        // Step 1: find all ancestors of the parent
        val parentIds = HashSet<Int>()
        findAllParentsForPlaylist(parentPlaylistId, parentIds)

        // Step 2: filter out childIds that would create a cycle
        val usedPlaylistIds = childIds.filter { parentIds.contains(it) }

        if (usedPlaylistIds.isNotEmpty()) {
            // Step 3: get playlist names for exception message
            val names = playlistDao.getPlaylistNames(usedPlaylistIds).joinToString(", ")
            throw Exception("Cannot add these playlists as children because it would create a cycle: $names")
        }

        // Step 4: add remaining children
        for (child in childIds) {
            playlistPlaylistDao.addChildToPlaylist(PlaylistPlaylist(parentPlaylistId, child))
        }
    }


    private suspend fun findAllParentsForPlaylist(playlistId:Int, parentIdsSet: HashSet<Int>)
    {
        val tmp = playlistPlaylistDao.getParentIdsForPlaylist(playlistId)
        for (parentId in tmp)
        {
            if (!parentIdsSet.contains(parentId))
            {
                parentIdsSet.add(parentId)
                findAllParentsForPlaylist(parentId,parentIdsSet)
            }
        }
    }

     suspend fun getChildPlaylists(playlistId:Int) : List<Playlist>
    {
        val childIds = playlistPlaylistDao.getChildIdsForPlaylist(playlistId)
        val childPlaylists  = playlistDao.getPlaylistsById(childIds)
        return childPlaylists
    }

}