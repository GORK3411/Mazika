package com.example.mazika.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mazika.model.PlaylistPlaylist

@Dao
interface PlaylistPlaylistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addChildToPlaylist(entity : PlaylistPlaylist)

    @Query("""
        SELECT childPlaylistId
        FROM PlaylistPlaylist
        WHERE parentPlaylistId = :playlistId
    """)
    suspend fun getChildIdsForPlaylist(playlistId: Int): List<Int>

    @Query("""
        SELECT parentPlaylistId
        FROM PlaylistPlaylist
        WHERE childPlaylistId = :playlistId
    """)
    suspend fun getParentIdsForPlaylist(playlistId: Int) : List<Int>

    @Query("DELETE FROM PlaylistPlaylist WHERE parentPlaylistId = :playlistId AND childPlaylistId IN (:childrenIds)")
    suspend fun removeChildrenFromPlaylist(playlistId : Int,childrenIds:List<Int>)

}