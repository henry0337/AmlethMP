package dev.myrlennia237.amlethmp.screen.login.ui

import dev.myrlennia237.amlethmp.screen.login.data.LoginRepository
import dev.myrlennia237.amlethmp.screen.login.data.SignInMethod
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeLoginRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isIdle() {
        val viewModel = LoginViewModel(repository)

        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun onSignIn_showsSigningInForThatMethodWhileWaiting() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)

        viewModel.onSignIn(SignInMethod.Discord)
        runCurrent()

        assertEquals(LoginUiState.SigningIn(SignInMethod.Discord), viewModel.uiState.value)
    }

    @Test
    fun onSignIn_success_movesToSignedIn() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)

        viewModel.onSignIn(SignInMethod.Google)
        runCurrent()
        repository.complete(Result.success(Unit))
        runCurrent()

        assertEquals(LoginUiState.SignedIn, viewModel.uiState.value)
    }

    @Test
    fun onSignIn_failure_movesToError() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)

        viewModel.onSignIn(SignInMethod.Google)
        runCurrent()
        repository.complete(Result.failure(IllegalStateException("network down")))
        runCurrent()

        assertEquals(LoginUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun onSignIn_afterError_canRetry() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)
        viewModel.onSignIn(SignInMethod.Google)
        runCurrent()
        repository.complete(Result.failure(IllegalStateException("network down")))
        runCurrent()

        viewModel.onSignIn(SignInMethod.Guest)
        runCurrent()

        assertEquals(LoginUiState.SigningIn(SignInMethod.Guest), viewModel.uiState.value)
        assertEquals(listOf(SignInMethod.Google, SignInMethod.Guest), repository.requestedMethods)
    }

    @Test
    fun onSignIn_whileSigningIn_isIgnored() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)

        viewModel.onSignIn(SignInMethod.Google)
        runCurrent()
        viewModel.onSignIn(SignInMethod.Discord)
        runCurrent()

        assertEquals(LoginUiState.SigningIn(SignInMethod.Google), viewModel.uiState.value)
        assertEquals(listOf(SignInMethod.Google), repository.requestedMethods)
    }

    private class FakeLoginRepository : LoginRepository {
        val requestedMethods = mutableListOf<SignInMethod>()
        private var pending = CompletableDeferred<Result<Unit>>()

        override suspend fun signIn(method: SignInMethod): Result<Unit> {
            requestedMethods += method
            pending = CompletableDeferred()
            return pending.await()
        }

        fun complete(result: Result<Unit>) {
            pending.complete(result)
        }
    }
}
