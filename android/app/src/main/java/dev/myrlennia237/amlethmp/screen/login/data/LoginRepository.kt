package dev.myrlennia237.amlethmp.screen.login.data

interface LoginRepository {
    suspend fun signIn(method: SignInMethod): Result<Unit>
}
