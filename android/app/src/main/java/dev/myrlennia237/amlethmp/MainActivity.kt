package dev.myrlennia237.amlethmp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.AndroidEntryPoint
import dev.myrlennia237.amlethmp.core.ui.theme.AMPTheme
import dev.myrlennia237.amlethmp.screen.Navigator
import dev.myrlennia237.amlethmp.screen.home.ui.HomeKey
import dev.myrlennia237.amlethmp.screen.login.ui.LoginKey

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            AMPTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainRoute()
                }
            }
        }
    }
}

@Composable
private fun MainRoute(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()

    // Nothing to draw while the stored session is read; the Surface behind already paints the background.
    startDestination?.let { Navigator(startKey = it.key, modifier = modifier) }
}

private val StartDestination.key: NavKey
    get() = when (this) {
        StartDestination.Login -> LoginKey
        StartDestination.Home -> HomeKey
    }
