# CURRENT TASK

## Task hiện tại
Admin Course Form Modal Contract & UX Hardening

## Trạng thái
TODO

## Mục tiêu
Kiểm tra và hoàn thiện module form tạo/sửa khóa học hiện có để đảm bảo `CourseFormModal.vue` khớp backend contract, xử lý validation rõ ràng, gọi đúng API create/update và tích hợp ổn định với `AdminCourseManagementPage.vue`.

## Vì sao làm task này?
Frontend hiện đã có `CourseFormModal.vue` và `AdminCourseManagementPage.vue` đã mở modal khi bấm "Tạo Khóa Học" hoặc "Sửa". Task tiếp theo không nên làm lại từ đầu, mà nên audit/hardening module này để form create/update khóa học đủ chắc trước khi phát triển sâu hơn phần quản lý section/lesson/public course.

## Không làm trong task này
- Không sửa backend Course API nếu frontend đã có thể khớp contract hiện tại.
- Không làm upload file thumbnail; chỉ dùng `thumbnailUrl` dạng URL text.
- Không làm quản lý section/lesson trong task này.
- Không làm publish/hide/delete nếu các action đó đang hoạt động.
- Không redesign toàn bộ trang admin course.
- Không làm payment/enrollment/public course detail.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`
- `docs/05_features/05_02_COURSE_FEATURES.md`
- `docs/07_database/07_02_COURSE_LESSON.md`

## Backend contract cần đối chiếu

### Create course
```http
POST /api/v1/admin/courses
```

Payload theo `CourseCreateReq`:
```json
{
  "title": "Khóa học N5 nhập môn",
  "slug": "khoa-hoc-n5-nhap-mon",
  "shortDescription": "Mô tả ngắn",
  "description": "Mô tả chi tiết",
  "thumbnailUrl": "https://example.com/thumb.jpg",
  "level": "N5",
  "courseType": "PAID",
  "originalPrice": 1200000,
  "salePrice": 799000
}
```

### Update course
```http
PUT /api/v1/admin/courses/{id}
```

Payload theo `CourseUpdateReq`, giống create nhưng có thêm:
```json
{
  "status": "DRAFT"
}
```

## Logic cần kiểm tra/hoàn thiện
- `CourseFormModal.vue` phải hỗ trợ rõ 2 mode:
  - Create: không truyền `status` nếu backend create không cần.
  - Update: truyền `status` hợp lệ.
- Khi bấm "Tạo Khóa Học":
  - mở modal trống.
  - submit gọi `AdminService.createCourse(payload)`.
  - save thành công thì đóng modal và reload danh sách.
- Khi bấm "Sửa":
  - nên lấy dữ liệu mới nhất bằng `AdminService.getCourseDetail(course.id)` trước khi mở form, hoặc giữ data row nếu muốn đơn giản nhưng phải đảm bảo đủ field.
  - submit gọi `AdminService.updateCourse(id, payload)`.
  - save thành công thì đóng modal và reload danh sách.
- Validation frontend nên khớp backend cơ bản:
  - `title` bắt buộc, tối đa 255 ký tự.
  - `slug` tối đa 255 ký tự nếu có nhập.
  - `level` bắt buộc.
  - `courseType` bắt buộc.
  - `originalPrice >= 0`.
  - `salePrice >= 0`.
  - nếu `courseType = FREE`, giá nên tự về 0 và input giá bị disable.
  - nếu `courseType = PAID`, không cho `salePrice > originalPrice` khi `originalPrice > 0`.
- API error phải hiển thị trong modal bằng `getApiErrorMessage`.
- Submit button có loading state và không bấm lặp khi đang submit.
- Modal close/cancel không để lại state lỗi cho lần mở sau.
- Không dùng text "Đang phát triển" cho create/update course nếu form đã hoạt động.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/components/admin/CourseFormModal.vue`
- `frontend/src/pages/admin/AdminCourseManagementPage.vue`
- Có thể chỉnh `frontend/src/services/admin.service.js` nếu phát hiện thiếu method hoặc sai endpoint.

## Checklist
- [ ] Đối chiếu form fields với `CourseCreateReq`.
- [ ] Đối chiếu form fields với `CourseUpdateReq`.
- [ ] Create course gọi đúng `POST /api/v1/admin/courses`.
- [ ] Update course gọi đúng `PUT /api/v1/admin/courses/{id}`.
- [ ] Create không gửi `status` nếu backend create không cần.
- [ ] Update có gửi `status`.
- [ ] Validate title/slug/level/courseType/price ở frontend.
- [ ] FREE course tự set giá về 0.
- [ ] API error hiển thị rõ trong modal.
- [ ] Save thành công đóng modal và reload danh sách.
- [ ] Không còn placeholder "Đang phát triển" cho create/update course.
- [ ] Chạy `npm run build`.

## Cách test sau khi hoàn thành
1. Đăng nhập bằng ADMIN.
2. Vào `/admin/courses`.
3. Bấm "Tạo Khóa Học".
4. Submit khi thiếu title/level/courseType, kỳ vọng hiện validation.
5. Tạo course FREE, kỳ vọng giá được gửi là 0.
6. Tạo course PAID với sale price lớn hơn original price, kỳ vọng bị chặn.
7. Tạo course hợp lệ, kỳ vọng modal đóng và danh sách reload.
8. Bấm "Sửa" một course, kiểm tra form có dữ liệu cũ.
9. Cập nhật title/status/price, kỳ vọng danh sách reload.
10. Chạy `npm run build`.

## Kết quả mong muốn
Module create/update course trên admin hoạt động ổn định, khớp backend DTO, có validation và error handling rõ ràng, sẵn sàng làm nền cho các task quản lý cấu trúc khóa học tiếp theo.
