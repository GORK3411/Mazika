package com.example.mazika.ui.playlists

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.MainActivity
import com.example.mazika.R


class PlaylistFragment:Fragment(R.layout.fragment_playlist) {
    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistViewModel = (activity as MainActivity).playlistViewModel
        //initialise RecyclerView
        recyclerView = view.findViewById(R.id.playlist_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        playlistViewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            /*
            val adapter = PlaylistAdapter(playlists)

             */
            val adapter = PlaylistAdapter(
                playlists,
                R.layout.playlist_view,
                bind = { holder, playlist -> holder.textView.text = playlist.name },
                onClick = { playlist -> /* normal click */
                    val bundle = Bundle().apply {
                        putInt("playlistId", playlist.id)
                        putString("playlistName", playlist.name)
                    }

                    try {
                       //findNavController().navigate(R.id.action_to_details, bundle)
                        val intent = Intent(requireContext(), PlaylistDetailsActivity::class.java)
                        intent.putExtra("playlistId", playlist.id)
                        startActivity(intent)

                    }
                    catch (e: Exception)
                    {
                        print(e)
                    }

                }
            )

            recyclerView.adapter = adapter;
        }
    }
}