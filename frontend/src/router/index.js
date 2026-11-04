import { createRouter, createWebHistory } from 'vue-router'
import setupGuards from './guards'

// Layouts
import MainLayout from '@/layouts/MainLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'
import StudentLayout from '@/layouts/StudentLayout.vue'
import LearningLayout from '@/layouts/LearningLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/pages/public/HomePage.vue')
      },
      {
        path: 'courses',
        name: 'CourseList',
        component: () => import('@/pages/public/CourseListPage.vue')
      },
      {
        path: 'courses/:slug',
        name: 'CourseDetail',
        component: () => import('@/pages/public/CourseDetailPage.vue')
      },
      {
        path: 'checkout/:orderId',
        name: 'Checkout',
        component: () => import('@/pages/student/CheckoutPage.vue'),
        meta: { requiresAuth: true, roles: ['STUDENT'] },
        props: true
      }
    ]
  },
  {
    path: '/',
    component: AuthLayout,
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/pages/auth/LoginPage.vue')
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/pages/auth/RegisterPage.vue')
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/pages/error/ForbiddenPage.vue')
  },
  {
    path: '/student',
    component: StudentLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/pages/student/StudentDashboardPage.vue'),
        meta: { roles: ['STUDENT'] }
      },
      {
        path: 'my-courses',
        name: 'StudentMyCourses',
        component: () => import('@/pages/student/MyCoursesPage.vue'),
        meta: { roles: ['STUDENT'] }
      },
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('@/pages/student/ProfilePage.vue')
      },
      {
        path: 'flashcards',
        name: 'StudentFlashcards',
        component: () => import('@/pages/student/FlashcardPage.vue'),
        meta: { roles: ['STUDENT'] }
      },
      {
        path: 'leaderboard',
        name: 'StudentLeaderboard',
        component: () => import('@/pages/student/LeaderboardPage.vue'),
        meta: { roles: ['STUDENT'] }
      },
      {
        path: 'orders',
        name: 'StudentOrders',
        component: () => import('@/pages/student/OrderHistoryPage.vue'),
        meta: { roles: ['STUDENT'] }
      }
    ]
  },
  {
    path: '/student',
    component: LearningLayout,
    meta: { requiresAuth: true, roles: ['STUDENT'] },
    children: [
      {
        path: 'lessons/:id',
        name: 'LessonLearning',
        component: () => import('@/pages/student/LessonLearningPage.vue')
      },
      {
        path: 'quizzes/:quizId',
        name: 'QuizTaking',
        component: () => import('@/pages/student/QuizTakingPage.vue')
      },
      {
        path: 'quizzes/:quizId/result/:attemptId',
        name: 'QuizResult',
        component: () => import('@/pages/student/QuizResultPage.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/pages/admin/AdminDashboardPage.vue'),
        meta: { roles: ['ADMIN', 'SUPER_ADMIN'] }
      },
      {
        path: 'users',
        name: 'AdminUserManagement',
        component: () => import('@/pages/admin/AdminUserManagementPage.vue'),
        meta: { roles: ['ADMIN', 'SUPER_ADMIN'] }
      },
      {
        path: 'courses',
        name: 'AdminCourseManagement',
        component: () => import('@/pages/admin/AdminCourseManagementPage.vue'),
        meta: { roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] }
      },
      {
        path: 'courses/:id/structure',
        name: 'AdminCourseStructure',
        component: () => import('@/pages/admin/AdminCourseStructurePage.vue'),
        meta: { roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] },
        props: true
      },
      {
        path: 'quizzes',
        name: 'AdminQuizManagement',
        component: () => import('@/pages/admin/AdminQuizManagementPage.vue'),
        meta: { roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] }
      },
      {
        path: 'quizzes/:id/builder',
        name: 'AdminQuizBuilder',
        component: () => import('@/pages/admin/AdminQuizBuilderPage.vue'),
        meta: { roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] },
        props: true
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

setupGuards(router)

export default router
