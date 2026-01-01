package com.example.mazika.ui.playlists

import android.view.View
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.Playlist

class PlaylistViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val textView: TextView;

    init {
        textView = itemView.findViewById(R.id.playlistName)
    }


    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = bindingAdapterPosition
            override fun getSelectionKey(): Long =
                (bindingAdapter as PlaylistAdapter).playlists[bindingAdapterPosition].id.toLong()
        }
}