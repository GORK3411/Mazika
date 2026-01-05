package com.example.mazika.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mazika.model.PlaylistSong
import com.example.mazika.model.Song

@Dao
interface PlaylistSongDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(entity: PlaylistSong)

    @Query("""
        SELECT songId
        FROM PlaylistSong
        WHERE playlistId = :playlistId
    """)
    suspend fun getSongIdsForPlaylist(playlistId: Int): List<Long>

    @Query(value = "DELETE FROM PlaylistSong WHERE playlistId = :playlistId AND songId IN (:songsId)")
    suspend fun removeSongsFromPlaylist(playlistId: Int, songsId: List<Long>)
}

