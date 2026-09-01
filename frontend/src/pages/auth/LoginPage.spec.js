import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import LoginPage from './LoginPage.vue'
import { createTestingPinia } from '@pinia/testing'
import { AuthService } from '@/services/auth.service'

// Mock vue-router
const mockPush = vi.fn()
const mockReplace = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
    replace: mockReplace
  }),
  useRoute: () => ({
    query: {}
  })
}))

// Mock AuthService
vi.mock('@/services/auth.service', () => ({
  AuthService: {
    login: vi.fn(),
    getCurrentUser: vi.fn()
  }
}))

// ---- Helpers to create realistic Axios error shapes ----

/** Backend returns an ApiResponse with error code and message (e.g. 401 LOGIN_FAILED) */
function makeAxiosApiError(status, code, message) {
  return {
    isAxiosError: true,
    message: `Request failed with status code ${status}`,
    code: 'ERR_BAD_REQUEST',
    response: {
      status,
      statusText: status === 401 ? 'Unauthorized' : 'Bad Request',
      data: { code, message, result: null }
    }
  }
}

/** Network error — no response at all (e.g. server down, DNS fail) */
function makeAxiosNetworkError() {
  return {
    isAxiosError: true,
    message: 'Network Error',
    code: 'ERR_NETWORK',
    response: undefined
  }
}

/** Timeout error — ECONNABORTED */
function makeAxiosTimeoutError() {
  return {
    isAxiosError: true,
    message: 'timeout of 5000ms exceeded',
    code: 'ECONNABORTED',
    response: undefined
  }
}

/** Server error (500) without backend ApiResponse body */
function makeAxiosServerError() {
  return {
    isAxiosError: true,
    message: 'Request failed with status code 500',
    code: 'ERR_BAD_RESPONSE',
    response: {
      status: 500,
      statusText: 'Internal Server Error',
      data: null
    }
  }
}

/** Rate limit error (429) with backend ApiResponse */
function makeAxiosRateLimitError() {
  return {
    isAxiosError: true,
    message: 'Request failed with status code 429',
    code: 'ERR_BAD_REQUEST',
    response: {
      status: 429,
      statusText: 'Too Many Requests',
      data: { code: 2009, message: 'Quá nhiều yêu cầu, vui lòng thử lại sau', result: null }
    }
  }
}

// ---- Mount helper ----
function mountLoginPage() {
  return mount(LoginPage, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn })],
      stubs: ['router-link']
    }
  })
}

async function fillAndSubmit(wrapper, email = 'test@example.com', password = 'password123') {
  await wrapper.find('input[type="email"]').setValue(email)
  await wrapper.find('input[type="password"]').setValue(password)
  await wrapper.find('form').trigger('submit.prevent')
  // flush microtasks so the async handleLogin settles
  await new Promise(resolve => setTimeout(resolve, 0))
}

// ============ Tests ============

describe('LoginPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders login form correctly', () => {
    const wrapper = mountLoginPage()
    expect(wrapper.find('h2').text()).toBe('Đăng nhập')
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
  })

  // ---- Backend ApiResponse errors ----

  it('displays backend ApiResponse error message (LOGIN_FAILED 2002)', async () => {
    AuthService.login.mockRejectedValue(
      makeAxiosApiError(401, 2002, 'Email hoặc mật khẩu không đúng')
    )

    const wrapper = mountLoginPage()
    await fillAndSubmit(wrapper)

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Email hoặc mật khẩu không đúng')
  })

  it('displays backend ApiResponse error message (ACCOUNT_LOCKED 2003)', async () => {
    AuthService.login.mockRejectedValue(
      makeAxiosApiError(403, 2003, 'Tài khoản đã bị khóa')
    )

    const wrapper = mountLoginPage()
    await fillAndSubmit(wrapper)

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Tài khoản đã bị khóa')
  })

  it('displays backend rate limit error (TOO_MANY_REQUESTS 2009)', async () => {
    AuthService.login.mockRejectedValue(makeAxiosRateLimitError())

    const wrapper = mountLoginPage()
    await fillAndSubmit(wrapper)

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Quá nhiều yêu cầu, vui lòng thử lại sau')
  })

  // ---- Network and infrastructure errors ----

  it('displays network error when server is unreachable', async () => {
    AuthService.login.mockRejectedValue(makeAxiosNetworkError())

    const wrapper = mountLoginPage()
    await fillAndSubmit(wrapper)

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Không thể kết nối đến máy chủ')
  })

  it('displays timeout error when request exceeds time limit', async () => {
    AuthService.login.mockRejectedValue(makeAxiosTimeoutError())

    const wrapper = mountLoginPage()
    await fillAndSubmit(wrapper)

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('quá hạn')
  })

  it('displays generic server error for 500 without ApiResponse body', async () => {
    AuthService.login.mockRejectedValue(makeAxiosServerError())

    const wrapper = mountLoginPage()
    await fillAndSubmit(wrapper)

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Lỗi hệ thống máy chủ')
  })

  // ---- Loading state ----

  it('disables submit button while loading', async () => {
    // Make login hang (never resolve) to test loading state
    AuthService.login.mockReturnValue(new Promise(() => {}))

    const wrapper = mountLoginPage()
    await wrapper.find('input[type="email"]').setValue('test@example.com')
    await wrapper.find('input[type="password"]').setValue('password123')
    await wrapper.find('form').trigger('submit.prevent')
    
    await new Promise(resolve => setTimeout(resolve, 0))

    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('Đang đăng nhập')
  })
})
