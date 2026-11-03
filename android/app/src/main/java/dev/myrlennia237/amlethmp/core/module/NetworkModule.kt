package dev.myrlennia237.amlethmp.core.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.myrlennia237.amlethmp.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
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

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        createRetrofit(BuildConfig.API_BASE_URL.toHttpUrl(), client, json)

    private const val TIMEOUT_SECONDS = 15L
}

internal fun createRetrofit(baseUrl: HttpUrl, client: OkHttpClient, json: Json): Retrofit =
    Retrofit.Builder()
        .baseUrl(baseUrl.withTrailingSlash())
        .client(client)
        .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE))
        .build()

// Retrofit resolves endpoint paths against the base URL and rejects one that does not end with "/".
private fun HttpUrl.withTrailingSlash(): HttpUrl =
    if (encodedPath.endsWith("/")) this else newBuilder().addPathSegment("").build()

private val JSON_MEDIA_TYPE = "application/json; charset=UTF-8".toMediaType()
