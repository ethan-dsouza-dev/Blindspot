package com.blindspot.app.ui.feed

import com.blindspot.app.data.model.Place
import com.blindspot.app.util.GeoUtils

/**
 * UI model for a Trending Now card. Wraps the existing [Place] domain model together with a
 * pre-formatted distance label, mirroring the (place, distanceLabel) pairing already used by
 * PlaceBanner and PlaceInfoSheet.
 */
data class TrendingPlaceItem(
    val place: Place,
    val distanceLabel: String,
)

/**
 * Hardcoded dummy data for the Trending Now section, used to iterate on design before the
 * backend endpoint is wired up. All imageUrl lists are empty to exercise the placeholder path.
 */
internal val dummyTrendingItems: List<TrendingPlaceItem> = listOf(
    TrendingPlaceItem(
        place = Place(
            id = "t1",
            name = "The Alley Cat",
            description = "A cozy neighbourhood bar with craft beers and live music.",
            category = "bar",
            latitude = 51.5080,
            longitude = -0.1281,
            imageUrl = null,
            rating = 4.3,
            priceLevel = 2,
            distanceMeters = 400.0,
        ),
        distanceLabel = GeoUtils.formatDistance(400.0),
    ),
    TrendingPlaceItem(
        place = Place(
            id = "t2",
            name = "Sakura Ramen",
            description = "Authentic Japanese ramen with rich tonkotsu broth.",
            category = "restaurant",
            latitude = 51.5095,
            longitude = -0.1340,
            imageUrl = null,
            rating = 4.7,
            priceLevel = 2,
            distanceMeters = 1100.0,
        ),
        distanceLabel = GeoUtils.formatDistance(1100.0),
    ),
    TrendingPlaceItem(
        place = Place(
            id = "t3",
            name = "Rooftop Social",
            description = "Open-air terrace bar with panoramic city views.",
            category = "lounge",
            latitude = 51.5110,
            longitude = -0.1200,
            imageUrl = null,
            rating = 4.5,
            priceLevel = 3,
            distanceMeters = 2300.0,
        ),
        distanceLabel = GeoUtils.formatDistance(2300.0),
    ),
    TrendingPlaceItem(
        place = Place(
            id = "t4",
            name = "Bloom & Grind",
            description = "Specialty coffee and homemade pastries in a floral setting.",
            category = "café",
            latitude = 51.5060,
            longitude = -0.1310,
            imageUrl = null,
            rating = 4.6,
            priceLevel = 1,
            distanceMeters = 800.0,
        ),
        distanceLabel = GeoUtils.formatDistance(800.0),
    ),
    TrendingPlaceItem(
        place = Place(
            id = "t5",
            name = "Night Owl Club",
            description = "Late-night DJ sets and a packed dance floor.",
            category = "club",
            latitude = 51.5130,
            longitude = -0.1265,
            imageUrl = null,
            rating = 4.1,
            priceLevel = 3,
            distanceMeters = 3000.0,
        ),
        distanceLabel = GeoUtils.formatDistance(3000.0),
    ),
)
