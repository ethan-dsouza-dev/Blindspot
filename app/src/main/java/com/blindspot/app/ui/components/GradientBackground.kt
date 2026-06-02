package com.blindspot.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.blindspot.app.ui.theme.BackgroundBottom
import com.blindspot.app.ui.theme.BackgroundMid
import com.blindspot.app.ui.theme.BackgroundTop
import com.blindspot.app.ui.theme.GeminiBlue
import com.blindspot.app.ui.theme.GeminiPink
import com.blindspot.app.ui.theme.GeminiViolet

/**
 * Full-screen animated, Gemini-style gradient background. Renders a deep base gradient with two
 * slowly drifting colored glows behind [content].
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "bg")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12_000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shift",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(BackgroundTop, BackgroundMid, BackgroundBottom),
                    ),
                )
                // Drifting violet glow.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GeminiViolet.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(size.width * (0.2f + 0.2f * shift), size.height * 0.25f),
                        radius = size.maxDimension * 0.55f,
                    ),
                    radius = size.maxDimension * 0.55f,
                    center = Offset(size.width * (0.2f + 0.2f * shift), size.height * 0.25f),
                )
                // Drifting blue glow.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GeminiBlue.copy(alpha = 0.30f), Color.Transparent),
                        center = Offset(size.width * (0.85f - 0.2f * shift), size.height * 0.7f),
                        radius = size.maxDimension * 0.5f,
                    ),
                    radius = size.maxDimension * 0.5f,
                    center = Offset(size.width * (0.85f - 0.2f * shift), size.height * 0.7f),
                )
                // Subtle pink accent.
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GeminiPink.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * (0.95f - 0.1f * shift)),
                        radius = size.maxDimension * 0.45f,
                    ),
                    radius = size.maxDimension * 0.45f,
                    center = Offset(size.width * 0.5f, size.height * (0.95f - 0.1f * shift)),
                )
            },
    ) {
        content()
    }
}
