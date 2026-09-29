package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.domino.BotProfile
import com.example.data.domino.BotRoster
import com.example.data.domino.DominoGamePlayMode
import com.example.ui.theme.DominoGold
import kotlinx.coroutines.delay

@Composable
fun BotLoadingDialog(
    isLoading: Boolean,
    bots: List<BotProfile>,
    playMode: DominoGamePlayMode = DominoGamePlayMode.PAREJAS_2V2,
    targetScore: Int = 100,
    onDismissRequest: () -> Unit = {}
) {
    if (!isLoading) return

    // Conteo regresivo exacto de 5 segundos
    var secondsRemaining by remember(isLoading) { mutableIntStateOf(5) }
    var displayBots by remember(isLoading, bots) { mutableStateOf(bots) }
    val isLockedIn = secondsRemaining <= 2

    // Temporizador regresivo de 5 segundos
    LaunchedEffect(isLoading) {
        if (isLoading) {
            secondsRemaining = 5
            for (sec in 5 downTo 1) {
                secondsRemaining = sec
                delay(1000L)
            }
        }
    }

    // Efecto de ruleta/búsqueda aleatoria en tiempo real durante los primeros 3 segundos
    LaunchedEffect(isLoading, bots) {
        if (isLoading && bots.isNotEmpty()) {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < 3000L) {
                displayBots = BotRoster.getRandomBots(count = bots.size, isTeams = playMode.isTeams)
                delay(200L)
            }
            // A los 2 segundos restantes, se fijan los bots seleccionados definitivamente
            displayBots = bots
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val progressValue by animateFloatAsState(
        targetValue = ((6 - secondsRemaining) / 5f).coerceIn(0.1f, 1f),
        animationSpec = tween(durationMillis = 950, easing = LinearEasing),
        label = "progress_val"
    )

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(16.dp)
                .testTag("bot_loading_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.8.dp, if (isLockedIn) DominoGold else Color(0xFF38BDF8).copy(alpha = glowAlpha)),
                shadowElevation = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Badge del Conteo Regresivo de 5 Segundos
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isLockedIn) DominoGold.copy(alpha = 0.18f) else Color(0xFF0284C7).copy(alpha = 0.2f),
                        border = BorderStroke(1.2.dp, if (isLockedIn) DominoGold else Color(0xFF38BDF8)),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isLockedIn) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = if (isLockedIn) DominoGold else Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLockedIn) "¡Mesa lista en ${secondsRemaining}s!" else "Iniciando en ${secondsRemaining}s...",
                                color = if (isLockedIn) DominoGold else Color(0xFFE2E8F0),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Icono animado de carga
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = if (isLockedIn) DominoGold else Color(0xFF38BDF8),
                            strokeWidth = 3.2.dp,
                            modifier = Modifier.size(54.dp)
                        )
                        Icon(
                            imageVector = if (isLockedIn) Icons.Default.Casino else Icons.Default.Search,
                            contentDescription = null,
                            tint = if (isLockedIn) DominoGold else Color(0xFF38BDF8),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isLockedIn) "¡Jugadores Convocados a la Mesa!" else "Buscando Rivales Aleatoriamente...",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isLockedIn) {
                            if (playMode.isTeams && bots.size == 3) "Parejas confirmadas (2 vs 2) • Meta: $targetScore pts"
                            else "Mesa individual confirmada (${bots.size + 1} Jugadores) • Meta: $targetScore pts"
                        } else {
                            "Buscando entre cientos de jugadores de la región..."
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLockedIn) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Roster de Bots seleccionados / escaneados en tiempo real
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        displayBots.forEachIndexed { index, bot ->
                            val isPartner = playMode.isTeams && bots.size == 3 && index == 1
                            val roleLabel = if (isPartner) "Tu Pareja" else "Rival ${if (playMode.isTeams) (if (index == 0) "1" else "2") else "${index + 1}"}"
                            val borderColor = when {
                                isLockedIn && isPartner -> DominoGold
                                isLockedIn -> Color(0xFF4ADE80)
                                isPartner -> Color(0xFF38BDF8)
                                else -> Color(0xFF475569)
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isPartner) Color(0xFF0C2A44) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, borderColor.copy(alpha = if (isLockedIn) 0.9f else 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar con animación de entrada
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isPartner) Color(0xFF0369A1) else Color(0xFF334155)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = bot.avatarEmoji, fontSize = 20.sp)
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Nombre, Ciudad y Rol
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = bot.name.replace(" (Bot)", ""),
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isPartner) Color(0xFF0284C7) else Color(0xFF334155),
                                                modifier = Modifier.padding(top = 1.dp)
                                            ) {
                                                Text(
                                                    text = roleLabel,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "📍 ${bot.originCity}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Medalla de Capacidad Táctica
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0F172A),
                                        border = BorderStroke(0.8.dp, DominoGold.copy(alpha = if (isLockedIn) 0.8f else 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = bot.capability.badgeEmoji, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = bot.capability.shortName,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DominoGold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Barra de progreso continua sincronizada con el conteo de 5 segundos
                    LinearProgressIndicator(
                        progress = { progressValue },
                        color = DominoGold,
                        trackColor = Color(0xFF334155),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isLockedIn) "Barajando y repartiendo 7 fichas por jugador..." else "Escaneando jugadores y afinando estrategias...",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFCBD5E1),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
