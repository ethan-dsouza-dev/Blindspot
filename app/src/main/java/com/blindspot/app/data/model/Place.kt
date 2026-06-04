package com.blindspot.app.data.model

/**
 * Domain model for a place the compass can point to.
 *
 * Named generically as [Place] (rather than "bar") so the feature can extend to any
 * category of location the user might be interested in.
 */
data class Place(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String? = null,
    val rating: Double? = null,
    val priceLevel: Int? = null,
    val distanceMeters: Double? = null,
)
