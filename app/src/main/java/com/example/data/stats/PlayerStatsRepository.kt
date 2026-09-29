package com.example.data.stats

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.data.domino.DominoTile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerDominoStats(
    val totalMatchesPlayed: Int = 0,
    val totalMatchesWon: Int = 0,
    val matchesPlayedParejas: Int = 0,
    val matchesWonParejas: Int = 0,
    val matchesPlayedIndividual: Int = 0,
    val matchesWonIndividual: Int = 0,
    val currentWinningStreak: Int = 0,
    val bestWinningStreak: Int = 0,
    val totalRoundsWon: Int = 0,
    val totalPointsScored: Int = 0,
    val trancasWon: Int = 0,
    val capicuaWins: Int = 0,
    val topWinningTileTop: Int = 6,
    val topWinningTileBottom: Int = 6,
    val topWinningTileCount: Int = 0,
    val playerRankTitle: String = "Aficionado del Dominó",
    val favoriteTileWins: Map<String, Int> = emptyMap()
) {
    val overallWinRatePercent: Int
        get() = if (totalMatchesPlayed > 0) ((totalMatchesWon.toFloat() / totalMatchesPlayed) * 100).toInt() else 0

    val parejasWinRatePercent: Int
        get() = if (matchesPlayedParejas > 0) ((matchesWonParejas.toFloat() / matchesPlayedParejas) * 100).toInt() else 0

    val individualWinRatePercent: Int
        get() = if (matchesPlayedIndividual > 0) ((matchesWonIndividual.toFloat() / matchesPlayedIndividual) * 100).toInt() else 0

    val averagePointsPerMatch: Int
        get() = if (totalMatchesPlayed > 0) totalPointsScored / totalMatchesPlayed else 0

    val topWinningTile: DominoTile
        get() = DominoTile(topWinningTileTop, topWinningTileBottom)
}

class PlayerStatsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("domino_player_stats_prefs", Context.MODE_PRIVATE)

    private val _stats = MutableStateFlow(loadStats())
    val stats: StateFlow<PlayerDominoStats> = _stats.asStateFlow()

    private fun loadStats(): PlayerDominoStats {
        val totalPlayed = prefs.getInt("total_played", 12)
        val totalWon = prefs.getInt("total_won", 8)
        val playedParejas = prefs.getInt("played_parejas", 8)
        val wonParejas = prefs.getInt("won_parejas", 6)
        val playedInd = prefs.getInt("played_ind", 4)
        val wonInd = prefs.getInt("won_ind", 2)
        val streak = prefs.getInt("current_streak", 3)
        val bestStreak = prefs.getInt("best_streak", 5)
        val roundsWon = prefs.getInt("rounds_won", 24)
        val points = prefs.getInt("total_points", 840)
        val trancas = prefs.getInt("trancas_won", 5)
        val capicuas = prefs.getInt("capicuas_won", 3)
        val topTop = prefs.getInt("top_tile_top", 6)
        val topBottom = prefs.getInt("top_tile_bottom", 6)
        val topCount = prefs.getInt("top_tile_count", 7)

        val rank = when {
            totalWon >= 50 -> "Gran Maestro del Doble Seis"
            totalWon >= 25 -> "Maestro de la Mesa"
            totalWon >= 10 -> "Estratega del Tranque"
            totalWon >= 5 -> "Jugador Avanzado"
            else -> "Aficionado del Dominó"
        }

        return PlayerDominoStats(
            totalMatchesPlayed = totalPlayed,
            totalMatchesWon = totalWon,
            matchesPlayedParejas = playedParejas,
            matchesWonParejas = wonParejas,
            matchesPlayedIndividual = playedInd,
            matchesWonIndividual = wonInd,
            currentWinningStreak = streak,
            bestWinningStreak = bestStreak,
            totalRoundsWon = roundsWon,
            totalPointsScored = points,
            trancasWon = trancas,
            capicuaWins = capicuas,
            topWinningTileTop = topTop,
            topWinningTileBottom = topBottom,
            topWinningTileCount = topCount,
            playerRankTitle = rank
        )
    }

    fun recordMatchOutcome(
        won: Boolean,
        isTeams: Boolean,
        pointsScored: Int,
        winningTile: DominoTile?,
        wasTranca: Boolean = false,
        wasCapicua: Boolean = false
    ) {
        val current = _stats.value
        val newPlayed = current.totalMatchesPlayed + 1
        val newWon = if (won) current.totalMatchesWon + 1 else current.totalMatchesWon
        val newPlayedParejas = if (isTeams) current.matchesPlayedParejas + 1 else current.matchesPlayedParejas
        val newWonParejas = if (isTeams && won) current.matchesWonParejas + 1 else current.matchesWonParejas
        val newPlayedInd = if (!isTeams) current.matchesPlayedIndividual + 1 else current.matchesPlayedIndividual
        val newWonInd = if (!isTeams && won) current.matchesWonIndividual + 1 else current.matchesWonIndividual

        val newStreak = if (won) current.currentWinningStreak + 1 else 0
        val newBestStreak = maxOf(current.bestWinningStreak, newStreak)
        val newPoints = current.totalPointsScored + pointsScored
        val newTrancas = if (won && wasTranca) current.trancasWon + 1 else current.trancasWon
        val newCapicuas = if (won && wasCapicua) current.capicuaWins + 1 else current.capicuaWins

        var topT = current.topWinningTileTop
        var topB = current.topWinningTileBottom
        var topC = current.topWinningTileCount

        if (won && winningTile != null) {
            val key = "${winningTile.left}_${winningTile.right}"
            val prevWinsForTile = prefs.getInt("tile_win_$key", 0) + 1
            prefs.edit().putInt("tile_win_$key", prevWinsForTile).apply()
            if (prevWinsForTile > topC) {
                topT = winningTile.left
                topB = winningTile.right
                topC = prevWinsForTile
            }
        }

        val rank = when {
            newWon >= 50 -> "Gran Maestro del Doble Seis"
            newWon >= 25 -> "Maestro de la Mesa"
            newWon >= 10 -> "Estratega del Tranque"
            newWon >= 5 -> "Jugador Avanzado"
            else -> "Aficionado del Dominó"
        }

        prefs.edit()
            .putInt("total_played", newPlayed)
            .putInt("total_won", newWon)
            .putInt("played_parejas", newPlayedParejas)
            .putInt("won_parejas", newWonParejas)
            .putInt("played_ind", newPlayedInd)
            .putInt("won_ind", newWonInd)
            .putInt("current_streak", newStreak)
            .putInt("best_streak", newBestStreak)
            .putInt("total_points", newPoints)
            .putInt("trancas_won", newTrancas)
            .putInt("capicuas_won", newCapicuas)
            .putInt("top_tile_top", topT)
            .putInt("top_tile_bottom", topB)
            .putInt("top_tile_count", topC)
            .apply()

        _stats.value = current.copy(
            totalMatchesPlayed = newPlayed,
            totalMatchesWon = newWon,
            matchesPlayedParejas = newPlayedParejas,
            matchesWonParejas = newWonParejas,
            matchesPlayedIndividual = newPlayedInd,
            matchesWonIndividual = newWonInd,
            currentWinningStreak = newStreak,
            bestWinningStreak = newBestStreak,
            totalPointsScored = newPoints,
            trancasWon = newTrancas,
            capicuaWins = newCapicuas,
            topWinningTileTop = topT,
            topWinningTileBottom = topB,
            topWinningTileCount = topC,
            playerRankTitle = rank
        )
    }
}
