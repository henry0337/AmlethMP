package dev.myrlennia237.amlethmp.core.session

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.crypto.tink.Aead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException

/**
 * Stores the session as AEAD-encrypted JSON. An empty file means "no session".
 * [aead] is resolved on every call so a failure to reach the key only affects that call.
 */
class EncryptedSessionSerializer(
    private val aead: () -> Aead
) : Serializer<Session?> {

    override val defaultValue: Session? = null

    override suspend fun readFrom(input: InputStream): Session? {
        val encrypted = input.readBytes()
        if (encrypted.isEmpty()) return null

        return try {
            val plain = aead().decrypt(encrypted, ASSOCIATED_DATA)
            json.decodeFromString<Session>(plain.decodeToString())
        } catch (e: GeneralSecurityException) {
            throw CorruptionException("Cannot decrypt the stored session", e)
        } catch (e: SerializationException) {
            throw CorruptionException("Cannot decode the stored session", e)
        }
    }

    override suspend fun writeTo(t: Session?, output: OutputStream) {
        if (t == null) return

        try {
            val plain = json.encodeToString(t).encodeToByteArray()
            withContext(Dispatchers.IO) {
                output.write(aead().encrypt(plain, ASSOCIATED_DATA))
            }
        } catch (e: GeneralSecurityException) {
            throw IOException("Cannot encrypt the session", e)
        }
    }

    private companion object {
        val ASSOCIATED_DATA = "session".encodeToByteArray()
        val json = Json { ignoreUnknownKeys = true }
    }
}
