# Android Coding Standards — Kotlin + Jetpack Compose

> Áp dụng cho ứng dụng Android native dùng Kotlin và Jetpack Compose. Đây là baseline cho dự án mới; ưu tiên convention hiện có của repository nếu đã được thống nhất. Những nội dung là khuyến nghị nên được điều chỉnh theo quy mô và yêu cầu thực tế, không phải mọi mục đều là luật bắt buộc của Android.
>
> File này là tài liệu sống: khi một quyết định kiến trúc thay đổi, sửa rule tại đúng mục của nó và nêu lý do trong commit message. Phần data layer (mục 6) bám theo [hướng dẫn data layer của Android](https://developer.android.com/topic/architecture/data-layer); chỗ nào lệch hướng dẫn đó thì ghi rõ lý do ngay tại rule.

## 1. Project và dependency

- Dùng Kotlin và Jetpack Compose cho UI mới; chỉ dùng Android Views khi cần tích hợp hoặc codebase hiện tại yêu cầu.
- Giữ phiên bản Android Gradle Plugin, Gradle, Kotlin, Compose và thư viện tương thích nhau; không tự ý nâng phiên bản ngoài phạm vi task.
- Quản lý dependency tập trung bằng Gradle Version Catalog (`gradle/libs.versions.toml`) nếu dự án đã dùng hoặc đang được thiết lập mới.
- Không thêm thư viện chỉ để giải quyết việc nhỏ nếu API hiện có đã đáp ứng.
- Không commit secret, keystore, signing credentials hoặc file cấu hình chứa thông tin nhạy cảm.
- Dùng cấu trúc module/package theo nhu cầu; không tạo module riêng cho mọi class hoặc feature nhỏ.

## 2. Kotlin style và naming

- Theo [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) và cấu hình formatter của repository.
- Package: chữ thường, không dùng dấu gạch dưới, ví dụ `com.example.app.feature.profile`.
- Class, interface, object, enum và type: `PascalCase`, ví dụ `ProfileViewModel`.
- Function, property và biến: `camelCase`, ví dụ `loadProfile()`.
- Hằng số `const val`: `UPPER_SNAKE_CASE`, ví dụ `MAX_RETRY_COUNT`.
- Đặt tên file `.kt` theo nội dung; tên class chính nên khớp tên file. Tránh tên mơ hồ như `Utils.kt`, `Common.kt` nếu không diễn tả đúng trách nhiệm.
- Composable tạo UI dùng tên `PascalCase`, thường là danh từ hoặc tên thành phần, ví dụ `ProfileScreen`, `ProfileCard`.
- Đặt tên theo nghiệp vụ: `ProfileUiState`, `ProfileViewModel`; repository và data source đặt tên theo loại dữ liệu và nguồn (xem mục 6); chỉ tạo `UseCase` khi cần tách logic nghiệp vụ.
- Ưu tiên `val`; tránh `!!`, mutable global state và ép kiểu không có kiểm chứng.
- Ưu tiên named arguments khi lời gọi nhiều tham số cùng kiểu hoặc nếu tên tham số giúp làm rõ ý nghĩa.

## 3. Kiến trúc và tổ chức code

- Tách rõ **UI layer** và **data layer**; thêm **domain layer** khi cần tái sử dụng hoặc đơn giản hóa business logic phức tạp.
- Dùng repository làm ranh giới truy cập dữ liệu; Composable và ViewModel không truy cập trực tiếp Retrofit, Room, DataStore, Firebase hay nguồn dữ liệu khác.
- Áp dụng unidirectional data flow (UDF): **state đi xuống UI, event/callback đi lên state holder**.
- Để logic giao diện trong UI layer; business logic và điều phối dữ liệu thuộc ViewModel hoặc tầng thích hợp, không đặt trong Composable.
- Ưu tiên tổ chức theo feature khi giúp tăng cohesion; đặt code dùng chung vào `core/` hoặc module tương đương khi có lý do rõ ràng.
- Data layer tổ chức theo loại dữ liệu, không theo màn hình: dữ liệu chỉ một feature dùng nằm ở `screen/<feature>/data`; dữ liệu nhiều feature cùng dùng (xác thực, phiên đăng nhập, cài đặt) nằm ở `core/`.
- Giữ module có cohesion cao, coupling thấp; chỉ tách multi-module khi mang lại lợi ích thực tế về ownership, build, reuse hoặc dependency boundaries.
- Dùng dependency injection theo lựa chọn của repository (ví dụ Hilt hoặc framework hiện có); không đưa thêm DI framework chỉ vì convention này.
- Tránh dùng `AndroidViewModel` hoặc đưa `Activity`, `View`, `Context` hay `Application` vào ViewModel nếu có thể chuyển dependency về UI/data layer.

## 4. Jetpack Compose và API của Composable

- Composable chủ yếu mô tả UI từ tham số/state; không thực hiện network, database hoặc side effect tùy tiện trong thân hàm.
- Ưu tiên **stateless Composable**: nhận dữ liệu bất biến và callback như `onClick`, `onValueChange`; hoist state để caller kiểm soát khi có lợi cho tái sử dụng và kiểm thử.
- Mỗi màn hình tách hai Composable: `XRoute` (stateful: lấy ViewModel, collect state, xử lý điều hướng và side effect UI như toast/snackbar) và `XScreen` (stateless: chỉ nhận `uiState` và callback, không side effect, không đọc `CompositionLocal` của nghiệp vụ). Composable con trong danh sách không nhận ViewModel; truyền sự kiện lên bằng lambda.
- Hoist state lên lowest common ancestor có thể đọc và cập nhật state; giữ state càng gần nơi sử dụng càng tốt.
- Mỗi Composable phát UI nên có một tham số `modifier: Modifier = Modifier` làm **tham số tùy chọn đầu tiên**, sau các tham số bắt buộc; áp dụng nó vào layout gốc và không khai báo nhiều tham số `Modifier`.
- Thứ tự tham số Composable: tham số bắt buộc → `modifier` → tham số tùy chọn → trailing `@Composable` lambda (nếu có).
- Không trả về giá trị từ Composable dùng để phát UI; truyền dữ liệu hoặc callback qua tham số.
- Tách Composable khi trách nhiệm, khả năng tái sử dụng hoặc khả năng kiểm thử được cải thiện; không tách máy móc mọi node nhỏ thành file riêng.
- Tham số callback của Composable đặt tên `on` + động từ + đối tượng (`onSignInClick`, `onItemClick`, `onValueChange`); callback mô tả sự kiện, không chứa side effect bất ngờ. Hàm của ViewModel xử lý sự kiện đặt tên bằng động từ (`signIn()`, `refresh()`, `userMessageShown()`), không có tiền tố `on`, để phân biệt hàm xử lý với callback.
- Ưu tiên Material 3 hoặc design system đang dùng; lấy màu, typography, shape và spacing từ theme/design tokens thay vì hard-code rải rác.
- Dùng resource (`stringResource`, `pluralStringResource`, `painterResource`, v.v.) cho nội dung cần bản địa hóa; không hard-code chuỗi UI cho ứng dụng đa ngôn ngữ.

## 5. State, lifecycle và side effects

- State cục bộ chỉ phục vụ UI có thể dùng `remember`; dùng `rememberSaveable` cho state UI nhỏ cần phục hồi qua configuration/process recreation khi phù hợp.
- Screen/business state thuộc ViewModel hoặc state holder có lifecycle phù hợp; không dùng `remember` để thay thế ViewModel cho dữ liệu màn hình cần tồn tại qua điều hướng hoặc configuration change.
- Với state từ ViewModel, ưu tiên expose `StateFlow<ScreenUiState>` bất biến; không public `MutableStateFlow` hoặc mutable collection để UI sửa trực tiếp.
- Thu thập Flow trong UI bằng `collectAsStateWithLifecycle()` khi phù hợp với lifecycle Android.
- Dùng `data class` cho state có thể đồng thời chứa nhiều trường; dùng `sealed interface`/`sealed class` khi các trạng thái loại trừ nhau.
- Biểu diễn loading, content, empty và error theo yêu cầu của màn hình; không dùng Boolean rời rạc nếu dẫn đến tổ hợp trạng thái vô lý.
- Dùng `LaunchedEffect` cho coroutine gắn với lifecycle/key của Composition; `DisposableEffect` cho đăng ký cần cleanup; `rememberCoroutineScope` cho coroutine được khởi chạy từ tương tác UI.
- Không chạy coroutine, I/O hoặc sửa state trực tiếp mỗi lần Composable được gọi; không dùng `GlobalScope` hoặc `runBlocking` trong đường đi UI.
- Không dùng Channel/SharedFlow cho event từ ViewModel lên UI: khi ViewModel sống lâu hơn UI, event có thể mất hoặc xử lý sai và để app ở trạng thái không nhất quán. Biến event thành state mà UI quan sát được (xem mục "UI state và event" bên dưới).

### UI state và event

- "State is; events happen": state luôn tồn tại và là đầu ra, event là đầu vào xảy ra rồi mất. Mọi event xuất phát từ ViewModel phải dẫn tới một lần cập nhật UI state; đừng nghĩ UI cần làm hành động gì, hãy nghĩ hành động đó làm state thay đổi ra sao.
- Phân loại logic theo nơi xuất phát: logic nghiệp vụ (đăng nhập, lưu dữ liệu) ủy thác cho ViewModel; logic hiển thị (điều hướng, toast, snackbar, lấy chuỗi từ resource, trạng thái mở/đóng của một widget) ở lại UI. ViewModel không giữ chuỗi đã dịch; nó expose loại lỗi hoặc dữ liệu thô và UI ánh xạ sang `stringResource`.
- Tổ chức UI state: gộp các state liên quan vào một object duy nhất để không lệch nhau; trong object đó dùng `sealed interface` cho nhóm trạng thái loại trừ nhau (ví dụ `Idle`/`SigningIn`/`SignedIn`) và field riêng cho phần độc lập. Không đặt sự kiện một lần (lỗi, thông báo) vào sealed state làm state dính.
- Thông báo tạm thời cho người dùng (lỗi, toast, snackbar) là một field trong UI state (`userMessage`, hoặc danh sách có `id` khi có thể xếp hàng). UI hiển thị rồi gọi hàm xác nhận của ViewModel (`userMessageShown()`) để xóa nó khỏi state, nhờ đó thông báo không hiện lại khi xoay màn hình hay khi Composable vào lại composition.
- Điều hướng do ViewModel kích hoạt cũng đi qua state (cờ hoàn tất, hoặc `Flow` từ data layer như phiên đăng nhập); `XRoute` quan sát rồi gọi callback do caller truyền vào (`rememberUpdatedState` cho callback trong `LaunchedEffect`). Điều hướng khởi nguồn từ thao tác bấm thì dùng `dropUnlessResumed`. Nếu màn hình nguồn còn nằm trong back stack, cần thêm cờ phía UI để không tự điều hướng lại.
- Logic UI cần Android SDK (Credential Manager, xin permission, Activity result) nằm trong state holder thuần tạo ở UI (`rememberXState(context)`) và ViewModel chỉ nhận kết quả dưới dạng giá trị thuần. Không truyền đối tượng gắn với `Context`/Activity vào ViewModel dưới dạng tham số hay dependency.
- Sản xuất state trong ViewModel: giữ `MutableStateFlow` private và cập nhật bằng `update { }`; state suy ra từ `Flow` của repository dùng `combine(...)` rồi `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue)` để pipeline dừng khi UI không còn quan sát.
- Không `launch` coroutine trong `init`/constructor của ViewModel (đối tượng chưa khởi tạo xong có thể bị đọc/ghi); dùng `stateIn` lazily hoặc hàm `initialize()` idempotent. `viewModelScope` không dùng cho công việc kéo dài từ 5 giây trở lên; dùng WorkManager.
- Khi `catch` trong coroutine phải để `CancellationException` đi qua (ném lại, hoặc chỉ bắt các loại lỗi cụ thể).

## 6. ViewModel, coroutines và data layer

- ViewModel xử lý logic cấp màn hình, gọi repository/use case và expose UI state; không chứa tham chiếu tới View/Activity.
- Dùng `viewModelScope` cho công việc gắn với ViewModel và để structured concurrency quản lý vòng đời coroutine.
- API suspend cho thao tác một lần; `Flow` cho dữ liệu thay đổi theo thời gian khi phù hợp.
- Repository chịu trách nhiệm phối hợp nguồn dữ liệu, mapping và chính sách lỗi/cache phù hợp; không rò rỉ chi tiết triển khai data source không cần thiết lên UI.
- Đảm bảo suspend function main-safe; thực hiện công việc blocking/I/O trên dispatcher phù hợp ở tầng thực thi.
- Inject dispatcher khi cần kiểm soát concurrency và testability; không hard-code dispatcher khắp business logic nếu khiến test khó viết.
- Không nuốt exception bằng `catch` rỗng; chuyển lỗi có ý nghĩa thành trạng thái/kết quả phù hợp và giữ cancellation hoạt động đúng.
- Dùng một nguồn dữ liệu chuẩn (single source of truth) cho mỗi loại state; tránh giữ bản sao mutable của cùng dữ liệu ở nhiều tầng.

### Repository và data source

- Mỗi loại dữ liệu có một repository, đặt tên `<Dữ liệu>Repository` theo dữ liệu mà nó quản lý (`AuthRepository`, `PlaylistRepository`), không theo màn hình hay hành động UI (`LoginRepository`).
- Data source đặt tên `<Dữ liệu><Remote|Local>DataSource` (`AuthRemoteDataSource`, `SessionLocalDataSource`). Không đưa chi tiết triển khai vào tên (`DataStore`, `Retrofit`, `Room`) trừ khi đang chạy song song hai implementation trong lúc migration.
- Giao tiếp backend qua Retrofit: interface Retrofit (`XApi`) chỉ khai báo endpoint và trả DTO; data source bọc nó và đổi lỗi HTTP/IO/serialization thành exception có kiểu của data layer. Retrofit `suspend` đã main-safe nên không bọc thêm `withContext`. Chỉ có một `Retrofit` và `OkHttpClient` dùng chung, dựng ở `NetworkModule`.
- Mỗi data source chỉ làm việc với một nguồn (một API, một DataStore/database/file). Việc phối hợp nhiều nguồn, giải quyết xung đột và logic nghiệp vụ thuộc repository.
- Repository nhận data source qua constructor và là điểm vào duy nhất của data layer; ViewModel, use case và Composable không giữ data source làm dependency hay tham số.
- Được gộp repository và data source vào một class khi chỉ có đúng một nguồn và không phụ thuộc repository khác; tách ra ngay khi có nguồn thứ hai (ví dụ phiên đăng nhập vừa lưu local vừa refresh qua mạng).
- Repository được phụ thuộc repository khác (ví dụ `AuthRepository` dùng `SessionRepository`), nhưng không tạo phụ thuộc vòng.
- Thao tác của nền tảng cần Activity hoặc hiện UI (Credential Manager, xin permission, Activity result) thuộc UI layer, không phải data source. UI lấy kết quả (ví dụ Google ID token), rồi đưa giá trị thuần cho ViewModel và repository.
- Mỗi interface data source cần ít nhất một implementation thật; fake chỉ phục vụ test (xem mục 11).

### Model

- Data source trả model của nguồn (DTO đánh dấu `internal`, có annotation serialization); repository map sang model của app. DTO không được lọt ra ngoài data layer.
- Logic phát sinh khi map (tính thời điểm hết hạn, chuẩn hóa dữ liệu) thuộc repository. Thời gian lấy từ `Clock` hoặc provider được inject, không gọi `System.currentTimeMillis()` trực tiếp trong logic.
- Model expose ra ngoài là immutable (`val`, `List` chỉ đọc). Model chứa token hoặc dữ liệu cá nhân override `toString()` để không lộ ra log.
- Cấu hình serialization nhận response từ backend phải chịu được trường mới (`ignoreUnknownKeys = true`), vì backend có thể thêm trường mà không báo trước.

### Dữ liệu thay đổi theo thời gian

- Dữ liệu có thể đổi sau khi đọc (phiên đăng nhập, cài đặt, danh sách đã lưu) expose bằng `Flow` lấy từ source of truth, ví dụ `val session: Flow<Session?>`, không chỉ có hàm đọc một lần. Thao tác ghi và đọc một lần dùng `suspend`.
- Dữ liệu cần tồn tại khi offline hoặc qua process death lấy nguồn local làm source of truth: DataStore cho dữ liệu nhỏ chỉ đọc/ghi nguyên khối, Room cho dữ liệu lớn hoặc cần truy vấn, file cho blob. Repository cập nhật nguồn local từ network; UI quan sát nguồn local.

### Lỗi

- Repository trả `Result<T>` cho thao tác một lần có lỗi dự kiến; luồng `Flow` dùng toán tử `catch`. Dùng một kiểu xử lý nhất quán trong toàn dự án.
- Lỗi dự kiến là exception có kiểu rõ ràng (ví dụ không có mạng, bị từ chối xác thực, server lỗi, dữ liệu sai định dạng) để ViewModel map thành UI state phù hợp. Không gộp mọi lỗi vào một trạng thái `Error` chung nếu UI cần phản ứng khác nhau.
- Chỉ bắt các loại lỗi dự kiến (`IOException`, `SerializationException`, `GeneralSecurityException`, …); không bắt `Exception`/`Throwable` rộng làm mất `CancellationException`.
- Lỗi không hiển thị chi tiết cho người dùng vẫn phải để lại dấu vết (log chỉ ở debug hoặc crash reporting) gồm loại exception, thao tác và mã trạng thái, không kèm body response, token hay dữ liệu cá nhân.

### Dispatcher, scope và vòng đời

- Inject `CoroutineDispatcher` qua Hilt qualifier (`@IoDispatcher`, `@DefaultDispatcher`) được cung cấp ở một nơi; không ghi trực tiếp `Dispatchers.IO` trong module hay class.
- Phân loại thao tác trước khi chọn scope:
  - Gắn với màn hình (ví dụ đăng nhập): chạy trong `viewModelScope` và bị hủy khi rời màn hình.
  - Cần sống lâu hơn màn hình (ví dụ refresh token, ghi cache): repository nhận một `@ApplicationScope CoroutineScope` qua constructor và chạy việc trong đó; không tự tạo `CoroutineScope` bên trong class.
  - Không được mất khi process bị kill: dùng WorkManager.
- Class giữ state trong bộ nhớ (cache) chọn scope DI theo thời gian cần sống: `@Singleton` nếu dùng toàn app, scope hẹp hơn nếu chỉ phục vụ một flow.

## 7. Lists, recomposition và performance

- Dùng `LazyColumn`/`LazyRow`/lazy grid cho danh sách dài hoặc lớn thay vì render mọi phần tử bằng `Column`/`Row`.
- Cấp `key` ổn định, duy nhất cho item trong lazy list; dùng `contentType` khi danh sách có nhiều loại item khác nhau.
- Tránh tính toán đắt tiền trong thân Composable; chỉ dùng `remember`/`derivedStateOf` khi có lý do cụ thể, không memoize mọi thứ theo thói quen.
- Đọc state càng gần nơi sử dụng càng tốt; với state thay đổi liên tục, áp dụng các kỹ thuật defer reads/lambda modifier khi phù hợp và sau khi đo lường.
- Không tối ưu dựa trên phỏng đoán; đo trên release-like build, dùng profiling/benchmark khi hiệu năng quan trọng.
- Với ứng dụng production hoặc luồng quan trọng, cân nhắc Baseline Profiles và Macrobenchmark theo nhu cầu.

## 8. Navigation, configuration và responsive UI

- Dùng một giải pháp navigation nhất quán với codebase; khi bắt đầu dự án mới, đánh giá Navigation 3 cùng nhu cầu và mức độ phù hợp, không tự ý migrate dự án hiện hữu chỉ vì convention.
- Điều hướng qua callback/route ở đúng tầng; tránh để reusable UI component tự sở hữu navigation toàn cục.
- `NavKey` của mỗi màn hình đặt trong file riêng (`LoginKey.kt`), không để lẫn trong file Composable của màn hình.
- Truyền ID hoặc dữ liệu tối thiểu qua route; tải dữ liệu đầy đủ từ repository tại màn hình đích khi phù hợp.
- Xử lý back navigation, deep links, trạng thái khôi phục và lỗi điều hướng theo yêu cầu ứng dụng.
- Dùng `dp` cho kích thước layout và `sp` cho text; tránh giả định mọi thiết bị có cùng kích thước, orientation hoặc font scale.
- Thiết kế adaptive cho tablet/foldable/multi-window khi nằm trong phạm vi sản phẩm; kiểm tra edge-to-edge và system bars theo cấu hình app hiện có.

## 9. Accessibility và UX

- Ưu tiên component Material/Compose chuẩn vì thường đã có semantics phù hợp; bổ sung semantics cho custom component khi cần.
- Icon có chức năng phải có nhãn dễ hiểu; icon trang trí không cung cấp thông tin nên dùng mô tả rỗng/ẩn phù hợp.
- Không dùng màu sắc làm tín hiệu duy nhất; đảm bảo contrast, focus, trạng thái disabled/error và kích thước tương tác phù hợp.
- Hỗ trợ font scaling, TalkBack, bàn phím/điều hướng hỗ trợ nếu phù hợp với loại UI.
- Dùng localized strings và plural resources; tránh ghép chuỗi thủ công khiến bản dịch không tự nhiên.
- Kiểm tra loading, empty, error, offline và permission-denied states đối với flow cần thiết.
- Thao tác cần mạng kiểm tra kết nối (`NetworkMonitor.isOnline`) trước khi mở bất kỳ luồng hệ thống nào (hộp chọn tài khoản, permission) hoặc gọi API; offline thì báo lỗi mất mạng ngay, không mở luồng đó. Thao tác không cần mạng (ví dụ tiếp tục với tư cách khách) không bị chặn. Kiểm tra này chỉ là lối thoát sớm: lỗi mạng xảy ra giữa chừng vẫn phải được xử lý bằng lỗi có kiểu của data layer.

## 10. Security và privacy

- Chỉ yêu cầu permission tối thiểu và giải thích đúng mục đích; không giả định permission đã được cấp.
- Dùng HTTPS/TLS; không tắt certificate validation trong production.
- Không hard-code API secret trong source, `BuildConfig`, assets hoặc APK; giá trị đóng gói trong app không phải secret an toàn.
- Định danh công khai (OAuth client ID, base URL của API) được đưa vào `BuildConfig` từ `local.properties` hoặc biến môi trường; không commit giá trị của môi trường riêng, và `local.properties` phải nằm trong `.gitignore`.
- Không ghi token, mật khẩu, dữ liệu cá nhân hoặc payload nhạy cảm vào log/crash message.
- Lưu dữ liệu nhạy cảm bằng cơ chế phù hợp với threat model; dùng Android Keystore cho key material khi thích hợp.
- Validate dữ liệu từ network, intent, deep link, clipboard, file và input người dùng trước khi tin dùng.
- Chỉ export component Android khi cần; bảo vệ component/deep link bằng permission và kiểm tra đầu vào phù hợp.
- Không tự triển khai cryptography; dùng API/thư viện chuẩn được duy trì.

## 11. Testing

- Unit test business logic, ViewModel, mapping và repository khi có logic đáng kiểm tra.
- Dùng fake/test double để test logic độc lập; unit test không phụ thuộc network thật hoặc backend bên ngoài.
- Fake data source đặt trong source set test (hoặc `debug` khi cố ý cần chạy app offline), không để trong `main`.
- Test không tự dựng lại cấu hình của production (`Json`, `OkHttpClient`, …); gọi chính provider hoặc factory của production (ví dụ `NetworkModule.provideJson()`) để cấu hình lệch bị bắt ngay ở test.
- Test data source mạng bằng MockWebServer với response có đủ các trường như backend thật, kể cả trường app không dùng.
- Dùng `kotlinx-coroutines-test` và test dispatcher/scheduler khi kiểm thử coroutine.
- Viết Compose UI test để kiểm tra semantics, hiển thị state và tương tác quan trọng; không phụ thuộc quá mức vào cấu trúc nội bộ của UI.
- Test các trạng thái chính: success, loading, empty, error và các trường hợp permission/offline nếu phù hợp.
- Ưu tiên selector có ý nghĩa (text, role, content description); chỉ dùng `testTag` khi cần định danh ổn định cho test.
- Dùng instrumented/E2E test cho các luồng người dùng quan trọng và tích hợp Android framework.
- Không tuyên bố test pass nếu chưa thực sự chạy; ghi rõ lệnh, kết quả và giới hạn môi trường.

## 12. Tooling, lint và quy trình thay đổi

- Dùng Kotlin formatter/style từ Android Studio/IntelliJ; thêm ktlint hoặc detekt nếu repository đã chọn hoặc team chủ động chuẩn hóa.
- Dùng Android Lint và xử lý lỗi/warning liên quan; không tắt rule toàn cục chỉ để làm CI xanh.
- Trước khi sửa, đọc code lân cận và Gradle configuration; ưu tiên giữ nguyên pattern đã có nếu không gây lỗi.
- Thay đổi nhỏ, có phạm vi rõ; tránh refactor không liên quan hoặc thêm dependency khi task không yêu cầu.
- Trước khi hoàn tất, chạy các Gradle task thích hợp với module/variant thực tế, ví dụ unit tests, lint và assemble; xác định đúng task từ project thay vì đoán.
- Báo cáo rõ phần đã đổi, kiểm tra đã chạy, kết quả và test nào chưa chạy.

## 13. Anti-pattern cần tránh

- Network/database call trong Composable.
- Composable nắm business logic lớn hoặc state mutable toàn cục.
- UI/ViewModel truy cập trực tiếp data source thay vì qua repository, kể cả khi data source được truyền vào dưới dạng tham số hàm.
- DTO của network/database lọt ra ngoài data layer.
- Một trạng thái `Error` chung cho mọi loại lỗi, hoặc `catch` mà không để lại dấu vết nào.
- `Dispatchers.IO` hoặc `CoroutineScope(...)` tạo ngay trong class thay vì được inject.
- Fake nằm trong `main`; test dựng lại cấu hình production thay vì dùng chính nó.
- Truyền đối tượng gắn với `Context`/Activity (hoặc lambda bao nó) vào hàm của ViewModel.
- Giữ lỗi hoặc thông báo trong UI state sau khi đã hiển thị, khiến nó hiện lại khi xoay màn hình.
- `launch` trong `init` của ViewModel, hoặc Composable "stateless" đọc `CompositionLocal` để gây side effect.
- `GlobalScope`, `runBlocking` trên đường UI, `!!` thiếu lý do hoặc `catch` rỗng.
- Public mutable state hoặc sửa trực tiếp dữ liệu nhận từ caller.
- Lazy list không có key ổn định khi item có identity.
- Hard-code strings/colors/spacing lặp lại ở nhiều nơi.
- Thêm tầng `UseCase`, module, abstraction hoặc thư viện mới khi chưa có lợi ích rõ ràng.
- Thay đổi navigation, DI, kiến trúc hoặc version catalog ngoài phạm vi task.

## Official references

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Android app architecture](https://developer.android.com/topic/architecture)
- [Recommendations for Android architecture](https://developer.android.com/topic/architecture/recommendations)
- [Data layer](https://developer.android.com/topic/architecture/data-layer)
- [Build an offline-first app](https://developer.android.com/topic/architecture/data-layer/offline-first)
- [Domain layer](https://developer.android.com/topic/architecture/domain-layer)
- [UI layer](https://developer.android.com/topic/architecture/ui-layer)
- [UI events](https://developer.android.com/topic/architecture/ui-layer/events)
- [UI state production](https://developer.android.com/topic/architecture/ui-layer/state-production)
- [Best practices for coroutines in Android](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
- [Now in Android (sample tham chiếu)](https://github.com/android/nowinandroid)
- [Compose UI architecture](https://developer.android.com/develop/ui/compose/architecture)
- [State and Jetpack Compose](https://developer.android.com/develop/ui/compose/state)
- [Where to hoist state](https://developer.android.com/develop/ui/compose/state-hoisting)
- [Compose side-effects](https://developer.android.com/develop/ui/compose/side-effects)
- [Compose component API guidelines](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-component-api-guidelines.md)
- [Compose performance best practices](https://developer.android.com/develop/ui/compose/performance)
- [Lazy lists and grids](https://developer.android.com/develop/ui/compose/lists)
- [Compose accessibility semantics](https://developer.android.com/develop/ui/compose/accessibility/semantics)
- [Compose UI testing](https://developer.android.com/develop/ui/compose/testing)
- [Android testing fundamentals](https://developer.android.com/training/testing/fundamentals)
- [Android security best practices](https://developer.android.com/privacy-and-security/security-best-practices)
- [Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles)
