package com.blindspot.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blindspot.app.ui.feed.TrendingPlaceItem
import com.blindspot.app.ui.theme.AuroraTokens

/**
 * "Trending Now" feed section: a left-aligned heading above a horizontally scrollable row of
 * [TrendingPlaceCard]s. Tapping a card invokes [onCardClick] with the tapped item.
 */
@Composable
fun TrendingNowSection(
    items: List<TrendingPlaceItem>,
    onCardClick: (TrendingPlaceItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Trending Now",
            style = MaterialTheme.typography.titleLarge,
            color = AuroraTokens.TextPrimary,
            modifier = Modifier.padding(start = 20.dp, bottom = 12.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.place.id }) { item ->
                TrendingPlaceCard(
                    item = item,
                    onClick = { onCardClick(item) },
                )
            }
        }
    }
}
