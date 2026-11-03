package dev.myrlennia237.amlethmp.core.network

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
internal abstract class NetworkMonitorModule {
    @Binds
    abstract fun bindNetworkMonitor(monitor: ConnectivityManagerNetworkMonitor): NetworkMonitor
}
