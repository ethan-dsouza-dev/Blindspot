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
     * Returns places within [radiusMeters] of the given coordinates and matching [priceLevel]
     * (1..4) when provided, ideally ordered nearest-first. [priceLevel] = null returns all prices.
     */
    suspend fun getNearbyPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = DEFAULT_RADIUS_METERS,
        priceLevel: Int? = null,
        category: String = "bar",
    ): Result<List<Place>>

    companion object {
        /** Minimum selectable search radius, in meters. */
        const val MIN_RADIUS_METERS = 150

        /** Maximum selectable search radius, in meters. */
        const val MAX_RADIUS_METERS = 5_000

        /** Default search radius applied until the user changes it, in meters. */
        const val DEFAULT_RADIUS_METERS = 1_000
        const val MIN_PRICE_LEVEL = 1
        const val MAX_PRICE_LEVEL = 4
    }
}
