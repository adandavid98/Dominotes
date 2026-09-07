package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["matchId"])]
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val matchId: Long,
    val roundNumber: Int,
    val winnerIndex: Int,
    val points: Int,
    val bonusTag: String? = null,
    val scoresSnapshotRaw: String, // Comma separated snapshot of scores after this round
    val timestamp: Long = System.currentTimeMillis()
)
