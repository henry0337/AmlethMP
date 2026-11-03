package dev.myrlennia237.amlethmp.core.auth

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

// Retrofit suspend calls are main-safe, so no dispatcher is needed here. Discord has no backend
// endpoint yet, so it fails explicitly instead of handing out tokens the server would not accept.
internal class RetrofitAuthRemoteDataSource(
    private val authApi: AuthApi,
    private val deviceName: String
) : AuthRemoteDataSource {

    override suspend fun signInWithGoogle(idToken: String): Result<TokenResponse> {
        val request = GoogleSignInRequest(idToken = idToken, client = CLIENT_PLATFORM, deviceName = deviceName)
        return request { authApi.signInWithGoogle(request) }
    }

    override suspend fun signInWithDiscord(): Result<TokenResponse> =
        Result.failure(AuthException.NotSupported("Discord"))

    private suspend fun request(call: suspend () -> TokenResponse): Result<TokenResponse> =
        try {
            Result.success(call())
        } catch (e: HttpException) {
            Result.failure(failureFor(e.code()))
        } catch (e: SerializationException) {
            Result.failure(AuthException.InvalidResponse(e))
        } catch (e: IOException) {
            Result.failure(AuthException.Network(e))
        }

    private fun failureFor(statusCode: Int): AuthException =
        if (statusCode in CLIENT_ERROR_CODES) AuthException.Rejected(statusCode) else AuthException.Server(statusCode)

    private companion object {
        const val CLIENT_PLATFORM = "android"
        val CLIENT_ERROR_CODES = 400..499
    }
}
