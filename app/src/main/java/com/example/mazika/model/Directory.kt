package com.example.mazika.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class Directory(@PrimaryKey(autoGenerate = true) val id: Int = 0,
                     val path: String, val updated_at: LocalDateTime) {
}