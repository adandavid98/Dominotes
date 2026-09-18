package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.key
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Casino
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.domino.DominoEngine
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

private data class PlayerFlightTrajectory(
    val startX: Float,
    val startY: Float,
    val startScale: Float,
    val startRotation: Float
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
    onStartWaitingGame: () -> Unit = {},
    onAddGuest: (String) -> Unit = {},
    onRemoveGuest: (Int) -> Unit = {},
    onFillBotsAndStart: () -> Unit = {},
    onCancelWaitingRoom: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (state.status == TableGameStatus.WAITING_START) {
        DominoRoomWaitingView(
            state = state,
            onStartGame = onStartWaitingGame,
            onAddGuest = onAddGuest,
            onRemoveGuest = onRemoveGuest,
            onFillBotsAndStart = onFillBotsAndStart,
            onCancel = onCancelWaitingRoom,
            modifier = modifier
        )
        return
    }

    val currentTurnPlayer = state.players.getOrNull(state.currentTurnIndex)
    val isCurrentTurnHuman = currentTurnPlayer != null && !currentTurnPlayer.isBot && state.status == TableGameStatus.PLAYING
    val humanPlayer = if (isCurrentTurnHuman) {
        currentTurnPlayer
    } else {
        state.players.firstOrNull { !it.isBot } ?: state.players.firstOrNull()
    }
    val isHumanTurn = isCurrentTurnHuman

    // Trajectory calculation: Human tile comes from bottom hand ("fondo");
    // opponents come from top: left, center, or right badge position
    val humanIndex = state.players.indexOf(humanPlayer)
    val opponents = state.players.filter { it != humanPlayer }
    val flightTrajectory = remember(state.lastPlayedTile?.id, state.lastPlayedByPlayerIndex) {
        val lastPlayerIndex = state.lastPlayedByPlayerIndex
        if (lastPlayerIndex == humanIndex || (lastPlayerIndex == null && state.lastPlayedByPlayerName?.contains("Tú", ignoreCase = true) == true)) {
            // Human player: emerges from the bottom deck/hand
            PlayerFlightTrajectory(
                startX = 0f,
                startY = 380f,
                startScale = 1.35f,
                startRotation = 0f
            )
        } else {
            // Opponents at the top row: left, center, right
            val oppIndex = opponents.indexOfFirst { state.players.indexOf(it) == lastPlayerIndex }
            when {
                opponents.size >= 3 -> {
                    when (oppIndex) {
                        0 -> PlayerFlightTrajectory(startX = -260f, startY = -280f, startScale = 0.65f, startRotation = -14f) // Left bot
                        1 -> PlayerFlightTrajectory(startX = 0f, startY = -290f, startScale = 0.65f, startRotation = 0f)       // Center bot / Partner
                        else -> PlayerFlightTrajectory(startX = 260f, startY = -280f, startScale = 0.65f, startRotation = 14f) // Right bot
                    }
                }
                opponents.size == 2 -> {
                    if (oppIndex == 0) {
                        PlayerFlightTrajectory(startX = -180f, startY = -280f, startScale = 0.65f, startRotation = -12f)
                    } else {
                        PlayerFlightTrajectory(startX = 180f, startY = -280f, startScale = 0.65f, startRotation = 12f)
                    }
                }
                else -> {
                    // 1 opponent: center top
                    PlayerFlightTrajectory(startX = 0f, startY = -290f, startScale = 0.65f, startRotation = 0f)
                }
            }
        }
    }

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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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
                        val team0Players = state.players.filter { it.teamId == 0 }
                        val team0Names = team0Players.joinToString(" + ") { it.name.split(" ").first() }.ifBlank { "Equipo 1" }
                        Column {
                            Text("Tu Pareja ($team0Names)", fontSize = 11.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold)
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
                        val team1Players = state.players.filter { it.teamId == 1 }
                        val team1Names = team1Players.joinToString(" + ") { it.name.split(" ").first() }.ifBlank { "Equipo 2" }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Rivales ($team1Names)", fontSize = 11.sp, color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold)
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
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            state.players.filter { it != humanPlayer }.forEach { opponent ->
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
                    val statusDotColor = when {
                        state.lastActionLog.contains("pensando", ignoreCase = true) -> Color(0xFFF59E0B)
                        state.lastActionLog.contains("jugó", ignoreCase = true) || state.lastActionLog.contains("abrió", ignoreCase = true) -> Color(0xFF10B981)
                        state.lastActionLog.contains("robó", ignoreCase = true) -> Color(0xFF38BDF8)
                        state.lastActionLog.contains("pasó", ignoreCase = true) -> Color(0xFFF87171)
                        isHumanTurn -> Color(0xFF10B981)
                        else -> Color(0xFF94A3B8)
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusDotColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val displayText = if (isHumanTurn && state.lastPlayedTile != null && state.lastPlayedByPlayerName != null) {
                        "Última jugada: ${state.lastPlayedByPlayerName} jugó la ficha [${state.lastPlayedTile.left}|${state.lastPlayedTile.right}]"
                    } else {
                        state.lastActionLog
                    }
                    Text(
                        text = displayText,
                        color = Color(0xFFF1F5F9),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                            maxTilesInRow = 4,
                            maxTilesInColumn = 3
                        )
                    }

                    // Sistema de Cámara Dinámica / Zoom Automático (Bounding Box Fitting)
                    val camera = calculateBoundingBoxFittingCamera(
                        contentWidth = layout.boundingWidth,
                        contentHeight = layout.boundingHeight,
                        viewportWidth = maxWidth.value,
                        viewportHeight = maxHeight.value,
                        paddingDp = 18f,
                        minScale = 0.15f,
                        maxScale = 1.0f
                    )

                    // Animación suave de transición de escala para una experiencia de juego visualmente atractiva
                    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = camera.scaleFactor,
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 350, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                        label = "camera_zoom"
                    )

                    val scaledW = layout.boundingWidth * animatedScale
                    val scaledH = layout.boundingHeight * animatedScale

                    // Contenedor centrado con las dimensiones escaladas
                    Box(
                        modifier = Modifier
                            .requiredSize(scaledW.dp, scaledH.dp)
                    ) {
                        // Contenedor interno que renderiza el tablero con origen fijo (0, 0) y escala animada
                        Box(
                            modifier = Modifier
                                .requiredSize(layout.boundingWidth.dp, layout.boundingHeight.dp)
                                .graphicsLayer {
                                    scaleX = animatedScale
                                    scaleY = animatedScale
                                    transformOrigin = TransformOrigin(0f, 0f)
                                }
                        ) {
                            layout.tiles.forEach { placedTile ->
                                key(placedTile.tile.id) {
                                    val isOpenLeft = placedTile.index == 0
                                    val isOpenRight = placedTile.index == state.boardTiles.size - 1

                                    val canPlayLeft = isOpenLeft && isHumanTurn && selectedTile != null && (state.leftEnd == null || selectedTile.canMatch(state.leftEnd))
                                    val canPlayRight = isOpenRight && isHumanTurn && selectedTile != null && (state.rightEnd == null || selectedTile.canMatch(state.rightEnd))
                                    val isNewlyPlaced = placedTile.tile.id == state.lastPlayedTile?.id

                                    AnimatedPlacedTileView(
                                        placedTile = placedTile,
                                        isNewlyPlaced = isNewlyPlaced,
                                        sourceStartX = flightTrajectory.startX,
                                        sourceStartY = flightTrajectory.startY,
                                        sourceScale = flightTrajectory.startScale,
                                        sourceRotation = flightTrajectory.startRotation,
                                        canPlayLeft = canPlayLeft,
                                        canPlayRight = canPlayRight,
                                        onPlayClick = {
                                            if (canPlayLeft) {
                                                onPlayTile(selectedTile!!, TilePlacement.LEFT)
                                            } else if (canPlayRight) {
                                                onPlayTile(selectedTile!!, TilePlacement.RIGHT)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM: Human Player Deck (Modern Slate Card)
        humanPlayer?.let { player ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                    .border(
                        if (isHumanTurn) 1.8.dp else 1.2.dp,
                        if (isHumanTurn) Color(0xFF10B981).copy(alpha = 0.85f) else Color(0xFF334155).copy(alpha = 0.7f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                // Player Name, Status, and Score Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
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
                            text = player.name,
                            color = if (isHumanTurn) Color.White else Color(0xFFE2E8F0),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Highly visible "¡TU TURNO!" badge
                        if (isHumanTurn) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.2.dp, Color(0xFF6EE7B7)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF047857), Color(0xFF10B981))
                                        )
                                    )
                                    .testTag("badge_your_turn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "¡TU TURNO!",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    // Score Pill and/or Bot turn indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!isHumanTurn && state.status == TableGameStatus.PLAYING) {
                            val currentBot = state.players.getOrNull(state.currentTurnIndex)
                            Text(
                                text = "Turno de: ${currentBot?.name ?: "..."}",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (isHumanTurn && state.lastPlayedTile != null && state.lastPlayedByPlayerName != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "Última: ${state.lastPlayedByPlayerName!!.replace(" (Bot)", "")} [${state.lastPlayedTile.left}|${state.lastPlayedTile.right}]",
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }

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
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Interactive Turn Actions Bar for Human (Dedicated row, perfectly sized, never squished)
                if (isHumanTurn) {
                    val playerHasPlayableTiles = DominoEngine.canPlayerPlay(player, state)
                    val canPlayLeft = selectedTile != null && (state.leftEnd == null || selectedTile.canMatch(state.leftEnd))
                    val canPlayRight = selectedTile != null && (state.rightEnd == null || selectedTile.canMatch(state.rightEnd))
                    val hasPlayableOption = selectedTile != null && state.boardTiles.isNotEmpty() && (canPlayLeft || canPlayRight)

                    if (hasPlayableOption) {
                        // Left / Right placement buttons when a valid tile is selected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (canPlayLeft) {
                                Button(
                                    onClick = { onPlayTile(selectedTile!!, TilePlacement.LEFT) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_play_left")
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Extremo Izq [${state.leftEnd ?: ""}]",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                            if (canPlayRight) {
                                Button(
                                    onClick = { onPlayTile(selectedTile!!, TilePlacement.RIGHT) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("btn_play_right")
                                ) {
                                    Text(
                                        "Extremo Der [${state.rightEnd ?: ""}]",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                            IconButton(
                                onClick = { onSelectTile(null) },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Deseleccionar", tint = Color(0xFF94A3B8))
                            }
                        }
                    } else if (playerHasPlayableTiles) {
                        // En dominó real: Si tienes fichas jugables, NO puedes pasar ni robar. Debes tirar una ficha.
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0D251D),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (selectedTile != null) {
                                        "Esa ficha no encaja. Toca una ficha con borde verde"
                                    } else if (state.boardTiles.isEmpty()) {
                                        "Tu turno: toca una ficha para abrir la mesa"
                                    } else {
                                        "Tu turno: toca una ficha con borde verde para tirarla"
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE2E8F0),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    } else if (state.boneyard.isNotEmpty()) {
                        // En dominó real: Si no tienes fichas para tirar pero hay en el pozo, debes robar del pozo
                        Button(
                            onClick = onDrawTile,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("btn_draw_tile")
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sin jugada — Robar del pozo (${state.boneyard.size})",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else {
                        // En dominó real: SOLO se puede pasar cuando NO tienes fichas jugables Y el pozo está vacío
                        Button(
                            onClick = onPassTurn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626),
                                contentColor = Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("btn_pass_turn")
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sin fichas jugables ni pozo — Pasar Turno",
                                fontSize = 12.sp,
                                color = Color(0xFFFEE2E2),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                if (state.status == TableGameStatus.ROUND_OVER || state.status == TableGameStatus.GAME_OVER) {
                    Button(
                        onClick = onNextRound,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .padding(vertical = 2.dp)
                            .testTag("btn_next_round_bottom")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.status == TableGameStatus.GAME_OVER) "Nueva Partida" else "Siguiente Ronda",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Human Tiles in Hand with Enhanced Ceramic Visuals & Highlight
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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
                                            val fitsLeft = state.boardTiles.isNotEmpty() && (state.leftEnd == null || tile.canMatch(state.leftEnd))
                                            val fitsRight = state.boardTiles.isNotEmpty() && (state.rightEnd == null || tile.canMatch(state.rightEnd))
                                            if (fitsLeft && !fitsRight) {
                                                onPlayTile(tile, TilePlacement.LEFT)
                                            } else if (fitsRight && !fitsLeft) {
                                                onPlayTile(tile, TilePlacement.RIGHT)
                                            } else {
                                                onSelectTile(null)
                                            }
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
                                width = 42.dp,
                                height = 84.dp,
                                isHighlighted = isSelected,
                                canPlayBorder = canPlay && !isSelected,
                                dimmed = !canPlay && isHumanTurn
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Round / Game Over Overlay Dialog over full table view
    if (state.status == TableGameStatus.ROUND_OVER || state.status == TableGameStatus.GAME_OVER) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            RoundOverOverlay(
                state = state,
                onNextRound = onNextRound
            )
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
        color = if (isTurn) color.copy(alpha = 0.22f) else Color(0xFF131D2F).copy(alpha = 0.88f),
        border = BorderStroke(
            width = if (isTurn) 1.8.dp else if (isPartner) 1.2.dp else 1.dp,
            color = if (isTurn) color else if (isPartner) Color(0xFF38BDF8).copy(alpha = 0.65f) else Color(0xFF334155).copy(alpha = 0.7f)
        ),
        shadowElevation = if (isTurn) 4.dp else 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Player name with status icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
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
                    fontWeight = if (isTurn || isPartner) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Visible Tiles Area: Distinct Ivory Dominos & clear count pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Clear, large, bright ivory domino tiles
                val tileCount = player.hand.size
                val tileWidth = when {
                    tileCount > 6 -> 8.5.dp
                    tileCount > 4 -> 9.5.dp
                    else -> 11.dp
                }
                val tileHeight = when {
                    tileCount > 6 -> 15.dp
                    tileCount > 4 -> 17.dp
                    else -> 19.dp
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(tileCount) {
                        DominoTileBackView(
                            width = tileWidth,
                            height = tileHeight,
                            isIvoryStyle = true
                        )
                    }
                }

                Spacer(modifier = Modifier.width(5.dp))

                // Score / Points pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.9f),
                    border = BorderStroke(0.8.dp, DominoGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${player.totalScore}p",
                        color = DominoGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
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

    val humanWonRound = state.roundWinnerIndex == 0
    val humanTeamWonRound = isTeamsMode && (state.roundWinnerIndex == 0 || state.roundWinnerIndex == 2)
    val humanWonGame = state.winnerPlayerIndex == 0 || (isTeamsMode && (state.winnerPlayerIndex == 0 || state.winnerPlayerIndex == 2))

    val overlayTitle = when {
        state.status == TableGameStatus.GAME_OVER -> {
            if (humanWonGame) "¡Ganaste la Partida!" else "¡Partida Terminada!"
        }
        humanWonRound -> "¡Ganaste la Ronda!"
        humanTeamWonRound -> "¡Ganó tu Pareja!"
        else -> "¡Fin de Ronda!"
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.98f),
        border = BorderStroke(1.5.dp, DominoGold.copy(alpha = 0.8f)),
        shadowElevation = 16.dp,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.92f)
            .heightIn(max = 580.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
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
                text = overlayTitle,
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

@Composable
private fun AnimatedPlacedTileView(
    placedTile: PlacedBoardTile,
    isNewlyPlaced: Boolean,
    sourceStartX: Float,
    sourceStartY: Float,
    sourceScale: Float,
    sourceRotation: Float,
    canPlayLeft: Boolean,
    canPlayRight: Boolean,
    onPlayClick: () -> Unit
) {
    // Only animate flight from player if this tile was just placed on the board in this turn.
    // Existing tiles never run the flight animation and start already settled at (0, 0).
    val shouldAnimateFlight = remember(placedTile.tile.id) { isNewlyPlaced }

    val flyOffsetX = remember(placedTile.tile.id) {
        Animatable(if (shouldAnimateFlight) sourceStartX else 0f)
    }
    val flyOffsetY = remember(placedTile.tile.id) {
        Animatable(if (shouldAnimateFlight) sourceStartY else 0f)
    }
    val flyScale = remember(placedTile.tile.id) {
        Animatable(if (shouldAnimateFlight) sourceScale else 1f)
    }
    val flyAlpha = remember(placedTile.tile.id) {
        Animatable(if (shouldAnimateFlight) 0.15f else 1f)
    }
    val flyRotation = remember(placedTile.tile.id) {
        Animatable(if (shouldAnimateFlight) sourceRotation else 0f)
    }

    if (shouldAnimateFlight) {
        LaunchedEffect(placedTile.tile.id) {
            launch {
                flyOffsetX.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 320f)
                )
            }
            launch {
                flyOffsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 320f)
                )
            }
            launch {
                flyScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.76f, stiffness = 340f)
                )
            }
            launch {
                flyAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                )
            }
            launch {
                flyRotation.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 320f)
                )
            }
        }
    }

    // Base position for tiles on table:
    // If table coordinates shift when adding a tile on the left branch,
    // existing tiles slide smoothly to their new normalized base without any flight or jump
    val animBaseX by animateFloatAsState(
        targetValue = placedTile.x,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
        label = "tile_x_${placedTile.tile.id}"
    )
    val animBaseY by animateFloatAsState(
        targetValue = placedTile.y,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f),
        label = "tile_y_${placedTile.tile.id}"
    )

    val isInFlight = shouldAnimateFlight && (flyOffsetX.value != 0f || flyOffsetY.value != 0f)

    Box(
        modifier = Modifier
            .offset(
                x = (animBaseX + flyOffsetX.value).dp,
                y = (animBaseY + flyOffsetY.value).dp
            )
            .size(placedTile.width.dp, placedTile.height.dp)
            .zIndex(if (isInFlight || isNewlyPlaced) 20f else 1f)
            .graphicsLayer {
                alpha = flyAlpha.value
                scaleX = flyScale.value
                scaleY = flyScale.value
                rotationZ = flyRotation.value
                shadowElevation = if (isInFlight) 16f else 2f
            }
            .then(
                if (canPlayLeft || canPlayRight) {
                    Modifier.clickable { onPlayClick() }
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

@Composable
fun DominoRoomWaitingView(
    state: DominoTableState,
    onStartGame: () -> Unit,
    onAddGuest: (String) -> Unit,
    onRemoveGuest: (Int) -> Unit,
    onFillBotsAndStart: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddGuestDialog by remember { mutableStateOf(false) }
    var guestNameInput by remember { mutableStateOf("") }
    var codeCopied by remember { mutableStateOf(false) }

    val requiredCount = state.targetPlayerCount
    val currentCount = state.players.size
    val isRoomFull = currentCount >= requiredCount
    val code = state.roomCode ?: "DOM"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B132B),
                        Color(0xFF091024),
                        Color(0xFF020617)
                    )
                )
            )
    ) {
        // SCROLLABLE BODY (Fits comfortably without scrolling on normal screens, but scrolls if screen is very short)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isRoomFull) 82.dp else 126.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 580.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP: Compact Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = DominoGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.2.dp, DominoGold.copy(alpha = 0.7f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = DominoGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SALA DE INVITADOS",
                            color = DominoGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Partida de $requiredCount Jugadores" +
                                    if (state.playMode.isTeams && requiredCount == 4) " • Parejas (2 vs 2)" else " • Individual",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(38.dp).testTag("btn_close_waiting_room")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Salir de la Sala",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CARD 1: CÓDIGO DE LA SALA (Exact layout from screenshot)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.9f),
                border = BorderStroke(1.5.dp, DominoGold.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CÓDIGO DE LA SALA",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = code,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        color = DominoGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Comparte este código para que tus invitados se unan desde la app",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Button: Copiar Código
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Código de Sala", code)
                                clipboard.setPrimaryClip(clip)
                                codeCopied = true
                                Toast.makeText(context, "Código $code copiado", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, DominoGold.copy(alpha = 0.85f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0x15FFFFFF)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_copy_room_code")
                        ) {
                            Icon(
                                imageVector = if (codeCopied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                contentDescription = "Copiar Código",
                                tint = DominoGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (codeCopied) "Copiado" else "Copiar Código",
                                color = DominoGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }

                        // Button: Compartir
                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "¡Únete a mi partida de dominó! Abre la app e ingresa el código de sala: $code"
                                    )
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Compartir código de sala")
                                context.startActivity(shareIntent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DominoGold),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_share_room_code")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir Código",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Compartir",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CARD 2: PARTICIPANTES CONECTADOS (Exact layout from screenshot)
            val remainingCount = (requiredCount - currentCount).coerceAtLeast(0)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.9f),
                border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Participantes Conectados",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isRoomFull) Color(0xFF10B981) else DominoGold
                        ) {
                            Text(
                                text = "$currentCount de $requiredCount",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (currentCount.toFloat() / requiredCount.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (isRoomFull) Color(0xFF10B981) else DominoGold,
                        trackColor = Color.White.copy(alpha = 0.12f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isRoomFull) {
                            "¡Mesa completa! Lista para iniciar la partida."
                        } else {
                            "Esperando a que entren $remainingCount participante(s) más con el código para iniciar la mano."
                        },
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ASIENTOS DE LA MESA EN CUADRÍCULA (2 columnas, enlarged)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ASIENTOS DE LA MESA",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isRoomFull) "Mesa Completa" else "Esperando participantes",
                    color = if (isRoomFull) Color(0xFF34D399) else Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val rowsCount = (requiredCount + 1) / 2
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (rowIndex in 0 until rowsCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (colIndex in 0 until 2) {
                            val seatIndex = rowIndex * 2 + colIndex
                            if (seatIndex < requiredCount) {
                                val player = state.players.getOrNull(seatIndex)
                                Box(modifier = Modifier.weight(1f)) {
                                    if (player != null) {
                                        val isHost = seatIndex == 0
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF1E293B).copy(alpha = 0.9f),
                                            border = BorderStroke(
                                                1.2.dp,
                                                if (isHost) DominoGold.copy(alpha = 0.8f) else Color(0xFF10B981).copy(alpha = 0.7f)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = PlayerColors.getOrElse(seatIndex) { Color(0xFF38BDF8) },
                                                        modifier = Modifier.size(30.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = player.name.firstOrNull()?.uppercase() ?: "J",
                                                                color = Color.Black,
                                                                fontWeight = FontWeight.Black,
                                                                fontSize = 13.sp
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = player.name,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            maxLines = 1
                                                        )
                                                        Text(
                                                            text = if (isHost) "Anfitrión" else "Invitado",
                                                            color = if (isHost) DominoGold else Color(0xFF38BDF8),
                                                            fontSize = 10.5.sp
                                                        )
                                                    }
                                                }

                                                if (!isHost) {
                                                    IconButton(
                                                        onClick = { onRemoveGuest(seatIndex) },
                                                        modifier = Modifier.size(26.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Remover",
                                                            tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color(0xFF34D399),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Empty Seat Waiting
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF0F172A).copy(alpha = 0.5f),
                                            border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.6f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color.White.copy(alpha = 0.06f),
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.HourglassEmpty,
                                                            contentDescription = null,
                                                            tint = Color(0xFF64748B),
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "Asiento #${seatIndex + 1}",
                                                        color = Color(0xFF94A3B8),
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = "Esperando...",
                                                        color = Color(0xFF64748B),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // STICKY BOTTOM ACTIONS BAR: Always directly visible and accessible without scrolling
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color(0xFF0F172A).copy(alpha = 0.97f),
            border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.7f)),
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 580.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isRoomFull) {
                        // All players ready - Host can start!
                        Button(
                            onClick = onStartGame,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DominoGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_start_friends_match")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡INICIAR PARTIDA Y REPARTIR!",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        // Not full yet - Option to add guest with code or complete with bots
                        Button(
                            onClick = {
                                guestNameInput = "Invitado ${currentCount + 1}"
                                showAddGuestDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_simulate_guest_join")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ingresar Invitado con Código",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onFillBotsAndStart,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.2.dp, DominoGold.copy(alpha = 0.85f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_fill_bots_and_start")
                            ) {
                                Text(
                                    text = "Completar con Bots",
                                    color = DominoGold,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }

                            OutlinedButton(
                                onClick = onCancel,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF64748B)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_cancel_room")
                            ) {
                                Text(
                                    text = "Cancelar Sala",
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog to add guest
    if (showAddGuestDialog) {
        AlertDialog(
            onDismissRequest = { showAddGuestDialog = false },
            title = {
                Text(
                    text = "Unirse a la Sala con Código",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Ingresando a la sala con código: $code",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = guestNameInput,
                        onValueChange = { guestNameInput = it },
                        label = { Text("Nombre del invitado") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = guestNameInput.ifBlank { "Invitado ${currentCount + 1}" }
                        onAddGuest(name)
                        showAddGuestDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DominoGold)
                ) {
                    Text("Ingresar a la Sala", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGuestDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

