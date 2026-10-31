package dev.myrlennia237.amlethmp.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.myrlennia237.amlethmp.R
import dev.myrlennia237.amlethmp.core.ui.theme.Border
import dev.myrlennia237.amlethmp.core.ui.theme.Danger
import dev.myrlennia237.amlethmp.core.ui.theme.Info as InfoColor
import dev.myrlennia237.amlethmp.core.ui.theme.Spacing
import dev.myrlennia237.amlethmp.core.ui.theme.Success as SuccessColor
import dev.myrlennia237.amlethmp.core.ui.theme.Warning as WarningColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * **[[Shared UI Component]]**
 *
 * Thông báo ngắn theo `design/components/Toast.html`. Chỉ vẽ giao diện; việc hiện, ẩn và tự đóng
 * do [AMPToastHost] đảm nhiệm.
 *
 * @author Myrlennia
 */
@Composable
fun AMPToast(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    type: ToastType = ToastType.Error
) {
    val shape = MaterialTheme.shapes.medium

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Space3),
        modifier = modifier
            .widthIn(max = TOAST_MAX_WIDTH)
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = type.color.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f), shape)
            .border(1.dp, lerp(Border, type.color, 0.38f), shape)
            .padding(
                start = Spacing.Space4,
                top = Spacing.Space3,
                end = Spacing.Space2,
                bottom = Spacing.Space3
            )
    ) {
        Icon(
            painter = painterResource(type.icon),
            contentDescription = null,
            tint = type.color,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .semantics { liveRegion = LiveRegionMode.Assertive }
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onDismiss)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.toast_close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Shows the toast of [state] at the top of the screen, below the status bar, and hides it after its
 * duration. The countdown pauses while the toast is touched or hovered and continues with the time
 * left. Place it once at the root, above the content.
 */
@Composable
fun AMPToastHost(
    state: AMPToastState,
    modifier: Modifier = Modifier
) {
    val toast = state.current
    // Kept after the toast is dismissed so that its exit animation still has something to draw.
    var shown by remember { mutableStateOf(toast) }
    if (toast != null && toast != shown) shown = toast

    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(toast) {
        if (toast == null) return@LaunchedEffect
        isPaused = false
        var timeLeft = toast.durationMillis
        while (timeLeft > 0) {
            snapshotFlow { isPaused }.first { !it }
            val startedAt = withFrameMillis { it }
            val wasPaused = withTimeoutOrNull(timeLeft.milliseconds) {
                snapshotFlow { isPaused }.first { it }
            } != null
            timeLeft = if (wasPaused) timeLeft - (withFrameMillis { it } - startedAt) else 0
        }
        state.dismiss()
    }

    val density = LocalDensity.current
    val enterOffset = with(density) { TOAST_ENTER_OFFSET.roundToPx() }
    val exitOffset = with(density) { TOAST_EXIT_OFFSET.roundToPx() }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isShortLandscape = maxWidth > maxHeight && maxHeight <= SHORT_LANDSCAPE_MAX_HEIGHT
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
                .padding(
                    start = Spacing.Space5,
                    top = if (isShortLandscape) Spacing.Space3 else Spacing.Space7,
                    end = Spacing.Space5
                )
        ) {
            AnimatedVisibility(
                visible = toast != null,
                enter = fadeIn(tween(TOAST_ENTER_MILLIS, easing = ToastEnterEasing)) +
                    slideInVertically(tween(TOAST_ENTER_MILLIS, easing = ToastEnterEasing)) { -enterOffset } +
                    scaleIn(tween(TOAST_ENTER_MILLIS, easing = ToastEnterEasing), initialScale = TOAST_SCALE),
                exit = fadeOut(tween(TOAST_EXIT_MILLIS, easing = ToastExitEasing)) +
                    slideOutVertically(tween(TOAST_EXIT_MILLIS, easing = ToastExitEasing)) { -exitOffset } +
                    scaleOut(tween(TOAST_EXIT_MILLIS, easing = ToastExitEasing), targetScale = TOAST_SCALE)
            ) {
                shown?.let { data ->
                    AMPToast(
                        message = data.message,
                        type = data.type,
                        onDismiss = state::dismiss,
                        modifier = Modifier.pointerInput(Unit) { trackPause { isPaused = it } }
                    )
                }
            }
        }
    }
}

@Stable
class AMPToastState {
    var current by mutableStateOf<ToastData?>(null)
        private set

    private var nextId = 0L

    fun show(
        message: String,
        type: ToastType = ToastType.Error,
        durationMillis: Long = DEFAULT_TOAST_DURATION_MILLIS
    ) {
        current = ToastData(nextId++, message, type, durationMillis)
    }

    fun dismiss() {
        current = null
    }
}

@Composable
fun rememberToastState(): AMPToastState = remember { AMPToastState() }

val LocalToastState = staticCompositionLocalOf { AMPToastState() }

enum class ToastType(val color: Color, @DrawableRes val icon: Int) {
    Error(Danger, R.drawable.ic_error),
    Warning(WarningColor, R.drawable.ic_warning),
    Success(SuccessColor, R.drawable.ic_success),
    Info(InfoColor, R.drawable.ic_info)
}

@Immutable
data class ToastData(
    val id: Long,
    val message: String,
    val type: ToastType,
    val durationMillis: Long
)

const val DEFAULT_TOAST_DURATION_MILLIS = 5_000L

private suspend fun PointerInputScope.trackPause(onPausedChange: (Boolean) -> Unit) {
    awaitPointerEventScope {
        var isHovered = false
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            when (event.type) {
                PointerEventType.Enter -> isHovered = true
                PointerEventType.Exit -> isHovered = false
            }
            onPausedChange(isHovered || event.changes.any { it.pressed })
        }
    }
}

private val TOAST_MAX_WIDTH = 420.dp
private val SHORT_LANDSCAPE_MAX_HEIGHT = 520.dp
private val TOAST_ENTER_OFFSET = 14.dp
private val TOAST_EXIT_OFFSET = 10.dp
private const val TOAST_SCALE = 0.97f
private const val TOAST_ENTER_MILLIS = 260
private const val TOAST_EXIT_MILLIS = 180
private val ToastEnterEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
private val ToastExitEasing = CubicBezierEasing(0.42f, 0f, 1f, 1f)
