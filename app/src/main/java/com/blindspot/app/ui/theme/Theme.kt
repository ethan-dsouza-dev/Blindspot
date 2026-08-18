package com.blindspot.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Dark color scheme built from the currently-active [AuroraTokens] palette. Computed inside
 * this composable (not as a top-level val) so it re-reads the tokens — and therefore reacts to
 * [AuroraTokens.setPalette] — on every recomposition. Dark-only by design (night-out discovery
 * product); dynamic color disabled.
 */
@Composable
fun BlindspotTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}