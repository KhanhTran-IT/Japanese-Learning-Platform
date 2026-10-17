import { describe, it, expect, vi, beforeEach } from 'vitest'
import { AuthService } from './auth.service'
import api from './api'

vi.mock('./api', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn()
  }
}))

describe('AuthService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('login sends credentials and relies on backend setting HttpOnly cookie', async () => {
    api.post.mockResolvedValueOnce({ data: { code: 1000, result: { accessToken: 'access-123' } } })

    const res = await AuthService.login('user@example.com', 'password123')

    expect(api.post).toHaveBeenCalledWith('/auth/login', {
      email: 'user@example.com',
      password: 'password123'
    })
    expect(res.data.result.accessToken).toBe('access-123')
    expect(res.data.result.refreshToken).toBeUndefined()
  })

  it('refreshToken calls /auth/refresh-token with empty body and withCredentials: true', async () => {
    api.post.mockResolvedValueOnce({ data: { code: 1000, result: { accessToken: 'new-access-123' } } })

    const res = await AuthService.refreshToken()

    expect(api.post).toHaveBeenCalledWith('/auth/refresh-token', {}, { withCredentials: true })
    expect(res.data.result.accessToken).toBe('new-access-123')
  })

  it('logout calls /auth/logout with withCredentials: true to clear cookie', async () => {
    api.post.mockResolvedValueOnce({ data: { code: 1000, result: null } })

    await AuthService.logout()

    expect(api.post).toHaveBeenCalledWith('/auth/logout', {}, { withCredentials: true })
  })
})
