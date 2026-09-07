package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DominoDao {
    @Query("SELECT * FROM matches ORDER BY createdAt DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Transaction
    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun getMatchWithRounds(matchId: Long): Flow<MatchWithRounds?>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchById(matchId: Long): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatchById(matchId: Long)

    @Query("SELECT * FROM rounds WHERE matchId = :matchId ORDER BY roundNumber ASC")
    fun getRoundsForMatch(matchId: Long): Flow<List<RoundEntity>>

    @Query("SELECT * FROM rounds WHERE matchId = :matchId ORDER BY roundNumber ASC")
    suspend fun getRoundsListForMatch(matchId: Long): List<RoundEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: RoundEntity): Long

    @Query("DELETE FROM rounds WHERE id = :roundId")
    suspend fun deleteRoundById(roundId: Long)

    @Query("DELETE FROM rounds WHERE matchId = :matchId")
    suspend fun clearRoundsForMatch(matchId: Long)
}
