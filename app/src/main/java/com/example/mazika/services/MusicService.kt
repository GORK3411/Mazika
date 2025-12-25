package com.example.mazika.services


import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.media.session.MediaSession
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager
import com.example.mazika.model.Song
import com.example.mazika.repository.SongRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow


@UnstableApi
class MusicService : Service() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private  var notificationManager: PlayerNotificationManager? = null

    private  var songRepository = SongRepository

    override fun onCreate() {
        super.onCreate()


        // 1. Create player
        player = ExoPlayer.Builder(this).build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val songPath = mediaItem?.localConfiguration?.uri?.path ?: return

                // Find the song in database
                currentSong = songRepository.getSongByPath(songPath,application)
                songRepository.currentSong.value = currentSong


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
            Actions.TOGGLE_PLAY.toString()->toggle();
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun toggle()
    {
        if (player.isPlaying) {
            player.pause()
        } else {
            // Only play if there is something to play
            if (player.mediaItemCount > 0) {
                player.play()
            }
        }
    }

    private var playlist: List<Song> = emptyList()
    private var currentSong : Song? = null;
    /*
    private fun start(songIds: List<Long>) {

        val songs = songRepository.getSongsByIds(songIds.toList(),application)

        if (songs.isEmpty()) return

        // Keep the first song as "currently playing"
        currentSong = songs[0]
        songRepository.currentSong.value = currentSong
        // Convert songs to MediaItems
        val mediaItems = songs.map { song ->
            MediaItem.fromUri(song.data)
        }

        player.setMediaItems(mediaItems)
        player.prepare()
        player.play()
    }
     */


    private fun start(songIds: List<Long>) {
        val songs = songRepository.getSongsByIds(songIds, application)
        if (songs.isEmpty()) return

        currentSong = songs[0]
        SongRepository.setCurrentSong(currentSong)

        val mediaItems = songs.map { MediaItem.fromUri(it.data) }
        player.setMediaItems(mediaItems)
        player.prepare()
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    val dur = player.duration
                    if (dur != -9223372036854775807L) {
                        SongRepository.setDuration(dur.toInt())
                    }
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                SongRepository.setIsPlaying(isPlaying)
            }
        })

        SongRepository.setDuration(player.duration.toInt())
        player.play()


        // start updating progress
        handler.post(progressRunnable)
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


    //keep track of the song's duration

    val currentPosition = MutableStateFlow(0L)
    val duration = MutableStateFlow(0L)

    private val handler = Handler(Looper.getMainLooper())

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (player.isPlaying) {
                val pos = player.currentPosition.toInt()
                SongRepository.setCurrentPosition(pos)
            }
            handler.postDelayed(this, 500)
        }
    }


}