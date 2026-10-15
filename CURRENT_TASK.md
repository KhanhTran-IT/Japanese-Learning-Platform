# CURRENT TASK

## Task hiện tại
Frontend Navigation, Responsive Shell & Interaction Consistency Hardening

## Trạng thái
TODO

## Mục tiêu
Làm sạch các điểm thiếu nhất quán còn lại trong giao diện sau MVP bằng cách chuẩn hóa điều hướng theo role, responsive shell cho mobile, semantics của các phần tử tương tác và các link/CTA public. Task này tập trung vào UX logic và khả năng sử dụng, không phải redesign thương hiệu.

## Kết luận từ UI Audit
Sau khi rà soát public, student, learning và admin flows, giao diện chưa đủ logic để chuyển ngay sang feature roadmap mới. Các vấn đề có ảnh hưởng xuyên trang:
- Mobile navbar không có link "Trang chủ" dù desktop có, khiến public navigation không tương đương giữa thiết bị.
- Navbar hiển thị Flashcards/Bảng xếp hạng cho admin dù các route đó chỉ dành cho student; click sẽ bị guard redirect, tạo cảm giác UI sai.
- Learning layout che link "Khóa học của tôi" trên mobile và không có đường điều hướng mobile rõ ràng tới course discovery; user phải dựa vào logo hoặc menu profile.
- Admin layout dùng sidebar cố định `w-56`, chưa có navigation pattern cho mobile/small tablet.
- Course cards trên Home/Course List bọc toàn card bằng `router-link` nhưng vẫn đặt button bên trong, tạo nested interactive controls và click behavior mơ hồ.
- Footer có social/support links dùng button hoặc `href="#"` không làm gì; các link level JLPT đều điều hướng tới cùng `/courses` thay vì filter tương ứng.
- Nhiều page dùng radius/card spacing không đồng nhất (`rounded-[24px]`, `rounded-[32px]`, `rounded-2xl`) dù design tokens đã khai báo radius nền tảng, khiến UI chưa thật sự clean và khó duy trì.

## Không làm trong task này
- Không thay đổi business rules enrollment, quiz, flashcard hoặc leaderboard.
- Không redesign toàn bộ visual identity/brand.
- Không thêm payment, recommendation hay trang marketing mới.
- Không đổi API backend trừ khi phát hiện lỗi contract trực tiếp chặn navigation UI.

## File cần kiểm tra/chỉnh sửa
- `frontend/src/components/common/Navbar.vue`
- `frontend/src/components/common/Footer.vue`
- `frontend/src/layouts/MainLayout.vue`
- `frontend/src/layouts/StudentLayout.vue`
- `frontend/src/layouts/LearningLayout.vue`
- `frontend/src/layouts/AdminLayout.vue`
- `frontend/src/pages/public/HomePage.vue`
- `frontend/src/pages/public/CourseListPage.vue`
- `frontend/src/pages/public/CourseDetailPage.vue`
- `frontend/src/pages/student/MyCoursesPage.vue`
- `frontend/src/pages/student/StudentDashboardPage.vue`
- `frontend/src/assets/styles/main.css`
- `frontend/src/router/index.js`
- Các frontend specs liên quan
- `docs/learning/LEARNING_LOG.md`
- `docs/learning/INTERVIEW_NOTES.md`
- `docs/learning/CONCEPTS_EXPLAINED.md`
- `docs/learning/BUG_LOG.md` nếu phát hiện bug mới

## Phạm vi thực hiện

### 1. Navigation parity và role-safe navigation
- Public navbar desktop/mobile đều có Trang chủ và Khóa học.
- Student-only links chỉ hiển thị cho student; admin chỉ thấy admin-appropriate actions.
- Public, Student và Learning shells có đường quay lại course discovery rõ ràng.
- Learning mobile phải có cách truy cập My Courses và Course List không phụ thuộc vào click logo.

### 2. Responsive shell
- Chuyển admin sidebar thành responsive navigation usable trên mobile/small tablet.
- Kiểm tra fixed/sticky elements không che nội dung hoặc CTA trên mobile.
- Giữ kích thước toolbar, controls và text ổn định ở desktop/mobile.

### 3. Interaction semantics và dead links
- Không đặt `button` bên trong `router-link` hoặc nested interactive control.
- Course card chỉ có một target điều hướng rõ ràng hoặc CTA có semantics riêng và không bubble sai.
- Footer social/support links phải là route/link thật, disabled state rõ ràng, hoặc bị loại bỏ khỏi UI hiện tại.
- JLPT footer links truyền đúng filter query.

### 4. Visual consistency cleanup
- Chuẩn hóa card radius, border, spacing và hover/focus state theo token hiện có.
- Giữ các view chuyên biệt như Learning/Quiz tối giản nhưng nhất quán về navigation và action hierarchy.
- Không thay đổi nội dung business chỉ để trang trí.

### 5. Verification
- Bổ sung/update unit tests cho navbar role/mobile states, link behavior và card CTA behavior.
- Chạy full frontend test + production build.
- Kiểm tra responsive ở mobile và desktop bằng screenshot/browser nếu môi trường hỗ trợ; nếu không, ghi rõ blocker.

## Checklist
- [ ] Navbar desktop và mobile có public navigation tương đương.
- [ ] Student-only/admin-only actions không hiển thị sai role.
- [ ] Learning mobile có navigation rõ về My Courses và Course List.
- [ ] Admin navigation usable ở mobile/small tablet.
- [ ] Không còn nested interactive controls trong course cards.
- [ ] Footer không còn link/CTA không có hành vi rõ ràng.
- [ ] Footer JLPT links mở đúng course filter.
- [ ] Card radius/spacing/focus state nhất quán theo design token.
- [ ] Frontend tests và build pass.
- [ ] Learning docs được cập nhật sau khi hoàn tất.
- [ ] Đề xuất task product tiếp theo sau UI hardening.

## Gợi ý branch
`fix/340-Frontend-Navigation-Responsive-Interaction-Hardening`
