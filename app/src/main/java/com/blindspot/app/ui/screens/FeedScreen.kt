package com.blindspot.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blindspot.app.data.model.Place
import com.blindspot.app.ui.components.CategoryFilterChips
import com.blindspot.app.ui.components.NearbyPlaceRow
import com.blindspot.app.ui.components.PlaceInfoSheet
import com.blindspot.app.ui.components.TrendingNowSection
import com.blindspot.app.ui.discovery.PlacesViewModel
import com.blindspot.app.ui.feed.TrendingPlaceItem
import com.blindspot.app.ui.theme.AuroraTokens
import com.blindspot.app.util.GeoUtils
import com.blindspot.app.util.categoryLabel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.blindspot.app.data.repository.FavoritesRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.blindspot.app.data.repository.FavoritesNotReadyException

/**
 * Feed tab: a pinned header (title + category filter chips) above a scrollable Trending Now rail
 * (loaded from [PlacesViewModel.trendingPlaces]) and Near You list (loaded from
 * [PlacesViewModel.nearbyPlaces]). Selecting a category filters both sections; a mismatch shows
 * an inline empty state.
 *
 * Tapping any venue opens the shared [PlaceInfoSheet]; its "Take me there" action calls
 * [onNavigateToMaps] to guide the user on the Maps tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToMaps: (Place) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlacesViewModel = koinViewModel(),
) {
    val places by viewModel.nearbyPlaces.collectAsStateWithLifecycle()
    val trendingPlaces by viewModel.trendingPlaces.collectAsStateWithLifecycle()
    var selectedItem by remember { mutableStateOf<TrendingPlaceItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val favoritesRepository: FavoritesRepository = koinInject()
    val favoriteIds by favoritesRepository.favoritePlaceIds.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val nearbyItems = remember(places) {
        places.map { place ->
            TrendingPlaceItem(
                place = place,
                distanceLabel = place.distanceMeters?.let(GeoUtils::formatDistance) ?: "",
            )
        }.sortedBy { it.place.distanceMeters ?: Double.MAX_VALUE }
    }

    val trendingItems = remember(trendingPlaces) {
        trendingPlaces.map { place ->
            TrendingPlaceItem(
                place = place,
                distanceLabel = place.distanceMeters?.let(GeoUtils::formatDistance) ?: "",
            )
        }
    }

    val categories = remember(places, trendingPlaces) {
        (places + trendingPlaces).map { it.categoryLabel }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val filteredNearbyItems = remember(nearbyItems, selectedCategory) {
        if (selectedCategory == null) nearbyItems
        else nearbyItems.filter { it.place.categoryLabel == selectedCategory }
    }
    val filteredTrendingItems = remember(trendingItems, selectedCategory) {
        if (selectedCategory == null) trendingItems
        else trendingItems.filter { it.place.categoryLabel == selectedCategory }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Text(
                text = "Feed",
                style = MaterialTheme.typography.headlineLarge,
                color = AuroraTokens.TextPrimary,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp),
            )
            CategoryFilterChips(
                categories = categories,
                selected = selectedCategory,
                onSelect = { selectedCategory = it },
                modifier = Modifier.padding(top = 20.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 24.dp, bottom = 96.dp),
        ) {
            if (filteredTrendingItems.isNotEmpty()) {
                item {
                    TrendingNowSection(
                        items = filteredTrendingItems,
                        onCardClick = { selectedItem = it },
                    )
                }
            }
            if (filteredNearbyItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Near you",
                        style = MaterialTheme.typography.titleLarge,
                        color = AuroraTokens.TextPrimary,
                        modifier = Modifier.padding(start = 24.dp, top = 32.dp, bottom = 4.dp),
                    )
                }
                items(filteredNearbyItems, key = { it.place.id }) { item ->
                    NearbyPlaceRow(
                        item = item,
                        onClick = { selectedItem = item },
                    )
                }
                // Future sections (Open Late, For You, etc.) go here as additional item { } blocks.
            }
            if (filteredTrendingItems.isEmpty() && filteredNearbyItems.isEmpty() && selectedCategory != null) {
                item {
                    FeedFilterEmptyState(category = selectedCategory!!)
                }
            }
        }
    }

    selectedItem?.let { item ->
        PlaceInfoSheet(
            place = item.place,
            distanceLabel = item.distanceLabel,
            sheetState = sheetState,
            isFavorite = item.place.id in favoriteIds,
            onToggleFavorite = {
                coroutineScope.launch {
                    try {
                        favoritesRepository.toggleFavorite(item.place.id)
                    } catch (e: FavoritesNotReadyException) {
                        // Favorites haven't loaded yet (or kept failing) — refuse rather than guess.
                        // The heart stays as-is; user can retry once connectivity/load succeeds.
                    }
                }
            },
            onDismiss = { selectedItem = null },
            onBack = { selectedItem = null },
            showBack = false,
            onViewOnMap = { onNavigateToMaps(item.place) },
        )
    }
}

@Composable
private fun FeedFilterEmptyState(category: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "No $category right now",
            style = MaterialTheme.typography.titleLarge,
            color = AuroraTokens.TextPrimary,
        )
        Text(
            text = "Try another filter or widen your search.",
            style = MaterialTheme.typography.bodyMedium,
            color = AuroraTokens.TextSecondary,
        )
    }
}
