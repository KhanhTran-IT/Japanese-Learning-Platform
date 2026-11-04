import { useAuthStore } from '@/stores/auth.store'
import { AuthService } from '@/services/auth.service'

/**
 * Returns the default dashboard/landing route based on user roles.
 * Priority: SUPER_ADMIN / ADMIN -> /admin/dashboard
 *           TEACHER             -> /admin/courses
 *           STUDENT             -> /student/dashboard
 *           Other / Guest       -> /
 */
export function getDefaultRouteForRoles(userRoles = []) {
  if (userRoles.includes('ADMIN') || userRoles.includes('SUPER_ADMIN')) {
    return '/admin/dashboard'
  }
  if (userRoles.includes('TEACHER')) {
    return '/admin/courses'
  }
  if (userRoles.includes('STUDENT')) {
    return '/student/dashboard'
  }
  return '/'
}

/**
 * Checks whether user has permission to access the target route.
 * Evaluates all matched route records from parent layout to leaf route.
 * Supports both modern array `meta.roles` and legacy string `meta.role`.
 */
export function isUserAuthorizedForRoute(to, userRoles = []) {
  if (!to) return true

  const records = to.matched && to.matched.length > 0 ? to.matched : [to]

  for (const record of records) {
    const requiredRoles = record.meta?.roles || (record.meta?.role ? [record.meta.role] : null)
    if (requiredRoles && requiredRoles.length > 0) {
      const hasPermission = requiredRoles.some(role => userRoles.includes(role))
      if (!hasPermission) {
        return false
      }
    }
  }

  return true
}

export default function setupGuards(router) {
  router.beforeEach(async (to, from) => {
    const authStore = useAuthStore()
    
    if (!authStore.initialized) {
      await authStore.initAuth()
    }

    // Public error pages like 403 are accessible without redirection
    if (to.path === '/403') {
      return
    }

    const requiresAuth = to.matched?.some(record => record.meta?.requiresAuth) || to.meta?.requiresAuth

    if (requiresAuth && !authStore.isAuthenticated) {
      return '/login'
    }

    // initAuth already handles hydrating the user on page reload
    if (authStore.isAuthenticated && !authStore.user) {
      try {
        const userRes = await AuthService.getCurrentUser()
        if (userRes.data?.code === 1000) {
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

    // Authenticated users trying to access login or register are safely redirected to their dashboard
    if (authStore.isAuthenticated && (to.path === '/login' || to.path === '/register')) {
      const userRoles = authStore.user?.roles || []
      return getDefaultRouteForRoles(userRoles)
    }

    // Role-based access control: enforce forbidden state (403) for unauthorized roles
    if (authStore.isAuthenticated && authStore.user) {
      const userRoles = authStore.user.roles || []
      if (!isUserAuthorizedForRoute(to, userRoles)) {
        return '/403'
      }
    }
  })
}
