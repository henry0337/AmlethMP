package dev.myrlennia237.amlethmp.core.session

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Exercises the real Android Keystore + Tink path that JVM unit tests cannot reach. */
@RunWith(AndroidJUnit4::class)
class SessionKeystoreTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val file = File(context.cacheDir, "session-keystore-test.enc")
    private val session = Session(
        accessToken = "access-token-123",
        refreshToken = "refresh-token-456",
        expiresAtMillis = 1_700_000_000_000L
    )

    @After
    fun tearDown() {
        file.delete()
    }

    private fun newRepository(job: Job) = DataStoreSessionRepository(
        createSessionDataStore(
            aead = { SessionAead.create(context) },
            produceFile = { file },
            scope = CoroutineScope(Dispatchers.IO + job)
        )
    )

    @Test
    fun saveAndRestore_roundTripsThroughTheKeystore() = runBlocking {
        val job = Job()
        val repository = newRepository(job)

        repository.save(session)

        assertEquals(session, repository.restore())
        job.cancelAndJoin()
    }

    @Test
    fun storedFile_doesNotContainThePlainTokens() = runBlocking {
        val job = Job()
        newRepository(job).save(session)
        job.cancelAndJoin()

        val text = String(file.readBytes(), Charsets.ISO_8859_1)

        assertTrue(file.length() > 0)
        assertFalse(text.contains(session.accessToken))
        assertFalse(text.contains(session.refreshToken))
    }

    @Test
    fun session_survivesANewRepositoryOverTheSameFile() = runBlocking {
        val firstJob = Job()
        newRepository(firstJob).save(session)
        firstJob.cancelAndJoin()

        val secondJob = Job()
        val restored = newRepository(secondJob).restore()
        secondJob.cancelAndJoin()

        assertEquals(session, restored)
    }

    @Test
    fun clear_removesTheSession() = runBlocking {
        val job = Job()
        val repository = newRepository(job)
        repository.save(session)

        repository.clear()

        assertNull(repository.restore())
        job.cancelAndJoin()
    }
}
