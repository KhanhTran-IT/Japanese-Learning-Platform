# CURRENT TASK

## Task hiện tại
MVP P0 End-to-End Demo Smoke Test & Hardening

## Trạng thái
TODO

## Mục tiêu
Chốt lại toàn bộ luồng P0 của MVP bằng cách chạy smoke test end-to-end từ guest/admin/student, ghi nhận lỗi còn tồn tại và chỉ fix các lỗi nhỏ/blocker ảnh hưởng trực tiếp tới demo.

## Vì sao làm task này?
Các phần P0 chính đã được xây dựng: auth, public course, admin course/section/lesson/resource, enrollment, student dashboard/my courses, lesson learning, progress, curriculum navigation và profile. Trước khi chuyển sang quiz hoặc payment ở P1, cần một task hardening để đảm bảo demo MVP chạy xuyên suốt, không bị đứt ở routing, permission, API contract hoặc UI state.

## Không làm trong task này
- Không làm quiz.
- Không làm payment/order.
- Không làm upload file thật.
- Không thêm feature lớn mới.
- Không redesign toàn bộ UI.
- Không refactor kiến trúc lớn.
- Không đổi database schema nếu không bắt buộc.
- Không làm P1/P2/P3 nếu không phải blocker của demo P0.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/30_PERMISSION_MATRIX.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/32_SEED_DATA.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/05_features/05_03_LEARNING_PROGRESS_FEATURES.md`
- `docs/07_database/07_01_AUTH_USER.md`
- `docs/07_database/07_02_COURSE_LESSON.md`
- `docs/08_api/08_01_AUTH_API.md`
- `docs/08_api/08_02_USER_API.md`
- `docs/08_api/08_03_COURSE_PUBLIC_API.md`
- `docs/08_api/08_04_LESSON_API.md`

## Vấn đề hiện tại
- Nhiều module P0 đã được làm theo từng task nhỏ, nhưng chưa có một lượt smoke test tổng thể.
- Có thể còn lỗi nối luồng giữa các màn hình: login redirect, public course -> enroll -> my courses -> lesson learning -> profile.
- Có thể còn endpoint/API contract lệch nhẹ giữa frontend và backend.
- Có thể còn permission rule chưa khớp giữa `SecurityConfig`, route guard và UI.
- Một số test/backend package có thể bị blocker môi trường Mockito/Byte Buddy, cần ghi rõ nếu còn.

## Hướng triển khai đề xuất

### 1. Chuẩn bị môi trường
- Kiểm tra backend chạy được.
- Kiểm tra frontend chạy được.
- Kiểm tra database/seed data có đủ:
  - admin user.
  - student user.
  - ít nhất một course `PUBLISHED`.
  - course có section, lesson, resource nếu có.

### 2. Smoke test guest/public
- Vào trang chủ.
- Vào danh sách khóa học.
- Search/filter course nếu UI đã có.
- Vào chi tiết khóa học.
- Xem curriculum public.
- Với lesson preview, đảm bảo guest/student có thể truy cập đúng theo rule hiện có.

### 3. Smoke test auth/student
- Register student mới.
- Login student.
- Kiểm tra redirect theo role.
- Vào Student Dashboard.
- Vào My Courses.
- Enroll course miễn phí.
- Vào lesson learning.
- Kiểm tra lesson content, resources, progress, complete lesson.
- Kiểm tra curriculum sidebar và previous/next.
- Vào Profile.
- Update profile.
- Change password.
- Logout và login lại bằng mật khẩu mới.

### 4. Smoke test admin
- Login admin.
- Vào Admin Dashboard.
- Vào User Management.
- Kiểm tra list user, lock/unlock nếu có data phù hợp.
- Vào Course Management.
- Tạo/sửa course cơ bản nếu form hiện tại đã hỗ trợ.
- Vào Course Structure.
- Tạo/sửa/xóa section.
- Tạo/sửa/xóa lesson.
- Tạo/sửa/xóa lesson resource.
- Publish/hide course nếu flow hiện có hỗ trợ.

### 5. Fix trong phạm vi task
Chỉ fix các lỗi thuộc nhóm:
- Sai route hoặc thiếu route link.
- Frontend gọi sai endpoint/base URL.
- UI state làm kẹt màn hình.
- API response unwrap sai.
- Permission config không khớp P0.
- Validation message hoặc error handling gây không dùng được flow.
- Lỗi build/test đơn giản do import, syntax, mock thiếu.

Không fix trong task này nếu lỗi dẫn tới feature lớn mới như upload thật, payment, quiz, notification hoặc report nâng cao.

## Cần tạo hoặc chỉnh sửa
- Có thể không cần tạo file mới nếu chỉ audit.
- Có thể chỉnh các file frontend/backend liên quan tới lỗi phát hiện trong smoke test.
- Nếu có bug rõ ràng, ghi ngắn trong docs/learning hoặc comment commit message.
- Nếu có nhiều lỗi lớn, không ôm hết; tạo task tiếp theo riêng.

## Checklist
- [ ] Backend chạy/package được hoặc blocker môi trường được ghi rõ.
- [ ] Frontend build được.
- [ ] Frontend test pass hoặc lỗi được phân loại rõ.
- [ ] Guest xem được public course flow.
- [ ] Student register/login được.
- [ ] Student enroll course miễn phí được.
- [ ] Student xem my courses/progress được.
- [ ] Student học lesson, lưu progress và complete lesson được.
- [ ] Student dùng curriculum sidebar/previous/next được.
- [ ] Student update profile/change password được.
- [ ] Admin xem dashboard được.
- [ ] Admin quản lý user cơ bản được.
- [ ] Admin quản lý course/section/lesson/resource cơ bản được.
- [ ] Không có lỗi console/API blocker trong luồng demo chính.
- [ ] Những lỗi ngoài phạm vi P0 được ghi lại thay vì code lan man.

## Cách test sau khi hoàn thành
1. Chạy backend.
2. Chạy frontend.
3. Thực hiện guest/public flow.
4. Thực hiện student flow từ register/login tới học bài và profile.
5. Thực hiện admin flow từ dashboard tới quản lý course structure.
6. Chạy `npm run build`.
7. Chạy `npm test`.
8. Chạy backend package/test phù hợp.
9. Ghi lại lỗi còn tồn tại nếu là blocker môi trường hoặc ngoài phạm vi.

## Kết quả mong muốn
MVP P0 có thể demo trơn tru từ đầu tới cuối. Nếu còn lỗi, lỗi đó phải được phân loại rõ: đã fix trong task, là blocker môi trường, hoặc là task riêng sau MVP/P1.
