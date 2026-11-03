package dev.myrlennia237.amlethmp.screen.login.data.remote

import dev.myrlennia237.amlethmp.core.session.Session
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class FakeAuthApi @Inject constructor() : AuthApi {

    override suspend fun signInWithGoogle(idToken: String): Result<Session> {
        delay(FAKE_LATENCY_MS.milliseconds)
        if (idToken.isBlank()) return Result.failure(IllegalArgumentException("Empty ID token"))
        return Result.success(fakeSession())
    }

    override suspend fun signInWithDiscord(): Result<Session> {
        delay(FAKE_LATENCY_MS.milliseconds)
        return Result.success(fakeSession())
    }

    private fun fakeSession() = Session(
        accessToken = UUID.randomUUID().toString(),
        refreshToken = UUID.randomUUID().toString(),
        expiresAtMillis = System.currentTimeMillis() + FAKE_TOKEN_LIFETIME_MS
    )

    private companion object {
        const val FAKE_LATENCY_MS = 1_500L
        const val FAKE_TOKEN_LIFETIME_MS = 60 * 60 * 1_000L
    }
}
