package com.example.mazika.ui.playlists

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.PlaylistSummary
import com.google.android.material.card.MaterialCardView

class PlaylistAdapter(
    private val onClick: (PlaylistSummary) -> Unit,
    private val onMoreClick: (anchor: View, playlist: PlaylistSummary) -> Unit
) : ListAdapter<PlaylistSummary, PlaylistAdapter.VH>(Diff) {

    var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.playlist_view, parent, false)
        return VH(v, onClick, onMoreClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val selected = tracker?.isSelected(item.id.toLong()) == true
        holder.bind(item, selected)
    }

    class VH(
        itemView: View,
        private val onClick: (PlaylistSummary) -> Unit,
        private val onMoreClick: (View, PlaylistSummary) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val card: MaterialCardView = itemView as MaterialCardView

        private val tvName: TextView = itemView.findViewById(R.id.tvPlaylistName)
        private val tvCount: TextView = itemView.findViewById(R.id.tvPlaylistCount)
        private val btnMore: ImageButton = itemView.findViewById(R.id.btnPlaylistMore)

        private var keyId: Long = RecyclerView.NO_ID

        fun bind(item: PlaylistSummary, isSelected: Boolean) {
            keyId = item.id.toLong()

            tvName.text = item.name
            tvCount.text = "${item.songCount} songs"

            val primary = resolveColorInt(card, androidx.appcompat.R.attr.colorPrimary)
            val onSurface = resolveColorInt(card, com.google.android.material.R.attr.colorOnSurface)
            val defaultStroke = ColorUtils.setAlphaComponent(onSurface, (0.12f * 255).toInt())

            card.strokeWidth = dp(card, if (isSelected) 2 else 1)
            card.strokeColor = if (isSelected) primary else defaultStroke

            itemView.setOnClickListener { onClick(item) }
            btnMore.setOnClickListener { onMoreClick(it, item) }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = bindingAdapterPosition
                override fun getSelectionKey(): Long = keyId
            }

        private fun dp(view: View, value: Int): Int =
            (value * view.resources.displayMetrics.density).toInt()
    }

    object Diff : DiffUtil.ItemCallback<PlaylistSummary>() {
        override fun areItemsTheSame(oldItem: PlaylistSummary, newItem: PlaylistSummary) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PlaylistSummary, newItem: PlaylistSummary) =
            oldItem == newItem
    }
}

/** Resolve theme attribute into a single color int. */
private fun resolveColorInt(view: View, attr: Int): Int {
    val tv = TypedValue()
    view.context.theme.resolveAttribute(attr, tv, true)
    return if (tv.resourceId != 0) {
        AppCompatResources.getColorStateList(view.context, tv.resourceId)?.defaultColor ?: tv.data
    } else {
        tv.data
    }
}
