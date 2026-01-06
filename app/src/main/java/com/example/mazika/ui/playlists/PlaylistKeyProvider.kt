package com.example.mazika.ui.playlists

import androidx.recyclerview.selection.ItemKeyProvider

class PlaylistKeyProvider(
    private val adapter: PlaylistAdapter
) : ItemKeyProvider<Long>(SCOPE_MAPPED) {

    override fun getKey(position: Int): Long? {
        val item = adapter.currentList.getOrNull(position) ?: return null
        return item.id.toLong()
    }

    override fun getPosition(key: Long): Int {
        return adapter.currentList.indexOfFirst { it.id.toLong() == key }
    }
}
