package dev.myrlennia237.amlethmp.core.auth

import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
internal abstract class AuthModule {
    @Binds
    abstract fun bindAuthRepository(repository: DefaultAuthRepository): AuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

        @Provides
        @Singleton
        fun provideAuthRemoteDataSource(authApi: AuthApi): AuthRemoteDataSource =
            RetrofitAuthRemoteDataSource(authApi = authApi, deviceName = Build.MODEL.orEmpty())
    }
}
