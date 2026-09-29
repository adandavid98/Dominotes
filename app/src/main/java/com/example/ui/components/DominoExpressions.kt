package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.DominoGold
import kotlinx.coroutines.delay

data class DominoReaction(
    val emoji: String,
    val phrase: String,
    val audioDecisive: Boolean = false
)

object DominoExpressionsList {
    val commonExpressions = listOf(
        DominoReaction("💥", "¡Trancao!", audioDecisive = true),
        DominoReaction("👑", "¡Capicúa!", audioDecisive = true),
        DominoReaction("⚡", "¡Por la cabeza!"),
        DominoReaction("✋", "¡Paso!"),
        DominoReaction("🔨", "¡Dale duro!", audioDecisive = true),
        DominoReaction("🎲", "¡Chuchazo!", audioDecisive = true),
        DominoReaction("🔥", "¡Abran paso!"),
        DominoReaction("👀", "¡Te tengo medido!"),
        DominoReaction("😎", "¡Dominó!")
    )
}

data class ActiveReactionBubble(
    val playerIndex: Int,
    val playerName: String,
    val expression: DominoReaction,
    val id: Long = System.currentTimeMillis()
)

@Composable
fun DominoReactionBubbleView(
    bubble: ActiveReactionBubble?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = bubble != null,
        enter = fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        exit = fadeOut() + scaleOut(),
        modifier = modifier.zIndex(20f)
    ) {
        if (bubble != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.5.dp, DominoGold),
                shadowElevation = 8.dp,
                modifier = Modifier.padding(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = bubble.expression.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = bubble.playerName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DominoGold
                        )
                        Text(
                            text = bubble.expression.phrase,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DominoExpressionsHeaderButton(
    onSendReaction: (DominoReaction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            onClick = { showMenu = !showMenu },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, if (showMenu) DominoGold else Color(0xFF475569)),
            shadowElevation = 2.dp,
            modifier = Modifier.testTag("btn_expressions_header")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "😄", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Expresiones",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (showMenu) DominoGold else Color(0xFFE2E8F0),
                    maxLines = 1
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
        ) {
            Text(
                text = "Frases del Dominó",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DominoGold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )

            DominoExpressionsList.commonExpressions.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.phrase,
                                fontSize = 13.sp,
                                fontWeight = if (item.audioDecisive) FontWeight.Bold else FontWeight.Medium,
                                color = if (item.audioDecisive) DominoGold else Color.White
                            )
                        }
                    },
                    onClick = {
                        showMenu = false
                        onSendReaction(item)
                    }
                )
            }
        }
    }
}

@Composable
fun DominoQuickReactionBar(
    onSendReaction: (DominoReaction) -> Unit,
    isSoundMuted: Boolean,
    onToggleSound: () -> Unit,
    onOpenSkins: () -> Unit,
    onOpenRules: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xE60F172A),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.EmojiEmotions,
                            contentDescription = "Expresiones y frases",
                            tint = DominoGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = if (isExpanded) "Frases típicas del juego" else "Expresiones",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Skins & Themes
                    Surface(
                        onClick = onOpenSkins,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(0.8.dp, Color(0xFF475569)),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "🎨 Temas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Regional Rules
                    Surface(
                        onClick = onOpenRules,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(0.8.dp, Color(0xFF475569)),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "🌎 Reglas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DominoGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Sound Toggle
                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isSoundMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Sonido",
                            tint = if (isSoundMuted) Color.Gray else DominoGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DominoExpressionsList.commonExpressions.forEach { item ->
                        Surface(
                            onClick = {
                                onSendReaction(item)
                                isExpanded = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, if (item.audioDecisive) DominoGold.copy(alpha = 0.8f) else Color(0xFF334155)),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = item.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.phrase,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.audioDecisive) DominoGold else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
