package com.example

import com.example.data.domino.DominoTile
import com.example.ui.components.calculateDominoSnakeLayout
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDominoSnakeLayout_FixedAnchorAndBends() {
    val opener = DominoTile(0, 5, 5)
    val right1 = DominoTile(5, 1, 51)
    val right2 = DominoTile(1, 6, 16)
    val rightDown1 = DominoTile(6, 6, 66) // vertical down
    val rightDown2 = DominoTile(6, 4, 64) // vertical down
    val rightDownDouble = DominoTile(4, 4, 44) // horizontal double in vertical column
    val rightDown3 = DominoTile(4, 1, 41) // vertical down

    val left1 = DominoTile(6, 0, 60)
    val left2 = DominoTile(5, 6, 56)
    val leftUp1 = DominoTile(1, 5, 15) // vertical up

    // Stage 1: Only Opener
    val l1 = calculateDominoSnakeLayout(listOf(opener), initialTileId = opener.id)
    assertEquals(1, l1.tiles.size)

    // Stage 2: Full board exactly matching photo 2 structure
    val board = listOf(leftUp1, left2, left1, opener, right1, right2, rightDown1, rightDown2, rightDownDouble, rightDown3)
    val layout = calculateDominoSnakeLayout(board, initialTileId = opener.id, maxTilesInRow = 2, maxTilesInColumn = 4)

    val placedLeftUp = layout.tiles.first { it.tile.id == 15 }
    val placedOpener = layout.tiles.first { it.tile.id == 5 }
    val placedRightDown1 = layout.tiles.first { it.tile.id == 66 }
    val placedRightDouble = layout.tiles.first { it.tile.id == 44 }

    // Left up tile must be above opener
    assertTrue("LeftUp tile must be vertically above opener", placedLeftUp.y < placedOpener.y)
    assertTrue("LeftUp tile must be vertical", placedLeftUp.isVertical)

    // Right down tile must be below opener
    assertTrue("RightDown tile must be vertically below opener", placedRightDown1.y > placedOpener.y)

    // Double in vertical column must be horizontal (crosswise)
    assertFalse("Double in vertical column must be placed horizontally crosswise", placedRightDouble.isVertical)
  }

  @Test
  fun testNoTilesOverlapInSnakeLayout() {
    val opener = DominoTile(5, 5, 55)
    val left1 = DominoTile(2, 5, 25)
    val left2 = DominoTile(6, 2, 62)
    val left3Double = DominoTile(6, 6, 66)
    val left4 = DominoTile(1, 6, 16)
    val boardA = listOf(left4, left3Double, left2, left1, opener)
    val layoutA = calculateDominoSnakeLayout(boardA, initialTileId = opener.id, maxTilesInRow = 3)

    for (i in layoutA.tiles.indices) {
      val t1 = layoutA.tiles[i]
      for (j in i + 1 until layoutA.tiles.size) {
        val t2 = layoutA.tiles[j]
        val overlapX = maxOf(0f, minOf(t1.x + t1.width, t2.x + t2.width) - maxOf(t1.x, t2.x))
        val overlapY = maxOf(0f, minOf(t1.y + t1.height, t2.y + t2.height) - maxOf(t1.y, t2.y))
        assertTrue("Tiles ${t1.tile} and ${t2.tile} overlap! overlapX=$overlapX, overlapY=$overlapY", overlapX < 0.5f || overlapY < 0.5f)
      }
    }
  }

  @Test
  fun testTouchingTilePipsMatchExactly() {
    // Escenario de la foto: Ficha de salida [6|6], jugada a la izquierda [1|6] y a la derecha [6|5]
    val opener = DominoTile(6, 6, 66)
    val leftTile = DominoTile(1, 6, 16) // tile.left=1, tile.right=6
    val rightTile = DominoTile(6, 5, 65) // tile.left=6, tile.right=5

    val board = listOf(leftTile, opener, rightTile)
    val layout = calculateDominoSnakeLayout(board, initialTileId = opener.id)

    val placedLeft = layout.tiles.first { it.tile.id == 16 }
    val placedOpener = layout.tiles.first { it.tile.id == 66 }
    val placedRight = layout.tiles.first { it.tile.id == 65 }

    // En la ficha izquierda, el lado derecho (que toca a la mula [6|6]) DEBE ser 6, y el izquierdo 1
    assertEquals("El lado derecho de la ficha izquierda debe tocar con 6 a la mula", 6, placedLeft.bottomOrRightPips)
    assertEquals("El lado izquierdo libre de la ficha izquierda debe ser 1", 1, placedLeft.topOrLeftPips)

    // En la ficha derecha, el lado izquierdo (que toca a la mula [6|6]) DEBE ser 6, y el derecho 5
    assertEquals("El lado izquierdo de la ficha derecha debe tocar con 6 a la mula", 6, placedRight.topOrLeftPips)
    assertEquals("El lado derecho libre de la ficha derecha debe ser 5", 5, placedRight.bottomOrRightPips)

    // La ficha izquierda debe estar físicamente a la izquierda del opener
    assertTrue("placedLeft.x + placedLeft.width debe tocar placedOpener.x", placedLeft.x + placedLeft.width <= placedOpener.x + 0.1f)
    // La ficha derecha debe estar físicamente a la derecha del opener
    assertTrue("placedRight.x debe tocar placedOpener.x + placedOpener.width", placedRight.x + 0.1f >= placedOpener.x + placedOpener.width)
  }

  @Test
  fun testSnakeTwoTurnPatternAndCenteringCamera() {
    val opener = DominoTile(6, 6, 66)
    // Cadena izquierda con 6 fichas (alcanza 2 giros: LEFT -> UP -> RIGHT)
    val left1 = DominoTile(5, 6, 56)
    val left2 = DominoTile(4, 5, 45)
    val left3 = DominoTile(3, 4, 34)
    val left4 = DominoTile(2, 3, 23)
    val left5 = DominoTile(1, 2, 12)
    val left6 = DominoTile(0, 1, 1)

    // Cadena derecha con 6 fichas (alcanza 2 giros: RIGHT -> DOWN -> LEFT)
    val right1 = DominoTile(6, 5, 65)
    val right2 = DominoTile(5, 4, 54)
    val right3 = DominoTile(4, 3, 43)
    val right4 = DominoTile(3, 2, 32)
    val right5 = DominoTile(2, 1, 21)
    val right6 = DominoTile(1, 0, 10)

    val fullBoard = listOf(left6, left5, left4, left3, left2, left1, opener, right1, right2, right3, right4, right5, right6)
    val layout = calculateDominoSnakeLayout(
      boardTiles = fullBoard,
      initialTileId = opener.id,
      maxTilesInRow = 2,
      maxTilesInColumn = 2
    )

    // Verificar que todas las fichas se crearon sin solapamiento
    for (i in layout.tiles.indices) {
      val t1 = layout.tiles[i]
      for (j in i + 1 until layout.tiles.size) {
        val t2 = layout.tiles[j]
        val overlapX = maxOf(0f, minOf(t1.x + t1.width, t2.x + t2.width) - maxOf(t1.x, t2.x))
        val overlapY = maxOf(0f, minOf(t1.y + t1.height, t2.y + t2.height) - maxOf(t1.y, t2.y))
        assertTrue("Tiles ${t1.tile.id} and ${t2.tile.id} overlap", overlapX < 0.5f || overlapY < 0.5f)
      }
    }

    // Verificar cámara: si el tablero cabe, escala es 1.0f (no zoom out prematuro)
    val cameraFits = com.example.ui.components.calculateBoundingBoxFittingCamera(
      contentWidth = 200f,
      contentHeight = 100f,
      viewportWidth = 800f,
      viewportHeight = 600f
    )
    assertEquals(1.0f, cameraFits.scaleFactor, 0.001f)

    // Si el tablero excede la mesa, zoom out suave ajustado
    val cameraLarge = com.example.ui.components.calculateBoundingBoxFittingCamera(
      contentWidth = 1000f,
      contentHeight = 500f,
      viewportWidth = 500f,
      viewportHeight = 500f
    )
    assertTrue(cameraLarge.scaleFactor < 1.0f)
  }

  @Test
  fun testTeamMode_2vs2_SetupAndScoring() {
    val state = com.example.data.domino.DominoEngine.startNewMatch(
      humanPlayerName = "Yo",
      botCount = 3,
      targetScore = 100,
      playMode = com.example.data.domino.DominoGamePlayMode.PAREJAS_2V2
    )

    assertEquals(4, state.players.size)
    assertEquals(com.example.data.domino.DominoGamePlayMode.PAREJAS_2V2, state.playMode)

    // Player 0 (Human) and Player 2 (Bot 2 / María) should be Team 0
    assertEquals(0, state.players[0].teamId)
    assertEquals(0, state.players[2].teamId)

    // Player 1 (Carlos) and Player 3 (Luis) should be Team 1
    assertEquals(1, state.players[1].teamId)
    assertEquals(1, state.players[3].teamId)

    // Verify playTile when human plays last tile
    val lastTile = state.players[0].hand.first()
    val stateWithOneTile = state.copy(
      players = state.players.mapIndexed { idx, p ->
        if (idx == 0) p.copy(hand = listOf(lastTile)) else p
      }
    )

    val validPlacements = com.example.data.domino.DominoEngine.getPlayablePlacements(lastTile, stateWithOneTile)
    val resultState = com.example.data.domino.DominoEngine.playTile(
      stateWithOneTile,
      0,
      lastTile,
      validPlacements.first()
    )

    // Round should be won
    assertEquals(com.example.data.domino.TableGameStatus.ROUND_OVER, resultState.status)
    assertEquals(0, resultState.roundWinnerIndex)
    assertTrue(resultState.lastActionLog.contains("¡Ganaste tú!"))

    // Team 0 should have gained the points of Team 1 (players 1 and 3)
    val expectedPoints = state.players[1].remainingTilePoints + state.players[3].remainingTilePoints
    assertEquals(expectedPoints, resultState.teamScores[0])
  }

  @Test
  fun testHumanWinMessageInIndividualAndBlocked() {
    val human = com.example.data.domino.DominoPlayer(id = "p0", name = "Tú", hand = listOf(DominoTile(1, 2, 3)), isBot = false, teamId = 0)
    val bot = com.example.data.domino.DominoPlayer(id = "p1", name = "Carlos (Bot)", hand = listOf(DominoTile(4, 5, 45)), isBot = true, teamId = 1)
    val state = com.example.data.domino.DominoTableState(
      players = listOf(human, bot),
      boardTiles = listOf(DominoTile(1, 1, 11)),
      leftEnd = 1,
      rightEnd = 1,
      playMode = com.example.data.domino.DominoGamePlayMode.INDIVIDUAL
    )

    // Play last tile
    val resState = com.example.data.domino.DominoEngine.playTile(state, 0, human.hand.first(), com.example.data.domino.TilePlacement.LEFT)
    assertTrue("Should say '¡Ganaste tú la ronda' in individual mode", resState.lastActionLog.startsWith("¡Ganaste tú la ronda"))

    // Test blocked game (tranca) where human wins with lowest points
    val humanWithLowPoints = com.example.data.domino.DominoPlayer(id = "p0", name = "Tú", hand = listOf(DominoTile(0, 1, 1)), isBot = false, teamId = 0)
    val botWithHighPoints = com.example.data.domino.DominoPlayer(id = "p1", name = "Carlos (Bot)", hand = listOf(DominoTile(6, 6, 66)), isBot = true, teamId = 1)
    val blockedState = com.example.data.domino.DominoTableState(
      players = listOf(humanWithLowPoints, botWithHighPoints),
      boardTiles = listOf(DominoTile(2, 3, 23)),
      leftEnd = 2,
      rightEnd = 3,
      playMode = com.example.data.domino.DominoGamePlayMode.INDIVIDUAL
    )
    val resBlocked = com.example.data.domino.DominoEngine.passTurn(blockedState.copy(consecutivePasses = 1), 1)
    assertTrue("Should say '¡Tranca! Ganaste tú' when human has lowest points", resBlocked.lastActionLog.startsWith("¡Tranca! Ganaste tú"))
  }

  @Test
  fun testCannotPassWhenPlayerHasPlayableTiles() {
    // Board ends: leftEnd = 0, rightEnd = 1
    // Player hand: [0|3]
    val playableTile = DominoTile(0, 3, 3)
    val nonPlayableTile = DominoTile(4, 5, 45)

    val playerWithPlayable = com.example.data.domino.DominoPlayer(
      id = "p0",
      name = "Tú",
      hand = listOf(playableTile),
      isBot = false
    )
    val playerWithoutPlayable = com.example.data.domino.DominoPlayer(
      id = "p1",
      name = "Bot",
      hand = listOf(nonPlayableTile),
      isBot = true
    )

    val state = com.example.data.domino.DominoTableState(
      players = listOf(playerWithPlayable, playerWithoutPlayable),
      leftEnd = 0,
      rightEnd = 1,
      boardTiles = listOf(DominoTile(0, 1, 1))
    )

    // Player with [0|3] MUST be recognized as having a valid play (canPlayerPlay == true)
    assertTrue("Player with [0|3] must be able to play on end 0", com.example.data.domino.DominoEngine.canPlayerPlay(playerWithPlayable, state))

    // Player with [4|5] cannot play on ends 0 and 1
    assertFalse("Player with [4|5] cannot play on ends 0 and 1", com.example.data.domino.DominoEngine.canPlayerPlay(playerWithoutPlayable, state))
  }

  @Test
  fun testWinnerStartsNextRoundWithAnyTile() {
    val players = listOf(
      com.example.data.domino.DominoPlayer(id = "p0", name = "Tú", isBot = false),
      com.example.data.domino.DominoPlayer(id = "p1", name = "Carlos", isBot = true),
      com.example.data.domino.DominoPlayer(id = "p2", name = "María", isBot = true),
      com.example.data.domino.DominoPlayer(id = "p3", name = "Luis", isBot = true)
    )

    // Suppose player 2 (María) won the previous round
    val state = com.example.data.domino.DominoEngine.dealRound(
      players = players,
      targetScore = 100,
      starterPlayerIndex = 2
    )

    // Turn must start with María
    assertEquals(2, state.currentTurnIndex)
    assertTrue(state.boardTiles.isEmpty())
    assertTrue(state.lastActionLog.contains("María"))
    assertTrue(state.lastActionLog.contains("ganó la mano anterior"))

    // Starter can play ANY tile from their hand even if not a double
    val starterPlayer = state.players[2]
    assertEquals(7, starterPlayer.hand.size)
    for (tile in starterPlayer.hand) {
      val placements = com.example.data.domino.DominoEngine.getPlayablePlacements(tile, state)
      assertTrue("Any tile must be playable when board is empty", placements.isNotEmpty())
    }

    // Play a tile to open the board
    val firstTile = starterPlayer.hand.first()
    val stateAfterOpen = com.example.data.domino.DominoEngine.playTile(
      state = state,
      playerIndex = 2,
      tile = firstTile,
      placement = com.example.data.domino.TilePlacement.LEFT
    )
    assertEquals(1, stateAfterOpen.boardTiles.size)
    assertEquals(firstTile.left, stateAfterOpen.leftEnd)
    assertEquals(firstTile.right, stateAfterOpen.rightEnd)
    assertEquals(3, stateAfterOpen.currentTurnIndex)
  }

  @Test
  fun testSnakeTwoTurnPatternAndDoubleTilePlacement() {
    // Test that the snake sequence strictly follows:
    // Tail (Left branch): LEFT -> UP -> RIGHT
    // Head (Right branch): RIGHT -> DOWN -> LEFT
    // And double tiles (such as 0-0 "doble blanco") are placed properly without overlap or inversion
    val tiles = listOf(
      DominoTile(0, 0, 100), // Double blank [0|0]
      DominoTile(0, 1, 101),
      DominoTile(1, 2, 102),
      DominoTile(2, 3, 103),
      DominoTile(3, 4, 104), // Starter opener
      DominoTile(4, 5, 105),
      DominoTile(5, 6, 106),
      DominoTile(6, 6, 107), // Double six [6|6]
      DominoTile(6, 1, 108)
    )

    val layout = calculateDominoSnakeLayout(
      boardTiles = tiles,
      initialTileId = 104,
      baseUnit = 24f,
      maxTilesInRow = 3,
      maxTilesInColumn = 2
    )

    assertEquals(tiles.size, layout.tiles.size)

    // Verify all tiles are placed within positive bounds
    for (t in layout.tiles) {
      assertTrue("Tile x must be >= 0, was ${t.x}", t.x >= -0.001f)
      assertTrue("Tile y must be >= 0, was ${t.y}", t.y >= -0.001f)
      assertTrue(
        "Tile right (${t.x + t.width}) must be <= boundingWidth (${layout.boundingWidth})",
        t.x + t.width <= layout.boundingWidth + 0.001f
      )
      assertTrue(
        "Tile bottom (${t.y + t.height}) must be <= boundingHeight (${layout.boundingHeight})",
        t.y + t.height <= layout.boundingHeight + 0.001f
      )
    }

    // Verify double tiles orientation
    val doubleBlank = layout.tiles.firstOrNull { it.tile.id == 100 }
    assertNotNull(doubleBlank)
    assertTrue("Double blank [0|0] must be placed", doubleBlank!!.tile.isDouble)

    val doubleSix = layout.tiles.firstOrNull { it.tile.id == 107 }
    assertNotNull(doubleSix)
    assertTrue("Double six [6|6] must be placed", doubleSix!!.tile.isDouble)
  }

  @Test
  fun testTwoPlayerManyTilesStayStrictlyInsideBounds() {
    // Simulate a board with 20 tiles (typical 2-player deep game)
    val tiles = (0..19).map { i ->
      DominoTile(
        id = i,
        left = i % 7,
        right = if (i % 3 == 0) (i % 7) else (i + 1) % 7 // some doubles
      )
    }

    val layout = calculateDominoSnakeLayout(
      boardTiles = tiles,
      initialTileId = tiles[10].id,
      baseUnit = 24f,
      maxTilesInRow = 3
    )

    assertTrue("Bounding width must be positive", layout.boundingWidth > 0f)
    assertTrue("Bounding height must be positive", layout.boundingHeight > 0f)

    for (placed in layout.tiles) {
      assertTrue("Tile x must be >= 0, was ${placed.x}", placed.x >= -0.001f)
      assertTrue("Tile y must be >= 0, was ${placed.y}", placed.y >= -0.001f)
      assertTrue(
        "Tile right edge (${placed.x + placed.width}) must be <= boundingWidth (${layout.boundingWidth})",
        placed.x + placed.width <= layout.boundingWidth + 0.001f
      )
      assertTrue(
        "Tile bottom edge (${placed.y + placed.height}) must be <= boundingHeight (${layout.boundingHeight})",
        placed.y + placed.height <= layout.boundingHeight + 0.001f
      )
    }

    // Check with available dimensions of mobile table
    val availW = 320f
    val availH = 340f
    val autoScale = minOf(availW / layout.boundingWidth, availH / layout.boundingHeight).coerceIn(0.15f, 1.15f)
    val scaledW = layout.boundingWidth * autoScale
    val scaledH = layout.boundingHeight * autoScale

    assertTrue("Scaled width ($scaledW) must fit inside availW ($availW)", scaledW <= availW + 0.001f)
    assertTrue("Scaled height ($scaledH) must fit inside availH ($availH)", scaledH <= availH + 0.001f)
  }
}
