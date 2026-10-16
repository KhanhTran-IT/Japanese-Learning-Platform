# CURRENT TASK

## Task hiện tại
Frontend Navigation, Responsive Shell & Interaction Consistency Hardening

## Trạng thái
DONE ✅

## Mục tiêu
Làm sạch các điểm thiếu nhất quán còn lại trong giao diện sau MVP bằng cách chuẩn hóa điều hướng theo role, responsive shell cho mobile, semantics của các phần tử tương tác và các link/CTA public. Task này tập trung vào UX logic và khả năng sử dụng, không phải redesign thương hiệu.

## Kết quả thực hiện
1. **Navigation Parity & Role Safety**:
   - `Navbar.vue`: Thêm "Trang chủ" vào mobile navigation menu tương đương desktop.
   - Thêm điều kiện `v-if="!isAdmin"` cho `Flashcards`, `Bảng xếp hạng`, `Khóa học của tôi`, và `Profile` trên cả desktop và mobile menu, ngăn ngừa admin nhìn thấy các liên kết bị guard redirect.
   - Hiển thị nhãn `Bảng quản trị` trên mobile menu cho tài khoản admin.
2. **Learning Layout Mobile Navigation**:
   - `LearningLayout.vue`: Thêm nút Back mũi tên trên mobile header dẫn trực tiếp về `/student/my-courses`.
   - Bổ sung liên kết "Khóa học của tôi" và "Khám phá khóa học" vào dropdown user menu, giúp học viên chuyển hướng dễ dàng mà không phụ thuộc vào click logo.
3. **Responsive Admin Shell**:
   - `AdminLayout.vue`: Chuyển desktop sidebar sang `hidden md:flex`, bổ sung nút toggle Hamburger trên mobile top bar.
   - Xây dựng mobile drawer có backdrop làm mờ, nút đóng `✕`, hỗ trợ đóng khi click backdrop, chọn menu item hoặc nhấn phím `Escape`.
   - Thêm liên kết `Về trang chủ` (`/`) trên cả desktop và mobile sidebar để admin nhanh chóng quay lại trang public.
4. **Interaction Semantics & Dead Links**:
   - Loại bỏ toàn bộ nested `<button>` bên trong `<router-link>` tại `HomePage.vue` và `CourseListPage.vue` (chuyển sang styled `<span>`), ngăn chặn vi phạm chuẩn HTML5 và click event bubbling.
   - `MyCourseCard.vue`: Chuẩn hóa card container thành semantic focusable button (`role="button"`, `tabindex="0"`, hỗ trợ Enter & Space), chuyển inner button sang styled span.
   - `Footer.vue`: Loại bỏ toàn bộ `href="#"`. Chuyển các liên kết JLPT sang query param thật (`/courses?level=N5`, etc.), bổ sung link Luyện tập (`flashcards`, `leaderboard`), vô hiệu hóa nút mạng xã hội chưa có chức năng với `disabled`, `aria-disabled="true"` và tooltip `title="Sắp ra mắt"`.
   - `RegisterPage.vue`: Loại bỏ các thẻ `<a href="#">` ở phần đồng ý điều khoản.
5. **Design Token Consistency**:
   - Loại bỏ các class arbitrary `rounded-[24px]` và `rounded-[32px]` trên toàn project, chuẩn hóa về token `rounded-2xl` và `rounded-3xl` tại `MyCoursesPage`, `StudentDashboardPage`, `ProfilePage`, `QuizTakingPage`, `QuizResultPage`.
6. **Testing & Verification**:
   - Bổ sung unit tests: `Navbar.spec.js` (3 tests), `Footer.spec.js` (3 tests), `MyCourseCard.spec.js` (2 tests).
   - Chạy `npm run test`: **PASS 14/14 test suites, 57/57 tests**.
   - Chạy `npm run build`: **Build thành công sạch sẽ trong 1.98s**.
   - Chạy kiểm tra thực tế bằng Browser Subagent: chụp ảnh màn hình desktop navbar/footer và mobile drawer menu thành công (`homepage_desktop`, `homepage_footer_actual`, `mobile_menu_open_success`).

## Checklist
- [x] Navbar desktop và mobile có public navigation tương đương.
- [x] Student-only/admin-only actions không hiển thị sai role.
- [x] Learning mobile có navigation rõ về My Courses và Course List.
- [x] Admin navigation usable ở mobile/small tablet.
- [x] Không còn nested interactive controls trong course cards.
- [x] Footer không còn link/CTA không có hành vi rõ ràng.
- [x] Footer JLPT links mở đúng course filter.
- [x] Card radius/spacing/focus state nhất quán theo design token.
- [x] Frontend tests và build pass.
- [x] Learning docs được cập nhật sau khi hoàn tất.
- [x] Đề xuất task product tiếp theo sau UI hardening.

