package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.domino.DominoGamePlayMode
import com.example.ui.components.DominoTileView
import com.example.ui.theme.DominoGold

enum class LobbyGameType {
    BOTS,
    FRIENDS
}

@Composable
fun DominoLobbyScreen(
    currentDisplayName: String,
    onStartBotGame: (totalPlayers: Int, targetScore: Int, playMode: DominoGamePlayMode) -> Unit,
    onOpenFriendsDialog: () -> Unit,
    onEditProfile: () -> Unit,
    hasActiveGame: Boolean,
    onResumeGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf(LobbyGameType.BOTS) }
    var selectedPlayerCount by remember { mutableIntStateOf(4) } // 2, 3, or 4 players
    var selectedPlayMode by remember { mutableStateOf(DominoGamePlayMode.PAREJAS_2V2) }
    var selectedTargetScore by remember { mutableIntStateOf(100) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF064E3B), // Emerald table green
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF020617)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Header Banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.85f)),
            border = BorderStroke(1.5.dp, DominoGold.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    DominoTileView(topPips = 6, bottomPips = 6, width = 28.dp, height = 48.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Lobby: Bienvenido!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Mesa interactiva de dominó",
                            fontSize = 12.sp,
                            color = DominoGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Player Profile Pill
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = Color(0x33FFFFFF),
                    border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                    modifier = Modifier.clickable { onEditProfile() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(DominoGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Jugador: $currentDisplayName",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cambiar",
                            color = DominoGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Optional Resume Game Banner if a match is already ongoing
        if (hasActiveGame) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResumeGame() }
                    .testTag("btn_resume_table_game")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Partida en progreso en la mesa",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Toca para regresar a la partida actual",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Button(
                        onClick = onResumeGame,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Continuar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: Game Mode Selection (Bots vs Amigos / Invitados)
        Text(
            text = "MODO DE JUEGO",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Option 1: Play vs Bots
            Card(
                onClick = { selectedType = LobbyGameType.BOTS },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedType == LobbyGameType.BOTS)
                        Color(0xFF047857) // Deep active emerald
                    else
                        Color(0xFF1E293B).copy(alpha = 0.8f)
                ),
                border = BorderStroke(
                    width = if (selectedType == LobbyGameType.BOTS) 2.dp else 1.dp,
                    color = if (selectedType == LobbyGameType.BOTS) DominoGold else Color(0x33FFFFFF)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("lobby_option_bots")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = if (selectedType == LobbyGameType.BOTS) DominoGold else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Jugar con Bots",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "IA inteligente",
                        color = if (selectedType == LobbyGameType.BOTS) Color(0xFFD1FAE5) else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Option 2: Play with Guests / Friends
            Card(
                onClick = { selectedType = LobbyGameType.FRIENDS },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedType == LobbyGameType.FRIENDS)
                        Color(0xFF047857)
                    else
                        Color(0xFF1E293B).copy(alpha = 0.8f)
                ),
                border = BorderStroke(
                    width = if (selectedType == LobbyGameType.FRIENDS) 2.dp else 1.dp,
                    color = if (selectedType == LobbyGameType.FRIENDS) DominoGold else Color(0x33FFFFFF)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("lobby_option_friends")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = if (selectedType == LobbyGameType.FRIENDS) DominoGold else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Con Invitados",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Crear o unirse a sala",
                        color = if (selectedType == LobbyGameType.FRIENDS) Color(0xFFD1FAE5) else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION: Number of Players (2, 3 o 4 personas/participantes)
        Text(
            text = "CANTIDAD DE PERSONAS / JUGADORES",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 6.dp)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple(2, "2 Jugadores", "Tú vs 1 oponente"),
                        Triple(3, "3 Jugadores", "Tú vs 2 oponentes"),
                        Triple(4, "4 Jugadores", "Mesa clásica (4)")
                    ).forEach { (count, title, sub) ->
                        val isSelected = selectedPlayerCount == count
                        Surface(
                            onClick = { selectedPlayerCount = count },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) DominoGold.copy(alpha = 0.2f) else Color(0x22FFFFFF),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) DominoGold else Color(0x33FFFFFF)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("player_count_$count")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (count == 2) Icons.Default.Person else Icons.Default.Group,
                                        contentDescription = null,
                                        tint = if (isSelected) DominoGold else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$count",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = if (isSelected) DominoGold else Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (count == 2) "2 personas" else if (count == 3) "3 personas" else "4 personas",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (selectedType == LobbyGameType.BOTS) {
                        when (selectedPlayerCount) {
                            2 -> "Jugarás mano a mano contra 1 Bot inteligente (7 fichas c/u, boneyard de 14)."
                            3 -> "Partida individual entre 3 jugadores (Tú y 2 Bots)."
                            else -> "Mesa completa tradicional de 4 jugadores (Tú y 3 Bots)."
                        }
                    } else {
                        when (selectedPlayerCount) {
                            2 -> "Sala para 2 personas cara a cara con código compartido."
                            3 -> "Sala para 3 personas jugando individual."
                            else -> "Sala para 4 personas (equipos de 2 o todos contra todos)."
                        }
                    },
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 16.sp
                )
            }
        }

        // SECTION: Modalidad en Parejas (2 vs 2) cuando hay 4 jugadores
        if (selectedPlayerCount == 4) {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "MODALIDAD DE EQUIPOS (MESA DE 4)",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Option 1: Parejas 2 vs 2
                val isParejas = selectedPlayMode == DominoGamePlayMode.PAREJAS_2V2
                Surface(
                    onClick = { selectedPlayMode = DominoGamePlayMode.PAREJAS_2V2 },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isParejas) Color(0xFF047857).copy(alpha = 0.85f) else Color(0xFF1E293B).copy(alpha = 0.8f),
                    border = BorderStroke(
                        width = if (isParejas) 2.dp else 1.dp,
                        color = if (isParejas) DominoGold else Color(0x33FFFFFF)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_parejas_2v2")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = if (isParejas) DominoGold else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "En Parejas (2 vs 2)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tú y María vs Carlos y Luis",
                            color = if (isParejas) Color(0xFFD1FAE5) else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Puntos y victoria en equipo",
                            color = if (isParejas) DominoGold else Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Option 2: Individual
                val isIndiv = selectedPlayMode == DominoGamePlayMode.INDIVIDUAL
                Surface(
                    onClick = { selectedPlayMode = DominoGamePlayMode.INDIVIDUAL },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isIndiv) Color(0xFF047857).copy(alpha = 0.85f) else Color(0xFF1E293B).copy(alpha = 0.8f),
                    border = BorderStroke(
                        width = if (isIndiv) 2.dp else 1.dp,
                        color = if (isIndiv) DominoGold else Color(0x33FFFFFF)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mode_individual")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isIndiv) DominoGold else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Individual",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Todos contra todos",
                            color = if (isIndiv) Color(0xFFD1FAE5) else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Cada jugador suma para sí",
                            color = if (isIndiv) DominoGold else Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // SECTION: Target Score (Meta de Puntos)
        Text(
            text = "META DE PUNTOS PARA GANAR",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(50, 100, 150, 200).forEach { score ->
                val isSelected = selectedTargetScore == score
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTargetScore = score },
                    label = {
                        Text(
                            text = "$score pts",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DominoGold,
                        selectedLabelColor = Color.Black,
                        selectedLeadingIconColor = Color.Black,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) DominoGold else Color(0x44FFFFFF),
                        selectedBorderColor = DominoGold,
                        enabled = true,
                        selected = isSelected
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("target_score_$score")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Primary Action Button: Start Game or Create/Join Room
        if (selectedType == LobbyGameType.BOTS) {
            val playModeToUse = if (selectedPlayerCount == 4) selectedPlayMode else DominoGamePlayMode.INDIVIDUAL
            val buttonTitle = if (selectedPlayerCount == 4 && selectedPlayMode == DominoGamePlayMode.PAREJAS_2V2) {
                "Iniciar en Parejas (2 vs 2)"
            } else {
                "Iniciar Partida con Bots ($selectedPlayerCount Jugadores)"
            }

            Button(
                onClick = {
                    onStartBotGame(selectedPlayerCount, selectedTargetScore, playModeToUse)
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DominoGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_lobby_start_game")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonTitle,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        } else {
            // Friends / Guests Mode
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenFriendsDialog,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DominoGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_lobby_create_room")
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Crear o Unirse a Sala de Invitados ($selectedPlayerCount Personas)",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        val playModeToUse = if (selectedPlayerCount == 4) selectedPlayMode else DominoGamePlayMode.INDIVIDUAL
                        onStartBotGame(selectedPlayerCount, selectedTargetScore, playModeToUse)
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DominoGold.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_lobby_quick_table")
                ) {
                    Text(
                        text = if (selectedPlayerCount == 4 && selectedPlayMode == DominoGamePlayMode.PAREJAS_2V2)
                            "Probar Mesa en Parejas (2 vs 2)"
                        else
                            "Probar Mesa de $selectedPlayerCount Personas Ahora",
                        color = DominoGold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
