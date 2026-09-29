package com.example.data.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class TileSkinStyle(
    val id: String,
    val displayName: String,
    val description: String,
    val baseColor: Color,
    val previewGradient: List<Color>,
    val dotColor: Color,
    val dividerColor: Color,
    val pinColors: List<Color>,
    val borderColor: Color
) {
    HUESO_CLASICO(
        id = "hueso_clasico",
        displayName = "Hueso Clásico",
        description = "Marfil pulido tradicional con perno central de latón",
        baseColor = Color(0xFFFBF9F3),
        previewGradient = listOf(Color(0xFFFFFFFF), Color(0xFFFAF7F0), Color(0xFFEDE8DD)),
        dotColor = Color(0xFF0F172A),
        dividerColor = Color(0xFF334155),
        pinColors = listOf(Color(0xFFFDE68A), Color(0xFFD97706), Color(0xFF78350F)),
        borderColor = Color(0xFFCBD5E1)
    ),
    ACRILICO_NOCHE(
        id = "acrilico_noche",
        displayName = "Acrílico Noche",
        description = "Elegante acrílico negro con puntos blancos de alto contraste",
        baseColor = Color(0xFF18181B),
        previewGradient = listOf(Color(0xFF27272A), Color(0xFF18181B), Color(0xFF09090B)),
        dotColor = Color(0xFFFFFFFF),
        dividerColor = Color(0xFF52525B),
        pinColors = listOf(Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFF78350F)),
        borderColor = Color(0xFF3F3F46)
    ),
    MADERA_PULIDA(
        id = "madera_pulida",
        displayName = "Madera Pulida",
        description = "Caoba noble tallada con puntos grabados en fuego dorado",
        baseColor = Color(0xFF5C3317),
        previewGradient = listOf(Color(0xFF783E19), Color(0xFF5C3317), Color(0xFF3E1F0D)),
        dotColor = Color(0xFFFDE68A),
        dividerColor = Color(0xFF29140A),
        pinColors = listOf(Color(0xFFFEF08A), Color(0xFFEAB308), Color(0xFF854D0E)),
        borderColor = Color(0xFF7C2D12)
    ),
    CARIBENO_VIBRANTE(
        id = "caribeno_vibrante",
        displayName = "Caribeño Multicolor",
        description = "Color esmeralda tropical con perno dorado y puntos marfil",
        baseColor = Color(0xFF064E3B),
        previewGradient = listOf(Color(0xFF047857), Color(0xFF065F46), Color(0xFF064E3B)),
        dotColor = Color(0xFFFFFFFF),
        dividerColor = Color(0xFF022C22),
        pinColors = listOf(Color(0xFFFDE047), Color(0xFFCA8A04), Color(0xFF713F12)),
        borderColor = Color(0xFF059669)
    );

    fun getBackgroundBrush(isHighlighted: Boolean, canPlayBorder: Boolean, dimmed: Boolean): Brush {
        val colors = when {
            isHighlighted -> listOf(Color(0xFFFFFFFF), Color(0xFFE0F2FE), Color(0xFFBAE6FD))
            canPlayBorder -> listOf(Color(0xFFFFFFFF), Color(0xFFF0FDF4), Color(0xFFDCFCE7))
            dimmed -> listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
            else -> previewGradient
        }
        return Brush.linearGradient(colors = colors, start = Offset.Zero, end = Offset.Infinite)
    }
}

enum class TableMatStyle(
    val id: String,
    val displayName: String,
    val description: String,
    val backgroundBrush: Brush,
    val borderColor: Color,
    val feltColor: Color,
    val accentColor: Color
) {
    FIELTRO_VERDE(
        id = "fieltro_verde",
        displayName = "Fieltro Verde",
        description = "Paño clásico de billar y dominó de club",
        backgroundBrush = Brush.radialGradient(
            colors = listOf(Color(0xFF065F46), Color(0xFF044835), Color(0xFF022C22))
        ),
        borderColor = Color(0xFF059669),
        feltColor = Color(0xFF064E3B),
        accentColor = Color(0xFF34D399)
    ),
    AZUL_CASINO(
        id = "azul_casino",
        displayName = "Azul Casino",
        description = "Tapete de casino de lujo con acabado aterciopelado",
        backgroundBrush = Brush.radialGradient(
            colors = listOf(Color(0xFF1E3A8A), Color(0xFF172554), Color(0xFF0F172A))
        ),
        borderColor = Color(0xFF3B82F6),
        feltColor = Color(0xFF1E293B),
        accentColor = Color(0xFF60A5FA)
    ),
    MADERA_RUSTICA(
        id = "madera_rustica",
        displayName = "Mesa de Madera",
        description = "Madera rústica criolla, sabor a patio y café",
        backgroundBrush = Brush.radialGradient(
            colors = listOf(Color(0xFF78350F), Color(0xFF451A03), Color(0xFF291004))
        ),
        borderColor = Color(0xFFB45309),
        feltColor = Color(0xFF451A03),
        accentColor = Color(0xFFFBBF24)
    ),
    CUERO_OSCURO(
        id = "cuero_oscuro",
        displayName = "Cuero Oscuro",
        description = "Elegante superficie de cuero negro con pespunte perimetral",
        backgroundBrush = Brush.radialGradient(
            colors = listOf(Color(0xFF27272A), Color(0xFF18181B), Color(0xFF09090B))
        ),
        borderColor = Color(0xFF71717A),
        feltColor = Color(0xFF18181B),
        accentColor = Color(0xFFE4E4E7)
    )
}
