package dev.myrlennia237.amlethmp.screen.login.data.remote

import dev.myrlennia237.amlethmp.core.module.NetworkModule
import dev.myrlennia237.amlethmp.core.session.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class RemoteAuthApiTest {

    private val server = MockWebServer()
    private val json = NetworkModule.provideJson()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `google posts the id token to auth google and maps the tokens to a session`() = runTest {
        server.enqueue(tokenResponse(expiresIn = 900))

        val result = api().signInWithGoogle("google-id-token")

        assertEquals(
            Session(accessToken = "access-jwt", refreshToken = "refresh-token", expiresAtMillis = NOW + 900_000L),
            result.getOrThrow()
        )
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/auth/google", request.url.encodedPath)
        val body = json.parseToJsonElement(request.body!!.utf8()).jsonObject
        assertEquals("google-id-token", body.getValue("id_token").jsonPrimitive.content)
        assertEquals("android", body.getValue("client").jsonPrimitive.content)
        assertEquals("Pixel 9", body.getValue("device_name").jsonPrimitive.content)
    }

    @Test
    fun `google tolerates fields the app does not know`() = runTest {
        server.enqueue(
            MockResponse.Builder().body(
                """{"access_token":"a","refresh_token":"r","token_type":"Bearer","expires_in":60,"extra":1}"""
            ).build()
        )

        val result = api().signInWithGoogle("google-id-token")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `google rejection keeps only the status code`() = runTest {
        server.enqueue(MockResponse.Builder().code(401).body("""{"detail":"Invalid Google ID token."}""").build())

        val failure = api().signInWithGoogle("google-id-token").exceptionOrNull()

        assertEquals(401, (failure as AuthApiException).statusCode)
    }

    @Test
    fun `google server error fails with its status code`() = runTest {
        server.enqueue(MockResponse.Builder().code(503).build())

        val failure = api().signInWithGoogle("google-id-token").exceptionOrNull()

        assertEquals(503, (failure as AuthApiException).statusCode)
    }

    @Test
    fun `google malformed body fails`() = runTest {
        server.enqueue(MockResponse.Builder().body("""{"access_token":"a"}""").build())

        val failure = api().signInWithGoogle("google-id-token").exceptionOrNull()

        assertTrue(failure is SerializationException)
    }

    @Test
    fun `google unreachable server fails with an io error`() = runTest {
        val api = api()
        server.close()

        val failure = api.signInWithGoogle("google-id-token").exceptionOrNull()

        assertTrue(failure is IOException)
    }

    @Test
    fun `base url with a trailing slash still reaches auth google`() = runTest {
        server.enqueue(tokenResponse(expiresIn = 60))

        api(baseUrl = server.url("/")).signInWithGoogle("google-id-token")

        assertEquals("/auth/google", server.takeRequest().url.encodedPath)
    }

    @Test
    fun `discord is not supported yet`() = runTest {
        val failure = api().signInWithDiscord().exceptionOrNull()

        assertTrue(failure is UnsupportedOperationException)
        assertEquals(0, server.requestCount)
    }

    private fun api(baseUrl: okhttp3.HttpUrl = server.url("")) = RemoteAuthApi(
        client = OkHttpClient(),
        json = json,
        baseUrl = baseUrl,
        deviceName = "Pixel 9",
        ioDispatcher = Dispatchers.Unconfined,
        currentTimeMillis = { NOW }
    )

    private fun tokenResponse(expiresIn: Long) = MockResponse.Builder().body(
        """{"access_token":"access-jwt","refresh_token":"refresh-token","token_type":"Bearer","expires_in":$expiresIn}"""
    ).build()

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
