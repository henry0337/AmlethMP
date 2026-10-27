package dev.myrlennia237.amlethmp.screen.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import dev.myrlennia237.amlethmp.R
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : NavKey

/** Temporary destination after sign-in; there is no Home design yet. */
@Composable
fun HomePlaceholderScreen(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        Text(
            text = stringResource(R.string.home_placeholder),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
