package com.blindspot.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blindspot.app.navigation.Destination
import com.blindspot.app.ui.components.aurora.AuroraSurface
import com.blindspot.app.ui.theme.AuroraTokens

private val PillShape = RoundedCornerShape(32.dp)

@Composable
fun FloatingNavPill(
    selected: Destination,
    onSelect: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AuroraSurface(
            shape = PillShape,
            modifier = Modifier.fillMaxWidth(0.9f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Destination.entries.forEach { destination ->
                    FloatingNavItem(
                        selected = selected == destination,
                        onClick = { onSelect(destination) },
                        label = destination.label,
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                                modifier = Modifier.size(24.dp),
                                tint = it,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.FloatingNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: @Composable (tint: Color) -> Unit,
) {
    val tint by animateColorAsState(
        targetValue = if (selected) AuroraTokens.AccentCyan else AuroraTokens.TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "navItemTint",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon(tint)
        Text(
            text = label,
            fontSize = 11.sp,
            color = tint,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
