package com.blindspot.app.data.remote

import com.blindspot.app.data.model.Place
import com.google.gson.annotations.SerializedName

/**
 * Network representation of a place returned by the backend.
 * Kept separate from the domain [Place] model so the API contract can evolve independently.
 */
data class PlaceDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("price_level") val priceLevel: Int? = null,
)

fun PlaceDto.toDomain(): Place = Place(
    id = id,
    name = name,
    description = description.orEmpty(),
    category = category.orEmpty(),
    latitude = latitude,
    longitude = longitude,
    imageUrl = imageUrl,
    rating = rating,
    priceLevel = priceLevel,
)
