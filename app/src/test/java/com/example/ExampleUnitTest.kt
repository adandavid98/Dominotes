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

    // Team 0 should have gained the points of Team 1 (players 1 and 3)
    val expectedPoints = state.players[1].remainingTilePoints + state.players[3].remainingTilePoints
    assertEquals(expectedPoints, resultState.teamScores[0])
  }
}
