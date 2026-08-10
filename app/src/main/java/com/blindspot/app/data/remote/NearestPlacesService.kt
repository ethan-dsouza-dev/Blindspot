package com.blindspot.app.data.remote

import com.blindspot.app.data.model.Place

/**
 * Thin service over [PlaceApi] that fetches the bars nearest the user's location and maps the
 * network [PlaceDto]s to domain [Place]s. Keeps the Retrofit contract isolated from callers.
 */
class NearestPlacesService(
    private val api: PlaceApi,
) {

    suspend fun nearby(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = DEFAULT_RADIUS_METERS,
        priceLevel: Int?,
    ): List<Place> =
        api.getNearbyPlaces(latitude, longitude, radiusMeters, priceLevel)
            .map { it.toDomain() }

    suspend fun trending(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = DEFAULT_TRENDING_RADIUS_METERS,
    ): List<Place> =
        api.getTrendingPlaces(latitude, longitude, radiusMeters)
            .map { it.toDomain() }

    /** Resolves a set of place IDs (e.g. favorites) to full [Place] domain objects. */
    suspend fun byIds(ids: List<String>): List<Place> =
        if (ids.isEmpty()) emptyList() else api.getPlacesByIds(ids).map { it.toDomain() }

    companion object {
        const val DEFAULT_RADIUS_METERS = 1000
        const val DEFAULT_TRENDING_RADIUS_METERS = 5000
    }
}