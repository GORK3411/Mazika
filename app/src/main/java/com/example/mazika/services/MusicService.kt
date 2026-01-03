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
import com.example.mazika.repository.Actions
import com.example.mazika.repository.PlayBackRepository
import com.example.mazika.repository.SongRepository

@UnstableApi
class MusicService : Service() {

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private var notificationManager: PlayerNotificationManager? = null

    private val songRepository = SongRepository
    private val playBackRepository = PlayBackRepository

    private var queue: List<Song> = emptyList()
    private var currentIndex = 0

    private val handler = Handler(Looper.getMainLooper())

    private val progressRunnable = object : Runnable {
        override fun run() {
            // ✅ Always publish position (even if paused, after seek etc.)
            val pos = player.currentPosition.toInt().coerceAtLeast(0)
            playBackRepository.position.value = pos

            // duration can become available later
            val dur = player.duration
            if (dur > 0 ) {
                playBackRepository.duration.value = dur.toInt()
            }

            handler.postDelayed(this, 300)
        }
    }

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this).build()

        player.addListener(object : Player.Listener {

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentIndex = player.currentMediaItemIndex.coerceAtLeast(0)

                playBackRepository.currentIndex.value = currentIndex
                playBackRepository.currentSong.value = queue.getOrNull(currentIndex)

                // when song changes reset position instantly
                playBackRepository.position.value = 0

                notificationManager?.invalidate()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    val dur = player.duration
                    if (dur > 0) {
                        playBackRepository.duration.value = dur.toInt()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playBackRepository.isPlaying.value = isPlaying
            }
        })

        mediaSession = MediaSession(this, "MusicService")
        mediaSession.isActive = true

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

        // start progress loop (safe even before songs)
        handler.post(progressRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.name -> {
                val ids = intent.getLongArrayExtra("songs_ID")?.toList() ?: emptyList()
                start(ids)
            }

            Actions.STOP.name -> {
                stopPlaybackAndService()
            }

            Actions.TOGGLE_PLAY.name -> toggle()

            Actions.NEXT.name -> next()

            Actions.PREVIOUS.name -> previous()

            Actions.SEEK_TO.name -> {
                val pos = intent.getLongExtra("positionMs", 0L).coerceAtLeast(0L)
                player.seekTo(pos)
                playBackRepository.position.value = pos.toInt()
            }
        }

        return START_STICKY
    }

    private fun start(songIds: List<Long>) {
        val songs = songRepository.getSongsByIds(songIds)
        if (songs.isEmpty()) return

        queue = songs
        currentIndex = 0

        playBackRepository.queue.value = songs
        playBackRepository.currentIndex.value = 0
        playBackRepository.currentSong.value = songs[0]
        playBackRepository.position.value = 0

        val mediaItems = songs.map { MediaItem.fromUri(it.data) }

        player.setMediaItems(mediaItems, /* resetPosition */ true)
        player.prepare()
        player.play()
    }

    private fun toggle() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.mediaItemCount > 0) player.play()
        }
    }

    private fun next() {
        if (player.mediaItemCount == 0) return
        player.seekToNextMediaItem()
        player.play()
    }

    private fun previous() {
        if (player.mediaItemCount == 0) return
        player.seekToPreviousMediaItem()
        player.play()
    }

    private fun stopPlaybackAndService() {
        try {
            player.stop()
        } catch (_: Exception) {}
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        notificationManager?.setPlayer(null)
        notificationManager = null

        handler.removeCallbacks(progressRunnable)

        player.release()
        mediaSession.release()

        super.onDestroy()
    }

    private val descriptionAdapter =
        object : PlayerNotificationManager.MediaDescriptionAdapter {

            override fun getCurrentContentTitle(player: Player): String {
                return playBackRepository.currentSong.value?.title ?: "Unknown"
            }

            override fun getCurrentContentText(player: Player): String? {
                return playBackRepository.currentSong.value?.artist ?: "Unknown"
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? = null

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                // optional: open app when tap notification
                return null
            }
        }

    private val notificationListener =
        object : PlayerNotificationManager.NotificationListener {

            override fun onNotificationPosted(id: Int, notification: Notification, ongoing: Boolean) {
                startForeground(id, notification)
            }

            override fun onNotificationCancelled(id: Int, dismissedByUser: Boolean) {
                stopPlaybackAndService()
            }
        }
}
