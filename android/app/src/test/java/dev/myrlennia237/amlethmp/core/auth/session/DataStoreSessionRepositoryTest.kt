package dev.myrlennia237.amlethmp.core.auth.session

import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

// Overwriting an existing session file (clear, replacing a corrupted file) is tested on a device in
// SessionKeystoreTest: on Windows, JDK 21 cannot rename a file over an existing one, which DataStore
// does on every write after the first.
class DataStoreSessionRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val aead = newTestAead()
    private val jobs = mutableListOf<Job>()

    @After
    fun tearDown() {
        jobs.forEach { it.cancel() }
    }

    /** Each call is like a fresh app process reading the same file. */
    private fun newRepository(): Pair<DataStoreSessionRepository, Job> {
        val job = Job().also { jobs += it }
        val dataStore = createSessionDataStore(
            aead = { aead },
            ioDispatcher = Dispatchers.IO,
            produceFile = { File(tempFolder.root, SESSION_FILE_NAME) },
            scope = CoroutineScope(Dispatchers.IO + job)
        )
        return DataStoreSessionRepository(dataStore) to job
    }

    @Test
    fun `a missing session emits null`() = runTest {
        val (repository, _) = newRepository()

        assertNull(repository.session.first())
    }

    @Test
    fun `save then read returns the session`() = runTest {
        val (repository, _) = newRepository()

        repository.save(testSession)

        assertEquals(testSession, repository.session.first())
    }

    @Test
    fun `a saved session survives a restart`() = runTest {
        val (first, firstJob) = newRepository()
        first.save(testSession)
        firstJob.cancelAndJoin()

        val (second, _) = newRepository()

        assertEquals(testSession, second.session.first())
    }

    @Test
    fun `an unreadable key is treated as no session`() = runTest {
        val (first, firstJob) = newRepository()
        first.save(testSession)
        firstJob.cancelAndJoin()

        val job = Job().also { jobs += it }
        val otherKeyStore = createSessionDataStore(
            aead = { newTestAead() },
            ioDispatcher = Dispatchers.IO,
            produceFile = { File(tempFolder.root, SESSION_FILE_NAME) },
            scope = CoroutineScope(Dispatchers.IO + job)
        )

        assertNull(DataStoreSessionRepository(otherKeyStore).session.first())
    }
}
