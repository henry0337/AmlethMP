package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import dev.myrlennia237.amlethmp.core.ui.device.P10Preview
import dev.myrlennia237.amlethmp.core.ui.device.P10ProPreview
import dev.myrlennia237.amlethmp.core.ui.theme.AMPTheme
import dev.myrlennia237.amlethmp.screen.login.data.SignInMethod

@P10Preview
@P10ProPreview
@Composable
private fun LoginScreenPreview(
    @PreviewParameter(LoginUiStateProvider::class) uiState: LoginUiState
) {
    AMPTheme {
        LoginScreen(uiState = uiState, onSignIn = {})
    }
}

private class LoginUiStateProvider : PreviewParameterProvider<LoginUiState> {
    override val values = sequenceOf(
        LoginUiState.Idle,
        LoginUiState.SigningIn(SignInMethod.Google),
        LoginUiState.SigningIn(SignInMethod.Discord),
        LoginUiState.Error
    )
}


