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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DominoTileBackView(
    modifier: Modifier = Modifier,
    width: Dp = 10.dp,
    height: Dp = 18.dp,
    borderColor: Color = Color(0xFFE2E8F0),
    isIvoryStyle: Boolean = true
) {
    val corner = (width * 0.22f).coerceIn(2.dp, 4.dp)
    Box(
        modifier = modifier
            .size(width, height)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(corner))
            .clip(RoundedCornerShape(corner))
            .background(
                if (isIvoryStyle) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF1F5F9),
                            Color(0xFFE2E8F0)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A),
                            Color(0xFF020617)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (isIvoryStyle) Color(0xFFCBD5E1) else borderColor.copy(alpha = 0.6f),
                shape = RoundedCornerShape(corner)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val midY = h / 2f

            // Central divider line (just like a real domino)
            drawLine(
                color = if (isIvoryStyle) Color(0xFF94A3B8) else Color(0xFF38BDF8).copy(alpha = 0.5f),
                start = Offset(w * 0.15f, midY),
                end = Offset(w * 0.85f, midY),
                strokeWidth = if (h > 14.dp.toPx()) 1.2f else 0.9f
            )

            // Center brass pin / rivet detail
            val pinRadius = (w * 0.12f).coerceIn(1f, 2.2f)
            drawCircle(
                color = if (isIvoryStyle) Color(0xFFD97706) else Color(0xFF38BDF8),
                radius = pinRadius,
                center = Offset(w / 2f, midY)
            )

            // Inner subtle border framing
            if (w >= 8.dp.toPx()) {
                drawRoundRect(
                    color = if (isIvoryStyle) Color(0x22000000) else Color(0x3338BDF8),
                    topLeft = Offset(1.5f, 1.5f),
                    size = androidx.compose.ui.geometry.Size(w - 3f, h - 3f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.8f)
                )
            }
        }
    }
}
