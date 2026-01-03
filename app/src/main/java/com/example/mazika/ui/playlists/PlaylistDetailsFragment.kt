package com.example.mazika.ui.playlists

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.repository.PlaylistRepository
import com.example.mazika.ui.songs.SongAdapter
import kotlinx.coroutines.launch

class PlaylistDetailsFragment():Fragment(R.layout.playlist_details_fragment) {
    private lateinit var playlistRepository: PlaylistRepository


    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistRepository = PlaylistRepository
        val playlistId = arguments?.getInt("playlistId")
        if (playlistId==null)
            return
        //initialise RecyclerView
        recyclerView = view.findViewById(R.id.added_song_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewLifecycleOwner.lifecycleScope.launch{
            val songs = playlistRepository.getSongsForPlaylist(playlistId);
                val adapter = SongAdapter(songs) { song -> }
                recyclerView.adapter = adapter
        }

    }


    override fun onDestroy() {
        findNavController().popBackStack()
        super.onDestroy()
    }




}