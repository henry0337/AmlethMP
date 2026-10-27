package dev.myrlennia237.amlethmp.screen.login.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import dev.myrlennia237.amlethmp.R

/** Terms and privacy notice; the two links are only styled for now because no URLs are defined yet. */
@Composable
internal fun LoginLegal(
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    val terms = stringResource(R.string.login_legal_terms)
    val privacy = stringResource(R.string.login_legal_privacy)
    val text = stringResource(R.string.login_legal, terms, privacy)
    val linkColor = MaterialTheme.colorScheme.onSurfaceVariant
    val annotated = remember(text, terms, privacy, linkColor) {
        buildAnnotatedString {
            append(text)
            listOf(terms, privacy).forEach { link ->
                val start = text.indexOf(link)
                if (start >= 0) {
                    addStyle(
                        SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                        start,
                        start + link.length
                    )
                }
            }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline,
        textAlign = textAlign,
        modifier = modifier.fillMaxWidth()
    )
}
