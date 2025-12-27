package com.example.mazika.ui.playlists

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R

class PlaylistViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val textView: TextView;

    init {
        textView = itemView.findViewById(R.id.playlistName)
    }
}