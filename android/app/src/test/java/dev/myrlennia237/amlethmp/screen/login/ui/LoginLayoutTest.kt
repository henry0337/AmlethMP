package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginLayoutTest {

    @Test
    fun `normal phone portrait uses portrait`() {
        assertEquals(LoginLayout.Portrait, loginLayout(width = 411.dp, height = 923.dp))
        assertEquals(LoginLayout.Portrait, loginLayout(width = 427.dp, height = 952.dp))
    }

    @Test
    fun `small phone portrait uses small portrait`() {
        assertEquals(LoginLayout.SmallPortrait, loginLayout(width = 360.dp, height = 640.dp))
        assertEquals(LoginLayout.SmallPortrait, loginLayout(width = 360.dp, height = 720.dp))
    }

    @Test
    fun `just above the small phone height uses portrait`() {
        assertEquals(LoginLayout.Portrait, loginLayout(width = 360.dp, height = 721.dp))
    }

    @Test
    fun `phone landscape uses landscape`() {
        assertEquals(LoginLayout.Landscape, loginLayout(width = 923.dp, height = 411.dp))
        assertEquals(LoginLayout.Landscape, loginLayout(width = 640.dp, height = 360.dp))
        assertEquals(LoginLayout.Landscape, loginLayout(width = 1000.dp, height = 520.dp))
    }

    @Test
    fun `landscape taller than the limit uses portrait`() {
        assertEquals(LoginLayout.Portrait, loginLayout(width = 1000.dp, height = 521.dp))
    }

    @Test
    fun `landscape as wide as a desktop uses portrait`() {
        assertEquals(LoginLayout.Portrait, loginLayout(width = 1280.dp, height = 500.dp))
    }
}
