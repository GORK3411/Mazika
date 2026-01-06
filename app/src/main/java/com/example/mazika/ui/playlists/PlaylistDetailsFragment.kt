package com.example.mazika.ui.playlists

import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.Song
import com.example.mazika.repository.PlaylistRepository
import com.example.mazika.ui.songs.SongAdapter
import com.example.mazika.ui.songs.SongDetailsLookup
import com.example.mazika.ui.songs.SongViewModel
import kotlinx.coroutines.launch

class PlaylistDetailsFragment : Fragment(R.layout.playlist_details_fragment) {

    private var playlistId: Int = -1;
    private val playlistRepository = PlaylistRepository
    private val allSongs: ArrayList<Song> = ArrayList<Song>();
    private lateinit var playlistTracker: SelectionTracker<Long>;
    private lateinit var songTracker:SelectionTracker<Long>;

    private lateinit var playlistRecycler: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter

    private lateinit var allSongsAdapter: SongAdapter
    private lateinit var addedSongAdapter: SongAdapter
    private lateinit var songViewModel: SongViewModel
    private lateinit var playlistViewModel: PlaylistViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistId = requireArguments().getInt("playlistId")
        val playlistName = requireArguments().getString("playlistName") ?: "Playlist"

        // Title
        view.findViewById<TextView>(R.id.tvHeaderPlaylistDetails).text = playlistName
        requireActivity().title = playlistName

        songViewModel = ViewModelProvider(requireActivity())[SongViewModel::class.java]
        playlistViewModel = ViewModelProvider(requireActivity())[PlaylistViewModel::class.java]



        //Recycler Views
        val addedSongsRecycler = view.findViewById<RecyclerView>(R.id.added_song_recycler_view)
        playlistRecycler = view.findViewById<RecyclerView>(R.id.playlist_recycler_view)
        val allSongsRecycler = view.findViewById<RecyclerView>(R.id.all_song_recycler_view)

        addedSongsRecycler.layoutManager = LinearLayoutManager(requireContext())
        playlistRecycler.layoutManager = LinearLayoutManager(requireContext())
        allSongsRecycler.layoutManager = LinearLayoutManager(requireContext())

        // Child playlists
        playlistAdapter = PlaylistAdapter(
            onClick = { playlist ->
                if(playlistTracker.selection.size()!=0)
                    return@PlaylistAdapter
                // handle click
                try {
                    //Make something to create a new PlaylistDetailsFragment
                }
                catch (e: Exception)
                {
                    print(e)
                }
            },
            onMoreClick = { holder, playlist ->
            }
        )
        playlistRecycler.adapter = playlistAdapter

        //Adding playlistTracker

        playlistTracker = SelectionTracker.Builder<Long>(
            "playlistSelection",
            playlistRecycler,
            StableIdKeyProvider(playlistRecycler),
            PlaylistDetailsLookup(playlistRecycler),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()

        playlistAdapter.tracker = playlistTracker
        playlistTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                val count = playlistTracker.selection.size()
                if (count > 0) {
                    if (actionMode == null) {
                        actionMode = requireActivity().startActionMode(playlistActionModeCallback)
                    }
                    actionMode?.title = "$count selected"
                } else {
                    actionMode?.finish()
                }
            }
        })

        //Added Song Adapter
        addedSongAdapter = SongAdapter { clicked ->
            val ids = addedSongAdapter.currentList.map { it.id }.toMutableList()
            ids.remove(clicked.id)
            ids.add(0, clicked.id)
            songViewModel.playSongs(ids)
        }
        addedSongsRecycler.adapter = addedSongAdapter

        //Added Song Tracker


        songTracker = SelectionTracker.Builder<Long>(
            "songSelection",
            addedSongsRecycler,
            StableIdKeyProvider(addedSongsRecycler),
            SongDetailsLookup(addedSongsRecycler),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()

        addedSongAdapter.tracker = songTracker
        songTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                val count = songTracker.selection.size()
                if (count > 0) {
                    if (actionMode == null) {
                        actionMode = requireActivity().startActionMode(songActionModeCallback)
                    }
                    actionMode?.title = "$count selected"
                } else {
                    actionMode?.finish()
                }
            }
        })

        //All Song Adapter
        allSongsAdapter = SongAdapter() { song -> }
        allSongsRecycler.adapter =allSongsAdapter

        // Load playlist songs
        viewLifecycleOwner.lifecycleScope.launch {
            val songs = PlaylistRepository.getSongsForPlaylist(playlistId)
            addedSongAdapter.submitList(songs)
        }

        // Now-playing highlight (same behavior as Songs tab)
        songViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            addedSongAdapter.setNowPlaying(song?.id, songViewModel.isPlaying.value == true)
        }

        songViewModel.isPlaying.observe(viewLifecycleOwner) { playing ->
            addedSongAdapter.setNowPlaying(songViewModel.currentSong.value?.id, playing == true)
        }
        loadData()
    }


    //Function to update everything
    fun loadData()
    {
        lifecycleScope.launch {
            // Load everything safely
            val songs = playlistRepository.getSongsForPlaylist(playlistId)
            val playlists = playlistRepository.getChildPlaylists(playlistId)
            val playlistSummaries = playlistViewModel.getPlaylistsWithSongCount(playlists.map { it.id })
            addedSongAdapter.submitList(songs)
            playlistAdapter.submitList(playlistSummaries)
            // All songs (same data or different source later)
            allSongs.clear()
            for (song in songs)
                allSongs.add(song)
            for (playlist in playlists)
            {
                val curSongs = playlistRepository.getSongsForPlaylist(playlist.id)
                curSongs.map {
                    if(!allSongs.contains(it))
                    {
                        allSongs.add(it)
                    }
                }
            }
            allSongsAdapter.submitList(allSongs)
        }
    }

    //Adding Menu

    //Everything related to actionMode
    private var actionMode: ActionMode? = null

    private val playlistActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menu?.clear()
            mode?.menuInflater?.inflate(R.menu.playlist_details_children_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val selectedIdsInt: List<Int> = playlistTracker.selection.map { it.toInt() }
            var res = false
            when(item?.itemId) {
                R.id.action_delete -> {

                    lifecycleScope.launch {
                        playlistRepository.removeChildrenFromPlaylist(playlistId,selectedIdsInt)
                        loadData()
                    }
                    mode?.finish()


                    res = true
                }
            }
            playlistTracker.clearSelection()
            return res
        }


        override fun onDestroyActionMode(mode: ActionMode?) {
            // Clear selection when ActionMode ends
            playlistTracker?.clearSelection()
            actionMode = null
        }
    }


    private val songActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menu?.clear()
            mode?.menuInflater?.inflate(R.menu.playlist_details_children_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val selectedIdsInt: List<Long> = songTracker.selection.toList()
            var res = false
            when(item?.itemId) {
                R.id.action_delete -> {

                    lifecycleScope.launch {
                        playlistRepository.removeSongsFromPlaylist(playlistId,selectedIdsInt)
                        loadData()
                    }
                    mode?.finish()
                    res = true
                }
            }
            songTracker.clearSelection()
            return res
        }
        override fun onDestroyActionMode(mode: ActionMode?) {
            // Clear selection when ActionMode ends
            playlistTracker?.clearSelection()
            actionMode = null
        }
    }

    override fun onStop() {
        super.onStop()

        if (::playlistTracker.isInitialized) {
            playlistTracker.clearSelection()
        }

        actionMode?.finish()
        actionMode = null
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::playlistTracker.isInitialized) {
            playlistTracker.clearSelection()
        }

        playlistRecycler.adapter = null
    }

}
