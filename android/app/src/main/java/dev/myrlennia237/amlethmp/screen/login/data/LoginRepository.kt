package dev.myrlennia237.amlethmp.screen.login.data

import dev.myrlennia237.amlethmp.core.session.Session
import dev.myrlennia237.amlethmp.core.session.SessionRepository
import dev.myrlennia237.amlethmp.screen.login.data.remote.AuthApi
import java.io.IOException
import javax.inject.Inject

interface LoginRepository {
    suspend fun signInWithGoogle(idToken: String): Result<Unit>

    suspend fun signInWithDiscord(): Result<Unit>

    suspend fun signInAsGuest(): Result<Unit>
}

class DefaultLoginRepository @Inject constructor(
    private val authApi: AuthApi,
    private val sessionRepository: SessionRepository
) : LoginRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> =
        store(authApi.signInWithGoogle(idToken))

    override suspend fun signInWithDiscord(): Result<Unit> =
        store(authApi.signInWithDiscord())

    override suspend fun signInAsGuest(): Result<Unit> = Result.success(Unit)

    private suspend fun store(result: Result<Session>): Result<Unit> = result.fold(
        onSuccess = { session ->
            try {
                sessionRepository.save(session)
                Result.success(Unit)
            } catch (e: IOException) {
                Result.failure(e)
            }
        },
        onFailure = { Result.failure(it) }
    )
}