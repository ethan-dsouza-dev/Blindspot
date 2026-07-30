package com.blindspot.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.blindspot.app.auth.TokenStore

class ProfileViewModel(
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val user = tokenStore.currentUser
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = user?.name?.takeIf { it.isNotBlank() } ?: user?.email ?: "User",
                        email = user?.email ?: "",
                        avatarUrl = user?.pictureUrl,
                        savedPlaces = emptyList(),
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