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

## Stateless JWT Architecture (Kiến trúc Xác thực Phi trạng thái)

### 1. Bản chất vấn đề

Trong mô hình Session truyền thống, Server cấp cho Client một `session_id` (Cookie) và Server phải duy trì một bảng băm (Hash Table) trên RAM (hoặc Redis) để nhớ xem `session_id` này thuộc về ai. Khi hệ thống mở rộng (Scale Out) lên nhiều server chạy song song, việc đồng bộ RAM giữa các server trở thành nút thắt cổ chai (Bottleneck).
Kiến trúc **Stateless JWT (JSON Web Token)** giải quyết triệt để bài toán này. Server không cần nhớ ai đã đăng nhập. Mọi thông tin (Email, Role, Expiration Time) đều được đóng gói trực tiếp vào chính Token. Khi Request bay đến bất kỳ Server nào, Server đó chỉ cần dùng Secret Key để giải mã và xác thực tính vẹn toàn (Integrity) của Token mà không cần query Database.

### 2. Triển khai trong Dự án

Hệ thống sử dụng cơ chế Token Kép (Dual-Token Mechanism):

- **Access Token (Ngắn hạn - 15 phút):** Chỉ chứa các Claim cơ bản, bay đi bay về liên tục trong Header của mọi Request.
- **Refresh Token (Dài hạn - 7 ngày):** Lưu ngầm trong Database (`refresh_tokens` table) và Cookie/LocalStorage. Chỉ bay lên Server 1 lần duy nhất khi Access Token hết hạn để xin cấp mới. Nó cung cấp cơ chế **Revocation (Thu hồi)**: Admin có thể khóa tài khoản, ép `revoked = true`, lập tức Refresh Token vô tác dụng, kẻ gian không thể xin thêm Access Token mới.

---

## Spring Security Filter Chain (Chuỗi màng lọc Bảo mật)

### 1. Khái niệm

Spring Security không can thiệp trực tiếp vào Controller của bạn. Thay vào đó, nó giăng ra một bức tường gồm nhiều lớp lưới lọc (Filters) đứng trước DispatcherServlet. Mọi Request từ ngoài Internet đi vào đều phải đi qua hệ thống ống nước (Pipeline) này. Nếu một Filter phát hiện dấu hiệu xâm nhập hoặc thiếu quyền, nó sẽ đánh bật Request ra ngoài ngay lập tức (Ném Exception) trước khi Request kịp chạm vào Code nghiệp vụ (Controller/Service).

### 2. Tùy chỉnh Filter trong Dự án (JwtAuthenticationFilter)

Dự án chèn thêm `JwtAuthenticationFilter` vào trước `UsernamePasswordAuthenticationFilter` mặc định của Spring.

```java
// Logic cốt lõi của JwtAuthenticationFilter
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        if (jwtUtil.validateToken(token)) {
            String email = jwtUtil.extractEmail(token);
            // Query DB lấy Role mới nhất của User (Chống lộ lọt quyền cũ)
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            // Ép thẻ hành nghề (Authentication) vào tay Request hiện tại
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Lưu vào Context của luồng (ThreadLocal)
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
    filterChain.doFilter(request, response); // Chuyển cho Filter tiếp theo
}
```

---

## SecurityContextHolder & ThreadLocal

### 1. Bản chất vấn đề

Trong môi trường Web (Servlet), mỗi Request của User (Client) bay tới sẽ được Tomcat cấp phát một Luồng xử lý độc lập (Thread). Hàng ngàn Request bay tới cùng lúc là hàng ngàn Threads chạy song song.
Câu hỏi: Làm sao các class ở tầng sâu (Service, Repository) có thể biết được Thread hiện tại đang chạy đại diện cho User nào, mà không cần phải truyền biến `User` lằng nhằng qua từng tham số hàm (`method(User u, String param)`)?

### 2. ThreadLocal và SecurityContext

Spring Security giải bài toán này bằng **ThreadLocal** - Một kho lưu trữ bộ nhớ đặc biệt, nơi dữ liệu chỉ có thể được nhìn thấy và truy cập bởi chính Thread đã tạo ra nó.
Khi `JwtAuthenticationFilter` (chạy trên Thread A) xác thực Token thành công, nó cất thẻ định danh vào két sắt của Thread A thông qua: `SecurityContextHolder.getContext().setAuthentication(auth)`.
Khi code chạy sâu xuống `LearningServiceImpl` (vẫn đang ở Thread A), ta chỉ cần gõ:

```java
String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
```

Spring tự động mở két sắt của Thread A, lấy ra email. Thread B kế bên gọi dòng code y hệt sẽ lấy ra két sắt của Thread B, dữ liệu hoàn toàn cô lập (Thread-Safe).

---

## Bắt Lỗi Bảo Mật Khéo Léo (Security Exception Handling)

### 1. Sự khác biệt giữa 401 và 403

- **401 Unauthorized (Chưa định danh):** Xảy ra ở vòng gửi xe. Hệ thống từ chối vì bạn không có Thẻ ra vào (Token), hoặc Thẻ đã hết hạn, hoặc Thẻ giả.
- **403 Forbidden (Sai thẩm quyền):** Xảy ra khi đã qua vòng gửi xe. Hệ thống nhận diện đúng bạn là học sinh (Student), nhưng bạn lại cố tình đá cửa xông vào phòng họp của Hội đồng Quản trị (Admin API). Hệ thống chặn lại vì bạn không đủ thẩm quyền (Authority).

### 2. Triển khai trong Dự án

Vì Spring Security Filter chạy _trước_ Controller, nếu xảy ra lỗi 401/403 ở đây, lỗi này sẽ không bị tóm bởi `@RestControllerAdvice` (GlobalExceptionHandler) như các lỗi Logic thông thường. Nếu để yên, Spring sẽ trả về một file HTML báo lỗi rất xấu xí.
Dự án đã giải quyết bằng cách định nghĩa 2 điểm đánh chặn (Interceptors):

- **JwtAuthenticationEntryPoint:** Chặn và ép kiểu lỗi 401 về định dạng JSON `ApiResponse`.
- **CustomAccessDeniedHandler:** Chặn và ép kiểu lỗi 403 về định dạng JSON `ApiResponse`.

```java
// Bơm vào cấu hình SecurityConfig
http.exceptionHandling(ex -> ex
    .authenticationEntryPoint(jwtAuthenticationEntryPoint) // Bắt lỗi 401 (Chưa đăng nhập)
    .accessDeniedHandler(customAccessDeniedHandler)        // Bắt lỗi 403 (Sai phân quyền)
);
```

---

## Method Security (@PreAuthorize) vs HttpSecurity Config

### 1. Khái niệm

Phân quyền là việc lập rào chắn bảo vệ API. Spring Security cung cấp 2 cách để đặt rào:

- **HttpSecurity (Bảo vệ theo URL Path):** Đặt rào ngay từ cổng Filter. `requestMatchers("/api/admin/**").hasRole("ADMIN")`. Ưu điểm: Tập trung, dễ nhìn, hiệu suất cao (Request bị đá văng từ ngoài ngõ).
- **Method Security (Bảo vệ theo Hành vi):** Đặt rào ngay trên đỉnh của Method bằng `@PreAuthorize`. Ưu điểm: Phân quyền cực mịn, có thể dùng biểu thức SpEL (Sping Expression Language) để kiểm tra logic phức tạp.

### 2. Ứng dụng nâng cao bằng SpEL

Trong khi HttpSecurity chỉ chặn cứng đường dẫn, `@PreAuthorize` cho phép đọc ngược thông số từ tham số truyền vào hàm (Method Arguments) để quyết định cho qua hay không.

```java
// Ví dụ: Chỉ cho phép người dùng xem thông tin Đơn hàng CỦA CHÍNH HỌ
@PreAuthorize("hasRole('ADMIN') or #orderUserId == authentication.principal.id")
@GetMapping("/orders/{orderUserId}")
public OrderDTO getOrderDetails(@PathVariable Long orderUserId) {
    ...
}
```

Nhờ SpEL, rào chắn này trở nên "thông minh": Admin thì qua tự do, nhưng User thường thì chỉ qua được nếu `orderUserId` trên URL trùng khớp với `id` của Token đang nắm giữ (Tuyệt chiêu chống IDOR ngay tại Controller).

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

---

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
```

Khi gọi hàm này, thay vì chạy 1 câu lệnh select lấy danh sách Course rồi lặp qua từng phần tử chạy tiếp N câu lệnh để lấy thông tin Giáo viên, Hibernate sẽ sinh ra duy nhất 1 lệnh SQL JOIN giữa bảng courses và users để kéo toàn bộ dữ liệu về cùng một lúc.

### Câu hỏi phỏng vấn liên quan

Sự khác biệt giữa @EntityGraph và từ khóa FETCH JOIN trong JPQL là gì?

### Câu trả lời ngắn gọn

Cả hai đều giải quyết lỗi N+1 Query thông qua SQL JOIN. Tuy nhiên, FETCH JOIN yêu cầu viết truy vấn tĩnh bằng chuỗi JPQL (@Query), còn @EntityGraph linh hoạt hơn, có thể khai báo đè trực tiếp lên các phương thức có sẵn của Spring Data JPA (như findAll, findById) mà không cần viết lại câu truy vấn.

---

## Multi-level Data Isolation (Cô lập dữ liệu đa cấp)

### 1. Định nghĩa và Bản chất

Data Isolation trong ứng dụng đa khách hàng (multi-tenant) hoặc đa người dùng (như nền tảng giáo dục) là kỹ thuật phân chia ranh giới vật lý hoặc logic, đảm bảo Dữ liệu của thực thể A không thể bị truy xuất hoặc chỉnh sửa bởi thực thể B nếu không có quyền.
Trong kiến trúc phần mềm, điều này bảo vệ hệ thống khỏi lỗ hổng IDOR (Insecure Direct Object References).

### 2. Triển khai trong Dự án (Code Thực Tế)

Trong `LessonAdminServiceImpl`, hệ thống không bao giờ tin tưởng `id` truyền từ Frontend. Khi nhận một request sửa/xóa bài học, thay vì chỉ tìm `Lesson` và xóa, nó thực thi chuỗi truy ngược phân cấp (Upward Traversal):

```java
// Trong LessonAdminServiceImpl
private void checkDataIsolation(Course course) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String currentUserEmail = auth.getName();

    // Kiểm tra role: Nếu là ADMIN/SUPER_ADMIN thì được bypass
    boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

    if (!isAdminOrSuperAdmin) {
        // Lấy Teacher sở hữu khóa học và đối chiếu email với Token hiện tại
        if (!course.getTeacher().getEmail().equals(currentUserEmail)) {
            throw new AppException(ErrorCode.DATA_ISOLATION_FORBIDDEN); // Bắn lỗi 403
        }
    }
}
```

**Luồng đi:** `Lesson` -> `.getSection()` -> `.getCourse()` -> `.getTeacher()` -> `.getEmail()` === `currentUserEmail`. Bất cứ mắt xích nào đứt gãy hoặc sai lệch, hành động sẽ bị chặn đứng (403 Forbidden).

---

## MultipleBagFetchException (Lỗi Nạp Nhiều Túi Dữ Liệu)

### 1. Bản chất vấn đề

Đây là cơn ác mộng thường gặp nhất khi tối ưu hóa hiệu suất truy vấn bằng Hibernate. Khi bạn thiết kế các quan hệ `@OneToMany` lồng nhau (VD: 1 Course có nhiều Sections, 1 Section có nhiều Lessons) và sử dụng kiểu `java.util.List`.
Khái niệm `Bag` trong Hibernate ám chỉ một tập hợp (Collection) không có trật tự và cho phép phần tử trùng lặp (giống y hệt `List` của Java).
Nếu dùng `@EntityGraph` hoặc `FETCH JOIN` để kéo cả Course, Section, và Lesson trong cùng 1 câu lệnh SQL, DB sẽ sinh ra Tích Đề-các (Cartesian Product). Hibernate nhận về một bảng kết quả khổng lồ chứa hàng trăm dòng lặp lại của cùng 1 Course. Vì `Bag` cho phép trùng lặp, Hibernate sợ rằng nếu nó tự ý lọc đi các phần tử trùng lặp, nó sẽ làm mất dữ liệu của lập trình viên, nên nó chọn cách ném Exception: `MultipleBagFetchException`.

### 2. Cách giải quyết trong Dự án

Sử dụng `java.util.Set` (cụ thể là `LinkedHashSet`) để thay thế cho `List`.

```java
// Trong entity Course
@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<CourseSection> sections = new LinkedHashSet<>(); // Set giải quyết Cartesian Product

// Trong entity CourseSection
@OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<Lesson> lessons = new LinkedHashSet<>();
```

- **Tại sao lại là Set?** Theo toán học, Set KHÔNG cho phép phần tử trùng lặp. Hibernate dựa vào điều này để dùng hàm `.equals()` lọc bỏ hoàn toàn các dòng lặp dư thừa sinh ra từ SQL JOIN, ánh xạ hoàn hảo thành một Object Tree sạch sẽ trên RAM.
- **Tại sao lại là LinkedHashSet?** Nếu dùng `HashSet` thường, thứ tự Section/Lesson sẽ lộn xộn. `LinkedHashSet` bảo tồn thứ tự chèn dữ liệu, kết hợp với trường `sortOrder` giúp API trả về danh sách bài học có thứ tự chính xác.

---

## Fail-Fast Principle (Nguyên tắc Thất bại nhanh)

### 1. Khái niệm

Fail-Fast là thiết kế hàm/thuật toán báo lỗi và ngắt luồng (throw Exception) ngay từ những dòng code đầu tiên khi phát hiện một điều kiện đầu vào không hợp lệ. Điều này ngăn chặn việc hệ thống tiếp tục chạy những đoạn code tiêu tốn tài nguyên (CPU, RAM, Network, Database) một cách vô ích.

### 2. Ứng dụng trong Ghi Danh Khóa Học (Course Enrollment)

Thay vì load hết thông tin, query kiểm tra đủ kiểu rồi mới gộp lại xử lý, hàm `enrollFreeCourse` từ chối phục vụ từng bước một (từ rẻ tới đắt):

```java
// 1. Kiểm tra RAM (Rẻ) - Ném lỗi ngay nếu khóa học chưa xuất bản
if (course.getStatus() != CourseStatus.PUBLISHED) {
    throw new AppException(ErrorCode.COURSE_NOT_AVAILABLE_FOR_ENROLLMENT);
}

// 2. Kiểm tra RAM (Rẻ) - Ném lỗi ngay nếu là khóa học trả phí
if (course.getCourseType() != CourseType.FREE) {
    throw new AppException(ErrorCode.COURSE_CANNOT_ENROLL_PAID);
}

// 3. Gọi Database (Đắt) - Ném lỗi nếu đã tồn tại bản ghi Ghi danh
if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
    throw new AppException(ErrorCode.USER_ALREADY_ENROLLED);
}
```

---

## Check-Then-Act & Race Condition (Xung đột trạng thái đa luồng)

### 1. Bản chất rủi ro

Trong các hệ thống phân tán, nếu nhiều request (vd: người dùng bấm liên tục nút Ghi danh 10 lần) chạy song song qua đoạn code `Check-Then-Act` (Kiểm tra xem chưa có -> Mới thêm vào), tất cả 10 luồng đều vượt qua vòng kiểm tra (vì tại tích tắc đó DB chưa kịp lưu). Hậu quả: 10 bản ghi Ghi danh được sinh ra cho cùng 1 user và 1 course, gây rác DB và lỗi logic sau này.

### 2. Giải pháp kiên cố ở tầng Database

Không bao giờ giao phó toàn bộ niềm tin cho code Java (Application Layer). Trách nhiệm giữ gìn sự toàn vẹn dữ liệu (Data Integrity) phải được ủy thác xuống mức thấp nhất: Database Layer thông qua Unique Constraint.

```java
// Entity CourseEnrollment
@Table(name = "course_enrollments", uniqueConstraints = {
    // Composite Unique Key chặn đứng Race Condition
    @UniqueConstraint(columnNames = {"user_id", "course_id"})
})
public class CourseEnrollment { ... }
```

Nhờ constraint này, DB Engine sẽ khóa (lock) các thao tác insert trùng lặp. Cho dù 10 luồng Java cùng gọi `.save(enrollment)`, chỉ 1 luồng thành công, 9 luồng còn lại sẽ bị ném `DataIntegrityViolationException` (Mã HTTP 500 hoặc 409).

---

## Upsert Pattern (Cập nhật hoặc Thêm mới)

### 1. Định nghĩa Upsert

Upsert = Update + Insert. Là mô hình xử lý một cục dữ liệu được gửi tới: Hệ thống tự đánh giá xem cần Ghi đè (Update) lên bản ghi cũ hay Tạo mới (Insert) bản ghi đầu tiên, giúp Frontend không cần phải gọi 2 API riêng biệt (API POST để tạo, API PUT để sửa).

### 2. Code Pattern trong JPA (Sử dụng Optional)

Trong Spring Data JPA, `save()` tự động đóng vai trò Upsert. Tuy nhiên để làm mịn luồng logic, chúng ta kết hợp nó với `.orElse()` của Optional:

```java
// Trong LearningServiceImpl.java (updateProgress)
// Lấy ra bản ghi tiến độ CŨ, hoặc TẠO MỚI bản ghi RỖNG nếu chưa có
LessonProgress progress = progressRepository.findByUserIdAndLessonId(user.getId(), lessonId)
        .orElse(LessonProgress.builder() // <-- Nếu không tìm thấy, tạo Entity trên RAM
                .user(user)
                .lesson(lesson)
                .watchedPercent(0.0)
                .isCompleted(false)
                .build());

// Cập nhật giá trị mới lên Entity
progress.setWatchedPercent(newPercent);

// Lưu xuống DB. JPA tự phân giải: Entity cũ -> UPDATE, Entity mới -> INSERT
progressRepository.save(progress);
```

---

## Anti-Downgrade Algorithm (High-water mark / Bảo toàn đỉnh)

### 1. Ngữ cảnh

Một học viên đang xem video đến phút 10 (tương đương 50% tiến độ). Do chưa hiểu, họ kéo thanh timeline ngược lại phút thứ 2. Lúc này Frontend báo về API tiến độ hiện tại là `10%`.
Nếu Backend dùng toán tử gán `=`, tiến độ của học viên sẽ bị tụt dốc thê thảm từ 50% về 10%, gây ức chế trải nghiệm học tập và đánh dấu sai tiến trình.

### 2. Thuật toán xử lý

Thuật toán Anti-Downgrade chỉ chấp nhận việc ghi đè trạng thái nếu trạng thái mới mang giá trị TÍCH CỰC HƠN (lớn hơn) trạng thái đang có.

```java
// Code bảo vệ trong LearningServiceImpl
// Chỉ cập nhật WatchedPercent nếu giá trị mới LỚN HƠN giá trị đang lưu trong DB
if (req.getWatchedPercent() != null && req.getWatchedPercent() > progress.getWatchedPercent()) {
    progress.setWatchedPercent(req.getWatchedPercent());
}
```

Nhờ lớp giáp logic này, dù học viên tua đi xem lại hàng ngàn lần ở các mốc thời gian cũ, tiến độ cao nhất (High-water mark) luôn được đóng băng bảo vệ.

## IDOR (Insecure Direct Object Reference)

### Giải thích ngắn gọn

Là một lỗ hổng bảo mật kiểm soát truy cập (Access Control Vulnerability). Xảy ra khi ứng dụng cung cấp quyền truy cập trực tiếp vào các đối tượng (như database records, files) dựa trên dữ liệu đầu vào của người dùng cung cấp (thường là các ID) mà không có cơ chế xác thực quyền sở hữu hợp lệ.

### Ví dụ trong project này

Thay vì thiết kế API `GET /users/{id}/progress`, ta dùng `GET /users/me/progress`. Chữ `me` được phân giải an toàn ở tầng Server thông qua JWT Token thay vì tin tưởng vào dữ liệu Client gửi lên.

### Ví dụ code dễ nhớ

```java
// Rủi ro IDOR: user có thể thử đổi id trên URL để xem dữ liệu người khác
@GetMapping("/users/{userId}/progress")
public List<ProgressRes> getProgress(@PathVariable Long userId) {
    return progressService.getProgressByUserId(userId);
}

// An toàn hơn: backend lấy user hiện tại từ SecurityContext/JWT
@GetMapping("/users/me/progress")
public List<ProgressRes> getMyProgress(Authentication authentication) {
    String email = authentication.getName();
    return progressService.getProgressByEmail(email);
}
```

### Điểm cần nhớ khi phỏng vấn

IDOR không phải lỗi "không login", mà là lỗi "login rồi nhưng truy cập nhầm dữ liệu của người khác". Vì vậy chỉ kiểm tra user đã đăng nhập là chưa đủ. Backend phải kiểm tra quyền sở hữu object, ví dụ progress này có thuộc về user hiện tại không, enrollment này có thuộc về student hiện tại không, hoặc user hiện tại có role admin không.

### Misconception hay gặp

- Nghĩ rằng ẩn nút trên frontend là đủ bảo mật. Sai, vì attacker có thể gọi API trực tiếp bằng Postman hoặc DevTools.
- Nghĩ rằng dùng UUID thay vì id số là hết IDOR. UUID chỉ làm khó đoán hơn, nhưng backend vẫn phải kiểm tra quyền.
- Nghĩ endpoint admin và endpoint student có thể dùng chung nếu frontend tự lọc. Sai, dữ liệu nhạy cảm phải được lọc ở backend.

---

## Data Aggregation (Tổng hợp dữ liệu / BFF Pattern)

### Giải thích ngắn gọn

Là quá trình thu thập, xử lý và tóm tắt dữ liệu từ nhiều bảng hoặc nguồn khác nhau trên Server, sau đó đóng gói lại thành một cục dữ liệu (JSON) duy nhất và tối ưu nhất để trả về cho Frontend hiển thị. Tránh việc Client phải thực hiện nhiều lời gọi mạng lẻ tẻ.

### Ví dụ trong project này

Màn Admin Dashboard cần nhiều dữ liệu cùng lúc: tổng số user, tổng số khóa học, tổng số lesson, tổng enrollment, danh sách user mới và khóa học mới. Nếu frontend tự gọi 5-6 API riêng rồi ghép lại, component sẽ phình to và thời gian tải dễ bị kéo dài. Backend có thể gom các query này trong `AdminDashboardServiceImpl` rồi trả về một DTO như `AdminDashboardRes`.

```java
public AdminDashboardRes getDashboardStats() {
    long totalUsers = userRepository.count();
    long totalCourses = courseRepository.count();
    long totalLessons = lessonRepository.count();
    long totalEnrollments = enrollmentRepository.count();

    return AdminDashboardRes.builder()
            .totalUsers(totalUsers)
            .totalCourses(totalCourses)
            .totalLessons(totalLessons)
            .totalEnrollments(totalEnrollments)
            .recentUsers(getRecentUsers())
            .recentCourses(getRecentCourses())
            .build();
}
```

### Lợi ích/Khi nào dùng

- Dùng cho các màn dashboard, profile summary, homepage, course detail, nơi UI cần nhiều mảnh dữ liệu để render một màn hình.
- Giảm round-trip network từ frontend tới backend.
- Giữ business rule tính toán ở backend, ví dụ "course published là gì", "progress tính thế nào".
- Giúp frontend đơn giản hơn vì chỉ nhận một response đã đúng shape để hiển thị.

### Lưu ý

Data Aggregation không có nghĩa là nhét mọi thứ vào một API khổng lồ. Nếu response quá nặng hoặc dữ liệu không phải lúc nào cũng cần, nên tách phần ít dùng thành API riêng hoặc lazy load.

---

## Frontend Foundation

### Giải thích ngắn gọn

Là quá trình dựng nền tảng frontend sao cho ứng dụng có thể chạy ổn định, route hoạt động đúng, state được tách rõ, và API client dễ mở rộng cho các màn hình sau này.

### Ví dụ trong project này

Trong dự án này, frontend foundation bao gồm việc tạo cấu trúc thư mục theo kiểu `src/pages`, `src/router`, `src/stores`, `src/services`, và dùng Axios để gọi `http://localhost:8080/api`.

### Câu hỏi phỏng vấn liên quan

- Tại sao cần tách router, store và service ra riêng?
- Axios interceptor có lợi ích gì?
- Pinia khác gì so với Vuex?

### Câu trả lời ngắn gọn

Router, store và service nên tách riêng để dễ bảo trì, dễ test và dễ mở rộng. Interceptor giúp gom logic token và lỗi API ở một chỗ. Pinia là cách quản lý state hiện đại và dễ dùng hơn trong Vue 3.

### Cấu trúc nên nhớ

```text
src/
  pages/       // Màn hình lớn theo route: LoginPage, AdminDashboardPage
  components/  // UI nhỏ có thể tái sử dụng: CourseCard, Modal, Button
  router/      // Khai báo route và navigation guards
  stores/      // Pinia store: auth, user, cart, learning
  services/    // Gọi API: auth.service.js, course.service.js
  utils/       // Hàm tiện ích thuần: formatDate, formatCurrency
```

### Điểm cần nhớ khi phỏng vấn

Frontend foundation tốt là nền để project lớn không bị rối. Page nên tập trung vào điều phối UI, service chịu trách nhiệm giao tiếp API, store giữ state dùng chung, router quyết định điều hướng. Khi chia đúng trách nhiệm, việc debug sẽ nhanh hơn: lỗi route thì tìm router, lỗi token thì tìm store/interceptor, lỗi endpoint thì tìm service.

## Axios Interceptors (Bộ đánh chặn Axios)

### Giải thích ngắn gọn
Là các hàm được chạy ngầm trước khi một request được gửi đi (Request Interceptor) hoặc trước khi hàm `.then()/.catch()` nhận được dữ liệu trả về (Response Interceptor).

### Ví dụ trong project này
- **Request Interceptor:** Trước khi Frontend gọi API lấy danh sách khóa học, hệ thống tự động móc Access Token từ Pinia và gắn vào header `Authorization: Bearer <token>`.
- **Response Interceptor:** Chặn lỗi 401 để tự động gọi API Refresh Token và retry request ban đầu.

### Ví dụ code dễ nhớ

```javascript
api.interceptors.request.use((config) => {
  const token = authStore.accessToken

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      authStore.logout()
      router.push('/login')
    }

    return Promise.reject(error)
  }
)
```

### Điểm cần nhớ khi phỏng vấn

Interceptor giúp gom logic lặp lại ở một chỗ. Nếu không có interceptor, mỗi service phải tự gắn token và tự xử lý lỗi 401, rất dễ thiếu sót. Tuy nhiên, interceptor cũng cần cẩn thận để tránh vòng lặp vô hạn khi refresh token fail hoặc khi chính request refresh token cũng bị 401.

---

## Vite Proxy

### Giải thích ngắn gọn
Là một tính năng của Vite Dev Server, hoạt động như một trạm trung chuyển (Reverse Proxy). Nó nhận request từ trình duyệt (ví dụ: `http://localhost:5173/api/users`), và chuyển tiếp nó đến Backend (ví dụ: `http://localhost:8080/api/users`). 

### Lợi ích
Giúp giải quyết triệt để lỗi CORS (Cross-Origin Resource Sharing) ở môi trường phát triển (Development) mà không cần cấu hình trên Backend.

### Ví dụ cấu hình

```javascript
// vite.config.js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

### Điểm cần nhớ khi phỏng vấn

Proxy của Vite chỉ chạy ở môi trường development. Khi deploy production, request sẽ đi qua domain thật, reverse proxy như Nginx, hoặc cấu hình CORS/backend tương ứng. Vì vậy không nên hiểu Vite proxy là giải pháp bảo mật; nó chủ yếu giúp quá trình dev frontend-backend mượt hơn.

## Frontend Authentication & Navigation Guards

### 1. Tóm tắt ngắn gọn
Xây dựng giao diện Đăng nhập/Đăng ký sử dụng Vue 3, thực hiện validate form ở phía Client, tích hợp RESTful API với Axios và bảo vệ các route nội bộ bằng Vue Router Guards.

### 2. Kiến thức phỏng vấn liên quan
Client-side Validation vs Server-side Validation, Vue Router Navigation Guards, JWT Storage (LocalStorage vs HttpOnly Cookies), XSS (Cross-Site Scripting).

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Tại sao phải làm Validation ở Frontend (Client-side) trong khi Backend đã validate rất chặt chẽ rồi?
Trả lời: 
Làm validation ở Client-side chủ yếu để tối ưu hóa Trải nghiệm người dùng (UX) và tiết kiệm tài nguyên Server. Khi validate ở Frontend, người dùng nhận được phản hồi ngay lập tức (ví dụ: sai định dạng email, mật khẩu quá ngắn) mà không cần chờ dữ liệu gửi qua mạng. Việc này giúp giảm thiểu các request rác không hợp lệ (Bad Request) bắn lên Server, giúp hệ thống hoạt động hiệu quả hơn. Tuy nhiên, Client-side validation có thể bị bypass (vượt qua), nên Server-side validation vẫn là lớp bảo mật bắt buộc cuối cùng.

#### Câu 2: Navigation Guards trong Vue Router hoạt động như thế nào để bảo vệ ứng dụng?
Trả lời:
Navigation Guards giống như các trạm kiểm soát (checkpoints) trước khi ứng dụng chuyển từ trang này sang trang khác. Em sử dụng `beforeEach` guard để kiểm tra xem một route có yêu cầu xác thực (`requiresAuth`) hay không. Nếu có, guard sẽ kiểm tra xem Token đã tồn tại trong Pinia Store (hoặc LocalStorage) chưa. Nếu chưa có Token, hệ thống sẽ chặn hành động điều hướng và dùng lệnh `router.push()` để đẩy người dùng về trang `/login`. Nó cũng giúp ngăn người dùng đã login truy cập lại vào trang Đăng nhập bằng cách đẩy thẳng họ vào Dashboard.

### Ví dụ code dễ nhớ

```javascript
router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      path: '/login',
      query: { redirect: to.fullPath }
    }
  }

  if (to.meta.role && !authStore.hasRole(to.meta.role)) {
    return '/403'
  }
})
```

### Điểm cần nhớ khi phỏng vấn

Navigation guard giúp bảo vệ trải nghiệm điều hướng trên frontend, nhưng không phải lớp bảo mật cuối cùng. Backend vẫn phải validate token, role và quyền sở hữu dữ liệu. Câu trả lời tốt khi phỏng vấn là: "Em dùng guard để user không vào nhầm màn hình, còn API thật vẫn được bảo vệ bằng Spring Security ở backend."

## Tối ưu thời gian tải trang với Promise.all & Quản lý trạng thái UI

### 1. Tóm tắt ngắn gọn
Tích hợp API thống kê và danh sách khóa học vào Student Dashboard, sử dụng `Promise.all` để fetch dữ liệu song song và quản lý chi tiết các trạng thái hiển thị (Loading, Empty, Data, Error).

### 2. Kiến thức phỏng vấn liên quan
Concurrent API Fetching, Skeleton Loading UI, Vue Component Lifecycle (`onMounted`), Empty States.

### 3. Câu hỏi phỏng vấn có thể gặp

#### Câu 1: Khi trang Dashboard cần gọi 2 API riêng biệt (lấy tiến độ học và lấy danh sách khóa học), bạn sẽ gọi chúng như thế nào để tối ưu hiệu năng?
Trả lời:
Em sử dụng `Promise.all()` để gọi cả 2 API song song thay vì gọi tuần tự (dùng `await` liên tiếp). Nếu API 1 mất 1s, API 2 mất 1.5s, việc gọi tuần tự sẽ tốn tổng cộng 2.5s. Với `Promise.all`, tổng thời gian chờ chỉ bằng thời gian của request lâu nhất là 1.5s. Điều này giúp tối ưu đáng kể tốc độ render lần đầu (First Paint) cho ứng dụng SPA.

#### Câu 2: Trải nghiệm người dùng (UX) sẽ bị ảnh hưởng thế nào nếu ta không xử lý trạng thái Loading và Empty State khi fetch data?
Trả lời:
Nếu không có Loading State, khi mạng chậm, màn hình sẽ trắng tinh hoặc hiển thị vỡ layout trong vài giây khiến người dùng tưởng web bị lỗi. Skeleton Loading hoặc Spinner giúp thông báo trực quan rằng hệ thống đang xử lý.
Nếu không có Empty State (khi mảng dữ liệu rỗng), UI sẽ hiển thị một khoảng trống khó hiểu. Việc có Empty State (ví dụ: "Bạn chưa có khóa học nào, hãy khám phá ngay") đóng vai trò điều hướng và giữ chân người dùng (Call-to-Action) rất hiệu quả.

### Ví dụ code dễ nhớ

```javascript
const isLoading = ref(false)
const errorMessage = ref('')

onMounted(async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const [statsRes, coursesRes] = await Promise.all([
      StudentService.getDashboardStats(),
      StudentService.getMyCourses()
    ])

    stats.value = statsRes.data.result
    courses.value = coursesRes.data.result
  } catch (error) {
    errorMessage.value = 'Không thể tải dữ liệu dashboard'
  } finally {
    isLoading.value = false
  }
})
```

### Lưu ý

`Promise.all()` phù hợp khi các request độc lập nhau. Nếu request thứ hai cần dữ liệu từ request thứ nhất, ví dụ cần `courseId` lấy từ API đầu tiên, thì phải gọi tuần tự. Ngoài ra, `Promise.all()` sẽ reject ngay nếu một promise fail; nếu muốn API nào thành công vẫn hiển thị phần đó, có thể dùng `Promise.allSettled()`.

## Role-Based Access Control ở Frontend

### Giải thích ngắn gọn

Role-Based Access Control (RBAC) ở frontend là cách kiểm tra role của user để quyết định họ được vào route nào hoặc nhìn thấy menu nào. Trong Vue, phần này thường được đặt trong router guard.

### Ví dụ trong project này

Route `/admin/dashboard` có `meta: { requiresAuth: true, role: 'ADMIN' }`. Guard đọc `authStore.user.roles` và chỉ cho user có `ADMIN` hoặc `SUPER_ADMIN` đi tiếp.

### Câu hỏi phỏng vấn liên quan

Frontend route guard có đủ để bảo mật API admin không?

### Câu trả lời ngắn gọn

Không đủ. Frontend chỉ chặn điều hướng ở trình duyệt. Backend vẫn phải kiểm tra quyền bằng Spring Security vì API có thể bị gọi trực tiếp.

### Ví dụ code dễ nhớ

```javascript
const routes = [
  {
    path: '/admin/users',
    component: AdminUserManagementPage,
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN'] }
  }
]

router.beforeEach((to) => {
  const authStore = useAuthStore()
  const requiredRoles = to.meta.roles

  if (requiredRoles && !requiredRoles.some(role => authStore.hasRole(role))) {
    return '/403'
  }
})
```

### Điểm cần nhớ khi phỏng vấn

RBAC ở frontend có 2 nhiệm vụ: điều hướng đúng và hiển thị UI đúng. Nó không thay thế RBAC ở backend. Một câu trả lời chắc là: "Frontend guard giúp user thường không thấy màn admin, nhưng backend vẫn là nơi quyết định request có được phép chạy không."

## Mock API Data

### Giải thích ngắn gọn

Mock API data là dữ liệu giả được dùng tạm khi API thật chưa sẵn sàng. Nó giúp frontend tiếp tục dựng UI, kiểm tra layout và xử lý state mà không phải chờ backend.

### Ví dụ trong project này

`admin.service.js` gọi `GET /api/v1/admin/dashboard`. Nếu backend trả 404 vì API chưa được triển khai, service trả mock data gồm `totalUsers`, `totalCourses`, `totalLessons`, `totalEnrollments`, `recentUsers` và `recentCourses`.

### Câu hỏi phỏng vấn liên quan

Vì sao nên đặt mock data ở service thay vì viết trực tiếp trong page component?

### Câu trả lời ngắn gọn

Vì service là nơi quản lý API call. Đặt mock ở service giúp page component không bị trộn logic dữ liệu, sau này thay mock bằng API thật cũng ít phải sửa UI.

### Ví dụ code dễ nhớ

```javascript
async function getDashboardStats() {
  try {
    return await api.get('/api/v1/admin/dashboard')
  } catch (error) {
    if (error.response?.status === 404) {
      return {
        data: {
          result: {
            totalUsers: 0,
            totalCourses: 0,
            totalLessons: 0,
            totalEnrollments: 0,
            recentUsers: [],
            recentCourses: []
          }
        }
      }
    }

    throw error
  }
}
```

### Lưu ý

Mock data chỉ nên là giải pháp tạm thời trong giai đoạn frontend đi trước backend. Khi API thật đã có, cần bỏ mock hoặc giới hạn mock theo môi trường development để tránh production che giấu lỗi backend thật.

## Frontend Service Layer

### Giải thích ngắn gọn

Frontend service layer là lớp file chuyên phụ trách gọi API, ví dụ `auth.service.js`, `student.service.js`, `admin.service.js`. Page/component sẽ gọi service thay vì dùng Axios trực tiếp ở nhiều nơi.

### Ví dụ trong project này

`AdminDashboardPage.vue` gọi `AdminService.getDashboardStats()` để lấy dữ liệu dashboard. Component không cần biết chi tiết endpoint, base URL hay cách xử lý 404 mock data.

### Câu hỏi phỏng vấn liên quan

Lợi ích của việc tách service layer trong frontend là gì?

### Câu trả lời ngắn gọn

Giúp code dễ đọc, dễ tái sử dụng, dễ đổi endpoint, dễ test và tránh việc logic gọi API bị lặp trong nhiều component.

### Ví dụ code dễ nhớ

```javascript
// services/admin.service.js
export const AdminService = {
  getUsers(params) {
    return api.get('/api/v1/admin/users', { params })
  },

  lockUser(id) {
    return api.put(`/api/v1/admin/users/${id}/lock`)
  }
}
```

### Điểm cần nhớ khi phỏng vấn

Component không nên biết quá nhiều về endpoint. Component chỉ cần gọi `AdminService.lockUser(id)`, còn URL, method, params và cách unwrap response nên được gom ở service. Cách này giúp khi backend đổi endpoint, ta sửa một nơi thay vì tìm trong nhiều component.

## Backend Dashboard Aggregation

### Giải thích ngắn gọn

Backend Dashboard Aggregation là cách backend gom nhiều chỉ số và danh sách dữ liệu từ nhiều bảng khác nhau, sau đó trả về một response duy nhất cho màn hình dashboard.

### Ví dụ trong project này

`AdminDashboardServiceImpl` lấy tổng user, course, lesson, enrollment và lấy thêm 5 user/khóa học mới nhất để trả về `AdminDashboardRes`.

### Câu hỏi phỏng vấn liên quan

Tại sao dashboard nên dùng một API tổng hợp thay vì frontend gọi nhiều API lẻ?

### Câu trả lời ngắn gọn

Một API tổng hợp giúp giảm số request, giảm logic ghép dữ liệu ở frontend và giữ cách tính số liệu tập trung ở backend.

### Ví dụ response DTO

```java
public record AdminDashboardRes(
        long totalUsers,
        long totalCourses,
        long totalLessons,
        long totalEnrollments,
        List<RecentUserRes> recentUsers,
        List<RecentCourseRes> recentCourses
) {}
```

### Lưu ý performance

Dashboard thường gọi nhiều query count. Với data nhỏ thì đơn giản, nhưng khi dữ liệu lớn cần cân nhắc index, cache, materialized view hoặc bảng thống kê riêng. Không nên join quá nhiều bảng nặng chỉ để render một dashboard nếu các số liệu có thể tính riêng hiệu quả hơn.

## Spring Data JPA Derived Query Method

### Giải thích ngắn gọn

Derived query method là cách đặt tên method trong Repository để Spring Data JPA tự sinh query dựa trên tên method.

### Ví dụ trong project này

`findTop5ByOrderByCreatedAtDesc()` giúp lấy 5 bản ghi mới nhất theo `createdAt` giảm dần mà không cần viết SQL thủ công.

### Câu hỏi phỏng vấn liên quan

Khi nào nên dùng derived query method?

### Câu trả lời ngắn gọn

Nên dùng khi query đơn giản, dễ đọc qua tên method. Nếu query phức tạp, nhiều điều kiện động hoặc cần tối ưu riêng thì nên dùng `@Query`, Specification hoặc QueryDSL.

### Ví dụ code dễ nhớ

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findTop5ByOrderByCreatedAtDesc();

    Page<User> findByStatus(UserStatus status, Pageable pageable);
}
```

### Misconception hay gặp

- Tên method càng dài càng tốt. Sai, method quá dài như `findByStatusAndRoleNameAndEmailContainingAndCreatedAtBetween...` rất khó đọc.
- Derived query luôn tối ưu. Sai, nó tiện nhưng vẫn cần xem SQL sinh ra khi query ảnh hưởng performance.
- Không cần `@Query`. Sai, `@Query` vẫn cần khi query có join, aggregate, điều kiện động hoặc tối ưu riêng.

## EntityGraph

### Giải thích ngắn gọn

`@EntityGraph` cho phép khai báo các quan hệ cần fetch kèm khi query Entity, giúp tránh việc load quan hệ quá muộn hoặc phát sinh nhiều query nhỏ.

### Ví dụ trong project này

`UserRepository.findTop5ByOrderByCreatedAtDesc()` dùng `@EntityGraph(attributePaths = {"roles"})` để lấy sẵn role của user khi map sang `RecentUserRes`.

### Câu hỏi phỏng vấn liên quan

`@EntityGraph` giúp tránh vấn đề gì trong JPA?

### Câu trả lời ngắn gọn

Nó giúp giảm rủi ro N+1 query và lỗi lazy loading khi service cần dùng dữ liệu từ quan hệ như `roles` hoặc `teacher`.

### Ví dụ code dễ nhớ

```java
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"roles"})
    List<User> findTop5ByOrderByCreatedAtDesc();
}
```

### Khi nào dùng

`@EntityGraph` hợp khi mặc định quan hệ là `LAZY`, nhưng ở một query cụ thể ta biết chắc cần fetch thêm quan hệ đó. Nó gọn hơn việc đổi entity sang `EAGER`, vì `EAGER` áp dụng mọi lúc và có thể làm query nặng ngoài ý muốn.

## Pagination

### Giải thích ngắn gọn

Pagination là kỹ thuật chia danh sách dữ liệu lớn thành nhiều trang nhỏ. Client gửi `page` và `size`, backend chỉ trả đúng phần dữ liệu cần hiển thị.

### Ví dụ trong project này

API `GET /api/v1/admin/users?page=0&size=10` trả 10 user đầu tiên cùng metadata như `currentPage`, `totalPages`, `totalElements`.

### Câu hỏi phỏng vấn liên quan

Vì sao danh sách user nên dùng pagination?

### Câu trả lời ngắn gọn

Vì số lượng user có thể tăng rất nhiều. Pagination giúp giảm tải database, giảm dung lượng response và giúp UI table chạy mượt hơn.

### Ví dụ code dễ nhớ

```java
@GetMapping("/admin/users")
public Page<UserRes> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return userService.getUsers(pageable);
}
```

### Điểm cần nhớ khi phỏng vấn

Spring Data dùng page index bắt đầu từ `0`, trong khi UI thường hiển thị từ trang `1`. Frontend cần map đúng: khi user bấm trang 1 thì gửi `page=0`. Nếu không thống nhất, UI rất dễ lệch một trang.

## Dynamic Filtering

### Giải thích ngắn gọn

Dynamic filtering là cách cho phép API lọc dữ liệu theo các tham số tùy chọn. Tham số nào không truyền thì bỏ qua điều kiện đó.

### Ví dụ trong project này

`UserRepository.findUsersByCriteria(keyword, status, role, pageable)` cho phép admin lọc user theo keyword, trạng thái và role. Nếu `status` là `null`, query không lọc theo status.

`CourseRepository.searchPublishedCourses(level, courseType, keyword, pageable)` cho phép public course list lọc theo level, loại khóa học và keyword. Query vẫn luôn giữ điều kiện `status = PUBLISHED`.

### Câu hỏi phỏng vấn liên quan

Vì sao cần filter động thay vì tạo nhiều endpoint riêng?

### Câu trả lời ngắn gọn

Filter động giúp API gọn hơn, frontend linh hoạt hơn và tránh phải tạo nhiều endpoint như `/users/by-role`, `/users/by-status`, `/users/search`.

### Ví dụ query dễ nhớ

```java
@Query("""
    SELECT u FROM User u
    WHERE (:keyword IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:status IS NULL OR u.status = :status)
      AND (:role IS NULL OR EXISTS (
          SELECT r FROM u.roles r WHERE r.name = :role
      ))
""")
Page<User> findUsersByCriteria(
        String keyword,
        UserStatus status,
        RoleName role,
        Pageable pageable
);
```

### Lưu ý

Filter động cần thống nhất cách hiểu giá trị rỗng. Frontend nên không gửi param hoặc gửi `null`/chuỗi rỗng theo contract đã định; backend nên parse rõ ràng để tránh lỗi enum, lỗi query hoặc filter sai.

## Enum Query Param Parsing

### Giải thích ngắn gọn

Enum Query Param Parsing là việc nhận query param dạng text từ URL rồi chuyển sang enum backend đang dùng. Nếu giá trị không hợp lệ, backend nên trả lỗi 400 rõ ràng.

### Ví dụ trong project này

`CoursePublicServiceImpl` nhận `level` và `courseType` dạng String, sau đó parse sang `CourseLevel` và `CourseType`. Nếu client gửi `level=INVALID`, service throw `AppException(ErrorCode.INVALID_REQUEST)`.

### Câu hỏi phỏng vấn liên quan

Vì sao không nên để lỗi enum sai trở thành lỗi 500?

### Câu trả lời ngắn gọn

Vì enum sai là lỗi input từ client, không phải lỗi hệ thống. Backend nên trả 400 để frontend biết cần sửa request hoặc hiển thị thông báo hợp lệ.

### Ví dụ code dễ nhớ

```java
private CourseLevel parseLevel(String level) {
    if (level == null || level.isBlank()) {
        return null;
    }

    try {
        return CourseLevel.valueOf(level.toUpperCase());
    } catch (IllegalArgumentException ex) {
        throw new AppException(ErrorCode.INVALID_REQUEST);
    }
}
```

### Điểm cần nhớ khi phỏng vấn

Enum từ URL là dữ liệu bên ngoài hệ thống, nên phải coi là input không tin cậy. Backend nên trả lỗi rõ ràng như `400 Bad Request`, thay vì để exception thô làm thành `500 Internal Server Error`. Lỗi 500 khiến người đọc hiểu nhầm là server hỏng, trong khi thực tế client gửi sai giá trị.

## Public Data Protection

### Giải thích ngắn gọn

Public Data Protection là nguyên tắc backend chỉ trả dữ liệu được phép hiển thị công khai, dù client có truyền query param như thế nào.

### Ví dụ trong project này

API public `GET /api/v1/courses` luôn lọc `CourseStatus.PUBLISHED`, nên các khóa học `DRAFT`, `HIDDEN`, `ARCHIVED` không bị lộ ra ngoài.

### Câu hỏi phỏng vấn liên quan

Vì sao không nên chỉ ẩn course chưa publish ở frontend?

### Câu trả lời ngắn gọn

Vì frontend có thể bị bypass. Điều kiện bảo vệ dữ liệu phải nằm ở backend query để mọi client đều chỉ nhận dữ liệu public hợp lệ.

### Ví dụ code dễ nhớ

```java
@Query("""
    SELECT c FROM Course c
    WHERE c.status = 'PUBLISHED'
      AND (:level IS NULL OR c.level = :level)
      AND (:courseType IS NULL OR c.courseType = :courseType)
""")
Page<Course> searchPublishedCourses(
        CourseLevel level,
        CourseType courseType,
        Pageable pageable
);
```

### Điểm cần nhớ khi phỏng vấn

Public API phải tự bảo vệ dữ liệu public. Dù frontend không hiển thị course `DRAFT`, attacker vẫn có thể gọi endpoint trực tiếp. Vì vậy điều kiện `status = PUBLISHED` nên nằm trong query/service backend, không phụ thuộc vào UI.

## Account Locking

### Giải thích ngắn gọn

Account locking là cơ chế khóa tài khoản bằng cách đổi trạng thái user sang `LOCKED`, thường dùng khi user vi phạm hoặc cần tạm ngưng truy cập.

### Ví dụ trong project này

API `PUT /api/v1/admin/users/{id}/lock` đổi `User.status` thành `LOCKED`. API `unlock` đổi lại thành `ACTIVE`.

### Câu hỏi phỏng vấn liên quan

Tại sao nên lock account bằng status thay vì xóa user?

### Câu trả lời ngắn gọn

Vì lock giữ lại dữ liệu lịch sử và quan hệ database. Xóa user có thể làm mất enrollment, progress hoặc gây lỗi dữ liệu liên quan.

### Ví dụ code dễ nhớ

```java
@Transactional
public UserRes lockUser(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    if (user.hasRole(RoleName.SUPER_ADMIN)) {
        throw new AppException(ErrorCode.CANNOT_LOCK_SUPER_ADMIN);
    }

    user.setStatus(UserStatus.LOCKED);
    return userMapper.toUserRes(user);
}
```

### Lưu ý bảo mật

Lock account nên đi kèm kiểm tra quyền: ai được lock, có được lock admin khác không, có được tự lock chính mình không. Ngoài ra backend login cũng phải kiểm tra `status`; nếu chỉ đổi status trong database nhưng login không kiểm tra thì tài khoản bị lock vẫn có thể đăng nhập.

## Admin Data Table

### Giải thích ngắn gọn

Admin Data Table là bảng dữ liệu dành cho màn quản trị, thường có search, filter, pagination, trạng thái loading/error/empty và các nút thao tác trên từng dòng.

### Ví dụ trong project này

`AdminUserManagementPage.vue` hiển thị bảng user với role, status, email verified, ngày tạo, đăng nhập cuối và nút khóa/mở khóa.

### Câu hỏi phỏng vấn liên quan

Một bảng admin tốt nên có những trạng thái UI nào?

### Câu trả lời ngắn gọn

Nên có loading, error, empty, data state và trạng thái đang xử lý action để user biết hệ thống đang làm gì.

### Các phần thường có trong admin table

- Search keyword để tìm nhanh theo email, tên khóa học hoặc title.
- Filter theo status, role, level hoặc course type.
- Pagination để không tải toàn bộ dữ liệu.
- Sort theo ngày tạo, tên hoặc trạng thái nếu nghiệp vụ cần.
- Action per row như lock/unlock, publish/hide, edit/delete.
- Loading, error, empty state và disabled state cho action đang chạy.

### Điểm cần nhớ khi phỏng vấn

Bảng admin là UI thao tác dữ liệu thật, nên cần rõ ràng hơn trang public. Khi action đang chạy, nên disable nút của dòng đó để tránh double click. Sau khi action thành công, UI nên phản ánh kết quả ngay; nếu thất bại, hiển thị lỗi rõ và không tự ý đổi state.

## Optimistic Row Update

### Giải thích ngắn gọn

Optimistic Row Update là cách cập nhật ngay một dòng trong UI sau khi action thành công, thay vì tải lại toàn bộ danh sách.

### Ví dụ trong project này

Sau khi gọi `AdminService.lockUser(user.id)` thành công, frontend tìm user trong `users` và đổi `status` của user đó thành kết quả backend trả về.

### Câu hỏi phỏng vấn liên quan

Khi nào nên cập nhật row thay vì reload cả table?

### Câu trả lời ngắn gọn

Nên dùng khi action chỉ thay đổi một phần nhỏ dữ liệu và backend đã trả kết quả mới đáng tin cậy. Cách này giúp UI phản hồi nhanh hơn.

### Ví dụ code dễ nhớ

```javascript
async function lockUser(user) {
  processingUserId.value = user.id

  try {
    const res = await AdminService.lockUser(user.id)
    const updatedUser = res.data.result
    const index = users.value.findIndex(item => item.id === user.id)

    if (index !== -1) {
      users.value[index] = updatedUser
    }
  } finally {
    processingUserId.value = null
  }
}
```

### Lưu ý

Optimistic Row Update ở đây thực chất là cập nhật UI sau khi API thành công. Nếu cập nhật trước khi API thành công thì gọi là optimistic UI đúng nghĩa, nhưng phải có rollback khi API fail. Với admin action quan trọng, cập nhật sau response thành công thường an toàn và dễ debug hơn.

## Role-Based UI Guard

### Giải thích ngắn gọn

Role-Based UI Guard là việc ẩn, hiện hoặc disable các nút/chức năng trên frontend dựa theo role của user hoặc role của đối tượng đang được thao tác.

### Ví dụ trong project này

Trang quản lý user không hiển thị nút khóa/mở khóa với tài khoản có role `SUPER_ADMIN`.

### Câu hỏi phỏng vấn liên quan

Role-Based UI Guard có thay thế được kiểm tra quyền ở backend không?

### Câu trả lời ngắn gọn

Không. UI guard chỉ giúp trải nghiệm rõ ràng hơn. Backend vẫn phải kiểm tra quyền thật vì API có thể bị gọi trực tiếp.

### Ví dụ code dễ nhớ

```vue
<button
  v-if="!user.roles.includes('SUPER_ADMIN')"
  :disabled="processingUserId === user.id"
  @click="lockUser(user)"
>
  Khóa
</button>
```

### Điểm cần nhớ khi phỏng vấn

Ẩn nút không phải bảo mật, nhưng vẫn rất cần cho UX. User thường không nên nhìn thấy action mà họ không được phép dùng. Tuy nhiên backend vẫn phải trả `403 Forbidden` nếu request không đủ quyền.

## State Transition

### Giải thích ngắn gọn

State Transition là việc chuyển một đối tượng từ trạng thái này sang trạng thái khác theo rule nghiệp vụ rõ ràng.

### Ví dụ trong project này

Course có thể chuyển từ `DRAFT` hoặc `HIDDEN` sang `PUBLISHED` khi admin gọi API publish, và chuyển sang `HIDDEN` khi admin gọi API hide.

### Câu hỏi phỏng vấn liên quan

Vì sao cần kiểm soát state transition ở backend?

### Câu trả lời ngắn gọn

Vì frontend có thể bị bypass. Backend phải là nơi quyết định trạng thái nào được chuyển, khi nào được chuyển và ai được phép chuyển.

### Ví dụ rule

```text
DRAFT  -> PUBLISHED  // được, nếu course có lesson
HIDDEN -> PUBLISHED  // được, nếu course có lesson
PUBLISHED -> HIDDEN  // được, để tạm ẩn khỏi public
ARCHIVED -> PUBLISHED // thường không nên, nếu archived nghĩa là đã đóng vĩnh viễn
```

### Ví dụ code dễ nhớ

```java
private void ensureCanPublish(Course course) {
    if (course.getStatus() == CourseStatus.ARCHIVED) {
        throw new AppException(ErrorCode.INVALID_COURSE_STATUS);
    }

    if (lessonRepository.countByCourseId(course.getId()) == 0) {
        throw new AppException(ErrorCode.COURSE_CANNOT_PUBLISH_EMPTY);
    }
}
```

### Lưu ý

State transition nên được đặt ở service/backend, không rải ở frontend. Khi rule thay đổi, ví dụ sau này chỉ teacher của course mới được publish, ta sửa ở một nơi và mọi client đều được bảo vệ.

## Publish Validation

### Giải thích ngắn gọn

Publish Validation là các điều kiện cần kiểm tra trước khi cho một nội dung xuất hiện public.

### Ví dụ trong project này

Trước khi publish course, backend kiểm tra khóa học có ít nhất một bài học. Nếu không có bài học, ném lỗi `COURSE_CANNOT_PUBLISH_EMPTY`.

### Câu hỏi phỏng vấn liên quan

Vì sao không nên cho publish khóa học rỗng?

### Câu trả lời ngắn gọn

Vì khóa học public phải có nội dung học tối thiểu. Publish khóa học rỗng làm hỏng trải nghiệm người dùng và giảm chất lượng dữ liệu public.

### Các điều kiện publish thường gặp

- Course phải có title, slug, description hợp lệ.
- Course phải có ít nhất một section hoặc lesson.
- Lesson public phải có nội dung học, video URL hoặc tài liệu cần thiết.
- Giá phải hợp lệ nếu là course trả phí.
- Teacher/owner phải tồn tại và đang active.

### Điểm cần nhớ khi phỏng vấn

Publish là ranh giới giữa dữ liệu nháp và dữ liệu public. Vì vậy validation khi publish thường chặt hơn validation khi save draft. Draft có thể thiếu nội dung để người tạo chỉnh dần, nhưng published content phải đủ chất lượng tối thiểu cho người học.

## Soft Visibility Control

### Giải thích ngắn gọn

Soft Visibility Control là cách ẩn/hiện dữ liệu bằng trạng thái thay vì xóa dữ liệu khỏi database.

### Ví dụ trong project này

API hide course đổi `Course.status` thành `HIDDEN`, nhờ vậy khóa học không hiện public nhưng dữ liệu course, lessons và enrollment vẫn được giữ.

### Câu hỏi phỏng vấn liên quan

Ẩn course khác gì xóa course?

### Câu trả lời ngắn gọn

Ẩn course chỉ thay đổi khả năng hiển thị. Xóa course có thể ảnh hưởng dữ liệu liên quan và khó khôi phục hơn.

### Ví dụ code dễ nhớ

```java
@Transactional
public CourseRes hideCourse(Long courseId) {
    Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));

    course.setStatus(CourseStatus.HIDDEN);
    return courseMapper.toCourseRes(course);
}
```

### Khi nào dùng

Soft visibility phù hợp khi dữ liệu vẫn có giá trị lịch sử hoặc có quan hệ với dữ liệu khác. Ví dụ course đã có enrollment thì xóa cứng có thể làm mất lịch sử học tập. Ẩn course giúp public list sạch hơn nhưng admin vẫn quản lý và khôi phục được.

## Spring Page Response Mapping

### Giải thích ngắn gọn

Spring Page Response Mapping là việc frontend đọc đúng cấu trúc phân trang mà Spring Data trả về, thường gồm `content`, `number`, `size`, `totalPages`, `totalElements`.

### Ví dụ trong project này

`AdminCourseManagementPage.vue` lấy danh sách khóa học từ `res.data.result.content` và lấy thông tin phân trang từ `res.data.result.number`, `totalPages`, `totalElements`.

### Câu hỏi phỏng vấn liên quan

Điểm khác nhau giữa `PageResponse` custom và Spring `Page` là gì?

### Câu trả lời ngắn gọn

`PageResponse` custom có field do mình tự định nghĩa như `data`, `currentPage`. Spring `Page` thường dùng `content`, `number`, `totalPages`, `totalElements`. Frontend phải map đúng cấu trúc backend trả.

### Ví dụ frontend mapping

```javascript
const result = res.data.result

courses.value = result.content
pagination.value = {
  currentPage: result.number,
  pageSize: result.size,
  totalPages: result.totalPages,
  totalElements: result.totalElements
}
```

### Lưu ý

Đây là API contract. Nếu backend trả Spring `Page` nhưng frontend lại đọc `result.data`, UI sẽ rỗng dù API thành công. Khi debug lỗi "API có data nhưng UI không hiện", nên kiểm tra response shape đầu tiên.

## Status Badge

### Giải thích ngắn gọn

Status Badge là nhãn UI ngắn, thường có màu, dùng để hiển thị trạng thái của một object trong bảng quản trị.

### Ví dụ trong project này

Course status `DRAFT`, `PUBLISHED`, `HIDDEN`, `ARCHIVED` được hiển thị bằng badge màu khác nhau trong `AdminCourseManagementPage.vue`.

### Câu hỏi phỏng vấn liên quan

Vì sao nên dùng badge cho trạng thái trong admin table?

### Câu trả lời ngắn gọn

Badge giúp người quản trị đọc bảng nhanh hơn, dễ phát hiện object nào đang published, hidden hoặc archived.

### Ví dụ mapping dễ nhớ

```javascript
const statusBadgeClass = {
  DRAFT: 'badge-gray',
  PUBLISHED: 'badge-green',
  HIDDEN: 'badge-yellow',
  ARCHIVED: 'badge-red'
}
```

### Lưu ý UI

Badge nên dùng màu nhất quán trên toàn app. Nếu `PUBLISHED` ở course list là màu xanh thì ở course detail/admin table cũng nên là xanh. Điều này giúp người dùng học được ngôn ngữ giao diện và scan bảng nhanh hơn.

## Feature Scoping

### Giải thích ngắn gọn

Feature Scoping là việc giới hạn phạm vi một task để task đủ nhỏ, dễ code, dễ test và ít rủi ro.

### Ví dụ trong project này

Task Course Management chỉ làm list, pagination, publish/hide/delete. Form create/update được để placeholder "Đang phát triển" và tách sang task riêng vì form khóa học có nhiều field.

### Câu hỏi phỏng vấn liên quan

Vì sao không nên nhồi quá nhiều chức năng vào một task frontend?

### Câu trả lời ngắn gọn

Vì task quá lớn sẽ khó review, khó test, dễ bug và dễ vượt phạm vi. Tách task giúp từng phần rõ ràng và ổn định hơn.

### Ví dụ cách scope hợp lý

```text
Task 1: Admin course list + pagination + publish/hide
Task 2: Course create/update form
Task 3: Course structure: sections + lessons
Task 4: Student course detail + enroll
Task 5: Learning page + progress tracking
```

### Điểm cần nhớ khi phỏng vấn

Feature scoping thể hiện tư duy làm việc thực tế. Không phải làm ít, mà là chia việc sao cho mỗi phần có thể code, review, test và rollback độc lập. Với dự án cá nhân, đây cũng là cách giúp mình không bị đuối khi feature ngày càng lớn.

## Reusable Form Modal

### Giải thích ngắn gọn

Reusable Form Modal là component form dạng popup có thể dùng lại cho nhiều chế độ, thường là tạo mới và cập nhật.

### Ví dụ trong project này

`CourseFormModal.vue` dùng `editingCourse` để biết đang create hay update. Nếu có `editingCourse`, form pre-fill dữ liệu cũ và gọi update API; nếu không có thì gọi create API.

### Câu hỏi phỏng vấn liên quan

Khi nào nên tách form thành component riêng?

### Câu trả lời ngắn gọn

Nên tách khi form có nhiều field, cần dùng lại cho create/update hoặc làm page component quá dài và khó đọc.

### Ví dụ logic create/update

```javascript
const isEditMode = computed(() => Boolean(props.editingCourse))

async function submitForm() {
  const payload = buildCoursePayload()

  if (isEditMode.value) {
    await AdminCourseService.updateCourse(props.editingCourse.id, payload)
  } else {
    await AdminCourseService.createCourse(payload)
  }

  emit('saved')
}
```

### Lưu ý

Reusable không có nghĩa là component phải xử lý mọi trường hợp. Nếu create và update khác nhau quá nhiều, tách thành hai component có thể dễ đọc hơn. Reusable tốt là khi hai mode chia sẻ phần lớn field, validation và layout.

## Props và Emits trong Vue

### Giải thích ngắn gọn

Props là dữ liệu component cha truyền xuống component con. Emits là sự kiện component con gửi ngược lên component cha.

### Ví dụ trong project này

`AdminCourseManagementPage.vue` truyền `editingCourse` vào `CourseFormModal.vue`. Khi lưu thành công, modal emit `saved` để page cha đóng modal và reload danh sách.

### Câu hỏi phỏng vấn liên quan

Vì sao không nên để component con tự reload page cha?

### Câu trả lời ngắn gọn

Vì component con nên độc lập và chỉ báo sự kiện. Component cha mới là nơi sở hữu danh sách course, nên cha quyết định reload hoặc cập nhật dữ liệu.

### Ví dụ code dễ nhớ

```vue
<!-- Parent -->
<CourseFormModal
  :editing-course="editingCourse"
  @saved="handleCourseSaved"
  @close="closeModal"
/>
```

```javascript
// Child
const props = defineProps({
  editingCourse: Object
})

const emit = defineEmits(['saved', 'close'])
```

### Điểm cần nhớ khi phỏng vấn

Props đi từ cha xuống con, emits đi từ con lên cha. Đây là one-way data flow của Vue. Component con không nên tự sửa trực tiếp prop vì prop thuộc quyền sở hữu của cha; nếu cần thay đổi, con emit event để cha quyết định.

## Frontend DTO Mapping

### Giải thích ngắn gọn

Frontend DTO Mapping là việc biến dữ liệu form thành payload đúng cấu trúc backend request DTO yêu cầu.

### Ví dụ trong project này

`CourseFormModal.vue` build payload cho create gồm title, slug, description, level, courseType và price. Khi update thì thêm `status` để khớp `CourseUpdateReq`.

### Câu hỏi phỏng vấn liên quan

Vì sao create payload và update payload không phải lúc nào cũng giống nhau?

### Câu trả lời ngắn gọn

Vì backend có thể đặt rule khác nhau cho create và update. Ví dụ create course mặc định status là `DRAFT`, còn update yêu cầu gửi `status`.

### Ví dụ code dễ nhớ

```javascript
function buildCreatePayload(form) {
  return {
    title: form.title.trim(),
    slug: form.slug.trim(),
    description: form.description.trim(),
    level: form.level,
    courseType: form.courseType,
    price: Number(form.price || 0)
  }
}

function buildUpdatePayload(form) {
  return {
    ...buildCreatePayload(form),
    status: form.status
  }
}
```

### Lưu ý

DTO mapping là nơi chuyển dữ liệu UI thành dữ liệu backend hiểu được. Không nên gửi nguyên object form nếu form có field chỉ phục vụ UI như `isSubmitting`, `previewImage`, `confirmDelete`, vì backend không cần và có thể reject request.

## Dynamic Route Params

### Giải thích ngắn gọn

Dynamic Route Params là tham số động trong URL, dùng để xác định dữ liệu cụ thể cần hiển thị.

### Ví dụ trong project này

Route `/admin/courses/:id/structure` dùng `:id` để biết admin đang quản lý cấu trúc của course nào.

### Câu hỏi phỏng vấn liên quan

Khi nào nên dùng dynamic route params?

### Câu trả lời ngắn gọn

Nên dùng khi một page cần hiển thị dữ liệu theo một object cụ thể như course id, lesson id hoặc user id.

### Ví dụ code dễ nhớ

```javascript
const route = useRoute()
const courseId = computed(() => route.params.id)

onMounted(() => {
  loadCourseStructure(courseId.value)
})
```

### Lưu ý

Dynamic param nên thể hiện đúng định danh mà backend cần. Nếu route dùng `:slug` nhưng API cần `id`, frontend sẽ phải có thêm bước chuyển slug sang id. Điều này không sai, nhưng phải được thiết kế rõ để tránh route đẹp ở frontend nhưng tích hợp API lại rối.

## Lazy Loading Child Data

### Giải thích ngắn gọn

Lazy loading child data là cách chỉ tải dữ liệu con khi người dùng thật sự cần xem, thay vì tải tất cả ngay khi mở page.

### Ví dụ trong project này

`AdminCourseStructurePage.vue` chỉ gọi API lấy lessons khi admin mở một section.

### Câu hỏi phỏng vấn liên quan

Lazy loading lessons có lợi ích gì?

### Câu trả lời ngắn gọn

Nó giảm số request ban đầu, giúp trang tải nhanh hơn và tránh tải dữ liệu không cần thiết nếu admin không mở section đó.

### Ví dụ code dễ nhớ

```javascript
async function toggleSection(section) {
  section.isExpanded = !section.isExpanded

  if (section.isExpanded && !section.lessonsLoaded) {
    section.isLoadingLessons = true

    try {
      const res = await AdminCourseService.getLessons(section.id)
      section.lessons = res.data.result
      section.lessonsLoaded = true
    } finally {
      section.isLoadingLessons = false
    }
  }
}
```

### Điểm cần nhớ khi phỏng vấn

Lazy loading giúp tối ưu initial load, nhưng cần cache trạng thái đã load để không gọi lại API vô ích mỗi lần mở/đóng. Nếu dữ liệu có thể thay đổi sau action create/update/delete lesson, cần invalidate hoặc reload lại section đó.

## Parent-Child UI State

### Giải thích ngắn gọn

Parent-Child UI State là cách quản lý trạng thái dữ liệu cha-con trong frontend, ví dụ course chứa sections, section chứa lessons.

### Ví dụ trong project này

Mỗi section trong `sections` có thêm state UI như `isExpanded`, `isLoadingLessons` và `lessons` để quản lý việc mở/đóng và tải bài học.

### Câu hỏi phỏng vấn liên quan

Vì sao cần thêm state UI như `isExpanded` vào dữ liệu section?

### Câu trả lời ngắn gọn

Vì backend chỉ trả dữ liệu nghiệp vụ, còn frontend cần thêm trạng thái hiển thị để biết section nào đang mở và section nào đang tải lessons.

### Ví dụ state mở rộng ở frontend

```javascript
sections.value = apiSections.map(section => ({
  ...section,
  isExpanded: false,
  isLoadingLessons: false,
  lessonsLoaded: false,
  lessons: []
}))
```

### Lưu ý

Không nên gửi các field UI-only như `isExpanded` hoặc `isLoadingLessons` ngược về backend. Đây là trạng thái hiển thị của trình duyệt, không phải dữ liệu nghiệp vụ. Khi build payload update section, cần chỉ lấy các field backend cần như `title`, `orderIndex`.

## API Response Mapping

### Giải thích ngắn gọn

API Response Mapping là việc frontend đọc đúng cấu trúc response từ backend rồi đưa dữ liệu vào state phù hợp để render UI.

### Ví dụ trong project này

`CourseListPage.vue` lấy danh sách khóa học từ `res.data.result.content`, lấy phân trang từ `res.data.result.number`, `totalPages` và `totalElements`.

### Câu hỏi phỏng vấn liên quan

Điều gì xảy ra nếu frontend map sai response backend?

### Câu trả lời ngắn gọn

UI có thể không hiển thị dữ liệu, pagination sai hoặc báo lỗi runtime. Vì vậy frontend phải bám sát API contract.

### Ví dụ lỗi thường gặp

```javascript
// Sai nếu backend trả Spring Page
courses.value = res.data.result.data

// Đúng nếu backend trả Spring Page
courses.value = res.data.result.content
```

### Cách debug nhanh

Khi UI không hiện data, bước đầu tiên nên `console.log(res.data)` hoặc xem tab Network để kiểm tra response thật. Đừng đoán field theo trí nhớ, vì mỗi backend có thể wrap response khác nhau: `data`, `result`, `content`, `items`, `records`.

## Frontend Filter State

### Giải thích ngắn gọn

Frontend Filter State là trạng thái người dùng chọn trên UI như keyword, level, course type. State này được chuyển thành query params khi gọi API.

### Ví dụ trong project này

`CourseListPage.vue` lưu `filters.keyword`, `filters.level`, `filters.courseType`; khi gọi API thì chỉ gửi param nào có giá trị.

### Câu hỏi phỏng vấn liên quan

Vì sao filter "Tất cả" nên gửi param rỗng hoặc không gửi param?

### Câu trả lời ngắn gọn

Vì backend hiểu param rỗng/null là bỏ qua filter. Nếu gửi một giá trị giả như `ALL_LEVELS` mà backend không hỗ trợ, request sẽ lỗi.

### Ví dụ code dễ nhớ

```javascript
function buildCourseParams() {
  const params = {
    page: currentPage.value,
    size: pageSize.value
  }

  if (filters.keyword) params.keyword = filters.keyword
  if (filters.level) params.level = filters.level
  if (filters.courseType) params.courseType = filters.courseType

  return params
}
```

### Lưu ý UX

Khi filter thay đổi, thường nên reset page về `0`. Nếu user đang ở trang 5 rồi đổi filter hẹp hơn, backend có thể không còn trang 5 và UI sẽ rỗng dù filter có kết quả ở trang đầu.

## UI Async States

### Giải thích ngắn gọn

UI Async States là các trạng thái giao diện khi làm việc với API bất đồng bộ: loading, success, error và empty.

### Ví dụ trong project này

`CourseListPage.vue` hiển thị spinner khi đang tải khóa học, message lỗi khi API fail và empty state khi API thành công nhưng không có course nào.

### Câu hỏi phỏng vấn liên quan

Vì sao cần phân biệt error state và empty state?

### Câu trả lời ngắn gọn

Error nghĩa là hệ thống chưa lấy được dữ liệu, còn empty nghĩa là lấy dữ liệu thành công nhưng không có kết quả. Hai tình huống cần thông báo và hành động khác nhau.

### Cách nghĩ đơn giản

```text
loading: đang chờ API
error: API fail hoặc không parse được dữ liệu
empty: API thành công nhưng danh sách rỗng
data: API thành công và có dữ liệu
```

### Điểm cần nhớ khi phỏng vấn

UI async state tốt giúp sản phẩm có cảm giác ổn định. Người dùng luôn biết hệ thống đang tải, đã lỗi, hay thật sự không có dữ liệu. Đây là khác biệt giữa "code chạy được" và "UI dùng được".

## Route Param Watcher

### Giải thích ngắn gọn

Route Param Watcher là việc theo dõi thay đổi của tham số trên URL để component gọi lại API hoặc cập nhật state khi tham số đổi.

### Ví dụ trong project này

`CourseDetailPage.vue` theo dõi `route.params.slug`. Nếu slug thay đổi, page gọi lại `CourseService.getCourseBySlug(slug)` để hiển thị đúng khóa học mới.

### Câu hỏi phỏng vấn liên quan

Vì sao `onMounted()` đôi khi chưa đủ khi dùng dynamic route?

### Câu trả lời ngắn gọn

Vì Vue Router có thể tái sử dụng cùng component khi chỉ param thay đổi. `watch` giúp component phản ứng với param mới mà không cần remount.

### Ví dụ code dễ nhớ

```javascript
watch(
  () => route.params.slug,
  (newSlug) => {
    if (newSlug) {
      loadCourseDetail(newSlug)
    }
  },
  { immediate: true }
)
```

### Lưu ý

`immediate: true` giúp watcher chạy ngay lần đầu component mount, nên có thể thay cho việc viết thêm `onMounted()`. Nếu API call phụ thuộc nhiều param, nên watch một object hoặc array param để tránh quên reload khi một param khác thay đổi.

## XSS Awareness

### Giải thích ngắn gọn

XSS Awareness là ý thức phòng tránh việc render HTML/script không tin cậy từ backend hoặc người dùng lên giao diện.

### Ví dụ trong project này

Course description được render bằng text thường với `white-space: pre-line` thay vì `v-html`, vì task này chưa cần rich text HTML.

### Câu hỏi phỏng vấn liên quan

Khi nào có thể dùng `v-html` trong Vue?

### Câu trả lời ngắn gọn

Chỉ nên dùng khi thật sự cần render HTML và dữ liệu đã được sanitize hoặc đến từ nguồn tin cậy. Nếu chỉ là text, interpolation an toàn hơn.

### Ví dụ dễ nhớ

```vue
<!-- An toàn hơn cho text thường -->
<p>{{ course.description }}</p>

<!-- Chỉ dùng khi HTML đã sanitize -->
<div v-html="sanitizedDescription"></div>
```

### Điểm cần nhớ khi phỏng vấn

XSS xảy ra khi app render nội dung không tin cậy như HTML/script và trình duyệt thực thi nó. Vue interpolation `{{ }}` tự escape text, nên an toàn hơn cho mô tả, comment, title. `v-html` bỏ qua cơ chế escape này, vì vậy phải sanitize trước.

## Placeholder CTA

### Giải thích ngắn gọn

Placeholder CTA là nút hành động được hiển thị để định hướng luồng sản phẩm nhưng chưa kích hoạt nghiệp vụ thật.

### Ví dụ trong project này

`CourseDetailPage.vue` hiển thị nút "Đăng ký học miễn phí" hoặc "Mua khóa học" nhưng để disabled vì task hiện tại chưa làm enrollment/payment UI.

### Câu hỏi phỏng vấn liên quan

Vì sao CTA chưa hoàn thiện nên disabled?

### Câu trả lời ngắn gọn

Vì người dùng không nên bấm vào một hành động chưa có xử lý thật. Disabled CTA giúp UI rõ phạm vi và tránh lỗi trải nghiệm.

### Lưu ý sản phẩm

Placeholder CTA hữu ích khi muốn giữ layout và định hướng flow tương lai, nhưng nên tránh tạo kỳ vọng sai. Nếu nút disabled, UI nên có style rõ là chưa thao tác được. Trong môi trường production, không nên để quá nhiều CTA giả vì người dùng sẽ mất niềm tin vào sản phẩm.

## Protected Action

### Giải thích ngắn gọn

Protected Action là hành động trên UI chỉ cho phép user đã đăng nhập hoặc có role phù hợp thực hiện.

### Ví dụ trong project này

Nút "Đăng ký học miễn phí" trên `CourseDetailPage.vue` yêu cầu user đăng nhập. Nếu chưa đăng nhập, frontend chuyển sang `/login` trước khi gọi API enroll.

### Câu hỏi phỏng vấn liên quan

Frontend check đăng nhập có thay thế backend security được không?

### Câu trả lời ngắn gọn

Không. Frontend check giúp UX tốt hơn, còn backend security mới là lớp bắt buộc để bảo vệ dữ liệu và nghiệp vụ.

### Ví dụ code dễ nhớ

```javascript
async function enrollFreeCourse(courseId) {
  if (!authStore.isAuthenticated) {
    router.push({
      path: '/login',
      query: { redirect: route.fullPath }
    })
    return
  }

  await CourseService.enrollFreeCourse(courseId)
}
```

### Lưu ý

Protected action khác với protected route. Có những page public ai cũng xem được, nhưng một hành động cụ thể trong page như enroll, favorite, comment, checkout lại yêu cầu đăng nhập. Vì vậy guard ở route chưa đủ; button handler cũng cần kiểm tra trạng thái auth để điều hướng UX đúng.

## Safe Redirect

### Giải thích ngắn gọn

Safe Redirect là cách chỉ cho phép điều hướng sau login tới đường dẫn nội bộ hợp lệ, tránh chuyển user sang URL lạ.

### Ví dụ trong project này

`LoginPage.vue` chỉ dùng `redirect` query nếu path bắt đầu bằng `/` và không bắt đầu bằng `//`. Nếu không hợp lệ, user được đưa về `/student/dashboard`.

### Câu hỏi phỏng vấn liên quan

Open redirect nguy hiểm ở điểm nào?

### Câu trả lời ngắn gọn

Kẻ xấu có thể lợi dụng redirect để đưa user sang website giả mạo sau khi đăng nhập. Vì vậy app chỉ nên redirect nội bộ.

### Ví dụ code dễ nhớ

```javascript
function getSafeRedirect(redirect) {
  if (!redirect) return '/student/dashboard'

  const isInternalPath = redirect.startsWith('/') && !redirect.startsWith('//')
  return isInternalPath ? redirect : '/student/dashboard'
}
```

### Misconception hay gặp

- Nghĩ redirect chỉ là UX nên không liên quan bảo mật. Sai, open redirect thường được dùng trong phishing.
- Chỉ check `startsWith('/')` là đủ. Chưa đủ, vì `//evil.com` cũng có thể được trình duyệt hiểu là external URL.
- Tin tuyệt đối vào query param `redirect`. Sai, query param là input từ client và phải validate.

## Idempotency Awareness

### Giải thích ngắn gọn

Idempotency Awareness là ý thức xử lý các hành động có thể bị gọi nhiều lần, ví dụ user bấm nút liên tục hoặc gửi lại request.

### Ví dụ trong project này

Frontend disable nút enroll khi `isEnrolling`, còn backend có unique constraint và xử lý `USER_ALREADY_ENROLLED` để chống ghi danh trùng.

### Câu hỏi phỏng vấn liên quan

Vì sao vừa cần disable nút ở frontend vừa cần chống trùng ở backend?

### Câu trả lời ngắn gọn

Disable nút chỉ giảm lỗi thao tác. Backend vẫn phải chống trùng vì request có thể được gửi từ tab khác, Postman hoặc do race condition.

### Ví dụ nhiều lớp bảo vệ

```javascript
// Frontend: giảm double click
if (isEnrolling.value) return
isEnrolling.value = true
```

```java
// Backend/database: bảo vệ dữ liệu thật
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "course_id"})
})
public class CourseEnrollment {
}
```

### Điểm cần nhớ khi phỏng vấn

Idempotency nghĩa là gọi một hành động nhiều lần nhưng kết quả cuối cùng vẫn không bị sai. Với enroll course, user bấm 10 lần thì kết quả đúng vẫn chỉ là một enrollment. Frontend giảm thao tác lặp, backend và database mới là lớp đảm bảo cuối cùng.

## Reusable Course Card

### Giải thích ngắn gọn

Reusable Course Card là component card khóa học có thể dùng ở nhiều page, nhận dữ liệu qua props và báo hành động qua emits.

### Ví dụ trong project này

`MyCourseCard.vue` nhận `course` qua props và emit `continue` khi user bấm nút học tiếp. `StudentDashboardPage` và `MyCoursesPage` có thể cùng dùng component này.

### Câu hỏi phỏng vấn liên quan

Vì sao component card không nên tự quyết định toàn bộ điều hướng?

### Câu trả lời ngắn gọn

Vì component card nên dễ tái sử dụng. Page cha có context đầy đủ hơn để quyết định route nào phù hợp.

### Ví dụ code dễ nhớ

```vue
<MyCourseCard
  v-for="course in courses"
  :key="course.id"
  :course="course"
  @continue="goToLearning"
/>
```

```javascript
const emit = defineEmits(['continue'])

function handleContinue() {
  emit('continue', props.course)
}
```

### Lưu ý

Card nên nhận dữ liệu qua props và phát event khi user tương tác. Nếu card tự gọi router hoặc API quá nhiều, nó sẽ khó dùng lại ở page khác. Ví dụ dashboard có thể muốn đi tới lesson gần nhất, còn course list có thể muốn đi tới course detail.

## Progress Mapping

### Giải thích ngắn gọn

Progress Mapping là việc frontend đọc dữ liệu tiến độ từ backend và hiển thị thành progress bar, phần trăm hoặc số bài đã học.

### Ví dụ trong project này

`MyCourseCard.vue` ưu tiên dùng `course.progressPercent`. Nếu field này không có, component fallback tính từ `completedLessons / totalLessons`.

### Câu hỏi phỏng vấn liên quan

Vì sao nên ưu tiên progress do backend tính?

### Câu trả lời ngắn gọn

Backend là nơi biết rule nghiệp vụ đầy đủ nhất. Frontend chỉ nên hiển thị hoặc fallback đơn giản khi cần.

### Ví dụ code dễ nhớ

```javascript
const progressPercent = computed(() => {
  if (props.course.progressPercent != null) {
    return props.course.progressPercent
  }

  if (!props.course.totalLessons) {
    return 0
  }

  return Math.round(
    (props.course.completedLessons / props.course.totalLessons) * 100
  )
})
```

### Lưu ý

Fallback ở frontend chỉ nên dùng để hiển thị. Nếu dữ liệu progress ảnh hưởng nghiệp vụ như cấp chứng chỉ, mở khóa bài học tiếp theo, hoặc tính hoàn thành course, backend phải là nơi tính chính thức.

## API Contract Alignment

### Giải thích ngắn gọn

API Contract Alignment là việc đảm bảo frontend route, service và backend endpoint dùng cùng kiểu định danh, cùng request/response.

### Ví dụ trong project này

Frontend đang điều hướng `/student/lessons/:slug`, nhưng backend learning API hiện là `GET /api/v1/lessons/{id}`. Task LearningPage cần xử lý rõ dùng `lessonId` hay thêm API lấy bài học theo slug trong course.

### Câu hỏi phỏng vấn liên quan

Điều gì xảy ra nếu frontend dùng slug còn backend chỉ nhận id?

### Câu trả lời ngắn gọn

Frontend sẽ không gọi được API đúng, dễ gây 404 hoặc phải viết logic tạm. Cần thống nhất contract trước khi tích hợp UI.

### Ví dụ vấn đề thường gặp

```text
Frontend route:
/student/lessons/n5-greetings

Backend endpoint:
GET /api/v1/lessons/15
```

Trong ví dụ này, frontend đang có `slug`, còn backend cần `id`. Có 3 cách xử lý rõ ràng:

- Đổi route frontend sang `/student/lessons/:id`.
- Thêm API backend `GET /api/v1/lessons/slug/{slug}` nếu slug unique.
- Dùng route có cả context như `/courses/:courseSlug/lessons/:lessonSlug` nếu lesson slug chỉ unique trong course.

### Điểm cần nhớ khi phỏng vấn

API contract là "hợp đồng" giữa frontend và backend. Contract không chỉ là URL, mà còn gồm method, auth requirement, request body, response shape, error code và kiểu định danh. Khi contract lệch, frontend và backend đều có thể đúng riêng lẻ nhưng app vẫn không chạy được.

## Lesson ID Routing

### Giải thích ngắn gọn

Lesson ID Routing là cách dùng id của bài học trong route/frontend để gọi đúng backend API học bài.

### Ví dụ trong project này

Route student learning dùng `/student/lessons/:id`, lấy id này để gọi backend `GET /api/v1/lessons/{id}` và `POST /api/v1/lessons/{id}/progress`.

### Câu hỏi phỏng vấn liên quan

Khi nào nên dùng id thay vì slug cho route?

### Câu trả lời ngắn gọn

Nên dùng id khi backend API đã định danh bằng id, hoặc khi slug không unique toàn hệ thống. Nếu muốn URL đẹp bằng slug, cần đảm bảo slug đủ unique hoặc đi kèm context như course slug.

### Ví dụ code dễ nhớ

```javascript
const route = useRoute()
const lessonId = computed(() => Number(route.params.id))

watch(
  lessonId,
  (id) => {
    if (id) {
      loadLesson(id)
    }
  },
  { immediate: true }
)
```

### Lưu ý

Route param luôn đi vào frontend dưới dạng string. Nếu backend/service cần number, nên convert rõ ràng bằng `Number()` và xử lý trường hợp invalid. Không nên truyền bừa string `"abc"` vào API id vì lỗi sẽ khó hiểu hơn.

## DTO Evolution

### Giải thích ngắn gọn

DTO Evolution là việc thay đổi DTO theo nhu cầu mới của client mà vẫn giữ contract rõ ràng và hạn chế phá vỡ code cũ.

### Ví dụ trong project này

`MyCourseRes` được bổ sung `lastLessonId` để frontend có thể điều hướng tới learning route đúng với backend API hiện có.

### Câu hỏi phỏng vấn liên quan

Thêm field mới vào response DTO có thường phá frontend cũ không?

### Câu trả lời ngắn gọn

Thường không, nếu chỉ thêm field optional và giữ các field cũ. Rủi ro lớn hơn là đổi tên hoặc xóa field đang được frontend sử dụng.

### Ví dụ dễ nhớ

```java
public record MyCourseRes(
        Long id,
        String title,
        String slug,
        Double progressPercent,
        Long lastLessonId // field mới để frontend điều hướng học tiếp
) {}
```

### Nguyên tắc khi evolve DTO

- Thêm field mới thường an toàn hơn đổi tên field cũ.
- Xóa field hoặc đổi kiểu dữ liệu là breaking change.
- Nếu cần đổi lớn, nên version API hoặc hỗ trợ song song trong một thời gian.
- Field mới nên có giá trị `null` hợp lý nếu dữ liệu cũ chưa có.

### Điểm cần nhớ khi phỏng vấn

DTO không phải entity. DTO là contract phục vụ client. Khi UI có nhu cầu hợp lý như "học tiếp bài gần nhất", backend có thể bổ sung `lastLessonId` vào DTO thay vì bắt frontend tự suy luận từ nhiều API khác nhau.

## Monotonic Progress

### Giải thích ngắn gọn

Monotonic Progress là rule tiến độ chỉ tăng hoặc giữ nguyên, không bị giảm xuống khi client gửi watchedPercent thấp hơn.

### Ví dụ trong project này

Backend update `LessonProgress.watchedPercent` bằng logic chỉ nhận giá trị mới nếu nó lớn hơn giá trị hiện tại. Frontend cũng dùng `Math.max()` để giữ UI cùng rule.

### Câu hỏi phỏng vấn liên quan

Vì sao progress không nên giảm khi client gửi giá trị thấp hơn?

### Câu trả lời ngắn gọn

Vì tiến độ học thường biểu diễn mức cao nhất user đã đạt được. Nếu cho giảm tùy ý, user có thể mất tiến độ hoặc UI bị nhảy lùi khó hiểu.

### Ví dụ code dễ nhớ

```java
double currentPercent = progress.getWatchedPercent();
double incomingPercent = req.getWatchedPercent();

progress.setWatchedPercent(Math.max(currentPercent, incomingPercent));

if (progress.getWatchedPercent() >= 90) {
    progress.setCompleted(true);
}
```

### Lưu ý

Monotonic progress phù hợp với "mức cao nhất đã học". Nhưng không phải mọi trạng thái đều monotonic. Ví dụ volume video, playback speed, hoặc câu trả lời quiz hiện tại có thể tăng giảm bình thường. Cần hiểu ý nghĩa nghiệp vụ trước khi áp dụng rule chỉ tăng.

## Learning API Service

### Giải thích ngắn gọn

Learning API Service là lớp frontend service gom các API liên quan tới học bài và cập nhật tiến độ.

### Ví dụ trong project này

`learning.service.js` có `getLessonDetail(id)` và `updateProgress(id, req)`, giúp `LessonLearningPage.vue` không phải biết chi tiết endpoint ở nhiều chỗ.

### Câu hỏi phỏng vấn liên quan

Vì sao nên tách learning API thành service riêng?

### Câu trả lời ngắn gọn

Vì page component sẽ gọn hơn, API dễ tái sử dụng và khi endpoint thay đổi chỉ cần sửa ở service.

### Ví dụ code dễ nhớ

```javascript
export const LearningService = {
  getLessonDetail(id) {
    return api.get(`/api/v1/lessons/${id}`)
  },

  updateProgress(id, payload) {
    return api.post(`/api/v1/lessons/${id}/progress`, payload)
  }
}
```

### Lưu ý

Learning page thường có nhiều interaction nhỏ như load lesson, update progress, mark completed, chuyển bài tiếp theo. Nếu gọi Axios trực tiếp trong component cho từng việc, component sẽ rất nhanh rối. Service layer giúp gom endpoint và giữ component tập trung vào UI/state.

## Derived Data

### Giải thích ngắn gọn

Derived Data là dữ liệu được tính ra từ dữ liệu khác, thường dùng để tối ưu hiển thị hoặc truy vấn.

### Ví dụ trong project này

`CourseEnrollment.progressPercent` được tính từ số lesson đã hoàn thành trong `lesson_progress` chia cho tổng số lesson của course.

### Câu hỏi phỏng vấn liên quan

Nhược điểm của việc lưu derived data là gì?

### Câu trả lời ngắn gọn

Nó có thể bị lệch nếu không được cập nhật khi dữ liệu nguồn thay đổi. Vì vậy cần cập nhật trong cùng nghiệp vụ hoặc transaction.

### Ví dụ dễ nhớ

```text
completedLessons = 7
totalLessons = 10
progressPercent = 70
```

`progressPercent` là derived data vì nó được tính từ `completedLessons` và `totalLessons`. Nếu lưu nó vào `course_enrollments`, việc đọc dashboard sẽ nhanh hơn vì không cần tính lại liên tục. Nhưng khi một lesson progress thay đổi, backend phải cập nhật lại field này.

### Khi nào nên lưu derived data

- Khi dữ liệu được đọc rất thường xuyên và tính toán lại tốn chi phí.
- Khi màn hình cần render nhanh, ví dụ dashboard hoặc my courses.
- Khi có thể cập nhật dữ liệu dẫn xuất trong cùng transaction với dữ liệu nguồn.

### Khi nào không nên lưu

- Khi công thức đơn giản và dữ liệu nhỏ.
- Khi dữ liệu nguồn thay đổi liên tục nhưng khó đảm bảo cập nhật đồng bộ.
- Khi sai lệch dữ liệu gây hậu quả nghiệp vụ nghiêm trọng.

## Enrollment Progress Recalculation

### Giải thích ngắn gọn

Enrollment Progress Recalculation là việc tính lại tiến độ khóa học của một student sau khi progress của lesson thay đổi.

### Ví dụ trong project này

Sau khi `LearningServiceImpl.updateProgress()` lưu lesson progress, backend đếm số bài đã hoàn thành và update `course_enrollments.progress_percent`.

### Câu hỏi phỏng vấn liên quan

Vì sao recalculation nên nằm ở backend?

### Câu trả lời ngắn gọn

Vì backend là nguồn sự thật của dữ liệu học tập. Nếu để frontend tự tính và gửi lên, user có thể gửi dữ liệu sai hoặc giả mạo.

### Ví dụ code dễ nhớ

```java
@Transactional
public LessonProgressRes updateProgress(Long lessonId, UpdateProgressReq req) {
    LessonProgress progress = upsertLessonProgress(lessonId, req);

    CourseEnrollment enrollment = enrollmentRepository
            .findByUserIdAndCourseId(progress.getUser().getId(), progress.getLesson().getCourse().getId())
            .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

    long totalLessons = lessonRepository.countByCourseId(enrollment.getCourse().getId());
    long completedLessons = progressRepository.countCompletedLessons(
            enrollment.getUser().getId(),
            enrollment.getCourse().getId()
    );

    double courseProgress = totalLessons == 0
            ? 0
            : (completedLessons * 100.0 / totalLessons);

    enrollment.setProgressPercent(courseProgress);
    return progressMapper.toLessonProgressRes(progress);
}
```

### Điểm cần nhớ khi phỏng vấn

Recalculation nên nằm trong transaction để lesson progress và enrollment progress không bị lệch. Nếu update lesson progress thành công nhưng update course progress fail, dữ liệu sẽ không nhất quán. Với hệ thống lớn hơn, có thể dùng event/background job, nhưng vẫn phải có chiến lược đảm bảo eventual consistency.

### Misconception hay gặp

- Nghĩ frontend tính progress rồi gửi lên backend là đủ. Sai, client không đáng tin và có thể gửi số giả.
- Nghĩ lưu derived data là luôn xấu. Không hẳn, nó tốt nếu giúp đọc nhanh và có cơ chế cập nhật đồng bộ.
- Nghĩ chỉ cần update bài hiện tại. Chưa đủ, vì thay đổi bài học có thể ảnh hưởng tiến độ tổng của cả khóa.

### Virtual Threads - Luồng Ảo (Java 21)

**Rating:** 🔴

**Định nghĩa:**
Là tính năng cốt lõi của Java 21, cho phép tạo ra hàng triệu luồng (threads) với chi phí bộ nhớ cực kỳ rẻ (vài KB một thread), giúp giải quyết bài toán nghẽn luồng khi có lượng truy cập lớn (I/O-heavy operations).

**Ví dụ:**
```java
// Tạo một Executor sử dụng Virtual Threads
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
executor.submit(() -> {
    // Xử lý đọc file từ vựng hoặc gọi API tiếng Nhật
});
```

**Lợi ích/Khi nào dùng:**
- Tăng throughput (băng thông xử lý) của server Spring Boot lên gấp nhiều lần.
- Không cần cấu hình Thread Pool phức tạp.

---

### Database Index - Chỉ Mục Dữ Liệu

**Rating:** 🔴

**Định nghĩa:**
Là một cấu trúc dữ liệu (thường là B-Tree) giúp Database tăng tốc độ tìm kiếm các bản ghi, tương tự như mục lục của một cuốn sách.

**Ví dụ:**
```java
// Cấu hình trong Entity để Hibernate tự sinh Index trong DB
@Table(name = "lessons", indexes = {
    @Index(name = "idx_lesson_slug", columnList = "slug")
})
public class Lesson { ... }
```

**Lợi ích/Khi nào dùng:**
- Tăng tốc tối đa cho các câu lệnh tìm kiếm theo điều kiện (`WHERE slug = ...`).
- Rất cần thiết cho các trường dữ liệu dùng để Search hoặc Filter ở Frontend.

**Misconception hay gặp:**
- Tạo Index cho mọi cột là tốt: Sai, Index làm chậm câu lệnh INSERT/UPDATE vì DB phải cập nhật lại cây B-Tree. Chỉ tạo cho cột nào thường xuyên xuất hiện trong mệnh đề WHERE.

## Integration Testing (Failsafe Plugin)

### Giải thích ngắn gọn

Integration Testing (Kiểm thử tích hợp) là việc kiểm tra xem các thành phần của hệ thống (như Controller, Service, Repository và Database) có hoạt động trơn tru với nhau hay không. Trong Spring Boot, Maven chia test làm hai pha: `test` (dành cho Unit Test qua plugin `surefire`) và `verify` (dành cho Integration Test qua plugin `failsafe`).

### Ví dụ trong project này

Các file kết thúc bằng `*IT.java` như `StudentDashboardIT.java` được cấu hình để chạy bởi `maven-failsafe-plugin` ở giai đoạn `verify`, giúp nạp toàn bộ `ApplicationContext` cùng database ảo H2 để test API từ ngoài vào trong.

### Câu hỏi phỏng vấn liên quan

Tại sao không dùng luôn `surefire` để chạy tất cả các bài test?

### Câu trả lời ngắn gọn

Vì Integration Test thường tốn nhiều thời gian và yêu cầu ứng dụng phải được đóng gói (package) hoặc dựng container (database, cache) trước khi chạy. Tách nó sang giai đoạn `verify` (sau giai đoạn `package`) giúp quy trình CI/CD fail fast: nếu unit test lỗi thì không cần mất công build package và chạy DB ảo.

### Điểm cần nhớ khi phỏng vấn

Khi nạp `ApplicationContext` bằng `@SpringBootTest`, Spring sẽ tự động tìm và khởi chạy các bean cấu hình như `CommandLineRunner` (ví dụ `DatabaseSeeder`). Nếu các thành phần này phụ thuộc vào dữ liệu thật nhưng lại bị `@MockBean` ghi đè, hệ thống sẽ báo lỗi. Việc sử dụng `@ConditionalOnProperty` để vô hiệu hóa các bean này ở môi trường test là giải pháp tốt nhất.

## Lombok Builder.Default

### Giải thích ngắn gọn

`@Builder.Default` là một annotation của Lombok đi kèm với `@Builder`, giúp giữ nguyên giá trị khởi tạo ban đầu của một trường dữ liệu (field) khi đối tượng được tạo thông qua pattern Builder.

### Ví dụ trong project này

Trong entity `Lesson`, trường `sortOrder` có giá trị khởi tạo `private Integer sortOrder = 0;`. Nếu không có `@Builder.Default`, khi tạo đối tượng bằng `Lesson.builder().title("A").build()`, trường `sortOrder` sẽ bị Lombok ghi đè thành `null`.

### Câu hỏi phỏng vấn liên quan

Vì sao Lombok cảnh báo `@Builder will ignore the initializing expression entirely`?

### Câu trả lời ngắn gọn

Vì nguyên tắc của Builder pattern là chỉ set các giá trị được khai báo rõ ràng. Để bảo toàn giá trị khởi tạo ở khai báo field, Lombok yêu cầu lập trình viên phải chủ động đánh dấu bằng `@Builder.Default`.

### Ví dụ code dễ nhớ

```java
@Builder
public class Course {
    // Không có @Builder.Default -> Nếu builder không gọi .status(), status sẽ = null
    private CourseStatus oldStatus = CourseStatus.DRAFT;

    // Có @Builder.Default -> Nếu builder không gọi .status(), status sẽ = DRAFT
    @Builder.Default
    private CourseStatus status = CourseStatus.DRAFT;
}
```

### Misconception hay gặp

- Nghĩ rằng Java gán `0` hoặc `DRAFT` nên object luôn có giá trị đó. Sai, vì Lombok Builder can thiệp vào quá trình tạo constructor, bỏ qua phép gán ban đầu nếu không có `Default`.

## @DataJpaTest vs @SpringBootTest

### Giải thích ngắn gọn

`@DataJpaTest` là một annotation dùng để test tầng Repository (JPA slice). Khác với `@SpringBootTest` (nạp toàn bộ cấu hình, controller, service, security), `@DataJpaTest` chỉ nạp các `@Repository`, `@Entity` và các cấu hình liên quan đến database.

### Ví dụ trong project này

Các file test cho repository như `LessonProgressRepositoryTest` và `CourseRepositoryTest` dùng `@DataJpaTest` kết hợp với H2 In-Memory Database để kiểm thử các query phức tạp như `@Query("UPDATE ...")` hay phân trang `Page<Course>`.

### Câu hỏi phỏng vấn liên quan

Vì sao nên dùng `@DataJpaTest` thay vì dùng Mockito (`@Mock`) để test các custom query trong Repository?

### Câu trả lời ngắn gọn

Vì Mockito chỉ giả lập hành vi (trả về kết quả cho trước), nó không thể kiểm chứng được câu lệnh SQL sinh ra có đúng cú pháp hay logic hay không. `@DataJpaTest` sử dụng database thực (hoặc H2) để chạy lệnh SQL, giúp phát hiện lỗi mapping và lỗi SQL.

### Ví dụ code dễ nhớ

```java
@DataJpaTest
@ActiveProfiles("test") // Đọc file application-test.yml (thường trỏ vào H2)
class CourseRepositoryTest {
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private EntityManager em;

    @Test
    void testCustomQuery() {
        // 1. Seed dữ liệu thật vào DB In-memory
        Course course = new Course(...);
        em.persist(course);
        em.flush();
        em.clear(); // Xóa cache để ép query chạy xuống DB

        // 2. Gọi Repository method cần test
        Optional<Course> found = courseRepository.findBySlug("test-slug");
        
        // 3. Assert kết quả
        assertThat(found).isPresent();
    }
}
```

### Điểm cần nhớ khi phỏng vấn

1. **Tốc độ**: Nhanh hơn `@SpringBootTest` rất nhiều.
2. **Transaction**: `@DataJpaTest` mặc định là `@Transactional`, tức là nó sẽ tự động **rollback** sau mỗi hàm `@Test`, giúp các test case hoàn toàn độc lập và không bị rác dữ liệu.
3. **Hibernate Cache**: Khi insert dữ liệu mẫu, thường phải gọi `em.flush()` và `em.clear()` để đảm bảo dữ liệu đã được đẩy vào DB thật, tránh trường hợp query đọc nhầm từ Hibernate L1 Cache, làm sai lệch kết quả test.

---

## 30. Cơ chế bảo mật Session: Refresh Token Rotation & Token Reuse Detection

Khi làm việc với các ứng dụng Single Page Application (React, Vue) hoặc Mobile App, việc lưu trữ Access Token và Refresh Token phía client tiềm ẩn rủi ro bảo mật (như XSS trộm token từ LocalStorage). Để giải quyết triệt để rủi ro này, dự án áp dụng **Refresh Token Rotation**.

### 1. Refresh Token Rotation là gì?
Thay vì cấp phát một Refresh Token có thời hạn sống rất lâu (ví dụ 1 tháng) và dùng đi dùng lại nhiều lần, **Rotation** hoạt động theo nguyên tắc:
- Mỗi khi client sử dụng Refresh Token để lấy Access Token mới, Backend sẽ **hủy luôn (revoke)** Refresh Token cũ đó.
- Sau đó, Backend sẽ cấp phát một cặp (Access Token MỚI + Refresh Token MỚI).
- **Lợi ích:** Token trở thành dạng *sử dụng 1 lần (One-time use)*. Nếu hacker đánh cắp được token, chúng chỉ có một cơ hội duy nhất để sử dụng. Và nếu client hợp pháp đã dùng nó trước, token đó đã bị vô hiệu hóa.

### 2. Token Reuse Detection (Phát hiện đánh cắp Session)
Mặc dù Token Rotation rất mạnh, nhưng điều gì sẽ xảy ra nếu Hacker dùng token đánh cắp được *trước* khi chủ nhân thật sự dùng nó? (Hacker dùng thành công và lấy được cặp token mới, đẩy chủ nhân ra ngoài).
Khi chủ nhân thật sự quay lại và cố gắng dùng Refresh Token cũ của họ (đã bị Backend đánh dấu là *Revoked* do Hacker đã dùng trước đó), Backend sẽ nhận diện được hành vi **Reuse (Dùng lại)**.

**Xử lý Token Reuse Detection:**
- Nếu Backend nhận được một yêu cầu sử dụng Refresh Token mà token đó có trạng thái `revoked = true` trong database.
- Backend hiểu rằng: "Oh, token này đáng lẽ không được dùng nữa, vậy mà có người đang cố dùng nó. Chắc chắn session này đã bị đánh cắp hoặc có xung đột nghiêm trọng!"
- **Hành động bảo vệ:** Backend lập tức tìm ra User ID sở hữu token đó và **REVOKE (Thu hồi) TOÀN BỘ CÁC TOKEN HIỆN CÓ** của user này. 
- Hậu quả: Cả Hacker và User thật đều bị đăng xuất. User thật sẽ phải nhập lại Username và Password để tạo một Session hoàn toàn mới và an toàn.

### 3. Database Cleanup (Dọn dẹp rác)
Vì Refresh Token Rotation tạo ra rất nhiều bản ghi token (cứ mỗi lần refresh là sinh ra 1 token mới và bỏ 1 token cũ), Database sẽ nhanh chóng phình to (Database Bloat).
**Giải pháp:** Sử dụng Spring Boot `@Scheduled(cron = "0 0 2 * * ?")` để tạo một **Job Cron** chạy ngầm vào 2h sáng mỗi ngày. Job này sẽ càn quét Database và xóa cứng (`DELETE`) tất cả các token đã quá hạn (`expiredAt < now`) hoặc đã bị revoke (`revoked = true`).

### Điểm cần nhớ khi phỏng vấn
- **Refresh Token Rotation**: Trả lời phỏng vấn viên rằng đây là kỹ thuật an toàn nhất cho SPA khi không thể dùng HttpOnly Cookies.
- **Reuse Detection**: Là "lá chắn cuối cùng" giúp cô lập hoàn toàn thiệt hại, buộc kẻ gian phải ngừng kết nối khi phát hiện sự bất thường.
- **Idempotency**: Chú ý xử lý các trường hợp Race Condition trên Frontend (ví dụ 2 request cùng gọi API refresh lúc token hết hạn) bằng cách dùng Mutex hoặc Promise Queue ở Frontend Axios Interceptor để tránh Backend hiểu lầm là bị đánh cắp.

---

## 31. Bảo mật Session với HttpOnly Cookies trong kiến trúc SPA

Trong kiến trúc Single Page Application (SPA), nơi lưu trữ thông tin xác thực đóng vai trò cực kỳ quan trọng. Việc chuyển đổi từ LocalStorage sang HttpOnly Cookie cho Refresh Token mang lại sự nâng cấp bảo mật đáng kể.

### 1. Tại sao LocalStorage lại nguy hiểm?
`localStorage` và `sessionStorage` trong trình duyệt là những nơi lưu trữ có thể dễ dàng truy cập bởi bất kỳ đoạn mã JavaScript nào chạy trên cùng domain thông qua biến toàn cục `window.localStorage`.
Nếu trang web dính lỗi **XSS (Cross-Site Scripting)**, mã độc của hacker có thể dễ dàng đọc được các Token lưu tại đây và gửi về máy chủ của chúng. Hậu quả là hacker có thể mạo danh người dùng (Account Takeover).

### 2. Sức mạnh của HttpOnly Cookie
**HttpOnly Cookie** là một giải pháp thiết kế chuyên biệt để chống lại XSS:
- Khi một Cookie được server thiết lập kèm cờ `HttpOnly=true`, trình duyệt sẽ chặn hoàn toàn mọi lệnh JavaScript (như `document.cookie`) muốn đọc hoặc ghi đè cookie này.
- Nghĩa là cho dù hacker có khai thác được lỗ hổng XSS, chúng cũng **không thể lấy trộm** được Refresh Token.
- Trình duyệt sẽ tự động đính kèm Cookie này một cách "vô hình" vào các request HTTP gửi đến đúng Domain và Path đã chỉ định.

### 3. Các thuộc tính Cookie quan trọng đi kèm
- **`Secure`**: Yêu cầu Cookie chỉ được gửi đi trên các kết nối HTTPS được mã hóa. Điều này chống lại kĩ thuật nghe lén mạng (Man-In-The-Middle attacks).
- **`SameSite`**: Dùng để chống lại tấn công **CSRF (Cross-Site Request Forgery)**.
  - `Strict`: Chỉ gửi cookie nếu người dùng đang ở chính xác domain đó. Tối ưu cho bảo mật nhưng có thể làm giảm trải nghiệm (ví dụ bấm link từ email vào sẽ không có cookie).
  - `Lax`: Gửi cookie khi điều hướng qua liên kết (navigate) nhưng không gửi trong các cross-origin POST/PUT request (như iframe, form ẩn). Đây là chuẩn mặc định an toàn cho hầu hết SPA.

### 4. Triển khai trong thực tế (Kỹ thuật "Silent Session Restore")
Lưu Refresh Token vào Cookie đồng nghĩa với việc Frontend sẽ không bao giờ nhìn thấy nó. Vậy làm sao ứng dụng biết người dùng đã đăng nhập khi họ tải lại trang?
- Access Token (sống ngắn hạn) sẽ được lưu trong RAM (biến JS / Pinia State).
- Khi người dùng tải lại trang (F5), Access Token trong RAM sẽ bị xóa sạch.
- **Silent Restore**: Ngay trước khi mount giao diện (ví dụ trong Vue Router `beforeEach`), Frontend sẽ tự động tạo một HTTP POST request ngầm gọi lên API `/api/auth/refresh-token`.
- Nhờ cấu hình Axios `{ withCredentials: true }`, trình duyệt tự động móc Refresh Token Cookie ra và đính kèm vào request.
- Backend kiểm tra Cookie hợp lệ, trả về một Access Token hoàn toàn mới và gán một Refresh Token Cookie mới (nếu có Rotation).
- Ứng dụng "sống lại" (Hydrated) với session mới mà người dùng không hề hay biết!

### Điểm cần nhớ khi phỏng vấn
- Luôn khẳng định `HttpOnly Cookie` là tiêu chuẩn vàng để lưu Refresh Token thay cho LocalStorage.
- Trình bày được luồng đi của "Silent Restore Session" để thuyết phục rằng dù giấu token đi, hệ thống vẫn duy trì được trải nghiệm mượt mà.
- Phân biệt rõ `HttpOnly` (chống XSS) và `SameSite` (chống CSRF).

---

## 32. Bảo mật tham số phân trang (Pagination Hardening)

Phân trang (Pagination) là một tính năng phổ biến trong các API RESTful để trả về một lượng nhỏ dữ liệu từ một tập dữ liệu lớn. Tuy nhiên, nếu không được kiểm soát kỹ lưỡng, nó có thể trở thành lỗ hổng để tấn công Từ chối dịch vụ (DoS).

### 1. Rủi ro của phân trang không kiểm soát
Mặc định, các framework như Spring Data cho phép client truyền lên `page` và `size`. Nếu developer chỉ gán giá trị mặc định (`defaultValue = "10"`) mà không Validate đầu vào:
- Client có thể truyền `size = 1000000`. Khi đó, câu lệnh SQL sẽ cố gắng query và load 1 triệu bản ghi vào RAM của server.
- Hành vi này có thể làm tràn bộ nhớ (Out of Memory - OOM) và làm sập ứng dụng.
- Client cũng có thể truyền `page = -1` hoặc `size = 0`, gây ra lỗi Runtime Exception hoặc SQL Exception không mong muốn (500 Internal Server Error).

### 2. Kỹ thuật củng cố (Hardening)
Việc củng cố (Hardening) đồng nghĩa với việc đưa hệ thống vào trạng thái an toàn nhất quán (Predictable State) thông qua Validation chặt chẽ:
- **Ngăn chặn số âm/không**: `page >= 0` và `size > 0`.
- **Giới hạn cận trên (Clamping/Max Size)**: Đặt ra một con số tối đa hợp lý cho ứng dụng (ví dụ: `size <= 100`). Bất kỳ Request nào vượt quá mức này sẽ bị chặn ngay tại cửa (tầng Controller) trước khi xuống Database.

### 3. Xử lý lỗi chuẩn mực (Standardized Error Response)
Khi phát hiện tham số không hợp lệ, hệ thống không nên tự động thay đổi giá trị (silently clamp) mà nên phản hồi rõ ràng cho Frontend biết họ đang truyền sai thông số.
- Ném ra một ngoại lệ có kiểm soát (như `AppException(ErrorCode.VALIDATION_ERROR)`).
- `GlobalExceptionHandler` sẽ chặn ngoại lệ này và trả về HTTP 400 Bad Request kèm mã lỗi nội bộ (ví dụ: `1005`) và thông báo rõ ràng: `"Page size must not be greater than 100"`.

### Điểm cần nhớ khi phỏng vấn
- Luôn khẳng định việc validate `page` và `size` là bắt buộc đối với các API public hoặc API có dữ liệu lớn để chống DoS.
- Giải thích được rằng Validation Exception ném ra từ Controller sẽ được gom lại và xử lý tại `GlobalExceptionHandler` (bằng `@RestControllerAdvice`), giúp Backend trả về các cấu trúc lỗi (`ApiResponse`) hoàn toàn đồng nhất.

---

## Idempotent Completion API

### Giải thích ngắn gọn

Idempotent Completion API là endpoint đánh dấu một tài nguyên là hoàn thành, nhưng gọi nhiều lần vẫn cho kết quả cuối cùng giống nhau. API không tạo dữ liệu trùng, không làm giảm progress và không khiến hệ thống rơi vào trạng thái sai.

### Ví dụ trong project này

Endpoint `POST /api/v1/lessons/{id}/complete` đánh dấu lesson là đã hoàn thành:
- `watchedPercent = 100`
- `isCompleted = true`
- `completedAt` có giá trị
- `course_enrollments.progress_percent` được tính lại

Nếu student bấm nút "Đánh dấu hoàn thành" nhiều lần, lesson vẫn chỉ ở trạng thái completed và progress vẫn là 100%.

### Câu hỏi phỏng vấn liên quan

Vì sao endpoint complete lesson nên được thiết kế idempotent?

### Câu trả lời ngắn gọn

Vì frontend có thể gửi request lặp lại do user bấm nhiều lần, mạng retry hoặc tab bị refresh. Idempotent giúp backend xử lý an toàn và giữ dữ liệu nhất quán.

---

## Service Helper Refactor

### Giải thích ngắn gọn

Service Helper Refactor là việc tách các đoạn logic dùng chung trong service thành method nhỏ có tên rõ ràng. Cách này giúp code ít duplicate, dễ đọc và giảm rủi ro hai API xử lý cùng nghiệp vụ nhưng lệch rule.

### Ví dụ trong project này

`LearningServiceImpl` tách helper cho các bước dùng chung giữa update progress và complete lesson:
- `validateAndGetLessonAccess()` kiểm tra current user, lesson, course published và enrollment.
- `upsertLessonProgress()` update hoặc tạo mới lesson progress.
- `recalculateEnrollmentProgress()` tính lại progress tổng của khóa học.

Nhờ vậy `updateProgress()` và `completeLesson()` cùng đi qua một bộ rule nghiệp vụ thống nhất.

### Câu hỏi phỏng vấn liên quan

Khi nào nên tách helper method trong service layer?

### Câu trả lời ngắn gọn

Khi nhiều method service dùng chung các bước nghiệp vụ quan trọng, nên tách helper để tránh duplicate, dễ test và đảm bảo các flow xử lý nhất quán.

---

## Frontend API Service Layer

### Giải thích ngắn gọn

Frontend API Service Layer là lớp gom các hàm gọi API vào một file/service riêng. Component Vue không gọi Axios trực tiếp ở nhiều nơi, mà gọi các method có tên nghiệp vụ như `getLessonDetail()`, `updateProgress()` hoặc `completeLesson()`.

### Ví dụ trong project này

`LearningService.completeLesson(id)` gọi:

```javascript
return api.post(`/v1/lessons/${id}/complete`)
```

`LessonLearningPage.vue` chỉ cần gọi `LearningService.completeLesson(lesson.value.id)`, không cần biết chi tiết base URL hay endpoint đầy đủ.

### Câu hỏi phỏng vấn liên quan

Vì sao nên tách API call ra service layer trong Vue?

### Câu trả lời ngắn gọn

Vì service layer giúp code dễ maintain, dễ đổi endpoint, dễ tái sử dụng và component tập trung vào UI logic thay vì chi tiết HTTP.

---

## Local State Synchronization After Mutation

### Giải thích ngắn gọn

Sau khi frontend gọi một API làm thay đổi dữ liệu, UI cần đồng bộ lại state local để hiển thị kết quả mới. Có hai cách phổ biến: tự cập nhật local state nếu dữ liệu đơn giản, hoặc fetch lại từ backend nếu cần chính xác toàn bộ.

### Ví dụ trong project này

Sau khi `completeLesson()` thành công, `LessonLearningPage.vue` cập nhật:
- `lesson.isCompleted = true`
- `lesson.watchedPercent = 100`
- `progressForm.isCompleted = true`
- `progressForm.watchedPercent = 100`

Nhờ vậy người học thấy bài học đã hoàn thành ngay lập tức.

### Câu hỏi phỏng vấn liên quan

Khi nào nên update local state, khi nào nên refetch dữ liệu?

### Câu trả lời ngắn gọn

Nếu thay đổi đơn giản và biết chắc kết quả, update local state là đủ và nhanh. Nếu backend trả thêm nhiều dữ liệu mới hoặc có logic phức tạp, nên fetch lại để tránh UI lệch dữ liệu thật.

---

## 34. Quản lý cấu hình & The Twelve-Factor App (Environment Variables)

### Giải thích ngắn gọn
Trong phát triển phần mềm hiện đại, có một triết lý thiết kế ứng dụng tên là **"The Twelve-Factor App"**. Yếu tố thứ 3 trong 12 nguyên tắc này quy định: **"Store config in the environment"** (Lưu trữ cấu hình trong biến môi trường). Nghĩa là mọi thông tin có thể thay đổi giữa các môi trường (Dev, Staging, Prod) — đặc biệt là các secret/password — phải được tách biệt hoàn toàn khỏi mã nguồn.

### Ví dụ trong project này
Thay vì code cứng mật khẩu Database vào file `application-dev.yml`:
```yaml
# XẤU (Lộ lọt thông tin nhạy cảm)
spring:
  datasource:
    password: "0209"
```

Chúng ta sử dụng cú pháp `${VAR_NAME}` của Spring Boot để đọc từ biến môi trường:
```yaml
# TỐT (An toàn và linh hoạt)
spring:
  datasource:
    password: ${DB_PASSWORD}
```

Và tạo một file `.env.example` chứa dữ liệu mẫu để các developer khác biết cách thiết lập:
```bash
# .env.example
DB_PASSWORD=0209
```
Mỗi developer sẽ copy file `.env.example` thành `.env` (file này đã được đưa vào `.gitignore` để không bị commit lên Git).

### Tại sao không nên dùng giá trị mặc định thẳng trong file (VD: `${DB_PASSWORD:0209}`)?
Nếu bạn thiết lập giá trị mặc định là một mật khẩu thực tế (ví dụ: `0209`), mật khẩu đó vẫn nằm trong code base và được đưa lên Git. Mục tiêu của chúng ta là xóa sổ hoàn toàn mọi vết tích của "Secret thật" trên hệ thống quản lý mã nguồn.

### Câu hỏi phỏng vấn liên quan
Tại sao chúng ta phải lưu secret trong biến môi trường thay vì config file?

### Câu trả lời ngắn gọn
Để đảm bảo **Bảo mật** (không vô tình commit password lên public repo như GitHub) và **Linh hoạt** (dễ dàng deploy cùng một build artifact lên nhiều môi trường khác nhau chỉ bằng cách truyền tập biến môi trường khác nhau mà không cần sửa code).

---

## 35. Kiểm thử Frontend (Frontend Testing) & Mocking trong Vue

### Giải thích ngắn gọn
Trong môi trường Frontend, có hai loại kiểm thử chính:
1. **Component / Integration Testing**: Render một Component duy nhất (bằng jsdom), làm giả (mock) mọi kết nối ra bên ngoài như gọi API hoặc điều hướng URL. Mục tiêu là kiểm tra xem Component có xử lý đúng state và render UI ra chính xác không. Công cụ: `Vitest`, `@vue/test-utils`.
2. **End-to-End (E2E) Testing**: Khởi động toàn bộ ứng dụng trên một trình duyệt thật (như Chrome), mô phỏng cú click chuột thật của người dùng và gọi đến Database thật. Mục tiêu là kiểm tra toàn bộ luồng. Công cụ: `Playwright`, `Cypress`.

### Ví dụ trong project này
Khi test `RegisterPage.vue`, nếu ta để nguyên nó gọi API thật, test sẽ rất chậm và có thể thất bại nếu mạng yếu hoặc Database chưa bật. Thay vào đó, ta sử dụng **Mocking** (làm giả):
```javascript
// Thay thế module auth.service.js bằng một object giả mạo
vi.mock('@/services/auth.service', () => ({
  AuthService: {
    register: vi.fn() // Tạo ra một hàm gián điệp (spy function)
  }
}))

// Giả lập tình huống API trả về lỗi
AuthService.register.mockRejectedValue({
  isAxiosError: true,
  response: { data: { message: 'Email đã được sử dụng' } }
})
```
Bằng cách này, Component vẫn nghĩ rằng nó đang nói chuyện với Backend thật, nhưng thực ra nó nhận được dữ liệu do ta "mớm" sẵn. Điều này giúp test chạy trong vài mili-giây và độc lập hoàn toàn.

### Câu hỏi phỏng vấn liên quan
Tại sao lại phải dùng `vi.mock()` để giả lập API khi viết component test? Sao không dùng API thật cho chính xác?

### Câu trả lời ngắn gọn
Bởi vì mục tiêu của component test là kiểm tra **logic giao diện** (ví dụ: hiển thị thông báo lỗi màu đỏ nếu request thất bại), chứ không phải kiểm tra backend. Nếu dùng API thật, bài test sẽ chậm, phụ thuộc vào môi trường (mạng/database), vi phạm tính cô lập của Unit Test và khó tạo ra các kịch bản lỗi (edge cases) một cách đáng tin cậy.

---

## 36. DTO Contract Alignment trong Frontend Form

### Giải thích ngắn gọn
DTO Contract Alignment nghĩa là frontend form phải gửi dữ liệu đúng với DTO mà backend định nghĩa. Tên field, kiểu dữ liệu, enum value và field bắt buộc phải khớp để API xử lý ổn định.

### Ví dụ trong project này
`CourseCreateReq` cần:
- `title`
- `slug`
- `shortDescription`
- `description`
- `thumbnailUrl`
- `level`
- `courseType`
- `originalPrice`
- `salePrice`

`CourseUpdateReq` giống create nhưng có thêm `status`. Vì vậy `CourseFormModal.vue` chỉ thêm `status` vào payload khi đang ở edit mode.

### Câu hỏi phỏng vấn liên quan
Vì sao frontend không nên tự đặt tên field khác backend DTO?

### Câu trả lời ngắn gọn
Vì backend bind JSON vào DTO theo tên field. Nếu frontend gửi sai tên field, backend có thể nhận `null`, trả validation error hoặc lưu dữ liệu thiếu.

---

## 37. Modal Form Create/Edit Mode

### Giải thích ngắn gọn
Một modal form có thể dùng chung cho cả tạo mới và chỉnh sửa bằng cách xác định mode dựa trên dữ liệu truyền vào. Nếu có object đang edit thì là update mode, nếu không có thì là create mode.

### Ví dụ trong project này
`CourseFormModal.vue` dùng `editingCourse`:
- `editingCourse = null` nghĩa là tạo khóa học mới.
- `editingCourse` có dữ liệu nghĩa là cập nhật khóa học.

Form từ đó quyết định:
- Tiêu đề modal là "Tạo Khóa học mới" hoặc "Cập nhật Khóa học".
- Submit gọi `AdminService.createCourse()` hoặc `AdminService.updateCourse()`.
- Update mode có field `status`, create mode không cần gửi `status`.

### Câu hỏi phỏng vấn liên quan
Lợi ích của việc dùng chung một modal cho create và update là gì?

### Câu trả lời ngắn gọn
Giúp tái sử dụng UI và validation, giảm duplicate code. Nhưng phải tách rõ logic mode để không gửi nhầm payload giữa create và update.

---

## 38. Nested Resource API

### Giải thích ngắn gọn
Nested Resource API là cách thiết kế endpoint thể hiện quan hệ cha-con giữa các tài nguyên. Ví dụ resource thuộc lesson, lesson thuộc section, section thuộc course.

### Ví dụ trong project này
Admin tạo tài liệu cho một lesson bằng endpoint:

```http
POST /api/v1/admin/lessons/{lessonId}/resources
```

URL này cho thấy resource mới sẽ được gắn với lesson có `lessonId`.

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng nested URL như `/lessons/{id}/resources`?

### Câu trả lời ngắn gọn
Khi tài nguyên con chỉ có ý nghĩa trong ngữ cảnh tài nguyên cha. Lesson resource luôn thuộc một lesson, nên nested URL giúp API rõ nghĩa hơn.

---

## 39. Data Isolation cho Teacher-Owned Content

### Giải thích ngắn gọn
Data isolation là rule đảm bảo user chỉ thao tác được dữ liệu thuộc phạm vi của mình. Với role teacher, điều này thường nghĩa là teacher chỉ được quản lý course/lesson/resource do chính họ sở hữu.

### Ví dụ trong project này
`LessonResourceAdminServiceImpl` kiểm tra course chứa lesson resource. Nếu user không phải `ADMIN` hoặc `SUPER_ADMIN`, hệ thống kiểm tra email teacher của course có khớp user hiện tại không. Nếu không khớp thì ném `DATA_ISOLATION_FORBIDDEN`.

### Câu hỏi phỏng vấn liên quan
Vì sao không chỉ dựa vào frontend để ẩn nút sửa/xóa resource?

### Câu trả lời ngắn gọn
Vì frontend có thể bị bypass bằng Postman hoặc script. Backend phải tự kiểm tra quyền để bảo vệ dữ liệu thật.

---

## 40. Isolated Auxiliary Error Handling

### Giải thích ngắn gọn
Isolated Auxiliary Error Handling là cách xử lý lỗi của các phần phụ trợ sao cho lỗi đó không làm hỏng luồng chính của màn hình. Một UI có thể có nội dung chính và nhiều panel phụ, mỗi phần nên có error state riêng khi hợp lý.

### Ví dụ trong project này
Trong `LessonLearningPage.vue`, nếu API lấy tài liệu đính kèm bị lỗi, trang chỉ hiển thị lỗi trong panel tài liệu. Nội dung bài học, video và progress vẫn hoạt động bình thường.

### Câu hỏi phỏng vấn liên quan
Vì sao không nên đưa user ra khỏi trang học bài khi load resource lỗi?

### Câu trả lời ngắn gọn
Vì resource chỉ là phần bổ trợ. Nếu phần học bài chính vẫn tải được, user vẫn nên tiếp tục học thay vì bị chặn bởi lỗi của panel phụ.

---

## 41. Safe External Links

### Giải thích ngắn gọn
Khi frontend mở link bên ngoài ở tab mới bằng `target="_blank"`, nên thêm `rel="noopener noreferrer"` để giảm rủi ro bảo mật liên quan tới `window.opener`.

### Ví dụ trong project này
Resource link trong trang học bài mở `fileUrl` ở tab mới:

```html
<a :href="res.fileUrl" target="_blank" rel="noopener noreferrer">
  {{ res.title }}
</a>
```

### Câu hỏi phỏng vấn liên quan
`rel="noopener noreferrer"` dùng để làm gì?

### Câu trả lời ngắn gọn
Nó ngăn tab mới truy cập lại tab gốc qua `window.opener`, giúp giảm rủi ro tabnabbing và một số hành vi không an toàn khi mở link bên ngoài.

---

## 42. Curriculum Context Endpoint

### Giải thích ngắn gọn
Curriculum context endpoint là API trả dữ liệu ngữ cảnh chương trình học xung quanh một lesson hiện tại. Dữ liệu này thường gồm course, sections, lessons, trạng thái tiến độ và bài trước/bài sau.

### Ví dụ trong project này
Trang học bài gọi:

```http
GET /api/v1/lessons/{lessonId}/curriculum
```

Backend trả về course title, danh sách section/lesson, `previousLessonId` và `nextLessonId` để frontend render sidebar học tập.

### Câu hỏi phỏng vấn liên quan
Vì sao không để frontend tự lấy toàn bộ course detail rồi tự tính previous/next lesson?

### Câu trả lời ngắn gọn
Vì backend là nơi nắm rule nghiệp vụ như lesson published, enrollment và sort order. Backend tính sẵn giúp frontend đơn giản hơn và tránh lộ dữ liệu không nên hiển thị.

---

## 43. Route Param Watcher trong Vue

### Giải thích ngắn gọn
Route param watcher là cách theo dõi thay đổi của tham số trên URL để component load lại dữ liệu khi route đổi nhưng component không bị destroy/recreate.

### Ví dụ trong project này
`LessonLearningPage.vue` dùng route `/student/lessons/:id`. Khi student click lesson khác trong sidebar, route vẫn dùng cùng component nhưng `id` thay đổi. Component cần watch `route.params.id` để fetch lại lesson, resources và curriculum.

### Câu hỏi phỏng vấn liên quan
Vì sao chuyển từ `/student/lessons/1` sang `/student/lessons/2` đôi khi không tự chạy lại `onMounted()`?

### Câu trả lời ngắn gọn
Vì Vue Router có thể tái sử dụng cùng component cho cùng route pattern. `onMounted()` chỉ chạy khi component mount, nên cần watch route param để xử lý thay đổi dữ liệu.

---

## 56. `.env.example` Whitelist trong Git

### Giải thích ngắn gọn
Khi project ignore các file môi trường như `.env`, đôi khi ta vẫn muốn commit `.env.example` để làm mẫu setup. Khi đó `.gitignore` cần rule phủ định để whitelist file mẫu.

### Ví dụ trong project này
`backend/.gitignore` có thể ignore `.env` nhưng cho phép track `.env.example`:

```gitignore
.env
!.env.example
```

### Câu hỏi phỏng vấn liên quan
Vì sao `.env.example` không nên bị ignore?

### Câu trả lời ngắn gọn
Vì `.env.example` là tài liệu setup quan trọng, giúp developer biết project cần biến môi trường nào mà không lộ secret thật.

---

## 57. Quiz Attempt

### Giải thích ngắn gọn
Quiz attempt là một lần làm bài quiz của user. Một user có thể làm cùng một quiz nhiều lần nếu hệ thống cho phép, và mỗi lần làm cần lưu điểm, thời gian, trạng thái và kết quả riêng.

### Ví dụ trong project này
Entity `QuizAttempt` lưu `user`, `quiz`, `startedAt`, `submittedAt`, `score`, `passed` và `status`. Khi student bấm bắt đầu quiz ở task sau, backend sẽ tạo một attempt mới.

### Câu hỏi phỏng vấn liên quan
Vì sao không lưu điểm trực tiếp vào bảng `quizzes`?

### Câu trả lời ngắn gọn
Vì `quizzes` là đề quiz dùng chung cho nhiều user. Điểm là kết quả của từng user trong từng lần làm, nên phải lưu ở bảng `quiz_attempts`.

---

## 58. EnumType.STRING trong JPA

### Giải thích ngắn gọn
`EnumType.STRING` là cách lưu enum vào database bằng tên enum thay vì số thứ tự. Nó giúp dữ liệu dễ đọc và an toàn hơn khi enum thay đổi thứ tự.

### Ví dụ trong project này
`QuizStatus.DRAFT` được lưu trong database là chuỗi `DRAFT`, không phải số `0`.

### Câu hỏi phỏng vấn liên quan
Rủi ro của `EnumType.ORDINAL` là gì?

### Câu trả lời ngắn gọn
Nếu đổi thứ tự enum trong code, số ordinal cũ trong database có thể trỏ sang ý nghĩa khác, gây lỗi dữ liệu nghiêm trọng.

---

## 44. Current User Endpoint Pattern

### Giải thích ngắn gọn
Current User Endpoint Pattern là cách thiết kế API dùng `/me` để đại diện cho user hiện tại. Backend xác định user bằng token/session thay vì nhận userId từ client.

### Ví dụ trong project này
Student cập nhật profile bằng:

```http
PUT /api/users/me
```

Backend lấy email/user hiện tại từ `SecurityContextHolder`, sau đó query `UserRepository.findByEmail()`.

### Câu hỏi phỏng vấn liên quan
Vì sao `/users/me` thường an toàn hơn `/users/{id}` cho màn hình profile cá nhân?

### Câu trả lời ngắn gọn
Vì client không được chọn userId cần sửa. Backend tự xác định user từ token đã xác thực, giúp tránh lỗi user sửa dữ liệu của người khác.

---

## 45. BCrypt Password Verification

### Giải thích ngắn gọn
BCrypt Password Verification là quá trình kiểm tra mật khẩu người dùng nhập với password hash đã lưu trong database bằng `PasswordEncoder.matches()`.

### Ví dụ trong project này
Khi đổi mật khẩu, `UserServiceImpl` kiểm tra `currentPassword` trước. Nếu đúng, backend encode `newPassword` rồi lưu vào `passwordHash`.

### Câu hỏi phỏng vấn liên quan
Vì sao không so sánh trực tiếp `currentPassword.equals(user.getPasswordHash())`?

### Câu trả lời ngắn gọn
Vì database lưu hash, không lưu mật khẩu gốc. BCrypt còn có salt nên mỗi lần hash có thể khác nhau; phải dùng `PasswordEncoder.matches()` để xác thực đúng cách.

---

## 46. Smoke Test

### Giải thích ngắn gọn
Smoke test là kiểm thử nhanh các luồng chính để chắc rằng hệ thống không bị lỗi nghiêm trọng sau khi build hoặc sau một loạt thay đổi.

### Ví dụ trong project này
Luồng smoke test P0 gồm: guest xem khóa học, student đăng ký/đăng nhập/enroll/học bài/cập nhật profile, admin quản lý user và course structure.

### Câu hỏi phỏng vấn liên quan
Smoke test có thay thế unit test hoặc integration test không?

### Câu trả lời ngắn gọn
Không. Smoke test chỉ kiểm tra hệ thống có chạy được các luồng chính hay không. Unit/integration test vẫn cần để kiểm tra logic chi tiết và tự động hóa hồi quy.

---

## 47. Environment Variable và Secret Management

### Giải thích ngắn gọn
Environment variable là biến cấu hình được truyền từ môi trường chạy app, ví dụ database password hoặc JWT secret. Secret management là cách quản lý các giá trị nhạy cảm đó để không lộ ra source code.

### Ví dụ trong project này
Backend dùng `.env` để cấu hình database password, admin password và JWT secret cho local development.

### Câu hỏi phỏng vấn liên quan
Vì sao nên commit `.env.example` thay vì commit `.env` thật?

### Câu trả lời ngắn gọn
`.env.example` cho người khác biết cần cấu hình key nào nhưng không chứa secret thật. `.env` thật thường chứa password/token nên cần để ngoài git.

---

## 50. Enrollment-Driven CTA

### Giải thích ngắn gọn
Enrollment-Driven CTA là cách render nút hành động chính dựa trên trạng thái ghi danh thật của user với course. CTA không chỉ phụ thuộc course miễn phí/trả phí mà còn phụ thuộc user đã enroll hay chưa.

### Ví dụ trong project này
Ở `CourseDetailPage.vue`, nếu student đã enroll course, nút chính phải là "Tiếp tục học". Nếu chưa enroll và course free, nút mới là "Đăng ký học miễn phí".

### Câu hỏi phỏng vấn liên quan
Vì sao không nên luôn hiển thị nút "Đăng ký" trên course detail?

### Câu trả lời ngắn gọn
Vì user đã enroll sẽ bị hiểu nhầm và có thể bấm đăng ký lại. UI nên phản ánh trạng thái nghiệp vụ hiện tại để flow tự nhiên hơn.

---

## 51. Route Layout Separation

### Giải thích ngắn gọn
Route Layout Separation là cách tách các nhóm route vào layout khác nhau tùy mục tiêu sử dụng. Một route quản lý/dashboard không nhất thiết dùng chung layout với route học bài.

### Ví dụ trong project này
`/student/dashboard` và `/student/my-courses` có thể dùng `StudentLayout`, nhưng `/student/lessons/:id` nên dùng `LearningLayout` để tạo không gian học tập riêng.

### Câu hỏi phỏng vấn liên quan
Vì sao tách `LearningLayout` khỏi `StudentLayout` có thể cải thiện UX?

### Câu trả lời ngắn gọn
Vì trang học cần tập trung vào lesson content và curriculum. Dashboard sidebar hoặc mobile bottom nav có thể chiếm chỗ và gây nhiễu trong lúc học.

---

## 48. Design Token

### Giải thích ngắn gọn
Design token là các giá trị thiết kế dùng chung như màu sắc, font size, spacing, shadow và border radius. Thay vì mỗi page tự chọn style riêng, toàn bộ app dùng chung một bộ token.

### Ví dụ trong project này
Frontend dùng Tailwind config và global CSS để định nghĩa các màu như `primary`, `background`, `surface`, `secondary` và áp dụng lại trên home page, course list, course detail và student pages.

### Câu hỏi phỏng vấn liên quan
Design token giúp gì khi redesign nhiều màn hình cùng lúc?

### Câu trả lời ngắn gọn
Nó giúp UI nhất quán và dễ chỉnh về sau. Nếu đổi màu brand hoặc spacing, ta chỉnh token thay vì sửa thủ công ở nhiều page.

---

## 49. UI Regression Sau Redesign

### Giải thích ngắn gọn
UI regression là lỗi hành vi hoặc hiển thị phát sinh sau khi sửa giao diện. Redesign có thể làm mất event handler, sai điều kiện render hoặc làm layout vỡ ở một trạng thái cụ thể.

### Ví dụ trong project này
Sau khi redesign, cần test lại các flow như login xong quay về trang chủ, course detail hiển thị đúng trạng thái đã ghi danh, và lesson learning không bị nhốt trong dashboard layout.

### Câu hỏi phỏng vấn liên quan
Vì sao giao diện nhìn đẹp hơn nhưng vẫn có thể là regression?

### Câu trả lời ngắn gọn
Vì UI không chỉ là hình ảnh. Nếu nút, route, state hoặc API call bị sai thì trải nghiệm người dùng vẫn hỏng dù màn hình nhìn đẹp.

---

## 50. Mock Data Context & Lỗi 404 Ẩn Trong Integration Test

### Giải thích ngắn gọn
Trong Spring Boot Integration Test (với `@SpringBootTest` và `@AutoConfigureMockMvc`), test case sẽ khởi động toàn bộ ngữ cảnh ứng dụng (Application Context). Điều này có nghĩa là các tầng Filter, Interceptor, và Service Validation đều sẽ chạy y như môi trường production. 
Nếu dữ liệu mock (làm giả) bị thiếu một thuộc tính cốt lõi nào đó mà Business Logic yêu cầu, thay vì trả về HTTP 500 hay báo lỗi Null, ứng dụng có thể trả về HTTP 404 (do Global Exception Handler map `AppException(NOT_FOUND)` sang HTTP 404).

### Ví dụ trong project này
Lỗi build do `LessonProgressIT` mong đợi kết quả trả về `200 OK` nhưng lại nhận được `404 Not Found`. Nguyên nhân không phải do URL gõ sai, mà do `Course` mock data quên khởi tạo field `status`. 
Khi request đi vào `LearningService`, nó bắt gặp validation logic:
```java
if (course.getStatus() != CourseStatus.PUBLISHED) {
    throw new AppException(ErrorCode.LESSON_NOT_FOUND); 
}
```
Và ném ra lỗi dẫn đến kết quả HTTP Response là 404, khiến Integration Test fail. Việc bổ sung `status(CourseStatus.PUBLISHED)` cho mock data đã khắc phục được hiện tượng này.

### Câu hỏi phỏng vấn liên quan
Tại sao khi viết Integration Test với Spring Boot MockMvc, gọi đúng URL nhưng lại bị báo lỗi 404?

### Câu trả lời ngắn gọn
Ngoài khả năng URL chưa tồn tại, lỗi 404 phổ biến trong `@SpringBootTest` thường do dữ liệu mock (Mock Data) hoặc cơ sở dữ liệu test (Test DB) không thỏa mãn các điều kiện business logic (như check trạng thái active/published). Khi các điều kiện này vi phạm, tầng Service thường ném ra lỗi kiểu `NotFoundException` và ExceptionHandler toàn cục sẽ map exception đó thành mã HTTP 404.

---

## 51. Auth Flow "Escape Hatch" (Lối thoát điều hướng) & UI Testing Resilience

### Giải thích ngắn gọn
- **Auth Flow "Escape Hatch"**: Khi thiết kế màn hình Đăng nhập/Đăng ký, không bao giờ được phép dồn người dùng vào một "ngõ cụt" (nơi họ chỉ có 2 lựa chọn: Đăng nhập hoặc Tắt tab). Cần luôn cung cấp các lối thoát như nút "Quay lại" hoặc liên kết về "Trang chủ" để đảm bảo trải nghiệm người dùng (UX) tự nhiên.
- **UI Testing Resilience**: Khi đập đi xây lại giao diện (Redesign) từ Vanilla CSS sang Tailwind CSS, các file kiểm thử tự động (Unit Tests / Component Tests) rất dễ bị gãy (fail) nếu chúng phụ thuộc quá nhiều vào class CSS hoặc cấu trúc DOM. Để giữ cho Test bền vững, ta cần bảo lưu các class cốt lõi mà Test đang query (ví dụ: `.btn-submit`, `.error-alert`) hoặc sử dụng các attribute độc lập như `data-testid` hoặc `role`.

### Ví dụ trong project này
Trong Layout xác thực mới (`AuthLayout.vue`), logic fallback được áp dụng bằng cách kiểm tra `window.history.length > 2`. Nếu có lịch sử duyệt, nút "Quay lại" xuất hiện; nếu không (ví dụ user copy link gửi cho nhau mở tab mới), nút fallback "Khóa học" sẽ hiển thị.
Khi viết lại giao diện Login/Register bằng Tailwind, mặc dù loại bỏ file `main.css` cũ, nhưng các template class như `.error-alert` và tag `h2` vẫn được giữ nguyên để các file `LoginPage.spec.js` và `RegisterPage.spec.js` (dùng thư viện `@vue/test-utils`) vẫn có thể tìm thấy phần tử DOM và verify text lỗi thành công.

### Câu hỏi phỏng vấn liên quan
Khi bạn được giao nhiệm vụ đập đi xây lại (redesign) một tính năng lớn trên Frontend, bạn làm gì để đảm bảo các bài Automated Test cũ không bị phá hỏng?

### Câu trả lời ngắn gọn
Tôi sẽ ưu tiên tách biệt (decouple) các Test Selectors ra khỏi Styling. Ví dụ, nếu các bài Test cũ đang query DOM thông qua các CSS Class thuần túy (ví dụ: `wrapper.find('.error-alert')`), tôi sẽ bảo lưu chính xác class name đó trong bộ code Tailwind mới, hoặc tốt hơn nữa, tôi sẽ refactor bài Test sang việc sử dụng `data-testid="..."` hoặc `role="..."` để đảm bảo UI/UX Design có thay đổi thế nào thì Logic Test vẫn hoàn toàn ổn định.

---

## 52. Database Migration (Flyway) vs Hibernate `ddl-auto`

### Giải thích ngắn gọn
- **`ddl-auto=update`**: Là cơ chế tự động đồng bộ Schema của Hibernate dựa vào JPA `@Entity`. Rất tiện lợi ở môi trường Dev, nhưng cực kỳ nguy hiểm ở Production. Hibernate không giỏi trong việc dự đoán ý định (VD: Nếu bạn đổi tên một cột `price` thành `sale_price`, Hibernate có thể tạo cột mới `sale_price` và bỏ qua/xóa dữ liệu ở cột `price` cũ).
- **Flyway**: Là công cụ quản lý Database Migration chuyên dụng. Mọi thay đổi về cấu trúc Database (Schema) đều phải được viết thành các file SQL đánh số phiên bản rõ ràng (VD: `V1__init.sql`, `V2__add_index.sql`). 
- Khi dùng Flyway, ta cài đặt Hibernate về chế độ `ddl-auto=validate`. Lúc này, Hibernate không tự sửa Database nữa, nó chỉ so sánh xem Schema hiện tại (do Flyway tạo) có khớp 100% với `@Entity` hay không. Nếu lệch, app sẽ báo lỗi và từ chối khởi động, giúp phát hiện lỗi cấu trúc sớm nhất có thể.

### Ví dụ trong project này
Trong `application-dev.yml`, cấu hình đã được đổi từ `update` thành `validate`.
Cùng với đó, một script thuần MariaDB `V1__init_schema.sql` đã được tạo ra trong thư mục `src/main/resources/db/migration/`. Nó chứa chính xác các câu lệnh `CREATE TABLE` kèm theo các Index tối ưu hóa (như `CREATE INDEX idx_course_status ON courses(status);`).
Khi hệ thống chạy (kể cả lúc test bằng `mvn clean verify`), Flyway sẽ quét file `V1`, tạo bảng, sau đó Spring Data JPA sẽ nhảy vào kiểm chứng. 

### Câu hỏi phỏng vấn liên quan
Tại sao người ta khuyên không bao giờ dùng `spring.jpa.hibernate.ddl-auto=update` ở môi trường Production?

Thay vào đó, ở Production ta sử dụng công cụ như Flyway hoặc Liquibase để kiểm soát từng thay đổi bằng mã SQL tường minh, và chỉ dùng `ddl-auto=validate` để Hibernate kiểm chứng lại xem DB Schema và Java Entities đã khớp nhau hoàn toàn hay chưa. Việc này đảm bảo tính ACID và an toàn dữ liệu tuyệt đối.

## Concept 53: API Abuse Protection & Dual-Key Rate Limiting

### Giải thích ngắn gọn
- **Rate Limiting (Giới hạn tỷ lệ)**: Là kỹ thuật giới hạn số lượng request mà một client có thể gửi đến API trong một khoảng thời gian nhất định (ví dụ: 20 request / 15 phút). Mục đích là để chống spam, chống tấn công từ chối dịch vụ (DDoS) và bảo vệ hệ thống khỏi bị lạm dụng (Abuse).
- **Dual-Key Throttling (Throttling Kép)**: Thay vì chỉ giới hạn theo IP, ta kết hợp thêm một khóa thứ hai (ví dụ: Email/Username). Việc này giải quyết 2 bài toán tấn công Authentication khét tiếng:
  1. **Brute Force (Botnet)**: Kẻ tấn công dùng hàng nghìn IP khác nhau để thử mật khẩu vào CÙNG MỘT tài khoản email. Giới hạn theo IP vô dụng, nên ta cần giới hạn theo **Email** (VD: 1 email chỉ được nhập sai 10 lần / 15 phút).
  2. **Credential Stuffing**: Kẻ tấn công có 1 danh sách hàng triệu email/password rò rỉ, dùng 1 IP để thử đăng nhập tuần tự vào TẤT CẢ các email đó. Giới hạn theo Email vô dụng, nên ta cần giới hạn theo **IP** (VD: 1 IP chỉ được thử 20 lần / 15 phút).
- **In-Memory Sliding Window**: Thuật toán lưu lại thời điểm (timestamp) của mỗi request. Các timestamp cũ hơn khung thời gian cho phép (window) sẽ bị xóa (evict). Ta có thể dùng cấu trúc dữ liệu cơ bản như `ConcurrentHashMap<String, Deque<Instant>>` trong RAM thay vì phải setup nguyên một hệ thống Redis cồng kềnh, lý tưởng cho các ứng dụng nhỏ đến vừa (monolith).
- **Generic 429 Response**: Mã lỗi `429 Too Many Requests` trả về khi bị Rate Limit phải chung chung, tuyệt đối không tiết lộ thông tin kiểu *"Email này đang bị khóa tạm thời"* vì hacker có thể dựa vào đó để khẳng định email có tồn tại (Account Enumeration).

### Ví dụ trong project này
Class `RateLimiterService` lưu cache các mốc thời gian truy cập. Trong `AuthController`, hàm `login()` sẽ kiểm tra song song IP (`X-Forwarded-For`) và Email người dùng truyền vào. Nếu bất kỳ chỉ số nào vượt ngưỡng (`>20 lần/IP` hoặc `>10 lần/Email`), lập tức ném ra lỗi `AppException(ErrorCode.TOO_MANY_REQUESTS)` làm cho API trả về mã lỗi 429 chuẩn xác.

### Câu hỏi phỏng vấn liên quan
Làm thế nào để bảo vệ API Đăng nhập khỏi các cuộc tấn công Brute-force và Credential Stuffing? Tại sao chỉ giới hạn theo IP là không đủ?

### Câu trả lời ngắn gọn
Chỉ giới hạn theo IP là không đủ vì kẻ tấn công có thể sử dụng mạng Botnet (hàng chục nghìn IP ẩn danh) để tấn công tập trung vào một tài khoản duy nhất (Brute-force). 
Để phòng thủ toàn diện, hệ thống cần thiết lập "Dual-Key Rate Limiting": Giới hạn cả số lượng request trên mỗi IP (chống Credential Stuffing - quét diện rộng) VÀ giới hạn số lượng request trên mỗi Email (chống Botnet Brute-force - tấn công tập trung). Đồng thời, thông báo lỗi luôn dùng mã HTTP 429 generic để ngăn ngừa kỹ thuật rà quét tài khoản (Account Enumeration).

## Concept 54: CORS & Cookie SameSite in Production

### Giải thích ngắn gọn
- **CORS (Cross-Origin Resource Sharing)**: Là cơ chế bảo mật của trình duyệt, ngăn không cho trang web ở domain A (ví dụ `app.com`) gọi API ngầm sang domain B (ví dụ `api.com`). Trừ khi backend ở domain B cấu hình header `Access-Control-Allow-Origin: app.com` cho phép. 
  - Lưu ý: Trình duyệt luôn gửi một request thăm dò gọi là **Pre-flight request (OPTIONS)** trước khi gửi request thật. Nếu Spring Security chặn mất request `OPTIONS` này (trả về 401), luồng CORS của trình duyệt sẽ chết cứng. Do đó phải tích hợp CORS vào thẳng Spring Security (`.cors()`).
- **Cookie SameSite**: Cookie dùng để lưu Refresh Token (hoặc Session). Trình duyệt có quy định gắt gao về việc gửi Cookie xuyên miền (Cross-site):
  - **Lax**: (Mặc định) Cookie chỉ được gửi đi nếu request diễn ra trên cùng một Site (cùng domain mẹ, ví dụ `app.example.com` gọi `api.example.com`).
  - **None**: Cho phép gửi Cookie xuyên miền (ví dụ `my-frontend.vercel.app` gọi API `my-backend.herokuapp.com`). TUY NHIÊN, để chống CSRF, trình duyệt **bắt buộc** Cookie `SameSite=None` phải đi kèm cờ `Secure=true` (chỉ chạy trên HTTPS). Nếu set `None` mà quên `Secure`, trình duyệt sẽ lẳng lặng vứt Cookie đó đi, gây ra lỗi đăng nhập ngầm rất khó debug.

### Ví dụ trong project này
Biến môi trường `app.cors.allowed-origins` được đưa vào để cấu hình nhiều domain tĩnh ở môi trường Production.
Class `CookieUtil` được trang bị thêm cơ chế phòng vệ **Fast-Fail** (`@PostConstruct`). Nó sẽ kiểm tra lúc khởi động: Nếu lập trình viên cấu hình `same-site: None` nhưng biến `secure: false`, Spring Boot sẽ văng exception và **từ chối khởi động (Crash)** ngay lập tức, ngăn ngừa việc đẩy code lỗi lên Production.

### Câu hỏi phỏng vấn liên quan
Khi deploy ứng dụng Frontend và Backend ở hai domain hoàn toàn khác nhau, tại sao user đăng nhập thành công, Server có trả về Set-Cookie Header chứa Refresh Token, nhưng các request API sau đó trình duyệt lại không chịu gửi Cookie đi?

### Câu trả lời ngắn gọn
Đó là do vi phạm chính sách SameSite của trình duyệt. Khi Frontend và Backend khác domain (Cross-site request), trình duyệt sẽ từ chối gửi Cookie trừ khi Cookie đó được gán 2 cờ: `SameSite=None` VÀ `Secure=true`. Nếu Server trả về `SameSite=None` nhưng lại quên bật cờ `Secure`, hoặc Server đang chạy trên HTTP (không mã hóa), trình duyệt (như Chrome/Firefox) sẽ chủ động block và loại bỏ Cookie đó để đảm bảo an toàn bảo mật, dẫn đến hiện tượng lỗi ngầm (silent failure).

## Concept 55: Giả lập (Mocking) Lỗi Thực Tế trong Frontend Unit Test

### Giải thích ngắn gọn
- Khi viết Unit Test cho luồng gọi API (ví dụ bằng Axios), một sai lầm cực kỳ phổ biến của Junior Developer là giả lập (mock) lỗi quá hời hợt. Ví dụ: `mockRejectedValue(new Error('Lỗi'))` hoặc `mockRejectedValue({ message: 'Lỗi' })`.
- Trong thực tế, các thư viện HTTP client như Axios trả về một cấu trúc lỗi rất phức tạp (Error Schema). Một lỗi Axios chuẩn (AxiosError) thường có `error.isAxiosError = true`, `error.response.data`, `error.response.status`, và `error.code`.
- Nếu Frontend code của bạn cố gắng bóc tách lỗi bằng cách đọc `error.response.data.message`, nhưng trong Unit Test bạn chỉ trả về `{ message: 'Lỗi' }`, test có thể PASS (nếu bạn không assert kỹ), nhưng ra thực tế Production, khi mạng rớt (Network Error), biến `error.response` sẽ bị `undefined`, dẫn đến ứng dụng bị crash trắng trang với lỗi `Cannot read properties of undefined (reading 'data')`.
- **Giải pháp**: Xây dựng các hàm Helper để giả lập chính xác 100% cấu trúc lỗi (Realistic Error Shapes) bao phủ đủ các trường hợp: Lỗi do Backend (4xx, 5xx có response), Lỗi mất kết nối mạng (Network Error không có response), Lỗi Timeout, v.v.

### Ví dụ trong project này
Trong `LoginPage.spec.js` và `RegisterPage.spec.js`, thay vì mock đơn giản, chúng ta đã tạo ra các helper:
1. `makeAxiosApiError(status, code, message)`: Giả lập lỗi từ Backend trả về đúng cấu trúc chuẩn `ApiResponse` (`response.data.code`, `response.data.message`).
2. `makeAxiosNetworkError()`: Giả lập rớt mạng. Biến `isAxiosError: true` nhưng `response: undefined`.
3. `makeAxiosTimeoutError()`: Giả lập lỗi timeout (`code: 'ECONNABORTED'`).
Nhờ vậy, Unit Test của chúng ta bao phủ (cover) toàn bộ các nhánh logic trong hàm `getApiErrorMessage()`, đảm bảo app không bao giờ bị crash vì lỗi chưa lường trước.

### Câu hỏi phỏng vấn liên quan
Tại sao khi viết Unit test xử lý lỗi API (Axios), ứng dụng test vẫn Pass nhưng khi mang lên môi trường thực tế gặp lỗi rớt mạng thì app lại bị crash (trắng màn hình)?

### Câu trả lời ngắn gọn
Nguyên nhân thường do lập trình viên giả lập (mock) cấu trúc lỗi không sát thực tế (Shallow Mocking). Khi test, dev thường mock lỗi có chứa object `response.data`. Nhưng trên thực tế, khi gặp Network Error (rớt mạng) hoặc Timeout, Axios không nhận được phản hồi từ server nên object `error.response` sẽ là `undefined`. Nếu Frontend cố truy cập `error.response.data.message` mà không kiểm tra trước, JavaScript sẽ quăng lỗi `TypeError: Cannot read properties of undefined` gây crash toàn bộ ứng dụng (White screen of death). Để khắc phục, cần viết test mô phỏng chính xác các Error Shapes khác nhau (có response và không có response).

---

## 59. Publish Workflow

### Giải thích ngắn gọn
Publish workflow là quy trình chuyển dữ liệu từ trạng thái nháp sang trạng thái được hiển thị cho user. Trước khi publish thường cần kiểm tra dữ liệu đã đủ điều kiện hay chưa.

### Ví dụ trong project này
Quiz mới tạo mặc định là `DRAFT`. Khi admin gọi publish, backend kiểm tra quiz đã có ít nhất một câu hỏi rồi mới đổi status thành `PUBLISHED`.

### Câu hỏi phỏng vấn liên quan
Vì sao không nên cho publish quiz rỗng?

### Câu trả lời ngắn gọn
Vì quiz rỗng không dùng được cho student và có thể làm hỏng flow start/submit quiz.

---

## 60. Historical Data Integrity trong Quiz

### Giải thích ngắn gọn
Historical Data Integrity là việc bảo vệ dữ liệu lịch sử để kết quả cũ không bị sai khi dữ liệu gốc thay đổi. Với quiz, nếu student đã làm bài, câu hỏi/đáp án liên quan cần được bảo vệ hoặc snapshot.

### Ví dụ trong project này
Backend chặn sửa/xóa question hoặc answer nếu đã có `QuizAttemptAnswer` liên quan, để lịch sử làm bài không bị biến dạng.

### Câu hỏi phỏng vấn liên quan
Vì sao sửa đáp án sau khi student đã làm quiz có thể nguy hiểm?

### Câu trả lời ngắn gọn
Vì kết quả cũ có thể dựa trên đáp án cũ. Nếu thay đổi đáp án gốc mà không snapshot/versioning, điểm và giải thích lịch sử có thể trở nên sai.

---

## 61. Answer Leakage Prevention

### Giải thích ngắn gọn
Answer Leakage Prevention là việc thiết kế API để không làm lộ đáp án đúng trước thời điểm người học nộp bài. Với quiz, backend có thể lưu `isCorrect`, nhưng response cho student khi đang làm bài phải loại bỏ field này.

### Ví dụ trong project này
`GET /api/v1/quizzes/{id}` trả danh sách câu hỏi và đáp án cho student, nhưng `AnswerLearningRes` chỉ chứa `id`, `content`, `sortOrder`, không trả `isCorrect`.

### Câu hỏi phỏng vấn liên quan
Vì sao không thể chỉ ẩn đáp án đúng bằng frontend?

### Câu trả lời ngắn gọn
Vì user có thể mở DevTools hoặc đọc network response. Nếu backend đã gửi `isCorrect`, dữ liệu coi như đã bị lộ.

---

## 62. Server-Side Quiz Scoring

### Giải thích ngắn gọn
Server-side scoring là việc backend tự tính điểm dựa trên đáp án gốc trong database, thay vì để frontend gửi điểm lên. Cách này bảo vệ tính toàn vẹn kết quả vì client luôn có thể bị sửa.

### Ví dụ trong project này
Khi student gọi `POST /api/v1/quizzes/{id}/submit`, backend lấy question/answer từ database, so sánh `answerId` đã chọn với answer có `isCorrect = true`, sau đó lưu score, correct count, wrong count và trạng thái passed vào `QuizAttempt`.

### Câu hỏi phỏng vấn liên quan
Vì sao frontend không nên tự tính điểm rồi gửi score lên backend?

### Câu trả lời ngắn gọn
Vì frontend chạy trên máy người dùng nên có thể bị chỉnh sửa. Backend phải là nơi quyết định điểm cuối cùng để tránh gian lận và giữ kết quả nhất quán.

---

## 63. Attempt Ownership Check

### Giải thích ngắn gọn
Attempt ownership check là bước xác minh phiên làm bài có thuộc về user hiện tại hay không. Đây là lớp bảo vệ bắt buộc cho submit/result API vì `attemptId` là dữ liệu có thể bị đoán hoặc thay đổi trên URL/request.

### Ví dụ trong project này
Khi submit quiz, `QuizLearningServiceImpl` lấy attempt theo `attemptId`, sau đó so sánh `attempt.getUser().getId()` với current user. Nếu không khớp, backend ném `QUIZ_ATTEMPT_FORBIDDEN`.

### Câu hỏi phỏng vấn liên quan
Vì sao kiểm tra JWT hợp lệ vẫn chưa đủ khi xem result của quiz attempt?

### Câu trả lời ngắn gọn
JWT chỉ chứng minh user đã đăng nhập. Backend vẫn phải kiểm tra resource-level permission để chắc chắn attempt đó thuộc về user hiện tại hoặc user có quyền admin.

---

## 64. Frontend API Service Layer

### Giải thích ngắn gọn
Frontend API Service Layer là lớp gom các lời gọi HTTP theo từng domain, ví dụ `quiz.service.js` cho toàn bộ API quiz. Component chỉ gọi hàm nghiệp vụ như `startQuiz()` hoặc `submitQuiz()` thay vì tự viết URL ở nhiều nơi.

### Ví dụ trong project này
`QuizTakingPage.vue`, `QuizResultPage.vue` và `StudentDashboardPage.vue` đều dùng `QuizService` để gọi backend quiz APIs.

### Câu hỏi phỏng vấn liên quan
Vì sao không nên gọi `api.get(...)` rải rác trực tiếp trong nhiều component?

### Câu trả lời ngắn gọn
Vì endpoint, payload và response contract sẽ bị lặp. Khi backend đổi API, ta phải sửa nhiều nơi và dễ tạo bug không nhất quán.

---

## 65. Attempt-Based Quiz UI State

### Giải thích ngắn gọn
Attempt-based UI state là cách frontend quản lý quiz theo phiên làm bài cụ thể. Trước khi start chỉ hiển thị thông tin quiz; sau khi backend trả `attemptId`, UI mới cho submit đáp án.

### Ví dụ trong project này
`QuizTakingPage.vue` lưu `attemptId` sau khi gọi `startQuiz()`. Khi submit, payload gửi cả `attemptId` và danh sách answer để backend kiểm tra phiên làm bài.

### Câu hỏi phỏng vấn liên quan
Vì sao không nên cho user submit quiz nếu chưa có `attemptId`?

### Câu trả lời ngắn gọn
Vì backend cần biết submit này thuộc phiên làm bài nào, của user nào và trạng thái attempt hiện tại có còn hợp lệ không.

---

## 66. Discoverability Gap trong User Flow

### Giải thích ngắn gọn
Discoverability gap là khoảng trống khi tính năng đã tồn tại nhưng user không có đường đi tự nhiên để tìm thấy nó. API và page có thể chạy đúng, nhưng flow vẫn chưa trọn nếu user phải biết URL hoặc ID thủ công.

### Ví dụ trong project này
Frontend đã có route `/student/quizzes/:quizId`, nhưng lesson learning page cần biết lesson hiện tại có quiz nào để hiển thị nút "Làm quiz". Vì vậy task tiếp theo nên bổ sung API discover quiz theo lesson/course hoặc tích hợp dữ liệu đó vào lesson response.

### Câu hỏi phỏng vấn liên quan
Vì sao tính năng đã code xong nhưng vẫn có thể chưa hoàn thiện về UX?

### Câu trả lời ngắn gọn
Vì user cần đường đi rõ ràng trong luồng sử dụng thật. Nếu chỉ truy cập được bằng URL thủ công, tính năng chưa thật sự được tích hợp vào sản phẩm.

---

## 67. Discovery API

### Giải thích ngắn gọn
Discovery API là endpoint giúp frontend tìm các tài nguyên liên quan trong một ngữ cảnh cụ thể, ví dụ lesson hiện tại có quiz nào. Nó thường trả metadata vừa đủ để render link, card hoặc CTA, không trả toàn bộ detail.

### Ví dụ trong project này
`GET /api/v1/lessons/{lessonId}/quizzes` trả danh sách quiz published gắn với lesson, gồm title, question count, latest attempt và remaining attempts để lesson page hiện nút "Làm quiz" hoặc "Xem kết quả".

### Câu hỏi phỏng vấn liên quan
Vì sao không bắt frontend tự đoán URL quiz bằng id hardcode?

### Câu trả lời ngắn gọn
Vì frontend không nên biết dữ liệu nghiệp vụ cố định. Backend phải cung cấp API discovery để UI hiển thị đúng theo dữ liệu thật.

---

## 68. Metadata Response Design

### Giải thích ngắn gọn
Metadata response design là cách thiết kế response chỉ gồm thông tin tóm tắt cần thiết cho một màn hình. Nó giúp API nhẹ hơn, bảo mật hơn và tránh frontend phụ thuộc vào dữ liệu chi tiết không cần dùng.

### Ví dụ trong project này
`QuizDiscoveryRes` trả `id`, `title`, `questionCount`, `latestAttemptId`, `latestScore`, `latestPassed`, `remainingAttempts`, nhưng không trả questions hoặc answers.

### Câu hỏi phỏng vấn liên quan
Vì sao discovery response không nên tái sử dụng luôn quiz detail response?

### Câu trả lời ngắn gọn
Vì detail response có thể quá nặng hoặc chứa dữ liệu không cần thiết. Discovery chỉ cần metadata để render entry point.

---

## 69. Contextual CTA

### Giải thích ngắn gọn
Contextual CTA là nút hành động xuất hiện đúng lúc, đúng nơi trong user flow. Thay vì bắt user tự tìm tính năng, giao diện đưa hành động phù hợp vào ngữ cảnh hiện tại.

### Ví dụ trong project này
Khi student đang học một lesson có quiz published, `LessonLearningPage.vue` hiển thị CTA "Làm bài", "Làm lại" hoặc "Xem kết quả" dựa trên discovery response.

### Câu hỏi phỏng vấn liên quan
Vì sao contextual CTA có thể quan trọng hơn việc chỉ tạo route mới?

### Câu trả lời ngắn gọn
Vì route mới chỉ làm tính năng tồn tại, còn contextual CTA giúp user thật sự tìm thấy và sử dụng tính năng trong luồng học tự nhiên.

---

## 70. Design Tokens - Token Thiết Kế

### Giải thích ngắn gọn
Design tokens là các giá trị thiết kế nguyên tử (atomic design values) được đặt tên và lưu trữ dưới dạng biến (CSS variables, JSON, YAML). Chúng đại diện cho các quyết định thiết kế cụ thể: màu sắc, font, spacing, border-radius, shadow... Thay vì hardcode giá trị hex khắp codebase, ta dùng token (`--stitch-primary`, `bg-stitch-card`) → thay đổi 1 nơi, cập nhật toàn bộ giao diện.

### Ví dụ trong project này
Stitch dùng `:root { --primary: #c1184a; }` rồi map vào Tailwind qua `@theme inline`. Dự án Vue chuyển đổi thành `--stitch-primary: #c1184a` trong `@layer base` và extend Tailwind config: `"stitch-primary": "var(--stitch-primary)"`. Prefix `stitch-` tránh xung đột với bảng màu Material Design đang dùng (`primary: #8f0020`).

### Câu hỏi phỏng vấn liên quan
Khi một dự án đã có hệ thống màu sắc, làm thế nào để thêm design tokens mới mà không phá vỡ giao diện hiện tại?

### Câu trả lời ngắn gọn
Dùng namespace/prefix riêng cho bộ tokens mới (ví dụ `stitch-*`), để hai hệ thống cùng tồn tại. Migrate từng page dần dần, chỉ xóa tokens cũ khi chắc chắn không còn reference nào.

---

## 71. Compound Component Pattern (Vue) - Mẫu Component Kết Hợp

### Giải thích ngắn gọn
Pattern chia một UI phức tạp thành nhiều sub-components có API riêng (props/slots) nhưng được thiết kế để dùng cùng nhau. Ví dụ: `<Card>` + slot nội dung, `<Modal>` + slot `footer`, `<EmptyState>` + slot `icon` và `action`. Mỗi sub-component tập trung một responsibility, nhưng khi kết hợp tạo thành UI phong phú.

### Ví dụ trong project này
`EmptyState.vue` có 3 slots: `icon` (custom SVG), default (mô tả), và `action` (nút CTA). Sử dụng:
```vue
<EmptyState title="Chưa có bài tập" description="Hãy tạo bài tập đầu tiên">
  <template #icon><MyIcon /></template>
  <template #action><Button>Tạo ngay</Button></template>
</EmptyState>
```

### Câu hỏi phỏng vấn liên quan
Slots trong Vue 3 khác gì với children trong React?

### Câu trả lời ngắn gọn
Vue slots hỗ trợ named slots natively (nhiều vùng nội dung trong 1 component), còn React dùng `children` cho 1 vùng và cần dùng props riêng hoặc compound pattern phức tạp hơn cho nhiều vùng.

---

## 72. Teleport/Portal Pattern - Kỹ Thuật Render Ngoài DOM Tree

### Giải thích ngắn gọn
`<Teleport>` (Vue 3) hay Portal (React) cho phép render một phần DOM vào vị trí khác trong DOM tree, thường là `<body>`, thay vì render tại vị trí khai báo trong component tree. Điều này giải quyết vấn đề stacking context (`z-index`, `overflow: hidden`) khiến overlay/modal bị cắt hoặc bị che.

### Ví dụ trong project này
`Modal.vue` dùng `<Teleport to="body">` để render backdrop và dialog ra ngoài `#app`. Nếu không dùng Teleport, Modal được render bên trong một container có `overflow: hidden` sẽ bị cắt mất phần nền mờ.

### Câu hỏi phỏng vấn liên quan
Tại sao Modal nên dùng Teleport thay vì render trực tiếp trong component cha?

### Câu trả lời ngắn gọn
Vì component cha có thể có `overflow: hidden`, `transform`, hoặc `z-index` tạo stacking context mới, khiến modal không hiển thị đúng trên toàn trang. Teleport đưa DOM node ra `<body>`, thoát khỏi mọi stacking context.


## 73. Component Extraction - Tách Component Dùng Chung

### Giải thích ngắn gọn
Component Extraction là kỹ thuật refactor đưa một đoạn UI lặp lại giữa nhiều file thành một component riêng biệt, import và tái sử dụng. Mục tiêu là Single Source of Truth cho phần UI đó: sửa 1 chỗ, cập nhật tất cả nơi sử dụng.

### Ví dụ trong project này
Navbar ban đầu được viết inline trong `MainLayout.vue` (~100 dòng), `StudentLayout.vue` có sidebar riêng (~90 dòng), mỗi nơi tự xử lý auth state, logout, navigation. Tách thành `Navbar.vue` component chung, cả 2 layouts chỉ cần `<Navbar />` → giảm duplicate, đảm bảo UX nhất quán.

### Câu hỏi phỏng vấn liên quan
Khi nào nên tách inline UI thành shared component, và khi nào nên giữ inline?

### Câu trả lời ngắn gọn
Tách khi: logic và visual lặp lại ≥ 2 nơi, hoặc component đủ phức tạp để có lifecycle/state riêng. Giữ inline khi: layout-specific (ví dụ AdminLayout sidebar chỉ dùng ở admin) và không có nhu cầu reuse.

---

## 74. Role-based UI Rendering - Hiển Thị Giao Diện Theo Vai Trò

### Giải thích ngắn gọn
Kỹ thuật thay đổi nội dung hiển thị trên UI dựa trên vai trò (role) của user đang đăng nhập. Khác với role-based access control (chặn ở router/backend), đây là việc ẩn/hiện các element trên cùng một component.

### Ví dụ trong project này
`Navbar.vue` sử dụng `computed` từ `auth.store` để xác định `isLoggedIn`, `isAdmin`, `dashboardRoute`. Guest thấy nút Đăng nhập/Đăng ký. Student thấy Dashboard + Profile + Avatar. Admin thấy link Admin panel. Tất cả trong cùng 1 component, không cần 3 navbar riêng.

### Câu hỏi phỏng vấn liên quan
Role-based UI rendering ở frontend có thay thế được backend authorization không?

### Câu trả lời ngắn gọn
Không. Frontend rendering chỉ là UX convenience (ẩn nút để user không nhầm lẫn). Backend authorization (router guards + API middleware) mới là security boundary thực sự. User có thể bypass frontend bằng DevTools hoặc gọi API trực tiếp.

---

## 75. Click Outside Pattern - Đóng Menu Khi Click Bên Ngoài

### Giải thích ngắn gọn
Pattern xử lý đóng dropdown/popover khi user click ra bên ngoài vùng menu. Thực hiện bằng cách đăng ký event listener trên `document` và kiểm tra `event.target` có nằm trong container menu không.

### Ví dụ trong project này
`Navbar.vue` dùng `@click.stop` trên mobile menu container để ngăn event bubbling, kết hợp `document.addEventListener('click', handleClickOutside)` để đóng menu khi click bất kỳ đâu ngoài menu. Phải cleanup listener trong `onUnmounted` để tránh memory leak.

### Câu hỏi phỏng vấn liên quan
Tại sao cần `@click.stop` trên menu container khi đã có `handleClickOutside`?

### Câu trả lời ngắn gọn
Vì click bên trong menu cũng bubble lên `document`, trigger `handleClickOutside` và đóng menu trước khi router-link kịp navigate. `.stop` ngăn event lan tới document, chỉ cho phép đóng khi click thật sự ở ngoài.

---

## 76. Vue Transition Component - Animation Enter/Leave

### Giải thích ngắn gọn
`<Transition>` là built-in component của Vue 3 cho phép thêm animation CSS khi element được insert (`v-if` true) hoặc remove (`v-if` false) khỏi DOM. Hỗ trợ class-based hoặc JavaScript hooks. Chỉ wrap được 1 child element trực tiếp.

### Ví dụ trong project này
Mobile menu trong `Navbar.vue` dùng `<Transition>` với `enter-from-class="opacity-0 -translate-y-4"` → `enter-to-class="opacity-100 translate-y-0"` tạo hiệu ứng slide-down mượt mà khi mở menu. Leave animation ngược lại: slide-up + fade-out.

### Câu hỏi phỏng vấn liên quan
`<Transition>` và `<TransitionGroup>` khác nhau thế nào?

### Câu trả lời ngắn gọn
`<Transition>` chỉ wrap 1 element, dùng cho toggle show/hide. `<TransitionGroup>` wrap danh sách (v-for), hỗ trợ thêm move transitions khi item thay đổi vị trí. TransitionGroup render wrapper element thật (mặc định `<span>`), Transition không render gì.


## 77. URL Query Sync - Đồng Bộ State Ứng Dụng Với URL

### Giải thích ngắn gọn
URL Query Sync là kỹ thuật lưu trạng thái giao diện (filters, search, page number, sort) vào URL query parameters (`?keyword=N5&page=2`). Khi user refresh trang, share link, hoặc bấm Back/Forward, ứng dụng khôi phục đúng trạng thái từ URL. Đây là yêu cầu cơ bản của bất kỳ trang có filter/search nào trong production.

### Ví dụ trong project này
`CourseListPage.vue` dùng `router.replace({ query })` để cập nhật URL mỗi khi user thay đổi filter, search, sort hoặc page. Kết hợp `watch(() => route.query)` để phản ứng khi user bấm Back/Forward. Khi mount, `syncFiltersFromUrl()` đọc `route.query` để khôi phục state → `fetchCourses()`.

### Câu hỏi phỏng vấn liên quan
Tại sao dùng `router.replace()` thay vì `router.push()` để sync filter?

### Câu trả lời ngắn gọn
`replace()` không tạo history entry mới, tránh user phải bấm Back nhiều lần khi chỉ đang thay đổi filter. `push()` sẽ tạo 1 entry cho mỗi lần click filter → Back button trở nên vô dụng vì chỉ quay lại filter trước đó thay vì trang trước đó.

---

## 78. Spring Data Pageable Sort Parameter - Tham Số Sắp Xếp

### Giải thích ngắn gọn
Spring Data Pageable nhận tham số `sort` từ query string dưới dạng `sort=field,direction` (ví dụ `sort=totalStudents,desc`). Spring tự parse string này thành `Sort` object. Có thể truyền nhiều sort: `sort=level,asc&sort=title,desc`. Frontend chỉ cần gửi đúng format, không cần tách field và direction thành 2 param riêng.

### Ví dụ trong project này
`CourseListPage.vue` có `<select>` với các option value như `id,desc`, `totalStudents,desc`, `originalPrice,asc`. Value này được truyền thẳng vào `params.sort` khi gọi `CourseService.getCourses(params)`. Axios serialize thành `?sort=totalStudents,desc`, Spring Boot Controller nhận qua `@PageableDefault Pageable pageable`.

### Câu hỏi phỏng vấn liên quan
Nếu frontend gửi sort field không tồn tại trong entity (ví dụ `sort=rating,desc` nhưng entity dùng `averageRating`), điều gì xảy ra?

### Câu trả lời ngắn gọn
Spring sẽ ném `PropertyReferenceException` hoặc bỏ qua tùy cấu hình. Đây là lý do frontend cần map đúng tên field trong entity (`averageRating,desc`) thay vì dùng tên UI (`rating`).

---

## 79. Static vs Dynamic Content Strategy - Chiến Lược Nội Dung Tĩnh và Động

### Giải thích ngắn gọn
Trên một landing page (HomePage), không phải mọi section đều cần gọi API. Marketing content (stats, features, testimonials) thường là static — thay đổi hiếm khi và không phụ thuộc user. Course listings là dynamic — phụ thuộc dữ liệu real-time từ database. Tách biệt giúp page render nhanh phần tĩnh trong khi API đang loading phần động.

### Ví dụ trong project này
`HomePage.vue` có 5 sections: Hero (static), Features (static), Courses Preview (dynamic — gọi API), Gamification (static), Testimonials (static), CTA (static). Chỉ section "Khóa học nổi bật" có loading spinner và error state. Các section khác render ngay lập tức từ `const` arrays.

### Câu hỏi phỏng vấn liên quan
Khi nào nên chuyển static content thành dynamic (từ CMS hoặc API)?

### Câu trả lời ngắn gọn
Khi: (1) content team cần thay đổi thường xuyên mà không deploy code, (2) content khác nhau theo locale/region, (3) cần A/B testing. Nếu content chỉ thay đổi mỗi vài tháng theo release cycle, giữ static trong code đơn giản và nhanh hơn.

---

## 80. Infinite Loop Prevention in Watchers - Tránh Vòng Lặp Vô Hạn Trong Watch

### Giải thích ngắn gọn
Khi component vừa `watch(route.query)` vừa gọi `router.replace({ query })` trong cùng logic, có nguy cơ tạo vòng lặp: replace → query thay đổi → watch fire → fetchCourses → replace lại → query thay đổi → ... Cần cơ chế so sánh (guard) để phát hiện query không thực sự thay đổi và skip re-fetch.

### Ví dụ trong project này
`CourseListPage.vue` dùng `JSON.stringify(newQuery) !== JSON.stringify(oldQuery)` trong callback của `watch(() => route.query, ...)` để chỉ chạy `syncFiltersFromUrl()` + `fetchCourses()` khi query thực sự khác. Nếu `router.replace()` set cùng query, watch fire nhưng stringify match → skip.

### Câu hỏi phỏng vấn liên quan
`JSON.stringify` compare có nhược điểm gì? Có cách nào tốt hơn không?

### Câu trả lời ngắn gọn
`JSON.stringify` phụ thuộc thứ tự key — `{a:1, b:2}` ≠ `{b:2, a:1}` dù logically equal. Trong Vue Router, `route.query` luôn giữ thứ tự key ổn định nên thực tế không gặp vấn đề. Cách tốt hơn: dùng flag boolean `isUpdatingUrl` set true trước `replace()`, check trong watch, reset sau.


## 81. Pinia Getter vs State trong Testing - Khác Biệt Khi Mock

### Giải thích ngắn gọn
Trong Pinia, `getters` là computed properties được tính từ `state`. Khi dùng `createTestingPinia({ initialState })`, chỉ có thể set **state** trực tiếp — getters sẽ tự động tính lại từ state đó. Nếu cố set getter trong `initialState`, nó sẽ bị ignore vì getter không phải state.

### Ví dụ trong project này
`useAuthStore` có getter `isAuthenticated: (state) => !!state.accessToken`. Trong test, phải set `initialState: { auth: { accessToken: 'token' } }` thay vì `{ auth: { isAuthenticated: true } }`. Getter `isAuthenticated` sẽ tự evaluate thành `true` khi `accessToken` có giá trị.

### Câu hỏi phỏng vấn liên quan
Làm sao override một Pinia getter trong unit test nếu cần giá trị khác với computed logic?

### Câu trả lời ngắn gọn
Sau khi mount component, lấy store instance bằng `useAuthStore()` và gán trực tiếp: `store.isAuthenticated = true` (trong testing mode, getters có thể writable). Hoặc set state sao cho getter trả về giá trị mong muốn — đây là cách an toàn hơn vì test luôn validate cả logic getter.

---

## 82. flushPromises vs nextTick vs setTimeout - Async Testing Strategies

### Giải thích ngắn gọn
Ba phương pháp đợi async operations trong Vue test utils:
- `await nextTick()`: Chỉ đợi DOM update cycle, không đợi Promises.
- `await flushPromises()`: Đợi tất cả pending Promises resolve (bao gồm chained `.then()` và `async/await`). Import từ `@vue/test-utils`.
- `await new Promise(r => setTimeout(r, 0))`: Đợi 1 macrotask, có thể miss promise chains phức tạp.

### Ví dụ trong project này
`CourseDetailPage.spec.js` ban đầu dùng `setTimeout(0)` nhưng fail vì `fetchCourseDetail()` chain thêm `checkEnrollmentStatus()` (2 API calls liên tiếp). Chuyển sang `flushPromises()` giải quyết vì nó drain toàn bộ microtask queue.

### Câu hỏi phỏng vấn liên quan
Khi nào dùng `nextTick()` và khi nào dùng `flushPromises()` trong Vue component test?

### Câu trả lời ngắn gọn
`nextTick()` khi chỉ cần đợi reactive DOM update (ví dụ sau `ref.value = x`, đợi template re-render). `flushPromises()` khi component có async operations (API calls, setTimeout callbacks) cần hoàn thành trước khi assert kết quả.

---

## 83. Duplicate Request Prevention Pattern - Ngăn Chặn Request Trùng Lặp

### Giải thích ngắn gọn
Khi user click nhanh nhiều lần vào nút submit/enroll, nhiều request giống nhau sẽ gửi đến server, gây lỗi hoặc data inconsistency. Pattern phổ biến: dùng boolean flag (`isEnrolling`) set `true` trước khi gửi request, bind vào `:disabled` attribute của button, và set `false` trong `finally` block.

### Ví dụ trong project này
`CourseDetailPage.vue` dùng `isEnrolling` ref: button có `:disabled="isEnrolling || !course.id"`, text thay đổi thành "Đang xử lý..." khi loading, và style chuyển sang `cursor-not-allowed` với màu muted. Server-side cũng có thể trả lỗi "đã ghi danh" — client xử lý bằng cách set `isEnrolled = true` khi nhận error message chứa keyword này.

### Câu hỏi phỏng vấn liên quan
Ngoài disable button, còn cách nào khác để ngăn duplicate request ở frontend?

### Câu trả lời ngắn gọn
(1) Debounce/throttle function wrapper, (2) AbortController cancel request trước đó, (3) Request deduplication middleware trong axios interceptor (cache pending requests by URL+params, return same Promise), (4) Optimistic UI update — disable interaction ngay lập tức trước khi request gửi.

---

## 84. Return URL Pattern - Redirect Sau Đăng Nhập

### Giải thích ngắn gọn
Khi guest user cố truy cập feature cần auth (enroll khóa học), app redirect họ đến login page kèm `redirect` query param chứa URL hiện tại. Sau khi đăng nhập thành công, LoginPage đọc `redirect` param và navigate user về đúng trang họ đang xem, thay vì luôn redirect về homepage.

### Ví dụ trong project này
`CourseDetailPage.vue`: `router.push({ path: '/login', query: { redirect: route.fullPath } })`. URL sẽ thành `/login?redirect=/courses/tieng-nhat-n5`. LoginPage sau khi auth thành công đọc `route.query.redirect` và dùng `router.replace(redirect)` để quay lại.

### Câu hỏi phỏng vấn liên quan
Tại sao dùng `route.fullPath` thay vì `route.path` cho redirect URL?

### Câu trả lời ngắn gọn
`fullPath` bao gồm cả query params và hash (`/courses?level=N5#reviews`), `path` chỉ có pathname (`/courses`). Dùng `fullPath` giữ nguyên context mà user đang xem (bộ lọc, anchor position), mang lại UX tốt hơn khi quay lại.



---

## 85. Axios Error Handling Pattern - Xử Lý Lỗi API & Network

### Giải thích ngắn gọn
Khi gọi API với thư viện như Axios, lỗi có thể đến từ 3 nguồn chính:
1. **Lỗi từ backend (HTTP errors):** Server trả về status code >= 400 (VD: 401 Unauthorized, 422 Unprocessable Entity). Axios map object này vào `error.response`.
2. **Lỗi mạng (Network errors):** Mất mạng, server chết không phản hồi, bị block CORS. Axios gửi request nhưng không nhận được response, map vào `error.request`.
3. **Lỗi cấu hình (Setup errors):** Lỗi syntax trong code trước khi gọi request, timeout interceptor.

### Ví dụ trong project này
Trong `LoginPage.vue` và `RegisterPage.vue`, catch block được tổ chức chuẩn xác:
```javascript
} catch (error) {
  if (error.response) {
    // 401, 409, 422 (xử lý logic API nghiệp vụ, field validation)
  } else if (error.request) {
    // Network Error, Server Unreachable
    formErrorMsg.value = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng."
  } else {
    formErrorMsg.value = "Đã xảy ra lỗi không xác định."
  }
}
```

### Câu hỏi phỏng vấn liên quan
Tại sao khi testing network error mock, ta phải define `request: {}` object trong mock error?

### Câu trả lời ngắn gọn
Bởi vì cấu trúc của network error trong Axios là một object có thuộc tính `request` nhưng không có `response`. Nếu mock object thiếu property `request`, logic `else if (error.request)` sẽ là falsy, dẫn code chạy sai vào nhánh fallback "lỗi không xác định".


---

## 86. Promise.all với Partial Failure Handling

### Giải thích ngắn gọn
Khi một trang cần gọi nhiều API cùng lúc, `Promise.all([...])` giúp chạy song song và đợi tất cả hoàn thành. Tuy nhiên, nếu BẤT KỲ 1 promise nào reject, toàn bộ `Promise.all` sẽ reject — trang sẽ hiển thị error dù các API khác đã trả về dữ liệu hợp lệ.

Giải pháp: Với các API không critical (phụ trợ, có thể thiếu mà trang vẫn hoạt động), thêm `.catch(() => null)` để biến rejection thành resolved value `null`. Code sau đó kiểm tra `if (result?.data?.code === 1000)` trước khi sử dụng.

### Ví dụ trong project này
`StudentDashboardPage.vue` gọi 3 API cùng lúc:
```javascript
const [progressRes, coursesRes, attemptsRes] = await Promise.all([
  StudentService.getDashboardProgress(),   // critical — throw nếu lỗi
  StudentService.getMyCourses(),           // critical — throw nếu lỗi
  QuizService.getMyQuizAttempts().catch(() => null)  // optional — swallow lỗi
])
```
Nếu Quiz API chết, dashboard vẫn render progress + courses. Widget quiz đơn giản ẩn đi.

### Câu hỏi phỏng vấn liên quan
`Promise.all` khác `Promise.allSettled` như thế nào? Khi nào nên dùng cái nào?

### Câu trả lời ngắn gọn
`Promise.all` fail-fast: reject ngay khi 1 promise reject. `Promise.allSettled` chờ TẤT CẢ promises settle (dù fulfilled hay rejected), trả về array `{ status, value/reason }`. Dùng `allSettled` khi cần kết quả của mọi promise bất kể thành công hay thất bại. Dùng `all` + `.catch()` khi muốn kiểm soát chính xác promise nào được phép fail.

---

## 87. Selective Migration Pattern - Lọc Feature Khi Migrate Từ Prototype

### Giải thích ngắn gọn
Khi migrate từ design prototype (Stitch/Figma) sang production code, không nên port 1:1 mọi thứ. Mỗi UI element cần kiểm tra: (1) Backend API đã có chưa? (2) Dữ liệu là thật hay mock? (3) Nếu chưa có API, có timeline rõ ràng không? Nếu câu trả lời là "chưa có" và "không rõ timeline", loại bỏ element đó khỏi production. Giữ lại prototype code làm reference cho phase sau.

### Ví dụ trong project này
Stitch `DashboardPage.tsx` có: XP system, Streak counter, Badges grid, Weekly Activity Chart, Daily Missions. Backend chỉ có `GET /users/me/progress` (3 fields) và `GET /users/me/courses`. Kết quả: Vue `StudentDashboardPage.vue` chỉ port stat cards + course list + quiz list. Loại bỏ 5 widgets mock data.

Stitch `ProfilePage.tsx` có 4 tabs: Info, Password, Orders, Notifications. Backend chỉ có API cho Info và Password. Kết quả: Vue `ProfilePage.vue` chỉ giữ 2 tabs.

### Câu hỏi phỏng vấn liên quan
Tại sao không giữ UI placeholder "Coming soon" cho các feature chưa có API?

### Câu trả lời ngắn gọn
Placeholder "Coming soon" chấp nhận được cho feature đơn giản (1 badge nhỏ). Nhưng với feature phức tạp (Badges grid, Weekly Chart, Notification Settings), placeholder tạo kỳ vọng sai cho user, tốn effort maintain code dead, và làm UI rối. Tốt hơn là loại bỏ hoàn toàn, giữ reference trong prototype, và thêm vào production khi API sẵn sàng.

---

### Vue Router Navigation Guards (onBeforeRouteLeave) - Bảo vệ luồng điều hướng

**Rating:** 🟢

**Định nghĩa:**
Các hook (hàm) của Vue Router cho phép chặn (intercept) hoặc can thiệp vào quá trình chuyển trang (navigation). Dùng phổ biến để kiểm tra quyền truy cập (auth guard) hoặc cảnh báo trước khi rời trang có dữ liệu chưa lưu.

**Ví dụ:**
```javascript
import { onBeforeRouteLeave } from 'vue-router'

onBeforeRouteLeave((to, from, next) => {
  if (isTakingQuiz.value && !isFinished.value) {
    const answer = window.confirm('Bạn đang làm bài kiểm tra. Nếu thoát bây giờ, kết quả sẽ bị mất. Bạn có chắc chắn muốn thoát?')
    if (answer) {
      next() // Cho phép chuyển trang
    } else {
      next(false) // Hủy bỏ thao tác chuyển trang
    }
  } else {
    next()
  }
})
```

**Lợi ích/Khi nào dùng:**

- `onBeforeRouteLeave`: Cảnh báo khi rời trang chứa form chưa submit, đang làm quiz, hoặc tiến trình upload chưa xong.
- `beforeEach`: Kiểm tra user đã login chưa trước khi cho phép vào các private route.

**Misconception hay gặp:**

- ❌ "Chỉ cần dùng window.onbeforeunload là đủ" - Sai, sự kiện này chỉ chạy khi đóng/load lại browser, không bắt được các thao tác chuyển trang nội bộ bằng Vue Router.


---

## 88. Lazy-Loading Tree Pattern - Tải Dữ Liệu Cây Theo Cấp

### Giải thích ngắn gọn
Khi UI hiển thị dữ liệu có cấu trúc cây nhiều cấp (Course → Section → Lesson → Resource), fetch toàn bộ cây 1 lần sẽ rất chậm nếu dữ liệu lớn. Thay vào đó, chỉ fetch cấp gốc khi trang load, và fetch cấp con khi user mở rộng (expand) node cha. Pattern này gọi là lazy-loading tree.

### Ví dụ trong project này
`AdminCourseStructurePage.vue` implement:
```javascript
// Khi trang load: chỉ fetch sections
const sectionRes = await AdminService.getSectionsByCourse(courseId)
sections.value = sectionRes.data.result.map(sec => ({
  ...sec,
  isExpanded: false,      // chưa mở
  isLoadingLessons: false, // chưa đang tải
  lessons: []              // chưa có dữ liệu con
}))

// Khi user click expand section:
const toggleSection = async (section) => {
  section.isExpanded = !section.isExpanded
  if (section.isExpanded && section.lessons.length === 0) {
    section.isLoadingLessons = true
    const res = await AdminService.getLessonsBySection(section.id)
    section.lessons = res.data.result || []
    section.isLoadingLessons = false
  }
}
```
Tương tự, mỗi lesson có `showResources`, `isLoadingResources`, `resources: []` — chỉ fetch khi user click "Tài liệu".

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng lazy-loading tree thay vì fetch toàn bộ cây 1 lần?

### Câu trả lời ngắn gọn
Dùng lazy-loading khi: (1) Cây có nhiều cấp và mỗi cấp có nhiều node, (2) User thường chỉ xem 1-2 nhánh, không cần toàn bộ, (3) API backend đã tách endpoint theo cấp. Fetch toàn bộ khi: cây nhỏ (< 50 nodes), user cần search/filter across nodes, hoặc cần render tree view đầy đủ ngay lập tức.

---

## 89. Centralized Admin Service Pattern - Gom API Call Vào 1 File

### Giải thích ngắn gọn
Thay vì tạo nhiều service file riêng biệt cho từng domain admin (course-admin.service, quiz-admin.service, user-admin.service), gom tất cả vào 1 file `admin.service.js`. Mỗi method tương ứng 1 endpoint API. File export 1 object `AdminService` duy nhất.

### Ví dụ trong project này
`admin.service.js` chứa ~30 methods chia theo comment section:
```javascript
export const AdminService = {
  // Dashboard
  async getDashboardStats() { return api.get('/v1/admin/dashboard') },
  // User Management
  async getUsers(params) { return api.get('/v1/admin/users', { params }) },
  async lockUser(id) { return api.put(`/v1/admin/users/${id}/lock`) },
  // Course Management
  async getCourses(params) { ... },
  async createCourse(payload) { ... },
  // Section & Lesson
  async getSectionsByCourse(courseId) { ... },
  async createLesson(sectionId, payload) { ... },
  // Quiz, Question, Answer...
}
```

### Câu hỏi phỏng vấn liên quan
Khi nào nên tách service file thay vì gom chung?

### Câu trả lời ngắn gọn
Gom chung khi: (1) Tất cả endpoint dùng cùng base path (`/v1/admin/...`), (2) Quy mô MVP dưới 50 endpoints, (3) Team nhỏ không cần phân chia ownership file. Tách khi: (1) File vượt 500 dòng, (2) Các domain có logic interceptor riêng (retry, cache), (3) Team lớn cần tránh merge conflict. Nguyên tắc: bắt đầu gom, tách khi có lý do cụ thể.

---

## 90. Data Isolation Pattern - Backend Bảo Vệ Dữ Liệu Theo Ownership

### Giải thích ngắn gọn
Trong hệ thống multi-tenant hoặc multi-role, Data Isolation đảm bảo user chỉ thao tác được trên dữ liệu mình sở hữu. ADMIN/SUPER_ADMIN có thể truy cập tất cả, nhưng TEACHER chỉ có quyền trên course do mình tạo. Logic này **phải** nằm ở backend (server-side enforcement), không dựa vào frontend hide/show UI.

### Ví dụ trong project này
`LessonAdminServiceImpl.java` implement `checkDataIsolation()`:
```java
private void checkDataIsolation(Course course) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String currentUserEmail = auth.getName();
    
    boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") 
                    || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    
    if (!isAdminOrSuperAdmin) {
        if (!course.getTeacher().getEmail().equals(currentUserEmail)) {
            throw new AppException(ErrorCode.DATA_ISOLATION_FORBIDDEN);
        }
    }
}
```
Frontend nhận HTTP 403 với error code `DATA_ISOLATION_FORBIDDEN` → hiển thị thông báo "Bạn không có quyền truy cập dữ liệu này."

### Câu hỏi phỏng vấn liên quan
Tại sao Data Isolation phải nằm ở backend thay vì chỉ ẩn UI ở frontend?

### Câu trả lời ngắn gọn
Frontend có thể bị bypass bằng DevTools, Postman, hoặc script tự động. Nếu backend không kiểm tra ownership, kẻ tấn công chỉ cần biết endpoint + ID là thao tác được trên dữ liệu của người khác. Backend check là lớp bảo vệ bắt buộc; frontend hide UI chỉ là UX convenience, không phải security measure.

---

## 91. Status Workflow Pattern (Publish/Hide/Archive) - Quản Lý Vòng Đời Entity

### Giải thích ngắn gọn
Nhiều entity trong CMS (Course, Quiz, Lesson, Section) có lifecycle status: `DRAFT → PUBLISHED → HIDDEN → ARCHIVED`. Mỗi transition được bảo vệ bởi 1 API endpoint riêng thay vì cho phép update status trực tiếp trong PUT request. Điều này đảm bảo backend validate điều kiện trước khi chuyển trạng thái (VD: quiz phải có ít nhất 1 câu hỏi mới được publish).

### Ví dụ trong project này
```javascript
// Frontend gọi endpoint riêng cho từng action:
await AdminService.publishQuiz(quiz.id)  // PUT /admin/quizzes/{id}/publish
await AdminService.hideQuiz(quiz.id)     // PUT /admin/quizzes/{id}/hide
await AdminService.deleteQuiz(quiz.id)   // DELETE /admin/quizzes/{id} → ARCHIVED

// Backend validation khi publish:
// - Quiz phải có >= 1 question
// - Tất cả question phải có >= 1 answer
// - Phải có ít nhất 1 correct answer
// Nếu không đạt → throw AppException → frontend hiển thị error
```

### Câu hỏi phỏng vấn liên quan
Tại sao dùng endpoint riêng (`PUT /publish`) thay vì cho phép update status trong `PUT /quizzes/{id}` body?

### Câu trả lời ngắn gọn
(1) Tách endpoint cho phép backend validate business rules riêng cho từng transition (publish cần check questions, hide cần check active enrollments). (2) Rõ ràng về intent: `PUT /publish` chỉ làm 1 việc, dễ audit log. (3) Tránh race condition: nếu 2 admin cùng PUT update quiz, 1 người gửi `status: PUBLISHED` xen lẫn với sửa title → kết quả khó đoán. Endpoint riêng đảm bảo atomicity.


---

## 92. Frontend Regression Testing - Kiểm Thử Hồi Quy Giao Diện

### Giải thích ngắn gọn
Regression testing là quá trình kiểm tra lại các tính năng hiện có sau khi thay đổi code (refactor, migrate UI, thêm feature mới) để đảm bảo không có gì bị hỏng. Trong context frontend Vue, regression tests sử dụng `@vue/test-utils` + Vitest để mount component, mock services, và assert DOM output.

### Ví dụ trong project này
```javascript
// Pattern: Test-proof selector — tìm button qua text thay vì class
const startBtn = wrapper.findAll('button')
  .find(b => b.text().includes('Bắt đầu làm bài'))
await startBtn.trigger('click')

// Pattern: Stub child component để isolate test
const wrapper = mount(LessonLearningPage, {
  global: {
    stubs: {
      'router-link': true,
      'LearningCurriculumSidebar': { template: '<div />' }
    }
  }
})

// Pattern: flushPromises thay vì setTimeout
import { flushPromises } from '@vue/test-utils'
await flushPromises() // ổn định hơn setTimeout(0)
```

### Câu hỏi phỏng vấn liên quan
Khi migrate UI (thay đổi CSS class, restructure DOM), tại sao test lại bị gãy? Làm sao viết test bền vững hơn?

### Câu trả lời ngắn gọn
Test dựa vào CSS class (`.btn-complete`, `.lesson-title`) bị gãy vì class thay đổi khi redesign. Test bền vững hơn nên dựa vào: (1) Text content (`wrapper.text().toContain()`), (2) Semantic HTML (`wrapper.find('h1')`), (3) ARIA attributes (`[aria-label="..."]`), (4) `data-testid` attributes (nếu team đồng ý convention). Ưu tiên test behavior (user nhấn nút → API được gọi) hơn test structure (DOM có class X).

---

## 93. Accessibility Testing trong Vue - Kiểm Thử Khả Năng Tiếp Cận

### Giải thích ngắn gọn
Accessibility (a11y) testing đảm bảo web app sử dụng được bởi mọi người, kể cả người dùng screen reader, keyboard-only, hoặc có vấn đề về thị lực. Trong test, kiểm tra: `aria-label` cho interactive elements, `alt` text cho images, `focus-visible` styling cho keyboard navigation.

### Ví dụ trong project này
```javascript
// Kiểm tra pagination buttons có aria-label
const prevBtn = wrapper.find('button[aria-label="Previous page"]')
expect(prevBtn.exists()).toBe(true)

// Kiểm tra image có alt text
const img = wrapper.find('img')
expect(img.attributes('alt')).toBe('Test')

// Kiểm tra keyboard focus styling
const courseLink = wrapper.find('.group')
expect(courseLink.classes()).toContain('focus-visible:ring-2')
```

### Câu hỏi phỏng vấn liên quan
Accessibility testing ở mức nào là đủ cho một web app MVP?

### Câu trả lời ngắn gọn
Ở mức MVP, tối thiểu cần: (1) `alt` text cho mọi `<img>`, (2) `aria-label` cho icon-only buttons, (3) `focus-visible` styling cho keyboard users, (4) Semantic HTML (h1 > h2 > h3, nav, main, aside), (5) Sufficient color contrast (WCAG AA). Không cần full WCAG AAA compliance nhưng baseline a11y là non-negotiable vì ảnh hưởng SEO và legal compliance ở nhiều quốc gia.

---

## 94. Hotlinking vs Self-Hosting Static Assets - Lưu Trữ Tài Nguyên Tĩnh

### Giải thích ngắn gọn
Hotlinking là việc dùng trực tiếp URL ảnh/font/tài nguyên từ server bên thứ ba (ví dụ `https://images.unsplash.com/...`) trong code production. Self-hosting là tải về, tối ưu, và phục vụ tài nguyên từ chính server hoặc CDN của project. Hotlinking gây rủi ro: ảnh bị xóa/thay đổi, thêm DNS lookup, vi phạm ToS của nguồn, không cache được ở build time.

### Ví dụ trong project này
```vue
<!-- ❌ Trước (Hotlinking Unsplash) — rủi ro production -->
<div style="background-image: url('https://images.unsplash.com/photo-1598957232485-fab51e0ed7e8?w=1600&h=900&fit=crop&auto=format')">

<!-- ✅ Sau (Self-hosted WebP) — ổn định, nhanh hơn, cache được -->
<script setup>
import heroBg from '@/assets/hero-bg.webp'
</script>
<div :style="{ backgroundImage: `url(${heroBg})` }">
```

### Câu hỏi phỏng vấn liên quan
Khi nào nên hotlink ảnh từ CDN bên ngoài vs self-host? Trade-off là gì?

### Câu trả lời ngắn gọn
Hotlink chỉ phù hợp khi: (1) nguồn có SLA đảm bảo uptime (ví dụ Google Fonts CDN), (2) ảnh thay đổi thường xuyên và cần luôn lấy bản mới nhất, (3) license cho phép. Self-host phù hợp khi: (1) ảnh tĩnh không thay đổi (hero, logo), (2) cần kiểm soát format/quality/kích thước, (3) cần cache ở build time (Vite hash asset → long-term caching), (4) cần hoạt động offline/trong mạng nội bộ. Trong production app, self-host là mặc định an toàn.

---

## 95. WebP Image Format & Responsive Image Strategy

### Giải thích ngắn gọn
WebP là định dạng ảnh do Google phát triển, hỗ trợ cả lossy và lossless compression. So với PNG, WebP thường nhỏ hơn 25-35%. So với JPEG, nhỏ hơn 25-34% ở cùng chất lượng (theo Google). Kết hợp với `loading="lazy"` và `decoding="async"` trên thẻ `<img>`, trang web giảm đáng kể thời gian tải ban đầu.

### Ví dụ trong project này
```bash
# Chuyển đổi logo từ PNG sang WebP (giảm 99% dung lượng)
# logo.png:  712 KB (1024×1024) → logo.webp: 5.3 KB (128×128)

# Lazy loading cho ảnh below-the-fold (dynamic content từ API)
<img :src="c.thumbnailUrl" :alt="c.title"
     loading="lazy" decoding="async"
     class="w-full h-full object-cover" />

# KHÔNG lazy load ảnh above-the-fold (hero, logo trên navbar)
<img src="@/assets/logo.webp" alt="BrianJP Logo" />
```

### Câu hỏi phỏng vấn liên quan
`loading="lazy"` hoạt động như thế nào? Khi nào KHÔNG nên dùng?

### Câu trả lời ngắn gọn
`loading="lazy"` ra lệnh cho browser trì hoãn tải ảnh cho đến khi ảnh sắp xuất hiện trong viewport (dựa trên Intersection Observer nội bộ). KHÔNG nên dùng cho ảnh above-the-fold (hero banner, logo navbar, LCP element) vì nó trì hoãn Largest Contentful Paint. Browser sẽ không tải ảnh lazy cho đến khi layout được tính toán xong, gây delay thêm ~200-500ms cho critical images.

---

## 96. Font Loading Strategy (`display=swap` vs `display=block`)

### Giải thích ngắn gọn
Khi load web font từ Google Fonts, tham số `display` kiểm soát hành vi hiển thị text trong lúc font đang tải. `display=block` gây FOIT (Flash of Invisible Text) — text biến mất hoàn toàn cho đến khi font tải xong, có thể kéo dài 3s trên mạng chậm. `display=swap` gây FOUT (Flash of Unstyled Text) — text hiển thị ngay bằng fallback font, rồi swap sang web font khi tải xong. FOUT tốt hơn cho UX vì user đọc được nội dung ngay.

### Ví dụ trong project này
```html
<!-- ❌ display=block: Icon biến mất 1-3s trên mạng chậm -->
<link href="...Material+Symbols+Outlined...&display=block" rel="stylesheet">

<!-- ✅ display=swap: Text fallback hiện ngay, icon swap vào khi sẵn -->
<link href="...Material+Symbols+Outlined...&display=swap" rel="stylesheet">
```

### Câu hỏi phỏng vấn liên quan
Giải thích sự khác biệt giữa `font-display: swap`, `block`, `fallback`, `optional`. Khi nào chọn cái nào?

### Câu trả lời ngắn gọn
`swap`: 0ms block period, infinite swap period — text hiện ngay bằng fallback, swap khi font ready. Tốt cho body text. `block`: 3s block period — text ẩn 3s, nếu font chưa tải xong thì fallback. Tốt cho icon fonts (nhưng UX kém). `fallback`: 100ms block, 3s swap — compromise giữa swap và block. `optional`: 100ms block, 0s swap — browser tự quyết định dùng font hay không dựa trên network. Tốt cho non-critical fonts khi ưu tiên performance tuyệt đối.

---

## 97. Spaced Repetition System (SRS) — Thuật toán lặp lại ngắt quãng

### Giải thích ngắn gọn
SRS là kỹ thuật quản lý bộ nhớ dài hạn bằng cách ôn tập thẻ (flashcard) đúng lúc sắp quên, thay vì ôn đều đặn mỗi ngày. Mỗi lần review, user đánh giá độ khó (EASY/MEDIUM/HARD), thuật toán sẽ tính khoảng thời gian (`interval_days`) và hệ số thuận lợi (`ease_factor`) để quyết định lần ôn tiếp theo. Thẻ dễ → interval tăng nhanh (3 → 7 → 18 ngày). Thẻ khó → interval về 0 (ôn lại ngay).

### Ví dụ trong project này
```java
// FlashcardServiceImpl.java — SRS logic
if (difficulty.equals("HARD")) {
    progress.setEaseFactor(Math.max(1.3, progress.getEaseFactor() - 0.2));
    progress.setIntervalDays(0); // Quay về ôn ngay
} else if (difficulty.equals("MEDIUM")) {
    if (progress.getIntervalDays() == 0) progress.setIntervalDays(1);
    else progress.setIntervalDays((int)(progress.getIntervalDays() * 1.2));
} else if (difficulty.equals("EASY")) {
    progress.setEaseFactor(progress.getEaseFactor() + 0.15);
    if (progress.getIntervalDays() == 0) progress.setIntervalDays(3);
    else progress.setIntervalDays((int)(progress.getIntervalDays() * progress.getEaseFactor()));
}
```

Bảng FlashcardProgress lưu trạng thái SRS riêng biệt cho từng cặp `(user_id, flashcard_id)`:
```java
@UniqueConstraint(columnNames = {"user_id", "flashcard_id"})
private Double easeFactor = 2.5;   // Hệ số dễ, giảm khi HARD, tăng khi EASY
private Integer intervalDays = 0;  // Khoảng ôn (ngày), 0 = ôn lại ngay
private LocalDateTime nextReviewTime; // Thời điểm ôn tiếp theo (UTC)
```

### Câu hỏi phỏng vấn liên quan
Giải thích thuật toán SRS cơ bản. Tại sao `ease_factor` có lower bound 1.3?

### Câu trả lời ngắn gọn
SRS hoạt động bằng cách nhân `interval_days` với `ease_factor` sau mỗi lần review thành công. Nếu user nhớ dễ, interval tăng theo cấp số nhân (giảm lượng ôn tập). Nếu user quên (HARD), interval reset về 0 để ôn lại ngay. `ease_factor` có lower bound 1.3 (thay vì 1.0 hoặc thấp hơn) để đảm bảo interval luôn tăng ít nhất 30% mỗi lần — nếu không, thẻ sẽ bị "kẹt" ở cùng interval mãi mãi. Đây là cải tiến so với thuật toán SM-2 gốc của Piotr Woźniak.

---

## 98. Idempotency Key — Ngăn chặn xử lý trùng lặp trong API

### Giải thích ngắn gọn
Idempotency key là một giá trị duy nhất do client tạo ra, gắn kèm với mỗi request mutate (POST/PUT). Server lưu key này lại và nếu nhận được request trùng key, server sẽ bỏ qua thay vì xử lý lại. Kỹ thuật này ngăn chặn tình huống: user click "Submit" 2 lần, mạng giật retry, hoặc frontend gọi API trùng.

### Ví dụ trong project này
```java
// Server-side: FlashcardServiceImpl.java
public void reviewCard(ReviewFlashcardReq req) {
    // Check trước khi xử lý — nếu key đã tồn tại, return ngay
    if (reviewLogRepository.findByIdempotencyKey(req.getIdempotencyKey()).isPresent()) {
        return; // Already processed, skip silently
    }
    // ... xử lý logic SRS ...
    FlashcardReviewLog log = FlashcardReviewLog.builder()
            .idempotencyKey(req.getIdempotencyKey()) // Lưu key vào DB
            .build();
    reviewLogRepository.save(log);
}
```

```javascript
// Client-side: FlashcardPage.vue
const generateIdempotencyKey = (cardId) => {
  return `review_${cardId}_${new Date().getTime()}_${Math.random().toString(36).substring(7)}`;
};
```

### Câu hỏi phỏng vấn liên quan
Tại sao cần idempotency key? Tại sao không chỉ dùng `unique(user_id, flashcard_id)` để ngăn duplicate?

### Câu trả lời ngắn gọn
`unique(user_id, flashcard_id)` chỉ ngăn được trường hợp review thẻ chỉ 1 lần duy nhất. Nhưng thực tế, cùng 1 thẻ sẽ được review nhiều lần (SRS yêu cầu ôn lại theo interval). Nên cần 1 key riêng biệt cho mỗi lần review để phân biệt "review lần thứ 3" vs "request retry của review lần thứ 3". Idempotency key sinh ra ở client đảm bảo mỗi user action (click button) chỉ tạo 1 key duy nhất, kể cả retry.

---

## 99. Tính toán thời gian due theo timezone người dùng

### Giải thích ngắn gọn
Server lưu tất cả thời gian dưới dạng UTC. Khi tính `nextReviewTime`, server nhận timezone string từ client (ví dụ `"Asia/Ho_Chi_Minh"`), chuyển thời điểm hiện tại sang local time, cộng interval rồi set đầu ngày (00:00) theo local, sau đó convert ngược về UTC để lưu. Điều này đảm bảo: thẻ "due ngày mai" luôn hiện ra vào 00:00 sáng theo giờ thực tế của user, không phải 00:00 UTC (tức 07:00 sáng giờ Việt Nam).

### Ví dụ trong project này
```java
// FlashcardServiceImpl.java — timezone-aware next review
ZoneId zoneId = ZoneId.of(req.getTimezone()); // e.g., "Asia/Ho_Chi_Minh"

LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
// Chuyển UTC → local
LocalDateTime localNow = nowUtc.atZone(ZoneOffset.UTC)
        .withZoneSameInstant(zoneId).toLocalDateTime();
// Cộng interval, đặt về đầu ngày local
LocalDateTime localNextDue = localNow.plusDays(progress.getIntervalDays())
        .withHour(0).withMinute(0).withSecond(0);
// Chuyển local → UTC để lưu
LocalDateTime nextDueUtc = localNextDue.atZone(zoneId)
        .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
progress.setNextReviewTime(nextDueUtc);
```

### Câu hỏi phỏng vấn liên quan
Tại sao lưu UTC trong database thay vì local time? Giải thích luồng convert timezone.

### Câu trả lời ngắn gọn
Lưu UTC vì: (1) Tránh nhập nhằng khi user thay đổi timezone hoặc DST (Daylight Saving Time) chuyển mùa. (2) So sánh thời gian giữa các user ở timezone khác nhau chỉ cần 1 phép so sánh. (3) Database không cần biết timezone — chỉ lưu giá trị absolute. Luồng convert: Client gửi timezone string → Server tính "đầu ngày local" = `localNow + interval → .withHour(0)` → Convert ngược về UTC bằng `atZone(local).withZoneSameInstant(UTC)`. Khi query thẻ due, chỉ cần `WHERE next_review_time <= NOW()` (UTC).

---

## 100. Đếm "due cards" = total − notDue (phương pháp đếm bù)

### Giải thích ngắn gọn
Khi tính số thẻ cần ôn, ta KHÔNG đếm trực tiếp "thẻ nào due" vì phải xử lý 2 loại khác nhau: (1) thẻ mới chưa có progress, (2) thẻ cũ quá hạn. Thay vào đó, đếm bù: `dueCards = totalCards − notDueCount`. Trong đó `notDueCount` chỉ đếm thẻ CÓ progress VÀ `next_review_time > now` (chưa đến hạn). Mọi thứ còn lại (chưa từng học + quá hạn) tự động nằm trong `dueCards`.

### Ví dụ trong project này
```java
// FlashcardServiceImpl.java
Long totalCards = flashcardRepository.countByDeckId(deck.getId());
Long notDueCount = progressRepository.countNotDueCardsByDeckId(
    user.getId(), deck.getId(), currentUtc
);
Long dueCards = totalCards - notDueCount;
// Math.max(0, dueCards) — phòng trường hợp data inconsistency
```

### Câu hỏi phỏng vấn liên quan
Tại sao dùng phương pháp "đếm bù" thay vì query trực tiếp danh sách thẻ due?

### Câu trả lời ngắn gọn
Đếm trực tiếp yêu cầu LEFT JOIN giữa `flashcards` và `flashcard_progress` để tìm cả thẻ chưa có record (IS NULL) lẫn thẻ quá hạn (< now) — query phức tạp hơn. Đếm bù chỉ cần 2 query đơn giản: `COUNT(*)` tổng và `COUNT(*)` có điều kiện `next_review_time > now`. Ngoài ra, `Math.max(0, dueCards)` xử lý edge case khi data progress tồn tại nhưng flashcard bị xóa (orphan records).

---

## 101. Server-Authoritative Scoring — Chống gian lận điểm số

### Giải thích ngắn gọn
Trong hệ thống gamification (XP, điểm số, level), điểm PHẢI được tính và lưu hoàn toàn phía server. Client không bao giờ được phép gửi request kiểu `POST /api/scores { "xp": 1000 }` — vì bất kỳ ai biết dùng cURL/Postman đều có thể gửi request giả để cộng vô hạn điểm. Thay vào đó, server tự cộng điểm bên trong business logic khi hoàn thành hành động thực sự (lesson completed, quiz passed).

### Ví dụ trong project này
```java
// ✅ Server-Authoritative: Cộng XP bên trong service logic (client không biết)
// LearningServiceImpl.java
private void upsertLessonProgress(User user, Lesson lesson, ...) {
    // ... lưu progress ...
    if (Boolean.TRUE.equals(isCompleted)) {
        scoringService.addXp(user.getId(), "LESSON_COMPLETED", 100, lesson.getId(),
            "Hoàn thành bài học: " + lesson.getTitle());
    }
}

// QuizLearningServiceImpl.java
if (Boolean.TRUE.equals(attempt.getPassed())) {
    scoringService.addXp(user.getId(), "QUIZ_PASSED", 200, quiz.getId(),
        "Vượt qua bài kiểm tra: " + quiz.getTitle());
}

// FlashcardServiceImpl.java
scoringService.addXp(user.getId(), "FLASHCARD_REVIEWED", 10, null,
    "Ôn tập thẻ flashcard");
```

```java
// ❌ Client-Authoritative: TUYỆT ĐỐI KHÔNG LÀM NHƯ NÀY
@PostMapping("/api/scores")
public void addScore(@RequestBody ScoreReq req) {
    scoreService.addXp(getCurrentUser().getId(), req.getXp()); // User tự gửi XP!!!
}
```

### Câu hỏi phỏng vấn liên quan
Sự khác biệt giữa server-authoritative và client-authoritative trong game/gamification? Khi nào cần chọn cái nào?

### Câu trả lời ngắn gọn
**Server-authoritative**: Mọi quyết định về trạng thái (điểm số, tiến trình, inventory) đều do server tính toán và xác thực. Client chỉ gửi "hành động" (hoàn thành bài học, trả lời quiz), server tự quyết định kết quả. Ưu điểm: chống cheat hoàn toàn. Nhược điểm: latency cao hơn. **Client-authoritative**: Client tự tính toán trạng thái rồi gửi lên server lưu. Ưu điểm: nhanh, responsive. Nhược điểm: dễ bị hack/cheat. Trong web app có scoreboard/leaderboard, PHẢI dùng server-authoritative. Client-authoritative chỉ phù hợp cho single-player game offline hoặc những giá trị không ảnh hưởng cạnh tranh.

---

## 102. Pre-Aggregated Scores vs Real-Time Aggregation

### Giải thích ngắn gọn
Khi cần hiển thị bảng xếp hạng, có 2 cách tính tổng điểm: (1) Real-time: `SELECT user_id, SUM(xp_amount) FROM learning_activities GROUP BY user_id ORDER BY SUM(xp_amount) DESC` — tính lại mỗi lần query. (2) Pre-aggregated: Lưu sẵn tổng vào bảng riêng `user_scores`, mỗi lần có XP mới chỉ cần `UPDATE total_xp = total_xp + newXp`. Query leaderboard chỉ cần `SELECT * FROM user_scores ORDER BY total_xp DESC`.

### Ví dụ trong project này
```java
// ScoringService.java — Pre-aggregate vào 3 period cùng lúc
updateUserScore(user, "ALL_TIME", "ALL", xpAmount);      // Tổng mọi thời điểm
updateUserScore(user, "WEEKLY", currentWeek, xpAmount);   // Tổng tuần này: "2026-W41"
updateUserScore(user, "MONTHLY", currentMonth, xpAmount); // Tổng tháng này: "2026-10"

private void updateUserScore(User user, String periodType, String periodValue, Integer xp) {
    UserScore score = scoreRepository
        .findByUserIdAndPeriodTypeAndPeriodValue(user.getId(), periodType, periodValue)
        .orElse(UserScore.builder()
            .user(user).periodType(periodType).periodValue(periodValue).totalXp(0)
            .build());
    score.setTotalXp(score.getTotalXp() + xp); // Cộng dồn, không tính lại từ đầu
    scoreRepository.save(score);
}
```

```sql
-- Bảng user_scores: index tối ưu cho ORDER BY
CREATE INDEX idx_scores_ranking ON user_scores(period_type, period_value, total_xp DESC);
-- Query leaderboard chỉ cần scan index, không cần GROUP BY
```

### Câu hỏi phỏng vấn liên quan
So sánh pre-aggregation vs real-time aggregation cho leaderboard. Trade-off là gì?

### Câu trả lời ngắn gọn
**Real-time aggregation** (`SUM GROUP BY`): Luôn chính xác, nhưng query chậm dần theo số lượng activities — O(N) per query với N = tổng activities. Nếu có 1 triệu activities, mỗi lần load bảng xếp hạng phải scan 1 triệu rows. **Pre-aggregation**: Write thêm 1 bước (cộng dồn khi insert), nhưng read cực nhanh — query chỉ scan bảng `user_scores` với ~N users thay vì ~M activities. Trade-off: (1) Nếu cần recalculate (fix bug điểm), phải chạy batch job tính lại từ `learning_activities`. (2) Code write phức tạp hơn (phải maintain cả activity log + score table). Trong thực tế, mọi leaderboard ở quy mô > 1000 users đều dùng pre-aggregation.

---

## 103. ISO-8601 Week Numbering (`IsoFields.WEEK_OF_WEEK_BASED_YEAR`)

### Giải thích ngắn gọn
Java `IsoFields.WEEK_OF_WEEK_BASED_YEAR` tuân theo chuẩn ISO-8601: tuần bắt đầu từ thứ Hai, tuần 1 là tuần đầu tiên chứa ít nhất 4 ngày của năm mới. Điều này khác với `Calendar.WEEK_OF_YEAR` (Java cũ) bắt đầu từ Chủ nhật (kiểu Mỹ). Ví dụ: nếu 1/1/2027 là thứ Sáu, thì tuần ISO là W53 của 2026, nhưng Calendar.WEEK_OF_YEAR sẽ cho W1 của 2027.

### Ví dụ trong project này
```java
// ScoringService.java
LocalDate now = LocalDate.now(ZoneOffset.UTC);
String currentWeek = now.getYear() + "-W" + now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
// Kết quả: "2026-W41" (tuần 41 của năm 2026)
```

### Câu hỏi phỏng vấn liên quan
ISO week numbering khác gì so với WEEK_OF_YEAR? Tại sao quan trọng khi làm leaderboard theo tuần?

### Câu trả lời ngắn gọn
Nếu dùng `Calendar.WEEK_OF_YEAR` (locale-dependent), cùng một ngày có thể thuộc tuần khác nhau tùy theo locale setting (Mỹ: tuần bắt đầu Chủ nhật, châu Âu: thứ Hai). Trong leaderboard đa quốc gia, 2 user ở US và VN sẽ bị tính vào tuần khác nhau cho cùng 1 ngày → ranking sai. ISO-8601 chuẩn hóa toàn cầu: tuần luôn bắt đầu thứ Hai, tránh nhập nhằng. Ngoài ra, ISO week có thể thuộc năm trước/sau ở ranh giới (W52/W53 vs W01), cần dùng `getWeekBasedYear()` thay vì `getYear()` ở biên năm nếu muốn chính xác tuyệt đối.

---

## 104. Streak Tracking — Logic tính chuỗi ngày liên tục

### Giải thích ngắn gọn
Streak (chuỗi ngày liên tục) là cơ chế gamification khuyến khích user học mỗi ngày. Logic: nếu ngày hoạt động cuối = hôm qua → streak + 1. Nếu ngày hoạt động cuối < hôm qua → streak reset = 1. Nếu ngày hoạt động cuối = hôm nay → giữ nguyên (đã tính). Phải dùng `LocalDate` (không có time), so sánh theo ngày UTC để tránh timezone issues.

### Ví dụ trong project này
```java
// ScoringService.java
LocalDate now = LocalDate.now(ZoneOffset.UTC);

if (user.getLastActivityDate() == null || user.getLastActivityDate().isBefore(now)) {
    // Chưa hoạt động hôm nay
    if (user.getLastActivityDate() != null
        && user.getLastActivityDate().plusDays(1).equals(now)) {
        // Hôm qua có hoạt động → tiếp tục streak
        user.setCurrentStreak(user.getCurrentStreak() + 1);
    } else if (user.getLastActivityDate() == null
        || user.getLastActivityDate().isBefore(now.minusDays(1))) {
        // Đã nghỉ > 1 ngày → reset
        user.setCurrentStreak(1);
    }
    user.setLastActivityDate(now);
}
// Nếu lastActivityDate == now → không làm gì (hoạt động nhiều lần trong ngày)
```

### Câu hỏi phỏng vấn liên quan
Streak logic có edge case nào cần xử lý?

### Câu trả lời ngắn gọn
(1) **Timezone**: User ở UTC+7 hoạt động lúc 23:30 local (= 16:30 UTC), rồi 00:30 local hôm sau (= 17:30 UTC) — cùng ngày UTC nhưng 2 ngày local. Nếu streak dựa trên UTC thì mất streak. Giải pháp: dùng timezone của user để tính ngày, hoặc chấp nhận UTC (đơn giản hơn). (2) **DST**: Ngày chuyển giờ có thể dài 23 hoặc 25 giờ. `plusDays(1)` đúng vì nó cộng calendar day, không cộng 24h. (3) **Server downtime**: Nếu server chết 1 ngày, user không thể hoạt động nhưng bị mất streak. Giải pháp: "streak freeze" cho phép bỏ qua 1 ngày.

---

## 105. Privacy Filter trong Leaderboard (is_private_leaderboard)

### Giải thích ngắn gọn
Một số user không muốn tên mình xuất hiện trên bảng xếp hạng công khai (vì lý do riêng tư hoặc không muốn bị so sánh). Cột `is_private_leaderboard` trên bảng `users` cho phép user opt-out. Query leaderboard sẽ JOIN với users và lọc `WHERE u.isPrivateLeaderboard = false`. User private vẫn tính XP bình thường nhưng không hiển thị trên bảng.

### Ví dụ trong project này
```java
// UserScoreRepository.java — JOIN FETCH + filter private users
@Query("SELECT us FROM UserScore us JOIN FETCH us.user u "
     + "WHERE us.periodType = :periodType AND us.periodValue = :periodValue "
     + "AND u.isPrivateLeaderboard = false "
     + "ORDER BY us.totalXp DESC")
Page<UserScore> findTopByPeriod(...);

// LeaderboardServiceImpl.java — Không trả về myRanking nếu private
if (!currentUser.getIsPrivateLeaderboard()) {
    Long myRank = scoreRepository.getRankByUserIdAndPeriod(...);
    // ...
}
```

### Câu hỏi phỏng vấn liên quan
GDPR và privacy-by-design ảnh hưởng gì đến thiết kế leaderboard?

### Câu trả lời ngắn gọn
GDPR (General Data Protection Regulation) yêu cầu: (1) **Consent**: User phải opt-in hoặc có quyền opt-out khỏi việc hiển thị dữ liệu cá nhân công khai. Bảng xếp hạng hiển thị tên + avatar = dữ liệu cá nhân. (2) **Right to erasure**: User xóa tài khoản thì mọi dữ liệu trên leaderboard phải bị xóa (`ON DELETE CASCADE`). (3) **Data minimization**: Chỉ hiển thị thông tin cần thiết (tên, avatar, XP) — không hiển thị email, phone. (4) **Privacy by default**: Nên mặc định `is_private_leaderboard = false` (opt-out) hoặc `= true` (opt-in) tùy jurisdiction. Project này chọn `= false` (hiện mặc định, user tự ẩn).

---

## 106. Rank Calculation bằng COUNT Subquery

### Giải thích ngắn gọn
Để tính thứ hạng của 1 user cụ thể mà không cần load toàn bộ bảng xếp hạng, dùng công thức: `rank = (số người có điểm cao hơn mình) + 1`. Triển khai bằng `COUNT + 1` subquery. Đây là cách tối ưu hơn `ROW_NUMBER() OVER (ORDER BY total_xp DESC)` khi chỉ cần rank của 1 user, vì `ROW_NUMBER` phải sort toàn bộ bảng.

### Ví dụ trong project này
```java
// UserScoreRepository.java
@Query("SELECT COUNT(us) + 1 FROM UserScore us JOIN us.user u "
     + "WHERE us.periodType = :periodType AND us.periodValue = :periodValue "
     + "AND u.isPrivateLeaderboard = false "
     + "AND us.totalXp > ("
     + "  SELECT us2.totalXp FROM UserScore us2 "
     + "  WHERE us2.userId = :userId "
     + "  AND us2.periodType = :periodType AND us2.periodValue = :periodValue"
     + ")")
Long getRankByUserIdAndPeriod(@Param("userId") Long userId, ...);
```

### Câu hỏi phỏng vấn liên quan
So sánh `COUNT + 1 subquery` vs `ROW_NUMBER()` cho rank calculation. Khi nào chọn cái nào?

### Câu trả lời ngắn gọn
**COUNT + 1 subquery**: O(N) chỉ để tính rank 1 user. Tốt khi chỉ cần rank cá nhân ("Bạn đang xếp hạng #47"). Index trên `(period_type, period_value, total_xp DESC)` giúp COUNT chỉ scan ít rows. **ROW_NUMBER()**: Phải sort toàn bộ bảng, tạo window function result set. Tốt khi cần rank cho toàn bộ danh sách (hiển thị bảng xếp hạng top 50). Trong project này: dùng ROW_NUMBER implicitly qua `ORDER BY + LIMIT` cho danh sách, COUNT + 1 cho rank cá nhân. Lưu ý: nếu 2 user cùng điểm, COUNT + 1 cho cùng rank (dense rank), còn ROW_NUMBER cho rank khác nhau (phụ thuộc tie-breaking).

---

## 107. @Cacheable trong Spring — Cache layer cho leaderboard

### Giải thích ngắn gọn
`@Cacheable` là annotation của Spring Cache, lưu kết quả method vào cache (in-memory/Redis). Khi method được gọi lại với cùng key, Spring trả về kết quả cached thay vì chạy lại logic. Phù hợp cho leaderboard vì: (1) leaderboard không cần real-time tuyệt đối (delay 1-5 phút chấp nhận được), (2) nhiều user cùng xem cùng 1 bảng xếp hạng → giảm load DB.

### Ví dụ trong project này
```java
// LeaderboardServiceImpl.java
@Cacheable(value = "leaderboard", key = "#timeframe + '-' + #page + '-' + #size")
public LeaderboardRes getLeaderboard(String timeframe, int page, int size) {
    // ... query DB ...
}
```

### Câu hỏi phỏng vấn liên quan
`@Cacheable` có những pitfall nào cần chú ý?

### Câu trả lời ngắn gọn
(1) **Cache invalidation**: Khi user cộng XP, cache vẫn giữ bảng xếp hạng cũ. Cần `@CacheEvict` hoặc TTL (time-to-live). Trong project chưa implement TTL → leaderboard có thể stale cho đến khi cache bị evict. (2) **Self-invocation**: `@Cacheable` dùng AOP proxy — nếu gọi method từ bên trong cùng class (`this.getLeaderboard()`), cache bị bypass. Phải gọi qua injected bean. (3) **Key collision**: Key phải unique. Nếu quên tham số (vd chỉ dùng `#timeframe` mà quên `#page`), 2 request khác page sẽ trả cùng kết quả. (4) **Serialization**: Nếu dùng Redis cache, DTO phải Serializable. Default `ConcurrentMapCacheManager` (in-memory) không cần nhưng không share giữa instances. (5) **currentUser trong cache**: Method vừa dùng `getCurrentUser()` vừa `@Cacheable` → tất cả user sẽ thấy `isMe = true` cho cùng người. Cần tách logic current user ra khỏi cached method, hoặc thêm userId vào cache key.

---

## 108. API Response Shape Contract

### Giải thích ngắn gọn
API response shape contract là thỏa thuận chính xác giữa backend, service layer frontend và component: dữ liệu nằm ở đâu, field tên gì, đã unwrap hay chưa. Nếu mỗi layer hiểu khác nhau, UI có thể trắng dữ liệu dù API trả đúng.

### Ví dụ trong project này
Backend `GET /api/v1/leaderboards` trả `ApiResponse<LeaderboardRes>` với dữ liệu ở `data.result`. `leaderboard.service.js` unwrap `response.data.result`, nên `LeaderboardPage.vue` phải đọc `result.rankings`, không đọc `response.data.rankings`.

### Câu hỏi phỏng vấn liên quan
Vì sao unit test frontend có thể pass nhưng production vẫn lỗi khi gọi API thật?

### Câu trả lời ngắn gọn
Vì mock response trong test không giống response thật. Test phải mô phỏng đúng boundary contract, hoặc service phải unwrap response trước để component không biết chi tiết `ApiResponse`.

---

## 109. User-Controlled Privacy Preference

### Giải thích ngắn gọn
User-controlled privacy preference là thiết kế cho phép user tự quyết định dữ liệu cá nhân nào được hiển thị công khai. Backend lưu field, API expose field, và frontend cung cấp UI để chỉnh.

### Ví dụ trong project này
User có thể bật `isPrivateLeaderboard` trong Profile. Khi bật, leaderboard backend không đưa user đó vào danh sách ranking công khai, nhưng XP vẫn được tính ở server.

### Câu hỏi phỏng vấn liên quan
Vì sao chỉ thêm cột `is_private_leaderboard` trong database chưa đủ?

### Câu trả lời ngắn gọn
Vì user cần có cách đọc và thay đổi preference đó. Privacy setting phải đi trọn vòng: database, API response/update, frontend UI và test.

---

## 110. Cache Personalization Boundary

### Giải thích ngắn gọn
Cache personalization boundary là ranh giới giữa dữ liệu public có thể cache chung và dữ liệu cá nhân hóa phải tính riêng theo user. Nếu vượt ranh giới này, cache có thể làm user này thấy trạng thái của user khác.

### Ví dụ trong project này
Leaderboard public rankings có thể cache theo `timeframe/page/size`, nhưng các field như `isMe` và `currentUserRanking` phụ thuộc user hiện tại. Vì vậy `LeaderboardServiceImpl.getLeaderboard()` không được cache nguyên response personalized bằng key public.

### Câu hỏi phỏng vấn liên quan
Khi nào một API response không nên cache bằng key chung?

### Câu trả lời ngắn gọn
Khi response chứa dữ liệu phụ thuộc user hiện tại, quyền truy cập, session, locale riêng tư hoặc bất kỳ flag cá nhân hóa nào. Nếu muốn cache, cần tách phần public khỏi phần personalized.

---

## 111. Nested Route Guard Resolution trong SPA

### Giải thích ngắn gọn
Trong Single Page Application (đặc biệt là Vue Router), khi cấu hình route theo mô hình nested (cha bọc layout, con là page cụ thể), metadata bảo mật (như `meta: { role: 'ADMIN' }`) thường chỉ được đặt ở route cha. Để bảo vệ toàn diện các trang con, navigation guard không được chỉ kiểm tra `to.meta` của leaf route mà phải duyệt qua mảng `to.matched` theo thứ tự từ con lên cha hoặc ngược lại.

### Ví dụ trong project này
```javascript
// Thay vì chỉ: const requiredRole = to.meta.role (dễ bỏ lọt nếu leaf route không định nghĩa meta)
const requiredRole = to.matched?.slice().reverse().find(record => record.meta?.role)?.meta?.role || to.meta?.role
```
Học viên cố truy cập `/admin/courses` hay `/admin/quizzes/1/builder` đều bị guard phát hiện route cha yêu cầu role `ADMIN` và bị điều hướng tức thì về `/student/dashboard`.

### Câu hỏi phỏng vấn liên quan
Tại sao dùng `to.matched.some()` hoặc `to.matched.find()` trong router guard an toàn hơn đọc trực tiếp thuộc tính trên `to.meta`?

### Câu trả lời ngắn gọn
Bởi vì `to.matched` chứa danh sách đầy đủ tất cả các bản ghi route đã khớp từ gốc tới lá. Nếu một middleware/guard chỉ đọc thuộc tính trên leaf route, nó sẽ bỏ qua mọi quy tắc phân quyền được thiết lập ở các route cha và layout bao quanh.

---

## 112. DDL Portability trong Flyway Migration (MariaDB vs H2)

### Giải thích ngắn gọn
DDL Portability là nguyên tắc viết các script migration cơ sở dữ liệu sao cho chúng có thể thực thi chính xác trên cả hệ quản trị dữ liệu production (như MariaDB/MySQL) và hệ quản trị dữ liệu in-memory dùng cho testing (như H2). Cần tránh các cú pháp viết tắt hoặc dialect-specific extensions.

### Ví dụ trong project này
Trong `V6__create_leaderboard_tables.sql`, cú pháp MariaDB thêm nhiều cột trong 1 câu lệnh `ALTER TABLE users ADD COLUMN c1 ..., ADD COLUMN c2 ...` khiến H2 báo lỗi cú pháp khi chạy `mvn test`. Khi tách thành từng câu lệnh độc lập `ALTER TABLE users ADD COLUMN c1 ...; ALTER TABLE users ADD COLUMN c2 ...;`, migration chạy trơn tru trên cả 2 database.

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng Testcontainers thay vì H2 cho integration testing của Spring Boot?

### Câu trả lời ngắn gọn
Khi migration hoặc query sử dụng các tính năng đặc thù sâu của production database (như JSON functions, window functions nâng cao, full-text search, stored procedures, dialect-specific constraints) mà H2 không thể mô phỏng chính xác 100%, lúc đó Testcontainers (chạy container MariaDB thật trong Docker khi test) là giải pháp tiêu chuẩn.

---

## 113. Production Secret Hygiene

### Giải thích ngắn gọn
Production secret hygiene là nguyên tắc không để secret thật như database password, JWT secret, Redis password hoặc API key xuất hiện trong source code, git history, docs public hoặc image build context. Repo chỉ nên lưu template như `.env.example`, còn giá trị thật được inject qua CI/CD secret store, server environment hoặc secret manager.

### Ví dụ trong project này
Root `.gitignore` ignore `.env`, `.env.*`, `*.env` nhưng giữ lại các file mẫu `!.env*.example`. Nhờ vậy `.env.prod` hoặc `.env.local` không bị commit nhầm, trong khi `.env.prod.example` vẫn mô tả đầy đủ biến cần cấu hình.

### Câu hỏi phỏng vấn liên quan
Nếu lỡ commit secret vào git rồi xóa ở commit sau thì đã an toàn chưa?

### Câu trả lời ngắn gọn
Chưa. Secret vẫn tồn tại trong git history, fork, cache CI hoặc remote clone. Cần rotate secret ngay, sau đó mới cân nhắc rewrite history bằng công cụ phù hợp nếu repo yêu cầu.

---

## 114. Localhost Port Binding cho Internal Services

### Giải thích ngắn gọn
Khi chạy Docker Compose production-like, không phải service nào cũng cần public port. Database và Redis thường chỉ cần backend truy cập nội bộ. Bind port vào `127.0.0.1` hoặc bỏ publish port sẽ giảm bề mặt tấn công so với mở ra mọi interface `0.0.0.0`.

### Ví dụ trong project này
`docker-compose.prod.yml` bind MariaDB và Redis vào localhost để admin vẫn có thể debug từ host nếu cần, nhưng không public trực tiếp database/cache ra mạng ngoài.

### Câu hỏi phỏng vấn liên quan
Sự khác nhau giữa `ports` và `expose` trong Docker Compose là gì?

### Câu trả lời ngắn gọn
`ports` publish container port ra host. `expose` chỉ document/mở port trong Docker network cho service khác truy cập, không publish ra host. Với DB/Redis production, ưu tiên Docker network hoặc localhost binding thay vì public host binding.

---

## 115. Fail-Open vs Fail-Closed Rate Limiter

### Giải thích ngắn gọn
Rate limiter dùng Redis có thể gặp lỗi khi Redis down, mất password hoặc network lỗi. Lúc đó hệ thống phải chọn policy: fail-open cho request đi qua, hoặc fail-closed chặn request. Đây là tradeoff giữa availability và security.

### Ví dụ trong project này
`RedisRateLimiterService` hỗ trợ cấu hình `APP_RATE_LIMIT_REDIS_FAILURE_POLICY` và normalize cả `fail-open` lẫn `fail_open`. Tài liệu vận hành giải thích khi nào chọn từng policy và cách debug Redis rate-limit key.

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng fail-closed thay vì fail-open?

### Câu trả lời ngắn gọn
Khi endpoint nhạy cảm với abuse, ví dụ login, password reset, payment hoặc admin mutation. Fail-closed có thể làm giảm availability nếu Redis lỗi, nhưng tránh việc rate limit bị bypass hoàn toàn.

---

## 116. Reproducible Builds & `npm ci` trong Continuous Integration

### Giải thích ngắn gọn
Trong môi trường Continuous Integration (CI), tính nhất quán và khả năng tái lập (reproducibility) của các bản build là yêu cầu sống còn. Nếu dùng `npm install`, npm có thể tự động nâng các dependency dạng caret (`^`) hoặc tilde (`~`) lên bản mới hơn vừa được publish trên registry, hoặc cố gắng bỏ qua cảnh báo xung đột peerDependency. Kết quả là code chạy được ở máy developer nhưng lại fail trên CI, hoặc ngược lại ("Works on my machine").
`npm ci` giải quyết vấn đề này bằng cách:
1. Đọc và thực thi cài đặt chính xác 100% dựa trên cây phụ thuộc được đóng băng trong `package-lock.json`.
2. Kiểm tra tính toàn vẹn (integrity hash) của từng package tarball.
3. Tuân thủ nghiêm ngặt peerDependencies. Nếu phát hiện xung đột (`ERESOLVE`), `npm ci` sẽ dừng ngay lập tức thay vì âm thầm cài đặt sai lệch.

### Ví dụ trong project này
Khi thiết lập GitHub Actions, `npm ci` ban đầu phát hiện xung đột peerDependency giữa `@pinia/testing@2.0.1` (đòi hỏi `pinia >=4.0.2`) và `pinia@3.0.4`. Thay vì lạm dụng flag `--legacy-peer-deps` để che giấu lỗi, chúng ta đã giải quyết tận gốc bằng cách chuẩn hóa `@pinia/testing` về phiên bản `^1.0.3` phù hợp với Pinia 3.x, giúp `package-lock.json` hoàn toàn tương thích và bản build CI chạy an toàn, nhất quán 100%.

### Câu hỏi phỏng vấn liên quan
Tại sao lệnh `npm ci` lại bắt buộc phải có file `package-lock.json`, trong khi `npm install` thì không?

### Câu trả lời ngắn gọn
Vì triết lý của `npm ci` là tái hiện lại chính xác cây thư mục `node_modules` đã được kiểm thử và xác nhận bởi developer thông qua `package-lock.json`. Nếu không có `package-lock.json`, npm không có cơ sở dữ liệu về phiên bản con (transitive dependencies) chính xác để cài đặt, do đó `npm ci` sẽ từ chối chạy. Ngược lại, `npm install` được thiết kế cho việc phát triển nên nếu thiếu `package-lock.json`, nó sẽ tự tính toán dependency tree mới và tự tạo ra lock file.

---

## 117. Static Dry-Run Validation cho Docker Compose trong CI

### Giải thích ngắn gọn
Trước khi đẩy code hoặc image lên môi trường Production, việc kiểm tra xem tệp cấu hình triển khai (`docker-compose.prod.yml`) có hợp lệ hay không là một bước phòng thủ thiết yếu. Tuy nhiên, trong môi trường CI tự động, việc khởi động thật các container (MySQL/MariaDB, Redis, Backend Spring Boot) tốn rất nhiều thời gian, tài nguyên RAM/CPU của runner và yêu cầu phải nạp secret production nhạy cảm.
Giải pháp là sử dụng chế độ Static Dry-Run thông qua lệnh:
```bash
docker compose -f docker-compose.prod.yml config
```
Lệnh này chỉ đọc cấu hình YAML, kiểm tra cú pháp schema chuẩn của Docker Compose, áp dụng các quy tắc nội suy biến môi trường (Environment Variable Interpolation), và in ra cấu hình chuẩn hóa mà không kéo image hay khởi động container.

### Ví dụ trong project này
Workflow GitHub Actions sử dụng tệp `.env.prod.example` (chứa các giá trị giả lập an toàn) để chạy `docker compose config`. Bước này giúp CI xác minh ngay:
- Không có lỗi chính tả tên biến môi trường.
- Các container tham chiếu đúng volume và network.
- Cấu hình port binding `127.0.0.1` hợp lệ.
Toàn bộ quá trình diễn ra trong 2-3 giây mà không cần bất kỳ mật khẩu thật nào.

### Câu hỏi phỏng vấn liên quan
Lệnh `docker compose config` có tác dụng gì trong pipeline CI/CD?

### Câu trả lời ngắn gọn
`docker compose config` đóng vai trò là một static linter và validator cho file Docker Compose. Nó kiểm tra cú pháp YAML, kiểm tra tính hợp lệ của schema phiên bản compose, kiểm tra sự tồn tại của các biến môi trường được nội suy và xác thực liên kết giữa các services/volumes/networks mà không cần khởi chạy container, giúp bắt lỗi cấu hình sớm trong pipeline CI.

---

## 118. Least Privilege Permissions & Concurrency Optimization trong GitHub Actions

### Giải thích ngắn gọn
GitHub Actions cung cấp môi trường tự động hóa mạnh mẽ nhưng cũng là mục tiêu hàng đầu của các cuộc tấn công chuỗi cung ứng (supply-chain attacks). Hai thực hành chuẩn (best practices) quan trọng nhất khi viết workflow:
1. **Nguyên tắc đặc quyền tối thiểu (Least Privilege Permissions):**
   Theo mặc định, `GITHUB_TOKEN` có thể được cấp quyền ghi (write) cho nhiều scope. Nếu một dependency độc hại hoặc script kiểm thử bên thứ ba bị tiêm nhiễm, kẻ tấn công có thể dùng token này để sửa đổi mã nguồn, tạo release giả mạo hoặc thay đổi PR. Bằng cách khai báo tường minh:
   ```yaml
   permissions:
     contents: read
   ```
   chúng ta tước bỏ mọi quyền ghi không cần thiết của runner.
2. **Tối ưu hóa hàng đợi với Concurrency Cancellation:**
   Khi lập trình viên liên tục push commit vào nhánh đang mở PR để sửa lỗi, nếu không cấu hình concurrency, mỗi commit sẽ kích hoạt một pipeline mới chạy song song, gây lãng phí runner phút của tổ chức. Khai báo:
   ```yaml
   concurrency:
     group: ${{ github.workflow }}-${{ github.ref }}
     cancel-in-progress: true
   ```
   sẽ tự động hủy bỏ ngay lập tức các build cũ chưa hoàn thành của nhánh đó để tập trung tài nguyên vào commit mới nhất.

### Ví dụ trong project này
Tệp `.github/workflows/ci.yml` áp dụng đầy đủ cả hai cơ chế trên:
- `permissions: contents: read` ở cấp root workflow.
- `concurrency` nhóm theo `${{ github.workflow }}-${{ github.ref }}` với `cancel-in-progress: true`.
- Tách 3 jobs `backend`, `frontend`, `docker-config` chạy song song (matrix/parallel execution) giúp giảm thời gian phản hồi toàn bộ pipeline xuống mức tối ưu.

### Câu hỏi phỏng vấn liên quan
Tại sao nên dùng `cancel-in-progress: true` trong cấu hình `concurrency` của GitHub Actions PR workflow?

### Câu trả lời ngắn gọn
Vì trên một Pull Request, kết quả kiểm thử của các commit cũ không còn giá trị ngay khi có commit mới được đẩy lên. Bằng cách tự động cancel các build đang chạy dở của commit cũ, hệ thống giải phóng ngay runner tài nguyên, tránh nghẽn hàng đợi CI và giúp developer nhận kết quả kiểm thử của commit mới nhất nhanh hơn.

---

## 119. Parent Search bằng `EXISTS` Subquery

### Giải thích ngắn gọn
Khi cần tìm parent entity dựa trên dữ liệu của child entity, `EXISTS` subquery là một cách an toàn để hỏi: "Có ít nhất một child thỏa điều kiện hay không?". Cách này tránh duplicate parent rows thường gặp khi join trực tiếp vào collection.

### Ví dụ trong project này
Public course search cần tìm course theo lesson title/content. Query dùng:
```sql
EXISTS (
  SELECT 1 FROM Lesson l
  WHERE l.course = c
  AND l.status = 'PUBLISHED'
  AND LOWER(l.title) LIKE ...
)
```
Nhờ vậy một course có 3 lesson match vẫn chỉ xuất hiện một lần trong danh sách khóa học.

### Câu hỏi phỏng vấn liên quan
Khi nào `EXISTS` tốt hơn `JOIN` trong truy vấn search?

### Câu trả lời ngắn gọn
Khi chỉ cần biết child entity có tồn tại hay không, không cần lấy dữ liệu child vào result. `EXISTS` giúp tránh nhân bản parent rows và thường diễn đạt đúng intent hơn.

---

## 120. Backward-Compatible Multi-Value Query Parameter

### Giải thích ngắn gọn
Backward-compatible API evolution là mở rộng khả năng API mà không phá client cũ. Với filter một giá trị, có thể giữ nguyên tên param nhưng cho phép định dạng nhiều giá trị phân tách bằng dấu phẩy.

### Ví dụ trong project này
Trước đây public course API nhận `level=N5`. Sau task này, API vẫn nhận `level=N5`, đồng thời nhận thêm `level=N5,N4`. Frontend multi-select gửi cùng param nên không cần tạo endpoint mới.

### Câu hỏi phỏng vấn liên quan
Ưu và nhược điểm của comma-separated query param so với repeated query param là gì?

### Câu trả lời ngắn gọn
Comma-separated dễ giữ backward-compatible và URL gọn (`level=N5,N4`). Repeated param (`level=N5&level=N4`) rõ semantic hơn với framework hỗ trợ collection trực tiếp. Chọn cách nào nên dựa trên contract hiện có và mức độ muốn tránh breaking change.

---

## 121. URL-Driven Filter State

### Giải thích ngắn gọn
URL-driven filter state là kỹ thuật đồng bộ trạng thái search/filter/pagination của UI vào query string. Nhờ đó URL trở thành source có thể share, refresh và back/forward được.

### Ví dụ trong project này
`CourseListPage.vue` đọc `route.query.keyword`, `route.query.level`, `route.query.courseType`, `route.query.sort`, `route.query.page` để khởi tạo state. Khi user chọn N5 và N4, page cập nhật URL thành `?level=N5,N4`.

### Câu hỏi phỏng vấn liên quan
Vì sao search page nên lưu filter vào URL thay vì chỉ giữ trong component state?

### Câu trả lời ngắn gọn
Vì search/filter là trạng thái điều hướng. User kỳ vọng copy link cho người khác, refresh trang hoặc quay lại bằng browser back mà vẫn thấy cùng kết quả.

---

## 122. Public API Enrichment via SecurityContextHolder

### Giải thích ngắn gọn
Public API Enrichment là kỹ thuật cho phép một public endpoint (không bắt buộc đăng nhập) tự động phát hiện danh tính người dùng nếu có token xác thực gửi kèm trong header `Authorization`. Dựa vào danh tính này, backend làm giàu thêm (enrich) dữ liệu cá nhân hóa (như `isEnrolled`, `lastLessonId`) vào response payload mà không phá vỡ khả năng truy cập tự do của khách vãng lai.

### Ví dụ trong project này
Tại `CoursePublicServiceImpl.java`:
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
    String username = auth.getName();
    // Query User, CourseEnrollment, LessonProgress để gán isEnrolled và lastLessonId
}
```
Khách vãng lai nhận `isEnrolled = false, lastLessonId = null`, còn học viên nhận `isEnrolled = true, lastLessonId = 15`.

### Câu hỏi phỏng vấn liên quan
Tại sao không tách thành 2 endpoint: một endpoint public cho khóa học, và một endpoint private cho trạng thái của học viên?

### Câu trả lời ngắn gọn
Tách 2 endpoint đòi hỏi frontend phải gửi 2 request nối tiếp (waterfall requests), gây ra độ trễ hiển thị và hiện tượng nút bấm bị nhảy trạng thái (state flickering). Gộp (enrich) vào 1 response giúp client render ngay lập tức trạng thái chính xác nhất (zero-latency UI state transition).

---

## 123. Backend-Driven UI State vs Client-Side Async Resolution

### Giải thích ngắn gọn
Backend-Driven UI State là mô hình trong đó server tính toán và trả về cờ trạng thái giao diện (như quyền hành động, trạng thái ghi danh, bước học tiếp theo) ngay trong API response, thay vì bắt client phải tự tải nhiều tập dữ liệu về rồi tự suy luận logic hiển thị.

### Ví dụ trong project này
Thay vì frontend phải gọi `StudentService.getMyCourses()`, lặp qua danh sách so sánh `c.id === courseId` để suy ra học viên đã đăng ký hay chưa, backend kiểm tra trực tiếp qua index DB và trả về `isEnrolled: true` ngay trong `CourseDetailPublicRes`.

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng backend-driven UI state thay vì client-side resolution?

### Câu trả lời ngắn gọn
Khi trạng thái đó quyết định các CTA hành vi sống còn (nút Mua/Ghi danh/Học tiếp), hoặc khi logic kiểm tra trên client tốn kém tài nguyên mạng/bộ nhớ và dễ dẫn đến bất đồng bộ giữa các trang hoặc giữa mobile và desktop.

---

## 124. Context-Aware UX & State Flickering Prevention

### Giải thích ngắn gọn
Context-Aware UX là thiết kế giao diện tự động thích ứng với trạng thái và mục đích hiện tại của người dùng. State Flickering Prevention là tập hợp các kỹ thuật frontend nhằm ngăn chặn hiện tượng component hiển thị trạng thái A (mặc định) trong vài mili-giây rồi bất ngờ đổi sang trạng thái B (thực tế) khi dữ liệu bất đồng bộ trả về.

### Ví dụ trong project này
- **Context-Aware:** `Navbar.vue` hiển thị "Khóa học của tôi" khi đã đăng nhập; `HomePage.vue` đổi banner CTA từ "Đăng ký tài khoản" thành "Vào học ngay".
- **Flickering Prevention:** `CourseDetailPage.vue` khởi tạo và bind ngay `course.isEnrolled` từ response chi tiết khóa học, loại bỏ hoàn toàn việc nút nháy từ "Đăng ký khóa học ngay" sang "Tiếp tục học".

### Câu hỏi phỏng vấn liên quan
Làm thế nào để tránh UI flickering khi component phụ thuộc vào dữ liệu authentication bất đồng bộ?

### Câu trả lời ngắn gọn
1. Lưu cache/hydrate auth state trong persistent store (Pinia + LocalStorage) trước khi component render.
2. Dùng skeleton loading placeholder hoặc backend-enriched response thay vì render giao diện mặc định dạng "unauthenticated" trước khi fetch xong API.

---

## 125. HTML5 Content Model & Nested Interactive Controls Restriction

### Giải thích ngắn gọn
Chuẩn HTML5 (W3C Content Model) phân loại các phần tử HTML thành các nhóm: Flow Content, Phrasing Content, Interactive Content. Quy tắc cơ bản quy định rằng một phần tử thuộc loại "Interactive Content" (như `<a>`, `<button>`, `<input>`, `<select>`) **không được phép** chứa bất kỳ phần tử Interactive Content nào khác bên trong.

### Ví dụ trong project này
Tại `HomePage.vue` và `CourseListPage.vue`, các thẻ khóa học trước đây bọc toàn bộ khối bằng `<router-link to="...">` (thẻ `<a>`), nhưng bên trong lại đặt thêm `<button class="...">Mua ngay</button>`. Điều này được sửa lại bằng cách đổi nút bên trong thành `<span class="inline-flex items-center ...">Mua ngay</span>`, đảm bảo toàn bộ thẻ card là một liên kết hợp lệ duy nhất.

### Câu hỏi phỏng vấn liên quan
Hậu quả kỹ thuật khi vi phạm quy tắc Nested Interactive Content là gì?

### Câu trả lời ngắn gọn
Gây ra xung đột bubbling sự kiện click, hành vi không thể dự đoán giữa các trình duyệt (ví dụ: Safari kích hoạt cả hai handler), phá vỡ thứ tự phím Tab, và làm công cụ hỗ trợ người khiếm thị (Screen Reader) thông báo hai nút điều khiển lồng nhau gây nhầm lẫn nghiêm trọng.

---

## 126. Accessible Clickable Card Pattern (WCAG 2.1)

### Giải thích ngắn gọn
Accessible Clickable Card Pattern là kỹ thuật xây dựng các thẻ giao diện tổng hợp (card) có thể bấm vào toàn bộ vùng hiển thị nhưng vẫn đáp ứng đầy đủ tiêu chuẩn tiếp cận WCAG: có vai trò rõ ràng, nhận diện được bằng bàn phím, và cung cấp nhãn ngữ nghĩa đầy đủ.

### Ví dụ trong project này
Tại `MyCourseCard.vue`:
```html
<div 
  class="bg-stitch-card rounded-2xl cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring"
  tabindex="0"
  role="button"
  :aria-label="`Tiếp tục học ${course.courseName}`"
  @click="$emit('continue', course)"
  @keydown.enter="$emit('continue', course)"
  @keydown.space.prevent="$emit('continue', course)"
>
```
Nút bên trong chỉ là thẻ `<span>` để hiển thị visual CTA `"Tiếp tục học →"`.

### Câu hỏi phỏng vấn liên quan
Tại sao sự kiện `@keydown.space` cần có modifier `.prevent`?

### Câu trả lời ngắn gọn
Vì trong trình duyệt, phím Space mặc định sẽ cuộn trang xuống một khoảng bằng chiều cao màn hình. Dùng `.prevent` sẽ ngăn chặn hành vi cuộn mặc định và chỉ thực thi hành động kích hoạt nút của card.

---

## 127. Mobile Navigation Shell & Viewport Drawer Traps

### Giải thích ngắn gọn
Mobile Navigation Shell là cấu trúc điều hướng thu gọn chuyên biệt cho thiết bị màn hình nhỏ (smartphones/tablets). Để đảm bảo trải nghiệm người dùng cao cấp, một Mobile Drawer cần xử lý đầy đủ các tương tác: lớp nền tối làm mờ (backdrop overlay), khả năng đóng linh hoạt (chạm ra ngoài, phím Escape, hoặc chọn menu item), và không được đẩy hay bóp méo nội dung chính bên dưới.

### Ví dụ trong project này
Tại `AdminLayout.vue`, sidebar desktop cố định `w-56` được ẩn trên mobile bằng `hidden md:flex`. Màn hình nhỏ dùng nút Hamburger trên top bar kích hoạt một drawer `fixed inset-0 z-50 md:hidden` trượt ra từ bên trái với backdrop `bg-black/70 backdrop-blur-sm`, đóng lại khi click backdrop hoặc nhấn Escape, giúp nội dung admin trang quản trị hiển thị đầy đủ và không bị tràn layout.

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng Navigation Drawer thay vì Bottom Navigation Bar trên mobile?

### Câu trả lời ngắn gọn
Khi menu có nhiều hơn 5 mục lựa chọn, hoặc khi menu phân cấp theo vai trò quản trị viên phức tạp có nhiều tác vụ phụ (như đăng xuất, chuyển sang trang công khai). Bottom Navigation chỉ phù hợp khi ứng dụng có 3-5 tác vụ cốt lõi lặp đi lặp lại hàng ngày.

---

## 128. Financial Data Snapshotting (Bất Biến Thông Tin Đơn Hàng)

### Giải thích ngắn gọn
Financial Data Snapshotting là nguyên tắc thiết kế cơ sở dữ liệu và domain logic trong e-commerce: khi một đơn hàng được tạo, các thông tin nhạy cảm về giá bán, tên mặt hàng, chiết khấu và thuế phải được sao chép (snapshot) trực tiếp vào bảng đơn hàng / chi tiết đơn hàng (`orders`, `order_items`), thay vì chỉ lưu khóa ngoại (`course_id`) trỏ sang bảng danh mục (`courses`).

### Ví dụ trong project này
Tại `OrderServiceImpl.java`:
```java
BigDecimal effectivePrice = (course.getSalePrice() != null && course.getSalePrice().compareTo(BigDecimal.ZERO) > 0)
        ? course.getSalePrice()
        : course.getOriginalPrice();

OrderItem item = OrderItem.builder()
        .course(course)
        .courseTitle(course.getTitle()) // Snapshot tên khóa học
        .price(effectivePrice)          // Snapshot giá bán hiệu lực
        .quantity(1)
        .build();
```
Nếu 6 tháng sau, admin tăng giá khóa học từ 350.000đ lên 500.000đ hoặc đổi tên thành "Khóa học N3 Pro", lịch sử đơn hàng của học viên vẫn hiển thị chính xác tên và số tiền 350.000đ mà họ đã mua tại thời điểm đó.

### Câu hỏi phỏng vấn liên quan
Tại sao không nên join trực tiếp bảng `courses` để lấy giá và tên khi hiển thị lịch sử đơn hàng?

### Câu trả lời ngắn gọn
Vì dữ liệu trong bảng `courses` là dữ liệu biến đổi theo thời gian (mutable catalog data). Nếu chỉ lưu ID và join khi query, giá của đơn hàng cũ sẽ bị biến động mỗi khi sản phẩm đổi giá, gây sai lệch báo cáo kế toán, tranh chấp pháp lý với khách hàng và vi phạm tính toàn vẹn kiểm toán (audit trail).

---

## 129. Defense-in-Depth Authorization (Bảo Mật Nhiều Lớp trong Spring Security)

### Giải thích ngắn gọn
Defense-in-depth (bảo vệ theo chiều sâu) là phương pháp bảo mật không phụ thuộc vào một tầng chốt chặn duy nhất. Trong backend Spring Boot, nó được hiện thực bằng cách kết hợp cả tầng lọc yêu cầu mạng (Filter Chain URL Matching) lẫn tầng phân quyền phương thức (Method Security with AOP).

### Ví dụ trong project này
1. **Lớp 1 - Web Security Filter Chain (`SecurityConfig.java`):**
   ```java
   .requestMatchers("/api/v1/orders", "/api/v1/orders/**").hasRole("STUDENT")
   ```
2. **Lớp 2 - Method Security (`OrderController.java`):**
   ```java
   @PostMapping
   @PreAuthorize("hasRole('STUDENT')")
   public ApiResponse<OrderRes> createOrder(@Valid @RequestBody OrderCreateReq req) { ... }
   ```
3. **Lớp 3 - Domain Ownership Check (`OrderServiceImpl.java`):**
   ```java
   if (!order.getUser().getId().equals(currentUser.getId())) {
       throw new AppException(ErrorCode.ORDER_ACCESS_DENIED);
   }
   ```
Dù developer có lỡ chỉnh sửa cấu hình endpoint ở `SecurityConfig`, tầng `@PreAuthorize` vẫn bảo vệ method. Và dù method security cho phép role `STUDENT`, tầng service vẫn kiểm tra người học chỉ được xem đúng đơn hàng do chính mình tạo ra (ngăn IDOR / cross-user data tampering).

### Câu hỏi phỏng vấn liên quan
Khi nào nên dùng URL pattern matching trong `SecurityFilterChain`, và khi nào nên dùng `@PreAuthorize`?

### Câu trả lời ngắn gọn
URL matching trong Filter Chain tốt cho việc phân vùng quyền diện rộng (ví dụ: `/api/admin/**`, `/api/auth/**`) chặn request ngay từ cửa ngõ trước khi vào DispatcherServlet. `@PreAuthorize` phù hợp cho phân quyền chi tiết (fine-grained authorization) tại từng hàm, hỗ trợ biểu thức SpEL phức tạp và kiểm tra thuộc tính của argument hoặc return object.

---

## 130. Duplicate Pending Order Prevention Policy trong E-Commerce

### Giải thích ngắn gọn
Duplicate Pending Order Prevention Policy là chính sách nghiệp vụ quy định hệ thống sẽ phản ứng ra sao khi một khách hàng gửi yêu cầu tạo đơn hàng mới cho một mặt hàng mà họ đã có một đơn hàng đang ở trạng thái chờ thanh toán (`PENDING`).

### Ví dụ trong project này
Tại `OrderServiceImpl.java`:
```java
boolean hasPendingOrder = orderRepository.existsByUserIdAndCourseIdAndStatus(
        currentUser.getId(),
        course.getId(),
        OrderStatus.PENDING
);
if (hasPendingOrder) {
    throw new AppException(ErrorCode.ORDER_DUPLICATE_PENDING);
}
```
Khi học viên bấm nút "Mua khóa học" nhiều lần hoặc mở nhiều tab, backend từ chối tạo đơn mới và trả về HTTP `409 CONFLICT` với mã lỗi `5004 ORDER_DUPLICATE_PENDING`.

### Câu hỏi phỏng vấn liên quan
Tại sao chọn trả về lỗi `409 Conflict` thay vì âm thầm trả về đơn hàng `PENDING` cũ (idempotent reuse)?

### Câu trả lời ngắn gọn
Trả về `409 Conflict` rõ ràng hơn về mặt ngữ nghĩa (explicit business contract): (1) Ngăn ngừa rủi ro sai lệch giá nếu giá khóa học thay đổi giữa 2 lần bấm, (2) Giúp frontend phân biệt rõ ràng giữa "vừa tạo mới thành công" và "đã có đơn hàng cũ cần tiếp tục thanh toán", từ đó hiển thị thông báo chính xác và chuyển hướng người dùng đến đúng hóa đơn đang chờ.

---

## 131. Token Hashing at Rest vs. Password Hashing (SHA-256 vs. BCrypt)

### Giải thích ngắn gọn
Mã hóa lưu trữ dữ liệu xác thực (At-Rest Security) chia làm hai trường phái hoàn toàn khác biệt tùy thuộc vào **Entropy (Độ ngẫu nhiên/Độ phức tạp)** của dữ liệu:
1. **Low-Entropy Data (Mật khẩu người dùng):** Người dùng thường đặt mật khẩu ngắn, dễ đoán, lặp lại. Bắt buộc phải sử dụng các thuật toán **Slow Hashing với Salt ngẫu nhiên và Memory-Hard** (như BCrypt, Argon2, PBKDF2). Mục tiêu là làm chậm tốc độ brute-force (ví dụ 1 hash mất 100-250ms).
2. **High-Entropy Data (Refresh Tokens, API Keys, Session Tokens):** Là các chuỗi ngẫu nhiên dài được sinh bằng thuật toán mật mã (cryptographically secure random) hoặc JWT ký chữ ký số 256/512-bit. Không gian mẫu là $2^{256}$, không thể bị brute-force hay tấn công rainbow table. Do đó, các token này **phải dùng Fast Deterministic Cryptographic Hash (như SHA-256)**.

### Ví dụ trong project này
Tại `TokenHashUtil.java` và `AuthServiceImpl.java`:
```java
public static String hashToken(String token) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(token.trim().getBytes(StandardCharsets.UTF_8));
    // convert to lowercase hex string
}

// Khi lưu CSDL:
RefreshToken entity = RefreshToken.builder()
    .user(user)
    .tokenHash(TokenHashUtil.hashToken(refreshTokenString))
    .token(null) // Tuyệt đối không lưu plaintext
    .build();
refreshTokenRepository.save(entity);

// Khi tra cứu:
String tokenHash = TokenHashUtil.hashToken(rawToken);
Optional<RefreshToken> tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash);
```
Vì SHA-256 là **tất định (deterministic)** (cùng 1 input luôn ra đúng 1 chuỗi hash 64 hex), database có thể đánh index Unique trên `token_hash` và tìm kiếm bản ghi với độ phức tạp **$O(1)$** mà không tốn tải CPU. Nếu dùng BCrypt, do salt ngẫu nhiên mỗi lần hash, ta không thể `SELECT WHERE token = hash(token)` được, buộc phải load toàn bộ bảng ra kiểm tra `matches()`, gây sập hệ thống (Denial of Service).

---

## 132. Refresh Token Rotation (RTR) & Token Reuse Detection

### Giải thích ngắn gọn
**Refresh Token Rotation (RTR)** là kỹ thuật bảo mật quy định mỗi Refresh Token chỉ được phép sử dụng **một lần duy nhất**. Khi client gửi Refresh Token cũ lên để xin Access Token mới, backend sẽ thu hồi (revoke) token cũ và cấp phát một Refresh Token hoàn toàn mới.

**Reuse Detection (Phát hiện tái sử dụng)** là cơ chế phòng vệ tự động: Nếu một Refresh Token đã bị thu hồi (`revoked == true`) mà lại được gửi lên server một lần nữa, hệ thống kết luận ngay rằng token đã bị rò rỉ (Token Theft) và một trong hai bên (kẻ tấn công hoặc nạn nhân) đang cố gắng sử dụng token cũ. Hệ thống sẽ ngay lập tức **hủy toàn bộ họ token (Token Family Revocation)** của người dùng đó, buộc cả hai bên phải đăng nhập lại.

### Ví dụ trong project này
Tại `AuthServiceImpl.java`:
```java
// 1. Kiểm tra Token Reuse Detection
if (Boolean.TRUE.equals(refreshTokenEntity.getRevoked())) {
    log.warn("Token reuse detected! Revoking all tokens for user ID: {}", refreshTokenEntity.getUser().getId());
    refreshTokenRepository.revokeAllByUserId(refreshTokenEntity.getUser().getId());
    throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);
}

// 2. Refresh Token Rotation: Hủy token cũ
refreshTokenEntity.setRevoked(true);
refreshTokenRepository.save(refreshTokenEntity);

// 3. Cấp phát token mới
String newAccessToken = jwtUtil.generateAccessToken(user);
String newRefreshToken = jwtUtil.generateRefreshToken(user);
RefreshToken newEntity = RefreshToken.builder()
    .user(user)
    .tokenHash(TokenHashUtil.hashToken(newRefreshToken))
    .token(null)
    .expiredAt(LocalDateTime.now().plusDays(7))
    .build();
refreshTokenRepository.save(newEntity);
```

---

## 133. Zero-Downtime Safe Migration Pattern cho Secret Data (Dual-Read & On-the-fly Upgrade)

### Giải thích ngắn gọn
Khi thay đổi cơ chế lưu trữ dữ liệu từ Plaintext sang Hashed trong một hệ thống lớn đang hoạt động liên tục (Live Production), không thể dừng hệ thống hoặc chạy một câu lệnh SQL Update khổng lồ khóa bảng (Table Lock). 

Chiến lược **Dual-Read & On-the-fly Upgrade (Lazy Migration)** giải quyết bài toán này:
1. **Step 1 - Non-blocking Schema Extension:** Thêm cột mới (`token_hash`) cho phép `NULL`.
2. **Step 2 - Dual-Read:**
   - Ưu tiên đọc bằng định dạng mới (`findByTokenHash`).
   - Nếu không thấy, fallback đọc định dạng cũ (`findByToken`).
3. **Step 3 - On-the-fly Upgrade:** Khi một bản ghi cũ được đọc qua fallback, hệ thống lập tức băm dữ liệu, cập nhật `tokenHash`, xóa trắng cột `token` plaintext về `null` và lưu lại.
4. **Step 4 - Natural Expiration Cleanup:** Các bản ghi cũ không được sử dụng sẽ tự hết hạn (TTL 7 ngày) và bị tiến trình dọn dẹp ban đêm (`TokenCleanupScheduler`) xóa vĩnh viễn.

### Ví dụ trong project này
```java
private Optional<RefreshToken> findRefreshToken(String rawToken) {
    if (rawToken == null || rawToken.isBlank()) return Optional.empty();
    String tokenHash = TokenHashUtil.hashToken(rawToken);
    
    // 1. Ưu tiên tìm qua token_hash
    Optional<RefreshToken> byHash = refreshTokenRepository.findByTokenHash(tokenHash);
    if (byHash.isPresent()) {
        return byHash;
    }
    
    // 2. Fallback tìm theo plaintext cho dữ liệu cũ (Legacy records)
    Optional<RefreshToken> legacy = refreshTokenRepository.findByToken(rawToken);
    if (legacy.isPresent()) {
        RefreshToken entity = legacy.get();
        // 3. Nâng cấp on-the-fly
        entity.setTokenHash(tokenHash);
        entity.setToken(null);
        return Optional.of(refreshTokenRepository.save(entity));
    }
    return Optional.empty();
}
```
Kết quả: 100% người dùng đang hoạt động không nhận thấy bất kỳ sự gián đoạn nào (Zero Downtime, No Force-Logout).

---

## 134. Cookie Security Attributes & The Least-Privilege Transport Principle

### Giải thích ngắn gọn
Cookie không chỉ là nơi lưu trữ key-value đơn thuần mà là một giao thức ủy quyền vận chuyển giữa Trình duyệt và Server. Để bảo vệ các bí mật nhạy cảm (như Refresh Token), việc thiết lập đúng các cờ bảo vệ (Cookie Flags) là bắt buộc:

1. **`HttpOnly = true`:**
   - **Tác dụng:** Cấm hoàn toàn JavaScript truy cập cookie qua thuộc tính `document.cookie`.
   - **Mục tiêu:** Miễn nhiễm trước các cuộc tấn công **XSS (Cross-Site Scripting)**. Ngay cả khi hacker chèn được script độc hại vào trang web, script đó cũng không thể đọc hoặc gửi Refresh Token về server của hacker.
2. **`Secure = true`:**
   - **Tác dụng:** Yêu cầu trình duyệt chỉ được phép gửi cookie qua các kết nối được mã hóa bằng **HTTPS / TLS**.
   - **Mục tiêu:** Ngăn chặn tấn công nghe lén đường truyền mạng **Man-in-the-Middle (MitM)** hoặc rò rỉ token qua mạng Wi-Fi công cộng không an toàn.
3. **`SameSite` (Strict vs. Lax vs. None):**
   - **`Strict`:** Cookie chỉ được gửi đi khi request bắt nguồn từ chính domain đó (First-party context). Nếu user click một đường link từ email hoặc Facebook sang web của bạn, cookie sẽ KHÔNG được gửi kèm.
   - **`Lax` (Khuyến nghị cho Authentication):** Cho phép gửi cookie khi user điều hướng cấp cao nhất (Top-level GET navigation như click link), nhưng **chặn tuyệt đối** khi request được kích hoạt chéo domain từ các thẻ `<img>`, `<iframe>`, hoặc form POST ngầm.
   - **`None`:** Cho phép gửi xuyên domain (Cross-site). Bắt buộc phải đi kèm `Secure = true`.
4. **`Path` (Nguyên tắc Đặc quyền Tối thiểu - Least Privilege):**
   - Thay vì để `Path=/` (mọi request tới mọi endpoint của server đều đính kèm Refresh Token), ta cấu hình `Path=/api/auth`.
   - Chỉ khi trình duyệt gọi các endpoint như `/api/auth/refresh-token` hay `/api/auth/logout`, cookie mới được gửi. Các request học tập, xem video, làm quiz thông thường sẽ không mang theo Refresh Token, giảm thiểu tối đa diện tích tấn công (Attack Surface).

### Ví dụ trong project này
Tại `CookieUtil.java`:
```java
ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(isSecure)
        .sameSite("Lax")
        .path("/api/auth")
        .maxAge(7 * 24 * 60 * 60) // 7 ngày
        .build();
response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
```

---

## 135. Ma Trận Mô Hình Đe Dọa (Threat Modeling Matrix) Cho Kiến Trúc Dual-Token

| Vector Tấn Công | Điểm Yếu Thường Gặp | Cơ Chế Phòng Thủ Đã Triển Khai Trong Dự Án |
| :--- | :--- | :--- |
| **XSS (Cross-Site Scripting)** | Lưu Refresh Token trong `localStorage` hoặc SessionStorage. | **`HttpOnly` Cookie:** JS không thể đọc được token. Đồng thời DTO Response đánh dấu `@JsonIgnore` loại bỏ hoàn toàn token khỏi JSON payload. |
| **CSRF (Cross-Site Request Forgery)** | Cookie gửi tự động trong các request cross-origin độc hại. | **`SameSite=Lax`:** Trình duyệt chặn gửi cookie từ các domain lạ. Ngoài ra các API thay đổi trạng thái đều dùng phương thức `POST` và xác thực qua CSRF-safe context. |
| **Rò rỉ Database / Backup Leak (SQL Injection / Dump Leak)** | Lưu token dạng plaintext trong bảng `refresh_tokens`. Kẻ tấn công trích xuất và chiếm quyền phiên hàng loạt. | **SHA-256 Hashing At Rest:** CSDL chỉ lưu chuỗi băm hex 64 ký tự. Không thể dịch ngược về JWT gốc để sử dụng. |
| **Token Theft & Replay Attack (Đánh cắp Refresh Token)** | Token dùng được nhiều lần vô hạn, không phát hiện được khi bị đánh cắp. | **Refresh Token Rotation (RTR) + Reuse Detection:** Token chỉ dùng 1 lần. Nếu token cũ đã bị thu hồi mà tái xuất hiện, hệ thống lập tức kích hoạt hủy toàn bộ họ token (`revokeAllByUserId`), ép logout cả 2 bên. |
| **Man-in-the-Middle (MitM)** | Truyền tải qua HTTP không mã hóa. | **`Secure = true` Flag:** Trình duyệt chỉ gửi cookie qua kết nối mã hóa SSL/TLS. |
| **Over-Exposure (Thừa đặc quyền truyền dẫn)** | Cookie gửi kèm cho tất cả static assets và API không liên quan. | **Scoped `Path=/api/auth`:** Giới hạn cookie chỉ gửi cho các route xác thực cần thiết. |

---

## 136. Idempotent Payment Completion

### Giải thích ngắn gọn

Idempotent Payment Completion nghĩa là request hoàn tất thanh toán có thể đến nhiều lần do double-click, timeout hoặc provider retry, nhưng hệ thống chỉ ghi nhận một giao dịch thành công và một lần cấp quyền học.

### Ví dụ trong project này

`PaymentServiceImpl.simulatePayment()` khóa order trước. Nếu order đã là `PAID`, service lấy `PaymentStatus.SUCCESS` đã tồn tại và trả response; không save thêm `Payment`, không tạo enrollment mới, không tăng `Course.totalStudents`.

### Câu hỏi phỏng vấn liên quan

Vì sao payment callback cần idempotent ngay cả khi frontend đã disable nút thanh toán?

### Câu trả lời ngắn gọn

Disable nút chỉ cải thiện UX và không kiểm soát được retry mạng, nhiều tab, hay callback từ provider. Backend phải tự bảo vệ dữ liệu để một giao dịch chỉ được ghi nhận đúng một lần.

---

## 137. Pessimistic Locking cho Checkout

### Giải thích ngắn gọn

Pessimistic locking là việc database khóa bản ghi khi transaction đọc để ghi. Transaction khác muốn cập nhật cùng bản ghi phải chờ tới khi lock được giải phóng.

### Ví dụ trong project này

`OrderRepository.findByIdForUpdate()` dùng `@Lock(LockModeType.PESSIMISTIC_WRITE)`. Trong `simulatePayment`, request đầu tiên khóa order `PENDING`, ghi payment và chuyển nó thành `PAID`; request thứ hai sau đó đọc trạng thái mới và trả kết quả idempotent.

### Câu hỏi phỏng vấn liên quan

Khi nào cân nhắc pessimistic lock thay vì optimistic lock?

### Câu trả lời ngắn gọn

Dùng pessimistic lock khi xung đột ghi cần được tuần tự hóa ngay từ đầu và side effect rất nhạy cảm, ví dụ payment. Optimistic lock phù hợp hơn với luồng đọc nhiều, xung đột ít và có thể retry an toàn.

---

## 138. Server-Authoritative Pricing

### Giải thích ngắn gọn

Server-authoritative pricing là nguyên tắc backend là nguồn duy nhất quyết định giá, khuyến mãi và số tiền phải trả. Frontend chỉ gửi định danh sản phẩm và hiển thị snapshot do backend trả về.

### Ví dụ trong project này

`CourseDetailPage.vue` gọi `OrderService.createOrder(courseId)` mà không gửi `price`. `CheckoutPage.vue` hiển thị `order.finalAmount`, `order.totalAmount` và item snapshot từ `GET /api/v1/orders/{id}`.

### Câu hỏi phỏng vấn liên quan

Vì sao không nên tin giá tiền do frontend gửi lên API checkout?

### Câu trả lời ngắn gọn

Người dùng có thể sửa request bằng DevTools hoặc proxy. Nếu server tin giá client gửi, họ có thể mua khóa học với giá 0. Backend phải tự tính và snapshot giá từ dữ liệu tin cậy.

---

## 139. Purchase Recovery Flow

### Giải thích ngắn gọn

Purchase recovery flow là luồng giúp người dùng quay lại giao dịch đang dở dang thay vì tạo order mới hoặc nhận một lỗi kỹ thuật khó hiểu.

### Ví dụ trong project này

Khi tạo order cho cùng course nhận `ORDER_DUPLICATE_PENDING` (5004), frontend gọi `GET /api/v1/orders/me`, tìm order `PENDING` có `item.courseId` phù hợp, rồi điều hướng tới `/checkout/:orderId`.

### Câu hỏi phỏng vấn liên quan

Tại sao không chỉ hiển thị lỗi 409 khi user đã có pending order?

### Câu trả lời ngắn gọn

409 đúng về nghiệp vụ, nhưng không đủ tốt về trải nghiệm. Frontend nên hiểu error code đặc thù và hướng user đến hành động tiếp theo an toàn là tiếp tục thanh toán order hiện có.

---

## 140. Fail-Fast Startup Validation với `@PostConstruct`

### Giải thích ngắn gọn

Fail-fast startup validation là pattern kiểm tra tính hợp lệ của cấu hình ngay tại thời điểm ứng dụng khởi động. Nếu phát hiện cấu hình không an toàn (secret yếu, placeholder chưa thay, mật khẩu mặc định), ứng dụng ném exception và từ chối khởi động hoàn toàn.

Nguyên tắc cốt lõi: **Phát hiện lỗi ở deployment, không phải sau incident.** Một hệ thống chạy thành công với JWT secret 6 bytes sẽ hoạt động bình thường cho tới khi bị brute-force — và lúc đó thì đã quá muộn.

### Ví dụ trong project này

`ProductionSecurityValidator` sử dụng `@PostConstruct` để gọi `validate()` ngay sau khi Spring inject xong tất cả `@Value`. Nếu danh sách `errors` không rỗng, method gọi `throw new IllegalStateException(errorMessage)` buộc Spring Context khởi tạo thất bại và application process thoát. Bean này chỉ tồn tại khi `@Profile("prod")`, không ảnh hưởng development.

```java
@PostConstruct
public void validateOnStartup() {
    List<String> errors = validate();
    if (!errors.isEmpty()) {
        throw new IllegalStateException(
            "Production configuration validation failed with " + errors.size() + " violation(s):\n" + ...
        );
    }
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao không để ứng dụng chạy rồi kiểm tra cấu hình sau?

### Câu trả lời ngắn gọn

Nếu chạy trước rồi kiểm tra sau, trong khoảng thời gian đó hệ thống phục vụ traffic với cấu hình không an toàn. Với JWT secret yếu, token đã ký trong giai đoạn này bị compromise vĩnh viễn. Fail-fast đảm bảo không có traffic nào được phục vụ với cấu hình lỗi.

---

## 141. Aggregate Error Reporting — Gom lỗi thay vì Fail-on-First

### Giải thích ngắn gọn

Aggregate error reporting là kỹ thuật thu thập tất cả vi phạm vào một collection rồi báo cáo toàn bộ trong một lần duy nhất, thay vì ném exception ngay khi gặp lỗi đầu tiên.

Trong ngữ cảnh configuration validation: nếu có 5 biến environment chưa được cung cấp, DevOps cần biết tất cả 5 biến đó trong một lần deploy thất bại thay vì phải deploy → fail → fix → deploy → fail → fix lặp đi lặp lại 5 lần.

### Ví dụ trong project này

`ProductionSecurityValidator.validate()` tạo một `List<String> errors = new ArrayList<>()`, truyền vào 8 phương thức validate con (`validateJwtSecrets(errors)`, `validateDatabase(errors)`, `validateRedis(errors)`, `validateCors(errors)`...). Mỗi phương thức chỉ `add()` mô tả lỗi vào list mà không throw. Sau khi tất cả đã chạy xong, `validateOnStartup()` kiểm tra list và ném một exception duy nhất chứa toàn bộ vi phạm.

```java
public List<String> validate() {
    List<String> errors = new ArrayList<>();
    validateJwtSecrets(errors);
    validateAdminPassword(errors);
    validateDatabase(errors);
    validateRedis(errors);
    validateCors(errors);
    validateCookies(errors);
    validateRateLimitPolicy(errors);
    validatePaymentSimulation(errors);
    return errors;
}
```

### Câu hỏi phỏng vấn liên quan

Aggregate error reporting có ưu điểm gì so với fail-on-first khi validate configuration?

### Câu trả lời ngắn gọn

Fail-on-first buộc operator phải fix từng lỗi một qua nhiều lần deploy. Aggregate cho phép fix tất cả trong một lần, tiết kiệm thời gian deployment đáng kể. Phương thức `validate()` trả về `List<String>` cũng dễ dàng unit test từng rule riêng lẻ mà không cần bắt exception.

---

## 142. Profile-Based Configuration Separation (`@Profile`)

### Giải thích ngắn gọn

Spring `@Profile` annotation giới hạn một bean chỉ được đăng ký vào ApplicationContext khi profile tương ứng đang active. Đây là cơ chế tách biệt cấu hình development và production, đảm bảo các ràng buộc nghiêm ngặt của production không gây phiền hà cho developer khi phát triển local.

**Nguyên tắc:** Development profile cho phép mọi thứ hoạt động nhanh (localhost, H2, JWT ngắn). Production profile bắt buộc mọi thứ phải an toàn. Hai thế giới này không bao giờ được trộn lẫn.

### Ví dụ trong project này

`ProductionSecurityValidator` đánh dấu `@Profile("prod")`. Khi developer chạy `mvn spring-boot:run` mà không set profile (mặc định là default profile), bean này hoàn toàn không tồn tại — developer thoải mái dùng `jwt.secret: myDevSecret`, `spring.datasource.url: jdbc:h2:mem:testdb` mà không bị chặn.

Khi deploy lên server với `SPRING_PROFILES_ACTIVE=prod`, bean được kích hoạt và fail-fast validation chạy.

### Câu hỏi phỏng vấn liên quan

Làm sao đảm bảo production security rules không ảnh hưởng developer workflow?

### Câu trả lời ngắn gọn

Đặt security validator bean dưới `@Profile("prod")` để nó chỉ tồn tại khi production profile active. Development chạy với default hoặc dev profile, không load bean. Cấu hình chia thành `application.yml` (development defaults) và `application-prod.yml` (chỉ environment variable, không fallback cho secrets).

---

## 143. Placeholder & Weak-Secret Detection bằng Regex + Blacklist

### Giải thích ngắn gọn

Placeholder detection là kỹ thuật phát hiện các giá trị cấu hình chưa được thay thế từ template (ví dụ `CHANGE_ME`, `YOUR_STRONG_PASSWORD_HERE`). Weak-secret detection là phát hiện các mật khẩu phổ biến quá đơn giản (`password`, `admin`, `123456`). Kết hợp cả hai tạo thành lớp bảo vệ ngăn production deploy với credential không an toàn.

**Hai chiến lược bổ trợ:**
- **Regex pattern:** Bắt các biến thể placeholder (case-insensitive) dù có suffix khác nhau (`CHANGE_ME_NOW`, `YOUR_STRONG_DB_PASSWORD_HERE_2026`).
- **Exact-match blacklist:** Bắt các mật khẩu yếu không chứa substring placeholder nhưng vẫn nguy hiểm (`password`, `root`, `admin`).

### Ví dụ trong project này

```java
private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
    "(?i).*(CHANGE_ME|YOUR_STRONG_|SECRET_HERE|PASSWORD_HERE|PLACEHOLDER|GENERATE_WITH|DEFAULT_PASSWORD).*");

private static final Set<String> FORBIDDEN_WEAK_PASSWORDS = Set.of(
    "password", "admin", "root", "123456", "password@123", "admin_password", "db_password", "redis_password");
```

Mỗi validation method gọi `isPlaceholder(value)` (regex) và kiểm tra `FORBIDDEN_WEAK_PASSWORDS.contains(value.toLowerCase())` (blacklist) trước khi validate độ dài.

### Câu hỏi phỏng vấn liên quan

Tại sao cần cả regex lẫn blacklist thay vì chỉ dùng một?

### Câu trả lời ngắn gọn

Regex bắt các template placeholder với nhiều biến thể suffix/prefix, nhưng không bắt được `password` hay `admin` vì chúng không chứa substring `CHANGE_ME`. Blacklist bắt các weak password phổ biến nhưng không cover hết mọi biến thể placeholder. Hai chiến lược bổ trợ nhau để giảm false-negative.

---

## 144. Docker Port Binding Security — `127.0.0.1` vs `0.0.0.0`

### Giải thích ngắn gọn

Khi Docker Compose map port với cú pháp `ports: - "3306:3306"`, Docker mặc định bind lên `0.0.0.0` — tức tất cả network interfaces của host machine. Trên VPS public, điều này có nghĩa cổng database (3306) và Redis (6379) mở ra internet cho mọi IP kết nối trực tiếp, hoàn toàn bypass ứng dụng backend.

Thay bằng `127.0.0.1:3306:3306` giới hạn kết nối chỉ từ localhost trên host machine. Các container trong cùng Docker network vẫn giao tiếp qua tên service (ví dụ `jdbc:mariadb://db:3306/...`) mà không cần port mapping ra host.

**Defense-in-depth:** Port binding + password authentication + firewall rules. Không có lớp nào đứng một mình; tất cả cùng bảo vệ.

### Ví dụ trong project này

`docker-compose.prod.yml` bind database và Redis chỉ ra localhost:

```yaml
services:
  db:
    ports:
      - "127.0.0.1:3306:3306"  # Chỉ host machine truy cập được
  redis:
    ports:
      - "127.0.0.1:6379:6379"  # Không public ra internet
    command: redis-server --requirepass "${REDIS_PASSWORD}"
```

Backend container kết nối qua Docker internal network: `DB_URL=jdbc:mariadb://db:3306/...`, `REDIS_HOST=redis`. Không đi qua port mapping của host.

### Câu hỏi phỏng vấn liên quan

Docker port mapping `0.0.0.0` có bypass firewall (iptables/ufw) không?

### Câu trả lời ngắn gọn

Có. Docker tự thêm iptables rules cho port mapping, có thể bypass ufw rules đã cấu hình. Bind `127.0.0.1` ngăn Docker tạo rule mở public. Với production, nên audit Docker iptables rules hoặc dùng `--iptables=false` kết hợp firewall thủ công.

---

## 145. Rate Limiter Failure Policy — FAIL_CLOSED vs FAIL_OPEN

### Giải thích ngắn gọn

Failure policy quyết định hành vi của rate limiter khi backend storage (Redis) không khả dụng. Đây là quyết định kiến trúc phản ánh sự đánh đổi giữa **bảo mật** (security) và **khả dụng** (availability) — một trong những trade-off cơ bản nhất trong thiết kế hệ thống.

- **FAIL_CLOSED:** Redis down → chặn tất cả request → bảo mật tối đa, nhưng có thể gây outage toàn bộ authentication nếu Redis offline lâu.
- **FAIL_OPEN:** Redis down → cho tất cả request đi qua → ưu tiên availability, nhưng rate limiting tạm thời bị vô hiệu, cho phép brute-force attack trong thời gian Redis offline.

### Ví dụ trong project này

`application-prod.yml` mặc định `app.rate-limit.redis-failure-policy: FAIL_CLOSED`. `ProductionSecurityValidator` kiểm tra giá trị phải thuộc `{FAIL_CLOSED, FAIL_OPEN}`, reject bất kỳ giá trị nào khác.

Quyết định chọn `FAIL_CLOSED` cho authentication endpoints vì nếu Redis down mà rate limiting bị tắt, kẻ tấn công có thể brute-force hàng triệu password combinations mỗi giây mà không bị chặn.

### Câu hỏi phỏng vấn liên quan

Trong CAP theorem, FAIL_CLOSED và FAIL_OPEN phản ánh sự đánh đổi nào?

### Câu trả lời ngắn gọn

FAIL_CLOSED ưu tiên Consistency/Safety (đảm bảo rate limit luôn được áp dụng) nhưng hy sinh Availability. FAIL_OPEN ưu tiên Availability nhưng hy sinh invariant rate limit. Với authentication, FAIL_CLOSED an toàn hơn vì hậu quả của brute-force thành công (account takeover) nghiêm trọng hơn downtime tạm thời.

---

## 146. Hibernate `ddl-auto` Strategies & Production Safety

### Giải thích ngắn gọn

`spring.jpa.hibernate.ddl-auto` kiểm soát cách Hibernate xử lý schema database so với JPA Entity mapping:

| Giá trị | Hành vi | Phù hợp với |
|---------|---------|-------------|
| `create` | Xóa toàn bộ schema, tạo lại từ đầu | Test chạy xong bỏ |
| `create-drop` | Tạo khi start, xóa khi shutdown | Test ngắn hạn |
| `update` | So sánh entity với schema, thêm cột/bảng thiếu (không xóa cột cũ) | Development nhanh |
| `validate` | Chỉ kiểm tra schema khớp entity, không sửa đổi gì | **Production** |
| `none` | Không làm gì | Khi dùng tool khác (Flyway) |

**Nguy hiểm của `update` trong production:** Hibernate không bao giờ xóa cột hoặc index đã tồn tại, dẫn đến schema drift tích lũy. Nó cũng không thể rename column, split table, hay thực hiện data migration phức tạp. Và quan trọng nhất: không có cách rollback.

### Ví dụ trong project này

`application-prod.yml` set `hibernate.ddl-auto: validate`. Nếu Entity thêm field mới mà chưa có Flyway migration tạo cột tương ứng, application fail-fast ngay tại startup thay vì chạy rồi crash khi query trúng cột thiếu.

`application.yml` (development) dùng `ddl-auto: update` để developer nhanh chóng thêm field mà không cần viết migration cho mỗi lần thay đổi nhỏ.

### Câu hỏi phỏng vấn liên quan

Tại sao `ddl-auto: update` nguy hiểm trong production?

### Câu trả lời ngắn gọn

`update` không xóa cột cũ nên schema drift tích lũy theo thời gian. Nó không hỗ trợ rollback, rename, hay data migration phức tạp. Với production, mọi schema change phải đi qua Flyway versioned migration để có audit trail, checksum verification, và khả năng review/rollback.

---

## 147. Secret Masking trong Error Logging

### Giải thích ngắn gọn

Secret masking là kỹ thuật chỉ hiển thị một phần nhỏ của giá trị bí mật (đủ để nhận diện) khi ghi log hoặc hiển thị trong error message, ngăn việc lộ toàn bộ secret ra log file, monitoring system, hay terminal output.

**Quy tắc:** Log file thường lưu trữ lâu dài, được backup, và có thể nhiều người có quyền đọc. Nếu log chứa toàn bộ JWT secret, bất kỳ ai có quyền đọc log đều có thể giả mạo token.

### Ví dụ trong project này

```java
private String maskSecret(String secret) {
    if (secret == null) return "null";
    if (secret.length() <= 8) return "***";
    return secret.substring(0, 4) + "..." + secret.substring(secret.length() - 4);
}
```

Error message hiển thị: `[JWT] 'jwt.secret.access' contains forbidden placeholder: 'CHAN...SE64'` thay vì hiện toàn bộ `CHANGE_ME_GENERATE_WITH_OPENSSL_RAND_BASE64_32`.

### Câu hỏi phỏng vấn liên quan

Tại sao không nên log toàn bộ giá trị secret khi validation thất bại?

### Câu trả lời ngắn gọn

Log files được lưu trữ, backup, và có thể nhiều người đọc. Nếu secret xuất hiện đầy đủ trong log, mọi người có quyền đọc log (developer, ops, monitoring tool) đều biết secret. Masking chỉ hiện đủ để nhận diện mà không lộ giá trị hoàn chỉnh.

---

## 148. Server-Authoritative Quiz Evaluation & Partial-Credit Scoring

### Giải thích ngắn gọn

Server-Authoritative Evaluation là nguyên tắc trong đó toàn bộ logic tính điểm, kiểm tra đúng/sai, cộng trừ điểm thành phần, và quyết định kết quả bài thi được thực hiện 100% trên server. Client chỉ đóng vai trò gửi lên danh sách lựa chọn của người dùng (`answerId`, `answerIds`, `userAnswerText`).

Đối với câu hỏi trắc nghiệm nhiều lựa chọn (`MULTIPLE_CHOICE`), thuật toán **Partial Credit with Penalty** giúp chấm điểm công bằng cho thí sinh chọn đúng một phần, đồng thời ngăn chặn tuyệt đối hành vi đoán mò (chọn tất cả các ô):

$$\text{ratio} = \max\left(0, \frac{C_{\text{selected}} - W_{\text{selected}}}{C_{\text{total}}}\right)$$
$$\text{pointsEarned} = \text{round}(\text{ratio} \times \text{maxPoints}, 2)$$

- $C_{\text{selected}}$: Số đáp án đúng mà thí sinh đã chọn.
- $W_{\text{selected}}$: Số đáp án sai mà thí sinh đã chọn (penalty).
- $C_{\text{total}}$: Tổng số đáp án đúng của câu hỏi ($C_{\text{total}} \ge 1$).
- Thí sinh chọn đúng toàn bộ và không chọn đáp án sai nào mới đạt full credit (`isCorrect = true`).

### Ví dụ trong project này

Trong [QuizLearningServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_quiz/service/QuizLearningServiceImpl.java):

```java
long correctSelected = selectedIds.stream().filter(correctIds::contains).count();
long wrongSelected = selectedIds.stream().filter(wrongIds::contains).count();

if (correctSelected == correctIds.size() && wrongSelected == 0) {
    isCorrect = true;
    pointsEarned = maxPoints;
} else if (correctSelected > 0) {
    double ratio = Math.max(0.0, (double) (correctSelected - wrongSelected) / (double) correctIds.size());
    pointsEarned = maxPoints.multiply(BigDecimal.valueOf(ratio))
            .setScale(2, RoundingMode.HALF_UP);
    isCorrect = pointsEarned.compareTo(maxPoints) == 0;
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao cần phạt điểm khi thí sinh chọn đáp án sai trong câu hỏi MULTIPLE_CHOICE?

### Câu trả lời ngắn gọn

Nếu chỉ tính điểm theo tỉ lệ đáp án đúng mà không phạt đáp án sai, thí sinh có thể gian lận bằng cách tích chọn tất cả các phương án để luôn đạt 100% điểm. Cơ chế trừ điểm đáp án sai triệt tiêu điểm số đoán mò, đảm bảo chỉ những thí sinh thực sự hiểu bài mới được điểm.

---

## 149. Duplicate Payload Attack Vector & In-Memory Deduplication Guard (`Set.add()`)

### Giải thích ngắn gọn

Duplicate Payload Attack Vector xảy ra khi một client độc hại gửi một danh sách chứa các phần tử trùng lặp khóa định danh (ví dụ nhiều phần tử cùng chứa `questionId = 50`) trong cùng một HTTP request body. 

Nếu backend xử lý theo dạng stream/for-loop ngây thơ hoặc dùng `Collectors.toMap(..., (a, b) -> b)`:
1. Đối tượng sau sẽ âm thầm ghi đè đối tượng trước, làm sai lệch trạng thái câu trả lời đã lưu.
2. Hoặc nếu có tích lũy điểm trong vòng lặp, kẻ tấn công có thể gửi lặp lại câu hỏi đã trả lời đúng hàng trăm lần để nhân số điểm nhận được lên gấp bội.

Cơ chế phòng thủ tối ưu là sử dụng cấu trúc dữ liệu `Set<Long>` và kiểm tra kết quả boolean của `set.add(key)`. Nếu phần tử đã tồn tại, `set.add()` trả về `false` với độ phức tạp $O(1)$, cho phép ném ngoại lệ `INVALID_REQUEST` ngay lập tức để từ chối toàn bộ request.

### Ví dụ trong project này

Trong [QuizLearningServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_quiz/service/QuizLearningServiceImpl.java):

```java
Set<Long> seenQuestionIds = new HashSet<>();
for (QuizSubmitAnswerReq ansReq : req.getAnswers()) {
    if (ansReq.getQuestionId() == null) {
        throw new AppException(ErrorCode.INVALID_REQUEST, "questionId không được để trống");
    }
    if (!quizQuestionIds.contains(ansReq.getQuestionId())) {
        throw new AppException(ErrorCode.INVALID_REQUEST,
                "Câu hỏi không thuộc bài kiểm tra này: " + ansReq.getQuestionId());
    }
    if (!seenQuestionIds.add(ansReq.getQuestionId())) {
        throw new AppException(ErrorCode.INVALID_REQUEST,
                "Dữ liệu nộp bài chứa câu hỏi trùng lặp: " + ansReq.getQuestionId());
    }
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao nên kiểm tra trùng lặp bằng `Set.add()` thay vì `List.contains()`?

### Câu trả lời ngắn gọn

`List.contains()` có độ phức tạp thời gian là $O(n)$, nếu kiểm tra trong vòng lặp duyệt $n$ phần tử thì tổng độ phức tạp sẽ là $O(n^2)$. Ngược lại, `HashSet.add()` có độ phức tạp trung bình là $O(1)$, giúp kiểm tra toàn bộ danh sách trong thời gian tuyến tính $O(n)$ mà không gây nghẽn CPU khi payload lớn.

---

## 150. Jackson SetterlessProperty Gotcha with Java Unmodifiable Collections

### Giải thích ngắn gọn

Trong thư viện Jackson (JSON ObjectMapper của Spring Boot), khi deserialize JSON thành Java Object, Jackson tìm kiếm cả Setter lẫn Getter. Nếu một class có một getter trả về Collection (ví dụ `public List<Long> getEffectiveAnswerIds()`) nhưng **không có setter** tương ứng, Jackson kích hoạt tính năng **SetterlessProperty**:
1. Jackson gọi getter để lấy đối tượng collection hiện tại (`getEffectiveAnswerIds()`).
2. Jackson cố gắng gọi phương thức `.add(...)` trên collection vừa lấy được để nhét các phần tử từ JSON vào.

**Cạm bẫy nghiêm trọng:** Nếu getter trả về một unmodifiable collection (ví dụ `List.of(...)` trong Java 9+ thuộc lớp `ImmutableCollections`), phương thức `.add()` sẽ ném ra `UnsupportedOperationException`. Ngoại lệ này bị wrap thành `JsonMappingException` và trả về mã lỗi HTTP 500 (Internal Server Error) cho client.

**Quy tắc:** Mọi helper method trong DTO có tiền tố `get...` hoặc `is...` mà không đại diện cho một JSON field thực sự cần nhận dữ liệu từ client đều **bắt buộc phải được đánh dấu `@JsonIgnore`**.

### Ví dụ trong project này

Trong [QuizSubmitAnswerReq.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_quiz/dto/QuizSubmitAnswerReq.java):

```java
/**
 * Resolves effective selected answer IDs from either answerIds or answerId.
 */
@com.fasterxml.jackson.annotation.JsonIgnore
public List<Long> getEffectiveAnswerIds() {
    if (answerIds != null && !answerIds.isEmpty()) {
        return answerIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
    if (answerId != null) {
        return List.of(answerId);
    }
    return List.of();
}
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào để tránh Jackson tự động serialize/deserialize các helper getter method trong DTO?

### Câu trả lời ngắn gọn

Đánh dấu `@JsonIgnore` trên method đó, hoặc đổi tên method không bắt đầu bằng tiền tố JavaBean (`get`/`is`), ví dụ `resolveEffectiveAnswerIds()`. Đánh dấu `@JsonIgnore` là giải pháp chuẩn mực và tường minh nhất trong Spring Boot.

---

## 151. Security Defense Persistence & Transaction Rollback Gotcha (`noRollbackFor`)

### Giải thích ngắn gọn

Trong Spring Boot, khi một phương thức được đánh dấu `@Transactional`, bất kỳ ngoại lệ nào kế thừa từ `RuntimeException` (ví dụ `AppException`) bị ném ra khỏi phương thức sẽ kích hoạt cơ chế tự động **Rollback** toàn bộ transaction về trạng thái ban đầu trước khi bắt đầu phương thức.

**Lỗ hổng bảo mật tiềm ẩn (Security Defense Rollback):**
Trong kịch bản phát hiện tấn công tái sử dụng refresh token cũ (Token Reuse / Replay Attack), nghiệp vụ yêu cầu server phải ngay lập tức **thu hồi toàn bộ phiên hoạt động** của người dùng đó (`revokeAllByUserId`) và ném ra ngoại lệ `AppException(ErrorCode.REFRESH_TOKEN_REVOKED)`.
Nếu method ném `AppException` trong transaction mặc định:
1. Spring rollback toàn bộ transaction!
2. Câu lệnh `UPDATE refresh_tokens SET revoked = true WHERE user_id = :userId` vừa thực thi bị hủy bỏ!
3. Phiên hoạt động của nạn nhân hoặc kẻ tấn công **vẫn tiếp tục tồn tại hợp lệ trong database**!

**Giải pháp:**
Sử dụng `@Transactional(noRollbackFor = AppException.class)` trên phương thức phòng thủ hoặc tách tác vụ thu hồi phiên sang một transaction độc lập (`@Transactional(propagation = Propagation.REQUIRES_NEW)`). Khi đó, các câu lệnh thu hồi token sẽ được `COMMIT` bền vững vào CSDL bất chấp việc phương thức kết thúc bằng ngoại lệ lỗi HTTP 401.

### Ví dụ trong project này

Trong [AuthServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_auth/service/AuthServiceImpl.java):

```java
@Override
@Transactional(noRollbackFor = AppException.class)
public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
    ...
    // Token Reuse Detection: thu hồi ngay lập tức và commit xuống DB
    if (Boolean.TRUE.equals(refreshTokenEntity.getRevoked())) {
        log.warn("Token reuse detected! Revoking all tokens for user ID: {}", refreshTokenEntity.getUser().getId());
        revokeUserSessions(refreshTokenEntity.getUser().getId());
        throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);
    }
    ...
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao cần cấu hình `noRollbackFor` khi ném ngoại lệ trong logic phát hiện tấn công token reuse?

### Câu trả lời ngắn gọn

Mặc định Spring sẽ rollback toàn bộ transaction khi có `RuntimeException`. Nếu không dùng `noRollbackFor`, lệnh thu hồi token family sẽ bị rollback và vô hiệu hóa, khiến các phiên của người dùng vẫn hoạt động sau cuộc tấn công.

---

## 152. RFC 7519 `jti` Claim & Unique Token Hash at Rest

### Giải thích ngắn gọn

Khi lưu trữ refresh token dưới dạng chuỗi băm an toàn (ví dụ SHA-256 hex digest) với ràng buộc duy nhất (`UNIQUE`), nếu hai token được cấp cho cùng một user trong cùng một millisecond (ví dụ khi user đăng nhập nhanh trên 2 tab hoặc chạy kiểm thử song song), các claims mặc định (`id`, `sub`, `iat`, `exp`) hoàn toàn giống nhau byte-for-byte. Kết quả là 2 chuỗi JWT giống hệt nhau và sinh ra SHA-256 hash giống hệt nhau, gây lỗi vi phạm khóa duy nhất `Unique index or primary key violation` trong cơ sở dữ liệu.

Theo tiêu chuẩn **RFC 7519 Section 4.1.7**, claim `jti` (JWT ID) cung cấp một mã định danh duy nhất cho JWT:
- Luôn bổ sung `"jti": UUID.randomUUID().toString()` vào claims khi tạo token.
- Đảm bảo tính duy nhất tuyệt đối (cryptographically unique) cho từng token kể cả khi được tạo cùng một thời điểm cho cùng một chủ thể.
- Ngăn chặn hoàn toàn việc đụng độ token hash trong CSDL.

### Ví dụ trong project này

Trong [JwtUtil.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_auth/util/JwtUtil.java):

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

### Câu hỏi phỏng vấn liên quan

Claim `jti` trong JWT là gì và mang lại lợi ích gì cho hệ thống lưu trữ token dạng hash?

### Câu trả lời ngắn gọn

`jti` (JWT ID) là định danh duy nhất ngẫu nhiên (UUID) cho từng token. Nó giúp tránh va chạm chuỗi JWT khi sinh cùng millisecond, đảm bảo mỗi token luôn sinh ra một SHA-256 hash độc nhất, không bị lỗi trùng khóa unique trong database.

---

## 153. Repeatable Pruning Query & Active Session Protection

### Giải thích ngắn gọn

Trong kiến trúc bảo mật session dạng token xoay vòng (rotating refresh tokens), các bản ghi trong bảng `refresh_tokens` sẽ tăng dần theo thời gian (token cũ bị revoke, token hết hạn). Một tiến trình dọn dẹp định kỳ (scheduled cleanup) cần đáp ứng 3 tiêu chí:
1. **Bảo vệ tuyệt đối phiên hoạt động (Active Session Protection):** Không bao giờ xóa hoặc thu hồi nhầm các token đang active (`revoked = false AND expiredAt >= now`).
2. **Tính lặp lại và lũy thừa (Repeatable / Idempotent):** Quét nhiều lần liên tiếp không gây lỗi, không làm xáo trộn dữ liệu hiện có; đợt quét thứ hai trả về 0 bản ghi xóa.
3. **Hiệu năng cao nhờ Index:** Các câu lệnh bulk delete phải được hỗ trợ bởi chỉ mục trên `expired_at` và `revoked` để tránh table lock trên bảng lớn.

### Ví dụ trong project này

Trong [RefreshTokenRepository.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_auth/repository/RefreshTokenRepository.java):

```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("DELETE FROM RefreshToken r WHERE r.expiredAt < :now OR r.revoked = true")
int deleteExpiredOrRevoked(@Param("now") LocalDateTime now);
```

- Điều kiện `expiredAt < now OR revoked = true` loại trừ hoàn toàn các token có `revoked = false AND expiredAt >= now`.
- Đi kèm chỉ mục `idx_refresh_tokens_expired_at` và `idx_refresh_tokens_revoked` trong migration `V10`.

### Câu hỏi phỏng vấn liên quan

Làm thế nào để đảm bảo job scheduled cleanup xóa token rác mà không xóa nhầm phiên đăng nhập của người dùng đang online?

### Câu trả lời ngắn gọn

Phân tách ranh giới rõ ràng trong câu query: chỉ xóa các token thỏa mãn `expiredAt < now` hoặc `revoked = true`. Các session hợp lệ đang hoạt động luôn có `revoked = false` và `expiredAt >= now`, do đó không bao giờ rơi vào tập điều kiện xóa.

---

## 154. Production Structured Logging & High-Performance Log Masking

### Giải thích ngắn gọn

Trong môi trường phân tán (Distributed / Microservices / Cloud-native), log dạng văn bản tự do (plain text) gây khó khăn rất lớn cho việc thu thập, phân tích cú pháp (parsing) và tìm kiếm tự động trên ELK (Elasticsearch/Logstash/Kibana), Loki hay CloudWatch.

**Structured Logging** chuyển đổi mọi dòng log thành một đối tượng JSON có schema chuẩn:
- Trường thời gian `@timestamp` ISO-8601.
- Mức độ `level` (INFO, WARN, ERROR).
- Tên logger `logger_name`, luồng xử lý `thread_name`.
- Metadata ngữ cảnh: `correlationId`, `requestMethod`, `requestUri`, `clientIp`, `service`, `environment`.

Tuy nhiên, việc in log ra JSON tiềm ẩn nguy cơ **rò rỉ dữ liệu nhạy cảm (PII & Credentials Leakage)** như mật khẩu, access token, cookie hoặc JWT. Kỹ thuật **Log Masking** giải quyết bài toán này bằng cách:
1. Xác định tập các trường nhạy cảm (`password`, `refreshToken`, `token`, `Bearer`, `Cookie`).
2. **Fast-path Optimization:** Kiểm tra sơ bộ bằng `indexOf` trước khi áp dụng Regex để tránh ngốn CPU (ReDoS và CPU spikes khi log tải lớn).
3. Thay thế giá trị nhạy cảm bằng chuỗi `***REDACTED***`.

### Ví dụ trong project này

Trong [LogMaskingConverter.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/logging/LogMaskingConverter.java):
```java
public static String maskSensitiveData(String message) {
    if (message == null || message.isEmpty()) return message;
    if (!quickCheck(message)) return message; // Fast-path: 95% log thường không cần regex

    String masked = message;
    for (Pattern pattern : SENSITIVE_PATTERNS) {
        masked = pattern.matcher(masked).replaceAll(REPLACEMENT);
    }
    return masked;
}
```

Và cấu hình [logback-spring.xml](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/resources/logback-spring.xml) tích hợp `LogstashEncoder` cùng `MaskedMessageProvider` cho môi trường production.

### Câu hỏi phỏng vấn liên quan

Tại sao việc che mờ log (Log Masking) bằng Regex có thể làm giảm hiệu năng hệ thống và cách khắc phục trong thiết kế là gì?

### Câu trả lời ngắn gọn

Áp dụng nhiều regex phức tạp trên mọi chuỗi log có thể gây ra CPU spike và nghẽn luồng I/O. Khắc phục bằng cách áp dụng **Fast-path check** (kiểm tra nhanh chuỗi con bằng `indexOf` với chi phí $O(N)$), chỉ khi log có chứa từ khóa nhạy cảm (`password`, `token`, `Bearer`) mới chạy qua pipeline Regex.

---

## 155. Distributed Request Tracing with MDC & Correlation ID Filter Lifecycle

### Giải thích ngắn gọn

Khi một hệ thống phục vụ hàng ngàn request đồng thời hoặc giao tiếp qua nhiều dịch vụ, việc theo dõi xem một chuỗi log thuộc về request hay người dùng nào trở thành thách thức lớn nếu chỉ dựa vào thread name.

**Correlation ID (Trace ID)** là một mã định danh duy nhất (thường là UUID v4) được gắn vào request ngay từ khi bước vào gateway hoặc controller:
1. **Tiếp nhận / Khởi tạo:** Đọc từ HTTP header `X-Correlation-ID`. Nếu client chưa gửi, bộ lọc sẽ tự sinh một UUID mới.
2. **Ngữ cảnh luồng (MDC - Mapped Diagnostic Context):** Lưu trữ Correlation ID vào `ThreadLocal` của SLF4J MDC. Nhờ đó, bất kỳ câu lệnh `log.info()`, `log.error()` nào trong toàn bộ tầng logic (Service, Repository, Helper) đều tự động mang theo Correlation ID mà không cần truyền biến thủ công qua từng tham số method.
3. **Phản hồi Client:** Trả lại header `X-Correlation-ID` trong HTTP response để người dùng hoặc frontend gửi mã này khi báo lỗi kỹ thuật (Ticket / Support).
4. **Bắt buộc dọn dẹp (ThreadLocal Cleanup):** Vì Servlet Container (Tomcat) tái sử dụng worker thread từ ThreadPool, bắt buộc phải gọi `MDC.clear()` trong khối `finally` của filter để ngăn ngừa rò rỉ dữ liệu sang request tiếp theo.

### Ví dụ trong project này

Trong [CorrelationIdFilter.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/logging/CorrelationIdFilter.java):
```java
try {
    String correlationId = request.getHeader("X-Correlation-ID");
    if (correlationId == null || correlationId.isBlank()) {
        correlationId = UUID.randomUUID().toString();
    }
    MDC.put("correlationId", correlationId);
    MDC.put("clientIp", extractClientIp(request));
    response.setHeader("X-Correlation-ID", correlationId);

    filterChain.doFilter(request, response);
} finally {
    MDC.clear(); // Ngăn ngừa ThreadLocal leak khi thread quay lại pool
}
```

### Câu hỏi phỏng vấn liên quan

Điều gì sẽ xảy ra nếu lập trình viên quên gọi `MDC.clear()` trong một ứng dụng Spring Boot chạy trên Tomcat?

### Câu trả lời ngắn gọn

Tomcat tái sử dụng các worker thread trong pool. Nếu không gọi `MDC.clear()`, Correlation ID và Client IP của request trước sẽ tồn tại trong `ThreadLocal`, khiến log của các request sau (có thể từ user khác) bị dán nhầm Correlation ID cũ, làm sai lệch hoàn toàn dữ liệu điều tra sự cố (audit & tracing).

---

## 156. Kubernetes Probes Architecture: Liveness vs Readiness & Component Health Aggregation

### Giải thích ngắn gọn

Trong kiến trúc containerized orchestration (Kubernetes / Docker Swarm), bộ điều phối cần phân biệt rõ giữa việc **tiến trình còn sống** và việc **ứng dụng đã sẵn sàng xử lý dữ liệu**:

1. **Liveness Probe (`/actuator/health/liveness`):**
   - **Mục đích:** Kiểm tra tiến trình JVM có bị treo cứng, deadlock hay rò rỉ bộ nhớ nghiêm trọng hay không.
   - **Hành động khi fail:** Kubernetes sẽ **KILL & RESTART** container.
   - **Quy tắc vàng:** Chỉ kiểm tra trạng thái nội bộ của container (`livenessState`). **Tuyệt đối KHÔNG** đưa CSDL bên ngoài hay Redis vào liveness probe, vì nếu CSDL quá tải chậm phản hồi, liveness fail sẽ khiến toàn bộ cluster pod bị restart đồng loạt, dẫn đến sập toàn bộ hệ thống (cascading failure).
2. **Readiness Probe (`/actuator/health/readiness`):**
   - **Mục đích:** Kiểm tra ứng dụng và các tài nguyên phụ thuộc (Database connection pool, Redis cache) có sẵn sàng nhận traffic người dùng hay không.
   - **Hành động khi fail:** Kubernetes sẽ **NGẮT ĐIỀU PHỐI TRAFFIC** (gỡ pod khỏi danh sách Service endpoints) nhưng **KHÔNG RESTART** pod. Khi database hoặc redis hồi phục, readiness probe pass trở lại và traffic tự động được định tuyến tới pod.
3. **Information Disclosure Prevention:**
   - Public health check endpoint (`/actuator/health`) tại production phải luôn cấu hình `show-details: never`. Nó chỉ trả về `{"status":"UP"}` mà không để lộ chi tiết cấu hình database, dung lượng ổ đĩa hay phiên bản Redis cho kẻ tấn công bên ngoài.

### Ví dụ trong project này

Trong [application-prod.yml](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/resources/application-prod.yml):
```yaml
management:
  endpoint:
    health:
      show-details: never # Chỉ trả về {"status":"UP"}
      probes:
        enabled: true
      group:
        liveness:
          include: livenessState
        readiness:
          include: readinessState,db,redisCustomHealthIndicator
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc đưa kiểm tra kết nối CSDL (Database Ping) vào Kubernetes Liveness Probe là một sai lầm chết người trong kiến trúc Production?

### Câu trả lời ngắn gọn

Khi database bị nghẽn tải hoặc bảo trì ngắn, database ping sẽ timeout làm Liveness probe fail. Kubernetes sẽ restart hàng loạt container cùng lúc. Khi các pod khởi động lại đồng loạt, chúng sẽ ồ ạt mở lại connection pool vào database, làm database càng sụp đổ nặng hơn (thảm họa thundering herd & cascading restart loop).

---

## 157. Metric Instrumentation with Micrometer & Dimensional Tagging for Prometheus

### Giải thích ngắn gọn

Giám sát thụ động qua file log chỉ cho biết những gì đã xảy ra trong quá khứ. Giám sát chủ động theo thời gian thực (Real-time Observability) đòi hỏi ứng dụng phải phát ra các **Metrics (Chỉ số)** có thể tổng hợp và cảnh báo:

**Micrometer** đóng vai trò là "SLF4J dành cho Metrics", cung cấp giao diện trừu tượng để ứng dụng gửi chỉ số tới Prometheus, Datadog hay InfluxDB:
- **Counter:** Đo lường các sự kiện tăng dần đều (monotonically increasing): số lần đăng nhập sai, số lần chạm rate limit, số đơn hàng thành công.
- **Timer / Histogram:** Đo lường độ trễ và phân phối thời gian (latency percentiles: P50, P95, P99).
- **Gauge:** Đo lường giá trị có thể tăng hoặc giảm tại một thời điểm: số kết nối database đang active, dung lượng heap memory đang dùng.

**Dimensional Tagging (Gắn nhãn theo chiều đo):**
Thay vì tạo ra hàng loạt metric tĩnh với tên khác nhau (ví dụ: `auth_fail_wrong_pass`, `auth_fail_locked_acc`), Prometheus khuyến nghị sử dụng **một metric duy nhất kèm các nhãn (tags)**:
- Tên metric: `app_auth_failures_total`
- Tag: `reason="invalid_credentials"`, `reason="account_locked"`, `reason="expired_token"`
Cách thiết kế này cho phép truy vấn PromQL linh hoạt:
- Tổng số lỗi auth: `sum(rate(app_auth_failures_total[5m]))`
- Lỗi do dò pass: `rate(app_auth_failures_total{reason="invalid_credentials"}[5m])`

### Ví dụ trong project này

Trong [ObservabilityMetrics.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/observability/ObservabilityMetrics.java):
```java
Counter authFailureInvalidCredentials = Counter.builder("app.auth.failures")
        .description("Authentication failures")
        .tag("reason", "invalid_credentials")
        .register(meterRegistry);

public void recordQuizSubmission(Long quizId, boolean passed) {
    Counter.builder("app.quiz.submissions")
            .tag("quiz_id", String.valueOf(quizId))
            .tag("passed", String.valueOf(passed))
            .register(meterRegistry)
            .increment();
}
```

### Câu hỏi phỏng vấn liên quan

### Câu trả lời ngắn gọn

Counter chỉ tăng (reset về 0 khi app restart), dùng để đếm sự kiện (request, lỗi auth, nộp bài). Gauge theo dõi giá trị tức thời biến thiên hai chiều (số kết nối DB, CPU, RAM). Trong Prometheus, luôn áp dụng hàm `rate()` hoặc `increase()` trên Counter, còn Gauge được đọc trực tiếp giá trị.

---

## 158. Non-blocking Consistent Snapshot with InnoDB MVCC (`--single-transaction`)

### Giải thích ngắn gọn

Trong môi trường Production có người dùng truy cập liên tục 24/7, việc sao lưu cơ sở dữ liệu nếu sử dụng cờ `--lock-all-tables` (khóa toàn bộ bảng) sẽ chặn đứng mọi câu lệnh `INSERT`, `UPDATE`, `DELETE` của học viên (ví dụ nộp bài quiz, tiến độ học, tạo đơn hàng thanh toán), gây ra downtime nghiêm trọng.

Engine **InnoDB** của MariaDB/MySQL giải quyết bài toán này nhờ kiến trúc **MVCC (Multi-Version Concurrency Control)** và cờ `--single-transaction`:
1. Khi bắt đầu dump, client gửi lệnh `START TRANSACTION WITH CONSISTENT SNAPSHOT` với transaction isolation level là `REPEATABLE READ`.
2. InnoDB tạo một snapshot nhất quán tại thời điểm giao dịch bắt đầu bằng cách sử dụng Undo Logs.
3. Trong suốt quá trình dump, các câu lệnh `SELECT` của `mariadb-dump` chỉ đọc dữ liệu từ snapshot đó.
4. Các ứng dụng khác vẫn tự do thực hiện ghi/sửa dữ liệu vào bảng mà không hề bị khóa (lock-free).
5. Kết hợp với `--quick` (đọc từng hàng thay vì tải cả bảng vào RAM) và `--hex-blob` (tránh lỗi ký tự nhị phân), hệ thống đạt được bản sao lưu nhất quán 100% với chi phí tài nguyên tối thiểu.

### Ví dụ trong project này

Trong [scripts/backup/backup.sh](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/scripts/backup/backup.sh):
```bash
DUMP_FLAGS=(
    "--single-transaction"
    "--quick"
    "--routines"
    "--triggers"
    "--events"
    "--default-character-set=utf8mb4"
    "--hex-blob"
    "--no-autocommit"
)
${DUMP_CMD} -h "${DB_HOST}" -P "${DB_PORT}" -u "${DB_USER}" -p"${DB_PASSWORD}" "${DUMP_FLAGS[@]}" "${DB_NAME}"
```

### Câu hỏi phỏng vấn liên quan

Tại sao `--single-transaction` chỉ đảm bảo tính nhất quán cho các bảng InnoDB mà không áp dụng được cho MyISAM?

### Câu trả lời ngắn gọn

Vì MyISAM không hỗ trợ giao dịch (transactions) và không có kiến trúc MVCC / Undo logs. Nếu dump bảng MyISAM với `--single-transaction`, các thao tác ghi đồng thời vẫn làm sai lệch dữ liệu; MyISAM bắt buộc phải dùng `--lock-tables`. Do đó, toàn bộ bảng nghiệp vụ production bắt buộc phải dùng engine InnoDB.

---

## 159. Ephemeral Cache vs Persistent State: The Redis Zero-Backup Strategy

### Giải thích ngắn gọn

Trong thiết kế kiến trúc hệ thống, nguyên tắc **Phân định Trạng thái (State Classification)** phân tách tài nguyên thành hai nhóm:
1. **Trạng thái Cốt lõi (Persistent State):** Dữ liệu không thể tái tạo nếu bị mất (tài khoản người dùng, đơn hàng, thanh toán, tiến độ học, token gia đình). Nhóm này **bắt buộc** phải được sao lưu định kỳ, mã hóa và lưu trữ ngoại biên (MariaDB).
2. **Dữ liệu Tạm thời (Ephemeral State / Volatile Cache):** Dữ liệu phái sinh, có thời gian sống ngắn (TTL), hoặc có thể tự động tính toán lại mà không ảnh hưởng tới tính toàn vẹn nghiệp vụ (bộ đếm rate limit, cache danh mục).

Trong Japanese Learning Platform:
- Redis chỉ được sử dụng cho `RedisRateLimiterService` với cấu trúc ZSET trượt cửa sổ **15 phút** và `RedisHealthIndicator` cho readiness probe.
- Toàn bộ phiên đăng nhập, JWT stateless và Refresh Token đều nằm trong bảng MariaDB `refresh_tokens`.
- **Zero-Backup Strategy:** Dữ liệu rate limit 15 phút không mang giá trị lịch sử và việc sao lưu định kỳ là hoàn toàn lãng phí tài nguyên. Nếu Redis bị restart hoặc sập, bộ đếm reset về 0 và tiếp tục phục vụ cửa sổ 15 phút tiếp theo mà không gây gián đoạn phiên người dùng hay thất thoát doanh thu.

### Ví dụ trong project này

Tại [docker-compose.prod.yml](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/docker-compose.prod.yml), Redis chỉ gắn volume `redis_data_prod:/data` để phục vụ khởi động lại nhanh cục bộ, nhưng hoàn toàn được miễn trừ khỏi pipeline sao lưu ngoại biên trong [DATABASE_BACKUP_RECOVERY.md](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/docs/DATABASE_BACKUP_RECOVERY.md).

### Câu hỏi phỏng vấn liên quan

Khi nào Redis cần được cấu hình RDB/AOF snapshot và backup định kỳ? Khi nào có thể áp dụng chiến lược Zero-Backup?

### Câu trả lời ngắn gọn

Nếu Redis được sử dụng làm Primary Data Store (ví dụ: lưu Session người dùng, Job Queue Celery/BullMQ, hay Shopping Cart chưa thanh toán), Redis bắt buộc phải được backup bền vững. Nếu Redis chỉ đóng vai trò L2 Cache hoặc Rate Limiter tạm thời có thể tái tạo tức thì, ta áp dụng Zero-Backup để tối ưu chi phí hạ tầng.

---

## 160. Cryptographic Checksum Integrity & Tamper Detection in Backup Pipelines

### Giải thích ngắn gọn

Một nguy cơ lớn trong việc lưu trữ sao lưu lâu dài là **Bit Rot (suy thoái dữ liệu âm thầm)** và **Tampering (bị mã độc tống tiền hoặc kẻ tấn công can thiệp sửa đổi)**. Nếu khôi phục một file sao lưu đã bị biến đổi cấu trúc, cơ sở dữ liệu có thể rơi vào trạng thái corrupt không thể cứu vãn.

Quy trình bảo vệ tính toàn vẹn đa lớp (Integrity Multi-layering):
1. **Mã băm SHA-256 độc lập:** Mỗi file sao lưu đã mã hóa `.sql.gz.enc` luôn được tính toán mã băm SHA-256 ngay sau khi xuất xưởng và lưu vào file companion `.sha256`.
2. **Pre-flight Integrity Verification:** Trước khi bất kỳ thao tác giải mã hay nạp dữ liệu nào được thực hiện, script `restore.sh` và `verify-restore.sh` bắt buộc tính lại mã SHA-256 của file hiện tại và so sánh với mã gốc. Nếu có sai lệch dù chỉ 1 bit (1 byte flipped), tiến trình lập tức dừng với mã lỗi `INTEGRITY_VIOLATION`.
3. **Mã hóa AES-256-CBC PBKDF2:** Khóa dẫn xuất qua 100,000 vòng lặp kèm Salt ngẫu nhiên ngăn ngừa hoàn toàn các cuộc tấn công Rainbow Table và bảo vệ bí mật dữ liệu khi lưu trữ tại S3 / Cloud Storage (Encryption at Rest).

### Ví dụ trong project này

Trong [scripts/backup/restore.sh](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/scripts/backup/restore.sh):
```bash
EXPECTED_HASH=$(awk '{print $1}' "${SHA_FILE}")
ACTUAL_HASH=$(sha256sum "${BACKUP_FILE}" | awk '{print $1}')
if [[ "${EXPECTED_HASH}" != "${ACTUAL_HASH}" ]]; then
    echo "[ERROR] CHECKSUM MISMATCH! Backup file is corrupted or tampered with." >&2
    exit 2
fi
```

### Câu hỏi phỏng vấn liên quan

Tại sao nên lưu mã băm SHA-256 của file đã mã hóa (`.enc.sha256`) thay vì lưu mã băm của file SQL gốc chưa mã hóa?

### Câu trả lời ngắn gọn

Bởi vì việc kiểm tra mã băm của file mã hóa có thể thực hiện ngay lập tức (Pre-flight check) trước khi giải mã. Nếu file bị hỏng hoặc bị giả mạo trên đường truyền/S3, hệ thống từ chối ngay mà không cần tốn tài nguyên CPU để giải mã một file rác, đồng thời tránh kích hoạt các lỗ hổng parser khi giải mã dữ liệu độc hại.

---

## 161. Isolated Restore Verification & Ephemeral Container Teardown

### Giải thích ngắn gọn

Nguyên tắc vàng của SRE: **"Một bản sao lưu chưa từng được khôi phục thử nghiệm thì không được coi là một bản sao lưu an toàn."**

Tuy nhiên, việc khôi phục thử nghiệm trực tiếp trên máy chủ Production là điều tối kỵ vì có thể gây gián đoạn dịch vụ hoặc ghi đè dữ liệu đang hoạt động. Giải pháp chuẩn là **Kiểm tra Phục hồi Cách ly (Isolated Verification)**:
1. **Container Tạm thời (Ephemeral Container):** Sử dụng Docker API để khởi tạo một container MariaDB độc lập kèm một Docker Network và Volume tạm thời ngẫu nhiên (`verify_$(date +%s)_$$`).
2. **Nạp & Thực thi Stream:** Giải mã và nạp bản backup vào container tạm này.
3. **Kiểm tra Tính toàn vẹn Ngữ nghĩa (Semantic Sanity Checks):**
   - Kiểm tra `flyway_schema_history` có đầy đủ các migration thành công hay không.
   - Kiểm tra sự hiện diện của đủ 13 bảng cốt lõi của ứng dụng.
   - Kiểm tra đếm dòng (`COUNT(*)`) trên các bảng quan trọng (`users`, `courses`, `lessons`).
   - Kiểm tra hiển thị đúng bảng mã UTF-8 tiếng Nhật (Kanji/Kana).
4. **Tự động Dọn dẹp Sạch sẽ (Auto Teardown):** Lệnh `trap cleanup EXIT` đảm bảo container và volume thử nghiệm bị tiêu hủy hoàn toàn, không để lại rác tài nguyên trên máy chủ.

### Ví dụ trong project này

Trong [scripts/backup/verify-restore.sh](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/scripts/backup/verify-restore.sh):
```bash
docker run -d --name "${VERIFY_CONTAINER}" --network "${VERIFY_NET}" \
    -e MARIADB_ROOT_PASSWORD="${TEMP_PASS}" -e MARIADB_DATABASE="japanese_learning_db" mariadb:10.11

# Stream giải mã trực tiếp vào container tạm
openssl enc -d -aes-256-cbc -pbkdf2 -iter 100000 "${OPENSSL_PASS_ARG[@]}" -in "${BACKUP_FILE}" \
    | gzip -d \
    | docker exec -i "${VERIFY_CONTAINER}" mariadb -uroot -p"${TEMP_PASS}" japanese_learning_db
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc kiểm tra cú pháp file dump bằng lệnh `grep` hay `head/tail` là chưa đủ để khẳng định bản backup hợp lệ?

### Câu trả lời ngắn gọn

Kiểm tra chuỗi chỉ xác nhận file có header/footer hợp lệ, nhưng không phát hiện được các lỗi ngữ nghĩa nghiêm trọng như: vi phạm khóa ngoại (foreign key constraint failure), kiểu dữ liệu không tương thích giữa các phiên bản MariaDB, hoặc dữ liệu bị lỗi encoding UTF-8. Chỉ có việc nạp thực tế vào một instance database độc lập mới chứng minh được bản backup sẵn sàng hoạt động 100%.

---

## 162. Flyway Migration Checksum Mismatch & Schema Repair Mechanics

### Giải thích ngắn gọn

Flyway là công cụ quản lý tiến hóa cấu trúc cơ sở dữ liệu (Database Schema Migration). Để đảm bảo tính bất biến (Immutability), Flyway tính toán mã băm **CRC32** cho mỗi file SQL migration (ví dụ `V8__hash_refresh_tokens.sql`) và lưu vào cột `checksum` của bảng `flyway_schema_history`.

**Nguyên nhân Checksum Mismatch:**
Khi một file migration đã từng được áp dụng vào database của lập trình viên, nếu sau đó file SQL đó bị sửa đổi nội dung (thêm comment, sửa câu lệnh, hoặc git revert giữa các branch), mã băm nội dung của file local sẽ thay đổi. Khi ứng dụng khởi động lại, Flyway so sánh checksum của file local với checksum trong CSDL; nếu khác nhau, Flyway lập tức ném ngoại lệ `FlywayValidateException` và dừng khởi động ứng dụng (Fail-fast).

**Cơ chế Sửa chữa (Repair Mechanics):**
1. **Lệnh `flyway:repair`:** Flyway cung cấp lệnh repair để căn chỉnh lại bảng `flyway_schema_history`, cập nhật lại checksum của các migration đã áp dụng cho khớp với file mã nguồn hiện tại.
2. **Cập nhật thủ công trong môi trường Dev:** Trong trường hợp không tích hợp Flyway CLI, lập trình viên có thể cập nhật trực tiếp checksum mới vào bảng:
   `UPDATE flyway_schema_history SET checksum = :newChecksum WHERE version = :version;`
3. **Nguyên tắc Production:** Tuyệt đối không bao giờ sửa đổi các file migration đã merge vào nhánh chính (`main`/`develop`) trên môi trường Production. Mọi thay đổi cấu trúc đều phải được thực hiện bằng một migration phiên bản mới (ví dụ `V11__...sql`).

### Ví dụ trong project này

Khi file `V8__hash_refresh_tokens.sql` bị lệch checksum giữa `802283801` (bản có `IF NOT EXISTS`) và `1094426290` (bản 24 dòng), bảng `flyway_schema_history` trên local dev được sửa chữa:
```sql
UPDATE japanese_learning_db.flyway_schema_history 
SET checksum = 1094426290 
WHERE version = '8';
```

### Câu hỏi phỏng vấn liên quan

Tại sao Flyway mặc định bật tính năng Validate Checksum và nghiêm cấm sửa đổi các migration đã chạy trong quá khứ?

### Câu trả lời ngắn gọn

Để bảo đảm tính tất định (determinism) của cơ sở dữ liệu. Nếu một file migration cũ bị thay đổi, các môi trường (Staging, Production, Dev của thành viên khác) sẽ có cấu trúc CSDL không đồng nhất, dẫn đến những lỗi dữ liệu tiềm ẩn cực kỳ khó debug. Thay vào đó, Flyway yêu cầu luôn tiến về phía trước (Forward-only migrations).

---

## 163. Least-Privilege Database User Architecture in Production

### Giải thích ngắn gọn

Nguyên tắc Phân quyền Tối thiểu (Principle of Least Privilege - PoLP) quy định rằng một đối tượng (user/tiến trình) chỉ được cấp chính xác các quyền hạn cần thiết để hoàn thành nhiệm vụ của nó, không thừa một quyền nào.

Trong kiến trúc CSDL Production:
- **Tài khoản Root/DBA (`root`):** Có toàn quyền tối cao trên toàn bộ máy chủ (`ALL PRIVILEGES ON *.* WITH GRANT OPTION`). Tài khoản này có thể drop database, shutdown server, đọc ghi file hệ điều hành (`LOAD_FILE`, `INTO OUTFILE`), và xem mọi schema hệ thống (`mysql`, `information_schema`).
- **Tài khoản Ứng dụng (`japanese_learning_user`):** Chỉ được cấp quyền trong phạm vi database của ứng dụng (`japanese_learning_db.*`):
  1. **Quyền DML (Data Manipulation Language):** `SELECT`, `INSERT`, `UPDATE`, `DELETE` để JPA/Hibernate thực hiện CRUD dữ liệu học viên, khóa học, đơn hàng.
  2. **Quyền DDL (Data Definition Language):** `CREATE`, `ALTER`, `INDEX`, `DROP`, `REFERENCES` để Flyway Migration tự động cập nhật bảng khi deploy phiên bản mới.
  3. **Không có quyền:** `SUPER`, `PROCESS`, `RELOAD`, `SHUTDOWN`, `FILE`, `GRANT OPTION`.

### Ví dụ trong project này

1. Cấp quyền trong MariaDB cho application user:
```sql
CREATE USER IF NOT EXISTS 'japanese_learning_user'@'%' IDENTIFIED BY 'YOUR_STRONG_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES 
ON japanese_learning_db.* TO 'japanese_learning_user'@'%';
FLUSH PRIVILEGES;
```
2. Bộ kiểm tra `ProductionSecurityValidator.java` từ chối khởi động nếu phát hiện backend dùng tài khoản root:
```java
if ("root".equalsIgnoreCase(datasourceUsername.trim())) {
    errors.add("[Database] 'spring.datasource.username' must not be 'root' in production. Use a dedicated least-privilege application user.");
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao không nên để ứng dụng web dùng tài khoản root kết nối CSDL, kể cả khi database chỉ nằm trong mạng nội bộ Docker?

### Câu trả lời ngắn gọn

Nếu ứng dụng gặp lỗ hổng SQL Injection hoặc thư viện bên thứ 3 bị dính Remote Code Execution (RCE), kẻ tấn công với quyền root có thể chiếm toàn quyền kiểm soát database server, đọc ghi file nhạy cảm trên host (như `/etc/passwd`), cài webshell, hoặc xóa sạch toàn bộ các CSDL khác trên cùng instance. Tài khoản phân quyền tối thiểu sẽ giới hạn phạm vi thiệt hại nghiêm ngặt trong một database duy nhất.

---

## 164. Container Non-Root Security (Container Breakout Mitigation)

### Giải thích ngắn gọn

Mặc định khi build Dockerfile, nếu không khai báo chỉ thị `USER`, tiến trình bên trong container sẽ chạy dưới tài khoản `root` (UID 0). Do container sử dụng chung nhân (Linux Kernel) với máy chủ host, UID 0 bên trong container ánh xạ trực tiếp tới UID 0 trên host (trừ khi bật User Namespaces).

**Rủi ro của Root Container:**
1. **Container Breakout:** Nếu kẻ tấn công khai thác được một lỗ hổng trong runtime (runc, containerd, kernel exploit) hoặc container mount nhầm Docker socket (`/var/run/docker.sock`), kẻ tấn công sẽ lập tức có toàn quyền root trên máy chủ vật lý bên ngoài.
2. **File Permission Tampering:** Tiến trình root có thể vô tình hoặc cố ý ghi đè các tệp nhạy cảm của hệ thống.

**Giải pháp Non-Root:**
Tạo một user và group chuyên biệt với UID/GID cố định không đặc quyền (ví dụ UID 10001), phân quyền sở hữu file jar cho user này, và kích hoạt `USER appuser:appgroup`.

### Ví dụ trong project này

Trong `backend/Dockerfile`:
```dockerfile
# Tạo non-root user và group (UID/GID 10001)
RUN apk add --no-cache curl && \
    addgroup -g 10001 -S appgroup && \
    adduser -u 10001 -S appuser -G appgroup

# Phân quyền tệp jar cho appuser
COPY --chown=appuser:appgroup ${JAR_FILE} app.jar

# Chuyển ngữ cảnh thực thi sang non-root
USER appuser:appgroup

# Tiến trình Java chạy dưới quyền UID 10001
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào để kiểm tra một Docker container đang chạy trên server có thực sự an toàn dưới quyền non-root hay không?

### Câu trả lời ngắn gọn

Sử dụng lệnh `docker inspect --format '{{.Config.User}}' <container_name>` để xem user được khai báo trong image, hoặc dùng lệnh `docker top <container_name>` trên host để xem cột UID của tiến trình đang chạy. Nếu UID khác 0 (ví dụ 10001 hoặc `appuser`), tiến trình đang chạy an toàn ở chế độ non-root.

---

## 165. Network Exposure Reduction & Reverse Proxy Binding Strategy

### Giải thích ngắn gọn

Trong kiến trúc triển khai microservices/web app nhiều tầng:
1. **Giảm thiểu bề mặt tấn công (Attack Surface Reduction):** Những dịch vụ thuần túy nội bộ như Database (MariaDB 3306) và Caching (Redis 6379) tuyệt đối không được publish cổng ra ngoài host vật lý (không dùng `ports: - "3306:3306"`). Thay vào đó, chúng chỉ cần kết nối qua mạng nội bộ Docker (`networks: - app-network-prod`) và dùng chỉ thị `expose: - "3306"` để backend trong cùng mạng truy cập được qua tên miền nội bộ `db:3306`.
2. **Chiến lược Ràng buộc Localhost (Localhost Binding Strategy):**
   - Nếu bind cổng backend ở dạng `ports: - "8080:8080"`, Docker mặc định sẽ lắng nghe trên mọi network interface (`0.0.0.0:8080`). Bất kỳ ai trên Internet đều có thể gửi request trực tiếp tới port 8080 qua HTTP plain-text, bỏ qua hoàn toàn tầng SSL/TLS, bộ lọc WAF và Rate Limiting của Nginx/Caddy.
   - Khi cấu hình `ports: - "127.0.0.1:8080:8080"`, chỉ có các tiến trình cục bộ trên cùng máy chủ (chính là Reverse Proxy Nginx/Caddy) mới có thể chuyển tiếp request vào backend. Toàn bộ người dùng ngoài Internet bắt buộc phải đi qua Reverse Proxy trên cổng 443 (HTTPS).

### Ví dụ trong project này

Trong `docker-compose.prod.yml`:
```yaml
services:
  db:
    expose:
      - "3306" # Không mở cổng ra host, chỉ nội bộ app-network-prod
      
  redis:
    expose:
      - "6379" # Không mở cổng ra host
      
  backend:
    ports:
      - "${BACKEND_BIND_IP:-127.0.0.1}:8080:8080" # Chỉ bind localhost
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc bỏ publish port 3306 của MariaDB ra host lại bảo vệ hệ thống tốt hơn việc chỉ đặt mật khẩu root thật mạnh?

### Câu trả lời ngắn gọn

Theo nguyên lý Phòng thủ theo chiều sâu (Defense-in-Depth): Mật khẩu mạnh chỉ là một lớp bảo vệ. Nếu mở cổng 3306 ra ngoài, database sẽ liên tục bị botnet quét cổng, tấn công brute-force mật khẩu làm nghẽn CPU/băng thông, hoặc bị khai thác các lỗ hổng zero-day trong giao thức MariaDB handshake trước cả khi xác thực. Khi không publish port ra host, kẻ tấn công hoàn toàn không có cách nào gửi gói tin TCP tới cổng 3306 từ Internet.

---

## 166. Container Resource Limits: Limits vs Reservations in Production Orchestration

### Giải thích ngắn gọn

Khi chạy nhiều container trên cùng một máy chủ VPS mà không đặt giới hạn tài nguyên, một container bị rò rỉ bộ nhớ (Memory Leak) hoặc bị vòng lặp vô tận (Infinite Loop) có thể ngốn 100% CPU và RAM của cả máy chủ, khiến hệ điều hành kích hoạt Linux Kernel OOM Killer tiêu diệt ngẫu nhiên các tiến trình quan trọng khác (bao gồm cả SSH daemon hoặc database).

Docker Compose hỗ trợ khai báo tài nguyên theo chuẩn Compose Specification:
- **`reservations` (Mức bảo đảm / Requests):** Mức tài nguyên tối thiểu mà máy chủ cam kết dành riêng cho container. Nếu máy chủ không đủ dung lượng này, container sẽ không được cấp phát khởi động.
- **`limits` (Trần tối đa / Limits):** Mức tài nguyên tối đa mà container được phép sử dụng:
  - **CPU Limit:** Sử dụng cgroups CFS (Completely Fair Scheduler) quota để giới hạn chu kỳ CPU. Nếu container vượt quá limit, CPU sẽ bị điều tiết (throttling) chậm lại chứ không bị kill.
  - **Memory Limit:** Sử dụng cgroups memory limit. Nếu tiến trình bên trong container vượt quá giới hạn RAM này, kernel sẽ gửi tín hiệu `SIGKILL` tiêu diệt tiến trình (OOMKilled) để bảo vệ toàn bộ phần còn lại của hệ thống.

### Ví dụ trong project này

Trong `docker-compose.prod.yml`:
```yaml
backend:
  deploy:
    resources:
      limits:
        cpus: '1.5'
        memory: 1024M
      reservations:
        cpus: '0.5'
        memory: 512M

db:
  deploy:
    resources:
      limits:
        cpus: '1.0'
        memory: 512M
      reservations:
        cpus: '0.25'
        memory: 256M
```

### Câu hỏi phỏng vấn liên quan

Java 21 trong container nhận diện giới hạn bộ nhớ (Memory Limit) của Docker như thế nào để tính toán kích thước Heap (`-Xmx`)?

### Câu trả lời ngắn gọn

Từ Java 10 trở đi và hoàn thiện ở Java 21, JVM mặc định kích hoạt cờ `-XX:+UseContainerSupport`. JVM tự động đọc các file cgroups của Linux (`/sys/fs/cgroup/memory.max` trên cgroups v2) để biết trần RAM của container thay vì đọc tổng RAM của máy chủ host. Từ đó, JVM tự động thiết lập kích thước Max Heap mặc định (khoảng 25% đến 50% memory limit của container) mà không cần cấu hình cờ `-Xmx` thủ công.

---

## 167. Secret Scanning & Allowlist Strategy (Gitleaks in CI/CD Pipelines)

### Giải thích ngắn gọn

Trong quy trình phát triển phần mềm, việc vô tình đẩy (commit/push) thông tin nhạy cảm (API keys, Private Keys, Database Passwords, JWT Secrets) lên Git repository là một trong những nguyên nhân hàng đầu dẫn đến vi phạm bảo mật dữ liệu. Một khi secret đã vào git history, việc xóa bằng `git commit` mới là hoàn toàn vô hiệu vì git tree vẫn lưu giữ commit cũ.

**Gitleaks** là công cụ phân tích tĩnh chuyên dụng phân tích cú pháp regex và entropy để phát hiện các secret bị rò rỉ:
1. **Quét tự động trong CI:** Chạy trên mỗi Pull Request và commit vào nhánh chính. Nếu phát hiện rò rỉ, pipeline lập tức đánh fail để chặn code không được merge.
2. **Chiến lược Allowlist an toàn (`.gitleaks.toml`):**
   - Trong quá trình viết unit test và integration test, lập trình viên bắt buộc phải dùng các chuỗi giả lập (mock secret, test tokens).
   - Nếu không có cấu hình allowlist, scanner sẽ liên tục báo động giả (False Positive) và chặn pipeline.
   - File `.gitleaks.toml` cho phép cấu hình loại trừ cụ thể theo đường dẫn file kiểm thử (`backend/src/test/**`, `*.spec.ts`, `docs/**`) hoặc theo giá trị cụ thể, trong khi vẫn áp dụng 100% quy tắc rà soát cho mã nguồn production.
3. **Log Redaction:** Luôn chạy gitleaks với cờ `--redact` để nếu phát hiện chuỗi secret vi phạm, công cụ sẽ ẩn đi các ký tự nhạy cảm, tránh việc màn hình CI log công khai lại trở thành nguồn rò rỉ secret.

### Ví dụ trong project này

Trong `.gitleaks.toml`:
```toml
title = "Japanese Learning Platform Gitleaks Configuration"

[extend]
useDefault = true

[allowlist]
description = "Ignore mock secrets and fixture credentials in tests, docs, and templates"
paths = [
  '''(?i)backend/src/test/.*''',
  '''(?i)frontend/src/.*\.spec\.(js|ts)''',
  '''(?i)docs/.*''',
  '''(?i)\.env\..*\.example$'''
]
```

Trong `.github/workflows/ci.yml`:
```yaml
- name: Run Gitleaks Secret Scan
  uses: gitleaks/gitleaks-action@v2
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    GITLEAKS_CONFIG: .gitleaks.toml
    GITLEAKS_ARGS: --verbose --redact
```

### Câu hỏi phỏng vấn liên quan

Tại sao cần chạy Secret Scanning cả ở local (Pre-commit Hook) và trên GitHub Actions CI Runner?

### Câu trả lời ngắn gọn

Theo nguyên tắc Defense-in-Depth: Pre-commit hook chạy trên máy lập trình viên giúp ngăn chặn secret ngay trước khi tạo commit (Shift Left Security). Tuy nhiên, pre-commit hook có thể bị lập trình viên bỏ qua bằng cờ `--no-verify`, hoặc máy trạm chưa cài đặt công cụ. Do đó, CI runner đóng vai trò người gác cổng tối cao không thể bị can thiệp (Authoritative Gatekeeper), đảm bảo 100% commit đẩy lên kho lưu trữ chung đều phải trải qua kiểm tra tự động.

---

## 168. Software Composition Analysis (SCA) & CVE Exception Management (.trivyignore with Compensating Controls)

### Giải thích ngắn gọn

Khoảng 80-90% mã nguồn trong một ứng dụng doanh nghiệp hiện đại thực chất là các thư viện nguồn mở bên thứ ba (Third-party dependencies). **Software Composition Analysis (SCA)** là quá trình tự động kiểm tra danh mục các thư viện phụ thuộc (pom.xml, package.json, Docker Base Images) đối chiếu với các cơ sở dữ liệu lỗ hổng bảo mật toàn cầu (NVD, GitHub Advisory Database) để tìm ra các mã **CVE (Common Vulnerabilities and Exposures)**.

**Quản lý ngoại lệ với Cơ chế bù đắp (Compensating Controls):**
- Trong thực tế, khi một CVE mới được công bố trên một thư viện bắc cầu (transitive dependency) như Tomcat hay Netty, Spring Boot có thể chưa phát hành bản vá ngay lập tức.
- Nếu pipeline CI cấu hình chặn cứng mọi CVE, toàn bộ quy trình release sản phẩm sẽ bị tê liệt hoàn toàn (Build Block).
- Kỹ sư không được phép tắt scanner hay hạ mức severity xuống thấp. Giải pháp chuẩn mực doanh nghiệp là lập **Tệp phê duyệt ngoại lệ (`.trivyignore`)**:
  - Ghi rõ mã CVE, ngày phê duyệt, và tài liệu phân tích chi tiết.
  - Trình bày **Biện pháp Kiểm soát Bù đắp (Compensating Control)** chứng minh rằng luồng thực thi của ứng dụng không kích hoạt đoạn code bị lỗi (Unreachable vulnerable path), ví dụ: Tomcat bị lỗi Form Auth nhưng hệ thống dùng Stateless JWT, hoặc Netty bị lỗi TLS nhưng chỉ giao tiếp Redis nội bộ.

### Ví dụ trong project này

Trong `.trivyignore`:
```text
# CVE-2026-43512: Apache Tomcat Digest Authentication timing attack
# Approved: 2026-10-28
# Compensating Control: System uses stateless JWT authentication via Spring Security FilterChain.
# Tomcat's built-in Digest Authenticator is never invoked or configured.
CVE-2026-43512

# CVE-2026-75595: Netty SNI hostname validation bypass
# Approved: 2026-10-28
# Compensating Control: Netty is used strictly internally by Lettuce Redis client.
# No public-facing TLS listeners terminate on Netty; public TLS is terminated at Nginx.
CVE-2026-75595
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc phân biệt giữa Direct Dependency và Transitive Dependency lại quan trọng trong việc khắc phục lỗ hổng SCA?

### Câu trả lời ngắn gọn

- **Direct Dependency:** Là thư viện ứng dụng trực tiếp khai báo (ví dụ `spring-boot-starter-web`). Ta có thể sửa bằng cách nâng phiên bản trong `pom.xml`.
- **Transitive Dependency:** Là thư viện được kéo theo gián tiếp bởi thư viện cha (ví dụ `tomcat-embed-core`). Việc tự ý ghi đè phiên bản transitive dependency (qua `<dependencyManagement>`) có thể phá vỡ tính tương thích nhị phân (Binary Incompatibility) và gây lỗi runtime (`NoSuchMethodError`, `ClassNotFoundException`). Do đó cần đánh giá kỹ lưỡng và chỉ áp dụng exception khi có biện pháp bù đắp an toàn.

---

## 169. GitHub Actions Service Containers for Real Database Integration Tests (MariaDB vs H2 InMemory)

### Giải thích ngắn gọn

Trong kiểm thử phần mềm, có sự đánh đổi lớn giữa **Tốc độ (Speed)** và **Độ tin cậy thực tế (Fidelity)**:
- **H2 In-Memory Database:** Rất nhanh, khởi tạo trong tích tắc, thích hợp cho Unit Tests chạy liên tục. Nhưng H2 không tương thích 100% với MariaDB/MySQL về:
  - Cú pháp Migration nâng cao (DDL statements, character set collations).
  - Khả năng xử lý chuỗi Unicode 4-byte (`utf8mb4_unicode_ci`) lưu trữ emoji hoặc Hán tự hiếm.
  - Các hàm đặc thù của MariaDB (`JSON_EXTRACT`, `REGEXP_SUBSTR`) và cách đánh chỉ mục (Index storage engines).
- **GitHub Actions Service Containers:**
  - Cho phép GitHub Actions runner tự động khởi chạy một container Docker độc lập chạy song song với job kiểm thử (ví dụ: `image: mariadb:10.11`).
  - Cung cấp cơ sở dữ liệu thật với cấu hình production chính xác cho các bài kiểm thử tích hợp (Integration Tests).

**Chiến lược Test Isolation bằng Biến Môi Trường:**
- Bộ test tích hợp MariaDB thật (`RealMariaDbIntegrationIT`) cần truy cập container MariaDB đang chạy.
- Để bài test không làm sập quy trình build cục bộ của dev (khi chưa bật Docker container), sử dụng chú thích `@EnabledIfEnvironmentVariable(named = "RUN_MARIADB_IT", matches = "true")`.
- Trong pipeline CI, biến này được thiết lập `true`, kích hoạt kiểm thử toàn diện 10 bản Flyway migrations và encoding tiếng Nhật.

### Ví dụ trong project này

Trong `.github/workflows/ci.yml`:
```yaml
mariadb-integration:
  runs-on: ubuntu-latest
  services:
    mariadb:
      image: mariadb:10.11
      env:
        MARIADB_ROOT_PASSWORD: it_root_password
        MARIADB_DATABASE: japanese_learning_it_db
        MARIADB_USER: it_user
        MARIADB_PASSWORD: it_password
      ports:
        - 3306:3306
      options: --health-cmd="healthcheck.sh --connect --innodb_initialized" --health-interval=10s ...
  steps:
    - name: Run Real MariaDB Integration Tests
      env:
        RUN_MARIADB_IT: "true"
        DB_HOST: 127.0.0.1
        DB_PORT: "3306"
      run: mvn -B test -Dtest=RealMariaDbIntegrationIT
```

Trong Java test:
```java
@SpringBootTest
@ActiveProfiles("mariadb-it")
@EnabledIfEnvironmentVariable(named = "RUN_MARIADB_IT", matches = "true")
class RealMariaDbIntegrationIT {
    @Test
    void flywayMigrations_AreAllSuccessfullyAppliedOnMariaDb() { ... }
}
```

### Câu hỏi phỏng vấn liên quan

Testcontainers trong Java khác gì so với GitHub Actions Service Containers? Khi nào nên chọn giải pháp nào?

### Câu trả lời ngắn gọn

- **Testcontainers:** Thư viện Java tự động điều khiển Docker daemon từ bên trong mã test code (`@Testcontainers`). Ưu điểm: Lập trình viên chạy lệnh `mvn test` trên máy cá nhân sẽ tự động pull và bật container nếu máy có Docker, tính đóng gói cao. Nhược điểm: Cần quyền truy cập Docker socket (`/var/run/docker.sock`), có thể gây phức tạp trong môi trường container-in-container hoặc Podman không có root socket.
- **GitHub Actions Service Containers:** Được quản lý trực tiếp bởi hạ tầng GitHub Actions Workflow (`services:` block). Ưu điểm: Tận dụng cơ chế network bridge nguyên bản của CI, nhẹ hơn, không phụ thuộc vào Docker socket bên trong Java tiến trình, cực kỳ ổn định và tương thích tốt trên mọi runner Linux.

---

## 170. Automated API Smoke Testing & High-Traffic Load Gates (p95/p99 Latency SLAs & Concurrency Control)

### Giải thích ngắn gọn

Sau khi mã nguồn đã vượt qua Unit Test và Build artifact thành công, hệ thống vẫn có thể gặp lỗi khi chạy thực tế (ví dụ: xung đột cấu hình YAML, lỗi kết nối cơ sở dữ liệu thật, lỗi serialization Jackson DTO, hoặc sụt giảm hiệu năng nghiêm trọng do N+1 query).

1. **API Smoke Testing (Kiểm thử Khói Tự động):**
   - Chạy một tập hợp nhỏ các HTTP requests nhanh nhằm xác nhận các tính năng cốt lõi nhất của ứng dụng đang sống và hoạt động đúng trước khi tiến hành các bước kiểm thử sâu hơn.
   - Kiểm tra: Actuator Health (`/actuator/health`), Liveness/Readiness Probes, OpenAPI Documentation (`/api-docs`), Public Read Endpoints (`/api/v1/courses`), và cấu trúc JSON chuẩn của Error Envelope khi gửi sai thông tin đăng nhập (HTTP 401).
2. **Controlled Load Testing Gate (Cổng Kiểm soát Tải):**
   - Thay vì chờ đến khi triển khai lên production mới phát hiện endpoint bị nghẽn, pipeline CI kích hoạt một bài kiểm tra chịu tải nhỏ có kiểm soát (ví dụ: 10 concurrent clients, 200 requests) nhắm vào các read-endpoint tần suất cao.
   - **Quality Gate SLA Thresholds:**
     - Tỷ lệ lỗi (Error Rate): Bắt buộc `0.00%`.
     - Phân vị 95 (p95 Latency): `≤ 300ms`.
     - Phân vị 99 (p99 Latency): `≤ 600ms`.
   - Nếu bất kỳ chỉ số nào vượt ngưỡng, CI sẽ fail ngay lập tức, ngăn chặn các đoạn mã gây suy thoái hiệu năng (Performance Regression) đi vào nhánh chính.

### Ví dụ trong project này

Trong `scripts/ci/load_test.mjs`:
```javascript
const SLA_MAX_ERROR_RATE = 0.00;
const SLA_MAX_P95_MS = 300;
const SLA_MAX_P99_MS = 600;

const p95 = sortedLatencies[Math.floor(sortedLatencies.length * 0.95)];
const p99 = sortedLatencies[Math.floor(sortedLatencies.length * 0.99)];

if (errorRate > SLA_MAX_ERROR_RATE || p95 > SLA_MAX_P95_MS || p99 > SLA_MAX_P99_MS) {
  console.error("❌ SLA QUALITY GATE BREACHED");
  process.exit(1);
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao trong kiểm thử hiệu năng API, độ trễ Percentile (p95, p99) lại có giá trị phát hiện lỗi cao hơn nhiều so với độ trễ Trung bình (Mean/Average Latency)?

### Câu trả lời ngắn gọn

Giá trị trung bình (Average) có xu hướng làm phẳng và che giấu các bất thường: Nếu 99 request phản hồi trong 10ms nhưng 1 request bị khóa database (Deadlock) mất 10,000ms, độ trễ trung bình chỉ là ~110ms (trông vẫn chấp nhận được). Tuy nhiên, đối với 1% người dùng gặp độ trễ 10s, đó là một trải nghiệm ứng dụng bị treo. Phân vị p95 và p99 phản ánh chính xác các điểm nghẽn nghiêm trọng nhất (Outliers) gây ra bởi GC pauses, Thread pool starvation, hoặc Database connection pool exhaustion.

---

## 171. JPA/Hibernate N+1 Query Problem & Batch Retrieval Architectures (@EntityGraph vs Batch IN Queries)

### Giải thích ngắn gọn

Vấn đề N+1 Query là một trong những nguyên nhân phổ biến nhất gây sụp đổ hiệu năng trong các ứng dụng sử dụng ORM (Hibernate/Spring Data JPA).

1. **Bản chất của N+1 Query:**
   - **Lazy Loading N+1:** Khi truy vấn danh sách $N$ đối tượng cha (ví dụ: $N$ bản ghi `QuizAttempt`), sau đó trong quá trình tuần tự hóa DTO hoặc logic xử lý, mã nguồn truy cập vào thực thể con lười (`a.getQuiz().getTitle()`). Hibernate sẽ phát sinh thêm $N$ câu truy vấn `SELECT * FROM quizzes WHERE id = ?` độc lập. Tổng số truy vấn gửi tới cơ sở dữ liệu là $1 + N$.
   - **Loop Repository N+1 ($1 + 3N$):** Lập trình viên truy vấn danh sách $N$ bài quiz của một bài học, sau đó dùng vòng lặp `for (Quiz q : quizzes)` để gọi các hàm repository đơn lẻ: `questionRepo.countByQuizId(q.getId())`, `attemptRepo.findLatest(...)`, `attemptRepo.count(...)`. Hệ thống phải thực thi $1 + 3N$ truy vấn mạng riêng biệt. Với một bài học có 5 quiz, backend thực thi tới 16 truy vấn SQL liên tiếp.

2. **Chiến lược xử lý triệt để:**
   - **`@EntityGraph`:** Khai báo danh sách các trường cần nạp sẵn (`attributePaths = {"quiz"}`) ngay trên phương thức Spring Data JPA. Hibernate tự động tạo câu lệnh SQL với `LEFT OUTER JOIN`, nạp toàn bộ thực thể liên kết chỉ trong **1 câu truy vấn duy nhất** mà vẫn giữ cấu trúc quan hệ JPA sạch sẽ.
   - **Batch Aggregations với Mệnh đề `IN`:** Với các nhu cầu thống kê (như đếm số câu hỏi, đếm số lượt thi), thay vì gọi đếm trong vòng lặp, ta viết các truy vấn gộp lô:
     ```sql
     SELECT q.quiz.id, COUNT(q) FROM Question q WHERE q.quiz.id IN :quizIds GROUP BY q.quiz.id
     ```
     Kết hợp với Java Stream API (`Collectors.toMap`), ta ánh xạ kết quả vào bộ nhớ (`Map<Long, Long>`), giảm số lượng round-trip từ $1 + 3N$ xuống còn **4 truy vấn cố định $O(1)$** trên toàn bộ request.

### Ví dụ trong project này

Trong `QuestionRepository.java` và `QuizLearningServiceImpl.java` (#382):
```java
// 1. Repository: Truy vấn gộp nhóm bằng IN clause
@Query("SELECT q.quiz.id, COUNT(q) FROM Question q WHERE q.quiz.id IN :quizIds GROUP BY q.quiz.id")
List<Object[]> countQuestionsByQuizIds(@Param("quizIds") Collection<Long> quizIds);

// 2. Service: Thực thi 4 truy vấn cố định O(1) thay vì 1 + 3N trong vòng lặp
List<Quiz> quizzes = quizRepository.findByLessonIdAndStatus(lessonId, QuizStatus.PUBLISHED);
List<Long> quizIds = quizzes.stream().map(Quiz::getId).toList();

// Tải hàng loạt số câu hỏi và attempts trong 3 batch queries
Map<Long, Long> questionCounts = questionRepository.countQuestionsByQuizIds(quizIds).stream()
    .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

Map<Long, Long> attemptCounts = quizAttemptRepository.countAttemptsByUserIdAndQuizIds(user.getId(), quizIds).stream()
    .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

Map<Long, QuizAttempt> latestAttempts = quizAttemptRepository.findByUserIdAndQuizIdInOrderByStartedAtDesc(user.getId(), quizIds).stream()
    .collect(Collectors.groupingBy(a -> a.getQuiz().getId(), Collectors.collectingAndThen(Collectors.toList(), list -> list.get(0))));
```

### Câu hỏi phỏng vấn liên quan

Sự khác biệt giữa `@EntityGraph` và `JOIN FETCH` trong JPQL là gì? Khi nào nên dùng giải pháp nào?

### Câu trả lời ngắn gọn

- **`JOIN FETCH`:** Được viết trực tiếp trong câu lệnh JPQL (ví dụ: `SELECT a FROM QuizAttempt a JOIN FETCH a.quiz`). Mặc định tạo phép `INNER JOIN`, ghi đè hoàn toàn chiến lược fetch type của entity và bắt buộc định nghĩa câu truy vấn tĩnh.
- **`@EntityGraph`:** Được Spring Data JPA cung cấp dưới dạng annotation đặt trên repository method (ví dụ: `@EntityGraph(attributePaths = {"quiz"})`). Hibernate tự động sinh `LEFT OUTER JOIN`, cho phép tái sử dụng các derived query methods có sẵn (`findByUserIdOrderByStartedAtDesc`) mà không cần viết lại toàn bộ câu lệnh JPQL, đồng thời hỗ trợ nạp nhiều nhánh quan hệ một cách linh hoạt.

---

## 172. Compound Indexing Architecture & Eliminating Database 'Using filesort' (MySQL/MariaDB B-Tree Optimization)

### Giải thích ngắn gọn

Trong MySQL/MariaDB, cây chỉ mục B-Tree lưu trữ các khóa theo thứ tự sắp xếp sẵn. Khi một câu lệnh truy vấn có cả mệnh đề lọc (`WHERE`) và mệnh đề sắp xếp (`ORDER BY`), kiến trúc chỉ mục quyết định trực tiếp việc database có thể đọc luồng dữ liệu ngay hay phải dừng lại để sắp xếp trên bộ nhớ đệm (`Using filesort`).

1. **Hiện tượng `Using filesort`:**
   - Giả sử bảng `quiz_attempts` có index trên cột `user_id`.
   - Khi chạy câu lệnh:
     ```sql
     SELECT * FROM quiz_attempts WHERE user_id = 10 ORDER BY started_at DESC;
     ```
   - Cơ sở dữ liệu dùng index `user_id` để lọc ra tất cả các bản ghi của user 10. Nhưng các bản ghi này không được sắp xếp theo `started_at`.
   - Động cơ InnoDB buộc phải đọc các dòng này vào một vùng nhớ tạm gọi là **`sort_buffer`** và chạy thuật toán QuickSort/MergeSort để sắp xếp lại kết quả trước khi trả về. Nếu tập dữ liệu lớn vượt quá `sort_buffer_size`, nó phải ghi các file tạm xuống ổ cứng (Disk I/O filesort), làm CPU tăng đột biến và độ trễ tăng vọt.

2. **Giải pháp Chỉ mục Hỗn hợp (Composite Index):**
   - Đánh chỉ mục hỗn hợp đa cột theo quy tắc Left-to-Right: Cột lọc trước, cột sắp xếp sau:
     ```sql
     CREATE INDEX idx_quiz_attempts_user_started ON quiz_attempts(user_id, started_at DESC);
     ```
   - Lúc này, với mỗi giá trị `user_id`, các nút con trong cây B-Tree đã được sắp xếp sẵn theo thứ tự giảm dần của `started_at`.
   - Cơ sở dữ liệu chỉ việc nhảy tới nhánh `user_id = 10` và duyệt tuần tự các nút từ trên xuống dưới (Index Range Scan), trả về kết quả ngay lập tức mà **hoàn toàn không cần filesort**.

### Ví dụ trong project này

Trong Flyway migration `V11__performance_indexes.sql`:
```sql
-- Khử filesort cho lịch sử làm bài kiểm tra học viên
CREATE INDEX idx_quiz_attempts_user_started ON quiz_attempts(user_id, started_at DESC);

-- Khử filesort khi tải cây giáo trình theo thứ tự chương
CREATE INDEX idx_course_sections_course_sort ON course_sections(course_id, sort_order);

-- Khử filesort cho dòng thời gian hoạt động học tập
CREATE INDEX idx_learning_activities_user_created ON learning_activities(user_id, created_at DESC);

-- Khử filesort cho lịch sử đơn hàng của học viên
CREATE INDEX idx_orders_user_created ON orders(user_id, created_at DESC);
```

### Câu hỏi phỏng vấn liên quan

Tại sao thứ tự các cột trong Composite Index (Chỉ mục kết hợp) lại mang tính quyết định sống còn đến hiệu quả của câu truy vấn?

### Câu trả lời ngắn gọn

Composite Index tuân thủ **Quy tắc Tiền tố Trái nhất (Leftmost Prefix Rule)**. Một chỉ mục trên `(A, B)` có thể phục vụ cho truy vấn `WHERE A = ?` và `WHERE A = ? ORDER BY B`, nhưng **hoàn toàn vô dụng** đối với truy vấn chỉ tìm kiếm theo `WHERE B = ?`. Do đó, nguyên tắc vàng khi thiết kế chỉ mục kết hợp là: Cột có độ chọn lọc cao (Equality Filters) đặt ở vị trí đầu tiên, tiếp theo là cột dùng để sắp xếp (`ORDER BY`) hoặc điều kiện dải (`Range Filters > < BETWEEN`).

---

## 173. HikariCP Connection Pool Sizing Methodology & Leak Detection Mechanics

### Giải thích ngắn gọn

Database Connection Pool quản lý tập hợp các kết nối JDBC được mở sẵn để ứng dụng tái sử dụng thay vì phải tốn chi phí đắt đỏ của việc bắt tay 3 bước TCP và xác thực bảo mật mỗi khi có request tới.

1. **Công thức Định cỡ Connection Pool (HikariCP / PostgreSQL Formula):**
   $$\text{pool\_size} = (\text{core\_count} \times 2) + \text{effective\_spindle\_count}$$
   - Trong môi trường container 2 vCPU gắn ổ cứng thể rắn SSD (NVMe), công thức cơ bản cho ra: $(2 \times 2) + 1 = 5$ kết nối đồng thời trên mỗi instance.
   - Để dự phòng đột biến lưu lượng (Spikes) cho hệ thống vừa đọc vừa làm bài kiểm tra (Write-burst), kích thước pool chuẩn cho mỗi API pod được định mức ở **15 - 20 kết nối**.
   - Cấu hình pool quá lớn (ví dụ 100 kết nối) không làm ứng dụng nhanh hơn mà gây nghẽn phần cứng (Hardware Throttling) do tranh chấp CPU Context Switch và nghẽn Disk I/O.

2. **Các Tham số Cốt lõi & Quản lý Vòng đời Kết nối:**
   - **`maximum-pool-size: 20`**: Số lượng kết nối tối đa có thể mở tới MariaDB.
   - **`minimum-idle: 10`**: Số kết nối nhàn rỗi luôn được duy trì sẵn sàng nhận việc.
   - **`connection-timeout: 20000ms` (20s)**: Thời gian tối đa một thread chờ lấy kết nối từ pool trước khi ném ngoại lệ `SQLException`, ngăn chặn tình trạng treo thread vô thời hạn.
   - **`max-lifetime: 1200000ms` (20 phút)**: Tuổi thọ tối đa của một kết nối. Giá trị này **bắt buộc phải nhỏ hơn `wait_timeout` của MariaDB** (thường là 8 giờ hoặc 30 phút trong cloud DB) từ 2-5 phút để đảm bảo HikariCP chủ động đóng và tạo mới kết nối trước khi server tự ngắt, triệt tiêu lỗi "Communications link failure".
   - **`leak-detection-threshold: 5000ms` (5s)**: Nếu một luồng mượn kết nối quá 5 giây mà chưa hoàn trả (chưa commit/rollback), HikariCP lập tức ghi log `WARN` kèm theo toàn bộ Stacktrace nơi đã mượn kết nối.

3. **Nguyên tắc `open-in-view: false`:**
   - Khi bật `OSIV = true` (mặc định của Spring Boot), Hibernate Session được giữ mở xuyên suốt quá trình Controller render JSON cho client. Nếu việc chuyển đổi JSON hoặc mạng client bị chậm, kết nối DB bị chiếm giữ vô ích.
   - Tắt `open-in-view: false` đảm bảo kết nối DB được giải phóng ngay khi Service method kết thúc transaction, giải phóng tài nguyên tức thì cho các request khác.

### Ví dụ trong project này

Trong `application-prod.yml` (#382):
```yaml
spring:
  jpa:
    open-in-view: false
  datasource:
    hikari:
      pool-name: JapaneseLearningProdPool
      maximum-pool-size: ${HIKARI_MAX_POOL_SIZE:20}
      minimum-idle: ${HIKARI_MIN_IDLE:10}
      connection-timeout: ${HIKARI_CONNECTION_TIMEOUT:20000}
      idle-timeout: ${HIKARI_IDLE_TIMEOUT:300000}
      max-lifetime: ${HIKARI_MAX_LIFETIME:1200000}
      leak-detection-threshold: ${HIKARI_LEAK_DETECTION:5000}
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc thiết lập `max-lifetime` của HikariCP lớn hơn `wait_timeout` của MariaDB/MySQL lại là một sai lầm chết người trong production?

### Câu trả lời ngắn gọn

Nếu `wait_timeout` của MariaDB là 600s mà `max-lifetime` của HikariCP là 900s, sau 600s không có truy vấn, MariaDB server sẽ đơn phương ngắt kết nối socket ở phía nó (TCP FIN). Tuy nhiên, pool HikariCP vẫn tưởng kết nối đó đang sống. Khi có một request của người dùng mượn đúng kết nối này để thực thi truy vấn, driver sẽ ném ra lỗi `Communications link failure / Connection closed by foreign host`, làm người dùng bị lỗi 500. `max-lifetime` phải luôn ngắn hơn `wait_timeout` ít nhất vài phút để HikariCP chủ động thu hồi và tái tạo kết nối sạch.

---

## 174. Spring Transactional Read-Only Optimization & Hibernate Dirty Checking Bypass

### Giải thích ngắn gọn

Trong Spring Boot, việc gắn chú thích `@Transactional(readOnly = true)` trên các phương thức chỉ đọc dữ liệu (như hiển thị Dashboard, tìm kiếm giáo trình, báo cáo thống kê) mang lại những lợi ích tối ưu hóa hiệu năng vượt bậc ở cả 3 tầng: Spring Framework, Hibernate ORM, và JDBC Driver.

1. **Tối ưu hóa tầng Hibernate (Bỏ qua Dirty Checking):**
   - Khi một transaction thông thường diễn ra, Hibernate nạp các thực thể vào Persistence Context và tạo một bản sao lưu trạng thái ban đầu (Hydrated Snapshot). Lúc transaction commit, Hibernate phải lặp qua toàn bộ thực thể trong context để so sánh từng thuộc tính xem có bị thay đổi không (Dirty Checking).
   - Với `readOnly = true`, Spring chỉ thị cho Hibernate chuyển `FlushMode` sang `FlushMode.MANUAL`. Hibernate sẽ **hoàn toàn không lưu bản snapshot và không chạy dirty checking**, giúp tiết kiệm từ 30% đến 50% mức tiêu thụ bộ nhớ Heap và giải phóng đáng kể chu kỳ CPU khi truy vấn hàng ngàn dòng dữ liệu.

2. **Tối ưu hóa tầng JDBC Driver:**
   - Spring gọi phương thức `connection.setReadOnly(true)` trên JDBC connection.
   - Một số cơ sở dữ liệu (như MySQL/MariaDB hoặc Oracle) có thể kích hoạt các đường dẫn thực thi tối ưu hóa riêng cho truy vấn đọc (Read-only Transactions) mà không cần cấp phát Transaction ID (XID) hoặc ghi nhật ký Undo/Redo Log.

3. **Định tuyến Bản sao Đọc (Read-Replica Routing):**
   - Trong kiến trúc cơ sở dữ liệu có Master-Slave (hoặc Primary-Replica), lớp phân tuyến `AbstractRoutingDataSource` dựa vào cờ `TransactionSynchronizationManager.isCurrentTransactionReadOnly()` để tự động chuyển toàn bộ câu lệnh `SELECT` sang Replica, giải tỏa tải cho nút Primary mà không cần can thiệp vào mã nguồn nghiệp vụ.

### Ví dụ trong project này

Trong `AdminDashboardServiceImpl.java` (#382):
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {
    // ...

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();
        long totalLessons = lessonRepository.count();
        long totalEnrollments = enrollmentRepository.count();

        List<RecentRegistrationDto> recentUsers = userRepository.findTop5ByOrderByCreatedAtDesc() ...;
        List<RecentOrderDto> recentOrders = orderRepository.findTop5ByOrderByCreatedAtDesc() ...;

        return AdminDashboardResponse.builder()
            .totalUsers(totalUsers)
            .totalCourses(totalCourses)
            .totalLessons(totalLessons)
            .totalEnrollments(totalEnrollments)
            .recentRegistrations(recentUsers)
            .recentOrders(recentOrders)
            .build();
    }
}
```

### Câu hỏi phỏng vấn liên quan

Điều gì sẽ xảy ra nếu ta vô tình gọi lệnh `repository.save()` hoặc sửa đổi thuộc tính của một Managed Entity bên trong một phương thức được đánh dấu `@Transactional(readOnly = true)`?

### Câu trả lời ngắn gọn

Do `readOnly = true` chuyển `FlushMode` thành `MANUAL`, Hibernate sẽ không bao giờ tự động gọi `flush()` khi commit transaction. Do đó, các thay đổi trên Managed Entity sẽ **âm thầm bị bỏ qua (Silently ignored)** và không được ghi xuống database. Nếu lập trình viên cố tình gọi `saveAndFlush()` hoặc thực hiện một câu lệnh Native SQL `UPDATE/INSERT`, tùy thuộc vào loại cơ sở dữ liệu và JDBC driver, hệ thống sẽ ném ra ngoại lệ `SQLException: Connection is read-only` hoặc `TransientDataAccessException`.

---

## 175. Server-Authoritative Order Snapshots vs Client-Side Price Calculation (Financial Integrity in E-Commerce)

### Giải thích ngắn gọn

Trong kiến trúc ứng dụng web thương mại điện tử hoặc bán khóa học, một trong những sai lầm chết người về bảo mật và toàn vẹn dữ liệu là để Frontend tự tính toán giá trị đơn hàng, áp dụng mã giảm giá, hoặc gửi giá trị tiền thanh toán lên Backend.

1. **Nguyên tắc Máy chủ Nắm Toàn quyền (Server-Authoritative Principle):**
   - **Tất cả các phép tính tài chính (Financial Computations)** như: giá gốc của khóa học, tỷ lệ giảm giá, tính hợp lệ và hạn mức của coupon, thuế, và số tiền cuối cùng (`finalAmount`) bắt buộc phải do Backend thực hiện và xác thực.
   - Khi tạo đơn hàng (`POST /api/v1/orders`), Client chỉ được phép gửi duy nhất định danh sản phẩm (`courseId`). Backend sẽ truy vấn giá từ cơ sở dữ liệu, snapshot (chụp lại) giá trị tại thời điểm đó vào bảng `orders` và `order_items`.
   - Khi hiển thị đơn hàng (`GET /api/v1/orders/me` hoặc `/api/v1/orders/:id`), Frontend đóng vai trò là tầng hiển thị thuần túy: đọc và định dạng các giá trị snapshot do server trả về (`formatPrice(order.finalAmount)`). Tuyệt đối không chạy logic `order.finalAmount = order.totalAmount - order.discountAmount` ở Client.

2. **Rủi ro nếu vi phạm:**
   - **Lỗ hổng Giả mạo Giá (Price Tampering / Parameter Tampering):** Kẻ tấn công có thể can thiệp HTTP request (qua DevTools hoặc Postman) để sửa giá từ 500.000đ thành 1.000đ trước khi gửi lên cổng thanh toán.
   - **Sai lệch Làm tròn Số học (Floating-Point Rounding Inconsistencies):** JavaScript sử dụng chuẩn IEEE 754 cho số thực (`0.1 + 0.2 !== 0.3`). Nếu Client tự tính toán chiết khấu hoặc thuế, kết quả có thể chênh lệch 1-2 đồng so với kiểu `BigDecimal` của Java Backend, gây xung đột đối soát giao dịch kế toán.

### Ví dụ trong project này

Trong `OrderHistoryPage.vue` và `CheckoutPage.vue` (#385):
```vue
<!-- Sử dụng trực tiếp giá trị snapshot của Server, không tính toán lại -->
<div class="flex items-baseline gap-1.5">
  <span class="text-stitch-muted-foreground font-medium">Tổng thanh toán:</span>
  <span class="font-stitch-serif font-bold text-lg sm:text-xl text-stitch-primary">
    {{ formatPrice(order.finalAmount) }}
  </span>
</div>
```

### Câu hỏi phỏng vấn liên quan

Tại sao các trường `price`, `totalAmount`, `discountAmount`, `finalAmount` trong bảng `orders` và `order_items` cần phải được lưu trữ dạng snapshot cố định thay vì tham chiếu `JOIN` trực tiếp tới bảng `courses` khi cần hiển thị?

### Câu trả lời ngắn gọn

Giá của khóa học có thể thay đổi trong tương lai (ví dụ hôm nay bán 500k, tháng sau tăng lên 800k hoặc giảm giá flash sale xuống 300k). Nếu bảng đơn hàng không lưu snapshot giá tại thời điểm mua mà `JOIN` động với bảng `courses`, lịch sử đơn hàng của học sinh mua từ tháng trước sẽ bị hiển thị sai lệch theo giá mới, làm hỏng báo cáo doanh thu tài chính và vi phạm tính pháp lý của hóa đơn giao dịch.

---

## 176. Purchase Continuity & Status-Aware Recovery Patterns in Single Page Applications (SPAs)

### Giải thích ngắn gọn

Trong quá trình mua sắm trực tuyến, người dùng thường gặp phải các tình huống gián đoạn giao dịch: mất kết nối mạng, tắt trình duyệt, thẻ thanh toán hết hạn mức, hoặc muốn suy nghĩ thêm trước khi chuyển khoản.

1. **Khái niệm Tính liên tục Mua hàng (Purchase Continuity):**
   - Là khả năng của hệ thống cho phép người dùng tiếp tục quy trình mua hàng dang dở từ bất kỳ điểm chạm nào trong ứng dụng mà không làm mất thông tin đơn hàng đã khởi tạo.
   - Khi học sinh đã bấm "Mua ngay" tạo một đơn hàng `PENDING`, nếu thoát ra ngoài, hệ thống cung cấp:
     - Banner cảnh báo nổi bật tại trang "Khóa học của tôi" (`MyCoursesPage`).
     - Mục "Đơn hàng" (`/student/orders`) hiển thị danh sách trực quan, có nút "Tiếp tục thanh toán" dẫn thẳng lại `/checkout/:orderId`.
   - Ngăn chặn triệt để hành vi tạo nhiều đơn hàng `PENDING` rác cho cùng một khóa học.

2. **Mô hình Phục hồi Theo Trạng thái (Status-Aware Recovery Pattern):**
   - Giao diện người dùng phải phản ứng chính xác theo máy trạng thái (Finite State Machine) của đơn hàng:
     - `PENDING`: Cung cấp CTA "Tiếp tục thanh toán" (`/checkout/:orderId`).
     - `PAID`: Đơn đã hoàn tất, cung cấp CTA "Vào học ngay" (tự động điều hướng đến bài học đang học dở `lastLessonId` hoặc trang danh sách khóa học).
     - `CANCELLED`, `FAILED`, `EXPIRED`, `REFUNDED`: Các trạng thái đóng (Terminal Statuses). Hệ thống hiển thị huy hiệu và dòng giải thích nguyên nhân rõ ràng, kèm liên kết an toàn "Xem khóa học khác" (`/courses`). **Tuyệt đối không hiển thị các nút thanh toán lại, hủy đơn, hoặc hoàn tiền** nếu backend chưa cung cấp API chính thức cho các hành vi đó.

### Ví dụ trong project này

Trong `OrderHistoryPage.vue` (#385):
```vue
<!-- PENDING: Phục hồi phiên checkout -->
<template v-if="order.status === 'PENDING'">
  <router-link :to="`/checkout/${order.id}`" class="px-5 py-2.5 bg-stitch-primary text-white rounded-xl">
    Tiếp tục thanh toán &rarr;
  </router-link>
</template>

<!-- PAID: Điều hướng thông minh vào bài học -->
<template v-else-if="order.status === 'PAID'">
  <button @click="goToLearningFlow(order)" class="px-5 py-2.5 bg-green-600 text-white rounded-xl">
    ▶ Vào học ngay
  </button>
</template>

<!-- TERMINAL: Thông điệp giải thích non-actionable -->
<template v-else>
  <div class="text-xs text-stitch-muted-foreground italic">
    {{ getTerminalStatusExplanation(order.status) }}
  </div>
  <router-link to="/courses" class="px-4 py-2 border rounded-xl">Xem khóa học</router-link>
</template>
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào để xử lý điều hướng thông minh khi người dùng bấm vào nút "Vào học ngay" từ một đơn hàng đã thanh toán (`PAID`)?

### Câu trả lời ngắn gọn

Không nên chỉ điều hướng cứng về trang chủ hoặc trang khóa học chung. Quy trình tối ưu: Lấy `courseId` từ `order.items[0]`, gọi API kiểm tra danh sách ghi danh `StudentService.getMyCourses()`. Nếu học viên đã có tiến độ bài học (`lastLessonId`), đưa thẳng học viên vào màn hình học của bài học đó (`/student/lessons/:id`). Nếu khóa học mới tinh chưa có bài học nào được mở hoặc có lỗi phát sinh, fallback an toàn về trang "Khóa học của tôi" (`/student/my-courses`).

---

## 177. Role-Safe Navigation Filtering & Defensive Vue Router Access Control (Student vs Admin vs Guest)

### Giải thích ngắn gọn

Trong các ứng dụng phân tán có nhiều vai trò người dùng (Multi-role SPAs), việc kiểm soát truy cập và hiển thị giao diện đòi hỏi sự phối hợp chặt chẽ giữa 2 tầng: **Router Navigation Guards** và **UI Navigation Components**.

1. **Tầng Router Navigation Guards (Chốt chặn An ninh Bắt buộc):**
   - Đảm nhiệm việc chặn truy cập trái phép ở cấp độ URL.
   - Khi người dùng gõ trực tiếp địa chỉ `/student/orders` hoặc `/admin/dashboard` trên thanh địa chỉ trình duyệt, guard kiểm tra:
     - Chưa đăng nhập (`requiresAuth: true && !isAuthenticated`) $\to$ Chuyển hướng sang `/login`.
     - Không đúng vai trò (`role: 'STUDENT'` nhưng token mang role `ADMIN`) $\to$ Chuyển hướng về Dashboard mặc định của role đó (`/admin/dashboard`).
   - Sử dụng cơ chế kế thừa metadata của Route cha (`to.matched.some(...)`) giúp bảo vệ toàn bộ các route con một cách tập trung và nhất quán.

2. **Tầng UI Navigation Component (Trải nghiệm Người dùng & Phòng vệ Trực quan):**
   - Router Guard chỉ chặn khi request đã diễn ra. Một giao diện chuyên nghiệp không bao giờ hiển thị các menu item mà người dùng không có quyền truy cập.
   - Trong `Navbar.vue`:
     - Khách vãng lai (Guest): Chỉ thấy "Trang chủ", "Khóa học", "Đăng nhập", "Đăng ký".
     - Quản trị viên (Admin): Chỉ thấy "Trang chủ", "Khóa học", "Admin", và "Dashboard Admin". Tuyệt đối không hiển thị các mục học sinh như "Khóa học của tôi", "Flashcards", "Bảng xếp hạng", "Đơn hàng".
     - Học viên (Student): Thấy đầy đủ các liên kết học tập và quản lý cá nhân.
   - Thực hiện qua các điều kiện ràng buộc như `v-if="!isAdmin"` và `v-if="isLoggedIn"` đồng bộ trên cả thanh Desktop Nav và Mobile Drawer Menu.

### Ví dụ trong project này

Trong `Navbar.vue`:
```vue
<!-- Desktop Navigation: Chỉ hiển thị cho học sinh, ẩn với Admin và Guest -->
<router-link 
  v-if="!isAdmin"
  to="/student/orders"
  class="text-sm font-medium focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded px-2 py-1"
  :class="[isRouteActive('/student/orders') ? 'text-stitch-primary' : 'text-stitch-muted-foreground']"
>
  Đơn hàng
</router-link>
```

### Câu hỏi phỏng vấn liên quan

Tại sao ẩn một nút trên giao diện (ví dụ bằng `v-if="isAdmin"`) không bao giờ được coi là một biện pháp bảo mật thực sự?

### Câu trả lời ngắn gọn

Ẩn phần tử trên giao diện chỉ là kỹ thuật tối ưu trải nghiệm người dùng (UX Filtering). Mã nguồn JavaScript chạy trên trình duyệt của người dùng, nên bất kỳ ai cũng có thể mở DevTools để sửa biến reactivity, xóa class CSS `hidden`, hoặc gọi trực tiếp API bằng Fetch/cURL. Bảo mật thực sự phải được thực thi ở 2 chốt chặn: **Router Navigation Guards** ở Frontend (chặn chuyển trang) và quan trọng nhất là **Authorization Filter / Method Security (`@PreAuthorize("hasRole('STUDENT')")`)** ở Backend (chặn truy cập dữ liệu).

---

## 178. Cạm Bẫy In-Memory Pagination (`HHH000104`) & Collection Fetching Trong JPA/Hibernate

### Giải thích ngắn gọn

Khi sử dụng Spring Data JPA để phân trang (`Pageable pageable`), các kỹ sư thường mắc phải một sai lầm phổ biến: cố tình nạp kèm quan hệ 1-N (`@OneToMany`) bằng `@EntityGraph(attributePaths = {"items"})` hoặc `JOIN FETCH o.items`.

1. **Bản chất vấn đề:**
   - Trong SQL quan hệ, phép JOIN giữa bảng cha (Parent) và bảng con (Child) sẽ nhân số dòng theo số lượng con: Nếu một đơn hàng (Order) có 3 sản phẩm (OrderItem), câu lệnh `SELECT * FROM orders o JOIN order_items i ON ...` sẽ trả về **3 dòng SQL** có cùng `orders.id`.
   - Nếu database áp dụng `LIMIT 10 OFFSET 0`, SQL server sẽ cắt ngẫu nhiên đúng 10 dòng kết quả JOIN. Khi đó, có thể chỉ có 3 hoặc 4 đơn hàng được nạp, và đơn hàng thứ 4 có thể bị mất bớt sản phẩm (vì các dòng sau của nó bị `LIMIT` cắt bỏ).
   - Hibernate nhận thức được nguy cơ sai lệch dữ liệu này, nên nó **tự động bỏ qua mệnh đề LIMIT/OFFSET ở tầng SQL** và in cảnh báo:
     ```text
     WARN: HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!
     ```
   - Hậu quả nghiêm trọng: Hibernate sẽ kéo **toàn bộ 1,000,000 dòng** từ cơ sở dữ liệu về bộ nhớ JVM Heap, khởi tạo hàng trăm ngàn Java Entity, sau đó mới dùng mã Java để cắt lấy 10 phần tử đầu tiên. Điều này dẫn tới nghẽn mạng DB, quá tải Garbage Collection và sập server với lỗi `java.lang.OutOfMemoryError: Java heap space`.

2. **Giải pháp chuẩn công nghiệp:**
   - **Tách truy vấn phân trang:** Thực hiện phân trang thuần túy trên Entity chính mà không JOIN collection:
     ```java
     Page<Order> findByUserId(Long userId, Pageable pageable);
     ```
     Câu lệnh SQL sinh ra sẽ là `SELECT * FROM orders WHERE user_id = ? ORDER BY ... LIMIT 10 OFFSET 0` (được tối ưu hóa bằng chỉ mục B-Tree).
   - **Gộp nạp Collection bằng `@BatchSize`:** Trên trường tập hợp của Entity cha, khai báo `@BatchSize(size = 25)`:
     ```java
     @BatchSize(size = 25)
     @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
     private List<OrderItem> items = new ArrayList<>();
     ```
   - Khi map 10 đơn hàng sang DTO trong cùng một Transaction, Hibernate sẽ tự động gom các ID của 10 đơn hàng đó lại và chỉ phát đúng 1 câu lệnh nạp lô:
     ```sql
     SELECT * FROM order_items WHERE order_id IN (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
     ```
   - Đưa độ phức tạp từ $1 + N$ queries về đúng 2 queries cố định $O(1)$, bảo đảm hiệu năng tối đa và xóa sổ hoàn toàn cảnh báo `HHH000104`.

### Ví dụ trong project này

Trong [Order.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_order/entity/Order.java) và [OrderRepository.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_order/repository/OrderRepository.java):
```java
// Order.java: BatchSize gom nhóm nạp items hiệu năng cao
@BatchSize(size = 25)
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
@Builder.Default
private List<OrderItem> items = new ArrayList<>();

// OrderRepository.java: Phân trang thuần túy ở tầng DB
Page<Order> findByUserId(Long userId, Pageable pageable);
```

### Câu hỏi phỏng vấn liên quan

Tại sao không được dùng `JOIN FETCH` hoặc `@EntityGraph` trên quan hệ `@OneToMany` khi truy vấn có tham số `Pageable` trong Spring Data JPA?

### Câu trả lời ngắn gọn

Bởi vì phép JOIN với bảng con làm nhân bản số dòng kết quả. Database không thể áp dụng `LIMIT / OFFSET` trên số lượng cha mà chỉ áp dụng được trên số dòng JOIN, dẫn tới mất mát dữ liệu con. Để tránh sai lệch, Hibernate buộc phải tải toàn bộ bảng vào bộ nhớ JVM rồi phân trang in-memory (`HHH000104`), gây nghẽn RAM và nguy cơ Out-of-Memory. Giải pháp chuẩn là phân trang bảng cha riêng biệt và dùng `@BatchSize` để nạp gộp bảng con qua mệnh đề `WHERE parent_id IN (...)`.

---

## 179. Sắp Xếp Tất Định (Deterministic Pagination) & Nguyên Lý Tie-Breaker Trong Cơ Sở Dữ Liệu

### Giải thích ngắn gọn

Trong kỹ thuật phân trang cơ sở dữ liệu (Pagination), **tính tất định (Determinism)** là yêu cầu bắt buộc để đảm bảo người dùng lật trang mà không bao giờ gặp phải hiện tượng nhảy dòng, lặp bản ghi hoặc sót bản ghi.

1. **Hiện tượng Bất định (Non-deterministic Ordering):**
   - Giả sử hệ thống chỉ sắp xếp theo thời gian tạo: `ORDER BY created_at DESC`.
   - Khi một người dùng tạo hàng loạt đơn hàng trong cùng 1 giây (hoặc trong các batch job/migration), nhiều bản ghi sẽ có giá trị `created_at` hoàn toàn giống nhau.
   - Theo chuẩn SQL, khi các bản ghi có cùng giá trị trong mệnh đề `ORDER BY`, thứ tự tương đối giữa chúng là **hoàn toàn tùy ý (Arbitrary)**. Khi thực hiện `LIMIT 2 OFFSET 0` (Trang 1) rồi `LIMIT 2 OFFSET 2` (Trang 2), công cụ lập kế hoạch thực thi (Query Optimizer) có thể trả về các dòng theo thứ tự khác nhau.
   - Hậu quả: Đơn hàng X có thể xuất hiện ở cả Trang 1 lẫn Trang 2, trong khi đơn hàng Y bị bỏ qua và không bao giờ hiển thị cho người dùng.

2. **Nguyên lý Khóa Phụ (Secondary Tie-Breaker):**
   - Để biến một truy vấn sắp xếp thành tất định, bắt buộc phải bổ sung một cột có tính chất duy nhất (Unique) vào cuối mệnh đề sắp xếp, thường là Khóa chính (Primary Key `id`):
     ```sql
     ORDER BY created_at DESC, id DESC
     ```
   - Vì `id` là duy nhất (`UNIQUE NOT NULL`), không bao giờ tồn tại hai bản ghi có cùng cặp giá trị `(created_at, id)`. Thứ tự trả về được đảm bảo duy nhất và cố định 100% qua mọi lần truy vấn.

3. **Tối ưu hóa Index cho Sắp xếp Tất định:**
   - Để tránh chi phí tính toán `Using filesort` khi sắp xếp theo 2 cột, cơ sở dữ liệu cần một Composite Index bao phủ:
     ```sql
     CREATE INDEX idx_orders_user_created_id ON orders(user_id, created_at DESC, id DESC);
     ```
   - Database engine sẽ duyệt trực tiếp các nút lá của cây B-Tree theo đúng thứ tự index mà không tốn bất kỳ chu kỳ CPU nào để sắp xếp lại trong bộ nhớ.

### Ví dụ trong project này

Trong [OrderController.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_order/controller/OrderController.java) và [V12__add_order_pagination_index.sql](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/resources/db/migration/V12__add_order_pagination_index.sql):
```java
// Spring Data Pageable với tie-breaker id DESC
Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
```
Và migration index:
```sql
CREATE INDEX idx_orders_user_created_id ON orders(user_id, created_at DESC, id DESC);
```

### Câu hỏi phỏng vấn liên quan

Tại sao khi viết API phân trang trong các ứng dụng có lưu lượng giao dịch cao, chỉ sắp xếp `ORDER BY created_at DESC` là chưa đủ?

### Câu trả lời ngắn gọn

Bởi vì `created_at` không phải là duy nhất. Khi nhiều bản ghi có cùng timestamp, thứ tự sắp xếp giữa chúng là bất định (Non-deterministic). Database có thể trả về các thứ tự khác nhau giữa các lần gọi `OFFSET`, dẫn tới hiện tượng lặp bản ghi hoặc mất bản ghi khi người dùng lật trang. Cần luôn thêm khóa chính làm tie-breaker: `ORDER BY created_at DESC, id DESC` kèm Composite Index tương ứng.

---

## 180. Mẫu Thiết Kế Adapter Tương Thích Ngược (Backward Compatibility Adapter) Trong Di Chuyển Khế Ước API

### Giải thích ngắn gọn

Trong quá trình tiến hóa hệ thống (API Evolution), việc thay đổi khế ước trả về (Response Contract) của một endpoint là điều thường xuyên xảy ra. Ví dụ: Chuyển đổi endpoint từ danh sách mảng phẳng `List<T>` sang đối tượng phân trang `PageResponse<T>` (`{ currentPage, pageSize, totalPages, totalElements, data: [...] }`).

1. **Thách thức:**
   - Có rất nhiều trang và component đang phụ thuộc vào cấu trúc cũ: `const orders = res.data.result || []`, `orders.filter(...)`, `orders.find(...)`, `orders.length`.
   - Nếu đổi backend trả về `PageResponse`, `res.data.result` sẽ biến thành một Object `{ currentPage, ... }`. Việc gọi `.filter()` trên Object sẽ lập tức gây sập ứng dụng với lỗi runtime `TypeError: orders.filter is not a function`.
   - Việc phải sửa đổi đồng loạt hàng chục component cùng lúc tiềm ẩn rủi ro hồi quy (Regression Risk) cao và tốn kém thời gian kiểm thử.

2. **Giải pháp Adapter Pattern tại tầng Service:**
   - Trong JavaScript, một `Array` thực chất là một `Object` đặc biệt có prototype của Array.
   - Tại hàm gọi API trung gian `OrderService.getMyOrders()`:
     ```javascript
     async getMyOrders(params) {
       const response = await api.get('/v1/orders/me', { params })
       if (response?.data?.result && !Array.isArray(response.data.result) && Array.isArray(response.data.result.data)) {
         const pageData = response.data.result
         const items = [...pageData.data]
         // Gắn metadata phân trang trực tiếp vào mảng
         items.currentPage = pageData.currentPage
         items.pageSize = pageData.pageSize
         items.totalPages = pageData.totalPages
         items.totalElements = pageData.totalElements
         items.data = pageData.data
         response.data.result = items
       }
       return response
     }
     ```
   - **Lợi ích kép:**
     1. `Array.isArray(res.data.result)` vẫn là `true`, các component cũ gọi `.filter()`, `.length`, `[0]` hoạt động bình thường 100%.
     2. Các component mới cần phân trang có thể đọc trực tiếp `res.data.result.totalPages`, `res.data.result.currentPage`.
     3. Đảm bảo di chuyển an toàn mà không làm gián đoạn hệ thống.

### Ví dụ trong project này

Trong [frontend/src/services/order.service.js](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/frontend/src/services/order.service.js):
Hàm `getMyOrders` chuẩn hóa kết quả `PageResponse` từ backend thành một array tương thích ngược, giúp toàn bộ các trang `OrderHistoryPage.vue`, `MyCoursesPage.vue`, `CourseDetailPage.vue` hoạt động trơn tru không cần chỉnh sửa.

### Câu hỏi phỏng vấn liên quan

Làm thế nào để nâng cấp một API từ trả về mảng sang trả về đối tượng phân trang (Pagination Envelope) mà không làm vỡ các màn hình frontend đang chạy trên production?

### Câu trả lời ngắn gọn

Áp dụng mẫu thiết kế Adapter ở tầng Service/Client SDK: Bắt lấy phản hồi của endpoint, trích xuất mảng dữ liệu `data` từ đối tượng phân trang, và gán các thuộc tính metadata (`totalPages`, `currentPage`, `totalElements`) trực tiếp lên mảng đó. Nhờ vậy, mã nguồn cũ vẫn nhận diện kết quả là một Array hợp lệ, trong khi mã nguồn mới có thể truy cập các thông tin phân trang một cách nhất quán.

---

## 181. Kỹ Thuật Batch Aggregate Queries (GROUP BY) Khắc Phục N+1 Cho Thống Kê Quan Hệ 1-N Trong Spring Data JPA

### Giải thích ngắn gọn

Trong các ứng dụng thực tế, màn hình Dashboard hoặc danh sách danh mục thường cần hiển thị các entity cha kèm theo số liệu thống kê tổng hợp từ các bảng con (ví dụ: Danh sách các bộ thẻ kèm tổng số thẻ và số thẻ đến hạn ôn tập; Danh sách danh mục kèm số sản phẩm; Danh sách khóa học kèm số học viên).

1. **Vấn đề chống mẫu (Anti-Pattern - N+1 Aggregates):**
   - Lập trình viên thường truy vấn danh sách entity cha: `List<Parent> parents = parentRepo.findAll()`.
   - Sau đó chạy vòng lặp `for (Parent p : parents)` và gọi các hàm đếm đơn lẻ:
     - `childRepo.countByParentId(p.getId())`
     - `statsRepo.countDueByParentId(p.getId(), userId)`
   - Nếu có $N$ bản ghi cha, hệ thống sẽ phát sinh $2N + 1$ câu lệnh SQL truy vấn vào database. Đây là biến thể nguy hiểm của bài toán N+1 Query vì các hàm `COUNT(*)` buộc database phải quét index hoặc partition nhiều lần, làm cạn kiệt connection pool khi lượng người dùng đồng thời tăng cao.

2. **Giải pháp chuẩn: Batch Aggregate Query với `GROUP BY`:**
   - Thay vì đếm riêng rẽ từng phần tử cha trong vòng lặp Java, ta gom toàn bộ danh sách ID của các entity cha: `List<Long> parentIds = parents.stream().map(Parent::getId).toList()`.
   - Viết câu truy vấn tổng hợp cấp cơ sở dữ liệu với mệnh đề `IN` và `GROUP BY`:
     ```java
     @Query("SELECT f.deck.id, COUNT(f.id) FROM Flashcard f WHERE f.deck.id IN :deckIds GROUP BY f.deck.id")
     List<Object[]> countCardsByDeckIds(@Param("deckIds") Collection<Long> deckIds);
     ```
   - Cơ sở dữ liệu chỉ cần thực hiện 1 câu lệnh SQL duy nhất, tận dụng Hash Aggregation hoặc Index Range Scan cực nhanh.
   - Tại tầng Service, map kết quả `List<Object[]>` thành một `Map<Long, Long>` trong bộ nhớ JVM:
     ```java
     Map<Long, Long> counts = repo.countCardsByDeckIds(deckIds).stream()
             .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Number) row[1]).longValue()));
     ```
   - Khi build DTO, việc tra cứu số lượng là thao tác $O(1)$ trên RAM: `counts.getOrDefault(deck.getId(), 0L)`.
   - Toàn bộ thao tác giảm từ $2N + 1$ truy vấn xuống đúng 3 truy vấn cố định (1 query lấy danh sách cha, 1 query đếm tổng thẻ theo lô, 1 query đếm thẻ chưa đến hạn theo lô), đạt hiệu năng $O(1)$ database roundtrips.

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_flashcard/service/FlashcardServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_flashcard/service/FlashcardServiceImpl.java):
Phương thức `getUserDecks()` gom nhóm các deck IDs và gọi `flashcardRepository.countCardsByDeckIds(deckIds)` kết hợp `progressRepository.countNotDueCardsByDeckIds(user.getId(), deckIds, currentUtc)` để nạp toàn bộ số liệu thẻ cần ôn tập mà không gây bão truy vấn.

### Câu hỏi phỏng vấn liên quan

Làm thế nào để hiển thị danh sách các entity cha kèm theo số lượng phần tử con được đếm từ bảng khác (Aggregate Count) mà không làm phát sinh vấn đề N+1 query trong Spring Data JPA?

### Câu trả lời ngắn gọn

Tránh gọi `countByParentId()` trong vòng lặp Java. Thay vào đó, thu thập danh sách ID của các entity cha, thực hiện một truy vấn duy nhất có mệnh đề `WHERE parent_id IN :ids GROUP BY parent_id`, trả về cặp `(parentId, count)` và chuyển đổi thành `Map<ParentId, Long>` trong JVM để tra cứu $O(1)$ khi ánh xạ sang DTO.

---

## 182. Tối Ưu Hóa Truy Vấn Trạng Thái Ôn Tập Flashcard (Spaced Repetition System - SRS) Bằng Composite Covering Index

### Giải thích ngắn gọn

Trong các nền tảng học ngoại ngữ, thuật toán Spaced Repetition System (như SM-2 hoặc Leitner) yêu cầu xác định xem một thẻ từ vựng đã đến hạn ôn tập hay chưa (`next_review_time <= :currentTime`).

1. **Thách thức:**
   - Mỗi người dùng có một tiến trình học riêng biệt cho từng thẻ từ vựng (`flashcard_progress` chứa `user_id`, `flashcard_id`, `deck_id`, `next_review_time`, `ease_factor`, `interval_days`).
   - Một deck có thể có hàng trăm thẻ. Khi người dùng bấm học, hệ thống cần:
     1. Lấy danh sách thẻ thuộc deck.
     2. Kiểm tra tiến độ của từng thẻ đối với người dùng hiện tại để lọc ra các thẻ chưa học (chưa có progress) hoặc đã đến hạn ôn (`nextReviewTime <= now`).
     3. Giới hạn số lượng thẻ mỗi lượt học (ví dụ 20 thẻ).
   - Nếu truy vấn bằng JOIN lồng nhau hoặc truy vấn từng thẻ (`findByUserIdAndFlashcardId`), database sẽ phải quét tuần tự (Full Table Scan) hoặc nạp quá nhiều dòng dữ liệu không cần thiết.

2. **Chiến lược tối ưu hóa toàn diện:**
   - **Tầng Ứng Dụng (Batch Fetching):** Nạp danh sách thẻ của deck kèm `JOIN FETCH f.deck`. Sau đó dùng `findByUserIdAndFlashcardIdIn(userId, cardIds)` để lấy toàn bộ tiến độ của học viên cho các thẻ này trong 1 truy vấn SQL duy nhất. Lập chỉ mục thành `Map<CardId, FlashcardProgress>` trong RAM để lọc điều kiện thời gian.
   - **Tầng Cơ Sở Dữ Liệu (Covering Index):**
     - Đánh chỉ mục `idx_flashcards_deck_id_asc` trên bảng `flashcards(deck_id, id ASC)`.
     - Đánh chỉ mục tổng hợp bao phủ (Composite Covering Index) `idx_flashcard_progress_user_card_due` trên bảng `flashcard_progress(user_id, flashcard_id, next_review_time)`.
   - **Hiệu quả của Covering Index:**
     Khi thực thi truy vấn tìm tiến độ theo `user_id` và tập hợp `flashcard_id`, cơ sở dữ liệu có thể tìm thấy toàn bộ dữ liệu lọc và so sánh mốc `next_review_time` ngay trên cấu trúc B-Tree của Index mà không cần tốn chi phí Disk I/O để tra cứu bảng dữ liệu gốc (Index Lookups / Table Access), giảm thời gian thực thi từ hàng chục mili-giây xuống dưới 1 mili-giây.

### Ví dụ trong project này

Trong [backend/src/main/resources/db/migration/V13__add_flashcard_performance_indexes.sql](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/resources/db/migration/V13__add_flashcard_performance_indexes.sql):
- `CREATE INDEX idx_flashcards_deck_id_asc ON flashcards(deck_id, id ASC);`
- `CREATE INDEX idx_flashcard_progress_user_card_due ON flashcard_progress(user_id, flashcard_id, next_review_time);`
Hai chỉ mục này hỗ trợ đắc lực cho các truy vấn `getDueCards` và `countNotDueCardsByDeckIds` trong `FlashcardServiceImpl.java`.

### Câu hỏi phỏng vấn liên quan

Thế nào là một "Covering Index" và tại sao một Composite Index gồm `(user_id, flashcard_id, next_review_time)` lại tối ưu vượt trội cho các truy vấn kiểm tra tiến độ ôn tập giãn cách?

### Câu trả lời ngắn gọn

Covering Index là chỉ mục chứa toàn bộ các cột cần thiết cho một truy vấn (cột trong `WHERE`, `JOIN` và `SELECT`). Khi thực hiện truy vấn lọc theo `user_id` và `flashcard_id` để so sánh `next_review_time`, database engine có thể trích xuất và kiểm tra dữ liệu trực tiếp từ B-Tree index mà không cần thực hiện bước "Bookmark/Table Lookup" đọc các trang đĩa của bảng chính, giúp giảm thiểu số lượt đọc đĩa và tăng tốc độ truy vấn lên mức tối đa.

---

## 183. Khế Ước Trả Về Thống Nhất Cho Frontend API Services (Uniform AxiosResponse Return Convention)

### Giải thích ngắn gọn

Trong các ứng dụng SPA (Single Page Application) sử dụng Axios làm HTTP client, tầng Service (hay còn gọi là API Client Layer) đóng vai trò trung gian giữa UI components và backend. Khi không có quy ước rõ ràng về kiểu trả về, các developer dễ tạo ra sự phân mảnh:
- Một số service trả `Promise<AxiosResponse>` nguyên vẹn.
- Một số service dùng `.then(res => res.data)` để "bóc vỏ", chỉ trả về phần body.
- Một số service dùng class, số khác dùng object literal.

Sự không nhất quán này dẫn đến: Consumer phải đoán kiểu trả về (`res.data.result` hay `res.result`?), gây lỗi runtime khó phát hiện, và khiến team khó maintain khi dự án mở rộng.

1. **Vấn đề của việc "bóc vỏ" (unwrapping) trong Service Layer:**
   - Khi service dùng `.then(res => res.data)`, consumer nhận được object `{ code, message, result }` nhưng mất hoàn toàn thông tin:
     - **HTTP Status Code** (`res.status`): Cần thiết để phân biệt 200 vs 201 vs 204, hoặc kiểm tra redirect 3xx.
     - **Response Headers** (`res.headers`): Cần thiết cho pagination (`X-Total-Count`), rate limiting (`X-RateLimit-Remaining`), caching (`ETag`).
     - **Interceptor Context**: Axios interceptors hoạt động trên `AxiosResponse`; nếu service đã bóc vỏ, interceptor hạ nguồn (downstream) không thể truy cập metadata.
   - Trong xử lý lỗi, nếu service đã unwrap `.data`, pattern `error.response?.data?.code` vẫn hoạt động ở `catch` block (vì Axios error giữ nguyên cấu trúc), nhưng consumer phải nhớ rằng success path và error path trả về kiểu dữ liệu khác nhau — vi phạm nguyên tắc Principle of Least Surprise.

2. **Quy ước chuẩn hóa (The Standard Convention):**
   - **Service Layer**: Trả `Promise<AxiosResponse>` nguyên vẹn — `return api.get(...)`, `return api.post(...)`.
   - **Consumer Pattern**:
     ```javascript
     const res = await SomeService.getData()
     if (res.data && res.data.code === 1000) {
       const payload = res.data.result
       // ... use payload
     }
     ```
   - **Error Pattern**:
     ```javascript
     catch (error) {
       const msg = getApiErrorMessage(error)      // Human-readable message
       const httpStatus = error.response?.status   // 401, 403, 500, etc.
       const bizCode = error.response?.data?.code  // Backend error code (5002, etc.)
     }
     ```
   - Quy ước này đảm bảo consumer luôn có quyền truy cập đầy đủ metadata, và pattern kiểm tra nhất quán xuyên suốt toàn bộ codebase.

### Ví dụ trong project này

Trong [frontend/src/services/flashcard.service.js](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/frontend/src/services/flashcard.service.js):
Trước khi chuẩn hóa, `getUserDecks()` trả về `api.get('/v1/flashcards/decks').then(res => res.data)`, consumer nhận `{ code, result }`. Sau khi chuẩn hóa, hàm trả `api.get('/v1/flashcards/decks')`, consumer nhận `AxiosResponse` đầy đủ và truy cập `res.data.result`.

Trong [frontend/src/services/leaderboard.service.js](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/frontend/src/services/leaderboard.service.js):
Trước đó được viết dạng `class LeaderboardService` với `.then(res => res.data)`. Sau khi chuẩn hóa, chuyển sang object literal trả `Promise<AxiosResponse>` trực tiếp, đồng nhất với 8 service còn lại.

### Câu hỏi phỏng vấn liên quan

Tại sao trong kiến trúc frontend SPA, tầng Service nên trả về nguyên vẹn `Promise<AxiosResponse>` thay vì "bóc vỏ" bằng `.then(res => res.data)` trước khi trả cho component?

### Câu trả lời ngắn gọn

Vì HTTP response chứa nhiều metadata quan trọng ngoài body (status code, headers, interceptor context). Khi service "bóc vỏ" sớm, consumer mất khả năng kiểm tra HTTP status (401 Unauthorized, 429 Rate Limited), đọc headers phân trang, và duy trì nhất quán giữa success path (`res.data.result`) và error path (`error.response.data`). Quy ước trả nguyên AxiosResponse cho phép consumer tự quyết cách xử lý, giữ service layer thuần khiết (pure HTTP facade), và đảm bảo pattern kiểm tra `res.data.code === 1000` thống nhất toàn codebase.

---

## 184. Shared API Response Utility — Tập Trung Logic Kiểm Tra Response Và Trích Xuất Payload Trong Frontend

### Giải thích ngắn gọn

Khi toàn bộ frontend services đã thống nhất trả `Promise<AxiosResponse>`, các component consumer đều cần thực hiện cùng một bộ thao tác kiểm tra: kiểm tra success code, trích xuất result payload, lấy error message, đọc HTTP status từ error. Nếu lặp lại các pattern này ở 20-30 component, rủi ro sai sót và inconsistency tăng theo cấp số nhân.

1. **Vấn đề khi không có shared utility:**
   - Component A kiểm tra `if (res.data.code === 1000)`, component B kiểm tra `if (res.data && res.data.code == 1000)` (loose equality), component C quên kiểm tra `res.data` null dẫn đến `TypeError: Cannot read properties of null`.
   - Khi backend thay đổi success code từ `1000` sang `0` hoặc thêm trường mới, phải sửa hàng chục file.

2. **Giải pháp: Shared Response Utility Module:**
   - Tạo module `api-response.js` chứa các helper thuần (pure functions):
     - `isApiSuccess(response)`: Trả `true` nếu `response.data.code === 1000`. Kiểm tra null-safe với `Boolean(response && response.data && ...)`.
     - `getApiResult(response, fallback)`: Trích xuất `response.data.result` nếu success, trả `fallback` (default `null`) nếu không.
     - `getHttpStatus(errorOrResponse)`: Hoạt động với cả `AxiosResponse` (trả `res.status`) lẫn `AxiosError` (trả `error.response.status`).
     - `getApiErrorCode(error)`: Trích xuất backend error code `error.response.data.code`.
     - `getApiErrorPayload(error)`: Trả toàn bộ `error.response.data` cho consumer cần parse chi tiết.
   - Re-export `getApiErrorMessage` từ `api-error.js` để consumer chỉ cần import từ 1 module duy nhất.

3. **Lợi ích đạt được:**
   - **Single Source of Truth**: Logic kiểm tra success/failure nằm ở 1 chỗ, tất cả component consume qua function call.
   - **Null Safety**: Utility xử lý null/undefined gracefully, không bao giờ throw trên input bất hợp lệ.
   - **Change Resilience**: Khi backend đổi contract (ví dụ success code, cấu trúc error), chỉ cần sửa 1 file.
   - **Testability**: Test utility 1 lần (unit test), tin tưởng ở mọi consumer.

### Ví dụ trong project này

Trong [frontend/src/utils/api-response.js](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/frontend/src/utils/api-response.js):
Module export 6 helper functions (`isApiSuccess`, `getApiResult`, `getHttpStatus`, `getApiErrorCode`, `getApiErrorPayload`, `getApiErrorMessage`) và cả named lẫn default export. Consumer component có thể import riêng lẻ `import { isApiSuccess, getApiResult } from '@/utils/api-response'` hoặc import toàn bộ `import apiResponse from '@/utils/api-response'`.

### Câu hỏi phỏng vấn liên quan

Khi dự án frontend có hàng chục component đều cần kiểm tra API response success/failure theo cùng pattern, làm thế nào để tránh code duplication mà không over-engineer?

### Câu trả lời ngắn gọn

Tạo một shared utility module chứa các pure functions nhỏ gọn: `isApiSuccess(res)` kiểm tra success code, `getApiResult(res, fallback)` trích xuất payload an toàn, `getApiErrorMessage(err)` format error message. Các hàm này stateless, null-safe, dễ unit test, và tạo single source of truth cho logic kiểm tra response. Khi backend thay đổi contract (đổi success code, cấu trúc error), chỉ cần sửa 1 file thay vì rà soát hàng chục component.










## 183. Vòng Đời Xác Thực An Toàn (Secure Authentication Lifecycle) & Triệt Tiêu Token Ma (Stale Bearer Token Prevention)

### Giải thích ngắn gọn

Trong kiến trúc Single Page Application (SPA) hiện đại kết hợp JWT và HttpOnly Cookie:
1. **Mô hình Token Kép Lai (Hybrid Dual-Token Pattern):**
   - **Access Token:** Lưu trong bộ nhớ ứng dụng (Memory / Pinia State), thời gian sống ngắn (10-15 phút). Dùng để xác thực API qua header `Authorization: Bearer <token>`.
   - **Refresh Token:** Lưu trong Cookie với các cờ `HttpOnly`, `Secure`, `SameSite` (Lax hoặc Strict/None tùy domain), thời gian sống dài (7-30 ngày). JavaScript tuyệt đối không thể đọc hay can thiệp vào Cookie này, chống lại 100% rủi ro bị đánh cắp qua tấn công XSS (Cross-Site Scripting).

2. **Cạm bẫy Stale Bearer Token trong Axios Defaults:**
   - Nhiều lập trình viên thường cấu hình: `axios.defaults.headers.common['Authorization'] = 'Bearer ' + token` để mọi request đều tự động mang token.
   - Khi người dùng đăng xuất hoặc khi refresh token thất bại, họ gọi `authStore.clearAuth()` để xóa token trong state.
   - **Hiểm họa:** Axios lưu `defaults.headers.common` dưới dạng một biến tĩnh toàn cục độc lập với State Manager. Nếu không xóa tường minh `delete axios.defaults.headers.common['Authorization']`, Axios sẽ tiếp tục tự động gộp header này vào **toàn bộ request tiếp theo** (kể cả request của khách vãng lai hoặc sau khi đăng xuất).
   - Server khi nhận được một Bearer token đã hết hạn trên một public endpoint có thể từ chối phục vụ (trả về 401 Unauthorized) thay vì cho phép khách xem nội dung công khai.

3. **Cơ chế Hàng Đợi 401 Đồng Thời (Concurrent 401 Queueing):**
   - Khi Access Token hết hạn, nếu một trang web phát đồng thời 5 request API, cả 5 request đều sẽ nhận mã lỗi 401 trong cùng tích tắc.
   - Nếu không có cơ chế chặn: Cả 5 request sẽ đồng thời gọi `POST /api/auth/refresh-token`. Khi backend áp dụng Token Rotation (thu hồi refresh token cũ và cấp refresh token mới ngay lập tức), request thứ 2 đến backend sẽ mang token cũ đã bị thu hồi bởi request thứ 1 $\rightarrow$ Backend kích hoạt cơ chế phòng vệ "Token Reuse Detection" và hủy toàn bộ phiên làm việc của người dùng!
   - **Giải pháp chuẩn:**
     - Request 401 đầu tiên đặt cờ `isRefreshing = true` và thực thi refresh.
     - Các request 401 tiếp theo được gom vào `failedQueue = []` dưới dạng các hàm `resolve / reject` của Promise.
     - Khi refresh thành công, duyệt hàng đợi, cập nhật token mới vào header của từng request và retry đồng loạt.
     - Khi refresh thất bại, reject toàn bộ hàng đợi, dọn dẹp state + defaults header và chuyển hướng về `/login`.

### Ví dụ trong project này

Trong [frontend/src/services/api.js](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/frontend/src/services/api.js):
- Helper `removeHeader(config, 'Authorization')` bảo đảm nếu `authStore.accessToken` là null, header Authorization bị xóa triệt để khỏi config request.
- `failedQueue` quản lý danh sách request chờ token mới khi token đang được xoay vòng.
- Trong [frontend/src/stores/auth.store.js](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/frontend/src/stores/auth.store.js), phương thức `clearAuth()` xóa triệt để cả state lẫn `api.defaults.headers.common['Authorization']`.
- Bộ kiểm thử hồi quy [frontend/src/services/api.spec.js](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/frontend/src/services/api.spec.js) kiểm chứng toàn bộ hành vi này với 10 kịch bản kiểm thử độc lập.

### Câu hỏi phỏng vấn liên quan

Tại sao khi người dùng đăng xuất trong một ứng dụng Vue/React sử dụng Axios, các request API công khai sau đó đôi khi vẫn bị lỗi 401 Unauthorized? Bạn thiết kế cơ chế dọn dẹp phiên và xử lý 401 đồng thời như thế nào?

### Câu trả lời ngắn gọn

Hiện tượng này xảy ra do rò rỉ Stale Bearer Token từ `axios.defaults.headers.common['Authorization']`. Khi ứng dụng chỉ xóa token trong State Manager (như Pinia/Redux) mà quên xóa trong `defaults.headers`, Axios vẫn tự động gộp header cũ vào các request sau đó, khiến backend coi đó là token hết hạn và trả về 401.
Để giải quyết:
1. Trong hàm `clearAuth()`, phải đồng thời xóa Pinia state và xóa thuộc tính `Authorization` trong `axios.defaults.headers.common`.
2. Trong Request Interceptor, nếu client unauthenticated, chủ động xóa header `Authorization` khỏi `config.headers`.
3. Xử lý 401 đồng thời bằng cờ `isRefreshing` và hàng đợi `failedQueue` để chỉ phát duy nhất 1 request refresh, tránh bão request và tránh kích hoạt cơ chế Token Reuse Revocation của backend.


## 184. Ma Trận Phân Quyền (Role-Permission Matrix), Phòng Vệ Chiều Sâu (Defense-in-Depth) & Chuyển Hướng 403 Forbidden Trong SPA

### Giải thích ngắn gọn

Trong kiến trúc ứng dụng web phân tán hiện đại (SPA + REST API):
1. **Nguyên Tắc Phòng Vệ Chiều Sâu (Defense-in-Depth):**
   - **Lớp 1 - Giao Diện Người Dùng (UI/UX Visibility):** Ẩn các nút bấm, menu hoặc tính năng mà người dùng không có quyền truy cập. Mục đích duy nhất là tạo ra trải nghiệm người dùng sạch sẽ, tránh gây bối rối và ngăn ngừa các cú click nhầm. **Tuyệt đối không được coi đây là cơ chế bảo mật (Security Control).**
   - **Lớp 2 - Bộ Điều Hướng Phía Trình Duyệt (Frontend Route Guards):** Can thiệp trước khi chuyển trang (`router.beforeEach`). Khi người dùng cố tình gõ URL vào thanh địa chỉ của trình duyệt mà không đủ quyền, guard sẽ chặn lại và chuyển hướng về trang `/403` (Forbidden) kèm thông điệp giải thích. Đây là cơ chế Fast-Fail UX, ngăn việc tải mã giao diện không cần thiết.
   - **Lớp 3 - Chốt Chặn Phía Máy Chủ (Backend Authoritative Enforcement):** Đây là ranh giới bảo mật DUY NHẤT và CÓ THẨM QUYỀN TUYỆT ĐỐI. Mọi API request đều được lọc qua Spring Security filter chain (`SecurityConfig`) và các annotation ở mức phương thức (`@PreAuthorize("hasRole(...)")`). Dù người dùng có dùng DevTools can thiệp vào router frontend hay dùng Postman gửi request giả mạo, backend vẫn từ chối và trả về mã lỗi HTTP 403 Forbidden.

2. **Cạm Bẫy Vòng Lặp Vô Tận (Infinite Navigation Loop) Trong Route Guards:**
   - Khi một route guard phát hiện người dùng không đủ quyền và thực hiện điều hướng `next('/403')` hoặc `return '/403'`, lệnh điều hướng mới này lại kích hoạt `router.beforeEach` một lần nữa.
   - Nếu route `/403` không được miễn trừ kiểm tra quyền hoặc bản thân nó lại đòi hỏi quyền nào đó, guard sẽ tiếp tục phát hiện lỗi và lại điều hướng về `/403`, tạo thành một vòng lặp vô tận khiến trình duyệt bị treo hoặc báo lỗi "Maximum call stack size exceeded".
   - **Giải pháp:** Luôn kiểm tra ngoại lệ: nếu `to.path === '/403'`, lập tức cho phép đi tiếp (`return true`).

3. **Kiểm Tra Quyền Hạn Trong Cấu Trúc Tuyến Đường Lồng Nhau (Nested Routes with `to.matched`):**
   - Trong Vue Router, một URL như `/admin/users` được cấu thành từ 2 bản ghi định tuyến: Layout cha (`/admin`) và Trang con (`/admin/users`).
   - Nếu ta chỉ kiểm tra `to.meta.roles`, ta có thể vô tình bỏ qua các ràng buộc ở cấp layout cha nếu meta chỉ được đặt ở cha mà không đặt ở con (hoặc ngược lại).
   - Sử dụng `to.matched.some(...)` hoặc duyệt toàn bộ danh sách `record` trong `to.matched` giúp đảm bảo người dùng phải đáp ứng thẩm quyền ở mọi cấp độ của tuyến đường.

### Ví dụ trong project này

Trong [frontend/src/router/guards.js](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/frontend/src/router/guards.js):
```javascript
export function isUserAuthorizedForRoute(to, userRoles = []) {
  const normalizedUserRoles = (userRoles || []).map(r => String(r).toUpperCase())

  // Kiểm tra tất cả các tầng route lồng nhau
  for (const record of to.matched || []) {
    const requiredRoles = record.meta?.roles || (record.meta?.role ? [record.meta.role] : null)
    if (requiredRoles && requiredRoles.length > 0) {
      const hasMatch = requiredRoles.some(role =>
        normalizedUserRoles.includes(String(role).toUpperCase())
      )
      if (!hasMatch) return false
    }
  }
  return true
}
```

Trang lỗi [frontend/src/pages/error/ForbiddenPage.vue](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/frontend/src/pages/error/ForbiddenPage.vue) cung cấp nút chuyển hướng an toàn dựa trên role thực tế của người dùng:
- `TEACHER` $\rightarrow$ `/admin/courses`
- `STUDENT` $\rightarrow$ `/student/dashboard`
- `ADMIN` / `SUPER_ADMIN` $\rightarrow$ `/admin/dashboard`

### Câu hỏi phỏng vấn liên quan

Tại sao việc ẩn các nút chức năng hoặc menu trên giao diện người dùng không được coi là một biện pháp bảo mật? Bạn thiết kế kiến trúc phân quyền nhiều tầng (Multi-tier Authorization) cho ứng dụng Single Page Application như thế nào?

### Câu trả lời ngắn gọn

Ẩn nút bấm trên UI chỉ là che đậy thị giác (Security through Obscurity). Bất kỳ ai cũng có thể mở DevTools để hiển thị lại nút bấm hoặc dùng curl/Postman để gọi trực tiếp endpoint của server.
Kiến trúc phân quyền chuẩn gồm 3 tầng:
1. **Tầng UI:** Ẩn các thành phần không thuộc thẩm quyền để tối ưu trải nghiệm (UX).
2. **Tầng Router Guard (Client):** Kiểm tra `meta.roles` trên toàn bộ cây định tuyến (`to.matched`), chuyển hướng người dùng vi phạm về trang 403 Forbidden để phản hồi nhanh.
3. **Tầng API & Controller (Server):** Dùng Spring Security (`@PreAuthorize`) xác thực token và kiểm tra quyền hạn thực tế. Đây là chốt chặn duy nhất có giá trị bảo mật bắt buộc.


## 185. Kiến Trúc Cổng Thanh Toán Độc Lập Nhà Cung Cấp (Provider-Agnostic Payment SPI), Chữ Ký Số HMAC-SHA512 & Webhook Idempotency

### Giải thích ngắn gọn

Trong các hệ thống thương mại điện tử, việc tích hợp cổng thanh toán (Payment Gateway) đòi hỏi độ tin cậy và khả năng mở rộng cao:
1. **Kiến Trúc Provider-Agnostic Payment SPI (Service Provider Interface):**
   - **Vấn đề:** Nếu tầng dịch vụ (`PaymentService`) gọi trực tiếp các phương thức đặc thù của VNPay, MoMo hay ZaloPay, code sẽ bị phân nhánh dày đặc bằng các khối `if-else` hoặc `switch-case`. Khi thêm cổng mới hoặc thay đổi quy cách chữ ký của một đối tác, toàn bộ luồng thanh toán cốt lõi có nguy cơ bị lỗi hồi quy (Regression).
   - **Giải pháp:** Xây dựng interface `PaymentGateway` chuẩn hóa làm ranh giới trừu tượng (Boundary), kết hợp mẫu thiết kế Strategy Pattern:
     - `PaymentInitCommand`: Chuẩn hóa dữ liệu đầu vào (mã đơn, số tiền, URL redirect, client IP).
     - `PaymentInitResult`: Chuẩn hóa dữ liệu trả về (URL chuyển hướng, mã giao dịch tham chiếu).
     - `PaymentWebhookPayload` & `PaymentWebhookResult`: Chuẩn hóa luồng tiếp nhận Webhook/IPN.
     - `PaymentGatewayRegistry`: Tự động thu thập mọi implementation của `PaymentGateway` và lập bản đồ `EnumMap<PaymentMethod, PaymentGateway>`, hỗ trợ tra cứu $O(1)$.
2. **Ký Số HMAC-SHA512 & Phòng Chống Timing Attack:**
   - **Tạo chữ ký khởi tạo (Init Signing):** Các tham số được sắp xếp theo thứ tự bảng chữ cái ASCII (`field1=val1&field2=val2...`), sau đó băm mật mã bằng thuật toán HMAC với khóa bí mật SHA-512 (`vnp_HashSecret`). Bên nhận tính toán lại chuỗi băm để xác nhận yêu cầu không bị chỉnh sửa (Tamper-Proof) trên đường truyền.
   - **Xác thực chữ ký IPN (Webhook Verification):** Kẻ tấn công có thể giả lập request IPN để đánh lừa hệ thống kích hoạt khóa học mà không trả tiền. Hệ thống phải trích xuất chữ ký từ request, tái tạo chuỗi dữ liệu gốc, tính toán lại mã hash và đối chiếu.
   - **Chống Timing Attack:** Tuyệt đối không dùng `String.equals()` vì toán tử này trả về `false` ngay khi gặp ký tự sai đầu tiên, cho phép kẻ tấn công đo độ trễ nano-giây để dò từng ký tự. Bắt buộc dùng `MessageDigest.isEqual()`, bảo đảm việc so sánh mảng byte diễn ra trong thời gian hằng số (Constant-Time).
3. **Tính Lũy Thừa (Idempotency) & Khóa Bi Quan Khi Xử Lý Webhook:**
   - **Bản chất của Webhook:** Mạng Internet không hoàn hảo; nhà mạng thanh toán áp dụng cơ chế Retry (gửi lại IPN nhiều lần sau 5m, 15m, 30m nếu chưa nhận được phản hồi).
   - **Xử lý Race Condition:** Khi 2 webhook gửi đến gần như đồng thời, nếu dùng optimistic locking hoặc đọc không khóa, cả 2 luồng đều có thể thấy đơn hàng ở trạng thái `PENDING` và cùng tiến hành kích hoạt ghi danh.
   - **Giải pháp:** Sử dụng `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`) trên `OrderRepository`. Luồng đầu tiên chiếm khóa, chuyển trạng thái đơn hàng sang `PAID` qua FSM (`order.transitionTo(PAID)`), kích hoạt ghi danh và commit. Luồng thứ hai sau khi có khóa sẽ thấy trạng thái đã là `PAID`, áp dụng quy tắc lũy thừa: bỏ qua việc ghi danh lặp lại và phản hồi ngay mã `02` (Order already confirmed) theo đúng chuẩn giao thức của VNPay.

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_payment/gateway/vnpay/VnPayPaymentGateway.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_payment/gateway/vnpay/VnPayPaymentGateway.java):
```java
// So sánh chữ ký an toàn với MessageDigest.isEqual chống timing attack
private boolean verifySignature(Map<String, String> params, String expectedHash) {
    String computedHashUrlEncoded = hashAllFields(fieldsToHash, true);
    String computedHashRaw = hashAllFields(fieldsToHash, false);

    byte[] expectedBytes = expectedHash.getBytes(StandardCharsets.UTF_8);
    boolean matchesUrlEncoded = MessageDigest.isEqual(
            computedHashUrlEncoded.getBytes(StandardCharsets.UTF_8), expectedBytes);
    boolean matchesRaw = MessageDigest.isEqual(
            computedHashRaw.getBytes(StandardCharsets.UTF_8), expectedBytes);

    return matchesUrlEncoded || matchesRaw;
}
```

Trong [backend/src/main/java/com/japaneselearning/module_payment/service/PaymentServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_payment/service/PaymentServiceImpl.java):
```java
// Idempotency: Nếu đơn hàng đã PAID từ trước, trả về mã 02 thành công ngay lập tức
if (order.getStatus() == OrderStatus.PAID) {
    log.info("VNPay IPN idempotent: Order already confirmed. orderCode={}", orderCode);
    return VnPayIpnResponse.orderAlreadyConfirmed();
}
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào để thiết kế một hệ thống tiếp nhận Webhook thanh toán an toàn, chịu lỗi và đảm bảo tính lũy thừa (Idempotency) khi đối tác thanh toán gửi lại (retry) nhiều lần?

### Câu trả lời ngắn gọn

Để tiếp nhận Webhook thanh toán chuẩn enterprise:
1. **Thẩm định chữ ký số:** Kiểm tra tính toàn vẹn của payload bằng HMAC-SHA512 với secret key, sử dụng hàm so sánh thời gian hằng số `MessageDigest.isEqual()` để triệt tiêu lỗ hổng Timing Attack.
2. **Khóa bi quan (Pessimistic Locking):** Dùng `SELECT ... FOR UPDATE` khóa bản ghi đơn hàng trong transaction để ngăn ngừa Race Condition giữa các webhook retry đến cùng lúc.
3. **Finite State Machine:** Ràng buộc việc chuyển đổi trạng thái (chỉ cho phép `PENDING` $\rightarrow$ `PAID`), nếu trạng thái hiện tại đã là `PAID` thì lập tức trả về mã xác nhận "Order already confirmed" mà không ghi danh hay trừ kho lần thứ hai.
4. **Kiểm tra số tiền:** Luôn so sánh số tiền trong webhook với số tiền lưu trữ trong DB để chống gian lận thay đổi giá trị đơn hàng (Tampering).


## 186. Bảo Mật Chuỗi Cung Ứng (Software Supply Chain Security), Quét Trivy SCA & Kiểm Soát Bù Đắp (Compensating Controls)

### Giải thích ngắn gọn

Trong quy trình phát triển DevSecOps hiện đại, việc quét lỗ hổng phụ thuộc và container (Software Composition Analysis - SCA) là một cổng kiểm soát chất lượng (Quality Gate) bắt buộc trong CI/CD:
1. **Phân Tầng Quét Bảo Mật (Multi-layer SCA Scanning):**
   - **Tầng mã nguồn / phụ thuộc build (`trivy fs`):** Quét trực tiếp file khai báo phụ thuộc (`pom.xml`, `package-lock.json`) để phát hiện các thư viện bên thứ ba chứa lỗ hổng CVE đã công bố.
   - **Tầng ảnh container triển khai (`trivy image`):** Quét hệ điều hành cơ sở (Base OS như Alpine, Debian) và các gói nhị phân (như curl, openssl) cùng artifact đóng gói (`app.jar`) trước khi đẩy lên Docker Registry.
2. **Thực Tiễn Kiểm Soát Bù Đắp (Compensating Controls Architecture):**
   - **Nghịch lý bản vá:** Không phải mọi CVE mức độ `CRITICAL` đều có bản vá ngay lập tức từ upstream, hoặc bản vá yêu cầu nâng cấp toàn bộ framework chính (ví dụ Spring Boot major upgrade) dẫn đến nguy cơ đứt gãy hệ thống trên diện rộng.
   - **Định nghĩa Compensating Controls:** Là các biện pháp kỹ thuật thay thế được áp dụng ở các tầng khác của kiến trúc nhằm vô hiệu hóa vector tấn công của lỗ hổng, khiến cho việc khai thác lỗ hổng đó trong ngữ cảnh thực tế của ứng dụng là bất khả thi.
   - **Quy trình quản lý ngoại lệ chuẩn hóa:**
     - Bước 1: Phân tích chi tiết CVE xem tính năng có lỗ hổng có được ứng dụng sử dụng hay không (ví dụ: lỗ hổng liên quan đến Form Authentication của Tomcat trong khi ứng dụng dùng Stateless JWT của Spring Security).
     - Bước 2: Thiết lập cấu hình bù đắp (ví dụ: tắt HTTP/2 ở container nội bộ, dập tắt WebDAV, sử dụng logging có cấu trúc Logstash JSON thay vì log mặc định của Tomcat).
     - Bước 3: Đưa mã CVE vào danh mục ngoại lệ được duyệt (`.trivyignore`) kèm bình luận giải trình kỹ thuật và dẫn chứng tài liệu kiến trúc.
3. **Thiết Kế CI/CD Pipeline Tránh "Silent Failures":**
   - Khi cấu hình các công cụ quét bảo mật xuất kết quả ra file báo cáo (`--output reports/report.txt`) kết hợp cờ ngắt build (`--exit-code 1`), nếu công cụ phát hiện lỗ hổng, nó sẽ thoát với mã lỗi 1 trước khi in ra stdout. Điều này dẫn đến việc lập trình viên chỉ thấy build failed mà không hề biết CVE nào bị vi phạm nếu không tải artifact zip về máy.
   - **Giải pháp:** Luôn bổ sung bước in nội dung file báo cáo với điều kiện `if: failure()` ngay sau bước quét để xuất toàn bộ bảng CVE trực tiếp ra console log của GitHub Actions.

### Ví dụ trong project này

Trong [.trivyignore](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/.trivyignore):
```text
# --- Tomcat Embedded (Spring Boot 3.3.6 Managed) ---
# Mitigated: Misleading security logs in Tomcat control flow; application relies on Logstash structured JSON audit logging and Spring Security, not Tomcat internal realm logs
CVE-2026-55276
```

Trong [.github/workflows/ci.yml](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/.github/workflows/ci.yml):
```yaml
      - name: Maven Dependency Vulnerability Scan (Trivy FS)
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: 'backend'
          format: 'table'
          output: 'reports/trivy-maven-report.txt'
          severity: 'CRITICAL'
          exit-code: '1'
          ignore-unfixed: false
          trivyignores: '.trivyignore'

      - name: Display Trivy Maven Report on Failure
        if: failure()
        run: |
          echo "=== Trivy Maven Dependency Vulnerability Report ==="
          cat reports/trivy-maven-report.txt || true
```

### Câu hỏi phỏng vấn liên quan

Khi pipeline CI bị chặn bởi một lỗ hổng bảo mật mức CRITICAL trong thư viện phụ thuộc của bên thứ ba mà chưa có bản vá chính thức, bạn sẽ xử lý tình huống này như thế nào?

### Câu trả lời ngắn gọn

Quy trình xử lý chuyên nghiệp gồm 4 bước:
1. **Phân tích bề mặt tấn công (Attack Surface Analysis):** Đọc kỹ CVE advisory để xác định điều kiện kích hoạt lỗ hổng. Nếu ứng dụng không dùng đến class/method hoặc servlet bị ảnh hưởng, nguy cơ thực tế có thể bằng không.
2. **Thiết lập biện pháp bù đắp (Compensating Controls):** Tắt tính năng dễ bị tấn công qua cấu hình framework (ví dụ tắt WebDAV, vô hiệu hóa Form auth, sử dụng reverse proxy để terminate giao thức nguy hiểm).
3. **Quản lý ngoại lệ có kiểm soát:** Thêm mã CVE vào `.trivyignore` kèm lý do kỹ thuật chi tiết, cập nhật tài liệu kiến trúc bảo mật (`docs/CI_SECURITY_AND_TESTING.md`) và thiết lập lịch rà soát (periodic review) khi phiên bản an toàn ra mắt.
4. **Giữ vững cổng kiểm soát CI:** Không bao giờ tắt bỏ công cụ quét bảo mật hoặc hạ mức severity chặn lỗi toàn cục (`CRITICAL`).

---

## 187. Kiểm Thử Hồi Quy Hợp Đồng API (API Contract Regression Testing) & Chuẩn Hóa Response Envelope

### Giải thích ngắn gọn

1. **Khái Niệm Hợp Đồng API (API Contract):**
   - Hợp đồng API là thỏa thuận bất biến giữa Backend (nhà cung cấp dịch vụ) và Frontend (bên tiêu thụ dịch vụ) về cấu trúc dữ liệu truyền nhận: đường dẫn endpoint, phương thức HTTP, headers, query params, request body, HTTP status codes, định dạng mã lỗi nội bộ và cấu trúc JSON trả về.
   - Khi ứng dụng phát triển lớn mạnh, các sửa đổi DTO, tái cấu trúc controller hoặc nâng cấp phiên bản framework rất dễ dẫn đến tình trạng vỡ hợp đồng ngầm (Silent Contract Breakage): ví dụ đổi tên trường từ `roles` thành `role`, gói kết quả thiếu trường `code: 1000`, trả về `null` thay vì mảng rỗng, hoặc thay đổi kiểu dữ liệu từ enum string sang int.
2. **Chuẩn Hóa Response Envelope (`ApiResponse<T>`):**
   - Thay vì trả về dữ liệu payload thô trực tiếp ở root của JSON, hệ thống sử dụng một lớp phong bì (Envelope Wrapper) chuẩn hóa toàn cục:
     - **Thành công:** `{ "code": 1000, "message": "...", "result": { ... } }` với HTTP status 200/201.
     - **Thất bại:** `{ "code": <errorCode>, "message": "..." }` với HTTP status tương ứng (400, 401, 403, 404, 500).
   - Envelope giúp frontend viết các interceptor và service helper xử lý tập trung: chỉ cần kiểm tra `res.data.code === 1000` là có thể tự tin bóc tách `result`, ngược lại lập tức ném lỗi với thông điệp chuẩn xác.
3. **Kiểm Thử Hồi Quy Hợp Đồng Tầng HTTP (Contract Regression Testing):**
   - Unit test kiểm tra logic nghiệp vụ tại tầng Service không thể bảo vệ hợp đồng API vì nó bypass hoàn toàn cơ chế serialization Jackson, Spring Security filter chain và `@RestControllerAdvice`.
   - Contract Regression Suite được xây dựng trên tầng MockMvc kết hợp cơ sở dữ liệu thật/in-memory, gửi request HTTP thực tế và sử dụng JsonPath assertions để khóa cứng cấu trúc schema JSON (`$.code`, `$.message`, `$.result.*`). Nếu bất kỳ lập trình viên nào xóa hoặc đổi tên trường DTO mà client đang phụ thuộc, bài test sẽ fail ngay lập tức trong CI.

### Ví dụ trong project này

Trong [backend/src/test/java/com/japaneselearning/contract/ApiContractRegressionIT.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/test/java/com/japaneselearning/contract/ApiContractRegressionIT.java):
```java
@Test
void login_validCredentials_returnsSuccessEnvelopeWithTokens() throws Exception {
    LoginReq req = LoginReq.builder()
            .username("student_a")
            .password("password123")
            .build();

    mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(1000))
            .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
            .andExpect(jsonPath("$.result.token").isString())
            .andExpect(jsonPath("$.result.refreshToken").isString())
            .andExpect(jsonPath("$.result.user.id").isNumber())
            .andExpect(jsonPath("$.result.user.username").value("student_a"));
}
```

Ở phía frontend [frontend/src/services/contract-regression.spec.js](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/frontend/src/services/contract-regression.spec.js):
```javascript
it('extracts result when code is 1000', () => {
    const envelope = { code: 1000, message: 'Thành công', result: { id: 1, name: 'N3 Course' } };
    expect(extractData(envelope)).toEqual({ id: 1, name: 'N3 Course' });
});
```

### Câu hỏi phỏng vấn liên quan

Tại sao cần có bộ kiểm thử API Contract Regression riêng biệt khi dự án đã có sẵn các bài Unit Test và E2E Test?

### Câu trả lời ngắn gọn

Unit Test chỉ kiểm tra từng hàm nghiệp vụ riêng lẻ trong bộ nhớ với các đối tượng bị mock, hoàn toàn bỏ qua quá trình ánh xạ HTTP, JSON serialization/deserialization và bảo mật. E2E Test (như Cypress/Playwright) thì quá chậm, cồng kềnh và dễ bị chập chờn (flaky). Bộ kiểm thử API Contract Regression ở tầng MockMvc lấp đầy khoảng trống này: nó chạy nhanh như unit test nhưng gửi request HTTP thật qua toàn bộ middleware pipeline, kiểm tra chính xác cấu trúc JSON payload trả về, đảm bảo backend và frontend không bị lệch schema dù mã nguồn liên tục tái cấu trúc.

---

## 188. Chuẩn Phân Trang Kép (Dual Pagination Standard: Spring Data Page vs Custom PageResponse) & Adapter Pattern

### Giải thích ngắn gọn

1. **Thực Trạng Phân Trang Kép Trong Hệ Thống Thực Tế:**
   - Trong quá trình phát triển ứng dụng Spring Boot, có hai trường phái phân trang phổ biến:
     - **Spring Data `Page<T>`:** Trả về đối tượng phân trang mặc định của Spring Framework với các trường: `content` (danh sách dữ liệu), `number` (trang hiện tại zero-based index), `size`, `totalElements`, `totalPages`, `first`, `last`, `empty`. Thường được dùng ở các catalog API công khai hoặc controller kế thừa Spring Data REST.
     - **Custom `PageResponse<T>`:** Trả về DTO do đội ngũ tự định nghĩa nhằm chuẩn hóa tên trường theo phong cách dự án: `currentPage` (có thể là 1-based index), `pageSize`, `totalPages`, `totalElements`, `data` (mảng đối tượng). Thường được dùng ở các module nghiệp vụ như Đơn hàng, Quản trị người dùng.
   - Khi cả hai chuẩn này cùng tồn tại trong một hệ thống (Hybrid Architecture), nếu không có quy ước rõ ràng, frontend sẽ gặp lỗi hiển thị danh sách rỗng hoặc lỗi phân trang do đọc nhầm trường `data` thành `content` hoặc ngược lại.
2. **Giải Pháp Kiến Trúc: Adapter Pattern Ở Tầng Frontend Service:**
   - Thay vì ép buộc đập đi xây lại toàn bộ backend làm gãy các client cũ, tầng frontend áp dụng **Adapter Pattern** ngay tại các file service (ví dụ `order.service.js`).
   - Service tiếp nhận `PageResponse<T>` từ backend, bóc tách mảng `data` làm kết quả trả về, đồng thời gắn trực tiếp các metadata phân trang (`totalElements`, `totalPages`, `pageNumber`, `pageSize`) vào chính đối tượng Array trả về. Nhờ vậy, các component Vue/React có thể tiêu thụ dữ liệu danh sách như một mảng thông thường và vẫn đọc được thông tin phân trang mà không bị phụ thuộc vào sự khác biệt của backend.

### Ví dụ trong project này

Trong [frontend/src/services/order.service.js](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/frontend/src/services/order.service.js):
```javascript
export const getMyOrders = async (params = {}) => {
  const response = await api.get('/orders/me', { params });
  const envelope = response.data;
  const pageData = envelope.result || {};
  const orders = Array.isArray(pageData.data) ? pageData.data : [];
  
  // Gắn metadata phân trang vào mảng kết quả (Adapter Pattern)
  orders.totalElements = pageData.totalElements ?? orders.length;
  orders.totalPages = pageData.totalPages ?? 1;
  orders.pageNumber = pageData.currentPage ?? 0;
  orders.pageSize = pageData.pageSize ?? orders.length;
  return orders;
};
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào để xử lý sự bất đồng bộ về cấu trúc phân trang giữa các microservices hoặc các API endpoints khác nhau trong một ứng dụng Frontend lớn?

### Câu trả lời ngắn gọn

Ta áp dụng Adapter Pattern tại tầng Data Access / API Client của Frontend. Mỗi API Service sẽ chịu trách nhiệm chuyển đổi cấu trúc phân trang đặc thù của backend (như `Page<T>` của Spring, `PageResponse<T>` custom, hay `Cursor-based` pagination) về một interface chuẩn duy nhất của Frontend (ví dụ `{ items: T[], pageInfo: { page, size, total, hasNext } }`). Điều này giúp cô lập hoàn toàn các UI Component khỏi sự biến đổi của backend, tăng tính tái sử dụng của các pagination component và giảm rủi ro vỡ giao diện.

---

## 189. Cơ Chế Cô Lập Quyền Sở Hữu Tài Nguyên (Resource Ownership Isolation) & Chuyển Đổi MockUser Trong MockMvc

### Giải thích ngắn gọn

1. **Phân Quyền RBAC vs Cô Lập Quyền Sở Hữu (Ownership Isolation):**
   - **RBAC (Role-Based Access Control):** Chỉ kiểm tra người dùng có vai trò phù hợp hay không (ví dụ: endpoint yêu cầu role `STUDENT`). Tuy nhiên, nếu chỉ kiểm tra vai trò, một học viên A có role `STUDENT` vẫn có thể đọc đơn hàng, xem bài thi hoặc nộp bài thi của học viên B (cũng có role `STUDENT`).
   - **Ownership Isolation (Object-Level Authorization):** Ngăn chặn lỗ hổng BOLA (Broken Object Level Authorization - lỗ hổng số 1 trong OWASP API Security Top 10). Hệ thống phải đối chiếu danh tính người dùng hiện tại trong `SecurityContext` với chủ sở hữu thực sự của bản ghi dữ liệu (`record.getUser().getId().equals(currentUser.getId())`). Nếu không trùng khớp, backend bắt buộc phải từ chối với HTTP 403 Forbidden và mã lỗi cụ thể (ví dụ: code 5005 `ORDER_ACCESS_DENIED`, code 4021 `QUIZ_ATTEMPT_FORBIDDEN`).
2. **Kỹ Thuật Chuyển Đổi Danh Tính Giữa Chừng Trong Kiểm Thử MockMvc:**
   - Khi viết kiểm thử tích hợp cho kịch bản BOLA, ta cần:
     1. Dưới danh nghĩa User A: Tạo tài nguyên (ví dụ đơn hàng hoặc bài thi quiz).
     2. Dưới danh nghĩa User B: Cố tình truy cập hoặc chỉnh sửa tài nguyên đó và kỳ vọng HTTP 403.
   - Annotation `@WithMockUser(username = "userA")` chỉ có hiệu lực cố định cho toàn bộ vòng đời của test method, không thể đổi user giữa chừng.
   - **Giải pháp:** Sử dụng `SecurityMockMvcRequestPostProcessors.user(username).roles(role)` truyền trực tiếp vào từng lệnh `mockMvc.perform()`. Bằng cách này, mỗi HTTP request có thể giả lập một người dùng khác nhau trong cùng một kịch bản test tích hợp mà không cần tạo token JWT thật.

### Ví dụ trong project này

Trong [backend/src/test/java/com/japaneselearning/contract/ApiContractRegressionIT.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/test/java/com/japaneselearning/contract/ApiContractRegressionIT.java):
```java
@Test
void quizOwnership_studentBCannotSubmitStudentAAttempt_returns403Forbidden() throws Exception {
    // 1. Khởi tạo bài thi dưới danh tính student_a
    QuizAttempt attempt = QuizAttempt.builder()
            .user(studentUser)
            .quiz(testQuiz)
            .status(AttemptStatus.IN_PROGRESS)
            .startedAt(LocalDateTime.now())
            .build();
    attempt = quizAttemptRepository.save(attempt);

    QuizSubmitReq submitReq = new QuizSubmitReq();
    submitReq.setAttemptId(attempt.getId());
    submitReq.setAnswers(List.of());

    // 2. student_b cố tình nộp bài thi của student_a -> Bị chặn 403 Forbidden
    mockMvc.perform(post("/api/v1/quizzes/attempts/submit")
                    .with(user("student_b").roles("STUDENT"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(submitReq)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(4021)); // QUIZ_ATTEMPT_FORBIDDEN
}
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào để phân biệt giữa lỗi HTTP 401 Unauthorized và HTTP 403 Forbidden, đặc biệt trong ngữ cảnh Resource Ownership Authorization?

### Câu trả lời ngắn gọn

- **HTTP 401 Unauthorized:** Thực chất là lỗi "Unauthenticated" - hệ thống không biết bạn là ai (thiếu token, token hết hạn, token bị giả mạo).
- **HTTP 403 Forbidden:** Hệ thống đã xác thực thành công danh tính của bạn, nhưng bạn **không có quyền** thực hiện hành vi này. Trong Resource Ownership, người dùng đã đăng nhập hợp lệ với vai trò `STUDENT`, nhưng tài nguyên đơn hàng hay bài thi thuộc quyền sở hữu của một học viên khác. Do đó, server phải trả về HTTP 403 (không phải 401 hay 404) để thể hiện rõ quyền truy cập bị từ chối.

---

## 190. Chiến Lược Dọn Dẹp Dữ Liệu Kiểm Thử Tích Hợp: `@DirtiesContext` vs Dọn Dẹp Thủ Công Theo Thứ Tự Khóa Ngoại (FK-Aware Teardown)

### Giải thích ngắn gọn

1. **Hiểm Họa Hiệu Năng Của `@DirtiesContext(BEFORE_EACH_TEST_METHOD)`:**
   - Khi viết kiểm thử tích hợp với Spring Boot (`@SpringBootTest`), các bài test thường chia sẻ chung một cơ sở dữ liệu in-memory (như H2). Để tránh xung đột dữ liệu giữa các test, lập trình viên thường đặt `@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)`.
   - Cơ chế này buộc Spring TestContext Framework phải đóng toàn bộ `ApplicationContext`, tạo lại DB schema và nạp lại toàn bộ bean trước **từng test method**. Với một test suite có 34 test cases, việc này có thể khiến thời gian chạy tăng vọt từ vài giây lên vài phút, gây nghẽn CI/CD.
2. **Chiến Lược Tối Ưu: `@DirtiesContext(AFTER_CLASS)` & FK-Aware Teardown:**
   - Thay vì khởi động lại context liên tục, ta chỉ tải Spring Context một lần duy nhất cho toàn bộ test class và dọn dẹp context sau khi chạy xong (`AFTER_CLASS`).
   - Dữ liệu giữa các test case được dọn dẹp bằng hàm `@BeforeEach` thông qua các repository `deleteAll()`.
   - **Quy tắc vàng về Thứ tự Khóa Ngoại (FK Dependency Order):** Nếu xóa bảng cha (bảng được tham chiếu) trước bảng con (bảng chứa foreign key), cơ sở dữ liệu quan hệ sẽ lập tức ném ngoại lệ `DataIntegrityViolationException: Referential integrity constraint violation`. Do đó, hàm dọn dẹp bắt buộc phải xóa theo thứ tự hình cây từ lá lên gốc:
     `Bảng liên kết/Bảng con` -> `Bảng nghiệp vụ trung gian` -> `Bảng gốc (Root entities)`.

### Ví dụ trong project này

Trong [backend/src/test/java/com/japaneselearning/contract/ApiContractRegressionIT.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/test/java/com/japaneselearning/contract/ApiContractRegressionIT.java):
```java
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ApiContractRegressionIT {

    @BeforeEach
    void setUp() {
        // Xóa sạch dữ liệu cũ theo đúng thứ tự khóa ngoại để tránh DataIntegrityViolationException
        quizAttemptAnswerRepository.deleteAll(); // Con của quiz_attempt
        quizAttemptRepository.deleteAll();       // Con của user & quiz
        orderItemRepository.deleteAll();         // Con của order
        orderRepository.deleteAll();             // Con của user
        enrollmentRepository.deleteAll();        // Con của user & course
        courseRepository.deleteAll();            // Con của category / độc lập
        userRepository.deleteAll();              // Bảng gốc

        // Khởi tạo dữ liệu mẫu cho bài test mới...
    }
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc dọn dẹp dữ liệu kiểm thử trong `@BeforeEach` lại được ưa chuộng hơn trong `@AfterEach`?

### Câu trả lời ngắn gọn

Dọn dẹp trong `@BeforeEach` đảm bảo môi trường kiểm thử luôn ở trạng thái sạch sẽ tuyệt đối ngay trước khi test case bắt đầu, bất kể test case trước đó kết thúc thành công, ném ngoại lệ bất ngờ hay bị dừng đột ngột. Nếu chỉ dọn dẹp trong `@AfterEach`, khi một test case bị crash giữa chừng hoặc assert fail làm ngắt method, bước dọn dẹp có thể bị bỏ qua khiến dữ liệu rác tồn đọng làm hỏng các test case chạy sau đó. Ngoài ra, việc dọn dẹp ở `@BeforeEach` còn cho phép lập trình viên giữ lại dữ liệu trong database sau khi test fail để thuận tiện cho việc kiểm tra và gỡ lỗi (debugging).



---

## 191. Phân Cấp Xuất Bản Đa Tầng & Chính Sách Ranh Giới Nội Dung Nghiêm Ngặt (Content Publication Hierarchy & Cascading Boundary)

### Giải thích ngắn gọn

1. **Vấn Đề Với Việc Kiểm Tra Trạng Thái Đơn Lẻ:**
   - Trong các hệ thống giáo dục trực tuyến hoặc CMS có cấu trúc phân cấp nhiều tầng:
     `Course (Khóa học)` -> `CourseSection (Chương học)` -> `Lesson (Bài học)` -> `Quiz (Bài kiểm tra) / Resource (Tài liệu)`.
   - Một sai lầm phổ biến là chỉ kiểm tra trạng thái xuất bản (`status == PUBLISHED`) của chính thực thể đang được truy vấn (ví dụ kiểm tra `lesson.getStatus()`).
   - Nếu tác giả đang soạn thảo một chương mới (`CourseSection` có trạng thái `DRAFT` hoặc `HIDDEN`) nhưng bên trong đó có một bài học đã lỡ bấm chuyển sang `PUBLISHED`, việc chỉ kiểm tra trạng thái của bài học sẽ làm lộ toàn bộ bài học này ra ngoài công chúng hoặc cho phép học viên truy cập sớm nội dung chưa sẵn sàng.
2. **Nguyên Tắc Xuất Bản Truyền Thừa (Cascading Publication Hierarchy):**
   - Một thực thể con chỉ được coi là "đã xuất bản và sẵn sàng phục vụ học viên" khi và chỉ khi **tất cả các thực thể tổ tiên (ancestors)** của nó đều ở trạng thái `PUBLISHED`:
     `Course.status == PUBLISHED` $\wedge$ `Section.status == PUBLISHED` $\wedge$ `Lesson.status == PUBLISHED` $\wedge$ `Quiz.status == PUBLISHED`.
   - Nếu bất kỳ nút nào trên đường dẫn từ gốc đến lá chưa được xuất bản (`DRAFT`, `HIDDEN`, `ARCHIVED`), toàn bộ nhánh bên dưới nó phải bị cô lập hoàn toàn khỏi mọi API công khai và API học tập của học viên.

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_learning/service/impl/LearningServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_learning/service/impl/LearningServiceImpl.java):
```java
private Lesson validateAndGetLessonAccess(Long lessonId) {
    Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));

    // Thực thi ranh giới xuất bản phân cấp: Khóa học, Chương học và Bài học đều phải PUBLISHED
    Course course = lesson.getCourse();
    if (course == null || course.getStatus() != CourseStatus.PUBLISHED) {
        throw new AppException(ErrorCode.LESSON_NOT_FOUND);
    }

    if (lesson.getSection() == null || lesson.getSection().getStatus() != CourseStatus.PUBLISHED) {
        throw new AppException(ErrorCode.LESSON_NOT_FOUND);
    }

    if (lesson.getStatus() != CourseStatus.PUBLISHED) {
        throw new AppException(ErrorCode.LESSON_NOT_FOUND);
    }

    // Tiếp tục kiểm tra quyền xem trước hoặc ghi danh khóa học...
    return lesson;
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc kiểm tra trạng thái của thực thể cha không nên dựa vào lazy-loading thông thường trong các hệ thống phân cấp lớn?

### Câu trả lời ngắn gọn

Trong Spring Boot / Hibernate, nếu các quan hệ cha con (`lesson.getSection()`, `lesson.getCourse()`) được cấu hình `FetchType.LAZY` mặc định, việc duyệt qua từng tầng cha trong vòng lặp hoặc service method sẽ kích hoạt hàng loạt câu truy vấn phụ (vấn đề N+1 Query). Để tối ưu hóa, ta nên sử dụng `@EntityGraph` (nạp trước các thực thể cha liên quan trong 1 câu SQL JOIN duy nhất) hoặc truy vấn trực tiếp bằng JPQL có kiểm tra điều kiện trạng thái của cả cha lẫn con ở tầng cơ sở dữ liệu (`WHERE l.status = 'PUBLISHED' AND l.section.status = 'PUBLISHED' AND l.course.status = 'PUBLISHED'`).

---

## 192. Lọc Dữ Liệu Tại Tầng Repository (Repository-Level Filtering) vs Lọc Bằng Java Stream (In-Memory Stream Filtering)

### Giải thích ngắn gọn

1. **Hiểm Họa Của Lọc Dữ Liệu Bằng Bộ Nhớ (In-Memory Stream Filtering):**
   - Khi cần hiển thị danh sách bài học công khai của một khóa học, một cách viết ngây thơ là gọi `lessonRepository.findByCourseId(courseId)` để lấy toàn bộ danh sách, sau đó dùng `.stream().filter(l -> l.getStatus() == PUBLISHED).toList()`.
   - *Về hiệu năng:* Cơ sở dữ liệu phải quét toàn bộ bảng và gửi qua đường truyền mạng cả những bản ghi nháp/ẩn. Hibernate buộc phải khởi tạo (instantiate) hàng chục Java Object và đưa chúng vào JPA 1st-level cache (Persistence Context), gây lãng phí CPU và heap memory nghiêm trọng.
   - *Về bảo mật:* Dữ liệu nháp nhạy cảm đã nằm trong bộ nhớ ứng dụng. Nếu một lập trình viên khác tái sử dụng phương thức repository này ở một API khác mà quên thêm bước `.filter()`, dữ liệu mật sẽ lập tức rò rỉ ra ngoài mà không có tầng phòng thủ nào ngăn chặn.
2. **Sức Mạnh Của Repository-Level Filtering:**
   - Đẩy toàn bộ mệnh đề lọc xuống câu truy vấn SQL/JPQL:
     `SELECT l FROM Lesson l WHERE l.course.id = :courseId AND l.status = :status AND l.section.status = :sectionStatus`.
   - *Ưu điểm:*
     - Database engine tận dụng chỉ mục (Index) trên cột `(course_id, status)` để chỉ đọc đúng các dòng thỏa mãn điều kiện.
     - Giảm thiểu lưu lượng mạng giữa Database và Application Server.
     - Nguyên tắc "Secure by Default": Tầng Persistence chỉ trả về các bản ghi hợp lệ, loại bỏ rủi ro rò rỉ dữ liệu do sơ suất lập trình ở tầng Controller/Service.

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_course/repository/LessonRepository.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_course/repository/LessonRepository.java):
```java
@Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId " +
       "AND l.status = :status AND l.section.status = :sectionStatus " +
       "ORDER BY l.sortOrder ASC")
List<Lesson> findByCourseIdAndStatusAndSectionStatusOrderBySortOrderAsc(
        @Param("courseId") Long courseId,
        @Param("status") CourseStatus status,
        @Param("sectionStatus") CourseStatus sectionStatus);
```
Và sử dụng tại [CoursePublicServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_course/service/impl/CoursePublicServiceImpl.java):
```java
// Lấy trực tiếp danh sách bài học đã xuất bản trong các chương đã xuất bản mà không cần nạp bài nháp vào bộ nhớ
List<Lesson> publishedLessons = lessonRepository.findByCourseIdAndStatusAndSectionStatusOrderBySortOrderAsc(
        courseId, CourseStatus.PUBLISHED, CourseStatus.PUBLISHED);
```

### Câu hỏi phỏng vấn liên quan

Khi nào thì nên kết hợp cả Repository-Level Filtering và In-Memory DTO Transformation?

### Câu trả lời ngắn gọn

Nên sử dụng Repository-Level Filtering để **loại bỏ triệt để các bản ghi bị cấm truy cập** (ví dụ bản ghi chưa xuất bản, bản ghi thuộc tenant khác) ngay từ database; sau đó sử dụng In-Memory Transformation (Stream map / DTO mapper) để **chuyển đổi cấu trúc hiển thị hoặc làm mờ dữ liệu** (Data Masking) dựa trên ngữ cảnh người dùng. Ví dụ: trong danh sách bài học đã xuất bản, ta dùng Stream map để xóa `videoUrl` và `content` đối với các bài học không phải xem trước (`isPreview = false`) khi người dùng chưa đăng nhập hoặc chưa mua khóa học.

---

## 193. Ngăn Chặn Lỗ Hổng Dò Quét ID (ID Enumeration / Information Disclosure) Bằng Phản Hồi Đồng Nhất HTTP 404 Not Found

### Giải thích ngắn gọn

1. **Lỗ Hổng Phân Biệt Mã Lỗi (Error Code Side-Channel / Information Disclosure):**
   - Giả sử một nền tảng giáo dục cho phép tác giả soạn thảo các khóa học đặc biệt sắp ra mắt (chứa thông tin bài thi hoặc lộ trình bí mật).
   - Nếu khi học viên gõ ID của một bài học không tồn tại (ví dụ `ID = 9999`), server trả về `HTTP 404 Not Found`.
   - Nhưng khi học viên gõ ID của một bài học nháp của giảng viên (ví dụ `ID = 105`), server kiểm tra thấy bài học chưa xuất bản và trả về `HTTP 403 Forbidden` (hoặc `401 Unauthorized`).
   - Kẻ tấn công có thể viết một script tự động quét từ `ID = 1` đến `10000`. Dựa vào sự khác biệt giữa 404 và 403, kẻ tấn công dễ dàng xác định được ID nào là bài học có thật trong hệ thống, từ đó khai thác tiếp các lỗ hổng khác (như Race Condition, IDOR, hoặc dò quét tên bài học).
2. **Nguyên Tắc Xử Lý Chuẩn OWASP:**
   - Đối với người dùng thông thường và khách vãng lai, một tài nguyên chưa xuất bản hoặc bị ẩn phải được đối xử **hoàn toàn tương đương với một tài nguyên không tồn tại**.
   - Do đó, cả hai trường hợp "ID không có trong database" và "ID tồn tại nhưng tài nguyên (hoặc cha của nó) chưa xuất bản" đều phải trả về cùng một mã HTTP `404 Not Found` kèm cùng một mã lỗi nghiệp vụ (ví dụ `LESSON_NOT_FOUND` hoặc `QUIZ_NOT_FOUND`).

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_learning/service/impl/QuizLearningServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_learning/service/impl/QuizLearningServiceImpl.java):
```java
private void validateQuizPublication(Quiz quiz) {
    if (quiz.getStatus() != QuizStatus.PUBLISHED) {
        throw new AppException(ErrorCode.QUIZ_NOT_FOUND); // Trả về 404 thay vì 403
    }
    Lesson lesson = quiz.getLesson();
    if (lesson == null || lesson.getStatus() != CourseStatus.PUBLISHED) {
        throw new AppException(ErrorCode.QUIZ_NOT_FOUND); // Trả về 404
    }
    if (lesson.getSection() == null || lesson.getSection().getStatus() != CourseStatus.PUBLISHED) {
        throw new AppException(ErrorCode.QUIZ_NOT_FOUND); // Trả về 404
    }
    if (lesson.getCourse() == null || lesson.getCourse().getStatus() != CourseStatus.PUBLISHED) {
        throw new AppException(ErrorCode.QUIZ_NOT_FOUND); // Trả về 404
    }
}
```

### Câu hỏi phỏng vấn liên quan

Có trường hợp nào mà tài nguyên chưa xuất bản lại cần trả về HTTP 403 Forbidden thay vì 404 Not Found không?

### Câu trả lời ngắn gọn

Có, đó là đối với các API dành riêng cho **tác giả (Author/Teacher) hoặc quản trị viên (Admin)** (ví dụ: `GET /api/v1/admin/lessons/{id}` hoặc `GET /api/v1/teacher/lessons/{id}`). Với các API này, danh tính của giảng viên đã được xác định; nếu giảng viên A cố tình truy cập vào bài nháp của giảng viên B mà không có quyền biên tập, hệ thống phải trả về `HTTP 403 Forbidden` (hoặc mã lỗi phân quyền tác giả) để thông báo rõ ràng rằng tài nguyên có tồn tại trong hệ thống quản trị nhưng bạn không có thẩm quyền chỉnh sửa.

---

## 194. Thứ Tự Kiểm Tra Xác Thực Đa Lớp Trong Các Workflow Nghiệp Vụ Phức Tạp (Multi-Step Authorization Ordering)

### Giải thích ngắn gọn

1. **Sự Xung Đột Về Mã Lỗi Khi Nhiều Điều Kiện Cùng Thất Bại:**
   - Trong một workflow xử lý nghiệp vụ phức tạp như nộp bài kiểm tra (`submitAttempt`), một request có thể vi phạm nhiều điều kiện bảo mật cùng một lúc:
     - Kịch bản: Kẻ tấn công (User B) chưa ghi danh khóa học, nhưng lại cố tình gửi một request nộp bài thi cho một lượt làm bài (`attemptId`) vốn thuộc về User A.
     - Nếu hệ thống kiểm tra "Người dùng hiện tại đã mua khóa học này chưa?" trước tiên, hệ thống sẽ trả về lỗi `HTTP 403 FORBIDDEN_ACCESS (Mã 3003)`.
     - Tuy nhiên, hành vi thực sự nghiêm trọng ở đây là **cố tình chiếm đoạt và can thiệp vào tài nguyên của người khác** (Lỗ hổng BOLA / IDOR). Lỗi đúng về mặt ngữ nghĩa phải là `HTTP 403 QUIZ_ATTEMPT_FORBIDDEN (Mã 4021)`.
2. **Quy Tắc Sắp Xếp Thứ Tự Phòng Thủ (Defense-in-Depth Pipeline):**
   - **Tầng 1: Trạng thái & Tồn tại của Tài nguyên (Resource Publication & Existence):** Kiểm tra xem bài thi và bài học có tồn tại và đang xuất bản hay không. Nếu không -> 404 Not Found.
   - **Tầng 2: Quyền sở hữu phiên làm việc (Attempt/Session Ownership):** Kiểm tra `attempt.getUser().equals(currentUser)`. Ngăn chặn hành vi giả mạo hoặc can thiệp phiên của người khác -> 403 Ownership Denied.
   - **Tầng 3: Tính toàn vẹn của dữ liệu nghiệp vụ (Business Data Integrity):** Kiểm tra xem lượt thi có đúng là của bài thi được chỉ định không (`attempt.getQuiz().equals(quiz)`). Nếu sai -> 400 Bad Request.
   - **Tầng 4: Quyền sử dụng dịch vụ (Domain Precondition / Enrollment):** Kiểm tra xem người dùng có quyền ghi danh khóa học chứa bài thi này không. Nếu chưa -> 403 Forbidden Access.

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_learning/service/impl/QuizLearningServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_learning/service/impl/QuizLearningServiceImpl.java):
```java
@Override
public QuizResultResp submitAttempt(QuizSubmitReq req) {
    QuizAttempt attempt = quizAttemptRepository.findById(req.getAttemptId())
            .orElseThrow(() -> new AppException(ErrorCode.QUIZ_ATTEMPT_NOT_FOUND));

    Quiz quiz = attempt.getQuiz();

    // 1. Kiểm tra trạng thái xuất bản bài thi (và bài học/chương học cha)
    validateQuizPublication(quiz);

    // 2. Kiểm tra quyền sở hữu lượt thi TRƯỚC TIÊN (Ownership Isolation)
    User currentUser = getCurrentUser();
    if (!attempt.getUser().getId().equals(currentUser.getId())) {
        throw new AppException(ErrorCode.QUIZ_ATTEMPT_FORBIDDEN); // Mã 4021
    }

    // 3. Kiểm tra tính toàn vẹn phiên
    if (attempt.getStatus() != QuizAttemptStatus.IN_PROGRESS) {
        throw new AppException(ErrorCode.QUIZ_ATTEMPT_ALREADY_SUBMITTED);
    }

    // 4. Kiểm tra điều kiện ghi danh khóa học của chủ sở hữu
    validateQuizAccess(quiz);

    // Tiến hành chấm điểm và lưu kết quả...
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc kiểm tra quyền sở hữu đối tượng (Ownership) lại được ưu tiên thực hiện trước các kiểm tra nghiệp vụ bổ sung (như Enrollment Status)?

### Câu trả lời ngắn gọn

Bởi vì việc kiểm tra quyền sở hữu xác định mối quan hệ trực tiếp 1-1 giữa người thực hiện hành động và bản ghi dữ liệu đang bị tác động. Nếu một người dùng không phải là chủ sở hữu của bản ghi đó, họ hoàn toàn không có tư cách để hệ thống thực hiện thêm bất kỳ đánh giá nghiệp vụ nào tiếp theo cho họ. Nếu kiểm tra điều kiện nghiệp vụ trước, thông tin lỗi trả về (ví dụ "Bạn chưa ghi danh khóa học") có thể cung cấp manh mối (hint) sai lệch hoặc để lộ logic nội bộ cho kẻ tấn công, che giấu đi nỗ lực tấn công leo thang đặc quyền trái phép.


---

## 195. Mô Hình Chính Sách Truy Cập Dùng Chung (Shared Domain Access Policy Pattern) Trong Kiến Trúc Phân Tầng

### Giải thích ngắn gọn

1. **Hiểm Họa Của Logic Truy Cập Phân Tán (Decentralized Access Logic):**
   - Trong một ứng dụng lớn, nhiều module khác nhau cùng cần kiểm tra xem "Một người dùng có quyền sử dụng tài nguyên này hay không?". Ví dụ:
     - Module Course Detail cần biết có nên hiển thị nút "Tiếp tục học" (`isEnrolled = true`) hay không.
     - Module Dashboard cần biết có nên đưa khóa học này vào danh sách "Khóa học của tôi" hay không.
     - Module Learning cần biết có cho phép xem video bài học hay không.
     - Module Quiz cần biết có cho phép làm bài kiểm tra hay không.
     - Module Order cần biết có cho phép tạo đơn mua hay chặn vì đã ghi danh rồi.
   - Nếu mỗi service tự viết các câu lệnh kiểm tra riêng rẽ (ví dụ service A viết `existsByUserIdAndCourseId`, service B viết `status == ACTIVE`, service C viết `List.of(ACTIVE, COMPLETED)`), hệ thống sẽ lập tức rơi vào tình trạng **Lệch pha trạng thái (State Drift / Inconsistency)**.
2. **Nguyên Tắc Của Shared Domain Access Policy Pattern:**
   - Tập trung hóa toàn bộ định nghĩa về tính hợp lệ của quyền truy cập vào một lớp chính sách duy nhất nằm tại Domain/Module sở hữu thực thể đó (`EnrollmentAccessPolicy`).
   - Các dịch vụ khác chỉ được phép tham chiếu tới chính sách dùng chung này (thông qua hằng số tập hợp trạng thái `ACCESSIBLE_STATUSES` hoặc hàm kiểm tra `hasAccess()`), không tự ý định nghĩa lại danh sách trạng thái.
   - Khi có sự thay đổi về chính sách (ví dụ bổ sung trạng thái `GRACE_PERIOD` hoặc `TRIAL`), ta chỉ cần cập nhật tại một nơi duy nhất.

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_enrollment/policy/EnrollmentAccessPolicy.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_enrollment/policy/EnrollmentAccessPolicy.java):
```java
public final class EnrollmentAccessPolicy {

    private EnrollmentAccessPolicy() {}

    public static final Set<EnrollmentStatus> ACCESSIBLE_STATUSES = Set.of(
            EnrollmentStatus.ACTIVE,
            EnrollmentStatus.COMPLETED
    );

    public static boolean hasAccess(EnrollmentStatus status) {
        return status != null && ACCESSIBLE_STATUSES.contains(status);
    }

    public static boolean hasAccess(CourseEnrollment enrollment) {
        return enrollment != null && hasAccess(enrollment.getStatus());
    }

    public static boolean isAlreadyEnrolled(EnrollmentStatus status) {
        return hasAccess(status);
    }
}
```
Và được gọi đồng nhất tại [CoursePublicServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_course/service/CoursePublicServiceImpl.java):
```java
if (enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(userId, course.getId(), EnrollmentAccessPolicy.ACCESSIBLE_STATUSES)) {
    isEnrolled = true;
    ...
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao không nên đặt logic kiểm tra trạng thái truy cập trực tiếp trong Controller hoặc Service tầng ứng dụng?

### Câu trả lời ngắn gọn

Bởi vì quy tắc "trạng thái nào được xem là có quyền truy cập" là một **Business Rule thuần túy thuộc tầng Domain**. Đặt logic này ở Controller hoặc Service ứng dụng làm vi phạm nguyên tắc Single Responsibility (SRP) và Don't Repeat Yourself (DRY), dẫn đến nguy cơ cao các lập trình viên khác nhau sẽ diễn giải và code các điều kiện lọc khác nhau, gây rò rỉ dữ liệu hoặc chặn nhầm quyền của người dùng.

---

## 196. Bảo Toàn Bản Ghi Lịch Sử (Audit Trail Preservation) vs Ràng Buộc Duy Nhất (Unique Constraints) Trong Cơ Sở Dữ Liệu Quan Hệ

### Giải thích ngắn gọn

1. **Thế Tiến Thoái Lưỡng Nan Khi Xử Lý Hủy / Tái Ghi Danh:**
   - Giả sử bảng `course_enrollments` có ràng buộc duy nhất để chống ghi danh trùng lặp:
     `UNIQUE (user_id, course_id)`.
   - Khi một học viên hủy khóa học (hoặc được hoàn tiền), trạng thái ghi danh chuyển thành `CANCELLED`.
   - Nếu hệ thống thực hiện Hard Delete (xóa dòng dữ liệu đó khỏi DB), toàn bộ lịch sử học tập (`enrolledAt`, tiến độ cũ) và mối liên kết với hóa đơn thanh toán trước đây sẽ bị phá vỡ hoàn toàn, vi phạm tiêu chuẩn kiểm toán tài chính (Audit Trail).
   - Tuy nhiên, nếu giữ lại dòng dữ liệu `CANCELLED`, khi học viên quyết định mua lại khóa học và hệ thống thực thi câu lệnh `INSERT`, cơ sở dữ liệu sẽ lập tức ném lỗi vi phạm khóa duy nhất: `DataIntegrityViolationException: Unique index or primary key violation`.
2. **Giải Pháp Kiến Trúc: In-Place Reactivation:**
   - Thay vì `INSERT` một bản ghi mới hoặc `DELETE` bản ghi cũ, hệ thống kiểm tra sự tồn tại của bản ghi theo cặp khóa `(user_id, course_id)`.
   - Nếu bản ghi đã tồn tại với trạng thái `CANCELLED` hoặc `PAUSED`, hệ thống thực hiện cập nhật tại chỗ:
     `UPDATE course_enrollments SET status = 'ACTIVE' WHERE id = :existingId`.
   - Nhờ đó:
     - Khóa chính `id` của bản ghi được bảo toàn, giữ nguyên toàn bộ quan hệ khóa ngoại với các bảng khác.
     - Thời điểm ghi danh ban đầu `enrolledAt` và lịch sử kiểm toán không bị mất.
     - Ràng buộc `UNIQUE (user_id, course_id)` không bao giờ bị vi phạm.

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_payment/service/PaymentServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_payment/service/PaymentServiceImpl.java):
```java
Optional<CourseEnrollment> existingOpt = enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId());
if (existingOpt.isPresent()) {
    CourseEnrollment existing = existingOpt.get();
    if (!EnrollmentAccessPolicy.hasAccess(existing.getStatus())) {
        existing.setStatus(EnrollmentStatus.ACTIVE);
        enrollmentRepository.save(existing);
        courseRepository.incrementTotalStudents(course.getId());
    }
} else {
    CourseEnrollment newEnrollment = CourseEnrollment.builder()
            .user(user)
            .course(course)
            .status(EnrollmentStatus.ACTIVE)
            .progressPercent(0)
            .build();
    enrollmentRepository.save(newEnrollment);
    courseRepository.incrementTotalStudents(course.getId());
}
```

### Câu hỏi phỏng vấn liên quan

Có giải pháp nào khác trên tầng Database Schema để hỗ trợ lưu nhiều bản ghi ghi danh mà không vi phạm Unique Constraint không?

### Câu trả lời ngắn gọn

Có thể áp dụng kỹ thuật **Partial Unique Index** (trong PostgreSQL) hoặc bổ sung cờ trạng thái/thời gian vào khóa duy nhất:
1. *Partial Unique Index:* `CREATE UNIQUE INDEX idx_active_enrollment ON course_enrollments (user_id, course_id) WHERE status IN ('ACTIVE', 'COMPLETED')`. Điều này cho phép có nhiều bản ghi `CANCELLED` nhưng chỉ tối đa 1 bản ghi `ACTIVE`.
2. *Thêm phiên bản:* Tạo bảng `enrollment_history` riêng biệt để ghi log mỗi lần thay đổi trạng thái, trong khi bảng chính `course_enrollments` chỉ lưu trạng thái hiện tại.
Trong các hệ thống dùng chung Hibernate/JPA và H2 in-memory test, kỹ thuật *In-Place Reactivation* là giải pháp đơn giản, hiệu quả và tương thích tốt nhất trên mọi loại cơ sở dữ liệu.

---

## 197. Kỹ Thuật Tái Kích Hoạt Bản Ghi Tại Chỗ Có Tính Bất Biến (Idempotent In-Place Reactivation)

### Giải thích ngắn gọn

1. **Hiểm Họa Của Retry / Webhook Trùng Lặp Trong Thanh Toán:**
   - Cổng thanh toán (như VNPay, Stripe) có cơ chế gửi lại Webhook thông báo thanh toán thành công (IPN Retry) nhiều lần khi gặp sự cố mạng hoặc phản hồi chậm.
   - Nếu hàm xử lý ghi danh không có tính chất *Idempotent* (tính lũy đẳng), một bản ghi đã được cập nhật thành `ACTIVE` hoặc `COMPLETED` có thể bị ghi đè nhiều lần hoặc kích hoạt tăng bộ đếm số lượng học viên (`incrementTotalStudents`) sai lệch.
2. **Quy Tắc Tái Kích Hoạt Lũy Đẳng (Idempotent Reactivation Rule):**
   - Trước khi cập nhật trạng thái của bản ghi hiện có, bắt buộc phải kiểm tra trạng thái hiện tại của nó thông qua Access Policy:
     - Nếu trạng thái hiện tại **chưa có quyền** (ví dụ `CANCELLED`, `PAUSED`): Tiến hành chuyển sang `ACTIVE`, lưu bản ghi và tăng bộ đếm học viên đúng 1 lần.
     - Nếu trạng thái hiện tại **đã có quyền** (`ACTIVE` hoặc `COMPLETED`): Giữ nguyên trạng thái (tuyệt đối không hạ cấp `COMPLETED` về `ACTIVE`) và không tăng bộ đếm học viên.

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_enrollment/service/CourseEnrollmentServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_enrollment/service/CourseEnrollmentServiceImpl.java):
```java
Optional<CourseEnrollment> existingOpt = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId);
if (existingOpt.isPresent()) {
    CourseEnrollment existing = existingOpt.get();
    if (EnrollmentAccessPolicy.hasAccess(existing.getStatus())) {
        throw new AppException(ErrorCode.USER_ALREADY_ENROLLED); // Ngăn ghi danh trùng
    }
    // Tái kích hoạt bản ghi lịch sử đã bị hủy hoặc tạm dừng
    existing.setStatus(EnrollmentStatus.ACTIVE);
    try {
        enrollmentRepository.saveAndFlush(existing);
        courseRepository.incrementTotalStudents(courseId);
        return;
    } catch (DataIntegrityViolationException ex) {
        throw new AppException(ErrorCode.USER_ALREADY_ENROLLED);
    }
}
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào để đảm bảo tính an toàn trong môi trường đồng thời (Concurrency Safety) khi hai request thanh toán hoặc ghi danh cùng lúc gọi vào In-Place Reactivation?

### Câu trả lời ngắn gọn

Ta kết hợp hai tầng bảo vệ:
1. *Pessimistic Locking / DB Row Lock:* Sử dụng `SELECT ... FOR UPDATE` khi đọc đơn hàng hoặc bản ghi ghi danh để tuần tự hóa các giao dịch thanh toán.
2. *Ràng buộc Database:* Ràng buộc `UNIQUE (user_id, course_id)` ở tầng database đóng vai trò là chốt chặn cuối cùng. Nếu hai transaction cố tình insert đồng thời, transaction đến sau sẽ văng `DataIntegrityViolationException` và được bọc thành mã lỗi nghiệp vụ an toàn thay vì làm hỏng dữ liệu.

---

## 198. Đồng Bộ Hóa Trạng Thái Đa Dịch Vụ Giữa Public Catalog, Dashboard Học Viên Và Business Workflows

### Giải thích ngắn gọn

1. **Hiện Tượng Xung Đột Trạng Thái Giữa Các Màn Hình (UI State Mismatch):**
   - Người dùng xem trang chi tiết khóa học: Thấy nút "Tiếp tục học" (vì backend trả về `isEnrolled: true`).
   - Người dùng vào trang Dashboard cá nhân: Không thấy khóa học đâu, hoặc thấy khóa học hiển thị với tiến độ nhưng bấm vào xem video thì bị báo lỗi 403 Forbidden.
   - Đây là biểu hiện kinh điển của việc các endpoint khác nhau sử dụng các logic kiểm tra trạng thái khác nhau.
2. **Thiết Lập Ma Trận Trạng Thái Nhất Quán (State Consistency Matrix):**
   - Một khóa học chỉ được coi là "đang sở hữu" khi nó thỏa mãn cả 3 tiêu chí:
     1. *Catalog:* `isEnrolled == true` $\iff status \in \{ACTIVE, COMPLETED\}$.
     2. *Dashboard:* Khóa học xuất hiện trong danh sách và được tính vào tổng tiến độ $\iff status \in \{ACTIVE, COMPLETED\}$.
     3. *Player & Quiz:* Được phép xem nội dung bài học trả phí và làm quiz $\iff status \in \{ACTIVE, COMPLETED\}$.
   - Khi trạng thái là `PAUSED` hoặc `CANCELLED`:
     - Catalog hiển thị nút "Mua khóa học" / "Đăng ký miễn phí" (`isEnrolled: false`).
     - Dashboard ẩn hoàn toàn khóa học này khỏi danh sách đang học.
     - Player & Quiz trả về HTTP 403 Forbidden Access.
     - Order Service cho phép tạo đơn hàng mới để học viên có thể mua lại.

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_user/service/StudentDashboardServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_user/service/StudentDashboardServiceImpl.java):
```java
@Override
@Transactional(readOnly = true)
public List<MyCourseRes> getMyCourses() {
    User user = getCurrentUser();
    // Dashboard chỉ lấy các khóa học mà học viên thực sự có quyền truy cập
    List<CourseEnrollment> enrollments = enrollmentRepository.findByUserIdAndStatusIn(
            user.getId(), EnrollmentAccessPolicy.ACCESSIBLE_STATUSES);

    if (enrollments.isEmpty()) {
        return List.of();
    }
    // ...
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao việc ẩn các khóa học `CANCELLED` hoặc `PAUSED` khỏi Dashboard học viên lại tốt hơn việc vẫn hiển thị nhưng gắn nhãn "Đã hủy" / "Tạm dừng"?

### Câu trả lời ngắn gọn

Tùy thuộc vào yêu cầu sản phẩm. Nếu giao diện Dashboard chính được thiết kế để học viên "Tiếp tục học" các bài học hàng ngày, việc hiển thị các khóa học không thể học sẽ làm rối mắt và gây lỗi 403 khi bấm nhầm. Nếu nghiệp vụ yêu cầu học viên xem lại các khóa học đã hủy, ta nên thiết kế một tab riêng biệt (ví dụ: "Lịch sử khóa học" hoặc "Khóa học đã tạm dừng") thay vì trộn lẫn vào danh sách khóa học đang hoạt động trên màn hình chính.


---

## 199. Cô Lập Quyền Sở Hữu Giảng Viên Trên Thao Tác Đọc Admin (Admin Teacher Read Isolation & Horizontal Privilege Escalation Prevention)

### Giải thích ngắn gọn

1. **Lỗ Hổng Đọc Ngang (Horizontal Read Leak) Trong Hệ Thống Multi-Tenant:**
   - Trong kiến trúc multi-tenant (nhiều giảng viên chia sẻ cùng một hệ thống), mỗi giảng viên chỉ nên nhìn thấy và thao tác trên các khóa học thuộc quyền sở hữu của mình.
   - Khi hệ thống bảo vệ các thao tác **ghi** (create, update, delete) bằng cách kiểm tra `course.getTeacher().getEmail().equals(currentUser)` trước khi cho phép thay đổi, nhưng quên kiểm tra trên thao tác **đọc** (`getCourse(Long id)`), một giảng viên có thể:
     - Đọc chi tiết khóa học nháp của giảng viên khác (thông tin giá bán, mô tả nội bộ, chiến lược định giá).
     - Thu thập slug, tiêu đề, và cấu trúc khóa học để sao chép nội dung.
   - Đây được phân loại là lỗ hổng **Broken Object Level Authorization (BOLA)** theo **OWASP API Security Top 10**, cụ thể là **Horizontal Privilege Escalation** (leo thang quyền ngang — cùng vai trò TEACHER nhưng truy cập tài nguyên của TEACHER khác).
2. **Nguyên Tắc Bảo Mật Đối Xứng Đọc-Ghi (Read-Write Symmetric Authorization):**
   - Mọi endpoint admin trả về DTO chứa thông tin tài nguyên (kể cả endpoint chỉ đọc `@Transactional(readOnly = true)`) đều **bắt buộc** phải thực thi cùng một hàm kiểm tra quyền sở hữu với các endpoint ghi.
   - **Quy tắc:** Nếu `updateCourse(id)` gọi `checkTeacherPermission(course)`, thì `getCourse(id)` cũng PHẢI gọi `checkTeacherPermission(course)`. Không có ngoại lệ.

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_course/service/CourseAdminServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_course/service/CourseAdminServiceImpl.java):
```java
@Override
@Transactional(readOnly = true)
public CourseRes getCourse(Long id) {
    Course course = courseRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_FOUND));
    checkTeacherPermission(course);  // Bắt buộc kiểm tra quyền sở hữu ngay cả khi chỉ đọc
    return mapToCourseRes(course);
}

private void checkTeacherPermission(Course course) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String currentUserEmail = auth.getName();
    
    boolean isAdminOrSuperAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

    if (!isAdminOrSuperAdmin) {
        if (course == null || course.getTeacher() == null || !course.getTeacher().getEmail().equals(currentUserEmail)) {
            throw new AppException(ErrorCode.DATA_ISOLATION_FORBIDDEN);
        }
    }
}
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào để phát hiện lỗ hổng Horizontal Privilege Escalation trên thao tác đọc trong quá trình kiểm toán bảo mật mã nguồn?

### Câu trả lời ngắn gọn

1. *Liệt kê tất cả các endpoint admin có tham số ID:* Tìm tất cả các phương thức service nhận tham số `Long id` hoặc `Long entityId` và trả về DTO.
2. *Kiểm tra sự hiện diện của lời gọi authorization guard:* Xác minh rằng mỗi phương thức đều gọi hàm kiểm tra quyền sở hữu (`checkTeacherPermission`, `checkDataIsolation`) **trước** khi trả về dữ liệu cho client.
3. *So sánh đối xứng với thao tác ghi:* Nếu `updateEntity(id)` có kiểm tra quyền nhưng `getEntity(id)` không có, đó chính là lỗ hổng đọc ngang.
4. *Viết integration test xác minh:* Tạo 2 người dùng cùng vai trò, người A tạo tài nguyên, người B gọi endpoint đọc bằng ID — kết quả phải luôn là 403 Forbidden.

---

## 200. Lỗ Hổng Oracle Attack Trên Empty-Result Query Trong Endpoint Listing Admin (Side-Channel Information Disclosure)

### Giải thích ngắn gọn

1. **Oracle Attack Trên Listing Endpoint:**
   - Trong các endpoint listing admin có filter theo `courseId` hoặc `lessonId`, nếu hệ thống chỉ kiểm tra quyền sở hữu khi kết quả truy vấn **không rỗng** (bằng cách lấy phần tử đầu tiên để resolve owner), thì trường hợp kết quả **rỗng** trở thành "lỗ hổng Oracle" (Oracle Attack Vector):
     - Request `GET /admin/quizzes?courseId=5` → 403 Forbidden → Kẻ tấn công biết: "Course 5 tồn tại, có bài thi, và thuộc người khác"
     - Request `GET /admin/quizzes?courseId=5` → 200 OK `{"content": []}` → Kẻ tấn công biết: "Course 5 tồn tại nhưng chưa có bài thi"
     - Request `GET /admin/quizzes?courseId=999` → 404 Not Found → Kẻ tấn công biết: "Course 999 không tồn tại"
   - Bằng cách quét tuần tự ID, kẻ tấn công lập được bản đồ hoàn chỉnh về sự tồn tại và trạng thái nội dung của toàn bộ khóa học, ngay cả khi không có quyền truy cập.
2. **Phòng Chống Bằng Parent Entity Pre-Check:**
   - Khi kết quả truy vấn rỗng, hệ thống phải nạp ngược lên thực thể cha (parent entity: `Lesson` từ `lessonId`, `Course` từ `courseId`) để tiếp tục thực thi kiểm tra cô lập dữ liệu.
   - Sau fix, response cho giảng viên ngoại bang luôn là 403 Forbidden bất kể tài nguyên có dữ liệu hay không, triệt tiêu hoàn toàn kênh bên (side channel).

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_quiz/service/QuizAdminServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_quiz/service/QuizAdminServiceImpl.java):
```java
if (lessonId != null) {
    quizPage = quizRepository.findByLessonId(lessonId, pageable);
    if (!quizPage.isEmpty()) {
        checkDataIsolation(resolveCourse(quizPage.getContent().get(0)));
    } else if (!isAdminOrSuperAdmin) {
        // Empty result → nạp parent entity để tiếp tục kiểm tra quyền sở hữu
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        checkDataIsolation(lesson.getCourse());
    }
}
```

### Câu hỏi phỏng vấn liên quan

Ngoài Oracle Attack trên empty result, còn có những kênh bên (side channels) nào khác có thể rò rỉ thông tin trong API?

### Câu trả lời ngắn gọn

1. *Timing-based side channel:* Thời gian phản hồi khác nhau giữa "tài nguyên tồn tại nhưng bị cấm" (cần thêm thời gian kiểm tra quyền) và "tài nguyên không tồn tại" (trả về nhanh). Phòng chống bằng cách đảm bảo cả hai nhánh đều thực thi cùng khối lượng xử lý tương đương.
2. *Error message side channel:* Thông báo lỗi chi tiết khác nhau giữa 403 "bạn không có quyền truy cập khóa học này" và 404 "khóa học không tồn tại" giúp kẻ tấn công phân biệt. Phòng chống bằng cách sử dụng mã lỗi và thông báo đồng nhất.
3. *HTTP header side channel:* Header `X-Powered-By`, `Server` hoặc response size khác nhau có thể tiết lộ thông tin. Phòng chống bằng cách chuẩn hóa response envelope và loại bỏ header nhạy cảm.

---

## 201. Pattern `resolveCourse(Quiz)` — Giải Phóng Parent Entity Từ Entity Con Gắn Đa Đường (Multi-Path Parent Resolution)

### Giải thích ngắn gọn

1. **Vấn Đề Entity Con Gắn Đa Đường:**
   - Trong mô hình dữ liệu, một `Quiz` có thể gắn trực tiếp vào `Course` (quiz tổng hợp cuối khóa) **hoặc** gắn vào `Lesson` (quiz bài học). Hai quan hệ `@ManyToOne` tồn tại song song: `quiz.getCourse()` và `quiz.getLesson()`.
   - Khi cần xác định `Course` sở hữu quiz để kiểm tra quyền giảng viên, code phải duyệt qua 2 đường:
     1. `quiz.getCourse()` → nếu không null → trả về course.
     2. `quiz.getLesson().getCourse()` → nếu quiz gắn vào lesson.
   - Nếu logic này bị lặp lại rải rác trong hàng chục phương thức CRUD (createQuiz, updateQuiz, deleteQuiz, getQuestion, createAnswer, ...), code trở nên khó bảo trì và dễ bị sai lệch khi thay đổi mô hình dữ liệu.
2. **Helper Method `resolveCourse(Quiz quiz)` — Single Resolution Point:**
   - Đóng gói logic giải phóng Course tại một điểm duy nhất.
   - Trả về `null` an toàn khi cả hai đường đều không có Course (phòng trường hợp edge case dữ liệu bất thường).
   - Giúp mọi phương thức CRUD chỉ cần gọi `checkDataIsolation(resolveCourse(quiz))` thay vì viết logic rẽ nhánh riêng.

### Ví dụ trong project này

Trong [backend/src/main/java/com/japaneselearning/module_quiz/service/QuizAdminServiceImpl.java](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_quiz/service/QuizAdminServiceImpl.java):
```java
private Course resolveCourse(Quiz quiz) {
    if (quiz == null) {
        return null;
    }
    if (quiz.getCourse() != null) {
        return quiz.getCourse(); // Quiz gắn trực tiếp vào Course
    }
    if (quiz.getLesson() != null) {
        return quiz.getLesson().getCourse(); // Quiz gắn vào Lesson → lấy Course qua Lesson
    }
    return null;
}

// Sử dụng thống nhất trong mọi thao tác CRUD:
checkDataIsolation(resolveCourse(quiz));         // Cho quiz operations
checkDataIsolation(resolveCourse(question.getQuiz())); // Cho question operations
checkDataIsolation(resolveCourse(answer.getQuestion() != null ? answer.getQuestion().getQuiz() : null)); // Cho answer operations
```

### Câu hỏi phỏng vấn liên quan

Khi mô hình dữ liệu cho phép entity con gắn đa đường vào entity cha, có nên áp dụng ràng buộc "chỉ cho phép một trong hai FK khác null" ở tầng database không?

### Câu trả lời ngắn gọn

Có, đây là best practice. Sử dụng CHECK CONSTRAINT ở tầng database: `CHECK ((course_id IS NOT NULL AND lesson_id IS NULL) OR (course_id IS NULL AND lesson_id IS NOT NULL))` để đảm bảo Quiz luôn gắn vào đúng một đường duy nhất. Kết hợp với validation ở tầng Service/DTO (`@Valid`) để ngăn chặn dữ liệu bất hợp lệ ngay trước khi chạm database. Helper method `resolveCourse()` đóng vai trò phòng thủ tầng ứng dụng, xử lý an toàn cả trường hợp dữ liệu di cư hoặc dữ liệu legacy chưa tuân thủ ràng buộc mới.

---

## 202. Null-Safe Guard Trong Multi-Tenant Data Isolation Check — Phòng Chống NPE Trên `@ManyToOne` Relationship

### Giải thích ngắn gọn

1. **Nguy Cơ NPE Trong Isolation Guard:**
   - Trong JPA, quan hệ `@ManyToOne` (ví dụ `Course.teacher`) cho phép giá trị `null` ở tầng persistence nếu cột foreign key trong bảng không có ràng buộc `NOT NULL` hoặc đã bị xóa thủ công.
   - Khi hàm kiểm tra quyền sở hữu (isolation guard) thực thi `course.getTeacher().getEmail()` mà `course.getTeacher()` trả về `null`, JVM ném `NullPointerException`.
   - Nếu NPE không được xử lý, Spring `@RestControllerAdvice` sẽ bắt ngoại lệ này và trả về HTTP 500 Internal Server Error, kèm theo stack trace chứa tên package, tên class, số dòng code — rò rỉ thông tin kiến trúc nội bộ cho kẻ tấn công.
2. **Chiến Lược Xử Lý: Coi null teacher là vi phạm cô lập dữ liệu (403):**
   - Một Course không có teacher (teacher = null) là trạng thái bất thường (orphan data). Trong ngữ cảnh multi-tenant isolation, không xác định được chủ sở hữu → không thể xác minh quyền → mặc định từ chối truy cập.
   - Trả về 403 `DATA_ISOLATION_FORBIDDEN` thay vì 500, vừa bảo vệ tính ổn định hệ thống vừa tuân thủ nguyên tắc "Deny by Default".

### Ví dụ trong project này

Pattern null-safe guard được áp dụng đồng nhất trong cả 5 admin service:
```java
// Trước fix (dễ bị NPE):
if (!course.getTeacher().getEmail().equals(currentUserEmail)) {
    throw new AppException(ErrorCode.DATA_ISOLATION_FORBIDDEN);
}

// Sau fix (null-safe):
if (course == null || course.getTeacher() == null || !course.getTeacher().getEmail().equals(currentUserEmail)) {
    throw new AppException(ErrorCode.DATA_ISOLATION_FORBIDDEN);
}
```

### Câu hỏi phỏng vấn liên quan

Ngoài null-safe check, còn có cách nào khác để ngăn ngừa NPE trong JPA entity relationship?

### Câu trả lời ngắn gọn

1. *Database-level NOT NULL constraint:* `ALTER TABLE courses ADD CONSTRAINT fk_teacher NOT NULL` đảm bảo mọi course luôn có teacher từ tầng database.
2. *JPA `@Column(nullable = false)`:* Hibernate sẽ ném `ConstraintViolationException` khi cố persist entity có foreign key null.
3. *Java `Optional<T>`:* Thiết kế getter trả về `Optional<User> getTeacher()` thay vì `User getTeacher()`, buộc caller phải xử lý trường hợp null.
4. *Lombok `@NonNull`:* Tự động thêm null-check trong constructor và setter, ném `NullPointerException` ngay khi gán giá trị null.
Trong thực tế production, kết hợp cả hai tầng: DB-level NOT NULL constraint (phòng thủ tầng lưu trữ) + application-level null-safe guard (phòng thủ tầng logic) là chiến lược phòng thủ sâu (Defense in Depth) tối ưu nhất.


---

## 203. State Machine Pattern Trong Enum Cho Entity Lifecycle Management — Encode Transition Rules Trực Tiếp Vào Domain Model

### Giải thích ngắn gọn

1. **State Machine Pattern:**
   - Là pattern quản lý vòng đời (lifecycle) của entity bằng cách định nghĩa rõ ràng: (1) tập hợp các trạng thái hợp lệ, (2) tập hợp các transition hợp lệ giữa các trạng thái, (3) điều kiện để transition xảy ra
   - Thay vì scatter logic if-else khắp service layer, tập trung transition rules vào một nơi duy nhất (Single Source of Truth)
   - Trong Java, có thể encode trực tiếp vào Enum bằng cách thêm method `canTransitionTo()` với `EnumSet` chứa allowed targets

2. **Tại sao encode vào Enum thay vì Service:**
   - **Cohesion**: Transition rules thuộc về domain model, không phải business logic layer
   - **Reusability**: Mọi service method (`publish`, `hide`, `delete`, `update`) đều dùng chung logic, không duplicate
   - **Testability**: Test transition rules độc lập, không cần mock service dependencies
   - **Discoverability**: Developer mới chỉ cần đọc enum để hiểu toàn bộ lifecycle

3. **Terminal State:**
   - State không có outgoing transitions (ví dụ: ARCHIVED)
   - `canTransitionTo()` luôn trả về `false` cho terminal state
   - Quan trọng cho data integrity — ngăn "zombie" entities bị reactivate

### Ví dụ trong project này

Trong [`CourseStatus.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japanlearning/backend/enums/CourseStatus.java):
```java
public enum CourseStatus {
    DRAFT, PUBLISHED, HIDDEN, ARCHIVED;

    public boolean canTransitionTo(CourseStatus target) {
        return switch (this) {
            case DRAFT -> target == PUBLISHED || target == ARCHIVED;
            case PUBLISHED -> target == HIDDEN || target == ARCHIVED;
            case HIDDEN -> target == PUBLISHED || target == ARCHIVED;
            case ARCHIVED -> false; // terminal state
        };
    }
}
```

Sử dụng trong [`CourseAdminServiceImpl.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japanlearning/backend/service/impl/CourseAdminServiceImpl.java):
```java
if (!course.getStatus().canTransitionTo(newStatus)) {
    throw new AppException(ErrorCode.COURSE_INVALID_STATUS_TRANSITION);
}
```

### Câu hỏi phỏng vấn liên quan

Khi nào nên dùng State Machine Pattern và encode transition logic vào Enum thay vì dùng if-else trong service layer?

### Câu trả lời ngắn gọn

Dùng State Machine Pattern khi entity có lifecycle phức tạp với nhiều trạng thái và transition rules cần enforce nhất quán. Encode vào Enum khi transition rules là **static** (không phụ thuộc runtime context) — ví dụ: DRAFT chỉ có thể sang PUBLISHED hoặc ARCHIVED, bất kể ai thực hiện. Nếu transition rules phụ thuộc vào **dynamic context** (quyền user, thời gian, external service), thì nên dùng State Machine framework (Spring State Machine) hoặc Strategy Pattern. Với project nhỏ-trung bình, enum method là đủ — tránh over-engineering.

---

## 204. Pricing Invariants Và CourseType-Price Consistency — Validation Business Rules Cross-Field Trên Server-Side

### Giải thích ngắn gọn

1. **Pricing Invariants:**
   - Là tập hợp các business rules ràng buộc mối quan hệ giữa `courseType` và các field giá (`originalPrice`, `salePrice`)
   - Invariant = điều kiện phải **luôn đúng** tại mọi thời điểm trong vòng đời entity, không chỉ lúc create
   - Ví dụ: FREE course → `originalPrice = 0 AND salePrice = 0`, PAID course → `originalPrice > 0 AND (salePrice == null OR salePrice <= originalPrice)`

2. **Cross-Field Validation:**
   - Validation phụ thuộc vào giá trị của **nhiều field cùng lúc** — không thể dùng đơn lẻ `@NotNull`, `@Min` trên từng field
   - Phải implement ở service layer hoặc custom validator, không phải DTO annotation layer
   - Pattern: nhóm các field liên quan vào một validation method riêng (`validatePricing()`)

3. **Normalization vs Rejection:**
   - **Normalization**: Auto-fix data khi intent rõ ràng (FREE course + any price → set to 0). Giảm friction cho client.
   - **Rejection**: Throw error khi intent không rõ (PAID course + no price → error). Yêu cầu explicit input.
   - Rule of thumb: Normalize khi chỉ có MỘT cách fix hợp lệ, reject khi có NHIỀU cách fix

### Ví dụ trong project này

Trong [`CourseAdminServiceImpl.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japanlearning/backend/service/impl/CourseAdminServiceImpl.java):
```java
private void validatePricing(CourseCreateReq req) {
    if (req.getCourseType() == CourseType.FREE) {
        // Normalization: auto-fix prices to 0
        req.setOriginalPrice(BigDecimal.ZERO);
        req.setSalePrice(BigDecimal.ZERO);
    } else { // PAID
        // Rejection: require explicit valid prices
        if (req.getOriginalPrice() == null || req.getOriginalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.COURSE_PRICE_REQUIRED);
        }
        if (req.getSalePrice() != null && req.getSalePrice().compareTo(req.getOriginalPrice()) > 0) {
            throw new AppException(ErrorCode.COURSE_SALE_PRICE_INVALID);
        }
    }
}
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào để validate business rules liên quan đến nhiều fields cùng lúc trong Spring Boot? Tại sao không dùng Bean Validation annotation?

### Câu trả lời ngắn gọn

Bean Validation annotations (`@NotNull`, `@Min`, `@Max`) chỉ validate **từng field độc lập**. Cho cross-field validation (ví dụ: `salePrice <= originalPrice`), có 3 cách: (1) **Custom class-level validator** với `@Constraint` — clean nhưng tách logic ra khỏi service, (2) **Service layer validation method** — đơn giản, đọc dễ hiểu, nằm cùng business logic, (3) **Spring Validator interface** — flexible nhưng verbose. Trong thực tế, service layer method phù hợp nhất cho các invariant đơn giản vì nó nằm cùng chỗ với business logic, dễ debug, và dễ test bằng unit test.

---

## 205. Update Bypass Prevention — Chống Escalate Trạng Thái Qua Generic Update Endpoint

### Giải thích ngắn gọn

1. **Vấn đề Update Bypass:**
   - Khi entity có dedicated state-change endpoints (ví dụ: `POST /publish`, `POST /hide`), nhưng generic update endpoint (`PUT /update`) cũng cho phép thay đổi field `status`
   - Attacker/client có thể bypass validation của dedicated endpoints bằng cách gửi `status` field qua generic update
   - Đây là dạng **privilege escalation** — user thực hiện action có validation cao (publish) qua channel có validation thấp (update)

2. **Nguyên tắc phòng chống:**
   - **"Mọi đường dẫn đến cùng một state phải đi qua cùng validation gates"** (All paths to the same state must go through the same validation gates)
   - Generic update endpoint phải detect status field changes và route chúng qua cùng validation pipeline
   - Hoặc: loại bỏ `status` khỏi update DTO (nhưng giảm flexibility)

3. **Implementation patterns:**
   - **Guard clause**: Trong `updateCourse()`, kiểm tra `if (req.getStatus() != null && req.getStatus() != currentStatus)` → gọi validation
   - **DTO separation**: Tách `CourseUpdateReq` (không có status) và `CourseStatusChangeReq` (chỉ có status) — strict nhưng nhiều DTO
   - **Middleware/AOP**: Intercept tất cả update requests và validate status changes — clean nhưng implicit

### Ví dụ trong project này

Trong [`CourseAdminServiceImpl.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japanlearning/backend/service/impl/CourseAdminServiceImpl.java):
```java
@Override
public CourseAdminRes updateCourse(Long courseId, CourseUpdateReq req) {
    Course course = getCourseWithOwnershipCheck(courseId);

    // Guard: detect status change via update endpoint
    if (req.getStatus() != null && req.getStatus() != course.getStatus()) {
        if (!course.getStatus().canTransitionTo(req.getStatus())) {
            throw new AppException(ErrorCode.COURSE_INVALID_STATUS_TRANSITION);
        }
        if (req.getStatus() == CourseStatus.PUBLISHED) {
            // Route through SAME validation as publishCourse()
            validatePublicationRequirements(course);
            validatePublicationCurriculum(course);
        }
    }
    // ... proceed with normal update
}
```

### Câu hỏi phỏng vấn liên quan

Trong REST API, khi entity có cả generic update endpoint và dedicated state-change endpoints, làm thế nào để ngăn user bypass validation bằng cách thay đổi status qua generic update?

### Câu trả lời ngắn gọn

Có 3 cách tiếp cận: (1) **Guard clause trong update handler**: detect status change, route qua cùng validation pipeline — linh hoạt, dễ implement, nhưng phải nhớ thêm guard mỗi khi có endpoint mới. (2) **Loại bỏ status khỏi update DTO**: bắt buộc dùng dedicated endpoints — strict nhất, zero bypass risk, nhưng giảm API flexibility. (3) **Interceptor/AOP**: tự động detect và validate status changes — DRY nhưng implicit, khó debug. Trong project thực tế, cách (1) phù hợp nhất cho team nhỏ vì explicit và dễ hiểu. Cách (2) phù hợp cho API public vì loại bỏ attack surface hoàn toàn.

---

## 206. Curriculum Publication Readiness Validation — Kiểm Tra Nội Dung Đã Publish Trước Khi Publish Khóa Học

### Giải thích ngắn gọn

1. **Publication Readiness:**
   - Khóa học chỉ được publish khi có nội dung đã sẵn sàng cho student xem
   - Yêu cầu tối thiểu: ≥1 published section VÀ ≥1 published lesson trong published section
   - Ngăn khóa học "rỗng" (có structure nhưng không có content) được publish

2. **Two-Level Validation:**
   - **Level 1 — Section**: Kiểm tra có ít nhất 1 section ở trạng thái PUBLISHED
   - **Level 2 — Lesson in Section**: Kiểm tra có ít nhất 1 lesson PUBLISHED nằm trong section PUBLISHED
   - Chỉ check Level 1 là không đủ — section có thể PUBLISHED nhưng tất cả lessons trong đó đều DRAFT

3. **Performance Consideration:**
   - Dùng custom `@Query` với `EXISTS` subquery thay vì load toàn bộ sections + lessons rồi filter trong Java
   - `SELECT CASE WHEN EXISTS(SELECT 1 FROM Lesson l JOIN l.section s WHERE s.course.id = :courseId AND s.status = 'PUBLISHED' AND l.status = 'PUBLISHED') THEN true ELSE false END`
   - O(1) database query thay vì O(N*M) entity loading

### Ví dụ trong project này

Trong [`LessonRepository.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japanlearning/backend/repository/LessonRepository.java):
```java
@Query("SELECT CASE WHEN EXISTS(" +
       "SELECT 1 FROM Lesson l JOIN l.section s " +
       "WHERE s.course.id = :courseId AND s.status = 'PUBLISHED' AND l.status = 'PUBLISHED'" +
       ") THEN true ELSE false END")
boolean hasPublishedLessonsInPublishedSections(@Param("courseId") Long courseId);
```

Trong [`CourseAdminServiceImpl.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japanlearning/backend/service/impl/CourseAdminServiceImpl.java):
```java
private void validatePublicationCurriculum(Course course) {
    if (!sectionRepository.existsByCourseIdAndStatus(course.getId(), SectionStatus.PUBLISHED)) {
        throw new AppException(ErrorCode.COURSE_CANNOT_PUBLISH_NO_PUBLISHED_LESSONS);
    }
    if (!lessonRepository.hasPublishedLessonsInPublishedSections(course.getId())) {
        throw new AppException(ErrorCode.COURSE_CANNOT_PUBLISH_NO_PUBLISHED_LESSONS);
    }
}
```

### Câu hỏi phỏng vấn liên quan

Khi cần kiểm tra điều kiện phức tạp trên dữ liệu liên quan (parent-child relationships) trước khi cho phép một action, bạn chọn cách nào: load entities rồi filter trong Java, hay dùng custom query?

### Câu trả lời ngắn gọn

Luôn ưu tiên custom query với `EXISTS` subquery khi chỉ cần biết **có hay không** (boolean check). Lý do: (1) **Performance**: database xử lý filtering nhanh hơn Java, đặc biệt khi data lớn — `EXISTS` dừng ngay khi tìm thấy 1 row, không scan toàn bộ. (2) **Memory**: không load hàng trăm entity vào heap chỉ để kiểm tra điều kiện. (3) **Atomicity**: query chạy trong 1 transaction, tránh race condition giữa load và check. Tuy nhiên, nếu sau khi check bạn cần **sử dụng** dữ liệu đó (ví dụ: hiển thị danh sách), thì load entities có ý nghĩa hơn vì tránh query 2 lần.


---

## 207. Idempotency Key Pattern — Đảm Bảo Safe Retry Cho Order/Payment API

### Giải thích ngắn gọn

1. **Vấn đề cần giải quyết:**
   - HTTP POST là non-idempotent — gọi 2 lần tạo 2 resources khác nhau
   - Network timeout, double-click, client retry đều gửi lại cùng request
   - Không có cách phân biệt giữa "retry cùng intent" và "request mới thực sự"
   - Đặc biệt nguy hiểm cho payment/order: duplicate charge, duplicate order

2. **Giải pháp — Idempotency Key:**
   - Client generate unique key (UUID) cho mỗi "intent" (ý định mua hàng)
   - Key được gửi qua HTTP header (`Idempotency-Key`) hoặc request body
   - Server lưu key cùng với resource được tạo, scoped theo user: `(user_id, idempotency_key)`
   - Khi nhận request với key đã tồn tại:
     - Same resource context → return existing (idempotent retry)
     - Different resource context → reject (key reuse conflict, 409)

3. **Key Scoping:**
   - **Per-user scope** (recommended): Unique constraint `(user_id, key)` — 2 users khác nhau có thể dùng cùng key value
   - **Global scope**: Unique constraint `(key)` — simple hơn nhưng gây false conflict giữa unrelated users
   - **Per-endpoint scope**: Key + endpoint path — phức tạp, chỉ cần khi 1 key phải idempotent trên nhiều endpoints

4. **Key Lifecycle:**
   - Key hợp lệ vĩnh viễn (simple) hoặc có TTL (phức tạp hơn, cần cleanup job)
   - NULL key = opt-out idempotency — request luôn tạo resource mới (backward compatibility)
   - Key chỉ áp dụng cho write operations (POST, PUT), không cho reads (GET)

### Ví dụ trong project này

Trong [`OrderServiceImpl.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_order/service/impl/OrderServiceImpl.java):
```java
// Check idempotency key TRƯỚC khi tạo order mới
if (StringUtils.hasText(idempotencyKey)) {
    Optional<Order> existing = orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
    if (existing.isPresent()) {
        Order existingOrder = existing.get();
        if (existingOrder.getCourseId().equals(courseId)) {
            return mapToOrderRes(existingOrder); // Idempotent retry — same intent
        }
        throw new AppException(ErrorCode.ORDER_IDEMPOTENCY_KEY_CONFLICT); // Key reuse — different intent
    }
}
```

Trong [`OrderController.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_order/controller/OrderController.java):
```java
@PostMapping
public ApiResponse<OrderRes> createOrder(
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @RequestBody OrderCreateReq req) {
    if (StringUtils.hasText(idempotencyKey) && !StringUtils.hasText(req.getIdempotencyKey())) {
        req.setIdempotencyKey(idempotencyKey); // Header takes precedence over empty body
    }
    // ...
}
```

### Câu hỏi phỏng vấn liên quan

Bạn thiết kế idempotency cho API tạo đơn hàng như thế nào? Phân biệt giữa retry hợp lệ và reuse key cho mục đích khác?

### Câu trả lời ngắn gọn

Dùng client-provided idempotency key scoped theo `(user_id, key)` với unique constraint trên database. Khi nhận key đã tồn tại: (1) kiểm tra business context — nếu same courseId → return existing order (idempotent retry), nếu different courseId → reject 409 Conflict (key reuse). Key là optional (NULL = no idempotency) để backward compatible. Database unique constraint là safety net, application logic là primary check. Pattern này theo đúng Stripe Payment Intents API convention — industry standard cho payment idempotency.

---

## 208. Pessimistic Locking (SELECT FOR UPDATE) — Serialize Concurrent Writes Per User

### Giải thích ngắn gọn

1. **Vấn đề — TOCTOU Race Condition:**
   - Check-then-act pattern: `if (!exists(X)) { create(X) }` — 2 concurrent transactions cùng pass check vì chưa thấy write của nhau
   - Xảy ra ở `READ_COMMITTED` isolation level (default của PostgreSQL/MySQL) — transaction chỉ thấy data đã committed
   - Nâng lên `SERIALIZABLE` giải quyết nhưng chi phí cao (toàn bộ transaction bị serialize, performance giảm mạnh)

2. **Giải pháp — Pessimistic Lock:**
   - `SELECT ... FOR UPDATE` lock row cho đến khi transaction commit/rollback
   - Concurrent transactions cùng lock row → chỉ 1 thread giành được, còn lại phải chờ (blocking)
   - Check-then-act trở thành atomic: thread đầu tiên check + create + commit → release lock → thread tiếp theo check → thấy data mới → rejected

3. **Lock Target Selection:**
   - Lock trên **entity target** (order row) — không thể khi entity chưa tồn tại
   - Lock trên **parent entity** (user row) — khả thi vì user luôn tồn tại trước khi tạo order
   - Lock trên **auxiliary table** (lock table riêng) — flexible nhưng thêm complexity
   - Quyết định: lock user row vì tất cả orders thuộc về user, và business rule scoped theo user

4. **Trade-offs:**
   - **Ưu điểm**: Simple, correct, database-native, no external dependencies
   - **Nhược điểm**: Blocking (threads chờ lock), potential deadlock (nếu lock nhiều row), không scale horizontally (lock chỉ work trong 1 database)
   - **Alternative — Optimistic Locking**: `@Version` field + retry on `OptimisticLockException` — non-blocking nhưng phức tạp hơn, cần retry logic ở caller
   - **Khi nào chọn Pessimistic**: Write contention cao (cùng user tạo order liên tục), business logic phức tạp (nhiều step sau lock), retry cost cao (payment processing)

### Ví dụ trong project này

Trong [`UserRepository.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_user/repository/UserRepository.java):
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT u FROM User u WHERE u.id = :id")
Optional<User> findByIdForUpdate(@Param("id") Long id);
```

Trong [`OrderServiceImpl.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_order/service/impl/OrderServiceImpl.java):
```java
@Override
@Transactional
public OrderRes createOrder(OrderCreateReq req) {
    Long userId = getCurrentUserId();

    // STEP 1: Lock user row — serialize all order creation for this user
    userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

    // STEP 2: Now safe to check + act without race condition
    if (orderRepository.existsByUserIdAndCourseIdAndStatus(userId, courseId, OrderStatus.PENDING)) {
        throw new AppException(ErrorCode.ORDER_PENDING_EXISTS);
    }
    // ... create order
}
```

### Câu hỏi phỏng vấn liên quan

Khi 2 concurrent requests từ cùng user tạo order cho cùng course, bạn giải quyết race condition như thế nào? So sánh pessimistic và optimistic locking trong trường hợp này.

### Câu trả lời ngắn gọn

Chọn **pessimistic lock** (`SELECT FOR UPDATE`) trên user row: (1) Lock target là user row vì order chưa tồn tại tại thời điểm check. (2) Toàn bộ check-then-act sequence chạy dưới lock → atomic. (3) So với optimistic locking (`@Version` + retry): pessimistic đơn giản hơn cho use case này vì **không cần retry logic**, write contention thấp (user tạo order không thường xuyên), và business logic sau lock phức tạp (pricing, idempotency check). Trade-off: pessimistic blocking threads, nhưng hold time ngắn (1 order creation ~50ms) nên acceptable. Nếu scale ra multi-database, sẽ cần chuyển sang distributed lock (Redis SETNX) hoặc optimistic locking.

---

## 209. DataIntegrityViolationException Làm Safety Net (Defense in Depth) — Bảo Vệ Tầng Database Khi Application Logic Bị Bypass

### Giải thích ngắn gọn

1. **Defense in Depth Principle:**
   - Không rely vào chỉ 1 tầng validation — nếu application-level check bị bypass (bug, race condition, configuration), database constraint là safety net cuối cùng
   - Application logic là primary protection (readable, testable, graceful errors)
   - Database constraint là secondary protection (absolute enforcement, less graceful)

2. **`save()` vs `saveAndFlush()` — Timing Matters:**
   - `save()`: JPA defer INSERT đến transaction commit — `DataIntegrityViolationException` throw ở commit time, **ngoài** method scope → khó catch
   - `saveAndFlush()`: Force INSERT ngay lập tức → exception throw tại dòng gọi → có thể catch và handle gracefully (re-check idempotency, return existing order)
   - Rule of thumb: dùng `saveAndFlush()` khi cần catch constraint violation trong cùng method

3. **Graceful Degradation:**
   - Catch `DataIntegrityViolationException` → không throw 500 Internal Server Error
   - Thay vào đó: re-query database, xác nhận duplicate, trả response phù hợp (existing order hoặc conflict error)
   - User experience: transparent — client không biết có race condition xảy ra

4. **Khi nào áp dụng:**
   - Unique constraints có thể bị violate bởi concurrent requests
   - Business rules enforce bằng application logic có window of vulnerability (TOCTOU)
   - Payment/order systems nơi duplicate creation có hậu quả tài chính

### Ví dụ trong project này

Trong [`OrderServiceImpl.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_order/service/impl/OrderServiceImpl.java):
```java
try {
    order = orderRepository.saveAndFlush(order); // Force immediate INSERT
} catch (DataIntegrityViolationException ex) {
    // Safety net: unique constraint (user_id, idempotency_key) violated
    // Re-check idempotency — maybe another thread created the same order
    if (StringUtils.hasText(idempotencyKey)) {
        Optional<Order> existing = orderRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existing.isPresent()) {
            Order existingOrder = existing.get();
            if (existingOrder.getCourseId().equals(courseId)) {
                return mapToOrderRes(existingOrder); // Idempotent — return existing
            }
            throw new AppException(ErrorCode.ORDER_IDEMPOTENCY_KEY_CONFLICT);
        }
    }
    throw ex; // Unknown constraint violation — re-throw
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao bạn dùng `saveAndFlush()` thay vì `save()` khi cần catch `DataIntegrityViolationException`? Vai trò của database constraint khi đã có application-level validation?

### Câu trả lời ngắn gọn

`save()` defer INSERT đến commit time — exception throw ngoài method scope, không thể catch. `saveAndFlush()` force INSERT ngay → catch tại chỗ, handle gracefully (re-query, return existing). Database constraint là **safety net**, không phải primary logic: (1) Application validation xử lý 99% cases với readable errors, (2) Constraint bắt 1% race conditions mà lock không cover (ví dụ: lock timeout, distributed system). Nguyên tắc Defense in Depth: primary protection (lock) + secondary protection (constraint) + graceful degradation (catch + re-check). Cost thêm 1 query khi hit constraint, nhưng xảy ra cực hiếm — worth it cho data integrity.


---

## 210. Stateless JWT Token Revocation — Token Versioning & Bounded-Time Invalidation Strategy

### Giải thích ngắn gọn

**Bài toán**: JWT stateless có tradeoff cố hữu — token hợp lệ cho đến khi hết hạn, không có cơ chế "pull back" token đã phát. Nhưng các sự kiện như lock tài khoản, xóa tài khoản, đổi mật khẩu, thay đổi role cần có hiệu lực ngay hoặc trong thời gian ngắn.

**Hai approach chính**:

1. **Fully Stateful** (blacklist): Lưu JTI (JWT ID) của revoked tokens trong Redis/DB, check trên mỗi request
   - Ưu: Immediate revocation với độ chính xác tuyệt đối
   - Nhược: **Database/Redis query trên mỗi HTTP request** — triệt tiêu hoàn toàn lợi ích stateless của JWT
   - Khi nào dùng: Yêu cầu immediate với token TTL dài (> 1h), security-critical (banking, medical)

2. **Token Versioning** (approach trong project này): Lưu `token_version` trong DB, nhúng vào token claim, check với multi-tier cache
   - Ưu: Bounded-time revocation (≤ local cache TTL), không query DB on hot path
   - Nhược: Không immediate trong mọi trường hợp — window = local cache TTL (60s mặc định)
   - Khi nào dùng: Acceptable bounded-time, performance quan trọng, Redis optional

**Revocation kép trong project này**:
- **Status-based** (account lock, delete): Check `user.status != ACTIVE` → immediate trong L3 fallback, bounded-time từ L1 cache TTL
- **Version-based** (password change, role change): `token_version` trong token < DB version → reject với bounded-time

### Giải thích chi tiết

**Token Version Claim Flow**:
```
Login:
  User.tokenVersion = 1
  JWT claims: {id: 1, tokenVersion: 1, roles: [...], exp: ...}

Password Change:
  DB: UPDATE users SET token_version = 2 WHERE id = 1
  Evict L1 local cache + L2 Redis for userId=1

Next HTTP Request with old token (tokenVersion=1):
  L1 miss (evicted) → L2 miss (evicted) → L3 DB query
  DB state: tokenVersion=2, status=ACTIVE
  claimedVersion (1) < state.getTokenVersion() (2) → REJECTED
  Response: 401 ACCESS_TOKEN_REVOKED
```

**3-Tier Cache Hierarchy**:
```
Request → JwtAuthenticationFilter → isTokenActive(userId, tokenVersion, email)
                                              │
                              ┌───────────────▼──────────────────┐
                              │  L1: ConcurrentHashMap<Long,     │
                              │  CachedSecurityState>            │
                              │  TTL: 60s, per-node, in-memory   │
                              └──────────────┬───────────────────┘
                                      miss   │
                              ┌──────────────▼───────────────────┐
                              │  L2: Redis (optional)            │
                              │  Keys: auth:user:version:{id}    │
                              │        auth:user:status:{id}     │
                              │  Failover: graceful warn + L3    │
                              └──────────────┬───────────────────┘
                                      miss   │
                              ┌──────────────▼───────────────────┐
                              │  L3: userRepository.findById()   │
                              │  Source of truth                  │
                              │  Warms L1 + L2 after query       │
                              └──────────────────────────────────┘
```

**Invalidation path khi state thay đổi**:
```
revokeUserSessions(userId) {
  refreshTokenRepository.revokeAllByUserId(userId)  // Revoke refresh tokens
  userRepository.incrementTokenVersion(userId)       // Bump version in DB
  tokenRevocationService.revokeUserTokens(userId)    // Evict L1 + L2
}
```

### Ví dụ trong project này

[`TokenRevocationServiceImpl.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/security/TokenRevocationServiceImpl.java):
```java
@Override
public boolean isTokenActive(Long userId, Integer tokenVersion, String email) {
    CachedSecurityState state = getSecurityState(userId); // L1 → L2 → L3
    if (state == null) return false;
    if (state.getStatus() != UserStatus.ACTIVE) return false;         // Status check
    if (claimedVersion < state.getTokenVersion()) return false;        // Version check
    return true;
}
```

[`JwtAuthenticationFilter.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/security/JwtAuthenticationFilter.java):
```java
if (!tokenRevocationService.isTokenActive(userId, tokenVersion, userEmail)) {
    writeErrorResponse(response, ErrorCode.ACCESS_TOKEN_REVOKED); // 401
    return;
}
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào bạn revoke JWT access token ngay lập tức mà không phải query database trên mỗi HTTP request? So sánh token blacklist vs token versioning.

### Câu trả lời ngắn gọn

**Chọn token versioning với multi-tier cache**: (1) Nhúng `tokenVersion` vào JWT claim. (2) Lưu `token_version` trong DB — tăng khi cần revoke tất cả tokens. (3) `JwtAuthenticationFilter` check version via 3-tier cache (L1 local in-memory 60s TTL → L2 Redis → L3 DB fallback chỉ khi miss). **Ưu điểm vs blacklist**: không query DB/Redis trên hot path — L1 phục vụ 99%+ requests. **Trade-off**: bounded-time revocation (≤ 60s window), không immediate. **So sánh với blacklist**: blacklist cho immediate revocation nhưng cần external store lookup mỗi request — triệt tiêu lợi ích stateless. Cho security boundary 60s là acceptable; nếu cần strict (<1s), giảm TTL hoặc dùng Redis với TTL ngắn hơn.

---

## 211. `@Modifying(clearAutomatically, flushAutomatically)` — Tránh JPA Dirty-Checking Overwrite JPQL Bulk Update

### Giải thích ngắn gọn

**Vấn đề**: Khi dùng `@Modifying` JPQL `UPDATE` trong cùng `@Transactional` transaction đang quản lý entity đó, Hibernate có thể **overwrite kết quả UPDATE bằng dirty-checking flush** của entity cũ trong persistence context.

**Cơ chế**:
1. Transaction T bắt đầu, `EntityManager` load entity `User{id=1, tokenVersion=1}` vào L1 persistence context (first-level cache)
2. `@Modifying` JPQL UPDATE chạy: DB `SET token_version = 2` ✅
3. Entity trong memory vẫn là `{tokenVersion=1}` — persistence context KHÔNG bị clear (vì thiếu `clearAutomatically`)
4. Cuối transaction commit: Hibernate dirty-checking thấy entity `user` có thay đổi khác (ví dụ: `status = DELETED`) → flush toàn bộ dirty entity
5. Hibernate generate `UPDATE users SET status='DELETED', token_version=1 WHERE id=1` ❌
6. `token_version` bị overwrite từ 2 về 1!

**Fix**:
```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("UPDATE User u SET u.tokenVersion = COALESCE(u.tokenVersion, 1) + 1 WHERE u.id = :id")
int incrementTokenVersion(@Param("id") Long id);
```

- `flushAutomatically = true`: Flush pending entity changes **trước** khi execute JPQL UPDATE → tránh pending changes interfere với JPQL
- `clearAutomatically = true`: Clear persistence context **sau** khi execute JPQL UPDATE → Hibernate không còn stale entity trong memory để flush đè lên

### Giải thích chi tiết

**Tại sao `clearAutomatically` cần thiết**:
- `@Modifying` UPDATE là SQL-level bypass — không thông qua JPA entity lifecycle
- JPA persistence context (L1 cache) không biết gì về sự thay đổi từ JPQL
- Nếu entity được tiếp tục modify sau JPQL, Hibernate flush sẽ bao gồm cả field đã được JPQL update → overwrite
- `clearAutomatically = true`: sau JPQL UPDATE, persistence context bị clear → entity phải được reload từ DB lần sau → consistent với DB state

**Khi nào thực sự cần**:
- Entity liên quan đang được quản lý bởi cùng EntityManager trong cùng transaction
- Có thể xảy ra dirty-checking flush sau JPQL UPDATE (transaction chưa commit)
- Entity có các field khác bị modify cùng transaction → flush sẽ bao gồm mọi dirty field

**Cost của `clearAutomatically = true`**:
- Tất cả entities trong persistence context bị evict → phải reload từ DB nếu cần tiếp tục sử dụng
- Thường chỉ cần thiết khi entity liên quan đang "in-flight" trong transaction

### Ví dụ trong project này

[`UserRepository.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_user/repository/UserRepository.java):
```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("UPDATE User u SET u.tokenVersion = COALESCE(u.tokenVersion, 1) + 1 WHERE u.id = :id")
int incrementTokenVersion(@Param("id") Long id);
```

Được gọi trong `AuthServiceImpl.revokeUserSessions()` — sau `userRepository.save(user)` trong cùng transaction của `deleteUser()`, `updateUserRoles()`, và `changePassword()`.

### Câu hỏi phỏng vấn liên quan

Tại sao `@Modifying` JPQL UPDATE đôi khi không có hiệu lực mặc dù SQL đã chạy thành công? Khi nào cần dùng `clearAutomatically = true`?

### Câu trả lời ngắn gọn

`@Modifying` JPQL là SQL-level operation, bypass JPA entity lifecycle — **JPA persistence context (L1 cache) không được cập nhật**. Nếu entity liên quan đang được quản lý trong cùng transaction, Hibernate dirty-checking flush khi commit sẽ generate UPDATE từ entity state trong memory (tokenVersion=1) và **overwrite kết quả JPQL** (tokenVersion=2). Dùng `clearAutomatically = true` để clear persistence context sau JPQL → entity phải reload từ DB nếu tiếp tục dùng → consistent. Dùng `flushAutomatically = true` để flush pending entity changes trước JPQL → tránh conflicts. Áp dụng bất cứ khi nào có `@Modifying` trong transaction đang quản lý entity liên quan.

---

## 212. MockMvc SecurityContext Isolation — TestSecurityContextHolder trong Security Integration Tests

### Giải thích ngắn gọn

**Vấn đề**: Trong `@SpringBootTest` với MockMvc, khi test method set authentication vào `SecurityContextHolder` để gọi service trực tiếp (không qua HTTP), context đó có thể "leak" sang MockMvc HTTP request tiếp theo — `JwtAuthenticationFilter` thấy authentication đã set → skip token validation → filter không chạy revocation check.

**Tại sao xảy ra**: Spring Security `SecurityContextHolder` mặc định dùng `MODE_THREADLOCAL` — context gắn với thread. MockMvc chạy trong cùng thread với test method. Nếu set authentication trong test thread và không clear trước MockMvc call, authentication vẫn còn trong context → `SecurityContextHolder.getContext().getAuthentication() != null` → JwtAuthenticationFilter skip toàn bộ block!

**Fix**:
```java
@BeforeEach
void setUp() {
    SecurityContextHolder.clearContext();
    TestSecurityContextHolder.clearContext(); // Clear cả Spring Test security context
    // ... setup
}

// Sau mỗi direct service call trong test:
adminUserService.deleteUser(student.getId());
SecurityContextHolder.clearContext();         // Cho mockMvc trong cùng thread
TestSecurityContextHolder.clearContext();     // Cho Spring Test context integration
```

### Giải thích chi tiết

**JwtAuthenticationFilter guard clause**:
```java
if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    // Chỉ chạy revocation check nếu authentication CHƯA được set!
    if (jwtUtil.isAccessTokenValid(jwt, userEmail)) {
        // ... check isTokenActive, set authentication
    }
}
```

Nếu `getAuthentication() != null` (leaked từ test setup), filter SKIP toàn bộ block → không set authentication từ JWT → Spring Security xử lý request với authentication cũ → kết quả khó đoán (thường là 403 nếu authentication đó không có đúng authorities cho endpoint).

**`SecurityContextHolder` vs `TestSecurityContextHolder`**:
- `SecurityContextHolder`: Đây là actual context holder được framework dùng trong runtime
- `TestSecurityContextHolder`: Spring Test variant — dùng trong `@SecurityContext` test annotation và một số Spring Test integrations
- Trong integration test với `@SpringBootTest` + MockMvc, cần clear cả 2 để đảm bảo isolation

**Khi nào cần thiết**:
- Test dùng `SecurityContextHolder.getContext().setAuthentication(...)` để mock authentication cho direct service calls
- Sau đó cùng test dùng MockMvc với JWT token để test HTTP layer
- `@DirtiesContext` reset Spring context giữa tests nhưng **không reset thread-local SecurityContextHolder**

### Ví dụ trong project này

[`AccessTokenRevocationSecurityIT.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/test/java/com/japaneselearning/common/security/AccessTokenRevocationSecurityIT.java):
```java
@BeforeEach
void setUp() {
    clearSecurityContext();  // Always clear before each test
    userRepository.deleteAll();
    // ... setup roles
}

private void clearSecurityContext() {
    SecurityContextHolder.clearContext();
    TestSecurityContextHolder.clearContext();
}

// In test method:
SecurityContextHolder.getContext().setAuthentication(
    new UsernamePasswordAuthenticationToken(admin.getEmail(), "Admin123!")
);
adminUserService.deleteUser(student.getId());
clearSecurityContext(); // Clear before MockMvc call!

mockMvc.perform(get("/api/users/me")
    .header("Authorization", "Bearer " + studentAccessToken))
    .andExpect(status().isUnauthorized()); // Now JwtFilter actually runs
```

### Câu hỏi phỏng vấn liên quan

Trong Spring Boot integration test với MockMvc, tại sao JwtAuthenticationFilter đôi khi không chạy mặc dù request có Bearer token? Làm thế nào để fix security context isolation giữa các bước trong cùng test method?

### Câu trả lời ngắn gọn

**Nguyên nhân**: `SecurityContextHolder.MODE_THREADLOCAL` — context gắn với thread. MockMvc chạy trong cùng thread với test. Nếu test set authentication (để gọi service trực tiếp) mà không clear trước MockMvc call, `JwtAuthenticationFilter` thấy `getAuthentication() != null` → skip block → không validate JWT, không check revocation. **Fix**: `clearSecurityContext()` gọi cả `SecurityContextHolder.clearContext()` và `TestSecurityContextHolder.clearContext()` sau mỗi direct service call và trong `@BeforeEach`. Quy tắc: **mỗi HTTP call qua MockMvc phải bắt đầu với clean security context** nếu muốn filter chain chạy đúng.


---

## 213. Multi-Layer API Payload Boundary Protection — Defense-in-Depth cho Request DTOs và Structural Validation

### Giải thích ngắn gọn

Multi-layer payload boundary protection là chiến lược phòng thủ nhiều lớp (defense-in-depth) nhằm ngăn ngừa payload abuse và DoS attacks:
1. **Layer 1 (Transport/Web Server)**: Cấu hình giới hạn kích thước header, form post body, max swallow size ở cấp độ web server (Tomcat/Nginx) nhằm ngắt kết nối sớm đối với request khổng lồ trước khi tiêu tốn memory parse dữ liệu.
2. **Layer 2 (Framework/DTO Validation)**: Dùng Jakarta Bean Validation (`@Size`, `@Pattern`, `@Min`, `@Max`) trên Request DTO để chặn các chuỗi không bounded, danh sách quá dài, hoặc dữ liệu sai format ngay khi Spring deserialize payload.
3. **Layer 3 (Application/Service Logic)**: Kiểm tra tính toàn vẹn cấu trúc nghiệp vụ (structural validation) so sánh giữa dữ liệu gửi lên và entity thực tế trong database (ví dụ: số lượng câu trả lời nộp so với số lượng câu hỏi của đề thi, số option chọn so với loại câu hỏi).

### Giải thích chi tiết

- **Tại sao chỉ dùng Layer 2 (DTO Validation) là không đủ?**
  - DTO validation chỉ kiểm tra tính hợp lệ tĩnh của dữ liệu (syntactic validation). Ví dụ: `@Size(max = 200)` trên list câu trả lời đảm bảo client không gửi 10,000 phần tử.
  - Tuy nhiên, DTO không biết bài kiểm tra cụ thể có bao nhiêu câu hỏi. Nếu đề thi chỉ có 5 câu hỏi mà client gửi 100 câu trả lời (vẫn thỏa mãn `<= 200`), DTO validation vẫn cho qua. Lúc này Layer 3 (Service) phải đối chiếu với số câu hỏi thực tế của quiz (`req.getAnswers().size() > questions.size()`).
  - Tương tự, DTO không thể biết câu hỏi ID 11 là `SINGLE_CHOICE` hay `MULTIPLE_CHOICE`. Chỉ có Service layer mới có thể truy vấn question type và enforce câu `SINGLE_CHOICE` chỉ được chọn tối đa 1 đáp án.

- **Tại sao chỉ dùng Layer 3 (Service Validation) là không đủ?**
  - Nếu không có Layer 2, payload chứa chuỗi 50MB hoặc list 50,000 items sẽ được Jackson parse toàn bộ vào heap memory, gây áp lực GC và DoS trước khi kịp vào đến Service.
  - Hơn nữa, validation lỗi ở DTO layer trả về mã lỗi `VALIDATION_ERROR` (1005) rõ ràng cho từng field, giúp client/frontend hiển thị thông báo lỗi chính xác trên form UI.

- **Cân nhắc dung lượng với nội dung đa ngữ (Japanese Content):**
  - Trong Java, chuỗi `String.length()` đếm số lượng UTF-16 code units (ký tự), không phải số byte.
  - Ký tự tiếng Nhật (Kanji, Hiragana, Katakana) khi encode UTF-8 chiếm 3 bytes mỗi ký tự.
  - Khi đặt `@Size(max = 50000)` cho course description hay `@Size(max = 100000)` cho lesson content HTML, dung lượng byte thực tế có thể lên đến 150KB - 300KB. Giới hạn này vừa đủ rộng cho tài liệu giảng dạy phong phú nhưng vẫn an toàn tuyệt đối trước payload megabyte/gigabyte.

### Ví dụ trong project này

1. **DTO Validation (Layer 2)**:
[`QuizSubmitReq.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_quiz/dto/QuizSubmitReq.java):
```java
@Data
public class QuizSubmitReq {
    @NotNull(message = "attemptId không được để trống")
    private Long attemptId;

    @Valid
    @NotEmpty(message = "Danh sách câu trả lời không được để trống")
    @Size(max = 200, message = "Số lượng câu trả lời không được vượt quá 200")
    private List<QuizSubmitAnswerReq> answers;
}
```

2. **Service Structural Validation (Layer 3)**:
[`QuizLearningServiceImpl.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_quiz/service/QuizLearningServiceImpl.java#L240-L286):
```java
// Bắt buộc số lượng câu trả lời không vượt quá số câu hỏi thực tế trong đề
if (req.getAnswers().size() > questions.size()) {
    throw new AppException(ErrorCode.INVALID_REQUEST,
            "Số lượng câu trả lời vượt quá số lượng câu hỏi của bài kiểm tra");
}

// Bắt buộc cấu trúc đáp án khớp với loại câu hỏi (SINGLE_CHOICE không được chọn > 1)
if ((type == QuestionType.SINGLE_CHOICE || type == QuestionType.TRUE_FALSE)
        && submitted.getEffectiveAnswerIds().size() > 1) {
    throw new AppException(ErrorCode.INVALID_REQUEST,
            "Câu hỏi lựa chọn đơn không được chọn nhiều đáp án: " + question.getId());
}
```

### Câu hỏi phỏng vấn liên quan

Làm thế nào để thiết kế validation toàn diện chống payload abuse / DoS trong hệ thống REST API Spring Boot? Sự khác biệt giữa Bean Validation và Domain/Service Validation là gì?

### Câu trả lời ngắn gọn

Áp dụng chiến lược **Defense-in-Depth** qua 3 tầng:
1. **Server Layer**: Giới hạn max header size (16KB) và max form post/swallow size (2MB) ở Tomcat để cắt kết nối request độc hại từ tầng socket/HTTP parser.
2. **Bean Validation (DTO Layer)**: Dùng Jakarta Validation (`@Size`, `@Pattern`) validate tính hợp lệ cú pháp (syntax) và kích thước thô (raw boundaries) trước khi code nghiệp vụ xử lý, trả về 400 `VALIDATION_ERROR` sớm.
3. **Domain/Service Layer**: Validate tính hợp lệ ngữ nghĩa (semantics) và cấu trúc quan hệ (structure) dựa trên dữ liệu database (ví dụ: quiz submission không vượt quá số lượng câu hỏi, user không submit options ngoài đề thi).

---

## 214. Transport-Layer vs Framework-Layer Request Size Limiting — Cấu Hình Kích Thước Request Trong Spring Boot & Tomcat

### Giải thích ngắn gọn

Request size limiting cần được phân định rõ ràng giữa các loại request và các tầng xử lý:
- **`server.max-http-request-header-size`**: Giới hạn tổng kích thước HTTP headers (request line + headers). Cần cân đối giữa việc chống Header Overflow DoS và đủ chỗ cho JWT Bearer Token dài.
- **`server.tomcat.max-http-form-post-size`**: Giới hạn dung lượng POST body dạng `application/x-www-form-urlencoded` và form data thông thường.
- **`server.tomcat.max-swallow-size`**: Giới hạn tối đa số bytes mà Tomcat sẵn sàng đọc tiếp và loại bỏ ("swallow") khi đã quyết định reject request (để có thể gửi response lỗi về cho client thay vì reset connection đột ngột).
- **`spring.servlet.multipart.max-file-size` / `max-request-size`**: Áp dụng riêng cho upload file dạng `multipart/form-data`. Không nên áp dụng giới hạn lớn này cho toàn bộ JSON body endpoints.

### Giải thích chi tiết

- **Tại sao mặc định Tomcat cần điều chỉnh?**
  - Mặc định Spring Boot Tomcat `max-http-request-header-size` là 8KB. Trong các hệ thống microservice hoặc JWT có nhúng nhiều claims (user roles, permissions, tokenVersion), header Authorization có thể tiệm cận 4KB - 6KB. Khi user gửi thêm cookie hoặc tracing headers (W3C traceparent), header dễ chạm ngưỡng 8KB dẫn đến lỗi HTTP 431 `Request Header Fields Too Large`. Tăng lên `16KB` là mức cân bằng an toàn.
  - Ngược lại, nếu không cấu hình `max-http-form-post-size`, Tomcat cho phép form POST lên đến 2MB, nhưng `max-swallow-size` mặc định là 2MB. Nếu client gửi request 50MB sai format, Tomcat sẽ cố đọc 2MB rồi mới ngắt, gây hao phí tài nguyên.
  - Cần tách biệt rõ: upload file tài liệu học tập/video bài giảng cần `100MB` (qua `spring.servlet.multipart`), nhưng các request JSON API thông thường (tạo khóa học, nộp bài kiểm tra, đặt hàng) chỉ nên dao động từ vài KB đến tối đa vài trăm KB.

### Ví dụ trong project này

[`application.yml`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/resources/application.yml):
```yaml
server:
  port: 8080
  forward-headers-strategy: framework
  max-http-request-header-size: 16KB
  tomcat:
    max-http-form-post-size: 2MB
    max-swallow-size: 2MB

spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

Và xử lý lỗi khi upload quá giới hạn trong [`GlobalExceptionHandler.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/exception/GlobalExceptionHandler.java):
```java
@ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(
        org.springframework.web.multipart.MaxUploadSizeExceededException e) {
    log.warn("Max upload size exceeded: {}", e.getMessage());
    return ResponseEntity
            .status(org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ApiResponse.error(ErrorCode.INVALID_REQUEST.getCode(), "Kích thước tệp tải lên vượt quá giới hạn cho phép"));
}
```

### Câu hỏi phỏng vấn liên quan

Tại sao cần phân tách giới hạn kích thước giữa Multipart Request (upload file) và JSON REST API request? Nếu để chung `max-request-size: 100MB`, rủi ro bảo mật là gì?

### Câu trả lời ngắn gọn

Nếu để chung giới hạn 100MB mà không có Bean Validation `@Size` ở DTO layer:
1. Attacker có thể gửi một JSON body nặng 99MB tới một endpoint thông thường như `/api/auth/login` hoặc `/api/v1/orders`.
2. Jackson JSON Parser sẽ phải allocate một khối lượng memory cực lớn trên Heap để deserialize JSON DOM / Tree Model, gây ra tình trạng High GC Pause, OutOfMemoryError (OOM), dẫn đến Crash toàn bộ JVM instance (DoS).
3. Do đó, cần tách biệt: Multipart file upload được stream trực tiếp ra disk/temp file (giới hạn 100MB), còn JSON REST API được chặn ở cả DTO boundaries (`@Size` hợp lý theo từng trường) và server connection limits.

---

## 215. Validation Ordering & Error Precedence Pitfall — Thứ Tự Kiểm Tra Validation và Ảnh Hưởng Đến Test Regression

### Giải thích ngắn gọn

Validation Ordering Pitfall là lỗi xảy ra khi bổ sung một điều kiện validation mới nhưng đặt sai thứ tự ưu tiên (precedence) so với các điều kiện validation hiện hữu. Khi một request không hợp lệ vi phạm đồng thời nhiều điều kiện, điều kiện nào được kiểm tra trước sẽ quyết định mã lỗi và message trả về. Nếu thay đổi thứ tự này, các unit/integration tests hiện tại (đang assert message hoặc error code cụ thể) sẽ bị fail, dẫn đến test regression.

### Giải thích chi tiết

- **Tình huống thực tế xảy ra trong Task #427**:
  - Test case có sẵn: `submitAttempt_DuplicateQuestionId_ThrowsInvalidRequest`.
  - Trong test này, đề thi có **1 câu hỏi** (ID 11). Payload gửi lên gồm **2 câu trả lời**, cả hai đều có `questionId = 11`.
  - Mục đích ban đầu của test: kiểm tra hệ thống phát hiện câu hỏi bị trùng lặp trong payload (`Dữ liệu nộp bài chứa câu hỏi trùng lặp`).
  - Khi triển khai tính năng mới: chúng ta thêm kiểm tra cấu trúc `req.getAnswers().size() > questions.size()` ("Số lượng câu trả lời vượt quá số lượng câu hỏi").
  - **Lỗi phát sinh**: Nếu đặt `answers.size() > questions.size()` trước vòng lặp duyệt duplicate:
    - `answers.size()` (2) > `questions.size()` (1) → ném ra exception "Số lượng câu trả lời vượt quá số lượng câu hỏi...".
    - Test case mong đợi message "Dữ liệu nộp bài chứa câu hỏi trùng lặp" bị FAIL!
  - **Quy tắc giải quyết**:
    - **Specific checks trước General checks**: Kiểm tra tính hợp lệ của từng item (null check, duplicate check, id membership check) mang tính chi tiết, cụ thể hơn là kiểm tra tổng số lượng.
    - Kiểm tra duplicate xác định rõ hành vi gian lận dữ liệu (payload manipulation), cần được phát hiện và báo lỗi chính xác trước khi so sánh tương quan số lượng.

### Ví dụ trong project này

[`QuizLearningServiceImpl.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/module_quiz/service/QuizLearningServiceImpl.java#L225-L244):
```java
// BƯỚC 1: Validate chi tiết từng phần tử — Kiểm tra trùng lặp và phạm vi thuộc quiz
java.util.Set<Long> seenQuestionIds = new java.util.HashSet<>();
for (QuizSubmitAnswerReq ansReq : req.getAnswers()) {
    if (ansReq.getQuestionId() == null) {
        throw new AppException(ErrorCode.INVALID_REQUEST, "questionId không được để trống");
    }
    // Duplicate check đặt TRƯỚC để bắt đúng lỗi payload trùng lặp
    if (!seenQuestionIds.add(ansReq.getQuestionId())) {
        throw new AppException(ErrorCode.INVALID_REQUEST,
                "Dữ liệu nộp bài chứa câu hỏi trùng lặp: " + ansReq.getQuestionId());
    }
    if (!quizQuestionIds.contains(ansReq.getQuestionId())) {
        throw new AppException(ErrorCode.INVALID_REQUEST,
                "Câu hỏi không thuộc bài kiểm tra này: " + ansReq.getQuestionId());
    }
}

// BƯỚC 2: Validate tổng thể cấu trúc payload (Structural Overflow Protection)
if (req.getAnswers().size() > questions.size()) {
    throw new AppException(ErrorCode.INVALID_REQUEST,
            "Số lượng câu trả lời vượt quá số lượng câu hỏi của bài kiểm tra");
}
```

### Câu hỏi phỏng vấn liên quan

Khi refactor hoặc thêm validation rules mới vào một service đang chạy trong production với hàng trăm test cases, bạn làm thế nào để đảm bảo không làm gãy contract và error precedence của hệ thống?

### Câu trả lời ngắn gọn

1. **Hiểu rõ Error Precedence**: Xác định rõ thứ tự kiểm tra từ cụ thể đến khái quát (Null check → Format/Type check → Duplicate/Membership check → Business/Structural limits).
2. **Chạy Full Regression Suite**: Luôn chạy toàn bộ test suite (`mvn test` và `mvn failsafe:integration-test`) thay vì chỉ chạy test cho code mới viết.
3. **Giữ nguyên API Contract**: Nếu business logic yêu cầu một điều kiện mới, cân nhắc xem nó là breaking change hay validation bổ trợ. Đảm bảo error code và message semantics không thay đổi ngầm định đối với các test cases/client integrations đã có.

---

## 216. Append-Only Audit Trail Pattern & Hibernate `@Immutable` Entity

### Khái niệm là gì?

**Append-Only Audit Trail** là mô hình thiết kế nhật ký kiểm toán trong đó hệ thống chỉ cho phép thao tác ghi mới (`INSERT`) và đọc (`SELECT`), tuyệt đối cấm mọi hành vi cập nhật (`UPDATE`) hoặc xóa bỏ (`DELETE`). Đây là yêu cầu bắt buộc của các khung tuân thủ an toàn thông tin quốc tế như SOC 2 Type II, PCI-DSS, ISO 27001 và GDPR Article 30 (Records of Processing Activities).

Để thực thi tính bất biến này ở tầng ORM mà không dựa dẫm hoàn toàn vào quy ước mã nguồn, Hibernate cung cấp annotation `@org.hibernate.annotations.Immutable`. Khi một entity được gắn `@Immutable`:
1. **Vô hiệu hóa Dirty Checking**: Hibernate không lưu snapshot trạng thái ban đầu của entity vào Persistence Context, giúp tiết kiệm bộ nhớ JVM Heap.
2. **Ngăn chặn SQL UPDATE**: Bất kỳ nỗ lực thay đổi thuộc tính nào của entity và gọi `save()` hay commit transaction đều bị Hibernate bỏ qua, không bao giờ sinh ra câu lệnh `UPDATE` xuống cơ sở dữ liệu.

### Vì sao quan trọng?

- **Non-repudiation (Tính không thể chối bỏ)**: Đảm bảo nhân sự nội bộ (kể cả quản trị viên cấp cao) không thể âm thầm thay đổi hoặc xóa bỏ dấu vết của các hành vi can thiệp trái phép.
- **Forensic Investigation (Điều tra số)**: Khi xảy ra sự cố bảo mật (như lộ dữ liệu học viên, gian lận thanh toán, hoặc đánh cắp token), đội ngũ phản ứng sự cố có thể dựng lại chính xác 100% dòng thời gian sự kiện (Timeline of Compromise) nhờ các bản ghi audit không thể bị làm sai lệch.
- **Bảo vệ nhiều lớp (Defense in Depth)**: Ngoài `@Immutable`, tầng repository ghi đè các hàm `delete*` để ném `UnsupportedOperationException`, và tầng DB chỉ cấp quyền `INSERT` / `SELECT` cho application database user.

### Ví dụ trong project này

[`AuditLog.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/audit/AuditLog.java):
```java
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_actor_id", columnList = "actor_id"),
    @Index(name = "idx_audit_logs_action", columnList = "action"),
    @Index(name = "idx_audit_logs_target", columnList = "target_type, target_id"),
    @Index(name = "idx_audit_logs_correlation_id", columnList = "correlation_id"),
    @Index(name = "idx_audit_logs_created_at", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.hibernate.annotations.Immutable
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 64)
    private AuditAction action;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_email", length = 150)
    private String actorEmail;

    @Column(name = "actor_role", length = 50)
    private String actorRole;

    @Column(name = "target_type", nullable = false, length = 64)
    private String targetType;

    @Column(name = "target_id", nullable = false, length = 128)
    private String targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 20)
    private AuditOutcome outcome;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

[`AuditLogRepository.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/audit/AuditLogRepository.java):
```java
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Override để chặn đứng mọi thao tác xóa ở tầng compile/runtime
    @Override
    default void delete(AuditLog entity) {
        throw new UnsupportedOperationException("Audit logs are append-only and cannot be deleted");
    }

    @Override
    default void deleteAll() {
        throw new UnsupportedOperationException("Audit logs are append-only and cannot be deleted");
    }
}
```

---

## 217. Transaction Propagation `REQUIRES_NEW` Cho Audit Trail & Pitfall Lồng Giao Dịch

### Khái niệm là gì?

Trong Spring Framework, `@Transactional(propagation = Propagation.REQUIRES_NEW)` là thuộc tính lan truyền giao dịch chỉ định rằng: khi phương thức được gọi, Spring sẽ tạm dừng (suspend) bất kỳ transaction hiện tại nào đang chạy, mượn một connection vật lý mới từ connection pool, mở một transaction hoàn toàn độc lập mới, commit hoặc rollback transaction này, sau đó mới khôi phục (resume) transaction ban đầu.

### Vì sao quan trọng trong Audit Logging?

Nếu phương thức ghi audit sử dụng `Propagation.REQUIRED` (mặc định), nó sẽ tham gia vào cùng một transaction với nghiệp vụ chính (ví dụ đổi mật khẩu hoặc thanh toán đơn hàng). Khi nghiệp vụ chính bị lỗi và ném ra Exception, toàn bộ transaction bị rollback, kéo theo việc bản ghi audit cũng biến mất! Kết quả là hệ thống không thể lưu lại dấu vết của các hành vi tấn công dò mật khẩu thất bại (brute force) hoặc giao dịch lỗi.

Bằng cách sử dụng `Propagation.REQUIRES_NEW`:
- Ngay cả khi transaction nghiệp vụ chính bị ném `AppException` và rollback, bản ghi audit với kết quả `AuditOutcome.FAILURE` vẫn được lưu vĩnh viễn vào cơ sở dữ liệu.
- Đảm bảo tính độc lập tuyệt đối giữa trạng thái lưu vết và kết quả nghiệp vụ.

### Cạm bẫy kỹ thuật (Pitfall) & Bài học kinh nghiệm

**Cạm bẫy Connection Pool Starvation & Deadlock với `saveAndFlush()`**:
- Khi một phương thức nghiệp vụ đang nắm giữ một transaction mở và gọi `saveAndFlush()` (như trong `OrderServiceImpl.createOrder()`), database đang giữ row lock / table lock cho bản ghi đơn hàng.
- Nếu ngay trong cùng một khối `try-catch` bắt lỗi trùng lặp (ví dụ `DataIntegrityViolationException`), ta lại gọi phương thức audit dùng `REQUIRES_NEW`, phương thức này bắt buộc phải lấy thêm một kết nối JDBC thứ 2 từ connection pool.
- Trong môi trường kiểm thử đồng thời (Concurrency Test với H2 in-memory), việc transaction cha bị rollback sau khi transaction con `REQUIRES_NEW` vừa chạy có thể gây lỗi `JpaSystemException: Unable to rollback against JDBC Connection - Connection is closed`.
- **Giải pháp**: Đối với các luồng có xử lý xung đột đồng thời cao (như tạo đơn hàng với Idempotency Key), tách việc ghi nhận audit ra ngoài khối `try-catch` sau khi nghiệp vụ hoàn tất thành công, hoặc chuyển sang cơ chế ghi nhận bất đồng bộ (`recordAsync`).

---

## 218. Asynchronous Audit Logging & ThreadPoolTaskExecutor `CallerRunsPolicy`

### Khái niệm là gì?

**Asynchronous Audit Logging** là mô hình ghi nhận sự kiện kiểm toán tách biệt khỏi luồng xử lý HTTP request chính (Non-blocking I/O). Thay vì người dùng phải chờ hệ thống thực thi câu lệnh SQL `INSERT INTO audit_logs`, sự kiện audit được đẩy vào một hàng đợi (Queue) trong bộ nhớ và được thực thi bởi một Thread Pool riêng biệt (`@Async("auditExecutor")`).

Để tránh mất dữ liệu khi lưu lượng tăng đột biến, cấu hình bộ xử lý từ chối (RejectedExecutionHandler) đóng vai trò sống còn:
- **`AbortPolicy`**: Ném `RejectedExecutionException` → làm hỏng request của người dùng (Không nên dùng).
- **`DiscardPolicy` / `DiscardOldestPolicy`**: Âm thầm vứt bỏ log → vi phạm quy chuẩn kiểm toán (Tuyệt đối không dùng).
- **`CallerRunsPolicy`**: Khi thread pool và hàng đợi đã đầy 100%, nhiệm vụ ghi audit sẽ được đẩy ngược lại cho chính luồng đang gọi (Calling Thread / Tomcat Worker) tự thực thi. Cơ chế này tự động kích hoạt **Backpressure** (áp lực ngược), làm chậm tốc độ tiếp nhận request của client một cách tự nhiên mà không bao giờ làm rớt bất kỳ dòng log nào.

### Ví dụ trong project này

[`AuditAsyncConfig.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/audit/AuditAsyncConfig.java):
```java
@Configuration
@EnableAsync
public class AuditAsyncConfig {

    @Bean(name = "auditExecutor")
    public Executor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("audit-exec-");
        // CallerRunsPolicy: Đảm bảo zero-drop audit events khi tải cao
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
```

---

## 219. Recursive Sensitive Data Redaction & Regex PII Sanitization Trong Nhật Ký Kiểm Toán

### Khái niệm là gì?

**Sensitive Data Redaction (Làm sạch dữ liệu nhạy cảm)** là kỹ thuật lọc, che giấu hoặc loại bỏ các thông tin bí mật (như mật khẩu, token JWT, số thẻ ngân hàng, mã CVV, cookie phiên) ra khỏi metadata của audit log trước khi chúng được lưu trữ lâu dài.

Một module làm sạch dữ liệu mạnh mẽ phải hoạt động theo nguyên lý đệ quy (Recursive Traversal) để phát hiện dữ liệu nhạy cảm dù chúng nằm ở bất kỳ độ sâu nào trong payload JSON (như nằm lồng trong Map, List, hoặc Object con).

### Giải pháp kỹ thuật đã triển khai

[`AuditDataRedactor.java`](file:///home/glitchtran/CODETHU/IT/ITWork/b2c_120626_JapaneseLearningWeb/backend/src/main/java/com/japaneselearning/common/audit/AuditDataRedactor.java):
1. **Blacklisted Keys Detection**:
   Tất cả các key chứa từ khóa: `password`, `secret`, `token`, `cookie`, `cvv`, `card`, `credential` (so sánh không phân biệt hoa thường) đều có giá trị bị thay thế ngay lập tức bằng `"[REDACTED]"`.
2. **Regex Pattern Sanitization**:
   Đối với các trường văn bản tự do hoặc chuỗi ký tự, module áp dụng các biểu thức chính quy để nhận dạng mẫu dữ liệu nhạy cảm:
   - **JSON Web Token (JWT)**: `Bearer\s+[A-Za-z0-9-_=]+\.[A-Za-z0-9-_=]+\.?[A-Za-z0-9-_.+/=]*` hoặc `eyJ[A-Za-z0-9-_=]+\.[A-Za-z0-9-_=]+\.?[A-Za-z0-9-_.+/=]*` → thay bằng `"[REDACTED_JWT]"`.
   - **Payment Card Numbers (PAN)**: Nhận diện chuỗi 13 đến 19 chữ số theo định dạng thẻ tín dụng/ghi nợ quốc tế (Visa, Mastercard, JCB) → thay bằng `"[REDACTED_CARD]"`.
3. **Graceful Degradation**:
   Nếu việc serialize metadata gặp sự cố (ví dụ gặp cyclic reference), hệ thống không ném Exception làm hỏng luồng audit mà sẽ ghi lại chuỗi fallback an toàn `{"redaction_error": "failed_to_serialize"}`.

