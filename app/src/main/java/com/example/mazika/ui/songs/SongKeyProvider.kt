package com.example.mazika.ui.songs

import androidx.recyclerview.selection.ItemKeyProvider

class SongKeyProvider(private val adapter: SongAdapter) : ItemKeyProvider<Long>(SCOPE_MAPPED) {

    override fun getKey(position: Int): Long? = adapter.songs[position].id

    override fun getPosition(key: Long): Int =
        adapter.songs.indexOfFirst { it.id == key }
}
