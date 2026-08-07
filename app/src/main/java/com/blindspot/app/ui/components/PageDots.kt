package com.blindspot.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.blindspot.app.ui.theme.AuroraTokens

/** Max dots rendered at once; larger result sets show a window around the active page so the
 * indicator never overflows the screen width or hides the current position. */
private const val MAX_DOTS = 7

/**
 * Pill of pager dots; the active dot stretches into a capsule in the accent cyan. For more than
 * [MAX_DOTS] pages only a [MAX_DOTS]-wide window centered on [currentPage] is shown (clamped at
 * the list ends), so the indicator stays bounded and the active dot always remains visible.
 */
@Composable
fun PageDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    val shown = pageCount.coerceAtMost(MAX_DOTS)
    val windowStart = (currentPage - (MAX_DOTS - 1) / 2)
        .coerceIn(0, (pageCount - MAX_DOTS).coerceAtLeast(0))

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        repeat(shown) { offset ->
            val index = windowStart + offset
            val selected = currentPage == index
            val width by animateDpAsState(
                targetValue = if (selected) 18.dp else 6.dp,
                label = "pageDotWidth",
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            AuroraTokens.AccentCyan
                        } else {
                            AuroraTokens.TextPrimary.copy(alpha = 0.4f)
                        },
                    ),
            )
        }
    }
}
