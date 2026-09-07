package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.GameMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewGameDialog(
    onDismiss: () -> Unit,
    onStartGame: (title: String, mode: GameMode, targetScore: Int, names: List<String>) -> Unit
) {
    var matchTitle by remember { mutableStateOf("Partida de Dominó") }
    var selectedMode by remember { mutableStateOf(GameMode.PAREJAS_2) }
    var selectedTarget by remember { mutableIntStateOf(100) }
    var customTargetText by remember { mutableStateOf("") }
    var isCustomTarget by remember { mutableStateOf(false) }

    val playerNames = remember(selectedMode) {
        mutableStateListOf<String>().apply {
            addAll(selectedMode.defaultTeams)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .testTag("new_game_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nueva Partida",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                Text(
                    text = "Nombre de la partida:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = matchTitle,
                    onValueChange = { matchTitle = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_game_title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Selector
                Text(
                    text = "Modalidad de juego:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameMode.entries.forEach { mode ->
                        val isSelected = selectedMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMode = mode },
                            label = { Text(mode.displayName, fontSize = 12.sp) },
                            modifier = Modifier.testTag("mode_chip_${mode.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Player / Team Names
                Text(
                    text = "Nombres de los equipos / jugadores:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                playerNames.forEachIndexed { index, name ->
                    OutlinedTextField(
                        value = name,
                        onValueChange = { playerNames[index] = it },
                        label = { Text("Equipo / Jugador ${index + 1}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("player_name_input_$index"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Target Score Selector
                Text(
                    text = "Meta de puntos para ganar:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(50, 100, 150, 200, 250).forEach { target ->
                        val isSelected = !isCustomTarget && selectedTarget == target
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTarget = target
                                isCustomTarget = false
                            },
                            label = { Text("$target pts", fontSize = 12.sp) },
                            modifier = Modifier.testTag("target_chip_$target")
                        )
                    }

                    // Custom target chip
                    FilterChip(
                        selected = isCustomTarget,
                        onClick = { isCustomTarget = true },
                        label = { Text("Personalizado", fontSize = 12.sp) }
                    )
                }

                if (isCustomTarget) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customTargetText,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 4) {
                                customTargetText = it
                            }
                        },
                        label = { Text("Puntos personalizados (ej: 120)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_target_input"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalTarget = if (isCustomTarget) {
                                customTargetText.toIntOrNull()?.takeIf { it > 0 } ?: 100
                            } else {
                                selectedTarget
                            }
                            val names = playerNames.mapIndexed { i, n ->
                                n.ifBlank { "Jugador ${i + 1}" }
                            }
                            onStartGame(matchTitle, selectedMode, finalTarget, names)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_confirm_start_game")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Comenzar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
