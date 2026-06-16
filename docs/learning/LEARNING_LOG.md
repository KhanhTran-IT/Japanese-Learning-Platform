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

# Nội dung cập nhật learning docs sau task Login API + JWT Token Generation

## 1. Thêm vào `docs/learning/LEARNING_LOG.md`

## 2026-06-15 - Login API + JWT Token Generation

### 1. Hôm nay tôi đã làm gì?

* Xây dựng API đăng nhập `POST /api/auth/login`.
* Tạo `LoginRequest` để nhận email/password từ client.
* Tạo `LoginResponse` để trả về access token, refresh token và thông tin user.
* Cấu hình JWT trong `application.yml`.
* Thêm thư viện JJWT vào `pom.xml`.
* Tạo `JwtUtil` để generate và xử lý JWT.
* Bổ sung logic login trong `AuthService` và `AuthServiceImpl`.
* Bổ sung endpoint login trong `AuthController`.
* Tạo hoặc cập nhật `RefreshToken` entity và `RefreshTokenRepository`.
* Lưu refresh token vào database sau khi login thành công.
* Bổ sung error code liên quan đến login như `AUTH_002`, `AUTH_003`.
* Test API bằng Swagger.

### 2. Kết quả đạt được

* Backend chạy được.
* Swagger test được API login.
* Login đúng email/password có thể trả về access token và refresh token.
* Response không trả password hoặc passwordHash.
* Refresh token được thiết kế để lưu database, chuẩn bị cho refresh-token API và logout API ở task tiếp theo.

### 3. Kiến thức tôi cần nhớ

* Login API không chỉ kiểm tra email/password mà còn là điểm bắt đầu của authentication flow.
* BCrypt dùng `passwordEncoder.matches(rawPassword, passwordHash)` để kiểm tra mật khẩu.
* Access token nên có thời gian sống ngắn để giảm rủi ro bảo mật.
* Refresh token nên có thời gian sống dài hơn và nên được lưu database để có thể revoke khi logout.
* JWT có thể chứa thông tin định danh như userId, email, roles, expiration.
* Không bao giờ trả Entity trực tiếp ra API, đặc biệt là User entity vì có thể chứa passwordHash.
* Error login nên trả chung một lỗi `Email hoặc mật khẩu không đúng` để tránh lộ email nào tồn tại trong hệ thống.

### 4. Những phần tôi cần ôn lại

* JWT gồm những phần nào: header, payload, signature.
* Sự khác nhau giữa access token và refresh token.
* Cách Spring Security sẽ dùng access token ở các request sau.
* Cách lưu và revoke refresh token.
* Cách xử lý token hết hạn.
* Cách thiết kế response DTO an toàn.

### 5. Checklist tự kiểm tra

* [ ] Tôi giải thích được Login API dùng để làm gì.
* [ ] Tôi giải thích được vì sao cần access token và refresh token.
* [ ] Tôi giải thích được vì sao refresh token nên lưu database.
* [ ] Tôi giải thích được vì sao không trả User entity trực tiếp.
* [ ] Tôi biết cách test login đúng/sai bằng Swagger hoặc Postman.
* [ ] Tôi biết task tiếp theo là Refresh Token API + Logout API.

---

## 2. Thêm vào `docs/learning/INTERVIEW_NOTES.md`

## Login API + JWT Token Generation

### 1. Tóm tắt ngắn gọn

Login API dùng để xác thực user bằng email/password. Nếu thông tin hợp lệ, backend tạo access token để user gọi các API cần đăng nhập, đồng thời tạo refresh token để lấy access token mới khi access token hết hạn. Refresh token được lưu vào database để hệ thống có thể revoke khi logout hoặc khi cần vô hiệu hóa token.

### 2. Kiến thức phỏng vấn liên quan

* Authentication là quá trình xác minh user là ai.
* Authorization là quá trình kiểm tra user có quyền làm gì.
* BCrypt dùng để hash và verify password.
* JWT là token có thể chứa thông tin user và thời gian hết hạn.
* Access token nên ngắn hạn.
* Refresh token nên dài hạn và có thể revoke.
* DTO giúp response an toàn hơn, không lộ Entity.
* Không nên trả thông báo quá chi tiết khi login sai để tránh lộ thông tin tài khoản.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Login API hoạt động như thế nào?

Trả lời:
Frontend gửi email và password lên backend. Backend tìm user theo email, kiểm tra password bằng BCrypt, kiểm tra trạng thái tài khoản, sau đó tạo access token và refresh token. Cuối cùng backend trả token và thông tin user an toàn về frontend.

#### Câu 2: Vì sao không so sánh password trực tiếp với passwordHash?

Trả lời:
Vì password trong database đã được hash bằng BCrypt. Mỗi lần hash có thể sinh ra chuỗi khác nhau do salt, nên phải dùng `passwordEncoder.matches()` để kiểm tra raw password với passwordHash.

#### Câu 3: Access token là gì?

Trả lời:
Access token là token ngắn hạn dùng để xác thực các request sau khi user đăng nhập. Frontend gửi token này trong header `Authorization: Bearer <token>`.

#### Câu 4: Refresh token là gì?

Trả lời:
Refresh token là token dài hạn dùng để xin access token mới khi access token hết hạn. Refresh token thường được lưu database để có thể revoke khi logout hoặc khi phát hiện rủi ro bảo mật.

#### Câu 5: Vì sao cần cả access token và refresh token?

Trả lời:
Access token sống ngắn giúp bảo mật tốt hơn. Refresh token sống dài giúp user không phải đăng nhập lại liên tục. Kết hợp cả hai giúp cân bằng bảo mật và trải nghiệm người dùng.

#### Câu 6: JWT gồm những phần nào?

Trả lời:
JWT gồm 3 phần: header, payload và signature. Header mô tả thuật toán, payload chứa claims, signature dùng để kiểm tra token có bị sửa đổi hay không.

#### Câu 7: Có nên lưu access token vào database không?

Trả lời:
Thông thường không cần. Access token thường stateless, backend verify bằng secret key hoặc public key. Nhưng refresh token nên lưu database để hỗ trợ revoke.

#### Câu 8: Vì sao không trả User entity trực tiếp trong LoginResponse?

Trả lời:
User entity có thể chứa dữ liệu nhạy cảm như passwordHash, status nội bộ hoặc quan hệ database phức tạp. Dùng DTO giúp chỉ trả những field cần thiết và an toàn.

#### Câu 9: Khi login sai email hoặc password, nên trả lỗi thế nào?

Trả lời:
Nên trả lỗi chung như “Email hoặc mật khẩu không đúng”, không nên nói rõ email không tồn tại hay password sai để tránh lộ thông tin tài khoản.

#### Câu 10: Refresh token được dùng trong task tiếp theo như thế nào?

Trả lời:
Task tiếp theo sẽ tạo API refresh-token. Frontend gửi refresh token lên backend, backend kiểm tra token hợp lệ, chưa hết hạn, chưa revoked và tồn tại trong database. Nếu hợp lệ, backend cấp access token mới.

---

## 3. Thêm vào `docs/learning/CONCEPTS_EXPLAINED.md`

## JWT

### Giải thích ngắn gọn

JWT là một dạng token dùng để truyền thông tin xác thực giữa client và server. Sau khi user đăng nhập thành công, backend tạo JWT và frontend gửi JWT này trong các request cần đăng nhập.

### Ví dụ trong project này

Khi user gọi `POST /api/auth/login` thành công, backend tạo `accessToken`. Frontend sẽ dùng access token này để gọi các API như `/api/users/me` hoặc API học bài sau này.

### Câu hỏi phỏng vấn liên quan

JWT gồm những phần nào?

### Câu trả lời ngắn gọn

JWT gồm header, payload và signature. Header mô tả thuật toán, payload chứa claims, signature dùng để xác minh token có bị chỉnh sửa hay không.

## Access Token

### Giải thích ngắn gọn

Access token là token ngắn hạn dùng để xác thực user khi gọi API protected.

### Ví dụ trong project này

Sau login, user nhận access token và gửi nó trong header:

```text
Authorization: Bearer <accessToken>
```

### Câu hỏi phỏng vấn liên quan

Vì sao access token nên sống ngắn?

### Câu trả lời ngắn gọn

Vì nếu access token bị lộ, thời gian bị lợi dụng sẽ ngắn hơn, giúp giảm rủi ro bảo mật.

## Refresh Token

### Giải thích ngắn gọn

Refresh token là token dài hạn dùng để lấy access token mới khi access token hết hạn.

### Ví dụ trong project này

Login API tạo refresh token và lưu vào bảng `refresh_tokens`. Task tiếp theo sẽ dùng refresh token này để tạo API `/api/auth/refresh-token`.

### Câu hỏi phỏng vấn liên quan

Vì sao refresh token nên lưu database?

### Câu trả lời ngắn gọn

Vì lưu database giúp backend có thể revoke refresh token khi user logout, đổi mật khẩu hoặc khi phát hiện rủi ro bảo mật.

## BCrypt Password Verification

### Giải thích ngắn gọn

BCrypt dùng để hash password và verify password. Khi login, backend không giải mã passwordHash mà dùng `passwordEncoder.matches()` để kiểm tra password user nhập có khớp với passwordHash không.

### Ví dụ trong project này

Trong Login API, service dùng:

```java
passwordEncoder.matches(request.getPassword(), user.getPasswordHash())
```

### Câu hỏi phỏng vấn liên quan

Vì sao không lưu password dạng plain text?

### Câu trả lời ngắn gọn

Vì nếu database bị lộ, password người dùng sẽ bị lộ trực tiếp. Hash bằng BCrypt giúp giảm rủi ro vì password thật không được lưu trong database.

## LoginResponse DTO

### Giải thích ngắn gọn

LoginResponse DTO là object dùng để trả dữ liệu login ra frontend, gồm access token, refresh token và thông tin user an toàn.

### Ví dụ trong project này

LoginResponse trả về:

```text
accessToken
refreshToken
user.id
user.fullName
user.email
user.roles
```

Không trả passwordHash.

### Câu hỏi phỏng vấn liên quan

Vì sao cần DTO thay vì trả Entity?

### Câu trả lời ngắn gọn

DTO giúp kiểm soát dữ liệu trả ra API, tránh lộ thông tin nhạy cảm và làm response rõ ràng hơn.
