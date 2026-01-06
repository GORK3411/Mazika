package com.example.mazika.ui.playlists

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.PlaylistSummary
import com.example.mazika.model.Song
import com.example.mazika.repository.PlayBackRepository
import com.example.mazika.repository.PlaylistRepository
import com.example.mazika.ui.songs.SongAdapter
import kotlinx.coroutines.launch

class PlaylistDetailsActivity : AppCompatActivity(R.layout.playlist_details_activity) {

    private var playlistId: Int = -1
    private val playlistRepository = PlaylistRepository

    private val allSongs: ArrayList<Song> = ArrayList()

    // ✅ IMPORTANT: declare adapters first, then init them
    private lateinit var addedSongAdapter: SongAdapter
    private lateinit var allSongsAdapter: SongAdapter
    private lateinit var childPlaylistAdapter: PlaylistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playlistId = intent.getIntExtra("playlistId", -1)
        if (playlistId == -1) {
            finish()
            return
        }

        val playlistName = intent.getStringExtra("playlistName")
        supportActionBar?.title = playlistName ?: "Playlist"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val addedSongsRecycler = findViewById<RecyclerView>(R.id.added_song_recycler_view)
        val childPlaylistsRecycler = findViewById<RecyclerView>(R.id.playlist_recycler_view)
        val allSongsRecycler = findViewById<RecyclerView>(R.id.all_song_recycler_view)

        addedSongsRecycler.layoutManager = LinearLayoutManager(this)
        childPlaylistsRecycler.layoutManager = LinearLayoutManager(this)
        allSongsRecycler.layoutManager = LinearLayoutManager(this)

        // ✅ Now it's SAFE to reference addedSongAdapter inside the lambda
        addedSongAdapter = SongAdapter { clicked ->
            val ids = addedSongAdapter.currentList.map { it.id }.toMutableList()
            ids.remove(clicked.id)
            ids.add(0, clicked.id)
            PlayBackRepository.play(ids)
        }
        addedSongsRecycler.adapter = addedSongAdapter

        allSongsAdapter = SongAdapter { clicked ->
            val ids = allSongsAdapter.currentList.map { it.id }.toMutableList()
            ids.remove(clicked.id)
            ids.add(0, clicked.id)
            PlayBackRepository.play(ids)
        }
        allSongsRecycler.adapter = allSongsAdapter

        childPlaylistAdapter = PlaylistAdapter(
            onClick = { pl ->
                val intent = Intent(this, PlaylistDetailsActivity::class.java).apply {
                    putExtra("playlistId", pl.id)
                    putExtra("playlistName", pl.name)
                }
                startActivity(intent)
            },
            onMoreClick = { anchor: View, pl: PlaylistSummary ->
                val menu = PopupMenu(anchor.context, anchor)
                menu.menuInflater.inflate(R.menu.menu_playlist_row, menu.menu)
                menu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_rename -> {
                            SimpleDialogFragment("Rename") { newName ->
                                lifecycleScope.launch {
                                    playlistRepository.renamePlaylist(pl.id, newName)
                                }
                            }.show(supportFragmentManager, "RenameChildPlaylist")
                            true
                        }
                        R.id.menu_delete -> {
                            lifecycleScope.launch {
                                playlistRepository.deletePlaylists(listOf(pl.id))
                            }
                            true
                        }
                        else -> false
                    }
                }
                menu.show()
            }
        )
        childPlaylistsRecycler.adapter = childPlaylistAdapter

        lifecycleScope.launch {
            val songsInThis = playlistRepository.getSongsForPlaylist(playlistId)
            addedSongAdapter.submitList(songsInThis)

            val childPlaylists = playlistRepository.getChildPlaylists(playlistId)

            val childSummaries: List<PlaylistSummary> = childPlaylists.map { p ->
                val count = playlistRepository.getSongsForPlaylist(p.id).size
                PlaylistSummary(id = p.id, name = p.name, songCount = count)
            }
            childPlaylistAdapter.submitList(childSummaries)

            val map = LinkedHashMap<Long, Song>()
            for (s in songsInThis) map[s.id] = s

            for (child in childPlaylists) {
                val childSongs = playlistRepository.getSongsForPlaylist(child.id)
                for (s in childSongs) {
                    if (!map.containsKey(s.id)) map[s.id] = s
                }
            }

            allSongs.clear()
            allSongs.addAll(map.values)

            allSongsAdapter.submitList(allSongs.toList())
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
                val ids = allSongs.map { it.id }
                if (ids.isNotEmpty()) {
                    PlayBackRepository.play(ids)
                }
                finish()
                true
            }

            R.id.action_delete -> {
                lifecycleScope.launch {
                    playlistRepository.deletePlaylists(listOf(playlistId))
                    finish()
                }
                true
            }

            R.id.action_rename -> {
                SimpleDialogFragment("Rename") { newName ->
                    lifecycleScope.launch {
                        playlistRepository.renamePlaylist(playlistId, newName)
                        supportActionBar?.title = newName
                    }
                }.show(supportFragmentManager, "RenamePlaylistDialog")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
