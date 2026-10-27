package dev.myrlennia237.amlethmp.screen.login.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.myrlennia237.amlethmp.core.ui.theme.ControlHeight
import dev.myrlennia237.amlethmp.core.ui.theme.PillShape
import dev.myrlennia237.amlethmp.core.ui.theme.Spacing

private const val DIMMED_ALPHA = 0.55f

/**
 * Pill-shaped sign-in button. While [isLoading] it shows a spinner and [loadingText];
 * when [isDimmed] it stays visible but faded, as another button is signing in.
 */
@Composable
internal fun LoginButton(
    text: String,
    loadingText: String,
    isLoading: Boolean,
    isEnabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDimmed: Boolean = false,
    border: BorderStroke? = null,
    icon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        border = border,
        contentPadding = PaddingValues(horizontal = Spacing.Space5),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ControlHeight)
            .alpha(if (isDimmed) DIMMED_ALPHA else 1f)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.Space3, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                icon?.invoke()
            }
            Text(
                text = if (isLoading) loadingText else text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
