package dev.myrlennia237.amlethmp.core.session

import java.io.IOException

interface SessionRepository {
    suspend fun save(session: Session)

    suspend fun restore(): Session?

    suspend fun clear()
}
