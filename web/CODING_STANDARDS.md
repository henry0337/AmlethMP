# Coding Standards — React / Next.js

> Phạm vi mặc định: Next.js App Router + React + TypeScript. Điều chỉnh theo phiên bản framework và kiến trúc thực tế của dự án.

## 1. Nguyên tắc chung

- Ưu tiên code dễ đọc, dễ kiểm thử và nhất quán; tránh trừu tượng hóa quá sớm.
- Phân biệt quy tắc bắt buộc của framework với quy ước riêng của team.
- Ưu tiên cấu hình tự động để kiểm tra style và chất lượng code.
- Không thêm thư viện, lớp trừu tượng hoặc folder nếu chưa có nhu cầu rõ ràng.

## 2. Cấu trúc dự án

- Dùng `app/` cho routing, layouts và các file convention của Next.js; có thể đặt trong `src/app/` nếu dự án chọn cấu trúc `src/`.
- Giữ đúng tên các file đặc biệt của Next.js, ví dụ `page.tsx`, `layout.tsx`, `loading.tsx`, `error.tsx`, `not-found.tsx` và `route.ts`.
- Dùng Route Groups như `(dashboard)` để tổ chức route mà không làm thay đổi URL.
- Dùng `components/ui/` cho UI dùng chung, ít phụ thuộc nghiệp vụ.
- Nhóm code theo feature khi phù hợp, ví dụ `features/products/{components,hooks,schemas,services,types,utils}`.
- Đặt tiện ích và hạ tầng dùng chung như database, auth và API client trong `lib/`.
- Đặt type gần nơi sử dụng; chỉ đưa vào `types/` khi thực sự dùng chung.
- Không tạo trước hàng loạt thư mục trống cho những chức năng chưa tồn tại.

## 3. Naming convention

- Đặt tên component và type/interface bằng `PascalCase`, ví dụ `ProductCard`, `ProductCardProps`.
- Đặt tên biến, function và custom Hook bằng `camelCase`.
- Custom Hook phải bắt đầu bằng `use`, ví dụ `useProductFilters`.
- Đặt tên boolean theo trạng thái hoặc khả năng, ví dụ `isLoading`, `hasPermission`, `canEdit`.
- Dùng `UPPER_SNAKE_CASE` cho hằng số cấu hình thực sự ổn định, không phải mọi biến khai báo bằng `const`.
- Dùng hậu tố có ý nghĩa như `Props`, `Dto`, `Schema` hoặc `Repository` khi cần phân biệt vai trò.
- Chọn một quy ước tên file component (`PascalCase` hoặc `kebab-case`) và áp dụng nhất quán; không đổi tên các file đặc biệt của Next.js.
- Đặt tên test thống nhất, ví dụ `ProductCard.test.tsx`.

## 4. Import và export

- Ưu tiên ES Modules (`import` / `export`) trong code ứng dụng.
- Dùng alias như `@/` cho import nội bộ khi dự án đã cấu hình alias.
- Thống nhất thứ tự import và để ESLint/công cụ phù hợp tự động sắp xếp nếu có thể.
- Dùng `import type` cho import chỉ phục vụ TypeScript khi phù hợp với cấu hình dự án.
- Dùng `named export` nhất quán cho component và utility thông thường nếu team chọn quy ước này.
- Tuân thủ yêu cầu export của Next.js: các file route như `page.tsx` và `layout.tsx` cần export mặc định component phù hợp.

## 5. React component và JSX

- Ưu tiên function component cho code mới.
- Đặt tên component theo vai trò và trách nhiệm rõ ràng.
- Tách component khi UI hoặc logic có thể tái sử dụng, có trách nhiệm độc lập hoặc làm component hiện tại khó hiểu.
- Không bắt buộc mỗi thẻ HTML hoặc mỗi đoạn JSX nhỏ phải thành một component riêng.
- Xem props là dữ liệu chỉ đọc; không mutate props hoặc dữ liệu đầu vào.
- Dùng `key` ổn định, ưu tiên ID dữ liệu; tránh key ngẫu nhiên và hạn chế dùng array index cho danh sách có thể thay đổi.
- Giữ JSX dễ đọc; tách điều kiện hoặc biểu thức lồng nhau quá phức tạp thành biến/function/component có tên rõ ràng.
- Không lưu vào state dữ liệu có thể tính trực tiếp từ props/state khác, trừ khi có lý do rõ ràng.
- Tránh đưa side effect vào lúc render; render nên giữ tính thuần.

## 6. Props, `type` và `interface`

- Có thể dùng cả `type` hoặc `interface` để mô tả Props dạng object đơn giản; chọn một convention mặc định cho team.
- Quy ước đề xuất: dùng `interface` cho Props dạng object thông thường hoặc contract cần mở rộng bằng `extends`.
- Dùng `type` cho union, discriminated union, intersection phức tạp hoặc kết quả từ `Pick`, `Omit`, `Partial` và utility types khác.
- Không chuyển toàn bộ `type` sang `interface` hoặc ngược lại chỉ để đồng bộ nếu không tạo ra lợi ích thực tế.
- Khai báo rõ các prop tùy chọn bằng `?`; tránh dùng kiểu quá rộng nếu dữ liệu có thể được mô tả chính xác hơn.
- Không cần dùng `React.FC` cho mọi component; khai báo kiểu Props trực tiếp là đủ.

## 7. React Hooks và state

- Tuân thủ Rules of Hooks: chỉ gọi Hooks ở cấp cao nhất của function component hoặc custom Hook; không gọi trong điều kiện, vòng lặp hay function lồng nhau.
- Không gọi Hooks sau nhánh return có điều kiện; giữ thứ tự gọi Hook ổn định.
- Cập nhật state qua setter hoặc API tương ứng; không mutate state trực tiếp.
- Dùng functional update khi giá trị mới phụ thuộc giá trị state trước đó.
- Dùng `useEffect` cho side effect cần đồng bộ với hệ thống bên ngoài; không dùng Effect chỉ để tính dữ liệu có thể suy ra khi render.
- Khai báo dependency của Effect đầy đủ và xử lý cleanup cho subscription, timer hoặc tài nguyên khi cần.
- Chỉ dùng `useMemo` và `useCallback` khi có lý do về hiệu năng hoặc ổn định tham chiếu; không memoize máy móc.

## 8. TypeScript

- Bật `strict` nếu tương thích với codebase và sửa dần các lỗi kiểu thay vì vô hiệu hóa kiểm tra hàng loạt.
- Ưu tiên `unknown` thay vì `any` khi chưa biết kiểu; validate/narrow kiểu trước khi sử dụng.
- Tránh `as any`, non-null assertion (`!`) và type assertion không có căn cứ.
- Tận dụng type inference; chỉ khai báo kiểu tường minh khi giúp rõ API hoặc tránh suy luận không chính xác.
- Dùng discriminated union để mô hình hóa các trạng thái loại trừ nhau khi phù hợp.
- Dùng `type` và `interface` theo mục đích; không coi cái nào luôn tốt hơn trong mọi trường hợp.
- Không giả định TypeScript tự kiểm tra dữ liệu runtime từ API, form, URL hoặc dịch vụ bên ngoài.

## 9. Next.js: Server/Client Components và routing

- Trong App Router, ưu tiên Server Components theo mặc định.
- Chỉ thêm `'use client'` ở component cần state, event handler, Effects hoặc API trình duyệt.
- Đặt ranh giới Client Component gần phần giao diện cần tương tác; tránh biến cả cây component thành client chỉ vì một phần nhỏ.
- Không import database client, secret hoặc module chỉ chạy phía server vào Client Component.
- Không truy cập `window`, `document` hoặc `localStorage` trong quá trình render phía server.
- Dùng `next/link` cho điều hướng nội bộ và `next/image` cho ảnh khi phù hợp.
- Cân nhắc `loading.tsx`, `error.tsx` và `not-found.tsx` cho các route cần trạng thái tải, lỗi và không tìm thấy.
- Dùng Metadata API hoặc các file metadata của Next.js khi cần SEO/chia sẻ.
- Với cấu hình và hành vi phụ thuộc phiên bản Next.js, kiểm tra tài liệu đúng phiên bản đang dùng.

## 10. Data fetching, forms và API

- Ưu tiên truy xuất dữ liệu trực tiếp từ tầng dữ liệu trong Server Component thay vì gọi vòng qua Route Handler nội bộ khi không cần thiết.
- Chạy các request độc lập song song khi an toàn để tránh waterfall không cần thiết.
- Xác định cache, revalidation và dynamic rendering theo độ mới, quyền riêng tư và yêu cầu nghiệp vụ; không mặc định cache mọi dữ liệu.
- Tránh để dữ liệu cá nhân hoặc dữ liệu theo người dùng bị cache/chia sẻ ngoài ý muốn.
- Validate dữ liệu ở server; client-side validation chỉ bổ trợ trải nghiệm người dùng.
- Với Server Actions và Route Handlers, xác minh dữ liệu đầu vào, authentication và authorization tại nơi thực hiện thao tác.
- Chuẩn hóa HTTP status code và cấu trúc lỗi; không gửi stack trace hoặc thông tin nội bộ nhạy cảm cho client.
- Chỉ trả về những trường dữ liệu cần thiết cho giao diện.

## 11. Security

- Không đặt secret trong code hoặc commit file môi trường có thông tin nhạy cảm.
- Coi biến `NEXT_PUBLIC_*` là dữ liệu công khai, không dùng để chứa secret.
- Không coi việc ẩn nút hoặc giới hạn điều hướng trên UI là biện pháp phân quyền.
- Kiểm tra quyền truy cập ở server cho từng thao tác nhạy cảm và từng tài nguyên được yêu cầu.
- Xem dữ liệu từ request, form, URL và dịch vụ ngoài là không đáng tin cậy cho đến khi được kiểm tra.
- Tách log chẩn đoán nội bộ khỏi thông báo lỗi gửi cho người dùng.

## 12. Styling, accessibility và performance

- Chọn một chiến lược styling chính và thống nhất cách đặt tên class, global styles, theme và design tokens.
- Dùng HTML semantic: `button` cho hành động, `a`/`Link` cho điều hướng và heading theo cấu trúc hợp lý.
- Đảm bảo form control có label; ảnh có `alt` phù hợp; thành phần tương tác hỗ trợ bàn phím và có focus indicator.
- Chỉ dùng ARIA khi HTML semantic chưa diễn đạt đủ ý nghĩa.
- Tối ưu ảnh, font, third-party scripts và bundle theo nhu cầu thực tế.
- Đo lường hiệu năng trước khi tối ưu sâu; không thêm memoization hoặc thư viện lớn nếu chưa có lý do.

## 13. Testing và công cụ chất lượng

- Viết unit test cho logic độc lập, component/integration test cho tương tác quan trọng và E2E test cho các luồng nghiệp vụ cốt lõi.
- Đặt tên test theo hành vi quan sát được, không phụ thuộc quá mức vào chi tiết triển khai nội bộ.
- Kiểm tra các trạng thái quan trọng như thành công, loading, validation error và quyền truy cập khi phù hợp.
- Dùng ESLint để kiểm tra lỗi code, Rules of Hooks, TypeScript và accessibility theo nhu cầu dự án.
- Dùng một formatter thống nhất, ví dụ Prettier, để tự động hóa quy tắc định dạng.
- Cấu hình scripts/CI để chạy lint, typecheck, test và build trước khi merge.

## 14. Git và code review

- Thống nhất quy ước tên branch và commit message trong team.
- Không commit secret, file sinh ra từ build hoặc artifact không cần thiết.
- Pull request cần mô tả mục đích, thay đổi chính, rủi ro và cách kiểm thử.
- Không bỏ qua lỗi lint, typecheck, test hoặc build chỉ để merge nhanh; ngoại lệ phải được giải thích rõ.
- Review ưu tiên tính đúng đắn, bảo mật, khả năng bảo trì và trải nghiệm người dùng trước khác biệt style nhỏ đã được formatter xử lý.

## 15. Tài liệu tham khảo

- React Rules: <https://react.dev/reference/rules>
- TypeScript Handbook: <https://www.typescriptlang.org/docs/handbook/intro.html>
- Next.js App Router: <https://nextjs.org/docs/app>
- Next.js Production Checklist: <https://nextjs.org/docs/app/guides/production-checklist>
- ESLint Configuration: <https://eslint.org/docs/latest/use/configure/>
