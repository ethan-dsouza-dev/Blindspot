package com.blindspot.app.ui.discovery

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindspot.app.data.model.Place
import com.blindspot.app.data.repository.PlaceRepository
import com.blindspot.app.data.repository.UnitsRepository
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlacesViewModel(
    private val placeRepository: PlaceRepository,
    private val locationProvider: LocationProvider,
    private val compassSensorManager: CompassSensorManager,
    private val unitsRepository: UnitsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    val nearbyPlaces: StateFlow<List<Place>> = uiState
        .map { it.places }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _trendingPlaces = MutableStateFlow<List<Place>>(emptyList())
    val trendingPlaces: StateFlow<List<Place>> = _trendingPlaces.asStateFlow()

    private val radiusMeters = MutableStateFlow(_uiState.value.radiusMeters)
    private val priceLevel = MutableStateFlow(_uiState.value.priceLevel)

    private var lastLocation: Location? = null
    private var deviceHeading: Float = 0f
    private var useKilometers: Boolean = true
    private var started = false
    private var placesLoaded = false

    fun start() {
        if (started) return
        started = true
        _uiState.update { it.copy(status = DiscoveryUiState.Status.Loading) }
        observeHeading()
        observeLocation()
        observeRadius()
        observePriceLevel()
        observeUnits()
    }

    /** Keeps the compass distance label in the user's chosen unit, and recomputes it
     * immediately whenever the preference changes (e.g. toggled while this screen is open). */
    private fun observeUnits() {
        viewModelScope.launch {
            unitsRepository.useKilometers.collect { value ->
                useKilometers = value
                recomputeCompass()
            }
        }
    }

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
            radiusMeters.drop(1)
                .debounce(RADIUS_DEBOUNCE_MS)
                .collectLatest {
                    val location = lastLocation ?: return@collectLatest
                    loadPlaces(location, isRefresh = true)
                }
        }
    }

    fun setPriceLevel(level: Int?) {
        val clamped = level?.coerceIn(
            PlaceRepository.MIN_PRICE_LEVEL,
            PlaceRepository.MAX_PRICE_LEVEL,
        )
        if (clamped == _uiState.value.priceLevel) return
        _uiState.update { it.copy(priceLevel = clamped) }
        priceLevel.value = clamped
    }

    @OptIn(FlowPreview::class)
    private fun observePriceLevel() {
        viewModelScope.launch {
            priceLevel.drop(1)
                .debounce(PRICE_DEBOUNCE_MS)
                .collectLatest {
                    val location = lastLocation ?: return@collectLatest
                    loadPlaces(location, isRefresh = true)
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
            locationProvider.lastLocation()?.let { onLocationUpdate(it) }
            locationProvider.locationUpdates().collect { onLocationUpdate(it) }
        }
    }

    private fun onLocationUpdate(location: Location) {
        lastLocation = location
        if (!placesLoaded) {
            placesLoaded = true
            loadPlaces(location)
            loadTrending(location)
        }
        recomputeCompass()
    }

    private fun loadPlaces(location: Location, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(status = DiscoveryUiState.Status.Loading) }
            }
            val result = placeRepository.getNearbyPlaces(
                location.latitude,
                location.longitude,
                _uiState.value.radiusMeters,
                _uiState.value.priceLevel,
            )
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = false) }
            }
            result
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
                    if (isRefresh) return@onFailure
                    placesLoaded = false
                    _uiState.update {
                        it.copy(
                            status = DiscoveryUiState.Status.Error,
                            errorMessage = error.message ?: "Could not load places.",
                        )
                    }
                }
        }
    }

    private fun loadTrending(location: Location) {
        viewModelScope.launch {
            placeRepository.getTrendingPlaces(
                location.latitude,
                location.longitude,
                _uiState.value.radiusMeters,
            )
                .onSuccess { _trendingPlaces.value = it }
        }
    }

    fun skipToNext() {
        val state = _uiState.value
        if (!state.hasNext) return
        _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
        recomputeCompass()
    }

    fun skipToPrevious() {
        val state = _uiState.value
        if (!state.hasPrevious) return
        _uiState.update { it.copy(currentIndex = it.currentIndex - 1) }
        recomputeCompass()
    }

    fun retry() {
        val location = lastLocation ?: return
        placesLoaded = true
        loadPlaces(location)
    }

    fun refresh() {
        val location = lastLocation ?: return
        if (_uiState.value.isRefreshing) return
        loadPlaces(location, isRefresh = true)
    }

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
                distanceLabel = GeoUtils.formatDistance(distance, useKilometers),
            )
        }
    }

    private companion object {
        const val RADIUS_DEBOUNCE_MS = 350L
        const val PRICE_DEBOUNCE_MS = 250L
    }
}