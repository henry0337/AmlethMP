package dev.myrlennia237.amlethmp.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.myrlennia237.amlethmp.R
import dev.myrlennia237.amlethmp.core.ui.theme.AMPTheme
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToastTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val state = AMPToastState()
    private val closeLabel = InstrumentationRegistry.getInstrumentation().targetContext
        .getString(R.string.toast_close)

    private fun setContent() {
        composeRule.setContent {
            AMPTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    AMPToastHost(state = state)
                }
            }
        }
    }

    @Test
    fun show_displaysTheMessage() {
        setContent()

        composeRule.runOnIdle { state.show("Đã thêm vào danh sách phát.", ToastType.Success) }

        composeRule.onNodeWithText("Đã thêm vào danh sách phát.").assertExists()
    }

    @Test
    fun noToast_showsNothing() {
        setContent()

        composeRule.onNodeWithContentDescription(closeLabel).assertDoesNotExist()
    }

    @Test
    fun closeButton_hidesTheToast() {
        setContent()
        composeRule.runOnIdle { state.show("Lỗi") }

        composeRule.onNodeWithContentDescription(closeLabel).performClick()

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Lỗi").assertDoesNotExist()
        assertNull(state.current)
    }

    @Test
    fun toast_hidesItselfAfterItsDuration() {
        setContent()
        composeRule.runOnIdle { state.show("Tự ẩn", durationMillis = 1_000L) }
        composeRule.onNodeWithText("Tự ẩn").assertExists()

        composeRule.mainClock.advanceTimeBy(1_500L)

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Tự ẩn").assertDoesNotExist()
        assertNull(state.current)
    }

    @Test
    fun newToast_replacesTheCurrentOne() {
        setContent()
        composeRule.runOnIdle { state.show("Thứ nhất") }

        composeRule.runOnIdle { state.show("Thứ hai", ToastType.Info) }

        composeRule.onNodeWithText("Thứ hai").assertExists()
        composeRule.onNodeWithText("Thứ nhất").assertDoesNotExist()
    }
}
