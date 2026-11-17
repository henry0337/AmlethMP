package dev.myrlennia237.amlethmp.core.ui.device

import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Pixel 10 - Portrait",
    group = "Pixel 10",
    device = "spec:width=1080px,height=2424px,dpi=420,navigation=buttons",
    showSystemUi = true,
    showBackground = true
)
@Preview(
    name = "Pixel 10 - Landscape",
    group = "Pixel 10",
    device = "spec:width=2424px,height=1080px,dpi=420,navigation=buttons",
    showSystemUi = true,
    showBackground = true
)
annotation class P10Preview
