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
     * Returns the places near [latitude]/[longitude] within [radiusMeters], nearest-first as
     * ordered by the backend. Network/parsing failures propagate to the caller.
     */
    suspend fun nearby(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = DEFAULT_RADIUS_METERS,
    ): List<Place> =
        api.getNearbyPlaces(latitude, longitude, radiusMeters)
            .map { it.toDomain() }

    companion object {
        const val DEFAULT_RADIUS_METERS = 100
    }
}
