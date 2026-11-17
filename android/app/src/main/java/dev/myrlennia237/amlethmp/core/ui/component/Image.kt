package dev.myrlennia237.amlethmp.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale

/**
 * **[[Shared UI Component]]**
 *
 * Phiên bản được port từ component [Image] của **Jetpack Compose** giúp hiển thị ảnh lên giao diện
 * thông qua một [bitmap].
 *
 * @see Image
 */
@Composable
fun AMPBitmapImage(
    bitmap: ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality
) {
    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality
    )
}

/**
 * **[[Shared UI Component]]**
 *
 * Phiên bản được port từ component [Image] của **Jetpack Compose** giúp hiển thị ảnh lên giao diện
 * thông qua một [vector].
 *
 * @see Image
 */
@Composable
fun AMPVectorImage(
    vector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    Image(
        imageVector = vector,
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}

/**
 * **[[Shared UI Component]]**
 *
 * Phiên bản được port từ component [Image] của **Jetpack Compose** giúp hiển thị ảnh lên giao diện
 * thông qua một [painter].
 *
 * @see Image
 */
@Composable
fun AMPPainterImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}

/**
 * **[[Shared UI Component]]**
 *
 * Giống với [AMPPainterImage], chỉ khác ở chỗ là component này sẽ không được sử dụng cho các tính
 * năng hỗ trợ cho **trợ năng (accessibility)**.
 *
 * @see AMPPainterImage
 */
@Composable
fun AMPNonAccessibilityPainterImage(
    painter: Painter,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}
