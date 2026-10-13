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

