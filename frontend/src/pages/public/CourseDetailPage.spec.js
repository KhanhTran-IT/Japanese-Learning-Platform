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

vi.mock('@/services/order.service', () => ({
  OrderService: {
    createOrder: vi.fn(),
    getMyOrders: vi.fn(),
    getOrderById: vi.fn()
  }
}))

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/courses/:slug', name: 'CourseDetail', component: CourseDetailPage },
    { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } },
    { path: '/checkout/:orderId', name: 'Checkout', component: { template: '<div>Checkout</div>' } }
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

  it('uses backend isEnrolled field without extra API call', async () => {
    const courseData = {
      id: 1,
      title: 'Test Course',
      courseType: 'FREE',
      slug: 'test-course',
      isEnrolled: true,
      lastLessonId: 42
    }
    CourseService.getCourseBySlug.mockResolvedValue({ data: { code: 1000, result: courseData } })
    
    const wrapper = await mountComponent({
      auth: { accessToken: 'token', user: { roles: ['STUDENT'] } }
    })
    
    expect(wrapper.text()).toContain('Tiếp tục học')
    expect(wrapper.text()).toContain('Đã ghi danh')
    // Should NOT call getMyCourses since backend already provided enrollment info
    expect(StudentService.getMyCourses).not.toHaveBeenCalled()
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

  it('renders purchase CTA for paid course and creates order on click', async () => {
    const courseData = {
      id: 2,
      title: 'Paid N3 Course',
      courseType: 'PAID',
      originalPrice: 1000000,
      salePrice: 799000,
      slug: 'paid-n3-course'
    }
    const { OrderService } = await import('@/services/order.service')
    CourseService.getCourseBySlug.mockResolvedValue({ data: { code: 1000, result: courseData } })
    StudentService.getMyCourses.mockResolvedValue({ data: { code: 1000, result: [] } })
    OrderService.getMyOrders.mockResolvedValue({ data: { code: 1000, result: [] } })
    OrderService.createOrder.mockResolvedValue({ data: { code: 1000, result: { id: 202, orderCode: 'ORD-202' } } })

    const pushSpy = vi.spyOn(router, 'push')

    const wrapper = await mountComponent({
      auth: { accessToken: 'token', user: { roles: ['STUDENT'] } }
    })

    expect(wrapper.text()).toContain('Mua khóa học')
    const buyBtn = wrapper.findAll('button').find(b => b.text().includes('Mua khóa học'))
    expect(buyBtn).toBeTruthy()

    await buyBtn.trigger('click')
    await flushPromises()

    expect(OrderService.createOrder).toHaveBeenCalledWith(2)
    expect(pushSpy).toHaveBeenCalledWith('/checkout/202')
  })

  it('navigates to existing checkout when createOrder returns ORDER_DUPLICATE_PENDING (5004)', async () => {
    const courseData = {
      id: 3,
      title: 'Paid N2 Course',
      courseType: 'PAID',
      originalPrice: 1500000,
      slug: 'paid-n2-course'
    }
    const { OrderService } = await import('@/services/order.service')
    CourseService.getCourseBySlug.mockResolvedValue({ data: { code: 1000, result: courseData } })
    StudentService.getMyCourses.mockResolvedValue({ data: { code: 1000, result: [] } })
    OrderService.getMyOrders
      .mockResolvedValueOnce({ data: { code: 1000, result: [] } }) // initial check returns empty
      .mockResolvedValueOnce({
        data: {
          code: 1000,
          result: [
            {
              id: 303,
              status: 'PENDING',
              items: [{ courseId: 3, courseTitle: 'Paid N2 Course' }]
            }
          ]
        }
      }) // on 5004 recovery
    OrderService.createOrder.mockRejectedValue({
      response: {
        status: 409,
        data: { code: 5004, message: 'Bạn đang có đơn hàng chờ thanh toán cho khóa học này' }
      }
    })

    const pushSpy = vi.spyOn(router, 'push')

    const wrapper = await mountComponent({
      auth: { accessToken: 'token', user: { roles: ['STUDENT'] } }
    })

    const buyBtn = wrapper.findAll('button').find(b => b.text().includes('Mua khóa học'))
    await buyBtn.trigger('click')
    await flushPromises()

    expect(OrderService.getMyOrders).toHaveBeenCalled()
    expect(pushSpy).toHaveBeenCalledWith('/checkout/303')
  })

  it('detects existing pending order on mount and shows continue payment CTA', async () => {
    const courseData = {
      id: 4,
      title: 'Paid N1 Master',
      courseType: 'PAID',
      originalPrice: 2000000,
      slug: 'paid-n1-master'
    }
    const { OrderService } = await import('@/services/order.service')
    CourseService.getCourseBySlug.mockResolvedValue({ data: { code: 1000, result: courseData } })
    StudentService.getMyCourses.mockResolvedValue({ data: { code: 1000, result: [] } })
    OrderService.getMyOrders.mockResolvedValue({
      data: {
        code: 1000,
        result: [
          {
            id: 404,
            status: 'PENDING',
            items: [{ courseId: 4, courseTitle: 'Paid N1 Master' }]
          }
        ]
      }
    })

    const pushSpy = vi.spyOn(router, 'push')

    const wrapper = await mountComponent({
      auth: { accessToken: 'token', user: { roles: ['STUDENT'] } }
    })

    expect(wrapper.text()).toContain('Tiếp tục thanh toán')
    expect(wrapper.text()).toContain('Bạn có đơn hàng chờ thanh toán')

    const continueBtn = wrapper.findAll('button').find(b => b.text().includes('Tiếp tục thanh toán'))
    await continueBtn.trigger('click')

    expect(pushSpy).toHaveBeenCalledWith('/checkout/404')
  })
})
