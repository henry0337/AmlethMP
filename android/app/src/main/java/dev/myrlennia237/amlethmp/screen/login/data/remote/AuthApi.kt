package dev.myrlennia237.amlethmp.screen.login.data.remote

import dev.myrlennia237.amlethmp.core.session.Session

interface AuthApi {
    suspend fun signInWithGoogle(idToken: String): Result<Session>

    suspend fun signInWithDiscord(): Result<Session>
}
