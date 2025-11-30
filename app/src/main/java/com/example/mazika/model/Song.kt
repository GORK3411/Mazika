package com.example.mazika.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
/*
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Directory::class,
            parentColumns = ["id"],  // column in Directory
            childColumns = ["directory_id"],  // column in Song
            onDelete = ForeignKey.CASCADE // optional, what happens when Directory is deleted
        )
    ]
)
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val directory_id: Int
)
*/
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val data: String   // file path (important for playback)
)

