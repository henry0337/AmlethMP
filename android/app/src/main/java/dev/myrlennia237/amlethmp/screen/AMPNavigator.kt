package dev.myrlennia237.amlethmp.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.myrlennia237.amlethmp.screen.home.ui.HomeKey
import dev.myrlennia237.amlethmp.screen.home.ui.HomePlaceholderScreen
import dev.myrlennia237.amlethmp.screen.login.ui.LoginKey
import dev.myrlennia237.amlethmp.screen.login.ui.LoginRoute

@Composable
fun Navigator(modifier: Modifier = Modifier) {
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
            entry<HomeKey> { HomePlaceholderScreen() }
        },
        modifier = modifier
    )
}
