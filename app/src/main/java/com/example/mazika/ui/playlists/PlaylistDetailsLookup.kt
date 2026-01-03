package com.example.mazika.ui.playlists

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class PlaylistDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y) ?: return null
        val holder = recyclerView.getChildViewHolder(view) as PlaylistViewHolder
        return object : ItemDetails<Long>() {
            override fun getPosition() = holder.adapterPosition
            override fun getSelectionKey() = holder.itemId
        }
    }
}


