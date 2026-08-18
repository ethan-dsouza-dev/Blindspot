package com.blindspot.app.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists and shares the user's distance-unit preference (kilometers vs. miles) so every
 * screen that formats a distance — Discover, Feed, Maps, Profile — reads the same value and
 * reacts immediately when it changes. Backed by SharedPreferences: simple, synchronous
 * persistence for a single boolean, no DataStore dependency needed.
 */
class UnitsRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _useKilometers = MutableStateFlow(prefs.getBoolean(KEY_USE_KM, true))
    val useKilometers: StateFlow<Boolean> = _useKilometers.asStateFlow()

    fun setUseKilometers(value: Boolean) {
        _useKilometers.value = value
        prefs.edit().putBoolean(KEY_USE_KM, value).apply()
    }

    private companion object {
        const val PREFS_NAME = "units_prefs"
        const val KEY_USE_KM = "use_kilometers"
    }
}