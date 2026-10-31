package dev.myrlennia237.amlethmp.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.google.crypto.tink.Aead
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

/** File name under `files/datastore/`; keep in sync with the backup exclusions in `res/xml`. */
internal const val SESSION_FILE_NAME = "session.enc"

/** A session file that cannot be read (corrupted, or its key is gone) is replaced by "no session". */
internal fun createSessionDataStore(
    aead: () -> Aead,
    produceFile: () -> File,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
): DataStore<Session?> =
    DataStoreFactory.create(
        serializer = EncryptedSessionSerializer(aead),
        corruptionHandler = ReplaceFileCorruptionHandler { null },
        scope = scope,
        produceFile = produceFile
    )
