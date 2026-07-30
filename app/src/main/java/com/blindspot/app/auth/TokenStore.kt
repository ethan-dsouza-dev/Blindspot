package com.blindspot.app.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit
import com.blindspot.app.data.remote.UserDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Secure token storage backed by [EncryptedSharedPreferences] + Android Keystore.
 * Stores the access token, refresh token and a flag indicating whether the user has signed in.
 */
class TokenStore(context: Context) {

    private val gson = Gson()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_tokens",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val _isAuthenticatedFlow = MutableStateFlow(isAuthenticated)
    val isAuthenticatedFlow: StateFlow<Boolean> = _isAuthenticatedFlow.asStateFlow()

    var isAuthenticated: Boolean
        get() = prefs.getBoolean(KEY_IS_AUTHENTICATED, false)
        set(value) {
            prefs.edit { putBoolean(KEY_IS_AUTHENTICATED, value) }
            _isAuthenticatedFlow.value = value
        }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit { putString(KEY_ACCESS_TOKEN, value) }

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit { putString(KEY_REFRESH_TOKEN, value) }

    var currentUser: UserDto?
        get() = prefs.getString(KEY_USER, null)?.let { gson.fromJson(it, UserDto::class.java) }
        set(value) = prefs.edit { putString(KEY_USER, value?.let { gson.toJson(it) }) }

    fun saveTokens(accessToken: String, refreshToken: String, user: UserDto? = null) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.currentUser = user
        this.isAuthenticated = true
    }

    fun clear() {
        prefs.edit { clear() }
        _isAuthenticatedFlow.value = isAuthenticated
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_IS_AUTHENTICATED = "is_authenticated"
        private const val KEY_USER = "current_user"
    }
}
