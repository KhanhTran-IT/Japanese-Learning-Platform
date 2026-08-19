import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth.store'
import { AuthService } from '@/services/auth.service'
import setupGuards from './guards'

// Mock AuthService
vi.mock('@/services/auth.service', () => ({
  AuthService: {
    getCurrentUser: vi.fn()
  }
}))

describe('Router Guards', () => {
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

  it('calls authStore.initAuth() if not initialized', async () => {
    const authStore = useAuthStore()
    authStore.initialized = false
    authStore.initAuth = vi.fn().mockResolvedValue()

    const to = { matched: [], path: '/some-path', meta: {} }
    const from = {}

    await beforeEachCallback(to, from)

    expect(authStore.initAuth).toHaveBeenCalledTimes(1)
  })

  it('redirects to /login if route requires auth and user is not authenticated', async () => {
    const authStore = useAuthStore()
    authStore.initialized = true
    authStore.accessToken = null

    const to = { matched: [{ meta: { requiresAuth: true } }], path: '/protected', meta: { requiresAuth: true } }
    const from = {}

    const result = await beforeEachCallback(to, from)

    expect(result).toBe('/login')
  })

  it('redirects to appropriate dashboard if authenticated user tries to access /login', async () => {
    const authStore = useAuthStore()
    authStore.initialized = true
    authStore.accessToken = 'fake-token'
    authStore.user = { roles: ['STUDENT'] }

    const to = { matched: [], path: '/login', meta: {} }
    const from = {}

    const result = await beforeEachCallback(to, from)

    expect(result).toBe('/student/dashboard')
  })
})
