package dev.myrlennia237.amlethmp.core.session

import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters

/** A software AEAD with a fresh random key; the Android Keystore is not available in JVM tests. */
internal fun newTestAead(): Aead {
    AeadConfig.register()
    return KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM)
        .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
}

internal val testSession = Session(
    accessToken = "access-token-123",
    refreshToken = "refresh-token-456",
    expiresAtMillis = 1_700_000_000_000L
)
