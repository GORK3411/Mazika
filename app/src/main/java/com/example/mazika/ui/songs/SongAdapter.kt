package com.example.mazika.ui.songs

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.selection.ItemDetailsLookup
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

    private var nowPlayingId: Long? = null
    private var nowPlayingIsPlaying: Boolean = false

    init {
        setHasStableIds(true)
    }

    fun setNowPlaying(songId: Long?, isPlaying: Boolean) {
        nowPlayingId = songId
        nowPlayingIsPlaying = isPlaying
        notifyDataSetChanged()
    }

    object Diff : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Song, newItem: Song) = oldItem == newItem
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

        private var songId: Long = RecyclerView.NO_ID

        fun bind(song: Song, isCurrent: Boolean, isPlaying: Boolean) {
            tvTitle.text = song.title
            tvArtist.text = song.artist
            tvDuration.text = formatDuration(song.duration)

            val accent = resolveColorInt(itemView, androidx.appcompat.R.attr.colorPrimary)
            val normal = resolveColorStateList(itemView, android.R.attr.textColorPrimary).defaultColor
            val secondary = resolveColorStateList(itemView, android.R.attr.textColorSecondary).defaultColor

            playingDot.visibility = if (isCurrent) View.VISIBLE else View.GONE

            tvTitle.setTextColor(if (isCurrent && isPlaying) accent else normal)
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

            songId = song.id
        }

        private fun formatDuration(ms: Long): String {
            val totalSec = (ms / 1000).toInt().coerceAtLeast(0)
            val min = totalSec / 60
            val sec = totalSec % 60
            return "%d:%02d".format(min, sec)
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = bindingAdapterPosition
                override fun getSelectionKey(): Long = songId
            }
    }
}

private fun resolveColorStateList(view: View, attr: Int): ColorStateList {
    val tv = TypedValue()
    view.context.theme.resolveAttribute(attr, tv, true)
    return if (tv.resourceId != 0) {
        AppCompatResources.getColorStateList(view.context, tv.resourceId)
            ?: ColorStateList.valueOf(tv.data)
    } else {
        ColorStateList.valueOf(tv.data)
    }
}

private fun resolveColorInt(view: View, attr: Int): Int {
    val tv = TypedValue()
    view.context.theme.resolveAttribute(attr, tv, true)
    return if (tv.resourceId != 0) {
        AppCompatResources.getColorStateList(view.context, tv.resourceId)?.defaultColor ?: tv.data
    } else {
        tv.data
    }
}
