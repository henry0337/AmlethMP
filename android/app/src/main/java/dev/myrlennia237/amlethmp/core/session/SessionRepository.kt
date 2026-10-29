package dev.myrlennia237.amlethmp.core.session

import java.io.IOException

interface SessionRepository {
    /** @throws IOException if the session cannot be encrypted or written. */
    suspend fun save(session: Session)

    /** The stored session, or null when there is none or it can no longer be read. */
    suspend fun restore(): Session?

    suspend fun clear()
}
