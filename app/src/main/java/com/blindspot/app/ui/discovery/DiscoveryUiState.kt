package com.blindspot.app.ui.discovery

import com.blindspot.app.data.model.Place

/**
 * UI state for the Discovery screen.
 */
data class DiscoveryUiState(
    val status: Status = Status.Loading,
    val places: List<Place> = emptyList(),
    val currentIndex: Int = 0,
    /** Needle rotation in degrees: bearing to the current place minus device heading. */
    val needleRotation: Float = 0f,
    /** Distance to the current place, pre-formatted for display (e.g. "320 m"). */
    val distanceLabel: String = "",
    val errorMessage: String? = null,
) {
    val currentPlace: Place? get() = places.getOrNull(currentIndex)
    val hasNext: Boolean get() = currentIndex < places.lastIndex
    val hasPrevious: Boolean get() = currentIndex > 0

    enum class Status { Loading, Content, Empty, Error }
}
