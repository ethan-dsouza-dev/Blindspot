package com.blindspot.app.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.blindspot.app.data.remote.AuthApi
import com.blindspot.app.data.remote.AuthResponse
import com.blindspot.app.data.remote.GoogleSignInRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class SignInResult {
    data class Success(val authResponse: AuthResponse) : SignInResult()
    data class Error(val message: String, val isCancellation: Boolean = false) : SignInResult()
}

class AuthRepository(
    private val context: Context,
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
) {
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    suspend fun signInWithGoogle(activity: Activity, serverClientId: String): SignInResult = withContext(Dispatchers.IO) {
        try {
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetSignInWithGoogleOption.Builder(serverClientId)
                        .build(),
                )
                .build()

            val response = credentialManager.getCredential(activity, request)
            val idToken = extractIdToken(response)
                ?: return@withContext SignInResult.Error("Google sign-in did not return an ID token")

            val authResponse = authApi.signInWithGoogle(GoogleSignInRequest(idToken))
            tokenStore.saveTokens(authResponse.accessToken, authResponse.refreshToken)

            SignInResult.Success(authResponse)
        } catch (e: GetCredentialException) {
            val isCancellation = e.message?.contains("cancelled", ignoreCase = true) == true
            SignInResult.Error(e.message ?: "Google sign-in failed", isCancellation = isCancellation)
        } catch (e: Exception) {
            SignInResult.Error(e.message ?: "Sign-in failed")
        }
    }

    suspend fun refreshTokens(): AuthResponse? = withContext(Dispatchers.IO) {
        val refreshToken = tokenStore.refreshToken ?: return@withContext null
        return@withContext try {
            val response = authApi.refresh(com.blindspot.app.data.remote.RefreshRequest(refreshToken))
            tokenStore.saveTokens(response.accessToken, response.refreshToken)
            response
        } catch (e: Exception) {
            tokenStore.clear()
            null
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        tokenStore.clear()
    }

    private fun extractIdToken(response: GetCredentialResponse): String? {
        val credential = response.credential
        if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return null
        }
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        return googleIdTokenCredential.idToken
    }
}
