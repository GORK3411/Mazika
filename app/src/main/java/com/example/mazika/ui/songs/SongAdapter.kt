package com.example.mazika.ui.songs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.Song

class SongAdapter(private val dataSet: List<Song>,val onSongClick: (Song)-> Unit) : RecyclerView.Adapter<SongViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SongViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_view,parent,false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int
    ) {
        val song = dataSet.get(position)
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        holder.textView.text = song.title
        holder.itemView.setOnClickListener {
           onSongClick(song)
        }
    }


    override fun getItemCount(): Int {
        return dataSet.count()
    }


}