package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.AuthUser
import com.example.data.domino.DominoEngine
import com.example.data.domino.DominoGamePlayMode
import com.example.data.domino.DominoTableState
import com.example.data.domino.DominoTile
import com.example.data.domino.TableGameStatus
import com.example.data.domino.TilePlacement
import com.example.data.local.DominoDatabase
import com.example.data.local.MatchEntity
import com.example.data.local.RoundEntity
import com.example.data.model.BonusTag
import com.example.data.model.GameMode
import com.example.data.model.ScoringDisplayMode
import com.example.data.repository.DominoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainAppTab {
    SCORER,
    PLAY_DOMINO
}

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
    val showHistoryScreen: Boolean = false,
    val currentTab: MainAppTab = MainAppTab.SCORER,
    val showAuthDialog: Boolean = false,
    val showFriendsDialog: Boolean = false,
    val inTableLobby: Boolean = true
)

class DominoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DominoRepository
    val authRepository = AuthRepository(application)
    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser

    private val _gameState = MutableStateFlow(ActiveGameState())
    val gameState: StateFlow<ActiveGameState> = _gameState.asStateFlow()

    // Interactive Domino Game Table State
    private val _tableState = MutableStateFlow(
        DominoEngine.startNewMatch(
            humanPlayerName = authRepository.currentUser.value?.displayName ?: "Jugador",
            botCount = 3,
            targetScore = 100
        )
    )
    val tableState: StateFlow<DominoTableState> = _tableState.asStateFlow()

    private val _selectedTile = MutableStateFlow<DominoTile?>(null)
    val selectedTile: StateFlow<DominoTile?> = _selectedTile.asStateFlow()

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

    // Navigation & Dialog Controls
    fun setCurrentTab(tab: MainAppTab) {
        _gameState.update { it.copy(currentTab = tab) }
    }

    fun setShowAuthDialog(show: Boolean) {
        _gameState.update { it.copy(showAuthDialog = show) }
    }

    fun setShowFriendsDialog(show: Boolean) {
        _gameState.update { it.copy(showFriendsDialog = show) }
    }

    // Google Auth actions
    fun signInGoogle(email: String, displayName: String) {
        val uid = "google_" + email.hashCode()
        val user = AuthUser(uid = uid, email = email, displayName = displayName)
        authRepository.signIn(user)
        _gameState.update { it.copy(showAuthDialog = false) }

        // Update human player name in domino game
        startNewDominoTableGame(roomCode = _tableState.value.roomCode)
    }

    fun signOutGoogle() {
        authRepository.signOut()
        _gameState.update { it.copy(showAuthDialog = false) }
        startNewDominoTableGame(roomCode = null)
    }

    fun updatePlayerDisplayName(newName: String) {
        authRepository.updateDisplayName(newName)
        startNewDominoTableGame(roomCode = _tableState.value.roomCode)
    }

    // Domino Table Game Actions
    fun setSelectedTile(tile: DominoTile?) {
        _selectedTile.value = tile
    }

    fun setInTableLobby(inLobby: Boolean) {
        _gameState.update { it.copy(inTableLobby = inLobby) }
    }

    fun startNewDominoTableGame(
        roomCode: String? = null,
        botCount: Int = 3,
        targetScore: Int = 100,
        playMode: DominoGamePlayMode = if (botCount == 3) DominoGamePlayMode.PAREJAS_2V2 else DominoGamePlayMode.INDIVIDUAL
    ) {
        val humanName = authRepository.currentUser.value?.displayName ?: "Tú"
        _tableState.value = DominoEngine.startNewMatch(
            humanPlayerName = humanName,
            botCount = botCount,
            targetScore = targetScore,
            roomCode = roomCode,
            playMode = playMode
        )
        _selectedTile.value = null
        _gameState.update { it.copy(inTableLobby = false) }
        checkTriggerBotTurns()
    }

    fun playHumanTile(tile: DominoTile, placement: TilePlacement) {
        val currentState = _tableState.value
        val turnIdx = currentState.currentTurnIndex
        val player = currentState.players.getOrNull(turnIdx)
        if (player == null || player.isBot || currentState.status != TableGameStatus.PLAYING) return

        val newState = DominoEngine.playTile(currentState, turnIdx, tile, placement)
        _tableState.value = newState
        _selectedTile.value = null

        checkTriggerBotTurns()
    }

    fun drawHumanTile() {
        val currentState = _tableState.value
        val turnIdx = currentState.currentTurnIndex
        val player = currentState.players.getOrNull(turnIdx)
        if (player == null || player.isBot || currentState.status != TableGameStatus.PLAYING) return
        // Regla oficial de dominó: No se puede robar si ya tienes fichas que puedes jugar
        if (DominoEngine.canPlayerPlay(player, currentState)) return

        val newState = DominoEngine.drawFromBoneyard(currentState, turnIdx)
        _tableState.value = newState
        checkTriggerBotTurns()
    }

    fun passHumanTurn() {
        val currentState = _tableState.value
        val turnIdx = currentState.currentTurnIndex
        val player = currentState.players.getOrNull(turnIdx)
        if (player == null || player.isBot || currentState.status != TableGameStatus.PLAYING) return
        // Regla oficial de dominó: No se puede pasar si tienes fichas que puedes tirar en la mesa
        if (DominoEngine.canPlayerPlay(player, currentState)) return
        // Tampoco se puede pasar si aún quedan fichas por robar en el pozo
        if (currentState.boneyard.isNotEmpty()) return

        val newState = DominoEngine.passTurn(currentState, turnIdx)
        _tableState.value = newState
        _selectedTile.value = null
        checkTriggerBotTurns()
    }

    fun nextTableRound() {
        val current = _tableState.value
        if (current.status == TableGameStatus.GAME_OVER) {
            val botCount = (current.players.size - 1).coerceIn(1, 3)
            startNewDominoTableGame(
                roomCode = current.roomCode,
                botCount = botCount,
                targetScore = current.targetScore,
                playMode = current.playMode
            )
        } else {
            val nextStarter = current.roundWinnerIndex
            _tableState.value = DominoEngine.dealRound(
                players = current.players,
                targetScore = current.targetScore,
                roomCode = current.roomCode,
                playMode = current.playMode,
                teamScores = current.teamScores,
                starterPlayerIndex = nextStarter
            )
            _selectedTile.value = null
            checkTriggerBotTurns()
        }
    }

    private var botTurnJob: kotlinx.coroutines.Job? = null

    private fun checkTriggerBotTurns() {
        botTurnJob?.cancel()
        botTurnJob = viewModelScope.launch {
            while (_tableState.value.status == TableGameStatus.PLAYING &&
                _tableState.value.currentTurnIndex in _tableState.value.players.indices &&
                _tableState.value.players[_tableState.value.currentTurnIndex].isBot
            ) {
                val botIdx = _tableState.value.currentTurnIndex
                val bot = _tableState.value.players.getOrNull(botIdx)
                if (bot != null) {
                    _tableState.update { current ->
                        current.copy(lastActionLog = "${bot.name} está pensando...")
                    }
                }
                delay(3000L) // Al menos 3 segundos para que los bots pongan las fichas y se aprecie mejor la jugada

                if (_tableState.value.status != TableGameStatus.PLAYING ||
                    _tableState.value.currentTurnIndex != botIdx
                ) {
                    break
                }

                val decision = DominoEngine.computeBotMove(_tableState.value, botIdx)
                when (decision) {
                    is DominoEngine.BotDecision.Play -> {
                        _tableState.value = DominoEngine.playTile(
                            _tableState.value,
                            botIdx,
                            decision.tile,
                            decision.placement
                        )
                        // Pausa de 2.5s para que se aprecie la ficha jugada y el mensaje con la ficha colocada
                        delay(2500L)
                    }
                    is DominoEngine.BotDecision.Draw -> {
                        _tableState.value = DominoEngine.drawFromBoneyard(_tableState.value, botIdx)
                        delay(1800L)
                    }
                    is DominoEngine.BotDecision.Pass -> {
                        _tableState.value = DominoEngine.passTurn(_tableState.value, botIdx)
                        delay(1800L)
                    }
                }
            }
        }
    }

    fun createFriendsRoom(
        code: String,
        playerCount: Int = 4,
        playMode: DominoGamePlayMode = if (playerCount == 4) DominoGamePlayMode.PAREJAS_2V2 else DominoGamePlayMode.INDIVIDUAL
    ) {
        val hostName = authRepository.currentUser.value?.displayName ?: "Tú"
        val hostPlayer = com.example.data.domino.DominoPlayer(
            id = "host_player",
            name = "$hostName (Anfitrión)",
            isBot = false,
            avatarColorIndex = 0,
            teamId = 0
        )

        _tableState.value = DominoTableState(
            players = listOf(hostPlayer),
            currentTurnIndex = 0,
            boardTiles = emptyList(),
            boneyard = emptyList(),
            status = TableGameStatus.WAITING_START,
            targetScore = 100,
            lastActionLog = "Sala $code creada. Esperando a que entren los participantes (1/$playerCount) con el código.",
            roomCode = code.trim().uppercase(),
            playMode = playMode,
            teamScores = listOf(0, 0),
            targetPlayerCount = playerCount.coerceIn(2, 4),
            isWaitingForGuests = true
        )
        _selectedTile.value = null
        _gameState.update {
            it.copy(
                inTableLobby = false,
                showFriendsDialog = false
            )
        }
    }

    fun joinFriendsRoom(code: String, guestName: String? = null) {
        val trimmedCode = code.trim().uppercase()
        val current = _tableState.value

        // If joining the active waiting room on this device
        if (current.roomCode?.equals(trimmedCode, ignoreCase = true) == true && current.status == TableGameStatus.WAITING_START) {
            if (current.players.size < current.targetPlayerCount) {
                val nextIndex = current.players.size
                val defaultName = guestName?.ifBlank { null } ?: "Invitado ${nextIndex + 1}"
                val teamId = if (current.playMode.isTeams && current.targetPlayerCount == 4) {
                    if (nextIndex == 2) 0 else 1
                } else {
                    nextIndex
                }
                val newPlayer = com.example.data.domino.DominoPlayer(
                    id = "guest_${System.currentTimeMillis()}_$nextIndex",
                    name = defaultName,
                    isBot = false,
                    avatarColorIndex = nextIndex % 4,
                    teamId = teamId
                )
                val updatedPlayers = current.players + newPlayer
                val isFull = updatedPlayers.size >= current.targetPlayerCount
                val log = if (isFull) {
                    "¡$defaultName entró con el código! Todos los participantes listos (${updatedPlayers.size}/${current.targetPlayerCount})."
                } else {
                    "$defaultName entró con el código. Conectados: ${updatedPlayers.size}/${current.targetPlayerCount}."
                }

                _tableState.value = current.copy(
                    players = updatedPlayers,
                    lastActionLog = log
                )
            }
        } else {
            // Join a new room by code
            val myName = guestName?.ifBlank { null } ?: (authRepository.currentUser.value?.displayName ?: "Invitado 1")
            val hostPlayer = com.example.data.domino.DominoPlayer(
                id = "guest_${System.currentTimeMillis()}",
                name = myName,
                isBot = false,
                avatarColorIndex = 0,
                teamId = 0
            )
            _tableState.value = DominoTableState(
                players = listOf(hostPlayer),
                currentTurnIndex = 0,
                boardTiles = emptyList(),
                boneyard = emptyList(),
                status = TableGameStatus.WAITING_START,
                targetScore = 100,
                lastActionLog = "Te has unido a la sala $trimmedCode. Esperando al resto de participantes...",
                roomCode = trimmedCode,
                playMode = DominoGamePlayMode.PAREJAS_2V2,
                teamScores = listOf(0, 0),
                targetPlayerCount = 4,
                isWaitingForGuests = true
            )
        }

        _gameState.update {
            it.copy(
                inTableLobby = false,
                showFriendsDialog = false
            )
        }
    }

    fun addGuestWithCode(name: String) {
        val current = _tableState.value
        if (current.status != TableGameStatus.WAITING_START || current.players.size >= current.targetPlayerCount) return

        val nextIndex = current.players.size
        val guestName = name.ifBlank { "Invitado ${nextIndex + 1}" }
        val teamId = if (current.playMode.isTeams && current.targetPlayerCount == 4) {
            if (nextIndex == 2) 0 else 1
        } else {
            nextIndex
        }

        val newPlayer = com.example.data.domino.DominoPlayer(
            id = "guest_${System.currentTimeMillis()}_$nextIndex",
            name = guestName,
            isBot = false,
            avatarColorIndex = nextIndex % 4,
            teamId = teamId
        )
        val updatedPlayers = current.players + newPlayer
        val isFull = updatedPlayers.size >= current.targetPlayerCount
        val log = if (isFull) {
            "¡$guestName se unió con el código! Todos los participantes (${updatedPlayers.size}/${current.targetPlayerCount}) listos para iniciar."
        } else {
            "$guestName se unió con el código ${current.roomCode}. Esperando a los demás (${updatedPlayers.size}/${current.targetPlayerCount})."
        }

        _tableState.value = current.copy(
            players = updatedPlayers,
            lastActionLog = log
        )
    }

    fun removePlayerFromWaitingRoom(playerIndex: Int) {
        val current = _tableState.value
        if (current.status != TableGameStatus.WAITING_START || playerIndex <= 0 || playerIndex >= current.players.size) return
        val removed = current.players[playerIndex]
        val updatedPlayers = current.players.filterIndexed { index, _ -> index != playerIndex }
        _tableState.value = current.copy(
            players = updatedPlayers,
            lastActionLog = "${removed.name} salió de la sala (${updatedPlayers.size}/${current.targetPlayerCount})."
        )
    }

    fun startWaitingRoomGame() {
        val current = _tableState.value
        if (current.players.size < current.targetPlayerCount) return

        // Configure player teams according to playMode and total players
        val configuredPlayers = current.players.mapIndexed { idx, player ->
            val teamId = if (current.playMode.isTeams && current.targetPlayerCount == 4) {
                if (idx == 0 || idx == 2) 0 else 1
            } else {
                idx
            }
            player.copy(teamId = teamId)
        }

        val startedState = DominoEngine.dealRound(
            players = configuredPlayers,
            targetScore = current.targetScore,
            roomCode = current.roomCode,
            playMode = current.playMode,
            teamScores = listOf(0, 0)
        )

        _tableState.value = startedState.copy(
            targetPlayerCount = current.targetPlayerCount,
            isWaitingForGuests = false
        )
        _selectedTile.value = null
        checkTriggerBotTurns()
    }

    fun fillRemainingSlotsWithBotsAndStart() {
        val current = _tableState.value
        val needed = current.targetPlayerCount - current.players.size
        if (needed <= 0) {
            startWaitingRoomGame()
            return
        }

        val botNames = listOf("Carlos (Bot)", "María (Bot)", "Luis (Bot)")
        val mutablePlayers = current.players.toMutableList()
        val isTeams = current.playMode.isTeams && current.targetPlayerCount == 4

        for (i in 0 until needed) {
            val playerIndex = mutablePlayers.size
            val teamId = if (isTeams) {
                if (playerIndex == 2) 0 else 1
            } else {
                playerIndex
            }
            val botName = botNames.getOrElse(i) { "Bot ${i + 1}" }
            mutablePlayers.add(
                com.example.data.domino.DominoPlayer(
                    id = "bot_${System.currentTimeMillis()}_$i",
                    name = botName,
                    isBot = true,
                    avatarColorIndex = playerIndex % 4,
                    teamId = teamId
                )
            )
        }

        val configuredPlayers = mutablePlayers.mapIndexed { idx, player ->
            val teamId = if (isTeams) {
                if (idx == 0 || idx == 2) 0 else 1
            } else {
                idx
            }
            player.copy(teamId = teamId)
        }

        val startedState = DominoEngine.dealRound(
            players = configuredPlayers,
            targetScore = current.targetScore,
            roomCode = current.roomCode,
            playMode = current.playMode,
            teamScores = listOf(0, 0)
        )

        _tableState.value = startedState.copy(
            targetPlayerCount = current.targetPlayerCount,
            isWaitingForGuests = false
        )
        _selectedTile.value = null
        checkTriggerBotTurns()
    }

    fun cancelWaitingRoom() {
        _tableState.value = _tableState.value.copy(
            status = TableGameStatus.GAME_OVER,
            isWaitingForGuests = false
        )
        _gameState.update { it.copy(inTableLobby = true) }
    }
}
