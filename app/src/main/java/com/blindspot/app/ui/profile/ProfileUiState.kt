package com.blindspot.app.ui.profile

import com.blindspot.app.data.model.Place
import com.blindspot.app.ui.theme.AppTheme

data class ProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val savedPlaces: List<Place> = emptyList(),
    val unitsInKilometers: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val currentTheme: AppTheme = AppTheme.AURORA,
    val error: String? = null,
)