package com.blindspot.app.ui.feed

import com.blindspot.app.data.model.Place

/**
 * UI model for a Trending Now card. Wraps the existing [Place] domain model together with a
 * pre-formatted distance label, mirroring the (place, distanceLabel) pairing already used by
 * PlaceBanner and PlaceInfoSheet.
 */
data class TrendingPlaceItem(
    val place: Place,
    val distanceLabel: String,
)
