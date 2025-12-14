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
}