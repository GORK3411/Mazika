package com.example.mazika.ui.playlists

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.ItemKeyProvider.SCOPE_MAPPED
import com.example.mazika.ui.songs.SongAdapter

class PlaylistKeyProvider(private val adapter: PlaylistAdapter):ItemKeyProvider<Long>(SCOPE_MAPPED) {
    override fun getKey(position: Int): Long? = adapter.getItemId(position)

    override fun getPosition(key: Long): Int =
        adapter.currentList.indexOfFirst { it.id.toLong() == key }
}