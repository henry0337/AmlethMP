package dev.myrlennia237.amlethmp.core.auth

import dev.myrlennia237.amlethmp.core.auth.session.Session
import dev.myrlennia237.amlethmp.core.auth.session.SessionRepository
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultAuthRepositoryTest {

    private val tokens =
        TokenResponse(accessToken = "access", refreshToken = "refresh", expiresInSeconds = 900)
    private val api = FakeAuthApi(tokens)
    private val sessions = InMemorySessionRepository()
    private val clock = Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC)
    private val repository = DefaultAuthRepository(
        remoteDataSource = AuthRemoteDataSource(authApi = api, deviceName = "Pixel 9"),
        sessionRepository = sessions,
        clock = clock
    )

    @Test
    fun `google sends the id token and stores a session that expires with the token`() = runTest {
        val result = repository.signInWithGoogle("google-id-token")

        assertTrue(result.isSuccess)
        assertEquals(listOf("google-id-token"), api.idTokens)
        assertEquals(
            Session(
                accessToken = "access",
                refreshToken = "refresh",
                expiresAtMillis =
                    NOW + 900_000L
            ),
            sessions.session.first()
        )
    }

    @Test
    fun `google remote failure fails and stores nothing`() = runTest {
        api.failure = IOException("unreachable")

        val result = repository.signInWithGoogle("google-id-token")

        assertTrue(result.exceptionOrNull() is AuthException.Network)
        assertNull(sessions.session.first())
    }

    @Test
    fun `discord fails as not supported and stores nothing`() = runTest {
        val result = repository.signInWithDiscord()

        assertTrue(result.exceptionOrNull() is AuthException.NotSupported)
        assertNull(sessions.session.first())
    }

    @Test
    fun `guest succeeds without calling the remote or storing a session`() = runTest {
        val result = repository.signInAsGuest()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<String>(), api.idTokens)
        assertNull(sessions.session.first())
    }

    @Test
    fun `failure to store the session fails the sign in with a storage error`() = runTest {
        val cause = IOException("disk full")
        sessions.saveError = cause

        val failure = repository.signInWithGoogle("google-id-token").exceptionOrNull()

        assertTrue(failure is AuthException.Storage)
        assertSame(cause, failure?.cause)
        assertNull(sessions.session.first())
    }

    private class FakeAuthApi(private val tokens: TokenResponse) : AuthApi {
        val idTokens = mutableListOf<String>()
        var failure: IOException? = null

        override suspend fun signInWithGoogle(request: GoogleSignInRequest): TokenResponse {
            idTokens += request.idToken
            failure?.let { throw it }
            return tokens
        }
    }

    private class InMemorySessionRepository : SessionRepository {
        var saveError: IOException? = null
        private val stored = MutableStateFlow<Session?>(null)

        override val session: Flow<Session?> = stored

        override suspend fun save(session: Session) {
            saveError?.let { throw it }
            stored.value = session
        }

        override suspend fun clear() {
            stored.value = null
        }
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
