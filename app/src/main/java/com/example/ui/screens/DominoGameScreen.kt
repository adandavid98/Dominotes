package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ScoringDisplayMode
import com.example.ui.ActiveGameState
import com.example.ui.DominoViewModel
import com.example.ui.MainAppTab
import com.example.ui.components.AddRoundDialog
import com.example.ui.components.DominoTableView
import com.example.ui.components.DominoTileView
import com.example.ui.components.FriendsRoomDialog
import com.example.ui.components.GoogleAccountDialog
import com.example.ui.components.NewGameDialog
import com.example.ui.components.RoundsTableView
import com.example.ui.components.ScoreCard
import com.example.ui.components.TrancaCalculatorDialog
import com.example.ui.components.VictoryDialog
import com.example.ui.theme.DominoGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DominoGameScreen(
    state: ActiveGameState,
    viewModel: DominoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val tableState by viewModel.tableState.collectAsStateWithLifecycle()
    val selectedTile by viewModel.selectedTile.collectAsStateWithLifecycle()
    val availableUpdate by viewModel.availableUpdate.collectAsStateWithLifecycle()

    val highestScore = state.scores.maxOrNull() ?: 0

    Scaffold(
        modifier = modifier.testTag("domino_game_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DominoTileView(topPips = 4, bottomPips = 5, width = 24.dp, height = 40.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (state.currentTab == MainAppTab.SCORER) state.title else "Mesa de Dominó",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (state.currentTab == MainAppTab.SCORER)
                                    "Meta: ${state.targetScore} pts • ${state.gameMode.displayName}"
                                else if (tableState.roomCode != null)
                                    "Sala Online: #${tableState.roomCode} • Meta ${tableState.targetScore} pts"
                                else
                                    "Partida vs Bots IA • Meta ${tableState.targetScore} pts",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Google Account Button
                    IconButton(
                        onClick = { viewModel.setShowAuthDialog(true) },
                        modifier = Modifier.testTag("btn_google_account")
                    ) {
                        if (currentUser != null) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser!!.displayName.take(1).uppercase(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Cuenta Google"
                            )
                        }
                    }

                    if (state.currentTab == MainAppTab.PLAY_DOMINO) {
                        if (!state.inTableLobby) {
                            // Button to return to Lobby / Change Room
                            IconButton(
                                onClick = { viewModel.setInTableLobby(true) },
                                modifier = Modifier.testTag("btn_table_lobby")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MeetingRoom,
                                    contentDescription = "Lobby de Juego",
                                    tint = DominoGold
                                )
                            }
                        }

                        // Friends Room / Multiplayer button
                        IconButton(
                            onClick = { viewModel.setShowFriendsDialog(true) },
                            modifier = Modifier.testTag("btn_friends_room")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Jugar con Amigos",
                                tint = DominoGold
                            )
                        }

                        // Restart table game (only when active table is showing)
                        if (!state.inTableLobby) {
                            IconButton(
                                onClick = {
                                    val botCount = (tableState.players.size - 1).coerceIn(1, 3)
                                    viewModel.startNewDominoTableGame(
                                        roomCode = tableState.roomCode,
                                        botCount = botCount,
                                        targetScore = tableState.targetScore
                                    )
                                },
                                modifier = Modifier.testTag("btn_restart_table")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reiniciar Mesa"
                                )
                            }
                        }
                    } else {
                        // Undo last round in Scorer
                        IconButton(
                            onClick = { viewModel.undoLastRound() },
                            enabled = state.rounds.isNotEmpty(),
                            modifier = Modifier.testTag("btn_undo_action")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Deshacer última ronda",
                                tint = if (state.rounds.isNotEmpty()) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }

                        // Match History button
                        IconButton(
                            onClick = { viewModel.setShowHistoryScreen(true) },
                            modifier = Modifier.testTag("btn_view_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Historial de partidas"
                            )
                        }

                        // More Menu for Scorer
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.testTag("btn_more_menu")
                            ) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Opciones")
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Nueva Partida") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.setShowNewGameDialog(true)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.PostAdd, contentDescription = null)
                                    },
                                    modifier = Modifier.testTag("menu_new_game")
                                )
                                DropdownMenuItem(
                                    text = { Text("Reiniciar Marcador") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.resetGame()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.RestartAlt, contentDescription = null)
                                    },
                                    modifier = Modifier.testTag("menu_reset_game")
                                )
                                DropdownMenuItem(
                                    text = { Text("Calculadora de Tranca") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.setShowTrancaCalculator(true)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Calculate, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Buscar actualizaciones") },
                                    onClick = {
                                        showMenu = false
                                        Toast.makeText(context, "Verificando en GitHub...", Toast.LENGTH_SHORT).show()
                                        viewModel.checkForAppUpdates { _, message ->
                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = state.currentTab == MainAppTab.SCORER,
                    onClick = { viewModel.setCurrentTab(MainAppTab.SCORER) },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                    label = { Text("Anotador") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_scorer")
                )
                NavigationBarItem(
                    selected = state.currentTab == MainAppTab.PLAY_DOMINO,
                    onClick = { viewModel.setCurrentTab(MainAppTab.PLAY_DOMINO) },
                    icon = { Icon(Icons.Default.TableRestaurant, contentDescription = null) },
                    label = { Text("Juego de Mesa") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DominoGold,
                        selectedTextColor = DominoGold
                    ),
                    modifier = Modifier.testTag("nav_play_domino")
                )
            }
        },
        floatingActionButton = {
            if (state.currentTab == MainAppTab.SCORER) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.setShowAddRoundDialog(true) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Anotar Ronda", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .testTag("btn_fab_add_round")
                        .windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }
    ) { innerPadding ->
        if (state.currentTab == MainAppTab.PLAY_DOMINO) {
            if (state.inTableLobby) {
                // Table Mode Lobby: Select Bots vs Friends & Player count (2, 3, 4)
                DominoLobbyScreen(
                    currentDisplayName = currentUser?.displayName ?: "Tú",
                    onStartBotGame = { totalPlayers, targetScore, playMode ->
                        val botCount = (totalPlayers - 1).coerceIn(1, 3)
                        viewModel.startNewDominoTableGame(
                            roomCode = null,
                            botCount = botCount,
                            targetScore = targetScore,
                            playMode = playMode
                        )
                    },
                    onOpenFriendsDialog = { viewModel.setShowFriendsDialog(true) },
                    onEditProfile = { viewModel.setShowAuthDialog(true) },
                    hasActiveGame = tableState.boardTiles.isNotEmpty() || tableState.players.any { it.totalScore > 0 },
                    onResumeGame = { viewModel.setInTableLobby(false) },
                    availableUpdate = availableUpdate,
                    onUpdateClick = { url -> viewModel.downloadUpdate(url) },
                    onDismissUpdate = { viewModel.dismissUpdateBanner() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            } else {
                // Interactive Domino Table Screen (Play vs Bots / Online Friends)
                DominoTableView(
                    state = tableState,
                    selectedTile = selectedTile,
                    onSelectTile = { viewModel.setSelectedTile(it) },
                    onPlayTile = { tile, placement -> viewModel.playHumanTile(tile, placement) },
                    onDrawTile = { viewModel.drawHumanTile() },
                    onPassTurn = { viewModel.passHumanTurn() },
                    onNextRound = { viewModel.nextTableRound() },
                    onStartWaitingGame = { viewModel.startWaitingRoomGame() },
                    onAddGuest = { viewModel.addGuestWithCode(it) },
                    onRemoveGuest = { viewModel.removePlayerFromWaitingRoom(it) },
                    onFillBotsAndStart = { viewModel.fillRemainingSlotsWithBotsAndStart() },
                    onCancelWaitingRoom = { viewModel.cancelWaitingRoom() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        } else {
            // Scorer Screen (Anotador Actual)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // In-App Update Notice for Scorer Screen
                if (availableUpdate != null && availableUpdate!!.hasUpdate) {
                    Surface(
                        onClick = { viewModel.downloadUpdate(availableUpdate!!.downloadUrl) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E3A8A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = Color(0xFF93C5FD),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Nueva versión disponible",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Actualizar",
                                color = Color(0xFF93C5FD),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                // Mode Switcher: "Por Rondas" vs "Acumulación Total"
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val isRounds = state.displayMode == ScoringDisplayMode.RONDAS
                        Surface(
                            onClick = { viewModel.setDisplayMode(ScoringDisplayMode.RONDAS) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isRounds) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (isRounds) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_mode_rounds")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewAgenda,
                                    contentDescription = null,
                                    tint = if (isRounds) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Por Rondas",
                                    fontSize = 13.sp,
                                    fontWeight = if (isRounds) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isRounds) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isQuick = state.displayMode == ScoringDisplayMode.ACUMULACION
                        Surface(
                            onClick = { viewModel.setDisplayMode(ScoringDisplayMode.ACUMULACION) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isQuick) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (isQuick) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_mode_quick")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = if (isQuick) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Acumulación Total",
                                    fontSize = 13.sp,
                                    fontWeight = if (isQuick) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isQuick) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Bar with direct "Nueva Partida" button for both "Por Rondas" and "Acumulación Total"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.setShowNewGameDialog(true) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_scorer_new_game")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PostAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Nueva Partida",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (state.rounds.isNotEmpty()) {
                        Text(
                            text = "Ronda #${state.rounds.size + 1}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Score Cards Grid (2 to 4 players/teams)
                val chunkedPlayers = state.playerNames.indices.chunked(2)
                chunkedPlayers.forEach { rowIndices ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowIndices.forEach { playerIndex ->
                            val score = state.scores.getOrElse(playerIndex) { 0 }
                            val isLeader = highestScore > 0 && score == highestScore
                            ScoreCard(
                                playerIndex = playerIndex,
                                name = state.playerNames[playerIndex],
                                score = score,
                                targetScore = state.targetScore,
                                isLeader = isLeader,
                                showQuickControls = state.displayMode == ScoringDisplayMode.ACUMULACION,
                                onQuickAdd = { pts ->
                                    viewModel.quickAddPoints(playerIndex, pts)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowIndices.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Table of Detailed Rounds
                AnimatedVisibility(
                    visible = state.rounds.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Historial de Rondas (${state.rounds.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            if (state.rounds.isNotEmpty()) {
                                Text(
                                    text = "Ronda actual: #${state.rounds.size + 1}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        RoundsTableView(
                            rounds = state.rounds,
                            playerNames = state.playerNames,
                            onDeleteRound = { roundId ->
                                viewModel.deleteRound(roundId)
                            }
                        )
                    }
                }

                // Extra bottom space for scrolling above FAB
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dialogs
    if (state.showAddRoundDialog) {
        AddRoundDialog(
            playerNames = state.playerNames,
            currentScores = state.scores,
            targetScore = state.targetScore,
            onDismiss = { viewModel.setShowAddRoundDialog(false) },
            onConfirm = { winnerIndex, points, bonusTag ->
                viewModel.addRound(winnerIndex, points, bonusTag)
            },
            onOpenTrancaCalculator = {
                viewModel.setShowAddRoundDialog(false)
                viewModel.setShowTrancaCalculator(true)
            }
        )
    }

    if (state.showTrancaCalculator) {
        TrancaCalculatorDialog(
            playerNames = state.playerNames,
            onDismiss = { viewModel.setShowTrancaCalculator(false) },
            onApplyTrancaRound = { winnerIndex, points, bonusTag ->
                viewModel.setShowTrancaCalculator(false)
                viewModel.addRound(winnerIndex, points, bonusTag)
            }
        )
    }

    if (state.showNewGameDialog) {
        NewGameDialog(
            onDismiss = { viewModel.setShowNewGameDialog(false) },
            onStartGame = { title, mode, target, names ->
                viewModel.startNewGame(title, mode, target, names)
            }
        )
    }

    if (state.showVictoryDialog && state.winnerIndex != null && state.winnerName != null) {
        VictoryDialog(
            winnerName = state.winnerName,
            winnerIndex = state.winnerIndex,
            playerNames = state.playerNames,
            scores = state.scores,
            targetScore = state.targetScore,
            totalRounds = state.rounds.size,
            onDismiss = { viewModel.setShowVictoryDialog(false) },
            onRematch = {
                viewModel.resetGame()
            }
        )
    }

    if (state.showAuthDialog) {
        GoogleAccountDialog(
            currentUser = currentUser,
            onSignIn = { email, name, photoUrl -> viewModel.signInGoogle(email, name, photoUrl) },
            onSignOut = { viewModel.signOutGoogle() },
            onUpdateName = { viewModel.updatePlayerDisplayName(it) },
            onDismiss = { viewModel.setShowAuthDialog(false) }
        )
    }

    if (state.showFriendsDialog) {
        FriendsRoomDialog(
            currentRoomCode = tableState.roomCode,
            userDisplayName = currentUser?.displayName ?: "Tú",
            initialPlayerCount = tableState.targetPlayerCount,
            onCreateRoom = { code, count -> viewModel.createFriendsRoom(code, count) },
            onJoinRoom = { code, guestName -> viewModel.joinFriendsRoom(code, guestName ?: "Invitado") },
            onDismiss = { viewModel.setShowFriendsDialog(false) }
        )
    }
}
