import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
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

/** Network error — no response at all */
function makeAxiosNetworkError() {
  return {
    isAxiosError: true,
    message: 'Network Error',
    code: 'ERR_NETWORK',
    response: undefined
  }
}

/** Timeout error */
function makeAxiosTimeoutError() {
  return {
    isAxiosError: true,
    message: 'timeout of 5000ms exceeded',
    code: 'ECONNABORTED',
    response: undefined
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
  // flush microtasks
  await new Promise(resolve => setTimeout(resolve, 0))
}

// ============ Tests ============

describe('RegisterPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders register form correctly', () => {
    const wrapper = mountRegisterPage()
    expect(wrapper.find('h2').text()).toBe('Đăng ký tài khoản')
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    const passwords = wrapper.findAll('input[type="password"]')
    expect(passwords.length).toBe(2)
  })

  // ---- Client-side validation ----

  it('displays error message on password mismatch (client-side)', async () => {
    const wrapper = mountRegisterPage()
    await fillAndSubmit(wrapper, { password: 'password123', confirmPassword: 'differentPassword' })

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Mật khẩu xác nhận không khớp')
    expect(AuthService.register).not.toHaveBeenCalled()
  })

  // ---- Backend ApiResponse errors ----

  it('displays backend error for duplicate email (EMAIL_ALREADY_EXISTS 2001)', async () => {
    AuthService.register.mockRejectedValue(
      makeAxiosApiError(409, 2001, 'Email đã tồn tại')
    )

    const wrapper = mountRegisterPage()
    await fillAndSubmit(wrapper)

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Email đã tồn tại')
  })

  it('displays backend validation error (VALIDATION_ERROR 1005)', async () => {
    AuthService.register.mockRejectedValue(
      makeAxiosApiError(400, 1005, 'password: Mật khẩu phải có ít nhất 8 ký tự')
    )

    const wrapper = mountRegisterPage()
    await fillAndSubmit(wrapper)

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Mật khẩu phải có ít nhất 8 ký tự')
  })

  // ---- Network and infrastructure errors ----

  it('displays network error when server is unreachable', async () => {
    AuthService.register.mockRejectedValue(makeAxiosNetworkError())

    const wrapper = mountRegisterPage()
    await fillAndSubmit(wrapper)

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Không thể kết nối đến máy chủ')
  })

  it('displays timeout error when request exceeds time limit', async () => {
    AuthService.register.mockRejectedValue(makeAxiosTimeoutError())

    const wrapper = mountRegisterPage()
    await fillAndSubmit(wrapper)

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('quá hạn')
  })

  it('displays generic unauthorized message for 401 without ApiResponse body', async () => {
    AuthService.register.mockRejectedValue(makeAxiosUnauthorizedError())

    const wrapper = mountRegisterPage()
    await fillAndSubmit(wrapper)

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Không có quyền truy cập')
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

    await new Promise(resolve => setTimeout(resolve, 0))

    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('Đang xử lý')
  })
})
