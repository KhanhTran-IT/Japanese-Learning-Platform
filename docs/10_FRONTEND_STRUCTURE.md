> Nguồn: tách từ file kế hoạch gốc `ke_hoach_he_thong_web_day_tieng_nhat(1).md`.

## 12. Cấu trúc thư mục frontend Vue 3

```text
src
│
├── assets
│   ├── images
│   ├── icons
│   └── styles
│
├── components
│   ├── common
│   ├── course
│   ├── lesson
│   ├── quiz
│   ├── flashcard
│   ├── game
│   └── admin
│
├── layouts
│   ├── MainLayout.vue
│   ├── AuthLayout.vue
│   ├── StudentLayout.vue
│   ├── AdminLayout.vue
│   └── TeacherLayout.vue
│
├── pages
│   ├── public
│   │   ├── HomePage.vue
│   │   ├── CourseListPage.vue
│   │   ├── CourseDetailPage.vue
│   │   ├── BlogPage.vue
│   │   └── ContactPage.vue
│   │
│   ├── auth
│   │   ├── LoginPage.vue
│   │   ├── RegisterPage.vue
│   │   ├── ForgotPasswordPage.vue
│   │   └── ResetPasswordPage.vue
│   │
│   ├── student
│   │   ├── StudentDashboardPage.vue
│   │   ├── MyCoursesPage.vue
│   │   ├── LearningPage.vue
│   │   ├── QuizPage.vue
│   │   ├── FlashcardPage.vue
│   │   ├── GamePage.vue
│   │   ├── ProfilePage.vue
│   │   └── OrdersPage.vue
│   │
│   └── admin
│       ├── AdminDashboardPage.vue
│       ├── UserManagementPage.vue
│       ├── CourseManagementPage.vue
│       ├── LessonManagementPage.vue
│       ├── QuizManagementPage.vue
│       ├── OrderManagementPage.vue
│       ├── ReportPage.vue
│       └── SiteSettingPage.vue
│
├── router
│   ├── index.js
│   └── guards.js
│
├── stores
│   ├── auth.store.js
│   ├── user.store.js
│   ├── course.store.js
│   └── cart.store.js
│
├── services
│   ├── api.js
│   ├── auth.service.js
│   ├── user.service.js
│   ├── course.service.js
│   ├── lesson.service.js
│   ├── quiz.service.js
│   ├── payment.service.js
│   └── admin.service.js
│
├── utils
│   ├── formatCurrency.js
│   ├── formatDate.js
│   └── validate.js
│
├── App.vue
└── main.js
```

---
