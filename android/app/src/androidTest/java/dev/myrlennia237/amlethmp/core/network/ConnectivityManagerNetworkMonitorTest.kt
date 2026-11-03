package dev.myrlennia237.amlethmp.core.network

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.milliseconds

/** Runs on a real device because ConnectivityManager has no JVM counterpart. */
@RunWith(AndroidJUnit4::class)
class ConnectivityManagerNetworkMonitorTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun isOnline_emitsTheCurrentStateWithoutWaitingForAChange() = runBlocking {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val expected = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val actual = withTimeout(TIMEOUT_MILLIS.milliseconds) { 
            ConnectivityManagerNetworkMonitor(context).isOnline.first()
        }

        assertEquals(expected, actual)
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}
