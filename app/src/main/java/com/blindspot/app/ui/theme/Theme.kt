package com.blindspot.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Single dark "Midnight Aurora" scheme built from [AuroraTokens]. The app is dark-only by
 * design (night-out discovery product); dynamic color is intentionally disabled so the curated
 * palette is preserved across devices.
 */
private val AuroraColorScheme = darkColorScheme(
    primary = AuroraTokens.AccentCyan,
    onPrimary = AuroraTokens.OnAccent,
    secondary = AuroraTokens.AccentTeal,
    onSecondary = AuroraTokens.OnAccent,
    tertiary = AuroraTokens.AccentTeal,
    onTertiary = AuroraTokens.OnAccent,
    background = AuroraTokens.BaseDeep,
    onBackground = AuroraTokens.TextPrimary,
    surface = AuroraTokens.BaseSlate,
    onSurface = AuroraTokens.TextPrimary,
    surfaceVariant = AuroraTokens.SurfaceElevated,
    onSurfaceVariant = AuroraTokens.TextSecondary,
    outline = AuroraTokens.SurfaceBorder,
    outlineVariant = AuroraTokens.SurfaceBorder,
    error = AuroraTokens.Negative,
    onError = AuroraTokens.OnAccent,
)

@Composable
fun BlindspotTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AuroraColorScheme,
        typography = Typography,
        content = content,
    )
}