package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DominoDatabase
import com.example.data.local.MatchEntity
import com.example.data.local.RoundEntity
import com.example.data.model.BonusTag
import com.example.data.model.GameMode
import com.example.data.model.ScoringDisplayMode
import com.example.data.repository.DominoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveGameState(
    val matchId: Long = 0L,
    val title: String = "Partida de Dominó",
    val gameMode: GameMode = GameMode.PAREJAS_2,
    val targetScore: Int = 100,
    val playerNames: List<String> = listOf("Nosotros", "Ellos"),
    val scores: List<Int> = listOf(0, 0),
    val rounds: List<RoundEntity> = emptyList(),
    val displayMode: ScoringDisplayMode = ScoringDisplayMode.RONDAS,
    val isGameOver: Boolean = false,
    val winnerName: String? = null,
    val winnerIndex: Int? = null,
    val showAddRoundDialog: Boolean = false,
    val showTrancaCalculator: Boolean = false,
    val showNewGameDialog: Boolean = false,
    val showVictoryDialog: Boolean = false,
    val showHistoryScreen: Boolean = false
)

class DominoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DominoRepository

    private val _gameState = MutableStateFlow(ActiveGameState())
    val gameState: StateFlow<ActiveGameState> = _gameState.asStateFlow()

    val matchHistory: StateFlow<List<MatchEntity>>

    init {
        val dao = DominoDatabase.getDatabase(application).dominoDao()
        repository = DominoRepository(dao)
        matchHistory = repository.allMatches.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initialize with a default ready-to-play match
        startNewGame(
            title = "Partida Rápida",
            mode = GameMode.PAREJAS_2,
            target = 100,
            names = listOf("Nosotros", "Ellos")
        )
    }

    fun setDisplayMode(mode: ScoringDisplayMode) {
        _gameState.update { it.copy(displayMode = mode) }
    }

    fun setShowAddRoundDialog(show: Boolean) {
        _gameState.update { it.copy(showAddRoundDialog = show) }
    }

    fun setShowTrancaCalculator(show: Boolean) {
        _gameState.update { it.copy(showTrancaCalculator = show) }
    }

    fun setShowNewGameDialog(show: Boolean) {
        _gameState.update { it.copy(showNewGameDialog = show) }
    }

    fun setShowVictoryDialog(show: Boolean) {
        _gameState.update { it.copy(showVictoryDialog = show) }
    }

    fun setShowHistoryScreen(show: Boolean) {
        _gameState.update { it.copy(showHistoryScreen = show) }
    }

    fun startNewGame(
        title: String,
        mode: GameMode,
        target: Int,
        names: List<String>
    ) {
        viewModelScope.launch {
            val validNames = if (names.size == mode.defaultTeams.size) names else mode.defaultTeams
            val initialScores = List(validNames.size) { 0 }

            val matchEntity = MatchEntity(
                title = title.ifBlank { "Partida de Dominó" },
                gameMode = mode.name,
                targetScore = target,
                playerNamesRaw = validNames.joinToString("||"),
                scoresRaw = initialScores.joinToString(","),
                isCompleted = false,
                winnerName = null
            )

            val newId = repository.createMatch(matchEntity)

            _gameState.update {
                ActiveGameState(
                    matchId = newId,
                    title = matchEntity.title,
                    gameMode = mode,
                    targetScore = target,
                    playerNames = validNames,
                    scores = initialScores,
                    rounds = emptyList(),
                    isGameOver = false,
                    winnerName = null,
                    winnerIndex = null,
                    showNewGameDialog = false,
                    showVictoryDialog = false
                )
            }
        }
    }

    fun addRound(winnerIndex: Int, points: Int, bonusTag: BonusTag = BonusTag.NINGUNO) {
        val currentState = _gameState.value
        if (winnerIndex !in currentState.playerNames.indices || points <= 0) return

        viewModelScope.launch {
            val newScores = currentState.scores.toMutableList()
            newScores[winnerIndex] = newScores[winnerIndex] + points

            val roundNumber = currentState.rounds.size + 1
            val scoresSnapshot = newScores.joinToString(",")

            val roundEntity = RoundEntity(
                matchId = currentState.matchId,
                roundNumber = roundNumber,
                winnerIndex = winnerIndex,
                points = points,
                bonusTag = if (bonusTag != BonusTag.NINGUNO) bonusTag.label else null,
                scoresSnapshotRaw = scoresSnapshot
            )

            val roundId = repository.addRound(roundEntity)
            val updatedRound = roundEntity.copy(id = roundId)
            val updatedRounds = currentState.rounds + updatedRound

            val targetReached = newScores[winnerIndex] >= currentState.targetScore
            val winnerName = if (targetReached) currentState.playerNames[winnerIndex] else null

            val updatedMatch = MatchEntity(
                id = currentState.matchId,
                title = currentState.title,
                gameMode = currentState.gameMode.name,
                targetScore = currentState.targetScore,
                playerNamesRaw = currentState.playerNames.joinToString("||"),
                scoresRaw = newScores.joinToString(","),
                isCompleted = targetReached,
                winnerName = winnerName
            )
            repository.updateMatch(updatedMatch)

            _gameState.update {
                it.copy(
                    scores = newScores,
                    rounds = updatedRounds,
                    isGameOver = targetReached,
                    winnerName = winnerName,
                    winnerIndex = if (targetReached) winnerIndex else null,
                    showAddRoundDialog = false,
                    showVictoryDialog = targetReached
                )
            }
        }
    }

    fun quickAddPoints(playerIndex: Int, delta: Int) {
        val currentState = _gameState.value
        if (playerIndex !in currentState.playerNames.indices) return

        if (delta > 0) {
            addRound(playerIndex, delta, BonusTag.NINGUNO)
        } else if (delta < 0) {
            // Subtract points directly if correction is needed
            viewModelScope.launch {
                val newScores = currentState.scores.toMutableList()
                newScores[playerIndex] = (newScores[playerIndex] + delta).coerceAtLeast(0)

                val updatedMatch = MatchEntity(
                    id = currentState.matchId,
                    title = currentState.title,
                    gameMode = currentState.gameMode.name,
                    targetScore = currentState.targetScore,
                    playerNamesRaw = currentState.playerNames.joinToString("||"),
                    scoresRaw = newScores.joinToString(","),
                    isCompleted = false,
                    winnerName = null
                )
                repository.updateMatch(updatedMatch)

                _gameState.update {
                    it.copy(
                        scores = newScores,
                        isGameOver = false,
                        winnerName = null,
                        winnerIndex = null
                    )
                }
            }
        }
    }

    fun undoLastRound() {
        val currentState = _gameState.value
        if (currentState.rounds.isEmpty()) return

        val lastRound = currentState.rounds.last()
        viewModelScope.launch {
            repository.deleteRound(lastRound.id)

            val remainingRounds = currentState.rounds.dropLast(1)
            val restoredScores = if (remainingRounds.isNotEmpty()) {
                remainingRounds.last().scoresSnapshotRaw
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
            } else {
                List(currentState.playerNames.size) { 0 }
            }

            val validScores = if (restoredScores.size == currentState.playerNames.size) {
                restoredScores
            } else {
                val fallback = currentState.scores.toMutableList()
                fallback[lastRound.winnerIndex] = (fallback[lastRound.winnerIndex] - lastRound.points).coerceAtLeast(0)
                fallback
            }

            val updatedMatch = MatchEntity(
                id = currentState.matchId,
                title = currentState.title,
                gameMode = currentState.gameMode.name,
                targetScore = currentState.targetScore,
                playerNamesRaw = currentState.playerNames.joinToString("||"),
                scoresRaw = validScores.joinToString(","),
                isCompleted = false,
                winnerName = null
            )
            repository.updateMatch(updatedMatch)

            _gameState.update {
                it.copy(
                    scores = validScores,
                    rounds = remainingRounds,
                    isGameOver = false,
                    winnerName = null,
                    winnerIndex = null,
                    showVictoryDialog = false
                )
            }
        }
    }

    fun deleteRound(roundId: Long) {
        val currentState = _gameState.value
        viewModelScope.launch {
            repository.deleteRound(roundId)
            val updatedRounds = repository.getRoundsForMatch(currentState.matchId)

            // Recalculate scores from rounds
            val recalculatedScores = MutableList(currentState.playerNames.size) { 0 }
            for (round in updatedRounds) {
                if (round.winnerIndex in recalculatedScores.indices) {
                    recalculatedScores[round.winnerIndex] += round.points
                }
            }

            val maxScore = recalculatedScores.maxOrNull() ?: 0
            val targetReached = maxScore >= currentState.targetScore
            val winnerIdx = if (targetReached) recalculatedScores.indexOf(maxScore) else null
            val winnerName = if (winnerIdx != null && winnerIdx >= 0) currentState.playerNames[winnerIdx] else null

            val updatedMatch = MatchEntity(
                id = currentState.matchId,
                title = currentState.title,
                gameMode = currentState.gameMode.name,
                targetScore = currentState.targetScore,
                playerNamesRaw = currentState.playerNames.joinToString("||"),
                scoresRaw = recalculatedScores.joinToString(","),
                isCompleted = targetReached,
                winnerName = winnerName
            )
            repository.updateMatch(updatedMatch)

            _gameState.update {
                it.copy(
                    scores = recalculatedScores,
                    rounds = updatedRounds,
                    isGameOver = targetReached,
                    winnerName = winnerName,
                    winnerIndex = winnerIdx
                )
            }
        }
    }

    fun resetGame() {
        val currentState = _gameState.value
        viewModelScope.launch {
            repository.clearRounds(currentState.matchId)
            val zeros = List(currentState.playerNames.size) { 0 }

            val updatedMatch = MatchEntity(
                id = currentState.matchId,
                title = currentState.title,
                gameMode = currentState.gameMode.name,
                targetScore = currentState.targetScore,
                playerNamesRaw = currentState.playerNames.joinToString("||"),
                scoresRaw = zeros.joinToString(","),
                isCompleted = false,
                winnerName = null
            )
            repository.updateMatch(updatedMatch)

            _gameState.update {
                it.copy(
                    scores = zeros,
                    rounds = emptyList(),
                    isGameOver = false,
                    winnerName = null,
                    winnerIndex = null,
                    showVictoryDialog = false
                )
            }
        }
    }

    fun loadMatch(matchId: Long) {
        viewModelScope.launch {
            val match = repository.getMatchById(matchId) ?: return@launch
            val rounds = repository.getRoundsForMatch(matchId)

            val names = match.playerNamesRaw.split("||").filter { it.isNotBlank() }
            val scores = match.scoresRaw.split(",").mapNotNull { it.trim().toIntOrNull() }

            val validScores = if (scores.size == names.size) scores else List(names.size) { 0 }
            val mode = runCatching { GameMode.valueOf(match.gameMode) }.getOrDefault(GameMode.PAREJAS_2)

            val winnerIdx = if (match.winnerName != null) names.indexOf(match.winnerName) else null

            _gameState.update {
                ActiveGameState(
                    matchId = match.id,
                    title = match.title,
                    gameMode = mode,
                    targetScore = match.targetScore,
                    playerNames = names,
                    scores = validScores,
                    rounds = rounds,
                    isGameOver = match.isCompleted,
                    winnerName = match.winnerName,
                    winnerIndex = if (winnerIdx != null && winnerIdx >= 0) winnerIdx else null,
                    showHistoryScreen = false,
                    showVictoryDialog = false
                )
            }
        }
    }

    fun deleteMatch(matchId: Long) {
        viewModelScope.launch {
            repository.deleteMatch(matchId)
            // If deleting current active match, start fresh
            if (_gameState.value.matchId == matchId) {
                startNewGame(
                    title = "Nueva Partida",
                    mode = GameMode.PAREJAS_2,
                    target = 100,
                    names = listOf("Nosotros", "Ellos")
                )
            }
        }
    }
}
