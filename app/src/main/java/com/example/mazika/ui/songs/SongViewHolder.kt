package com.example.mazika.ui.songs

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R

class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
    val tvArtist: TextView = itemView.findViewById(R.id.tvArtist)
    val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)


}
