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
import com.blindspot.app.data.repository.UnitsRepository
import com.blindspot.app.data.repository.ThemeRepository
import com.blindspot.app.ui.theme.AppTheme
import com.blindspot.app.data.repository.NotificationsRepository

class ProfileViewModel(
    private val tokenStore: TokenStore,
    private val favoritesRepository: FavoritesRepository,
    private val placeRepository: PlaceRepository,
    private val unitsRepository: UnitsRepository,
    private val themeRepository: ThemeRepository,
    private val notificationsRepository: NotificationsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeFavorites()
        observeUnits()
        observeTheme()
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            notificationsRepository.notificationsEnabled.collect { value ->
                _uiState.update { it.copy(notificationsEnabled = value) }
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            themeRepository.theme.collect { value ->
                _uiState.update { it.copy(currentTheme = value) }
            }
        }
    }

    fun onThemeChange(theme: AppTheme) {
        themeRepository.setTheme(theme)
    }

    private fun observeUnits() {
        viewModelScope.launch {
            unitsRepository.useKilometers.collect { value ->
                _uiState.update { it.copy(unitsInKilometers = value) }
            }
        }
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
        unitsRepository.setUseKilometers(useKilometers)
    }

    fun onToggleNotifications(enabled: Boolean) {
        notificationsRepository.setNotificationsEnabled(enabled)
    }

    fun onSignOut() {
        // TODO: hook up when auth exists
    }
}