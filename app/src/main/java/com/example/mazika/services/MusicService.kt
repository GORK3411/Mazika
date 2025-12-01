package com.example.mazika.services


import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.media.session.MediaSession
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager
import com.example.mazika.R


@UnstableApi
class MusicService : Service() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private  var notificationManager: PlayerNotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        // 1. Create player
        player = ExoPlayer.Builder(this).build()

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
            Actions.START.toString()->strat(intent.getStringExtra("song_uri").toString())
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

    override fun onDestroy() {
        notificationManager?.setPlayer(null)
        player.release()
        mediaSession.release()
        super.onDestroy()
    }


    enum class Actions
    {
        START,STOP
    }

    private val descriptionAdapter =
        object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): String {
                return "Song Title"
            }

            override fun getCurrentContentText(player: Player): String? {
                return "Artist Name"
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