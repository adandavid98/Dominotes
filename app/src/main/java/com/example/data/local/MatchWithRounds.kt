package com.example.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class MatchWithRounds(
    @Embedded val match: MatchEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "matchId"
    )
    val rounds: List<RoundEntity>
)
