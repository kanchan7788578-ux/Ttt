package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val ConsoleDarkColorScheme = darkColorScheme(
    primary = CyanGlow,
    onPrimary = ConsoleObsidianDeep,
    primaryContainer = ConsoleSurfaceHighest,
    onPrimaryContainer = CyanGlow,
    secondary = CoralPulse,
    onSecondary = ConsoleObsidianDeep,
    secondaryContainer = ConsoleSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = EmeraldConnected,
    onTertiary = ConsoleObsidianDeep,
    background = ConsoleObsidian,
    onBackground = TextPrimary,
    surface = ConsoleSurface,
    onSurface = TextPrimary,
    surfaceVariant = ConsoleSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = ConsoleObsidianDeep,
    surfaceContainerLow = ConsoleObsidian,
    surfaceContainer = ConsoleSurface,
    surfaceContainerHigh = ConsoleSurfaceElevated,
    surfaceContainerHighest = ConsoleSurfaceHighest,
    outline = ConsoleOutline,
    outlineVariant = ConsoleOutlineSubtle,
    error = CrimsonAlert,
    onError = ConsoleObsidianDeep
)

val ConsoleShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TvPadTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ConsoleDarkColorScheme,
        typography = Typography,
        shapes = ConsoleShapes,
        content = content
    )
}
