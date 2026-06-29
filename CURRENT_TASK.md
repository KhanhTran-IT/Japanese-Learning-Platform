# CURRENT TASK

## Task hiện tại
Student Lesson Learning & Progress API (API Học bài và Lưu tiến độ)

## Trạng thái
DONE
Ngày hoàn thành: 29/06/2026

## Mục tiêu
Xây dựng luồng API dành cho Học viên (`STUDENT`) để có thể xem chi tiết trọn vẹn nội dung một Bài học (bao gồm cả các trường bí mật như `videoUrl`) sau khi đã ghi danh, đồng thời cung cấp API để cập nhật và lưu trữ tiến độ học tập (phần trăm xem video, trạng thái hoàn thành).

## Vì sao làm task này?
Đây là tính năng lõi để hiện thực hóa việc học trực tuyến theo đúng yêu cầu MVP: "Hệ thống lưu bài học đã hoàn thành" và "Hệ thống lưu phần trăm video đã xem nếu có". Sau khi API Ghi danh đã cấp quyền, hệ thống cần cơ chế kiểm tra quyền đó để nhả nội dung video cho Học viên và tracking sự tiến bộ của họ trong suốt khóa học.

## Không làm trong task này
- Không làm tính năng Quiz cuối bài.
- Không tính toán chứng chỉ hoàn thành khóa học.
- Không phát triển các tính năng Gamification (XP, Streak).

## File tài liệu cần dùng
- Ràng buộc nghiệp vụ: Bảng `lesson_progress` trong thiết kế cơ sở dữ liệu.
- Tài liệu định hướng MVP: `docs/23_MVP_SCOPE.md`.

## API cần làm
- `GET /api/v1/lessons/{id}`: Truy xuất toàn bộ chi tiết một bài học.
  - *Bảo mật:* Yêu cầu xác thực qua Token. Nếu Bài học đang thiết lập `isPreview = false`, hệ thống bắt buộc kiểm tra xem User hiện tại đã ghi danh (có bản ghi trong `course_enrollments`) hay chưa. Nếu chưa ghi danh, ném lỗi 403 Forbidden.
- `POST /api/v1/lessons/{id}/progress`: Cập nhật tiến độ học tập của bài học.
  - *Payload (Gợi ý):* `{ "watchedPercent": 80.5, "isCompleted": false }`.

## Logic xử lý kiến trúc & Nghiệp vụ
1. **Kiểm soát Truy cập Nội dung (Authorization Barrier):**
   - Lấy thông tin Bài học (`Lesson`) qua ID. Kiểm tra trạng thái Khóa học cha.
   - Nếu `isPreview == true`, bỏ qua kiểm tra ghi danh, trả về DTO chứa full thông tin (Video, tài liệu).
   - Nếu `isPreview == false`, thực hiện truy vấn bảng `course_enrollments` với `userId` hiện tại. Nếu có bản ghi `ACTIVE`, trả về DTO full thông tin. Nếu không, chặn bằng `AUTH_003` (Forbidden - Cần ghi danh để xem bài học này).
2. **Cập nhật Tiến độ (`lesson_progress`):**
   - Khi Frontend định kỳ (ví dụ mỗi 10 giây) gọi API POST để báo cáo % xem video, sử dụng thuật toán **Upsert** (Cập nhật hoặc Thêm mới): Tìm trong bảng `lesson_progress` xem user đã có record cho bài học này chưa.
   - Chú ý: `watchedPercent` mới truyền lên chỉ được phép ghi đè nếu nó lớn hơn `watchedPercent` đã lưu trong DB (Học viên không bị mất tiến độ khi xem lại đoạn cũ).
   - Nếu `isCompleted == true`, tự động gắn thời gian vào cột `completed_at`.

## Cần tạo hoặc chỉnh sửa
- Khởi tạo Entity `LessonProgress` và `LessonProgressRepository` (Bao gồm hàm `findByUserIdAndLessonId`).
- `LessonLearningRes` (DTO trả về full chi tiết bài học cho màn hình học tập).
- `ProgressUpdateReq` (DTO cho request cập nhật phần trăm).
- Bổ sung logic vào module học tập (Tạo mới `LearningService` hoặc dùng chung `LessonService` tùy kiến trúc hiện tại).
- Tạo/Cập nhật Controller xử lý endpoint học tập.

## Error code cần dùng (Theo chuẩn PREFIX_00X)
- `LESSON_001`: Lesson not found (404)
- `AUTH_003`: Forbidden - Trạng thái chưa ghi danh (403)
- `VALID_001`: Validation Error

## Checklist
- [ ] Thiết lập bảng `lesson_progress` đảm bảo tính chất Unique kép.
- [ ] Xây dựng luồng xác thực cấp phép xem video bài học chặt chẽ.
- [ ] Áp dụng thuật toán Upsert để lưu hoặc tạo mới tiến độ học tập.
- [ ] Triển khai luật chỉ cập nhật `watchedPercent` lên chứ không cho phép lùi xuống.
- [ ] Dùng Postman test tài khoản chưa Enroll truy cập bài học trả phí (Kỳ vọng 403).
- [ ] Test tài khoản đã Enroll gọi xem bài học (Kỳ vọng 200, hiển thị `videoUrl`).
- [ ] Gọi POST cập nhật tiến độ nhiều lần để kiểm tra thuật toán ghi đè tiến độ lớn nhất.

## Kết quả mong muốn
Học viên được phân quyền minh bạch để tiêu thụ nội dung đa phương tiện, đồng thời hệ thống bám sát tiến độ học tập và ghi nhận trạng thái hoàn thành chính xác của họ.