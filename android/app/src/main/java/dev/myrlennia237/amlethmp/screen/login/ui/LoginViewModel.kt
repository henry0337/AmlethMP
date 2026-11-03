package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.myrlennia237.amlethmp.core.auth.AuthException
import dev.myrlennia237.amlethmp.core.auth.AuthRepository
import dev.myrlennia237.amlethmp.core.network.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signIn(method: SignInMethod) {
        if (_uiState.value.status != LoginStatus.Idle) return

        // Locks the buttons right away: the connectivity check below is asynchronous.
        _uiState.update { it.copy(status = LoginStatus.SigningIn(method)) }
        viewModelScope.launch {
            // Checked before anything starts, so offline the Google account sheet is never shown.
            if (method.needsNetwork && !networkMonitor.isOnline.first()) {
                showFailure(LoginUserMessage.NetworkUnavailable)
                return@launch
            }

            when (method) {
                SignInMethod.Google -> _uiState.update {
                    it.copy(status = LoginStatus.SigningIn(method, awaitingGoogleIdToken = true))
                }
                SignInMethod.Discord -> request(method) { authRepository.signInWithDiscord() }
                SignInMethod.Guest -> request(method) { authRepository.signInAsGuest() }
            }
        }
    }

    fun googleIdTokenReceived(idToken: String) {
        if (!_uiState.value.isAwaitingGoogleIdToken) return

        _uiState.update { it.copy(status = LoginStatus.SigningIn(SignInMethod.Google)) }
        viewModelScope.launch {
            request(SignInMethod.Google) { authRepository.signInWithGoogle(idToken) }
        }
    }

    // Closing the account picker is the user's choice, not an error worth a message.
    fun googleSignInCancelled() {
        if (!_uiState.value.isAwaitingGoogleIdToken) return

        _uiState.update { it.copy(status = LoginStatus.Idle) }
    }

    fun googleSignInFailed(cause: Throwable) {
        if (!_uiState.value.isAwaitingGoogleIdToken) return

        Timber.w(cause, "Google sign-in could not provide an ID token")
        showFailure(LoginUserMessage.SignInFailed)
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private suspend fun request(method: SignInMethod, call: suspend () -> Result<Unit>) {
        call().fold(
            onSuccess = { _uiState.update { it.copy(status = LoginStatus.SignedIn) } },
            onFailure = { failure ->
                Timber.w(failure, "Sign-in with %s failed", method)
                showFailure(failure.toUserMessage())
            }
        )
    }

    private fun showFailure(message: LoginUserMessage) {
        _uiState.update { it.copy(status = LoginStatus.Idle, userMessage = message) }
    }
}

private val SignInMethod.needsNetwork: Boolean
    get() = this != SignInMethod.Guest

private fun Throwable.toUserMessage(): LoginUserMessage =
    if (this is AuthException.Network) LoginUserMessage.NetworkUnavailable else LoginUserMessage.SignInFailed
