package dev.myrlennia237.amlethmp.feature.login.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.myrlennia237.amlethmp.R
import dev.myrlennia237.amlethmp.feature.login.data.SignInMethod
import dev.myrlennia237.amlethmp.ui.theme.Discord
import dev.myrlennia237.amlethmp.ui.theme.Light
import dev.myrlennia237.amlethmp.ui.theme.OnLight
import dev.myrlennia237.amlethmp.ui.theme.Spacing

private val TabletBreakpoint = 768.dp
private val DesktopBreakpoint = 1280.dp
private val PhoneContentMaxWidth = 400.dp
private val TabletCardMaxWidth = 460.dp

private const val PHONE_FOCAL_BIAS = -0.1f
private const val DESKTOP_FOCAL_BIAS = 0.24f
private const val DESKTOP_IMAGE_WEIGHT = 1.15f

@Composable
fun LoginRoute(
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnSignedIn by rememberUpdatedState(onSignedIn)

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.SignedIn) currentOnSignedIn()
    }

    LoginScreen(
        uiState = uiState,
        onSignIn = viewModel::onSignIn,
        modifier = modifier
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onSignIn: (SignInMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            maxWidth >= DesktopBreakpoint -> DesktopLayout(uiState, onSignIn)
            maxWidth >= TabletBreakpoint -> TabletLayout(uiState, onSignIn)
            else -> PhoneLayout(uiState, onSignIn)
        }
    }
}

@Composable
private fun PhoneLayout(
    uiState: LoginUiState,
    onSignIn: (SignInMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LoginBackdrop(
            focalBias = PHONE_FOCAL_BIAS,
            scrim = fullScreenScrim(),
            modifier = Modifier.fillMaxSize()
        )
        LoginScrollColumn(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(
                    start = Spacing.Space5,
                    top = Spacing.Space6,
                    end = Spacing.Space5,
                    bottom = Spacing.Space5
                )
        ) {
            // Empty first item: SpaceBetween then splits the free space evenly above and below the intro.
            Spacer(modifier = Modifier)
            LoginIntro(
                titleStyle = MaterialTheme.typography.headlineLarge,
                logoSize = 64.dp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(max = PhoneContentMaxWidth)
                    .fillMaxWidth()
                    .padding(bottom = Spacing.Space6)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.Space6),
                modifier = Modifier
                    .widthIn(max = PhoneContentMaxWidth)
                    .fillMaxWidth()
            ) {
                LoginActions(
                    uiState = uiState,
                    onSignIn = onSignIn,
                    textAlign = TextAlign.Center
                )
                LoginLegal(textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun TabletLayout(
    uiState: LoginUiState,
    onSignIn: (SignInMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LoginBackdrop(
            focalBias = PHONE_FOCAL_BIAS,
            scrim = fullScreenScrim(),
            modifier = Modifier.fillMaxSize()
        )
        LoginScrollColumn(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(Spacing.Space7)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.Space6),
                modifier = Modifier
                    .widthIn(max = TabletCardMaxWidth)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
                    .padding(horizontal = Spacing.Space6, vertical = Spacing.Space7)
            ) {
                LoginIntro(
                    titleStyle = MaterialTheme.typography.displaySmall,
                    logoSize = 72.dp,
                    textAlign = TextAlign.Center
                )
                LoginActions(
                    uiState = uiState,
                    onSignIn = onSignIn,
                    textAlign = TextAlign.Center
                )
                LoginLegal(textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun DesktopLayout(
    uiState: LoginUiState,
    onSignIn: (SignInMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    val background = MaterialTheme.colorScheme.background.copy(alpha = 0.25f)

    Row(modifier = modifier.fillMaxSize()) {
        LoginBackdrop(
            focalBias = DESKTOP_FOCAL_BIAS,
            scrim = Brush.verticalGradient(listOf(background, background)),
            fadeToPanel = true,
            modifier = Modifier
                .weight(DESKTOP_IMAGE_WEIGHT)
                .fillMaxHeight()
        )
        LoginScrollColumn(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(Spacing.Space7)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.Space6),
                modifier = Modifier
                    .widthIn(max = PhoneContentMaxWidth)
                    .fillMaxWidth()
            ) {
                LoginIntro(
                    titleStyle = MaterialTheme.typography.displaySmall,
                    logoSize = 72.dp,
                    textAlign = TextAlign.Start
                )
                LoginActions(
                    uiState = uiState,
                    onSignIn = onSignIn,
                    textAlign = TextAlign.Start
                )
                LoginLegal(textAlign = TextAlign.Start)
            }
        }
    }
}

@Composable
private fun fullScreenScrim(): Brush {
    val background = MaterialTheme.colorScheme.background
    return Brush.verticalGradient(
        0f to background.copy(alpha = 0.6f),
        0.55f to background.copy(alpha = 0.88f),
        1f to background.copy(alpha = 0.96f)
    )
}

/** Scrolls when the content does not fit, otherwise stretches to the full height so [verticalArrangement] applies. */
@Composable
private fun LoginScrollColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = verticalArrangement,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
            content = content
        )
    }
}

@Composable
private fun LoginBackdrop(
    focalBias: Float,
    scrim: Brush,
    modifier: Modifier = Modifier,
    fadeToPanel: Boolean = false
) {
    val grayscale = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
    val maskModifier = if (fadeToPanel) {
        Modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Black,
                        0.55f to Color.Black,
                        1f to Color.Transparent
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
    } else {
        Modifier
    }

    Box(modifier = modifier.then(maskModifier)) {
        Image(
            painter = painterResource(R.drawable.login_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(horizontalBias = focalBias, verticalBias = 0f),
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

@Composable
private fun LoginIntro(
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

@Composable
private fun LoginActions(
    uiState: LoginUiState,
    onSignIn: (SignInMethod) -> Unit,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    val activeMethod = (uiState as? LoginUiState.SigningIn)?.method
    val isEnabled = uiState is LoginUiState.Idle || uiState is LoginUiState.Error

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.Space3),
        modifier = modifier
    ) {
        if (uiState is LoginUiState.Error) {
            LoginErrorAlert(textAlign = textAlign)
        }

        LoginButton(
            text = stringResource(R.string.login_continue_google),
            loadingText = stringResource(R.string.login_signing_in),
            isLoading = activeMethod == SignInMethod.Google,
            isEnabled = isEnabled,
            isDimmed = activeMethod != null && activeMethod != SignInMethod.Google,
            containerColor = Light,
            contentColor = OnLight,
            onClick = { onSignIn(SignInMethod.Google) },
            icon = {
                Image(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null
                )
            }
        )
        LoginButton(
            text = stringResource(R.string.login_continue_discord),
            loadingText = stringResource(R.string.login_signing_in),
            isLoading = activeMethod == SignInMethod.Discord,
            isEnabled = isEnabled,
            isDimmed = activeMethod != null && activeMethod != SignInMethod.Discord,
            containerColor = Discord,
            contentColor = Light,
            onClick = { onSignIn(SignInMethod.Discord) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_discord),
                    contentDescription = null
                )
            }
        )

        LoginDivider()

        LoginButton(
            text = stringResource(R.string.login_continue_guest),
            loadingText = stringResource(R.string.login_signing_in),
            isLoading = activeMethod == SignInMethod.Guest,
            isEnabled = isEnabled,
            isDimmed = activeMethod != null && activeMethod != SignInMethod.Guest,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            onClick = { onSignIn(SignInMethod.Guest) }
        )
        Text(
            text = stringResource(R.string.login_guest_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LoginErrorAlert(
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Assertive }
    ) {
        Text(
            text = stringResource(R.string.login_error),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = textAlign,
            modifier = Modifier.padding(horizontal = Spacing.Space4, vertical = Spacing.Space3)
        )
    }
}

@Composable
private fun LoginDivider(modifier: Modifier = Modifier) {
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

@Composable
private fun LoginLegal(
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
