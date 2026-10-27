package dev.myrlennia237.amlethmp.screen.login.data

import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/** Placeholder until the real authentication is wired to the backend; always succeeds. */
class FakeLoginRepository @Inject constructor() : LoginRepository {
    override suspend fun signIn(method: SignInMethod): Result<Unit> {
        delay(FAKE_LATENCY_MS.milliseconds)
        return Result.success(Unit)
    }

    private companion object {
        const val FAKE_LATENCY_MS = 1_500L
    }
}
