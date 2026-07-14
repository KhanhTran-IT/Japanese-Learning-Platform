# Nhật Ký Học Tập

Tài liệu để ghi lại quá trình học tập, những kiến thức mới học được, và tiến độ phát triển dự án.

## Hướng dẫn ghi chép

- **Ghi hàng ngày hoặc theo từng phiên làm việc** để dễ theo dõi tiến độ
- **Viết ngắn gọn nhưng đầy đủ** - chỉ cần đủ chi tiết để nhớ lại sau này
- **Ghi theo format mẫu** ở phía dưới
- **Highlight những điểm quan trọng** bằng cách sử dụng `**đậm**` hoặc emoji
- **Link tới file liên quan** nếu cần thiết

## Template mẫu

```markdown
### [Ngày/Ngày giờ]

**Tập trung vào:** [Main task/feature được làm]

**Những gì đã học:**

- Kiến thức/kỹ năng 1
- Kiến thức/kỹ năng 2

**Code pattern hay:**
\`\`\`java
// Mã code hay gặp
\`\`\`

**Tiến độ:**

- [ ] Subtask 1
- [x] Subtask 2

**Ghi chú/Vấn đề:** Ghi lại những vấn đề gặp phải
```

## Nhật ký

### 12/06/2026 - Bắt đầu project

**Tập trung vào:** Setup dự án Java Spring Boot + VueJS

**Những gì đã học:**

- Cấu trúc Maven project cơ bản
- Tổ chức folder backend/frontend

**Tiến độ:**

- [x] Khởi tạo backend folder
- [x] Khởi tạo frontend folder

**Ghi chú:** Đây là phần mở đầu của dự án Japanese Learning Web

---

### 12/06/2026 - Setup Backend Foundation

**Tập trung vào:** Xây dựng nền tảng backend cho Japanese Learning Platform

**Kết quả đạt được:** ✅

- Backend chạy thành công trên `localhost:8080`
- Swagger UI mở được tại `http://localhost:8080/swagger-ui/index.html`
- Endpoint `/api/health` hoạt động (GET method)

**Thành phần & File đã setup:**

- `pom.xml` - Maven configuration với dependencies Spring Boot, Swagger
- `src/main/java/com/example/` - Java source folder
  - `config/` - Spring configuration, Swagger config
  - `controller/` - REST controllers (HealthController)
  - `service/` - Business logic
  - `repository/` - Database layer
  - `entity/` - JPA entities
  - `dto/` - Data Transfer Objects
- `src/main/resources/`
  - `application.yml` - Application configuration (server port, database, logging)
- `target/` - Compiled classes (Maven build output)

**Kiến thức cần nhớ:**

1. **Spring Boot Starter Dependencies**
   - `spring-boot-starter-web` - REST API support
   - `spring-boot-starter-data-jpa` - Database ORM
   - `springdoc-openapi-starter-webmvc-ui` - Swagger/OpenAPI documentation

2. **Project Structure Pattern**
   - Tách riêng `controller` (API endpoints), `service` (business logic), `repository` (database access)
   - Giúp code dễ maintain, test, và scale

3. **Swagger/OpenAPI Setup**
   - Tự động generate API documentation từ code
   - Endpoint được định nghĩa bằng `@RestController` và `@GetMapping`, `@PostMapping`, etc.
   - Swagger UI cho phép test API trực tiếp từ browser

4. **Maven Build System**
   - `mvn clean install` - compile, test, package
   - `mvn spring-boot:run` - chạy Spring Boot application
   - Dependencies được quản lý tập trung trong `pom.xml`

5. **HTTP REST Principles**
   - GET - lấy dữ liệu (idempotent)
   - POST - tạo dữ liệu mới
   - PUT - cập nhật dữ liệu
   - DELETE - xóa dữ liệu
   - Status code: 200 OK, 201 Created, 400 Bad Request, 404 Not Found, 500 Server Error

**Code Pattern hay gặp:**

```java
// Health Check Endpoint
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
```

**Phần cần ôn lại:**

- 🟡 Cấu hình Spring Security (authentication, authorization)
- 🟡 Cách set up database connection (MySQL, PostgreSQL)
- 🟡 Exception handling và custom error response
- 🟡 Validation (Bean Validation, @Valid)
- 🟡 Dependency Injection chi tiết (@Autowired, constructor injection)
- 🟡 AOP (Aspect-Oriented Programming) cho logging, error handling

**Checklist tự kiểm tra:**

- [x] Backend application chạy không có lỗi
- [x] Swagger UI mở được
- [x] Health endpoint trả về 200 OK
- [x] Maven build thành công (target/classes)
- [x] application.yml cấu hình đúng
- [ ] Thêm multiple endpoints cho các features (user, course, quiz, etc.)
- [ ] Setup database connection
- [ ] Thêm error handling global
- [ ] Thêm request validation
- [ ] Viết unit tests cho controllers
- [ ] Setup authentication/authorization

**Ghi chú:**

- Backend setup xong, sẵn sàng để phát triển các API endpoints
- Cần theo dõi Swagger documentation để đảm bảo API đúng spec
- Tiếp theo: Setup database entities, repository, services
- Tài liệu reference: [Spring Boot Docs](https://spring.io/projects/spring-boot), [Swagger/OpenAPI](https://swagger.io/)

---

### 12/06/2026 - Setup Auth/User Database Foundation

**Tập trung vào:** Setup Entities và Repositories cho Auth/User module

**Kết quả đạt được:** ✅

- Đã tạo thành công các bảng `users`, `roles`, `user_roles`, `refresh_tokens` trong MariaDB thông qua Hibernate.

**Thành phần & File đã setup:**

- **Enum:** `UserStatus.java`, `RoleName.java`
- **Entity:** `User.java`, `Role.java`, `RefreshToken.java`
- **Repository:** `UserRepository.java`, `RoleRepository.java`, `RefreshTokenRepository.java`

**Kiến thức cần nhớ:**

1. **Quan hệ N-N (ManyToMany)**
   - `User` và `Role` là ManyToMany.
   - Không dùng `CascadeType.ALL` hay `CascadeType.REMOVE` cho quan hệ này để tránh xóa nhầm Role khi xóa User. Phải dùng `CascadeType.PERSIST, CascadeType.MERGE`.
2. **Infinite Recursion (Vòng lặp vô hạn)**
   - Tuyệt đối tránh dùng `@Data` hoặc `@ToString`, `@EqualsAndHashCode` của Lombok trên các Entity có quan hệ vì rất dễ gây vòng lặp vô hạn. Thay vào đó nên dùng thủ công `@Getter`, `@Setter`.
3. **N+1 Query & Lazy Loading**
   - Luôn dùng `fetch = FetchType.LAZY` cho các quan hệ `@ManyToOne`, `@ManyToMany` để tránh việc Hibernate tự động thực thi quá nhiều truy vấn phụ.

**Code Pattern hay gặp:**

```java
// Khai báo ManyToMany an toàn giữa User và Role
@Builder.Default
@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();
```

**Phần cần ôn lại:**

- 🟡 Lifecycle của JPA (Persist, Merge, Remove, Detach, Refresh)
- 🟡 Sự khác biệt giữa các chiến lược tạo khóa chính (GenerationType: IDENTITY, SEQUENCE, AUTO)
- 🟡 Viết custom query bằng `@Query` hoặc phương thức quy ước trong Spring Data JPA

**Checklist tự kiểm tra:**

- [x] Không dùng `@Data` trên Entity
- [x] Đã dùng `FetchType.LAZY`
- [x] Ứng dụng chạy không lỗi
- [x] Đã kiểm tra schema trực tiếp trên DB (MariaDB)

---

### 13/06/2026 - Seed Roles + Admin User

**Tập trung vào:** Khởi tạo dữ liệu gốc cho Auth/User module thông qua DatabaseSeeder

**Kết quả đạt được:** ✅

- Đã tạo thành công `DatabaseSeeder` implement `CommandLineRunner` để tự động chạy khi Spring Boot khởi động.
- Đã seed đủ 6 roles (`SUPER_ADMIN`, `ADMIN`, `TEACHER`, `CONTENT_EDITOR`, `STUDENT`, `GUEST`).
- Đã tạo admin mặc định (hash password bằng BCrypt).
- Ứng dụng kiểm tra trùng lặp thông minh, không insert lại dữ liệu khi restart.

**Thành phần & File đã setup:**

- **Thư viện:** Thêm `spring-boot-starter-security` vào `pom.xml`.
- **Cấu hình:** `SecurityConfig` cung cấp bean `PasswordEncoder` và tắt xác thực mặc định (permitAll).
- **Properties:** Đưa email/password admin vào `application.yml` tránh hard-code.
- **Seeder:** `DatabaseSeeder.java`.

**Kiến thức cần nhớ:**

1. **CommandLineRunner**
   - Rất hữu ích để chạy script khởi tạo dữ liệu một lần khi ứng dụng Spring Boot vừa khởi động xong.
2. **Spring Security cơ bản**
   - Luôn dùng `BCryptPasswordEncoder` để mã hóa mật khẩu trước khi lưu database. Tuyệt đối không lưu plain-text.
   - Khi đưa Security vào project, tất cả endpoint sẽ bị khóa mặc định. Cần định nghĩa `SecurityFilterChain` với `permitAll()` nếu chưa làm module Auth để không block các API khác.
3. **Tránh trùng lặp dữ liệu**
   - Khi seed data, luôn phải query kiểm tra sự tồn tại (ví dụ: `roleRepository.findByName(...)` hoặc `userRepository.existsByEmail(...)`) trước khi gọi `.save()`.

**Code Pattern hay gặp:**

```java
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // ... logic seed data ...
    }
}
```

**Checklist tự kiểm tra:**

- [x] DatabaseSeeder chạy thành công
- [x] Không duplicate data khi chạy lại app
- [x] Password của admin đã được mã hóa BCrypt ($2a$...)
- [x] Tạo được enum UserStatus, RoleName
- [x] Tạo được entity User, Role, RefreshToken
- [x] Tạo được repository UserRepository, RoleRepository, RefreshTokenRepository
- [x] ManyToMany relationship hoạt động đúng
- [x] Database tables được tạo tự động bởi Hibernate
- [ ] Thêm custom query methods vào Repository
- [ ] Setup UserService để handle user operations
- [ ] Thêm validation cho User entity
- [ ] Viết tests cho User entity và repository

---

### 14/06/2026 - Register API (Module Auth)

**Tập trung vào:** Xây dựng luồng API hoàn chỉnh (Controller → Service → Repository) cho chức năng Đăng ký tài khoản học viên.

**Kết quả đạt được:** ✅

- Đã tạo `RegisterRequest` DTO với Bean Validation (chống rác dữ liệu từ đầu vào).
- Đã tạo `RegisterResponse` DTO (không bao giờ lộ Entity hay passwordHash ra ngoài).
- Đã implement `AuthServiceImpl` xử lý logic: validate password khớp, kiểm tra email trùng, lấy role STUDENT mặc định, băm mật khẩu bằng BCrypt, và lưu user.
- Đã tạo `AuthController` với endpoint `POST /api/auth/register`.
- Đã bắt lỗi chuẩn bằng các ErrorCode mới: `EMAIL_ALREADY_EXISTS`, `PASSWORD_CONFIRM_NOT_MATCH`, `ROLE_NOT_FOUND`.

**Kiến thức cần nhớ:**

1. **Bean Validation (@Valid)**
   - Đặt `@Valid` trước `@RequestBody` trong Controller.
   - Thêm annotation `@NotBlank`, `@Size`, `@Email` trong DTO.
   - Spring sẽ tự chặn request lỗi mà không cần code `if-else` trong Controller. Lỗi sẽ được bắt bởi `GlobalExceptionHandler` (bắt `MethodArgumentNotValidException`).
2. **Tách biệt DTO và Entity**
   - Rất quan trọng! Không bao giờ dùng Entity (`User`) làm kiểu trả về của API, vì Entity chứa các thông tin nhạy cảm (như `passwordHash`) hoặc các mapping phức tạp dễ gây lỗi đệ quy (JSON Infinite Recursion).
3. **Mã hóa mật khẩu (Password Hashing)**
   - Luôn gọi `passwordEncoder.encode(rawPassword)` trước khi set vào Entity. Cấm lưu plain-text.
4. **Idempotent / Uniqueness Check**
   - Gọi `userRepository.existsByEmail(...)` trước khi xử lý, ném `AppException` với HTTP 409 (Conflict) nếu trùng, để UX frontend hiển thị báo lỗi đỏ ở ô Email.

**Code Pattern hay gặp:**

```java
// Controller
@PostMapping("/register")
public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ApiResponse.success("Đăng ký thành công", authService.register(request));
}

// Service (Validation + Business Logic)
if (userRepository.existsByEmail(request.getEmail())) {
    throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
}

// Hash password
String hashedPassword = passwordEncoder.encode(request.getPassword());
```

---

### 15/06/2026 - Login API & JWT Token Generation (Module Auth)

**Tập trung vào:** API Đăng nhập và sinh JWT (Access Token & Refresh Token)

**Kết quả đạt được:** ✅

- Tích hợp thành công thư viện `jjwt` (version 0.12.5).
- Định cấu hình JWT secret và thời gian hết hạn thông qua `application.yml`.
- Tạo `JwtUtil` class để chuyên tạo và parse 2 loại token: Access Token và Refresh Token.
- Tạo API `POST /api/auth/login`. Kiểm tra mật khẩu (BCrypt), check trạng thái tài khoản.
- Lưu Refresh Token vào CSDL (bảng `refresh_tokens`) nhằm mục đích dễ dàng thu hồi sau này (Logout/Đổi mật khẩu).
- Phòng thủ Enumerate Attack bằng cách trả chung 1 mã lỗi `AUTH_002` (Code 2002: Email hoặc mật khẩu không đúng) thay vì bóc tách chi tiết lỗi email hay lỗi password.

**Kiến thức cần nhớ:**

1. **Chiến lược 2 Tokens (Access & Refresh):**
   - Access Token: Sinh mệnh ngắn (15-30p), giúp việc cấp quyền cho các request tới các endpoints diễn ra nhanh chóng (stateless, decode nhanh). Không nên lưu vào database.
   - Refresh Token: Sinh mệnh dài (ví dụ 7 ngày), dùng để trao đổi lấy Access Token mới. Bắt buộc nên lưu vào database để hệ thống có quyền thu hồi (revoke) khi cần thiết.
2. **Ngăn chặn Enumerate Attack:**
   - Trong quá trình Login, dù là user không tồn tại, hay sai mật khẩu thì ta luôn báo lỗi chung một thông điệp như "Email hoặc mật khẩu không đúng" với mã HTTP 401 Unauthorized. Điều này ngăn chặn hacker thu thập danh sách email người dùng.
3. **Cập nhật Last Login:**
   - Khi user login thành công, cập nhật cột `lastLoginAt` trong Entity `User` để phục vụ thống kê.
   - Lỗi bảo mật do sinh ra Enumerate Attack nếu báo quá chi tiết sai email hay sai pass.
4. **JJWT 0.12.5 changes:**
   - API của jjwt bản mới đã thay đổi, bắt buộc phải dùng `Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token)` thay vì các hàm cũ đã bị deprecated. Khóa secretKey dùng thuật toán HMAC-SHA cần phải có độ dài ít nhất 256 bits (32 bytes).

---

### 16/06/2026 - Refresh Token & Logout API (Module Auth)

**Tập trung vào:** Quản lý vòng đời Token, xây dựng API gia hạn Access Token và API đăng xuất.

**Kết quả đạt được:** ✅

- Tạo mới các DTO `RefreshTokenRequest`, `RefreshTokenResponse`, `LogoutRequest`.
- Bổ sung các endpoint `POST /api/auth/refresh-token` và `POST /api/auth/logout`.
- Tận dụng lại class `JwtUtil` để giải mã email từ token cũ.
- Áp dụng các Rule quản lý trạng thái token:
  - Check xem token có trong CSDL không (`2006` - `INVALID_REFRESH_TOKEN`).
  - Check xem token đã bị revoke chưa (`2008` - `REFRESH_TOKEN_REVOKED`).
  - Check xem token hết hạn chưa (`2007` - `REFRESH_TOKEN_EXPIRED`).
  - Trạng thái user cũng được check lại (Nếu tài khoản bị khóa thì không cấp token mới).
- Ở endpoint Logout, thay vì xóa dòng trong Database, ta chỉ set field `revoked = true` (Soft Delete / Update State) để dễ dàng tracking thiết bị, log kiểm toán (audit).

**Kiến thức cần nhớ:**

1. **Idempotency trong Logout:**
   - Khi user bấm Logout nhiều lần bằng 1 Refresh Token, hoặc truyền Refresh Token tào lao, API Logout vẫn trả về HTTP 200 Success mà không bắn lỗi. Thiết kế kiểu Idempotent này giúp frontend nhàn hơn trong xử lý rác cookie/localstorage.
2. **Refresh Token Rotation:**
   - Tạm thời chưa áp dụng Rotation (cấp lại toàn bộ cặp Access + Refresh mới) để hệ thống đơn giản hơn. Refresh token cũ (nếu chưa revoke/expire) vẫn dùng được tới 7 ngày.
3. **Mã lỗi 401 Unauthorized:**
   - Cực kỳ hữu dụng khi phân biệt rạch ròi bằng Error Code (2006, 2007, 2008). Front-end bắt được mã 2007 là có thể nhắc user đăng nhập lại thay vì báo "Lỗi hệ thống".

---

### 17/06/2026 - Access Token Authentication & User ME API (Module User)

**Tập trung vào:** Xây dựng Filter bảo vệ API và Endpoint lấy thông tin User đang đăng nhập.

**Kết quả đạt được:** ✅

- Tạo `CustomUserDetails` và `CustomUserDetailsService` tích hợp với Spring Security.
- Tạo `JwtAuthenticationFilter` để chặn các request, lấy và giải mã token, gán Authentication vào `SecurityContextHolder`.
- Cấu hình `SecurityConfig`: thêm Filter, chỉnh Session thành `STATELESS`, phân loại Public (`/api/auth/**`) và Protected endpoints.
- Triển khai `JwtAuthenticationEntryPoint` để đồng nhất JSON lỗi 401 trả về theo cấu trúc `ApiResponse`.
- Xây dựng API `GET /api/users/me` đọc email từ SecurityContext, query CSDL và trả về `CurrentUserResponse`.

**Kiến thức cần nhớ:**

1. **Lỗi `LazyInitializationException` trong Filter:**
   - Trong `CustomUserDetailsService.loadUserByUsername()`, vì `user.getRoles()` sử dụng LAZY fetch, nếu không khởi tạo (gọi `user.getRoles().size()`) bên trong block `@Transactional` thì khi Filter (bên ngoài transaction) lấy Authorities sẽ bị crash ứng dụng, dẫn đến Authentication thất bại ngầm.
2. **Xử lý Exception trong Filter:**
   - `OncePerRequestFilter` nằm ngoài chu trình của `DispatcherServlet` nên `@ControllerAdvice` sẽ không bắt được.
   - Giải pháp: Inject `HandlerExceptionResolver` vào Filter và gọi `handlerExceptionResolver.resolveException(...)` để đẩy luồng lỗi về cho GlobalExceptionHandler.

---

### 19/06/2026 - Basic Role-Based Authorization & Security Rules (Module Auth/User)

**Tập trung vào:** Phân quyền API dựa trên Role, thiết lập AccessDeniedHandler và Security Filter Chain.

**Kết quả đạt được:** ✅

- Định nghĩa phân vùng URL: Public (`/api/auth/**`, `/api-docs/**`), Admin (`/api/admin/**`), Học viên (`/api/student/**`) và Authenticated (`/api/users/me`).
- Xây dựng `CustomAccessDeniedHandler` để bắt ngoại lệ 403 Forbidden do Spring ném ra khi có Token hợp lệ nhưng thiếu Role truy cập. Việc này giúp response luôn là JSON chuẩn `ApiResponse`.
- Bật annotation `@EnableMethodSecurity` trong `SecurityConfig` để chuẩn bị cho việc chặn quyền mức độ Controller (`@PreAuthorize`) trong tương lai.
- Tạo một endpoint nháp (`/api/admin/test`) để kiểm chứng độc lập quyền Admin.

**Kiến thức cần nhớ:**

1. **Phân biệt 401 và 403:**
   - **401 Unauthorized:** Chặn ở Filter khi Token thiếu/sai. Hệ thống "không biết bạn là ai".
   - **403 Forbidden:** Chặn sau Filter khi Token đúng. Hệ thống "biết bạn là ai nhưng bạn không đủ quyền (Role)".
2. **Authority Format:**
   - Spring Security quy ước Role Authority phải bắt đầu bằng `ROLE_`. Nếu CustomUserDetails chỉ trả về `ADMIN` thì `hasRole("ADMIN")` sẽ không bao giờ khớp. (Hàm `hasRole` ngầm định cộng thêm `ROLE_` khi so sánh).
3. **Thứ tự khai báo Filter Chain:**
   - Luôn đặt `.requestMatchers` cụ thể lên trước (vd: `/api/admin/**`), rồi mới đến những request bao quát (`anyRequest().authenticated()`). Nếu đảo lộn thứ tự, hệ thống sẽ bỏ sót rule phân quyền.

---

### 20/06/2026 - Course & Lesson Database Foundation (Module Course)

**Tập trung vào:** Xây dựng Domain Model (Entities, Enums, Repositories) cho Bounded Context Course/Lesson. Chuẩn bị nền tảng dữ liệu cho các API CRUD.

**Kết quả đạt được:** ✅

- Gom 4 Entity (`Course`, `CourseSection`, `Lesson`, `LessonResource`) vào chung package `module_course` để đảm bảo tính toàn vẹn của Bounded Context, thuận lợi cho việc tách Microservice sau này nếu cần.
- Setup thành công các Enum hệ thống (`CourseLevel`, `CourseType`, `CourseStatus`, `ResourceType`).
- Cấu hình JPA Relationships: sử dụng `fetch = FetchType.LAZY` cho tất cả `@ManyToOne` để chặn N+1 queries.
- Sử dụng `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` một cách an toàn.
- Ngăn chặn lỗi StackOverflow do `toString()` đệ quy của Lombok bằng cách gắn `@ToString.Exclude` và `@EqualsAndHashCode.Exclude` trên các collection List.
- Cấu hình unique constraint: `slug` của Course là unique toàn cầu, trong khi `slug` của Lesson là unique trong phạm vi một khóa học (`course_id`, `slug`).

**Kiến thức cần nhớ:**

1. **Lombok `@Data` và JPA:** Cực kỳ cẩn thận với `@Data` hoặc `@ToString` khi có `@OneToMany` và `@ManyToOne` vòng tròn nhau. Khi in ra log, nó sẽ lặp vô tận gây sập hệ thống (StackOverflowError). Việc Explicitly Exclude là bắt buộc.
2. **Package Cohesion (Sự gắn kết gói):** Không nên xé nhỏ một Aggregate Root (Course) ra làm nhiều module rời rạc (Course Module, Lesson Module) chỉ vì thấy chúng dài. Điều đó phá vỡ nguyên lý thiết kế Domain-Driven Design (DDD) và làm việc cascade, truy vấn trở nên ác mộng.

---

### 21/06/2026 - Admin Course CRUD API

**Tập trung vào:** Xây dựng API Quản trị Khóa học, giải quyết bài toán Data Isolation (Dữ liệu biệt lập theo Teacher) và hiệu năng (N+1 Query).

**Kết quả đạt được:** ✅

- Tạo bộ API hoàn chỉnh `/api/v1/admin/courses` hỗ trợ POST, PUT, DELETE, GET.
- Bọc toàn bộ Response trả về bằng `ApiResponse` chuẩn.
- Map DTO thành công mà không để lọt Entity nhạy cảm của Teacher ra ngoài.
- Giải quyết bài toán **N+1 Query** cực kỳ triệt để bằng `@EntityGraph(attributePaths = {"teacher"})` trong Spring Data JPA.
- Tích hợp thành công `SlugUtils` tự generate đường dẫn SEO-friendly.
- Update file `SecurityConfig` để mở khóa prefix `/api/v1/admin/**` cho role `TEACHER` cùng với `ADMIN`.

**Kiến thức cần nhớ:**

1. **Data Isolation (Phân quyền theo Record):** Role `TEACHER` và `ADMIN` đều có thể vào endpoint `/api/v1/admin/courses`. Nhưng ở Service, chúng ta đã chèn logic: Nếu là `TEACHER`, chỉ được thao tác trên Record có `teacher_id` khớp với `user_id` hiện tại. Đây là cách làm bảo mật cực kỳ an toàn.
2. **`@EntityGraph` so với `JOIN FETCH`:** Thay vì viết Custom Query `@Query("SELECT c FROM Course c JOIN FETCH c.teacher")` thủ công, Spring cung cấp `@EntityGraph` giúp mã nguồn gọn gàng hơn mà vẫn giải quyết được N+1 Query. Chú ý: Override hàm `findById` mặc định của JpaRepository để thêm `@EntityGraph` là một mẹo rất hay.

---

### 23/06/2026 - Admin Section CRUD API

**Tập trung vào:** Quản lý chương học của khóa học, logic Auto SortOrder, và các quy tắc xóa dữ liệu an toàn.

**Kết quả đạt được:** ✅

- Triển khai toàn bộ API tạo, lấy danh sách, cập nhật, và xóa Section.
- Tính năng Auto SortOrder: Khi tạo Section không có `sortOrder`, JPA custom query `MAX(sortOrder)` hoạt động hoàn hảo để đếm số lượng hiện tại và tự cộng thêm 1.
- Bảo vệ dữ liệu bằng Rule "Không cho phép xóa Section nếu có Lesson bên trong".
- Logic Data Isolation đã được tái sử dụng thành công: So sánh trực tiếp `Course.getTeacher().getEmail()` với `Authentication` context email. Người dùng không phải là sở hữu của khóa học sẽ bị văng mã `AUTH_003` (403 Forbidden).

---

### 24/06/2026 - Admin Lesson CRUD API

**Tập trung vào:** Quản lý bài học, kỹ thuật Data Isolation lội ngược dòng Entity (Lesson → Section → Course → Teacher), Auto Slug và Auto SortOrder.

**Kết quả đạt được:** ✅

- Triển khai 5 endpoint hoàn chỉnh: POST, GET danh sách, GET chi tiết, PUT, DELETE cho Lesson.
- Tự động sinh **slug** từ tiêu đề bài học bằng `SlugUtils`. Check trùng slug trong **phạm vi khóa học** (2 khóa khác nhau có thể trùng slug).
- Tự động tính **sortOrder** bằng `@Query("SELECT MAX(l.sortOrder)...")` rồi cộng 1.
- Data Isolation cực nặng: Dùng `@EntityGraph(attributePaths = {"section.course.teacher"})` để móc chuỗi 4 tầng Entity chỉ trong **1 câu SQL duy nhất**. Không có N+1 Query.

**Kiến thức cần nhớ:**

1. **Data Isolation lội ngược dòng (Deep Chain):** Khi cần kiểm tra quyền sở hữu của Teacher trên một Lesson, ta phải lội ngược `Lesson → Section → Course → Teacher`. Thay vì gọi 3 câu `SELECT` riêng lẻ (gây N+1), dùng `@EntityGraph(attributePaths = {"section.course.teacher"})` để JPA tạo 1 câu `LEFT OUTER JOIN` gộp tất cả lại. Đây là kỹ thuật tối ưu hiệu năng rất quan trọng.
2. **Slug unique trong phạm vi Course:** Unique constraint `@UniqueConstraint(columnNames = {"course_id", "slug"})` ở Entity cho phép 2 khóa học khác nhau có lesson cùng slug, nhưng trong cùng 1 khóa thì không. Repository check bằng `existsByCourseIdAndSlug()`.
3. **`@Builder.Default`:** Khi dùng Lombok `@Builder` với DTO, nếu muốn trường `isPreview` mặc định là `false` khi client không gửi, phải đánh dấu `@Builder.Default` chứ không chỉ gán `= false` đơn thuần.

---

### 25/06/2026 - Student Course Public API (Guest Access & N+1 Anti-Pattern)

**Tập trung vào:** Mở khóa các endpoint cho Guest/Student xem khóa học, bảo mật dữ liệu trả phí (Data Protection), và tối ưu hóa truy vấn đa tầng chống N+1.

**Kết quả đạt được:** ✅

- Cấu hình thành công `SecurityConfig` cho phép luồng `/api/v1/courses/**` truy cập tự do mà không cần Token.
- Tạo bộ 4 DTOs hoàn toàn độc lập với Admin (`CoursePublicRes`, `CourseDetailPublicRes`, `SectionPublicRes`, `LessonPublicRes`) để lọc sạch dữ liệu.
- Xử lý Data Protection: Duyệt danh sách bài học, nếu `isPreview = false`, xóa toàn bộ `videoUrl` và `content` về `null` trước khi trả về. Học thử (`isPreview = true`) được hiển thị bình thường.
- Tối ưu hóa truy vấn tuyệt đối: Sử dụng `@EntityGraph(attributePaths = {"teacher", "sections", "sections.lessons"})` để lấy Course -> Section -> Lesson trong **1 câu SQL duy nhất**.

**Kiến thức cần nhớ:**

1. **MultipleBagFetchException trong Hibernate:** Khi dùng `@EntityGraph` để kéo 2 tập hợp dạng `List` (Ví dụ: `List<CourseSection>` và `List<Lesson>`), Hibernate sẽ văng lỗi `MultipleBagFetchException` do lo ngại Cartesian Product sinh ra sai lệch dữ liệu.
2. **Cách Fix MultipleBagFetchException tối ưu nhất:** Chuyển kiểu dữ liệu của Collection trong Entity từ `java.util.List` sang `java.util.Set` (cụ thể là `LinkedHashSet` để giữ nguyên thứ tự thêm vào).
3. **Data Protection tại Tầng Service:** Không bao giờ phụ thuộc vào Frontend để ẩn link video. Phải set `videoUrl = null` ở Backend DTO nếu học viên không có quyền truy cập (hoặc bài học không cho học thử).

---

### 27/06/2026 - Free Course Enrollment API (Data Integrity & Fail-Fast Logic)

**Tập trung vào:** Chống lặp dữ liệu (Race Condition) và triển khai luồng nghiệp vụ API Fail-Fast.

**Kết quả đạt được:** ✅

- Xây dựng thành công hệ thống DTO và luồng cho bảng trung gian `CourseEnrollment`.
- Đảm bảo tính nhất quán (Consistency) của dữ liệu: Áp dụng `@UniqueConstraint(columnNames = {"user_id", "course_id"})` cấp độ Database để vĩnh viễn không có chuyện 1 học viên bị nhân đôi bản ghi học tập do lỗi mạng / spam API.
- Tối ưu hóa chu trình kiểm tra nghiệp vụ: Load thông tin Course lên RAM -> Kiểm tra trạng thái (Published) -> Kiểm tra loại (Free) -> Truy vấn DB check trùng (Enrollment exists). Nếu fail ở bất kỳ bước nào thì ném Exception ngay lập tức.
- Xây dựng script shell automation gọi API tạo người dùng, phân quyền tự động và kiểm tra logic (Mocking kịch bản Ghi danh khóa có phí, khóa miễn phí, ghi danh đúp).

**Kiến thức cần nhớ:**

1. **Bảo mật Endpoint Ghi danh:** API Enrollment không bao giờ tin tưởng ID người dùng từ Request Body (Dễ bị Postman chọc ngoáy). Bắt buộc phải lấy từ `SecurityContextHolder.getContext().getAuthentication().getName()`.
2. **Composite Unique Key Database:** Hibernate tự động Generate Unique Constraint qua Annotation `@Table(uniqueConstraints = ...)`. Giúp bảo vệ hệ thống khỏi những trường hợp Request gửi đồng thời trong cùng 1 mili-giây (Race Conditions).

---

### 29/06/2026 - Student Lesson Learning & Progress API (Single Responsibility Principle)

**Tập trung vào:** Luồng nhả nội dung (Video/Tài liệu) dựa trên quyền Ghi danh và ghi nhận phần trăm học tập Upsert.

**Kết quả đạt được:** ✅

- Tạo package `module_learning` độc lập hoàn toàn với `module_course` để quản lý logic của người học. Việc phân tách logic Admin CRUD (`LessonAdminService`) và logic Student View (`LearningService`) giúp codebase tuân thủ chuẩn xác nguyên lý SRP (Single Responsibility Principle).
- Xây dựng thuật toán Anti-Downgrade Progress: Khi Client bắn request POST liên tục chứa `% video đã xem`, server chỉ cho phép cập nhật nếu giá trị mới LỚN HƠN giá trị cũ. Ngăn ngừa tình trạng học viên xem lại đoạn đầu video và bị mất toàn bộ % trước đó.
- Xây dựng Content Barrier: Kiểm tra 2 lớp. Lớp 1: Khóa học có `PUBLISHED` không. Lớp 2: Bài học có `isPreview = false` không, nếu có thì lội sang bảng `CourseEnrollment` để chặn ngay bằng HTTP 403 Forbidden.

**Kiến thức cần nhớ:**

1. **Upsert Logic cơ bản trong JPA:** Tìm kiếm bản ghi bằng `Repository.findBy...()`. Nếu trả về `Optional.empty()`, dùng `Builder` tạo mới. Sau khi thay đổi giá trị, chạy `Repository.save(entity)`. Hibernate sẽ tự động chọn INSERT hoặc UPDATE tùy vào việc Entity đó có `ID` hay chưa.
2. **Phân cực dữ liệu Output:** API cho Admin trả về `LessonRes` chứa trạng thái/ngày tạo để quản trị. API cho Student trả về `LessonLearningRes` ẩn ngày tạo/cập nhật, nhưng gắn thêm các trường progress như `watchedPercent` và `isCompleted`. DTO phải phục vụ UI.

---

### 01/07/2026 - Student Dashboard & My Learning APIs (Data Aggregation & Anti-IDOR)

**Tập trung vào:** Tổng hợp dữ liệu (Aggregation) từ nhiều bảng để tính toán tiến độ học tập và phòng chống lỗ hổng IDOR.

**Kết quả đạt được:** ✅

- Xây dựng thành công `StudentDashboardService` và tích hợp vào `UserController`.
- Cung cấp API `GET /api/users/me/courses` trả về danh sách khóa học kèm theo: Số bài đã hoàn thành, tổng số bài, % tiến độ, và bài học truy cập gần nhất.
- Cung cấp API `GET /api/users/me/progress` trả về bảng tổng quan: Tổng số khóa học, tổng số bài đã học, và % tiến độ toàn khóa.
- Áp dụng triệt để nguyên tắc **Anti-IDOR**: Không nhận bất kỳ ID người dùng nào từ URL hay Body. Mọi truy vấn đều sử dụng `userId` bóc tách từ JWT Security Context.

**Kiến thức cần nhớ:**

1. **Data Aggregation:** Thay vì bắt Frontend phải gọi 10 API để tự cộng trừ nhân chia, Backend sẽ gom nhóm (Join) dữ liệu từ `CourseEnrollment` và `LessonProgress` lại, tính toán sẵn % và trả về một DTO duy nhất. Điều này giúp giảm thiểu độ trễ mạng và logic phía Client.
2. **Xử lý chia cho 0 (ZeroDivisionError):** Luôn phải có block `if (totalLessons > 0)` trước khi tính `(completed / total) * 100` để tránh bug sập luồng khi khóa học chưa có bài học nào.

**Phần cần ôn lại:**

- 🟡 ValidationException làm sao mapping sang HTTP response tuỳ custom?
- 🟡 Exception precedence khi có nhiều exception handler (global vs local)?
- 🟡 Sự khác biệt giữa `@Validated` (class-level) và `@Valid` (method parameter)?
- 🟡 Custom validator annotation (nếu cần validation phức tạp)?

**Checklist tự kiểm tra:**

- [x] Endpoint POST /api/auth/register tạo thành công
- [x] DTO RegisterRequest có validation đầy đủ
- [x] DTO RegisterResponse không expose password hay entity khác
- [x] AuthService.register() validate đủ các case: password not match, email exists, role not found
- [x] Password được hash BCrypt trước khi lưu
- [x] Error code chuẩn hóa (2001, 2010, 3002)
- [x] Test thành công: POST /api/auth/register với dữ liệu hợp lệ
- [x] Test exception: gọi lại register với email trùng → HTTP 409
- [x] Test exception: password không khớp confirmPassword → HTTP 400
- [x] Swagger UI hiển thị endpoint đầy đủ

**Ghi chú:**

- Task này là bước tiếp theo sau Seed Roles + Admin User. Bây giờ user có thể tự đăng ký thay vì chỉ có admin seeded mặc định.
- Ưu điểm của task: Tách Controller/Service/Repository rõ ràng, không logic trong Controller, DTO tách khỏi Entity, error chuẩn hóa.
- Tiếp theo: Làm Login API để authenticate user, sau đó JWT token.

---

### 02/07/2026 - Frontend Foundation (Vue 3 + Vite + Axios + Pinia)

**Tập trung vào:** Xây dựng nền tảng frontend để kết nối với backend và chuẩn bị cho các màn hình auth/course/student.

**Kết quả đạt được:** ✅

- Chuẩn bị task mới cho frontend foundation sau khi backend learning flow đã ổn.
- Xác định rõ cấu trúc thư mục theo kế hoạch: `src/pages`, `src/router`, `src/stores`, `src/services`, `src/layouts`.
- Xác định rõ các công việc cốt lõi: setup router, store auth, Axios interceptor, environment config.

**Kiến thức cần nhớ:**

1. **Vue 3 + Vite:** Vite cho tốc độ dev nhanh, phù hợp cho frontend nhỏ-medium.
2. **Pinia:** Quản lý state hiện đại thay cho Vuex, thân thiện với Composition API.
3. **Axios interceptor:** Nơi tập trung gắn token, xử lý 401/refresh, và chuẩn hóa lỗi API.
4. **Environment variables:** Dùng `.env.development` / `.env.production` để tránh hard-code URL backend.

**Phần cần ôn lại:**

- 🟡 Cách thiết lập router guard cho auth-required routes.
- 🟡 Cách dùng Pinia store với async actions.
- 🟡 Cách parse response từ backend `ApiResponse` chuẩn.

**Checklist tự kiểm tra:**

- [ ] Khởi tạo project frontend thành công
- [ ] Router và layout cơ bản chạy được
- [ ] Store auth hoạt động
- [ ] Axios client kết nối được backend
- [ ] Môi trường dev chạy trên localhost

**Ghi chú:**

- Task này là bước nối tiếp sau backend learning flow.
- Sau khi foundation ổn, các task tiếp theo sẽ là auth pages, course pages, và student dashboard UI.

---

### 03/07/2026 - Frontend Design & Demo Preparation

**Tập trung vào:** Thiết kế giao diện Frontend (UI/UX) cho hệ thống BrianJP để chuẩn bị bản demo gửi cho khách hàng.

**Kết quả đạt được:** ✅
- Tiếp nối thành công của việc xây dựng Frontend Foundation ngày hôm qua, hôm nay đã lên ý tưởng và chuẩn bị các component giao diện.
- Mục tiêu chính là làm cho các trang Placeholder (HomePage, LoginPage, Dashboard) trở nên bắt mắt và chuyên nghiệp hơn để gây ấn tượng mạnh với khách hàng trong buổi demo sắp tới.

**Kiến thức cần nhớ:**
1. **First Impression:** Một bản demo tốt không chỉ cần tính năng chạy mượt mà (đã làm rất tốt ở Backend) mà giao diện cũng phải tạo được cảm giác "Premium".
2. **Demo-Driven Development:** Xây dựng phần nổi của tảng băng trước để khách hàng có thể hình dung và feedback sớm, tránh đi chệch hướng yêu cầu nghiệp vụ thực tế.

**Tiến độ:**
- [x] Lên ý tưởng giao diện Demo
- [ ] Chỉnh sửa CSS/UI cho HomePage
- [ ] Hoàn thiện luồng UX Login -> Dashboard

**Ghi chú:**
- Khách hàng rất quan trọng trải nghiệm người dùng (UX), vì vậy Frontend cần được trau chuốt kỹ lưỡng. Hẹn gặp lại vào phiên làm việc tiếp theo để biến các ý tưởng này thành code thực tế!

---

### 05/07/2026 - Refactor Cấu Hình Spring Boot (Dev/Prod Profiles)

**Tập trung vào:** Tách biệt cấu hình môi trường development và production cho Spring Boot backend để sẵn sàng deploy an toàn.

**Kết quả đạt được:** ✅

- Phát hiện file `application.yml` đang **hardcode** toàn bộ thông tin nhạy cảm: database credentials (`root/0209`), JWT secrets, admin password → Nếu lộ source code, hệ thống bị compromise hoàn toàn.
- Chia cấu hình thành **3 file YAML** theo chuẩn Spring Profiles:
  - `application.yml` → Cấu hình chung (server port, multipart, JWT expiration, `spring.profiles.active: dev`).
  - `application-dev.yml` → Hardcoded credentials cho dev local, `ddl-auto: update`, `show-sql: true`, Swagger bật.
  - `application-prod.yml` → Dùng env vars (`${DB_URL}`, `${JWT_ACCESS_SECRET}`...), `ddl-auto: validate`, tắt Swagger, ẩn Actuator health details.

**Kiến thức cần nhớ:**

1. **Spring Profiles:** Cơ chế `spring.profiles.active` cho phép Spring Boot tự động merge file `application-{profile}.yml` lên trên `application.yml`. Profile-specific properties luôn **override** common properties.
2. **Environment Variables trong YAML:** Cú pháp `${ENV_VAR}` cho phép Spring Boot đọc giá trị từ biến môi trường hệ thống tại runtime, không cần hardcode.
3. **Hibernate `ddl-auto` modes:** `update` (tự động sửa schema — chỉ dev), `validate` (chỉ kiểm tra schema khớp entity — prod, kết hợp Flyway/Liquibase).
4. **Swagger trong Production:** Tắt hoàn toàn bằng `springdoc.api-docs.enabled: false` và `springdoc.swagger-ui.enabled: false`.

**Phần cần ôn lại:**

- 🟡 Cách dùng Flyway/Liquibase để quản lý database migration thay vì dựa vào `ddl-auto`.
- 🟡 Cách cấu hình `.env` file hoặc Docker Compose secrets cho production deployment.

**Checklist tự kiểm tra:**

- [x] Tạo `application.yml` chỉ chứa cấu hình chung, set default profile `dev`
- [x] Tạo `application-dev.yml` với hardcoded credentials cho local dev
- [x] Tạo `application-prod.yml` sử dụng environment variables
- [x] Hibernate prod: `ddl-auto: validate`, `show-sql: false`
- [x] Tắt Swagger trong prod
- [x] Ẩn Actuator health details trong prod
- [x] `mvn test` pass thành công

**Ghi chú:**

- Đây là bước bắt buộc trước khi deploy bất kỳ ứng dụng Spring Boot nào lên server thực. Không bao giờ hardcode secrets trong file được commit lên Git.
- Tiếp theo cần thêm file `.env.example` vào repo để document các biến môi trường cần thiết cho production.

---

### 06/07/2026 - Fix Concurrency Bug trong `LearningServiceImpl#updateProgress`

**Tập trung vào:** Xử lý race condition khi nhiều request đồng thời cập nhật tiến độ học tập cho cùng một user + lesson.

**Kết quả đạt được:** ✅

- Phát hiện method `updateProgress` gốc dùng pattern **read-then-write** (`find → check → save`) — dẫn đến 2 lỗi nghiêm trọng:
  - **Duplicate Insert Race:** 2 thread cùng thấy "chưa có record" → cả 2 đều insert → thread thứ 2 bị `DataIntegrityViolationException` do UNIQUE constraint trên `(user_id, lesson_id)`.
  - **watchedPercent giảm:** Request cũ (70%) có thể ghi đè request mới (90%) nếu race nhau.
- Áp dụng chiến lược **"Update-first, Insert-on-miss"** kết hợp **Atomic UPDATE query**:
  1. Thêm `updateProgressAtomically()` vào `LessonProgressRepository` — Một câu JPQL `UPDATE` nguyên tử với `CASE WHEN` để đảm bảo `watchedPercent` **chỉ tăng, không bao giờ giảm**.
  2. Sửa logic trong Service: `updateProgressAtomically()` trước → nếu return `0` → `save()` để insert → nếu bị `DataIntegrityViolationException` → gọi lại `updateProgressAtomically()`.

**Kiến thức cần nhớ:**

1. **Race Condition & TOCTOU (Time-of-check to Time-of-use):** Giữa lúc check (`find`) và lúc act (`save`), trạng thái có thể đã thay đổi bởi thread khác. Đây là lỗi kinh điển trong lập trình concurrent.
2. **Atomic UPDATE với CASE WHEN:** Đẩy logic nghiệp vụ (chỉ tăng, không giảm) xuống tầng database bằng `CASE WHEN` trong SQL — database tự đảm bảo tính nguyên tử, không cần lock ở tầng application.
3. **Idempotent Upsert Pattern:** "Update-first, Insert-on-miss with retry" là pattern phổ biến khi cần upsert an toàn mà không dùng `INSERT ... ON DUPLICATE KEY UPDATE` (vì JPA không hỗ trợ native).
4. **`@Modifying` + `@Query`:** Annotation `@Modifying` bắt buộc khi dùng `@Query` cho các câu `UPDATE`/`DELETE` trong Spring Data JPA. Method phải trả về `int` hoặc `void`.

**Phần cần ôn lại:**

- 🟡 Pessimistic Locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) vs Optimistic Locking (`@Version`) — khi nào dùng cái nào?
- 🟡 `@Retryable` của Spring Retry — cách dùng annotation để tự động retry khi gặp exception thay vì catch thủ công.

**Checklist tự kiểm tra:**

- [x] Thêm `updateProgressAtomically()` vào `LessonProgressRepository` với JPQL atomic UPDATE
- [x] Sửa `LearningServiceImpl#updateProgress` theo pattern Update-first, Insert-on-miss
- [x] Bắt `DataIntegrityViolationException` và retry bằng atomic update
- [x] `watchedPercent` đảm bảo chỉ tăng, không giảm (logic CASE WHEN trong SQL)
- [x] `mvn clean compile test` pass thành công

**Ghi chú:**

- Bug này không bao giờ xảy ra khi test thủ công (vì chỉ có 1 người click), nhưng sẽ xảy ra ngay khi frontend tự động gửi progress update mỗi vài giây trong khi user đang xem video.
- Pattern "Update-first, Insert-on-miss" hiệu quả hơn "Insert-first, Update-on-conflict" vì phần lớn request sau lần đầu tiên đều là update (chỉ insert 1 lần duy nhất).

---

### 07/07/2026 - Fix Authorization Bug trong `CourseAdminServiceImpl#getCourses`

**Tập trung vào:** Sửa lỗi phân quyền khiến Teacher nhìn thấy khóa học của tất cả giáo viên khác trong trang quản lý admin.

**Kết quả đạt được:** ✅

- Phát hiện method `getCourses` đã có code kiểm tra role (`isTeacher`, `isAdminOrSuperAdmin`) nhưng **không sử dụng kết quả** — luôn gọi `courseRepository.findAll(pageable)` cho mọi role → Teacher thấy hết khóa học của người khác → vi phạm **Data Isolation**.
- Thêm query `findByTeacherEmail(String email, Pageable pageable)` vào `CourseRepository` với `@EntityGraph(attributePaths = {"teacher"})`.
- Sửa logic `getCourses`: Admin/Super Admin → `findAll()`, Teacher → `findByTeacherEmail()`.

**Kiến thức cần nhớ:**

1. **Data Isolation (Cô lập dữ liệu):** Trong hệ thống multi-role, mỗi user chỉ nên thấy dữ liệu thuộc về mình. Phân quyền phải được thực thi ở **tầng query** (WHERE clause), không phải filter sau khi lấy hết dữ liệu ra memory.
2. **Spring Data JPA Query Derivation:** Tên method `findByTeacherEmail` tự động được parse thành `WHERE teacher.email = ?`. Spring Data JPA hỗ trợ navigation qua relationship (`teacher` → `email`) mà không cần viết JPQL thủ công.
3. **`@EntityGraph` cho Performance:** Sử dụng `@EntityGraph(attributePaths = {"teacher"})` để fetch eager quan hệ `ManyToOne` trong cùng 1 query SQL (`LEFT JOIN FETCH`), tránh N+1 query khi map sang DTO cần thông tin teacher.
4. **Defense in Depth:** Kết hợp cả role check ở controller level **và** query-level filtering ở service level. Nếu một lớp bị bypass, lớp kia vẫn bảo vệ.

**Phần cần ôn lại:**

- 🟡 `@PreAuthorize` / `@PostAuthorize` của Spring Security Method Security — cách dùng SpEL expression để phân quyền ở tầng method.
- 🟡 Custom `Specification` với JPA Criteria API — khi logic filter phức tạp hơn (nhiều điều kiện AND/OR, search, sort).

**Checklist tự kiểm tra:**

- [x] Thêm `findByTeacherEmail(String email, Pageable pageable)` vào `CourseRepository`
- [x] Sử dụng `@EntityGraph` cho query mới để tránh N+1
- [x] Sửa `getCourses` để Admin thấy tất cả, Teacher chỉ thấy của mình
- [x] Response DTO (`CourseRes`) không thay đổi
- [x] `mvn clean compile test` pass thành công

**Ghi chú:**

- Đây là lỗi "silent bug" — không crash, không báo lỗi, nhưng **lộ dữ liệu** cho user không có quyền. Loại bug này nguy hiểm nhất vì rất khó phát hiện bằng test thủ công.
- Bài học rút ra: Mỗi khi viết API có tính chất "list all", luôn tự hỏi: *"User này có quyền thấy TẤT CẢ dữ liệu hay chỉ dữ liệu của mình?"*

---

### 08/07/2026 - Tối ưu hóa N+1 Query trong `StudentDashboardServiceImpl`

**Tập trung vào:** Loại bỏ vấn đề N+1 query khi tải danh sách khóa học và tiến độ học tập trên trang Student Dashboard.

**Kết quả đạt được:** ✅

- Phát hiện method `getMyCourses` sử dụng vòng lặp để duyệt qua danh sách các khóa học đã đăng ký (`enrollments`), và bên trong vòng lặp có gọi 2 query tới `LessonProgressRepository` cho mỗi khóa học (1 query đếm số bài học đã hoàn thành, 1 query lấy tiến độ mới nhất). Nếu user có N khóa học, hệ thống sẽ gọi `1 + 2N` queries (N+1 query problem).
- Tối ưu hóa bằng cách thay thế các query đơn lẻ bằng **Grouped / Projection Queries**:
  - Tạo projection interfaces `CourseProgressCount` và `CourseLatestProgress` trong `LessonProgressRepository`.
  - Viết custom `@Query` để lấy tổng số bài hoàn thành của khóa học (dùng `GROUP BY` và lọc theo danh sách `courseIds` đang học).
  - Viết custom `@Query` để lấy tiến độ học mới nhất bằng subquery với `MAX(updatedAt)`. Đặc biệt, thêm điều kiện `MAX(id)` (Tie-breaker) vào subquery để đảm bảo tính Deterministic khi có nhiều bản ghi trùng thời gian `updatedAt`.
  - Thay đổi kiểu dữ liệu trả về của hàm COUNT trong JPA Projection từ `Integer` sang `Long` để tránh lỗi mapping, sau đó ép kiểu (cast) sang `int` một cách an toàn bằng `.intValue()` trong service.
- Chuyển đổi kết quả query thành `Map<Long, ...>` trong memory và lookup `O(1)` bên trong vòng lặp. Tổng số query giảm xuống còn đúng 3 queries (và chỉ query trên những khóa học user đang enroll) bất kể user đăng ký bao nhiêu khóa học.

**Kiến thức cần nhớ:**

1. **N+1 Query Problem:** Là một vấn đề về hiệu năng kinh điển khi hệ thống thực hiện 1 query để lấy danh sách N phần tử, sau đó thực hiện thêm N queries để lấy dữ liệu chi tiết cho từng phần tử.
2. **JPA Projections:** Thay vì fetch toàn bộ Entity (có thể nặng và chậm), ta có thể định nghĩa các `interface` chỉ chứa các getter tương ứng với các cột/alias trong câu lệnh SQL để Spring Data JPA tự động map dữ liệu (Projection).
3. **In-Memory Grouping (Map Lookup):** Việc fetch toàn bộ dữ liệu cần thiết bằng 1 query lớn, đưa vào một cấu trúc dữ liệu tối ưu như `HashMap`, rồi lookup trong Java sẽ nhanh hơn rất nhiều so với việc chọc xuống Database liên tục.
4. **Subquery trong JPQL và Deterministic Results:** JPQL hỗ trợ sử dụng subquery trong mệnh đề `WHERE` để giải quyết các bài toán lấy bản ghi mới nhất theo nhóm (Greatest-n-per-group) khi không có SQL Window Functions. Luôn sử dụng một column có tính unique (như `id`) làm tie-breaker để query trả về kết quả nhất quán.
5. **JPA COUNT Projection:** Hàm `COUNT()` trong SQL khi được Spring Data JPA map qua interface projection thường trả về kiểu `Long`, không phải `Integer`. Sử dụng `Long` trong interface để tránh `ConverterNotFoundException`.

**Phần cần ôn lại:**

- 🟡 Cấu hình log SQL parameter (như `p6spy` hoặc `datasource-proxy`) để dễ dàng detect N+1 query ngay từ lúc dev.
- 🟡 Cách sử dụng SQL Window Functions (như `ROW_NUMBER() OVER (PARTITION BY ...)`) trong native query nếu subquery JPQL bị chậm với dữ liệu lớn.

**Checklist tự kiểm tra:**

- [x] Tạo các interfaces projection `CourseProgressCount` (kiểu `Long`), `CourseLatestProgress`
- [x] Thêm grouped query vào `LessonProgressRepository` (có filter theo `courseIds` và tie-breaker `MAX(id)`)
- [x] Chuyển đổi logic `StudentDashboardServiceImpl#getMyCourses` sang fetch gom nhóm và in-memory lookup bằng Map
- [x] Đảm bảo cấu trúc response (`MyCourseRes`) không bị ảnh hưởng
- [x] `mvn clean compile test` pass thành công

**Ghi chú:**

- Hiệu năng của API `getMyCourses` giờ đây đã sẵn sàng cho production và có thể mở rộng (scale) tốt kể cả khi học viên mua hàng chục khóa học.

---

### 09/07/2026 - Đồng bộ Course Lesson Counters (Issue 5)

**Tập trung vào:** Đảm bảo các field tổng hợp (`totalLessons`, `totalDurationMinutes`) của `Course` luôn chính xác mỗi khi admin tạo, sửa, hoặc xóa bài học.

**Kết quả đạt được:** ✅

- Cập nhật DTO `LessonCreateReq`, `LessonUpdateReq` và `LessonRes` để hỗ trợ field `durationMinutes` (kèm theo validate `@Min(0)`).
- Viết query `getCourseTotals` trong `LessonRepository` để tự động tổng hợp data: đếm bài học (`COUNT`) và tính tổng thời lượng (`SUM(durationMinutes)`). Query trả về kiểu `Long` để tránh lỗi mapping và bỏ đi thao tác `CAST` cứng nhắc.
- Trong `LessonAdminServiceImpl`, thêm hàm `syncCourseTotals`. Hàm này gọi `lessonRepository.flush()` để đẩy thay đổi xuống DB, sau đó tính toán lại tổng và cập nhật entity `Course`. Tất cả chạy an toàn trong một transaction.

**Kiến thức cần nhớ:**

1. **JPA Entity Lifecycle & Flush:** Khi thay đổi entity trong transaction, dữ liệu chỉ lưu trên RAM. Nếu gọi một câu `SELECT` query liên quan, Hibernate tự động gọi `flush()` (chế độ AUTO) để đẩy SQL xuống DB trước nhằm lấy dữ liệu chuẩn, nhưng an toàn nhất thì có thể tự gọi `flush()`.
2. **Database Aggregation:** Thay vì fetch toàn bộ lessons lên RAM để đếm vòng lặp, đẩy việc tính `COUNT` và `SUM` xuống DB thông qua custom JPQL query tối ưu hơn nhiều.
3. **Transactional Boundary:** Nếu xử lý `Course` update chung trong transaction sửa `Lesson`, thì không cần gọi `courseRepository.save(course)`. Chỉ cần update entity managed là JPA tự commit vào DB cuối transaction.

**Checklist tự kiểm tra:**

- [x] Thêm field `durationMinutes` vào các Request/Response DTO.
- [x] Tạo `CourseTotals` interface projection và query.
- [x] Đồng bộ thông qua `syncCourseTotals()` lúc create/update/delete lesson.
- [x] Pass `mvn test` thành công.

---

### 10/07/2026 - Cấu hình Global Max Page Size cho Pagination

**Tập trung vào:** Cấu hình mức tối đa an toàn (max-page-size) cho Spring Data Pageable để ngăn ngừa các tấn công/stress test qua API phân trang.

**Kết quả đạt được:** ✅

- Cấu hình file `application.yml` thêm các thuộc tính của `spring.data.web.pageable`.
- Đặt `default-page-size: 10` để giữ lại hành vi cũ.
- Thiết lập `max-page-size: 100` để giới hạn cứng. Client truyền parameter `size` vượt mức này sẽ bị Spring ép về 100.

**Kiến thức cần nhớ:**

1. **Security in Pagination:** Người dùng ác ý hoặc bot scraping có thể gửi request dạng `?size=999999` để ép database trả về khối lượng dữ liệu khổng lồ (DoS attack) hoặc để crawl toàn bộ website trong một request. `max-page-size` là phương pháp bảo vệ tiêu chuẩn.

**Checklist tự kiểm tra:**

- [x] Bổ sung cấu hình `spring.data.web.pageable` trong `application.yml`.
- [x] Kiểm tra lại test qua `mvn clean test` thành công.

---

### 11/07/2026 - Tối ưu hiệu năng JWT Authentication (Issue 7)

**Tập trung vào:** Loại bỏ database query trên mỗi authenticated request bằng cách xây dựng `Authentication` trực tiếp từ JWT claims thay vì gọi `UserDetailsService.loadUserByUsername()`.

**Kết quả đạt được:** ✅

- Phân tích `JwtAuthenticationFilter` hiện tại: mỗi request có Bearer token đều gọi `userDetailsService.loadUserByUsername(email)` → query DB để lấy `User` entity + roles. Đây là bottleneck lớn vì access token đã chứa sẵn `email`, `id`, và `roles`.
- Thêm method `extractRoles(token)` vào `JwtUtil` để trích xuất danh sách roles trực tiếp từ JWT claims mà không cần chạm DB.
- Refactor `JwtAuthenticationFilter`:
  - Xóa bỏ dependency `CustomUserDetailsService` (không còn cần inject).
  - Sau khi validate token, extract roles từ claims và xây dựng `List<GrantedAuthority>` bằng `SimpleGrantedAuthority`.
  - Tạo `UserDetails` bằng `org.springframework.security.core.userdetails.User` với email + authorities.
  - Set `Authentication` vào `SecurityContext` như cũ → toàn bộ downstream code (`@PreAuthorize`, `auth.getName()`, `auth.getAuthorities()`) hoạt động bình thường.
- Thêm Javadoc trên class ghi rõ security tradeoff.

**Kiến thức cần nhớ:**

1. **Stateless JWT Tradeoff:** Khi không query DB trên mỗi request, nếu user bị lock/xóa/đổi role trong DB, thay đổi sẽ không có hiệu lực ngay lập tức. User vẫn giữ quyền truy cập cho đến khi Access Token hết hạn. Với `jwt.expiration.access = 900000` (15 phút), cửa sổ rủi ro rất nhỏ và được chấp nhận rộng rãi trong kiến trúc stateless.
2. **Claims-based Authentication:** JWT đã được ký (signed) bằng secret key. Nếu signature hợp lệ, thì data bên trong (email, roles) là đáng tin cậy và không cần verify lại với DB. Đây là nguyên lý cốt lõi của stateless authentication.
3. **Spring Security User class:** `org.springframework.security.core.userdetails.User` là implementation sẵn có của `UserDetails`, dùng để tạo nhanh một principal từ username + authorities mà không cần custom class.

**Phần cần ôn lại:**

- 🟡 Token Revocation Strategy: Nếu sau này cần revoke token ngay lập tức (ví dụ: force logout), có thể dùng blacklist (Redis/in-memory set) để check token `jti` trước khi chấp nhận.
- 🟡 Refresh Token Rotation: Khi refresh token, có thể re-fetch user status từ DB tại thời điểm đó để đảm bảo user vẫn active trước khi cấp access token mới.

**Checklist tự kiểm tra:**

- [x] Thêm `extractRoles()` vào `JwtUtil`
- [x] Refactor `JwtAuthenticationFilter` để build Authentication từ claims
- [x] Xóa unused dependency `CustomUserDetailsService` khỏi filter
- [x] Thêm Javadoc document tradeoff
- [x] `mvn clean compile test` pass thành công

---

### 11/07/2026 - Frontend Auth UI & Integration (Login/Register)

**Tập trung vào:** Xây dựng giao diện và tích hợp API cho chu trình xác thực người dùng (Đăng ký, Đăng nhập) trên Frontend Vue 3.

**Kết quả đạt được:** ✅

- Phân tích rủi ro bảo mật giữa **LocalStorage** và **HTTP-Only Cookies** cho việc lưu trữ JWT Token. Kết luận: Sử dụng LocalStorage cho MVP kết hợp cơ chế auto-escaping chống XSS mặc định của Vue 3. Chuyển sang HTTP-Only Cookie ở Phase 2.
- Cập nhật `auth.service.js`: Bổ sung field `confirmPassword` vào hàm `register()` để khớp với DTO `RegisterRequest` của Backend.
- Xây dựng `RegisterPage.vue` hoàn chỉnh:
  - Form 4 trường: Họ tên, Email, Mật khẩu, Xác nhận mật khẩu.
  - Validation phía client: Regex email, password >= 8 ký tự, confirm password khớp.
  - Bắt lỗi API (400 Validation, 409 Trùng email) và hiển thị thông báo đỏ trên form.
  - Đăng ký thành công → alert → redirect `/login`.
- Xây dựng `LoginPage.vue` hoàn chỉnh:
  - Form Email + Password với trạng thái loading (disabled button khi đang xử lý).
  - Bắt lỗi 401/2002 (sai email/mật khẩu) → hiển thị cảnh báo.
  - Login thành công → lưu Token vào Pinia Store → fetch `/users/me` → redirect `/student/dashboard`.
- Cập nhật `router/guards.js`: Thêm logic **chống login lại** — nếu user đã đăng nhập mà truy cập `/login` hoặc `/register`, tự động đẩy sang `/student/dashboard`.

**Kiến thức cần nhớ:**

1. **XSS vs CSRF Tradeoff:** LocalStorage dễ bị XSS (JavaScript đọc được token), HTTP-Only Cookie miễn nhiễm XSS nhưng dễ bị CSRF. Vue 3 tự động escape template rendering nên rủi ro XSS rất thấp trừ khi dùng `v-html` với nội dung không an toàn.
2. **Navigation Guard Pattern:** Sử dụng `router.beforeEach()` của Vue Router để kiểm tra `meta.requiresAuth` trên route và `authStore.isAuthenticated` để bảo vệ route. Return path string để redirect (không dùng `next()` deprecated).
3. **Reactive Form Validation:** Dùng `reactive()` cho form object và `ref()` cho error message. Validate trước khi bắn request lên server để tiết kiệm tài nguyên backend.

**Checklist tự kiểm tra:**

- [x] Form Login và Register có validation hiển thị lỗi thân thiện
- [x] API Đăng ký tích hợp thành công, hiển thị thông báo và chuyển hướng
- [x] API Đăng nhập tích hợp thành công, lưu token an toàn
- [x] Router Guards chặn truy cập trái phép và chống login lại
- [x] Test luồng hoàn chỉnh qua Browser: Register → Login → Dashboard redirect ✅

**Ghi chú:**

- Tất cả request từ Auth pages đều đi qua `api.js` (Axios) đã cài đặt Token Refresh tự động ở task Foundation.
- Tiếp theo cần xây dựng UI cho Student Dashboard để hiển thị dữ liệu học tập thực tế.

---

### 12/07/2026 - Student Dashboard UI & My Courses View

**Tập trung vào:** Xây dựng giao diện trang Dashboard cho Học viên, hiển thị tổng quan tiến độ học tập và danh sách khóa học đã ghi danh.

**Kết quả đạt được:** ✅

- Phân tích kỹ thuật về cách gọi nhiều API đồng thời trong Vue 3: So sánh `Promise.all()` (render cùng lúc) vs gọi riêng lẻ (render dần dần). Chọn `Promise.all()` cho MVP vì code gọn, quản lý state đơn giản, và giao diện render 1 lần — tạo cảm giác Premium.
- Tạo `student.service.js` — Service layer chuyên gọi 2 API: `GET /users/me/progress` và `GET /users/me/courses`.
- Xây dựng component `ProgressOverviewCard.vue`:
  - Thẻ thống kê có icon, nhãn, giá trị số, dải màu bên trái (border-left).
  - Hỗ trợ hiển thị dạng phần trăm qua prop `isPercent`.
  - Hiệu ứng hover nâng card (translateY) tạo cảm giác tương tác.
- Xây dựng component `MyCourseCard.vue`:
  - Thumbnail gradient placeholder khi không có ảnh.
  - **Progress Bar 3 màu**: xanh dương (< 50%), vàng (50-99%), xanh lá (100%) — trực quan và dễ nhận biết.
  - Metadata: số bài đã hoàn thành / tổng bài, tên bài học gần nhất.
  - Nút "Học tiếp" / "Bắt đầu học" tùy theo trạng thái.
- Viết lại `StudentDashboardPage.vue`:
  - Fetch dữ liệu bằng `Promise.all` trong `onMounted`.
  - 3 trạng thái UI: **Loading** (Spinner xoay), **Error** (kèm nút "Thử lại"), **Empty State** (kèm nút "Khám phá khóa học").
  - Grid responsive cho cả thẻ thống kê và danh sách khóa học.
- Cập nhật `StudentLayout.vue`:
  - Topbar hiển thị **tên user thực tế** từ Pinia store (thay vì hardcode "Học viên").
  - Thêm **nút Đăng xuất** ở footer sidebar (chuyển đỏ khi hover).
  - Thêm icon emoji cho các menu item.

**Kiến thức cần nhớ:**

1. **`Promise.all()` vs Sequential Await:** `Promise.all` gọi tất cả API cùng lúc và chờ cái chậm nhất. Tổng thời gian = `max(API_1, API_2)` thay vì `API_1 + API_2`. Phù hợp khi các API độc lập nhau và cần render đồng bộ.
2. **Component-Based Architecture:** Tách giao diện thành các component nhỏ (`ProgressOverviewCard`, `MyCourseCard`) giúp code dễ maintain, dễ test, và có thể tái sử dụng ở nhiều trang khác nhau.
3. **Empty State UX:** Khi data rỗng, không để trang trắng. Hiển thị thông điệp khuyến khích hành động (Call-to-Action) giúp user biết phải làm gì tiếp theo — đây là best practice phổ biến trong thiết kế ứng dụng.
4. **Computed Properties cho Logic Hiển thị:** Dùng `computed()` để tính toán `progressPercent` và `progressClass` thay vì tính trong template. Code sạch hơn và Vue tự cache kết quả cho performance.

**Checklist tự kiểm tra:**

- [x] Tạo `student.service.js` với `getDashboardProgress()` và `getMyCourses()`
- [x] Component `ProgressOverviewCard.vue` với icon, label, value, border color
- [x] Component `MyCourseCard.vue` với progress bar 3 màu, metadata, nút action
- [x] `StudentDashboardPage.vue` với Promise.all, Loading/Error/Empty states
- [x] `StudentLayout.vue` hiển thị tên user thật + nút Đăng xuất
- [x] Test API qua Vite Proxy: Progress code 1000, Courses code 1000 (2 khóa) ✅

**Ghi chú:**

- Giao diện Dashboard đã sẵn sàng cho bản Demo gửi khách hàng. Tiếp theo cần xây dựng trang Course Catalog (khám phá khóa học) để hoàn thiện luồng trải nghiệm của Học viên.

---

### 13/07/2026 - Fix Axios Auth Interceptor (Refresh Token Loop)

**Tập trung vào:** Sửa lỗi logic trong Axios Auth Interceptor khiến các request public (như Login, Register) bị kẹt trong vòng lặp refresh token và mất thông báo lỗi gốc.

**Kết quả đạt được:** ✅

- Phân tích vấn đề: Khi login sai mật khẩu (backend trả 401 Unauthorized), interceptor tự động kích hoạt tiến trình refresh token. Vì refresh token cũng không hợp lệ/không tồn tại, nó gọi `authStore.clearAuth()` và redirect về `/login`, làm gián đoạn luồng login và "nuốt" mất error message thật sự ("Email hoặc mật khẩu không đúng").
- Cập nhật `frontend/src/services/api.js`:
  - Thêm một `publicAuthPaths` whitelist (bao gồm `/auth/login`, `/auth/register`, `/auth/refresh`, `/auth/logout`).
  - Kiểm tra điều kiện: Nếu `error.response.status === 401` và URL không thuộc danh sách public, mới tiến hành refresh token.
  - Ngược lại, nếu URL là API public, ném thẳng error gốc (`Promise.reject(error)`) ra ngoài.
- Kết quả: `LoginPage.vue` và `RegisterPage.vue` đã nhận được chính xác object `error.response.data.message` từ backend để render lên giao diện màu đỏ cảnh báo. Không còn hiện tượng nháy trang (unexpected redirect).

**Kiến thức cần nhớ:**

1. **Interceptor Whitelisting:** Các API public (nhất là API xác thực) cần được loại trừ (whitelist) khỏi logic tự động renew token. Nếu không, hệ thống sẽ cố gắng chữa lỗi 401 của sai mật khẩu bằng cách xin token mới, gây sai lệch logic hoàn toàn.
2. **Error Transparency:** Interceptor không nên "nuốt" lỗi của các chức năng cần hiển thị lỗi chi tiết cho user (như đăng nhập, đăng ký). Luôn đảm bảo ném `error` ra ngoài bằng `Promise.reject(error)` đúng lúc.

**Checklist tự kiểm tra:**

- [x] Thêm logic whitelist vào file `api.js`.
- [x] Test sai mật khẩu ở `/login`.
- [x] Xác nhận hiện thông báo "Email hoặc mật khẩu không đúng".
- [x] Xác nhận không bị redirect trang một cách bất thường.

---

### 14/07/2026 - Align Frontend Refresh Token Flow with Backend Contract

**Tập trung vào:** Điều chỉnh logic gọi API refresh token trên frontend (`api.js`) để khớp chính xác với chuẩn response và endpoint của backend.

**Kết quả đạt được:** ✅

- Cập nhật endpoint trong `api.js`: Đổi `POST /auth/refresh` thành `POST /auth/refresh-token`.
- Cập nhật danh sách whitelist `publicAuthPaths` để loại trừ `/auth/refresh-token` (ngăn chặn vòng lặp vô hạn khi chính request refresh bị lỗi 401).
- **Xử lý Response:** Nhận thấy backend chỉ trả về `accessToken` mới (một practice phổ biến trong JWT để kéo dài phiên làm việc mà không cần thiết phát hành refresh token mới liên tục). Do đó, bổ sung logic bảo toàn `refreshToken` cũ nếu backend không trả về `refreshToken` mới:
  ```javascript
  const newRefreshToken = data.result.refreshToken || authStore.refreshToken
  ```
- **Xử lý Failure:** Giữ nguyên logic an toàn — khi quá trình refresh thất bại (ví dụ: refresh token hết hạn), gọi `authStore.clearAuth()` và tự động redirect người dùng về trang `/login`.

**Kiến thức cần nhớ:**

1. **Refresh Token Rotation vs Static Refresh Token:**
   - *Rotation:* Backend trả về cả Access Token và Refresh Token mới mỗi lần refresh. An toàn hơn nhưng phức tạp.
   - *Static:* Backend chỉ trả về Access Token mới, giữ nguyên Refresh Token cũ cho đến khi nó hết hạn. Code frontend cần có fallback logic (dùng `||`) để tránh việc vô tình lưu `undefined` đè lên Refresh Token đang còn hạn.
2. **API Contract Consistency:** Lỗi phổ biến nhất khi tích hợp Frontend - Backend là lệch chuẩn endpoint hoặc cấu trúc payload/response. Việc verify kỹ API Contract (ví dụ backend trả gì, tên trường là gì) là cực kỳ quan trọng.

**Checklist tự kiểm tra:**

- [x] Cập nhật URL `/auth/refresh-token` trong axios call và whitelist.
- [x] Thêm logic fallback bảo toàn `refreshToken`.
- [x] Đảm bảo auth được clear và redirect về `/login` khi refresh lỗi.