package com.blindspot.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit definition for the places endpoint.
 *
 * The backend is not yet implemented; this interface documents the expected contract so the
 * real implementation can be dropped in by swapping the repository binding in Koin.
 */
interface PlaceApi {

    @GET("places")
    suspend fun getNearbyPlaces(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("category") category: String = "bar",
        @Query("radius_m") radiusMeters: Int = 5_000,
    ): List<PlaceDto>
}
