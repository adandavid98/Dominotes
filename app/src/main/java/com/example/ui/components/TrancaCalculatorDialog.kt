package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BonusTag
import com.example.ui.theme.DominoGold
import com.example.ui.theme.DominoGreen

@Composable
fun TrancaCalculatorDialog(
    playerNames: List<String>,
    onDismiss: () -> Unit,
    onApplyTrancaRound: (winnerIndex: Int, points: Int, bonusTag: BonusTag) -> Unit
) {
    // Puntos en mano de cada equipo/jugador
    val pipsInHand = remember {
        mutableStateMapOf<Int, String>().apply {
            playerNames.indices.forEach { put(it, "") }
        }
    }

    var scoringRuleSumAll by remember { mutableStateOf(true) } // true = suma fichas contrarias; false = diferencia

    val parsedPoints = playerNames.indices.map { idx ->
        pipsInHand[idx]?.toIntOrNull() ?: 0
    }

    // Identify lowest points (winner)
    val minPoints = parsedPoints.minOrNull() ?: 0
    val winnerIndices = parsedPoints.indices.filter { parsedPoints[it] == minPoints && pipsInHand[it]?.isNotBlank() == true }
    val isDecided = winnerIndices.size == 1 && parsedPoints.any { it > 0 }
    val winningIndex = if (isDecided) winnerIndices.first() else 0

    // Points calculation:
    // Standard rule: Winner receives sum of all opponents' pips (or total table pips)
    val opponentsSum = parsedPoints.filterIndexed { index, _ -> index != winningIndex }.sum()
    val difference = (opponentsSum - parsedPoints.getOrElse(winningIndex) { 0 }).coerceAtLeast(0)
    val awardedPoints = if (scoringRuleSumAll) opponentsSum else difference

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .testTag("tranca_calculator_dialog"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = DominoGold.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = DominoGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Calculadora de Tranca",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Cierre de partida por bloqueo",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Introduce los puntos que le quedaron a cada uno en la mano:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input rows for each player/team
                playerNames.forEachIndexed { index, name ->
                    val color = TeamColors[index % TeamColors.size]
                    val isCurrentWinner = isDecided && winningIndex == index

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrentWinner) color.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(
                            width = if (isCurrentWinner) 2.dp else 1.dp,
                            color = if (isCurrentWinner) color else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (isCurrentWinner) {
                                        Text(
                                            text = "🏆 Menor puntuación (Gana)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = color
                                        )
                                    }
                                }
                            }

                            // Quick add chips & TextField
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = pipsInHand[index] ?: "",
                                    onValueChange = { value ->
                                        if (value.all { it.isDigit() } && value.length <= 3) {
                                            pipsInHand[index] = value
                                        }
                                    },
                                    placeholder = { Text("0") },
                                    modifier = Modifier
                                        .width(70.dp)
                                        .testTag("tranca_input_$index"),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rule Switcher: Sum of opponents vs Difference
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Regla de conteo:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            onClick = { scoringRuleSumAll = true },
                            shape = RoundedCornerShape(8.dp),
                            color = if (scoringRuleSumAll) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            border = BorderStroke(1.dp, if (scoringRuleSumAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = "Suma contraria",
                                fontSize = 11.sp,
                                fontWeight = if (scoringRuleSumAll) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (scoringRuleSumAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            onClick = { scoringRuleSumAll = false },
                            shape = RoundedCornerShape(8.dp),
                            color = if (!scoringRuleSumAll) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            border = BorderStroke(1.dp, if (!scoringRuleSumAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = "Diferencia",
                                fontSize = 11.sp,
                                fontWeight = if (!scoringRuleSumAll) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (!scoringRuleSumAll) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Result Box
                if (isDecided) {
                    val winnerName = playerNames[winningIndex]
                    val winnerColor = TeamColors[winningIndex % TeamColors.size]

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = DominoGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, DominoGreen.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = DominoGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Resultado de la Tranca",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Gana $winnerName con ${parsedPoints[winningIndex]} puntos en mano.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = winnerColor
                            )
                            Text(
                                text = "Puntos que se le anotan: +$awardedPoints pts",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else if (parsedPoints.any { it > 0 }) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Introduce los puntos restantes de los equipos para determinar el ganador del cierre.",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
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
                            if (isDecided && awardedPoints > 0) {
                                onApplyTrancaRound(winningIndex, awardedPoints, BonusTag.TRANCA)
                            }
                        },
                        enabled = isDecided && awardedPoints > 0,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDecided) TeamColors[winningIndex % TeamColors.size] else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("apply_tranca_button")
                    ) {
                        Text("Anotar en Partida", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
