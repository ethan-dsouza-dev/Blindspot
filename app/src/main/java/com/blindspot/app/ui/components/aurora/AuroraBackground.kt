package com.blindspot.app.ui.components.aurora

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.blindspot.app.ui.theme.AuroraTokens

/**
 * Full-screen "Midnight Aurora" background: a static slate-to-deep vertical gradient with a
 * single faint cyan radial glow in the upper-center area (behind the compass). Intentionally
 * has no animation — calm and battery-friendly.
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(AuroraTokens.BaseSlate, AuroraTokens.BaseDeep),
                    ),
                )
                // Single static cyan glow behind the compass area.
                val glowCenter = Offset(size.width * 0.5f, size.height * 0.42f)
                val glowRadius = size.maxDimension * 0.45f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AuroraTokens.AccentCyan.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        center = glowCenter,
                        radius = glowRadius,
                    ),
                    radius = glowRadius,
                    center = glowCenter,
                )
            },
    ) {
        content()
    }
}
