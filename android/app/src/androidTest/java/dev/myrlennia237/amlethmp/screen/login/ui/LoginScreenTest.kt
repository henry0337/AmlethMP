package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.myrlennia237.amlethmp.R
import dev.myrlennia237.amlethmp.core.ui.component.AMPToastHost
import dev.myrlennia237.amlethmp.core.ui.component.AMPToastState
import dev.myrlennia237.amlethmp.core.ui.component.LocalToastState
import dev.myrlennia237.amlethmp.core.ui.theme.AMPTheme
import dev.myrlennia237.amlethmp.screen.login.data.SignInMethod
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /** Texts are read from resources so the tests pass in whichever language the device uses. */
    private fun text(@StringRes id: Int) = context.getString(id)

    private fun setContent(
        uiState: LoginUiState,
        onSignIn: (SignInMethod) -> Unit = {},
        toastState: AMPToastState = AMPToastState()
    ) {
        composeRule.setContent {
            AMPTheme {
                CompositionLocalProvider(LocalToastState provides toastState) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LoginScreen(uiState = uiState, onSignIn = onSignIn)
                        AMPToastHost(state = toastState)
                    }
                }
            }
        }
    }

    @Test
    fun idle_showsAllSignInOptions() {
        setContent(LoginUiState.Idle)

        composeRule.onNodeWithText(text(R.string.login_continue_google)).assertIsEnabled()
        composeRule.onNodeWithText(text(R.string.login_continue_discord)).assertIsEnabled()
        composeRule.onNodeWithText(text(R.string.login_continue_guest)).assertIsEnabled()
        composeRule.onNodeWithText(text(R.string.login_guest_hint)).assertExists()
    }

    @Test
    fun clickingEachButton_reportsTheChosenMethod() {
        val methods = mutableListOf<SignInMethod>()
        setContent(LoginUiState.Idle, onSignIn = { methods += it })

        composeRule.onNodeWithText(text(R.string.login_continue_google)).performClick()
        composeRule.onNodeWithText(text(R.string.login_continue_discord)).performClick()
        composeRule.onNodeWithText(text(R.string.login_continue_guest)).performClick()

        assertEquals(listOf(SignInMethod.Google, SignInMethod.Discord, SignInMethod.Guest), methods)
    }

    @Test
    fun signingIn_lockedButtonsAndShowsProgressLabel() {
        setContent(LoginUiState.SigningIn(SignInMethod.Google))

        composeRule.onNodeWithText(text(R.string.login_signing_in)).assertIsNotEnabled()
        composeRule.onNodeWithText(text(R.string.login_continue_discord)).assertIsNotEnabled()
        composeRule.onNodeWithText(text(R.string.login_continue_guest)).assertIsNotEnabled()
    }

    @Test
    fun error_showsAToastAndKeepsButtonsEnabledForRetry() {
        setContent(LoginUiState.Error)

        composeRule.onNodeWithText(text(R.string.login_error)).assertExists()
        composeRule.onNodeWithText(text(R.string.login_continue_google)).assertIsEnabled()
    }

    @Test
    fun idle_doesNotShowErrorMessage() {
        setContent(LoginUiState.Idle)

        composeRule.onNodeWithText(text(R.string.login_error)).assertDoesNotExist()
    }
}
