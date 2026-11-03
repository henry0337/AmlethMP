package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.myrlennia237.amlethmp.R
import dev.myrlennia237.amlethmp.core.auth.AuthException
import dev.myrlennia237.amlethmp.core.auth.AuthRepository
import dev.myrlennia237.amlethmp.core.network.NetworkMonitor
import dev.myrlennia237.amlethmp.core.ui.component.AMPToastHost
import dev.myrlennia237.amlethmp.core.ui.component.AMPToastState
import dev.myrlennia237.amlethmp.core.ui.component.LocalToastState
import dev.myrlennia237.amlethmp.core.ui.theme.AMPTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class LoginRouteTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository = FakeAuthRepository()
    private val network = FakeNetworkMonitor()
    private val viewModel = LoginViewModel(repository, network)

    private fun text(@StringRes id: Int) = context.getString(id)

    private fun setContent() {
        val toastState = AMPToastState()
        composeRule.setContent {
            AMPTheme {
                CompositionLocalProvider(LocalToastState provides toastState) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LoginRoute(onSignedIn = {}, viewModel = viewModel)
                        AMPToastHost(state = toastState)
                    }
                }
            }
        }
    }

    private fun waitForText(@StringRes id: Int) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(text(id)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun failedSignIn_showsAToastAndKeepsButtonsEnabledForRetry() {
        repository.discordResult = Result.failure(AuthException.NotSupported("Discord"))
        setContent()

        composeRule.onNodeWithText(text(R.string.login_continue_discord)).performClick()

        waitForText(R.string.login_error)
        composeRule.onNodeWithText(text(R.string.login_continue_google)).assertIsEnabled()
    }

    @Test
    fun networkFailure_showsTheNetworkMessage() {
        repository.discordResult = Result.failure(AuthException.Network(IOException("offline")))
        setContent()

        composeRule.onNodeWithText(text(R.string.login_continue_discord)).performClick()

        waitForText(R.string.login_error_network)
    }

    @Test
    fun shownMessage_isClearedFromTheState() {
        repository.discordResult = Result.failure(AuthException.Server(503))
        setContent()

        composeRule.onNodeWithText(text(R.string.login_continue_discord)).performClick()

        waitForText(R.string.login_error)
        composeRule.waitUntil(timeoutMillis = 5_000) { viewModel.uiState.value.userMessage == null }
        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun offline_tappingGoogleShowsTheNetworkMessageAndKeepsButtonsEnabled() {
        network.online.value = false
        setContent()

        composeRule.onNodeWithText(text(R.string.login_continue_google)).performClick()

        waitForText(R.string.login_error_network)
        composeRule.onNodeWithText(text(R.string.login_continue_google)).assertIsEnabled()
    }

    @Test
    fun idle_doesNotShowAMessage() {
        setContent()

        composeRule.onNodeWithText(text(R.string.login_error)).assertDoesNotExist()
        composeRule.onNodeWithText(text(R.string.login_error_network)).assertDoesNotExist()
    }

    private class FakeNetworkMonitor : NetworkMonitor {
        val online = MutableStateFlow(true)

        override val isOnline: Flow<Boolean> = online
    }

    private class FakeAuthRepository : AuthRepository {
        var discordResult: Result<Unit> = Result.success(Unit)

        override suspend fun signInWithGoogle(idToken: String): Result<Unit> = Result.success(Unit)

        override suspend fun signInWithDiscord(): Result<Unit> = discordResult

        override suspend fun signInAsGuest(): Result<Unit> = Result.success(Unit)
    }
}
