package com.example.data.repository

import com.example.data.local.DominoDao
import com.example.data.local.MatchEntity
import com.example.data.local.MatchWithRounds
import com.example.data.local.RoundEntity
import kotlinx.coroutines.flow.Flow

class DominoRepository(private val dao: DominoDao) {
    val allMatches: Flow<List<MatchEntity>> = dao.getAllMatches()

    fun getMatchWithRounds(matchId: Long): Flow<MatchWithRounds?> {
        return dao.getMatchWithRounds(matchId)
    }

    suspend fun getMatchById(matchId: Long): MatchEntity? {
        return dao.getMatchById(matchId)
    }

    suspend fun createMatch(match: MatchEntity): Long {
        return dao.insertMatch(match)
    }

    suspend fun updateMatch(match: MatchEntity) {
        dao.updateMatch(match)
    }

    suspend fun deleteMatch(matchId: Long) {
        dao.deleteMatchById(matchId)
    }

    suspend fun addRound(round: RoundEntity): Long {
        return dao.insertRound(round)
    }

    suspend fun deleteRound(roundId: Long) {
        dao.deleteRoundById(roundId)
    }

    suspend fun clearRounds(matchId: Long) {
        dao.clearRoundsForMatch(matchId)
    }

    suspend fun getRoundsForMatch(matchId: Long): List<RoundEntity> {
        return dao.getRoundsListForMatch(matchId)
    }
}
