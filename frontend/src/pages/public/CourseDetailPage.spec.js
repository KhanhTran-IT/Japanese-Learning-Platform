import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import CourseDetailPage from './CourseDetailPage.vue'
import { createTestingPinia } from '@pinia/testing'
import { useAuthStore } from '@/stores/auth.store'
import { CourseService } from '@/services/course.service'
import { StudentService } from '@/services/student.service'
import { createRouter, createWebHistory } from 'vue-router'

vi.mock('@/services/course.service', () => ({
  CourseService: {
    getCourseBySlug: vi.fn(),
    enrollFreeCourse: vi.fn()
  }
}))

vi.mock('@/services/student.service', () => ({
  StudentService: {
    getMyCourses: vi.fn()
  }
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/courses/:slug', name: 'CourseDetail', component: CourseDetailPage },
    { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } }
  ]
})

describe('CourseDetailPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    router.currentRoute.value.params = { slug: 'test-course' }
  })

  const mountComponent = async (initialState = {}) => {
    const pinia = createTestingPinia({
      createSpy: vi.fn,
      initialState
    })
    
    const wrapper = mount(CourseDetailPage, {
      global: {
        plugins: [pinia, router],
        stubs: {
          RouterLink: true
        }
      }
    })
    
    // Wait for initial fetch
    await flushPromises()
    return wrapper
  }

  it('displays loading state initially', async () => {
    // delay resolution to see loading state
    CourseService.getCourseBySlug.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))
    
    const pinia = createTestingPinia({ createSpy: vi.fn })
    const wrapper = mount(CourseDetailPage, {
      global: { plugins: [pinia, router], stubs: { RouterLink: true } }
    })
    
    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('displays error state on fetch failure', async () => {
    CourseService.getCourseBySlug.mockRejectedValue(new Error('Network error'))
    const wrapper = await mountComponent()
    
    expect(wrapper.text()).toContain('Đã xảy ra lỗi')
    expect(wrapper.text()).toContain('Thử lại')
  })

  it('renders guest view correctly (login to learn)', async () => {
    const courseData = {
      id: 1,
      title: 'Test Course',
      courseType: 'FREE',
      slug: 'test-course'
    }
    CourseService.getCourseBySlug.mockResolvedValue({ data: { code: 1000, result: courseData } })
    
    const wrapper = await mountComponent({
      auth: { accessToken: null, user: null }
    })
    
    expect(wrapper.text()).toContain('Test Course')
    expect(wrapper.text()).toContain('Đăng nhập để học')
  })

  it('renders enrolled view correctly (continue learning)', async () => {
    const courseData = {
      id: 1,
      title: 'Test Course',
      courseType: 'FREE',
      slug: 'test-course'
    }
    CourseService.getCourseBySlug.mockResolvedValue({ data: { code: 1000, result: courseData } })
    StudentService.getMyCourses.mockResolvedValue({ data: { code: 1000, result: [{ courseId: 1 }] } })
    
    const wrapper = await mountComponent({
      auth: { accessToken: 'token', user: { roles: ['STUDENT'] } }
    })
    
    expect(wrapper.text()).toContain('Tiếp tục học')
    expect(wrapper.text()).toContain('Đã ghi danh')
  })

  it('allows authenticated student to enroll in free course', async () => {
    const courseData = {
      id: 1,
      title: 'Test Course',
      courseType: 'FREE',
      slug: 'test-course'
    }
    CourseService.getCourseBySlug.mockResolvedValue({ data: { code: 1000, result: courseData } })
    StudentService.getMyCourses.mockResolvedValue({ data: { code: 1000, result: [] } }) // Not enrolled
    CourseService.enrollFreeCourse.mockResolvedValue({ data: { code: 1000 } })
    
    const wrapper = await mountComponent({
      auth: { accessToken: 'token', user: { roles: ['STUDENT'] } }
    })
    
    expect(wrapper.text()).toContain('Đăng ký miễn phí')
    
    // Find the enroll button and click it
    const buttons = wrapper.findAll('button')
    const enrollBtn = buttons.find(b => b.text().includes('Đăng ký miễn phí'))
    expect(enrollBtn).toBeTruthy()
    
    await enrollBtn.trigger('click')
    
    expect(CourseService.enrollFreeCourse).toHaveBeenCalledWith(1)
    
    // Wait for the async enroll method to finish
    await flushPromises()
    
    expect(wrapper.text()).toContain('Ghi danh thành công')
    expect(wrapper.text()).toContain('Tiếp tục học')
  })
})
