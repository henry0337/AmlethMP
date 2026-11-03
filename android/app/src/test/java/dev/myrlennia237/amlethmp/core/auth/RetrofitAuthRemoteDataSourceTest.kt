package dev.myrlennia237.amlethmp.core.auth

import dev.myrlennia237.amlethmp.core.module.NetworkModule
import dev.myrlennia237.amlethmp.core.module.createRetrofit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RetrofitAuthRemoteDataSourceTest {

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
    fun `google posts the id token to auth google and returns the tokens`() = runTest {
        server.enqueue(tokenResponse(expiresIn = 900))

        val result = dataSource().signInWithGoogle("google-id-token")

        assertEquals(
            TokenResponse(accessToken = "access-jwt", refreshToken = "refresh-token", expiresInSeconds = 900),
            result.getOrThrow()
        )
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/auth/google", request.url.encodedPath)
        assertTrue(request.headers["Content-Type"].orEmpty().startsWith("application/json"))
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

        val result = dataSource().signInWithGoogle("google-id-token")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `google rejection fails as rejected and keeps only the status code`() = runTest {
        server.enqueue(MockResponse.Builder().code(401).body("""{"detail":"Invalid Google ID token."}""").build())

        val failure = dataSource().signInWithGoogle("google-id-token").exceptionOrNull()

        assertEquals(401, (failure as AuthException.Rejected).statusCode)
    }

    @Test
    fun `google server error fails as a server error with its status code`() = runTest {
        server.enqueue(MockResponse.Builder().code(503).build())

        val failure = dataSource().signInWithGoogle("google-id-token").exceptionOrNull()

        assertEquals(503, (failure as AuthException.Server).statusCode)
    }

    @Test
    fun `google malformed body fails as an invalid response`() = runTest {
        server.enqueue(MockResponse.Builder().body("""{"access_token":"a"}""").build())

        val failure = dataSource().signInWithGoogle("google-id-token").exceptionOrNull()

        assertTrue(failure is AuthException.InvalidResponse)
    }

    @Test
    fun `google unreachable server fails as a network error`() = runTest {
        val dataSource = dataSource()
        server.close()

        val failure = dataSource.signInWithGoogle("google-id-token").exceptionOrNull()

        assertTrue(failure is AuthException.Network)
    }

    @Test
    fun `base url with a trailing slash still reaches auth google`() = runTest {
        server.enqueue(tokenResponse(expiresIn = 60))

        dataSource(baseUrl = server.url("/")).signInWithGoogle("google-id-token")

        assertEquals("/auth/google", server.takeRequest().url.encodedPath)
    }

    @Test
    fun `base url with a path and no trailing slash keeps that path`() = runTest {
        server.enqueue(tokenResponse(expiresIn = 60))

        dataSource(baseUrl = server.url("/api")).signInWithGoogle("google-id-token")

        assertEquals("/api/auth/google", server.takeRequest().url.encodedPath)
    }

    @Test
    fun `discord is not supported yet`() = runTest {
        val failure = dataSource().signInWithDiscord().exceptionOrNull()

        assertTrue(failure is AuthException.NotSupported)
        assertEquals(0, server.requestCount)
    }

    private fun dataSource(baseUrl: HttpUrl = server.url("")) = RetrofitAuthRemoteDataSource(
        authApi = createRetrofit(baseUrl, OkHttpClient(), json).create(AuthApi::class.java),
        deviceName = "Pixel 9"
    )

    private fun tokenResponse(expiresIn: Long) = MockResponse.Builder().body(
        """{"access_token":"access-jwt","refresh_token":"refresh-token","token_type":"Bearer","expires_in":$expiresIn}"""
    ).build()
}
