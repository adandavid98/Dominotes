package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScoringDisplayMode
import com.example.ui.ActiveGameState
import com.example.ui.DominoViewModel
import com.example.ui.components.AddRoundDialog
import com.example.ui.components.DominoTileView
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
    var showMenu by remember { mutableStateOf(false) }

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
                                text = state.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Meta: ${state.targetScore} pts • ${state.gameMode.displayName}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Undo last action button
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

                    // More Menu
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
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
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
                            .testTag("tab_mode_accumulation")
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

            Spacer(modifier = Modifier.height(10.dp))

            // Scoreboards Section
            if (state.playerNames.size == 2) {
                // 2 Teams / Players side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScoreCard(
                        playerIndex = 0,
                        name = state.playerNames[0],
                        score = state.scores[0],
                        targetScore = state.targetScore,
                        isLeader = highestScore > 0 && state.scores[0] == highestScore,
                        showQuickControls = state.displayMode == ScoringDisplayMode.ACUMULACION,
                        onQuickAdd = { delta -> viewModel.quickAddPoints(0, delta) },
                        modifier = Modifier.weight(1f)
                    )

                    ScoreCard(
                        playerIndex = 1,
                        name = state.playerNames[1],
                        score = state.scores[1],
                        targetScore = state.targetScore,
                        isLeader = highestScore > 0 && state.scores[1] == highestScore,
                        showQuickControls = state.displayMode == ScoringDisplayMode.ACUMULACION,
                        onQuickAdd = { delta -> viewModel.quickAddPoints(1, delta) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // 3 or 4 Players grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.playerNames.chunked(2).forEach { rowNames ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowNames.forEach { name ->
                                val idx = state.playerNames.indexOf(name)
                                ScoreCard(
                                    playerIndex = idx,
                                    name = name,
                                    score = state.scores.getOrElse(idx) { 0 },
                                    targetScore = state.targetScore,
                                    isLeader = highestScore > 0 && state.scores.getOrElse(idx) { 0 } == highestScore,
                                    showQuickControls = state.displayMode == ScoringDisplayMode.ACUMULACION,
                                    onQuickAdd = { delta -> viewModel.quickAddPoints(idx, delta) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowNames.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Tool Bar: "Calculadora de Tranca" & "Nueva Partida" shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = { viewModel.setShowTrancaCalculator(true) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_tranca_calculator_shortcut")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = DominoGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Calcular Tranca",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    onClick = { viewModel.setShowNewGameDialog(true) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_new_game_shortcut")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PostAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Nueva Partida",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rounds List Section
            RoundsTableView(
                rounds = state.rounds,
                playerNames = state.playerNames,
                onDeleteRound = { roundId -> viewModel.deleteRound(roundId) }
            )

            // Bottom spacing to avoid floating action button overlap
            Spacer(modifier = Modifier.height(84.dp))
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
}
