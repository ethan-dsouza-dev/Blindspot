package com.blindspot.app.auth

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed class SignInUiState {
    data object Idle : SignInUiState()
    data object Loading : SignInUiState()
    data class Error(val message: String) : SignInUiState()
    data object SignedIn : SignInUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val serverClientId: String,
) : ViewModel() {

    var uiState by mutableStateOf<SignInUiState>(SignInUiState.Idle)
        private set

    fun signInWithGoogle(activity: Activity) {
        if (serverClientId.isBlank()) {
            uiState = SignInUiState.Error("Google Sign-In is not configured (missing server client ID)")
            return
        }

        uiState = SignInUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(activity, serverClientId)
            uiState = when (result) {
                is SignInResult.Success -> SignInUiState.SignedIn
                is SignInResult.Error -> if (result.isCancellation) SignInUiState.Idle else SignInUiState.Error(result.message)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            uiState = SignInUiState.Idle
        }
    }

    fun consumeError() {
        if (uiState is SignInUiState.Error) {
            uiState = SignInUiState.Idle
        }
    }
}
