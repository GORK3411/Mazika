package com.example.mazika.ui.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.MainActivity
import com.example.mazika.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlaylistPickerBottomSheet(
    private val onPlaylistSelected: (playlistId: Long) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.bottomsheet_playlist_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistViewModel = (activity as MainActivity).playlistViewModel
        //initialise RecyclerView
        recyclerView = view.findViewById(R.id.playlistRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        playlistViewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            val adapter = PlaylistAdapter(playlists)
            recyclerView.adapter = adapter
        }

    }
}
