package com.blindspot.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit definition for the routing endpoint.
 *
 * Maps to `GET /routes?from_lat=&from_lng=&to_lat=&to_lng=&mode=` on the Blindspot backend,
 * which proxies the Geoapify Routing API and returns the geometry as an encoded polyline.
 */
interface RouteApi {

    @GET("routes")
    suspend fun getRoute(
        @Query("from_lat") fromLatitude: Double,
        @Query("from_lng") fromLongitude: Double,
        @Query("to_lat") toLatitude: Double,
        @Query("to_lng") toLongitude: Double,
        @Query("mode") mode: String,
    ): RouteDto
}
