# Giải Thích Các Khái Niệm

Tài liệu ghi lại các khái niệm, pattern, best practices mà bạn đã học được và muốn ghi nhớ lâu dài.

## Hướng dẫn ghi chép

- **Ghi tên concept rõ ràng** - tên tiếng Anh và tiếng Việt
- **Giải thích ngắn gọn** ý tưởng chính
- **Cho ví dụ cụ thể** từ project hoặc real-world
- **Ghi lợi ích/khi nào dùng**
- **Ghi các misconception** hay gặp phải
- **Link tới resources** nếu có
- **Rating hiểu biết**: 🔴 Chưa hiểu | 🟡 Hiểu một phần | 🟢 Hiểu rõ

## Template mẫu

```markdown
### [Concept Name] - [Tên tiếng Việt]

**Rating:** 🔴/🟡/🟢

**Định nghĩa:**
Giải thích ngắn gọn về concept

**Ví dụ:**
\`\`\`java
// Ví dụ code
\`\`\`

**Lợi ích/Khi nào dùng:**

- Lợi ích 1
- Lợi ích 2

**Misconception hay gặp:**

- Hiểu sai 1
- Hiểu sai 2

**Resources:** Link tới bài viết, video
```

## Concepts

### Dependency Injection (DI) - Tiêm Phụ Thuộc

**Rating:** 🟢

**Định nghĩa:**
Pattern mà các dependency (phụ thuộc) được truyền từ bên ngoài vào class, thay vì class tự tạo. Ở Spring Boot dùng `@Autowired` hoặc constructor injection.

**Ví dụ:**

```java
// ❌ Tự tạo dependency - khó test
public class UserService {
    private UserRepository repo = new UserRepository();
}

// ✅ Inject dependency - dễ test
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }
}
```

**Lợi ích/Khi nào dùng:**

- Dễ test (mock dependency)
- Giảm coupling giữa các class
- Dễ thay đổi implementation

**Misconception hay gặp:**

- DI chỉ là `@Autowired` - sai, constructor injection tốt hơn
- DI làm code phức tạp - sai, nó làm code dễ maintain

---

### REST API Design - Thiết Kế REST API

**Rating:** 🟢

**Định nghĩa:**
Cách thiết kế API theo REST principles: sử dụng HTTP methods (GET, POST, PUT, DELETE) đúng cách, tài nguyên được đại diện bởi URL.

**Ví dụ:**

```
GET    /api/users           - Lấy danh sách users
GET    /api/users/{id}      - Lấy user cụ thể
POST   /api/users           - Tạo user mới
PUT    /api/users/{id}      - Cập nhật user
DELETE /api/users/{id}      - Xóa user
```

**Lợi ích/Khi nào dùng:**

- API dễ hiểu, dễ sử dụng
- Standard, dễ collaborate với team
- Client có thể guess endpoint

**Misconception hay gặp:**

- POST dùng cho mọi thao tác - sai, PUT cho update
- URL nên có động từ `/api/users/create` - sai, dùng HTTP method

---

### Vue Composition API - API soạn thảo Vue

**Rating:** 🟡

**Định nghĩa:**
Cách viết Vue 3 component bằng function thay vì object. Giúp tổ chức logic tốt hơn và tái sử dụng code dễ hơn.

**Ví dụ:**

```javascript
// Vue 2 / Options API
export default {
  data() { return { count: 0 } },
  methods: { increment() { this.count++ } }
}

// Vue 3 / Composition API
export default {
  setup() {
    const count = ref(0)
    const increment = () => count.value++
    return { count, increment }
  }
}
```

**Lợi ích/Khi nào dùng:**

- Logic liên quan được group lại
- Dễ tái sử dụng logic qua composables
- Setup() dễ test

**Misconception hay gặp:**

- Phải dùng Composition API - sai, Options API cũng ok cho đơn giản
- `value` là lỗi - không, ref.value cần thiết

---

### N+1 Query Problem - Vấn đề Truy Vấn N+1

**Rating:** 🟡

**Định nghĩa:**
Khi query 1 user (1 query), sau đó lặp qua 100 user để query orders của mỗi user (100 queries), tổng cộng 101 queries. Cần dùng JOIN hoặc fetch strategy tốt.

**Ví dụ:**

```java
// ❌ N+1 problem
List<User> users = userRepository.findAll(); // 1 query
users.forEach(u -> {
    System.out.println(u.getOrders()); // 100 queries
});

// ✅ Fix với JOIN FETCH hoặc eager loading
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();
```

**Lợi ích/Khi nào dùng:**

- Cải thiện performance đáng kể
- Đặc biệt quan trọng khi data lớn

**Misconception hay gặp:**

- Lazy loading luôn xấu - không, lazy loading ok nếu đúng juncture
- Phải eager load mọi thứ - không, chỉ eager load khi cần

---

## Spring Boot Backend Foundation Concepts

### 1. Spring Boot - Spring Boot Framework

**Rating:** 🟢

**Giải thích:**
Framework Java giúp tạo standalone, production-ready REST API server một cách nhanh chóng. Tự động setup configuration, không cần file XML phức tạp.

**Ví dụ trong project:**

- Backend folder của bạn dùng Spring Boot
- Chạy `mvn spring-boot:run` → Server chạy tại `localhost:8080`
- Swagger UI mở được → tất cả do Spring Boot

**Ưu điểm:**

- Setup nhanh, convention over configuration
- Tự động config Tomcat web server
- Dễ deploy (1 file JAR chứa tất cả)

**Câu hỏi phỏng vấn:**

> "Tại sao bạn chọn Spring Boot thay vì Spring Framework thuần?"

**Câu trả lời mẫu:**

> "Spring Boot nhanh hơn, ít boilerplate, tự động config mọi thứ. Spring Framework thuần cần config XML dài dòng. Spring Boot thích hợp hơn cho startup hoặc project cần nhanh."

---

### 2. REST API - RESTful API

**Rating:** 🟢

**Giải thích:**
Kiến trúc API dùng HTTP protocol để định nghĩa operations trên resources. Sử dụng standard HTTP methods (GET, POST, PUT, DELETE) và HTTP status codes.

**Ví dụ trong project:**

```
GET  http://localhost:8080/api/health        → Lấy health status
POST http://localhost:8080/api/users         → Tạo user mới
PUT  http://localhost:8080/api/users/{id}    → Cập nhật user
DELETE http://localhost:8080/api/users/{id}  → Xóa user
```

**Ưu điểm:**

- Standard, mọi developer đều hiểu
- Stateless, dễ scale
- Mỗi endpoint có trách nhiệm rõ ràng

**Câu hỏi phỏng vấn:**

> "REST API khác GraphQL ở điểm nào?"

**Câu trả lời mẫu:**

> "REST dùng URL + HTTP method để định nghĩa operation, GraphQL dùng single endpoint + query language. REST dễ caches và hiểu, GraphQL linh hoạt hơn nhưng phức tạp hơn."

---

### 3. Controller - HTTP Request Handler

**Rating:** 🟢

**Giải thích:**
Class trong Spring Boot xử lý HTTP requests từ client. Định nghĩa endpoints, nhận/trả dữ liệu, delegate logic cho Service layer.

**Ví dụ trong project:**

```java
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
```

**Ưu điểm:**

- Tách biệt HTTP handling từ business logic
- Dễ test (mock HTTP layer)
- Rõ ràng, mỗi method là một endpoint

**Câu hỏi phỏng vấn:**

> "Controller có nên chứa business logic không?"

**Câu trả lời mẫu:**

> "Không. Controller chỉ handle HTTP request/response và validation. Business logic nên ở Service layer. Này giúp code dễ test, tái sử dụng, maintain."

---

### 4. ApiResponse - Chuẩn hóa Response

**Rating:** 🟢

**Giải thích:**
Class wrapper để chuẩn hóa format response trả về từ tất cả API endpoints. Đảm bảo client luôn biết structure response.

**Ví dụ trong project:**

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "status": "UP",
    "timestamp": "2026-06-12T10:30:00"
  }
}
```

**Ưu điểm:**

- Frontend developer biết chính xác format
- Dễ xử lý lỗi (code, message chuẩn)
- Dễ thêm metadata (pagination, timestamp)

**Câu hỏi phỏng vấn:**

> "Tại sao cần ApiResponse wrapper? Không trả response object trực tiếp được?"

**Câu trả lời mẫu:**

> "Nếu không có wrapper: API này trả `{user: {...}}`, API khác trả `[{...}]`, client phải xử lý từng cách. Có wrapper: Tất cả trả `{code, message, data}`, client xử lý 1 cách duy nhất. Dễ maintain, dễ debug, dễ log."

---

### 5. GlobalExceptionHandler - Xử lý Lỗi Toàn Cục

**Rating:** 🟢

**Giải thích:**
Class Spring dùng `@RestControllerAdvice` bắt tất cả exceptions xảy ra trong application và trả về response chuẩn thay vì error 500 lộn xộn.

**Ví dụ trong project:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(404)
            .body(ApiResponse.error(404, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(500)
            .body(ApiResponse.error(500, "Internal server error"));
    }
}
```

**Ưu điểm:**

- Tất cả lỗi trả về format chuẩn
- Không expose sensitive information
- Dễ log và track lỗi

**Câu hỏi phỏng vấn:**

> "Nếu không có GlobalExceptionHandler thì sao?"

**Câu trả lời mẫu:**

> "Client nhận stack trace dài lộn xộn, không hiểu gì. Nên có GlobalExceptionHandler để catch lỗi, format chuẩn, trả message user-friendly."

---

### 6. Swagger/OpenAPI - API Documentation Tự Động

**Rating:** 🟢

**Giải thích:**
Tool tự động generate documentation cho API từ code annotations. Cho phép test API trực tiếp từ web UI.

**Ví dụ trong project:**

```java
@GetMapping("/health")
@Operation(summary = "Check API health status")
@ApiResponse(responseCode = "200", description = "API is UP")
public ResponseEntity<ApiResponse> health() {
    return ResponseEntity.ok(ApiResponse.success(Map.of("status", "UP")));
}
```

Mở tại `http://localhost:8080/swagger-ui/index.html` → thấy endpoint `/api/health` với description và response format.

**Ưu điểm:**

- Documentation luôn up-to-date (từ code)
- Frontend dev tự khám phá API
- Có thể test endpoint trực tiếp

**Câu hỏi phỏng vấn:**

> "Swagger documentation tự động cập nhật khi code thay đổi sao?"

**Câu trả lời mẫu:**

> "Vì Swagger annotations `@Operation`, `@ApiResponse` được viết trực tiếp trong code. Khi bạn deploy, Swagger generate documentation từ annotations này. Nên nó luôn match code hiện tại."

---

### 7. Health Check API - Kiểm Tra Server Sống

**Rating:** 🟢

**Giải thích:**
Endpoint đơn giản (thường GET /health) để kiểm tra xem backend server có đang chạy bình thường hay không. Dùng trong production monitoring.

**Ví dụ trong project:**

```
GET http://localhost:8080/api/health
Response: {
  "status": "UP",
  "timestamp": "2026-06-12T10:30:00",
  "database": "CONNECTED"
}
```

**Ưu điểm:**

- DevOps/Kubernetes dùng để biết server còn sống
- Load balancer biết route request vào server healthy
- Alert khi server fail

**Câu hỏi phỏng vấn:**

> "Health Check API dùng để làm gì? Tại sao quan trọng?"

**Câu trả lời mẫu:**

> "Health Check giúp monitoring system biết server còn sống hay chết. Kubernetes/Docker tự động restart container nếu health check fail. Load balancer tránh route request vào server dead. Quan trọng cho high availability."

---

### 8. application.yml - Cấu Hình Ứng Dụng

**Rating:** 🟢

**Giải thích:**
File YAML trong Spring Boot để cấu hình ứng dụng: server port, database connection, logging, profile (dev/prod).

**Ví dụ trong project:**

```yaml
# src/main/resources/application.yml
spring:
  application:
    name: japanese-learning-platform
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  datasource:
    url: jdbc:mysql://localhost:3306/jlp
    username: root
    password: password

server:
  port: 8080
  servlet:
    context-path: /
```

**Ưu điểm:**

- Externalize configuration (không hardcode)
- Dễ thay đổi giữa dev/prod environments
- Centralized configuration

**Câu hỏi phỏng vấn:**

> "Tại sao không hardcode database URL vào code?"

**Câu trả lời mẫu:**

> "Vì dev, staging, production cần database khác nhau. Hardcode sẽ phải rebuild code cho mỗi environment. Dùng application.yml cho phép set via environment variables, không cần rebuild."

---

### 9. Maven - Build Tool & Dependency Manager

**Rating:** 🟢

**Giải thích:**
Tool Java để build project, manage dependencies (libraries). File `pom.xml` định nghĩa project structure, dependencies, build configuration.

**Ví dụ trong project:**

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    </dependency>
</dependencies>

<!-- Chạy command -->
mvn clean install      <!-- Download dependencies, compile, package -->
mvn spring-boot:run    <!-- Run Spring Boot application -->
```

**Ưu điểm:**

- Tập trung quản lý dependencies 1 chỗ
- Tự động download từ Maven repository
- Build reproducible (lần build nào cũng giống)

**Câu hỏi phỏng vấn:**

> "Maven là gì? Tại sao cần?"

**Câu trả lời mẫu:**

> "Maven là build tool + dependency manager cho Java. `pom.xml` define dependencies (Spring Boot, JPA, etc.), Maven tự động download từ repo, compile code, run tests, package. Không cần manually manage JAR files."

---

### 10. Dependency - Phụ Thuộc / Thư Viện

**Rating:** 🟢

**Giải thích:**
Một library/framework bên ngoài mà project cần để hoạt động. Ví dụ Spring Boot, JPA, MySQL Driver. Maven manage dependencies từ Maven Central Repository.

**Ví dụ trong project:**

```xml
<!-- Dependencies trong pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.1.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

**Ưu điểm:**

- Code reuse, không cần viết lại
- Tiết kiệm thời gian development
- Có version control (dễ update)

**Câu hỏi phỏng vấn:**

> "Dependency hell là gì? Làm sao tránh?"

**Câu trả lời mẫu:**

> "Dependency hell: Project A depend B v1.0, B depend C v2.0; Project A depend C v3.0 → Conflict. Tránh: Dùng dependency management tool (Maven), update dependencies định kỳ, test sau khi update, đừng dùng quá nhiều dependencies."

---

## Ghi chú

**Tổng kết Backend Foundation:**

- Spring Boot tự động setup, Maven manage dependencies
- Controller handle HTTP, Service handle logic, Repository handle database
- ApiResponse + GlobalExceptionHandler chuẩn hóa error handling
- Swagger tự động generate documentation
- Health Check + application.yml chuẩn bị cho production

**Phần cần ôn lại thêm:**

- Spring Security (authentication, authorization)
- JPA/Hibernate (database ORM)
- Unit testing với JUnit + Mockito
- Docker + Kubernetes deployment

---

## Auth/User Database - JPA & Entities Concepts

### JPA Entity - JPA Thực thể

**Rating:** 🟢

**Giải thích:**
Class Java được đánh dấu `@Entity` để Hibernate map với 1 table trong database. Mỗi field là 1 column, mỗi instance là 1 row.

**Ví dụ trong project:**

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;  // ACTIVE, INACTIVE, SUSPENDED, DELETED
}
```

**Ưu điểm:**

- Hibernate tự động tạo/update tables
- Code Java thay vì SQL thuần
- Dễ maintain, refactor

**Câu hỏi phỏng vấn:**

> "Vì sao phải dùng @Entity thay vì viết SQL CREATE TABLE?"

**Câu trả lời:**

> "Vì @Entity cho phép:
>
> - Write once, deploy everywhere (database agnostic)
> - Tự động create/update schema (Hibernate ddl-auto)
> - Type-safe queries (không string SQL dễ sai)
> - Easier refactoring (IDE can track references)."

---

### ManyToMany - Quan hệ Nhiều - Nhiều

**Rating:** 🟢

**Giải thích:**
Khi 1 entity có thể liên kết với nhiều entity khác, và entity kia cũng có thể liên kết với nhiều entity đầu tiên. Cần 1 join table ở giữa.

**Ví dụ trong project:**

```java
// User.java
@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();

// Role.java
@ManyToMany(mappedBy = "roles")  // Inverse side
private Set<User> users = new HashSet<>();
```

**Database structure:**

```
table users: id, email, password, ...
table roles: id, name, ...
table user_roles: user_id (FK), role_id (FK)  -- Join table
```

**Ưu điểm:**

- Hỗ trợ các relationship phức tạp
- Hibernate handle join logic

**Misconception hay gặp:**

- ❌ "Đặt Role đảo ngược là user_id, role_id" - sai, join table chứa 2 FKs
- ❌ "CascadeType.ALL là tốt nhất" - sai, dùng PERSIST + MERGE thường an toàn hơn

**Câu hỏi phỏng vấn:**

> "Tại sao ManyToMany cần join table? Không thể lưu trong 1 table được sao?"

**Câu trả lời:**

> "Vì nếu lưu trong 1 table (ví dụ users): users (id, email, role_id) thì:
>
> - 1 user có nhiều roles → phải có nhiều rows cho 1 user
> - Dữ liệu duplicate (email, password duplicate nhiều lần)
> - Waste storage, khó query
>
> Dùng join table:
>
> - users: id, email, password (1 row per user)
> - roles: id, name (1 row per role)
> - user_roles: user_id, role_id (many rows)
> - Dữ liệu normalized, efficient."

---

### FetchType.LAZY vs EAGER - Kiểu Tải Dữ Liệu

**Rating:** 🟢

**Giải thích:**
Kiểm soát khi nào related entities được load từ database.

**Ví dụ:**

```java
// LAZY: Roles không load khi query User
@ManyToMany(fetch = FetchType.LAZY)
private Set<Role> roles;

// EAGER: Roles tự động load khi query User (dùng JOIN)
@ManyToMany(fetch = FetchType.EAGER)
private Set<Role> roles;
```

**So sánh:**
| | LAZY | EAGER |
|---|---|---|
| **Query User** | 1 query | 1 query (+ JOIN) |
| **Access roles** | +1 query | 0 queries (đã loaded) |
| **100 users** | 1 + N queries (N+1) | 1 query (1 JOIN) |
| **Memory** | Thấp | Cao |
| **Network** | Bất ngờ delays | Có thể slow JOIN |

**Quy tắc:**

- Dùng **LAZY** mặc định (hầu hết trường hợp)
- Dùng **EAGER** chỉ khi chắc chắn cần data + acceptable JOINs

**Câu hỏi phỏng vấn:**

> "N+1 problem là gì? Làm sao fix?"

**Câu trả lời:**

> "N+1: Query 1 user → N+1 queries thay vì 1. Fix: Dùng FetchType.EAGER hoặc @Query với LEFT JOIN FETCH."

---

### Spring Data JPA Repository - Truy vấn Dữ liệu

**Rating:** 🟢

**Giải thích:**
Interface extends `JpaRepository<T, ID>` để query database mà không viết SQL.

**Ví dụ trong project:**

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // Method name conventions
    User findByEmail(String email);
    List<User> findByStatus(UserStatus status);
    List<User> findByStatusAndCreatedDateAfter(UserStatus status, LocalDateTime date);

    // Custom query
    @Query("SELECT u FROM User u WHERE u.status = :status")
    List<User> findActiveUsers(@Param("status") UserStatus status);
}
```

**Ưu điểm:**

- Tự động generate implementations
- Type-safe queries
- Mixin methods từ JpaRepository (save, delete, findAll, etc.)

**Phương thức quy ước:**

- `findBy*` → SELECT ... WHERE
- `findBy*OrderBy*` → ORDER BY
- `countBy*` → COUNT
- `*And*`, `*Or*` → AND, OR

**Câu hỏi phỏng vấn:**

> "Spring Data tự động generate queries làm thế nào?"

**Câu trả lời:**

> "Spring dùng reflection để parse method name, generate SQL tương ứng. Ví dụ `findByEmail` → `SELECT * FROM users WHERE email = ?`. Nếu method complex quá, dùng @Query annotation viết SQL tay."

---

### Enum - Tập hợp Giá trị Cố định

**Rating:** 🟢

**Giải thích:**
Type Java với một tập các giá trị cố định (không thể add/remove). Dùng để ràng buộc valid values.

**Ví dụ trong project:**

```java
public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    DELETED("Deleted");

    private final String displayName;
    UserStatus(String displayName) {
        this.displayName = displayName;
    }
}

public enum RoleName {
    ADMIN, TEACHER, STUDENT
}

// Sử dụng
@Enumerated(EnumType.STRING)  // Lưu tên enum ("ACTIVE")
private UserStatus status;
```

**Database:**

```sql
-- MySQL sử dụng ENUM type
ALTER TABLE users ADD COLUMN status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED');
```

**Ưu điểm:**

- Type-safe (không thể gán giá trị random)
- Compile-time checking
- Switch statements dễ

**Ví dụ sử dụng:**

```java
if (user.getStatus() == UserStatus.ACTIVE) {
    // ...
}
```

**Câu hỏi phỏng vấn:**

> "Enum ORDINAL vs STRING, cái nào tốt hơn?"

**Câu trả lời:**

> "STRING tốt hơn vì:
>
> - @Enumerated(EnumType.STRING): Lưu "ACTIVE" (readable, safe)
> - @Enumerated(EnumType.ORDINAL): Lưu 0, 1, 2 (compact, nhưng if reorder enum → data corrupt)
>
> Best practice: Dùng STRING."

---

### CascadeType - Lan truyền Hành động

**Rating:** 🟢

**Giải thích:**
Khi thực hiện hành động trên parent entity (persist, merge, remove), nó lan truyền tới child entities.

**Ví dụ:**

```java
// ❌ Nguy hiểm - dùng ALL
@OneToMany(cascade = CascadeType.ALL)
private Set<RefreshToken> tokens;  // Xóa user → xóa tokens OK

// ❌ Còn nguy hiểm hơn - dùng ALL cho ManyToMany
@ManyToMany(cascade = CascadeType.ALL)  // Xóa user → xóa roles! Sai!
private Set<Role> roles;

// ✅ Đúng - dùng PERSIST + MERGE cho ManyToMany
@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
private Set<Role> roles;
```

**Các CascadeType:**
| Type | Ý nghĩa |
|---|---|
| **PERSIST** | Khi persist parent → persist children |
| **MERGE** | Khi merge parent → merge children |
| **REMOVE** | Khi delete parent → delete children |
| **REFRESH** | Khi refresh parent → refresh children |
| **ALL** | Tất cả các hành động trên |

**Quy tắc:**

- OneToMany (User → RefreshTokens): `CascadeType.ALL` OK
- ManyToMany (User ↔ Roles): `CascadeType.PERSIST, MERGE` (never REMOVE)

**Câu hỏi phỏng vấn:**

> "Tại sao không nên dùng CascadeType.REMOVE cho ManyToMany?"

**Câu trả lời:**

> "Vì 1 role có thể thuộc nhiều users. Nếu xóa user với REMOVE cascade → xóa role luôn → tất cả users khác mất role. Disaster!"

---

### RefreshToken - Token Tái tạo

**Rating:** 🟢

**Giải thích:**
Token dài hạn dùng để request AccessToken mới khi token hiện tại hết hạn. Được lưu trong database để có thể revoke.

**Ví dụ trong project:**

```java
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
}

// Flow:
// 1. User login → sinh RefreshToken, lưu DB, trả cho client
// 2. AccessToken expire → client gửi RefreshToken
// 3. Server check RefreshToken còn valid không, sinh AccessToken mới
// 4. Logout → xóa RefreshToken từ DB (revoke)
```

**Ưu điểm:**

- Tăng security (short-lived AccessToken)
- Có thể logout (revoke token)
- Có thể track user sessions

**Câu hỏi phỏng vấn:**

> "Tại sao cần RefreshToken? Không thể dùng long-lived AccessToken?"

**Câu trả lời:**

> "Nếu AccessToken bị steal:
>
> - Long-lived (7 ngày): Attacker dùng được 7 ngày
> - Short-lived (15 phút): Attacker chỉ dùng được 15 phút
>
> RefreshToken:
>
> - Long-lived, nhưng lưu backend DB
> - Backend có thể track, revoke khi nghi ngờ
> - Frontend không thể revoke AccessToken, chỉ backend revoke RefreshToken."

---

### @JoinTable - Bảng Kết Nối

**Rating:** 🟢

**Giải thích:**
Annotation định nghĩa tên và structure của join table cho ManyToMany relationships.

**Ví dụ:**

```java
@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinTable(
    name = "user_roles",  // Tên join table
    joinColumns = @JoinColumn(name = "user_id"),  // FK column pointing to User
    inverseJoinColumns = @JoinColumn(name = "role_id")  // FK column pointing to Role
)
private Set<Role> roles = new HashSet<>();
```

**Generated table:**

```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

**Câu hỏi phỏng vấn:**

> "Tại sao phải dùng @JoinTable? Hibernate không thể tự generate?"

**Câu trả lời:**

> "Hibernate có thể tự generate, nhưng tên column/table có thể không đẹp (ví dụ user_roles_id, user_roles_role_id). @JoinTable cho phép customize:
>
> - Tên join table
> - Tên FK columns
> - Indexes, constraints
>
> Best practice: Luôn customize thay vì dùng default."

---

### @GeneratedValue - Tự động Sinh Khóa Chính

**Rating:** 🟢

**Giải thích:**
Annotation xác định cách sinh giá trị khóa chính (ID). Có nhiều chiến lược:

**Ví dụ:**

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)  // DB auto-increment
private Long id;

// Strategies:
// IDENTITY: DB auto-increment (auto_increment trong MySQL)
// SEQUENCE: DB sequence (Oracle, PostgreSQL)
// AUTO: Spring tự chọn based on database
// UUID: Dùng UUID thay vì numeric
```

**Ưu điểm:**

- ID tự động sinh, không cần set tay
- Database level guarantee unique

**Câu hỏi phỏng vấn:**

> "Generationtype IDENTITY vs SEQUENCE khác gì?"

**Câu trả lời:**

> "IDENTITY (MySQL): Auto-increment column (1, 2, 3, ...). Sau khi insert, get ID từ DB.
> SEQUENCE (Oracle): DB có sequence object, Spring call để get next ID trước insert.
>
> IDENTITY dễ hơn (1 step), SEQUENCE linh hoạt hơn (batch operations)."

---

## Register API - DTO & Validation Concepts

### DTO (Data Transfer Object) - Đối Tượng Truyền Tải Dữ Liệu

**Rating:** 🟢

**Giải thích:**
DTO là class riêng dùng để nhận/trả dữ liệu từ HTTP request/response, không phải Entity database. Tách biệt Entity khỏi API giúp tăng bảo mật, linh hoạt, và tránh lỗi.

**Ví dụ:**

```java
// DTO nhận từ client
@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;
}

// Entity database (không return trực tiếp)
@Entity
public class User {
    private String passwordHash;  // Nhạy cảm, không expose
    private LocalDateTime createdAt;
    private Boolean emailVerified;
}

// DTO trả về client
@Data
public class RegisterResponse {
    private Long id;
    private String fullName;
    private String email;
    private List<String> roles;  // Chỉ role name, không toàn bộ Role entity
}
```

**Ưu điểm:**

- **Bảo mật**: Không expose passwordHash, createdAt, hay internal fields
- **Flexibility**: Frontend schema khác DB schema, DTO là bridge
- **Tránh Infinite Recursion**: User ↔ Role ManyToMany, nếu trả Entity → JSON serializer bị vòng lặp
- **API Versioning**: Tạo RegisterRequestV2, RegisterResponseV2 mà không thay Entity
- **Validation**: DTO chứa annotation validate, Entity không cần

**Misconception:**

- ❌ "Dùng Entity trực tiếp làm DTO cũng được" → Sai, rủi ro bảo mật & vòng lặp JSON
- ❌ "DTO chỉ dùng khi phức tạp" → Sai, nên dùng từ đầu dù đơn giản

**Câu hỏi phỏng vấn:**

> "Nếu Entity có 30 field, nhưng API chỉ cần 5 field, cách nào tốt nhất?"

**Câu trả lời:**

> "Tạo DTO chỉ với 5 field cần thiết. Không trả toàn bộ Entity. Lợi ích:
>
> - API response nhẹ hơn
> - Frontend biết rõ cấu trúc
> - DB schema thay đổi không ảnh hưởng API
> - Bảo mật: không expose internal fields"

---

### Bean Validation - Validate Dữ Liệu Tự Động

**Rating:** 🟢

**Giải thích:**
Chuẩn Java (JSR-303/JSR-380) để validate dữ liệu thông qua annotation. Spring Boot auto-enable Bean Validation.

**Ví dụ:**

```java
@Data
public class RegisterRequest {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150, message = "Họ tên ≤ 150 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, max = 100, message = "Mật khẩu 8-100 ký tự")
    private String password;

    @NotBlank
    private String confirmPassword;
}

@RestController
public class AuthController {
    @PostMapping("/register")
    // @Valid ← Trigger Bean Validation
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Nếu request sai format → MethodArgumentNotValidException
        // → GlobalExceptionHandler xử lý
        // → Không chạy đến đây
    }
}
```

**Common annotations:**

- `@NotNull` / `@NotBlank`: Field không được null/blank
- `@Size(min, max)`: String/Collection size
- `@Email`: Email format
- `@Min` / `@Max`: Number range
- `@Pattern`: Regex matching
- `@Valid`: Trigger validation cho nested objects

**Ưu điểm:**

- Không cần `if-else` checks trong Controller
- Declarative (nói LÀ CÁI GÌ, không nói LÀM GÌ)
- Reusable: Cùng DTO dùng ở nhiều Controller
- Lỗi tập trung xử lý trong GlobalExceptionHandler

**Misconception:**

- ❌ "Validate trong Controller trước @Valid" → Không cần, @Valid đã làm
- ❌ "Quên @Valid, validation không chạy" → Sai, validation chỉ chạy nếu có @Valid

**Câu hỏi phỏng vấn:**

> "Nếu quên `@Valid` trước `@RequestBody`, điều gì sẽ xảy ra?"

**Câu trả lời:**

> "Bean Validation không chạy. Request có thể chứa null/blank values, trực tiếp vào Service. Hậu quả:
>
> - NullPointerException khi access field
> - Invalid data vào database
> - Khó debug
>
> Best practice: **Luôn** thêm @Valid."

---

### @Transactional - Quản Lý Transaction

**Rating:** 🟢

**Giải thích:**
Annotation định nghĩa transaction boundary. Spring wrap method trong 1 transaction - hoặc tất cả commit, hoặc tất cả rollback.

**Ví dụ:**

```java
@Service
public class AuthServiceImpl {

    @Transactional  // ← Transaction wraps toàn bộ method
    public RegisterResponse register(RegisterRequest request) {
        // Step 1: Validate
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);  // ← Rollback ngay
        }

        // Step 2: Get role
        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // Step 3: Create user
        User user = new User(...);
        userRepository.save(user);  // ← INSERT

        // Step 4: Return response
        return toResponse(user);  // ← Nếu đến đây thành công → COMMIT
    }
}
```

**ACID guarantees:**

- **Atomicity**: All-or-nothing (tất cả commit hoặc tất cả rollback)
- **Consistency**: DB từ consistent state này sang consistent state khác
- **Isolation**: Transactions không ảnh hưởng lẫn nhau
- **Durability**: Commit thành công, data không bao giờ mất

**Khi nào commit/rollback:**

- **Commit**: Method kết thúc bình thường (return)
- **Rollback**: Exception ném ra (mặc định RuntimeException, hoặc custom exception)

**Ưu điểm:**

- Tự động rollback nếu có lỗi → không orphan data
- Consistent state, không partial update
- Dễ sử dụng: chỉ cần 1 annotation

**Misconception:**

- ❌ "Không cần @Transactional, database tự quản lý" → Sai, cần để wrap multiple operations
- ❌ "@Transactional bắt tất cả exception" → Sai, mặc định chỉ RuntimeException

**Câu hỏi phỏng vấn:**

> "Nếu `userRepository.save()` thành công, nhưng `roleRepository.findByName()` throw exception, điều gì xảy ra?"

**Câu trả lời:**

> "Với @Transactional: ROLLBACK toàn bộ, userRepository.save() được undo.
> Không @Transactional: User được lưu, exception ném ra → orphan data."

---

### ErrorCode Enum - Chuẩn Hóa Lỗi

**Rating:** 🟢

**Giải thích:**
Enum tập trung định nghĩa lỗi, bao gồm code (số), HTTP status, message. Thay vì throw Exception với string message tùy tiện.

**Ví dụ:**

```java
public enum ErrorCode {
    // 2xxx: Auth errors
    EMAIL_ALREADY_EXISTS(2001, HttpStatus.CONFLICT, "Email đã tồn tại"),
    PASSWORD_CONFIRM_NOT_MATCH(2010, HttpStatus.BAD_REQUEST, "Mật khẩu xác nhận không khớp"),

    // 3xxx: User/Role errors
    ROLE_NOT_FOUND(3002, HttpStatus.INTERNAL_SERVER_ERROR, "Không tìm thấy role"),
}

// Throw AppException với ErrorCode
if (userRepository.existsByEmail(email)) {
    throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);  // 409 Conflict
}

// API response
{
    "success": false,
    "code": 2001,
    "message": "Email đã tồn tại",
    "data": null
}
```

**Ưu điểm:**

- **Tập trung định nghĩa**: Tất cả error ở 1 nơi, dễ bảo trì
- **Chuẩn hóa**: Mỗi error có code, status, message duy nhất
- **Frontend parse**: Biết code 2001 = email conflict, code 2010 = password mismatch
- **I18n (Internationalization)**: Code không thay đổi, message dịch
- **IDE support**: Autocomplete, tránh typo

**Quy tắc đặt code:**

```
2xxx: Auth (2001, 2002, ..., 2010, 2020, ...)
3xxx: User/Role
4xxx: Course
5xxx: Payment
```

**Misconception:**

- ❌ "Throw Exception(\"Email already exists\")" → Sai, frontend khó parse
- ❌ "Mỗi file Exception riêng" → Sai, tập trung ErrorCode enum

**Câu hỏi phỏng vấn:**

> "Tại sao dùng ErrorCode enum thay vì throw Exception với message?"

**Câu trả lời:**

> "ErrorCode enum tập trung, chuẩn hóa, dễ maintain:
>
> - 1 nơi định nghĩa → dễ bảo trì
> - Frontend parse code (không parse string) → logic linh hoạt
> - Tránh typo bằng autocomplete
> - I18n: code cố định, message dịch"

---

### PasswordEncoder - Mã Hóa Mật Khẩu An Toàn

**Rating:** 🟢

**Giải thích:**
Spring Security interface để hash/verify password an toàn. BCryptPasswordEncoder là implementation được recommend.

**Ví dụ:**

```java
@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // ← Recommend implementation
    }
}

// Trong Service
@Service
public class AuthServiceImpl {
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest request) {
        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        // hashedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMye..."

        User user = new User();
        user.setPasswordHash(hashedPassword);  // Lưu hash, không lưu plain-text
        userRepository.save(user);
    }

    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}
```

**Hash vs Encryption:**

| Aspect          | Hash (BCrypt)            | Encryption                    |
| --------------- | ------------------------ | ----------------------------- |
| **Reversible?** | ❌ No                    | ✅ Yes                        |
| **Use for**     | Password, sensitive data | Credit card, encrypted fields |
| **Security**    | ✅ Nên dùng              | ❌ Không cho password         |
| **Speed**       | Chậm (intentional)       | Nhanh                         |

**Tại sao BCrypt an toàn hơn MD5:**

- **Speed**: BCrypt chậm (~1,000 guesses/s), MD5 nhanh (1 tỷ guesses/s)
- **Salt**: BCrypt tự động add salt, MD5 không
- **Rainbow table**: MD5 có pre-computed tables, BCrypt không

**Ưu điểm:**

- Auto-salt: Mỗi hash khác nhau mặc dù cùng password
- Slow-by-design: Chặn brute-force
- Industry standard: OWASP recommend

**Misconception:**

- ❌ "MD5 nhanh nên tốt hơn" → Sai, BCrypt chậm là feature
- ❌ "Hash password là encryption" → Sai, hash không thể reverse
- ❌ "Lưu plain-text password" → Sai, rủi ro bảo mật cao

**Câu hỏi phỏng vấn:**

> "Làm sao verify login nếu password không thể reverse-hash?"

**Câu trả lời:**

> "Dùng `passwordEncoder.matches(rawPassword, hashedPassword)`:
>
> 1. Take raw password từ login request
> 2. Hash nó cùng công thức
> 3. Compare với stored hash
> 4. Nếu match → password đúng"

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

# Nội dung cập nhật learning docs sau task Refresh Token API + Logout API

## Refresh Token API

### Giải thích ngắn gọn

Refresh Token API là API dùng để cấp access token mới khi access token cũ hết hạn. Client gửi refresh token lên backend, backend kiểm tra token hợp lệ rồi trả access token mới.

### Ví dụ trong project này

Frontend gọi:

```http id="k8nmqv"
POST /api/auth/refresh-token
```

với body:

```json id="pw3lwu"
{
  "refreshToken": "jwt-refresh-token"
}
```

Nếu token hợp lệ, backend trả về access token mới.

### Câu hỏi phỏng vấn liên quan

Vì sao cần Refresh Token API?

### Câu trả lời ngắn gọn

Vì access token nên sống ngắn để bảo mật, nên cần refresh token để lấy access token mới mà không bắt user đăng nhập lại liên tục.

## Logout API

### Giải thích ngắn gọn

Logout API dùng để kết thúc phiên đăng nhập. Trong hệ thống dùng JWT, logout thường revoke refresh token thay vì xóa access token ngay lập tức.

### Ví dụ trong project này

Khi user logout, backend tìm refresh token trong database và cập nhật:

```text id="nxorwz"
revoked = true
```

Sau đó refresh token này không thể dùng để lấy access token mới.

### Câu hỏi phỏng vấn liên quan

Logout với JWT khác gì session truyền thống?

### Câu trả lời ngắn gọn

Session truyền thống có thể xóa session trên server. JWT access token thường stateless nên không dễ xóa ngay, vì vậy hệ thống thường revoke refresh token để ngăn cấp token mới.

## Revoked Token

### Giải thích ngắn gọn

Revoked token là token đã bị thu hồi. Token này không còn được phép sử dụng dù có thể chưa hết hạn.

### Ví dụ trong project này

Sau khi gọi logout, refresh token được cập nhật `revoked = true`. Nếu gọi refresh-token bằng token này, backend trả lỗi `AUTH_008`.

### Câu hỏi phỏng vấn liên quan

Vì sao cần trạng thái revoked?

### Câu trả lời ngắn gọn

Vì token có thể vẫn chưa hết hạn nhưng user đã logout hoặc token cần bị vô hiệu hóa vì lý do bảo mật.

## Refresh Token Rotation

### Giải thích ngắn gọn

Refresh token rotation là kỹ thuật cấp refresh token mới mỗi lần user gọi refresh-token API, đồng thời revoke refresh token cũ.

### Ví dụ trong project này

Hiện tại MVP chưa cần rotation. Hệ thống chỉ cấp access token mới và giữ refresh token cũ cho đến khi hết hạn hoặc logout.

### Câu hỏi phỏng vấn liên quan

Refresh token rotation có lợi ích gì?

### Câu trả lời ngắn gọn

Nó giúp tăng bảo mật vì refresh token cũ sẽ bị vô hiệu hóa sau mỗi lần dùng, giảm nguy cơ token bị đánh cắp và tái sử dụng.

# Nội dung cập nhật learning docs sau task Access Token Authentication + GET /api/users/me

## JwtAuthenticationFilter

### Giải thích ngắn gọn

`JwtAuthenticationFilter` là filter chạy trước khi request đi vào controller. Nó đọc access token từ header `Authorization`, validate token, lấy thông tin user và đưa user vào Spring Security context.

### Ví dụ trong project này

Khi frontend gọi:

```http
GET /api/users/me
Authorization: Bearer <accessToken>
```

`JwtAuthenticationFilter` sẽ lấy token, kiểm tra token hợp lệ, load user từ database và set authentication vào `SecurityContextHolder`.

### Câu hỏi phỏng vấn liên quan

JwtAuthenticationFilter dùng để làm gì?

### Câu trả lời ngắn gọn

Nó dùng để đọc JWT từ request, validate token và xác thực user cho request hiện tại.

## SecurityContextHolder

### Giải thích ngắn gọn

`SecurityContextHolder` là nơi Spring Security lưu thông tin authentication của request hiện tại.

### Ví dụ trong project này

Sau khi token hợp lệ, filter set user vào `SecurityContextHolder`. Sau đó API `/api/users/me` có thể lấy user hiện tại từ context này.

### Câu hỏi phỏng vấn liên quan

SecurityContextHolder dùng để làm gì?

### Câu trả lời ngắn gọn

Nó lưu thông tin user đã xác thực trong request hiện tại để các tầng sau như Controller hoặc Service có thể biết ai đang gọi API.

## CustomUserDetails

### Giải thích ngắn gọn

`CustomUserDetails` là class đại diện cho user theo chuẩn Spring Security. Nó thường implement `UserDetails`.

### Ví dụ trong project này

User entity trong database có các field như id, email, passwordHash, roles. `CustomUserDetails` bọc các thông tin này để Spring Security hiểu được username, password và authorities.

### Câu hỏi phỏng vấn liên quan

Vì sao cần CustomUserDetails?

### Câu trả lời ngắn gọn

Vì Spring Security làm việc với interface `UserDetails`, nên ta cần chuyển User entity của project sang object phù hợp với Spring Security.

## CustomUserDetailsService

### Giải thích ngắn gọn

`CustomUserDetailsService` là service dùng để load user từ database theo email hoặc username.

### Ví dụ trong project này

Khi filter extract email từ JWT, service này tìm user trong database và trả về `CustomUserDetails`.

### Câu hỏi phỏng vấn liên quan

UserDetailsService dùng để làm gì?

### Câu trả lời ngắn gọn

Nó dùng để load thông tin user từ database cho Spring Security xác thực và phân quyền.

## AuthenticationEntryPoint

### Giải thích ngắn gọn

`AuthenticationEntryPoint` xử lý khi user chưa đăng nhập hoặc token không hợp lệ mà cố truy cập API protected.

### Ví dụ trong project này

Nếu gọi `/api/users/me` không có token hoặc token sai, `JwtAuthenticationEntryPoint` trả lỗi 401.

### Câu hỏi phỏng vấn liên quan

AuthenticationEntryPoint dùng khi nào?

### Câu trả lời ngắn gọn

Nó được gọi khi request chưa được xác thực nhưng cố truy cập tài nguyên cần authentication.

## SessionCreationPolicy.STATELESS

### Giải thích ngắn gọn

`STATELESS` nghĩa là backend không lưu session đăng nhập trên server. Mỗi request phải gửi token để tự chứng minh user là ai.

### Ví dụ trong project này

Frontend phải gửi access token trong header `Authorization` khi gọi API protected.

### Câu hỏi phỏng vấn liên quan

Vì sao JWT thường dùng stateless session?

### Câu trả lời ngắn gọn

Vì JWT chứa thông tin xác thực trong token, backend không cần lưu session server-side, phù hợp với REST API và dễ scale hơn.

## Bearer Token

### Giải thích ngắn gọn

Bearer token là cách gửi access token trong HTTP header.

### Ví dụ trong project này

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Câu hỏi phỏng vấn liên quan

Bearer token được gửi ở đâu?

### Câu trả lời ngắn gọn

Bearer token thường được gửi trong header `Authorization` của HTTP request.

# Nội dung cập nhật learning docs sau task Basic Role-Based Authorization + Security Rules

## Role-Based Authorization

### Giải thích ngắn gọn

Role-Based Authorization là cách kiểm soát quyền truy cập dựa trên vai trò của user, ví dụ `ADMIN`, `STUDENT`, `SUPER_ADMIN`.

### Ví dụ trong project này

* `ADMIN` và `SUPER_ADMIN` được vào `/api/admin/**`.
* `STUDENT` không được vào `/api/admin/**`.
* User đã đăng nhập được gọi `/api/users/me`.

### Câu hỏi phỏng vấn liên quan

Role-Based Authorization là gì?

### Câu trả lời ngắn gọn

Đó là cơ chế kiểm tra user có role phù hợp hay không trước khi cho phép truy cập một API hoặc chức năng.

## HTTP 401 Unauthorized

### Giải thích ngắn gọn

HTTP 401 xảy ra khi request chưa được xác thực hoặc token không hợp lệ.

### Ví dụ trong project này

Gọi `/api/users/me` mà không gửi access token sẽ nhận 401.

### Câu hỏi phỏng vấn liên quan

Khi nào trả 401?

### Câu trả lời ngắn gọn

Khi user chưa đăng nhập, không gửi token, token sai hoặc token hết hạn.

## HTTP 403 Forbidden

### Giải thích ngắn gọn

HTTP 403 xảy ra khi user đã đăng nhập nhưng không có đủ quyền truy cập tài nguyên.

### Ví dụ trong project này

User role `STUDENT` gọi `/api/admin/test` sẽ bị 403.

### Câu hỏi phỏng vấn liên quan

401 và 403 khác nhau thế nào?

### Câu trả lời ngắn gọn

401 là chưa xác thực, còn 403 là đã xác thực nhưng không đủ quyền.

## AccessDeniedHandler

### Giải thích ngắn gọn

`AccessDeniedHandler` xử lý lỗi khi user đã authenticated nhưng không có quyền truy cập API.

### Ví dụ trong project này

Khi STUDENT truy cập `/api/admin/**`, `CustomAccessDeniedHandler` trả response chuẩn với HTTP 403.

### Câu hỏi phỏng vấn liên quan

AccessDeniedHandler dùng để làm gì?

### Câu trả lời ngắn gọn

Nó dùng để xử lý lỗi 403 trong Spring Security.

## AuthenticationEntryPoint

### Giải thích ngắn gọn

`AuthenticationEntryPoint` xử lý lỗi khi user chưa authenticated hoặc token không hợp lệ.

### Ví dụ trong project này

Khi request không có access token mà gọi API protected, `JwtAuthenticationEntryPoint` trả HTTP 401.

### Câu hỏi phỏng vấn liên quan

AuthenticationEntryPoint khác AccessDeniedHandler thế nào?

### Câu trả lời ngắn gọn

AuthenticationEntryPoint xử lý 401, còn AccessDeniedHandler xử lý 403.

## @EnableMethodSecurity

### Giải thích ngắn gọn

`@EnableMethodSecurity` cho phép dùng annotation như `@PreAuthorize` để phân quyền trực tiếp ở method.

### Ví dụ trong project này

Sau này có thể dùng:

```java
@PreAuthorize("hasRole('ADMIN')")
public CourseResponse createCourse(...) {
    ...
}
```

### Câu hỏi phỏng vấn liên quan

@EnableMethodSecurity dùng để làm gì?

### Câu trả lời ngắn gọn

Nó bật cơ chế phân quyền ở cấp method trong Spring Security.

## hasRole và hasAuthority

### Giải thích ngắn gọn

`hasRole('ADMIN')` thường tự thêm prefix `ROLE_`, còn `hasAuthority('ROLE_ADMIN')` kiểm tra đúng authority truyền vào.

### Ví dụ trong project này

Nếu authority lưu trong Spring Security là `ROLE_ADMIN`, có thể dùng:

```java
hasRole("ADMIN")
```

hoặc:

```java
hasAuthority("ROLE_ADMIN")
```

### Câu hỏi phỏng vấn liên quan

hasRole và hasAuthority khác nhau thế nào?

### Câu trả lời ngắn gọn

`hasRole` thường tự thêm prefix `ROLE_`, còn `hasAuthority` kiểm tra chính xác authority.

## JPA Cascade & Orphan Removal

### Giải thích ngắn gọn
Là các cấu hình trong Spring Data JPA giúp tự động hóa việc đồng bộ dữ liệu giữa bảng cha và bảng con. Cascade giúp thao tác (Thêm/Sửa/Xóa) lan truyền từ cha xuống con. Orphan Removal dọn dẹp các dữ liệu rác không còn được tham chiếu.

### Ví dụ trong project này
Entity `Course` (Cha) có danh sách `CourseSection` (Con). Khi cấu hình `cascade = CascadeType.ALL, orphanRemoval = true`: Nếu Admin xóa 1 Course, tất cả Section của nó bị xóa. Nếu Admin xóa 1 phần tử Section ra khỏi `List<CourseSection>`, Section đó cũng tự bay khỏi database.

### Câu hỏi phỏng vấn liên quan
Phân biệt CascadeType.REMOVE và orphanRemoval = true?

### Câu trả lời ngắn gọn
`CascadeType.REMOVE` chỉ xóa con khi cha bị xóa. `orphanRemoval = true` bao gồm cả tính năng trên, và còn xóa luôn con nếu con bị gỡ khỏi danh sách của cha (bị cắt đứt quan hệ).

---

## Lombok Infinite Recursion (Lỗi đệ quy Lombok)

### Giải thích ngắn gọn
Lỗi xảy ra khi hai Object tham chiếu chéo nhau (Quan hệ 2 chiều) và cố gắng in nội dung của nhau ra (thông qua hàm `toString()` hoặc `equals()/hashCode()`), tạo thành vòng lặp vô hạn gây tràn bộ nhớ (StackOverflow).

### Ví dụ trong project này
`Lesson` mapping đến `Course`. `Course` lại chứa `List<Lesson>`. Khi in `Course`, Lombok gọi in `Lesson`, `Lesson` lại gọi in `Course`... Để tránh, ta dùng `@ToString.Exclude` ở thuộc tính `course` trong entity `Lesson`.

### Câu hỏi phỏng vấn liên quan
Tại sao lại bị StackOverflow khi dùng @Data của Lombok trong entity có quan hệ `@OneToMany`?

### Câu trả lời ngắn gọn
Vì `@Data` tự động generate `@ToString` và `@EqualsAndHashCode`. Hai entity cha con gọi qua lại các hàm này tạo thành vòng lặp vô hạn. Cần đổi sang dùng `@Getter`, `@Setter` hoặc dùng `@ToString.Exclude` để ngắt vòng lặp.

## Spring SecurityContextHolder

### Giải thích ngắn gọn
Là nơi lưu trữ trung tâm của Spring Security, chứa thông tin chi tiết về ngữ cảnh bảo mật hiện tại của ứng dụng, bao gồm cả thông tin về người dùng (Principal) đang tương tác với hệ thống trong luồng xử lý request hiện tại (ThreadLocal).

### Ví dụ trong project này
Khi Admin gửi một request kèm JWT Token hợp lệ đến endpoint `POST /api/v1/admin/courses`, tầng Filter sẽ xác thực token và lưu thông tin Admin đó vào `SecurityContextHolder`. Tại tầng Service, ta gọi `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` để lấy ra ID của Admin và gán vào trường `teacher` của khóa học một cách tự động.

### Câu hỏi phỏng vấn liên quan
Làm sao ứng dụng phân biệt được dữ liệu Security của các request chạy đồng thời?

### Câu trả lời ngắn gọn
Mặc định Spring Security sử dụng chiến lược lưu trữ `MODE_THREADLOCAL`. Nghĩa là mỗi request đi vào hệ thống sẽ được xử lý trên một Thread riêng biệt, và thông tin bảo mật trong `SecurityContextHolder` được gắn chặt vào Thread đó, đảm bảo dữ liệu cô lập tuyệt đối giữa các người dùng.

---

## JPA @EntityGraph

### Giải thích ngắn gọn
Là một tính năng của JPA (được Spring Data hỗ trợ qua annotation) giúp định nghĩa một giải pháp nạp dữ liệu một cách linh hoạt tại thời điểm chạy (runtime), chỉ định chính xác các thuộc tính liên kết nào cần được nạp ngay lập tức (`FETCH`) bằng cách sinh câu lệnh SQL `LEFT JOIN`.

### Ví dụ trong project này
Trong `CourseRepository`, ta khai báo:
```java
@EntityGraph(attributePaths = {"teacher"})
Page<Course> findAll(Pageable pageable);

Khi gọi hàm này, thay vì chạy 1 câu lệnh select lấy danh sách Course rồi lặp qua từng phần tử chạy tiếp N câu lệnh để lấy thông tin Giáo viên, Hibernate sẽ sinh ra duy nhất 1 lệnh SQL JOIN giữa bảng courses và users để kéo toàn bộ dữ liệu về cùng một lúc.
Câu hỏi phỏng vấn liên quan

Sự khác biệt giữa @EntityGraph và từ khóa FETCH JOIN trong JPQL là gì?
Câu trả lời ngắn gọn

Cả hai đều giải quyết lỗi N+1 Query thông qua SQL JOIN. Tuy nhiên, FETCH JOIN yêu cầu viết truy vấn tĩnh bằng chuỗi JPQL (@Query), còn @EntityGraph linh hoạt hơn, có thể khai báo đè trực tiếp lên các phương thức có sẵn của Spring Data JPA (như findAll, findById) mà không cần viết lại câu truy vấn.

## Business Logic Constraints (Ràng buộc Nghiệp vụ tầng Ứng dụng)

### Giải thích ngắn gọn
Là các quy tắc điều hướng, kiểm tra tính hợp lệ của hành động dữ liệu được thiết lập hoàn toàn bằng mã nguồn tại tầng nghiệp vụ (Service Layer), thay vì dựa dẫm vào các ràng buộc cứng của Database (như Foreign Key, Check Constraint). Nó giúp hệ thống xử lý các kịch bản linh hoạt hơn và trả về thông báo lỗi thân thiện.

### Ví dụ trong project này
Quy định chặn hành vi xóa một Chương học nếu bên trong nó vẫn còn chứa bài học. Thay vì để Database ném ra một lỗi hệ thống khô khan về xung đột khóa ngoại `Foreign Key Constraint Violation (SQLState: 23000)`, tầng Service chủ động đếm số lượng bài học con, nếu lớn hơn 0 sẽ ném ngay một Custom Exception kèm mã lỗi định nghĩa trước là `SECTION_002`.

---

## Race Condition & State Validation (Xung đột trạng thái do đồng thời)

### Giải thích ngắn gọn
Là tình trạng xảy ra khi nhiều luồng xử lý (Threads/Requests) cùng truy cập, kiểm tra dữ liệu trạng thái và cùng cố gắng ghi đè lên tài nguyên đó tại một thời điểm, dẫn đến kết quả dữ liệu cuối cùng không chính xác như dự kiến.

### Cách phòng tránh trong thiết kế
- Sử dụng các cơ chế Khóa (Locking): Optimistic Lock (Khóa lạc quan bằng trường `@Version`) hoặc Pessimistic Lock (Khóa bi quan khóa trực tiếp bản ghi DB).
- Chuyển đổi logic tính toán phụ thuộc trạng thái (như tự tăng số thứ tự, trừ số lượng tồn kho) về dạng câu lệnh cập nhật nguyên tử (Atomic Update) trong một Transaction duy nhất.

## Multi-level Data Isolation (Cô lập dữ liệu đa tầng)

### Giải thích ngắn gọn
Là giải pháp kiến trúc bảo mật cấp ứng dụng, áp dụng cho các hệ thống có cấu trúc dữ liệu phân cấp hình cây phức tạp. Quy trình bắt buộc hệ thống phải xác thực chuỗi quyền sở hữu từ thực thể lá (thực thể con nhỏ nhất) ngược lên thực thể gốc (Aggregate Root) để đảm bảo không một hành vi sửa đổi dữ liệu trái phép nào vượt qua được bộ lọc phân quyền.

### Ví dụ trong project này
Luồng kiểm tra an toàn dữ liệu khi cập nhật một Bài học (`Lesson`):
`Client gửi request PUT LessonID` -> `Hệ thống tìm Lesson` -> `Lấy ra Section tương ứng` -> `Lấy ra Course tương ứng` -> `Xác minh TeacherID của Course trùng với Token người dùng`.

## MultipleBagFetchException

### Giải thích ngắn gọn
Là lỗi do Hibernate ném ra khi bạn cố gắng truy vấn (Eager Fetch/Fetch Join) hai hoặc nhiều Collection có kiểu `List` (được Hibernate hiểu là Bag) của một Entity trong cùng một câu lệnh truy vấn.

### Ví dụ trong project này
Thực thể `Course` có `List<CourseSection>`, và `CourseSection` có `List<Lesson>`. Khi dùng `@EntityGraph` yêu cầu nạp cả 2 List này cùng lúc, SQL sẽ sinh ra kết quả tích Đề-các (Cartesian Product) khổng lồ chứa dữ liệu trùng lặp. Hibernate bó tay trong việc parse mớ dữ liệu đó vào các `List`. Giải pháp là đổi kiểu khai báo sang `Set<CourseSection>` và `Set<Lesson>`.

### Câu hỏi phỏng vấn liên quan
Tại sao đổi từ List sang Set lại fix được MultipleBagFetchException?

### Câu trả lời ngắn gọn
Bởi vì cấu trúc dữ liệu `Set` có bản chất không cho phép chứa phần tử trùng lặp. Khi nhận được kết quả bảng chéo Cartesian Product từ database, Hibernate có thể dựa vào hàm `equals()` và `hashCode()` để tự động loại bỏ các dòng bị lặp lại một cách chính xác, điều mà kiểu `List` (Bag) không làm được.