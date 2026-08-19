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

describe('LoginPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders login form correctly', () => {
    const wrapper = mount(LoginPage, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })],
        stubs: ['router-link']
      }
    })
    expect(wrapper.find('h2').text()).toBe('Đăng nhập')
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
  })

  it('displays error message on failed login', async () => {
    // Make login reject with an error
    AuthService.login.mockRejectedValue({
      response: {
        data: { message: 'Thông tin đăng nhập không chính xác' }
      }
    })

    const wrapper = mount(LoginPage, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })],
        stubs: ['router-link']
      }
    })

    await wrapper.find('input[type="email"]').setValue('test@example.com')
    await wrapper.find('input[type="password"]').setValue('wrongpassword')
    await wrapper.find('form').trigger('submit.prevent')
    
    // Wait for async operations to complete
    await new Promise(resolve => setTimeout(resolve, 0))

    const errorAlert = wrapper.find('.error-alert')
    expect(errorAlert.exists()).toBe(true)
    expect(errorAlert.text()).toContain('Thông tin đăng nhập không chính xác')
  })
})
