package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
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
                Box(modifier = Modifier.requiredSize(width, height).testTag(SCREEN_TAG)) {
                    LoginScreen(uiState = uiState, onSignInClick = {})
                }
            }
        }
    }

    /**
     * Positions are measured in pixels from the top-left of the box, because the box may not start at
     * the window's origin and dp values inside a scaled-down layout use the scaled density.
     */
    private fun assertFits() {
        val screen = composeRule.onNodeWithTag(SCREEN_TAG).pixelBounds()
        val texts = listOf(
            text(R.string.login_title),
            text(R.string.login_continue_google),
            text(R.string.login_continue_discord),
            text(R.string.login_continue_guest),
            text(R.string.login_guest_hint)
        )
        texts.forEach { text ->
            val bounds = composeRule.onNodeWithText(text).pixelBounds()
            assertTrue("\"$text\" is outside the screen: $bounds in $screen", screen.contains(bounds))
        }
        val legal = composeRule.onNodeWithText(text(R.string.login_legal_terms), substring = true).pixelBounds()
        assertTrue("legal notice is outside the screen: $legal in $screen", screen.contains(legal))
    }

    private fun SemanticsNodeInteraction.pixelBounds(): Rect {
        val node = fetchSemanticsNode()
        return Rect(node.positionInRoot, node.size.toSize())
    }

    private fun Rect.contains(other: Rect) =
        other.left >= left && other.top >= top && other.right <= right && other.bottom <= bottom

    @Test
    fun smallPhonePortrait_fitsTheScreen() {
        setContent(360.dp, 640.dp, LoginUiState())

        assertFits()
    }

    @Test
    fun normalPhonePortrait_fitsTheScreen() {
        setContent(411.dp, 923.dp, LoginUiState())

        assertFits()
    }

    @Test
    fun phoneLandscape_fitsTheScreen() {
        setContent(923.dp, 411.dp, LoginUiState())

        assertFits()
    }

    @Test
    fun smallPhoneLandscape_fitsTheScreen() {
        setContent(640.dp, 360.dp, LoginUiState())

        assertFits()
    }
}

private const val SCREEN_TAG = "login-screen"
