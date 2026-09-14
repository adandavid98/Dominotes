package com.example.data.domino

import kotlin.random.Random

object DominoEngine {

    fun startNewMatch(
        humanPlayerName: String,
        botCount: Int = 3,
        targetScore: Int = 100,
        roomCode: String? = null,
        playMode: DominoGamePlayMode = if (botCount == 3) DominoGamePlayMode.PAREJAS_2V2 else DominoGamePlayMode.INDIVIDUAL
    ): DominoTableState {
        val totalPlayers = 1 + botCount
        val tilesPerPlayer = 7

        val isTeams = playMode == DominoGamePlayMode.PAREJAS_2V2 && totalPlayers == 4

        val players = mutableListOf<DominoPlayer>()
        // Player 0: Human -> Team 0 ("Nosotros")
        players.add(
            DominoPlayer(
                id = "human_player",
                name = humanPlayerName.ifBlank { "Tú" },
                isBot = false,
                avatarColorIndex = 0,
                teamId = 0
            )
        )

        // Bot 1: Carlos (Left opponent) -> Team 1
        // Bot 2: María (Partner across) -> Team 0 (Compañero/a)
        // Bot 3: Luis (Right opponent) -> Team 1
        val botNames = listOf("Carlos (Bot)", "María (Bot)", "Luis (Bot)")
        for (i in 1..botCount) {
            val botName = botNames.getOrElse(i - 1) { "Bot $i" }
            val teamId = if (isTeams) {
                if (i == 2) 0 else 1 // i=2 (index 2 in players: partner sitting across)
            } else {
                i
            }
            players.add(
                DominoPlayer(
                    id = "bot_$i",
                    name = botName,
                    isBot = true,
                    avatarColorIndex = i,
                    teamId = teamId
                )
            )
        }

        return dealRound(
            players = players,
            targetScore = targetScore,
            roomCode = roomCode,
            playMode = playMode,
            teamScores = listOf(0, 0)
        )
    }

    fun dealRound(
        players: List<DominoPlayer>,
        targetScore: Int,
        roomCode: String? = null,
        playMode: DominoGamePlayMode = DominoGamePlayMode.PAREJAS_2V2,
        teamScores: List<Int> = listOf(0, 0),
        starterPlayerIndex: Int? = null
    ): DominoTableState {
        val allTiles = DominoTile.createDoubleSixSet().shuffled(Random.Default)
        var deckIndex = 0
        val tilesPerPlayer = 7

        val updatedPlayers = players.mapIndexed { index, player ->
            val playerTiles = allTiles.subList(deckIndex, deckIndex + tilesPerPlayer)
            deckIndex += tilesPerPlayer
            player.copy(hand = playerTiles.sortedWith(compareByDescending<DominoTile> { it.isDouble }.thenByDescending { it.totalPoints }))
        }

        val boneyard = if (deckIndex < allTiles.size) {
            allTiles.subList(deckIndex, allTiles.size)
        } else {
            emptyList()
        }

        // Determine starting player:
        // If a winner from the previous round is specified (starterPlayerIndex), that player has the right
        // to open the hand first with ANY tile (whether double or not).
        // For the first hand of a match, the player with the highest double (or highest tile) opens.
        val starterIndex: Int
        val starterMsg: String

        if (starterPlayerIndex != null && starterPlayerIndex in updatedPlayers.indices) {
            starterIndex = starterPlayerIndex
            val startingPlayer = updatedPlayers[starterIndex]
            starterMsg = "${startingPlayer.name} ganó la mano anterior y abre la mesa con cualquier ficha"
        } else {
            var bestIdx = 0
            var bestDouble = -1
            for (i in updatedPlayers.indices) {
                val doubles = updatedPlayers[i].hand.filter { it.isDouble }.map { it.left }
                val maxD = doubles.maxOrNull() ?: -1
                if (maxD > bestDouble) {
                    bestDouble = maxD
                    bestIdx = i
                }
            }

            if (bestDouble == -1) {
                var highestPoints = -1
                for (i in updatedPlayers.indices) {
                    val maxP = updatedPlayers[i].hand.maxOfOrNull { it.totalPoints } ?: 0
                    if (maxP > highestPoints) {
                        highestPoints = maxP
                        bestIdx = i
                    }
                }
            }

            starterIndex = bestIdx
            val startingPlayer = updatedPlayers[starterIndex]
            starterMsg = if (bestDouble >= 0) {
                "${startingPlayer.name} abre la primera mano con el doble [$bestDouble|$bestDouble]"
            } else {
                "${startingPlayer.name} tiene la salida inicial"
            }
        }

        return DominoTableState(
            players = updatedPlayers,
            currentTurnIndex = starterIndex,
            boardTiles = emptyList(),
            boneyard = boneyard,
            leftEnd = null,
            rightEnd = null,
            status = TableGameStatus.PLAYING,
            targetScore = targetScore,
            lastActionLog = starterMsg,
            consecutivePasses = 0,
            winnerPlayerIndex = null,
            roundWinnerIndex = null,
            pointsWonThisRound = 0,
            isBlocked = false,
            roomCode = roomCode,
            playMode = playMode,
            teamScores = teamScores,
            targetPlayerCount = players.size,
            isWaitingForGuests = false
        )
    }

    fun getPlayablePlacements(tile: DominoTile, state: DominoTableState): List<TilePlacement> {
        if (state.boardTiles.isEmpty()) {
            return listOf(TilePlacement.LEFT) // First tile
        }
        val left = state.leftEnd ?: return emptyList()
        val right = state.rightEnd ?: return emptyList()

        val placements = mutableListOf<TilePlacement>()
        if (tile.canMatch(left)) {
            placements.add(TilePlacement.LEFT)
        }
        if (tile.canMatch(right)) {
            if (!placements.contains(TilePlacement.RIGHT)) {
                placements.add(TilePlacement.RIGHT)
            }
        }
        return placements
    }

    fun canPlayerPlay(player: DominoPlayer, state: DominoTableState): Boolean {
        if (state.boardTiles.isEmpty()) return player.hand.isNotEmpty()
        val left = state.leftEnd ?: return true
        val right = state.rightEnd ?: return true
        return player.hand.any { it.canMatch(left) || it.canMatch(right) }
    }

    fun playTile(
        state: DominoTableState,
        playerIndex: Int,
        tile: DominoTile,
        placement: TilePlacement
    ): DominoTableState {
        val player = state.players.getOrNull(playerIndex) ?: return state
        val updatedHand = player.hand.filterNot { it.id == tile.id }

        val newBoard = state.boardTiles.toMutableList()
        val newLeft: Int
        val newRight: Int
        val newInitialTileId: Int? = state.initialTileId ?: if (newBoard.isEmpty()) tile.id else newBoard.firstOrNull()?.id

        if (newBoard.isEmpty()) {
            newBoard.add(tile)
            newLeft = tile.left
            newRight = tile.right
        } else {
            val currentLeft = state.leftEnd!!
            val currentRight = state.rightEnd!!

            if (placement == TilePlacement.LEFT) {
                // Connecting to left end: the tile's right side must match currentLeft
                val oriented = if (tile.right == currentLeft) {
                    tile
                } else {
                    DominoTile(tile.right, tile.left, tile.id)
                }
                newBoard.add(0, oriented)
                newLeft = oriented.left
                newRight = currentRight
            } else {
                // Connecting to right end: the tile's left side must match currentRight
                val oriented = if (tile.left == currentRight) {
                    tile
                } else {
                    DominoTile(tile.right, tile.left, tile.id)
                }
                newBoard.add(oriented)
                newLeft = currentLeft
                newRight = oriented.right
            }
        }

        val updatedPlayer = player.copy(hand = updatedHand)
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[playerIndex] = updatedPlayer

        val log = if (state.boardTiles.isEmpty()) {
            "${player.name} abrió con la ficha [${tile.left}|${tile.right}]"
        } else {
            "${player.name} jugó la ficha [${tile.left}|${tile.right}]"
        }

        // Check if player won the round (emptied hand)
        if (updatedHand.isEmpty()) {
            val isTeams = state.playMode == DominoGamePlayMode.PAREJAS_2V2 && state.players.size == 4
            val winnerTeamId = updatedPlayer.teamId

            val pointsWon = if (isTeams) {
                // Sum remaining points of the opposing team (players with different teamId)
                updatedPlayers.filter { it.teamId != winnerTeamId }.sumOf { it.remainingTilePoints }
            } else {
                updatedPlayers.filterIndexed { idx, _ -> idx != playerIndex }.sumOf { it.remainingTilePoints }
            }

            val newScore = updatedPlayer.totalScore + pointsWon
            val newTeamScores = state.teamScores.toMutableList()
            if (winnerTeamId in newTeamScores.indices) {
                newTeamScores[winnerTeamId] = newTeamScores[winnerTeamId] + pointsWon
            }

            // In teams mode, both partners reflect team score or individual score
            if (isTeams) {
                for (i in updatedPlayers.indices) {
                    if (updatedPlayers[i].teamId == winnerTeamId) {
                        updatedPlayers[i] = updatedPlayers[i].copy(
                            totalScore = if (i == playerIndex) newScore else updatedPlayers[i].totalScore + pointsWon
                        )
                    }
                }
            } else {
                updatedPlayers[playerIndex] = updatedPlayer.copy(totalScore = newScore)
            }

            val isGameWon = if (isTeams) {
                (newTeamScores.getOrNull(winnerTeamId) ?: newScore) >= state.targetScore
            } else {
                newScore >= state.targetScore
            }

            val teamName = if (winnerTeamId == 0) "Tu Pareja" else "Pareja Rival"
            val isHumanWinner = !updatedPlayer.isBot
            val winMsg = if (isTeams) {
                if (isHumanWinner) {
                    "¡Ganaste tú! $teamName suma +$pointsWon pts"
                } else {
                    "¡${updatedPlayer.name} dominó! $teamName suma +$pointsWon pts"
                }
            } else {
                if (isHumanWinner) {
                    "¡Ganaste tú la ronda y sumaste +$pointsWon pts!"
                } else {
                    "¡${updatedPlayer.name} dominó la ronda y sumó +$pointsWon pts!"
                }
            }

            return state.copy(
                players = updatedPlayers,
                boardTiles = newBoard,
                leftEnd = newLeft,
                rightEnd = newRight,
                initialTileId = newInitialTileId,
                teamScores = newTeamScores,
                status = if (isGameWon) TableGameStatus.GAME_OVER else TableGameStatus.ROUND_OVER,
                winnerPlayerIndex = if (isGameWon) playerIndex else null,
                roundWinnerIndex = playerIndex,
                pointsWonThisRound = pointsWon,
                isBlocked = false,
                lastActionLog = winMsg,
                lastPlayedTile = tile,
                lastPlayedByPlayerName = player.name,
                lastPlayedByPlayerIndex = playerIndex
            )
        }

        val nextTurn = (playerIndex + 1) % state.players.size
        return state.copy(
            players = updatedPlayers,
            boardTiles = newBoard,
            leftEnd = newLeft,
            rightEnd = newRight,
            initialTileId = newInitialTileId,
            currentTurnIndex = nextTurn,
            consecutivePasses = 0,
            lastActionLog = log,
            lastPlayedTile = tile,
            lastPlayedByPlayerName = player.name,
            lastPlayedByPlayerIndex = playerIndex
        )
    }

    fun drawFromBoneyard(state: DominoTableState, playerIndex: Int): DominoTableState {
        if (state.boneyard.isEmpty()) return state
        val player = state.players[playerIndex]
        val drawn = state.boneyard.first()
        val remainingBoneyard = state.boneyard.drop(1)

        val updatedHand = player.hand + drawn
        val updatedPlayer = player.copy(
            hand = updatedHand.sortedWith(compareByDescending<DominoTile> { it.isDouble }.thenByDescending { it.totalPoints })
        )
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[playerIndex] = updatedPlayer

        return state.copy(
            players = updatedPlayers,
            boneyard = remainingBoneyard,
            lastActionLog = "${player.name} robó del pozo"
        )
    }

    fun passTurn(state: DominoTableState, playerIndex: Int): DominoTableState {
        val player = state.players[playerIndex]
        val newPasses = state.consecutivePasses + 1
        val updatedLog = "${player.name} pasó turno"

        // Check for Tranca / Blocked game
        if (newPasses >= state.players.size) {
            return resolveBlockedGame(state)
        }

        val nextTurn = (playerIndex + 1) % state.players.size
        return state.copy(
            currentTurnIndex = nextTurn,
            consecutivePasses = newPasses,
            lastActionLog = updatedLog
        )
    }

    private fun resolveBlockedGame(state: DominoTableState): DominoTableState {
        val isTeams = state.playMode == DominoGamePlayMode.PAREJAS_2V2 && state.players.size == 4

        // In domino teams: the winning side is either the individual with fewest points,
        // or the team with fewest total points (traditionally the individual player with fewest points wins for their team).
        val minPoints = state.players.minOf { it.remainingTilePoints }
        val winners = state.players.mapIndexedNotNull { idx, p ->
            if (p.remainingTilePoints == minPoints) idx else null
        }

        val roundWinnerIdx = winners.first()
        val winnerPlayer = state.players[roundWinnerIdx]
        val winnerTeamId = winnerPlayer.teamId

        val sumPointsWon = if (isTeams) {
            // In teams domino, points from the losing team are awarded to the winning team
            state.players.filter { it.teamId != winnerTeamId }.sumOf { it.remainingTilePoints }
        } else {
            state.players.filterIndexed { idx, _ -> idx != roundWinnerIdx }.sumOf { it.remainingTilePoints }
        }

        val updatedPlayers = state.players.toMutableList()
        val newScore = winnerPlayer.totalScore + sumPointsWon
        val newTeamScores = state.teamScores.toMutableList()
        if (winnerTeamId in newTeamScores.indices) {
            newTeamScores[winnerTeamId] = newTeamScores[winnerTeamId] + sumPointsWon
        }

        if (isTeams) {
            for (i in updatedPlayers.indices) {
                if (updatedPlayers[i].teamId == winnerTeamId) {
                    updatedPlayers[i] = updatedPlayers[i].copy(
                        totalScore = if (i == roundWinnerIdx) newScore else updatedPlayers[i].totalScore + sumPointsWon
                    )
                }
            }
        } else {
            updatedPlayers[roundWinnerIdx] = winnerPlayer.copy(totalScore = newScore)
        }

        val isGameWon = if (isTeams) {
            (newTeamScores.getOrNull(winnerTeamId) ?: newScore) >= state.targetScore
        } else {
            newScore >= state.targetScore
        }

        val teamLabel = if (winnerTeamId == 0) "Tu Pareja" else "Pareja Rival"
        val isHumanWinner = !winnerPlayer.isBot
        val logMsg = if (isTeams) {
            if (isHumanWinner) {
                "¡Tranca! Ganaste tú ($minPoints pts). $teamLabel suma +$sumPointsWon pts"
            } else {
                "¡Tranca! Ganó ${winnerPlayer.name} ($minPoints pts). $teamLabel suma +$sumPointsWon pts"
            }
        } else {
            if (isHumanWinner) {
                "¡Tranca! Ganaste tú con $minPoints pts (+ $sumPointsWon pts de mesa)"
            } else {
                "¡Tranca! Ganó ${winnerPlayer.name} con $minPoints pts (+ $sumPointsWon pts de mesa)"
            }
        }

        return state.copy(
            players = updatedPlayers,
            status = if (isGameWon) TableGameStatus.GAME_OVER else TableGameStatus.ROUND_OVER,
            winnerPlayerIndex = if (isGameWon) roundWinnerIdx else null,
            roundWinnerIndex = roundWinnerIdx,
            pointsWonThisRound = sumPointsWon,
            isBlocked = true,
            teamScores = newTeamScores,
            lastActionLog = logMsg
        )
    }

    fun computeBotMove(state: DominoTableState, botIndex: Int): BotDecision {
        val bot = state.players[botIndex]
        val validMoves = mutableListOf<Pair<DominoTile, TilePlacement>>()

        for (tile in bot.hand) {
            val placements = getPlayablePlacements(tile, state)
            for (p in placements) {
                validMoves.add(tile to p)
            }
        }

        if (validMoves.isNotEmpty()) {
            // Priority: Doubles first, then highest points
            val bestMove = validMoves.maxWithOrNull(
                compareBy<Pair<DominoTile, TilePlacement>> { it.first.isDouble }
                    .thenBy { it.first.totalPoints }
            )!!
            return BotDecision.Play(bestMove.first, bestMove.second)
        }

        // No moves: can draw from boneyard?
        if (state.boneyard.isNotEmpty()) {
            return BotDecision.Draw
        }

        // Must pass
        return BotDecision.Pass
    }

    sealed class BotDecision {
        data class Play(val tile: DominoTile, val placement: TilePlacement) : BotDecision()
        data object Draw : BotDecision()
        data object Pass : BotDecision()
    }
}
