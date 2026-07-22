package com.blindspot.app.ui.discovery

import com.blindspot.app.data.model.Place
import com.blindspot.app.data.repository.PlaceRepository

data class DiscoveryUiState(
    val status: Status = Status.Loading,
    val places: List<Place> = emptyList(),
    val currentIndex: Int = 0,
    val radiusMeters: Int = PlaceRepository.DEFAULT_RADIUS_METERS,
    /** Current price point (1..4, i.e. $..$$$$). Null = show all prices. */
    val priceLevel: Int? = null,
    /** Needle rotation in degrees: bearing to the current place minus device heading. */
    val needleRotation: Float = 0f,
    /** Distance to the current place, pre-formatted for display (e.g. "320 m"). */
    val distanceLabel: String = "",
    /** True while a silent reload (manual refresh or radius change) is in flight. */
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
) {
    val currentPlace: Place? get() = places.getOrNull(currentIndex)
    val hasNext: Boolean get() = currentIndex < places.lastIndex
    val hasPrevious: Boolean get() = currentIndex > 0

    enum class Status { Loading, Content, Empty, Error }
}
