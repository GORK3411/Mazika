package com.example.mazika.ui.playlists

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.model.Playlist
import com.example.mazika.model.Song
import com.example.mazika.repository.PlaylistRepository
import com.example.mazika.ui.songs.SongAdapter
import kotlinx.coroutines.launch

class PlaylistDetailsActivity : AppCompatActivity(R.layout.playlist_details_activity) {

    private val playlistRepository = PlaylistRepository
    private val allSongs: ArrayList<Song> = ArrayList<Song>();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playlistId = intent.getIntExtra("playlistId", -1)
        if (playlistId == -1) {
            finish()
            return
        }
        val playlistName = intent.getStringExtra("playlistName");
        supportActionBar?.title = playlistName ?: "Playlist"

        val addedSongsRecycler = findViewById<RecyclerView>(R.id.added_song_recycler_view)
        val playlistRecycler = findViewById<RecyclerView>(R.id.playlist_recycler_view)
        val allSongsRecycler = findViewById<RecyclerView>(R.id.all_song_recycler_view)

        addedSongsRecycler.layoutManager = LinearLayoutManager(this)
        playlistRecycler.layoutManager = LinearLayoutManager(this)
        allSongsRecycler.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            // Load everything safely
            val songs = playlistRepository.getSongsForPlaylist(playlistId)
            val playlists = playlistRepository.getChildPlaylists(playlistId)

            // Added songs
            addedSongsRecycler.adapter = SongAdapter(songs) { song ->
                println(song.title)
            }

            // Child playlists
            playlistRecycler.adapter = PlaylistAdapter(
                playlists,
                R.layout.playlist_view,
                bind = { holder, playlist ->
                    holder.textView.text = playlist.name
                },
                onClick = { playlist ->
                    // handle click
                }
            )

            // All songs (same data or different source later)
            ///DO THIS LATER

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
            allSongsRecycler.adapter = SongAdapter(songs) { song ->
                println(song.title)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
