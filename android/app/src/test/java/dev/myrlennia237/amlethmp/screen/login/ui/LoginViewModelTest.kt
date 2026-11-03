package dev.myrlennia237.amlethmp.screen.login.ui

import dev.myrlennia237.amlethmp.core.auth.AuthException
import dev.myrlennia237.amlethmp.core.auth.AuthRepository
import dev.myrlennia237.amlethmp.core.network.NetworkMonitor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAuthRepository()
    private val network = FakeNetworkMonitor()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newViewModel() = LoginViewModel(repository, network)

    @Test
    fun `initial state is idle without a message`() {
        val viewModel = newViewModel()

        assertEquals(LoginUiState(), viewModel.uiState.value)
    }

    @Test
    fun `google waits for the id token before calling the repository`() = runTest(dispatcher) {
        val viewModel = newViewModel()

        viewModel.signIn(SignInMethod.Google)
        runCurrent()

        assertEquals(
            LoginStatus.SigningIn(SignInMethod.Google, awaitingGoogleIdToken = true),
            viewModel.uiState.value.status
        )
        assertEquals(emptyList<String>(), repository.calls)
    }

    @Test
    fun `buttons are locked while the connectivity check runs`() = runTest(dispatcher) {
        val viewModel = newViewModel()

        viewModel.signIn(SignInMethod.Google)

        assertEquals(LoginStatus.SigningIn(SignInMethod.Google), viewModel.uiState.value.status)
    }

    @Test
    fun `google id token is sent to the repository and signs in`() = runTest(dispatcher) {
        val viewModel = newViewModel()
        viewModel.signIn(SignInMethod.Google)
        runCurrent()

        viewModel.googleIdTokenReceived("google-id-token")
        runCurrent()

        assertEquals(LoginStatus.SigningIn(SignInMethod.Google), viewModel.uiState.value.status)
        repository.complete(Result.success(Unit))
        runCurrent()

        assertEquals(LoginStatus.SignedIn, viewModel.uiState.value.status)
        assertEquals(listOf("google:google-id-token"), repository.calls)
    }

    @Test
    fun `google id token nobody asked for is ignored`() = runTest(dispatcher) {
        val viewModel = newViewModel()

        viewModel.googleIdTokenReceived("google-id-token")
        runCurrent()

        assertEquals(LoginUiState(), viewModel.uiState.value)
        assertEquals(emptyList<String>(), repository.calls)
    }

    @Test
    fun `google repository failure goes back to idle with a failure message`() = runTest(dispatcher) {
        val viewModel = newViewModel()
        viewModel.signIn(SignInMethod.Google)
        runCurrent()
        viewModel.googleIdTokenReceived("google-id-token")
        runCurrent()

        repository.complete(Result.failure(AuthException.Rejected(401)))
        runCurrent()

        assertEquals(
            LoginUiState(status = LoginStatus.Idle, userMessage = LoginUserMessage.SignInFailed),
            viewModel.uiState.value
        )
    }

    @Test
    fun `a network failure after the check shows the network message`() = runTest(dispatcher) {
        val viewModel = newViewModel()
        viewModel.signIn(SignInMethod.Discord)
        runCurrent()

        repository.complete(Result.failure(AuthException.Network(IOException("offline"))))
        runCurrent()

        assertEquals(LoginUserMessage.NetworkUnavailable, viewModel.uiState.value.userMessage)
        assertEquals(LoginStatus.Idle, viewModel.uiState.value.status)
    }

    @Test
    fun `offline google shows the network message without asking for an id token`() = runTest(dispatcher) {
        network.online.value = false
        val viewModel = newViewModel()

        viewModel.signIn(SignInMethod.Google)
        runCurrent()

        assertEquals(
            LoginUiState(status = LoginStatus.Idle, userMessage = LoginUserMessage.NetworkUnavailable),
            viewModel.uiState.value
        )
        assertEquals(false, viewModel.uiState.value.isAwaitingGoogleIdToken)
        assertEquals(emptyList<String>(), repository.calls)
    }

    @Test
    fun `offline discord shows the network message without calling the repository`() = runTest(dispatcher) {
        network.online.value = false
        val viewModel = newViewModel()

        viewModel.signIn(SignInMethod.Discord)
        runCurrent()

        assertEquals(
            LoginUiState(status = LoginStatus.Idle, userMessage = LoginUserMessage.NetworkUnavailable),
            viewModel.uiState.value
        )
        assertEquals(emptyList<String>(), repository.calls)
    }

    @Test
    fun `guest signs in even when offline`() = runTest(dispatcher) {
        network.online.value = false
        val viewModel = newViewModel()

        viewModel.signIn(SignInMethod.Guest)
        runCurrent()
        repository.complete(Result.success(Unit))
        runCurrent()

        assertEquals(LoginStatus.SignedIn, viewModel.uiState.value.status)
        assertEquals(listOf("guest"), repository.calls)
    }

    @Test
    fun `signing in works again once the network is back`() = runTest(dispatcher) {
        network.online.value = false
        val viewModel = newViewModel()
        viewModel.signIn(SignInMethod.Google)
        runCurrent()
        network.online.value = true

        viewModel.signIn(SignInMethod.Google)
        runCurrent()

        assertEquals(true, viewModel.uiState.value.isAwaitingGoogleIdToken)
    }

    @Test
    fun `google cancelled by the user goes back to idle without a message`() = runTest(dispatcher) {
        val viewModel = newViewModel()
        viewModel.signIn(SignInMethod.Google)
        runCurrent()

        viewModel.googleSignInCancelled()
        runCurrent()

        assertEquals(LoginUiState(), viewModel.uiState.value)
        assertEquals(emptyList<String>(), repository.calls)
    }

    @Test
    fun `google id token failure goes back to idle with a failure message`() = runTest(dispatcher) {
        val viewModel = newViewModel()
        viewModel.signIn(SignInMethod.Google)
        runCurrent()

        viewModel.googleSignInFailed(IllegalStateException("no credentials"))
        runCurrent()

        assertEquals(
            LoginUiState(status = LoginStatus.Idle, userMessage = LoginUserMessage.SignInFailed),
            viewModel.uiState.value
        )
        assertEquals(emptyList<String>(), repository.calls)
    }

    @Test
    fun `guest signs in without asking for a google id token`() = runTest(dispatcher) {
        val viewModel = newViewModel()

        viewModel.signIn(SignInMethod.Guest)
        runCurrent()

        assertEquals(LoginStatus.SigningIn(SignInMethod.Guest), viewModel.uiState.value.status)
        repository.complete(Result.success(Unit))
        runCurrent()

        assertEquals(LoginStatus.SignedIn, viewModel.uiState.value.status)
        assertEquals(listOf("guest"), repository.calls)
    }

    @Test
    fun `user message shown clears the message`() = runTest(dispatcher) {
        val viewModel = newViewModel()
        viewModel.signIn(SignInMethod.Discord)
        runCurrent()
        repository.complete(Result.failure(AuthException.NotSupported("Discord")))
        runCurrent()

        viewModel.userMessageShown()

        assertEquals(LoginUiState(), viewModel.uiState.value)
    }

    @Test
    fun `sign in after a failure can retry`() = runTest(dispatcher) {
        val viewModel = newViewModel()
        viewModel.signIn(SignInMethod.Discord)
        runCurrent()
        repository.complete(Result.failure(AuthException.Server(503)))
        runCurrent()

        viewModel.signIn(SignInMethod.Guest)
        runCurrent()

        assertEquals(LoginStatus.SigningIn(SignInMethod.Guest), viewModel.uiState.value.status)
        assertEquals(listOf("discord", "guest"), repository.calls)
    }

    @Test
    fun `sign in while signing in is ignored`() = runTest(dispatcher) {
        val viewModel = newViewModel()

        viewModel.signIn(SignInMethod.Discord)
        viewModel.signIn(SignInMethod.Guest)
        runCurrent()

        assertEquals(LoginStatus.SigningIn(SignInMethod.Discord), viewModel.uiState.value.status)
        assertEquals(listOf("discord"), repository.calls)
    }

    private class FakeNetworkMonitor : NetworkMonitor {
        val online = MutableStateFlow(true)

        override val isOnline: Flow<Boolean> = online
    }

    private class FakeAuthRepository : AuthRepository {
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
