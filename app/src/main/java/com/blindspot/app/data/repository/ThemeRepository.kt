package com.blindspot.app.data.repository

import android.content.Context
import com.blindspot.app.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Persists and shares the user's selected theme (Aurora/Dusk). */
class ThemeRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(
        AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.AURORA.name) ?: AppTheme.AURORA.name),
    )
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    fun setTheme(value: AppTheme) {
        _theme.value = value
        prefs.edit().putString(KEY_THEME, value.name).apply()
    }

    private companion object {
        const val PREFS_NAME = "theme_prefs"
        const val KEY_THEME = "selected_theme"
    }
}