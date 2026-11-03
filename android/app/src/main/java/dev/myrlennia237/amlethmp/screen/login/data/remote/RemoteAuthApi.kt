package dev.myrlennia237.amlethmp.screen.login.data.remote

import dev.myrlennia237.amlethmp.core.session.Session
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.coroutines.executeAsync
import java.io.IOException

// Talks to the AmlethMP backend. Discord has no backend endpoint yet, so it fails explicitly
// instead of handing out a session that would not work against the server.
class RemoteAuthApi(
    private val client: OkHttpClient,
    private val json: Json,
    private val baseUrl: HttpUrl,
    private val deviceName: String,
    private val ioDispatcher: CoroutineDispatcher,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) : AuthApi {

    override suspend fun signInWithGoogle(idToken: String): Result<Session> {
        val payload = GoogleSignInRequest(idToken = idToken, client = CLIENT_PLATFORM, deviceName = deviceName)
        val request = Request.Builder()
            .url(baseUrl.newBuilder().addPathSegments(GOOGLE_SIGN_IN_PATH).build())
            .post(json.encodeToString(payload).toRequestBody(JSON_MEDIA_TYPE))
            .build()
        return execute(request)
    }

    override suspend fun signInWithDiscord(): Result<Session> =
        Result.failure(UnsupportedOperationException("Discord sign-in is not available yet"))

    private suspend fun execute(request: Request): Result<Session> {
        val call: Call = client.newCall(request)
        return try {
            call.executeAsync().use { response -> withContext(ioDispatcher) { readSession(response) } }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    private fun readSession(response: Response): Result<Session> {
        if (!response.isSuccessful) return Result.failure(AuthApiException(response.code))

        return try {
            val body = json.decodeFromString<TokenResponse>(response.body.string())
            Result.success(
                Session(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    expiresAtMillis = currentTimeMillis() + body.expiresInSeconds * MILLIS_PER_SECOND
                )
            )
        } catch (e: SerializationException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    private companion object {
        const val GOOGLE_SIGN_IN_PATH = "auth/google"
        const val CLIENT_PLATFORM = "android"
        const val MILLIS_PER_SECOND = 1_000L
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

// Only the status code is kept: the response body must never end up in logs or toasts.
class AuthApiException(val statusCode: Int) : Exception("Auth request failed with HTTP $statusCode")
