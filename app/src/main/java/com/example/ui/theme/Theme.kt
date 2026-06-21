package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HabitOSDarkColorScheme = darkColorScheme(
  primary = GlowIndigo,
  secondary = EmeraldGreen,
  tertiary = CustomPurple,
  background = SlateDarkBackground,
  surface = SlateCardBackground,
  onPrimary = TextPrimary,
  onSecondary = SlateDarkBackground,
  onBackground = TextPrimary,
  onSurface = TextPrimary,
  surfaceVariant = GlassWhite,
  onSurfaceVariant = TextSecondary,
  error = ErrorRed,
  onError = Color.White
)

@Composable
fun HabitOSTheme(
  content: @Composable () -> Unit,
) {
  // HabitOS enforces a stunning space dark aesthetic (Glassmorphic) out of the box!
  MaterialTheme(
    colorScheme = HabitOSDarkColorScheme,
    typography = Typography,
    content = content
  )
}
