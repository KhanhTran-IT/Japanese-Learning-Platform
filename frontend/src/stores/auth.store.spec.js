import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth.store'
import axios from 'axios'
import api from '@/services/api'

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
})
