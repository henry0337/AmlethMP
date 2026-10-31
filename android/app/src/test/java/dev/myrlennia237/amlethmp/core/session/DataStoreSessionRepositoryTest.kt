package dev.myrlennia237.amlethmp.core.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

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
            produceFile = { File(tempFolder.root, SESSION_FILE_NAME) },
            scope = CoroutineScope(Dispatchers.IO + job)
        )
        return DataStoreSessionRepository(dataStore) to job
    }

    @Test
    fun restore_withoutSavedSession_returnsNull() = runTest {
        val (repository, _) = newRepository()

        assertNull(repository.restore())
    }

    @Test
    fun save_thenRestore_returnsTheSession() = runTest {
        val (repository, _) = newRepository()

        repository.save(testSession)

        assertEquals(testSession, repository.restore())
    }

    @Test
    fun savedSession_survivesARestart() = runTest {
        val (first, firstJob) = newRepository()
        first.save(testSession)
        firstJob.cancelAndJoin()

        val (second, _) = newRepository()

        assertEquals(testSession, second.restore())
    }

    @Test
    fun clear_removesTheSession() = runTest {
        val (repository, _) = newRepository()
        repository.save(testSession)

        repository.clear()

        assertNull(repository.restore())
    }

    @Test
    fun corruptedFile_isTreatedAsNoSessionAndCanBeOverwritten() = runTest {
        File(tempFolder.root, SESSION_FILE_NAME).writeBytes(byteArrayOf(1, 2, 3, 4, 5))
        val (repository, _) = newRepository()

        assertNull(repository.restore())

        repository.save(testSession)
        assertEquals(testSession, repository.restore())
    }

    @Test
    fun unreadableKey_isTreatedAsNoSession() = runTest {
        val (first, firstJob) = newRepository()
        first.save(testSession)
        firstJob.cancelAndJoin()

        val job = Job().also { jobs += it }
        val otherKeyStore = createSessionDataStore(
            aead = { newTestAead() },
            produceFile = { File(tempFolder.root, SESSION_FILE_NAME) },
            scope = CoroutineScope(Dispatchers.IO + job)
        )

        assertNull(DataStoreSessionRepository(otherKeyStore).restore())
    }
}
