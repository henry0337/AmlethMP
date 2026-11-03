package dev.myrlennia237.amlethmp.screen.login.data

import dev.myrlennia237.amlethmp.core.session.Session
import dev.myrlennia237.amlethmp.core.session.SessionRepository
import dev.myrlennia237.amlethmp.screen.login.data.remote.AuthApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class DefaultLoginRepositoryTest {

    private val session = Session(accessToken = "access", refreshToken = "refresh", expiresAtMillis = 1_000L)
    private val authApi = StubAuthApi(Result.success(session))
    private val sessions = InMemorySessionRepository()
    private val repository = DefaultLoginRepository(authApi, sessions)

    @Test
    fun `google sends the id token and stores the session`() = runTest {
        val result = repository.signInWithGoogle("google-id-token")

        assertTrue(result.isSuccess)
        assertEquals(listOf("google:google-id-token"), authApi.calls)
        assertEquals(session, sessions.restore())
    }

    @Test
    fun `google api failure fails and stores nothing`() = runTest {
        val failure = IOException("offline")
        authApi.result = Result.failure(failure)

        val result = repository.signInWithGoogle("google-id-token")

        assertEquals(failure, result.exceptionOrNull())
        assertNull(sessions.restore())
    }

    @Test
    fun `discord stores the session`() = runTest {
        val result = repository.signInWithDiscord()

        assertTrue(result.isSuccess)
        assertEquals(listOf("discord"), authApi.calls)
        assertEquals(session, sessions.restore())
    }

    @Test
    fun `guest succeeds without calling the api or storing a session`() = runTest {
        val result = repository.signInAsGuest()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<String>(), authApi.calls)
        assertNull(sessions.restore())
    }

    @Test
    fun `failure to store the session fails the sign in`() = runTest {
        val failure = IOException("disk full")
        sessions.saveError = failure

        val result = repository.signInWithGoogle("google-id-token")

        assertEquals(failure, result.exceptionOrNull())
        assertNull(sessions.restore())
    }

    private class StubAuthApi(var result: Result<Session>) : AuthApi {
        val calls = mutableListOf<String>()

        override suspend fun signInWithGoogle(idToken: String): Result<Session> {
            calls += "google:$idToken"
            return result
        }

        override suspend fun signInWithDiscord(): Result<Session> {
            calls += "discord"
            return result
        }
    }

    private class InMemorySessionRepository : SessionRepository {
        var saveError: IOException? = null
        private var stored: Session? = null

        override suspend fun save(session: Session) {
            saveError?.let { throw it }
            stored = session
        }

        override suspend fun restore(): Session? = stored

        override suspend fun clear() {
            stored = null
        }
    }
}
