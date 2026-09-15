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
