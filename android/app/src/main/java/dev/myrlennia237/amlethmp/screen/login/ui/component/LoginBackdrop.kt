package dev.myrlennia237.amlethmp.screen.login.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import dev.myrlennia237.amlethmp.R

private const val FOCAL_BIAS = -0.1f

/** Full-screen grayscale photo dimmed by a vertical scrim so the content on top stays legible. */
@Composable
internal fun LoginBackdrop(modifier: Modifier = Modifier) {
    val grayscale = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
    val background = MaterialTheme.colorScheme.background
    val scrim = Brush.verticalGradient(
        0f to background.copy(alpha = 0.6f),
        0.55f to background.copy(alpha = 0.88f),
        1f to background.copy(alpha = 0.96f)
    )

    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.login_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(horizontalBias = FOCAL_BIAS, verticalBias = 0f),
            colorFilter = grayscale,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrim)
        )
    }
}
