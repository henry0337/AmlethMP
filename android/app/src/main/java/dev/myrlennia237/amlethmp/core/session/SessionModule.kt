package dev.myrlennia237.amlethmp.core.session

import android.content.Context
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.myrlennia237.amlethmp.core.module.ApplicationScope
import dev.myrlennia237.amlethmp.core.module.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    fun provideSessionRepository(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        @ApplicationScope applicationScope: CoroutineScope
    ): SessionRepository {
        val aead by lazy { SessionAead.create(context) }
        val dataStore = createSessionDataStore(
            aead = { aead },
            ioDispatcher = ioDispatcher,
            produceFile = { context.dataStoreFile(SESSION_FILE_NAME) },
            scope = applicationScope + ioDispatcher
        )
        return DataStoreSessionRepository(dataStore)
    }
}
