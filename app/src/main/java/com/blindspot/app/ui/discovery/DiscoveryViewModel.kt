package com.blindspot.app.ui.discovery

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindspot.app.data.model.Place
import com.blindspot.app.data.repository.PlaceRepository
import com.blindspot.app.location.LocationProvider
import com.blindspot.app.sensor.CompassSensorManager
import com.blindspot.app.util.GeoUtils
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
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

    /** Latest slider radius. Reloads are debounced off this stream so we query once the
     * user stops sliding rather than on every intermediate value. */
    private val radiusMeters = MutableStateFlow(_uiState.value.radiusMeters)

    private var lastLocation: Location? = null
    private var deviceHeading: Float = 0f
    private var started = false
    private var placesLoaded = false

    /**
     * Begins location + heading collection. Places are loaded only once a real location fix is
     * available (see [onLocationUpdate]); we never query with a placeholder coordinate, which
     * previously generated places near (0,0) and produced wildly wrong distances.
     */
    fun start() {
        if (started) return
        started = true
        _uiState.update { it.copy(status = DiscoveryUiState.Status.Loading) }
        observeHeading()
        observeLocation()
        observeRadius()
    }

    /**
     * Sets the search radius from the slider, clamped to the supported range. The value is
     * reflected in the UI immediately; the actual reload is debounced (see [observeRadius]).
     */
    fun setRadius(meters: Int) {
        val clamped = meters.coerceIn(
            PlaceRepository.MIN_RADIUS_METERS,
            PlaceRepository.MAX_RADIUS_METERS,
        )
        if (clamped == _uiState.value.radiusMeters) return
        _uiState.update { it.copy(radiusMeters = clamped) }
        radiusMeters.value = clamped
    }

    @OptIn(FlowPreview::class)
    private fun observeRadius() {
        viewModelScope.launch {
            // drop(1) skips the initial value; the first load happens on the location fix.
            radiusMeters.drop(1)
                .debounce(RADIUS_DEBOUNCE_MS)
                .collectLatest {
                    val location = lastLocation ?: return@collectLatest
                    loadPlaces(location)
                }
        }
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
            // Seed with last known location for an immediate fix, if available.
            locationProvider.lastLocation()?.let { onLocationUpdate(it) }
            locationProvider.locationUpdates().collect { onLocationUpdate(it) }
        }
    }

    private fun onLocationUpdate(location: Location) {
        lastLocation = location
        // Load places against the first real fix; later movement just refreshes the compass.
        if (!placesLoaded) {
            placesLoaded = true
            loadPlaces(location)
        }
        recomputeCompass()
    }

    private fun loadPlaces(location: Location) {
        viewModelScope.launch {
            _uiState.update { it.copy(status = DiscoveryUiState.Status.Loading) }
            placeRepository.getNearbyPlaces(
                location.latitude,
                location.longitude,
                _uiState.value.radiusMeters,
            )
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
                    placesLoaded = false // allow retry to reload
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

    /** Returns the compass to the previously recommended place (the reverse of [skipToNext]). */
    fun skipToPrevious() {
        val state = _uiState.value
        if (!state.hasPrevious) return
        _uiState.update { it.copy(currentIndex = it.currentIndex - 1) }
        recomputeCompass()
    }

    /** Manual retry after an error. Reloads against the latest known location, if any. */
    fun retry() {
        val location = lastLocation ?: return
        placesLoaded = true
        loadPlaces(location)
    }

    private fun recomputeCompass() {
        val state = _uiState.value
        val place: Place = state.currentPlace ?: return
        val location = lastLocation ?: return

        val bearing = GeoUtils.bearingBetween(
            location.latitude, location.longitude, place.latitude, place.longitude,
        )
        val rotation = GeoUtils.normalizeDegrees(bearing - deviceHeading)
        // Always compute distance from the live location so it updates as the user moves. The
        // backend-provided `place.distanceMeters` is only a snapshot from query time.
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

    private companion object {
        /** How long the user must stop sliding before we requery, in milliseconds. */
        const val RADIUS_DEBOUNCE_MS = 350L
    }
}
