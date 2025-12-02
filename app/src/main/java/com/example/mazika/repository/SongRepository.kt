package com.example.mazika.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import com.example.mazika.model.Song

class SongRepository(private val context: Context) {

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATA
    )

    @SuppressLint("Range")
    public fun loadSongs() : List<Song> {
        val songList = mutableListOf<Song>()

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use {
            while (it.moveToNext()) {

                val id = it.getLong(it.getColumnIndex(MediaStore.Audio.Media._ID))
                val title = it.getString(it.getColumnIndex(MediaStore.Audio.Media.TITLE)) ?: "Unknown"
                val artist = it.getString(it.getColumnIndex(MediaStore.Audio.Media.ARTIST)) ?: "Unknown"
                val duration = it.getLong(it.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val data = it.getString(it.getColumnIndex(MediaStore.Audio.Media.DATA))

                songList.add(Song(id, title, artist, duration, data))
            }
        }

        return songList
    }


    @SuppressLint("Range")
    public fun getSongsByIds(songIds : List<Long>) : List<Song>
    {
        val songList = mutableListOf<Song>()
        if (songIds.isEmpty()) return songList

        // Convert List<Long> to comma-separated string
        val idsString = songIds.joinToString(",")


        val selection = "${MediaStore.Audio.Media._ID} IN ($idsString) AND ${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use {
            while (it.moveToNext()) {

                val id = it.getLong(it.getColumnIndex(MediaStore.Audio.Media._ID))
                val title = it.getString(it.getColumnIndex(MediaStore.Audio.Media.TITLE)) ?: "Unknown"
                val artist = it.getString(it.getColumnIndex(MediaStore.Audio.Media.ARTIST)) ?: "Unknown"
                val duration = it.getLong(it.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val data = it.getString(it.getColumnIndex(MediaStore.Audio.Media.DATA))

                songList.add(Song(id, title, artist, duration, data))
            }
        }


        return songList
    }
}