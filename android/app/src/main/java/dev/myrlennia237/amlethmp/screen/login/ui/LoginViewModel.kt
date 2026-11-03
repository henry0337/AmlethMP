package dev.myrlennia237.amlethmp.screen.login.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.myrlennia237.amlethmp.screen.login.data.LoginRepository
import dev.myrlennia237.amlethmp.screen.login.data.model.SignInMethod
import dev.myrlennia237.amlethmp.screen.login.data.google.GoogleIdTokenSource
import dev.myrlennia237.amlethmp.screen.login.data.google.GoogleSignInCancelledException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onSignIn(method: SignInMethod, googleIdTokenSource: GoogleIdTokenSource) {
        val current = _uiState.value
        if (current is LoginUiState.SigningIn || current is LoginUiState.SignedIn) return

        _uiState.value = LoginUiState.SigningIn(method)
        viewModelScope.launch {
            val result = when (method) {
                SignInMethod.Google -> googleIdTokenSource.fetch().fold(
                    onSuccess = { idToken -> repository.signInWithGoogle(idToken) },
                    onFailure = { Result.failure(it) }
                )
                SignInMethod.Discord -> repository.signInWithDiscord()
                SignInMethod.Guest -> repository.signInAsGuest()
            }
            _uiState.value = result.fold(
                onSuccess = { LoginUiState.SignedIn },
                onFailure = { if (it is GoogleSignInCancelledException) LoginUiState.Idle else LoginUiState.Error }
            )
        }
    }
}
