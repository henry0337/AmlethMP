package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import dev.myrlennia237.amlethmp.R
import dev.myrlennia237.amlethmp.core.ui.component.AMPScrollColumn
import dev.myrlennia237.amlethmp.core.ui.component.LocalToastState
import dev.myrlennia237.amlethmp.core.ui.theme.Spacing
import dev.myrlennia237.amlethmp.screen.login.data.SignInMethod
import dev.myrlennia237.amlethmp.screen.login.ui.component.LoginActions
import dev.myrlennia237.amlethmp.screen.login.ui.component.LoginBackdrop
import dev.myrlennia237.amlethmp.screen.login.ui.component.LoginGuestHint
import dev.myrlennia237.amlethmp.screen.login.ui.component.LoginIntro
import dev.myrlennia237.amlethmp.screen.login.ui.component.LoginLegal
import kotlinx.serialization.Serializable

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
    val toastState = LocalToastState.current
    val errorMessage = stringResource(R.string.login_error)

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Error) toastState.show(errorMessage)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val layout = loginLayout(width = maxWidth, height = maxHeight)

        LoginBackdrop(modifier = Modifier.fillMaxSize())
        // The backdrop draws behind the system bars; only the content stays inside the safe area.
        Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            when (layout) {
                LoginLayout.Portrait -> LoginPortrait(uiState, onSignIn)
                LoginLayout.SmallPortrait -> ScaledDown(SMALL_PHONE_SCALE) { LoginPortrait(uiState, onSignIn) }
                LoginLayout.Landscape -> LoginLandscape(uiState, onSignIn)
            }
        }
    }
}

@Composable
private fun LoginPortrait(
    uiState: LoginUiState,
    onSignIn: (SignInMethod) -> Unit
) {
    AMPScrollColumn(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
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
                .widthIn(max = PORTRAIT_MAX_WIDTH)
                .fillMaxWidth()
                .padding(bottom = Spacing.Space6)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.Space6),
            modifier = Modifier
                .widthIn(max = PORTRAIT_MAX_WIDTH)
                .fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Space3)) {
                LoginActions(uiState = uiState, onSignIn = onSignIn)
                LoginGuestHint()
            }
            LoginLegal(textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun LoginLandscape(
    uiState: LoginUiState,
    onSignIn: (SignInMethod) -> Unit
) {
    AMPScrollColumn(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.Space5, vertical = Spacing.Space2)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.Space2),
            modifier = Modifier
                .widthIn(max = LANDSCAPE_CONTENT_MAX_WIDTH)
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Space5),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoginIntro(
                    titleStyle = MaterialTheme.typography.headlineLarge,
                    logoSize = 56.dp,
                    textAlign = TextAlign.Start,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                )
                LoginActions(
                    uiState = uiState,
                    onSignIn = onSignIn,
                    spacing = Spacing.Space2,
                    controlHeight = LANDSCAPE_CONTROL_HEIGHT,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Space5),
                verticalAlignment = Alignment.Top
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.Space2),
                    modifier = Modifier.weight(1f)
                ) {
                    LoginGuestHint()
                    LoginLegal(
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = Spacing.Space1)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScaledDown(scale: Float, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density * scale, density.fontScale),
        content = content
    )
}

/** Which layout the design prescribes for the available size. */
internal enum class LoginLayout {
    Portrait,

    /** Portrait on a low screen such as 360x640 dp: same layout, whole UI scaled down. */
    SmallPortrait,

    /** Very short landscape screen: intro on the left, actions on the right. */
    Landscape
}

internal fun loginLayout(width: Dp, height: Dp): LoginLayout = when {
    width > height && height <= LANDSCAPE_MAX_HEIGHT && width < LANDSCAPE_MAX_WIDTH -> LoginLayout.Landscape
    width < COMPACT_WIDTH && height >= width && height <= SMALL_PORTRAIT_MAX_HEIGHT -> LoginLayout.SmallPortrait
    else -> LoginLayout.Portrait
}

@Serializable
data object LoginKey : NavKey


private const val SMALL_PHONE_SCALE = 0.88f

private val COMPACT_WIDTH = 600.dp
private val SMALL_PORTRAIT_MAX_HEIGHT = 720.dp
private val LANDSCAPE_MAX_HEIGHT = 520.dp
private val LANDSCAPE_MAX_WIDTH = 1280.dp

private val PORTRAIT_MAX_WIDTH = 400.dp
private val LANDSCAPE_CONTENT_MAX_WIDTH = 840.dp
private val LANDSCAPE_CONTROL_HEIGHT = 44.dp
