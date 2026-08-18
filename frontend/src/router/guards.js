import { useAuthStore } from '@/stores/auth.store'
import { AuthService } from '@/services/auth.service'

export default function setupGuards(router) {
  router.beforeEach(async (to, from) => {
    const authStore = useAuthStore()
    
    if (!authStore.initialized) {
      await authStore.initAuth()
    }

    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

    if (requiresAuth && !authStore.isAuthenticated) {
      return '/login'
    }

    // initAuth already handles hydrating the user on page reload
    if (authStore.isAuthenticated && !authStore.user) {
      try {
        const userRes = await AuthService.getCurrentUser()
        if (userRes.data.code === 1000) {
          authStore.setUser(userRes.data.result)
        } else {
          throw new Error('Invalid user fetch response')
        }
      } catch (error) {
        // Token might be invalid or expired, clear auth
        authStore.clearAuth()
        if (requiresAuth) {
          return '/login'
        }
      }
    }

    if (authStore.isAuthenticated && (to.path === '/login' || to.path === '/register')) {
      const userRoles = authStore.user?.roles || []
      return (userRoles.includes('ADMIN') || userRoles.includes('SUPER_ADMIN')) ? '/admin/dashboard' : '/student/dashboard'
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
