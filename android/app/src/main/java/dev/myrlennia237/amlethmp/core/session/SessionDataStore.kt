package dev.myrlennia237.amlethmp.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.google.crypto.tink.Aead
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import java.io.File

internal const val SESSION_FILE_NAME = "session.enc"

internal fun createSessionDataStore(
    aead: () -> Aead,
    ioDispatcher: CoroutineDispatcher,
    produceFile: () -> File,
    scope: CoroutineScope
): DataStore<Session?> =
    DataStoreFactory.create(
        serializer = EncryptedSessionSerializer(aead, ioDispatcher),
        corruptionHandler = ReplaceFileCorruptionHandler { null },
        scope = scope,
        produceFile = produceFile
    )
