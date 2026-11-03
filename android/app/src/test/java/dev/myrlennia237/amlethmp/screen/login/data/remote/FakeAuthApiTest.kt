package dev.myrlennia237.amlethmp.screen.login.data.remote

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAuthApiTest {

    private val api = FakeAuthApi()

    @Test
    fun `google with a token returns a session`() = runTest {
        assertTrue(api.signInWithGoogle("google-id-token").isSuccess)
    }

    @Test
    fun `google with a blank token fails`() = runTest {
        assertTrue(api.signInWithGoogle(" ").isFailure)
    }

    @Test
    fun `discord returns a session`() = runTest {
        assertTrue(api.signInWithDiscord().isSuccess)
    }
}
