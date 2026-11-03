package dev.myrlennia237.amlethmp.core.auth

import java.io.IOException

// Only the status code is kept: a response body must never end up in logs or toasts.
sealed class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class Network(cause: IOException) : AuthException("The auth server could not be reached", cause)

    class Rejected(val statusCode: Int) : AuthException("The auth request was rejected with HTTP $statusCode")

    class Server(val statusCode: Int) : AuthException("The auth server failed with HTTP $statusCode")

    class InvalidResponse(cause: Throwable) : AuthException("The auth server sent an unreadable response", cause)

    class Storage(cause: IOException) : AuthException("The session could not be stored", cause)

    class NotSupported(method: String) : AuthException("$method sign-in is not available yet")
}
