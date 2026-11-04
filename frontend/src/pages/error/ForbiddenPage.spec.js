import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ForbiddenPage from './ForbiddenPage.vue'
import { createTestingPinia } from '@pinia/testing'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

describe('ForbiddenPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const mountForbiddenPage = (userRoles = ['STUDENT']) => {
    return mount(ForbiddenPage, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: {
              auth: {
                user: { id: 1, fullName: 'Test User', roles: userRoles },
                accessToken: 'test-token',
                initialized: true
              }
            }
          })
        ]
      }
    })
  }

  it('renders 403 Forbidden status, icon, and title correctly', () => {
    const wrapper = mountForbiddenPage(['STUDENT'])

    expect(wrapper.text()).toContain('403 - Forbidden')
    expect(wrapper.text()).toContain('Truy cập bị từ chối')
    expect(wrapper.text()).toContain('Tài khoản của bạn không có quyền hạn cần thiết')
  })

  it('displays user current role information for STUDENT', () => {
    const wrapper = mountForbiddenPage(['STUDENT'])
    expect(wrapper.text()).toContain('Học viên (Student)')
  })

  it('displays user current role information for TEACHER', () => {
    const wrapper = mountForbiddenPage(['TEACHER'])
    expect(wrapper.text()).toContain('Giảng viên (Teacher)')
  })

  it('displays user current role information for ADMIN', () => {
    const wrapper = mountForbiddenPage(['ADMIN'])
    expect(wrapper.text()).toContain('Quản trị viên (Admin)')
  })

  it('navigates to role-appropriate dashboard when clicking "Về trang của bạn"', async () => {
    // 1. STUDENT -> /student/dashboard
    const studentWrapper = mountForbiddenPage(['STUDENT'])
    await studentWrapper.find('.btn-dashboard').trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/student/dashboard')

    // 2. TEACHER -> /admin/courses
    const teacherWrapper = mountForbiddenPage(['TEACHER'])
    await teacherWrapper.find('.btn-dashboard').trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/admin/courses')

    // 3. ADMIN -> /admin/dashboard
    const adminWrapper = mountForbiddenPage(['ADMIN'])
    await adminWrapper.find('.btn-dashboard').trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/admin/dashboard')
  })

  it('navigates to home ("/") when clicking "Trang chủ"', async () => {
    const wrapper = mountForbiddenPage(['STUDENT'])
    await wrapper.find('.btn-home').trigger('click')
    expect(mockPush).toHaveBeenCalledWith('/')
  })
})
