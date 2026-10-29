package dev.myrlennia237.amlethmp.core.session

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.first
import java.io.IOException

class DataStoreSessionRepository(
    private val dataStore: DataStore<Session?>
) : SessionRepository {

    override suspend fun save(session: Session) {
        dataStore.updateData { session }
    }

    override suspend fun restore(): Session? =
        try {
            dataStore.data.first()
        } catch (_: IOException) {
            null
        }

    override suspend fun clear() {
        dataStore.updateData { null }
    }
}
