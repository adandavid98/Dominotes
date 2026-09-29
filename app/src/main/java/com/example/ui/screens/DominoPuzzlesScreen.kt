package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.domino.DominoTile
import com.example.data.domino.TilePlacement
import com.example.data.puzzle.DominoPuzzle
import com.example.data.puzzle.DominoPuzzleRepository
import com.example.data.theme.TileSkinStyle
import com.example.ui.components.DominoTileView
import com.example.ui.components.HorizontalDominoTileView
import com.example.ui.theme.DominoGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DominoPuzzlesScreen(
    tileSkin: TileSkinStyle,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val puzzles = DominoPuzzleRepository.allPuzzles
    var currentPuzzleIndex by remember { mutableStateOf(0) }
    val currentPuzzle = puzzles.getOrElse(currentPuzzleIndex) { puzzles.first() }

    var selectedTile by remember { mutableStateOf<DominoTile?>(null) }
    var selectedPlacement by remember { mutableStateOf(TilePlacement.LEFT) }
    var isSolved by remember { mutableStateOf(false) }
    var showErrorFeedback by remember { mutableStateOf(false) }
    var showClueDialog by remember { mutableStateOf(false) }

    fun checkMove(tile: DominoTile, placement: TilePlacement) {
        if (tile.id == currentPuzzle.winningTile.id && placement == currentPuzzle.winningPlacement) {
            isSolved = true
            showErrorFeedback = false
        } else {
            showErrorFeedback = true
        }
    }

    Scaffold(
        modifier = modifier.testTag("domino_puzzles_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = DominoGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Desafíos & Puzzles",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("puzzles_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showClueDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Ver Pista",
                            tint = DominoGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Puzzle Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                puzzles.forEachIndexed { index, p ->
                    val isSelected = index == currentPuzzleIndex
                    Surface(
                        onClick = {
                            currentPuzzleIndex = index
                            selectedTile = null
                            isSolved = false
                            showErrorFeedback = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) DominoGold else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) DominoGold else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (p.isDailyChallenge) {
                                Text(text = "🔥 ", fontSize = 12.sp)
                            }
                            Text(
                                text = p.category,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Challenge Title & Scenario Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentPuzzle.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DominoGold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0F172A)
                        ) {
                            Text(
                                text = currentPuzzle.difficulty,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DominoGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentPuzzle.scenarioDescription,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Board Scenario Preview (Ends and Sample tiles)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                border = BorderStroke(1.dp, Color(0xFF059669)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ESTADO DE LA MESA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF022C22),
                            border = BorderStroke(1.dp, DominoGold)
                        ) {
                            Text(
                                text = "Extremo Izq: [${currentPuzzle.boardLeftEnd}]",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DominoGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF022C22),
                            border = BorderStroke(1.dp, DominoGold)
                        ) {
                            Text(
                                text = "Extremo Der: [${currentPuzzle.boardRightEnd}]",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DominoGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sample board chain tiles
                    if (currentPuzzle.sampleBoardTiles.isNotEmpty()) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            currentPuzzle.sampleBoardTiles.forEach { tile ->
                                HorizontalDominoTileView(
                                    leftPips = tile.left,
                                    rightPips = tile.right,
                                    tileSkin = tileSkin,
                                    width = 54.dp,
                                    height = 28.dp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Mesa limpia: Apertura inicial de ronda",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Player Hand (Tap to select tile)
            Text(
                text = "Tu Mano de Fichas (Selecciona tu jugada maestra):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                currentPuzzle.playerHand.forEach { tile ->
                    val isSelected = selectedTile?.id == tile.id
                    DominoTileView(
                        topPips = tile.left,
                        bottomPips = tile.right,
                        tileSkin = tileSkin,
                        width = 46.dp,
                        height = 88.dp,
                        isHighlighted = isSelected,
                        canPlayBorder = isSelected,
                        modifier = Modifier
                            .clickable {
                                selectedTile = tile
                                showErrorFeedback = false
                            }
                    )
                }
            }

            // Placement Selector & Execute Button
            if (selectedTile != null && !isSolved) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Ficha elegida: [${selectedTile!!.left}|${selectedTile!!.right}]. ¿Por cuál extremo la juegas?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    selectedPlacement = TilePlacement.LEFT
                                    checkMove(selectedTile!!, TilePlacement.LEFT)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedPlacement == TilePlacement.LEFT) DominoGold else MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "⬅ Extremo Izq",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedPlacement == TilePlacement.LEFT) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Button(
                                onClick = {
                                    selectedPlacement = TilePlacement.RIGHT
                                    checkMove(selectedTile!!, TilePlacement.RIGHT)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedPlacement == TilePlacement.RIGHT) DominoGold else MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Extremo Der ➡",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedPlacement == TilePlacement.RIGHT) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Error feedback
            if (showErrorFeedback) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF7F1D1D),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "❌", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Esa jugada le da ventaja al rival o no tranca con el menor puntaje. ¡Revisa la pista o prueba otra combinación!",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Victory & Solution Explanation Card
            if (isSolved) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                    border = BorderStroke(2.dp, DominoGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎉 ⭐⭐⭐", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "¡JUGADA PERFECTA!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DominoGold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentPuzzle.solutionExplanation,
                            fontSize = 13.sp,
                            color = Color.White,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val nextIdx = (currentPuzzleIndex + 1) % puzzles.size
                                currentPuzzleIndex = nextIdx
                                selectedTile = null
                                isSolved = false
                                showErrorFeedback = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DominoGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Siguiente Desafío",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClueDialog) {
        AlertDialog(
            onDismissRequest = { showClueDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = DominoGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Pista Táctica", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(text = currentPuzzle.clueText, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = { showClueDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DominoGold)
                ) {
                    Text("Entendido", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
