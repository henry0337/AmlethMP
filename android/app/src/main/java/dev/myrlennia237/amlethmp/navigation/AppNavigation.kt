package dev.myrlennia237.amlethmp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.myrlennia237.amlethmp.feature.home.ui.HomePlaceholderScreen
import dev.myrlennia237.amlethmp.feature.login.ui.LoginRoute
import kotlinx.serialization.Serializable

@Serializable
data object LoginKey : NavKey

@Serializable
data object HomeKey : NavKey

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(LoginKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<LoginKey> {
                LoginRoute(
                    onSignedIn = {
                        backStack.clear()
                        backStack.add(HomeKey)
                    }
                )
            }
            entry<HomeKey> {
                HomePlaceholderScreen()
            }
        },
        modifier = modifier
    )
}
