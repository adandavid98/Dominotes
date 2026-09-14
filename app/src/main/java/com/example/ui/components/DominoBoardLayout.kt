package com.example.ui.components

import com.example.data.domino.DominoTile

data class PlacedBoardTile(
    val tile: DominoTile,
    val index: Int,
    val x: Float, // in dp
    val y: Float, // in dp
    val width: Float, // in dp
    val height: Float, // in dp
    val isVertical: Boolean,
    val topOrLeftPips: Int,
    val bottomOrRightPips: Int
)

data class DominoBoardLayout(
    val tiles: List<PlacedBoardTile>,
    val boundingWidth: Float,
    val boundingHeight: Float
)

/**
 * Calculates a fixed-anchor domino board layout matching authentic domino play:
 * 1. The opener tile stays fixed as the center anchor.
 * 2. Tiles already placed NEVER move or shift relative to each other.
 * 3. Right branch:
 *    - Grows HORIZONTALLY to the right.
 *    - When turning 90° DOWN, continues going DOWN as a vertical column (non-doubles vertical, doubles horizontal).
 *    - If exceptionally long, turns left at the bottom.
 * 4. Left branch:
 *    - Grows HORIZONTALLY to the left.
 *    - When turning 90° UP, continues going UP as a vertical column (non-doubles vertical, doubles horizontal).
 *    - If exceptionally long, turns right at the top.
 * 5. Tiles connect with ZERO GAP at joints without overlapping parallel rows.
 */
fun calculateDominoSnakeLayout(
    boardTiles: List<DominoTile>,
    initialTileId: Int? = null,
    baseUnit: Float = 22f, // dp
    maxTilesInRow: Int = 3,
    maxTilesInColumn: Int = 3
): DominoBoardLayout {
    if (boardTiles.isEmpty()) {
        return DominoBoardLayout(emptyList(), 0f, 0f)
    }

    val s = baseUnit
    val wShort = s      // e.g. 22dp
    val wLong = 2f * s  // e.g. 44dp

    val openerIndex = if (initialTileId != null) {
        boardTiles.indexOfFirst { it.id == initialTileId }.takeIf { it >= 0 } ?: (boardTiles.size / 2)
    } else {
        boardTiles.size / 2
    }

    val placed = mutableListOf<PlacedBoardTile>()

    // 1. PLACE ANCHOR (OPENER TILE) AT (0, 0)
    val opener = boardTiles[openerIndex]
    val openerIsVertical = opener.isDouble
    val openerWidth = if (openerIsVertical) wShort else wLong
    val openerHeight = if (openerIsVertical) wLong else wShort
    val openerY = if (openerIsVertical) -(wLong - wShort) / 2f else 0f

    placed.add(
        PlacedBoardTile(
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
    )

    // 2. RIGHT BRANCH: From openerIndex + 1 to boardTiles.size - 1
    // Phase 0: HORIZONTAL RIGHT -> Phase 1: VERTICAL DOWN -> Phase 2: HORIZONTAL LEFT
    if (openerIndex + 1 < boardTiles.size) {
        var curX = openerWidth
        var curY = 0f
        var phase = 0 // 0: RIGHT, 1: DOWN, 2: LEFT
        var countInPhase = 0
        var colX = 0f
        var colBottomY = 0f
        var lastTileWasDouble = openerIsVertical

        for (idx in (openerIndex + 1) until boardTiles.size) {
            val tile = boardTiles[idx]

            if (phase == 0 && countInPhase >= maxTilesInRow) {
                phase = 1
                countInPhase = 0
                // Connect directly under the right end of the last horizontal tile,
                // accounting for crosswise vertical extent if the corner tile is a double.
                colX = curX - wShort
                colBottomY = if (lastTileWasDouble) wShort + (wLong - wShort) / 2f else wShort
            }

            when (phase) {
                0 -> {
                    // Moving RIGHT
                    if (tile.isDouble) {
                        val tw = wShort
                        val th = wLong
                        val px = curX
                        val py = -(wLong - wShort) / 2f
                        placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, true, tile.left, tile.right))
                        curX += tw
                    } else {
                        val tw = wLong
                        val th = wShort
                        val px = curX
                        val py = 0f
                        placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, false, tile.left, tile.right))
                        curX += tw
                    }
                    countInPhase++
                }
                1 -> {
                    // Check if should turn LEFT at bottom
                    if (countInPhase >= maxTilesInColumn) {
                        phase = 2
                        countInPhase = 0
                        if (lastTileWasDouble) {
                            curX = colX - (wLong - wShort) / 2f
                            curY = colBottomY - wShort
                        } else {
                            curX = colX
                            curY = colBottomY - wShort
                        }
                    }

                    if (phase == 1) {
                        // Moving DOWN in column
                        if (tile.isDouble) {
                            // Crosswise double in downward vertical column is horizontal
                            val tw = wLong
                            val th = wShort
                            val px = colX - (wLong - wShort) / 2f
                            val py = colBottomY
                            placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, false, tile.left, tile.right))
                            colBottomY += th
                        } else {
                            val tw = wShort
                            val th = wLong
                            val px = colX
                            val py = colBottomY
                            placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, true, tile.left, tile.right))
                            colBottomY += th
                        }
                        countInPhase++
                    } else {
                        // Turned LEFT
                        if (tile.isDouble) {
                            val tw = wShort
                            val th = wLong
                            val px = curX - tw
                            val py = curY - (wLong - wShort) / 2f
                            placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, true, tile.left, tile.right))
                            curX -= tw
                        } else {
                            val tw = wLong
                            val th = wShort
                            val px = curX - tw
                            val py = curY
                            placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, false, tile.right, tile.left))
                            curX -= tw
                        }
                        countInPhase++
                    }
                }
                2 -> {
                    // Continuing LEFT
                    if (tile.isDouble) {
                        val tw = wShort
                        val th = wLong
                        val px = curX - tw
                        val py = curY - (wLong - wShort) / 2f
                        placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, true, tile.left, tile.right))
                        curX -= tw
                    } else {
                        val tw = wLong
                        val th = wShort
                        val px = curX - tw
                        val py = curY
                        placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, false, tile.right, tile.left))
                        curX -= tw
                    }
                    countInPhase++
                }
            }
            lastTileWasDouble = tile.isDouble
        }
    }

    // 3. LEFT BRANCH: From openerIndex - 1 downTo 0
    // Phase 0: HORIZONTAL LEFT -> Phase 1: VERTICAL UP -> Phase 2: HORIZONTAL RIGHT
    if (openerIndex > 0) {
        var curX = 0f
        var curY = 0f
        var phase = 0 // 0: LEFT, 1: UP, 2: RIGHT
        var countInPhase = 0
        var colX = 0f
        var colTopY = 0f
        var lastTileWasDouble = openerIsVertical

        for (idx in (openerIndex - 1) downTo 0) {
            val tile = boardTiles[idx]

            if (phase == 0 && countInPhase >= maxTilesInRow) {
                phase = 1
                countInPhase = 0
                // Connect directly on top of the left end of the leftmost horizontal tile,
                // accounting for crosswise vertical extent if the corner tile is a double.
                colX = curX
                colTopY = if (lastTileWasDouble) -(wLong - wShort) / 2f else 0f
            }

            when (phase) {
                0 -> {
                    // Moving LEFT
                    if (tile.isDouble) {
                        val tw = wShort
                        val th = wLong
                        val px = curX - tw
                        val py = -(wLong - wShort) / 2f
                        placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, true, tile.left, tile.right))
                        curX -= tw
                    } else {
                        val tw = wLong
                        val th = wShort
                        val px = curX - tw
                        val py = 0f
                        placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, false, tile.left, tile.right))
                        curX -= tw
                    }
                    countInPhase++
                }
                1 -> {
                    // Check if should turn RIGHT at top
                    if (countInPhase >= maxTilesInColumn) {
                        phase = 2
                        countInPhase = 0
                        if (lastTileWasDouble) {
                            curX = colX - (wLong - wShort) / 2f + wLong
                            curY = colTopY
                        } else {
                            curX = colX + wShort
                            curY = colTopY
                        }
                    }

                    if (phase == 1) {
                        // Moving UP in column
                        if (tile.isDouble) {
                            // Crosswise double in upward vertical column is horizontal
                            val tw = wLong
                            val th = wShort
                            val px = colX - (wLong - wShort) / 2f
                            val py = colTopY - th
                            placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, false, tile.left, tile.right))
                            colTopY -= th
                        } else {
                            val tw = wShort
                            val th = wLong
                            val px = colX
                            val py = colTopY - th
                            placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, true, tile.left, tile.right))
                            colTopY -= th
                        }
                        countInPhase++
                    } else {
                        // Turned RIGHT
                        if (tile.isDouble) {
                            val tw = wShort
                            val th = wLong
                            val px = curX
                            val py = curY - (wLong - wShort) / 2f
                            placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, true, tile.left, tile.right))
                            curX += tw
                        } else {
                            val tw = wLong
                            val th = wShort
                            val px = curX
                            val py = curY
                            placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, false, tile.right, tile.left))
                            curX += tw
                        }
                        countInPhase++
                    }
                }
                2 -> {
                    // Continuing RIGHT
                    if (tile.isDouble) {
                        val tw = wShort
                        val th = wLong
                        val px = curX
                        val py = curY - (wLong - wShort) / 2f
                        placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, true, tile.left, tile.right))
                        curX += tw
                    } else {
                        val tw = wLong
                        val th = wShort
                        val px = curX
                        val py = curY
                        placed.add(PlacedBoardTile(tile, idx, px, py, tw, th, false, tile.right, tile.left))
                        curX += tw
                    }
                    countInPhase++
                }
            }
            lastTileWasDouble = tile.isDouble
        }
    }

    // Normalize coordinates so the bounding box starts at (0, 0)
    val minX = placed.minOfOrNull { it.x } ?: 0f
    val minY = placed.minOfOrNull { it.y } ?: 0f
    val maxX = placed.maxOfOrNull { it.x + it.width } ?: 0f
    val maxY = placed.maxOfOrNull { it.y + it.height } ?: 0f

    val normalized = placed.map {
        it.copy(x = it.x - minX, y = it.y - minY)
    }

    val boundingWidth = (maxX - minX).coerceAtLeast(wLong)
    val boundingHeight = (maxY - minY).coerceAtLeast(wLong)

    return DominoBoardLayout(normalized, boundingWidth, boundingHeight)
}
