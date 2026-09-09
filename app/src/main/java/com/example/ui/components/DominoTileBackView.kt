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
    width: Dp = 28.dp,
    height: Dp = 48.dp
) {
    val corner = 5.dp
    Box(
        modifier = modifier
            .size(width, height)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(corner))
            .clip(RoundedCornerShape(corner))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E293B), // Slate 800
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF020617)  // Deep slate
                    )
                )
            )
            .border(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.35f), RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.78f)) {
            val w = size.width
            val h = size.height
            // Minimalist diamond ornament
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                radius = 2.5f,
                center = Offset(w / 2f, h / 2f)
            )
            // Sleek concentric subtle border
            drawRect(
                color = Color(0xFF94A3B8).copy(alpha = 0.2f),
                topLeft = Offset(1f, 1f),
                size = androidx.compose.ui.geometry.Size(w - 2f, h - 2f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
        }
    }
}
