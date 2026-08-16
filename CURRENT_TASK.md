# CURRENT TASK

## Task hiện tại
Frontend Lesson Complete API Integration

## Trạng thái
TODO

## Mục tiêu
Cập nhật frontend để nút "Đánh dấu hoàn thành" trong trang học bài gọi endpoint chuyên biệt `POST /api/v1/lessons/{id}/complete` thay vì dùng API update progress chung. Sau khi complete thành công, UI cần phản ánh lesson đã hoàn thành, watched percent là 100% và không làm hỏng luồng lưu progress hiện tại.

## Vì sao làm task này?
Backend đã có API complete lesson riêng. Frontend nên gọi đúng endpoint nghiệp vụ để code rõ nghĩa hơn, giảm việc tự dựng payload complete ở client và giúp backend kiểm soát rule hoàn thành bài học nhất quán.

## Không làm trong task này
- Không sửa backend complete API.
- Không làm quiz.
- Không làm lesson resources.
- Không làm video tracking tự động.
- Không làm lesson sidebar/curriculum.
- Không làm payment/enrollment flow.
- Không redesign toàn bộ trang học bài.
- Không làm form Create/Update khóa học. Phần form này hiện đang dùng nút "Đang phát triển"; vì cấu trúc Course cần nhiều input field và xử lý file, nên tách thành module riêng ở task kế tiếp cho gọn.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/26_API_PRIORITY.md`
- `docs/31_DETAILED_TESTING_PLAN.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`
- `docs/08_api/08_04_LESSON_API.md`

## API cần tích hợp
```http
POST /api/v1/lessons/{id}/complete
Authorization: Bearer <accessToken>
```

## Request mẫu
```http
POST /api/v1/lessons/10/complete
```

Không cần body.

## Response mong muốn
```json
{
  "code": 1000,
  "message": "Hoàn thành bài học thành công",
  "result": null
}
```

## Logic xử lý
- Thêm method `completeLesson(id)` trong frontend learning service.
- Method này gọi `POST /v1/lessons/{id}/complete` vì Axios base URL đã là `/api`.
- Cập nhật function `markCompleted()` trong trang học bài:
  - kiểm tra lesson đã load và có `id`.
  - bật loading state trong lúc gọi API.
  - gọi `LearningService.completeLesson(lessonId)`.
  - nếu thành công, cập nhật local state:
    - `lesson.isCompleted = true`
    - `lesson.watchedPercent = 100`
    - progress form hoặc progress state liên quan cũng về 100 nếu đang dùng.
  - hiển thị thông báo thành công theo pattern hiện có.
  - nếu lỗi, dùng helper xử lý error message hiện có.
- Giữ API `updateProgress()` cho nút/lưu tiến độ xem bài thủ công nếu trang đang có chức năng này.
- Không gửi payload `{ watchedPercent: 100, isCompleted: true }` cho complete nữa.
- Không làm giảm progress local nếu backend đang có rule watchedPercent chỉ tăng.

## Cần tạo hoặc chỉnh sửa
- `frontend/src/services/learning.service.js`
- `frontend/src/pages/student/LessonLearningPage.vue`

## Checklist
- [ ] `LearningService` có method `completeLesson(id)`.
- [ ] Nút "Đánh dấu hoàn thành" gọi endpoint `POST /api/v1/lessons/{id}/complete`.
- [ ] Complete API không gửi request body.
- [ ] Khi complete thành công, UI hiển thị bài học đã hoàn thành.
- [ ] `watchedPercent` trên UI được set về 100.
- [ ] Loading state hoạt động, tránh bấm lặp gây nhiều request không cần thiết.
- [ ] Error state hiển thị theo pattern hiện có.
- [ ] Luồng lưu progress cũ vẫn hoạt động.
- [ ] Chạy `npm run build`.

## Cách test sau khi hoàn thành
1. Đăng nhập bằng STUDENT đã enroll course.
2. Vào trang học bài có quyền học.
3. Bấm "Đánh dấu hoàn thành".
4. Kiểm tra Network tab thấy request `POST /api/v1/lessons/{id}/complete`.
5. Kiểm tra request không có body.
6. Kiểm tra UI chuyển sang trạng thái đã hoàn thành và progress là 100%.
7. Refresh trang, kiểm tra trạng thái completed vẫn được load lại từ backend.
8. Kiểm tra nút/lưu progress thường vẫn không lỗi.
9. Chạy `npm run build`.

## Kết quả mong muốn
Frontend dùng đúng endpoint complete lesson chuyên biệt, UI cập nhật rõ ràng sau khi hoàn thành bài học và luồng progress hiện tại vẫn ổn định.
