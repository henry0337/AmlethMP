package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.myrlennia237.amlethmp.R
import dev.myrlennia237.amlethmp.core.ui.theme.AMPTheme
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
        onSignInClick: (SignInMethod) -> Unit = {}
    ) {
        composeRule.setContent {
            AMPTheme {
                LoginScreen(uiState = uiState, onSignInClick = onSignInClick)
            }
        }
    }

    @Test
    fun idle_showsAllSignInOptions() {
        setContent(LoginUiState())

        composeRule.onNodeWithText(text(R.string.login_continue_google)).assertIsEnabled()
        composeRule.onNodeWithText(text(R.string.login_continue_discord)).assertIsEnabled()
        composeRule.onNodeWithText(text(R.string.login_continue_guest)).assertIsEnabled()
        composeRule.onNodeWithText(text(R.string.login_guest_hint)).assertExists()
    }

    @Test
    fun clickingEachButton_reportsTheChosenMethod() {
        val methods = mutableListOf<SignInMethod>()
        setContent(LoginUiState(), onSignInClick = { methods += it })

        composeRule.onNodeWithText(text(R.string.login_continue_google)).performClick()
        composeRule.onNodeWithText(text(R.string.login_continue_discord)).performClick()
        composeRule.onNodeWithText(text(R.string.login_continue_guest)).performClick()

        assertEquals(listOf(SignInMethod.Google, SignInMethod.Discord, SignInMethod.Guest), methods)
    }

    @Test
    fun signingIn_lockedButtonsAndShowsProgressLabel() {
        setContent(LoginUiState(status = LoginStatus.SigningIn(SignInMethod.Google)))

        composeRule.onNodeWithText(text(R.string.login_signing_in)).assertIsNotEnabled()
        composeRule.onNodeWithText(text(R.string.login_continue_discord)).assertIsNotEnabled()
        composeRule.onNodeWithText(text(R.string.login_continue_guest)).assertIsNotEnabled()
    }
}
