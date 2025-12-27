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
    private  var notificationManager: PlayerNotificationManager? = null
    private  var songRepository = SongRepository
    private  var playBackRepository = PlayBackRepository
    private var queue: List<Song> = emptyList()
    private var currentIndex = 0;
    val TIME_UNSET = -9223372036854775807L

    override fun onCreate() {
        super.onCreate()
        // 1. Create player
        player = ExoPlayer.Builder(this).build()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentIndex = player.currentMediaItemIndex

                playBackRepository.currentIndex.value = currentIndex
                playBackRepository.currentSong.value = queue.getOrNull(currentIndex)

                notificationManager?.invalidate()
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    val dur = player.duration
                    if (dur != TIME_UNSET) {
                        playBackRepository.duration.value = dur.toInt()
                    }
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playBackRepository.isPlaying.value = isPlaying
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
            Actions.NEXT.toString()->next();
            Actions.PREVIOUS.toString()->previous()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun next() {
        if (queue.isEmpty()) return

        currentIndex++

        // Loop to beginning if we reach the end
        if (currentIndex >= queue.size) {
            currentIndex = 0
        }

        playAtIndex(currentIndex)
    }

    private fun previous() {
        if (queue.isEmpty()) return

        currentIndex--

        // Loop to end if we go below zero
        if (currentIndex < 0) {
            currentIndex = queue.size - 1
        }

        playAtIndex(currentIndex)
    }

    private fun playAtIndex(index: Int) {
        val song = queue[index]

        val mediaItem = MediaItem.fromUri(song.data)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        // 🔁 update repository state
        PlayBackRepository.currentSong.value = song
        PlayBackRepository.currentIndex.value = index
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

    private fun start(songIds: List<Long>) {
        val songs = songRepository.getSongsByIds(songIds)
        if (songs.isEmpty()) return

        queue = songs
        currentIndex = 0

        playBackRepository.queue.value = songs
        playBackRepository.currentIndex.value = 0
        playBackRepository.currentSong.value = songs[0]

        val mediaItems = songs.map { MediaItem.fromUri(it.data) }
        player.setMediaItems(mediaItems)
        player.prepare()

        player.play()


        // start updating progress
        handler.post(progressRunnable)
    }

    override fun onDestroy() {
        notificationManager?.setPlayer(null)
        player.release()
        mediaSession.release()
        handler.removeCallbacks(progressRunnable)
        super.onDestroy()
    }




    private val descriptionAdapter =
        object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): String {
                return playBackRepository.currentSong.value?.title ?:"Unknown"//"Song Title"
            }

            override fun getCurrentContentText(player: Player): String? {
                return  playBackRepository.currentSong.value?.artist ?:"Unknown"
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
    private val handler = Handler(Looper.getMainLooper())

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (player.isPlaying) {
                // Publish current position to PlaybackRepository
                playBackRepository.position.value = player.currentPosition.toInt()
            }
            handler.postDelayed(this, 500)
        }
    }



}