package dev.myrlennia237.amlethmp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.myrlennia237.amlethmp.core.session.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sessionRepository: SessionRepository
) : ViewModel() {

    // Null until the stored session has been read. Only the session present at launch decides where
    // the app starts: signing in or out later navigates explicitly, so the first value is the only one.
    val startDestination: StateFlow<StartDestination?> = sessionRepository.session
        .take(1)
        .map { session -> if (session != null) StartDestination.Home else StartDestination.Login }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
}

enum class StartDestination {
    Login,
    Home
}
