import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import OrderHistoryPage from './OrderHistoryPage.vue'
import { createTestingPinia } from '@pinia/testing'
import { OrderService } from '@/services/order.service'
import { StudentService } from '@/services/student.service'
import { createRouter, createWebHistory } from 'vue-router'

vi.mock('@/services/order.service', () => ({
  OrderService: {
    getMyOrders: vi.fn(),
    getOrderById: vi.fn(),
    createOrder: vi.fn()
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
    { path: '/courses', name: 'Courses', component: { template: '<div>Courses</div>' } },
    { path: '/checkout/:orderId', name: 'Checkout', component: { template: '<div>Checkout</div>' } },
    { path: '/student/orders', name: 'StudentOrders', component: OrderHistoryPage },
    { path: '/student/my-courses', name: 'StudentMyCourses', component: { template: '<div>My Courses</div>' } },
    { path: '/student/lessons/:id', name: 'LessonLearning', component: { template: '<div>Lesson</div>' } }
  ]
})

describe('OrderHistoryPage.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const mountComponent = async () => {
    const pinia = createTestingPinia({ createSpy: vi.fn })
    const wrapper = mount(OrderHistoryPage, {
      global: {
        plugins: [pinia, router]
      }
    })
    await flushPromises()
    return wrapper
  }

  it('displays loading state initially', async () => {
    OrderService.getMyOrders.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))

    const pinia = createTestingPinia({ createSpy: vi.fn })
    const wrapper = mount(OrderHistoryPage, {
      global: { plugins: [pinia, router] }
    })

    expect(wrapper.find('.animate-spin').exists()).toBe(true)
    expect(wrapper.text()).toContain('Đang tải lịch sử đơn hàng...')
  })

  it('displays error state with friendly message and retry button', async () => {
    OrderService.getMyOrders.mockRejectedValueOnce({
      isAxiosError: true,
      message: 'Request failed with status code 500',
      response: { status: 500, data: { message: 'Máy chủ đang bảo trì.' } }
    })

    const wrapper = await mountComponent()

    expect(wrapper.text()).toContain('Không thể tải danh sách đơn hàng')
    expect(wrapper.text()).toContain('Máy chủ đang bảo trì.')

    const retryBtn = wrapper.find('button')
    expect(retryBtn.text()).toContain('Thử lại')

    // Mock successful retry
    OrderService.getMyOrders.mockResolvedValueOnce({
      data: { code: 1000, result: [] }
    })
    await retryBtn.trigger('click')
    await flushPromises()

    expect(OrderService.getMyOrders).toHaveBeenCalledTimes(2)
  })

  it('displays empty state when student has no orders', async () => {
    OrderService.getMyOrders.mockResolvedValue({
      data: { code: 1000, result: [] }
    })

    const wrapper = await mountComponent()

    expect(wrapper.text()).toContain('Bạn chưa có đơn hàng nào')
    expect(wrapper.text()).toContain('Khám phá các khóa học ngay')
  })

  it('renders order list with server-authoritative details and status badges', async () => {
    const mockOrders = [
      {
        id: 201,
        orderCode: 'ORD-20261030-001',
        status: 'PENDING',
        totalAmount: 600000,
        discountAmount: 100000,
        finalAmount: 500000,
        createdAt: '2026-10-30T09:00:00',
        items: [
          { id: 1, courseId: 10, courseTitle: 'Khóa học N3 Cấp Tốc', price: 600000, quantity: 1 }
        ]
      },
      {
        id: 202,
        orderCode: 'ORD-20261030-002',
        status: 'PAID',
        totalAmount: 800000,
        discountAmount: 0,
        finalAmount: 800000,
        createdAt: '2026-10-30T10:00:00',
        items: [
          { id: 2, courseId: 11, courseTitle: 'Khóa học N2 Kanji Master', price: 800000, quantity: 1 }
        ]
      }
    ]

    OrderService.getMyOrders.mockResolvedValue({
      data: { code: 1000, result: mockOrders }
    })

    const wrapper = await mountComponent()

    expect(wrapper.text()).toContain('ORD-20261030-001')
    expect(wrapper.text()).toContain('Khóa học N3 Cấp Tốc')
    expect(wrapper.text()).toContain('Chờ thanh toán')
    expect(wrapper.text()).toMatch(/500[.,]000đ/)

    expect(wrapper.text()).toContain('ORD-20261030-002')
    expect(wrapper.text()).toContain('Khóa học N2 Kanji Master')
    expect(wrapper.text()).toContain('Đã thanh toán')
    expect(wrapper.text()).toMatch(/800[.,]000đ/)
  })

  it('filters orders by tab (ALL, PENDING, PAID, OTHER)', async () => {
    const mockOrders = [
      { id: 1, orderCode: 'ORD-1', status: 'PENDING', finalAmount: 100000, items: [] },
      { id: 2, orderCode: 'ORD-2', status: 'PAID', finalAmount: 200000, items: [] },
      { id: 3, orderCode: 'ORD-3', status: 'CANCELLED', finalAmount: 300000, items: [] }
    ]

    OrderService.getMyOrders.mockResolvedValue({
      data: { code: 1000, result: mockOrders }
    })

    const wrapper = await mountComponent()

    // Default: ALL shows all 3
    expect(wrapper.text()).toContain('ORD-1')
    expect(wrapper.text()).toContain('ORD-2')
    expect(wrapper.text()).toContain('ORD-3')

    // Click PENDING tab
    const pendingTab = wrapper.findAll('button').find(b => b.text().includes('Chờ thanh toán'))
    await pendingTab.trigger('click')
    expect(wrapper.text()).toContain('ORD-1')
    expect(wrapper.text()).not.toContain('ORD-2')
    expect(wrapper.text()).not.toContain('ORD-3')

    // Click PAID tab
    const paidTab = wrapper.findAll('button').find(b => b.text().includes('Đã thanh toán'))
    await paidTab.trigger('click')
    expect(wrapper.text()).not.toContain('ORD-1')
    expect(wrapper.text()).toContain('ORD-2')
    expect(wrapper.text()).not.toContain('ORD-3')

    // Click OTHER tab
    const otherTab = wrapper.findAll('button').find(b => b.text().includes('Khác'))
    await otherTab.trigger('click')
    expect(wrapper.text()).not.toContain('ORD-1')
    expect(wrapper.text()).not.toContain('ORD-2')
    expect(wrapper.text()).toContain('ORD-3')
  })

  it('provides recovery action to resume checkout for PENDING orders', async () => {
    const mockOrders = [
      {
        id: 301,
        orderCode: 'ORD-PENDING-301',
        status: 'PENDING',
        finalAmount: 450000,
        items: [{ id: 1, courseId: 10, courseTitle: 'N4 Starter', price: 450000 }]
      }
    ]

    OrderService.getMyOrders.mockResolvedValue({
      data: { code: 1000, result: mockOrders }
    })

    const wrapper = await mountComponent()

    const resumeLink = wrapper.find('a[href="/checkout/301"]')
    expect(resumeLink.exists()).toBe(true)
    expect(resumeLink.text()).toContain('Tiếp tục thanh toán')
  })

  it('provides safe learning navigation for PAID orders', async () => {
    const pushSpy = vi.spyOn(router, 'push')
    const mockOrders = [
      {
        id: 401,
        orderCode: 'ORD-PAID-401',
        status: 'PAID',
        finalAmount: 500000,
        items: [{ id: 1, courseId: 99, courseTitle: 'N1 Masterclass', price: 500000 }]
      }
    ]

    OrderService.getMyOrders.mockResolvedValue({
      data: { code: 1000, result: mockOrders }
    })

    StudentService.getMyCourses.mockResolvedValue({
      data: {
        code: 1000,
        result: [{ courseId: 99, lastLessonId: 123 }]
      }
    })

    const wrapper = await mountComponent()

    const learnBtn = wrapper.findAll('button').find(b => b.text().includes('Vào học ngay'))
    expect(learnBtn).toBeTruthy()
    await learnBtn.trigger('click')
    await flushPromises()

    expect(pushSpy).toHaveBeenCalledWith('/student/lessons/123')
  })

  it('renders non-actionable explanations and no payment/refund buttons for terminal statuses', async () => {
    const mockOrders = [
      { id: 501, orderCode: 'ORD-CAN', status: 'CANCELLED', finalAmount: 100000, items: [] },
      { id: 502, orderCode: 'ORD-FAIL', status: 'FAILED', finalAmount: 200000, items: [] },
      { id: 503, orderCode: 'ORD-EXP', status: 'EXPIRED', finalAmount: 300000, items: [] },
      { id: 504, orderCode: 'ORD-REF', status: 'REFUNDED', finalAmount: 400000, items: [] }
    ]

    OrderService.getMyOrders.mockResolvedValue({
      data: { code: 1000, result: mockOrders }
    })

    const wrapper = await mountComponent()

    // Assert localized status labels
    expect(wrapper.text()).toContain('Đã hủy')
    expect(wrapper.text()).toContain('Thất bại')
    expect(wrapper.text()).toContain('Hết hạn')
    expect(wrapper.text()).toContain('Đã hoàn tiền')

    // Assert terminal explanations
    expect(wrapper.text()).toContain('Đơn hàng đã hủy')
    expect(wrapper.text()).toContain('Thanh toán không thành công')
    expect(wrapper.text()).toContain('Đơn hàng quá hạn')
    expect(wrapper.text()).toContain('Đã hoàn tiền')

    // Ensure NO payment or checkout action buttons exist for these terminal orders
    expect(wrapper.findAll('a[href*="/checkout"]').length).toBe(0)

    // Ensure NO unsupported actions like cancel, refund, pay
    const buttonTexts = wrapper.findAll('button').map(b => b.text())
    expect(buttonTexts.some(t => t.includes('Hủy đơn') || t.includes('Hoàn tiền') || t.includes('Thanh toán'))).toBe(false)
  })

  it('copies order code to clipboard when clicking copy button', async () => {
    const writeTextSpy = vi.fn().mockResolvedValue(undefined)
    Object.assign(navigator, {
      clipboard: {
        writeText: writeTextSpy
      }
    })

    const mockOrders = [
      { id: 601, orderCode: 'ORD-COPY-ME', status: 'PENDING', finalAmount: 200000, items: [] }
    ]

    OrderService.getMyOrders.mockResolvedValue({
      data: { code: 1000, result: mockOrders }
    })

    const wrapper = await mountComponent()

    const copyBtn = wrapper.findAll('button').find(b => b.text().includes('Sao chép'))
    expect(copyBtn).toBeTruthy()
    await copyBtn.trigger('click')
    await flushPromises()

    expect(writeTextSpy).toHaveBeenCalledWith('ORD-COPY-ME')
    expect(wrapper.text()).toContain('Đã sao chép')
  })
})
