package com.example.mazika.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val data: String,   // file path (important for playback)
    val createDate: Long
)

