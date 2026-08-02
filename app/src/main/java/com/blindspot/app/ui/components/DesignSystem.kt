package com.blindspot.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blindspot.app.ui.theme.AuroraTokens

/**
 * Shared interaction modifiers for the "Midnight Aurora" system.
 * Press/focus states use cyan overlays and spring scaling so every interactive
 * element feels consistent and smooth.
 */

/** Subtle scale-down on press. */
fun Modifier.pressEffect(
    scale: Float = 0.98f,
    interactionSource: MutableInteractionSource? = null,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo { name = "pressEffect" },
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (pressed) scale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "pressScale",
    )
    graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
    }
}

/** Full-width row highlight on press (cyan tint, no scale). */
fun Modifier.listRowPress(
    interactionSource: MutableInteractionSource? = null,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo { name = "listRowPress" },
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    background(
        color = if (pressed) AuroraTokens.AccentCyan.copy(alpha = 0.05f) else Color.Transparent,
    )
}

/** Card press — subtle scale, no overlay. */
fun Modifier.cardPress(
    scale: Float = 0.99f,
    interactionSource: MutableInteractionSource? = null,
): Modifier = pressEffect(scale = scale, interactionSource = interactionSource)

/** Nav item press — slightly stronger scale. */
fun Modifier.navItemPress(
    interactionSource: MutableInteractionSource? = null,
): Modifier = pressEffect(scale = 0.95f, interactionSource = interactionSource)

/** Focus ring for accessibility (keyboard / switch navigation). */
fun Modifier.focusEffect(
    interactionSource: MutableInteractionSource? = null,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo { name = "focusEffect" },
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    border(
        width = if (focused) 2.dp else 0.dp,
        color = AuroraTokens.AccentCyan.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp),
    )
}

/** Enforce the 48dp minimum touch target for small clickable elements. */
fun Modifier.minTouchTarget(
    minWidth: Dp = 48.dp,
    minHeight: Dp = 48.dp,
): Modifier = composed(
    inspectorInfo = debugInspectorInfo { name = "minTouchTarget" },
) {
    widthIn(min = minWidth).heightIn(min = minHeight)
}