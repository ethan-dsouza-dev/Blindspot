package com.blindspot.app.auth

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Adds the stored access token to every outgoing request as `Authorization: Bearer <token>`.
 */
class AuthInterceptor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.accessToken ?: return chain.proceed(chain.request())
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}

/**
 * Handles 401 responses by attempting to refresh the access token once, then retrying the
 * original request with the new token. If refresh fails, tokens are cleared and the caller
 * will receive the final 401 response.
 */
class TokenRefreshAuthenticator(
    private val tokenStore: TokenStore,
    private val authRepository: AuthRepository,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val newTokens = runBlocking { authRepository.refreshTokens() } ?: return null

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
