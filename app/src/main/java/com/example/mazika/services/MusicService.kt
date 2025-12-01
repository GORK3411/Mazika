package com.example.mazika.services


import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.mazika.R


class MusicService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action)
        {
            Actions.START.toString()->strat()
            Actions.STOP.toString()->stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun strat() {
        val notification = NotificationCompat.Builder(this,"running_channel")
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("MUSIC")
            .setContentText("AAAAAAAAAAA")
            .build()

        startForeground(1,notification)


        //Try running music
    }

    enum class Actions
    {
        START,STOP
    }
}