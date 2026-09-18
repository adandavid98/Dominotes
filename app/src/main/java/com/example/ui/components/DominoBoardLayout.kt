package com.example.ui.components

import com.example.data.domino.DominoTile

/**
 * Representa una ficha colocada en coordenadas cartesianas (dp).
 */
data class PlacedBoardTile(
    val tile: DominoTile,
    val index: Int,
    val x: Float, // en dp
    val y: Float, // en dp
    val width: Float, // en dp
    val height: Float, // en dp
    val isVertical: Boolean,
    val topOrLeftPips: Int,
    val bottomOrRightPips: Int
)

/**
 * Bounding Box rectangular con dimensiones totales y límites calculados.
 */
data class DominoBoardBoundingBox(
    val minX: Float,
    val maxX: Float,
    val minY: Float,
    val maxY: Float,
    val width: Float,
    val height: Float
)

/**
 * Resultado completo del layout y encuadre del tablero de dominó.
 */
data class DominoBoardLayout(
    val tiles: List<PlacedBoardTile>,
    val boundingWidth: Float,
    val boundingHeight: Float,
    val boundingBox: DominoBoardBoundingBox = DominoBoardBoundingBox(0f, boundingWidth, 0f, boundingHeight, boundingWidth, boundingHeight)
)

/**
 * Dirección cardinal de propagación del tren de fichas.
 */
enum class BoardDirection {
    RIGHT,
    DOWN,
    LEFT,
    UP;

    fun opposite(): BoardDirection = when (this) {
        RIGHT -> LEFT
        LEFT -> RIGHT
        DOWN -> UP
        UP -> DOWN
    }
}

/**
 * Parámetros de transformación de la cámara 2D para centrar y escalar el tablero.
 */
data class DominoCameraTransform(
    val scaleFactor: Float,
    val offsetX: Float,
    val offsetY: Float,
    val contentWidth: Float,
    val contentHeight: Float
)

/**
 * SISTEMA DE CÁMARA DINÁMICA / ZOOM AUTOMÁTICO (Bounding Box Fitting).
 *
 * Ajusta de forma proporcional y uniforme el tablero completo dentro de la mesa verde
 * garantizando que ninguna ficha se salga de los bordes bajo ninguna resolución o pantalla.
 */
fun calculateBoundingBoxFittingCamera(
    contentWidth: Float,
    contentHeight: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    paddingDp: Float = 16f,
    minScale: Float = 0.15f,
    maxScale: Float = 1.0f
): DominoCameraTransform {
    val usableWidth = (viewportWidth - (paddingDp * 2f)).coerceAtLeast(40f)
    val usableHeight = (viewportHeight - (paddingDp * 2f)).coerceAtLeast(40f)

    val safeContentW = contentWidth.coerceAtLeast(1f)
    val safeContentH = contentHeight.coerceAtLeast(1f)

    // Solo se reduce la escala (zoom out) si el contenido supera el espacio útil de la mesa.
    // Si cabe holgadamente, se mantiene en 1.0f (escala natural) sin alejarse innecesariamente.
    val rawScale = minOf(
        usableWidth / safeContentW,
        usableHeight / safeContentH,
        1.0f
    )

    val scaleFactor = rawScale.coerceIn(minScale, maxScale)
    val scaledContentW = safeContentW * scaleFactor
    val scaledContentH = safeContentH * scaleFactor

    val offsetX = (viewportWidth - scaledContentW) / 2f
    val offsetY = (viewportHeight - scaledContentH) / 2f

    return DominoCameraTransform(
        scaleFactor = scaleFactor,
        offsetX = offsetX,
        offsetY = offsetY,
        contentWidth = safeContentW,
        contentHeight = safeContentH
    )
}

/**
 * Calcula con precisión qué puntos (pips) van en cada mitad de la ficha según la dirección
 * y el extremo del tren, garantizando que los extremos conectados coincidan SIEMPRE en número.
 */
fun calculateTilePips(
    tile: DominoTile,
    isLeftBranch: Boolean,
    currentDirection: BoardDirection
): Pair<Int, Int> {
    if (tile.isDouble) {
        return Pair(tile.left, tile.right)
    }

    // En DominoEngine:
    // Rama Derecha (Head, isLeftBranch == false):
    //   tile.left es la cara interior que toca a la ficha previa.
    //   tile.right es la cara exterior que avanza hacia el extremo libre.
    // Rama Izquierda (Tail, isLeftBranch == true):
    //   tile.right es la cara interior que toca a la ficha previa.
    //   tile.left es la cara exterior que avanza hacia el extremo libre.
    return if (!isLeftBranch) {
        when (currentDirection) {
            BoardDirection.RIGHT -> Pair(tile.left, tile.right) // Izq toca previa, Der avanza
            BoardDirection.DOWN  -> Pair(tile.left, tile.right) // Arriba toca previa, Abajo avanza
            BoardDirection.LEFT  -> Pair(tile.right, tile.left) // Der toca previa, Izq avanza
            BoardDirection.UP    -> Pair(tile.right, tile.left) // Abajo toca previa, Arriba avanza
        }
    } else {
        when (currentDirection) {
            BoardDirection.LEFT  -> Pair(tile.left, tile.right) // Der toca previa (right=tile.right), Izq avanza (left=tile.left)
            BoardDirection.UP    -> Pair(tile.left, tile.right) // Abajo toca previa (bottom=tile.right), Arriba avanza (top=tile.left)
            BoardDirection.RIGHT -> Pair(tile.right, tile.left) // Izq toca previa (left=tile.right), Der avanza (right=tile.left)
            BoardDirection.DOWN  -> Pair(tile.right, tile.left) // Arriba toca previa (top=tile.right), Abajo avanza (bottom=tile.left)
        }
    }
}

/**
 * Gestor de Extremo de Tren de Dominó (Branch).
 *
 * Mantiene la continuidad física directa (cero huecos), orienta los dobles perpendiculares
 * y realiza giros ortogonales en L perfectos evitando colisiones y retrocesos.
 */
private class DominoSnakeBranch(
    private val wShort: Float,
    private val wLong: Float,
    private val maxTilesInRow: Int,
    private val maxTilesInColumn: Int,
    initialDirection: BoardDirection,
    private val isLeftBranch: Boolean,
    initialTile: PlacedBoardTile
) {
    var currentDirection: BoardDirection = initialDirection
        private set

    var lastPlaced: PlacedBoardTile = initialTile
        private set

    var countInCurrentSegment: Int = 0

    /**
     * Secuencia estricta de giros solicitada para el tablero:
     * - Rama Izquierda (Tail):
     *     Segmento 0: Avanza horizontal hacia LEFT
     *     Giro 1: Gira 90° hacia UP (vertical arriba)
     *     Giro 2: Gira 90° hacia RIGHT (horizontal derecha y continúa horizontal)
     * - Rama Derecha (Head):
     *     Segmento 0: Avanza horizontal hacia RIGHT
     *     Giro 1: Gira 90° hacia DOWN (vertical abajo)
     *     Giro 2: Gira 90° hacia LEFT (horizontal izquierda y continúa horizontal)
     */
    fun getNextDirection(currentDir: BoardDirection): BoardDirection {
        return if (isLeftBranch) {
            when (currentDir) {
                BoardDirection.LEFT -> BoardDirection.UP
                BoardDirection.UP -> BoardDirection.RIGHT
                BoardDirection.RIGHT -> BoardDirection.RIGHT // Una vez que gira a RIGHT continúa horizontal hacia la derecha
                BoardDirection.DOWN -> BoardDirection.LEFT
            }
        } else {
            when (currentDir) {
                BoardDirection.RIGHT -> BoardDirection.DOWN
                BoardDirection.DOWN -> BoardDirection.LEFT
                BoardDirection.LEFT -> BoardDirection.LEFT // Una vez que gira a LEFT continúa horizontal hacia la izquierda
                BoardDirection.UP -> BoardDirection.RIGHT
            }
        }
    }

    /**
     * Coloca la siguiente ficha en contacto físico directo con la ficha anterior (lastPlaced).
     */
    fun placeNextTile(tile: DominoTile, index: Int): PlacedBoardTile {
        val isHorizontal = (currentDirection == BoardDirection.RIGHT || currentDirection == BoardDirection.LEFT)
        val limitForSegment = if (isHorizontal) maxTilesInRow else maxTilesInColumn

        var isTurning = false
        val oldDir = currentDirection

        if (countInCurrentSegment >= limitForSegment) {
            val nextDir = getNextDirection(currentDirection)
            if (nextDir != currentDirection) {
                isTurning = true
                currentDirection = nextDir
                countInCurrentSegment = 0
            }
        }

        // Fichas dobles: perpendiculares al avance. Fichas normales: paralelas al avance.
        val placeVertical = if (tile.isDouble) {
            (currentDirection == BoardDirection.RIGHT || currentDirection == BoardDirection.LEFT)
        } else {
            (currentDirection == BoardDirection.DOWN || currentDirection == BoardDirection.UP)
        }

        val tileW = if (placeVertical) wShort else wLong
        val tileH = if (placeVertical) wLong else wShort

        val tileX: Float
        val tileY: Float

        if (!isTurning) {
            // Avance recto continuo
            when (currentDirection) {
                BoardDirection.RIGHT -> {
                    tileX = lastPlaced.x + lastPlaced.width
                    val lineCenterY = lastPlaced.y + lastPlaced.height / 2f
                    tileY = lineCenterY - tileH / 2f
                }
                BoardDirection.LEFT -> {
                    tileX = lastPlaced.x - tileW
                    val lineCenterY = lastPlaced.y + lastPlaced.height / 2f
                    tileY = lineCenterY - tileH / 2f
                }
                BoardDirection.DOWN -> {
                    val lineCenterX = lastPlaced.x + lastPlaced.width / 2f
                    tileX = lineCenterX - tileW / 2f
                    tileY = lastPlaced.y + lastPlaced.height
                }
                BoardDirection.UP -> {
                    val lineCenterX = lastPlaced.x + lastPlaced.width / 2f
                    tileX = lineCenterX - tileW / 2f
                    tileY = lastPlaced.y - tileH
                }
            }
        } else {
            // Giro ortogonal en L de 90°: conexión exacta borde con borde sin solapamiento ni desalineación.
            // Para giros a horizontal (RIGHT / LEFT):
            //   - UP -> RIGHT: la ficha horizontal se apoya inmediatamente arriba o al tope de la vertical previa (tileX = lastPlaced.x + lastPlaced.width, tileY = lastPlaced.y).
            //   - UP -> LEFT: la ficha horizontal sale a la izquierda al tope de la vertical previa (tileX = lastPlaced.x - tileW, tileY = lastPlaced.y).
            //   - DOWN -> RIGHT: la ficha horizontal sale a la derecha en la base inferior de la vertical previa (tileX = lastPlaced.x + lastPlaced.width, tileY = (lastPlaced.y + lastPlaced.height) - tileH).
            //   - DOWN -> LEFT: la ficha horizontal sale a la izquierda en la base inferior de la vertical previa (tileX = lastPlaced.x - tileW, tileY = (lastPlaced.y + lastPlaced.height) - tileH).
            // Para giros a vertical (UP / DOWN):
            //   - LEFT -> UP: la ficha vertical va arriba (tileY = lastPlaced.y - tileH). Su borde izquierdo se alinea con la ficha previa (tileX = lastPlaced.x).
            //   - RIGHT -> UP: la ficha vertical va arriba (tileY = lastPlaced.y - tileH). Su borde derecho se alinea con la ficha previa (tileX = (lastPlaced.x + lastPlaced.width) - tileW).
            //   - LEFT -> DOWN: la ficha vertical va abajo (tileY = lastPlaced.y + lastPlaced.height). Su borde izquierdo se alinea con la ficha previa (tileX = lastPlaced.x).
            //   - RIGHT -> DOWN: la ficha vertical va abajo (tileY = lastPlaced.y + lastPlaced.height). Su borde derecho se alinea con la ficha previa (tileX = (lastPlaced.x + lastPlaced.width) - tileW).
            when {
                oldDir == BoardDirection.RIGHT && currentDirection == BoardDirection.DOWN -> {
                    tileX = (lastPlaced.x + lastPlaced.width) - tileW
                    tileY = lastPlaced.y + lastPlaced.height
                }
                oldDir == BoardDirection.DOWN && currentDirection == BoardDirection.LEFT -> {
                    tileX = lastPlaced.x - tileW
                    tileY = (lastPlaced.y + lastPlaced.height) - tileH
                }
                oldDir == BoardDirection.LEFT && currentDirection == BoardDirection.DOWN -> {
                    tileX = lastPlaced.x
                    tileY = lastPlaced.y + lastPlaced.height
                }
                oldDir == BoardDirection.DOWN && currentDirection == BoardDirection.RIGHT -> {
                    tileX = lastPlaced.x + lastPlaced.width
                    tileY = (lastPlaced.y + lastPlaced.height) - tileH
                }
                oldDir == BoardDirection.LEFT && currentDirection == BoardDirection.UP -> {
                    tileX = lastPlaced.x
                    tileY = lastPlaced.y - tileH
                }
                oldDir == BoardDirection.UP && currentDirection == BoardDirection.RIGHT -> {
                    tileX = lastPlaced.x + lastPlaced.width
                    tileY = lastPlaced.y
                }
                oldDir == BoardDirection.RIGHT && currentDirection == BoardDirection.UP -> {
                    tileX = (lastPlaced.x + lastPlaced.width) - tileW
                    tileY = lastPlaced.y - tileH
                }
                oldDir == BoardDirection.UP && currentDirection == BoardDirection.LEFT -> {
                    tileX = lastPlaced.x - tileW
                    tileY = lastPlaced.y
                }
                else -> {
                    tileX = lastPlaced.x + lastPlaced.width
                    tileY = lastPlaced.y
                }
            }
        }

        countInCurrentSegment++

        val (pips1, pips2) = calculateTilePips(tile, isLeftBranch, currentDirection)

        val newPlaced = PlacedBoardTile(
            tile = tile,
            index = index,
            x = tileX,
            y = tileY,
            width = tileW,
            height = tileH,
            isVertical = placeVertical,
            topOrLeftPips = pips1,
            bottomOrRightPips = pips2
        )

        lastPlaced = newPlaced
        return newPlaced
    }
}

/**
 * SISTEMA ALGORÍTMICO: Colocación de fichas de Dominó en Serpiente (Snaking / Turns).
 */
fun calculateDominoSnakeLayout(
    boardTiles: List<DominoTile>,
    initialTileId: Int? = null,
    baseUnit: Float = 24f, // dp: ancho corto de ficha
    maxTilesInRow: Int = 4,
    maxTilesInColumn: Int = 3
): DominoBoardLayout {
    if (boardTiles.isEmpty()) {
        return DominoBoardLayout(
            tiles = emptyList(),
            boundingWidth = 0f,
            boundingHeight = 0f,
            boundingBox = DominoBoardBoundingBox(0f, 0f, 0f, 0f, 0f, 0f)
        )
    }

    val s = baseUnit
    val wShort = s      // e.g. 24dp
    val wLong = 2f * s  // e.g. 48dp

    val openerIndex = if (initialTileId != null) {
        boardTiles.indexOfFirst { it.id == initialTileId }.takeIf { it >= 0 } ?: (boardTiles.size / 2)
    } else {
        boardTiles.size / 2
    }

    val placed = mutableListOf<PlacedBoardTile>()

    // 1. COLOCAR FICHA INICIAL / ANCLA EN (0, 0)
    val opener = boardTiles[openerIndex]
    val openerIsVertical = opener.isDouble
    val openerWidth = if (openerIsVertical) wShort else wLong
    val openerHeight = if (openerIsVertical) wLong else wShort
    val openerY = if (openerIsVertical) -(wLong - wShort) / 2f else 0f

    val initialPlaced = PlacedBoardTile(
        tile = opener,
        index = openerIndex,
        x = 0f,
        y = openerY,
        width = openerWidth,
        height = openerHeight,
        isVertical = openerIsVertical,
        topOrLeftPips = opener.left,
        bottomOrRightPips = opener.right
    )
    placed.add(initialPlaced)

    // 2. RAMA DERECHA (HEAD): Desde (openerIndex + 1) hasta el final de boardTiles
    if (openerIndex + 1 < boardTiles.size) {
        val headBranch = DominoSnakeBranch(
            wShort = wShort,
            wLong = wLong,
            maxTilesInRow = maxTilesInRow,
            maxTilesInColumn = maxTilesInColumn,
            initialDirection = BoardDirection.RIGHT,
            isLeftBranch = false,
            initialTile = initialPlaced
        )

        for (idx in (openerIndex + 1) until boardTiles.size) {
            val tile = boardTiles[idx]
            placed.add(headBranch.placeNextTile(tile, idx))
        }
    }

    // 3. RAMA IZQUIERDA (TAIL): Desde (openerIndex - 1) decreciendo hasta 0
    if (openerIndex > 0) {
        val tailBranch = DominoSnakeBranch(
            wShort = wShort,
            wLong = wLong,
            maxTilesInRow = maxTilesInRow,
            maxTilesInColumn = maxTilesInColumn,
            initialDirection = BoardDirection.LEFT,
            isLeftBranch = true,
            initialTile = initialPlaced
        )

        for (idx in (openerIndex - 1) downTo 0) {
            val tile = boardTiles[idx]
            placed.add(tailBranch.placeNextTile(tile, idx))
        }
    }

    // 4. CÁLCULO DE BOUNDING BOX Y NORMALIZACIÓN A (0, 0)
    val minX = placed.minOfOrNull { it.x } ?: 0f
    val minY = placed.minOfOrNull { it.y } ?: 0f
    val maxX = placed.maxOfOrNull { it.x + it.width } ?: 0f
    val maxY = placed.maxOfOrNull { it.y + it.height } ?: 0f

    val normalized = placed.map {
        it.copy(x = it.x - minX, y = it.y - minY)
    }

    val boundingWidth = (maxX - minX).coerceAtLeast(wLong)
    val boundingHeight = (maxY - minY).coerceAtLeast(wLong)

    val bbox = DominoBoardBoundingBox(
        minX = 0f,
        maxX = boundingWidth,
        minY = 0f,
        maxY = boundingHeight,
        width = boundingWidth,
        height = boundingHeight
    )

    return DominoBoardLayout(
        tiles = normalized,
        boundingWidth = boundingWidth,
        boundingHeight = boundingHeight,
        boundingBox = bbox
    )
}
