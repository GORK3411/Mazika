package com.example.mazika.ui.songs

import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.mazika.MyDatabase
import com.example.mazika.R
import com.example.mazika.model.Song
import com.example.mazika.repository.PlayBackRepository
import com.example.mazika.repository.PlaylistRepository
import com.example.mazika.repository.SongRepository
import com.example.mazika.services.MusicService
import com.example.mazika.ui.playlists.PlaylistPickerBottomSheet
import com.example.mazika.ui.playlists.PlaylistViewModel
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class SongViewFragment : Fragment(R.layout.fragment_item_list) {

    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var songViewModel: SongViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var tracker:SelectionTracker<Long>;
    private val playBackRepository = PlayBackRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        playlistRepository = PlaylistRepository

        //initialise RecyclerView
        recyclerView = view.findViewById(R.id.song_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        songViewModel.songs.observe(viewLifecycleOwner) { songs ->
            val adapter = SongAdapter(songs) { song -> onSongClick(song) }
            recyclerView.adapter = adapter

            //Add selectionTracker to select multiple songs
            tracker = SelectionTracker.Builder(
                "songSelection",
                recyclerView,
                SongKeyProvider(adapter),
                SongDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage()
            ).withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
            ).build()

            adapter.tracker = tracker // VERY IMPORTANT!
            //Observer for ActionMode
            tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    val count = tracker.selection.size()
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

    @OptIn(UnstableApi::class)
    private fun onSongClick(song: Song) {
        if(tracker.selection.size()==0)
        {
            val selectedIds = listOf<Long>(song.id)
            playSelectedSongs(selectedIds)
            // TODO: play the song using ExoPlayer
        }
    }


    //Action Mode

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
                    runSelectedSongsFromTracker()
                    mode?.finish()
                    return true
                }
                R.id.menu_add_to_playlist-> {
                    /*
                    viewLifecycleOwner.lifecycleScope.launch {
                        addSongsToPlaylist()
                    }
                     */
                    val sheet = PlaylistPickerBottomSheet { playlistId ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            playlistRepository.addSongsToPlaylist(
                                playlistId,
                                tracker.selection.toList()
                            )
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
    private suspend fun addSongsToPlaylist()
    {
        val selectedIds = tracker.selection.toList()
        playlistRepository.addSongsToPlaylist(1,selectedIds)
        val tmp = playlistRepository.getSongsForPlaylist(1)
        for (a in tmp)
        {
            print(a.title)
        }
    }
    private fun runSelectedSongsFromTracker()
    {
        val selectedIds = tracker.selection.toList()
        playSelectedSongs(selectedIds)
    }

    @OptIn(UnstableApi::class)
    fun playSelectedSongs(selectedIds:List<Long>)
    {
        playBackRepository.play(selectedIds)
    }



}