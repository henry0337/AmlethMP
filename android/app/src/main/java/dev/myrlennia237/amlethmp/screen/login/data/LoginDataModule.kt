package dev.myrlennia237.amlethmp.screen.login.data

import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.myrlennia237.amlethmp.BuildConfig
import dev.myrlennia237.amlethmp.screen.login.data.remote.AuthApi
import dev.myrlennia237.amlethmp.screen.login.data.remote.RemoteAuthApi
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
abstract class LoginDataModule {
    @Binds
    abstract fun bindLoginRepository(repository: DefaultLoginRepository): LoginRepository

    companion object {
        @Provides
        @Singleton
        fun provideAuthApi(client: OkHttpClient, json: Json): AuthApi = RemoteAuthApi(
            client = client,
            json = json,
            baseUrl = BuildConfig.API_BASE_URL.toHttpUrl(),
            deviceName = Build.MODEL.orEmpty(),
            ioDispatcher = Dispatchers.IO
        )
    }
}
