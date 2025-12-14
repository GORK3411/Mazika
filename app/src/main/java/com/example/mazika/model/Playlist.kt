package com.example.mazika.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
@Entity
class Playlist(@PrimaryKey(autoGenerate = true) val id: Int = 0,
               val name: String) {
}