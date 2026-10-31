package dev.myrlennia237.amlethmp.core.ui.device

import androidx.compose.ui.tooling.preview.Preview

/**
 * Google Pixel 10 Pro (1280x2856 px, 480 dpi) in portrait and landscape.
 * Values come from Android Studio's `pixel_10_pro` device definition.
 */
@Preview(
    name = "Pixel 10 Pro - Portrait",
    group = "Pixel 10 Pro",
    device = "spec:width=1280px,height=2856px,dpi=480,navigation=buttons",
    showSystemUi = true,
    showBackground = true
)
@Preview(
    name = "Pixel 10 Pro - Landscape",
    group = "Pixel 10 Pro",
    device = "spec:width=2856px,height=1280px,dpi=480,navigation=buttons",
    showSystemUi = true,
    showBackground = true
)
annotation class P10ProPreview
