package com.example.mazika.ui.playlists

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.Playlist
import com.example.mazika.model.Song
import com.example.mazika.repository.PlayBackRepository
import com.example.mazika.repository.PlaylistRepository
import com.example.mazika.ui.songs.SongAdapter
import com.example.mazika.ui.songs.SongDetailsLookup
import kotlinx.coroutines.launch

class PlaylistDetailsActivity : AppCompatActivity(R.layout.playlist_details_activity) {

    private var playlistId: Int = -1;
    private val playlistRepository = PlaylistRepository
    private val allSongs: ArrayList<Song> = ArrayList<Song>();
    private lateinit var playlistTracker:SelectionTracker<Long>;
    private lateinit var songTracker:SelectionTracker<Long>;

    private lateinit var playlistRecycler: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var addedSongAdapter: SongAdapter

    private lateinit var allSongsAdapter: SongAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

         playlistId = intent.getIntExtra("playlistId", -1)
        if (playlistId == -1) {
            finish()
            return
        }
        val playlistName = intent.getStringExtra("playlistName");
        supportActionBar?.title = playlistName ?: "Playlist"


        val addedSongsRecycler = findViewById<RecyclerView>(R.id.added_song_recycler_view)
        playlistRecycler = findViewById<RecyclerView>(R.id.playlist_recycler_view)
        val allSongsRecycler = findViewById<RecyclerView>(R.id.all_song_recycler_view)

        addedSongsRecycler.layoutManager = LinearLayoutManager(this)
        playlistRecycler.layoutManager = LinearLayoutManager(this)
        allSongsRecycler.layoutManager = LinearLayoutManager(this)
        // Added songs
        addedSongAdapter = SongAdapter() { song -> }
        addedSongsRecycler.adapter = addedSongAdapter




        // Child playlists
        playlistAdapter = PlaylistAdapter(
            R.layout.playlist_view,
            bind = { holder, playlist ->
                holder.textView.text = playlist.name
            },
            onClick = { playlist ->
                if(playlistTracker.selection.size()!=0)
                    return@PlaylistAdapter
                // handle click
                try {
                    //findNavController().navigate(R.id.action_to_details, bundle)
                    val intent = Intent(this, PlaylistDetailsActivity::class.java)
                    intent.putExtra("playlistId", playlist.id)
                    intent.putExtra("playlistName", playlist.name)
                    startActivity(intent)

                }
                catch (e: Exception)
                {
                    print(e)
                }
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
                        actionMode = startSupportActionMode(playlistActionModeCallback)
                    }
                    actionMode?.title = "$count selected"
                } else {
                    actionMode?.finish()
                }
            }
        })

        allSongsAdapter = SongAdapter() { song -> }
        allSongsRecycler.adapter =allSongsAdapter
        //Loading Data
        loadData()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Add Song Tracker


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
                        actionMode = startSupportActionMode(songActionModeCallback)
                    }
                    actionMode?.title = "$count selected"
                } else {
                    actionMode?.finish()
                }
            }
        })

    }

    fun loadData()
    {
        lifecycleScope.launch {
            // Load everything safely
            val songs = playlistRepository.getSongsForPlaylist(playlistId)
            val playlists = playlistRepository.getChildPlaylists(playlistId)
            addedSongAdapter.submitList(songs)
            playlistAdapter.submitList(playlists)
            // All songs (same data or different source later)
            ///DO THIS LATER

            allSongs.clear()
            for (song in songs)
            {
                allSongs.add(song)
            }
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.playlist_details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_play -> {
                val allSongsId = allSongs.map { it.id }
                PlayBackRepository.play(allSongsId)
                finish()
                true
            }
            R.id.action_delete->
            {
                lifecycleScope.launch {
                playlistRepository.deletePlaylists(listOf(playlistId))
                }
                finish()
                true
            }
            R.id.action_rename->
            {
                SimpleDialogFragment("Rename") { playlistName ->
                    lifecycleScope.launch {
                    playlistRepository.renamePlaylist(playlistId,playlistName)
                        supportActionBar?.title = playlistName
                    }
                }.show(supportFragmentManager, "CreatePlaylistDialog")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


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
