package com.example.mazika.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mazika.model.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
@Query("SELECT * FROM Playlist")
fun getAll() : Flow<List<Playlist>>

@Insert
suspend fun insert(playlist: Playlist)

    @Query("SELECT name FROM Playlist WHERE id IN (:playlistIds)")
    suspend fun getPlaylistNames(playlistIds: List<Int>): List<String>

    @Query("SELECT * FROM Playlist  WHERE id IN (:playlistIds) ")
    suspend fun getPlaylistsById(playlistIds: List<Int>) : List<Playlist>
}
