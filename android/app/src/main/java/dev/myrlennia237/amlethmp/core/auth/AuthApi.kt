package dev.myrlennia237.amlethmp.core.auth

import retrofit2.http.Body
import retrofit2.http.POST

// Declares the backend endpoints only; AuthRemoteDataSource turns its failures into AuthException.
internal interface AuthApi {

    @POST("auth/google")
    suspend fun signInWithGoogle(@Body request: GoogleSignInRequest): TokenResponse
}
