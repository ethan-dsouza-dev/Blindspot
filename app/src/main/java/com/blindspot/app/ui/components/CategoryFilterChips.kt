package com.blindspot.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.blindspot.app.ui.theme.AuroraTokens

/**
 * Horizontal row of pill-shaped category filters: a leading "All" chip plus one chip per
 * [categories] entry. The active chip fills with the accent cyan; tapping it again (or "All")
 * clears the filter back to `null`. Color transitions are spring-animated.
 */
@Composable
fun CategoryFilterChips(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") {
            FilterChipPill(
                label = "All",
                selected = selected == null,
                onClick = { onSelect(null) },
            )
        }
        items(categories, key = { it }) { category ->
            FilterChipPill(
                label = category,
                selected = selected == category,
                onClick = { onSelect(if (selected == category) null else category) },
            )
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val background by animateColorAsState(
        targetValue = if (selected) AuroraTokens.AccentCyan else AuroraTokens.SurfaceElevated,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chipBackground",
    )
    val borderColor = if (selected) Color.Transparent else AuroraTokens.SurfaceBorder
    val contentColor = if (selected) AuroraTokens.OnAccent else AuroraTokens.TextSecondary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .pressEffect(interactionSource = interactionSource)
            .wrapContentHeight()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}
