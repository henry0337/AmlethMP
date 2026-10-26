package dev.myrlennia237.amlethmp.feature.login.data

interface LoginRepository {
    suspend fun signIn(method: SignInMethod): Result<Unit>
}
