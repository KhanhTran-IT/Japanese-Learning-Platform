import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import RegisterPage from './RegisterPage.vue'
import { AuthService } from '@/services/auth.service'

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

// Mock AuthService
vi.mock('@/services/auth.service', () => ({
  AuthService: {
    register: vi.fn()
  }
}))

// ---- Helpers to create realistic Axios error shapes ----

/** Backend returns an ApiResponse with error code and message */
function makeAxiosApiError(status, code, message) {
  return {
    isAxiosError: true,
    message: `Request failed with status code ${status}`,
    code: 'ERR_BAD_REQUEST',
    response: {
      status,
      statusText: status === 409 ? 'Conflict' : 'Bad Request',
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

/** 401 Unauthorized without backend ApiResponse body */
function makeAxiosUnauthorizedError() {
  return {
    isAxiosError: true,
    message: 'Request failed with status code 401',
    code: 'ERR_BAD_REQUEST',
    response: {
      status: 401,
      statusText: 'Unauthorized',
      data: null
    }
  }
}

// ---- Mount helper ----
function mountRegisterPage() {
  return mount(RegisterPage, {
    global: {
      stubs: ['router-link']
    }
  })
}

async function fillAndSubmit(wrapper, { fullName = 'Test User', email = 'test@example.com', password = 'password123', confirmPassword = 'password123' } = {}) {
  await wrapper.find('input[type="text"]').setValue(fullName)
  await wrapper.find('input[type="email"]').setValue(email)
  const passwords = wrapper.findAll('input[type="password"]')
  await passwords[0].setValue(password)
  await passwords[1].setValue(confirmPassword)
  await wrapper.find('form').trigger('submit.prevent')
  await flushPromises()
}

// ============ Tests ============

describe('RegisterPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders register form correctly', () => {
    const wrapper = mountRegisterPage()
    expect(wrapper.find('h1').text()).toBe('Tạo tài khoản')
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    const passwords = wrapper.findAll('input[type="password"]')
    expect(passwords.length).toBe(2)
  })

  // ---- Client-side validation ----

  it('displays error message on password mismatch (client-side)', async () => {
    const wrapper = mountRegisterPage()
    await fillAndSubmit(wrapper, { password: 'password123', confirmPassword: 'differentPassword' })

    expect(wrapper.text()).toContain('Mật khẩu xác nhận không khớp')
    expect(AuthService.register).not.toHaveBeenCalled()
  })

  // ---- Backend ApiResponse errors ----

  it('displays backend error for duplicate email (409)', async () => {
    AuthService.register.mockRejectedValue(
      makeAxiosApiError(409, 2001, 'Email đã tồn tại')
    )

    const wrapper = mountRegisterPage()
    await fillAndSubmit(wrapper)

    expect(wrapper.text()).toContain('Email này đã được đăng ký.')
  })

  it('displays backend validation error under fields (422)', async () => {
    AuthService.register.mockRejectedValue({
      isAxiosError: true,
      response: {
        status: 422,
        data: {
          code: 1005,
          message: 'Validation failed',
          result: { password: 'Mật khẩu phải có ít nhất 8 ký tự' }
        }
      }
    })

    const wrapper = mountRegisterPage()
    await fillAndSubmit(wrapper)

    expect(wrapper.text()).toContain('Mật khẩu phải có ít nhất 8 ký tự')
  })

  // ---- Network and infrastructure errors ----

  it('displays network error when server is unreachable', async () => {
    AuthService.register.mockRejectedValue(makeAxiosNetworkError())

    const wrapper = mountRegisterPage()
    await fillAndSubmit(wrapper)

    expect(wrapper.text()).toContain('Không thể kết nối đến máy chủ')
  })

  // ---- Loading state ----

  it('disables submit button while loading', async () => {
    AuthService.register.mockReturnValue(new Promise(() => {}))

    const wrapper = mountRegisterPage()
    await wrapper.find('input[type="text"]').setValue('Test User')
    await wrapper.find('input[type="email"]').setValue('test@example.com')
    const passwords = wrapper.findAll('input[type="password"]')
    await passwords[0].setValue('password123')
    await passwords[1].setValue('password123')
    await wrapper.find('form').trigger('submit.prevent')

    await flushPromises()

    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('Đang xử lý')
  })
})
