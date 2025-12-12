package com.example.mazika.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import com.example.mazika.model.Song
import kotlinx.coroutines.flow.MutableStateFlow

object SongRepository{


    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.DATE_ADDED
    )

    @SuppressLint("Range")
    public fun loadSongs(context: Context) : List<Song> {
        val songList = mutableListOf<Song>()

        //condition to load songs
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        //order at the end
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        //object that will move on each song
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )

        //foreach media item it will fill a song and add it to the resulting list
        cursor?.use {
            while (it.moveToNext()) {

                val id = it.getLong(it.getColumnIndex(MediaStore.Audio.Media._ID))
                val title = it.getString(it.getColumnIndex(MediaStore.Audio.Media.TITLE)) ?: "Unknown"
                val artist = it.getString(it.getColumnIndex(MediaStore.Audio.Media.ARTIST)) ?: "Unknown"
                val duration = it.getLong(it.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val data = it.getString(it.getColumnIndex(MediaStore.Audio.Media.DATA))
                val createDate = it.getLong(it.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED))
                songList.add(Song(id, title, artist, duration, data,createDate))
            }
        }

        return songList
    }


    @SuppressLint("Range")
    public fun getSongsByIds(songIds : List<Long>,context: Context) : List<Song>
    {
        val songList = mutableListOf<Song>()
        if (songIds.isEmpty()) return songList

        // Convert List<Long> to comma-separated string
        val idsString = songIds.joinToString(",")


        val selection = "${MediaStore.Audio.Media._ID} IN ($idsString) AND ${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

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
                val createDate = it.getLong(it.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED))
                songList.add(Song(id, title, artist, duration, data,createDate))
            }
        }


        return songList
    }

    fun getSongByPath(songPath: String,context: Context): Song?
    {
        return loadSongs(context).find { it.data == songPath }
    }

    //Test


    val currentSong = MutableStateFlow<Song?>(null)
    val isPlaying = MutableStateFlow(false)


}