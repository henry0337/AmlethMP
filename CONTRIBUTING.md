# Hướng dẫn đóng góp (Contributing)

Tài liệu này mô tả cách làm việc chung trong repository AmlethMP. Hãy đọc trước khi bắt đầu.

## Quy trình làm việc

```text
Issue → Branch → Commit → Pull Request → Review → Merge
```

1. **Tạo hoặc nhận một issue.** Mọi thay đổi nên bắt nguồn từ một issue. Dùng đúng template: Bug Report, Feature Request, Technical Task hoặc Documentation.
2. **Tạo branch từ `master`** theo quy ước bên dưới.
3. **Code và commit** theo quy ước commit.
4. **Mở Pull Request** vào `master`, điền đủ template và liên kết issue bằng `Closes #<số issue>`.
5. **Chờ review.** Sửa theo góp ý, sau đó merge khi đã được approve.

Không push trực tiếp lên `master`.

## Quy ước tên branch

Định dạng: `<loại>/<mô-tả-ngắn>`, viết thường, nối bằng dấu gạch ngang.

| Loại | Dùng cho | Ví dụ |
|---|---|---|
| `feat` | Tính năng mới | `feat/login-screen` |
| `fix` | Sửa lỗi | `fix/player-crash-on-pause` |
| `docs` | Tài liệu | `docs/setup-guide` |
| `refactor` | Cải tổ code, không đổi hành vi | `refactor/auth-service` |
| `chore` | Cấu hình, tooling, dependency | `chore/update-gradle` |

## Quy ước commit

Dùng [Conventional Commits](https://www.conventionalcommits.org/):

```text
<loại>(<phạm vi>): <mô tả ngắn>
```

- **Loại:** `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `style`, `perf`.
- **Phạm vi (tùy chọn):** `android`, `web`, `windows`, `linux`, `backend`, `design`.
- **Mô tả:** viết thường, ngắn gọn, không có dấu chấm cuối, mô tả điều commit làm.

Ví dụ:

```text
feat(android): add login screen
fix(backend): handle expired refresh token
docs: add contributing guide
```

## Pull Request

- Mỗi PR chỉ nên làm **một việc**, đủ nhỏ để review dễ dàng.
- Tiêu đề PR theo cùng quy ước commit.
- Điền đầy đủ PR template, nhất là mục liên kết issue và checklist.
- Với thay đổi giao diện, đính kèm screenshot hoặc video.

## Quy ước theo từng phần

- **`backend/`:** tuân thủ [`backend/CODING_STANDARDS.md`](backend/CODING_STANDARDS.md).
- **`design/`:** là nguồn chuẩn (source of truth) cho giao diện của mọi platform. Khi code UI, không tự ý thêm hoặc bỏ thành phần giao diện. Nếu thấy thiết kế thiếu hoặc mâu thuẫn với code, hãy tạo issue để báo thay vì tự sửa thiết kế.
- **`android/`, `web/`, `windows/`, `linux/`:** giao diện phải bám theo `design/`.

## Báo lỗi và đề xuất

- Gặp lỗi: tạo issue bằng **Bug Report**.
- Có ý tưởng: tạo issue bằng **Feature Request**.
- Công việc kỹ thuật nội bộ: tạo issue bằng **Technical Task**.

Hãy tìm trong các issue hiện có trước khi tạo issue mới để tránh trùng lặp.
