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

describe('RegisterPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders register form correctly', () => {
    const wrapper = mount(RegisterPage, {
      global: {
        stubs: ['router-link']
      }
    })
    expect(wrapper.find('h2').text()).toBe('Đăng ký tài khoản')
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    const passwords = wrapper.findAll('input[type="password"]')
    expect(passwords.length).toBe(2)
  })

  it('displays error message on password mismatch', async () => {
    const wrapper = mount(RegisterPage, {
      global: {
        stubs: ['router-link']
      }
    })

    await wrapper.find('input[type="text"]').setValue('Test User')
    await wrapper.find('input[type="email"]').setValue('test@example.com')
    
    const passwords = wrapper.findAll('input[type="password"]')
    await passwords[0].setValue('password123')
    await passwords[1].setValue('password456')
    
    await wrapper.find('form').trigger('submit.prevent')
    
    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Mật khẩu xác nhận không khớp')
    expect(AuthService.register).not.toHaveBeenCalled()
  })

  it('displays error message on failed registration API call', async () => {
    AuthService.register.mockRejectedValue({
      isAxiosError: true,
      message: 'Network Error',
      response: {
        data: { message: 'Email đã được sử dụng' }
      }
    })

    const wrapper = mount(RegisterPage, {
      global: {
        stubs: ['router-link']
      }
    })

    await wrapper.find('input[type="text"]').setValue('Test User')
    await wrapper.find('input[type="email"]').setValue('test@example.com')
    
    const passwords = wrapper.findAll('input[type="password"]')
    await passwords[0].setValue('password123')
    await passwords[1].setValue('password123')
    
    await wrapper.find('form').trigger('submit.prevent')
    
    await new Promise(resolve => setTimeout(resolve, 0))

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Email đã được sử dụng')
  })
})
