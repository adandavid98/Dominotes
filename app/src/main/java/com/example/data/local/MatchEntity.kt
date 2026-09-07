package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val gameMode: String, // from GameMode enum
    val targetScore: Int,
    val playerNamesRaw: String, // Comma separated or serialized
    val scoresRaw: String, // Comma separated scores matching playerNames
    val isCompleted: Boolean = false,
    val winnerName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
