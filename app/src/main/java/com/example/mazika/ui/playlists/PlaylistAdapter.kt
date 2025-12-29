package com.example.mazika.ui.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.Playlist
/*
class PlaylistAdapter(var playlists: List<Playlist>): RecyclerView.Adapter<PlaylistViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.playlist_view,parent,false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PlaylistViewHolder,
        position: Int
    ) {
        val playlist = playlists.get(position)
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        holder.textView.text = playlist.name
    }

    override fun getItemCount(): Int {
       return playlists.count();
    }


}
 */

class PlaylistAdapter(
    private val playlists: List<Playlist>,
    @LayoutRes private val itemLayout: Int,
    private val bind: (PlaylistViewHolder, Playlist) -> Unit,
    private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        bind(holder, playlist)
        holder.itemView.setOnClickListener { onClick(playlist) }
    }

    override fun getItemCount(): Int = playlists.size
}
