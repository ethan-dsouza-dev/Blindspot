package com.blindspot.app.data.repository

import com.blindspot.app.data.model.Place

/**
 * Source of [Place]s for the discovery feature.
 *
 * Swap the bound implementation in the Koin module to move from [MockPlaceRepository]
 * to a real network-backed one without touching the UI / ViewModel.
 */
interface PlaceRepository {

    /**
     * Returns places near the given coordinates, ideally ordered nearest-first.
     */
    suspend fun getNearbyPlaces(
        latitude: Double,
        longitude: Double,
        category: String = "bar",
    ): Result<List<Place>>
}
