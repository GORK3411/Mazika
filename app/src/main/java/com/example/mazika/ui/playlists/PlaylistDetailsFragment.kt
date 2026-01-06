package com.example.mazika.ui.playlists

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.repository.PlaylistRepository
import com.example.mazika.ui.songs.SongAdapter
import com.example.mazika.ui.songs.SongViewModel
import kotlinx.coroutines.launch

class PlaylistDetailsFragment : Fragment(R.layout.playlist_details_fragment) {

    private lateinit var songViewModel: SongViewModel
    private lateinit var adapter: SongAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistId = requireArguments().getInt("playlistId")
        val playlistName = requireArguments().getString("playlistName") ?: "Playlist"

        // Title
        view.findViewById<TextView>(R.id.tvHeaderPlaylistDetails).text = playlistName
        requireActivity().title = playlistName

        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]

        val rv = view.findViewById<RecyclerView>(R.id.rvPlaylistSongs)
        rv.layoutManager = LinearLayoutManager(requireContext())

        // ✅ FIX: adapter is initialized first, then used inside lambda safely
        adapter = SongAdapter { clicked ->
            val ids = adapter.currentList.map { it.id }.toMutableList()
            ids.remove(clicked.id)
            ids.add(0, clicked.id)
            songViewModel.playSongs(ids)
        }
        rv.adapter = adapter

        // Load playlist songs
        viewLifecycleOwner.lifecycleScope.launch {
            val songs = PlaylistRepository.getSongsForPlaylist(playlistId)
            adapter.submitList(songs)
        }

        // Now-playing highlight (same behavior as Songs tab)
        songViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            adapter.setNowPlaying(song?.id, songViewModel.isPlaying.value == true)
        }

        songViewModel.isPlaying.observe(viewLifecycleOwner) { playing ->
            adapter.setNowPlaying(songViewModel.currentSong.value?.id, playing == true)
        }
    }
}
