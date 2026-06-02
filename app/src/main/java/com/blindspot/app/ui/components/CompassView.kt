package com.blindspot.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blindspot.app.ui.theme.GeminiBlue
import com.blindspot.app.ui.theme.GeminiPink
import com.blindspot.app.ui.theme.GeminiViolet
import kotlin.math.min

/**
 * Stateless, reusable compass dial that points its needle toward a target.
 *
 * @param rotationDegrees the needle rotation (clockwise) where the target lies relative to the
 *   top of the screen — typically `bearingToPlace - deviceHeading`.
 * @param distanceLabel optional short label rendered in the center (e.g. "320 m").
 * @param targetLabel optional secondary label rendered under the distance.
 */
@Composable
fun CompassView(
    rotationDegrees: Float,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
    distanceLabel: String? = null,
    targetLabel: String? = null,
) {
    // Animate along the shortest path to avoid spinning across the 0/360 boundary.
    val animatedRotation by animateFloatAsState(
        targetValue = rotationDegrees,
        animationSpec = tween(durationMillis = 500),
        label = "needle",
    )

    val needleBrush = remember {
        Brush.verticalGradient(listOf(GeminiPink, GeminiViolet, GeminiBlue))
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            drawDial()
            rotate(degrees = animatedRotation, pivot = center) {
                drawNeedle(needleBrush)
            }
        }

        if (distanceLabel != null) {
            Text(
                text = distanceLabel,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
        if (targetLabel != null) {
            Text(
                text = targetLabel,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.Center).padding(top = 40.dp),
            )
        }
    }
}

private fun DrawScope.drawDial() {
    val radius = min(size.width, size.height) / 2f
    // Outer glass ring.
    drawCircle(
        color = Color.White.copy(alpha = 0.06f),
        radius = radius,
        center = center,
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.18f),
        radius = radius,
        center = center,
        style = Stroke(width = 2f),
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.10f),
        radius = radius * 0.78f,
        center = center,
        style = Stroke(width = 1f),
    )

    // Tick marks around the dial.
    val tickCount = 60
    for (i in 0 until tickCount) {
        val angle = Math.toRadians((i * 360.0 / tickCount))
        val isMajor = i % 5 == 0
        val outer = radius
        val inner = radius - if (isMajor) 18f else 9f
        val startX = center.x + (outer * Math.sin(angle)).toFloat()
        val startY = center.y - (outer * Math.cos(angle)).toFloat()
        val endX = center.x + (inner * Math.sin(angle)).toFloat()
        val endY = center.y - (inner * Math.cos(angle)).toFloat()
        drawLine(
            color = Color.White.copy(alpha = if (isMajor) 0.45f else 0.18f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isMajor) 3f else 1.5f,
        )
    }
}

private fun DrawScope.drawNeedle(brush: Brush) {
    val radius = min(size.width, size.height) / 2f
    val needleLength = radius * 0.62f
    val needleHalfWidth = radius * 0.07f

    // Pointer (toward target) — a tapered triangle.
    val pointer = Path().apply {
        moveTo(center.x, center.y - needleLength)
        lineTo(center.x - needleHalfWidth, center.y)
        lineTo(center.x + needleHalfWidth, center.y)
        close()
    }
    drawPath(path = pointer, brush = brush)

    // Tail (opposite side) — subtle, semi-transparent.
    val tail = Path().apply {
        moveTo(center.x, center.y + needleLength * 0.6f)
        lineTo(center.x - needleHalfWidth, center.y)
        lineTo(center.x + needleHalfWidth, center.y)
        close()
    }
    drawPath(path = tail, color = Color.White.copy(alpha = 0.25f))

    // Center hub.
    drawCircle(color = Color.White, radius = needleHalfWidth * 0.9f, center = center)
    drawCircle(
        color = GeminiViolet,
        radius = needleHalfWidth * 0.5f,
        center = center,
    )
}
