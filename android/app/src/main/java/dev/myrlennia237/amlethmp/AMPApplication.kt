package dev.myrlennia237.amlethmp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Đây là lớp sẽ đại diện cho toàn bộ ứng dụng chịu trách nhiệm chứa các thành phần được tạo bởi
 * **Dagger** để phục vụ cho việc **DI - Dependency Injection**.
 */
@HiltAndroidApp
class AMPApplication : Application()
