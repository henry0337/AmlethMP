# AmlethMP Android

## Cấu hình cục bộ

Thêm vào `android/local.properties` (file này đã bị git bỏ qua, đừng commit):

```properties
GOOGLE_WEB_CLIENT_ID=xxxxxxxx.apps.googleusercontent.com
```

`GOOGLE_WEB_CLIENT_ID` là OAuth client ID loại **Web** của dự án Google Cloud, dùng để xin Google ID token khi đăng nhập. Có thể đặt bằng biến môi trường cùng tên (ví dụ trên CI). Nếu không đặt, giá trị là chuỗi rỗng: app vẫn build được nhưng đăng nhập Google sẽ không hoạt động.

Giá trị được đọc vào `BuildConfig.GOOGLE_WEB_CLIENT_ID`. Sau khi sửa `local.properties`, cần sync Gradle lại.

## Kết nối backend

Đăng nhập Google gọi `POST {API_BASE_URL}/auth/google` của thư mục `backend/`. Mặc định
`API_BASE_URL` là `http://10.0.2.2:8000` (địa chỉ emulator dùng để gọi `localhost` của máy
host). Với điện thoại thật, đặt trong `android/local.properties`:

```properties
API_BASE_URL=http://192.168.1.10:8000
```

hoặc chạy `adb reverse tcp:8000 tcp:8000` rồi dùng `http://localhost:8000`. Backend cần chạy
với `--host 0.0.0.0` (xem `backend/README.md`) và `GOOGLE_WEB_CLIENT_ID` của backend phải
trùng với giá trị ở trên. HTTP thuần chỉ được phép ở bản debug, cho các địa chỉ trong
`src/debug/res/xml/network_security_config.xml`; bản release bắt buộc dùng HTTPS.

Ngoài Web client ID, Google Cloud còn cần một OAuth client loại **Android** với package
`dev.myrlennia237.amlethmp` và SHA-1 của keystore đang ký bản debug, nếu không Credential
Manager sẽ không trả về ID token.

Đăng nhập Discord chưa có endpoint ở backend nên hiện báo lỗi.
