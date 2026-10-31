package dev.myrlennia237.amlethmp.core.session

import androidx.datastore.core.CorruptionException
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
    private val serializer = EncryptedSessionSerializer { aead }

    private suspend fun encrypt(session: Session?): ByteArray {
        val output = ByteArrayOutputStream()
        serializer.writeTo(session, output)
        return output.toByteArray()
    }

    @Test
    fun roundTrip_returnsTheSameSession() = runTest {
        val bytes = encrypt(testSession)

        assertEquals(testSession, serializer.readFrom(ByteArrayInputStream(bytes)))
    }

    @Test
    fun written_bytesDoNotContainThePlainTokens() = runTest {
        val text = String(encrypt(testSession), Charsets.ISO_8859_1)

        assertFalse(text.contains(testSession.accessToken))
        assertFalse(text.contains(testSession.refreshToken))
    }

    @Test
    fun writingNull_producesAnEmptyFileThatReadsAsNoSession() = runTest {
        val bytes = encrypt(null)

        assertEquals(0, bytes.size)
        assertNull(serializer.readFrom(ByteArrayInputStream(bytes)))
    }

    @Test
    fun tamperedBytes_areReportedAsCorruption() = runTest {
        val bytes = encrypt(testSession)
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 0x01).toByte()

        assertThrows(CorruptionException::class.java) {
            kotlinx.coroutines.runBlocking { serializer.readFrom(ByteArrayInputStream(bytes)) }
        }
    }

    @Test
    fun bytesEncryptedWithAnotherKey_areReportedAsCorruption() = runTest {
        val bytes = encrypt(testSession)
        val otherKeySerializer = EncryptedSessionSerializer { newTestAead() }

        assertThrows(CorruptionException::class.java) {
            kotlinx.coroutines.runBlocking { otherKeySerializer.readFrom(ByteArrayInputStream(bytes)) }
        }
    }

    @Test
    fun toString_doesNotRevealTokens() {
        val text = testSession.toString()

        assertFalse(text.contains(testSession.accessToken))
        assertFalse(text.contains(testSession.refreshToken))
    }
}
