package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CosmicTeal,
    secondary = CosmicCyan,
    tertiary = CosmicPink,
    background = CosmicObsidian,
    surface = CosmicSurface,
    onPrimary = CosmicObsidian,
    onSecondary = CosmicObsidian,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CosmicCard,
    onSurfaceVariant = TextMutedDark,
    outline = BorderDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF0369A1), // Elegant deep sky blue
    secondary = Color(0xFF4F46E5), // Indigo
    tertiary = Color(0xFFDB2777), // Deep pink
    background = Color(0xFFF8FAFC), // Crisp clean slate
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // For the cosmic vibe, let's allow dynamic color or default dark scheme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> DarkColorScheme // Force dark theme for the true Cosmic Space experience by default
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
