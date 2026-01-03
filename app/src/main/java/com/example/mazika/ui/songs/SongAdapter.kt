package com.example.mazika.ui.songs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.Song

class SongAdapter(
                  private val onSongClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongVH>(Diff) {

    var tracker: SelectionTracker<Long>? = null
    init { setHasStableIds(true) }
    private var nowPlayingId: Long? = null
    private var nowPlayingIsPlaying: Boolean = false

    fun setNowPlaying(songId: Long?, isPlaying: Boolean) {
        nowPlayingId = songId
        nowPlayingIsPlaying = isPlaying
        notifyDataSetChanged() // simple + reliable for a student project
    }

    object Diff : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Song, newItem: Song) = oldItem == newItem
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongVH(view, onSongClick)
    }

    override fun onBindViewHolder(holder: SongVH, position: Int) {
        val song = getItem(position)
        val isCurrent = (song.id == nowPlayingId)
        holder.bind(song, isCurrent, nowPlayingIsPlaying)
    }

    class SongVH(
        itemView: View,
        private val onSongClick: (Song) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvArtist: TextView = itemView.findViewById(R.id.tvArtist)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
        private val playingDot: View = itemView.findViewById(R.id.viewPlayingDot)

        fun bind(song: Song, isCurrent: Boolean, isPlaying: Boolean) {
            tvTitle.text = song.title
            tvArtist.text = song.artist
            tvDuration.text = formatDuration(song.duration)

            // Now playing highlight
            playingDot.visibility = if (isCurrent) View.VISIBLE else View.GONE
            val accent = ContextCompat.getColor(itemView.context, R.color.accent)
            val normal = ContextCompat.getColor(itemView.context, R.color.text_primary)
            val secondary = ContextCompat.getColor(itemView.context, R.color.text_secondary)

            if (isCurrent && isPlaying) {
                tvTitle.setTextColor(accent)
            } else {
                tvTitle.setTextColor(normal)
            }

            tvArtist.setTextColor(secondary)
            tvDuration.setTextColor(secondary)

            itemView.setOnClickListener { onSongClick(song) }

            btnMore.setOnClickListener { v ->
                val menu = PopupMenu(v.context, v)
                menu.menu.add("Add to playlist")
                menu.menu.add("Share")
                menu.menu.add("Details")
                menu.setOnMenuItemClickListener { item ->
                    Toast.makeText(v.context, "${item.title}: ${song.title}", Toast.LENGTH_SHORT).show()
                    true
                }
                menu.show()
            }
        }

        private fun formatDuration(ms: Long): String {
            val totalSec = (ms / 1000).toInt().coerceAtLeast(0)
            val min = totalSec / 60
            val sec = totalSec % 60
            return "%d:%02d".format(min, sec)
        }
    }
}
