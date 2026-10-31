package dev.myrlennia237.amlethmp.screen.login.data

import dev.myrlennia237.amlethmp.core.session.Session
import dev.myrlennia237.amlethmp.core.session.SessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class FakeLoginRepositoryTest {

    private val sessions = InMemorySessionRepository()
    private val repository = FakeLoginRepository(sessions)

    @Test
    fun guest_signsInWithoutStoringASession() = runTest {
        val result = repository.signIn(SignInMethod.Guest)

        assertTrue(result.isSuccess)
        assertNull(sessions.restore())
    }

    @Test
    fun google_signsInAndStoresASession() = runTest {
        val result = repository.signIn(SignInMethod.Google)

        assertTrue(result.isSuccess)
        assertNotNull(sessions.restore())
    }

    @Test
    fun discord_signsInAndStoresASession() = runTest {
        val result = repository.signIn(SignInMethod.Discord)

        assertTrue(result.isSuccess)
        assertNotNull(sessions.restore())
    }

    @Test
    fun failureToStoreTheSession_failsTheSignIn() = runTest {
        val failure = IOException("disk full")
        sessions.saveError = failure

        val result = repository.signIn(SignInMethod.Google)

        assertEquals(failure, result.exceptionOrNull())
        assertNull(sessions.restore())
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
