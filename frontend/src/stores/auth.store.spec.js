import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth.store'
import axios from 'axios'
import api from '@/services/api'
import { AuthCoordinator, _resetCoordinatorState } from '@/services/auth-coordinator'

vi.mock('axios')
vi.mock('@/services/api', () => ({
  default: {
    defaults: {
      baseURL: '/api',
      headers: { common: {} }
    },
    get: vi.fn()
  }
}))

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    api.defaults.headers.common = {}
    _resetCoordinatorState()
  })

  it('initAuth successfully restores session via HttpOnly cookie', async () => {
    axios.post.mockResolvedValueOnce({
      data: {
        code: 1000,
        result: { accessToken: 'cookie-restored-token' }
      }
    })
    api.get.mockResolvedValueOnce({
      data: {
        code: 1000,
        result: { id: 1, email: 'student@example.com', roles: ['STUDENT'] }
      }
    })

    const store = useAuthStore()
    await store.initAuth()

    expect(axios.post).toHaveBeenCalledWith('/api/auth/refresh-token', {}, { withCredentials: true })
    expect(store.accessToken).toBe('cookie-restored-token')
    expect(store.user).toEqual({ id: 1, email: 'student@example.com', roles: ['STUDENT'] })
    expect(store.isAuthenticated).toBe(true)
    expect(api.defaults.headers.common['Authorization']).toBe('Bearer cookie-restored-token')
  })

  it('initAuth clears auth state and Authorization header if cookie refresh fails (401 / expired)', async () => {
    axios.post.mockRejectedValueOnce(new Error('Unauthorized'))

    const store = useAuthStore()
    store.setTokens('stale-token')
    store.setUser({ id: 1 })
    expect(api.defaults.headers.common['Authorization']).toBe('Bearer stale-token')

    await store.initAuth()

    expect(store.accessToken).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(store.initialized).toBe(true)
    expect(api.defaults.headers.common['Authorization']).toBeUndefined()
  })

  it('clearAuth removes Axios default Authorization header as well as Pinia state', () => {
    const store = useAuthStore()
    store.setTokens('active-access-token')
    store.setUser({ id: 10, email: 'user@example.com', roles: ['STUDENT'] })

    expect(store.accessToken).toBe('active-access-token')
    expect(store.isAuthenticated).toBe(true)
    expect(store.user).not.toBeNull()
    expect(api.defaults.headers.common['Authorization']).toBe('Bearer active-access-token')

    store.clearAuth()

    expect(store.accessToken).toBeNull()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(api.defaults.headers.common['Authorization']).toBeUndefined()
  })

  it('setTokens sets Authorization header when token provided, and clears it when null', () => {
    const store = useAuthStore()

    store.setTokens('new-token-123')
    expect(store.accessToken).toBe('new-token-123')
    expect(api.defaults.headers.common['Authorization']).toBe('Bearer new-token-123')

    store.setTokens(null)
    expect(store.accessToken).toBeNull()
    expect(api.defaults.headers.common['Authorization']).toBeUndefined()
  })

  it('never stores refresh token in store state', () => {
    const store = useAuthStore()
    expect(store.refreshToken).toBeUndefined()
    expect(Object.keys(store.$state)).not.toContain('refreshToken')
  })

  it('initAuth serializes behind an active cross-tab refresh lock', async () => {
    const tab1 = new AuthCoordinator({ tabId: 'tab-1' })
    const lock1 = await tab1.acquireRefreshLock({ timeout: 1000, ttl: 3000 })

    axios.post.mockResolvedValueOnce({
      data: {
        code: 1000,
        result: { accessToken: 'serialized-tab-token' }
      }
    })
    api.get.mockResolvedValueOnce({
      data: {
        code: 1000,
        result: { id: 2, email: 'tab2@example.com', roles: ['STUDENT'] }
      }
    })

    const store = useAuthStore()
    const initPromise = store.initAuth()

    // Wait a tick while tab 1 holds the lock
    await new Promise(r => setTimeout(r, 40))
    expect(axios.post).not.toHaveBeenCalled()

    // Tab 1 releases lock
    lock1.release(true)

    await initPromise

    expect(axios.post).toHaveBeenCalledWith('/api/auth/refresh-token', {}, { withCredentials: true })
    expect(store.accessToken).toBe('serialized-tab-token')
    expect(store.isAuthenticated).toBe(true)

    tab1.destroy()
  })

  it('initAuth aborts without network call when another tab reports REFRESH_FAILURE', async () => {
    const tab1 = new AuthCoordinator({ tabId: 'tab-1' })
    const lock1 = await tab1.acquireRefreshLock({ timeout: 1000, ttl: 3000 })

    const store = useAuthStore()
    const initPromise = store.initAuth()

    await new Promise(r => setTimeout(r, 40))

    // Tab 1 reports refresh failure (session expired)
    lock1.release(false, { reason: 'Cookie expired' })

    await initPromise

    expect(axios.post).not.toHaveBeenCalled()
    expect(store.accessToken).toBeNull()
    expect(store.initialized).toBe(true)
    expect(store.isAuthenticated).toBe(false)

    tab1.destroy()
  })

  it('deduplicates concurrent in-flight initAuth calls within the same tab', async () => {
    axios.post.mockResolvedValueOnce({
      data: {
        code: 1000,
        result: { accessToken: 'single-call-token' }
      }
    })
    api.get.mockResolvedValueOnce({
      data: {
        code: 1000,
        result: { id: 3, email: 'dedup@example.com', roles: ['STUDENT'] }
      }
    })

    const store = useAuthStore()
    const [p1, p2, p3] = [store.initAuth(), store.initAuth(), store.initAuth()]

    await Promise.all([p1, p2, p3])

    expect(axios.post).toHaveBeenCalledTimes(1)
    expect(store.accessToken).toBe('single-call-token')
  })
})
