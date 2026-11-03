package dev.myrlennia237.amlethmp.core.session

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class EncryptedSessionSerializerTest {

    private val aead = newTestAead()
    private val serializer = EncryptedSessionSerializer({ aead }, Dispatchers.Unconfined)

    private suspend fun encrypt(session: Session?): ByteArray {
        val output = ByteArrayOutputStream()
        serializer.writeTo(session, output)
        return output.toByteArray()
    }

    @Test
    fun `round trip returns the same session`() = runTest {
        val bytes = encrypt(testSession)

        assertEquals(testSession, serializer.readFrom(ByteArrayInputStream(bytes)))
    }

    @Test
    fun `written bytes do not contain the plain tokens`() = runTest {
        val text = String(encrypt(testSession), Charsets.ISO_8859_1)

        assertFalse(text.contains(testSession.accessToken))
        assertFalse(text.contains(testSession.refreshToken))
    }

    @Test
    fun `writing null produces an empty file that reads as no session`() = runTest {
        val bytes = encrypt(null)

        assertEquals(0, bytes.size)
        assertNull(serializer.readFrom(ByteArrayInputStream(bytes)))
    }

    @Test
    fun `tampered bytes are reported as corruption`() = runTest {
        val bytes = encrypt(testSession)
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 0x01).toByte()

        assertThrows(CorruptionException::class.java) {
            kotlinx.coroutines.runBlocking { serializer.readFrom(ByteArrayInputStream(bytes)) }
        }
    }

    @Test
    fun `bytes encrypted with another key are reported as corruption`() = runTest {
        val bytes = encrypt(testSession)
        val otherKeySerializer = EncryptedSessionSerializer({ newTestAead() }, Dispatchers.Unconfined)

        assertThrows(CorruptionException::class.java) {
            kotlinx.coroutines.runBlocking { otherKeySerializer.readFrom(ByteArrayInputStream(bytes)) }
        }
    }

    @Test
    fun `toString does not reveal the tokens`() {
        val text = testSession.toString()

        assertFalse(text.contains(testSession.accessToken))
        assertFalse(text.contains(testSession.refreshToken))
    }
}
