package dev.myrlennia237.amlethmp.screen.login.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GoogleSignInRequest(
    @SerialName("id_token") val idToken: String,
    val client: String,
    @SerialName("device_name") val deviceName: String
) {
    override fun toString(): String = "GoogleSignInRequest(client=$client)"
}

@Serializable
internal data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresInSeconds: Long
) {
    override fun toString(): String = "TokenResponse(expiresInSeconds=$expiresInSeconds)"
}
