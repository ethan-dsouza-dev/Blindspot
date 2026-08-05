package com.blindspot.app.data.remote

import com.blindspot.app.data.model.Place

/**
 * Thin service over [PlaceApi] that fetches the bars nearest the user's location and maps the
 * network [PlaceDto]s to domain [Place]s. Keeps the Retrofit contract isolated from callers.
 */
class NearestPlacesService(
    private val api: PlaceApi,
) {

    /**
     * Returns the places near [latitude]/[longitude] within [radiusMeters] and matching
     * [priceLevel] (1..4), nearest-first as ordered by the backend. Network/parsing failures
     * propagate to the caller.
     */
    suspend fun nearby(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = DEFAULT_RADIUS_METERS,
        priceLevel: Int?,
    ): List<Place> =
        api.getNearbyPlaces(latitude, longitude, radiusMeters, priceLevel)
            .map { it.toDomain() }

    /**
     * Returns the currently trending places near [latitude]/[longitude] within [radiusMeters],
     * in the backend's trending order. Network/parsing failures propagate to the caller.
     */
    suspend fun trending(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = DEFAULT_TRENDING_RADIUS_METERS,
    ): List<Place> =
        api.getTrendingPlaces(latitude, longitude, radiusMeters)
            .map { it.toDomain() }

    companion object {
        const val DEFAULT_RADIUS_METERS = 1000

        /** Backend default radius for the trending endpoint, in meters. */
        const val DEFAULT_TRENDING_RADIUS_METERS = 5000
    }
}
