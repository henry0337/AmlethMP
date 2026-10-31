package dev.myrlennia237.amlethmp.screen.login.ui

import dev.myrlennia237.amlethmp.screen.login.data.SignInMethod

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data class SigningIn(val method: SignInMethod) : LoginUiState
    data object Error : LoginUiState
    data object SignedIn : LoginUiState
}
