package dev.myrlennia237.amlethmp.core.session

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val session: Flow<Session?>

    suspend fun save(session: Session)

    suspend fun clear()
}
