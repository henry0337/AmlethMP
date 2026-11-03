package dev.myrlennia237.amlethmp.core.session

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import java.io.IOException

class DataStoreSessionRepository(
    private val dataStore: DataStore<Session?>
) : SessionRepository {

    // An unreadable file means the user has to sign in again, which is better than failing the app.
    override val session: Flow<Session?> = dataStore.data.catch { e ->
        if (e !is IOException) throw e
        Timber.w(e, "Cannot read the stored session")
        emit(null)
    }

    override suspend fun save(session: Session) {
        dataStore.updateData { session }
    }

    override suspend fun clear() {
        dataStore.updateData { null }
    }
}
