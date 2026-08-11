package com.blindspot.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class FavoriteRequest(
    @SerializedName("placeId") val placeId: String,
)

data class FavoritesResponse(
    @SerializedName("placeIds") val placeIds: List<String>,
)

interface FavoritesApi {

    @GET("api/favorites")
    suspend fun getFavorites(): FavoritesResponse

    @POST("api/favorites")
    suspend fun addFavorite(@Body request: FavoriteRequest)

    @DELETE("api/favorites/{placeId}")
    suspend fun removeFavorite(@Path("placeId") placeId: String)
}