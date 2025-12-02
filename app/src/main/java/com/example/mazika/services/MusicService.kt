package com.example.mazika.services


import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.media.session.MediaSession
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager
import com.example.mazika.R
import com.example.mazika.model.Song
import com.example.mazika.repository.SongRepository


@UnstableApi
class MusicService : Service() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private  var notificationManager: PlayerNotificationManager? = null

    private lateinit var songRepository : SongRepository

    override fun onCreate() {
        super.onCreate()

        songRepository = SongRepository(this)
        // 1. Create player
        player = ExoPlayer.Builder(this).build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val songPath = mediaItem?.localConfiguration?.uri?.path ?: return

                // Find the song in database
                currentSong = songRepository.getSongByPath(songPath)

                // Update the notification (important)
                notificationManager?.invalidate()
            }
        })

        // 2. Create MediaSession
        mediaSession = MediaSession(this, "MusicService")
        mediaSession.isActive = true

        // 3. Create notification manager
        notificationManager = PlayerNotificationManager.Builder(
            this,
            1,
            "running_channel"
        )
            .setMediaDescriptionAdapter(descriptionAdapter)
            .setNotificationListener(notificationListener)

            .build()

        notificationManager?.setPlayer(player)
        notificationManager?.setMediaSessionToken(mediaSession.sessionToken)
    }
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.action)
        {
            Actions.START.toString()->start(intent.getLongArrayExtra("songs_ID")?.toList() ?: emptyList())//strat(intent.getStringExtra("song_uri").toString())
            Actions.STOP.toString()->stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun strat(uri:String) {

        // Build the media item.
        val mediaItem = MediaItem.fromUri(uri)
        // Set the media item to be played.
        player.setMediaItem(mediaItem)
        // Prepare the player.
        player.prepare()
        // Start the playback.
        player.play()
    }

    private var playlist: List<Song> = emptyList()
    private var currentSong : Song? = null;
    private fun start(songIds: List<Long>) {

        val songs = songRepository.getSongsByIds(songIds.toList())

        if (songs.isEmpty()) return

        // Keep the first song as "currently playing"
        currentSong = songs[0]

        // Convert songs to MediaItems
        val mediaItems = songs.map { song ->
            MediaItem.fromUri(song.data)
        }

        player.setMediaItems(mediaItems)
        player.prepare()
        player.play()
    }


    override fun onDestroy() {
        notificationManager?.setPlayer(null)
        player.release()
        mediaSession.release()
        super.onDestroy()
    }


    enum class Actions
    {
        START,STOP,TOGGLE_PLAY;


    }

    private val descriptionAdapter =
        object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): String {
                return currentSong?.title ?:"Unknown"//"Song Title"
            }

            override fun getCurrentContentText(player: Player): String? {
                return currentSong?.artist ?:"Unknown"
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                return null
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                return null
            }
        }

    private val notificationListener =
        object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationPosted(id: Int, notification: Notification, ongoing: Boolean) {
                startForeground(id, notification)
            }

            override fun onNotificationCancelled(id: Int, dismissedByUser: Boolean) {
                stopForeground(true)
                stopSelf()
            }
        }
}