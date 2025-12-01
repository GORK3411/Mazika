package com.example.mazika

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavArgument
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mazika.databinding.ActivityMainBinding
import com.example.mazika.repository.SongRepository
import com.example.mazika.ui.songs.SongAdapter
import com.example.mazika.ui.songs.SongViewFragment
import com.example.mazika.ui.songs.SongViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var songViewModel: SongViewModel
    private val PERMISSION_REQUEST_CODE = 123
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_list,R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //things i added

        //permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.FOREGROUND_SERVICE)
            ,0)

        //Notification
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel("running_channel","Running", NotificationManager.IMPORTANCE_HIGH)
            val  notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }


        //load the songs
        songViewModel = ViewModelProvider(this)[SongViewModel::class.java]


        songViewModel.songs.observe(this)
        {
            songs->
            Toast.makeText(this, ""+songs.size, Toast.LENGTH_SHORT).show()
            /*
            val tmp = findViewById<View>(R.id.song_recycler_view)
            if (tmp==null)
            {
                Toast.makeText(this, "NUUUUUUUUUUUUUUUUUUULLLLLLLLLLLLL", Toast.LENGTH_LONG).show()
            }else
            {
                val songAdapter = SongAdapter(songs)
                val recyclerView : RecyclerView = findViewById(R.id.song_recycler_view)
                //recyclerView.layoutManager = LinearLayoutManager(this)
                //recyclerView.adapter=songAdapter
            }
            */

        }


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_MEDIA_AUDIO),PERMISSION_REQUEST_CODE)
        }

        songViewModel.fetchSongs()

    }

    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted → load songs

            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}