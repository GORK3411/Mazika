package com.example.mazika.ui.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.Playlist
import com.example.mazika.model.Song

class PlaylistAdapter(
    @LayoutRes private val itemLayout: Int,
    private val bind: (PlaylistViewHolder, Playlist) -> Unit,
    private val onClick: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistViewHolder>(DIFF_CALLBACK) {


    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Playlist>() {
            override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean =
                oldItem.name == newItem.name
        }
    }
    var tracker: SelectionTracker<Long>? = null
    init { setHasStableIds(true) }

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        return PlaylistViewHolder(view)


    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        bind(holder, playlist)
        holder.itemView.setOnClickListener { onClick(playlist) }

        //For tracker
        val isSelected = tracker?.isSelected(playlist.id.toLong()) ?: false
        holder.itemView.isActivated = isSelected

    }


}
