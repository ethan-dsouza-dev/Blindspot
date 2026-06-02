package com.blindspot.app.ui.discovery

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindspot.app.data.model.Place
import com.blindspot.app.data.repository.PlaceRepository
import com.blindspot.app.location.LocationProvider
import com.blindspot.app.sensor.CompassSensorManager
import com.blindspot.app.util.GeoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds the place list and [DiscoveryUiState.currentIndex], and continuously recomputes the
 * compass needle rotation from the latest user location, device heading, and current target.
 *
 * Skip logic: [skipToNext] advances the index and the compass retargets reactively.
 */
class DiscoveryViewModel(
    private val placeRepository: PlaceRepository,
    private val locationProvider: LocationProvider,
    private val compassSensorManager: CompassSensorManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    private var lastLocation: Location? = null
    private var deviceHeading: Float = 0f
    private var started = false

    /**
     * Begins location + heading collection and loads places. Safe to call repeatedly (e.g. after
     * permission is granted); only the first call starts collection.
     */
    fun start() {
        if (started) return
        started = true
        observeHeading()
        observeLocation()
        loadPlaces()
    }

    private fun observeHeading() {
        viewModelScope.launch {
            compassSensorManager.headingDegrees().collect { heading ->
                deviceHeading = heading
                recomputeCompass()
            }
        }
    }

    private fun observeLocation() {
        viewModelScope.launch {
            // Seed with last known location for an immediate fix.
            locationProvider.lastLocation()?.let {
                lastLocation = it
                if (_uiState.value.places.isEmpty()) loadPlaces()
                recomputeCompass()
            }
            locationProvider.locationUpdates().collect { location ->
                lastLocation = location
                recomputeCompass()
            }
        }
    }

    private fun loadPlaces() {
        val location = lastLocation
        viewModelScope.launch {
            _uiState.update { it.copy(status = DiscoveryUiState.Status.Loading) }
            // Default to a neutral coordinate if we have no fix yet; will refresh when one arrives.
            val lat = location?.latitude ?: 0.0
            val lng = location?.longitude ?: 0.0
            placeRepository.getNearbyPlaces(lat, lng)
                .onSuccess { places ->
                    _uiState.update {
                        it.copy(
                            status = if (places.isEmpty()) {
                                DiscoveryUiState.Status.Empty
                            } else {
                                DiscoveryUiState.Status.Content
                            },
                            places = places,
                            currentIndex = 0,
                        )
                    }
                    recomputeCompass()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            status = DiscoveryUiState.Status.Error,
                            errorMessage = error.message ?: "Could not load places.",
                        )
                    }
                }
        }
    }

    /** Advances the compass to the next place in the list. */
    fun skipToNext() {
        val state = _uiState.value
        if (!state.hasNext) return
        _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
        recomputeCompass()
    }

    /** Manual retry after an error. */
    fun retry() = loadPlaces()

    private fun recomputeCompass() {
        val state = _uiState.value
        val place: Place = state.currentPlace ?: return
        val location = lastLocation ?: return

        val bearing = GeoUtils.bearingBetween(
            location.latitude, location.longitude, place.latitude, place.longitude,
        )
        val rotation = GeoUtils.normalizeDegrees(bearing - deviceHeading)
        val distance = GeoUtils.distanceMeters(
            location.latitude, location.longitude, place.latitude, place.longitude,
        )

        _uiState.update {
            it.copy(
                needleRotation = rotation,
                distanceLabel = GeoUtils.formatDistance(distance),
            )
        }
    }
}
