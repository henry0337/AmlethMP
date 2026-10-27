package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.myrlennia237.amlethmp.screen.login.data.SignInMethod
import dev.myrlennia237.amlethmp.core.ui.theme.AmlethMPTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        uiState: LoginUiState,
        onSignIn: (SignInMethod) -> Unit = {}
    ) {
        composeRule.setContent {
            AmlethMPTheme {
                LoginScreen(uiState = uiState, onSignIn = onSignIn)
            }
        }
    }

    @Test
    fun idle_showsAllSignInOptions() {
        setContent(LoginUiState.Idle)

        composeRule.onNodeWithText("Continue with Google").assertIsEnabled()
        composeRule.onNodeWithText("Continue with Discord").assertIsEnabled()
        composeRule.onNodeWithText("Continue as guest").assertIsEnabled()
        composeRule.onNodeWithText("Guest sessions don't save your library.").assertExists()
    }

    @Test
    fun clickingEachButton_reportsTheChosenMethod() {
        val methods = mutableListOf<SignInMethod>()
        setContent(LoginUiState.Idle, onSignIn = { methods += it })

        composeRule.onNodeWithText("Continue with Google").performClick()
        composeRule.onNodeWithText("Continue with Discord").performClick()
        composeRule.onNodeWithText("Continue as guest").performClick()

        assertEquals(listOf(SignInMethod.Google, SignInMethod.Discord, SignInMethod.Guest), methods)
    }

    @Test
    fun signingIn_lockedButtonsAndShowsProgressLabel() {
        setContent(LoginUiState.SigningIn(SignInMethod.Google))

        composeRule.onNodeWithText("Signing in…").assertIsNotEnabled()
        composeRule.onNodeWithText("Continue with Discord").assertIsNotEnabled()
        composeRule.onNodeWithText("Continue as guest").assertIsNotEnabled()
    }

    @Test
    fun error_showsMessageAndKeepsButtonsEnabledForRetry() {
        setContent(LoginUiState.Error)

        composeRule.onNodeWithText("Something went wrong while signing in. Please try again.").assertExists()
        composeRule.onNodeWithText("Continue with Google").assertIsEnabled()
    }

    @Test
    fun idle_doesNotShowErrorMessage() {
        setContent(LoginUiState.Idle)

        composeRule.onNodeWithText("Something went wrong while signing in. Please try again.").assertDoesNotExist()
    }
}
