package com.example.mazika.ui.songs

import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
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
    private lateinit var tracker:SelectionTracker<Long>;

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]

        recyclerView = view.findViewById(R.id.song_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())




            var adapter: SongAdapter? = null;

        songViewModel.songs.observe(viewLifecycleOwner) { songs ->
            val adapter = SongAdapter(songs) { song -> onSongClick(song) }
            recyclerView.adapter = adapter
            //recyclerView.setHasStableIds(true)

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
            //When an item is selected/unselected print a message
            tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    val count = tracker.selection.size()
                    Toast.makeText(requireContext(), "Selected: $count", Toast.LENGTH_SHORT).show()
                }
            })

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


    //Action Mode

    private var actionMode: ActionMode? = null

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.selection_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when(item?.itemId) {
                R.id.menu_play -> {
                    playSelectedSongs()
                    mode?.finish()
                    return true
                }
                /*
                R.id.menu_delete -> {
                    deleteSelectedSongs()
                    mode?.finish()
                    return true
                }
                R.id.menu_share -> {
                    shareSelectedSongs()
                    mode?.finish()
                    return true
                }

                 */
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            // Clear selection when ActionMode ends
            tracker?.clearSelection()
            actionMode = null
        }
    }

    @OptIn(UnstableApi::class)
    fun playSelectedSongs()
    {
        val selectedIds = tracker.selection.toList()
        Intent(requireContext(),MusicService::class.java).also {
            it.action = MusicService.Actions.START.toString()
            //it.putExtra("song_uri",song.data)
            it.putExtra("songs_ID",selectedIds.toLongArray())
            requireActivity().startService(it)}

        //for later
        /*
        val firstSong = songs.first { it.id == selectedIds[0] }
        (requireActivity() as MainActivity).updateMiniPlayer(firstSong.title, true)
         */

    }
}