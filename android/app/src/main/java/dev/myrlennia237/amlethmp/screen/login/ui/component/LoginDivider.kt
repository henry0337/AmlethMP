package dev.myrlennia237.amlethmp.screen.login.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import dev.myrlennia237.amlethmp.R
import dev.myrlennia237.amlethmp.core.ui.theme.Spacing

/** Decorative "or" separator; hidden from accessibility services. */
@Composable
internal fun LoginDivider(modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Space4),
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { }
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 1.dp)
                .background(lineColor)
        )
        Text(
            text = stringResource(R.string.login_divider_or),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 1.dp)
                .background(lineColor)
        )
    }
}
