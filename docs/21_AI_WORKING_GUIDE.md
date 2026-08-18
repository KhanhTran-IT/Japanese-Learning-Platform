# AI_WORKING_GUIDE - Cách vibe code hiệu quả với project này

## Nguyên tắc chính

AI chỉ nên được giao một task nhỏ mỗi lần. Mỗi task cần có bối cảnh, module, database liên quan, API liên quan, quy chuẩn code và đầu ra mong muốn.

## Quy trình dùng AI cho mỗi chức năng

1. Gửi `00_MASTER_CONTEXT.md`.
2. Gửi file module liên quan trong `05_features`.
3. Gửi file database liên quan trong `07_database`.
4. Gửi file API liên quan trong `08_api`.
5. Gửi `18_CODE_CONVENTIONS.md` và `12_SECURITY_CHECKLIST.md`.
6. Yêu cầu AI phân tích trước khi code.
7. Chỉ code từng lớp/file: Entity → Repository → DTO → Mapper → Service → Controller → Test.
8. Sau khi code xong, dùng prompt review.

## Công thức prompt chuẩn

```text
Bạn hãy đóng vai Senior Fullstack Developer kiêm System Architect.

Tôi đang làm project Japanese Learning Platform.
Stack:
- Backend: Java Spring Boot
- Frontend: Vue 3
- Database: MySQL
- Auth: JWT
- Kiến trúc: Modular Monolith

Module hiện tại: [tên module]
Tài liệu liên quan tôi đã gửi:
- [file feature]
- [file database]
- [file api]
- [file convention]

Yêu cầu nghiệp vụ:
- [yêu cầu 1]
- [yêu cầu 2]
- [yêu cầu 3]

Ràng buộc:
- Không trả Entity trực tiếp ra API
- Dùng DTO request/response
- Có validate
- Có GlobalExceptionHandler
- Có phân quyền rõ ràng
- Code dễ mở rộng, phù hợp production

Hãy làm theo thứ tự:
1. Phân tích nghiệp vụ
2. Kiểm tra database/API đã đủ chưa
3. Đề xuất luồng xử lý
4. Sau khi phân tích xong mới viết code từng file
5. Giải thích cách test bằng Postman/Swagger
6. Tự review lỗi bảo mật và lỗi mở rộng
```

## Thứ tự làm backend khuyên dùng

1. common response + exception + validation
2. auth/user/role
3. course/section/lesson
4. enrollment/progress
5. order/payment/coupon
6. quiz/question/answer/attempt
7. flashcard
8. game/gamification
9. notification
10. cms/admin/report

## Thứ tự làm frontend khuyên dùng

1. layout + router + guard
2. axios service + interceptor
3. auth store
4. public pages: home, course list, course detail
5. student pages: my courses, learning page, progress
6. admin pages: dashboard, user, course, lesson
7. payment flow
8. quiz/flashcard/game pages

## Câu lệnh review code sau khi AI sinh code

```text
Hãy review nghiêm khắc phần code vừa viết theo các tiêu chí:
1. Có lỗi bảo mật không?
2. Có lỗi phân quyền không?
3. Có trả Entity trực tiếp không?
4. Có thiếu validate không?
5. Có lỗi transaction không?
6. Có khả năng phát sinh N+1 query không?
7. Có khó mở rộng không?
8. Có cần tách DTO/Mapper/Service nhỏ hơn không?
9. Có test case nào bắt buộc phải có?
10. Có điểm nào chưa phù hợp production không?
```
