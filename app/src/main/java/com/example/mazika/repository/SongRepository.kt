package com.example.mazika.repository

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import com.example.mazika.model.Song
@SuppressLint("StaticFieldLeak")
object SongRepository {


    lateinit var contentResolver: ContentResolver

    fun init(context: Context) {
        contentResolver = context.applicationContext.contentResolver
    }

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.DATE_ADDED
    )

    @SuppressLint("Range")
    fun loadSongs() : List<Song> {
        //condition to load songs
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
                "${MediaStore.Audio.Media.DATA} NOT LIKE '%WhatsApp/Media/WhatsApp Voice Notes/%' AND " +
                "${MediaStore.Audio.Media.DATA} NOT LIKE '%WhatsApp/Media/WhatsApp Audio/%'"

        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

       return runCursor(selection,sortOrder)
    }


    @SuppressLint("Range")
    public fun getSongsByIds(songIds : List<Long>) : List<Song>
    {
        if (songIds.isEmpty()) return mutableListOf<Song>()

        // Convert List<Long> to comma-separated string
        val idsString = songIds.joinToString(",")

        val selection = "${MediaStore.Audio.Media._ID} IN ($idsString) AND ${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        return runCursor(selection,sortOrder)
    }



    @SuppressLint("Range")
    private fun runCursor(selection:String, sortOrder: String) : List<Song>
    {

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )

        val songList = mutableListOf<Song>()
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
}