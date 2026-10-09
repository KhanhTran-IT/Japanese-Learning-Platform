> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 11. Cấu trúc thư mục backend Spring Boot

```text
src/main/java/com/japaneselearning
│
├── JapaneseLearningApplication.java
│
├── common
│   ├── config
│   │   ├── SecurityConfig.java
│   │   ├── CorsConfig.java
│   │   ├── SwaggerConfig.java
│   │   ├── RedisConfig.java
│   │   └── MailConfig.java
│   │
│   ├── exception
│   │   ├── GlobalExceptionHandler.java
│   │   ├── AppException.java
│   │   ├── ErrorCode.java
│   │   └── ValidationError.java
│   │
│   ├── response
│   │   ├── ApiResponse.java
│   │   └── PageResponse.java
│   │
│   ├── security
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CustomUserDetails.java
│   │   └── CustomUserDetailsService.java
│   │
│   ├── util
│   │   ├── SlugUtil.java
│   │   ├── DateTimeUtil.java
│   │   └── FileUtil.java
│   │
│   └── constant
│       ├── RoleConstant.java
│       └── AppConstant.java
│
├── module_auth
│   ├── controller
│   ├── service
│   ├── dto
│   │   ├── request
│   │   └── response
│   ├── entity
│   └── repository
│
├── module_user
│   ├── controller
│   ├── service
│   ├── dto
│   ├── entity
│   └── repository
│
├── module_course
│   ├── controller
│   ├── service
│   ├── dto
│   ├── entity
│   └── repository
│
├── module_lesson
├── module_quiz
├── module_flashcard
├── module_game
├── module_payment
├── module_notification
├── module_admin
├── module_report
└── module_cms
```

---
