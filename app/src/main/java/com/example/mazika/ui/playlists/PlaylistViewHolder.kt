package com.example.mazika.ui.playlists

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R

class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val tvName: TextView = itemView.findViewById(R.id.tvPlaylistName)
    val tvCount: TextView = itemView.findViewById(R.id.tvPlaylistCount)
    val btnMore: ImageButton = itemView.findViewById(R.id.btnPlaylistMore)

    private var playlistId: Long = RecyclerView.NO_ID

    fun bindKey(id: Int) {
        playlistId = id.toLong()
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int = bindingAdapterPosition
            override fun getSelectionKey(): Long = playlistId
        }
}
