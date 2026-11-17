# FastAPI + Python Coding Standards

> Mục tiêu: code dễ đọc, có type rõ ràng, dễ kiểm thử và an toàn. Đây là chuẩn mặc định; ưu tiên convention hiện có của repository nếu không gây lỗi hoặc rủi ro bảo mật.

## 1. Python style và naming

- Tuân thủ PEP 8; dùng 4 spaces, không dùng tab để thụt lề.
- Dùng `snake_case` cho module, package, function và biến; `PascalCase` cho class; `UPPER_SNAKE_CASE` cho hằng số.
- Đặt tên thể hiện ý nghĩa nghiệp vụ; tránh tên mơ hồ như `data`, `tmp`, `obj` nếu có thể cụ thể hơn.
- Giữ function/class tập trung vào một trách nhiệm; tách khi điều đó cải thiện độ rõ ràng, không tách máy móc theo số dòng.
- Thêm docstring cho public module/class/function khi mục đích hoặc contract chưa rõ; comment giải thích lý do, không lặp lại code.
- Dùng f-string cho nội suy chuỗi; không dùng bare `except:`.
- Không sửa code không liên quan tới phạm vi task nếu không cần thiết.

## 2. Type hints và Python features

- Thêm type hints cho public function, service/repository boundary và dữ liệu phức tạp.
- Ưu tiên type cụ thể; tránh `Any` nếu chưa có lý do và không dùng `type: ignore` thiếu giải thích.
- Với Python 3.10+, ưu tiên `X | None`, `list[str]`, `dict[str, int]`; phải phù hợp `requires-python` của dự án.
- Dùng `Enum`, `Literal`, dataclass hoặc TypedDict khi làm rõ contract; không tạo abstraction chỉ để thêm type.
- Không dùng mutable object làm default argument; dùng `None` hoặc `default_factory` phù hợp.
- Xử lý `None` và lỗi tường minh; tránh `assert` để validate input ở runtime production.

## 3. FastAPI routes và API contract

- Nhóm endpoint theo domain bằng `APIRouter`; giữ `main.py`/application factory gọn.
- Route handler nên điều phối HTTP concerns; chuyển nghiệp vụ phức tạp sang service/domain function.
- Dùng `Annotated[..., Depends(...)]` cho dependency injection khi phù hợp với phiên bản và style của dự án.
- Khai báo request/response schemas rõ ràng bằng Pydantic; đặt `response_model` hoặc return annotation có chủ đích cho endpoint.
- Không trả trực tiếp ORM object hoặc internal model nếu điều đó có thể làm lộ trường nội bộ; định nghĩa response DTO/schema.
- Dùng HTTP method, status code và error response đúng ngữ nghĩa; không trả `200 OK` cho mọi tình huống.
- Validate pagination, sorting, filtering và giới hạn kích thước input; đặt default và maximum rõ ràng.
- Giữ API docs có ý nghĩa: summary, description, tags và response status khi giúp client sử dụng API.
- Không đổi contract API hoặc tên field công khai mà không xem xét backward compatibility.

## 4. Pydantic và configuration

- Với code mới, ưu tiên Pydantic v2; theo phiên bản đã pin trong repo và không trộn APIs v1/v2 tùy tiện.
- Tách schema đầu vào, đầu ra và persistence model khi trách nhiệm khác nhau (ví dụ `UserCreate`, `UserRead`, `UserUpdate`).
- Dùng validators cho quy tắc xác thực gắn với schema; đưa quy tắc nghiệp vụ phức tạp vào service/domain layer.
- Không mặc định tin dữ liệu từ request, environment, file hoặc external API; validate tại ranh giới hệ thống.
- Đọc cấu hình qua một Settings/config module; dùng `pydantic-settings` nếu dự án đã chọn hệ sinh thái Pydantic.
- Không hardcode secret, API key, credential hoặc environment-specific URL trong source code.

## 5. Architecture và dependency injection

- Tổ chức code theo domain/feature; dùng cấu trúc đơn giản trước, chỉ thêm layer khi có trách nhiệm thực sự.
- Giữ hướng phụ thuộc rõ ràng: router/API layer không nên chứa truy vấn DB phức tạp; business logic không phụ thuộc trực tiếp vào HTTP request nếu có thể tránh.
- Dùng FastAPI `Depends` để cung cấp dependency có vòng đời rõ ràng như current user, settings hoặc database session.
- Không tạo repository/service interface cho mọi class theo kiểu máy móc; lựa chọn mức tách phù hợp độ phức tạp và khả năng test.
- Dùng application lifespan cho tài nguyên dùng chung cần khởi tạo/dọn dẹp (connection pool, client, model); đảm bảo cleanup đúng cách.
- Tránh import vòng tròn; không đặt logic có side effect nặng ở cấp module khi import.

## 6. Async, I/O và database

- Chỉ dùng `async def` khi các thao tác I/O được gọi hỗ trợ async hoặc không có công việc blocking cần chạy đồng bộ.
- Không gọi blocking I/O, `time.sleep()` hoặc thư viện đồng bộ nặng trực tiếp trên event loop; chọn API async hoặc đưa công việc blocking vào thread/process phù hợp.
- Với SQLAlchemy, tuân thủ phiên bản đang dùng; code mới theo SQLAlchemy 2.x style nếu dự án cho phép.
- Mỗi request/task cần session scope rõ ràng; không chia sẻ một `AsyncSession` đồng thời giữa nhiều task.
- Dùng transaction rõ ràng cho các thao tác ghi; commit/rollback có chủ đích và không giữ transaction lâu hơn cần thiết.
- Dùng parameterized query/ORM expressions; không ghép chuỗi SQL từ dữ liệu đầu vào.
- Dùng migration có version control (ví dụ Alembic) khi schema DB thay đổi; không dựa vào thao tác thủ công không được ghi nhận.
- Tránh N+1 queries; chỉ eager-load, batch hoặc thêm index khi dữ liệu và truy vấn cho thấy cần thiết.
- Không dùng `asyncio.create_task()` cho công việc quan trọng mà không quản lý vòng đời, lỗi và shutdown.

## 7. Error handling, logging và observability

- Dùng `HTTPException` hoặc exception handler phù hợp tại HTTP boundary; không đưa HTTP-specific exception sâu vào domain layer nếu có thể tránh.
- Trả lỗi nhất quán, hữu ích cho client nhưng không chứa stack trace, secret, SQL, token hoặc dữ liệu cá nhân không cần thiết.
- Bắt những exception có thể xử lý; không nuốt lỗi bằng `except Exception: pass`.
- Dùng `logging` hoặc logger chuẩn của dự án; không dùng `print()` làm logging production.
- Log theo cấu trúc nếu hệ thống đã hỗ trợ; thêm request/correlation ID khi phù hợp.
- Không log password, access token, authorization header, secret hoặc payload nhạy cảm.
- Chuyển lỗi dependency bên ngoài thành lỗi phù hợp, đồng thời giữ nguyên thông tin chẩn đoán trong log nội bộ.

## 8. Security

- Xác thực danh tính và kiểm tra quyền truy cập tài nguyên ở server; không coi việc ẩn UI hoặc endpoint trong docs là kiểm soát truy cập.
- Mặc định mọi input bên ngoài là không đáng tin; validate kiểu, độ dài, range và format.
- Chỉ trả các field cần thiết; áp dụng least privilege cho DB, service account và API token.
- Quản lý secret qua environment/secret manager; không commit `.env` có secret và không đưa bí mật vào log.
- Cấu hình CORS rõ ràng cho origin/method/header cần thiết; không dùng wildcard credentials trong production.
- Dùng HTTPS ở môi trường triển khai; cấu hình cookie/token/session theo threat model của ứng dụng.
- Dùng password hashing chuyên dụng và thuật toán hiện hành; không lưu hoặc log mật khẩu dạng plaintext.
- Cân nhắc rate limiting, timeout, giới hạn upload/request body và quota cho endpoint dễ bị lạm dụng.
- Giữ dependency được cập nhật; review rủi ro bảo mật trước khi thêm package mới.

## 9. Testing

- Dùng pytest; đặt test trong `tests/` hoặc theo convention hiện tại của repo.
- Đặt tên file `test_*.py` và tên test mô tả hành vi/điều kiện/kết quả.
- Viết unit test cho logic nghiệp vụ và integration/API test cho status code, response schema, validation và auth.
- Dùng fixtures để quản lý app/client/test data; dọn tài nguyên sau test và cô lập dữ liệu giữa các test.
- Test happy path, boundary cases, invalid input, not-found, permission-denied và error paths phù hợp với endpoint.
- Dùng `TestClient` cho test đồng bộ; với async tests, dùng công cụ async được dự án cấu hình và tránh trộn event loop không chủ đích.
- Mock network/third-party service ở unit test; không mock quá mức các thành phần mà integration test cần xác minh.
- Không phụ thuộc thứ tự chạy test; không dùng production data hoặc credential thật trong test.

## 10. Dependencies, tooling và CI

- Ưu tiên `pyproject.toml` cho project metadata và tool configuration; giữ dependency/lock file đồng bộ với package manager thực tế (`uv`, Poetry, PDM hoặc pip-tools).
- Không tự ý đổi package manager, Python version hoặc framework major version trong một task không liên quan.
- Dùng một formatter/linter chính; lựa chọn mặc định gợi ý là Ruff cho lint/format.
- Dùng mypy hoặc Pyright khi dự án cần static type checking; cấu hình mức kiểm tra nhất quán thay vì bật/tắt tùy tiện.
- CI nên chạy lint, format check, type check (nếu dùng), tests và build/package checks phù hợp.
- Không thêm công cụ hoặc dependency mới nếu công cụ sẵn có đã đáp ứng yêu cầu.
- Kiểm tra cấu hình và scripts hiện có trước khi chạy hoặc đề xuất lệnh.

## 11. Gợi ý cấu trúc dự án

```text
app/
├── main.py              # tạo FastAPI app / include routers
├── core/                # settings, logging, security, cross-cutting concerns
├── api/
│   ├── deps.py           # dependencies dùng chung
│   └── routes/           # APIRouter theo domain
├── schemas/              # Pydantic request/response models nếu dùng chung
├── services/             # nghiệp vụ nếu dự án cần layer này
├── repositories/         # truy cập dữ liệu nếu dự án cần layer này
├── models/               # ORM/persistence models
└── db/                   # engine, session, migrations integration

 tests/
```

- Đây là gợi ý, không phải cấu trúc bắt buộc; với dự án lớn, cân nhắc nhóm theo domain/feature.
- Tránh tạo mọi folder/layer ngay từ đầu nếu chưa có nhu cầu thực tế.

## 12. Checklist trước khi hoàn tất task

- [ ] Code theo style và naming nhất quán với repository.
- [ ] Request/response contracts và validation được cập nhật đúng.
- [ ] Kiểm tra authorization và không làm lộ dữ liệu nhạy cảm.
- [ ] Có test phù hợp với thay đổi.
- [ ] Chạy các lệnh lint/type-check/test có sẵn hoặc báo rõ lệnh chưa chạy và lý do.
- [ ] Không đưa secret, file tạm hoặc thay đổi ngoài phạm vi vào commit.
- [ ] Tóm tắt thay đổi, kiểm tra đã chạy và hạn chế còn lại.

## Tài liệu tham chiếu

- [FastAPI documentation](https://fastapi.tiangolo.com/)
- [FastAPI — Bigger Applications](https://fastapi.tiangolo.com/tutorial/bigger-applications/)
- [FastAPI — Dependencies](https://fastapi.tiangolo.com/tutorial/dependencies/)
- [FastAPI — Concurrency and async/await](https://fastapi.tiangolo.com/async/)
- [FastAPI — Response Model](https://fastapi.tiangolo.com/tutorial/response-model/)
- [Python PEP 8](https://peps.python.org/pep-0008/)
- [Python typing](https://docs.python.org/3/library/typing.html)
- [Pydantic documentation](https://docs.pydantic.dev/latest/)
- [SQLAlchemy asyncio](https://docs.sqlalchemy.org/en/20/orm/extensions/asyncio.html)
- [pytest documentation](https://docs.pytest.org/en/stable/)
- [Ruff documentation](https://docs.astral.sh/ruff/)
