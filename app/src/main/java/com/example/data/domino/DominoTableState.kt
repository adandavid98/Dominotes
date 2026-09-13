package com.example.data.domino

enum class TableGameStatus {
    WAITING_START,
    PLAYING,
    ROUND_OVER,
    GAME_OVER
}

enum class DominoGamePlayMode(val label: String, val isTeams: Boolean) {
    INDIVIDUAL("Individual", false),
    PAREJAS_2V2("En Parejas (2 vs 2)", true)
}

data class DominoPlayer(
    val id: String,
    val name: String,
    val isBot: Boolean,
    val hand: List<DominoTile> = emptyList(),
    val totalScore: Int = 0,
    val avatarColorIndex: Int = 0,
    val teamId: Int = 0 // 0 = Team Nosotros / Equipo 1, 1 = Team Rivales / Equipo 2
) {
    val remainingTilePoints: Int get() = hand.sumOf { it.totalPoints }
}

data class DominoTableState(
    val players: List<DominoPlayer> = emptyList(),
    val currentTurnIndex: Int = 0,
    val boardTiles: List<DominoTile> = emptyList(),
    val boneyard: List<DominoTile> = emptyList(), // Pozo / Dormilón
    val leftEnd: Int? = null,
    val rightEnd: Int? = null,
    val status: TableGameStatus = TableGameStatus.WAITING_START,
    val targetScore: Int = 100,
    val lastActionLog: String = "Partida lista para comenzar",
    val consecutivePasses: Int = 0,
    val winnerPlayerIndex: Int? = null,
    val roundWinnerIndex: Int? = null,
    val pointsWonThisRound: Int = 0,
    val isBlocked: Boolean = false,
    val roomCode: String? = null,
    val initialTileId: Int? = null,
    val playMode: DominoGamePlayMode = DominoGamePlayMode.PAREJAS_2V2,
    val teamScores: List<Int> = listOf(0, 0), // Team 0 and Team 1 scores
    val targetPlayerCount: Int = 4,
    val isWaitingForGuests: Boolean = false,
    val lastPlayedTile: DominoTile? = null,
    val lastPlayedByPlayerName: String? = null,
    val lastPlayedByPlayerIndex: Int? = null
)
