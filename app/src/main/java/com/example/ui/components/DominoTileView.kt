package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DominoDotBlack
import com.example.ui.theme.DominoTileIvory

@Composable
fun DominoTileView(
    topPips: Int,
    bottomPips: Int,
    modifier: Modifier = Modifier,
    width: Dp = 38.dp,
    height: Dp = 68.dp,
    dotColor: Color = DominoDotBlack,
    backgroundColor: Color = DominoTileIvory
) {
    Box(
        modifier = modifier
            .size(width, height)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val midY = h / 2f
            val dotRadius = w * 0.08f

            // Dividing line
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(w * 0.12f, midY),
                end = Offset(w * 0.88f, midY),
                strokeWidth = 2f
            )

            // Center metallic pivot bead
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = dotRadius * 0.75f,
                center = Offset(w / 2f, midY)
            )

            // Draw Top Half Pips
            drawPipsHalf(
                pips = topPips.coerceIn(0, 6),
                centerX = w / 2f,
                centerY = midY / 2f,
                width = w * 0.72f,
                height = (midY) * 0.72f,
                dotRadius = dotRadius,
                dotColor = dotColor
            )

            // Draw Bottom Half Pips
            drawPipsHalf(
                pips = bottomPips.coerceIn(0, 6),
                centerX = w / 2f,
                centerY = midY + (midY / 2f),
                width = w * 0.72f,
                height = (midY) * 0.72f,
                dotRadius = dotRadius,
                dotColor = if (bottomPips == 6 || bottomPips == 5) Color(0xFFE11D48) else dotColor
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPipsHalf(
    pips: Int,
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    dotRadius: Float,
    dotColor: Color
) {
    val left = centerX - width / 2f
    val right = centerX + width / 2f
    val top = centerY - height / 2f
    val bottom = centerY + height / 2f

    when (pips) {
        1 -> {
            drawCircle(dotColor, dotRadius, Offset(centerX, centerY))
        }
        2 -> {
            drawCircle(dotColor, dotRadius, Offset(left, top))
            drawCircle(dotColor, dotRadius, Offset(right, bottom))
        }
        3 -> {
            drawCircle(dotColor, dotRadius, Offset(left, top))
            drawCircle(dotColor, dotRadius, Offset(centerX, centerY))
            drawCircle(dotColor, dotRadius, Offset(right, bottom))
        }
        4 -> {
            drawCircle(dotColor, dotRadius, Offset(left, top))
            drawCircle(dotColor, dotRadius, Offset(right, top))
            drawCircle(dotColor, dotRadius, Offset(left, bottom))
            drawCircle(dotColor, dotRadius, Offset(right, bottom))
        }
        5 -> {
            drawCircle(dotColor, dotRadius, Offset(left, top))
            drawCircle(dotColor, dotRadius, Offset(right, top))
            drawCircle(dotColor, dotRadius, Offset(centerX, centerY))
            drawCircle(dotColor, dotRadius, Offset(left, bottom))
            drawCircle(dotColor, dotRadius, Offset(right, bottom))
        }
        6 -> {
            drawCircle(dotColor, dotRadius, Offset(left, top))
            drawCircle(dotColor, dotRadius, Offset(right, top))
            drawCircle(dotColor, dotRadius, Offset(left, centerY))
            drawCircle(dotColor, dotRadius, Offset(right, centerY))
            drawCircle(dotColor, dotRadius, Offset(left, bottom))
            drawCircle(dotColor, dotRadius, Offset(right, bottom))
        }
    }
}
