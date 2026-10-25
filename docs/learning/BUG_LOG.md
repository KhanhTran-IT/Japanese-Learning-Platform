# Nhật Ký Bug và Lỗi

Tài liệu ghi lại các bug, lỗi gặp phải và cách khắc phục. Giúp tránh lặp lại lỗi và làm tài liệu tham khảo.

## Hướng dẫn ghi chép

- **Ghi tên bug rõ ràng** dễ search lại sau
- **Mô tả triệu chứng chi tiết** - bug biểu hiện như thế nào?
- **Ghi nguyên nhân** nếu tìm được
- **Ghi cách fix** cụ thể, có thể reproduce lại
- **Ghi error log/stack trace** nếu có
- **Đánh giá mức độ nghiêm trọng**: 🔴 Critical | 🟠 High | 🟡 Medium | 🟢 Low
- **Ghi status**: ❌ Chưa fix | ✅ Đã fix | 🔄 Đang debug

## Template mẫu

```markdown
### [Bug ID: #001] - [Tên bug ngắn gọn]

**Status:** ❌/✅/🔄
**Mức độ:** 🔴/🟠/🟡/🟢

**Triệu chứng:**

- Hiện tượng 1
- Hiện tượng 2

**Nguyên nhân:**
Mô tả chi tiết nguyên nhân gây ra bug

**Cách fix:**
\`\`\`java
// Code fix
\`\`\`

**Error log:**
\`\`\`
stack trace hoặc error message
\`\`\`

**Test lại:** Cách để verify bug đã fix

**Ghi chú:** Những điểm cần lưu ý
```

## Bug Log

### [Bug ID: #001] - NullPointerException khi fetch user từ database

**Status:** ✅
**Mức độ:** 🔴

**Triệu chứng:**

- API `/api/users/{id}` trả về 500 Internal Server Error
- Không thể lấy thông tin user

**Nguyên nhân:**
Query database không kiểm tra null, entity user không tồn tại nhưng code vẫn cố access property

**Cách fix:**

```java
Optional<User> user = userRepository.findById(id);
if (user.isEmpty()) {
    throw new UserNotFoundException("User not found");
}
return user.get();
```

**Error log:**

```
java.lang.NullPointerException
    at com.example.service.UserService.getUser(UserService.java:45)
```

**Test lại:** Gọi API với user ID không tồn tại, kiểm tra status 404

**Ghi chú:** Luôn sử dụng Optional khi làm việc với database query

---

### [Bug ID: #002] - VueJS component không re-render khi props thay đổi

**Status:** ✅
**Mức độ:** 🟠

**Triệu chứng:**

- Thay đổi data từ parent component
- Child component không update UI

**Nguyên nhân:**
Đang mutate array/object trực tiếp thay vì tạo reference mới

**Cách fix:**

```javascript
// ❌ Sai
this.items.push(newItem);

// ✅ Đúng
this.items = [...this.items, newItem];
```

**Test lại:** Thêm item vào list, kiểm tra UI cập nhật

**Ghi chú:** Vue chỉ detect thay đổi nếu tạo reference mới

---

## 2026-07-17 - Admin login bị redirect sai dashboard

### 1. Lỗi xảy ra khi nào?

Sau khi đăng nhập bằng tài khoản admin, frontend vẫn điều hướng cứng về `/student/dashboard` thay vì `/admin/dashboard`.

### 2. Log lỗi chính

```text
Không có stack trace backend.
Triệu chứng ở frontend: user role ADMIN đăng nhập thành công nhưng bị đưa về dashboard của STUDENT hoặc bị route guard điều hướng lại.
```

### 3. Nguyên nhân

Logic redirect trong `LoginPage.vue` chưa dựa trên role của user sau khi gọi `/api/users/me`. Code cũ giả định mọi user đăng nhập xong đều đi đến khu vực student.

### 4. Cách sửa

Sau khi lấy `userData`, đọc `userData.roles`. Nếu roles có `ADMIN` hoặc `SUPER_ADMIN` thì chuyển đến `/admin/dashboard`, ngược lại chuyển đến `/student/dashboard`.

### 5. Tôi học được gì?

Khi hệ thống có nhiều role, redirect sau đăng nhập phải dựa vào dữ liệu user thật từ backend. Không nên hard-code một dashboard mặc định cho mọi tài khoản.

---

### [Bug ID: #003] - RuntimeException: ADMIN role not found khi chạy Integration Test

**Status:** ✅
**Mức độ:** 🟠

**Triệu chứng:**
- Khi chạy `mvn verify` hoặc các Integration Test (`*IT.java`) có sử dụng `@SpringBootTest`, ApplicationContext bị crash và báo lỗi `ADMIN role not found. Cannot seed admin user.`
- Nguyên nhân: Các file test (ví dụ `AuthControllerIT`) có sử dụng `@MockBean` để mock `RoleRepository` và `UserRepository`. Tuy nhiên, Bean `DatabaseSeeder` (một `CommandLineRunner`) vẫn được load bởi Spring Boot và cố gắng gọi `roleRepository.findByName()`. Do repository đã bị mock (trả về Optional.empty), việc tìm role ADMIN bị thất bại dẫn tới exception.

**Nguyên nhân:**
Spring Boot mặc định load toàn bộ các bean trong context, bao gồm cả `DatabaseSeeder`, trong khi môi trường mock test không có dữ liệu thực tế.

**Cách fix:**
Sử dụng `@ConditionalOnProperty` để vô hiệu hóa `DatabaseSeeder` nếu property `app.seeder.enabled` = false. Trong file cấu hình `application.yml` dành riêng cho môi trường test (`src/test/resources/application.yml`), ta set `app.seeder.enabled: false`.

```java
// Trong DatabaseSeeder.java
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner { ... }
```

**Test lại:** Chạy `mvn verify`, ApplicationContext load thành công và không bị dính exception của Seeder.

**Ghi chú:** Khi viết Integration Test với `@SpringBootTest`, cần cẩn thận với các Bean khởi tạo dữ liệu ban đầu (Seeder, Runner). Luôn cấp cơ chế bật/tắt chúng qua cấu hình để không xung đột với các `@MockBean`.

---

## 2026-10-06 - Modal form admin bị "trắng" trên dark theme

### 1. Lỗi xảy ra khi nào?

Sau khi migrate các modal form (CourseFormModal, SectionFormModal, LessonFormModal, ResourceFormModal, QuizFormModal) sang Stitch dark theme, `<select>` và `<option>` vẫn hiển thị text trắng trên nền trắng (browser default styling cho option).

### 2. Log lỗi chính

```text
Không có console error.
Triệu chứng: Dropdown <select> mở ra, các <option> hiển thị text trắng trên background trắng → không đọc được.
```

### 3. Nguyên nhân

CSS đặt `color: white` cho input/select (dark theme), nhưng `<option>` element khi dropdown mở ra sử dụng browser native rendering. Browser không kế thừa background-color từ parent `<select>` cho popup dropdown, nên option text trắng hiện trên nền trắng mặc định của browser.

### 4. Cách sửa

Thêm `class="bg-[#161b27]"` cho mỗi `<option>` element:
```html
<select class="bg-white/5 text-white ...">
  <option value="DRAFT" class="bg-[#161b27]">Bản nháp</option>
  <option value="PUBLISHED" class="bg-[#161b27]">Đã xuất bản</option>
</select>
```

### 5. Tôi học được gì?

Khi làm dark theme, `<option>` element là special case — browser native rendering override CSS inheritance. Phải set background color trực tiếp trên từng `<option>`, không thể dựa vào parent `<select>` styling. Đây là quirk của HTML form elements mà CSS spec không standardize hoàn toàn cho dropdown popup.


---

## 2026-10-07 - Test failures sau migrate Stitch UI

### [Bug ID: #004] - LessonLearningPage.spec.js gãy hoàn toàn sau UI migration

**Status:** ✅
**Mức độ:** 🟠

**Triệu chứng:**
- 3/3 tests trong `LessonLearningPage.spec.js` fail.
- `Error: Cannot call text on an empty DOMWrapper` khi tìm `.lesson-title`, `.btn-complete`, `.safe-content`.
- `AssertionError: expected false to be true` khi tìm `.error-state`.

**Nguyên nhân:**
Sau khi migrate sang Stitch UI, các CSS class cũ (`.lesson-title`, `.btn-complete`, `.progress-text`, `.error-state`, `.safe-content`) không còn tồn tại trong template. UI mới dùng cấu trúc tab-based thay vì flat layout cũ.

**Cách fix:**
Rewrite toàn bộ test file:
```javascript
// Cũ (brittle CSS class selector):
expect(wrapper.find('.lesson-title').text()).toBe('Test Lesson')

// Mới (text content + semantic selector):
expect(wrapper.find('h1').text()).toContain('Test Lesson')
expect(wrapper.text()).toContain('Test content')
```

Đồng thời:
- Stub `LearningCurriculumSidebar` child component.
- Dùng `flushPromises()` thay `setTimeout(0)`.
- Tìm button "Đánh dấu xong bài học" qua text content thay vì class `.btn-complete`.

**Test lại:** `npm run test` — 38/38 tests pass.

**Ghi chú:** Bài học quan trọng: CSS class selectors trong test là brittle. Mỗi lần redesign UI sẽ phải rewrite test. Nên dùng text-based hoặc `data-testid` selectors cho test bền vững hơn.

---

### [Bug ID: #005] - QuizTakingPage.spec.js mock sai method name và response structure

**Status:** ✅
**Mức độ:** 🟡

**Triệu chứng:**
- `__vite_ssr_import_2__.QuizService.getQuiz is not a function`.
- `expected "vi.fn()" to be called with arguments: [ 1 ]` nhưng received `[ "1" ]`.
- `expected "vi.fn()" to be called with arguments: [ 999, [...] ]` nhưng received `[ "1", { attemptId: 999, answers: [...] } ]`.

**Nguyên nhân:**
3 lỗi riêng biệt:
1. Mock khai báo `getQuizToTake` nhưng code thật gọi `QuizService.getQuiz()`.
2. `useRoute().params.quizId` trả về string `"1"`, test expect number `1`.
3. `QuizService.submitQuiz` nhận `(quizId, { attemptId, answers })`, test expect `(999, [...])`.

**Cách fix:**
```javascript
// 1. Đổi tên mock method
QuizService: { getQuiz: vi.fn(), ... }

// 2. Match string param
expect(QuizService.startQuiz).toHaveBeenCalledWith('1')

// 3. Match đúng payload structure
expect(QuizService.submitQuiz).toHaveBeenCalledWith('1', {
  attemptId: 999,
  answers: [
    { questionId: 101, answerId: 1 },
    { questionId: 102, userAnswerText: 'My answer' }
  ]
})
```

**Test lại:** `npm run test` — 38/38 tests pass.

**Ghi chú:** Trước khi viết mock cho service, luôn `grep -n "ServiceName" Component.vue` để xác nhận chính xác tên method, kiểu params, và response structure. Không đoán.

---

### [Bug ID: #006] - Logo.png 728KB cho icon 32×32px trên navbar

**Status:** ✅
**Mức độ:** 🟠

**Triệu chứng:**
- Build output hiển thị `dist/assets/logo-DpsyHiEy.png — 728.86 kB`. Một file ảnh static chiếm gần 1/3 dung lượng toàn bộ JavaScript bundle.
- Trên mạng 3G, tải ảnh logo mất ~3-5s, gây delay visual cho navbar.

**Nguyên nhân:**
File `logo.png` gốc có kích thước 1024×1024 pixels (PNG, non-interlaced), nhưng chỉ được dùng ở `h-8 w-8` (32×32px CSS pixels, tối đa 64×64 physical pixels trên 2x display). 96% pixels bị lãng phí.

**Cách fix:**
```bash
# Resize + chuyển format WebP
convert src/assets/logo.png -resize 128x128 -quality 80 src/assets/logo.webp
# 128×128 đủ cho 4x retina display (32px CSS × 4 = 128px)
```
Cập nhật reference trong `LearningLayout.vue`:
```vue
<!-- Cũ -->
<img src="@/assets/logo.png" alt="BrianJP Logo" />
<!-- Mới -->
<img src="@/assets/logo.webp" alt="BrianJP Logo" />
```

**Kết quả:** `logo.png` 728.86 KB → `logo.webp` 5.36 KB (giảm 99.3%).

**Test lại:** `npm run build` + `npm run test` — 38/38 tests pass.

**Ghi chú:** Giữ nguyên `logo.png` gốc trong repo làm source file. Nếu cần resize lại hoặc export format khác trong tương lai, luôn bắt đầu từ file gốc (không resize từ file đã nén).

---

### [Bug ID: #007] - Hotlink Unsplash ảnh nền — rủi ro die link production

**Status:** ✅
**Mức độ:** 🟡

**Triệu chứng:**
- `HomePage.vue` dùng `style="background-image: url('https://images.unsplash.com/photo-1598957232485-...')"` — ảnh nền Hero section.
- `AuthLayout.vue` dùng URL tương tự cho ảnh nền trang Login/Register.
- Trên mạng hạn chế hoặc khi Unsplash CDN chậm, hero section hiển thị trống trong 2-5s.

**Nguyên nhân:**
Hotlinking ảnh từ server bên thứ ba: (1) thêm DNS lookup cho `images.unsplash.com`, (2) không cache ở build time, (3) Unsplash có thể thay đổi URL structure hoặc rate-limit.

**Cách fix:**
```bash
# Download và convert sang WebP
curl -sL "https://images.unsplash.com/..." -o src/assets/hero-bg.jpg
curl -sL "https://images.unsplash.com/..." -o src/assets/auth-bg.jpg
convert src/assets/hero-bg.jpg -quality 75 src/assets/hero-bg.webp
convert src/assets/auth-bg.jpg -quality 75 src/assets/auth-bg.webp
```
```vue
<!-- Import và dùng dynamic style binding -->
import heroBg from '@/assets/hero-bg.webp'
<div :style="{ backgroundImage: `url(${heroBg})` }">
```

**Kết quả:** Ảnh nền được Vite hash và bundle, load từ static assets thay vì fetch runtime. Xóa file `.jpg` trung gian, chỉ giữ `.webp`.

**Test lại:** `npm run build` + `npm run test` — 38/38 tests pass.

**Ghi chú:** Luật chung: mọi ảnh tĩnh dùng trong UI phải nằm trong `src/assets/` và import qua Vite. Chỉ ảnh dynamic (user upload, API response) mới dùng URL runtime.

---

### [Bug ID: #008] - Lombok @Builder bỏ qua giá trị mặc định của field

**Status:** ✅
**Mức độ:** 🟡

**Triệu chứng:**
- Compilation warning: `@Builder will ignore the initializing expression entirely.`
- `FlashcardProgress` khi tạo bằng `.builder().build()` có `easeFactor = null` và `intervalDays = null` thay vì `2.5` và `0`.
- Khi save vào DB bị lỗi NOT NULL constraint violation.

**Nguyên nhân:**
Lombok `@Builder` không sử dụng giá trị khởi tạo inline (`= 2.5`) của field. Builder pattern tạo instance qua constructor nội bộ riêng, bỏ qua field initializer. Phải đánh dấu `@Builder.Default` để Lombok biết dùng giá trị default.

**Cách fix:**
```java
// ❌ Sai — Builder bỏ qua giá trị 2.5
@Column(name = "ease_factor", nullable = false)
private Double easeFactor = 2.5;

// ✅ Đúng — @Builder.Default báo Lombok giữ giá trị default
@Builder.Default
@Column(name = "ease_factor", nullable = false)
private Double easeFactor = 2.5;

@Builder.Default
@Column(name = "interval_days", nullable = false)
private Integer intervalDays = 0;
```

**Kết quả:** Build 0 warnings, entity tạo đúng giá trị mặc định khi dùng builder pattern.

**Test lại:** `mvn test -Dtest=FlashcardServiceImplTest` — 2/2 tests PASS.

**Ghi chú:** Luật chung: mọi field trong entity dùng `@Builder` mà có giá trị mặc định đều PHẢI kèm `@Builder.Default`. Không có ngoại lệ.

---

### [Bug ID: #009] - ErrorCode.BAD_REQUEST không tồn tại

**Status:** ✅
**Mức độ:** 🔴

**Triệu chứng:**
- Build lỗi: `cannot find symbol: variable BAD_REQUEST, location: class ErrorCode`
- `FlashcardServiceImpl.java` line 99 dùng `ErrorCode.BAD_REQUEST` nhưng enum `ErrorCode` của project không có giá trị này.

**Nguyên nhân:**
Copy-paste từ codebase khác hoặc tham chiếu sai tên constant. Enum `ErrorCode` của project dùng `INVALID_REQUEST` thay vì `BAD_REQUEST`.

**Cách fix:**
```java
// ❌ Sai
.orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST));

// ✅ Đúng — dùng tên đúng trong project
.orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
```

**Kết quả:** Build thành công, không còn compilation error.

**Ghi chú:** Luôn kiểm tra `ErrorCode.java` trước khi dùng — mỗi project có naming convention riêng cho error codes. Dùng IDE autocomplete thay vì gõ tay.

---

### [Bug ID: #010] - Import vue-toastification không tồn tại

**Status:** ✅
**Mức độ:** 🟡

**Triệu chứng:**
- Frontend test fail: `Failed to resolve import "vue-toastification"`.
- `FlashcardPage.vue` import `useToast` từ `vue-toastification` nhưng package không nằm trong `package.json`.

**Nguyên nhân:**
Khi migrate component từ Stitch prototype sang Vue production, giữ lại import từ thư viện mà prototype dùng nhưng codebase production chưa cài. Stitch prototype có thể có `vue-toastification` trong dependencies riêng.

**Cách fix:**
Thay thế toast bằng cơ chế error hiển thị nội bộ (`errorMsg` reactive state + `alert()` cho trường hợp critical):
```javascript
// ❌ Import thư viện không tồn tại
import { useToast } from 'vue-toastification';
const toast = useToast();
toast.error('Lỗi...');

// ✅ Dùng reactive state nội bộ
const errorMsg = ref('');
errorMsg.value = 'Lỗi...';
// Hoặc alert() cho trường hợp cần thông báo ngay
alert('Lỗi khi lưu kết quả');
```

**Kết quả:** Tất cả 41/41 frontend tests PASS. Không cần thêm dependency vào `package.json`.

**Test lại:** `npm run test` — 9/9 test suites, 41/41 tests pass.

**Ghi chú:** Khi migrate UI từ prototype, luôn kiểm tra `package.json` production vs prototype để phát hiện dependency mismatch. Ưu tiên dùng những gì đã có sẵn thay vì cài thêm thư viện chỉ vì prototype dùng.

---

### [Bug ID: #011] - Fully Qualified Name (FQN) inline thay vì import statement

**Status:** ✅
**Mức độ:** 🟢

**Triệu chứng:**
- Code dùng `private final com.japaneselearning.module_leaderboard.service.ScoringService scoringService;` thay vì import + tên class ngắn.
- Code hoạt động đúng nhưng vi phạm Java convention, khó đọc, IDE warning.

**Nguyên nhân:**
Khi thêm dependency mới từ module khác bằng code generation (AI hoặc template), không tự động thêm import statement ở đầu file mà dùng FQN inline cho nhanh.

**Cách fix:**
```java
// ❌ FQN inline — khó đọc, không đúng convention
private final com.japaneselearning.module_leaderboard.service.ScoringService scoringService;

// ✅ Import chuẩn
import com.japaneselearning.module_leaderboard.service.ScoringService;
// ...
private final ScoringService scoringService;
```

Fix áp dụng cho 3 file: `LearningServiceImpl.java`, `QuizLearningServiceImpl.java`, `FlashcardServiceImpl.java`.

**Kết quả:** Code đúng convention, IDE không còn warning.

**Test lại:** `mvn clean install -DskipTests` — BUILD SUCCESS.

**Ghi chú:** Luật: KHÔNG BAO GIỜ dùng FQN trong field/method body. Ngoại lệ duy nhất: khi 2 class cùng tên từ 2 package khác nhau cần dùng cùng lúc (ví dụ `java.util.Date` và `java.sql.Date`), chỉ import 1 cái và FQN cái còn lại. Nhưng đó là smell → nên rename hoặc dùng alias.

---

### [Bug ID: #012] - @Cacheable + getCurrentUser() gây cache pollution

**Status:** ✅
**Mức độ:** 🟡

**Triệu chứng:**
- `LeaderboardServiceImpl.getLeaderboard()` dùng `@Cacheable` với key `"#timeframe + '-' + #page + '-' + #size"`.
- Method bên trong gọi `getCurrentUser()` để tính `isMe` flag và `currentUserRanking`.
- User A load leaderboard → kết quả cached với `isMe=true` cho User A và `currentUserRanking` của User A.
- User B load cùng timeframe/page → lấy kết quả cache → thấy `isMe=true` cho User A thay vì B, và nhận thông tin ranking của User A.

**Nguyên nhân:**
Cache key không chứa userId và response mang tính cá nhân hóa (personalized state) → tất cả user share cùng 1 cached result → `isMe` flag và `currentUserRanking` bị rò rỉ cho mọi user ngoại trừ user đầu tiên trigger cache.

**Cách fix:**
- Loại bỏ `@Cacheable` khỏi `LeaderboardServiceImpl.getLeaderboard()` (Option A - fix an toàn, triệt để rủi ro rò rỉ dữ liệu).
- Mỗi request tính toán độc lập `isMe` theo `currentUser.getId()` và lấy `currentUserRanking` (chỉ khi `!currentUser.getIsPrivateLeaderboard()`).
- Bổ sung test kiểm chứng:
  1. `getLeaderboard_ShouldNotBeAnnotatedWithCacheable`: Dùng reflection xác nhận method không bị gắn `@Cacheable`.
  2. `getLeaderboard_SequentialCallsByUserAAndUserB_ShouldPersonalizePerUserWithoutCachePollution`: Gọi liên tiếp với User A rồi User B trên cùng tham số (timeframe, page, size) và xác nhận User B nhận đúng `isMe` cho User B, không bị reuse kết quả của User A.
  3. `getLeaderboard_WhenPrivate_ShouldNotReturnMyRanking`: Xác nhận user private trả về `currentUserRanking = null` và không query rank.

**Test lại:**
- `mvn -Dtest=LeaderboardServiceImplTest test` (JDK 21) — 4/4 tests PASS.

**Ghi chú:** Đây là anti-pattern phổ biến khi dùng `@Cacheable`: đặt user-specific logic (session, SecurityContext) bên trong cached method với public cache key. Rule: cached method PHẢI stateless — không phụ thuộc vào user hiện tại. Nếu sau này cần cache danh sách public, tách riêng method cache DTO ranking public (không chứa `isMe`/`currentUserRanking`) và kết hợp personalization ở tầng ngoài.

---

### [Bug ID: #013] - Frontend Leaderboard đọc sai response shape và gọi sai endpoint

**Status:** ✅
**Mức độ:** 🔴

**Triệu chứng:**
- `LeaderboardPage.vue` đọc dữ liệu theo dạng `response.data.rankings`.
- `leaderboard.service.js` trả `response.data` từ Axios, tức là toàn bộ `ApiResponse`.
- Backend thật trả dữ liệu trong `ApiResponse.result`, nên production UI có nguy cơ không render ranking dù API trả 200.
- Service cũng gọi `/leaderboards`, trong khi backend route thật là `/api/v1/leaderboards`.

**Nguyên nhân:**
Frontend service/page/test không thống nhất contract:
```javascript
// Sai trong test cũ
{ data: { rankings: [] } }

// Backend thật
{ code: 1000, result: { rankings: [] } }
```

**Cách fix:**
```javascript
// leaderboard.service.js
const response = await api.get('/v1/leaderboards', { params });
return response.data?.result || { rankings: [], currentUserRanking: null };

// LeaderboardPage.vue
const result = await leaderboardService.getLeaderboard(timeframe, 0, 50);
leaderboardData.value = result.rankings || [];
```

**Kết quả:**
- `LeaderboardPage` đọc dữ liệu đúng contract.
- `LeaderboardPage.spec.js` mock theo result đã unwrap.
- Frontend tests liên quan pass.

**Test lại:**
- `npm run test -- LeaderboardPage.spec.js ProfilePage.spec.js` PASS.
- `npm run build` PASS.

**Ghi chú:** Khi service quyết định unwrap `ApiResponse.result`, component không nên biết wrapper `code/message/result` nữa. Test phải mock đúng return value của service, không mock theo cảm tính.

---

### [Bug ID: #014] - Flyway V6 Migration cú pháp MariaDB shorthand gây lỗi H2 in-memory test database

**Status:** ✅
**Mức độ:** 🔴

**Triệu chứng:**
- Khi chạy `mvn verify` hoặc bất kỳ test suite nào dùng H2 database với Flyway migration enabled, build bị crash ở migration `V6__create_leaderboard_tables.sql`.
- Error log: `org.h2.jdbc.JdbcSQLSyntaxErrorException: Syntax error in SQL statement "ALTER TABLE USERS ADD COLUMN LEVEL INT NOT NULL DEFAULT 1, ADD COLUMN [*]CURRENT_STREAK INT..."`

**Nguyên nhân:**
- File `V6__create_leaderboard_tables.sql` sử dụng cú pháp mở rộng của MySQL/MariaDB: `ALTER TABLE users ADD COLUMN col1 ..., ADD COLUMN col2 ...;` (thêm nhiều cột trong 1 statement phân tách bằng dấu phẩy) và `UNIQUE KEY uk_name (...)`.
- H2 (chế độ tiêu chuẩn / PostgreSQL / Oracle mode) không hỗ trợ comma-separated `ADD COLUMN` trong một câu lệnh đơn, và yêu cầu cú pháp chuẩn `CONSTRAINT uk_name UNIQUE (...)`.

**Cách fix:**
- Tách từng câu lệnh `ADD COLUMN` thành các statement riêng biệt kết thúc bằng dấu chấm phẩy:
```sql
ALTER TABLE users ADD COLUMN level INT NOT NULL DEFAULT 1;
ALTER TABLE users ADD COLUMN current_streak INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN last_activity_date DATE;
ALTER TABLE users ADD COLUMN country VARCHAR(10) DEFAULT '🇻🇳';
ALTER TABLE users ADD COLUMN is_private_leaderboard BOOLEAN NOT NULL DEFAULT FALSE;
```
- Chuẩn hóa cú pháp unique key:
```sql
CONSTRAINT uk_user_period UNIQUE (user_id, period_type, period_value)
```

**Kết quả:**
- Flyway migrations chạy mượt mà trên cả MariaDB (production/dev) và H2 (in-memory testing).

**Test lại:**
- `mvn clean verify` — Flyway validate & migrate 6/6 migrations thành công trên H2.

---

### [Bug ID: #015] - Thiếu Mock ScoringService trong Flashcard & Quiz Integration Tests gây NPE và FK Constraint Violation

**Status:** ✅
**Mức độ:** 🟠

**Triệu chứng:**
- `FlashcardServiceImplTest.reviewCard` bị fail do `NullPointerException`.
- `QuizLearningWorkflowIT.submitAttempt_ValidAnswers_CalculatesScoreCorrectly`, `QuizTimeLimitIT.submitAttempt_WithinTimeLimit_Success`, và `QuizTimeLimitIT.submitAttempt_UntimedQuiz_SuccessRegardlessOfTime` bị fail với lỗi:
  `Referential integrity constraint violation: PUBLIC.LEARNING_ACTIVITIES FOREIGN KEY(USER_ID) REFERENCES PUBLIC.USERS(ID)`

**Nguyên nhân:**
- Khi tích hợp module gamification/leaderboard, logic nộp bài quiz thành công hoặc ôn tập flashcard tự động gọi `scoringService.addXp()`.
- Trong `FlashcardServiceImplTest`, bean `scoringService` chưa được `@Mock` dẫn đến null khi gọi method.
- Trong `QuizLearningWorkflowIT` và `QuizTimeLimitIT`, `@MockBean` được cấu hình cho `userRepository` (mocked in-memory, không lưu vào DB), nhưng `ScoringService` không được mock, khiến Spring Boot inject real `ScoringServiceImpl`. Khi `ScoringServiceImpl` persist entity `LearningActivity`, H2 kiểm tra foreign key `user_id` và throw constraint violation.

**Cách fix:**
- Trong `FlashcardServiceImplTest.java`: Bổ sung `@Mock private ScoringService scoringService;`.
- Trong `QuizLearningWorkflowIT.java` và `QuizTimeLimitIT.java`: Bổ sung `@MockBean private ScoringService scoringService;` và stub `when(userRepository.findById(...))`.

**Kết quả:**
- Toàn bộ 68 unit tests và 53 integration tests của backend chạy hoàn hảo (121/121 tests PASS).

**Test lại:**
- `mvn clean verify` — BUILD SUCCESS 100%.

---

### [Bug ID: #016] - Vue Router Guards chỉ đọc to.meta.role thay vì duyệt to.matched làm hở quyền truy cập child routes

**Status:** ✅
**Mức độ:** 🟡

**Triệu chứng:**
- Trong `guards.js`, role check được viết: `const requiredRole = to.meta.role`.
- Trong routing configuration (`index.js`), meta `{ requiresAuth: true, role: 'ADMIN' }` được khai báo trên route cha (`/admin`), các route con (ví dụ `users`, `courses`, `quizzes/:id/builder`) không khai báo lại thuộc tính `role`.
- Khi navigate tới child route, nếu router hoặc test context không sao chép phẳng `meta` sang child record, `to.meta.role` bị `undefined`, dẫn đến việc role guard bị bỏ qua.

**Nguyên nhân:**
- Logic guard không duyệt ngược qua danh sách `to.matched` để tìm kiếm role được áp đặt bởi route cha.

**Cách fix:**
- Sử dụng fallback kiểm tra trong `to.matched`:
```javascript
const requiredRole = to.matched?.slice().reverse().find(record => record.meta?.role)?.meta?.role || to.meta?.role
```
- Bổ sung 2 test cases mới trong `guards.spec.js` để kiểm chứng:
  1. Student truy cập route con của Admin (`/admin/users`) bị chặn và chuyển hướng về `/student/dashboard`.
  2. Admin truy cập route Student (`/student/dashboard`) bị chuyển hướng về `/admin/dashboard`.

**Kết quả:**
- Tuyệt đối ngăn chặn role mismatch giữa route cha và route con.

**Test lại:**
- `npm run test` — 11/11 test files, 47/47 tests PASS.
- `npm run build` — built clean.

---

### [Bug ID: #017] - Production Hardening: Hở port DB/Redis public, thiếu .env trong root .gitignore và rate limiter không nhận FAIL_OPEN dạng underscore

**Status:** ✅
**Mức độ:** 🟠

**Triệu chứng:**
1. Root `.gitignore` không có quy tắc loại trừ `.env` hay `.env.prod`. Khi DevOps copy `.env.prod.example` ra `.env.prod` theo tài liệu hướng dẫn, file `.env.prod` bị git xem là untracked và rất dễ bị commit lộ mật khẩu thật lên repository.
2. `docker-compose.prod.yml` ánh xạ ports của MariaDB (`3306:3306`) và Redis (`6379:6379`) mặc định lên `0.0.0.0`, mở trực tiếp cổng database và cache ra mạng internet công cộng của VPS.
3. Trong `RedisRateLimiterService`, logic kiểm tra policy chỉ so sánh `POLICY_FAIL_OPEN.equalsIgnoreCase(redisFailurePolicy)` với `POLICY_FAIL_OPEN = "fail-open"`. Nếu DevOps thiết lập biến môi trường theo chuẩn hoa/gạch dưới thông dụng `APP_RATE_LIMIT_REDIS_FAILURE_POLICY=FAIL_OPEN` hoặc `fail_open`, so sánh trả về `false`, khiến hệ thống bị ép chạy ở chế độ `FAIL_CLOSED` và chặn toàn bộ request khi Redis bảo trì.
4. `application-prod.yml` thiếu khai báo placeholder cho `app.cors.allowed-origins`, `app.cookie.same-site`, `app.cookie.secure`, `app.media.trusted-domains`, khiến profile prod fallback về `localhost:5173` của môi trường dev.

**Nguyên nhân:**
- Cấu hình deployment ban đầu tập trung vào chạy được mẫu mà chưa rà soát kỹ các khía cạnh bảo mật hạ tầng và độ linh hoạt khi cấu hình qua biến môi trường.

**Cách fix:**
1. Trong `.gitignore` gốc: Bổ sung `*.env`, `.env`, `.env.*` cùng ngoại lệ `!.env*.example`.
2. Trong `docker-compose.prod.yml`:
   - Giới hạn binding port DB và Redis về localhost (`127.0.0.1:3306:3306`, `127.0.0.1:6379:6379`).
   - Hỗ trợ override biến qua `${APP_RATE_LIMIT_REDIS_FAILURE_POLICY:-FAIL_CLOSED}`, `${APP_CORS_ALLOWED_ORIGINS:-...}`, `${APP_COOKIE_SECURE:-true}`, `${APP_COOKIE_SAME_SITE:-Strict}`, `${APP_MEDIA_TRUSTED_DOMAINS:-}`.
3. Trong `RedisRateLimiterService.java`:
   - Bổ sung kiểm tra `|| "fail_open".equalsIgnoreCase(redisFailurePolicy)`.
4. Trong `application-prod.yml`, `.env.prod.example` và `backend/.env.example`: Đồng bộ đầy đủ các placeholder và hướng dẫn cấu hình production.

**Kết quả:**
- Đảm bảo an toàn tuyệt đối khi triển khai VPS, không sợ commit nhầm secret thật, không lộ port DB ra internet, và hệ thống rate limiter phản ứng chính xác với mọi định dạng cấu hình.

**Test lại:**
- `git check-ignore -v .env.prod` -> khớp ignore rule.
- `mvn test -Dtest=RedisRateLimiterFailurePolicyTest` -> 9/9 tests PASS.
- `mvn clean verify` -> BUILD SUCCESS (68 unit tests, 53 integration tests).
- `npm run test` -> 47/47 tests PASS.
- `npm run build` -> build thành công sạch sẽ.

---

### [Bug ID: #018] - Lỗi ERESOLVE khi chạy npm ci do xung đột peerDependency giữa @pinia/testing và pinia

**Status:** ✅
**Mức độ:** 🟡

**Triệu chứng:**
- Khi chạy lệnh cài đặt chuẩn CI `npm ci` trong thư mục `frontend/`, npm báo lỗi và dừng tiến trình:
  `npm error code ERESOLVE`
  `npm error Could not resolve dependency: peer pinia@">=4.0.2" from @pinia/testing@2.0.1`
  `npm error Found: pinia@3.0.4`

**Nguyên nhân:**
- `package.json` khai báo `"pinia": "^3.0.4"` cùng `"@pinia/testing": "^2.0.1"`.
- Phiên bản `@pinia/testing@2.0.1` yêu cầu peer dependency `pinia: >=4.0.2`.
- Khi dùng `npm install` cục bộ trước đây có thể đã dùng `--force` hoặc `--legacy-peer-deps`, nhưng `npm ci` trong pipeline CI tuân thủ nghiêm ngặt peer dependency và từ chối cài đặt nếu có xung đột phiên bản.

**Cách fix:**
- Hạ version devDependency `@pinia/testing` về `^1.0.3` (phiên bản chính thức tương thích với `pinia: >=3.0.4`).
- Cập nhật lại `frontend/package.json` và `frontend/package-lock.json` qua `npm install --save-dev @pinia/testing@1.0.3`.

**Kết quả:**
- `npm ci` chạy mượt mà, cài đặt đúng 309 packages trong 6 giây không còn lỗi ERESOLVE.
- Toàn bộ 47 frontend unit tests và `npm run build` tiếp tục hoạt động chính xác 100%.

**Test lại:**
- `npm ci` -> exit code 0.
- `npm run test` -> 47/47 tests PASS.
- `npm run build` -> built in 1.97s, sạch sẽ.

### [Bug ID: #019] - Trạng thái ghi danh không đồng bộ tại Course Detail và CTA gây hiểu nhầm cho học viên đã đăng nhập

**Status:** ✅
**Mức độ:** 🟡

**Triệu chứng:**
- Học viên đã ghi danh khóa học khi truy cập trang chi tiết khóa học (`/courses/:id`) vẫn thấy nút "Đăng ký khóa học ngay". Khi bấm vào, backend ném exception `ALREADY_ENROLLED (4009)`.
- Thanh CTA nổi trên mobile layout không có nút "Tiếp tục học" khi học viên đã ghi danh, chỉ hiển thị "Đăng ký ngay".
- Navbar desktop thiếu link quay về "Trang chủ" và không có link trực tiếp tới "Khóa học của tôi" (`/student/my-courses`).
- Banner CTA chân trang chủ (`HomePage.vue`) vẫn hiển thị "Đăng ký tài khoản miễn phí" ngay cả khi học viên đang đăng nhập.

**Nguyên nhân:**
- API Public Course Detail `GET /api/public/courses/{id}` chỉ trả về thông tin khóa học mà không phản hồi thông tin trạng thái học của user hiện tại (`isEnrolled`, `lastLessonId`), dẫn đến frontend phải tự fetch thêm API khác hoặc phụ thuộc vào danh sách khóa học client-side.
- Logic kiểm tra `isEnrolled` ở client không đồng bộ tức thời với response của trang chi tiết khóa học.
- Navbar và HomePage chưa đọc `authStore.isAuthenticated` để render CTA theo ngữ cảnh (context-aware).

**Cách fix:**
- **Backend:** Thêm `isEnrolled` và `lastLessonId` vào `CourseDetailPublicRes`. Trong `CoursePublicServiceImpl`, đọc `SecurityContextHolder` một cách an toàn (không ép buộc đăng nhập, khách vãng lai vẫn xem bình thường); nếu đã đăng nhập, truy vấn `CourseEnrollmentRepository` và `LessonProgressRepository` để trả về trạng thái ghi danh và bài học gần nhất.
- **Frontend CourseDetailPage:** Ưu tiên dùng `course.isEnrolled` và `course.lastLessonId` từ backend để chuyển CTA ngay lập tức thành "▶ Tiếp tục học" kèm nhãn "✓ Đã ghi danh", đồng bộ cho cả desktop và mobile bar. Thêm fallback kiểm tra lỗi `4009` khi bấm enroll.
- **Frontend Navbar & HomePage:** Bổ sung link "Trang chủ" và "Khóa học của tôi" trên Navbar khi user đã login; đổi CTA banner ở HomePage thành "Vào học ngay →" dẫn tới dashboard nếu đã đăng nhập.

**Kết quả:**
- Toàn bộ flow khám phá, ghi danh, quay lại học diễn ra mượt mà, không còn lỗi 4009 hay nút bấm gây hiểu nhầm.
- Backend test `CoursePublicControllerIT` (3 test cases) PASS.
- Frontend test 49/49 PASS, build hoàn tất không lỗi.

### [Bug ID: #020] - Lỗi Nested Interactive Controls trong Course Cards, Thiếu Parity Navigation Mobile/Desktop, và Sidebar Admin Không Responsive Trên Mobile

**Status:** ✅
**Mức độ:** 🟡

**Triệu chứng:**
- Thẻ khóa học trên trang chủ (`HomePage.vue`) và danh sách khóa học (`CourseListPage.vue`) bọc toàn bộ thẻ bằng thẻ `router-link` (thẻ `<a>`) nhưng bên trong lại chứa thẻ `<button>`, vi phạm chuẩn ngữ nghĩa HTML5 (nested interactive controls) và gây lỗi bubbling sự kiện click, đồng thời gây khó khăn cho trình đọc màn hình / bàn phím.
- Navbar trên mobile thiếu liên kết "Trang chủ" dù desktop có, làm mất tính nhất quán giữa các thiết bị.
- Tài khoản Admin khi đăng nhập vẫn nhìn thấy các liên kết dành riêng cho Student (`Flashcards`, `Bảng xếp hạng`) trên cả desktop navbar và mobile menu; khi nhấn vào sẽ bị route guard chuyển hướng.
- Layout Admin (`AdminLayout.vue`) dùng sidebar cố định `w-56`, trên màn hình nhỏ/di động chiếm phần lớn diện tích khiến nội dung chính bị đè nén, không có cơ chế ngăn kéo (drawer).
- Footer chứa các liên kết chết `href="#"` (nhảy lên đầu trang) và các liên kết JLPT đều chỉ trỏ về `/courses` thay vì kèm query filter cụ thể.
- Màn hình học (`LearningLayout.vue`) trên di động ẩn liên kết "Khóa học của tôi" và "Khám phá khóa học", bắt buộc học viên phải nhấn vào logo.

**Nguyên nhân:**
- Chưa tách bạch ngữ nghĩa giữa phần tử điều hướng và phần tử hiển thị (Visual CTA vs Interactive Button).
- Thiếu các directive điều kiện theo role (`v-if="!isAdmin"`) tại thanh điều hướng cho các tính năng học viên.
- Chưa thiết kế Responsive Navigation Drawer cho giao diện quản trị viên.
- Footer dùng dữ liệu placeholder tĩnh và router-link chưa gắn query parameter.

**Cách fix:**
- **Thẻ khóa học:** Chuyển thẻ `<button>` bên trong `<router-link>` thành `<span class="inline-flex ...">` có cùng styling. Đối với `MyCourseCard`, chuẩn hóa container thành nút điều hướng có tính tiếp cận (`role="button"`, `tabindex="0"`, hỗ trợ Enter & Space) và chuyển nút bên trong thành `span`.
- **Navbar:** Thêm liên kết "Trang chủ" vào mobile menu. Bổ sung `v-if="!isAdmin"` cho `Flashcards`, `Bảng xếp hạng`, `Khóa học của tôi`, `Profile` trên desktop và mobile.
- **AdminLayout:** Chuyển sidebar desktop sang `hidden md:flex`, bổ sung nút Hamburger và Mobile Drawer trượt từ bên trái kèm backdrop làm mờ, hỗ trợ phím Escape.
- **LearningLayout:** Thêm nút mũi tên Back trên mobile header dẫn về `/student/my-courses` và bổ sung link "Khóa học của tôi" + "Khám phá khóa học" trong dropdown tài khoản.
- **Footer:** Chuyển các link JLPT sang `:to="{ path: '/courses', query: { level } }"`, loại bỏ hoàn toàn `href="#"`, chuyển nút mạng xã hội sang trạng thái `disabled` kèm `aria-disabled="true"` và tooltip `title="Sắp ra mắt"`.
- **Token:** Thay thế toàn bộ class `rounded-[24px]` và `rounded-[32px]` thành `rounded-2xl` và `rounded-3xl`.

**Kết quả:**
- 14/14 test suites passed (57/57 tests pass, bao gồm 8 unit tests mới).
- `npm run build` thành công trong 1.98s.
- Kiểm tra trực quan bằng Browser Subagent xác nhận giao diện desktop và mobile menu hiển thị chuẩn xác, không còn phần tử lỗi.

---

### [18/10/2026] Lỗ hổng lưu trữ Refresh Token Plaintext & Lỗi Function "SHA2" not found trong Flyway H2 Test

**Triệu chứng:**
1. **Lỗ hổng bảo mật nghiêm trọng (CWE-312 / OWASP A02: Cryptographic Failures):** Refresh Token được lưu trữ dưới dạng chuỗi thô (plaintext JWT) trong bảng `refresh_tokens`. Trong trường hợp database bị rò rỉ dữ liệu (SQL dump, compromised backup, hoặc log injection), kẻ tấn công có thể trích xuất toàn bộ Refresh Token của tất cả tài khoản và chiếm đoạt phiên mà không cần mật khẩu.
2. **Lỗi kiểm thử Flyway trong môi trường H2 (CI/CD):** Khi thêm script Flyway `V8__hash_refresh_tokens.sql` chứa hàm SQL `SHA2(token, 256)` và cú pháp `ALTER TABLE refresh_tokens DROP INDEX token`, quá trình chạy `mvn test` sử dụng H2 in-memory database bị ném ngoại lệ `FlywaySqlScriptException: Function "SHA2" not found (SQL State 90022)` làm sập toàn bộ ApplicationContext của 37 integration tests.

**Nguyên nhân:**
1. Thiết kế ban đầu lưu trực tiếp chuỗi token sinh ra từ `JwtUtil.generateRefreshToken()` vào cột `token VARCHAR(500) UNIQUE`.
2. H2 Database không hỗ trợ cú pháp SQL riêng biệt của MariaDB/MySQL (`SHA2()` và `ALTER TABLE ... DROP INDEX`). Trong khi đó, `src/test/resources/application.yml` chưa cấu hình tắt Flyway cho test profile (`spring.flyway.enabled`), dẫn đến việc cả Flyway và Hibernate `ddl-auto: create-drop` cùng chạy chồng chéo trên H2 in-memory DB.

**Cách fix:**
1. **Hash Token At Rest:**
   - Tạo `TokenHashUtil.hashToken()` tính toán SHA-256 hex digest chuẩn mực.
   - Bổ sung cột `token_hash VARCHAR(64)` vào Entity `RefreshToken` và CSDL.
   - `AuthServiceImpl` chỉ lưu `tokenHash`, đặt `token` plaintext thành `null` khi đăng nhập hoặc xoay vòng token.
2. **Zero-Downtime Safe Migration Strategy (Dual-Read / Lazy Upgrade):**
   - Viết hàm `findRefreshToken()` tìm trước bằng `findByTokenHash()`. Nếu là bản ghi cũ, fallback sang `findByToken()`, tự động băm, xóa plaintext và lưu lại ngay trong transaction mà không làm gián đoạn người dùng.
3. **Phân tách môi trường Test & Flyway:**
   - Cấu hình `spring.flyway.enabled: false` trong `backend/src/test/resources/application.yml` để H2 thuần túy khởi tạo bảng thông qua Hibernate schema generation dựa trên JPA Entity annotations.
   - Giữ nguyên `V8__hash_refresh_tokens.sql` cho môi trường MariaDB/MySQL production.
4. **Bổ sung kiểm thử tự động:**
   - Tạo `TokenHashUtilTest`, `AuthServiceImplTest` (kiểm thử băm token, rotation, reuse detection và lazy upgrade).
   - Cập nhật `AuthControllerIT` và bổ sung unit test frontend `auth.service.spec.js`, `auth.store.spec.js`.

**Kết quả:**
- Toàn bộ 97/97 backend integration & unit tests PASS (0 failure, 0 error).
- Toàn bộ 62/62 frontend unit tests PASS.
- Mọi Refresh Token mới đều được mã hóa một chiều an toàn ở trạng thái nghỉ (at rest), vận chuyển an toàn duy nhất qua HttpOnly cookie.

---

### [Bug ID: #021] - Sửa nội dung Flyway V8 sau khi migration đã phát hành

**Status:** ✅
**Mức độ:** 🔴

**Triệu chứng:**
- Task payment đã thay đổi nội dung `V8__hash_refresh_tokens.sql` để thêm các câu lệnh `IF EXISTS`.
- Với database đã chạy V8 từ hotfix refresh-token, Flyway sẽ phát hiện checksum khác với bản ghi trong `flyway_schema_history` và từ chối khởi động application.

**Nguyên nhân:**
- Nhầm lẫn giữa việc viết DDL có thể chạy lại và nguyên tắc của Flyway versioned migration. Migration đã được phát hành phải được xem là immutable.

**Cách fix:**
- Khôi phục byte-for-byte nội dung `V8__hash_refresh_tokens.sql` đã phát hành.
- Giữ toàn bộ schema payment trong migration mới `V9__create_payment_tables.sql`.
- Với migration local bị lỗi dở dang, kiểm tra nguyên nhân trước khi chạy `flyway:repair`; không sửa file migration cũ để né checksum lỗi.

**Kết quả:**
- Database đã chạy hotfix V8 sẽ tiếp tục pass Flyway checksum validation.
- Payment schema vẫn được áp dụng tuần tự qua V9 mà không làm thay đổi lịch sử migration.

---

### [Bug ID: #022] - Vue Test Utils RouterLink Stub Slot Truncation & ICU Currency Delimiter Differences

**Status:** ✅
**Mức độ:** 🟡

**Triệu chứng:**
1. Trong unit test `CheckoutPage.spec.js`, `expect(wrapper.text()).toContain('Khám phá khóa học')` bị fail mặc dù trong template có thẻ `<router-link to="/courses">Khám phá khóa học</router-link>`.
2. Assertion `expect(wrapper.text()).toContain('500.000đ')` bị fail trong môi trường test Node.js / JSDOM vì chuỗi thực tế trả về là `500,000đ`.

**Nguyên nhân:**
1. Khi cấu hình `stubs: { RouterLink: true }` trong `mount()` của `@vue/test-utils`, component `<router-link>` bị thay thế bằng component stub rỗng `<router-link-stub></router-link-stub>` mà không render nội dung slot bên trong (slot children bị nuốt chửng).
2. Hàm `Intl.NumberFormat('vi-VN')` phụ thuộc vào bảng mã ICU (International Components for Unicode) được đóng gói trong Node.js binary. Trên một số phiên bản Node / môi trường OS, dấu phân cách hàng nghìn của locale `vi-VN` có thể là dấu chấm (`.`) hoặc dấu phẩy (`,`).

**Cách fix:**
1. Loại bỏ stubbing `RouterLink: true` thủ công và sử dụng router instance thật từ `createRouter({ history: createWebHistory(), routes })` trong `global.plugins` để render đầy đủ thẻ `<a>` cùng nội dung slot.
2. Dùng biểu thức chính quy `expect(wrapper.text()).toMatch(/500[.,]000đ/)` để tương thích linh hoạt với cả hai chuẩn phân tách hàng nghìn của các runtime Node.js khác nhau.

**Kết quả:**
- 7/7 tests trong `CheckoutPage.spec.js` PASS 100% trên mọi môi trường Node / CI mà không phụ thuộc phiên bản ICU.

---

### [Bug ID: #023] - CORS localhost Fallback lộ trong Production Configuration

**Status:** ✅
**Mức độ:** 🔴

**Triệu chứng:**
- `CorsConfig.java` chứa logic fallback: nếu `app.cors.allowed-origins` trống hoặc chưa được cung cấp, tự động sử dụng `http://localhost:5173` làm allowed origin.
- Khi deploy production và quên cung cấp biến `APP_CORS_ALLOWED_ORIGINS` trong `.env.prod`, ứng dụng khởi động thành công với CORS cho phép `http://localhost:5173` — tạo vector tấn công cho malware trên máy nạn nhân hoặc kẻ tấn công cùng mạng LAN gọi API production.

**Nguyên nhân:**
- Phát triển ban đầu ưu tiên developer convenience: nếu quên cấu hình CORS, ứng dụng vẫn hoạt động với frontend chạy trên `localhost:5173`. Logic này hợp lý cho development nhưng không bao giờ nên tồn tại khi profile production active.
- Thiếu cơ chế phân biệt hành vi giữa development và production cho CORS configuration.

**Cách fix:**
1. **Loại bỏ localhost fallback trong `CorsConfig.java`:** Khi `allowedOrigins` rỗng, không fallback sang bất kỳ giá trị nào. Ở development, `application.yml` cung cấp `http://localhost:5173` tường minh. Ở production, `application-prod.yml` sử dụng `${APP_CORS_ALLOWED_ORIGINS}` không có default.
2. **Thêm CORS validation trong `ProductionSecurityValidator`:** Kiểm tra danh sách origins không rỗng, không chứa `*`, không chứa `localhost`/`127.0.0.1`, bắt buộc `https://` scheme, không có trailing slash, và URI hợp lệ.
3. **Kết quả:** `ProductionSecurityValidator` chặn startup nếu CORS origins chưa cung cấp hoặc chứa localhost, buộc DevOps phải cung cấp danh sách origins HTTPS tường minh.

**Kết quả:**
- 6 unit tests CORS validation PASS (localhost, wildcard, non-HTTPS, trailing slash, empty list, valid HTTPS).
- Development workflow không bị ảnh hưởng (validator chỉ chạy dưới `@Profile("prod")`).

---

### [Bug ID: #024] - Docker Compose Port Binding Expose Database/Redis ra Public Internet

**Status:** ✅
**Mức độ:** 🔴

**Triệu chứng:**
- `docker-compose.prod.yml` sử dụng port mapping `ports: - "3306:3306"` và `ports: - "6379:6379"` cho database và Redis.
- Trên VPS có public IP, Docker bind các port này lên `0.0.0.0` (tất cả network interfaces), khiến MariaDB (3306) và Redis (6379) mở ra internet cho mọi IP kết nối trực tiếp.
- Đặc biệt: Docker tự thêm iptables rules cho port mapping, **bypass ufw/firewall rules** đã thiết lập. Ngay cả khi sysadmin đã cấu hình `ufw deny 3306`, Docker vẫn mở cổng này.

**Nguyên nhân:**
- Docker Compose mặc định bind port ra `0.0.0.0` khi không chỉ định IP prefix. Đây là hành vi documented nhưng rất dễ bị bỏ qua, đặc biệt khi copy-paste từ development sang production.
- Docker sử dụng NAT rules riêng trong iptables chain `DOCKER`, hoạt động trước chain `INPUT` nơi ufw rules được áp dụng. Kết quả là ufw "tưởng" đã chặn port nhưng thực tế Docker đã mở sẵn.

**Cách fix:**
1. **Bind port ra `127.0.0.1` tường minh:**
   ```yaml
   db:
     ports:
       - "127.0.0.1:3306:3306"
   redis:
     ports:
       - "127.0.0.1:6379:6379"
   ```
2. **Backend container giao tiếp qua Docker internal network:** `DB_URL=jdbc:mariadb://db:3306/...`, `REDIS_HOST=redis`. Không đi qua port mapping của host, sử dụng Docker DNS resolution giữa các container trong cùng network.
3. **Bổ sung Redis authentication:** `command: redis-server --requirepass "${REDIS_PASSWORD}"`. Redis mặc định không yêu cầu xác thực — nếu port bị expose, bất kỳ ai kết nối được đều có toàn quyền CRUD.

**Kết quả:**
- Database và Redis chỉ truy cập được từ host machine hoặc containers cùng Docker network.
- `ProductionSecurityValidator` kiểm tra `REDIS_HOST` không phải localhost, `REDIS_PASSWORD` không rỗng/placeholder, tạo lớp bảo vệ bổ sung.
- Nguyên tắc defense-in-depth: port binding + password authentication + firewall rules. Mỗi lớp bảo vệ độc lập, không lớp nào phụ thuộc hoàn toàn vào lớp khác.

---

### [Bug ID: #025] - Lỗ hổng nộp trùng câu hỏi (Duplicate Payload) và gửi câu hỏi ngoại lai (Foreign Question) trong Quiz Submission

**Status:** ✅
**Mức độ:** 🔴

**Triệu chứng:**
- Khi user nộp bài kiểm tra qua endpoint `POST /api/v1/learning/quizzes/{id}/submit`:
  1. User có thể gửi một mảng chứa nhiều bản ghi cho cùng một `questionId` (duplicate question payload). Hệ thống xử lý lặp lại trên cùng câu hỏi, dẫn đến nguy cơ cộng dồn điểm, sai lệch thống kê tổng số câu hỏi (`totalQuestions`), hoặc ghi đè kết quả tùy thuộc vào thứ tự duyệt mảng.
  2. User có thể nhúng `questionId` thuộc về một quiz khác hoặc question không hề tồn tại trong bài thi. Server query câu hỏi đó và tiến hành chấm điểm, dẫn đến lộ thông tin câu hỏi khác hoặc làm lệch thang điểm bài thi.
  3. Loại câu hỏi `MULTIPLE_CHOICE` chưa có logic chấm điểm nhiều lựa chọn và tính điểm thành phần (partial credit) có phạt câu sai; chỉ hỗ trợ `answerId` đơn lẻ của `SINGLE_CHOICE`.

**Nguyên nhân:**
- Backend `QuizLearningServiceImpl.submitQuiz()` duyệt trực tiếp danh sách `request.getAnswers()` mà không có lớp guard kiểm tra tính duy nhất (uniqueness) của `questionId` trong payload gửi lên.
- Backend không đối chiếu danh sách `questionId` gửi lên với danh sách câu hỏi thực tế thuộc sở hữu của `quizId` đang làm bài (`questionRepository.findByQuizIdOrderBySortOrderAsc(quizId)`).
- DTO `QuizSubmitAnswerReq` chỉ có trường `Long answerId`, chưa hỗ trợ `List<Long> answerIds` cho câu hỏi nhiều đáp án.

**Cách fix:**
1. **Duyệt và chặn trùng lặp câu hỏi (Duplicate Payload Guard):**
   ```java
   Set<Long> seenQuestionIds = new HashSet<>();
   for (QuizSubmitAnswerReq req : request.getAnswers()) {
       if (req.getQuestionId() != null && !seenQuestionIds.add(req.getQuestionId())) {
           throw new AppException(ErrorCode.INVALID_REQUEST, 
               "Dữ liệu nộp bài chứa câu hỏi trùng lặp: questionId=" + req.getQuestionId());
       }
   }
   ```
2. **Kiểm tra quyền sở hữu câu hỏi (Foreign Question Scope Guard):**
   ```java
   Set<Long> quizQuestionIds = questions.stream()
       .map(Question::getId)
       .collect(Collectors.toSet());
   // Validate mọi submitted questionId phải nằm trong quizQuestionIds
   if (req.getQuestionId() != null && !quizQuestionIds.contains(req.getQuestionId())) {
       throw new AppException(ErrorCode.INVALID_REQUEST, 
           "Câu hỏi không thuộc bài kiểm tra này: questionId=" + req.getQuestionId());
   }
   ```
3. **Cài đặt thuật toán Partial Credit có phạt câu sai cho `MULTIPLE_CHOICE`:**
   $$\text{ratio} = \max\left(0, \frac{C_{\text{selected}} - W_{\text{selected}}}{C_{\text{total}}}\right), \quad \text{pointsEarned} = \text{round}(\text{ratio} \times \text{maxPoints}, 2)$$
   - Tránh gian lận chọn tất cả các đáp án (ví dụ chọn cả 4 đáp án sẽ bị phạt trừ hết điểm thành 0).
4. **Cài đặt chuẩn hóa chuỗi `FILL_BLANK`:**
   - Cắt khoảng trắng đầu cuối (trim), gộp khoảng trắng thừa ở giữa (`\\s+` $\to$ `" "`), chuyển về chữ thường (lowercase) để so khớp nội dung linh hoạt mà vẫn chặt chẽ.

**Kết quả:**
- 100% server-authoritative scoring: client không thể can thiệp điểm số hay gửi payload dị thường.
- Xử lý mượt mà cả câu hỏi bỏ trống (unanswered questions tính 0 điểm, `isCorrect=false`).
- Đạt 15/15 unit test (`QuizLearningServiceImplTest`) và 12/12 integration test (`QuizLearningWorkflowIT`) pass 100%.

---

### [Bug ID: #026] - Jackson SetterlessProperty Gây UnsupportedOperationException Khi DTO Có Getter Trả Về Immutable Collection

**Status:** ✅
**Mức độ:** 🟠

**Triệu chứng:**
- Khi chạy Integration Test `QuizLearningWorkflowIT` gửi request submit quiz có body JSON:
  ```json
  {
    "answers": [
      { "questionId": 1, "answerId": 1 }
    ]
  }
  ```
- Endpoint trả về lỗi HTTP 500:
  ```
  org.springframework.http.converter.HttpMessageNotReadableException: JSON parse error: null
  Caused by: java.lang.UnsupportedOperationException
      at java.base/java.util.ImmutableCollections.uoe(ImmutableCollections.java:142)
      at java.base/java.util.ImmutableCollections$AbstractImmutableCollection.add(ImmutableCollections.java:147)
      at com.fasterxml.jackson.databind.deser.std.CollectionDeserializer._deserializeFromArray(CollectionDeserializer.java:361)
  ```

**Nguyên nhân:**
- Trong `QuizSubmitAnswerReq.java`, lập trình viên thêm helper method để hỗ trợ tương thích ngược:
  ```java
  public List<Long> getEffectiveAnswerIds() {
      if (answerIds != null && !answerIds.isEmpty()) return answerIds;
      if (answerId != null) return List.of(answerId); // List.of() trả về ImmutableCollections
      return Collections.emptyList();
  }
  ```
- Jackson ObjectMapper theo mặc định bật feature `MapperFeature.USE_GETTERS_AS_SETTERS` (còn gọi là *setterless collection/map property*):
  - Khi deserialize một object JSON, Jackson quét tất cả getter công khai.
  - Khi thấy getter `getEffectiveAnswerIds()` trả về một `Collection` (`List<Long>`) mà **không có setter** tương ứng (`setEffectiveAnswerIds`), Jackson suy luận rằng collection này đã được khởi tạo sẵn trong instance và cố gắng mutate nó bằng cách gọi getter rồi invoke `.add()` hoặc `.addAll()` các phần tử từ JSON vào collection đó!
  - Nhưng do `getEffectiveAnswerIds()` trả về `List.of()` hoặc `Collections.emptyList()` — vốn là các Java 9+ Immutable Collections — lệnh `.add()` ném ngay `UnsupportedOperationException`.

**Cách fix:**
- Thêm annotation `@com.fasterxml.jackson.annotation.JsonIgnore` trên helper getter trong DTO:
  ```java
  @JsonIgnore
  public List<Long> getEffectiveAnswerIds() {
      if (answerIds != null && !answerIds.isEmpty()) {
          return answerIds;
      }
      if (answerId != null) {
          return List.of(answerId);
      }
      return Collections.emptyList();
  }
  ```
- Việc đánh dấu `@JsonIgnore` báo hiệu cho Jackson hoàn toàn bỏ qua method này trong cả serialization lẫn deserialization, triệt tiêu tính năng setterless mutation.

**Kết quả:**
- JSON deserialization hoạt động trơn tru với cả legacy payload (`answerId: 1`) và modern payload (`answerIds: [1, 2]`).
- Tất cả MockMvc WebMvc integration test pass 100% không còn lỗi `HttpMessageNotReadableException`.

---

### [Bug ID: #027] - Lệnh Thu Hồi Phiên (Token Reuse Family Revocation) Bị Rollback Do Ném RuntimeException Trong Transaction Mặc Định

**Status:** ✅
**Mức độ:** 🔴

**Triệu chứng:**
- Khi phát hiện một refresh token cũ đã bị thu hồi được tái sử dụng (Token Reuse Attack / Replay Attack):
  - Log server hiển thị: `Token reuse detected! Revoking all tokens for user ID: ...` và `Revoked 1 active refresh token session(s) for user ID: ...`.
  - Nhưng sau đó, token đang hoạt động hợp lệ khác của user vẫn tiếp tục dùng để refresh thành công!
  - Kiểm tra database: cờ `revoked` của các token còn lại vẫn là `false` (0), không hề bị thu hồi như mong đợi.

**Nguyên nhân:**
- Phương thức `AuthServiceImpl.refreshToken()` được đánh dấu `@Transactional`.
- Khi phát hiện `refreshTokenEntity.getRevoked() == true`, hệ thống thực thi câu lệnh SQL:
  `UPDATE refresh_tokens SET revoked = true WHERE user_id = :userId`
  rồi ngay sau đó ném:
  `throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);`
- Vì `AppException` kế thừa từ `RuntimeException`, cơ chế mặc định của Spring Transaction Manager sẽ đánh dấu **RollbackOnly** cho toàn bộ transaction.
- Khi phương thức kết thúc bằng ngoại lệ này, câu lệnh UPDATE thu hồi token vừa chạy bị **Rollback** hoàn toàn, dữ liệu CSDL được khôi phục về trạng thái trước đó. Hành vi phòng vệ bảo mật bị vô hiệu hóa ngầm mà không có bất kỳ thông báo lỗi nào!

**Cách fix:**
- Cấu hình `@Transactional(noRollbackFor = AppException.class)` trên phương thức `refreshToken()`:
  ```java
  @Override
  @Transactional(noRollbackFor = AppException.class)
  public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
      ...
      if (Boolean.TRUE.equals(refreshTokenEntity.getRevoked())) {
          log.warn("Token reuse detected! Revoking all tokens for user ID: {}", refreshTokenEntity.getUser().getId());
          revokeUserSessions(refreshTokenEntity.getUser().getId());
          throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);
      }
      ...
  }
  ```
- Hoặc cấu hình `Propagation.REQUIRES_NEW` trên phương thức `revokeUserSessions(Long userId)` để lệnh thu hồi luôn được commit trong transaction riêng biệt.

**Kết quả:**
- Khi phát hiện token reuse, toàn bộ session của tài khoản bị thu hồi và commit ngay lập tức vào database trước khi ném lỗi HTTP 401.
- Kẻ tấn công hoặc thiết bị cũ hoàn toàn không thể tái sử dụng bất kỳ token nào trong gia đình token đó.
- Test `RefreshTokenLifecycleIT.refreshTokenRotation_AndTokenReuseDetection` pass 100%.

---

### [Bug ID: #028] - Vi Phạm Khóa Duy Nhất (Unique Constraint Violation) Trên `token_hash` Do Trùng Lặp Chuỗi JWT Trong Cùng Millisecond

**Status:** ✅
**Mức độ:** 🟠

**Triệu chứng:**
- Khi người dùng đăng nhập nhanh liên tiếp hoặc khi xoay vòng token (rotation) diễn ra trong cùng một millisecond (đặc biệt trong các integration test hoặc môi trường concurrency cao):
  - Database ném lỗi:
    ```
    org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException: Unique index or primary key violation: 
    "PUBLIC.CONSTRAINT_INDEX_C ON PUBLIC.REFRESH_TOKENS(TOKEN_HASH NULLS FIRST) VALUES ( '4e0948...b4c735' )"
    ```
  - Endpoint `/api/v1/auth/refresh-token` trả về mã lỗi HTTP 500 Internal Server Error.

**Nguyên nhân:**
- Bảng `refresh_tokens` có ràng buộc duy nhất trên cột `token_hash VARCHAR(64) UNIQUE`.
- Phương thức `JwtUtil.buildToken()` tạo token bằng các claims: `extraClaims` (chứa `id: userId`), `subject: email`, `issuedAt: System.currentTimeMillis()`, và `expiration: currentTime + ms`.
- Nếu hai token được sinh ra trong cùng một millisecond cho cùng một user, toàn bộ header và payload của JWT giống nhau từng byte.
- Hàm `TokenHashUtil.hashToken(token)` thực hiện SHA-256 trên chuỗi giống nhau sẽ tạo ra cùng một giá trị hash, dẫn đến vi phạm ràng buộc `UNIQUE` khi insert vào bảng `refresh_tokens`.

**Cách fix:**
- Bổ sung claim chuẩn RFC 7519 Section 4.1.7: `jti` (JWT ID) với giá trị UUID ngẫu nhiên vào `allClaims` khi tạo token trong `JwtUtil`:
  ```java
  private String buildToken(Map<String, Object> extraClaims, String subject, long expiration, SecretKey key) {
      Map<String, Object> allClaims = new HashMap<>();
      if (extraClaims != null) {
          allClaims.putAll(extraClaims);
      }
      allClaims.putIfAbsent("jti", java.util.UUID.randomUUID().toString());

      return Jwts.builder()
              .claims(allClaims)
              .subject(subject)
              .issuedAt(new Date(System.currentTimeMillis()))
              .expiration(new Date(System.currentTimeMillis() + expiration))
              .signWith(key)
              .compact();
  }
  ```
- Nhờ có `jti` độc nhất cho mỗi lần gọi, mọi chuỗi JWT luôn có nội dung khác nhau và sinh ra SHA-256 hash hoàn toàn khác nhau.

**Kết quả:**
- Triệt tiêu 100% rủi ro va chạm hash kể cả khi sinh hàng ngàn token trong cùng một millisecond.
- Tất cả các integration tests chạy song song hoặc liên tiếp đều pass mượt mà không gặp lỗi Unique Constraint.

---

### [Bug ID: #029] - Log Masking Substring Key Collision Trong Unit Test Assertion

**Status:** ✅
**Mức độ:** 🟡

**Triệu chứng:**
- Khi chạy kiểm thử `LogMaskingConverterTest.shouldMaskAllPasswordVariants`, test case bị fail với assertion error:
  ```
  Expecting actual:
    "{"currentPassword":"***REDACTED***","newPassword":"***REDACTED***","confirmPassword":"***REDACTED***"}"
  not to contain:
    "newPass"
  ```
- Build maven bị thất bại `BUILD FAILURE` mặc dù chuỗi log đã được thay thế `***REDACTED***`.

**Nguyên nhân:**
- Input của test case là:
  `{"currentPassword":"oldPass","newPassword":"newPass","confirmPassword":"newPass"}`
- Regex đã che mờ chính xác giá trị của trường thành:
  `{"currentPassword":"***REDACTED***","newPassword":"***REDACTED***","confirmPassword":"***REDACTED***"}`
- Tuy nhiên, câu lệnh assertion trong test kiểm tra:
  `assertThat(result).doesNotContain("newPass");`
- Từ khóa `"newPass"` vô tình là chuỗi con (substring) nằm ngay trong tên thuộc tính JSON `"newPassword"`.
- Vì tên trường `"newPassword"` không bị che mờ (chỉ giá trị value bị che mờ), kết quả log vẫn chứa chuỗi con `"newPass"` dẫn đến assertion fail oan.

**Cách fix:**
- Sử dụng các giá trị bí mật mẫu không trùng lặp và không phải là chuỗi con của tên trường JSON (ví dụ: `oldSecret123`, `myNewSecret456`):
  ```java
  @Test
  @DisplayName("Should mask currentPassword, newPassword, confirmPassword fields")
  void shouldMaskAllPasswordVariants() {
      String input = "{\"currentPassword\":\"oldSecret123\",\"newPassword\":\"myNewSecret456\",\"confirmPassword\":\"myNewSecret456\"}";
      String result = LogMaskingConverter.maskSensitiveData(input);

      assertThat(result).doesNotContain("oldSecret123");
      assertThat(result).doesNotContain("myNewSecret456");
      assertThat(result).contains("***REDACTED***");
  }
  ```

**Kết quả:**
- Test case kiểm tra chính xác giá trị nhạy cảm đã bị che mờ mà không bị nhầm lẫn với tên key JSON. Test pass 100%.

---

### [Bug ID: #030] - RateLimiterService Subclasses Constructor Breakage Khi Inject ObservabilityMetrics

**Status:** ✅
**Mức độ:** 🟠

**Triệu chứng:**
- Sau khi thêm metric `app_rate_limit_exceeded_total` vào `InMemoryRateLimiterService` và `RedisRateLimiterService`, quá trình biên dịch test (`mvn test-compile`) báo lỗi:
  ```
  [ERROR] RateLimiterServiceTest.java:[19,46] constructor InMemoryRateLimiterService in class com.japaneselearning.common.security.InMemoryRateLimiterService cannot be applied to given types;
    required: com.japaneselearning.common.observability.ObservabilityMetrics
    found:    no arguments
  [ERROR] RedisRateLimiterFailurePolicyTest.java:[33,19] constructor RedisRateLimiterService in class com.japaneselearning.common.security.RedisRateLimiterService cannot be applied to given types;
    required: org.springframework.data.redis.core.StringRedisTemplate,com.japaneselearning.common.observability.ObservabilityMetrics
    found:    org.springframework.data.redis.core.StringRedisTemplate
  ```

**Nguyên nhân:**
- Khi thêm trường `private final ObservabilityMetrics observabilityMetrics;` và đánh dấu `@RequiredArgsConstructor` (Lombok) trên `InMemoryRateLimiterService` và `RedisRateLimiterService`, Lombok tự động tạo constructor yêu cầu đầy đủ các trường `final`.
- Các lớp unit test cũ (`RateLimiterServiceTest` và `RedisRateLimiterFailurePolicyTest`) khởi tạo service trực tiếp bằng từ khóa `new InMemoryRateLimiterService()` (0 tham số) hoặc `new RedisRateLimiterService(redisTemplate)` (1 tham số) nên bị sai chữ ký hàm (constructor signature mismatch).

**Cách fix:**
- Cập nhật các test file truyền `ObservabilityMetrics` vào constructor:
  - Trong `RateLimiterServiceTest`: Khởi tạo `ObservabilityMetrics metrics = new ObservabilityMetrics(new SimpleMeterRegistry());` và truyền vào `new InMemoryRateLimiterService(metrics)`.
  - Trong `RedisRateLimiterFailurePolicyTest`: Bổ sung `@Mock private ObservabilityMetrics observabilityMetrics;` và truyền `new RedisRateLimiterService(redisTemplate, observabilityMetrics)`.
  - Đồng thời bổ sung mock `ObservabilityMetrics` trong `AuthServiceImplTest`, `QuizLearningServiceImplTest`, `PaymentServiceImplTest` để đảm bảo Mockito inject đầy đủ dependency khi test các tầng service.

**Kết quả:**
- Toàn bộ các unit test và integration tests biên dịch sạch sẽ và pass 100%.

---

### [Bug ID: #031] - Lỗi Sai Lệch Mã Băm Flyway Migration Version 8 (Checksum Mismatch) Do Revert File Mã Nguồn

**Status:** ✅
**Mức độ:** 🟠

**Triệu chứng:**
- Khi chạy lệnh khởi động ứng dụng backend thủ công bằng lệnh:
  `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
- Ứng dụng dừng khởi động ngay ở bước khởi tạo context (Spring Context Initialization Failure) với thông báo lỗi từ Flyway:
  ```text
  Validate failed: Migrations have failed validation
  Migration checksum mismatch for migration version 8
  -> Applied to database : 802283801
  -> Resolved locally    : 1094426290
  Either revert the changes to the migration, or run repair to update the schema history.
  ```

**Nguyên nhân:**
- Bảng `flyway_schema_history` lưu trữ mã băm CRC32 của mỗi file SQL migration khi áp dụng vào database.
- Trong lịch sử commit của dự án:
  - Ở commit `8000d6c3`, file `V8__hash_refresh_tokens.sql` từng được chỉnh sửa thêm các mệnh đề `IF NOT EXISTS` (tính ra mã băm checksum: `802283801`), và database MariaDB môi trường dev local của lập trình viên đã nạp migration này.
  - Sau đó ở commit `27c2f009`, file `V8` được revert về bản 24 dòng nguyên bản (khiến mã băm checksum tính từ file local đổi thành `1094426290`).
- Khi Spring Boot khởi động, bộ kiểm tra tính toàn vẹn của Flyway phát hiện file migration local có checksum khác với bản ghi đã lưu trong database, kích hoạt cơ chế bảo vệ tính bất biến và dừng ứng dụng (Fail-fast).

**Cách fix:**
- Cập nhật lại bản ghi lịch sử của version 8 trong bảng `flyway_schema_history` của CSDL MariaDB local để khớp với file mã nguồn hiện tại:
  ```sql
  UPDATE japanese_learning_db.flyway_schema_history 
  SET checksum = 1094426290 
  WHERE version = '8';
  ```
- Sau đó khởi động lại Spring Boot. Flyway tự động nhận diện bản ghi đã hợp lệ và tiếp tục thực thi các migration tiếp theo (`V10__add_refresh_token_lifecycle_indexes.sql`).

**Kết quả:**
- Flyway xác thực thành công 11 migrations, tự động áp dụng `V10`, và ứng dụng Spring Boot khởi động thành công trong ~10 giây.





