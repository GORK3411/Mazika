package com.example.mazika.ui.songs

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R

class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView;

    init {
        textView = itemView.findViewById(R.id.songName)
    }
}