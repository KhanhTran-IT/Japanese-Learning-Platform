import { createRouter, createWebHistory } from 'vue-router'
import setupGuards from './guards'

// Layouts
import MainLayout from '@/layouts/MainLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'
import StudentLayout from '@/layouts/StudentLayout.vue'
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
    path: '/student',
    component: StudentLayout,
    meta: { requiresAuth: true, role: 'STUDENT' },
    children: [
      {
        path: 'dashboard',
        name: 'StudentDashboard',
        component: () => import('@/pages/student/StudentDashboardPage.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/pages/admin/AdminDashboardPage.vue')
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
