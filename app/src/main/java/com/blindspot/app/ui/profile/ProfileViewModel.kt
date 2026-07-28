package com.blindspot.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    // TODO: inject a real UserRepository once auth/backend exists,
    // same pattern as PlaceRepository in AppModule.kt
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // TODO: replace with real repository call
            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = "Bron",
                    email = "bron@example.com",
                    avatarUrl = null,
                    savedPlaces = emptyList(),
                )
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