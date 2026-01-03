package com.example.mazika.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.mazika.model.Song
import com.example.mazika.services.MusicService
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(UnstableApi::class)
object PlayBackRepository {

    private lateinit var appContext: Context
    fun init(context: Context) { appContext = context.applicationContext }

    val currentSong = MutableStateFlow<Song?>(null)
    val isPlaying = MutableStateFlow(false)
    val position = MutableStateFlow(0)   // ms
    val duration = MutableStateFlow(0)   // ms

    val queue = MutableStateFlow<List<Song>>(emptyList())
    val currentIndex = MutableStateFlow(0)

    fun play(songIds: List<Long>) {
        sendAction(Actions.START) {
            putExtra("songs_ID", songIds.toLongArray())
        }
    }

    fun toggle() = sendAction(Actions.TOGGLE_PLAY)
    fun next() = sendAction(Actions.NEXT)
    fun previous() = sendAction(Actions.PREVIOUS)

    fun seekTo(positionMs: Long) {
        sendAction(Actions.SEEK_TO) {
            putExtra("positionMs", positionMs)
        }
        position.value = positionMs.toInt()
    }

    private fun requireContext(): Context {
        check(::appContext.isInitialized) {
            "PlayBackRepository not initialized. Call init(context) first."
        }
        return appContext
    }

    private fun sendAction(action: Actions, extras: Intent.() -> Unit = {}) {
        val intent = Intent(requireContext(), MusicService::class.java).apply {
            this.action = action.name
            extras()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent)
        } else {
            requireContext().startService(intent)
        }
    }
}
