package com.example.mazika

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.example.mazika.databinding.ActivityMainBinding
import com.example.mazika.repository.PlaylistRepository
import com.example.mazika.ui.playlists.PlaylistViewModel
import com.example.mazika.ui.songs.SongViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var songViewModel: SongViewModel
    public lateinit var playlistViewModel: PlaylistViewModel
    private val PERMISSION_REQUEST_CODE = 123
    private lateinit var binding: ActivityMainBinding


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //These were already when i created the project
        //This part is used for the bottom navigation Bar
        //To add a new button you need to add a new Item in "selection_menu.xml" create a fragment and add it in "mobile_navigation.xml"
        //Note that both the fragment and item should have the same ID
        //Also the ID needs to be added "appBarConfiguration" inside "MainActivity.kt"
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_list,
                R.id.navigation_playlists,
                R.id.navigation_settings
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // Permissions + notification channel
        demandPermissionsAndChannel()

        // ViewModel
        songViewModel = ViewModelProvider(this)[SongViewModel::class.java]
        songViewModel.fetchSongs()

        // Mini-player setup
        setupMiniPlayer(navController)

        //Playlist
        //deleteDatabase("mazika.db")
        val db = Room.databaseBuilder(this, MyDatabase::class.java,
            "mazika.db")
            .fallbackToDestructiveMigration(false).build()

        PlaylistRepository.init(db.playlistDao,db.playlistSongDao,db.playlistPlaylistDao)
        playlistViewModel = PlaylistViewModel(PlaylistRepository)



        //playlistViewModel.addPlaylist("Sad")
        //playlistViewModel.addPlaylist("BOMBA")
        //playlistViewModel.addPlaylist("Another PLAYLIST ")


    }

    private fun setupMiniPlayer(navController: NavController) {
        val miniPlayer = binding.miniPlayer

        miniPlayer.visibility = View.GONE

        // Tap mini-player => open full player screen
        miniPlayer.setOnClickListener {
            if (navController.currentDestination?.id != R.id.playerFragment) {
                navController.navigate(R.id.playerFragment)
            }
        }

        // Show/hide mini-player depending on current screen
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isPlayerScreen = destination.id == R.id.playerFragment
            val hasSong = songViewModel.currentSong.value != null
            miniPlayer.visibility = if (!isPlayerScreen && hasSong) View.VISIBLE else View.GONE
        }

        // Song title
        songViewModel.currentSong.observe(this) { song ->
            if (song == null) {
                miniPlayer.visibility = View.GONE
            } else {
                // if not on player screen, show
                val isPlayerScreen = navController.currentDestination?.id == R.id.playerFragment
                miniPlayer.visibility = if (!isPlayerScreen) View.VISIBLE else View.GONE
                binding.currentSongTitle.text = song.title
            }
        }

        // Play/Pause icon
        songViewModel.isPlaying.observe(this) { playing ->
            binding.btnPlayPause.setImageResource(
                if (playing) android.R.drawable.ic_media_pause
                else android.R.drawable.ic_media_play
            )
        }

        // Buttons
        binding.btnPlayPause.setOnClickListener { songViewModel.togglePlayback() }
        binding.btnNext.setOnClickListener { songViewModel.next() }
        binding.btnPrevious.setOnClickListener { songViewModel.previous() }

        // Progress
        songViewModel.duration.observe(this) { dur ->
            binding.progressBar.max = dur.coerceAtLeast(1)
        }
        songViewModel.position.observe(this) { pos ->
            binding.progressBar.progress = pos.coerceAtLeast(0)
            binding.progressText.text = formatTime(pos)
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun demandPermissionsAndChannel() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "running_channel",
                "Running",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // If audio permission granted, refresh songs
            val audioIndex = permissions.indexOfFirst {
                it == Manifest.permission.READ_MEDIA_AUDIO || it == Manifest.permission.READ_EXTERNAL_STORAGE
            }
            if (audioIndex != -1 && grantResults[audioIndex] == PackageManager.PERMISSION_GRANTED) {
                songViewModel.fetchSongs()
            }
        }
    }
}
