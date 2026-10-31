package dev.myrlennia237.amlethmp.screen.login.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.myrlennia237.amlethmp.R
import dev.myrlennia237.amlethmp.core.ui.theme.ControlHeight
import dev.myrlennia237.amlethmp.core.ui.theme.Discord
import dev.myrlennia237.amlethmp.core.ui.theme.Light
import dev.myrlennia237.amlethmp.core.ui.theme.OnLight
import dev.myrlennia237.amlethmp.core.ui.theme.Spacing
import dev.myrlennia237.amlethmp.screen.login.data.SignInMethod
import dev.myrlennia237.amlethmp.screen.login.ui.LoginUiState

/** The Google and Discord buttons, the "or" divider and the guest button. */
@Composable
internal fun LoginActions(
    uiState: LoginUiState,
    onSignIn: (SignInMethod) -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = Spacing.Space3,
    controlHeight: Dp = ControlHeight
) {
    val activeMethod = (uiState as? LoginUiState.SigningIn)?.method
    val isEnabled = uiState is LoginUiState.Idle || uiState is LoginUiState.Error

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier
    ) {
        LoginButton(
            text = stringResource(R.string.login_continue_google),
            loadingText = stringResource(R.string.login_signing_in),
            isLoading = activeMethod == SignInMethod.Google,
            isEnabled = isEnabled,
            isDimmed = activeMethod != null && activeMethod != SignInMethod.Google,
            containerColor = Light,
            contentColor = OnLight,
            minHeight = controlHeight,
            onClick = { onSignIn(SignInMethod.Google) },
            icon = {
                Image(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null
                )
            }
        )
        LoginButton(
            text = stringResource(R.string.login_continue_discord),
            loadingText = stringResource(R.string.login_signing_in),
            isLoading = activeMethod == SignInMethod.Discord,
            isEnabled = isEnabled,
            isDimmed = activeMethod != null && activeMethod != SignInMethod.Discord,
            containerColor = Discord,
            contentColor = Light,
            minHeight = controlHeight,
            onClick = { onSignIn(SignInMethod.Discord) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_discord),
                    contentDescription = null
                )
            }
        )

        LoginDivider()

        LoginButton(
            text = stringResource(R.string.login_continue_guest),
            loadingText = stringResource(R.string.login_signing_in),
            isLoading = activeMethod == SignInMethod.Guest,
            isEnabled = isEnabled,
            isDimmed = activeMethod != null && activeMethod != SignInMethod.Guest,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            minHeight = controlHeight,
            onClick = { onSignIn(SignInMethod.Guest) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_person),
                    contentDescription = null
                )
            }
        )
    }
}

/** Note shown under the guest button: nothing is stored until the user signs in. */
@Composable
internal fun LoginGuestHint(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.login_guest_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.outline,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}
