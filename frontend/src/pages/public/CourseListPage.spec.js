import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import CourseListPage from './CourseListPage.vue'
import { CourseService } from '@/services/course.service'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/courses/:slug', name: 'CourseDetail', component: { template: '<div>Course Detail</div>' } }
  ]
})

vi.mock('@/services/course.service', () => ({
  CourseService: {
    getCourses: vi.fn()
  }
}))

describe('CourseListPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    window.scrollTo = vi.fn()
  })

  it('displays loading state initially', async () => {
    CourseService.getCourses.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))
    const wrapper = mount(CourseListPage, {
      global: { plugins: [router] }
    })
    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('displays error state if fetch fails', async () => {
    CourseService.getCourses.mockRejectedValue(new Error('Network Error'))
    const wrapper = mount(CourseListPage, {
      global: { plugins: [router] }
    })
    await flushPromises()
    expect(wrapper.text()).toContain('Đã xảy ra lỗi')
    expect(wrapper.text()).toContain('Network Error')
  })

  it('displays empty state if no courses found', async () => {
    CourseService.getCourses.mockResolvedValue({
      data: { code: 1000, result: { content: [], number: 0, totalPages: 0, totalElements: 0 } }
    })
    const wrapper = mount(CourseListPage, {
      global: { plugins: [router] }
    })
    await flushPromises()
    expect(wrapper.text()).toContain('Không tìm thấy khóa học')
  })

  it('displays course list and pagination correctly', async () => {
    CourseService.getCourses.mockResolvedValue({
      data: { 
        code: 1000, 
        result: { 
          content: [
            { id: 1, title: 'N5 Course', level: 'N5', courseType: 'FREE', originalPrice: 0, slug: 'n5-course' },
            { id: 2, title: 'N4 Course', level: 'N4', courseType: 'PAID', originalPrice: 100000, slug: 'n4-course' }
          ], 
          number: 0, 
          totalPages: 2, 
          totalElements: 14 
        } 
      }
    })
    const wrapper = mount(CourseListPage, {
      global: { plugins: [router] }
    })
    await flushPromises()
    
    // Check titles
    expect(wrapper.text()).toContain('N5 Course')
    expect(wrapper.text()).toContain('N4 Course')
    
    // Check tags
    expect(wrapper.text()).toContain('Miễn phí')
    expect(wrapper.text()).toContain('Trả phí')

    // Check pagination
    expect(wrapper.text()).toContain('Trang 1 / 2')
  })

  it('applies filters correctly', async () => {
    CourseService.getCourses.mockResolvedValue({
      data: { code: 1000, result: { content: [], number: 0, totalPages: 0, totalElements: 0 } }
    })
    const wrapper = mount(CourseListPage, {
      global: { plugins: [router] }
    })
    await flushPromises()
    
    // Click N5 filter
    const n5Button = wrapper.findAll('button').find(b => b.text() === 'N5')
    await n5Button.trigger('click')
    
    expect(CourseService.getCourses).toHaveBeenCalledWith(expect.objectContaining({
      level: 'N5'
    }))
  })

  it('meets basic accessibility requirements', async () => {
    CourseService.getCourses.mockResolvedValue({
      data: { code: 1000, result: { content: [
        { id: 1, title: 'Test', thumbnailUrl: 'test.png', slug: 'test' }
      ], number: 0, totalPages: 2, totalElements: 1 } }
    })
    const wrapper = mount(CourseListPage, {
      global: { plugins: [router] }
    })
    await flushPromises()
    
    // Check if pagination buttons have aria-labels
    const prevBtn = wrapper.find('button[aria-label="Previous page"]')
    const nextBtn = wrapper.find('button[aria-label="Next page"]')
    expect(prevBtn.exists()).toBe(true)
    expect(nextBtn.exists()).toBe(true)

    // Check if image has alt text
    const img = wrapper.find('img')
    expect(img.attributes('alt')).toBe('Test')

    // Check keyboard focus styles
    const courseLink = wrapper.find('.group')
    expect(courseLink.classes()).toContain('focus-visible:ring-2')
  })
})
