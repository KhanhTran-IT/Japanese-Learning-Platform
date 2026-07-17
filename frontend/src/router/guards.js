import { useAuthStore } from '@/stores/auth.store'

export default function setupGuards(router) {
  router.beforeEach((to, from) => {
    const authStore = useAuthStore()
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

    if (requiresAuth && !authStore.isAuthenticated) {
      return '/login'
    }

    if (authStore.isAuthenticated && (to.path === '/login' || to.path === '/register')) {
      return '/student/dashboard'
    }

    // Role-based access control
    const requiredRole = to.meta.role
    if (requiredRole && authStore.user) {
      const userRoles = authStore.user.roles || []
      if (!userRoles.includes(requiredRole) && !userRoles.includes('SUPER_ADMIN')) {
        // Nếu user không đúng role, điều hướng về trang chủ mặc định
        return userRoles.includes('ADMIN') ? '/admin/dashboard' : '/student/dashboard'
      }
    }
  })
}
