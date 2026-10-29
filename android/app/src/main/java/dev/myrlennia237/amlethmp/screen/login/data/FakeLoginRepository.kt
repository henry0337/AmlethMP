package dev.myrlennia237.amlethmp.screen.login.data

import dev.myrlennia237.amlethmp.core.session.Session
import dev.myrlennia237.amlethmp.core.session.SessionRepository
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * Placeholder until the real authentication is wired to the backend: it always signs in and stores
 * made-up tokens. Guest sessions are never stored.
 */
class FakeLoginRepository @Inject constructor(
    private val sessionRepository: SessionRepository
) : LoginRepository {

    override suspend fun signIn(method: SignInMethod): Result<Unit> {
        delay(FAKE_LATENCY_MS.milliseconds)
        if (method == SignInMethod.Guest) return Result.success(Unit)

        return try {
            sessionRepository.save(fakeSession())
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
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
