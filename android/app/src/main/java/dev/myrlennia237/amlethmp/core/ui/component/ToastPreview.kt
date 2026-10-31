package dev.myrlennia237.amlethmp.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.myrlennia237.amlethmp.core.ui.theme.AMPTheme
import dev.myrlennia237.amlethmp.core.ui.theme.Spacing

@Preview(name = "Toast - Variants", showBackground = true, widthDp = 411, heightDp = 420)
@Composable
private fun ToastVariantsPreview() {
    AMPTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.Space4),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(Spacing.Space5)
        ) {
            AMPToast(
                message = "Đã có lỗi xảy ra khi đăng nhập. Vui lòng thử lại.",
                type = ToastType.Error,
                onDismiss = {}
            )
            AMPToast(
                message = "Kết nối yếu, một số bài hát có thể tải chậm.",
                type = ToastType.Warning,
                onDismiss = {}
            )
            AMPToast(
                message = "Đã thêm vào danh sách phát.",
                type = ToastType.Success,
                onDismiss = {}
            )
            AMPToast(
                message = "Bản cập nhật mới đã sẵn sàng. Khởi động lại để áp dụng thay đổi và tiếp tục nghe nhạc.",
                type = ToastType.Info,
                onDismiss = {}
            )
        }
    }
}
