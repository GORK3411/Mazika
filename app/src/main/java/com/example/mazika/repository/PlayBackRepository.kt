package com.example.mazika.repository

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.mazika.model.Song
import com.example.mazika.services.MusicService
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(UnstableApi::class)
object PlayBackRepository {
    lateinit var appContext: Context
    fun init(context: Context) { appContext = context.applicationContext }
    val currentSong = MutableStateFlow<Song?>(null)
    val isPlaying = MutableStateFlow(false)
    val position = MutableStateFlow(0)
    val duration = MutableStateFlow(0)
    // 👇 new (read-only from UI perspective)
    val queue = MutableStateFlow<List<Song>>(emptyList())
    val currentIndex = MutableStateFlow(0)


    fun play(songIds: List<Long>) {
        sendAction(Actions.START) {
            putExtra("songs_ID", songIds.toLongArray())
        }
    }

    fun toggle() {
        sendAction(Actions.TOGGLE_PLAY)
    }

    private fun requireContext(): Context {
        check(::appContext.isInitialized) {
            "PlayBackRepository not initialized. Call init(context) first."
        }
        return appContext
    }

    private fun sendAction(
        action: Actions,
        extras: Intent.() -> Unit = {}
    ) {
        val intent = Intent(requireContext(), MusicService::class.java).apply {
            this.action = action.name
            extras()
        }
        requireContext().startService(intent)
    }

}