import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
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

function makeAxiosNetworkError() {
  return {
    isAxiosError: true,
    message: 'Network Error',
    code: 'ERR_NETWORK',
    response: undefined,
    request: {}
  }
}

function makeAxiosTimeoutError() {
  return {
    isAxiosError: true,
    message: 'timeout of 5000ms exceeded',
    code: 'ECONNABORTED',
    response: undefined,
    request: {}
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
  await flushPromises()
}

// ============ Tests ============

describe('LoginPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders login form correctly', () => {
    const wrapper = mountLoginPage()
    expect(wrapper.find('h1').text()).toBe('Đăng nhập')
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
  })

  // ---- Backend ApiResponse errors ----

  it('displays backend error message for incorrect password (401)', async () => {
    AuthService.login.mockRejectedValue(
      makeAxiosApiError(401, 2002, 'Email hoặc mật khẩu không chính xác.')
    )

    const wrapper = mountLoginPage()
    await fillAndSubmit(wrapper)

    expect(wrapper.text()).toContain('Email hoặc mật khẩu không chính xác.')
  })

  it('displays validation errors under fields (422)', async () => {
    AuthService.login.mockRejectedValue({
      isAxiosError: true,
      response: {
        status: 422,
        data: {
          code: 1005,
          message: 'Validation failed',
          result: { email: 'Email không hợp lệ' }
        }
      }
    })

    const wrapper = mountLoginPage()
    await fillAndSubmit(wrapper)

    expect(wrapper.text()).toContain('Email không hợp lệ')
  })

  it('displays backend rate limit error (429)', async () => {
    AuthService.login.mockRejectedValue(makeAxiosRateLimitError())

    const wrapper = mountLoginPage()
    await fillAndSubmit(wrapper)

    expect(wrapper.text()).toContain('Bạn đã thử quá nhiều lần')
  })

  // ---- Network errors ----

  it('displays network error when server is unreachable', async () => {
    AuthService.login.mockRejectedValue(makeAxiosNetworkError())

    const wrapper = mountLoginPage()
    await fillAndSubmit(wrapper)

    expect(wrapper.text()).toContain('Không thể kết nối đến máy chủ')
  })

  // ---- Loading state ----

  it('disables submit button while loading', async () => {
    // Make login hang (never resolve) to test loading state
    AuthService.login.mockReturnValue(new Promise(() => {}))

    const wrapper = mountLoginPage()
    await wrapper.find('input[type="email"]').setValue('test@example.com')
    await wrapper.find('input[type="password"]').setValue('password123')
    await wrapper.find('form').trigger('submit.prevent')
    
    await flushPromises()

    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('Đang xử lý')
  })
  
  // ---- Successful login ----
  
  it('redirects to student dashboard on successful login', async () => {
    AuthService.login.mockResolvedValue({
      data: { code: 1000, result: { accessToken: 'token123' } }
    })
    AuthService.getCurrentUser.mockResolvedValue({
      data: { code: 1000, result: { roles: ['STUDENT'] } }
    })
    
    const wrapper = mountLoginPage()
    await fillAndSubmit(wrapper)
    
    expect(mockPush).toHaveBeenCalledWith('/student/dashboard')
  })
})
