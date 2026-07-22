package com.blindspot.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit definition for the bars-nearby endpoint.
 *
 * Maps to `GET /bars/nearby?lat=&lng=&radius=&priceLevel=` on the Blindspot backend.
 */
interface PlaceApi {

    @GET("bars/nearby")
    suspend fun getNearbyPlaces(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("radius") radiusMeters: Int,
        @Query("priceLevel") priceLevel: Int?,
    ): List<PlaceDto>
}
