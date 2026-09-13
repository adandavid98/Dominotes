package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DominoTileView(
    topPips: Int,
    bottomPips: Int,
    modifier: Modifier = Modifier,
    width: Dp = 40.dp,
    height: Dp = 80.dp,
    dotColor: Color = Color(0xFF0F172A),
    backgroundColor: Color = Color(0xFFFFFFFF),
    isHighlighted: Boolean = false,
    dimmed: Boolean = false,
    canPlayBorder: Boolean = false
) {
    // Proportional, elegant corner radius (scaled with width)
    val cornerRadius = (width * 0.13f).coerceIn(3.dp, 8.dp)

    Box(
        modifier = modifier
            .size(width, height)
            .shadow(
                elevation = if (isHighlighted) 10.dp else if (canPlayBorder) 6.dp else 4.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = if (isHighlighted) Color(0xFF38BDF8) else if (canPlayBorder) Color(0xFF10B981) else Color(0x66000000),
                spotColor = if (isHighlighted) Color(0xFF0284C7) else if (canPlayBorder) Color(0xFF059669) else Color(0x40000000)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = if (isHighlighted) listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFE0F2FE),
                        Color(0xFFBAE6FD)
                    ) else if (canPlayBorder) listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF0FDF4),
                        Color(0xFFDCFCE7)
                    ) else if (dimmed) listOf(
                        Color(0xFFE2E8F0),
                        Color(0xFFCBD5E1)
                    ) else listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF8FAFC),
                        Color(0xFFF1F5F9)
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .border(
                width = if (isHighlighted) 2.4.dp else if (canPlayBorder) 2.2.dp else 1.2.dp,
                color = if (isHighlighted) Color(0xFF0284C7) else if (canPlayBorder) Color(0xFF10B981) else Color(0xFF94A3B8).copy(alpha = 0.6f),
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val midY = h / 2f
            val dotRadius = (w * 0.082f).coerceIn(1.8f, 5.5f)

            // Inner subtle rim bevel
            drawRoundRect(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(1f, 1f),
                size = Size(w - 2f, h - 2f),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )

            // Clear, high-visibility divider groove
            drawLine(
                color = Color(0xFF1E293B), // Slate 800 for crisp definition
                start = Offset(w * 0.07f, midY),
                end = Offset(w * 0.93f, midY),
                strokeWidth = (w * 0.035f).coerceIn(1.5f, 3.5f)
            )
            // Subtle embossed highlight under the line
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(w * 0.07f, midY + 1.2f),
                end = Offset(w * 0.93f, midY + 1.2f),
                strokeWidth = 1f
            )

            // Metallic brass center pivot bead
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFDE68A), Color(0xFFD97706), Color(0xFF78350F)),
                    center = Offset(w / 2f - 0.4f, midY - 0.4f),
                    radius = dotRadius * 0.9f
                ),
                radius = dotRadius * 0.7f,
                center = Offset(w / 2f, midY)
            )

            val effectiveDotColor = if (dimmed) dotColor.copy(alpha = 0.4f) else dotColor

            // Safe pip bounding area for each half (strictly centered, never touching borders or corners)
            val pipSpanX = w * 0.50f
            val pipSpanY = midY * 0.50f

            // Top Half Pips
            drawPipsHalf(
                pips = topPips.coerceIn(0, 6),
                centerX = w / 2f,
                centerY = midY / 2f,
                spanX = pipSpanX,
                spanY = pipSpanY,
                dotRadius = dotRadius,
                dotColor = effectiveDotColor
            )

            // Bottom Half Pips
            drawPipsHalf(
                pips = bottomPips.coerceIn(0, 6),
                centerX = w / 2f,
                centerY = midY + (midY / 2f),
                spanX = pipSpanX,
                spanY = pipSpanY,
                dotRadius = dotRadius,
                dotColor = effectiveDotColor
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPipsHalf(
    pips: Int,
    centerX: Float,
    centerY: Float,
    spanX: Float,
    spanY: Float,
    dotRadius: Float,
    dotColor: Color
) {
    val left = centerX - spanX / 2f
    val right = centerX + spanX / 2f
    val top = centerY - spanY / 2f
    val bottom = centerY + spanY / 2f

    fun drawPip(x: Float, y: Float) {
        // Deep sunken bevel shadow for rich contrast
        drawCircle(
            color = Color.Black.copy(alpha = 0.18f),
            radius = dotRadius + 0.8f,
            center = Offset(x, y + 0.8f)
        )
        // Solid high-contrast dot
        drawCircle(
            color = dotColor,
            radius = dotRadius,
            center = Offset(x, y)
        )
        // Specular enamel shine in top-left
        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = dotRadius * 0.35f,
            center = Offset(x - dotRadius * 0.30f, y - dotRadius * 0.30f)
        )
    }

    when (pips) {
        1 -> {
            drawPip(centerX, centerY)
        }
        2 -> {
            drawPip(left, top)
            drawPip(right, bottom)
        }
        3 -> {
            drawPip(left, top)
            drawPip(centerX, centerY)
            drawPip(right, bottom)
        }
        4 -> {
            drawPip(left, top)
            drawPip(right, top)
            drawPip(left, bottom)
            drawPip(right, bottom)
        }
        5 -> {
            drawPip(left, top)
            drawPip(right, top)
            drawPip(centerX, centerY)
            drawPip(left, bottom)
            drawPip(right, bottom)
        }
        6 -> {
            drawPip(left, top)
            drawPip(right, top)
            drawPip(left, centerY)
            drawPip(right, centerY)
            drawPip(left, bottom)
            drawPip(right, bottom)
        }
    }
}
