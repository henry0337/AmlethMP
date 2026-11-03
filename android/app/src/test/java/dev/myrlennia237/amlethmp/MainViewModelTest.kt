package dev.myrlennia237.amlethmp

import dev.myrlennia237.amlethmp.core.session.Session
import dev.myrlennia237.amlethmp.core.session.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val sessions = FakeSessionRepository()
    private val session = Session(accessToken = "access", refreshToken = "refresh", expiresAtMillis = 1_000L)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `start destination is unknown until the session has been read`() = runTest {
        val viewModel = MainViewModel(sessions)
        collectStartDestination(viewModel)

        assertNull(viewModel.startDestination.value)
    }

    @Test
    fun `no stored session starts at login`() = runTest {
        val viewModel = MainViewModel(sessions)
        collectStartDestination(viewModel)

        sessions.stored.emit(null)

        assertEquals(StartDestination.Login, viewModel.startDestination.value)
    }

    @Test
    fun `a stored session starts at home`() = runTest {
        val viewModel = MainViewModel(sessions)
        collectStartDestination(viewModel)

        sessions.stored.emit(session)

        assertEquals(StartDestination.Home, viewModel.startDestination.value)
    }

    @Test
    fun `a session stored after launch does not move the start destination`() = runTest {
        val viewModel = MainViewModel(sessions)
        collectStartDestination(viewModel)
        sessions.stored.emit(null)

        sessions.stored.emit(session)

        assertEquals(StartDestination.Login, viewModel.startDestination.value)
    }

    // The start destination only runs while observed; it must be subscribed before the session is
    // emitted because a shared flow without replay drops values nobody is collecting.
    private fun TestScope.collectStartDestination(viewModel: MainViewModel) {
        backgroundScope.launch { viewModel.startDestination.collect {} }
        runCurrent()
    }

    private class FakeSessionRepository : SessionRepository {
        val stored = MutableSharedFlow<Session?>()

        override val session: Flow<Session?> = stored

        override suspend fun save(session: Session) = stored.emit(session)

        override suspend fun clear() = stored.emit(null)
    }
}
