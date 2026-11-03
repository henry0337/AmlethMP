package dev.myrlennia237.amlethmp.core.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.myrlennia237.amlethmp.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        allowComments = BuildConfig.DEBUG
        prettyPrint = BuildConfig.DEBUG
        coerceInputValues = true
        encodeDefaults = true
        // The backend may add fields (e.g. token_type) that the app does not model.
        ignoreUnknownKeys = true
    }

    private const val TIMEOUT_SECONDS = 15L
}
