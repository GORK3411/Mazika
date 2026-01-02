package com.example.mazika.ui.playlists

import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.MainActivity
import com.example.mazika.R
import com.example.mazika.repository.PlaylistRepository
import com.example.mazika.ui.songs.SongDetailsLookup
import com.example.mazika.ui.songs.SongKeyProvider
import kotlinx.coroutines.launch


class PlaylistFragment:Fragment(R.layout.fragment_playlist) {
    private val playlistRepository : PlaylistRepository = PlaylistRepository
    private lateinit var playlistViewModel: PlaylistViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var tracker:SelectionTracker<Long>;


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
                    if(tracker.selection.size()!=0)
                    {
                        return@PlaylistAdapter
                    }
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
            recyclerView.adapter = adapter

            tracker = SelectionTracker.Builder<Long>(
                "playlistSelection",
                recyclerView,
                StableIdKeyProvider(recyclerView),
                PlaylistDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage()
            )
                .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .build()

            adapter.tracker = tracker
            tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    val count = tracker.selection.size()
                    Toast.makeText(requireActivity(), ""+count, Toast.LENGTH_SHORT).show()
                    if (count > 0) {
                        if (actionMode == null) {
                            actionMode = requireActivity().startActionMode(actionModeCallback)
                        }
                        actionMode?.title = "$count selected"
                    } else {
                        actionMode?.finish()
                    }
                }
            })

        }


    }

    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menu?.clear()
            mode?.menuInflater?.inflate(R.menu.selection_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when(item?.itemId) {
                R.id.menu_play -> {
                    mode?.finish()
                    return true
                }
                R.id.menu_add_to_playlist-> {

                    val selectedIdsInt: List<Int> = tracker.selection.map { it.toInt() }


                    val sheet = PlaylistPickerBottomSheet() { playlistId ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            try {
                                playlistRepository.addChildToPlaylist(
                                    playlistId,
                                    selectedIdsInt
                                )
                            }
                            catch (e: Exception)
                            {
                                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                            }

                        }

                    }
                    sheet.show(parentFragmentManager, "PlaylistPicker")



                    mode?.finish()
                    return true
                }

            }
            return false
        }


        override fun onDestroyActionMode(mode: ActionMode?) {
            // Clear selection when ActionMode ends
            tracker?.clearSelection()
            actionMode = null
        }
    }
}