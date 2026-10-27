package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import dev.myrlennia237.amlethmp.core.ui.component.AMPScrollColumn
import dev.myrlennia237.amlethmp.core.ui.theme.Spacing
import dev.myrlennia237.amlethmp.screen.login.data.SignInMethod
import dev.myrlennia237.amlethmp.screen.login.ui.component.LoginActions
import dev.myrlennia237.amlethmp.screen.login.ui.component.LoginBackdrop
import dev.myrlennia237.amlethmp.screen.login.ui.component.LoginIntro
import dev.myrlennia237.amlethmp.screen.login.ui.component.LoginLegal
import kotlinx.serialization.Serializable

@Serializable
data object LoginKey : NavKey

@Composable
fun LoginRoute(
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnSignedIn by rememberUpdatedState(onSignedIn)

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.SignedIn) currentOnSignedIn()
    }

    LoginScreen(
        uiState = uiState,
        onSignIn = viewModel::onSignIn,
        modifier = modifier
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onSignIn: (SignInMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LoginBackdrop(modifier = Modifier.fillMaxSize())
        AMPScrollColumn(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(
                    start = Spacing.Space5,
                    top = Spacing.Space6,
                    end = Spacing.Space5,
                    bottom = Spacing.Space5
                )
        ) {
            // Empty first item: SpaceBetween then splits the free space evenly above and below the intro.
            Spacer(modifier = Modifier)
            LoginIntro(
                titleStyle = MaterialTheme.typography.headlineLarge,
                logoSize = 64.dp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
                    .padding(bottom = Spacing.Space6)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.Space6),
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
            ) {
                LoginActions(
                    uiState = uiState,
                    onSignIn = onSignIn,
                    textAlign = TextAlign.Center
                )
                LoginLegal(textAlign = TextAlign.Center)
            }
        }
    }
}
