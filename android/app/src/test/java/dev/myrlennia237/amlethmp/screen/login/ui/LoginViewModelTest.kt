package dev.myrlennia237.amlethmp.screen.login.ui

import dev.myrlennia237.amlethmp.screen.login.data.LoginRepository
import dev.myrlennia237.amlethmp.screen.login.data.model.SignInMethod
import dev.myrlennia237.amlethmp.screen.login.data.google.GoogleIdTokenSource
import dev.myrlennia237.amlethmp.screen.login.data.google.GoogleSignInCancelledException
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
    private val googleToken = GoogleIdTokenSource { Result.success("google-id-token") }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle`() {
        val viewModel = LoginViewModel(repository)

        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `onSignIn shows signing in for that method while waiting`() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)

        viewModel.onSignIn(SignInMethod.Discord, googleToken)
        runCurrent()

        assertEquals(LoginUiState.SigningIn(SignInMethod.Discord), viewModel.uiState.value)
    }

    @Test
    fun `google success sends the google token and moves to signed in`() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)

        viewModel.onSignIn(SignInMethod.Google, googleToken)
        runCurrent()
        repository.complete(Result.success(Unit))
        runCurrent()

        assertEquals(LoginUiState.SignedIn, viewModel.uiState.value)
        assertEquals(listOf("google:google-id-token"), repository.calls)
    }

    @Test
    fun `google repository failure moves to error`() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)

        viewModel.onSignIn(SignInMethod.Google, googleToken)
        runCurrent()
        repository.complete(Result.failure(IllegalStateException("network down")))
        runCurrent()

        assertEquals(LoginUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun `google cancelled by the user goes back to idle without calling the repository`() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)

        viewModel.onSignIn(SignInMethod.Google) { Result.failure(GoogleSignInCancelledException()) }
        runCurrent()

        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
        assertEquals(emptyList<String>(), repository.calls)
    }

    @Test
    fun `google token failure moves to error without calling the repository`() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)

        viewModel.onSignIn(SignInMethod.Google) { Result.failure(IllegalStateException("no credentials")) }
        runCurrent()

        assertEquals(LoginUiState.Error, viewModel.uiState.value)
        assertEquals(emptyList<String>(), repository.calls)
    }

    @Test
    fun `guest does not ask google for a token`() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)

        viewModel.onSignIn(SignInMethod.Guest) { error("Google must not be asked") }
        runCurrent()
        repository.complete(Result.success(Unit))
        runCurrent()

        assertEquals(LoginUiState.SignedIn, viewModel.uiState.value)
        assertEquals(listOf("guest"), repository.calls)
    }

    @Test
    fun `onSignIn after an error can retry`() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)
        viewModel.onSignIn(SignInMethod.Discord, googleToken)
        runCurrent()
        repository.complete(Result.failure(IllegalStateException("network down")))
        runCurrent()

        viewModel.onSignIn(SignInMethod.Guest, googleToken)
        runCurrent()

        assertEquals(LoginUiState.SigningIn(SignInMethod.Guest), viewModel.uiState.value)
        assertEquals(listOf("discord", "guest"), repository.calls)
    }

    @Test
    fun `onSignIn while signing in is ignored`() = runTest(dispatcher) {
        val viewModel = LoginViewModel(repository)

        viewModel.onSignIn(SignInMethod.Google, googleToken)
        runCurrent()
        viewModel.onSignIn(SignInMethod.Discord, googleToken)
        runCurrent()

        assertEquals(LoginUiState.SigningIn(SignInMethod.Google), viewModel.uiState.value)
        assertEquals(listOf("google:google-id-token"), repository.calls)
    }

    private class FakeLoginRepository : LoginRepository {
        val calls = mutableListOf<String>()
        private var pending = CompletableDeferred<Result<Unit>>()

        override suspend fun signInWithGoogle(idToken: String) = await("google:$idToken")

        override suspend fun signInWithDiscord() = await("discord")

        override suspend fun signInAsGuest() = await("guest")

        private suspend fun await(call: String): Result<Unit> {
            calls += call
            pending = CompletableDeferred()
            return pending.await()
        }

        fun complete(result: Result<Unit>) {
            pending.complete(result)
        }
    }
}
