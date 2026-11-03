package dev.myrlennia237.amlethmp.screen.login.data.google

fun interface GoogleIdTokenSource {
    suspend fun fetch(): Result<String>
}

class GoogleSignInCancelledException : Exception("Google sign-in was cancelled")
