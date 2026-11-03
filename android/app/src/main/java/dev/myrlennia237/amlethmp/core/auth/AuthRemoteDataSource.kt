package dev.myrlennia237.amlethmp.core.auth

internal interface AuthRemoteDataSource {
    suspend fun signInWithGoogle(idToken: String): Result<TokenResponse>

    suspend fun signInWithDiscord(): Result<TokenResponse>
}
