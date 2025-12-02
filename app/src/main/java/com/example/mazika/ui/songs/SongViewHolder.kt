package com.example.mazika.ui.songs

import android.view.View
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.Song

class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView: TextView;

    init {
        textView = itemView.findViewById(R.id.songName)
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition() = bindingAdapterPosition
            override fun getSelectionKey() = (bindingAdapter as SongAdapter).songs[bindingAdapterPosition].id
        }

}