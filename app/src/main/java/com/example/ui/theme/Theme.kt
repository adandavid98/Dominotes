package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DominoBlueLight,
    onPrimary = DominoNavyDark,
    primaryContainer = DominoBlue,
    onPrimaryContainer = Color.White,
    secondary = DominoGoldLight,
    onSecondary = DominoNavyDark,
    tertiary = DominoRedLight,
    onTertiary = Color.White,
    background = DominoNavyDark,
    onBackground = Color(0xFFF8FAFC),
    surface = DominoSurfaceDark,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DominoCardDark,
    onSurfaceVariant = Color(0xFFCBD5E1)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DominoBlue,
    onPrimary = Color.White,
    primaryContainer = DominoBlueContainer,
    onPrimaryContainer = DominoNavyDark,
    secondary = DominoGold,
    onSecondary = DominoNavyDark,
    tertiary = DominoRed,
    onTertiary = Color.White,
    background = DominoBackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = DominoSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = DominoCardLight,
    onSurfaceVariant = Color(0xFF475569)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
