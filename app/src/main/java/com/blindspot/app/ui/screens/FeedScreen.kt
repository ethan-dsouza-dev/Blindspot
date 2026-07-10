package com.blindspot.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blindspot.app.data.model.Place
import com.blindspot.app.ui.components.NearbyPlaceRow
import com.blindspot.app.ui.components.PlaceInfoSheet
import com.blindspot.app.ui.components.TrendingNowSection
import com.blindspot.app.ui.feed.TrendingPlaceItem
import com.blindspot.app.ui.feed.dummyTrendingItems
import com.blindspot.app.ui.theme.AuroraTokens

/**
 * Explore tab: a Trending Now rail plus a Near You list (dummy data for design iteration).
 * Additional sections (Open Late, For You, etc.) can be added as further LazyColumn items.
 *
 * Tapping any venue opens the shared [PlaceInfoSheet]; its "Take me there" action calls
 * [onNavigateToMaps] to guide the user on the Maps tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToMaps: (Place) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedItem by remember { mutableStateOf<TrendingPlaceItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val nearbyItems = remember { dummyTrendingItems.sortedBy { it.place.distanceMeters } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 100.dp),
    ) {
        item {
            Text(
                text = "Explore",
                style = MaterialTheme.typography.headlineLarge,
                color = AuroraTokens.TextPrimary,
                modifier = Modifier.padding(start = 20.dp, top = 24.dp),
            )
        }
        item {
            TrendingNowSection(
                items = dummyTrendingItems,
                onCardClick = { selectedItem = it },
                modifier = Modifier.padding(top = 28.dp),
            )
        }
        item {
            Text(
                text = "Near you",
                style = MaterialTheme.typography.titleLarge,
                color = AuroraTokens.TextPrimary,
                modifier = Modifier.padding(start = 20.dp, top = 28.dp, bottom = 4.dp),
            )
        }
        items(nearbyItems, key = { it.place.id }) { item ->
            NearbyPlaceRow(
                item = item,
                onClick = { selectedItem = item },
            )
        }
        // Future sections (Open Late, For You, etc.) go here as additional item { } blocks.
    }

    selectedItem?.let { item ->
        PlaceInfoSheet(
            place = item.place,
            distanceLabel = item.distanceLabel,
            sheetState = sheetState,
            onDismiss = { selectedItem = null },
            onBack = { selectedItem = null },
            showBack = false,
            onViewOnMap = { onNavigateToMaps(item.place) },
        )
    }
}
