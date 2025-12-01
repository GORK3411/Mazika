package com.example.mazika.ui.songs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.Song
import com.example.mazika.services.MusicService

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
            recyclerView.adapter = SongAdapter(songs){song -> onSongClick(song)}
        }
    }

    private fun onSongClick(song: Song) {
        /*
        Toast.makeText(requireContext(), "Clicked: ${song.title}", Toast.LENGTH_SHORT).show()
        val player = ExoPlayer.Builder(requireContext()).build()

        // Build the media item.
        val mediaItem = MediaItem.fromUri(song.data)
        // Set the media item to be played.
        player.setMediaItem(mediaItem)
        // Prepare the player.
        player.prepare()
        // Start the playback.
        player.play()
         */

        Intent(requireContext(),MusicService::class.java).also {
            it.action = MusicService.Actions.START.toString()
            it.putExtra("song_uri",song.data)
            requireActivity().startService(it)}
        // TODO: play the song using ExoPlayer
    }
}