package com.blindspot.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blindspot.app.data.repository.PlaceRepository
import com.blindspot.app.ui.theme.AuroraTokens

/**
 * "Midnight Aurora" single-select price-point filter. A collapsed pill shows the current
 * selection ("Any" or $..$$$$); tapping it reveals the full option list with a smooth
 * expand/fade animation and a rotating chevron. Selecting an option calls [onPriceChange].
 *
 * @param priceLevel current price point, 1..4, or null for "Any".
 * @param onPriceChange called with the newly selected price level, or null for "Any".
 */
@Composable
fun PriceFilterDropdown(
    priceLevel: Int?,
    onPriceChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "priceChevronRotation",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Price",
                style = MaterialTheme.typography.labelLarge,
                color = AuroraTokens.TextSecondary,
            )
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .border(1.dp, AuroraTokens.SurfaceBorder, MaterialTheme.shapes.medium)
                    .background(AuroraTokens.SurfaceElevated)
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = priceLabel(priceLevel),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = AuroraTokens.AccentCyan,
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse price filter" else "Expand price filter",
                    tint = AuroraTokens.TextSecondary,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .rotate(chevronRotation),
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(tween(200)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .border(1.dp, AuroraTokens.SurfaceBorder, MaterialTheme.shapes.medium)
                    .background(AuroraTokens.SurfaceElevated),
            ) {
                listOf(null) + (PlaceRepository.MIN_PRICE_LEVEL..PlaceRepository.MAX_PRICE_LEVEL).toList()
                    .forEach { level ->
                        val selected = level == priceLevel
                        Text(
                            text = priceLabel(level),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            ),
                            color = if (selected) AuroraTokens.AccentCyan else AuroraTokens.TextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPriceChange(level)
                                    expanded = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                        )
                    }
            }
        }
    }
}

/** Renders a price level (1..4) as repeated dollar signs, or "Any" for null. */
private fun priceLabel(level: Int?): String =
    if (level == null) "Any" else "$".repeat(level.coerceIn(1, 4))
