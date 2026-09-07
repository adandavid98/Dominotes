package com.example.data.model

enum class GameMode(val displayName: String, val defaultTeams: List<String>) {
    PAREJAS_2("Parejas (2 Equipos)", listOf("Nosotros", "Ellos")),
    INDIVIDUAL_2("Individual (2 Jugadores)", listOf("Jugador 1", "Jugador 2")),
    INDIVIDUAL_3("Individual (3 Jugadores)", listOf("Jugador 1", "Jugador 2", "Jugador 3")),
    INDIVIDUAL_4("Individual (4 Jugadores)", listOf("Jugador 1", "Jugador 2", "Jugador 3", "Jugador 4"))
}

enum class ScoringDisplayMode {
    RONDAS,          // Vista detallada de rondas
    ACUMULACION      // Vista rápida de suma acumulativa
}

enum class BonusTag(val label: String, val defaultBonusPoints: Int = 0) {
    NINGUNO("Normal", 0),
    CAPICUA("Capicúa", 0),
    TRANCA("Tranca / Cierre", 0),
    PASO("Paso", 0),
    CHUCHAZO("Chuchazo (Doble Blanco)", 0)
}

data class DominoTile(val top: Int, val bottom: Int) {
    val totalPips: Int get() = top + bottom
}
