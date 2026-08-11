package com.blindspot.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindspot.app.auth.TokenStore
import com.blindspot.app.data.repository.FavoritesRepository
import com.blindspot.app.data.repository.PlaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class ProfileViewModel(
    private val tokenStore: TokenStore,
    private val favoritesRepository: FavoritesRepository,
    private val placeRepository: PlaceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeFavorites()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val user = withContext(Dispatchers.IO) { tokenStore.currentUser }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = user?.name?.takeIf { it.isNotBlank() } ?: user?.email ?: "User",
                        email = user?.email ?: "",
                        avatarUrl = user?.pictureUrl,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Couldn't load profile",
                    )
                }
            }
        }
    }

    /** Keeps `savedPlaces` in sync with the shared favorites set — this is the same list that
     * drives the heart icon in [com.blindspot.app.ui.components.PlaceInfoSheet] everywhere else
     * in the app, so favoriting/unfavoriting from any screen updates here automatically. */
    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.favoritePlaceIds.collectLatest { ids ->
                if (ids.isEmpty()) {
                    _uiState.update { it.copy(savedPlaces = emptyList()) }
                    return@collectLatest
                }

                var attempt = 0
                while (true) {
                    val result = placeRepository.getPlacesByIds(ids.toList())
                    if (result.isSuccess) {
                        _uiState.update { it.copy(savedPlaces = result.getOrThrow()) }
                        break
                    }
                    attempt++
                    delay(minOf(30_000L, 1_000L * (1 shl attempt.coerceAtMost(5))))
                }
            }
        }
    }

    fun onToggleUnits(useKilometers: Boolean) {
        _uiState.update { it.copy(unitsInKilometers = useKilometers) }
        // TODO: persist to DataStore/SharedPrefs
    }

    fun onToggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        // TODO: persist to DataStore/SharedPrefs
    }

    fun onSignOut() {
        // TODO: hook up when auth exists
    }
}