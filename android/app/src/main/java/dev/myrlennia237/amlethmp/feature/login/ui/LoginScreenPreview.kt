package dev.myrlennia237.amlethmp.feature.login.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import dev.myrlennia237.amlethmp.feature.login.data.SignInMethod
import dev.myrlennia237.amlethmp.ui.theme.AmlethMPTheme

private class LoginUiStateProvider : PreviewParameterProvider<LoginUiState> {
    override val values = sequenceOf(
        LoginUiState.Idle,
        LoginUiState.SigningIn(SignInMethod.Google),
        LoginUiState.SigningIn(SignInMethod.Discord),
        LoginUiState.Error
    )
}

@Preview(name = "Phone", widthDp = 393, heightDp = 852)
@Composable
private fun LoginScreenPhonePreview(
    @PreviewParameter(LoginUiStateProvider::class) uiState: LoginUiState
) {
    AmlethMPTheme {
        LoginScreen(uiState = uiState, onSignIn = {})
    }
}

@Preview(name = "Tablet", widthDp = 1024, heightDp = 768)
@Composable
private fun LoginScreenTabletPreview() {
    AmlethMPTheme {
        LoginScreen(uiState = LoginUiState.Idle, onSignIn = {})
    }
}

@Preview(name = "Desktop", widthDp = 1440, heightDp = 810)
@Composable
private fun LoginScreenDesktopPreview() {
    AmlethMPTheme {
        LoginScreen(uiState = LoginUiState.Error, onSignIn = {})
    }
}
