import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import axios from 'axios'
import api, { _resetRefreshState, setRedirectHandler, resetRedirectHandler } from './api'
import { useAuthStore } from '@/stores/auth.store'

describe('api.js authentication lifecycle and regression tests', () => {
  let authStore
  let mockRedirect
  let adapterMock

  beforeEach(() => {
    setActivePinia(createPinia())
    authStore = useAuthStore()
    _resetRefreshState()

    mockRedirect = vi.fn()
    setRedirectHandler(mockRedirect)

    delete api.defaults.headers.common['Authorization']
    delete api.defaults.headers.common.Authorization

    adapterMock = vi.fn()
    api.defaults.adapter = adapterMock
    vi.restoreAllMocks()
  })

  // Helper to create an Axios 401 error with config
  const create401Error = (config, url = '/test-protected') => {
    const error = new Error('Request failed with status code 401')
    error.isAxiosError = true
    error.config = config || { url, headers: {} }
    error.response = {
      status: 401,
      statusText: 'Unauthorized',
      data: { code: 2002, message: 'Unauthorized' }
    }
    return error
  }

  describe('Request Interceptor', () => {
    it('attaches Authorization header when accessToken exists in store', async () => {
      authStore.setTokens('valid-access-token')

      adapterMock.mockResolvedValueOnce({
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {},
        data: { message: 'success' }
      })

      await api.get('/courses')

      expect(adapterMock).toHaveBeenCalledTimes(1)
      const callConfig = adapterMock.mock.calls[0][0]
      const authHeader = callConfig.headers?.Authorization || (callConfig.headers?.get && callConfig.headers.get('Authorization'))
      expect(authHeader).toBe('Bearer valid-access-token')
    })

    it('does not send Authorization header when user is unauthenticated', async () => {
      authStore.clearAuth()

      adapterMock.mockResolvedValueOnce({
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {},
        data: { message: 'success' }
      })

      await api.get('/public-data')

      expect(adapterMock).toHaveBeenCalledTimes(1)
      const callConfig = adapterMock.mock.calls[0][0]
      const authHeader = callConfig.headers?.Authorization || (callConfig.headers?.get && callConfig.headers.get('Authorization'))
      expect(authHeader).toBeUndefined()
    })

    it('removes pre-existing or stale Authorization header if authStore has no accessToken', async () => {
      authStore.clearAuth()

      adapterMock.mockResolvedValueOnce({
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {},
        data: { message: 'success' }
      })

      await api.get('/public-data', {
        headers: { Authorization: 'Bearer stale-leftover-token' }
      })

      expect(adapterMock).toHaveBeenCalledTimes(1)
      const callConfig = adapterMock.mock.calls[0][0]
      const authHeader = callConfig.headers?.Authorization || (callConfig.headers?.get && callConfig.headers.get('Authorization'))
      expect(authHeader).toBeUndefined()
    })
  })

  describe('Response Interceptor - Public Auth Endpoints', () => {
    it('does not attempt token refresh for 401 on /auth/login', async () => {
      const postSpy = vi.spyOn(axios, 'post')

      adapterMock.mockRejectedValueOnce(create401Error(null, '/auth/login'))

      await expect(api.post('/auth/login', { email: 'a@b.com' })).rejects.toThrow()
      expect(postSpy).not.toHaveBeenCalled()
      expect(mockRedirect).not.toHaveBeenCalled()
    })

    it('does not attempt token refresh for 401 on /auth/logout', async () => {
      const postSpy = vi.spyOn(axios, 'post')

      adapterMock.mockRejectedValueOnce(create401Error(null, '/auth/logout'))

      await expect(api.post('/auth/logout')).rejects.toThrow()
      expect(postSpy).not.toHaveBeenCalled()
    })
  })

  describe('Response Interceptor - Token Replacement & Retry', () => {
    it('successfully refreshes token on 401 and retries original request with new token', async () => {
      authStore.setTokens('expired-access-token')

      // Spy on axios.post for refresh-token
      const postSpy = vi.spyOn(axios, 'post').mockResolvedValueOnce({
        data: {
          code: 1000,
          result: { accessToken: 'fresh-new-token' }
        }
      })

      // 1st call fails with 401, 2nd call (retry) succeeds
      adapterMock
        .mockRejectedValueOnce(create401Error(null, '/users/me'))
        .mockResolvedValueOnce({
          status: 200,
          statusText: 'OK',
          headers: {},
          config: {},
          data: { result: { id: 1, email: 'student@example.com' } }
        })

      const res = await api.get('/users/me')

      expect(res.data.result.email).toBe('student@example.com')
      expect(postSpy).toHaveBeenCalledWith('/api/auth/refresh-token', {}, { withCredentials: true })
      expect(authStore.accessToken).toBe('fresh-new-token')
      expect(api.defaults.headers.common['Authorization']).toBe('Bearer fresh-new-token')

      // Check the retry call sent the fresh token
      expect(adapterMock).toHaveBeenCalledTimes(2)
      const retryConfig = adapterMock.mock.calls[1][0]
      const authHeader = retryConfig.headers?.Authorization || (retryConfig.headers?.get && retryConfig.headers.get('Authorization'))
      expect(authHeader).toBe('Bearer fresh-new-token')
      expect(retryConfig._retry).toBe(true)
    })
  })

  describe('Response Interceptor - Refresh Failure & Stale Token Prevention', () => {
    it('clears auth, removes default header, redirects to login, and ensures NO stale token is sent after failed refresh', async () => {
      authStore.setTokens('stale-expired-token')
      authStore.setUser({ id: 1, name: 'Student' })

      // Refresh fails with 401 (refresh token expired)
      vi.spyOn(axios, 'post').mockRejectedValueOnce(new Error('Refresh token expired'))

      adapterMock.mockRejectedValueOnce(create401Error(null, '/student/courses'))

      await expect(api.get('/student/courses')).rejects.toThrow('Refresh token expired')

      // 1. Auth state in Pinia is cleared
      expect(authStore.accessToken).toBeNull()
      expect(authStore.user).toBeNull()
      expect(authStore.isAuthenticated).toBe(false)

      // 2. Axios default common Authorization header is removed
      expect(api.defaults.headers.common['Authorization']).toBeUndefined()

      // 3. Deterministic redirect called
      expect(mockRedirect).toHaveBeenCalledTimes(1)

      // 4. REGRESSION CHECK: Subsequent request after failed refresh must NEVER send stale bearer token
      adapterMock.mockResolvedValueOnce({
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {},
        data: { message: 'public response' }
      })

      await api.get('/public-courses')

      const secondCallConfig = adapterMock.mock.calls[1][0]
      const headerSent = secondCallConfig.headers?.Authorization || (secondCallConfig.headers?.get && secondCallConfig.headers.get('Authorization'))
      expect(headerSent).toBeUndefined()
    })
  })

  describe('Response Interceptor - Concurrent 401 Handling', () => {
    it('triggers only ONE refresh call for concurrent 401s and resolves all queued requests on success', async () => {
      authStore.setTokens('token-to-expire')

      let resolveRefresh
      const refreshPromise = new Promise((resolve) => {
        resolveRefresh = resolve
      })

      const postSpy = vi.spyOn(axios, 'post').mockImplementation(() => refreshPromise)

      // 3 calls fail with 401
      adapterMock
        .mockRejectedValueOnce(create401Error(null, '/req1'))
        .mockRejectedValueOnce(create401Error(null, '/req2'))
        .mockRejectedValueOnce(create401Error(null, '/req3'))
        // 3 retry calls succeed
        .mockResolvedValueOnce({ status: 200, data: { name: 'req1-success' } })
        .mockResolvedValueOnce({ status: 200, data: { name: 'req2-success' } })
        .mockResolvedValueOnce({ status: 200, data: { name: 'req3-success' } })

      // Dispatch 3 concurrent requests
      const p1 = api.get('/req1')
      const p2 = api.get('/req2')
      const p3 = api.get('/req3')

      // Resolve refresh token
      resolveRefresh({
        data: {
          code: 1000,
          result: { accessToken: 'concurrent-refreshed-token' }
        }
      })

      const [r1, r2, r3] = await Promise.all([p1, p2, p3])

      expect(r1.data.name).toBe('req1-success')
      expect(r2.data.name).toBe('req2-success')
      expect(r3.data.name).toBe('req3-success')

      // Exactly ONE refresh request made
      expect(postSpy).toHaveBeenCalledTimes(1)
      expect(authStore.accessToken).toBe('concurrent-refreshed-token')
    })

    it('rejects all queued concurrent requests and clears auth when refresh fails', async () => {
      authStore.setTokens('dying-token')

      let rejectRefresh
      const refreshPromise = new Promise((_, reject) => {
        rejectRefresh = reject
      })

      const postSpy = vi.spyOn(axios, 'post').mockImplementation(() => refreshPromise)

      adapterMock
        .mockRejectedValueOnce(create401Error(null, '/batch1'))
        .mockRejectedValueOnce(create401Error(null, '/batch2'))
        .mockRejectedValueOnce(create401Error(null, '/batch3'))

      const p1 = api.get('/batch1')
      const p2 = api.get('/batch2')
      const p3 = api.get('/batch3')

      rejectRefresh(new Error('Network refresh error'))

      await expect(p1).rejects.toThrow('Network refresh error')
      await expect(p2).rejects.toThrow('Network refresh error')
      await expect(p3).rejects.toThrow('Network refresh error')

      expect(postSpy).toHaveBeenCalledTimes(1)
      expect(authStore.accessToken).toBeNull()
      expect(api.defaults.headers.common['Authorization']).toBeUndefined()
      expect(mockRedirect).toHaveBeenCalledTimes(1)
    })
  })

  describe('Logout Regression - No Stale Token Sent', () => {
    it('ensures no stale bearer token is sent after logout and clearAuth', async () => {
      authStore.setTokens('user-logged-in-token')
      expect(api.defaults.headers.common['Authorization']).toBe('Bearer user-logged-in-token')

      // Simulate logout sequence: clearAuth
      authStore.clearAuth()

      expect(authStore.accessToken).toBeNull()
      expect(api.defaults.headers.common['Authorization']).toBeUndefined()

      adapterMock.mockResolvedValueOnce({
        status: 200,
        statusText: 'OK',
        headers: {},
        config: {},
        data: { guest: true }
      })

      await api.get('/courses')

      expect(adapterMock).toHaveBeenCalledTimes(1)
      const callConfig = adapterMock.mock.calls[0][0]
      const headerSent = callConfig.headers?.Authorization || (callConfig.headers?.get && callConfig.headers.get('Authorization'))
      expect(headerSent).toBeUndefined()
    })
  })
})
