package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DominoBlue
import com.example.ui.theme.DominoGold
import com.example.ui.theme.DominoGreen
import com.example.ui.theme.DominoRed

val TeamColors = listOf(
    DominoBlue,
    DominoRed,
    DominoGold,
    DominoGreen
)

@Composable
fun ScoreCard(
    playerIndex: Int,
    name: String,
    score: Int,
    targetScore: Int,
    isLeader: Boolean,
    showQuickControls: Boolean,
    onQuickAdd: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null
) {
    val themeColor = TeamColors[playerIndex % TeamColors.size]
    val progress = (score.toFloat() / targetScore.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "score_progress")
    val remaining = (targetScore - score).coerceAtLeast(0)

    val cardBorderColor by animateColorAsState(
        targetValue = if (isLeader && score > 0) themeColor else themeColor.copy(alpha = 0.3f),
        label = "border_color"
    )

    Card(
        modifier = modifier
            .testTag("score_card_$playerIndex")
            .then(
                if (onCardClick != null) Modifier.clickable { onCardClick() }
                else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLeader) 4.dp else 1.dp),
        border = BorderStroke(if (isLeader && score > 0) 2.dp else 1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Color chip, Player Name, and Leader Trophy
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(themeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.2.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isLeader && score > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DominoGold.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, DominoGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Líder",
                                tint = DominoGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "LÍDER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DominoGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Score Display: Huge Points and Target
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 44.sp,
                        lineHeight = 46.sp
                    ),
                    color = themeColor,
                    modifier = Modifier.testTag("score_value_$playerIndex")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "/ $targetScore",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = themeColor,
                trackColor = themeColor.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Remaining to win
            Text(
                text = if (score >= targetScore) "¡Ganador! 🏆" else "Faltan $remaining pts",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (score >= targetScore) themeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (score >= targetScore) FontWeight.Bold else FontWeight.Medium
                )
            )

            // Direct Accumulation Quick Buttons (Shown when quick controls active)
            if (showQuickControls) {
                Spacer(modifier = Modifier.height(14.dp))
                // Row 1: +5, +10, +25
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickButton(
                        label = "+5",
                        onClick = { onQuickAdd(5) },
                        color = themeColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_5_$playerIndex")
                    )
                    QuickButton(
                        label = "+10",
                        onClick = { onQuickAdd(10) },
                        color = themeColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_10_$playerIndex")
                    )
                    QuickButton(
                        label = "+25",
                        onClick = { onQuickAdd(25) },
                        color = themeColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_25_$playerIndex")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: +30, +35, +40
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickButton(
                        label = "+30",
                        onClick = { onQuickAdd(30) },
                        color = themeColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_30_$playerIndex")
                    )
                    QuickButton(
                        label = "+35",
                        onClick = { onQuickAdd(35) },
                        color = themeColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_35_$playerIndex")
                    )
                    QuickButton(
                        label = "+40",
                        onClick = { onQuickAdd(40) },
                        color = themeColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_add_40_$playerIndex")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 3: Subtract / Correction
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickSubtractButton(
                        label = "-5 puntos",
                        onClick = { onQuickAdd(-5) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_sub_5_$playerIndex")
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickButton(
    label: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.45f)),
        modifier = modifier.height(46.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun QuickSubtractButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
        modifier = modifier.height(38.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Restar 5 puntos",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
