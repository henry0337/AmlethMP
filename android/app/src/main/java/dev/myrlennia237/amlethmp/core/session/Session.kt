package dev.myrlennia237.amlethmp.core.session

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtMillis: Long
) {
    override fun toString(): String = "Session(expiresAtMillis=$expiresAtMillis)"
}
