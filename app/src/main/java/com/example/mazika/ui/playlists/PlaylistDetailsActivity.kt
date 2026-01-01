package com.example.mazika.ui.playlists

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.R
import com.example.mazika.repository.PlaylistRepository
import com.example.mazika.ui.songs.SongAdapter
import kotlinx.coroutines.launch

class PlaylistDetailsActivity : AppCompatActivity(R.layout.playlist_details_activity) {

    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playlistRepository = PlaylistRepository

        val playlistId = intent.getIntExtra("playlistId", -1)
        if (playlistId == -1) {
            finish()
            return
        }

        recyclerView = findViewById(R.id.song_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val songs = playlistRepository.getSongsForPlaylist(playlistId)
            recyclerView.adapter = SongAdapter(songs) { song ->
                // song click logic
                print(song.title)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
