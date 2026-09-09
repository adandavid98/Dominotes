package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.domino.DominoPlayer
import com.example.data.domino.DominoTableState
import com.example.data.domino.DominoTile
import com.example.data.domino.TableGameStatus
import com.example.data.domino.TilePlacement
import com.example.ui.theme.DominoGold

private val PlayerColors = listOf(
    Color(0xFF38BDF8), // Sky Blue (Human Player)
    Color(0xFF34D399), // Emerald (Opponent 1)
    Color(0xFFFBBF24), // Amber (Opponent 2)
    Color(0xFFA78BFA)  // Violet (Opponent 3)
)

@Composable
fun DominoTableView(
    state: DominoTableState,
    selectedTile: DominoTile?,
    onSelectTile: (DominoTile?) -> Unit,
    onPlayTile: (DominoTile, TilePlacement) -> Unit,
    onDrawTile: () -> Unit,
    onPassTurn: () -> Unit,
    onNextRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val humanPlayerIndex = state.players.indexOfFirst { !it.isBot }.takeIf { it >= 0 } ?: 0
    val humanPlayer = state.players.getOrNull(humanPlayerIndex)
    val isHumanTurn = state.currentTurnIndex == humanPlayerIndex && state.status == TableGameStatus.PLAYING

    // Infinite animation for Turn Pulsing
    val turnTransition = rememberInfiniteTransition(label = "turn_pulse")
    val pulseScale by turnTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by turnTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF0A0F1D), // Dark slate
                        Color(0xFF020617)  // Deep background
                    )
                )
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP: Opponents Status Bar
        val isTeamsMode = state.playMode.isTeams && state.players.size == 4

        if (isTeamsMode) {
            // Team Mode Banner: Shows Team "Nosotros" (Tú + María) vs Team "Rivales" (Carlos + Luis)
            val team0Score = state.teamScores.getOrElse(0) { 0 }
            val team1Score = state.teamScores.getOrElse(1) { 0 }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.9f),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Team 0 (Nosotros)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0284C7).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Tu Pareja (Tú + María)", fontSize = 11.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold)
                            Text("$team0Score / ${state.targetScore} pts", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // VS Divider badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "2 vs 2",
                            color = DominoGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Team 1 (Rivales)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Rivales (Carlos + Luis)", fontSize = 11.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold)
                            Text("$team1Score / ${state.targetScore} pts", fontSize = 11.sp, color = Color(0xFFF87171), fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDC2626).copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Color(0xFFF87171)),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            state.players.drop(1).forEachIndexed { botRelIdx, opponent ->
                val actualIndex = state.players.indexOf(opponent)
                val isTurn = state.currentTurnIndex == actualIndex && state.status == TableGameStatus.PLAYING
                val isPartner = isTeamsMode && opponent.teamId == 0
                OpponentBadge(
                    player = opponent,
                    isTurn = isTurn,
                    isPartner = isPartner,
                    color = if (isPartner) Color(0xFF38BDF8) else PlayerColors.getOrElse(actualIndex) { Color(0xFF94A3B8) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action / Status Banner (Glassmorphic modern pill)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.75f),
            border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isHumanTurn) Color(0xFF38BDF8) else Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.lastActionLog,
                        color = Color(0xFFF1F5F9),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
                if (state.boneyard.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Pozo: ${state.boneyard.size}",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // CENTER: The Game Table Felt (Modern Dark Forest Felt with Radial Depth)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.2.dp, Color(0xFF134E4A).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp)
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF064E3B).copy(alpha = 0.85f),
                                Color(0xFF042F2E).copy(alpha = 0.95f),
                                Color(0xFF021E1E)
                            ),
                            center = center,
                            radius = size.maxDimension * 0.7f
                        )
                    )
                }
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.boardTiles.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.06f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = null,
                                    tint = Color(0xFF6EE7B7).copy(alpha = 0.85f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isHumanTurn) "¡Es tu turno de abrir la mesa!\nToca una ficha de tu mano para iniciar"
                            else "Esperando apertura de mesa...",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    val layout = remember(state.boardTiles, state.initialTileId) {
                        calculateDominoSnakeLayout(
                            boardTiles = state.boardTiles,
                            initialTileId = state.initialTileId,
                            baseUnit = 24f,
                            maxTilesInRow = 3
                        )
                    }

                    // Dynamically calculate scale so all tiles fit on screen with zero gap & no scroll
                    val availW = (maxWidth.value - 20f).coerceAtLeast(60f)
                    val availH = (maxHeight.value - 20f).coerceAtLeast(60f)
                    val autoScale = minOf(availW / layout.boundingWidth, availH / layout.boundingHeight).coerceIn(0.35f, 1.25f)

                    Box(
                        modifier = Modifier
                            .size(layout.boundingWidth.dp, layout.boundingHeight.dp)
                            .graphicsLayer {
                                scaleX = autoScale
                                scaleY = autoScale
                                transformOrigin = TransformOrigin.Center
                            }
                    ) {
                        layout.tiles.forEach { placedTile ->
                            val isOpenLeft = placedTile.index == 0
                            val isOpenRight = placedTile.index == state.boardTiles.size - 1

                            val canPlayLeft = isOpenLeft && isHumanTurn && selectedTile != null && (state.leftEnd == null || selectedTile.canMatch(state.leftEnd))
                            val canPlayRight = isOpenRight && isHumanTurn && selectedTile != null && (state.rightEnd == null || selectedTile.canMatch(state.rightEnd))

                            Box(
                                modifier = Modifier
                                    .offset(x = placedTile.x.dp, y = placedTile.y.dp)
                                    .size(placedTile.width.dp, placedTile.height.dp)
                                    .then(
                                        if (canPlayLeft) {
                                            Modifier.clickable { onPlayTile(selectedTile!!, TilePlacement.LEFT) }
                                        } else if (canPlayRight) {
                                            Modifier.clickable { onPlayTile(selectedTile!!, TilePlacement.RIGHT) }
                                        } else {
                                            Modifier
                                        }
                                    )
                            ) {
                                if (placedTile.isVertical) {
                                    DominoTileView(
                                        topPips = placedTile.topOrLeftPips,
                                        bottomPips = placedTile.bottomOrRightPips,
                                        width = placedTile.width.dp,
                                        height = placedTile.height.dp,
                                        isHighlighted = canPlayLeft || canPlayRight
                                    )
                                } else {
                                    HorizontalDominoTileView(
                                        leftPips = placedTile.topOrLeftPips,
                                        rightPips = placedTile.bottomOrRightPips,
                                        width = placedTile.width.dp,
                                        height = placedTile.height.dp,
                                        isHighlighted = canPlayLeft || canPlayRight
                                    )
                                }
                            }
                        }
                    }
                }

                // Round / Game Over Overlay Dialog
                if (state.status == TableGameStatus.ROUND_OVER || state.status == TableGameStatus.GAME_OVER) {
                    RoundOverOverlay(
                        state = state,
                        onNextRound = onNextRound
                    )
                }
            }
        }

        // BOTTOM: Human Player Deck (Modern Slate Card)
        humanPlayer?.let { player ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                    .border(
                        if (isHumanTurn) 1.8.dp else 1.2.dp,
                        if (isHumanTurn) Color(0xFF10B981).copy(alpha = 0.85f) else Color(0xFF334155).copy(alpha = 0.7f),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Player Name, Score, and Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Turn Pulsing Radar Dot
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(20.dp)
                        ) {
                            if (isHumanTurn) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp * pulseScale)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = 0.35f * pulseAlpha))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF34D399))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF64748B))
                                )
                            }
                        }

                        // Player Name
                        Text(
                            text = if (player.name.equals("Tú", ignoreCase = true)) "Tú" else "${player.name} (Tú)",
                            color = if (isHumanTurn) Color.White else Color(0xFFE2E8F0),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )

                        // Highly visible "¡TU TURNO!" badge
                        if (isHumanTurn) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.5.dp, Color(0xFF6EE7B7)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF047857), Color(0xFF10B981))
                                        )
                                    )
                                    .testTag("badge_your_turn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "¡TU TURNO!",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        // Score Pill
                        val isTeamsMode = state.playMode.isTeams && state.players.size == 4
                        val team0Score = state.teamScores.getOrElse(0) { player.totalScore }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (isTeamsMode) "Pareja: $team0Score / ${state.targetScore} pts" else "${player.totalScore} / ${state.targetScore} pts",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Interactive Action Buttons for Human
                    if (isHumanTurn) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (state.boneyard.isNotEmpty()) {
                                Button(
                                    onClick = onDrawTile,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    contentPadding = PaddingValues(horizontal = 11.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(9.dp),
                                    modifier = Modifier.testTag("btn_draw_tile")
                                ) {
                                    Text("Robar (${state.boneyard.size})", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Button(
                                onClick = onPassTurn,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(9.dp),
                                modifier = Modifier.testTag("btn_pass_turn")
                            ) {
                                Text("Pasar", fontSize = 12.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else if (state.status == TableGameStatus.PLAYING) {
                        val currentBot = state.players.getOrNull(state.currentTurnIndex)
                        Text(
                            text = "Turno de: ${currentBot?.name ?: "..."}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Action Buttons when a playable tile is selected from hand
                if (isHumanTurn && selectedTile != null && state.boardTiles.isNotEmpty()) {
                    val canPlayLeft = state.leftEnd == null || selectedTile.canMatch(state.leftEnd)
                    val canPlayRight = state.rightEnd == null || selectedTile.canMatch(state.rightEnd)

                    if (canPlayLeft || canPlayRight) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (canPlayLeft) {
                                Button(
                                    onClick = { onPlayTile(selectedTile, TilePlacement.LEFT) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Extremo Izq [${state.leftEnd ?: ""}]", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (canPlayRight) {
                                Button(
                                    onClick = { onPlayTile(selectedTile, TilePlacement.RIGHT) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Extremo Der [${state.rightEnd ?: ""}]", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // Human Tiles in Hand with Enhanced Ceramic Visuals & Highlight
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    player.hand.forEach { tile ->
                        val isSelected = selectedTile?.id == tile.id
                        val canPlay = isHumanTurn && (
                                state.boardTiles.isEmpty() ||
                                        (state.leftEnd != null && tile.canMatch(state.leftEnd)) ||
                                        (state.rightEnd != null && tile.canMatch(state.rightEnd))
                                )

                        Box(
                            modifier = Modifier
                                .clickable {
                                    if (isHumanTurn) {
                                        if (isSelected) {
                                            onSelectTile(null)
                                        } else {
                                            onSelectTile(tile)
                                            // If initial board is empty, play immediately
                                            if (state.boardTiles.isEmpty()) {
                                                onPlayTile(tile, TilePlacement.LEFT)
                                            }
                                        }
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            DominoTileView(
                                topPips = tile.left,
                                bottomPips = tile.right,
                                width = 44.dp,
                                height = 88.dp,
                                isHighlighted = isSelected,
                                dimmed = !canPlay && isHumanTurn
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpponentBadge(
    player: DominoPlayer,
    isTurn: Boolean,
    isPartner: Boolean = false,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isTurn) color.copy(alpha = 0.2f) else Color(0xFF1E293B).copy(alpha = 0.7f),
        border = BorderStroke(
            width = if (isTurn) 1.5.dp else if (isPartner) 1.2.dp else 1.dp,
            color = if (isTurn) color else if (isPartner) Color(0xFF38BDF8).copy(alpha = 0.6f) else Color(0xFF334155).copy(alpha = 0.6f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPartner) Icons.Default.Groups else if (player.isBot) Icons.Default.SmartToy else Icons.Default.Person,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isPartner) "${player.name.replace(" (Bot)", "")} (Tu Pareja)" else player.name.replace(" (Bot)", ""),
                    color = Color.White,
                    fontWeight = if (isTurn || isPartner) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Mini tiles indicator
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(player.hand.size) {
                        DominoTileBackView(width = 6.dp, height = 11.dp)
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${player.totalScore}p",
                    color = DominoGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RoundOverOverlay(
    state: DominoTableState,
    onNextRound: () -> Unit
) {
    val isTeamsMode = state.playMode.isTeams && state.players.size == 4
    val team0Score = state.teamScores.getOrElse(0) { 0 }
    val team1Score = state.teamScores.getOrElse(1) { 0 }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.96f),
        border = BorderStroke(1.5.dp, DominoGold.copy(alpha = 0.8f)),
        shadowElevation = 12.dp,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.92f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = DominoGold.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, DominoGold.copy(alpha = 0.4f)),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = DominoGold,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (state.status == TableGameStatus.GAME_OVER) "¡Partida Terminada!" else "¡Fin de Ronda!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state.lastActionLog,
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Scoreboard (Team mode or Individual)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (isTeamsMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tu Pareja (Tú + María)", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("$team0Score / ${state.targetScore} pts", color = DominoGold, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pareja Rival (Carlos + Luis)", color = Color(0xFFF87171), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("$team1Score / ${state.targetScore} pts", color = DominoGold, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                    } else {
                        state.players.forEach { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(p.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "${p.totalScore} / ${state.targetScore} pts",
                                    color = DominoGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNextRound,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_next_round")
            ) {
                Text(
                    text = if (state.status == TableGameStatus.GAME_OVER) "Nueva Partida" else "Siguiente Ronda",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
