package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import dev.myrlennia237.amlethmp.core.ui.device.P10Preview
import dev.myrlennia237.amlethmp.core.ui.device.P10ProPreview
import dev.myrlennia237.amlethmp.core.ui.theme.AMPTheme

@P10Preview
@P10ProPreview
@Composable
private fun LoginScreenPreview(
    @PreviewParameter(LoginUiStateProvider::class) uiState: LoginUiState
) {
    AMPTheme {
        LoginScreen(uiState = uiState, onSignInClick = {})
    }
}

private class LoginUiStateProvider : PreviewParameterProvider<LoginUiState> {
    override val values = sequenceOf(
        LoginUiState(),
        LoginUiState(status = LoginStatus.SigningIn(SignInMethod.Google)),
        LoginUiState(status = LoginStatus.SigningIn(SignInMethod.Discord))
    )
}
