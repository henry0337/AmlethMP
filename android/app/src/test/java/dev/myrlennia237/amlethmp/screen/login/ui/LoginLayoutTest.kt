package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginLayoutTest {

    @Test
    fun normalPhonePortrait_usesPortrait() {
        assertEquals(LoginLayout.Portrait, loginLayout(width = 411.dp, height = 923.dp))
        assertEquals(LoginLayout.Portrait, loginLayout(width = 427.dp, height = 952.dp))
    }

    @Test
    fun smallPhonePortrait_usesSmallPortrait() {
        assertEquals(LoginLayout.SmallPortrait, loginLayout(width = 360.dp, height = 640.dp))
        assertEquals(LoginLayout.SmallPortrait, loginLayout(width = 360.dp, height = 720.dp))
    }

    @Test
    fun justAboveTheSmallPhoneHeight_usesPortrait() {
        assertEquals(LoginLayout.Portrait, loginLayout(width = 360.dp, height = 721.dp))
    }

    @Test
    fun phoneLandscape_usesLandscape() {
        assertEquals(LoginLayout.Landscape, loginLayout(width = 923.dp, height = 411.dp))
        assertEquals(LoginLayout.Landscape, loginLayout(width = 640.dp, height = 360.dp))
        assertEquals(LoginLayout.Landscape, loginLayout(width = 1000.dp, height = 520.dp))
    }

    @Test
    fun landscapeTallerThanTheLimit_usesPortrait() {
        assertEquals(LoginLayout.Portrait, loginLayout(width = 1000.dp, height = 521.dp))
    }

    @Test
    fun landscapeAsWideAsADesktop_usesPortrait() {
        assertEquals(LoginLayout.Portrait, loginLayout(width = 1280.dp, height = 500.dp))
    }
}
