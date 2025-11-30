package com.example.mazika.ui.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R

/**
 * A fragment representing a list of Items.
 */
class SongViewFragment : Fragment(R.layout.fragment_item_list) {

    private lateinit var songViewModel: SongViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]

        recyclerView = view.findViewById(R.id.song_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        songViewModel.songs.observe(viewLifecycleOwner) { songs ->
            recyclerView.adapter = SongAdapter(songs)
        }
    }
}