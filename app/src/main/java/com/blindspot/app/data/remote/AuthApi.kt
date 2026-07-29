package com.blindspot.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.UUID

data class GoogleSignInRequest(
    @SerializedName("idToken") val idToken: String,
)

data class RefreshRequest(
    @SerializedName("refreshToken") val refreshToken: String,
)

data class UserDto(
    @SerializedName("id") val id: UUID,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String?,
    @SerializedName("pictureUrl") val pictureUrl: String?,
)

data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Long,
    @SerializedName("user") val user: UserDto,
)

interface AuthApi {

    @POST("api/auth/google")
    suspend fun signInWithGoogle(@Body request: GoogleSignInRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse
}
