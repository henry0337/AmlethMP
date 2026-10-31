package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.myrlennia237.amlethmp.R
import dev.myrlennia237.amlethmp.core.ui.theme.AMPTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The whole Login screen must fit, without scrolling, at the reference sizes of the design.
 * The screen is laid out in a box of the given size, which may be larger than the test device's
 * window, so positions are checked from the layout bounds rather than from what is on screen.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenLayoutTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun text(@StringRes id: Int) = context.getString(id)

    private fun setContent(width: Dp, height: Dp, uiState: LoginUiState) {
        composeRule.setContent {
            AMPTheme {
                Box(modifier = Modifier.requiredSize(width, height)) {
                    LoginScreen(uiState = uiState, onSignIn = {})
                }
            }
        }
    }

    private fun assertFits(width: Dp, height: Dp) {
        val texts = buildList {
            add(text(R.string.login_title))
            add(text(R.string.login_continue_google))
            add(text(R.string.login_continue_discord))
            add(text(R.string.login_continue_guest))
            add(text(R.string.login_guest_hint))
        }
        texts.forEach { text ->
            val bounds = composeRule.onNodeWithText(text).getUnclippedBoundsInRoot()
            assertTrue(
                "\"$text\" is outside ${width}x$height: $bounds",
                bounds.left >= 0.dp && bounds.top >= 0.dp && bounds.right <= width && bounds.bottom <= height
            )
        }
        val legal = composeRule.onNodeWithText(text(R.string.login_legal_terms), substring = true).getUnclippedBoundsInRoot()
        assertTrue("legal notice is outside ${width}x$height: $legal", legal.bottom <= height)
    }

    @Test
    fun smallPhonePortrait_fitsTheScreen() {
        setContent(360.dp, 640.dp, LoginUiState.Idle)

        assertFits(360.dp, 640.dp)
    }

    @Test
    fun normalPhonePortrait_fitsTheScreen() {
        setContent(411.dp, 923.dp, LoginUiState.Idle)

        assertFits(411.dp, 923.dp)
    }

    @Test
    fun phoneLandscape_fitsTheScreen() {
        setContent(923.dp, 411.dp, LoginUiState.Idle)

        assertFits(923.dp, 411.dp)
    }

    @Test
    fun smallPhoneLandscape_fitsTheScreen() {
        setContent(640.dp, 360.dp, LoginUiState.Idle)

        assertFits(640.dp, 360.dp)
    }
}
