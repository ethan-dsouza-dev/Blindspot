package com.blindspot.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.blindspot.app.ui.components.PlaceInfoSheet
import com.blindspot.app.ui.components.TrendingNowSection
import com.blindspot.app.ui.feed.TrendingPlaceItem
import com.blindspot.app.ui.feed.dummyTrendingItems

/**
 * Feed tab. Currently hosts the Trending Now section (dummy data for design iteration).
 * Additional sections (For You, Recently Viewed, etc.) can be added as further LazyColumn items.
 *
 * Tapping a trending card opens a [PlaceInfoSheet] whose "View on Map" action calls
 * [onNavigateToMaps] to switch the app to the Maps tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToMaps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedItem by remember { mutableStateOf<TrendingPlaceItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Text(
                text = "Feed",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp),
            )
        }
        item {
            TrendingNowSection(
                items = dummyTrendingItems,
                onCardClick = { selectedItem = it },
            )
        }
        // Future sections (For You, Recently Viewed, etc.) go here as additional item { } blocks.
    }

    selectedItem?.let { item ->
        PlaceInfoSheet(
            place = item.place,
            distanceLabel = item.distanceLabel,
            sheetState = sheetState,
            onDismiss = { selectedItem = null },
            onSkip = { selectedItem = null },
            onBack = { selectedItem = null },
            showBack = false,
            onViewOnMap = onNavigateToMaps,
        )
    }
}
