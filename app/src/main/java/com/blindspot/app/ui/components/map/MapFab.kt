package com.blindspot.app.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.blindspot.app.ui.components.aurora.AuroraFloating
import com.blindspot.app.ui.components.focusEffect
import com.blindspot.app.ui.components.navItemPress
import com.blindspot.app.ui.theme.AuroraTokens

/**
 * Floating circular action button for the map screen: 48dp aurora pill with the shared
 * press/focus interactions. When [active], the resting [tint] icon swaps to [activeTint] and a
 * soft glow fill renders behind it (mirroring the AccentCyanGlow token at the active color).
 *
 * Caller supplies placement via [modifier]; the button itself enforces the 48dp circle.
 */
@Composable
fun MapFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AuroraTokens.TextTertiary,
    active: Boolean = false,
    activeTint: Color = AuroraTokens.AccentCyan,
    iconSize: Dp = 22.dp,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    AuroraFloating(
        shape = CircleShape,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .navItemPress(interactionSource = interactionSource)
            .focusEffect(interactionSource = interactionSource),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (active) {
                        Modifier.background(activeTint.copy(alpha = 0.15f), CircleShape)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (active) activeTint else tint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}