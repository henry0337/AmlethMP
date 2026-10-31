package dev.myrlennia237.amlethmp.core.session

import android.content.Context
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    fun provideSessionRepository(@ApplicationContext context: Context): SessionRepository {
        val aead by lazy { SessionAead.create(context) }
        val dataStore = createSessionDataStore(
            aead = { aead },
            produceFile = { context.dataStoreFile(SESSION_FILE_NAME) }
        )
        return DataStoreSessionRepository(dataStore)
    }
}
