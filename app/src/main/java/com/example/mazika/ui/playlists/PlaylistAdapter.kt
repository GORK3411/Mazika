package com.example.mazika.ui.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.Playlist
class PlaylistAdapter(
    val playlists: List<Playlist>,
    @LayoutRes private val itemLayout: Int,
    private val bind: (PlaylistViewHolder, Playlist) -> Unit,
    private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistViewHolder>() {

    var tracker: SelectionTracker<Long>? = null
    init { setHasStableIds(true) }

    override fun getItemId(position: Int): Long {
        return playlists[position].id.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        return PlaylistViewHolder(view)


    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        bind(holder, playlist)
        holder.itemView.setOnClickListener { onClick(playlist) }

        //For tracker
        val isSelected = tracker?.isSelected(playlist.id.toLong()) ?: false
        holder.itemView.isActivated = isSelected

    }

    override fun getItemCount(): Int = playlists.size
}
