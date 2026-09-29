package com.example.data.puzzle

import com.example.data.domino.DominoTile
import com.example.data.domino.TilePlacement

data class DominoPuzzle(
    val id: String,
    val title: String,
    val difficulty: String, // Fácil, Medio, Difícil, Maestro
    val category: String, // Tranca, Capicúa, Deducción, Salida
    val scenarioDescription: String,
    val clueText: String,
    val boardLeftEnd: Int,
    val boardRightEnd: Int,
    val sampleBoardTiles: List<DominoTile>,
    val playerHand: List<DominoTile>,
    val winningTile: DominoTile,
    val winningPlacement: TilePlacement,
    val solutionExplanation: String,
    val isDailyChallenge: Boolean = false
)

object DominoPuzzleRepository {

    val allPuzzles: List<DominoPuzzle> = listOf(
        DominoPuzzle(
            id = "puzzle_daily_1",
            title = "Desafío Diario: La Tranca Dorada",
            difficulty = "Medio",
            category = "Tranca",
            scenarioDescription = "Quedan pocas fichas en la mesa. La punta izquierda tiene un [4] y la derecha un [2]. El rival de tu derecha no tiene cuatro y se sabe que tiene fichas altas. ¿Cuál ficha debes jugar para asegurar la tranca con el menor puntaje?",
            clueText = "Observa que si cierras con el [4|2], igualas ambas puntas a 2 y tu mano restante sumará apenas 3 puntos contra los 12 del rival.",
            boardLeftEnd = 4,
            boardRightEnd = 2,
            sampleBoardTiles = listOf(
                DominoTile(6, 4),
                DominoTile(6, 6),
                DominoTile(6, 5),
                DominoTile(5, 2)
            ),
            playerHand = listOf(
                DominoTile(4, 2),
                DominoTile(3, 0),
                DominoTile(5, 5)
            ),
            winningTile = DominoTile(4, 2),
            winningPlacement = TilePlacement.LEFT,
            solutionExplanation = "¡Jugada magistral! Al jugar el [4|2] en la punta izquierda, cierras el 4 y conviertes ambas cabezas en [2], trancando la partida de inmediato. Con tu [3|0] en mano (3 pts), ganas la tranca con ventaja aplastante.",
            isDailyChallenge = true
        ),
        DominoPuzzle(
            id = "puzzle_capicua_1",
            title = "La Capicúa Inevitable",
            difficulty = "Fácil",
            category = "Capicúa",
            scenarioDescription = "La mesa muestra un [3] a la izquierda y un [5] a la derecha. Tienes dos fichas en tu mano. Debes jugar la ficha que garantice dominar con Capicúa en tu próxima jugada.",
            clueText = "Busca dejar en una punta el mismo número que tu última ficha tiene en ambos extremos.",
            boardLeftEnd = 3,
            boardRightEnd = 5,
            sampleBoardTiles = listOf(
                DominoTile(1, 3),
                DominoTile(1, 1),
                DominoTile(1, 5)
            ),
            playerHand = listOf(
                DominoTile(5, 4),
                DominoTile(4, 3)
            ),
            winningTile = DominoTile(5, 4),
            winningPlacement = TilePlacement.RIGHT,
            solutionExplanation = "¡Perfecto! Al colocar [5|4] por la derecha, dejas los extremos en [3] y [4]. Tu última ficha es exactamente el [4|3], lo que te permite dominar por cualquiera de los dos lados (¡Capicúa!).",
            isDailyChallenge = false
        ),
        DominoPuzzle(
            id = "puzzle_ahogar_1",
            title = "Ahogar la Mula Rival",
            difficulty = "Difícil",
            category = "Deducción",
            scenarioDescription = "El contrincante lleva 3 turnos aguantando el [5|5] (la 'mula'). Los extremos son [6] y [1]. ¿Cómo lo obligas a pasar o a soltar su ficha en desventaja?",
            clueText = "Corta el juego del 1 para abrir la mesa a un número donde tu compañero tenga control.",
            boardLeftEnd = 6,
            boardRightEnd = 1,
            sampleBoardTiles = listOf(
                DominoTile(2, 6),
                DominoTile(2, 3),
                DominoTile(3, 1)
            ),
            playerHand = listOf(
                DominoTile(1, 6),
                DominoTile(6, 3),
                DominoTile(0, 0)
            ),
            winningTile = DominoTile(1, 6),
            winningPlacement = TilePlacement.RIGHT,
            solutionExplanation = "¡Estrategia pura! Jugando el [1|6] cierras el juego del 1 e igualas a [6] en ambas puntas. Como ya han salido cuatro 'seises', dejas al rival sin opciones y totalmente desarmado.",
            isDailyChallenge = false
        ),
        DominoPuzzle(
            id = "puzzle_salida_1",
            title = "El Jaque de Apertura",
            difficulty = "Medio",
            category = "Salida",
            scenarioDescription = "Partida empatada 90-90. Ganaste la mano anterior y te toca abrir la mesa. Tienes 4 fichas con el número [3] y la mula [3|3]. ¿Cuál es la apertura óptima para dominar el ritmo?",
            clueText = "Tener la mula del número que más dominas te asegura el control de las dos primeras vueltas.",
            boardLeftEnd = 0,
            boardRightEnd = 0,
            sampleBoardTiles = emptyList(),
            playerHand = listOf(
                DominoTile(3, 3),
                DominoTile(3, 5),
                DominoTile(3, 2),
                DominoTile(3, 0),
                DominoTile(6, 4)
            ),
            winningTile = DominoTile(3, 3),
            winningPlacement = TilePlacement.LEFT,
            solutionExplanation = "¡Correcto! Salir con la mula [3|3] teniendo 4 fichas de ese palo te da el dominio absoluto de la mesa y fuerza a tus rivales a jugar para ti.",
            isDailyChallenge = false
        )
    )

    fun getDailyPuzzle(): DominoPuzzle {
        return allPuzzles.firstOrNull { it.isDailyChallenge } ?: allPuzzles.first()
    }
}
