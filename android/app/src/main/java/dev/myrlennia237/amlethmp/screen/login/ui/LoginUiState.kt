package dev.myrlennia237.amlethmp.screen.login.ui

data class LoginUiState(
    val status: LoginStatus = LoginStatus.Idle,
    val userMessage: LoginUserMessage? = null
) {
    val isAwaitingGoogleIdToken: Boolean
        get() = (status as? LoginStatus.SigningIn)?.awaitingGoogleIdToken == true
}

sealed interface LoginStatus {
    data object Idle : LoginStatus

    // The Google ID token comes from a system sheet the screen shows, so the ViewModel waits for the
    // screen to report it before it can call the backend.
    data class SigningIn(
        val method: SignInMethod,
        val awaitingGoogleIdToken: Boolean = false
    ) : LoginStatus

    data object SignedIn : LoginStatus
}

enum class LoginUserMessage {
    SignInFailed,
    NetworkUnavailable
}
