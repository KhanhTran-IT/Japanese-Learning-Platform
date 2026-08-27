# CURRENT TASK

## Task hiện tại
Frontend Visual Redesign from Google Stitch Reference

## Trạng thái
TODO

## Mục tiêu
Thiết kế lại giao diện frontend theo bộ giao diện mẫu Google Stitch đã export, đồng thời giữ nguyên logic Vue, route guard, service API và các luồng P0 đang hoạt động.

## Design reference
Thư mục mẫu Google Stitch đã được giải nén và đưa vào workspace tại:

```text
stitch_nihongo_friendly_learning/
```

Trong thư mục có các màn hình mẫu:
- `stitch_nihongo_friendly_learning/trang_chu_brianjp/code.html`
- `stitch_nihongo_friendly_learning/trang_chu_brianjp/screen.png`
- `stitch_nihongo_friendly_learning/danh_sach_khoa_hoc_brianjp/code.html`
- `stitch_nihongo_friendly_learning/danh_sach_khoa_hoc_brianjp/screen.png`
- `stitch_nihongo_friendly_learning/chi_tiet_khoa_hoc_brianjp/code.html`
- `stitch_nihongo_friendly_learning/chi_tiet_khoa_hoc_brianjp/screen.png`
- `stitch_nihongo_friendly_learning/trang_hoc_bai_brianjp/code.html`
- `stitch_nihongo_friendly_learning/trang_hoc_bai_brianjp/screen.png`
- `stitch_nihongo_friendly_learning/brianjp_logo/screen.png`
- `stitch_nihongo_friendly_learning/zen_nihongo/DESIGN.md`

Không cần dùng file zip trong `Downloads` nữa. Antigravity chỉ cần đọc trực tiếp thư mục `stitch_nihongo_friendly_learning/`.

## Vì sao làm task này?
MVP P0 đã có các luồng chính: public course, auth, student learning, profile và admin management. Sau khi chức năng nền đã đủ để demo, bước hợp lý tiếp theo là nâng chất lượng UI để sản phẩm nhìn nhất quán, chuyên nghiệp và gần với thiết kế mục tiêu hơn.

## Không làm trong task này
- Không đổi backend API.
- Không đổi database.
- Không làm quiz.
- Không làm payment.
- Không làm upload file thật.
- Không viết lại toàn bộ frontend từ đầu.
- Không phá route/service/store hiện có.
- Không xóa logic loading/error/empty state.
- Không hardcode dữ liệu thay cho API thật.
- Không commit file zip/design reference nếu không cần.

## File tài liệu cần dùng
- `docs/00_MASTER_CONTEXT.md`
- `docs/23_MVP_SCOPE.md`
- `docs/24_USER_FLOWS.md`
- `docs/25_SCREEN_LIST.md`
- `docs/10_FRONTEND_STRUCTURE.md`
- `docs/11_BACKEND_FRONTEND_CONFIG.md`
- `docs/18_CODE_CONVENTIONS.md`
- `docs/21_AI_WORKING_GUIDE.md`

## File frontend cần ưu tiên

### Public pages
- `frontend/src/pages/public/HomePage.vue`
- `frontend/src/pages/public/CourseListPage.vue`
- `frontend/src/pages/public/CourseDetailPage.vue`
- `frontend/src/layouts/MainLayout.vue`

### Student learning pages
- `frontend/src/pages/student/LessonLearningPage.vue`
- `frontend/src/components/lesson/LearningCurriculumSidebar.vue`
- `frontend/src/pages/student/StudentDashboardPage.vue`
- `frontend/src/pages/student/MyCoursesPage.vue`
- `frontend/src/pages/student/ProfilePage.vue`
- `frontend/src/layouts/StudentLayout.vue`

### Shared frontend files
- `frontend/src/assets/`
- `frontend/src/styles/` nếu project có global style
- `frontend/src/router/index.js` chỉ chỉnh nếu thật sự cần route text/nav

## Vấn đề hiện tại
- Giao diện hiện tại chủ yếu được build theo từng task chức năng nên chưa thống nhất visual system.
- Một số page dùng style riêng, chưa có cảm giác cùng một brand.
- Public pages và learning page cần bám gần mẫu Google Stitch hơn để demo đẹp hơn.
- Cần chuyển cảm hứng từ HTML/CSS mẫu sang Vue component hiện tại mà không làm mất API integration.

## Hướng triển khai đề xuất

### 1. Đọc và phân tích design reference
- Đọc trực tiếp thư mục `stitch_nihongo_friendly_learning/`.
- Đọc `stitch_nihongo_friendly_learning/zen_nihongo/DESIGN.md` trước nếu có mô tả style.
- Mở từng `screen.png` để hiểu layout, spacing, màu sắc, typography.
- Đọc `code.html` để lấy class/style/token tham khảo.
- Không copy nguyên HTML một cách mù quáng; cần port sang Vue structure hiện tại.

### 2. Xác định design system nhẹ
Rút ra các yếu tố dùng chung:
- brand name/logo treatment.
- bảng màu chính/phụ.
- font scale.
- button style.
- card style.
- input/filter style.
- section spacing.
- responsive breakpoints.
- empty/loading/error state style.

Nếu project chưa có global CSS rõ ràng, có thể tạo hoặc cập nhật style chung, nhưng chỉ khi giúp giảm trùng lặp thật sự.

### 3. Redesign theo thứ tự ưu tiên
Ưu tiên port các màn hình có mẫu Stitch trước:
1. Home page.
2. Course list page.
3. Course detail page.
4. Lesson learning page.

Sau đó làm các màn hình student còn lại để đồng bộ:
5. Student dashboard.
6. My courses.
7. Profile.

Admin pages chỉ chỉnh nhẹ nếu có thời gian để tránh làm vỡ layout quản trị.

### 4. Quy tắc giữ logic
- Giữ nguyên API service calls.
- Giữ nguyên route names và paths.
- Giữ nguyên route guard.
- Giữ nguyên state loading/error/empty.
- Giữ nguyên action chính: enroll, learn lesson, update progress, complete lesson, view resources, update profile, change password.
- Nếu đổi markup, phải đảm bảo test hiện có vẫn tìm được nội dung cần kiểm tra hoặc cập nhật test mock/selector hợp lý.

### 5. Responsive và accessibility
- Kiểm tra mobile width cho public pages và lesson page.
- Button/link phải có text rõ nghĩa.
- Form input cần label rõ ràng.
- Không để text tràn container.
- Không để sidebar che nội dung chính trên mobile.
- Color contrast phải đủ đọc.

## Checklist
- [ ] Đã đọc Google Stitch reference trong `stitch_nihongo_friendly_learning/`.
- [ ] Home page bám style mẫu.
- [ ] Course list page bám style mẫu.
- [ ] Course detail page bám style mẫu.
- [ ] Lesson learning page bám style mẫu.
- [ ] Student dashboard/my courses/profile đồng bộ visual system.
- [ ] Main/student layout đồng bộ brand/navigation.
- [ ] API integration hiện có không bị phá.
- [ ] Loading/error/empty state vẫn hoạt động.
- [ ] Responsive desktop/mobile ổn.
- [ ] Không commit file zip/reference binary nếu không cần.
- [ ] Chạy `npm run build`.
- [ ] Chạy `npm test`.
- [ ] Nếu có visual issue còn lại, ghi rõ để tách task sau.

## Cách test sau khi hoàn thành
1. Chạy frontend.
2. Mở home page và so với `stitch_nihongo_friendly_learning/trang_chu_brianjp/screen.png`.
3. Mở course list và so với `stitch_nihongo_friendly_learning/danh_sach_khoa_hoc_brianjp/screen.png`.
4. Mở course detail và so với `stitch_nihongo_friendly_learning/chi_tiet_khoa_hoc_brianjp/screen.png`.
5. Mở lesson learning và so với `stitch_nihongo_friendly_learning/trang_hoc_bai_brianjp/screen.png`.
6. Test guest xem course.
7. Test student enroll và học lesson.
8. Test progress/complete/resources/curriculum sidebar.
9. Test profile update/change password.
10. Test responsive mobile.
11. Chạy `npm run build`.
12. Chạy `npm test`.

## Kết quả mong muốn
Frontend có giao diện nhất quán, thân thiện và gần với mẫu Google Stitch, nhưng toàn bộ logic P0 hiện có vẫn chạy ổn định.
