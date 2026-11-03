package dev.myrlennia237.amlethmp.core.auth

import dev.myrlennia237.amlethmp.core.session.Session
import dev.myrlennia237.amlethmp.core.session.SessionRepository
import java.io.IOException
import java.time.Clock
import javax.inject.Inject

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<Unit>

    suspend fun signInWithDiscord(): Result<Unit>

    suspend fun signInAsGuest(): Result<Unit>
}

internal class DefaultAuthRepository @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val sessionRepository: SessionRepository,
    private val clock: Clock
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> =
        signIn { remoteDataSource.signInWithGoogle(idToken) }

    override suspend fun signInWithDiscord(): Result<Unit> =
        signIn { remoteDataSource.signInWithDiscord() }

    // A guest has no account, so there is nothing to request and no session to store.
    override suspend fun signInAsGuest(): Result<Unit> = Result.success(Unit)

    private suspend fun signIn(request: suspend () -> Result<TokenResponse>): Result<Unit> =
        request().fold(
            onSuccess = { tokens -> store(tokens.toSession(nowMillis = clock.millis())) },
            onFailure = { Result.failure(it) }
        )

    private suspend fun store(session: Session): Result<Unit> =
        try {
            sessionRepository.save(session)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(AuthException.Storage(e))
        }
}

private fun TokenResponse.toSession(nowMillis: Long) = Session(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresAtMillis = nowMillis + expiresInSeconds * MILLIS_PER_SECOND
)

private const val MILLIS_PER_SECOND = 1_000L
