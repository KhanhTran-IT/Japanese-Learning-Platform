import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth.store'
import { AuthService } from '@/services/auth.service'
import setupGuards, { getDefaultRouteForRoles, isUserAuthorizedForRoute } from './guards'

// Mock AuthService
vi.mock('@/services/auth.service', () => ({
  AuthService: {
    getCurrentUser: vi.fn()
  }
}))

describe('Router Guards & Role-Permission Enforcement', () => {
  let routerMock
  let beforeEachCallback

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    routerMock = {
      beforeEach: vi.fn((cb) => {
        beforeEachCallback = cb
      })
    }
    
    setupGuards(routerMock)
  })

  describe('getDefaultRouteForRoles helper', () => {
    it('returns /admin/dashboard for SUPER_ADMIN and ADMIN', () => {
      expect(getDefaultRouteForRoles(['SUPER_ADMIN'])).toBe('/admin/dashboard')
      expect(getDefaultRouteForRoles(['ADMIN'])).toBe('/admin/dashboard')
      expect(getDefaultRouteForRoles(['ADMIN', 'TEACHER'])).toBe('/admin/dashboard')
    })

    it('returns /admin/courses for TEACHER', () => {
      expect(getDefaultRouteForRoles(['TEACHER'])).toBe('/admin/courses')
    })

    it('returns /student/dashboard for STUDENT', () => {
      expect(getDefaultRouteForRoles(['STUDENT'])).toBe('/student/dashboard')
    })

    it('returns / for empty or unknown roles', () => {
      expect(getDefaultRouteForRoles([])).toBe('/')
      expect(getDefaultRouteForRoles(['UNKNOWN'])).toBe('/')
    })
  })

  describe('isUserAuthorizedForRoute helper', () => {
    it('allows access when route has no role restrictions', () => {
      const to = { matched: [{ meta: {} }, { meta: {} }], meta: {} }
      expect(isUserAuthorizedForRoute(to, ['STUDENT'])).toBe(true)
      expect(isUserAuthorizedForRoute(to, [])).toBe(true)
    })

    it('checks roles array matching', () => {
      const to = {
        matched: [
          { meta: { roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } },
          { meta: { roles: ['ADMIN', 'SUPER_ADMIN'] } }
        ]
      }
      expect(isUserAuthorizedForRoute(to, ['TEACHER'])).toBe(false)
      expect(isUserAuthorizedForRoute(to, ['ADMIN'])).toBe(true)
      expect(isUserAuthorizedForRoute(to, ['SUPER_ADMIN'])).toBe(true)
    })

    it('supports legacy single role string in meta', () => {
      const to = {
        matched: [
          { meta: { role: 'STUDENT' } }
        ]
      }
      expect(isUserAuthorizedForRoute(to, ['STUDENT'])).toBe(true)
      expect(isUserAuthorizedForRoute(to, ['TEACHER'])).toBe(false)
    })
  })

  describe('Authentication & Initialization', () => {
    it('calls authStore.initAuth() if not initialized', async () => {
      const authStore = useAuthStore()
      authStore.initialized = false
      authStore.initAuth = vi.fn().mockResolvedValue()

      const to = { matched: [], path: '/courses', meta: {} }
      await beforeEachCallback(to, {})

      expect(authStore.initAuth).toHaveBeenCalledTimes(1)
    })

    it('redirects unauthenticated users to /login when route requires auth', async () => {
      const authStore = useAuthStore()
      authStore.initialized = true
      authStore.accessToken = null

      const to = { matched: [{ meta: { requiresAuth: true } }], path: '/student/dashboard', meta: { requiresAuth: true } }
      const result = await beforeEachCallback(to, {})

      expect(result).toBe('/login')
    })

    it('allows access to public error page /403 without redirect', async () => {
      const authStore = useAuthStore()
      authStore.initialized = true
      authStore.accessToken = null

      const to = { matched: [], path: '/403', meta: {} }
      const result = await beforeEachCallback(to, {})

      expect(result).toBeUndefined()
    })
  })

  describe('Safe Redirect on Login / Register for Every Role', () => {
    it('redirects STUDENT to /student/dashboard when accessing /login', async () => {
      const authStore = useAuthStore()
      authStore.initialized = true
      authStore.accessToken = 'token'
      authStore.user = { roles: ['STUDENT'] }

      const result = await beforeEachCallback({ matched: [], path: '/login', meta: {} }, {})
      expect(result).toBe('/student/dashboard')
    })

    it('redirects TEACHER to /admin/courses when accessing /register', async () => {
      const authStore = useAuthStore()
      authStore.initialized = true
      authStore.accessToken = 'token'
      authStore.user = { roles: ['TEACHER'] }

      const result = await beforeEachCallback({ matched: [], path: '/register', meta: {} }, {})
      expect(result).toBe('/admin/courses')
    })

    it('redirects ADMIN to /admin/dashboard when accessing /login', async () => {
      const authStore = useAuthStore()
      authStore.initialized = true
      authStore.accessToken = 'token'
      authStore.user = { roles: ['ADMIN'] }

      const result = await beforeEachCallback({ matched: [], path: '/login', meta: {} }, {})
      expect(result).toBe('/admin/dashboard')
    })

    it('redirects SUPER_ADMIN to /admin/dashboard when accessing /login', async () => {
      const authStore = useAuthStore()
      authStore.initialized = true
      authStore.accessToken = 'token'
      authStore.user = { roles: ['SUPER_ADMIN'] }

      const result = await beforeEachCallback({ matched: [], path: '/login', meta: {} }, {})
      expect(result).toBe('/admin/dashboard')
    })
  })

  describe('STUDENT Role Enforcement', () => {
    let authStore

    beforeEach(() => {
      authStore = useAuthStore()
      authStore.initialized = true
      authStore.accessToken = 'student-token'
      authStore.user = { roles: ['STUDENT'] }
    })

    it('allows access to student dashboard', async () => {
      const to = {
        matched: [{ meta: { requiresAuth: true } }, { meta: { roles: ['STUDENT'] } }],
        path: '/student/dashboard',
        meta: { roles: ['STUDENT'] }
      }
      const result = await beforeEachCallback(to, {})
      expect(result).toBeUndefined()
    })

    it('redirects to /403 when STUDENT tries to access ADMIN-only dashboard', async () => {
      const to = {
        matched: [
          { meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } },
          { meta: { roles: ['ADMIN', 'SUPER_ADMIN'] } }
        ],
        path: '/admin/dashboard',
        meta: {}
      }
      const result = await beforeEachCallback(to, {})
      expect(result).toBe('/403')
    })

    it('redirects to /403 when STUDENT tries to access ADMIN-only user management', async () => {
      const to = {
        matched: [
          { meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } },
          { meta: { roles: ['ADMIN', 'SUPER_ADMIN'] } }
        ],
        path: '/admin/users',
        meta: {}
      }
      const result = await beforeEachCallback(to, {})
      expect(result).toBe('/403')
    })

    it('redirects to /403 when STUDENT tries to access TEACHER/ADMIN course management', async () => {
      const to = {
        matched: [
          { meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } },
          { meta: { roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } }
        ],
        path: '/admin/courses',
        meta: {}
      }
      const result = await beforeEachCallback(to, {})
      expect(result).toBe('/403')
    })
  })

  describe('TEACHER Role Enforcement', () => {
    let authStore

    beforeEach(() => {
      authStore = useAuthStore()
      authStore.initialized = true
      authStore.accessToken = 'teacher-token'
      authStore.user = { roles: ['TEACHER'] }
    })

    it('allows TEACHER to access course management (/admin/courses)', async () => {
      const to = {
        matched: [
          { meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } },
          { meta: { roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } }
        ],
        path: '/admin/courses',
        meta: {}
      }
      const result = await beforeEachCallback(to, {})
      expect(result).toBeUndefined()
    })

    it('allows TEACHER to access quiz management (/admin/quizzes)', async () => {
      const to = {
        matched: [
          { meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } },
          { meta: { roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } }
        ],
        path: '/admin/quizzes',
        meta: {}
      }
      const result = await beforeEachCallback(to, {})
      expect(result).toBeUndefined()
    })

    it('redirects to /403 when TEACHER tries to access ADMIN-only dashboard', async () => {
      const to = {
        matched: [
          { meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } },
          { meta: { roles: ['ADMIN', 'SUPER_ADMIN'] } }
        ],
        path: '/admin/dashboard',
        meta: {}
      }
      const result = await beforeEachCallback(to, {})
      expect(result).toBe('/403')
    })

    it('redirects to /403 when TEACHER tries to access ADMIN-only user management', async () => {
      const to = {
        matched: [
          { meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } },
          { meta: { roles: ['ADMIN', 'SUPER_ADMIN'] } }
        ],
        path: '/admin/users',
        meta: {}
      }
      const result = await beforeEachCallback(to, {})
      expect(result).toBe('/403')
    })

    it('redirects to /403 when TEACHER tries to access STUDENT-only orders', async () => {
      const to = {
        matched: [
          { meta: { requiresAuth: true } },
          { meta: { roles: ['STUDENT'] } }
        ],
        path: '/student/orders',
        meta: {}
      }
      const result = await beforeEachCallback(to, {})
      expect(result).toBe('/403')
    })

    it('allows TEACHER to access user profile (/student/profile)', async () => {
      const to = {
        matched: [
          { meta: { requiresAuth: true } },
          { meta: {} }
        ],
        path: '/student/profile',
        meta: {}
      }
      const result = await beforeEachCallback(to, {})
      expect(result).toBeUndefined()
    })
  })

  describe('ADMIN and SUPER_ADMIN Role Enforcement', () => {
    it('allows ADMIN to access dashboard, users, courses and quizzes', async () => {
      const authStore = useAuthStore()
      authStore.initialized = true
      authStore.accessToken = 'admin-token'
      authStore.user = { roles: ['ADMIN'] }

      const toDashboard = {
        matched: [
          { meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } },
          { meta: { roles: ['ADMIN', 'SUPER_ADMIN'] } }
        ],
        path: '/admin/dashboard',
        meta: {}
      }
      expect(await beforeEachCallback(toDashboard, {})).toBeUndefined()

      const toUsers = {
        matched: [
          { meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } },
          { meta: { roles: ['ADMIN', 'SUPER_ADMIN'] } }
        ],
        path: '/admin/users',
        meta: {}
      }
      expect(await beforeEachCallback(toUsers, {})).toBeUndefined()
    })

    it('redirects ADMIN to /403 when accessing STUDENT-only checkout/orders', async () => {
      const authStore = useAuthStore()
      authStore.initialized = true
      authStore.accessToken = 'admin-token'
      authStore.user = { roles: ['ADMIN'] }

      const toCheckout = {
        matched: [
          { meta: { requiresAuth: true, roles: ['STUDENT'] } }
        ],
        path: '/checkout/100',
        meta: { roles: ['STUDENT'] }
      }
      expect(await beforeEachCallback(toCheckout, {})).toBe('/403')
    })

    it('allows SUPER_ADMIN to access all admin endpoints', async () => {
      const authStore = useAuthStore()
      authStore.initialized = true
      authStore.accessToken = 'super-token'
      authStore.user = { roles: ['SUPER_ADMIN'] }

      const toDashboard = {
        matched: [
          { meta: { requiresAuth: true, roles: ['ADMIN', 'SUPER_ADMIN', 'TEACHER'] } },
          { meta: { roles: ['ADMIN', 'SUPER_ADMIN'] } }
        ],
        path: '/admin/dashboard',
        meta: {}
      }
      expect(await beforeEachCallback(toDashboard, {})).toBeUndefined()
    })
  })
})
