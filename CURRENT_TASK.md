# CURRENT TASK

## Task hiện tại

Frontend Foundation (Vue 3 + Vite + API Client + Router + Pinia)

## Trạng thái

TODO

## Mục tiêu

Thiết lập nền tảng frontend cho hệ thống, bao gồm cấu trúc thư mục Vue 3, routing cơ bản, store quản lý auth, service gọi API bằng Axios, và cấu hình môi trường phát triển. Task này là bước chuẩn bị cho các màn hình đăng nhập, khóa học, học bài và dashboard học viên.

## Vì sao làm task này?

Sau khi backend auth/course/learning đã có các API cốt lõi, frontend cần một nền tảng vững chắc để kết nối được với backend và mở rộng nhanh. Nếu không có foundation đúng, các màn hình sau sẽ khó maintain và dễ lặp logic.

## Không làm trong task này

- Không làm toàn bộ trang auth hoàn chỉnh.
- Không làm trang khóa học chi tiết đầy đủ.
- Không làm dashboard học viên UI hoàn chỉnh.
- Không làm quiz, payment, gamification.

## File tài liệu cần dùng

- docs/10_FRONTEND_STRUCTURE.md
- docs/11_BACKEND_FRONTEND_CONFIG.md
- docs/25_SCREEN_LIST.md
- docs/26_API_PRIORITY.md

## Cần tạo hoặc chỉnh sửa

- frontend/package.json và cấu hình dependencies
- frontend/src/main.js
- frontend/src/App.vue
- frontend/src/router/index.js
- frontend/src/stores/auth.store.js
- frontend/src/services/api.js
- frontend/.env.development
- frontend/src/layouts và cấu trúc folder ban đầu

## Logic xử lý

- Khởi tạo ứng dụng Vue 3 bằng Vite.
- Cấu hình router cơ bản cho các route public/auth/student/admin.
- Tạo Pinia store cho auth state.
- Tạo Axios instance với base URL và interceptor cho token.
- Chuẩn hóa cấu hình môi trường để gọi backend ở localhost.

## Checklist

- [ ] Khởi tạo frontend project và cài dependencies cần thiết
- [ ] Thiết lập router cơ bản và layout ban đầu
- [ ] Thiết lập Pinia store cho auth
- [ ] Thiết lập Axios client và interceptor
- [ ] Cấu hình .env cho môi trường development
- [ ] Chạy được frontend trên localhost

## Cách test sau khi hoàn thành

1. Chạy `npm install` và `npm run dev`.
2. Mở trình duyệt và kiểm tra ứng dụng load thành công.
3. Kiểm tra router có điều hướng đúng giữa các route cơ bản.
4. Kiểm tra request API có đi qua Axios client đúng base URL.

## Kết quả mong muốn

Frontend có thể chạy ổn định, có cấu trúc rõ ràng, có store/router/service cơ bản để tiếp tục phát triển các màn hình tiếp theo.
