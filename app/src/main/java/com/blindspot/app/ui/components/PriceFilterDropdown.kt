package com.blindspot.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blindspot.app.data.repository.PlaceRepository
import com.blindspot.app.ui.components.aurora.AuroraFloating
import com.blindspot.app.ui.theme.AuroraTokens

@Composable
fun PriceFilterDropdown(
    priceLevel: Int?,
    onPriceChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "priceChevronRotation"
    )
    val options = remember {
        listOf<Int?>(null) + (PlaceRepository.MIN_PRICE_LEVEL..PlaceRepository.MAX_PRICE_LEVEL).toList()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Price",
                style = MaterialTheme.typography.bodyMedium,
                color = AuroraTokens.TextSecondary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priceLabel(priceLevel),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = AuroraTokens.AccentCyan
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse price filter" else "Expand price filter",
                    tint = AuroraTokens.TextSecondary,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .rotate(chevronRotation)
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )) + fadeIn(spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )),
            exit = shrinkVertically(animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )) + fadeOut(spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ))
        ) {
            AuroraFloating(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column {
                    options.forEachIndexed { index, level ->
                        val selected = level == priceLevel
                        Text(
                            text = priceLabel(level),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (selected) AuroraTokens.AccentCyan else AuroraTokens.TextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPriceChange(level)
                                    expanded = false
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        )
                        if (index < options.lastIndex) {
                            HorizontalDivider(
                                color = AuroraTokens.SurfaceBorder,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Renders a price level (1..4) as repeated dollar signs, or "Any" for null. */
private fun priceLabel(level: Int?): String =
    if (level == null) "Any" else "$".repeat(level.coerceIn(1, 4))