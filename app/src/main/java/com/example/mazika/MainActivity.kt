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
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
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

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_list,R.id.navigation_playlists,R.id.navigation_settings
                //,R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        //New things added
        DemandPermissions()
        //initiate songViewModel and fetch songs
        songViewModel = ViewModelProvider(this)[SongViewModel::class.java]
        songViewModel.fetchSongs()

        AddPlayButton()

        addPlayBar()

        //Playlist
        //deleteDatabase("mazika.db")
        val db = Room.databaseBuilder(this, MyDatabase::class.java,
            "mazika.db")
            .fallbackToDestructiveMigration(false).build()

        PlaylistRepository.init(db.playlistDao,db.playlistSongDao,db.playlistPlaylistDao)
        playlistViewModel = PlaylistViewModel(PlaylistRepository)
        playlistViewModel.playlists.observe(this) { playlists ->
            Toast.makeText(
                this,
                "Playlists count: ${playlists.size}",
                Toast.LENGTH_SHORT
            ).show()
        }


        //playlistViewModel.addPlaylist("Sad")
        //playlistViewModel.addPlaylist("BOMBA")
        //playlistViewModel.addPlaylist("Another PLAYLIST ")


    }

    private fun addPlayBar() {
        val miniPlayer: LinearLayout = findViewById(R.id.miniPlayer)
        val tvSongTitle: TextView = findViewById(R.id.currentSongTitle)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val progressText: TextView = findViewById(R.id.progressText)

        miniPlayer.visibility = View.GONE


        songViewModel.currentSong.observe(this) { song ->
            if (song == null) {
                miniPlayer.visibility = View.GONE
            } else {
                miniPlayer.visibility = View.VISIBLE
                tvSongTitle.text = song.title
            }
        }


        songViewModel.duration.observe(this) { duration ->
            progressBar.max = duration.toInt()
        }
        songViewModel.position.observe(this) { position ->
            progressBar.progress = position
            progressText.text = formatTime(position)
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    @OptIn(UnstableApi::class)
    private fun AddPlayButton()
    {
        val btnPlayPause = findViewById<ImageButton>(R.id.btnPlayPause)
        btnPlayPause.setOnClickListener {
            songViewModel.togglePlayback()
        }

        val btnNext = findViewById<ImageButton>(R.id.btnNext)
        val btnPrev = findViewById<ImageButton>(R.id.btnPrevious)
        btnNext.setOnClickListener {
            songViewModel.next()
        }
        btnPrev.setOnClickListener {
            songViewModel.previous()
        }

    }



    private fun DemandPermissions()
    {
        //permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO,Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.FOREGROUND_SERVICE
                )
            ,PERMISSION_REQUEST_CODE)

        //Notification
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel("running_channel","Running", NotificationManager.IMPORTANCE_HIGH)
            val  notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        /*
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_MEDIA_AUDIO),PERMISSION_REQUEST_CODE)
        }

         */
    }
    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val audioPermissionIndex = permissions.indexOf(Manifest.permission.READ_MEDIA_AUDIO)

            if (audioPermissionIndex != -1 &&
                grantResults[audioPermissionIndex] == PackageManager.PERMISSION_GRANTED)
            {
                // User granted Read Media Audio → fetch songs
                songViewModel.fetchSongs()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val tmp = super.onSupportNavigateUp();
        return tmp;
    }

}