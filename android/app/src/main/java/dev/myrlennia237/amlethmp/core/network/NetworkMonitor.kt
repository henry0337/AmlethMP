package dev.myrlennia237.amlethmp.core.network

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    // Emits the current state as soon as it is collected, then every change.
    val isOnline: Flow<Boolean>
}
