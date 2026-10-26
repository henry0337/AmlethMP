package dev.myrlennia237.amlethmp.feature.login.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginDataModule {
    @Binds
    abstract fun bindLoginRepository(repository: FakeLoginRepository): LoginRepository
}
