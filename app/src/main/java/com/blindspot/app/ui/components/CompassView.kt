package com.blindspot.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blindspot.app.ui.theme.AuroraTokens
import kotlin.math.abs
import kotlin.math.min

/**
 * Color set for [CompassView]. Defaults use the "Midnight Aurora" tokens; pass a custom
 * instance to restyle the dial without touching other callers.
 */
data class CompassColors(
    val needleColors: List<Color> = listOf(AuroraTokens.AccentCyan, AuroraTokens.AccentTeal),
    val dialFill: Color = AuroraTokens.CompassDialFill,
    val dialStroke: Color = AuroraTokens.CompassDialStroke,
    val dialInnerStroke: Color = AuroraTokens.CompassDialInnerStroke,
    val tickMajor: Color = AuroraTokens.CompassTickMajor,
    val tickMinor: Color = AuroraTokens.CompassTickMinor,
    val needleTail: Color = AuroraTokens.CompassNeedleTail,
    val hub: Color = AuroraTokens.CompassHub,
    val hubInner: Color = AuroraTokens.CompassHubInner,
    val distanceText: Color = AuroraTokens.TextPrimary,
    val targetText: Color = AuroraTokens.TextSecondary,
)

/**
 * Stateless, reusable compass dial that points its needle toward a target.
 *
 * @param rotationDegrees the needle rotation (clockwise) where the target lies relative to the
 *   top of the screen — typically `bearingToPlace - deviceHeading`.
 * @param distanceLabel optional short label rendered in the center (e.g. "320 m").
 * @param targetLabel optional secondary label rendered under the distance.
 * @param colors color set for the dial, needle, and labels.
 */
@Composable
fun CompassView(
    rotationDegrees: Float,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp,
    distanceLabel: String? = null,
    targetLabel: String? = null,
    colors: CompassColors = CompassColors(),
) {
    // Track an "unwrapped" target so the needle always turns the short way and never spins a
    // full circle across the 0/360 boundary. Each update nudges the accumulated target by the
    // signed delta in [-180, 180].
    var unwrappedTarget by remember { mutableFloatStateOf(rotationDegrees) }
    LaunchedEffect(rotationDegrees) {
        val delta = ((rotationDegrees - unwrappedTarget + 540f) % 360f) - 180f
        unwrappedTarget += delta
    }

    // A medium-stiffness spring feels responsive without overshooting wildly.
    val animatedRotation by animateFloatAsState(
        targetValue = unwrappedTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "needle",
    )

    // "Locked on": the needle points (almost) straight ahead. Fades a glow in behind the tip so
    // the user gets a quiet confirmation they're walking the right way.
    val headingError = ((animatedRotation % 360f) + 540f) % 360f - 180f
    val lockedOn = abs(headingError) <= 10f
    val glowAlpha by animateFloatAsState(
        targetValue = if (lockedOn) 0.25f else 0f,
        label = "lockGlow",
    )

    val needleBrush = remember(colors) {
        Brush.verticalGradient(colors.needleColors)
    }

    val textMeasurer = rememberTextMeasurer()
    val cardinalStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = colors.tickMajor,
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            drawDial(colors)
            drawCardinals(textMeasurer, cardinalStyle)
            rotate(degrees = animatedRotation, pivot = center) {
                if (glowAlpha > 0.01f) {
                    drawNeedleGlow(colors, glowAlpha)
                }
                drawNeedle(needleBrush, colors)
            }
        }

        // Labels live in the bottom half of the dial, centered around the ~3/4 height point.
        if (distanceLabel != null || targetLabel != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = size / 4),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (distanceLabel != null) {
                    Text(
                        text = distanceLabel,
                        style = MaterialTheme.typography.displayMedium,
                        color = colors.distanceText,
                    )
                }
                if (targetLabel != null) {
                    Text(
                        text = targetLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.targetText,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawDial(colors: CompassColors) {
    val radius = min(size.width, size.height) / 2f
    // Outer ring.
    drawCircle(
        color = colors.dialFill,
        radius = radius,
        center = center,
    )
    drawCircle(
        color = colors.dialStroke,
        radius = radius,
        center = center,
        style = Stroke(width = 2f),
    )
    drawCircle(
        color = colors.dialInnerStroke,
        radius = radius * 0.78f,
        center = center,
        style = Stroke(width = 1f),
    )

    // Tick marks around the dial: minor every 15°, major every 45°.
    val tickCount = 24
    for (i in 0 until tickCount) {
        val angle = Math.toRadians((i * 360.0 / tickCount))
        val isMajor = i % 3 == 0
        val outer = radius
        val inner = radius - if (isMajor) 18f else 9f
        val startX = center.x + (outer * Math.sin(angle)).toFloat()
        val startY = center.y - (outer * Math.cos(angle)).toFloat()
        val endX = center.x + (inner * Math.sin(angle)).toFloat()
        val endY = center.y - (inner * Math.cos(angle)).toFloat()
        drawLine(
            color = if (isMajor) colors.tickMajor else colors.tickMinor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = if (isMajor) 3f else 1.5f,
        )
    }
}

/** N/E/S/W letters just inside the tick ring. The dial is screen-fixed, so N = device-up. */
private fun DrawScope.drawCardinals(textMeasurer: TextMeasurer, style: TextStyle) {
    val radius = min(size.width, size.height) / 2f
    val labelRadius = radius * 0.85f
    val cardinals = listOf("N" to 0.0, "E" to 90.0, "S" to 180.0, "W" to 270.0)
    cardinals.forEach { (label, degrees) ->
        val angle = Math.toRadians(degrees)
        val layout = textMeasurer.measure(label, style)
        val x = center.x + (labelRadius * Math.sin(angle)).toFloat() - layout.size.width / 2f
        val y = center.y - (labelRadius * Math.cos(angle)).toFloat() - layout.size.height / 2f
        drawText(layout, topLeft = Offset(x, y))
    }
}

/** Soft accent glow behind the needle tip, shown only when locked onto the target heading. */
private fun DrawScope.drawNeedleGlow(colors: CompassColors, alpha: Float) {
    val radius = min(size.width, size.height) / 2f
    val tip = Offset(center.x, center.y - radius * 0.62f)
    val glowColor = colors.needleColors.first()
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor.copy(alpha = alpha), Color.Transparent),
            center = tip,
            radius = radius * 0.28f,
        ),
        radius = radius * 0.28f,
        center = tip,
    )
}

private fun DrawScope.drawNeedle(brush: Brush, colors: CompassColors) {
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
    drawPath(path = tail, color = colors.needleTail)

    // Center hub.
    drawCircle(color = colors.hub, radius = needleHalfWidth * 0.9f, center = center)
    drawCircle(
        color = colors.hubInner,
        radius = needleHalfWidth * 0.5f,
        center = center,
    )
}
