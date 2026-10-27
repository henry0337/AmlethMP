package dev.myrlennia237.amlethmp.screen.login.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import dev.myrlennia237.amlethmp.R
import dev.myrlennia237.amlethmp.core.ui.theme.Spacing

@Composable
internal fun LoginIntro(
    titleStyle: TextStyle,
    logoSize: Dp,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.Space3),
        modifier = modifier
    ) {
        Image(
            painter = painterResource(R.drawable.logo_mark),
            contentDescription = stringResource(R.string.login_logo_description),
            modifier = Modifier
                .padding(bottom = Spacing.Space3)
                .size(logoSize)
        )
        Text(
            text = stringResource(R.string.login_title),
            style = titleStyle,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = textAlign,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() }
        )
        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
