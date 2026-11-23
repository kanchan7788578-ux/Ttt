package com.example.tv.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val TvDarkColorScheme = darkColorScheme(
    primary = CyanGlow,
    onPrimary = ConsoleObsidianDeep,
    primaryContainer = ConsoleSurfaceHighest,
    onPrimaryContainer = CyanGlow,
    secondary = CoralPulse,
    onSecondary = ConsoleObsidianDeep,
    tertiary = EmeraldConnected,
    onTertiary = ConsoleObsidianDeep,
    background = ConsoleObsidian,
    onBackground = TextPrimary,
    surface = ConsoleSurface,
    onSurface = TextPrimary,
    surfaceVariant = ConsoleSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = ConsoleOutline,
    error = CrimsonAlert,
    onError = ConsoleObsidianDeep
)

val TvShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun TvArcadeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TvDarkColorScheme,
        typography = TvTypography,
        shapes = TvShapes,
        content = content
    )
}
